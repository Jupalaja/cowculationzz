package com.jupalaja.calorieCounter.infra.output.ports

interface AIModelProcessingPort {
    fun extractQueryFromNaturalLanguage(naturalLanguageQuery: String): String

    fun generateProteinSummary(nutritionDataJson: String): String

    fun transcribeAudio(
        audioBytes: ByteArray,
        mimeType: String,
    ): String
}
