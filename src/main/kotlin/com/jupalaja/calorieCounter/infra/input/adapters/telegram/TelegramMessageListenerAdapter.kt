package com.jupalaja.calorieCounter.infra.input.adapters.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.dispatcher.voice
import com.jupalaja.calorieCounter.domain.dto.MessageReceived
import com.jupalaja.calorieCounter.domain.enums.MessageType
import com.jupalaja.calorieCounter.infra.input.ports.MessagingInputPort
import com.jupalaja.calorieCounter.infra.output.adapters.telegram.TelegramMessageSenderAdapter
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Base64

@Component
class TelegramMessageListenerAdapter(
    private val messagingInputPort: MessagingInputPort,
    private val telegramMessagingAdapter: TelegramMessageSenderAdapter,
    @Value("\${api.telegram.token}") private val telegramBotToken: String,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var bot: com.github.kotlintelegrambot.Bot

    @PostConstruct
    fun startBot() {
        if (telegramBotToken.isBlank() || telegramBotToken == "\${TELEGRAM_BOT_TOKEN}") {
            logger.warn("Telegram bot token is not set. Bot will not start.")
            return
        }

        bot =
            bot {
                token = telegramBotToken
                dispatch {
                    command("start") {
                        telegramMessagingAdapter.sendWelcomeMessage(message.chat.id.toString())
                    }
                    text {
                        val event =
                            MessageReceived(
                                chatId = message.chat.id.toString(),
                                message = text,
                                messageType = MessageType.TEXT,
                            )
                        messagingInputPort.processMessage(event)
                    }
                    voice {
                        try {
                            val voiceFileId = media.fileId
                            val mimeType = media.mimeType ?: "audio/ogg"
                            val fileBytes = bot.downloadFileBytes(voiceFileId)
                            if (fileBytes != null) {
                                // This is a simplified approach - in production you might want to use a proper serialization
                                val serializedAudioData = "${Base64.getEncoder().encodeToString(fileBytes)}|$mimeType"
                                val event =
                                    MessageReceived(
                                        chatId = message.chat.id.toString(),
                                        message = serializedAudioData,
                                        messageType = MessageType.VOICE,
                                    )
                                messagingInputPort.processMessage(event)
                            } else {
                                logger.error("Failed to download voice message with fileId: $voiceFileId")
                                telegramMessagingAdapter.sendErrorMessage(
                                    message.chat.id.toString(),
                                    "Sorry, I couldn't process your voice message.",
                                )
                            }
                        } catch (e: Exception) {
                            logger.error("Error processing voice message", e)
                            telegramMessagingAdapter.sendErrorMessage(
                                message.chat.id.toString(),
                                "Sorry, an error occurred while processing your voice message.",
                            )
                        }
                    }
                }
            }

        telegramMessagingAdapter.setBot(bot)

        Thread {
            bot.startPolling()
        }.start()
        logger.info("Telegram bot started polling.")
    }

    @PreDestroy
    fun stopBot() {
        if (::bot.isInitialized) {
            logger.info("Stopping Telegram bot polling.")
            bot.stopPolling()
        }
    }
}
