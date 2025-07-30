package com.jupalaja.calorieCounter.infra.output.ports

import java.nio.file.Path

interface AIModelProcessingPort {
    fun generateText(prompt: String): String

    fun transcribeAudio(filePath: Path): String
}
