package com.bhuvaneshw.pdf.compose.ui

import androidx.compose.foundation.layout.BoxScope
import com.bhuvaneshw.pdf.compose.PdfState

open class PdfContainerScope internal constructor(
    val pdfState: PdfState
)

class PdfContainerBoxScope internal constructor(
    pdfState: PdfState,
    private val boxScope: BoxScope
) : PdfContainerScope(pdfState), BoxScope by boxScope

class PdfToolBarScope internal constructor(
    val pdfState: PdfState,
    val toolBarState: PdfToolBarState,
)
