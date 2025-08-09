@file:SuppressLint("UseKtx")
@file:Suppress("unused")

package com.bhuvaneshw.pdf

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.print.PrintDocumentAdapter
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.webkit.WebView
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
import com.bhuvaneshw.pdf.resource.AssetResourceLoader
import com.bhuvaneshw.pdf.resource.ContentResourceLoader
import com.bhuvaneshw.pdf.resource.FileResourceLoader
import com.bhuvaneshw.pdf.resource.NetworkResourceLoader
import com.bhuvaneshw.pdf.resource.PdfViewerResourceLoader
import com.bhuvaneshw.pdf.resource.ResourceLoader
import com.bhuvaneshw.pdf.setting.UiSettings
import java.io.File
import kotlin.math.abs

class PdfViewer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    var isInitialized = false; internal set
    var currentSource: String? = null; internal set
    var currentPage: Int = 1; internal set
    var pagesCount: Int = 0; internal set
    var currentPageScale: Float = 0f; internal set
    var currentPageScaleValue: String = ""; internal set
    var properties: PdfDocumentProperties? = null; internal set

    /**
     * Changes require reinitializing PdfViewer
     */
    var highlightEditorColors: List<Pair<String, Int>> = defaultHighlightEditorColors
    var pdfPrintAdapter: PrintDocumentAdapter? = null

    internal val listeners = mutableListOf<PdfListener>()
    internal val webInterface: WebInterface = WebInterface(this)
    internal val mainHandler = Handler(Looper.getMainLooper())
    internal var onReadyListeners = mutableListOf<PdfViewer.() -> Unit>()
    internal var tempBackgroundColor: Int? = null

    internal val resourceLoaders = listOf(
        PdfViewerResourceLoader(context, webInterface::onLoadFailed),
        AssetResourceLoader(context, webInterface::onLoadFailed),
        ContentResourceLoader(context, webInterface::onLoadFailed),
        FileResourceLoader(webInterface::onLoadFailed),
        NetworkResourceLoader(webInterface::onLoadFailed),
    )

    internal val webView: WebView = PdfJsWebView()

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
        internal set(value) {
            if (field != value)
                listeners.forEach {
                    it.onActualScaleLimitChange(value, actualMaxPageScale, actualDefaultPageScale)
                }
            field = value
            if (value > 0) webView setDirectly "MIN_SCALE"(value)
        }
    var actualMaxPageScale = 0f
        internal set(value) {
            if (field != value)
                listeners.forEach {
                    it.onActualScaleLimitChange(actualMinPageScale, value, actualDefaultPageScale)
                }
            field = value
            if (value > 0) webView setDirectly "MAX_SCALE"(value)
        }
    var actualDefaultPageScale = 0f
        internal set(value) {
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

    val editor = PdfEditor(this)

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

    /**
     * source uri can be
     * - "asset://" or "file:///android_asset/" => Asset file
     * - "content://" => Content uri, like uri from Document picker
     * - "file://" or "/" => Direct file path, like path of file from app's files folder (not recommended for accessing file from internal storage)
     * - "https://" or "http://" => Network url
     */
    fun load(source: Uri) {
        load(source.toString())
    }

    /**
     * source string can be
     * - "asset://" or "file:///android_asset/" => Asset file
     * - "content://" => Content uri, like uri from Document picker
     * - "file://" or "/" => Direct file path, like path of file from app's files folder (not recommended for accessing file from internal storage)
     * - "https://" or "http://" => Network url
     */
    fun load(source: String) {
        when {
            source.startsWith("file:///android_asset/") ->
                loadFromAsset(source.replaceFirst("file:///android_asset/", ""))

            source.startsWith("asset://") ->
                loadFromAsset(source.replaceFirst("asset://", ""))

            source.startsWith("content://") ->
                loadFromContentUri(source)

            source.startsWith("file://") ->
                loadFromFile(source.replaceFirst("file://", ""))

            source.startsWith("/") ->
                loadFromFile(source)

            source.startsWith("https://") || source.startsWith("http://") ->
                loadFromUrl(source)

            else ->
                throw IllegalArgumentException("No resource loader is available for provided source! $source")
        }
    }

    fun loadFromAsset(assetPath: String) {
        openUrl(urlFor(AssetResourceLoader.PATH, assetPath))
    }

    fun loadFromContentUri(contentUri: Uri) {
        loadFromContentUri(contentUri.toString())
    }

    fun loadFromContentUri(contentUri: String) {
        openUrl(urlFor(ContentResourceLoader.PATH, Uri.encode(contentUri)))
    }

    fun loadFromFile(file: File) {
        loadFromFile(file.absolutePath)
    }

    fun loadFromFile(filePath: String) {
        openUrl(urlFor(FileResourceLoader.PATH, Uri.encode(filePath)))
    }

    fun loadFromUrl(url: Uri) {
        loadFromUrl(url.toString())
    }

    fun loadFromUrl(url: String) {
        openUrl(urlFor(NetworkResourceLoader.PATH, Uri.encode(url)))
    }

    private fun openUrl(url: String, originalUrl: String = url) {
        checkViewer()
        currentPage = 1
        pagesCount = 0
        currentPageScale = 0f
        currentPageScaleValue = ""
        properties = null
        currentSource = url

        listeners.forEach { it.onPageLoadStart() }
        webView callDirectly "openUrl"("{url: '$url', originalUrl: '$originalUrl'}")
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

    fun printFile() {
        pdfPrintAdapter ?: throw RuntimeException("PdfPrintAdapter has not been set!")
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

    fun createSharableUri(authority: String): Uri? {
        return resourceLoaders
            .firstOrNull { it.canHandle(Uri.parse(currentSource ?: return null)) }
            ?.createSharableUri(context, authority, currentSource ?: return null)
    }

    override fun setLayerType(layerType: Int, paint: Paint?) {
        super.setLayerType(layerType, paint)
        webView.setLayerType(layerType, paint)
    }

    internal fun loadPage() {
        webView.loadUrl(PDF_VIEWER_URL)
    }

    internal fun checkViewer() {
        if (!isInitialized) throw PdfViewerNotInitializedException()
    }

    internal fun dispatchRotationChange(
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

    internal fun dispatchSnapChange(
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

    internal fun dispatchPageAlignMode(
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

    internal fun adjustAlignMode(alignMode: PageAlignMode): PageAlignMode {
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

    internal fun dispatchSinglePageArrangement(
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

    internal fun dispatchHighlightColor(
        highlightColor: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorHighlightColorChange(highlightColor) }
        ) {
            webView callDirectly "selectHighlightColor"(highlightColor.toJsHex().toJsString())
        }
    }

    internal fun dispatchShowAllHighlights(
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

    internal fun dispatchHighlightThickness(
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

    internal fun dispatchFreeFontColor(
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

    internal fun dispatchFreeFontSize(
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

    internal fun dispatchInkColor(
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

    internal fun dispatchInkThickness(
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

    internal fun dispatchInkOpacity(
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

    internal fun dispatchScrollSpeedLimit(
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

    internal fun adjustAlignModeAndArrangementMode(scrollMode: PageScrollMode) {
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

    internal fun setUpActualScaleValues(callback: () -> Unit) {
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

    sealed class ScrollSpeedLimit {

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

    companion object {
        internal const val PDF_VIEWER_URL =
            "https://${ResourceLoader.RESOURCE_DOMAIN}${PdfViewerResourceLoader.PATH}com/bhuvaneshw/mozilla/pdfjs/pdf_viewer.html"
        private const val COLOR_NOT_FOUND = 11
        private val ZOOM_SCALE_RANGE = -4f..-1f

        /**
         * Controls suppression of WebView console logs.
         *
         * IMPORTANT: Set to 'false' *only* when debugging WebView behavior,
         * as it will allow console logs to appear. Keep it 'true' in production
         * or non-debug scenarios to avoid unnecessary log noise.
         */
        var preventWebViewConsoleLog = true

        val defaultHighlightEditorColors = listOf(
            "yellow" to Color.parseColor("#FFFF98"),
            "green" to Color.parseColor("#53FFBC"),
            "blue" to Color.parseColor("#80EBFF"),
            "pink" to Color.parseColor("#FFCBE6"),
            "red" to Color.parseColor("#FF4F5F"),
        )

        @Suppress("NOTHING_TO_INLINE")
        private inline fun urlFor(path: String, source: String) =
            "https://${ResourceLoader.RESOURCE_DOMAIN}$path$source"
    }

    private fun setPreviews(context: Context, containerBgColor: Int) {
        addView(
            LinearLayout(context).apply {
                orientation = VERTICAL
                if (containerBgColor == COLOR_NOT_FOUND) {
                    if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
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
