package com.bhuvaneshw.pdf.compose

import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import com.bhuvaneshw.pdf.PdfDocumentProperties
import com.bhuvaneshw.pdf.PdfListener
import com.bhuvaneshw.pdf.PdfUnstableApi
import com.bhuvaneshw.pdf.PdfViewer
import com.bhuvaneshw.pdf.WebViewError

@Composable
fun rememberPdfState(
    source: String,
    highlightEditorColors: List<Pair<String, Color>> = PdfViewerDefaults.highlightEditorColors,
    defaultHighlightColor: Color = highlightEditorColors.firstOrNull()?.second
        ?: PdfViewerDefaults.highlightEditorColors[0].second,
): PdfState = rememberPdfState(
    source = PdfSource.Plain(source),
    highlightEditorColors = highlightEditorColors,
    defaultHighlightColor = defaultHighlightColor,
)

@Composable
fun rememberPdfState(
    source: PdfSource,
    highlightEditorColors: List<Pair<String, Color>> = PdfViewerDefaults.highlightEditorColors,
    defaultHighlightColor: Color = highlightEditorColors.firstOrNull()?.second
        ?: PdfViewerDefaults.highlightEditorColors[0].second,
): PdfState = remember {
    PdfState(
        source = source,
        highlightEditorColors = highlightEditorColors,
        defaultHighlightColor = defaultHighlightColor
    )
}

class PdfState(
    source: PdfSource,
    val highlightEditorColors: List<Pair<String, Color>> = PdfViewerDefaults.highlightEditorColors,
    val defaultHighlightColor: Color = highlightEditorColors.firstOrNull()?.second
        ?: PdfViewerDefaults.highlightEditorColors[0].second,
) {

    var source by mutableStateOf(source); internal set
    var pdfViewer: PdfViewer? by mutableStateOf(null); internal set
    var loadingState: PdfLoadingState by mutableStateOf(PdfLoadingState.Initializing); internal set
    var webViewError: WebViewError? by mutableStateOf(null); internal set

    var pagesCount by mutableIntStateOf(0); internal set
    var currentPage by mutableIntStateOf(0); internal set
    var currentScale by mutableFloatStateOf(0f); internal set
    var properties by mutableStateOf<PdfDocumentProperties?>(null); internal set
    var passwordRequired by mutableStateOf(false); internal set
    var scrollState by mutableStateOf(ScrollState()); internal set
    var matchState: MatchState by mutableStateOf(MatchState.Initialized()); internal set
    var scrollMode by mutableStateOf(PdfViewer.PageScrollMode.SINGLE_PAGE); internal set
    var spreadMode by mutableStateOf(PdfViewer.PageSpreadMode.NONE); internal set
    var rotation by mutableStateOf(PdfViewer.PageRotation.R_0); internal set
    var scaleLimit by mutableStateOf(ScaleLimit()); internal set
    var actualScaleLimit by mutableStateOf(ActualScaleLimit()); internal set
    var snapPage by mutableStateOf(false); internal set
    var singlePageArrangement by mutableStateOf(false); internal set
    var alignMode by mutableStateOf(PdfViewer.PageAlignMode.DEFAULT); internal set
    var printState: PdfPrintState by mutableStateOf(PdfPrintState.Idle); internal set

    @PdfUnstableApi
    var scrollSpeedLimit: PdfViewer.ScrollSpeedLimit by mutableStateOf(PdfViewer.ScrollSpeedLimit.None); internal set
    val editor = Editor()

    inner class Editor internal constructor() {
        var highlightColor by mutableStateOf(
            highlightEditorColors.firstOrNull()?.second
                ?: PdfViewerDefaults.highlightEditorColors.first().second
        )
            internal set
        var highlightThickness by mutableIntStateOf(12); internal set
        var showAllHighlights by mutableStateOf(true); internal set
        var freeFontColor by mutableStateOf(Color.Black); internal set
        var freeFontSize by mutableIntStateOf(10); internal set
        var inkThickness by mutableIntStateOf(1); internal set
        var inkColor by mutableStateOf(Color.Black); internal set
        var inkOpacity by mutableIntStateOf(100); internal set
    }

    fun clearFind() {
        matchState = MatchState.Initialized()
    }

    internal fun setPdfViewerTo(viewer: PdfViewer) {
        if (pdfViewer == viewer) return

        this.pdfViewer = viewer
        viewer.addListener(Listener())
        viewer.onReady { this@PdfState.loadingState = PdfLoadingState.Loading(0f) }

        if (viewer.isInitialized) {
            if (viewer.pagesCount == 0)
                this@PdfState.loadingState = PdfLoadingState.Loading(0f)
            else this@PdfState.loadingState = PdfLoadingState.Finished(viewer.pagesCount)
        } else this@PdfState.loadingState = PdfLoadingState.Initializing

        pagesCount = viewer.pagesCount
        currentPage = viewer.currentPage
        currentScale = viewer.currentPageScale
        properties = viewer.properties
        passwordRequired = false
        scrollState = ScrollState()
        matchState = MatchState.Initialized()
        scrollMode = viewer.pageScrollMode
        spreadMode = viewer.pageSpreadMode
        rotation = viewer.pageRotation
        scaleLimit = ScaleLimit(
            viewer.minPageScale,
            viewer.maxPageScale,
            viewer.defaultPageScale
        )
        actualScaleLimit = ActualScaleLimit(
            viewer.actualMinPageScale,
            viewer.actualMaxPageScale,
            viewer.actualDefaultPageScale
        )
        snapPage = viewer.snapPage
        singlePageArrangement = viewer.singlePageArrangement
        alignMode = viewer.pageAlignMode

        @OptIn(PdfUnstableApi::class)
        scrollSpeedLimit = viewer.scrollSpeedLimit
        editor.highlightColor = Color(viewer.editor.highlightColor)
        editor.highlightThickness = viewer.editor.highlightThickness
        editor.showAllHighlights = viewer.editor.showAllHighlights
        editor.freeFontColor = Color(viewer.editor.freeFontColor)
        editor.freeFontSize = viewer.editor.freeFontSize
        editor.inkThickness = viewer.editor.inkThickness
        editor.inkColor = Color(viewer.editor.inkColor)
        editor.inkOpacity = viewer.editor.inkOpacity
    }

    internal fun clearPdfViewer() {
        pdfViewer = null
    }

    inner class Listener internal constructor() : PdfListener {
        override fun onPageLoadStart() {
            this@PdfState.loadingState = PdfLoadingState.Loading(0f)
        }

        override fun onPageLoadSuccess(pagesCount: Int) {
            this@PdfState.loadingState = PdfLoadingState.Finished(pagesCount)
            this@PdfState.pagesCount = pagesCount
            this@PdfState.currentPage = 1
        }

        override fun onPageLoadFailed(exception: Exception) {
            this@PdfState.loadingState = PdfLoadingState.Error(exception)
        }

        override fun onReceivedError(error: WebViewError) {
            this@PdfState.webViewError = error
        }

        override fun onProgressChange(progress: Float) {
            this@PdfState.loadingState = PdfLoadingState.Loading(progress)
        }

        override fun onPageChange(pageNumber: Int) {
            this@PdfState.currentPage = pageNumber
        }

        override fun onScaleChange(scale: Float) {
            this@PdfState.currentScale = scale
        }

        override fun onFindMatchStart() {
            this@PdfState.matchState = MatchState.Started()
        }

        override fun onFindMatchChange(current: Int, total: Int) {
            this@PdfState.matchState = MatchState.Progress(current, total)
        }

        override fun onFindMatchComplete(found: Boolean) {
            this@PdfState.matchState =
                MatchState.Completed(found, matchState.current, matchState.total)
        }

        override fun onScrollChange(
            currentOffset: Int,
            totalOffset: Int,
            isHorizontalScroll: Boolean
        ) {
            this@PdfState.scrollState = ScrollState(currentOffset, totalOffset, isHorizontalScroll)
        }

        override fun onLoadProperties(properties: PdfDocumentProperties) {
            this@PdfState.properties = properties
        }

        override fun onPasswordDialogChange(isOpen: Boolean) {
            this@PdfState.passwordRequired = isOpen
        }

        override fun onScrollModeChange(scrollMode: PdfViewer.PageScrollMode) {
            this@PdfState.scrollMode = scrollMode
        }

        override fun onSpreadModeChange(spreadMode: PdfViewer.PageSpreadMode) {
            this@PdfState.spreadMode = spreadMode
        }

        override fun onRotationChange(rotation: PdfViewer.PageRotation) {
            this@PdfState.rotation = rotation
        }

        override fun onScaleLimitChange(
            minPageScale: Float,
            maxPageScale: Float,
            defaultPageScale: Float
        ) {
            this@PdfState.scaleLimit = ScaleLimit(minPageScale, maxPageScale, defaultPageScale)
        }

        override fun onActualScaleLimitChange(
            minPageScale: Float,
            maxPageScale: Float,
            defaultPageScale: Float
        ) {
            this@PdfState.actualScaleLimit =
                ActualScaleLimit(minPageScale, maxPageScale, defaultPageScale)
        }

        override fun onSnapChange(snapPage: Boolean) {
            this@PdfState.snapPage = snapPage
        }

        override fun onSinglePageArrangementChange(
            requestedArrangement: Boolean,
            appliedArrangement: Boolean
        ) {
            this@PdfState.singlePageArrangement = appliedArrangement
        }

        override fun onAlignModeChange(
            requestedMode: PdfViewer.PageAlignMode,
            appliedMode: PdfViewer.PageAlignMode
        ) {
            this@PdfState.alignMode = appliedMode
        }

        override fun onScrollSpeedLimitChange(
            requestedLimit: PdfViewer.ScrollSpeedLimit,
            appliedLimit: PdfViewer.ScrollSpeedLimit
        ) {
            @OptIn(PdfUnstableApi::class)
            this@PdfState.scrollSpeedLimit = appliedLimit
        }

        override fun onEditorHighlightColorChange(@ColorInt highlightColor: Int) {
            this@PdfState.editor.highlightColor = Color(highlightColor)
        }

        override fun onEditorShowAllHighlightsChange(showAll: Boolean) {
            this@PdfState.editor.showAllHighlights = showAll
        }

        override fun onEditorHighlightThicknessChange(@IntRange(from = 8, to = 24) thickness: Int) {
            this@PdfState.editor.highlightThickness = thickness
        }

        override fun onEditorFreeFontColorChange(@ColorInt fontColor: Int) {
            this@PdfState.editor.freeFontColor = Color(fontColor)
        }

        override fun onEditorFreeFontSizeChange(fontSize: Int) {
            this@PdfState.editor.freeFontSize = fontSize
        }

        override fun onEditorInkColorChange(color: Int) {
            this@PdfState.editor.inkColor = Color(color)
        }

        override fun onEditorInkThicknessChange(thickness: Int) {
            this@PdfState.editor.inkThickness = thickness
        }

        override fun onEditorInkOpacityChange(opacity: Int) {
            this@PdfState.editor.inkOpacity = opacity
        }

        override fun onPrintProcessStart() {
            this@PdfState.printState = PdfPrintState.Loading(0f)
        }

        override fun onPrintProcessProgress(progress: Float) {
            this@PdfState.printState = PdfPrintState.Loading(progress)
        }

        override fun onPrintProcessEnd() {
            this@PdfState.printState = PdfPrintState.Idle
        }

        override fun onPrintCancelled() {
            this@PdfState.printState = PdfPrintState.Idle
        }
    }
}

sealed class PdfSource {

    data class Asset(val assetPath: String) : PdfSource()

    data class ContentUri(val contentUri: Uri) : PdfSource() {
        constructor(contentUri: String) : this(contentUri.toUri())
    }

    data class File(val file: java.io.File) : PdfSource() {
        constructor(filePath: String) : this(java.io.File(filePath))
    }

    data class Plain(val source: String) : PdfSource() {
        constructor(source: Uri) : this(source.toString())
    }

    data class Url(val url: String) : PdfSource() {
        constructor(url: Uri) : this(url.toString())
    }
}

sealed interface PdfLoadingState {
    data object Initializing : PdfLoadingState
    data class Loading(@param:FloatRange(0.0, 1.0) val progress: Float) : PdfLoadingState
    data class Finished(val pagesCount: Int) : PdfLoadingState
    data class Error(val exception: Exception) : PdfLoadingState {
        val message: String? get() = exception.message
    }

    val isLoading: Boolean get() = this is Initializing || this is Loading
    val isInitialized: Boolean get() = this !is Initializing
}

data class ScrollState(
    val currentOffset: Int = 0,
    val totalOffset: Int = 0,
    val isHorizontalScroll: Boolean = false,
) {
    val ratio: Float get() = currentOffset.toFloat() / totalOffset.toFloat()
}

sealed class MatchState(val current: Int = 0, val total: Int = 0) {
    class Initialized(current: Int = 0, total: Int = 0) : MatchState(current, total)
    class Started(current: Int = 0, total: Int = 0) : MatchState(current, total)
    class Progress(current: Int, total: Int) : MatchState(current, total)
    class Completed(val found: Boolean, current: Int, total: Int) : MatchState(current, total)

    val isLoading: Boolean get() = this is Started || this is Progress
}

sealed interface PdfPrintState {
    data object Idle : PdfPrintState
    data class Loading(@param:FloatRange(0.0, 1.0) val progress: Float) : PdfPrintState

    val isLoading: Boolean get() = this is Loading
}
