package com.acutecoder.pdfviewer.compose

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.acutecoder.pdf.PdfDocumentProperties
import com.acutecoder.pdf.PdfListener
import com.acutecoder.pdf.PdfViewer
import com.acutecoder.pdf.PdfViewer.Zoom

@Composable
fun rememberPdfState(source: String): PdfState = remember { PdfState(source = source) }

class PdfState(source: String) {

    var source by mutableStateOf(source); internal set
    var pdfViewer: PdfViewer? by mutableStateOf(null); internal set
    var isInitialized by mutableStateOf(false); internal set

    var isLoading by mutableStateOf(false); internal set
    var errorMessage by mutableStateOf<String?>(null); internal set
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

    fun load(source: String) {
        this.source = source
    }

    fun setViewer(viewer: PdfViewer) {
        this.pdfViewer = viewer
        viewer.addListener(Listener())
        viewer.onReady { this@PdfState.isInitialized = true }

        pagesCount = viewer.pagesCount
        currentPage = viewer.currentPage
        currentScale = viewer.currentPageScale
        properties = viewer.properties
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
    }

    inner class Listener internal constructor() : PdfListener {
        override fun onPageLoadStart() {
            isLoading = true
            errorMessage = null
        }

        override fun onPageLoadSuccess(pagesCount: Int) {
            isLoading = false
            this@PdfState.pagesCount = pagesCount
            currentPage = 1
        }

        override fun onPageLoadFailed(errorMessage: String) {
            this@PdfState.errorMessage = errorMessage
        }

        override fun onPageChange(pageNumber: Int) {
            currentPage = pageNumber
        }

        override fun onScaleChange(scale: Float) {
            currentScale = scale
        }

        override fun onFindMatchStart() {
            matchState = MatchState.Initialized()
        }

        override fun onFindMatchChange(current: Int, total: Int) {
            matchState = MatchState.Progress(current, total)
        }

        override fun onFindMatchComplete(found: Boolean) {
            matchState = MatchState.Completed(found, matchState.current, matchState.total)
        }

        override fun onScrollChange(
            currentOffset: Int,
            totalOffset: Int,
            isHorizontalScroll: Boolean
        ) {
            scrollState = ScrollState(currentOffset, totalOffset, isHorizontalScroll)
        }

        override fun onLoadProperties(properties: PdfDocumentProperties) {
            this@PdfState.properties = properties
        }

        override fun onPasswordDialogChange(isOpen: Boolean) {
            passwordRequired = isOpen
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
            scaleLimit = ScaleLimit(minPageScale, maxPageScale, defaultPageScale)
        }

        override fun onActualScaleLimitChange(
            minPageScale: Float,
            maxPageScale: Float,
            defaultPageScale: Float
        ) {
            actualScaleLimit = ActualScaleLimit(minPageScale, maxPageScale, defaultPageScale)
        }
    }
}

data class ScrollState(
    val currentOffset: Int = 0,
    val totalOffset: Int = 0,
    val isHorizontalScroll: Boolean = false,
) {
    val ratio: Float
        get() = currentOffset.toFloat() / totalOffset.toFloat()
}

sealed class MatchState(val current: Int = 0, val total: Int = 0) {
    class Initialized(current: Int = 0, total: Int = 0) : MatchState(current, total)
    class Progress(current: Int, total: Int) : MatchState(current, total)
    class Completed(val found: Boolean, current: Int, total: Int) : MatchState(current, total)
}

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
