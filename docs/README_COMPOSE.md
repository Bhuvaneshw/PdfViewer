
# PdfViewer

A lightweight **Android PDF viewer library** powered by Mozilla's [PDF.js](https://github.com/mozilla/pdf.js), offering seamless PDF rendering and interactive features. Supports both Jetpack Compose and Xml.

For setup see [Setup](../README.md#1-setup)
<br>For XML examples see [XML Examples](README_XML.md)

## Compose Examples

### Simple Pdf Viewer
```kotlin
@Composable
fun SimplePdfViewer() {
    val pdfState = rememberPdfState("file:///android_asset/sample.pdf")

    PdfViewer(
        pdfState = pdfState,
        modifier = Modifier
    )
}
```

### Changing container color
```kotlin
@Composable
fun PdfViewerContainerColor() {
    val pdfState = rememberPdfState("file:///android_asset/sample.pdf")

    PdfViewer(
        pdfState = pdfState,
        modifier = Modifier,
        containerColor = Color.Transparent
    )
}
```

### Default onReady callback
```kotlin
@Composable
fun PdfViewerDefaultOnReadyCallback() {
    val pdfState = rememberPdfState("file:///android_asset/sample.pdf")

    PdfViewer(
        pdfState = pdfState,
        modifier = Modifier,
        onReady = DefaultOnReadyCallback { // this: PdfViewer
            // loadSource is called before this callback
        }
    )
}
```

### Custom onReady callback
```kotlin
@Composable
fun PdfViewerCustomOnReadyCallback() {
    val pdfState = rememberPdfState("file:///android_asset/sample.pdf")

    PdfViewer(
        pdfState = pdfState,
        modifier = Modifier,
        onReady = CustomOnReadyCallback { loadSource ->  // this: PdfViewer
            loadSource()           // Mandatory to call
        }
    )
}
```

### With PdfViewerContainer
```kotlin
@Composable
fun PdfViewerWithContainer() {
    val pdfState = rememberPdfState("file:///android_asset/sample.pdf")

    PdfViewerContainer(
        pdfState = pdfState,
        pdfViewer = {// this: PdfContainerBoxScope
            PdfViewer(             // pdfState is passed by PdfContainerBoxScope
                modifier = Modifier,
            )
        }
    )
}
```

### With ToolBar and ScrollBar
```kotlin
@Composable
fun PdfViewerWithToolBarAndScrollBar() {
    val pdfState = rememberPdfState("file:///android_asset/sample.pdf")

    PdfViewerContainer(
        pdfState = pdfState,
        pdfViewer = {
            PdfViewer(
                modifier = Modifier
            )
        },
        pdfToolBar = {
            PdfToolBar(
                title = "Title",
            )
        },
        pdfScrollBar = { parentSize ->
            PdfScrollBar(
                parentSize = parentSize
            )
        },
    )
}
```

### ToolBar back icon
```kotlin
@Composable
fun ToolBarBack() {
    val pdfState = rememberPdfState("file:///android_asset/sample.pdf")

    PdfViewerContainer(
        pdfState = pdfState,
        pdfViewer = {
            PdfViewer(
                modifier = Modifier
            )
        },
        pdfToolBar = {
            PdfToolBar(
                title = "Title",
                onBack = {
                    // called when back is clicked
                },
//                backIcon = { // this: PdfToolBarScope
//                    // or create your own back icon
//                }
            )
        },
        pdfScrollBar = { parentSize ->
            PdfScrollBar(
                parentSize = parentSize
            )
        },
    )
}
```

### Listening for findBar state
```kotlin
@Composable
fun ListeningForFindBarState() {
    val pdfState = rememberPdfState("file:///android_asset/sample.pdf")
    val toolBarState = rememberToolBarState()

    LaunchedEffect(toolBarState.isFindBarOpen) {
        // Do something
    }
    BackHandler {
        if (toolBarState.isFindBarOpen)
            toolBarState.isFindBarOpen = false
    }

    PdfViewerContainer(
        pdfState = pdfState,
        pdfViewer = {
            PdfViewer(
                modifier = Modifier
            )
        },
        pdfToolBar = {
            PdfToolBar(
                title = "Title",
                toolBarState = toolBarState,
            )
        },
        pdfScrollBar = { parentSize ->
            PdfScrollBar(
                parentSize = parentSize
            )
        },
    )
}
```

### Extended ToolBar
```kotlin
@Composable
fun ExtendedToolBar() {
    val pdfState = rememberPdfState("file:///android_asset/sample.pdf")

    PdfViewerContainer(
        pdfState = pdfState,
        pdfViewer = {
            PdfViewer(
                modifier = Modifier
            )
        },
        pdfToolBar = {
            PdfToolBar(
                title = "Title",
                dropDownMenu = { onDismiss, defaultMenus ->
                    ExtendedTooBarMenus(
                        onDismiss = onDismiss,
                        defaultMenus = defaultMenus
                    )
                }
            )
        },
        pdfScrollBar = { parentSize ->
            PdfScrollBar(
                parentSize = parentSize
            )
        },
    )
}

@Composable
private fun ExtendedTooBarMenus(
    onDismiss: () -> Unit,
    defaultMenus: @Composable (filter: (PdfToolBarMenuItem) -> Boolean) -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = "Extended Menu") },
        onClick = {
            // Do something
            onDismiss()
        }
    )
    defaultMenus { menuItem -> // Default menus
        true // or filter like, menuItem != PdfToolBarMenuItem.CUSTOM_PAGE_ARRANGEMENT
    }
}
```

### Colors
```kotlin
@Composable
fun Colors() {
    val pdfState = rememberPdfState("file:///android_asset/sample.pdf")

    PdfViewerContainer(
        pdfState = pdfState,
        pdfViewer = {
            PdfViewer(
                modifier = Modifier,
                containerColor = Color.Red
            )
        },
        pdfToolBar = {
            PdfToolBar(
                title = "Title",
                contentColor = Color.Blue,
            )
        },
        pdfScrollBar = { parentSize ->
            PdfScrollBar(
                parentSize = parentSize,
                contentColor = Color.Blue,
                handleColor = Color.Green
            )
        },
    )
}
```

### Customize ScrollBar
```kotlin
@Composable
fun CustomizeScrollBar() {
    val pdfState = rememberPdfState("file:///android_asset/sample.pdf")

    PdfViewerContainer(
        pdfState = pdfState,
        pdfViewer = {
            PdfViewer(
                modifier = Modifier,
            )
        },
        pdfToolBar = {
            PdfToolBar(
                title = "Title",
            )
        },
        pdfScrollBar = { parentSize ->
            PdfScrollBar(
                parentSize = parentSize,
                useVerticalScrollBarForHorizontalMode = true,
                interactiveScrolling = true,
            )
        },
    )
}
```

### Listener and onCreateViewer callback
```kotlin
@Composable
fun PdfViewerOnCreateViewerCallback() {
    val pdfState = rememberPdfState("file:///android_asset/sample.pdf")

    PdfViewer(
        pdfState = pdfState,
        modifier = Modifier,
        onCreateViewer = { // this: PdfViewer
            addListener(PdfOnPageLoadFailed {  // Specific listener (Extension functions)
            })
            // or
            addListener(object: PdfListener {
                // implement required methods
            })
        }
    )
}
```
> [!NOTE]
> For Download listener see implementation in [PdfViewerActivity.kt](/app/src/main/java/com/bhuvaneshw/pdfviewerdemo/PdfViewerActivity.kt)

### States that PdfState provides
1. source: String 
2. pdfViewer: PdfViewer? 
3. loadingState: PdfLoadingState 
4. pagesCount: Int 
5. currentPage: Int 
6. currentScale: Float 
7. properties: PdfDocumentProperties? 
8. passwordRequired: Boolean 
9. scrollState: ScrollState 
10. matchState: MatchState 
11. scrollMode: PdfViewer.PageScrollMode 
12. spreadMode: PdfViewer.PageSpreadMode 
13. rotation: PdfViewer.PageRotation 
14. scaleLimit: ScaleLimit 
15. actualScaleLimit: ActualScaleLimit 
16. snapPage: Boolean 
17. singlePageArrangement: Boolean 
18. alignMode: PdfViewer.PageAlignMode 
19. scrollSpeedLimit

### See Also
1. [Implementations](../README.md#3-see-also)
2. [PdfViewer public members](../README.md#4-public-members)
