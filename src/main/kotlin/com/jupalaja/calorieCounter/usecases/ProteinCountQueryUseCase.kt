package com.jupalaja.calorieCounter.usecases

import com.jupalaja.calorieCounter.domain.dto.MessageReceived
import com.jupalaja.calorieCounter.domain.dto.MessageResponse
import com.jupalaja.calorieCounter.domain.enums.MessageType
import com.jupalaja.calorieCounter.infra.input.ports.MessagingInputPort
import com.jupalaja.calorieCounter.infra.output.adapters.calorieNinjas.CalorieNinjasAdapter
import com.jupalaja.calorieCounter.infra.output.ports.AIModelProcessingPort
import com.jupalaja.calorieCounter.infra.output.ports.MessagingOutputPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Base64

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
            messagingOutputPort.sendErrorMessage(event.chatId, "Sorry, an error occurred while processing your request.")
        }
    }

    private fun processTextMessage(event: MessageReceived) {
        val processedQuery = aiModelProcessingPort.extractQueryFromNaturalLanguage(event.message)
        val nutritionData = calorieNinjasAdapter.getNutritionInfo(processedQuery)
        val responseText = aiModelProcessingPort.generateProteinSummary(nutritionData)

        messagingOutputPort.sendMessage(MessageResponse(event.chatId, responseText))
    }

    private fun processVoiceMessage(event: MessageReceived) {
        val parts = event.message.split("|")
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid voice message format")
        }

        val audioBytes = Base64.getDecoder().decode(parts[0])
        val mimeType = parts[1]

        val transcribedText = aiModelProcessingPort.transcribeAudio(audioBytes, mimeType)
        val processedQuery = aiModelProcessingPort.extractQueryFromNaturalLanguage(transcribedText)
        val nutritionData = calorieNinjasAdapter.getNutritionInfo(processedQuery)
        val responseText = aiModelProcessingPort.generateProteinSummary(nutritionData)

        messagingOutputPort.sendMessage(MessageResponse(event.chatId, responseText))
    }
}
