package com.bhuvaneshw.pdf

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.util.AttributeSet
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.bhuvaneshw.pdf.js.Body
import com.bhuvaneshw.pdf.js.call
import com.bhuvaneshw.pdf.js.callDirectly
import com.bhuvaneshw.pdf.js.invoke
import com.bhuvaneshw.pdf.js.set
import com.bhuvaneshw.pdf.js.setDirectly
import com.bhuvaneshw.pdf.js.toJsHex
import com.bhuvaneshw.pdf.js.toJsRgba
import com.bhuvaneshw.pdf.js.toJsString
import com.bhuvaneshw.pdf.js.with
import com.bhuvaneshw.pdf.setting.UiSettings
import kotlin.math.abs

class PdfViewer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    var isInitialized = false; private set
    var currentSource: String? = null; private set
    var currentPage: Int = 1; private set
    var pagesCount: Int = 0; private set
    var currentPageScale: Float = 0f; private set
    var currentPageScaleValue: String = ""; private set
    var properties: PdfDocumentProperties? = null; private set

    /**
     * Changes require reinitializing PdfViewer
     */
    var highlightEditorColors: List<Pair<String, Int>> = defaultHighlightEditorColors
    var pdfPrintAdapter: PrintDocumentAdapter? = null

    private val listeners = mutableListOf<PdfListener>()
    private val webInterface = WebInterface()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var onReadyListeners = mutableListOf<PdfViewer.() -> Unit>()
    private var tempBackgroundColor: Int? = null

    @SuppressLint("SetJavaScriptEnabled")
    private val webView: WebView = WebView(context).apply {
        setBackgroundColor(Color.TRANSPARENT)

        if (isInEditMode) return@apply

        settings.run {
            javaScriptEnabled = true
            allowFileAccess = true
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = true
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()

                if (url.startsWith("file:///android_asset/"))
                    return super.shouldOverrideUrlLoading(view, request)

                if (URLUtil.isValidUrl(url))
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (view == null) return

                if (!isInitialized) {
                    view callDirectly "setupHelper" {
                        post {
                            isInitialized = true
                            tempBackgroundColor?.let { setContainerBackgroundColor(it) }
                            onReadyListeners.forEach { it(this@PdfViewer) }
                        }
                    }
                }
            }
        }

        setDownloadListener { url, _, _, _, _ ->
            webInterface.getBase64StringFromBlobUrl(url)?.let { evaluateJavascript(it, null) }
        }
    }

    val ui = UiSettings(webView)
        get() {
            checkViewer()
            return field
        }

    val findController = FindController(webView)
        get() {
            checkViewer()
            return field
        }

    var pageScrollMode: PageScrollMode = PageScrollMode.VERTICAL
        set(value) {
            checkViewer()
            field = value
            webView callDirectly value.function()
            adjustAlignModeAndArrangementMode(value)
            dispatchSnapChange(snapPage, false)
        }

    var pageSpreadMode: PageSpreadMode = PageSpreadMode.NONE
        set(value) {
            checkViewer()
            field = value
            webView callDirectly value.function()
            if (value != PageSpreadMode.NONE && singlePageArrangement)
                singlePageArrangement = false
        }

    var cursorToolMode: CursorToolMode = CursorToolMode.TEXT_SELECT
        set(value) {
            checkViewer()
            field = value
            webView callDirectly value.function()
        }

    var pageRotation: PageRotation = PageRotation.R_0
        set(value) {
            checkViewer()
            field = value
            dispatchRotationChange(value)
        }

    var doubleClickThreshold: Long = 300
        set(value) {
            checkViewer()
            field = value
            webView setDirectly "DOUBLE_CLICK_THRESHOLD"(value)
        }

    var longClickThreshold: Long = 500
        set(value) {
            checkViewer()
            field = value
            webView setDirectly "LONG_CLICK_THRESHOLD"(value)
        }

    @FloatRange(from = -4.0, to = 10.0)
    var minPageScale = 0.1f
        set(value) {
            field = value
            if (isInitialized) {
                if (value in ZOOM_SCALE_RANGE) getActualScaleFor(Zoom.entries[abs(value.toInt()) - 1]) {
                    actualMinPageScale = it ?: actualMinPageScale
                } else actualMinPageScale = value
            }
            if (field != value)
                listeners.forEach { it.onScaleLimitChange(value, maxPageScale, defaultPageScale) }
        }

    @FloatRange(from = -4.0, to = 10.0)
    var maxPageScale = 10f
        set(value) {
            if (field != value)
                listeners.forEach { it.onScaleLimitChange(minPageScale, value, defaultPageScale) }
            field = value
            if (isInitialized) {
                if (value in ZOOM_SCALE_RANGE) getActualScaleFor(Zoom.entries[abs(value.toInt()) - 1]) {
                    actualMaxPageScale = it ?: actualMaxPageScale
                } else actualMaxPageScale = value
            }
        }

    @FloatRange(from = -4.0, to = 10.0)
    var defaultPageScale = Zoom.AUTOMATIC.floatValue
        set(value) {
            if (field != value)
                listeners.forEach { it.onScaleLimitChange(minPageScale, maxPageScale, value) }
            field = value
            if (isInitialized) {
                if (value in ZOOM_SCALE_RANGE) getActualScaleFor(Zoom.entries[abs(value.toInt()) - 1]) {
                    actualDefaultPageScale = it ?: actualDefaultPageScale
                    scalePageTo(actualDefaultPageScale)
                } else {
                    actualDefaultPageScale = value
                    scalePageTo(value)
                }
            }
        }

    var actualMinPageScale = 0f
        private set(value) {
            if (field != value)
                listeners.forEach {
                    it.onActualScaleLimitChange(value, actualMaxPageScale, actualDefaultPageScale)
                }
            field = value
            if (value > 0) webView setDirectly "MIN_SCALE"(value)
        }
    var actualMaxPageScale = 0f
        private set(value) {
            if (field != value)
                listeners.forEach {
                    it.onActualScaleLimitChange(actualMinPageScale, value, actualDefaultPageScale)
                }
            field = value
            if (value > 0) webView setDirectly "MAX_SCALE"(value)
        }
    var actualDefaultPageScale = 0f
        private set(value) {
            if (field != value)
                listeners.forEach {
                    it.onActualScaleLimitChange(actualMinPageScale, actualMaxPageScale, value)
                }
            field = value
        }

    var snapPage = false
        set(value) {
            checkViewer()
            field = value
            dispatchSnapChange(value)
        }

    var pageAlignMode = PageAlignMode.DEFAULT
        set(value) {
            checkViewer()
            field = dispatchPageAlignMode(value)
        }

    var singlePageArrangement = false
        set(value) {
            checkViewer()
            field = dispatchSinglePageArrangement(value)
        }

    @PdfUnstableApi
    var scrollSpeedLimit: ScrollSpeedLimit = ScrollSpeedLimit.None
        set(value) {
            checkViewer()
            field = dispatchScrollSpeedLimit(value)
        }

    val editor = Editor()

    init {
        val containerBgColor = attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.PdfViewer, defStyleAttr, 0)
            val color =
                typedArray.getColor(R.styleable.PdfViewer_containerBackgroundColor, COLOR_NOT_FOUND)
            typedArray.recycle()
            color
        } ?: COLOR_NOT_FOUND

        if (!isInEditMode) {
            addView(webView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            webView.addJavascriptInterface(webInterface, "JWI")
            loadPage()

            if (containerBgColor != COLOR_NOT_FOUND)
                setContainerBackgroundColor(containerBgColor)
        } else setPreviews(context, containerBgColor)
    }

    @JvmOverloads
    fun load(source: String, originalUrl: String = source) {
        checkViewer()
        currentPage = 1
        pagesCount = 0
        currentPageScale = 0f
        currentPageScaleValue = ""
        properties = null
        currentSource = source

        listeners.forEach { it.onPageLoadStart() }
        webView callDirectly "openFile"("{url: '$source', originalUrl: '$originalUrl'}")
    }

    fun onReady(onReady: PdfViewer.() -> Unit) {
        onReadyListeners.add(onReady)
        if (isInitialized) onReady(this)
    }

    fun addListener(listener: PdfListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: PdfListener) {
        listeners.remove(listener)
    }

    fun clearAllListeners() {
        onReadyListeners.clear()
        listeners.clear()
    }

    fun goToPage(@IntRange(from = 1) pageNumber: Int): Boolean {
        if (pageNumber in 1..pagesCount) {
            webView set "page"(pageNumber)
            return true
        }

        return false
    }

    @JvmOverloads
    fun scrollToRatio(
        @FloatRange(from = 0.0, to = 1.0) ratio: Float,
        isHorizontalScroll: Boolean = pageScrollMode == PageScrollMode.HORIZONTAL
    ) {
        webView callDirectly "scrollToRatio"(ratio, isHorizontalScroll)
    }

    fun scrollTo(@IntRange(from = 0) offset: Int) {
        webView callDirectly "scrollTo"(offset)
    }

    fun goToNextPage() = goToPage(currentPage + 1)

    fun goToPreviousPage() = goToPage(currentPage - 1)

    fun goToFirstPage() {
        webView callDirectly "goToFirstPage"()
    }

    fun goToLastPage() {
        webView callDirectly "goToLastPage"()
    }

    fun scalePageTo(@FloatRange(from = -4.0, to = 10.0) scale: Float) {
        if (scale in ZOOM_SCALE_RANGE)
            zoomTo(Zoom.entries[abs(scale.toInt()) - 1])
        else {
            if (actualMaxPageScale < actualMinPageScale)
                throw RuntimeException("Max Page Scale($actualMaxPageScale) is less than Min Page Scale($actualMinPageScale)")
            webView set "pdfViewer.currentScale"(
                scale.coerceIn(actualMinPageScale, actualMaxPageScale)
            )
        }
    }

    fun zoomIn() {
        webView call "zoomIn"()
    }

    fun zoomOut() {
        webView call "zoomOut"()
    }

    fun zoomTo(zoom: Zoom) {
        getActualScaleFor(zoom) { scale ->
            if (scale != null && scale in actualMinPageScale..actualMaxPageScale)
                webView set "pdfViewer.currentScaleValue"(zoom.value.toJsString())
        }
    }

    fun zoomToMaximum() {
        scalePageTo(actualMaxPageScale)
    }

    fun zoomToMinimum() {
        scalePageTo(actualMinPageScale)
    }

    fun isZoomInMaxScale(): Boolean {
        return currentPageScale == actualMaxPageScale
    }

    fun isZoomInMinScale(): Boolean {
        return currentPageScale == actualMinPageScale
    }

    fun downloadFile() {
        webView callDirectly "downloadFile"()
    }

    @PdfUnstableApi
    fun printFile() {
        webView callDirectly "printFile"()
    }

    @PdfUnstableApi
    fun startPresentationMode() {
        webView callDirectly "startPresentationMode"()
    }

    fun rotateClockWise() {
        pageRotation = PageRotation.entries.let { it[(it.indexOf(pageRotation) + 1) % it.size] }
    }

    fun rotateCounterClockWise() {
        pageRotation = PageRotation.entries.let {
            it[(it.indexOf(pageRotation) - 1).let { index ->
                if (index < 0) index + it.size else index
            }]
        }
    }

    fun showDocumentProperties() {
        webView callDirectly "showDocumentProperties"()
    }

    fun reInitialize() {
        isInitialized = false
        webView.reload()
    }

    fun setContainerBackgroundColor(@ColorInt color: Int) {
        if (!isInitialized) {
            tempBackgroundColor = color
            return
        }
        if (tempBackgroundColor != null) tempBackgroundColor = null

        webView with Body set "style.backgroundColor"(color.toJsRgba().toJsString())
    }

    fun saveState(outState: Bundle) {
        webView.saveState(outState)
    }

    fun restoreState(inState: Bundle) {
        webView.restoreState(inState)
    }

    fun getActualScaleFor(zoom: Zoom, callback: (scale: Float?) -> Unit) {
        webView callDirectly "getActualScaleFor"(zoom.value.toJsString()) {
            callback(it?.toFloatOrNull())
        }
    }

    private fun loadPage() {
        webView.loadUrl(PDF_VIEWER_URL)
    }

    private fun checkViewer() {
        if (!isInitialized) throw PdfViewerNotInitializedException()
    }

    private fun dispatchRotationChange(
        pageRotation: PageRotation,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onRotationChange(pageRotation) }
        ) {
            webView set "pdfViewer.pagesRotation"(pageRotation.degree)
        }
    }

    private fun dispatchSnapChange(
        snapPage: Boolean,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onSnapChange(snapPage) }
        ) {
            if (snapPage) {
                when (pageScrollMode) {
                    PageScrollMode.HORIZONTAL -> webView callDirectly "enableHorizontalSnapBehavior"()
                    PageScrollMode.VERTICAL, PageScrollMode.WRAPPED -> webView callDirectly "enableVerticalSnapBehavior"()
                    else -> {}
                }
            } else webView callDirectly "removeSnapBehavior"()
        }
    }

    private fun dispatchPageAlignMode(
        pageAlignMode: PageAlignMode,
        dispatchToListener: Boolean = true,
    ): PageAlignMode {
        return dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onAlignModeChange(pageAlignMode, it) }
        ) {
            val alignMode = adjustAlignMode(pageAlignMode)
            webView callDirectly "centerPage"(
                alignMode.vertical,
                alignMode.horizontal,
                singlePageArrangement
            )
            alignMode
        }
    }

    private fun adjustAlignMode(alignMode: PageAlignMode): PageAlignMode {
        if (singlePageArrangement) return alignMode

        when (pageScrollMode) {
            PageScrollMode.VERTICAL, PageScrollMode.WRAPPED -> {
                if (alignMode == PageAlignMode.CENTER_VERTICAL || alignMode == PageAlignMode.CENTER_BOTH)
                    return PageAlignMode.DEFAULT
            }

            PageScrollMode.HORIZONTAL -> {
                if (alignMode == PageAlignMode.CENTER_HORIZONTAL || alignMode == PageAlignMode.CENTER_BOTH)
                    return PageAlignMode.DEFAULT
            }

            PageScrollMode.SINGLE_PAGE -> {}
        }

        return alignMode
    }

    private fun dispatchSinglePageArrangement(
        singlePageArrangement: Boolean,
        dispatchToListener: Boolean = true,
    ): Boolean {
        return dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onSinglePageArrangementChange(singlePageArrangement, it) }
        ) {
            val newValue =
                if (singlePageArrangement) {
                    (pageScrollMode == PageScrollMode.VERTICAL || pageScrollMode == PageScrollMode.HORIZONTAL)
                            && pageSpreadMode == PageSpreadMode.NONE
                } else false
            webView callDirectly if (newValue) "applySinglePageArrangement"() else "removeSinglePageArrangement"()
            newValue
        }
    }

    private fun dispatchHighlightColor(
        highlightColor: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorHighlightColorChange(highlightColor) }
        ) {
            webView callDirectly "selectHighlighterColor"(highlightColor.toJsHex().toJsString())
        }
    }

    private fun dispatchShowAllHighlights(
        showAll: Boolean,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorShowAllHighlightsChange(showAll) }
        ) {
            webView callDirectly if (showAll) "showAllHighlights"() else "hideAllHighlights"()
        }
    }

    private fun dispatchHighlightThickness(
        @IntRange(from = 8, to = 24) thickness: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorHighlightThicknessChange(thickness) }
        ) {
            webView callDirectly "setHighlighterThickness"(thickness)
        }
    }

    private fun dispatchFreeFontColor(
        @ColorInt fontColor: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorFreeFontColorChange(fontColor) }
        ) {
            webView callDirectly "setFreeTextFontColor"(
                fontColor.toJsHex(includeAlpha = false).toJsString()
            )
        }
    }

    private fun dispatchFreeFontSize(
        @IntRange(from = 5, to = 100) fontSize: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorFreeFontSizeChange(fontSize) }
        ) {
            webView callDirectly "setFreeTextFontSize"(fontSize)
        }
    }

    private fun dispatchInkColor(
        @ColorInt color: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorInkColorChange(color) }
        ) {
            webView callDirectly "setInkColor"(color.toJsHex(includeAlpha = false).toJsString())
        }
    }

    private fun dispatchInkThickness(
        @IntRange(from = 1, to = 20) thickness: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorInkThicknessChange(thickness) }
        ) {
            webView callDirectly "setInkThickness"(thickness)
        }
    }

    private fun dispatchInkOpacity(
        @IntRange(from = 1, to = 100) opacity: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorInkOpacityChange(opacity) }
        ) {
            webView callDirectly "setInkOpacity"(opacity)
        }
    }

    private fun dispatchScrollSpeedLimit(
        scrollSpeedLimit: ScrollSpeedLimit,
        dispatchToListener: Boolean = true,
    ): ScrollSpeedLimit {
        return dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onScrollSpeedLimitChange(scrollSpeedLimit, it) }
        ) {
            if (!singlePageArrangement) {
                webView callDirectly "removeScrollLimit"()
                return@dispatch ScrollSpeedLimit.None
            }
            webView callDirectly when (scrollSpeedLimit) {
                is ScrollSpeedLimit.AdaptiveFling -> "limitScroll"(
                    scrollSpeedLimit.limit,
                    scrollSpeedLimit.flingThreshold,
                    true,
                    true,
                )

                is ScrollSpeedLimit.Fixed -> "limitScroll"(
                    scrollSpeedLimit.limit,
                    scrollSpeedLimit.flingThreshold,
                    scrollSpeedLimit.canFling,
                    false,
                )

                ScrollSpeedLimit.None -> "removeScrollLimit"()
            }
            scrollSpeedLimit
        }
    }

    private inline fun <T> dispatch(
        dispatchToListener: Boolean = true,
        callListener: PdfListener.(result: T) -> Unit,
        block: () -> T,
    ): T {
        val result = block()
        if (dispatchToListener) listeners.forEach { callListener(it, result) }
        return result
    }

    private fun adjustAlignModeAndArrangementMode(scrollMode: PageScrollMode) {
        if (singlePageArrangement) {
            if (scrollMode != PageScrollMode.VERTICAL && scrollMode != PageScrollMode.HORIZONTAL)
                singlePageArrangement = false
            else return
        }

        when (scrollMode) {
            PageScrollMode.VERTICAL, PageScrollMode.WRAPPED -> {
                if (pageAlignMode == PageAlignMode.CENTER_VERTICAL || pageAlignMode == PageAlignMode.CENTER_BOTH)
                    pageAlignMode = PageAlignMode.DEFAULT
            }

            PageScrollMode.HORIZONTAL -> {
                if (pageAlignMode == PageAlignMode.CENTER_HORIZONTAL || pageAlignMode == PageAlignMode.CENTER_BOTH)
                    pageAlignMode = PageAlignMode.DEFAULT
            }

            PageScrollMode.SINGLE_PAGE -> {
                pageAlignMode = pageAlignMode
            }
        }
    }

    private fun setUpActualScaleValues(callback: () -> Unit) {
        var isMinSet = false
        var isMaxSet = false
        var isDefaultSet = false
        fun checkAndCall() {
            if (isMinSet && isMaxSet && isDefaultSet) callback()
        }

        if (minPageScale in ZOOM_SCALE_RANGE)
            getActualScaleFor(Zoom.entries[abs(minPageScale.toInt()) - 1]) {
                actualMinPageScale = it ?: actualMinPageScale
                isMinSet = true
                checkAndCall()
            }
        else {
            actualMinPageScale = minPageScale
            isMinSet = true
            checkAndCall()
        }

        if (maxPageScale in ZOOM_SCALE_RANGE)
            getActualScaleFor(Zoom.entries[abs(maxPageScale.toInt()) - 1]) {
                actualMaxPageScale = it ?: actualMaxPageScale
                isMaxSet = true
                checkAndCall()
            }
        else {
            actualMaxPageScale = maxPageScale
            isMaxSet = true
            checkAndCall()
        }

        if (defaultPageScale in ZOOM_SCALE_RANGE)
            getActualScaleFor(Zoom.entries[abs(defaultPageScale.toInt()) - 1]) {
                actualDefaultPageScale = it ?: actualDefaultPageScale
                isDefaultSet = true
                checkAndCall()
            }
        else {
            actualDefaultPageScale = defaultPageScale
            isDefaultSet = true
            checkAndCall()
        }
    }

    enum class Zoom(internal val value: String, val floatValue: Float) {
        AUTOMATIC("auto", -1f),
        PAGE_FIT("page-fit", -2f),
        PAGE_WIDTH("page-width", -3f),
        ACTUAL_SIZE("page-actual", -4f)
    }

    enum class CursorToolMode(internal val function: String) {
        TEXT_SELECT("selectCursorSelectTool"),
        HAND("selectCursorHandTool")
    }

    enum class PageScrollMode(internal val function: String) {
        VERTICAL("selectScrollVertical"),
        HORIZONTAL("selectScrollHorizontal"),
        WRAPPED("selectScrollWrapped"),
        SINGLE_PAGE("selectScrollPage")
    }

    enum class PageSpreadMode(internal val function: String) {
        NONE("selectSpreadNone"),
        ODD("selectSpreadOdd"),
        EVEN("selectSpreadEven")
    }

    enum class PageRotation(internal val degree: Int) {
        R_0(0),
        R_90(90),
        R_180(180),
        R_270(270),
    }

    enum class PageAlignMode(internal val vertical: Boolean, internal val horizontal: Boolean) {
        DEFAULT(false, false),
        CENTER_VERTICAL(true, false),
        CENTER_HORIZONTAL(false, true),
        CENTER_BOTH(true, true),
    }

    sealed class ScrollSpeedLimit private constructor() {

        /**
         * Default behavior. No limit is applied.
         */
        data object None : ScrollSpeedLimit()

        /**
         * Applies scroll speed limit
         *
         * Flings based on the parameter - canFling
         */
        data class Fixed(
            @FloatRange(from = 0.0, fromInclusive = false) val limit: Float = 100f,
            @FloatRange(from = 0.0, fromInclusive = false) val flingThreshold: Float = 0.5f,
            val canFling: Boolean = false,
        ) : ScrollSpeedLimit()

        /**
         * Applies scroll speed limit
         *
         * Flings only when the page size is less than its container's size.
         */
        data class AdaptiveFling(
            @FloatRange(from = 0.0, fromInclusive = false) val limit: Float = 100f,
            @FloatRange(from = 0.0, fromInclusive = false) val flingThreshold: Float = 0.5f,
        ) : ScrollSpeedLimit()
    }

    inner class Editor internal constructor() {
        var textHighlighterOn = false
            set(value) {
                field = value
                webView callDirectly if (value) "openTextHighlighter"() else "closeTextHighlighter"()
            }

        var freeTextOn = false
            set(value) {
                field = value
                webView callDirectly if (value) "openEditorFreeText"() else "closeEditorFreeText"()
            }

        var inkOn = false
            set(value) {
                field = value
                webView callDirectly if (value) "openEditorInk"() else "closeEditorInk"()
            }

        var stampOn = false
            set(value) {
                field = value
                webView callDirectly if (value) "openEditorStamp"() else "closeEditorStamp"()
            }

        var highlightColor =
            highlightEditorColors.firstOrNull()?.second
                ?: defaultHighlightEditorColors.first().second
            set(value) {
                field = value
                dispatchHighlightColor(value)
            }

        var showAllHighlights = true
            set(value) {
                field = value
                dispatchShowAllHighlights(value)
            }

        @IntRange(from = 8, to = 24)
        var highlightThickness = 12
            set(value) {
                field = value
                dispatchHighlightThickness(value)
            }

        @ColorInt
        var freeFontColor = Color.BLACK
            set(value) {
                field = value
                dispatchFreeFontColor(value)
            }

        @IntRange(from = 5, to = 100)
        var freeFontSize = 10
            set(value) {
                field = value
                dispatchFreeFontSize(value)
            }

        @ColorInt
        var inkColor = Color.BLACK
            set(value) {
                field = value
                dispatchInkColor(value)
            }

        @IntRange(from = 1, to = 20)
        var inkThickness = 1
            set(value) {
                field = value
                dispatchInkThickness(value)
            }

        @IntRange(from = 1, to = 100)
        var inkOpacity = 100
            set(value) {
                field = value
                dispatchInkOpacity(value)
            }

        val isEditing: Boolean get() = textHighlighterOn || freeTextOn || inkOn || stampOn

        fun undo() {
            webView callDirectly "undo"()
        }

        fun redo() {
            webView callDirectly "redo"()
        }

    }

    companion object {
        private const val PDF_VIEWER_URL =
            "file:///android_asset/com/bhuvaneshw/mozilla/pdfjs/pdf_viewer.html"
        private const val COLOR_NOT_FOUND = 11
        private val ZOOM_SCALE_RANGE = -4f..-1f

        val defaultHighlightEditorColors = listOf(
            "yellow" to Color.parseColor("#FFFF98"),
            "green" to Color.parseColor("#53FFBC"),
            "blue" to Color.parseColor("#80EBFF"),
            "pink" to Color.parseColor("#FFCBE6"),
            "red" to Color.parseColor("#FF4F5F"),
        )
    }

    @Suppress("Unused")
    private inner class WebInterface {
        private var findMatchStarted = false

        @JavascriptInterface
        fun onLoadSuccess(count: Int) = post {
            pagesCount = count
            setUpActualScaleValues {
                scalePageTo(actualDefaultPageScale)
            }
            dispatchRotationChange(pageRotation, false)
            dispatchSnapChange(snapPage, false)

            dispatchSinglePageArrangement(singlePageArrangement, false)
            dispatchPageAlignMode(pageAlignMode, false)
            @OptIn(PdfUnstableApi::class)
            dispatchScrollSpeedLimit(scrollSpeedLimit, false)

            listeners.forEach { it.onPageLoadSuccess(count) }
        }

        @JavascriptInterface
        fun onLoadFailed(error: String) = post {
            listeners.forEach { it.onPageLoadFailed(error) }
        }

        @JavascriptInterface
        fun onPageChange(pageNumber: Int) = post({ currentPage != pageNumber }) {
            currentPage = pageNumber
            listeners.forEach { it.onPageChange(pageNumber) }
        }

        @JavascriptInterface
        fun onScaleChange(scale: Float, scaleValue: String) =
            post({ currentPageScale != scale || currentPageScaleValue != scaleValue }) {
                currentPageScale = scale
                currentPageScaleValue = scaleValue
                listeners.forEach { it.onScaleChange(scale) }
            }

        @JavascriptInterface
        fun onFindMatchStart() = post {
            findMatchStarted = true
            listeners.forEach { it.onFindMatchStart() }
        }

        @JavascriptInterface
        fun onFindMatchChange(current: Int, total: Int) = post {
            if (findMatchStarted) listeners.forEach { it.onFindMatchChange(current, total) }
        }

        @JavascriptInterface
        fun onFindMatchComplete(found: Boolean) = post {
            if (findMatchStarted) listeners.forEach { it.onFindMatchComplete(found) }
            findMatchStarted = false
        }

        @JavascriptInterface
        fun onScroll(currentOffset: Int, totalOffset: Int, isHorizontal: Boolean) = post {
            if (pageScrollMode != PageScrollMode.SINGLE_PAGE)
                listeners.forEach { it.onScrollChange(currentOffset, totalOffset, isHorizontal) }
        }

        @JavascriptInterface
        fun onPasswordDialogChange(isOpen: Boolean) = post {
            listeners.forEach { it.onPasswordDialogChange(isOpen) }
        }

        @JavascriptInterface
        fun onSpreadModeChange(ordinal: Int) = post {
            listeners.forEach { it.onSpreadModeChange(PageSpreadMode.entries[ordinal]) }
        }

        @JavascriptInterface
        fun onScrollModeChange(ordinal: Int) = post {
            listeners.forEach { it.onScrollModeChange(PageScrollMode.entries[ordinal]) }
        }

        @JavascriptInterface
        fun onSingleClick() = post {
            listeners.forEach { it.onSingleClick() }
        }

        @JavascriptInterface
        fun onDoubleClick() = post {
            listeners.forEach { it.onDoubleClick() }
        }

        @JavascriptInterface
        fun onLongClick() = post {
            listeners.forEach { it.onLongClick() }
        }

        @JavascriptInterface
        fun onLinkClick(link: String) = post {
            if (!link.startsWith(PDF_VIEWER_URL))
                listeners.forEach { it.onLinkClick(link) }
        }

        @JavascriptInterface
        fun getHighlightEditorColorsString() = highlightEditorColors
            .joinToString(separator = ",") { "${it.first}=${it.second.toJsHex()}" }

        @JavascriptInterface
        fun onLoadProperties(
            title: String,
            subject: String,
            author: String,
            creator: String,
            producer: String,
            creationDate: String,
            modifiedDate: String,
            keywords: String,
            language: String,
            pdfFormatVersion: String,
            fileSize: Long,
            isLinearized: Boolean,
            encryptFilterName: String,
            isAcroFormPresent: Boolean,
            isCollectionPresent: Boolean,
            isSignaturesPresent: Boolean,
            isXFAPresent: Boolean,
            customJson: String
        ) = post {
            properties = PdfDocumentProperties(
                title = title,
                subject = subject,
                author = author,
                creator = creator,
                producer = producer,
                creationDate = creationDate,
                modifiedDate = modifiedDate,
                keywords = keywords,
                language = language,
                pdfFormatVersion = pdfFormatVersion,
                fileSize = fileSize,
                isLinearized = isLinearized,
                encryptFilterName = encryptFilterName,
                isAcroFormPresent = isAcroFormPresent,
                isCollectionPresent = isCollectionPresent,
                isSignaturesPresent = isSignaturesPresent,
                isXFAPresent = isXFAPresent,
                customJson = customJson,
            ).apply { listeners.forEach { it.onLoadProperties(this) } }
        }

        @JavascriptInterface
        fun createPrintJob() = post {
            pdfPrintAdapter?.let { pdfPrintAdapter ->
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "${context.packageName} Document"

                if (pdfPrintAdapter is PdfPrintAdapter)
                    pdfPrintAdapter.webView = webView

                printManager.print(
                    jobName,
                    pdfPrintAdapter,
                    PrintAttributes.Builder().build()
                )
            }
        }

        @JavascriptInterface
        fun conveyMessage(message: String) = post {
            pdfPrintAdapter?.let { pdfPrintAdapter ->
                if (pdfPrintAdapter is PdfPrintAdapter)
                    pdfPrintAdapter.onMessage(message)
            }
        }

        @JavascriptInterface
        fun getBase64FromBlobData(base64Data: String) {
            val pdfAsBytes: ByteArray = Base64.decode(
                base64Data.replaceFirst("^data:application/pdf;base64,".toRegex(), ""),
                0
            )

            post { listeners.forEach { it.onSavePdf(pdfAsBytes) } }
        }

        fun getBase64StringFromBlobUrl(blobUrl: String): String? {
            if (blobUrl.startsWith("blob")) {
                return "var xhr = new XMLHttpRequest();" +
                        "xhr.open('GET', '" + blobUrl + "', true);" +
                        "xhr.setRequestHeader('Content-type','application/pdf');" +
                        "xhr.responseType = 'blob';" +
                        "xhr.onload = function(e) {" +
                        "    if (this.status == 200) {" +
                        "        var blobPdf = this.response;" +
                        "        var reader = new FileReader();" +
                        "        reader.readAsDataURL(blobPdf);" +
                        "        reader.onloadend = function() {" +
                        "            base64data = reader.result;" +
                        "            JWI.getBase64FromBlobData(base64data);" +
                        "        }" +
                        "    }" +
                        "};" +
                        "xhr.send();"
            }
            return null
        }

        private inline fun post(
            crossinline condition: () -> Boolean = { true },
            runnable: Runnable
        ) {
            mainHandler.post {
                if (condition()) runnable.run()
            }
        }
    }

    private fun setPreviews(context: Context, containerBgColor: Int) {
        addView(
            LinearLayout(context).apply {
                orientation = VERTICAL
                if (containerBgColor == COLOR_NOT_FOUND) {
                    if (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES)
                        setBackgroundColor(Color.parseColor("#2A2A2E"))
                    else setBackgroundColor(Color.parseColor("#d4d4d7"))
                } else setBackgroundColor(containerBgColor)
                addView(createPageView(context, 1))
                addView(createPageView(context, 2))
                addView(createPageView(context, 3))
            },
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
    }

    @SuppressLint("SetTextI18n")
    private fun createPageView(context: Context, pageNo: Int): View {
        return TextView(context).apply {
            setBackgroundColor(Color.WHITE)
            layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f).apply {
                    setMargins(24, 24, 24, 0)
                }
            gravity = Gravity.CENTER
            text = "Page $pageNo"
            setTextColor(Color.BLACK)
        }
    }

}
