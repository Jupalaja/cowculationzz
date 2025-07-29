package com.jupalaja.calorieCounter.infra.input.ports

interface AudioProcessingPort {
    fun transcribeAudio(audioBytes: ByteArray, mimeType: String): String
}