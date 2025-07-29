package com.jupalaja.calorieCounter.infra.input.adapters.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.dispatcher.voice
import com.github.kotlintelegrambot.entities.ChatId
import com.jupalaja.calorieCounter.services.GeminiService
import com.jupalaja.calorieCounter.services.ProteinService
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TelegramBotAdapter(
    private val proteinService: ProteinService,
    private val geminiService: GeminiService,
    @Value("\${api.telegram.token}") private val telegramBotToken: String
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var bot: com.github.kotlintelegrambot.Bot

    @PostConstruct
    fun startBot() {
        if (telegramBotToken.isBlank() || telegramBotToken == "\${TELEGRAM_BOT_TOKEN}") {
            logger.warn("Telegram bot token is not set. Bot will not start.")
            return
        }

        bot = bot {
            token = telegramBotToken
            dispatch {
                command("start") {
                    val welcomeMessage = "Hello! I can help you with nutrition information. Just send me what you ate and I'll tell you the total protein."
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = welcomeMessage)
                }
                text {
                    try {
                        val processedQuery = geminiService.getQueryFromNaturalLanguage(text)
                        val totalProtein = proteinService.getTotalProtein(processedQuery)
                        val responseText = geminiService.getProteinSummary(totalProtein)
                        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = responseText)
                    } catch (e: Exception) {
                        logger.error("Error processing message: '$text'", e)
                        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Sorry, an error occurred while processing your request.")
                    }
                }
                voice {
                    try {
                        val voiceFileId = media.fileId
                        val mimeType = media.mimeType ?: "audio/ogg"
                        val fileBytes = bot.downloadFileBytes(voiceFileId)
                        if (fileBytes != null) {
                            val transcribedText = geminiService.getTextFromAudio(fileBytes, mimeType)
                            val processedQuery = geminiService.getQueryFromNaturalLanguage(transcribedText)
                            val totalProtein = proteinService.getTotalProtein(processedQuery)
                            val responseText = geminiService.getProteinSummary(totalProtein)
                            bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = responseText)
                        } else {
                            logger.error("Failed to download voice message with fileId: $voiceFileId")
                            bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Sorry, I couldn't process your voice message.")
                        }
                    } catch (e: Exception) {
                        logger.error("Error processing voice message", e)
                        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Sorry, an error occurred while processing your voice message.")
                    }
                }
            }
        }

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
