package com.bhuvaneshw.pdf.compose

import android.net.Uri
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import com.bhuvaneshw.pdf.PdfDocumentProperties
import com.bhuvaneshw.pdf.PdfListener
import com.bhuvaneshw.pdf.PdfViewer
import com.bhuvaneshw.pdf.WebViewError
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun PdfViewer.loadingStateFlow(): Flow<PdfLoadingState> = flowIt { emit ->
    object : PdfListener {
        override fun onPageLoadStart() {
            emit(PdfLoadingState.Initializing)
        }

        override fun onProgressChange(progress: Float) {
            emit(PdfLoadingState.Loading(progress))
        }

        override fun onPageLoadSuccess(pagesCount: Int) {
            emit(PdfLoadingState.Finished(pagesCount))
        }

        override fun onPageLoadFailed(exception: Exception) {
            emit(PdfLoadingState.Error(exception))
        }
    }
}

fun PdfViewer.webViewErrorFlow(): Flow<WebViewError> = flowIt { emit ->
    object : PdfListener {
        override fun onReceivedError(error: WebViewError) {
            emit(error)
        }
    }
}

fun PdfViewer.pageNumberFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onPageChange(pageNumber: Int) {
            emit(pageNumber)
        }
    }
}

fun PdfViewer.scaleFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onPageChange(pageNumber: Int) {
            emit(pageNumber)
        }
    }
}

fun PdfViewer.savePdfFlow(): Flow<ByteArray> = flowIt { emit ->
    object : PdfListener {
        override fun onSavePdf(pdfAsBytes: ByteArray) {
            emit(pdfAsBytes)
        }
    }
}

fun PdfViewer.matchStateFlow(): Flow<MatchState> = flowIt { emit ->
    object : PdfListener {
        private var current = 0
        private var total = 0

        override fun onFindMatchStart() {
            emit(MatchState.Started())
        }

        override fun onFindMatchChange(current: Int, total: Int) {
            this.current = current
            this.total = total
            emit(MatchState.Progress(current, total))
        }

        override fun onFindMatchComplete(found: Boolean) {
            emit(MatchState.Completed(found, current, total))
        }
    }
}

fun PdfViewer.scrollStateFlow(): Flow<ScrollState> = flowIt { emit ->
    object : PdfListener {
        override fun onScrollChange(
            currentOffset: Int,
            totalOffset: Int,
            isHorizontalScroll: Boolean
        ) {
            emit(ScrollState(currentOffset, totalOffset, isHorizontalScroll))
        }
    }
}

fun PdfViewer.propertiesFlow(): Flow<PdfDocumentProperties> = flowIt { emit ->
    object : PdfListener {
        override fun onLoadProperties(properties: PdfDocumentProperties) {
            emit(properties)
        }
    }
}

fun PdfViewer.passwordDialogFlow(): Flow<Boolean> = flowIt { emit ->
    object : PdfListener {
        override fun onPasswordDialogChange(isOpen: Boolean) {
            emit(isOpen)
        }
    }
}

fun PdfViewer.scrollModeFlow(): Flow<PdfViewer.PageScrollMode> = flowIt { emit ->
    object : PdfListener {
        override fun onScrollModeChange(scrollMode: PdfViewer.PageScrollMode) {
            emit(scrollMode)
        }
    }
}

fun PdfViewer.spreadModeFlow(): Flow<PdfViewer.PageSpreadMode> = flowIt { emit ->
    object : PdfListener {
        override fun onSpreadModeChange(spreadMode: PdfViewer.PageSpreadMode) {
            emit(spreadMode)
        }
    }
}

fun PdfViewer.rotationFlow(): Flow<PdfViewer.PageRotation> = flowIt { emit ->
    object : PdfListener {
        override fun onRotationChange(rotation: PdfViewer.PageRotation) {
            emit(rotation)
        }
    }
}

fun PdfViewer.singleClickFlow(): Flow<Unit> = flowIt { emit ->
    object : PdfListener {
        override fun onSingleClick() {
            emit(Unit)
        }
    }
}

fun PdfViewer.doubleClickFlow(): Flow<Unit> = flowIt { emit ->
    object : PdfListener {
        override fun onDoubleClick() {
            emit(Unit)
        }
    }
}

fun PdfViewer.longClickFlow(): Flow<Unit> = flowIt { emit ->
    object : PdfListener {
        override fun onLongClick() {
            emit(Unit)
        }
    }
}

fun PdfViewer.linkClickFlow(): Flow<String> = flowIt { emit ->
    object : PdfListener {
        override fun onLinkClick(link: String) {
            emit(link)
        }
    }
}

fun PdfViewer.snapFlow(): Flow<Boolean> = flowIt { emit ->
    object : PdfListener {
        override fun onSnapChange(snapPage: Boolean) {
            emit(snapPage)
        }
    }
}

fun PdfViewer.singlePageArrangementFlow(): Flow<Pair<Boolean, Boolean>> = flowIt { emit ->
    object : PdfListener {
        override fun onSinglePageArrangementChange(
            requestedArrangement: Boolean,
            appliedArrangement: Boolean
        ) {
            emit(requestedArrangement to appliedArrangement)
        }
    }
}

fun PdfViewer.editorHighlightColorFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorHighlightColorChange(highlightColor: Int) {
            emit(highlightColor)
        }
    }
}

fun PdfViewer.editorShowAllHighlightsFlow(): Flow<Boolean> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorShowAllHighlightsChange(showAll: Boolean) {
            emit(showAll)
        }
    }
}

fun PdfViewer.editorHighlightThicknessFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorHighlightThicknessChange(thickness: Int) {
            emit(thickness)
        }
    }
}

fun PdfViewer.editorFreeFontColorFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorFreeFontColorChange(fontColor: Int) {
            emit(fontColor)
        }
    }
}

fun PdfViewer.editorFreeFontSizeFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorFreeFontSizeChange(fontSize: Int) {
            emit(fontSize)
        }
    }
}

fun PdfViewer.editorInkColorFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorInkColorChange(color: Int) {
            emit(color)
        }
    }
}

fun PdfViewer.editorInkThicknessFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorInkThicknessChange(thickness: Int) {
            emit(thickness)
        }
    }
}

fun PdfViewer.editorInkOpacityFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorInkOpacityChange(opacity: Int) {
            emit(opacity)
        }
    }
}

fun PdfViewer.renderProcessGoneFlow(handled: () -> Boolean = { true }): Flow<RenderProcessGoneDetail?> =
    flowIt { emit ->
        object : PdfListener {
            override fun onRenderProcessGone(detail: RenderProcessGoneDetail?): Boolean {
                emit(detail)
                return handled()
            }
        }
    }

fun PdfViewer.printStateFlow(): Flow<PdfPrintState> = flowIt { emit ->
    object : PdfListener {
        override fun onPrintProcessStart() {
            emit(PdfPrintState.Starting)
        }

        override fun onPrintProcessProgress(progress: Float) {
            emit(PdfPrintState.Loading(progress))
        }

        override fun onPrintProcessEnd() {
            emit(PdfPrintState.Completed)
        }

        override fun onPrintCancelled() {
            emit(PdfPrintState.Cancelled)
        }
    }
}

fun PdfViewer.editorMessageFlow(): Flow<String> = flowIt { emit ->
    object : PdfListener {
        override fun onShowEditorMessage(message: String) {
            emit(message)
        }
    }
}

fun PdfViewer.scaleLimitFlow(): Flow<Triple<Float, Float, Float>> = flowIt { emit ->
    object : PdfListener {
        override fun onScaleLimitChange(
            minPageScale: Float,
            maxPageScale: Float,
            defaultPageScale: Float
        ) {
            emit(Triple(minPageScale, maxPageScale, defaultPageScale))
        }
    }
}

fun PdfViewer.actualScaleLimitFlow(): Flow<Triple<Float, Float, Float>> = flowIt { emit ->
    object : PdfListener {
        override fun onActualScaleLimitChange(
            minPageScale: Float,
            maxPageScale: Float,
            defaultPageScale: Float
        ) {
            emit(Triple(minPageScale, maxPageScale, defaultPageScale))
        }
    }
}

fun PdfViewer.alignModeFlow(): Flow<Pair<PdfViewer.PageAlignMode, PdfViewer.PageAlignMode>> =
    flowIt { emit ->
        object : PdfListener {
            override fun onAlignModeChange(
                requestedMode: PdfViewer.PageAlignMode,
                appliedMode: PdfViewer.PageAlignMode
            ) {
                emit(requestedMode to appliedMode)
            }
        }
    }

fun PdfViewer.scrollSpeedLimitFlow(): Flow<Pair<PdfViewer.ScrollSpeedLimit, PdfViewer.ScrollSpeedLimit>> =
    flowIt { emit ->
        object : PdfListener {
            override fun onScrollSpeedLimitChange(
                requestedLimit: PdfViewer.ScrollSpeedLimit,
                appliedLimit: PdfViewer.ScrollSpeedLimit
            ) {
                emit(requestedLimit to appliedLimit)
            }
        }
    }

fun PdfViewer.showFileChooserFlow(handled: () -> Boolean): Flow<Pair<ValueCallback<Array<out Uri?>?>?, WebChromeClient.FileChooserParams?>> =
    flowIt { emit ->
        object : PdfListener {
            override fun onShowFileChooser(
                filePathCallback: ValueCallback<Array<out Uri?>?>?,
                fileChooserParams: WebChromeClient.FileChooserParams?
            ): Boolean {
                emit(filePathCallback to fileChooserParams)
                return handled()
            }
        }
    }

private inline fun <T> PdfViewer.flowIt(
    crossinline createListener: ((T) -> Unit) -> PdfListener
): Flow<T> = callbackFlow {
    val listener = createListener { value -> trySend(value).isSuccess }
    addListener(listener)
    awaitClose { removeListener(listener) }
}
