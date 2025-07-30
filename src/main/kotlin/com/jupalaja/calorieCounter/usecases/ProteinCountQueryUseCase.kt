package com.jupalaja.calorieCounter.usecases

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.jupalaja.calorieCounter.domain.dto.MessageReceived
import com.jupalaja.calorieCounter.domain.dto.MessageResponse
import com.jupalaja.calorieCounter.domain.enums.MessageType
import com.jupalaja.calorieCounter.infra.input.ports.MessagingInputPort
import com.jupalaja.calorieCounter.infra.output.adapters.calorieNinjas.CalorieNinjasAdapter
import com.jupalaja.calorieCounter.infra.output.ports.AIModelProcessingPort
import com.jupalaja.calorieCounter.infra.output.ports.MessagingOutputPort
import com.jupalaja.calorieCounter.shared.constants.Messages.BLANK_TEXT_MESSAGE_ERROR
import com.jupalaja.calorieCounter.shared.constants.Messages.GENERAL_PROCESSING_ERROR
import com.jupalaja.calorieCounter.shared.constants.Messages.NATURAL_LANGUAGE_QUERY_PROMPT_TEMPLATE
import com.jupalaja.calorieCounter.shared.constants.Messages.NULL_VOICE_DATA_ERROR
import com.jupalaja.calorieCounter.shared.constants.Messages.PROTEIN_SUMMARY_PROMPT_TEMPLATE
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.text.DecimalFormat

@Service
class ProteinCountQueryUseCase(
    private val aiModelProcessingPort: AIModelProcessingPort,
    private val messagingOutputPort: MessagingOutputPort,
    private val calorieNinjasAdapter: CalorieNinjasAdapter,
    private val objectMapper: ObjectMapper,
) : MessagingInputPort {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun processMessage(event: MessageReceived) {
        try {
            when (event.messageType) {
                MessageType.TEXT -> this.processTextMessage(event)
                MessageType.VOICE -> this.processVoiceMessage(event)
            }
        } catch (e: Exception) {
            this.logger.error("[PROCESS_MESSAGE] Error processing message for chatId: {}", event.chatId, e)
            this.messagingOutputPort.sendMessage(MessageResponse(event.chatId, GENERAL_PROCESSING_ERROR))
        }
    }

    private fun processTextMessage(event: MessageReceived) {
        if (event.text.isNullOrBlank()) {
            this.logger.warn("[PROCESS_TEXT_MESSAGE] Received a text message event with null or blank text for chatId: {}", event.chatId)
            this.messagingOutputPort.sendMessage(MessageResponse(event.chatId, BLANK_TEXT_MESSAGE_ERROR))
            return
        }
        this.processQueryAndRespond(event.chatId, event.text)
    }

    private fun processVoiceMessage(event: MessageReceived) {
        if (event.data == null) {
            this.logger.warn("[PROCESS_VOICE_MESSAGE] Received a voice message event with null data for chatId: {}", event.chatId)
            this.messagingOutputPort.sendMessage(MessageResponse(event.chatId, NULL_VOICE_DATA_ERROR))
            return
        }
        val tempAudioFile = event.data.toFile()
        try {
            val transcribedText = this.aiModelProcessingPort.transcribeAudio(event.data)
            this.processQueryAndRespond(event.chatId, transcribedText)
        } finally {
            if (tempAudioFile.exists()) {
                tempAudioFile.delete()
            }
        }
    }

    private fun processQueryAndRespond(
        chatId: String,
        queryText: String,
    ) {
        val queryPrompt = NATURAL_LANGUAGE_QUERY_PROMPT_TEMPLATE.format(queryText)
        val processedQuery = this.aiModelProcessingPort.generateText(queryPrompt)

        val nutritionData = this.calorieNinjasAdapter.getNutritionInfo(processedQuery)

        val summaryPrompt = this.buildProteinSummaryPrompt(nutritionData)
        val responseText = this.aiModelProcessingPort.generateText(summaryPrompt)

        this.messagingOutputPort.sendMessage(MessageResponse(chatId, responseText))
    }

    private fun buildProteinSummaryPrompt(nutritionDataJson: String): String {
        var itemsListString: String
        var formattedTotalProtein: String
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.HALF_UP

        try {
            val nutritionData: JsonNode = this.objectMapper.readTree(nutritionDataJson)
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
            this.logger.error("[BUILD_PROTEIN_SUMMARY_PROMPT] Error parsing nutrition data JSON: {}", nutritionDataJson, e)
            itemsListString = ""
            formattedTotalProtein = "0"
        }

        val prompt = PROTEIN_SUMMARY_PROMPT_TEMPLATE.format(itemsListString, formattedTotalProtein)
        return prompt
    }
}
