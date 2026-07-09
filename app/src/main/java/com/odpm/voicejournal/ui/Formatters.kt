package com.odpm.voicejournal.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val CAPTURED_AT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

fun formatCapturedAt(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
    CAPTURED_AT_FORMATTER.format(instant.atZone(zone))

fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
