package com.bhuvaneshw.pdf.compose.ui

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Creates and remembers a [PdfToolBarState] for a PDF viewer toolbar.
 *
 * @param predictiveBackThreshold The threshold for predictive back gestures. Value can be 0f(exclusive) to 1f.
 * @see PdfToolBarState
 */
@Composable
fun rememberPdfToolBarState(
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) predictiveBackThreshold: Float = 0.1f
) = remember { PdfToolBarState(predictiveBackThreshold) }

/**
 * A state object that can be hoisted to control and observe the PDF viewer toolbar state.
 *
 * In a composable, create and remember an instance of this class using [rememberPdfToolBarState].
 * This class manages the visibility of different toolbar sections like the find bar and annotation editor.
 *
 * @param predictiveBackThreshold The threshold for predictive back gestures. Value can be 0f(exclusive) to 1f.
 * @see rememberPdfToolBarState
 * @see com.bhuvaneshw.pdf.compose.PdfViewer
 */
class PdfToolBarState(
    @param:FloatRange(from = 0.0, to = 1.0, fromInclusive = false)
    val predictiveBackThreshold: Float
) {

    init {
        if (predictiveBackThreshold <= 0f) {
            throw IllegalArgumentException("Predictive Back Threshold cannot be less than or equal to zero.")
        }
    }

    /**
     * `true` if the find bar is visible, `false` otherwise.
     */
    var isFindBarOpen by mutableStateOf(false)

    /**
     * `true` if the annotation editor is visible, `false` otherwise.
     */
    var isEditorOpen by mutableStateOf(false)

    /**
     * `true` if the text highlighter tool is currently active in the annotation editor.
     */
    var isTextHighlighterOn by mutableStateOf(false)

    /**
     * `true` if the free text tool is currently active in the annotation editor.
     */
    var isEditorFreeTextOn by mutableStateOf(false)

    /**
     * `true` if the ink (drawing) tool is currently active in the annotation editor.
     */
    var isEditorInkOn by mutableStateOf(false)

    /**
     * `true` if the stamp tool is currently active in the annotation editor.
     */
    var isEditorStampOn by mutableStateOf(false)

    /**
     * A state object that provides predictive back gesture support for the toolbar.
     */
    val withBackProgress = PredictiveToolBarState()

    internal var backProgress by mutableFloatStateOf(0f)

    /**
     * Handles the back press event to dismiss UI elements managed by this state.
     *
     * This function should be called when the user presses the back button. It determines
     * which UI element to close based on a priority order. For instance, an active editor tool
     * is closed before the editor itself.
     *
     * The order of closing is:
     * 1. Active editor tool (highlighter, free text, ink, stamp)
     * 2. Annotation editor
     * 3. Find bar
     *
     * @return `true` if the back press was consumed and a UI element was closed, `false` otherwise.
     *         If `false` is returned, the caller should handle the back press further (e.g., by closing the screen).
     */
    fun handleBackPressed(): Boolean {
        when {
            isTextHighlighterOn -> isTextHighlighterOn = false
            isEditorFreeTextOn -> isEditorFreeTextOn = false
            isEditorInkOn -> isEditorInkOn = false
            isEditorStampOn -> isEditorStampOn = false

            isEditorOpen -> isEditorOpen = false
            isFindBarOpen -> isFindBarOpen = false

            else -> return false
        }

        return true
    }

    /**
     * Checks if there is any UI element that can be closed by a back press.
     *
     * @return `true` if a back press can be handled, `false` otherwise.
     */
    fun canHandleBackPressed(): Boolean {
        return isTextHighlighterOn ||
                isEditorFreeTextOn ||
                isEditorInkOn ||
                isEditorStampOn ||
                isEditorOpen ||
                isFindBarOpen
    }

    /**
     * Updates the progress of the predictive back gesture.
     *
     * @param progress The progress of the back gesture, between 0.0 and 1.0.
     */
    fun updateBackProgress(@FloatRange(0.0, 1.0) progress: Float) {
        backProgress = progress
    }

    /**
     * A state object that provides predictive back gesture support for the toolbar.
     *
     * This class is used to determine the visibility of toolbar elements based on the progress
     * of a predictive back gesture.
     */
    inner class PredictiveToolBarState {
        /**
         * `true` if the editor is open and should be visible during a predictive back gesture.
         */
        val isEditorOpen: Boolean
            get() {
                if (isEditorInnerBarOpen)
                    return this@PdfToolBarState.isEditorOpen
                return this@PdfToolBarState.isEditorOpen && backProgress < predictiveBackThreshold
            }

        /**
         * `true` if the text highlighter tool is open and should be visible during a predictive back gesture.
         */
        val isTextHighlighterOn: Boolean
            get() {
                return this@PdfToolBarState.isTextHighlighterOn && backProgress < predictiveBackThreshold
            }

        /**
         * `true` if the free text tool is open and should be visible during a predictive back gesture.
         */
        val isEditorFreeTextOn: Boolean
            get() {
                return this@PdfToolBarState.isEditorFreeTextOn && backProgress < predictiveBackThreshold
            }

        /**
         * `true` if the ink tool is open and should be visible during a predictive back gesture.
         */
        val isEditorInkOn: Boolean
            get() {
                return this@PdfToolBarState.isEditorInkOn && backProgress < predictiveBackThreshold
            }

        /**
         * `true` if the stamp tool is open and should be visible during a predictive back gesture.
         */
        val isEditorStampOn: Boolean
            get() {
                return this@PdfToolBarState.isEditorStampOn && backProgress < predictiveBackThreshold
            }

        /**
         * `true` if any of the inner annotation editor tools are open.
         */
        val isEditorInnerBarOpen: Boolean
            get() {
                return this@PdfToolBarState.isTextHighlighterOn ||
                        this@PdfToolBarState.isEditorFreeTextOn ||
                        this@PdfToolBarState.isEditorInkOn ||
                        this@PdfToolBarState.isEditorStampOn
            }

        /**
         * `true` if the find bar is open and should be visible during a predictive back gesture.
         */
        val isFindBarOpen: Boolean
            get() {
                return this@PdfToolBarState.isFindBarOpen && backProgress < predictiveBackThreshold
            }
    }
}
