try {
    // Checking for Android JWI
    JWI;
} catch (e) {
    // No Android JWI is provided.
    console.log("Using dummy interface");

    window.JWI = {
        getHighlightEditorColorsString() {
            return "yellow=#ffff98,green=#53FFBC,blue=#80EBFF,pink=#FFCBE6,red=#FF4F5F";
        },
        onLoadFailed() {},
        onLoadSuccess() {},
        onPasswordDialogChange() {},
        onLinkClick() {},
        onSingleClick() {},
        onDoubleClick() {},
        onLongClick() {},
        onScaleChange() {},
        onPageChange() {},
        onSpreadModeChange() {},
        onScrollModeChange() {},
        onScroll() {},
        onFindMatchChange() {},
        onFindMatchStart() {},
        onFindMatchComplete() {},
        onLoadProperties() {},
        createWebPrintJob() {},
    };
}
