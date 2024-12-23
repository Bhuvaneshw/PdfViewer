function openFile(args) {
    PDFViewerApplication.open(args)
        .then(() => sendDocumentProperties())
        .catch((e) => JWI.onLoadFailed(e.message));

    let callback = (event) => {
        const { pageNumber } = event;
        PDFViewerApplication.eventBus.off("pagerendered", callback);
        JWI.onLoadSuccess(PDFViewerApplication.pagesCount);
    };
    PDFViewerApplication.eventBus.on("pagerendered", callback);
}

let DOUBLE_CLICK_THRESHOLD = 300;
let LONG_CLICK_THRESHOLD = 500;
function doOnLast() {
    const observerTarget = document.querySelector("#passwordDialog");
    observerTarget.style.margin = "24px auto";
    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === "attributes" && mutation.attributeName === "open") {
                JWI.onPasswordDialogChange(observerTarget.getAttribute("open") !== null);
            }
        });
    });
    observer.observe(observerTarget, { attributes: true });

    const viewerContainer = $("#viewerContainer");
    let singleClickTimer;
    let longClickTimer;
    let isLongClick = false;

    viewerContainer.addEventListener("click", (e) => {
        e.preventDefault();
        if (e.detail === 1) {
            singleClickTimer = setTimeout(() => {
                if (e.target.tagName === "A") JWI.onLinkClick(e.target.href);
                else JWI.onSingleClick();
            }, DOUBLE_CLICK_THRESHOLD);
        }
    });

    viewerContainer.addEventListener("dblclick", (e) => {
        clearTimeout(singleClickTimer);
        JWI.onDoubleClick();
    });

    viewerContainer.addEventListener("touchstart", (e) => {
        isLongClick = false;
        if (e.touches.length === 1) {
            longClickTimer = setTimeout(() => {
                isLongClick = true;
                JWI.onLongClick();
            }, LONG_CLICK_THRESHOLD);
        }
    });

    viewerContainer.addEventListener("touchend", (e) => {
        clearTimeout(longClickTimer);
    });

    viewerContainer.addEventListener("touchmove", (e) => {
        clearTimeout(longClickTimer);
    });
}

function setupHelper() {
    PDFViewerApplication.findBar.highlightAll.click();

    PDFViewerApplication.eventBus.on("scalechanging", (event) => {
        const { scale } = event;
        JWI.onScaleChange(scale, PDFViewerApplication.pdfViewer.currentScaleValue);
    });

    PDFViewerApplication.eventBus.on("pagechanging", (event) => {
        const { pageNumber } = event;
        JWI.onPageChange(pageNumber);
    });

    PDFViewerApplication.eventBus.on("updatefindcontrolstate", (event) => {
        JWI.onFindMatchChange(event.matchesCount?.current || 0, event.matchesCount?.total || 0);
    });

    PDFViewerApplication.eventBus.on("updatefindmatchescount", (event) => {
        JWI.onFindMatchChange(event.matchesCount?.current || 0, event.matchesCount?.total || 0);
    });

    PDFViewerApplication.eventBus.on("spreadmodechanged", (event) => {
        JWI.onSpreadModeChange(event.mode);
    });

    PDFViewerApplication.eventBus.on("scrollmodechanged", (event) => {
        JWI.onScrollModeChange(event.mode);
    });

    const viewerContainer = $("#viewerContainer");
    viewerContainer.addEventListener("scroll", () => {
        let currentOffset;
        let totalScrollable;
        let isHorizontalScroll = false;

        if (viewerContainer.scrollHeight > viewerContainer.clientHeight) {
            currentOffset = viewerContainer.scrollTop;
            totalScrollable = viewerContainer.scrollHeight - viewerContainer.clientHeight;
        } else if (viewerContainer.scrollWidth > viewerContainer.clientWidth) {
            currentOffset = viewerContainer.scrollLeft;
            totalScrollable = viewerContainer.scrollWidth - viewerContainer.clientWidth;
            isHorizontalScroll = true;
        }

        JWI.onScroll(Math.round(currentOffset), totalScrollable, isHorizontalScroll);
    });

    const searchInput = document.getElementById("findInput");
    const observer = new MutationObserver((mutationsList) => {
        mutationsList.forEach((mutation) => {
            if (mutation.type === "attributes" && mutation.attributeName === "data-status") {
                const newStatus = searchInput.getAttribute("data-status");

                switch (newStatus) {
                    case "pending":
                        JWI.onFindMatchStart();
                        break;
                    case "notFound":
                        JWI.onFindMatchComplete(false);
                        break;
                    default:
                        JWI.onFindMatchComplete(true);
                }
            }
        });
    });
    observer.observe(searchInput, {
        attributes: true,
        attributeFilter: ["data-status"],
    });
}

function setEditorModeButtonsEnabled(enabled) {
    $("#editorModeButtons").style.display = enabled ? "inline flex" : "none";
}

function setEditorHighlightButtonEnabled(enabled) {
    $("#editorHighlight").style.display = enabled ? "inline-block" : "none";
}

function setEditorFreeTextButtonEnabled(enabled) {
    $("#editorFreeText").style.display = enabled ? "inline-block" : "none";
}

function setEditorStampButtonEnabled(enabled) {
    $("#editorStamp").style.display = enabled ? "inline-block" : "none";
}

function setEditorInkButtonEnabled(enabled) {
    $("#editorInk").style.display = enabled ? "inline-block" : "none";
}

function setToolbarViewerMiddleEnabled(enabled) {
    $("#toolbarViewerMiddle").style.display = enabled ? "flex" : "none";
}

function setToolbarViewerLeftEnabled(enabled) {
    $("#toolbarViewerLeft").style.display = enabled ? "flex" : "none";
}

function setToolbarViewerRightEnabled(enabled) {
    $("#toolbarViewerRight").style.display = enabled ? "flex" : "none";
}

function setSidebarToggleButtonEnabled(enabled) {
    $("#sidebarToggleButton").style.display = enabled ? "flex" : "none";
}

function setPageNumberContainerEnabled(enabled) {
    $("#numPages").parentElement.style.display = enabled ? "flex" : "none";
}

function setViewFindButtonEnabled(enabled) {
    $("#viewFindButton").style.display = enabled ? "flex" : "none";
}

function setZoomOutButtonEnabled(enabled) {
    $("#zoomOutButton").style.display = enabled ? "flex" : "none";
}

function setZoomInButtonEnabled(enabled) {
    $("#zoomInButton").style.display = enabled ? "flex" : "none";
}

function setZoomScaleSelectContainerEnabled(enabled) {
    $("#scaleSelectContainer").style.display = enabled ? "flex" : "none";
}

function setSecondaryToolbarToggleButtonEnabled(enabled) {
    $("#secondaryToolbarToggleButton").style.display = enabled ? "flex" : "none";
}

function setToolbarEnabled(enabled) {
    $(".toolbar").style.display = enabled ? "block" : "none";
    $("#viewerContainer").style.top = enabled ? "var(--toolbar-height)" : "0px";
    $("#viewerContainer").style.setProperty("--visible-toolbar-height", enabled ? "var(--toolbar-height)" : "0px");
}

function setSecondaryPrintEnabled(enabled) {
    $("#secondaryPrint").style.display = enabled ? "flex" : "none";
}

function setSecondaryDownloadEnabled(enabled) {
    $("#secondaryDownload").style.display = enabled ? "flex" : "none";
}

function setPresentationModeEnabled(enabled) {
    $("#presentationMode").style.display = enabled ? "flex" : "none";
}

function setGoToFirstPageEnabled(enabled) {
    $("#firstPage").style.display = enabled ? "flex" : "none";
}

function setGoToLastPageEnabled(enabled) {
    $("#lastPage").style.display = enabled ? "flex" : "none";
}

function setPageRotateCwEnabled(enabled) {
    $("#pageRotateCw").style.display = enabled ? "flex" : "none";
}

function setPageRotateCcwEnabled(enabled) {
    $("#pageRotateCcw").style.display = enabled ? "flex" : "none";
}

function setCursorSelectToolEnabled(enabled) {
    $("#cursorSelectTool").style.display = enabled ? "flex" : "none";
}

function setCursorHandToolEnabled(enabled) {
    $("#cursorHandTool").style.display = enabled ? "flex" : "none";
}

function setScrollPageEnabled(enabled) {
    $("#scrollPage").style.display = enabled ? "flex" : "none";
}

function setScrollVerticalEnabled(enabled) {
    $("#scrollVertical").style.display = enabled ? "flex" : "none";
}

function setScrollHorizontalEnabled(enabled) {
    $("#scrollHorizontal").style.display = enabled ? "flex" : "none";
}

function setScrollWrappedEnabled(enabled) {
    $("#scrollWrapped").style.display = enabled ? "flex" : "none";
}

function setSpreadNoneEnabled(enabled) {
    $("#spreadNone").style.display = enabled ? "flex" : "none";
}

function setSpreadOddEnabled(enabled) {
    $("#spreadOdd").style.display = enabled ? "flex" : "none";
}

function setSpreadEvenEnabled(enabled) {
    $("#spreadEven").style.display = enabled ? "flex" : "none";
}

function setDocumentPropertiesEnabled(enabled) {
    $("#documentProperties").style.display = enabled ? "flex" : "none";
}

function downloadFile() {
    $("#secondaryDownload").click();
}

function printFile() {
    $("#secondaryPrint").click();
}

function startPresentationMode() {
    $("#presentationMode").click();
}

function goToFirstPage() {
    $("#firstPage").click();
}

function goToLastPage() {
    $("#lastPage").click();
}

function selectCursorSelectTool() {
    $("#cursorSelectTool").click();
}

function selectCursorHandTool() {
    $("#cursorHandTool").click();
}

function selectScrollPage() {
    $("#scrollPage").click();
}

function selectScrollVertical() {
    $("#scrollVertical").click();
}

function selectScrollHorizontal() {
    $("#scrollHorizontal").click();
}

function selectScrollWrapped() {
    $("#scrollWrapped").click();
}

function selectSpreadNone() {
    $("#spreadNone").click();
}

function selectSpreadOdd() {
    $("#spreadOdd").click();
}

function selectSpreadEven() {
    $("#spreadEven").click();
}

function showDocumentProperties() {
    $("#documentProperties").click();
}

function startFind(searchTerm) {
    const findInput = $("#findInput");
    if (findInput) {
        findInput.value = searchTerm;

        const caseSensitive = $("#findMatchCase")?.checked || false;
        const entireWord = $("#findEntireWord")?.checked || false;
        const highlightAll = $("#findHighlightAll")?.checked || false;
        const matchDiacritics = $("#findMatchDiacritics")?.checked || false;

        PDFViewerApplication.eventBus.dispatch("find", {
            source: this,
            type: "",
            query: searchTerm,
            phraseSearch: false,
            caseSensitive: caseSensitive,
            entireWord: entireWord,
            highlightAll: highlightAll,
            matchDiacritics: matchDiacritics,
            findPrevious: false,
        });
    } else {
        console.error("Find toolbar input not found.");
    }
}

function stopFind() {
    PDFViewerApplication.eventBus.dispatch("find", {
        source: this,
        type: "",
        query: "",
        phraseSearch: false,
        caseSensitive: false,
        entireWord: false,
        highlightAll: false,
        findPrevious: false,
    });
}

function findNext() {
    $("#findNextButton").click();
}

function findPrevious() {
    $("#findPreviousButton").click();
}

function setFindHighlightAll(enabled) {
    $("#findHighlightAll").checked = enabled;
}

function setFindMatchCase(enabled) {
    $("#findMatchCase").checked = enabled;
}

function setFindEntireWord(enabled) {
    $("#findEntireWord").checked = enabled;
}

function setFindMatchDiacritics(enabled) {
    $("#findMatchDiacritics").checked = enabled;
}

function setViewerScrollbar(enabled) {
    if (enabled) $("#viewerContainer").classList.remove("noScrollbar");
    else $("#viewerContainer").classList.add("noScrollbar");
}

function scrollTo(offset) {
    $("#viewerContainer").scrollTop = offset;
}

function scrollToRatio(ratio, isHorizontalScroll) {
    let viewerContainer = $("#viewerContainer");
    if (isHorizontalScroll) {
        let totalScrollable = viewerContainer.scrollWidth - viewerContainer.clientWidth;
        viewerContainer.scrollLeft = totalScrollable * ratio;
    } else {
        let totalScrollable = viewerContainer.scrollHeight - viewerContainer.clientHeight;
        viewerContainer.scrollTop = totalScrollable * ratio;
    }
}

function sendDocumentProperties() {
    PDFViewerApplication.pdfDocument.getMetadata().then((info) => {
        JWI.onLoadProperties(
            info.info.Title || "-",
            info.info.Subject || "-",
            info.info.Author || "-",
            info.info.Creator || "-",
            info.info.Producer || "-",
            info.info.CreationDate || "-",
            info.info.ModDate || "-",
            info.info.Keywords || "-",
            info.info.Language || "-",
            info.info.PDFFormatVersion || "-",
            info.contentLength || 0,
            info.info.IsLinearized || "-",
            info.info.EncryptFilterName || "-",
            info.info.IsAcroFormPresent || "-",
            info.info.IsCollectionPresent || "-",
            info.info.IsSignaturesPresent || "-",
            info.info.IsXFAPresent || "-",
            JSON.stringify(info.info.Custom || "{}")
        );
    });
}

function getLabelText() {
    return $("#passwordText").innerText;
}

function submitPassword(password) {
    $("#password").value = password;
    $("#passwordSubmit").click();
}

function cancelPasswordDialog() {
    $("#passwordCancel").click();
}

function getActualScaleFor(value) {
    const SCROLLBAR_PADDING = 40;
    const VERTICAL_PADDING = 5;
    const MAX_AUTO_SCALE = 1.25;
    const ScrollMode = {
        UNKNOWN: -1,
        VERTICAL: 0,
        HORIZONTAL: 1,
        WRAPPED: 2,
        PAGE: 3,
    };
    const SpreadMode = {
        UNKNOWN: -1,
        NONE: 0,
        ODD: 1,
        EVEN: 2,
    };
    const currentPage = PDFViewerApplication.pdfViewer._pages[PDFViewerApplication.pdfViewer._currentPageNumber - 1];
    if (!currentPage) return -1;
    let hPadding = SCROLLBAR_PADDING,
        vPadding = VERTICAL_PADDING;
    if (this.isInPresentationMode) {
        hPadding = vPadding = 4;
        if (this._spreadMode !== SpreadMode.NONE) {
            hPadding *= 2;
        }
    } else if (this.removePageBorders) {
        hPadding = vPadding = 0;
    } else if (this._scrollMode === ScrollMode.HORIZONTAL) {
        [hPadding, vPadding] = [vPadding, hPadding];
    }
    const pageWidthScale = (((PDFViewerApplication.pdfViewer.container.clientWidth - hPadding) / currentPage.width) * currentPage.scale) / PDFViewerApplication.pdfViewer.pageWidthScaleFactor();
    const pageHeightScale = ((PDFViewerApplication.pdfViewer.container.clientHeight - vPadding) / currentPage.height) * currentPage.scale;
    let scale = -3;
    function isPortraitOrientation(size) {
        return size.width <= size.height;
    }
    switch (value) {
        case "page-actual":
            scale = 1;
            break;
        case "page-width":
            scale = pageWidthScale;
            break;
        case "page-height":
            scale = pageHeightScale;
            break;
        case "page-fit":
            scale = Math.min(pageWidthScale, pageHeightScale);
            break;
        case "auto":
            const horizontalScale = isPortraitOrientation(currentPage) ? pageWidthScale : Math.min(pageHeightScale, pageWidthScale);
            scale = Math.min(MAX_AUTO_SCALE, horizontalScale);
            break;
        default:
            scale = -2;
    }
    return scale;
}

function enableVerticalSnapBehavior() {
    let viewerContainer = $("#viewerContainer");

    viewerContainer.classList.remove("horizontal-snap");
    viewerContainer.classList.add("vertical-snap");
    viewerContainer.style.scrollSnapType = "y mandatory";
    viewerContainer._originalScrollSnapType = "y mandatory";
}

function enableHorizontalSnapBehavior() {
    let viewerContainer = $("#viewerContainer");

    viewerContainer.classList.remove("vertical-snap");
    viewerContainer.classList.add("horizontal-snap");
    viewerContainer.style.scrollSnapType = "x mandatory";
    viewerContainer._originalScrollSnapType = "x mandatory";
}

function removeSnapBehavior() {
    let viewerContainer = $("#viewerContainer");

    viewerContainer.classList.remove("vertical-snap");
    viewerContainer.classList.remove("horizontal-snap");
    viewerContainer.style.scrollSnapType = "none";
    viewerContainer._originalScrollSnapType = "none";
}

function centerPage(vertical, horizontal, singlePageArrangemenentEnabled = false) {
    let viewerContainer = $("#viewerContainer");

    if (singlePageArrangemenentEnabled) {
        viewerContainer.classList.add("single-page-arrangement");
        viewerContainer.classList.remove("vertical-center");
        viewerContainer.classList.remove("horizontal-center");

        if (vertical) viewerContainer.classList.add("single-page-arrangement-vertical-center");
        else viewerContainer.classList.remove("single-page-arrangement-vertical-center");

        if (horizontal) viewerContainer.classList.add("single-page-arrangement-horizontal-center");
        else viewerContainer.classList.remove("single-page-arrangement-horizontal-center");
    } else {
        viewerContainer.classList.remove("single-page-arrangement");
        viewerContainer.classList.remove("single-page-arrangement-vertical-center");
        viewerContainer.classList.remove("single-page-arrangement-horizontal-center");

        if (vertical) viewerContainer.classList.add("vertical-center");
        else viewerContainer.classList.remove("vertical-center");

        if (horizontal) viewerContainer.classList.add("horizontal-center");
        else viewerContainer.classList.remove("horizontal-center");
    }
}

function applySinglePageArrangement() {
    if ($all(".full-size-container").length != 0) return "Already in view pager mode";

    let pages = $all(".page");

    pages.forEach((page) => {
        let parent = page.parentElement;
        parent.removeChild(page);

        let pageContainer = document.createElement("div");
        pageContainer.classList.add("full-size-container");

        pageContainer.appendChild(page);
        parent.appendChild(pageContainer);
    });
}

function removeSinglePageArrangement() {
    let pageContainers = $all(".full-size-container");

    pageContainers.forEach((pageContainer) => {
        let parent = pageContainer.parentElement;
        let page = pageContainer.children[0];

        parent.removeChild(pageContainer);
        parent.appendChild(page);
    });
}

function limitScroll(maxSpeed = 100) {
    const viewerContainer = document.querySelector("#viewerContainer");
    if (!viewerContainer) return;

    let lastTouchX = 0;
    let lastTouchY = 0;

    const disableSnap = () => {
        viewerContainer.style.scrollSnapType = "none";
    };

    const restoreSnap = () => {
        viewerContainer.style.scrollSnapType = viewerContainer._originalScrollSnapType;
    };

    const touchStartHandler = (event) => {
        if (event.touches.length > 1) return;

        lastTouchX = event.touches[0].clientX;
        lastTouchY = event.touches[0].clientY;
        disableSnap();
    };

    const touchMoveHandler = (event) => {
        if (event.touches.length > 1) return;

        const touch = event.touches[0];
        const currentTouchX = touch.clientX;
        const currentTouchY = touch.clientY;

        let deltaX = lastTouchX - currentTouchX;
        let deltaY = lastTouchY - currentTouchY;

        if (Math.abs(deltaX) > maxSpeed) {
            deltaX = deltaX > 0 ? maxSpeed : -maxSpeed;
        }

        if (Math.abs(deltaY) > maxSpeed) {
            deltaY = deltaY > 0 ? maxSpeed : -maxSpeed;
        }

        viewerContainer.scrollLeft += deltaX;
        viewerContainer.scrollTop += deltaY;

        lastTouchX = currentTouchX;
        lastTouchY = currentTouchY;

        event.preventDefault();
    };

    const touchEndHandler = (event) => {
        if (event.touches.length > 1) return;

        restoreSnap();
    };

    viewerContainer.addEventListener("touchstart", touchStartHandler);
    viewerContainer.addEventListener("touchmove", touchMoveHandler, { passive: false });
    viewerContainer.addEventListener("touchend", touchEndHandler);

    viewerContainer._scrollHandlers = { touchStartHandler, touchMoveHandler, touchEndHandler };
}

function removeScrollLimit() {
    const viewerContainer = document.querySelector("#viewerContainer");
    if (!viewerContainer || !viewerContainer._scrollHandlers) return;

    const { touchStartHandler, touchMoveHandler, touchEndHandler } = viewerContainer._scrollHandlers;

    viewerContainer.removeEventListener("touchstart", touchStartHandler);
    viewerContainer.removeEventListener("touchmove", touchMoveHandler);
    viewerContainer.removeEventListener("touchend", touchEndHandler);

    viewerContainer.style.scrollSnapType = window.getComputedStyle(viewerContainer).scrollSnapType;

    delete viewerContainer._scrollHandlers;
}

function $(query) {
    return document.querySelector(query);
}

function $all(query) {
    return document.querySelectorAll(query);
}
