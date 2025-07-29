package com.jupalaja.calorieCounter.domain.dto.messaging

import com.jupalaja.calorieCounter.domain.enums.MessageType

data class MessageReceivedEvent(
    val chatId: String,
    val message: String,
    val messageType: MessageType
)