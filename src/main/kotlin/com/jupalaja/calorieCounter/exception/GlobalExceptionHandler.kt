package com.jupalaja.calorieCounter.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<Map<String, String>> {
        val errorMessage = exception.message ?: "An unexpected error occurred"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to errorMessage))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        val errorMessage = exception.message ?: "Invalid request data"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to errorMessage))
    }
}
