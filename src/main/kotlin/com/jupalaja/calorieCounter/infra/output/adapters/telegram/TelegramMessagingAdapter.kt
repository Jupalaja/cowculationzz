package com.jupalaja.calorieCounter.infra.output.adapters.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.jupalaja.calorieCounter.domain.dto.messaging.MessageResponseEvent
import com.jupalaja.calorieCounter.infra.output.ports.MessagingOutputPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TelegramMessagingAdapter : MessagingOutputPort {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var bot: Bot

    fun setBot(bot: Bot) {
        this.bot = bot
    }

    override fun sendMessage(event: MessageResponseEvent) {
        if (::bot.isInitialized) {
            bot.sendMessage(chatId = ChatId.fromId(event.chatId.toLong()), text = event.response)
        } else {
            logger.warn("Bot not initialized, cannot send message to chatId: ${event.chatId}")
        }
    }

    override fun sendWelcomeMessage(chatId: String) {
        val welcomeMessage = "Hello! I can help you with nutrition information. Just send me what you ate and I'll tell you the total protein."
        sendMessage(MessageResponseEvent(chatId, welcomeMessage))
    }

    override fun sendErrorMessage(chatId: String, error: String) {
        sendMessage(MessageResponseEvent(chatId, error))
    }
}
