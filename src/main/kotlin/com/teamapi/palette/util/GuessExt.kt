package com.teamapi.palette.util

import org.apache.tika.config.TikaConfig
import org.apache.tika.detect.DefaultDetector
import org.apache.tika.metadata.Metadata
import java.io.ByteArrayInputStream

fun guessMediaType(bytes: ByteArray) = DefaultDetector().detect(
    ByteArrayInputStream(bytes),
    Metadata()
).toString()

fun guessExtension(bytes: ByteArray): String =
    TikaConfig.getDefaultConfig().mimeRepository.forName(guessMediaType(bytes)).extension
