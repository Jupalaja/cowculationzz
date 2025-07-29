package com.jupalaja.calorieCounter.infra.input.adapters.http

import com.jupalaja.calorieCounter.domain.dtos.calorieNinjas.NutritionResponseDTO
import com.jupalaja.calorieCounter.services.GeminiService
import com.jupalaja.calorieCounter.services.NutritionService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody


@Controller
@RequestMapping("/nutrition")
class NutritionHttpAdapter(
    private val nutritionService: NutritionService,
    private val geminiService: GeminiService,
) {

    @GetMapping
    @ResponseBody
    fun getNutritionInfo(@RequestParam("query") query: String): ResponseEntity<NutritionResponseDTO> {
        val processedQuery = geminiService.getQueryFromNaturalLanguage(query)
        return ResponseEntity.ok(nutritionService.getNutrition(processedQuery))
    }
}
