package com.bhuvaneshw.pdf

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.bhuvaneshw.pdf.PdfViewer.Companion.defaultHighlightEditorColors
import com.bhuvaneshw.pdf.js.callDirectly
import com.bhuvaneshw.pdf.js.invoke

/**
 * Provides functionality to edit a PDF document.
 */
class PdfEditor internal constructor(private val pdfViewer: PdfViewer) {
    /**
     * Enables or disables the text highlighter tool.
     */
    var textHighlighterOn = false
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.webView callDirectly if (value) "openTextHighlighter"() else "closeTextHighlighter"()
        }

    /**
     * Enables or disables the free text tool.
     */
    var freeTextOn = false
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.webView callDirectly if (value) "openEditorFreeText"() else "closeEditorFreeText"()
        }

    /**
     * Enables or disables the ink tool for free-hand drawing.
     */
    var inkOn = false
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.webView callDirectly if (value) "openEditorInk"() else "closeEditorInk"()
        }

    /**
     * Enables or disables the stamp tool.
     */
    var stampOn = false
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.webView callDirectly if (value) "openEditorStamp"() else "closeEditorStamp"()
        }

    /**
     * The current color used for highlighting text.
     */
    var highlightColor =
        pdfViewer.highlightEditorColors.firstOrNull()?.second
            ?: defaultHighlightEditorColors.first().second
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchHighlightColor(value)
        }

    /**
     * Toggles the visibility of all highlights in the document.
     */
    var showAllHighlights = true
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchShowAllHighlights(value)
        }

    /**
     * The thickness of the highlight, ranging from 8 to 24.
     */
    @IntRange(from = 8, to = 24)
    var highlightThickness = 12
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchHighlightThickness(value)
        }

    /**
     * The color of the free text font.
     */
    @ColorInt
    var freeFontColor = Color.BLACK
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchFreeFontColor(value)
        }

    /**
     * The size of the free text font, ranging from 5 to 100.
     */
    @IntRange(from = 5, to = 100)
    var freeFontSize = 10
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchFreeFontSize(value)
        }

    /**
     * The color of the ink used for drawing.
     */
    @ColorInt
    var inkColor = Color.BLACK
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchInkColor(value)
        }

    /**
     * The thickness of the ink, ranging from 1 to 20.
     */
    @IntRange(from = 1, to = 20)
    var inkThickness = 1
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchInkThickness(value)
        }

    /**
     * The opacity of the ink, ranging from 1 to 100.
     */
    @IntRange(from = 1, to = 100)
    var inkOpacity = 100
        set(value) {
            pdfViewer.checkViewer()
            field = value
            pdfViewer.dispatchInkOpacity(value)
        }

    /**
     * If true, applies the highlight color on text selection.
     */
    var applyHighlightColorOnTextSelection = false

    /**
     * Indicates if there are any unsaved changes in the editor.
     */
    var hasUnsavedChanges: Boolean = false; internal set

    /**
     * Returns true if any of the editor modes (text highlight, free text, ink, stamp) are active.
     */
    val isEditing: Boolean get() = textHighlighterOn || freeTextOn || inkOn || stampOn

    /**
     * Undoes the last editing action.
     */
    fun undo() {
        pdfViewer.checkViewer()
        pdfViewer.webView callDirectly "undo"()
    }

    /**
     * Redoes the last undone editing action.
     */
    fun redo() {
        pdfViewer.checkViewer()
        pdfViewer.webView callDirectly "redo"()
    }

    /**
     * Represents different types of annotation events that can occur in the editor.
     */
    sealed interface AnnotationEventType {
        /**
         * Represents different types of unsaved events.
         */
        sealed interface Unsaved : AnnotationEventType {
            /** Unsaved highlight annotation. */
            data object Highlight : Unsaved

            /** Unsaved free text annotation. */
            data object FreeText : Unsaved

            /** Unsaved ink annotation. */
            data object Ink : Unsaved

            /** Unsaved stamp annotation. */
            data object Stamp : Unsaved
        }

        /**
         * Represents different types of saved events.
         */
        sealed interface Saved : AnnotationEventType {
            /** Annotation has been downloaded. */
            data object Downloaded : Saved

            /** Annotation has been printed. */
            data object Printed : Saved
        }

        /**
         * Represents an unknown annotation event.
         * @property type The string representation of the event type.
         */
        data class Unknown(val type: String?) : AnnotationEventType

        companion object
    }

    /**
     * Represents the state of the various editor modes.
     *
     * @property isTextHighlighterOn True if the text highlighter is active.
     * @property isEditorFreeTextOn True if the free text editor is active.
     * @property isEditorInkOn True if the ink editor is active.
     * @property isEditorStampOn True if the stamp editor is active.
     */
    data class EditorModeState(
        val isTextHighlighterOn: Boolean = false,
        val isEditorFreeTextOn: Boolean = false,
        val isEditorInkOn: Boolean = false,
        val isEditorStampOn: Boolean = false
    )
}
