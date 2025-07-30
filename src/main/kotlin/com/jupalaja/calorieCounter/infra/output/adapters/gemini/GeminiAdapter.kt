package com.jupalaja.calorieCounter.infra.output.adapters.gemini

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.Part
import com.jupalaja.calorieCounter.infra.output.ports.AIModelProcessingPort
import com.jupalaja.calorieCounter.shared.constants.MessageConstants.NATURAL_LANGUAGE_QUERY_PROMPT_TEMPLATE
import com.jupalaja.calorieCounter.shared.constants.MessageConstants.PROTEIN_SUMMARY_PROMPT_TEMPLATE
import com.jupalaja.calorieCounter.utils.getMimeType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.RoundingMode
import java.nio.file.Path
import java.text.DecimalFormat

@Component
class GeminiAdapter(
    @Value("\${api.gemini.model}") private val modelName: String,
    @Value("\${api.gemini.key}") apiKey: String,
    private val objectMapper: ObjectMapper,
) : AIModelProcessingPort {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val geminiClient: Client = Client.builder().apiKey(apiKey).build()

    override fun extractQueryFromNaturalLanguage(naturalLanguageQuery: String): String {
        logger.info("[GET_QUERY_FROM_NATURAL_LANGUAGE] Processing natural language query: {}", naturalLanguageQuery)
        if (naturalLanguageQuery.isBlank()) {
            throw IllegalArgumentException("Query cannot be blank.")
        }
        val prompt = NATURAL_LANGUAGE_QUERY_PROMPT_TEMPLATE.format(naturalLanguageQuery)

        return try {
            val response = geminiClient.models.generateContent(modelName, prompt, null)
            val query = response.text()?.trim()
            logger.info("[GET_QUERY_FROM_NATURAL_LANGUAGE] Generated query from Gemini: {}", query)
            if (query.isNullOrBlank()) {
                throw IllegalStateException("Gemini API returned an empty or null response.")
            }
            query
        } catch (e: Exception) {
            logger.error("[GET_QUERY_FROM_NATURAL_LANGUAGE] Error communicating with Gemini API", e)
            throw RuntimeException("Error processing natural language query with Gemini API.", e)
        }
    }

    override fun generateProteinSummary(nutritionDataJson: String): String {
        logger.info("[GET_PROTEIN_SUMMARY] Generating summary for nutrition data: {}", nutritionDataJson)

        var itemsListString: String
        var formattedTotalProtein: String
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.HALF_UP

        try {
            val nutritionData: JsonNode = objectMapper.readTree(nutritionDataJson)
            val items = nutritionData.get("items")

            if (items == null || !items.isArray || items.isEmpty) {
                itemsListString = ""
                formattedTotalProtein = "0"
            } else {
                val totalProtein = items.sumOf { it.get("protein_g")?.asDouble() ?: 0.0 }
                formattedTotalProtein = df.format(totalProtein)

                itemsListString =
                    items.joinToString("\n") { item ->
                        val proteinG = item.get("protein_g")?.asDouble() ?: 0.0
                        "Item: ${item.get("name")?.asText() ?: "Unknown"}, Protein: ${df.format(proteinG)}g"
                    }
            }
        } catch (e: JsonProcessingException) {
            logger.error("[GET_PROTEIN_SUMMARY] Error parsing nutrition data JSON: $nutritionDataJson", e)
            itemsListString = ""
            formattedTotalProtein = "0"
        }

        val prompt = PROTEIN_SUMMARY_PROMPT_TEMPLATE.format(itemsListString, formattedTotalProtein)

        return try {
            val response = geminiClient.models.generateContent(modelName, prompt, null)
            val summary = response.text()?.trim()
            logger.info("[GET_PROTEIN_SUMMARY] Generated summary from Gemini: {}", summary)
            if (summary.isNullOrBlank()) {
                throw IllegalStateException("Gemini API returned an empty or null response for protein summary.")
            }
            summary
        } catch (e: Exception) {
            logger.error("[GET_PROTEIN_SUMMARY] Error communicating with Gemini API", e)
            throw RuntimeException("Error generating protein summary with Gemini API.", e)
        }
    }

    override fun transcribeAudio(filePath: Path): String {
        logger.info("[GET_TEXT_FROM_AUDIO] Transcribing audio from path: $filePath")
        val audioFile = filePath.toFile()
        val mimeType = audioFile.getMimeType()
        logger.info("[GET_TEXT_FROM_AUDIO] Detected mimeType for ${audioFile.name}: $mimeType")
        val audioBytes = audioFile.readBytes()

        if (audioBytes.isEmpty()) {
            throw IllegalArgumentException("Audio file is empty.")
        }

        val audioPart = Part.fromBytes(audioBytes, mimeType)
        val promptPart = Part.fromText("Transcribe this audio message.")
        val content = Content.fromParts(promptPart, audioPart)

        return try {
            val response = geminiClient.models.generateContent(modelName, content, null)
            val text = response.text()?.trim()
            logger.info("[GET_TEXT_FROM_AUDIO] Transcribed text from Gemini: {}", text)
            if (text.isNullOrBlank()) {
                throw IllegalStateException("Gemini API returned an empty or null response for audio transcription.")
            }
            text
        } catch (e: Exception) {
            logger.error("[GET_TEXT_FROM_AUDIO] Error communicating with Gemini API for audio transcription", e)
            throw RuntimeException("Error transcribing audio with Gemini API.", e)
        }
    }
}
