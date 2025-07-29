package com.jupalaja.calorieCounter.services

import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.Part
import com.jupalaja.calorieCounter.domain.dto.calorieNinjas.NutritionResponseDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.text.DecimalFormat

@Service
class GeminiService(
    @Value("\${api.gemini.model}") private val modelName: String,
    private val geminiClient: Client,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val naturalLanguageQueryPromptTemplate = """
        Summarize the following user request into a simple query in English for a nutrition API.
        The user wants to know the nutritional information of some food.
        Extract the food items and quantities, and translate them to English.
        For example, if the user asks 'what are the calories in 3 boiled eggs and a slice of bread', the output should be '3 boiled eggs and a slice of bread'.
        If the user asks in another language, for example 'me comí 2 manzanas y una banana', the output should be '2 apples and a banana'.
        Another example, if the user asks 'me comi un pan grande, de media libra', the output should be 'half a pound of large bread'.
        Only return the query, with no other text, explanation, or formatting.
        The user request is: "%s"
    """.trimIndent()

    private val proteinSummaryPromptTemplate = """
        Generate a summary in Spanish for the provided list of foods and their protein content.
        The summary should start with "Claro, éste es el resumen".
        Then, list each food item with its protein content on a new line, using a bullet point. For example: "- La pechuga de pollo contiene 50g de proteína".
        After the list, add a blank line.
        Finally, add a sentence with the total protein amount. For example: "En total estarías consumiendo 60g de proteína".

        If the list of items is empty or the total protein is 0, the response should be "Los alimentos que proporcionaste no parecen contener proteínas."

        Here is the list of food items and their protein content:
        %s

        The total protein is %sg.

        Only return the final formatted text. Do not add any other explanations or formatting.
    """.trimIndent()

    fun getQueryFromNaturalLanguage(naturalLanguageQuery: String): String {
        logger.info("[GET_QUERY_FROM_NATURAL_LANGUAGE] Processing natural language query: {}", naturalLanguageQuery)
        if (naturalLanguageQuery.isBlank()) {
            throw IllegalArgumentException("Query cannot be blank.")
        }
        val prompt = naturalLanguageQueryPromptTemplate.format(naturalLanguageQuery)

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

    fun getProteinSummary(nutritionData: NutritionResponseDTO): String {
        logger.info("[GET_PROTEIN_SUMMARY] Generating summary for nutrition data: {}", nutritionData)

        if (nutritionData.items.isEmpty()) {
            return "Alimento no encontrado en la base de datos"
        }

        val totalProtein = nutritionData.items.sumOf { it.proteinG }

        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.HALF_UP
        val formattedTotalProtein = df.format(totalProtein)

        val itemsListString = nutritionData.items.joinToString("\n") {
            "Item: ${it.name}, Protein: ${df.format(it.proteinG)}g"
        }

        val prompt = proteinSummaryPromptTemplate.format(itemsListString, formattedTotalProtein)

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

    fun getTextFromAudio(audioBytes: ByteArray, mimeType: String): String {
        logger.info("[GET_TEXT_FROM_AUDIO] Transcribing audio of size: ${audioBytes.size} bytes and mimeType: $mimeType")
        if (audioBytes.isEmpty()) {
            throw IllegalArgumentException("Audio bytes cannot be empty.")
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
