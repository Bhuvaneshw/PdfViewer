package com.bhuvaneshw.pdf.setting

import android.webkit.WebView
import com.bhuvaneshw.pdf.PdfEditorModeApi
import com.bhuvaneshw.pdf.js.PdfFindBar
import com.bhuvaneshw.pdf.js.PdfSideBar
import com.bhuvaneshw.pdf.js.call
import com.bhuvaneshw.pdf.js.callDirectly
import com.bhuvaneshw.pdf.js.invoke
import com.bhuvaneshw.pdf.js.set
import com.bhuvaneshw.pdf.js.toJsString
import com.bhuvaneshw.pdf.js.with

class UiSettings internal constructor(private val webView: WebView) {

    @PdfEditorModeApi
    inner class EditorMode internal constructor() {
        var enabled: Boolean = false
            set(value) {
                field = value
                webView callDirectly "setEditorModeButtonsEnabled"(value)
            }
        var editorHighlightButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setEditorHighlightButtonEnabled"(value)
            }
        var editorFreeTextButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setEditorFreeTextButtonEnabled"(value)
            }
        var editorStampButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setEditorStampButtonEnabled"(value)
            }
        var editorInkButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setEditorInkButtonEnabled"(value)
            }
    }

    inner class ToolbarMiddle internal constructor() {
        var enabled: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setToolbarViewerMiddleEnabled"(value)
            }
        var zoomInButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setZoomInButtonEnabled"(value)
            }
        var zoomOutButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setZoomOutButtonEnabled"(value)
            }
        var zoomScaleSelectContainer: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setZoomScaleSelectContainerEnabled"(value)
            }
    }

    inner class ToolbarLeft internal constructor() {
        var enabled: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setToolbarViewerLeftEnabled"(value)
            }
        var sidebarToggleButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSidebarToggleButtonEnabled"(value)
            }
        var findButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setViewFindButtonEnabled"(value)
            }
        var pageNumberContainer: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setPageNumberContainerEnabled"(value)
            }
    }

    inner class ToolbarRight internal constructor() {
        var enabled: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setToolbarViewerRightEnabled"(value)
            }

        @OptIn(PdfEditorModeApi::class)
        val editorMode = EditorMode()
        var secondaryToolbarToggleButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSecondaryToolbarToggleButtonEnabled"(value)
            }
    }

    inner class ToolbarSecondary internal constructor() {
        var secondaryPrint: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSecondaryPrintEnabled"(value)
            }
        var secondaryDownload: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSecondaryDownloadEnabled"(value)
            }
        var presentationMode: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setPresentationModeEnabled"(value)
            }
        var goToFirstPage: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setGoToFirstPageEnabled"(value)
            }
        var goToLastPage: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setGoToLastPageEnabled"(value)
            }
        var pageRotateClockwise: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setPageRotateCwEnabled"(value)
            }
        var pageRotateCounterClockwise: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setPageRotateCcwEnabled"(value)
            }
        var cursorSelectTool: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setCursorSelectToolEnabled"(value)
            }
        var cursorHandTool: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setCursorHandToolEnabled"(value)
            }
        var scrollPageWise: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setScrollPageEnabled"(value)
            }
        var scrollVertical: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setScrollVerticalEnabled"(value)
            }
        var scrollHorizontal: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setScrollHorizontalEnabled"(value)
            }
        var scrollWrapped: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setScrollWrappedEnabled"(value)
            }
        var spreadNone: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSpreadNoneEnabled"(value)
            }
        var spreadOdd: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSpreadOddEnabled"(value)
            }
        var spreadEven: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSpreadEvenEnabled"(value)
            }
        var documentProperties: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setDocumentPropertiesEnabled"(value)
            }
    }

    inner class PasswordDialog internal constructor() {
        fun getLabelText(callback: (String?) -> Unit) {
            webView callDirectly "getLabelText"(callback = callback)
        }

        fun submitPassword(password: String) {
            webView callDirectly "JWI.onPasswordDialogChange"(false)
            webView callDirectly "submitPassword"(password.toJsString())
        }

        fun cancel() {
            webView callDirectly "cancelPasswordDialog"()
        }
    }

    inner class PrintDialog internal constructor() {
        fun cancel() {
            webView callDirectly "cancelPrinting"()
        }
    }

    val toolbarLeft = ToolbarLeft()
    val toolbarMiddle = ToolbarMiddle()
    val toolbarRight = ToolbarRight()
    val toolBarSecondary = ToolbarSecondary()
    val passwordDialog = PasswordDialog()
    val printDialog = PrintDialog()

    var toolbarEnabled: Boolean = false
        set(value) {
            field = value
            webView callDirectly "setToolbarEnabled"(value)
        }
    var isSideBarOpen: Boolean = false
        set(value) {
            field = value
            webView with PdfSideBar call if (value) "open"() else "close"()
            webView with PdfSideBar set "sidebarContainer.style.display"((if (value) "" else "none").toJsString())
        }
    var isFindBarOpen: Boolean = false
        set(value) {
            field = value
            webView with PdfFindBar call if (value) "open"() else "close"()
        }
    var viewerScrollbar: Boolean = true
        set(value) {
            field = value
            webView.isVerticalScrollBarEnabled = value
            webView.isHorizontalScrollBarEnabled = value
            webView callDirectly "setViewerScrollbar"(value)
        }

}