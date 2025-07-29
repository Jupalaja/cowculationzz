package com.jupalaja.calorieCounter.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class CalorieNinjasConfig(
    @Value("\${api.calorieninjas.url}") private val apiUrl: String,
    @Value("\${api.calorieninjas.key}") private val apiKey: String,
) {
    @Bean
    fun calorieNinjasWebClient(): WebClient =
        WebClient
            .builder()
            .baseUrl(apiUrl)
            .defaultHeader("X-Api-Key", apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
}
