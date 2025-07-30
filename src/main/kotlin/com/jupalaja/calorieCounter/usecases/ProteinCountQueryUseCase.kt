package com.jupalaja.calorieCounter.usecases

import com.jupalaja.calorieCounter.domain.dto.MessageReceived
import com.jupalaja.calorieCounter.domain.dto.MessageResponse
import com.jupalaja.calorieCounter.domain.enums.MessageType
import com.jupalaja.calorieCounter.infra.input.ports.MessagingInputPort
import com.jupalaja.calorieCounter.infra.output.adapters.calorieNinjas.CalorieNinjasAdapter
import com.jupalaja.calorieCounter.infra.output.ports.AIModelProcessingPort
import com.jupalaja.calorieCounter.infra.output.ports.MessagingOutputPort
import com.jupalaja.calorieCounter.shared.constants.MessageConstants.BLANK_TEXT_MESSAGE_ERROR
import com.jupalaja.calorieCounter.shared.constants.MessageConstants.GENERAL_PROCESSING_ERROR
import com.jupalaja.calorieCounter.shared.constants.MessageConstants.NULL_VOICE_DATA_ERROR
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ProteinCountQueryUseCase(
    private val aiModelProcessingPort: AIModelProcessingPort,
    private val messagingOutputPort: MessagingOutputPort,
    private val calorieNinjasAdapter: CalorieNinjasAdapter,
) : MessagingInputPort {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun processMessage(event: MessageReceived) {
        try {
            when (event.messageType) {
                MessageType.TEXT -> processTextMessage(event)
                MessageType.VOICE -> processVoiceMessage(event)
            }
        } catch (e: Exception) {
            logger.error("Error processing message for chatId: ${event.chatId}", e)
            messagingOutputPort.sendMessage(MessageResponse(event.chatId, GENERAL_PROCESSING_ERROR))
        }
    }

    private fun processTextMessage(event: MessageReceived) {
        if (event.text == null || event.text.isBlank()) {
            logger.warn("Received a text message event with null or blank text for chatId: ${event.chatId}")
            messagingOutputPort.sendMessage(MessageResponse(event.chatId, BLANK_TEXT_MESSAGE_ERROR))
            return
        }
        val processedQuery = aiModelProcessingPort.extractQueryFromNaturalLanguage(event.text)
        val nutritionData = calorieNinjasAdapter.getNutritionInfo(processedQuery)
        val responseText = aiModelProcessingPort.generateProteinSummary(nutritionData)

        messagingOutputPort.sendMessage(MessageResponse(event.chatId, responseText))
    }

    private fun processVoiceMessage(event: MessageReceived) {
        if (event.data == null) {
            logger.warn("Received a voice message event with null data for chatId: ${event.chatId}")
            messagingOutputPort.sendMessage(MessageResponse(event.chatId, NULL_VOICE_DATA_ERROR))
            return
        }
        val tempAudioFile = event.data.toFile()
        try {
            val transcribedText = aiModelProcessingPort.transcribeAudio(event.data)
            val processedQuery = aiModelProcessingPort.extractQueryFromNaturalLanguage(transcribedText)
            val nutritionData = calorieNinjasAdapter.getNutritionInfo(processedQuery)
            val responseText = aiModelProcessingPort.generateProteinSummary(nutritionData)

            messagingOutputPort.sendMessage(MessageResponse(event.chatId, responseText))
        } finally {
            if (tempAudioFile.exists()) {
                tempAudioFile.delete()
            }
        }
    }
}
