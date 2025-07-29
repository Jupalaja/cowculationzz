package com.jupalaja.calorieCounter.infra.input.adapters.http

import com.jupalaja.calorieCounter.domain.dtos.ProteinResponseDTO
import com.jupalaja.calorieCounter.services.GeminiService
import com.jupalaja.calorieCounter.services.ProteinService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody


@Controller
@RequestMapping("/protein")
class ProteinHttpAdapter(
    private val proteinService: ProteinService,
    private val geminiService: GeminiService,
) {

    @GetMapping
    @ResponseBody
    fun getProteinInfo(@RequestParam("message") message: String): ResponseEntity<ProteinResponseDTO> {
        val processedQuery = geminiService.getQueryFromNaturalLanguage(message)
        val totalProtein = proteinService.getTotalProtein(processedQuery)
        val responseText = geminiService.getProteinSummaryInSpanish(totalProtein)
        return ResponseEntity.ok(ProteinResponseDTO(text = responseText))
    }
}
