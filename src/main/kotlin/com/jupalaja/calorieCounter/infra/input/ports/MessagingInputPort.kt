package com.jupalaja.calorieCounter.infra.input.ports

import com.jupalaja.calorieCounter.domain.dto.MessageReceived

interface MessagingInputPort {
    fun processMessage(event: MessageReceived)
}
