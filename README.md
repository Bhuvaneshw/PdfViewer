
# PdfViewer

A lightweight **Android PDF Viewer Library** powered by Mozilla's [PDF.js](https://github.com/mozilla/pdf.js), offering seamless PDF rendering and interactive features. Supports both **Jetpack Compose** and **XML**.

---
## 📢 Any iOS dev intreseted in CMP? — [check this](https://github.com/Bhuvaneshw/PdfViewer/discussions/47).

## Screenshots
<img src="screenshots/1.png" width="190" alt="ScreenShot1"/> <img src="screenshots/2.png" width="190" alt="ScreenShot2"/>
<img src="screenshots/3.png" width="190" alt="ScreenShot3"/> <img src="screenshots/4.png" width="190" alt="ScreenShot4"/> <img src="screenshots/5.png" width="190" alt="ScreenShot5"/>

## Demo
You can download apk from [here](/app/release/app-release.apk)

## Contents
1. [Setup](#1-setup)<br>
   1.1. [Setup - Kotlin DSL](#11-kotlin-dsl)<br>
   1.2. [Setup - Groovy DSL](#12-groovy-dsl)<br>
2. [Usage](#2-usage)<br>
   2.1. [Jetpack Compose PdfViewer](#21-jetpack-compose-pdfviewer)<br>
   2.2. [XML PdfViewer](#22-xml-pdfviewer)<br>
   2.3. [More Examples](#23-more-examples)<br>
3. [See also](#3-see-also)<br>
4. [Public Members](#4-public-members)<br>
5. [License](#5-license)
6. [External Libraries used](#6-external-libraries-used-for-demo-app)
7. [Contributions](#7-contributions)

## 1. Setup
### 1.1. Kotlin DSL
<details open>
<summary>View Kotlin DSL Setup</summary>

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle.kts or settings.gradle.kts at the end of repositories:
```kotlin
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenCentral()
    maven("https://jitpack.io")
  }
}
```
Step 2. Add the dependency
```kotlin
dependencies {
    implementation("com.github.bhuvaneshw.pdfviewer:$module:$version")
}
```
Replace <b>$module</b> with <b>compose</b>, <b>compose-ui</b>, <b>core</b> or <b>ui</b>
Replace <b>$version</b> with latest version<br/>
Latest version: [![](https://jitpack.io/v/bhuvaneshw/pdfviewer.svg)](https://jitpack.io/#bhuvaneshw/pdfviewer)

<details>
<summary>View Module Usage Options</summary>

#### Compose PDF Viewer (Core only)
Minimal setup for rendering PDFs using Jetpack Compose.
```kotlin
implementation("com.github.bhuvaneshw.pdfviewer:compose:1.0.0")
```
#### Compose PDF Viewer with UI Components
Enhanced Compose viewer setup including PdfViewerContainer, PdfToolBar, and PdfScrollBar.
```kotlin
implementation("com.github.bhuvaneshw.pdfviewer:compose:1.0.0")
implementation("com.github.bhuvaneshw.pdfviewer:compose-ui:1.0.0")
```

#### XML PDF Viewer (Core only)
Use the minimal setup for rendering PDFs.
```kotlin
implementation("com.github.bhuvaneshw.pdfviewer:core:1.0.0")
```

#### XML PDF Viewer with UI Components
Includes PdfViewerContainer, PdfToolBar, and PdfScrollBar for a complete viewing experience.
```kotlin
implementation("com.github.bhuvaneshw.pdfviewer:core:1.0.0")
implementation("com.github.bhuvaneshw.pdfviewer:ui:1.0.0")
```
</details>
</details>
<br/>

> [!NOTE]  
> **If you are upgrading to v1.1.0 see [Migration](docs/MIGRATION.md)**

<br/>

## 1.2. Groovy DSL
<details>
<summary>View Groovy DSL setup</summary>

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle or settings.gradle at the end of repositories:
```groovy
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
      mavenCentral()
      maven { url 'https://jitpack.io' }
  }
}
```
Step 2. Add the dependency
```groovy
dependencies {
    implementation 'com.github.bhuvaneshw.pdfviewer:$module:$version'
}
```
</details>

## 2. Usage
### 2.1 Jetpack Compose PdfViewer

Include compose dependency

```kotlin
val pdfState = rememberPdfState(source = "source")
PdfViewer(
   pdfState = pdfState,
   modifier = Modifier,
   containerColor = Color.Transparent,
   onReady = {
      // Optional work
   }
)
```

source (string) can be
1. Asset Path, like "asset://sample.pdf" or "file:///android_asset/sample.pdf"
2. Android Uri, like uri starting with "content://" from Document Picker
3. Network url, like "https://example.com/sample.pdf"
4. Direct file path like "/data/data/&lt;YourPackageName&gt;/files/sample.pdf" or ~~"file:///sdcard/Downloads/sample.pdf"~~ (Not Recommended for accessing file from External Storage)

### 2.2 XML PdfViewer
Include PdfViewer in your xml
```xml
<com.bhuvaneshw.pdf.PdfViewer
    android:id="@+id/pdf_viewer"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/md_theme_primaryContainer"
    app:containerBackgroundColor="@android:color/transparent" />
```
Then call load function
```kotlin
  // Kotlin
  pdfViewer.onReady {
    load("source")
  }
```
```java
  // Java
  PdfUtil.onReady(pdfViewer, () -> {
    pdfViewer.load("source");
  });
```

> [!WARNING]
> You should not access below members before the PdfViewer is initialized!
> 1. PdfViewer.load()
> 2. PdfViewer.loadFromAsset()
> 3. PdfViewer.loadFromFileUri()
> 4. PdfViewer.loadFromUrl()
> 5. PdfViewer.ui
> 6. PdfViewer.findController
> 7. PdfViewer.pageScrollMode
> 8. PdfViewer.pageSpreadMode
> 9. PdfViewer.cursorToolMode
> 10. PdfViewer.pageRotation
> 11. PdfViewer.doubleClickThreshold
> 12. PdfViewer.longClickThreshold
> 13. PdfViewer.snapPage
> 14. PdfViewer.pageAlignMode
> 15. PdfViewer.singlePageArrangement
> 16. PdfViewer.scrollSpeedLimit
> 17. PdfEditor.undo()
> 18. PdfEditor.redo()
> 19. All setters in PdfEditor

### 2.3 More Examples
1. For Jetpack Compose examples see [Jetpack Compose Examples](docs/README_COMPOSE.md)
2. For XML examples see [XML Examples](docs/README_XML.md)

## 3. See also
> [!NOTE]
> [ComposePdfViewerActivity.kt](/app/src/main/java/com/bhuvaneshw/pdfviewerdemo/ComposePdfViewerActivity.kt)<br>
> [PdfViewerActivity.kt](/app/src/main/java/com/bhuvaneshw/pdfviewerdemo/PdfViewerActivity.kt)<br>
> [ExtendedToolBar.kt](/app/src/main/java/com/bhuvaneshw/pdfviewerdemo/ExtendedToolBar.kt)<br>

## 4. Public Members
<details>
<summary>View Public Members</summary>

`isInitialized: Boolean`
Indicates whether the PDF viewer has been initialized.

`currentUrl: String?`
The current URL of the loaded PDF document.

`currentPage: Int`
The current page number of the PDF document.

`pagesCount: Int`
The total number of pages in the currently loaded PDF document.

`currentPageScale: Float`
The scale factor of the current page (zoom level).

`currentPageScaleValue: String`
The current scale value of the PDF page (e.g., `page-fit`, `auto`).

`properties: PdfDocumentProperties?`
The properties of the currently loaded PDF document, such as title, author, etc.

`ui: UiSettings`
Returns the `UiSettings` for the PDF viewer. Provides settings related to the UI provided by Mozilla's PDF.js.

`findController: FindController`
Returns the `FindController` for the PDF viewer. Provides functionality for finding text in the PDF.

`pageScrollMode: PageScrollMode`
Defines the page scroll mode (e.g., vertical, horizontal, wrapped).

`pageSpreadMode: PageSpreadMode`
Defines the page spread mode (e.g., none, odd, even).

`cursorToolMode: CursorToolMode`
Defines the cursor tool mode (e.g., text select, hand tool).

`load(url: String, originalUrl: String = url)`
Loads a PDF file from the specified `url`. The `originalUrl` parameter is optional and defaults to the `url`.

`onReady(onReady: PdfOnReadyListener)`
Registers a listener that gets called when the PDF viewer is initialized and ready.

`addListener(listener: PdfListener)`
Adds a listener to be notified of PDF events (e.g., page load).

`removeListener(listener: PdfListener)` and `removeListener(listener: PdfOnReadyListener)`
Removes a previously added listener.

`goToPage(pageNumber: Int)`
Navigates to the specified page number in the PDF.

`scrollToRatio(ratio: Float)`
Scrolls the viewer to a specific ratio (0f - 1f) (calculated to offset).

`scrollTo(offset: Int)`
Scrolls the viewer to the specified offset.

`goToNextPage()`
Navigates to the next page in the PDF.

`goToPreviousPage()`
Navigates to the previous page in the PDF.

`goToFirstPage()`
Navigates to the first page in the PDF.

`goToLastPage()`
Navigates to the last page in the PDF.

`scalePageTo(scale: Float)`
Zooms the current page to the specified scale factor.

`zoomIn()`
Zooms in on the current page.

`zoomOut()`
Zooms out on the current page.

`zoomTo(zoom: Zoom)`
Zooms to a specified zoom mode (e.g., `PAGE_FIT`, `PAGE_WIDTH`).

`downloadFile()`
Initiates the download of the currently viewed PDF file.

`printFile()` - unstable
Prints the currently viewed PDF file.

`startPresentationMode()` - unstable
Starts presentation mode, which is typically used for viewing PDFs in full-screen mode.

`rotateClockWise()`
Rotates the PDF clockwise by 90 degrees.

`rotateCounterClockWise()`
Rotates the PDF counter-clockwise by 90 degrees.

`showDocumentProperties()`
Displays the properties of the current PDF document (e.g., title, author).

`reInitialize()`
Re-initializes the PDF viewer, reloading the webview.

`setContainerBackgroundColor(color: Int)`
Sets the background color of the PDF viewer container.

</details>

## 5. License
[Also see PDF.js License](LICENSE_PDF_JS.md)
```
Copyright 2025 Bhuvaneshwaran

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 6. External Libraries used for demo app
1. [MohammedAlaaMorsi/RangeSeekBar](https://github.com/MohammedAlaaMorsi/RangeSeekBar)
2. [jaredrummler/ColorPicker](https://github.com/jaredrummler/ColorPicker)
3. [mhssn95/compose-color-picker](https://github.com/mhssn95/compose-color-picker)

## 7. Contributions

Contributions are welcome! If you have ideas for improvements, bug fixes, or new features, please feel free to:

1.  Fork the repository.
2.  Create a new branch for your changes.
3.  Make your changes and commit them.
4.  Push to the branch.
5.  Open a pull request.
