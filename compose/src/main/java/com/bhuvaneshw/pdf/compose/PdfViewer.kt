package com.bhuvaneshw.pdf.compose

import android.content.Context
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
    factory: (context: Context) -> PdfViewer = { PdfViewer(context = it) },
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
            factory(context).also {
                if (!it.isInEditMode) {
                    it.highlightEditorColors = pdfState.highlightEditorColors.map { colorPair ->
                        colorPair.first to colorPair.second.toArgb()
                    }
                    pdfState.setPdfViewerTo(it)
                    onCreateViewer?.invoke(it)
                    it.onReady {
                        it.editor.highlightColor = pdfState.defaultHighlightColor.toArgb()
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
