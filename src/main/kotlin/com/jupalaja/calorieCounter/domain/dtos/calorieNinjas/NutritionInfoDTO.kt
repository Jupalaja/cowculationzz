package com.jupalaja.calorieCounter.domain.dtos.calorieNinjas

import com.fasterxml.jackson.annotation.JsonProperty


data class NutritionInfoDTO(
    @JsonProperty("sugar_g")
    val sugarG: Double,
    @JsonProperty("fiber_g")
    val fiberG: Double,
    @JsonProperty("serving_size_g")
    val servingSizeG: Double,
    @JsonProperty("sodium_mg")
    val sodiumMg: Int,
    val name: String,
    @JsonProperty("potassium_mg")
    val potassiumMg: Int,
    @JsonProperty("fat_saturated_g")
    val fatSaturatedG: Double,
    @JsonProperty("fat_total_g")
    val fatTotalG: Double,
    val calories: Double,
    @JsonProperty("cholesterol_mg")
    val cholesterolMg: Int,
    @JsonProperty("protein_g")
    val proteinG: Double,
    @JsonProperty("carbohydrates_total_g")
    val carbohydratesTotalG: Double
)
