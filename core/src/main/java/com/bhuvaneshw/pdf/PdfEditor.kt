package com.bhuvaneshw.pdf

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.bhuvaneshw.pdf.PdfViewer.Companion.defaultHighlightEditorColors
import com.bhuvaneshw.pdf.js.callDirectly
import com.bhuvaneshw.pdf.js.invoke

class PdfEditor internal constructor(private val pdfViewer: PdfViewer) {
    var textHighlighterOn = false
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.webView callDirectly if (value) "openTextHighlighter"() else "closeTextHighlighter"()
        }

    var freeTextOn = false
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.webView callDirectly if (value) "openEditorFreeText"() else "closeEditorFreeText"()
        }

    var inkOn = false
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.webView callDirectly if (value) "openEditorInk"() else "closeEditorInk"()
        }

    var stampOn = false
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.webView callDirectly if (value) "openEditorStamp"() else "closeEditorStamp"()
        }

    var highlightColor =
        pdfViewer.highlightEditorColors.firstOrNull()?.second
            ?: defaultHighlightEditorColors.first().second
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchHighlightColor(value)
        }

    var showAllHighlights = true
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchShowAllHighlights(value)
        }

    @IntRange(from = 8, to = 24)
    var highlightThickness = 12
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchHighlightThickness(value)
        }

    @ColorInt
    var freeFontColor = Color.BLACK
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchFreeFontColor(value)
        }

    @IntRange(from = 5, to = 100)
    var freeFontSize = 10
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchFreeFontSize(value)
        }

    @ColorInt
    var inkColor = Color.BLACK
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchInkColor(value)
        }

    @IntRange(from = 1, to = 20)
    var inkThickness = 1
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchInkThickness(value)
        }

    @IntRange(from = 1, to = 100)
    var inkOpacity = 100
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchInkOpacity(value)
        }

    var hasUnsavedChanges: Boolean = false; internal set
    val isEditing: Boolean get() = textHighlighterOn || freeTextOn || inkOn || stampOn

    fun undo() {
        pdfViewer.checkViewer()
        pdfViewer.webView callDirectly "undo"()
    }

    fun redo() {
        pdfViewer.checkViewer()
        pdfViewer.webView callDirectly "redo"()
    }

    sealed interface AnnotationEventType {
        sealed interface Unsaved : AnnotationEventType {
            data object Highlight : Unsaved
            data object FreeText : Unsaved
            data object Ink : Unsaved
            data object Stamp : Unsaved
        }

        sealed interface Saved : AnnotationEventType {
            data object Downloaded : Saved
            data object Printed : Saved
        }

        data class Unknown(val type: String) : AnnotationEventType

        companion object
    }
}
