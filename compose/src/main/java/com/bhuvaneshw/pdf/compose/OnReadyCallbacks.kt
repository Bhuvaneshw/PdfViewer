package com.bhuvaneshw.pdf.compose

import com.bhuvaneshw.pdf.PdfViewer

sealed interface OnReadyCallback {
    fun onReady(pdfViewer: PdfViewer, loadSource: () -> Unit)
}

data class DefaultOnReadyCallback(
    private val callback: (PdfViewer.() -> Unit)? = null
) : OnReadyCallback {
    override fun onReady(pdfViewer: PdfViewer, loadSource: () -> Unit) {
        loadSource()
        callback?.invoke(pdfViewer)
    }
}

data class CustomOnReadyCallback(
    private val callback: PdfViewer.(loadSource: () -> Unit) -> Unit
) : OnReadyCallback {
    override fun onReady(pdfViewer: PdfViewer, loadSource: () -> Unit) {
        callback(pdfViewer, loadSource)
    }
}
