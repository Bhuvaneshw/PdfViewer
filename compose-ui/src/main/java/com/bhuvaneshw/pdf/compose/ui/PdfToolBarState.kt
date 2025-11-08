package com.bhuvaneshw.pdf.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Creates and remembers a [PdfToolBarState] for a PDF viewer toolbar.
 *
 * @see PdfToolBarState
 */
@Composable
fun rememberToolBarState() = remember { PdfToolBarState() }

/**
 * A state object that can be hoisted to control and observe the PDF viewer toolbar state.
 *
 * In a composable, create and remember an instance of this class using [rememberToolBarState].
 * This class manages the visibility of different toolbar sections like the find bar and annotation editor.
 *
 * @see rememberToolBarState
 * @see com.bhuvaneshw.pdf.compose.PdfViewer
 */
class PdfToolBarState {

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
}
