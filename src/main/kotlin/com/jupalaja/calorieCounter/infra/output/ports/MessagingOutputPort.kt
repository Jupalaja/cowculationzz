package com.jupalaja.calorieCounter.infra.output.ports

import com.jupalaja.calorieCounter.domain.dto.MessageResponse

interface MessagingOutputPort {
    fun sendMessage(event: MessageResponse)
}
