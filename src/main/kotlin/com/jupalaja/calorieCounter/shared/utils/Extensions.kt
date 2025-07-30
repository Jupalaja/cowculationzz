package com.jupalaja.calorieCounter.shared.utils

import java.io.File
import java.nio.file.Files

fun File.getMimeType(): String = Files.probeContentType(this.toPath())
