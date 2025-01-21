package com.bhuvaneshw.pdfviewer.compose

import androidx.annotation.FloatRange
import com.bhuvaneshw.pdf.PdfViewer.Zoom

data class ScaleLimit(
    @FloatRange(-4.0, 10.0) val minPageScale: Float = 0.1f,
    @FloatRange(-4.0, 10.0) val maxPageScale: Float = 10f,
    @FloatRange(-4.0, 10.0) val defaultPageScale: Float = Zoom.AUTOMATIC.floatValue,
)

data class ActualScaleLimit(
    @FloatRange(0.0, 10.0) val minPageScale: Float = 0.1f,
    @FloatRange(0.0, 10.0) val maxPageScale: Float = 10f,
    @FloatRange(0.0, 10.0) val defaultPageScale: Float = 0f,
)
