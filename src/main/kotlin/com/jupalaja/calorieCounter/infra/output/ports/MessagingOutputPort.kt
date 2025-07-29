package com.jupalaja.calorieCounter.infra.output.ports

import com.jupalaja.calorieCounter.domain.dto.messaging.MessageResponseEvent

interface MessagingOutputPort {
    fun sendMessage(event: MessageResponseEvent)
    fun sendWelcomeMessage(chatId: String)
    fun sendErrorMessage(chatId: String, error: String)
}