package com.jupalaja.calorieCounter.infra.output.ports

interface NaturalLanguageProcessingPort {
    fun extractQueryFromNaturalLanguage(naturalLanguageQuery: String): String

    fun generateProteinSummary(nutritionDataJson: String): String
}
