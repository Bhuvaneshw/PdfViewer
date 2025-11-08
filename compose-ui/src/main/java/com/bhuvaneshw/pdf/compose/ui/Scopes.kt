package com.bhuvaneshw.pdf.compose.ui

import androidx.compose.foundation.layout.BoxScope
import com.bhuvaneshw.pdf.compose.PdfState

/**
 * A scope for composables that are part of the PDF viewer container.
 * Provides access to the [pdfState].
 *
 * @see com.bhuvaneshw.pdf.compose.PdfViewer
 */
open class PdfContainerScope internal constructor(
    /**
     * The state of the PDF viewer.
     * @see com.bhuvaneshw.pdf.compose.PdfState
     */
    val pdfState: PdfState
)

/**
 * A scope for composables within a PDF viewer container that is also a [BoxScope].
 * Provides access to both the PDF viewer state and the receiver scope of a [androidx.compose.foundation.layout.Box] composable.
 *
 * @see com.bhuvaneshw.pdf.compose.PdfViewer
 * @see androidx.compose.foundation.layout.BoxScope
 */
class PdfContainerBoxScope internal constructor(
    pdfState: PdfState,
    private val boxScope: BoxScope
) : PdfContainerScope(pdfState), BoxScope by boxScope

/**
 * A scope for composables that are part of the PDF viewer's toolbar.
 * Provides access to the [pdfState] and [toolBarState].
 *
 * @see com.bhuvaneshw.pdf.compose.ui.PdfToolBar
 */
class PdfToolBarScope internal constructor(
    /**
     * The state of the PDF viewer.
     * @see com.bhuvaneshw.pdf.compose.PdfState
     */
    val pdfState: PdfState,
    /**
     * The state of the PDF viewer's toolbar.
     * @see com.bhuvaneshw.pdf.compose.ui.PdfToolBarState
     */
    val toolBarState: PdfToolBarState,
)
