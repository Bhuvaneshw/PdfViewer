package com.bhuvaneshw.pdf

import android.net.Uri
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange

interface PdfListener {

    fun onPageLoadStart() {}
    fun onPageLoadSuccess(pagesCount: Int) {}
    fun onPageLoadFailed(exception: Exception) {}
    fun onReceivedError(error: WebViewError) {}
    fun onProgressChange(@FloatRange(0.0, 1.0) progress: Float) {}
    fun onPageChange(pageNumber: Int) {}
    fun onScaleChange(scale: Float) {}
    fun onSavePdf(pdfAsBytes: ByteArray) {}
    fun onFindMatchStart() {}
    fun onFindMatchChange(current: Int, total: Int) {}
    fun onFindMatchComplete(found: Boolean) {}
    fun onScrollChange(currentOffset: Int, totalOffset: Int, isHorizontalScroll: Boolean) {}
    fun onLoadProperties(properties: PdfDocumentProperties) {}
    fun onPasswordDialogChange(isOpen: Boolean) {}
    fun onScrollModeChange(scrollMode: PdfViewer.PageScrollMode) {}
    fun onSpreadModeChange(spreadMode: PdfViewer.PageSpreadMode) {}
    fun onRotationChange(rotation: PdfViewer.PageRotation) {}
    fun onSingleClick() {}
    fun onDoubleClick() {}
    fun onLongClick() {}
    fun onLinkClick(link: String) {}
    fun onSnapChange(snapPage: Boolean) {}
    fun onSinglePageArrangementChange(requestedArrangement: Boolean, appliedArrangement: Boolean) {}
    fun onEditorHighlightColorChange(@ColorInt highlightColor: Int) {}
    fun onEditorShowAllHighlightsChange(showAll: Boolean) {}
    fun onEditorHighlightThicknessChange(@IntRange(from = 8, to = 24) thickness: Int) {}
    fun onEditorFreeFontColorChange(@ColorInt fontColor: Int) {}
    fun onEditorFreeFontSizeChange(@IntRange(from = 5, to = 100) fontSize: Int) {}
    fun onEditorInkColorChange(@ColorInt color: Int) {}
    fun onEditorInkThicknessChange(@IntRange(from = 1, to = 20) thickness: Int) {}
    fun onEditorInkOpacityChange(@IntRange(from = 1, to = 100) opacity: Int) {}
    fun onRenderProcessGone(detail: RenderProcessGoneDetail?): Boolean = false
    fun onPrintProcessStart() {}
    fun onPrintProcessProgress(@FloatRange(0.0, 1.0) progress: Float) {}
    fun onPrintProcessEnd() {}
    fun onPrintCancelled() {}
    fun onShowEditorMessage(message: String) {}
    fun onAnnotationEditor(type: PdfEditor.AnnotationEventType) {}

    fun onScaleLimitChange(
        @FloatRange(-4.0, 10.0) minPageScale: Float,
        @FloatRange(-4.0, 10.0) maxPageScale: Float,
        @FloatRange(-4.0, 10.0) defaultPageScale: Float
    ) {
    }

    fun onActualScaleLimitChange(
        @FloatRange(0.0, 10.0) minPageScale: Float,
        @FloatRange(0.0, 10.0) maxPageScale: Float,
        @FloatRange(0.0, 10.0) defaultPageScale: Float
    ) {
    }

    fun onAlignModeChange(
        requestedMode: PdfViewer.PageAlignMode,
        appliedMode: PdfViewer.PageAlignMode
    ) {
    }

    fun onScrollSpeedLimitChange(
        requestedLimit: PdfViewer.ScrollSpeedLimit,
        appliedLimit: PdfViewer.ScrollSpeedLimit
    ) {
    }

    fun onShowFileChooser(
        filePathCallback: ValueCallback<Array<out Uri?>?>?,
        fileChooserParams: FileChooserParams?
    ): Boolean = false

}
