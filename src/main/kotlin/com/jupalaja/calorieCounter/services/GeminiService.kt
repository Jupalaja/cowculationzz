package com.jupalaja.calorieCounter.services

import com.google.genai.Client
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GeminiService(
    @Value("\${api.gemini.model}") private val modelName: String,
    private val geminiClient: Client,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val promptTemplate = """
        Summarize the following user request into a simple query in English for a nutrition API.
        The user wants to know the nutritional information of some food.
        Extract the food items and quantities, and translate them to English.
        For example, if the user asks 'what are the calories in 3 boiled eggs and a slice of bread', the output should be '3 boiled eggs and a slice of bread'.
        If the user asks in another language, for example 'me comí 2 manzanas y una banana', the output should be '2 apples and a banana'.
        Another example, if the user asks 'me comi un pan grande, de media libra', the output should be 'half a pound of large bread'.
        Only return the query, with no other text, explanation, or formatting.
        The user request is: "%s"
    """.trimIndent()

    fun getQueryFromNaturalLanguage(naturalLanguageQuery: String): String {
        logger.info("[GET_QUERY_FROM_NATURAL_LANGUAGE] Processing natural language query: {}", naturalLanguageQuery)
        if (naturalLanguageQuery.isBlank()) {
            throw IllegalArgumentException("Query cannot be blank.")
        }
        val prompt = promptTemplate.format(naturalLanguageQuery)

        return try {
            val response = geminiClient.models.generateContent(modelName, prompt, null)
            val query = response.text()?.trim()
            logger.info("[GET_QUERY_FROM_NATURAL_LANGUAGE] Generated query from Gemini: {}", query)
            if (query.isNullOrBlank()) {
                throw IllegalStateException("Gemini API returned an empty or null response.")
            }
            query
        } catch (e: Exception) {
            logger.error("[GET_QUERY_FROM_NATURAL_LANGUAGE] Error communicating with Gemini API", e)
            throw RuntimeException("Error processing natural language query with Gemini API.", e)
        }
    }
}
