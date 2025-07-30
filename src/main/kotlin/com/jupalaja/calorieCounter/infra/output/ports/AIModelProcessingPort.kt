package com.jupalaja.calorieCounter.infra.output.ports

import java.nio.file.Path

interface AIModelProcessingPort {
    fun extractQueryFromNaturalLanguage(naturalLanguageQuery: String): String

    fun generateProteinSummary(nutritionDataJson: String): String

    fun transcribeAudio(filePath: Path): String
}
