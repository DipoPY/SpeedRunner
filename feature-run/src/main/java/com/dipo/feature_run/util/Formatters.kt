package com.dipo.feature_run.util

import java.text.DateFormat
import java.util.Date
import java.util.Locale

fun Long.toDisplayDateTime(): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
        .format(Date(this))

fun Long.toDisplayDuration(): String {
    val totalSeconds = this / 1_000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return buildString {
        if (hours > 0) {
            append(hours)
            append(" ч ")
        }
        append(minutes)
        append(" мин ")
    }
}

fun Double.toKilometersString(): String =
    String.format(Locale.getDefault(), "%.2f", this / 1000.0)
