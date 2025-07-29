package com.jupalaja.calorieCounter.application.usecases

import com.jupalaja.calorieCounter.domain.dto.messaging.MessageReceivedEvent
import com.jupalaja.calorieCounter.domain.dto.messaging.MessageResponseEvent
import com.jupalaja.calorieCounter.domain.enums.MessageType
import com.jupalaja.calorieCounter.infra.input.ports.MessagingInputPort
import com.jupalaja.calorieCounter.infra.input.ports.AudioProcessingPort
import com.jupalaja.calorieCounter.infra.output.ports.MessagingOutputPort
import com.jupalaja.calorieCounter.infra.output.ports.NaturalLanguageProcessingPort
import com.jupalaja.calorieCounter.services.ProteinService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class ProcessNutritionQueryUseCase(
    private val proteinService: ProteinService,
    private val naturalLanguageProcessingPort: NaturalLanguageProcessingPort,
    private val audioProcessingPort: AudioProcessingPort,
    private val messagingOutputPort: MessagingOutputPort
) : MessagingInputPort {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun processMessage(event: MessageReceivedEvent) {
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

    private fun processTextMessage(event: MessageReceivedEvent) {
        val processedQuery = naturalLanguageProcessingPort.extractQueryFromNaturalLanguage(event.message)
        val nutritionData = proteinService.getTotalProtein(processedQuery)
        val responseText = naturalLanguageProcessingPort.generateProteinSummary(nutritionData)
        
        messagingOutputPort.sendMessage(MessageResponseEvent(event.chatId, responseText))
    }

    private fun processVoiceMessage(event: MessageReceivedEvent) {
        val parts = event.message.split("|")
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid voice message format")
        }
        
        val audioBytes = Base64.getDecoder().decode(parts[0])
        val mimeType = parts[1]
        
        val transcribedText = audioProcessingPort.transcribeAudio(audioBytes, mimeType)
        val processedQuery = naturalLanguageProcessingPort.extractQueryFromNaturalLanguage(transcribedText)
        val nutritionData = proteinService.getTotalProtein(processedQuery)
        val responseText = naturalLanguageProcessingPort.generateProteinSummary(nutritionData)
        
        messagingOutputPort.sendMessage(MessageResponseEvent(event.chatId, responseText))
    }
}
