package com.bhuvaneshw.pdfviewer.compose.ui

import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

internal fun Float.checkNaN(valueIfNaN: Float): Float {
    return if (isNaN()) valueIfNaN
    else this
}

internal fun findSelectedOption(options: Array<String>, currentValue: String): Int {
    options.forEachIndexed { index, option ->
        if (option == currentValue)
            return index
    }
    return -1
}

internal fun Long.formatToSize(): String {
    if (this <= 0) return "0 B"
    val unit = when {
        this < 1024 -> "B"
        this < 1024 * 1024 -> "KB"
        this < 1024 * 1024 * 1024 -> "MB"
        this < 1024L * 1024 * 1024 * 1024 -> "GB"
        this < 1024L * 1024 * 1024 * 1024 * 1024 -> "TB"
        else -> "PB"
    }
    val value = when (unit) {
        "B" -> this.toDouble()
        "KB" -> this / 1024.0
        "MB" -> this / (1024.0 * 1024)
        "GB" -> this / (1024.0 * 1024 * 1024)
        "TB" -> this / (1024.0 * 1024 * 1024 * 1024)
        "PB" -> this / (1024.0 * 1024 * 1024 * 1024 * 1024)
        else -> 0.0
    }
    return "%.1f %s".format(value, unit)
}

internal fun String.formatToDate(): String {
    val cleanDate = if (this.startsWith("D:")) this.substring(2) else this
    if (cleanDate.length < 14) return this
    val rawDate = cleanDate.substring(0, 14)

    val parser = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
    val date = parser.parse(rawDate) ?: return "Invalid Date"

    return formatter.format(date)
}

internal fun String.formatZoom(zoom: Float): String {
    return when (this) {
        Zoom.AUTOMATIC.value -> "Zoom (Auto)"
        Zoom.PAGE_FIT.value -> "Zoom (Page Fit)"
        Zoom.PAGE_WIDTH.value -> "Zoom (Page Width)"
        Zoom.ACTUAL_SIZE.value -> "Zoom (Actual Size)"
        else -> "Zoom (${(zoom * 100).roundToInt()}%)"
    }
}

internal enum class Zoom(internal val value: String) {
    AUTOMATIC("auto"),
    PAGE_FIT("page-fit"),
    PAGE_WIDTH("page-width"),
    ACTUAL_SIZE("page-actual")
}
