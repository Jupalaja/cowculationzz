package com.jupalaja.calorieCounter.infra.input.ports

interface AudioProcessingInputPort {
    fun transcribeAudio(
        audioBytes: ByteArray,
        mimeType: String,
    ): String
}
