package com.jupalaja.calorieCounter.infra.output.ports

import com.jupalaja.calorieCounter.domain.dto.NutritionResponseDTO

interface NaturalLanguageProcessingPort {
    fun extractQueryFromNaturalLanguage(naturalLanguageQuery: String): String

    fun generateProteinSummary(nutritionData: NutritionResponseDTO): String
}
