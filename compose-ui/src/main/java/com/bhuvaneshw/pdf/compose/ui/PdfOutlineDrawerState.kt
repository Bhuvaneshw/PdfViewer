package com.bhuvaneshw.pdf.compose.ui

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Creates and remembers a [PdfOutlineDrawerState].
 *
 * @param initialValue The initial value of the drawer state.
 * @param confirmStateChange Optional callback to confirm or veto a pending state change.
 * @return A remembered [PdfOutlineDrawerState].
 */
@Composable
fun rememberPdfOutlineDrawerState(
    initialValue: DrawerValue,
    confirmStateChange: (DrawerValue) -> Boolean = { true }
): PdfOutlineDrawerState {
    val drawerState = rememberDrawerState(initialValue, confirmStateChange)
    return remember { PdfOutlineDrawerState(drawerState) }
}

/**
 * State for the PDF outline drawer.
 *
 * @param drawerState The underlying [DrawerState].
 */
data class PdfOutlineDrawerState(val drawerState: DrawerState) {

    internal var backProgress by mutableStateOf(0f)

    /**
     * Updates the progress of the back gesture.
     *
     * @param progress The new progress value.
     */
    fun updateBackProgress(progress: Float) {
        backProgress = progress
    }
}
