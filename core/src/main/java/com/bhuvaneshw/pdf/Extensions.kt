package com.bhuvaneshw.pdf

import android.content.Context
import android.net.Uri
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import com.bhuvaneshw.pdf.setting.PdfSettingsManager
import com.bhuvaneshw.pdf.setting.SharedPreferencePdfSettingsSaver

fun Context.sharedPdfSettingsManager(name: String, mode: Int = Context.MODE_PRIVATE) =
    PdfSettingsManager(SharedPreferencePdfSettingsSaver(this, name, mode))

@PdfUnstableApi
fun PdfViewer.callSafely(
    checkScrollSpeedLimit: Boolean = true,
    checkEditing: Boolean = true,
    block: ScrollSpeedLimitScope.() -> Unit
) {
    if (checkEditing && editor.isEditing) return

    val scope = ScrollSpeedLimitScope()

    if (checkScrollSpeedLimit && scrollSpeedLimit != PdfViewer.ScrollSpeedLimit.None) {
        val originalScrollSpeedLimit = scrollSpeedLimit
        scrollSpeedLimit = PdfViewer.ScrollSpeedLimit.None
        block.invoke(scope)
        scope.onEnabled?.invoke()
        scrollSpeedLimit = originalScrollSpeedLimit
    } else block.invoke(scope)
}

fun ScrollSpeedLimitScope.callIfScrollSpeedLimitIsEnabled(onEnabled: () -> Unit) {
    this.onEnabled = onEnabled
}

class ScrollSpeedLimitScope internal constructor(internal var onEnabled: (() -> Unit)? = null)

fun PdfViewer.addListener(
    onPageLoadStart: (() -> Unit)? = null,
    onPageLoadSuccess: ((pagesCount: Int) -> Unit)? = null,
    onPageLoadFailed: ((errorMessage: String) -> Unit)? = null,
    onReceivedError: ((error: WebViewError) -> Unit)? = null,
    onPageChange: ((pageNumber: Int) -> Unit)? = null,
    onScaleChange: ((scale: Float) -> Unit)? = null,
    onSavePdf: ((pdfAsBytes: ByteArray) -> Unit)? = null,
    onFindMatchStart: (() -> Unit)? = null,
    onFindMatchChange: ((current: Int, total: Int) -> Unit)? = null,
    onFindMatchComplete: ((found: Boolean) -> Unit)? = null,
    onScrollChange: ((currentOffset: Int, totalOffset: Int, isHorizontalScroll: Boolean) -> Unit)? = null,
    onLoadProperties: ((properties: PdfDocumentProperties) -> Unit)? = null,
    onPasswordDialogChange: ((isOpen: Boolean) -> Unit)? = null,
    onScrollModeChange: ((scrollMode: PdfViewer.PageScrollMode) -> Unit)? = null,
    onSpreadModeChange: ((spreadMode: PdfViewer.PageSpreadMode) -> Unit)? = null,
    onRotationChange: ((rotation: PdfViewer.PageRotation) -> Unit)? = null,
    onSingleClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onLinkClick: ((link: String) -> Unit)? = null,
    onSnapChange: ((snapPage: Boolean) -> Unit)? = null,
    onSinglePageArrangementChange: ((requestedArrangement: Boolean, appliedArrangement: Boolean) -> Unit)? = null,
    onEditorHighlightColorChange: ((highlightColor: Int) -> Unit)? = null,
    onEditorShowAllHighlightsChange: ((showAll: Boolean) -> Unit)? = null,
    onEditorHighlightThicknessChange: ((thickness: Int) -> Unit)? = null,
    onEditorFreeFontColorChange: ((fontColor: Int) -> Unit)? = null,
    onEditorFreeFontSizeChange: ((fontSize: Int) -> Unit)? = null,
    onEditorInkColorChange: ((color: Int) -> Unit)? = null,
    onEditorInkThicknessChange: ((thickness: Int) -> Unit)? = null,
    onEditorInkOpacityChange: ((opacity: Int) -> Unit)? = null,
    onRenderProcessGone: ((detail: RenderProcessGoneDetail?) -> Boolean)? = null,
    onScaleLimitChange: ((minPageScale: Float, maxPageScale: Float, defaultPageScale: Float) -> Unit)? = null,
    onActualScaleLimitChange: ((minPageScale: Float, maxPageScale: Float, defaultPageScale: Float) -> Unit)? = null,
    onAlignModeChange: ((requestedMode: PdfViewer.PageAlignMode, appliedMode: PdfViewer.PageAlignMode) -> Unit)? = null,
    onScrollSpeedLimitChange: ((requestedLimit: PdfViewer.ScrollSpeedLimit, appliedLimit: PdfViewer.ScrollSpeedLimit) -> Unit)? = null,
    onShowFileChooser: ((filePathCallback: ValueCallback<Array<out Uri?>?>?, fileChooserParams: FileChooserParams?) -> Boolean)? = null,
) {
    addListener(object : PdfListener {
        override fun onPageLoadStart() {
            onPageLoadStart?.invoke()
        }

        override fun onPageLoadSuccess(pagesCount: Int) {
            onPageLoadSuccess?.invoke(pagesCount)
        }

        override fun onPageLoadFailed(errorMessage: String) {
            onPageLoadFailed?.invoke(errorMessage)
        }

        override fun onReceivedError(error: WebViewError) {
            onReceivedError?.invoke(error)
        }

        override fun onPageChange(pageNumber: Int) {
            onPageChange?.invoke(pageNumber)
        }

        override fun onScaleChange(scale: Float) {
            onScaleChange?.invoke(scale)
        }

        override fun onSavePdf(pdfAsBytes: ByteArray) {
            onSavePdf?.invoke(pdfAsBytes)
        }

        override fun onFindMatchStart() {
            onFindMatchStart?.invoke()
        }

        override fun onFindMatchChange(current: Int, total: Int) {
            onFindMatchChange?.invoke(current, total)
        }

        override fun onFindMatchComplete(found: Boolean) {
            onFindMatchComplete?.invoke(found)
        }

        override fun onScrollChange(
            currentOffset: Int,
            totalOffset: Int,
            isHorizontalScroll: Boolean
        ) {
            onScrollChange?.invoke(currentOffset, totalOffset, isHorizontalScroll)
        }

        override fun onLoadProperties(properties: PdfDocumentProperties) {
            onLoadProperties?.invoke(properties)
        }

        override fun onPasswordDialogChange(isOpen: Boolean) {
            onPasswordDialogChange?.invoke(isOpen)
        }

        override fun onScrollModeChange(scrollMode: PdfViewer.PageScrollMode) {
            onScrollModeChange?.invoke(scrollMode)
        }

        override fun onSpreadModeChange(spreadMode: PdfViewer.PageSpreadMode) {
            onSpreadModeChange?.invoke(spreadMode)
        }

        override fun onRotationChange(rotation: PdfViewer.PageRotation) {
            onRotationChange?.invoke(rotation)
        }

        override fun onSingleClick() {
            onSingleClick?.invoke()
        }

        override fun onDoubleClick() {
            onDoubleClick?.invoke()
        }

        override fun onLongClick() {
            onLongClick?.invoke()
        }

        override fun onLinkClick(link: String) {
            onLinkClick?.invoke(link)
        }

        override fun onSnapChange(snapPage: Boolean) {
            onSnapChange?.invoke(snapPage)
        }

        override fun onSinglePageArrangementChange(
            requestedArrangement: Boolean,
            appliedArrangement: Boolean
        ) {
            onSinglePageArrangementChange?.invoke(requestedArrangement, appliedArrangement)
        }

        override fun onEditorHighlightColorChange(highlightColor: Int) {
            onEditorHighlightColorChange?.invoke(highlightColor)
        }

        override fun onEditorShowAllHighlightsChange(showAll: Boolean) {
            onEditorShowAllHighlightsChange?.invoke(showAll)
        }

        override fun onEditorHighlightThicknessChange(thickness: Int) {
            onEditorHighlightThicknessChange?.invoke(thickness)
        }

        override fun onEditorFreeFontColorChange(fontColor: Int) {
            onEditorFreeFontColorChange?.invoke(fontColor)
        }

        override fun onEditorFreeFontSizeChange(fontSize: Int) {
            onEditorFreeFontSizeChange?.invoke(fontSize)
        }

        override fun onEditorInkColorChange(color: Int) {
            onEditorInkColorChange?.invoke(color)
        }

        override fun onEditorInkThicknessChange(thickness: Int) {
            onEditorInkThicknessChange?.invoke(thickness)
        }

        override fun onEditorInkOpacityChange(opacity: Int) {
            onEditorInkOpacityChange?.invoke(opacity)
        }

        override fun onRenderProcessGone(detail: RenderProcessGoneDetail?): Boolean {
            return onRenderProcessGone?.invoke(detail) == true
        }

        override fun onScaleLimitChange(
            minPageScale: Float,
            maxPageScale: Float,
            defaultPageScale: Float
        ) {
            onScaleLimitChange?.invoke(minPageScale, maxPageScale, defaultPageScale)
        }

        override fun onActualScaleLimitChange(
            minPageScale: Float,
            maxPageScale: Float,
            defaultPageScale: Float
        ) {
            onActualScaleLimitChange?.invoke(minPageScale, maxPageScale, defaultPageScale)
        }

        override fun onAlignModeChange(
            requestedMode: PdfViewer.PageAlignMode,
            appliedMode: PdfViewer.PageAlignMode
        ) {
            onAlignModeChange?.invoke(requestedMode, appliedMode)
        }

        override fun onScrollSpeedLimitChange(
            requestedLimit: PdfViewer.ScrollSpeedLimit,
            appliedLimit: PdfViewer.ScrollSpeedLimit
        ) {
            onScrollSpeedLimitChange?.invoke(requestedLimit, appliedLimit)
        }

        override fun onShowFileChooser(
            filePathCallback: ValueCallback<Array<out Uri?>?>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            return onShowFileChooser?.invoke(filePathCallback, fileChooserParams) == true
        }
    })
}
