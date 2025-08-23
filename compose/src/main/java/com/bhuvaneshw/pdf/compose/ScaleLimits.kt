package com.bhuvaneshw.pdf.compose

import androidx.annotation.FloatRange
import com.bhuvaneshw.pdf.PdfViewer.Zoom

data class ScaleLimit(
    @param:FloatRange(-4.0, 10.0) val minPageScale: Float = 0.1f,
    @param:FloatRange(-4.0, 10.0) val maxPageScale: Float = 10f,
    @param:FloatRange(-4.0, 10.0) val defaultPageScale: Float = Zoom.AUTOMATIC.floatValue,
)

data class ActualScaleLimit(
    @param:FloatRange(0.0, 10.0) val minPageScale: Float = 0.1f,
    @param:FloatRange(0.0, 10.0) val maxPageScale: Float = 10f,
    @param:FloatRange(0.0, 10.0) val defaultPageScale: Float = 0f,
)
