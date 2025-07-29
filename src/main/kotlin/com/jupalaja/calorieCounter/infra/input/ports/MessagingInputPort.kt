package com.jupalaja.calorieCounter.infra.input.ports

import com.jupalaja.calorieCounter.domain.dto.messaging.MessageReceivedEvent

interface MessagingInputPort {
    fun processMessage(event: MessageReceivedEvent)
}