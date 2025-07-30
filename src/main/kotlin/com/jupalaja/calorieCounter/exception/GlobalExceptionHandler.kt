package com.jupalaja.calorieCounter.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<Map<String, String>> {
        val errorMessage = exception.message ?: "An unexpected error occurred"
        this.logger.error("[HANDLE_EXCEPTION] An unexpected error occurred: {}", errorMessage, exception)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to errorMessage))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        val errorMessage = exception.message ?: "Invalid request data"
        this.logger.warn("[HANDLE_ILLEGAL_ARGUMENT_EXCEPTION] Invalid argument exception: {}", errorMessage)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to errorMessage))
    }
}
