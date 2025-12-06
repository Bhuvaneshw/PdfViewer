package com.bhuvaneshw.pdf.ui

/**
 * Represents the menu items available in the PDF toolbar.
 *
 * @property id The unique identifier for the menu item.
 */
enum class PdfToolBarMenuItem(internal val id: Int) {
    /**
     * Menu item to download the PDF document.
     */
    DOWNLOAD(0),

    /**
     * Menu item to control the zoom level of the document.
     */
    ZOOM(1),

    /**
     * Menu item to navigate to a specific page in the document.
     */
    GO_TO_PAGE(2),

    /**
     * Menu item to rotate the document clockwise.
     * @see com.bhuvaneshw.pdf.PdfEditor
     */
    ROTATE_CLOCK_WISE(3),

    /**
     * Menu item to rotate the document anti-clockwise.
     * @see com.bhuvaneshw.pdf.PdfEditor
     */
    ROTATE_ANTI_CLOCK_WISE(4),

    /**
     * Menu item to change the scroll mode of the document.
     */
    SCROLL_MODE(5),

    /**
     * Menu item to arrange the document in single page mode.
     */
    SINGLE_PAGE_ARRANGEMENT(6),

    /**
     * Menu item to arrange the document in spread (two-page) mode.
     */
    SPREAD_MODE(7),

    /**
     * Menu item to change the alignment of the document.
     */
    ALIGN_MODE(8),

    /**
     * Menu item to enable or disable page snapping.
     */
    SNAP_PAGE(9),

    /**
     * Menu item to view the properties of the document.
     * @see com.bhuvaneshw.pdf.PdfDocumentProperties
     */
    PROPERTIES(10),

    /**
     * Menu item to view the document outline.
     */
    OUTLINE(11),

    /**
     * Menu item to view the document attachments.
     */
    ATTACHMENTS(12),
}
