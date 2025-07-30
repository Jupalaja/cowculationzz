package com.jupalaja.calorieCounter.infra.output.adapters.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.jupalaja.calorieCounter.domain.dto.MessageResponse
import com.jupalaja.calorieCounter.infra.output.ports.MessagingOutputPort
import com.jupalaja.calorieCounter.shared.constants.MessageConstants.WELCOME_MESSAGE
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TelegramMessageSenderAdapter : MessagingOutputPort {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var bot: Bot

    fun setBot(bot: Bot) {
        this.bot = bot
    }

    override fun sendMessage(event: MessageResponse) {
        if (::bot.isInitialized) {
            bot.sendMessage(chatId = ChatId.fromId(event.chatId.toLong()), text = event.response)
        } else {
            logger.warn("Bot not initialized, cannot send message to chatId: ${event.chatId}")
        }
    }

    override fun sendWelcomeMessage(chatId: String) {
        sendMessage(MessageResponse(chatId, WELCOME_MESSAGE))
    }

    override fun sendErrorMessage(
        chatId: String,
        error: String,
    ) {
        sendMessage(MessageResponse(chatId, error))
    }
}
