package com.jupalaja.calorieCounter.domain.dto

import com.jupalaja.calorieCounter.domain.enums.MessageType

data class MessageReceived(
    val chatId: String,
    val message: String,
    val messageType: MessageType,
)
