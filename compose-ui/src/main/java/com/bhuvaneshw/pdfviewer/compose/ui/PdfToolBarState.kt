package com.bhuvaneshw.pdfviewer.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberToolBarState() = remember { PdfToolBarState() }

class PdfToolBarState {
    var isFindBarOpen by mutableStateOf(false)
    var isEditorOpen by mutableStateOf(false)
    var isTextHighlighterOn by mutableStateOf(false)
    var isEditorFreeTextOn by mutableStateOf(false)
    var isEditorInkOn by mutableStateOf(false)
}
