package com.bhuvaneshw.pdfviewer.compose

import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.bhuvaneshw.pdf.PdfViewer

@Composable
fun PdfViewer(
    pdfState: PdfState,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    onCreateViewer: (PdfViewer.() -> Unit)? = null,
    onReady: OnReadyCallback = DefaultOnReadyCallback(),
) {
    LaunchedEffect(pdfState.source) {
        pdfState.pdfViewer?.run {
            if (isInitialized)
                load(source = pdfState.source)
        }
    }

    AndroidView(
        factory = { context ->
            PdfViewer(context).also {
                if (!it.isInEditMode) {
                    it.highlightEditorColors = pdfState.highlightEditorColors.map { colorPair ->
                        colorPair.first to colorPair.second.toArgb()
                    }
                    it.editor.highlightColor = pdfState.defaultHighlightColor.toArgb()
                    pdfState.setPdfViewerTo(it)
                    onCreateViewer?.invoke(it)
                    it.onReady {
                        onReady.onReady(this) { load(pdfState.source) }
                    }
                } else pdfState.loadingState = PdfLoadingState.Finished(3)

                it.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                containerColor?.toArgb()?.let { color ->
                    it.setContainerBackgroundColor(color)
                }
            }
        },
        onRelease = {
            it.clearAllListeners()
            pdfState.clearPdfViewer()
        },
        onReset = {
            it.clearAllListeners()
        },
        update = {
            containerColor?.toArgb()?.let { color ->
                it.setContainerBackgroundColor(color)
            }
        },
        modifier = modifier
    )
}

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
