package com.bhuvaneshw.pdf.compose.ui

/**
 * Represents the menu items available in the PDF viewer toolbar.
 *
 * @see com.bhuvaneshw.pdf.compose.PdfViewer
 */
enum class PdfToolBarMenuItem(internal val displayName: String) {
    /**
     * Menu item to save the PDF document.
     */
    SAVE("Save"),

    /**
     * Menu item to control the zoom level of the document.
     */
    ZOOM("Zoom"),

    /**
     * Menu item to navigate to a specific page number.
     */
    GO_TO_PAGE("Go to page"),

    /**
     * Menu item to rotate the document clockwise.
     */
    ROTATE_CLOCK_WISE("Rotate Clockwise"),

    /**
     * Menu item to rotate the document anti-clockwise.
     */
    ROTATE_ANTI_CLOCK_WISE("Rotate Anti Clockwise"),

    /**
     * Menu item to change the scrolling mode.
     *
     * @see com.bhuvaneshw.pdf.js.PdfSideBar
     */
    SCROLL_MODE("Scroll Mode"),

    /**
     * Menu item for custom page arrangement.
     */
    CUSTOM_PAGE_ARRANGEMENT("Custom Page Arrangement"),

    /**
     * Menu item to toggle spread mode.
     *
     * @see com.bhuvaneshw.pdf.js.PdfSideBar
     */
    SPREAD_MODE("Split Mode"),

    /**
     * Menu item to align the document.
     */
    ALIGN_MODE("Align Mode"),

    /**
     * Menu item to snap to pages.
     */
    SNAP_PAGE("Snap Page"),

    /**
     * Menu item to view document properties.
     */
    PROPERTIES("Properties")
}
