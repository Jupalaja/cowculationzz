package com.jupalaja.calorieCounter.domain.dto

import com.jupalaja.calorieCounter.domain.enums.MessageType
import java.nio.file.Path

data class MessageReceived(
    val chatId: String,
    val text: String? = null,
    val data: Path? = null,
    val messageType: MessageType,
)
