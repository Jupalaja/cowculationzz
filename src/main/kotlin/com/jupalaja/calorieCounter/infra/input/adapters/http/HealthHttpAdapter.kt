package com.jupalaja.calorieCounter.infra.input.adapters.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class HealthHttpAdapter {
    @GetMapping("/health")
    @ResponseBody
    fun getHealthCheck(): ResponseEntity<HashMap<String, String>> = ResponseEntity(hashMapOf("status" to "UP"), HttpStatus.OK)
}
