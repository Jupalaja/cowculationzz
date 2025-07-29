package com.jupalaja.calorieCounter.domain.dto.messaging

data class MessageResponseEvent(
    val chatId: String,
    val response: String
)