@file:Suppress("FunctionName")

package com.bhuvaneshw.pdf

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange

interface PdfListener {

    fun onPageLoadStart() {}
    fun onPageLoadSuccess(pagesCount: Int) {}
    fun onPageLoadFailed(errorMessage: String) {}
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

}

fun PdfOnPageLoadStart(callback: () -> Unit) =
    object : PdfListener {
        override fun onPageLoadStart() {
            callback()
        }
    }

fun PdfOnPageLoadSuccess(callback: (pagesCount: Int) -> Unit) =
    object : PdfListener {
        override fun onPageLoadSuccess(pagesCount: Int) {
            callback(pagesCount)
        }
    }

fun PdfOnPageLoadFailed(callback: (errorMessage: String) -> Unit) =
    object : PdfListener {
        override fun onPageLoadFailed(errorMessage: String) {
            callback(errorMessage)
        }
    }

fun PdfOnPageChange(callback: (pageNumber: Int) -> Unit) =
    object : PdfListener {
        override fun onPageChange(pageNumber: Int) {
            callback(pageNumber)
        }
    }

fun PdfOnScaleChange(callback: (scale: Float) -> Unit) =
    object : PdfListener {
        override fun onScaleChange(scale: Float) {
            callback(scale)
        }
    }

fun PdfOnSavePdf(callback: (pdfAsBytes: ByteArray) -> Unit) =
    object : PdfListener {
        override fun onSavePdf(pdfAsBytes: ByteArray) {
            callback(pdfAsBytes)
        }
    }

fun PdfOnFindMatchStart(callback: () -> Unit) =
    object : PdfListener {
        override fun onFindMatchStart() {
            callback()
        }
    }

fun PdfOnFindMatchChange(callback: (current: Int, total: Int) -> Unit) =
    object : PdfListener {
        override fun onFindMatchChange(current: Int, total: Int) {
            callback(current, total)
        }
    }

fun PdfOnFindMatchComplete(callback: (found: Boolean) -> Unit) =
    object : PdfListener {
        override fun onFindMatchComplete(found: Boolean) {
            callback(found)
        }
    }

fun PdfOnScrollChange(callback: (currentOffset: Int, totalOffset: Int, isHorizontal: Boolean) -> Unit) =
    object : PdfListener {
        override fun onScrollChange(
            currentOffset: Int,
            totalOffset: Int,
            isHorizontalScroll: Boolean
        ) {
            callback(currentOffset, totalOffset, isHorizontalScroll)
        }
    }

fun PdfOnLoadProperties(callback: (properties: PdfDocumentProperties) -> Unit) =
    object : PdfListener {
        override fun onLoadProperties(properties: PdfDocumentProperties) {
            callback(properties)
        }
    }

fun PdfOnPasswordDialogChange(callback: (isOpen: Boolean) -> Unit) =
    object : PdfListener {
        override fun onPasswordDialogChange(isOpen: Boolean) {
            callback(isOpen)
        }
    }

fun PdfOnScrollModeChange(callback: (scrollMode: PdfViewer.PageScrollMode) -> Unit) =
    object : PdfListener {
        override fun onScrollModeChange(scrollMode: PdfViewer.PageScrollMode) {
            callback(scrollMode)
        }
    }

fun PdfOnSpreadModeChange(callback: (spreadMode: PdfViewer.PageSpreadMode) -> Unit) =
    object : PdfListener {
        override fun onSpreadModeChange(spreadMode: PdfViewer.PageSpreadMode) {
            callback(spreadMode)
        }
    }

fun PdfOnRotationChange(callback: (rotation: PdfViewer.PageRotation) -> Unit) =
    object : PdfListener {
        override fun onRotationChange(rotation: PdfViewer.PageRotation) {
            callback(rotation)
        }
    }

fun PdfOnSingleClick(callback: () -> Unit) =
    object : PdfListener {
        override fun onSingleClick() {
            callback()
        }
    }

fun PdfOnDoubleClick(callback: () -> Unit) =
    object : PdfListener {
        override fun onDoubleClick() {
            callback()
        }
    }

fun PdfOnLongClick(callback: () -> Unit) =
    object : PdfListener {
        override fun onLongClick() {
            callback()
        }
    }

fun PdfOnLinkClick(callback: (link: String) -> Unit) =
    object : PdfListener {
        override fun onLinkClick(link: String) {
            callback(link)
        }
    }

fun PdfOnScaleLimitChange(callback: (minPageScale: Float, maxPageScale: Float, defaultPageScale: Float) -> Unit) =
    object : PdfListener {
        override fun onScaleLimitChange(
            minPageScale: Float,
            maxPageScale: Float,
            defaultPageScale: Float
        ) {
            callback(minPageScale, maxPageScale, defaultPageScale)
        }
    }

fun PdfOnActualScaleLimitChange(callback: (minPageScale: Float, maxPageScale: Float, defaultPageScale: Float) -> Unit) =
    object : PdfListener {
        override fun onActualScaleLimitChange(
            minPageScale: Float,
            maxPageScale: Float,
            defaultPageScale: Float
        ) {
            callback(minPageScale, maxPageScale, defaultPageScale)
        }
    }

fun PdfOnScrollSpeedLimitChange(
    callback: (
        requestedLimit: PdfViewer.ScrollSpeedLimit,
        appliedLimit: PdfViewer.ScrollSpeedLimit
    ) -> Unit
) =
    object : PdfListener {
        override fun onScrollSpeedLimitChange(
            requestedLimit: PdfViewer.ScrollSpeedLimit,
            appliedLimit: PdfViewer.ScrollSpeedLimit
        ) {
            callback(requestedLimit, appliedLimit)
        }
    }

fun PdfOnSnapChange(callback: (snapPage: Boolean) -> Unit) =
    object : PdfListener {
        override fun onSnapChange(snapPage: Boolean) {
            callback(snapPage)
        }
    }

fun PdfOnSinglePageArrangementChange(callback: (requestedArrangement: Boolean, appliedArrangement: Boolean) -> Unit) =
    object : PdfListener {
        override fun onSinglePageArrangementChange(
            requestedArrangement: Boolean,
            appliedArrangement: Boolean
        ) {
            callback(requestedArrangement, appliedArrangement)
        }
    }

fun PdfOnAlignModeChange(
    callback: (
        requestedMode: PdfViewer.PageAlignMode,
        appliedMode: PdfViewer.PageAlignMode
    ) -> Unit
) =
    object : PdfListener {
        override fun onAlignModeChange(
            requestedMode: PdfViewer.PageAlignMode,
            appliedMode: PdfViewer.PageAlignMode
        ) {
            callback(requestedMode, appliedMode)
        }
    }

fun PdfOnEditorHighlightColorChange(callback: (highlightColor: Int) -> Unit) =
    object : PdfListener {
        override fun onEditorHighlightColorChange(@ColorInt highlightColor: Int) {
            callback(highlightColor)
        }
    }

fun PdfOnEditorShowAllHighlightsChange(callback: (showAll: Boolean) -> Unit) =
    object : PdfListener {
        override fun onEditorShowAllHighlightsChange(showAll: Boolean) {
            callback(showAll)
        }
    }

fun PdfOnEditorHighlightThicknessChange(callback: (thickness: Int) -> Unit) =
    object : PdfListener {
        override fun onEditorHighlightThicknessChange(@IntRange(from = 8, to = 24) thickness: Int) {
            callback(thickness)
        }
    }

fun PdfOnEditorFreeFontColorChange(callback: (fontColor: Int) -> Unit) =
    object : PdfListener {
        override fun onEditorFreeFontColorChange(@ColorInt fontColor: Int) {
            callback(fontColor)
        }
    }

fun PdfOnEditorFreeFontSizeChange(callback: (fontSize: Int) -> Unit) =
    object : PdfListener {
        override fun onEditorFreeFontSizeChange(@IntRange(from = 5, to = 100) fontSize: Int) {
            callback(fontSize)
        }
    }

fun PdfOnEditorInkColorChange(callback: (color: Int) -> Unit) =
    object : PdfListener {
        override fun onEditorInkColorChange(@ColorInt color: Int) {
            callback(color)
        }
    }

fun PdfOnEditorInkThicknessChange(callback: (thickness: Int) -> Unit) =
    object : PdfListener {
        override fun onEditorInkThicknessChange(@IntRange(from = 1, to = 20) thickness: Int) {
            callback(thickness)
        }
    }

fun PdfOnEditorInkOpacityChange(callback: (opacity: Int) -> Unit) =
    object : PdfListener {
        override fun onEditorInkOpacityChange(@IntRange(from = 1, to = 100) opacity: Int) {
            callback(opacity)
        }
    }
