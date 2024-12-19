package com.acutecoder.pdfviewer.compose.ui

enum class PdfToolBarMenuItem(internal val displayName: String) {
    DOWNLOAD("Download"),
    ZOOM("Zoom"),
    GO_TO_PAGE("Go to page"),
    ROTATE_CLOCK_WISE("Rotate Clockwise"),
    ROTATE_ANTI_CLOCK_WISE("Rotate Anti Clockwise"),
    SCROLL_MODE("Scroll Mode"),
    SPREAD_MODE("Split Mode"),
    ALIGN_MODE("Align Mode"),
    SNAP_PAGE("Snap Page"),
    PROPERTIES("Properties")
}