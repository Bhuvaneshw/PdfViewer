function openUrl(args) {
    PDFViewerApplication.open(args)
        .then(() => sendDocumentProperties())
        .catch((e) => {
//            JWI.onLoadFailed(e.message)
        });

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
    hideAllControls();

    const loadingBar = document.getElementById("loadingBar");
    const loadingObserver = new MutationObserver(() => {
        const progress = parseInt(getComputedStyle(loadingBar).getPropertyValue("--progressBar-percent"));
        JWI.onProgressChange(progress);
    });
    loadingObserver.observe(loadingBar, {
        attributes: true,
        attributeFilter: ["style"],
    });

    const passwordDialog = $("#passwordDialog");
    passwordDialog.style.margin = "24px auto";
    const passwordDialogObserver = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === "attributes" && mutation.attributeName === "open") {
                JWI.onPasswordDialogChange(passwordDialog.getAttribute("open") !== null);
            }
        });
    });
    passwordDialogObserver.observe(passwordDialog, { attributes: true });

    const printDialog = $("#printServiceDialog");
    const printDialogObserver = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === "attributes" && mutation.attributeName === "open") {
                printDialog.style.display = "none";
                if (printDialog.open) {
                    JWI.onPrintProcessStart();
                } else {
                    JWI.onPrintProcessEnd();
                }
            }
        });
    });
    printDialogObserver.observe(printDialog, { attributes: true });

    const printProgress = printDialog.querySelector("progress");
    const printProgressObserver = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === "attributes" && mutation.attributeName === "value") {
                JWI.onPrintProcessProgress(parseFloat(printProgress.value) / 100);
            }
        });
    });
    printProgressObserver.observe(printProgress, { attributes: true });

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
    PDFViewerApplication.pdfSidebar.close();

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
        let isHorizontalScroll = PDFViewerApplication.pdfViewer._scrollMode === ScrollMode.HORIZONTAL;

        if (isHorizontalScroll) {
            currentOffset = viewerContainer.scrollLeft;
            totalScrollable = viewerContainer.scrollWidth - viewerContainer.clientWidth;
        } else {
            currentOffset = viewerContainer.scrollTop;
            totalScrollable = viewerContainer.scrollHeight - viewerContainer.clientHeight;
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

function hideAllControls() {
    const ids = ["#sidebarContainer", ".hiddenSmallView", "#editorModeButtons", ".hiddenMediumView", "#secondaryOpenFile", "#viewBookmark"];

    setToolbarEnabled(false);
    ids.forEach((id) => {
        $(id).style.display = "none";
    });
}

window.originalPrint = window.print;
window.print = () => {
    JWI.createPrintJob();
};

function extractPrintImages() {
    let pages = $("#printContainer").querySelectorAll("img");
    JWI.conveyMessage(null, "PRINT_START", null);

    pages.forEach((page, index) => {
        const canvas = document.createElement("canvas");
        const ctx = canvas.getContext("2d");

        canvas.width = page.naturalWidth;
        canvas.height = page.naturalHeight;

        ctx.drawImage(page, 0, 0);

        const base64Data = canvas.toDataURL("image/png");

        JWI.conveyMessage(base64Data, "PAGE_DATA", `${index + 1}`);
    });

    $("#printContainer").textContent = "";
    JWI.conveyMessage(null, "PRINT_END", null);
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
    $("#printContainer").textContent = "";
    $("#secondaryPrint").click();
}

function cancelPrinting() {
    $("#printCancel").click();
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

const ScrollMode = {
    UNKNOWN: -1,
    VERTICAL: 0,
    HORIZONTAL: 1,
    WRAPPED: 2,
    PAGE: 3,
};

function getActualScaleFor(value) {
    const SCROLLBAR_PADDING = 40;
    const VERTICAL_PADDING = 5;
    const MAX_AUTO_SCALE = 1.25;
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
    if (PDFViewerApplication.pdfViewer.isInPresentationMode) {
        hPadding = vPadding = 4;
        if (PDFViewerApplication.pdfViewer._spreadMode !== SpreadMode.NONE) {
            hPadding *= 2;
        }
    } else if (PDFViewerApplication.pdfViewer.removePageBorders) {
        hPadding = vPadding = 0;
    } else if (PDFViewerApplication.pdfViewer._scrollMode === ScrollMode.HORIZONTAL) {
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

function limitScroll(maxSpeed = 100, flingThreshold = 0.5, canFling = false, adaptiveFling = false) {
    const viewerContainer = $("#viewerContainer");
    if (!viewerContainer) return;

    let lastTouchX = 0;
    let lastTouchY = 0;
    let lastTouchTime = 0;
    let accumulatedDeltaX = 0;
    let accumulatedDeltaY = 0;
    let restoreTimer;
    let isDragging = false;

    viewerContainer._originalScrollSnapType = window.getComputedStyle(viewerContainer).scrollSnapType;

    const disableSnap = () => {
        viewerContainer.style.scrollSnapType = "none";
        if (restoreTimer) clearTimeout(restoreTimer);
    };

    const restoreSnap = () => {
        viewerContainer.style.scrollSnapType = viewerContainer._originalScrollSnapType;
    };

    const clamp = (value, max) => Math.max(-max, Math.min(value, max));

    const touchStartHandler = (event) => {
        if (event.touches.length > 1) return;

        lastTouchX = event.touches[0].clientX;
        lastTouchY = event.touches[0].clientY;
        lastTouchTime = event.timeStamp;
        PDFViewerApplication._touchStartCurrentPage = PDFViewerApplication.page;

        accumulatedDeltaX = 0;
        accumulatedDeltaY = 0;
        isDragging = false;

        disableSnap();
    };

    const touchMoveHandler = (event) => {
        if (event.touches.length > 1) return;

        const touch = event.touches[0];
        const currentTouchX = touch.clientX;
        const currentTouchY = touch.clientY;

        let deltaX = lastTouchX - currentTouchX;
        let deltaY = lastTouchY - currentTouchY;

        deltaX = clamp(deltaX, maxSpeed);
        deltaY = clamp(deltaY, maxSpeed);
        if (!isDragging && (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5)) {
            isDragging = true;
        }

        viewerContainer.scrollLeft += deltaX;
        viewerContainer.scrollTop += deltaY;

        accumulatedDeltaX += deltaX;
        accumulatedDeltaY += deltaY;

        lastTouchX = currentTouchX;
        lastTouchY = currentTouchY;

        event.preventDefault();
    };

    const touchEndHandler = (event) => {
        if (!isDragging) return;

        const touchEndTime = event.timeStamp;
        const timeElapsed = touchEndTime - lastTouchTime;

        const velocityX = accumulatedDeltaX / timeElapsed;
        const velocityY = accumulatedDeltaY / timeElapsed;

        const isVerticalScroll = PDFViewerApplication.pdfViewer.scrollMode == ScrollMode.VERTICAL;
        const isHorizontalScroll = PDFViewerApplication.pdfViewer.scrollMode == ScrollMode.HORIZONTAL;

        const containerHeight = viewerContainer.clientHeight;
        const containerWidth = viewerContainer.clientWidth;

        let targetPage = PDFViewerApplication.pdfViewer.getPageView(PDFViewerApplication._touchStartCurrentPage - 1);
        const pageHeight = targetPage.div.clientHeight;
        const pageWidth = targetPage.div.clientWidth;

        const canFlingPage = adaptiveFling ? pageWidth < containerWidth || pageHeight < containerHeight : canFling;

        event.preventDefault();

        if (canFlingPage && isHorizontalScroll && Math.abs(velocityX) > flingThreshold && Math.abs(velocityX) > Math.abs(velocityY)) {
            if (velocityX > 0) {
                setScrollToNextPage();
            } else {
                setScrollToPreviousPage();
            }
        } else if (canFlingPage && isVerticalScroll && Math.abs(velocityY) > flingThreshold && Math.abs(velocityY) > Math.abs(velocityX)) {
            if (velocityY > 0) {
                setScrollToNextPage();
            } else {
                setScrollToPreviousPage();
            }
        } else if (isVerticalScroll && viewerContainer.scrollTop > targetPage.div.offsetTop + (targetPage.div.clientWidth * 4) / 5) {
            setScrollToNextPage();
        } else if (isVerticalScroll && viewerContainer.scrollTop < targetPage.div.offsetTop - (containerHeight * 2) / 4) {
            setScrollToPreviousPage();
        } else if (isHorizontalScroll && viewerContainer.scrollLeft > targetPage.div.offsetLeft + (targetPage.div.clientWidth * 4) / 5) {
            setScrollToNextPage();
        } else if (isHorizontalScroll && viewerContainer.scrollLeft < targetPage.div.offsetLeft - (containerHeight * 2) / 4) {
            setScrollToPreviousPage();
        } else if (setScrollToCurrentPage()) {
            restoreTimer = setTimeout(() => {
                restoreSnap();
            }, 500);
        } else {
            //restoreSnap();
        }
    };

    const resizeAndScaleListener = () => {
        setScrollToCurrentPage();
    };

    viewerContainer.addEventListener("touchstart", touchStartHandler);
    viewerContainer.addEventListener("touchmove", touchMoveHandler, { passive: false });
    viewerContainer.addEventListener("touchend", touchEndHandler, { passive: false });
    window.addEventListener("resize", resizeAndScaleListener);
    PDFViewerApplication.eventBus.on("scalechanging", resizeAndScaleListener);

    viewerContainer._scrollHandlers = { touchStartHandler, touchMoveHandler, touchEndHandler, resizeAndScaleListener };
}

function removeScrollLimit() {
    const viewerContainer = $("#viewerContainer");
    if (!viewerContainer || !viewerContainer._scrollHandlers) return;

    const { touchStartHandler, touchMoveHandler, touchEndHandler, resizeAndScaleListener } = viewerContainer._scrollHandlers;

    viewerContainer.removeEventListener("touchstart", touchStartHandler);
    viewerContainer.removeEventListener("touchmove", touchMoveHandler);
    viewerContainer.removeEventListener("touchend", touchEndHandler);
    window.removeEventListener("resize", resizeAndScaleListener);
    PDFViewerApplication.eventBus.off("scalechanging", resizeAndScaleListener);

    viewerContainer.style.scrollSnapType = viewerContainer._originalScrollSnapType;

    delete viewerContainer._scrollHandlers;
}

function setScrollToPreviousPage() {
    setScrollToPage(PDFViewerApplication.pdfViewer.getPageView(PDFViewerApplication._touchStartCurrentPage - 2), true);
}

function setScrollToNextPage() {
    setScrollToPage(PDFViewerApplication.pdfViewer.getPageView(PDFViewerApplication._touchStartCurrentPage));
}

function setScrollToCurrentPage() {
    let targetPage = PDFViewerApplication.pdfViewer.getPageView(PDFViewerApplication._touchStartCurrentPage - 1);
    const viewerContainer = $("#viewerContainer");

    const isVerticalScroll = PDFViewerApplication.pdfViewer.scrollMode == ScrollMode.VERTICAL;
    const isHorizontalScroll = PDFViewerApplication.pdfViewer.scrollMode == ScrollMode.HORIZONTAL;

    if (!targetPage || !viewerContainer) return;

    const containerHeight = viewerContainer.clientHeight;
    const containerWidth = viewerContainer.clientWidth;

    const pageHeight = targetPage.div.clientHeight;
    const pageWidth = targetPage.div.clientWidth;

    const currentScrollTop = viewerContainer.scrollTop;
    const currentScrollLeft = viewerContainer.scrollLeft;

    let targetOffsetTop, targetOffsetLeft;

    if (pageHeight >= containerHeight || pageWidth >= containerWidth) {
        if (isVerticalScroll) {
            let canChange = currentScrollTop < targetPage.div.offsetTop || currentScrollTop + containerHeight > PDFViewerApplication.pdfViewer.getPageView(PDFViewerApplication._touchStartCurrentPage)?.div?.offsetTop || 0;
            if (pageHeight > containerHeight && canChange) targetOffsetTop = nearest(currentScrollTop, targetPage.div.offsetTop, targetPage.div.offsetTop + pageHeight - containerHeight);
            else if (pageWidth > containerWidth && canChange) targetOffsetTop = targetPage.div.offsetTop - Math.abs(containerHeight - pageHeight) / 2;
            else targetOffsetTop = currentScrollTop;
        } else targetOffsetTop = currentScrollTop;
        if (isHorizontalScroll) {
            let canChange = currentScrollLeft < targetPage.div.offsetLeft || currentScrollLeft + containerWidth > PDFViewerApplication.pdfViewer.getPageView(PDFViewerApplication._touchStartCurrentPage)?.div?.offsetLeft || 0;
            if (pageWidth > containerWidth && canChange) targetOffsetLeft = nearest(currentScrollLeft, targetPage.div.offsetLeft, targetPage.div.offsetLeft + pageWidth - containerWidth);
            else if (pageHeight > containerHeight && canChange) targetOffsetLeft = targetPage.div.offsetLeft - Math.abs(containerWidth - pageWidth) / 2;
            else targetOffsetLeft = currentScrollLeft;
        } else targetOffsetLeft = currentScrollLeft;
    } else {
        targetOffsetLeft = targetPage.div.offsetLeft - (targetPage.div.parentElement.clientWidth - targetPage.div.clientWidth) / 2;
        targetOffsetTop = targetPage.div.offsetTop - (targetPage.div.parentElement.clientHeight - targetPage.div.clientHeight) / 2;
    }

    smoothScrollTo(viewerContainer, targetOffsetTop, targetOffsetLeft);
}

function setScrollToPage(targetPage, goToEnd = false) {
    const containerHeight = viewerContainer.clientHeight;
    const containerWidth = viewerContainer.clientWidth;

    const pageHeight = targetPage.div.clientHeight;
    const pageWidth = targetPage.div.clientWidth;

    let targetOffsetTop, targetOffsetLeft;

    if (pageHeight >= containerHeight || pageWidth >= containerWidth) {
        const currentScrollTop = viewerContainer.scrollTop;
        const currentScrollLeft = viewerContainer.scrollLeft;
        const isVerticalScroll = PDFViewerApplication.pdfViewer.scrollMode == ScrollMode.VERTICAL;
        const isHorizontalScroll = PDFViewerApplication.pdfViewer.scrollMode == ScrollMode.HORIZONTAL;

        if (isVerticalScroll) targetOffsetLeft = currentScrollLeft;
        else {
            if (goToEnd) targetOffsetLeft = targetPage.div.offsetLeft + targetPage.div.clientWidth - containerWidth;
            else targetOffsetLeft = targetPage.div.offsetLeft;
        }
        if (isHorizontalScroll) targetOffsetTop = currentScrollTop;
        else {
            if (goToEnd) targetOffsetTop = targetPage.div.offsetTop + targetPage.div.clientHeight - containerHeight;
            else targetOffsetTop = targetPage.div.offsetTop;
        }
    } else {
        targetOffsetLeft = targetPage.div.offsetLeft - (targetPage.div.parentElement.clientWidth - targetPage.div.clientWidth) / 2;
        targetOffsetTop = targetPage.div.offsetTop - (targetPage.div.parentElement.clientHeight - targetPage.div.clientHeight) / 2;
    }

    smoothScrollTo(viewerContainer, targetOffsetTop, targetOffsetLeft);
}

function smoothScrollTo(container, targetScrollTop, targetScrollLeft, duration = 250) {
    let startScrollLeft = container.scrollLeft;
    let startScrollTop = container.scrollTop;
    const distanceLeft = targetScrollLeft - startScrollLeft;
    const distanceTop = targetScrollTop - startScrollTop + 8.5;
    const startTime = performance.now();

    function step(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const easeInOutQuad = progress < 0.5 ? 2 * progress * progress : 1 - Math.pow(-2 * progress + 2, 2) / 2;

        container.scrollLeft = startScrollLeft + distanceLeft * easeInOutQuad;
        container.scrollTop = startScrollTop + distanceTop * easeInOutQuad;

        if (progress < 1) {
            requestAnimationFrame(step);
        }
    }

    requestAnimationFrame(step);
}

function nearest(currentPoint, point1, point2) {
    if (Math.abs(currentPoint - point1) < Math.abs(currentPoint - point2)) {
        return point1;
    } else return point2;
}

function openTextHighlighter() {
    let toggleButton = $("#editorHighlightButton");
    if (toggleButton.classList.contains("toggled")) return;
    toggleButton.click();
}

function closeTextHighlighter() {
    let toggleButton = $("#editorHighlightButton");
    if (!toggleButton.classList.contains("toggled")) return;
    toggleButton.click();
}

function openEditorFreeText() {
    let toggleButton = $("#editorFreeTextButton");
    if (toggleButton.classList.contains("toggled")) return;
    toggleButton.click();
}

function closeEditorFreeText() {
    let toggleButton = $("#editorFreeTextButton");
    if (!toggleButton.classList.contains("toggled")) return;
    toggleButton.click();
}

function openEditorInk() {
    let toggleButton = $("#editorInkButton");
    if (toggleButton.classList.contains("toggled")) return;
    toggleButton.click();
}

function closeEditorInk() {
    let toggleButton = $("#editorInkButton");
    if (!toggleButton.classList.contains("toggled")) return;
    toggleButton.click();
}

function openEditorStamp() {
    let toggleButton = $("#editorStampButton");
    if (toggleButton.classList.contains("toggled")) return;
    toggleButton.click();
}

function closeEditorStamp() {
    let toggleButton = $("#editorStampButton");
    if (!toggleButton.classList.contains("toggled")) return;
    toggleButton.click();
}

function setHighlighterThickness(thickness) {
    let thicknessInput = $("#editorFreeHighlightThickness");
    thicknessInput.value = thickness;
    thicknessInput.dispatchEvent(new Event("input"));
    thicknessInput.dispatchEvent(new Event("change"));
}

function showAllHighlights() {
    let toggleButton = $("#editorHighlightShowAll");
    if (toggleButton.getAttribute("aria-pressed") == "true") return;
    toggleButton.click();
}

function hideAllHighlights() {
    let toggleButton = $("#editorHighlightShowAll");
    if (toggleButton.getAttribute("aria-pressed") == "false") return;
    toggleButton.click();
}

function setFreeTextFontSize(fontSize) {
    let fontSizeInput = $("#editorFreeTextFontSize");
    fontSizeInput.value = fontSize;
    fontSizeInput.dispatchEvent(new Event("input"));
    fontSizeInput.dispatchEvent(new Event("change"));
}

function setFreeTextFontColor(fontColor) {
    let fontColorInput = $("#editorFreeTextColor");
    fontColorInput.value = fontColor;
    fontColorInput.dispatchEvent(new Event("input"));
    fontColorInput.dispatchEvent(new Event("change"));
}

function setInkColor(color) {
    let colorInput = $("#editorInkColor");
    colorInput.value = color;
    colorInput.dispatchEvent(new Event("input"));
    colorInput.dispatchEvent(new Event("change"));
}

function setInkThickness(thickness) {
    let thicknessInput = $("#editorInkThickness");
    thicknessInput.value = thickness;
    thicknessInput.dispatchEvent(new Event("input"));
    thicknessInput.dispatchEvent(new Event("change"));
}

function setInkOpacity(opacity) {
    let opacityInput = $("#editorInkOpacity");
    opacityInput.value = opacity;
    opacityInput.dispatchEvent(new Event("input"));
    opacityInput.dispatchEvent(new Event("change"));
}

function selectHighlightColor(color) {
    try {
        $(`[data-color="${color.toLowerCase()}"]`).click();
    } catch (e) {
        console.log("Unable to set highlight color! If this affects the behaviour, please raise an issue!");
    }

    $all(".editToolbar .colorPicker").forEach((colorPicker) => {
        if (!colorPicker.parentElement.parentElement.classList.contains("hidden")) {
            if (colorPicker.querySelectorAll(".dropdown").length == 0) {
                colorPicker.click();
                colorPicker.querySelector(`[data-color="${color.toLowerCase()}"]`).click();
                colorPicker.click();
            } else colorPicker.querySelector(`[data-color="${color.toLowerCase()}"]`).click();
        }
    });
}

function requestStampInsert(image) {
    let imageInput = $("#editorStampAddImage");
    imageInput.value = image;
    imageInput.dispatchEvent(new Event("input"));
}

function undo() {
    const undoEvent = new KeyboardEvent("keydown", {
        key: "z",
        code: "KeyZ",
        ctrlKey: true,
        bubbles: true,
        cancelable: true,
    });

    document.dispatchEvent(undoEvent);
}

function redo() {
    const undoEvent = new KeyboardEvent("keydown", {
        key: "y",
        code: "KeyY",
        ctrlKey: true,
        bubbles: true,
        cancelable: true,
    });

    document.dispatchEvent(undoEvent);
}

function $(query) {
    return document.querySelector(query);
}

function $all(query) {
    return document.querySelectorAll(query);
}
