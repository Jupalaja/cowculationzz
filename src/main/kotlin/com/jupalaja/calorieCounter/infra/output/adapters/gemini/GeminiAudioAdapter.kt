package com.jupalaja.calorieCounter.infra.output.adapters.gemini

import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.Part
import com.jupalaja.calorieCounter.infra.input.ports.AudioProcessingInputPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class GeminiAudioAdapter(
    @Value("\${api.gemini.model}") private val modelName: String,
    private val geminiClient: Client,
) : AudioProcessingInputPort {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun transcribeAudio(
        audioBytes: ByteArray,
        mimeType: String,
    ): String {
        logger.info("[GET_TEXT_FROM_AUDIO] Transcribing audio of size: ${audioBytes.size} bytes and mimeType: $mimeType")
        if (audioBytes.isEmpty()) {
            throw IllegalArgumentException("Audio bytes cannot be empty.")
        }

        val audioPart = Part.fromBytes(audioBytes, mimeType)
        val promptPart = Part.fromText("Transcribe this audio message.")
        val content = Content.fromParts(promptPart, audioPart)

        return try {
            val response = geminiClient.models.generateContent(modelName, content, null)
            val text = response.text()?.trim()
            logger.info("[GET_TEXT_FROM_AUDIO] Transcribed text from Gemini: {}", text)
            if (text.isNullOrBlank()) {
                throw IllegalStateException("Gemini API returned an empty or null response for audio transcription.")
            }
            text
        } catch (e: Exception) {
            logger.error("[GET_TEXT_FROM_AUDIO] Error communicating with Gemini API for audio transcription", e)
            throw RuntimeException("Error transcribing audio with Gemini API.", e)
        }
    }
}
