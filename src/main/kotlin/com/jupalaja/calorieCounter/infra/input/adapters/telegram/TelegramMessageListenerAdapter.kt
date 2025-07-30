package com.jupalaja.calorieCounter.infra.input.adapters.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.dispatcher.voice
import com.jupalaja.calorieCounter.domain.dto.MessageReceived
import com.jupalaja.calorieCounter.domain.dto.MessageResponse
import com.jupalaja.calorieCounter.domain.enums.MessageType
import com.jupalaja.calorieCounter.infra.input.ports.MessagingInputPort
import com.jupalaja.calorieCounter.infra.output.adapters.telegram.TelegramMessageSenderAdapter
import com.jupalaja.calorieCounter.shared.constants.Messages.VOICE_MESSAGE_GENERAL_ERROR
import com.jupalaja.calorieCounter.shared.constants.Messages.VOICE_MESSAGE_PROCESSING_ERROR
import com.jupalaja.calorieCounter.shared.constants.Messages.WELCOME_MESSAGE
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.io.File

@Component
@Primary
class TelegramMessageListenerAdapter(
    private val messagingInputPort: MessagingInputPort,
    private val telegramMessagingAdapter: TelegramMessageSenderAdapter,
    @Value("\${api.telegram.token}") private val telegramBotToken: String,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var bot: Bot

    @PostConstruct
    fun startBot() {
        if (this.telegramBotToken.isBlank()) {
            this.logger.warn("Telegram bot token is not set. Bot will not start.")
            return
        }

        this.bot =
            bot {
                token = this@TelegramMessageListenerAdapter.telegramBotToken
                dispatch {
                    command("start") {
                        this@TelegramMessageListenerAdapter.telegramMessagingAdapter.sendMessage(
                            MessageResponse(
                                message.chat.id.toString(),
                                WELCOME_MESSAGE,
                            ),
                        )
                    }
                    text {
                        val event =
                            MessageReceived(
                                chatId = message.chat.id.toString(),
                                text = text,
                                messageType = MessageType.TEXT,
                            )
                        this@TelegramMessageListenerAdapter.messagingInputPort.processMessage(event)
                    }
                    voice {
                        try {
                            val voiceFileId = media.fileId
                            val fileBytes = bot.downloadFileBytes(voiceFileId)

                            if (fileBytes != null) {
                                val tempFile = File.createTempFile("voice-", ".ogg")
                                tempFile.writeBytes(fileBytes)

                                val event =
                                    MessageReceived(
                                        chatId = message.chat.id.toString(),
                                        data = tempFile.toPath(),
                                        messageType = MessageType.VOICE,
                                    )
                                this@TelegramMessageListenerAdapter.messagingInputPort.processMessage(event)
                            } else {
                                this@TelegramMessageListenerAdapter.logger.error("Failed to download voice message with fileId: $voiceFileId")
                                this@TelegramMessageListenerAdapter.telegramMessagingAdapter.sendMessage(
                                    MessageResponse(
                                        message.chat.id.toString(),
                                        VOICE_MESSAGE_PROCESSING_ERROR,
                                    ),
                                )
                            }
                        } catch (e: Exception) {
                            this@TelegramMessageListenerAdapter.logger.error("Error processing voice message", e)
                            this@TelegramMessageListenerAdapter.telegramMessagingAdapter.sendMessage(
                                MessageResponse(
                                    message.chat.id.toString(),
                                    VOICE_MESSAGE_GENERAL_ERROR,
                                ),
                            )
                        }
                    }
                }
            }

        this.telegramMessagingAdapter.setBot(this.bot)

        Thread {
            this.bot.startPolling()
        }.start()
        this.logger.info("Telegram bot started polling.")
    }

    @PreDestroy
    fun stopBot() {
        if (this::bot.isInitialized) {
            this.logger.info("Stopping Telegram bot polling.")
            this.bot.stopPolling()
        }
    }
}
