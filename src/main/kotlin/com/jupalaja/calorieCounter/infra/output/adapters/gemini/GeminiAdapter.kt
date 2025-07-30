package com.jupalaja.calorieCounter.infra.output.adapters.gemini

import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.Part
import com.jupalaja.calorieCounter.infra.output.ports.AIModelProcessingPort
import com.jupalaja.calorieCounter.shared.utils.getMimeType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class GeminiAdapter(
    @Value("\${api.gemini.model}") private val modelName: String,
    @Value("\${api.gemini.key}") apiKey: String,
) : AIModelProcessingPort {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val geminiClient: Client = Client.builder().apiKey(apiKey).build()

    override fun generateText(prompt: String): String {
        this.logger.info("[GENERATE_TEXT] Generating text for prompt")
        if (prompt.isBlank()) {
            throw IllegalArgumentException("Prompt cannot be blank.")
        }

        return try {
            val response = this.geminiClient.models.generateContent(this.modelName, prompt, null)
            val text = response.text()?.trim()
            this.logger.info("[GENERATE_TEXT] Generated text from Gemini")
            if (text.isNullOrBlank()) {
                throw IllegalStateException("Gemini API returned an empty or null response.")
            }
            text
        } catch (e: Exception) {
            this.logger.error("[GENERATE_TEXT] Error communicating with Gemini API", e)
            throw RuntimeException("Error generating text with Gemini API.", e)
        }
    }

    override fun transcribeAudio(filePath: Path): String {
        this.logger.info("[GET_TEXT_FROM_AUDIO] Transcribing audio from path: $filePath")
        val audioFile = filePath.toFile()
        val mimeType = audioFile.getMimeType()
        this.logger.info("[GET_TEXT_FROM_AUDIO] Detected mimeType for ${audioFile.name}: $mimeType")
        val audioBytes = audioFile.readBytes()

        if (audioBytes.isEmpty()) {
            throw IllegalArgumentException("Audio file is empty.")
        }

        val audioPart = Part.fromBytes(audioBytes, mimeType)
        val promptPart = Part.fromText("Transcribe this audio message.")
        val content = Content.fromParts(promptPart, audioPart)

        return try {
            val response = this.geminiClient.models.generateContent(this.modelName, content, null)
            val text = response.text()?.trim()
            this.logger.info("[GET_TEXT_FROM_AUDIO] Transcribed text from Gemini: {}", text)
            if (text.isNullOrBlank()) {
                throw IllegalStateException("Gemini API returned an empty or null response for audio transcription.")
            }
            text
        } catch (e: Exception) {
            this.logger.error("[GET_TEXT_FROM_AUDIO] Error communicating with Gemini API for audio transcription", e)
            throw RuntimeException("Error transcribing audio with Gemini API.", e)
        }
    }
}
