
# PdfViewer

A lightweight **Android PDF viewer library** powered by Mozilla's [PDF.js](https://github.com/mozilla/pdf.js), offering seamless PDF rendering and interactive features. Supports both Jetpack Compose and Xml.

## Setup
For setup see [Setup](README.md#1-setup)
<br>For Jetpack Compose examples see [Jetpack Compose Examples](README_COMPOSE.md)

## Examples

### Simple Pdf Viewer
```xml
<com.acutecoder.pdf.PdfViewer
    android:id="@+id/pdf_viewer"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```
```kotlin
val pdfViewer = findViewById<PdfViewer>(R.id.pdf_viewer)
val source = "file:///android_asset/sample.pdf"

// Kotlin
pdfViewer.onReady {
    load(source)
}
```

```java
// Java
PdfUtil.onReady(pdfViewer, () -> {
    pdfViewer.load(source);
});
```

### Changing container color
```xml
<com.acutecoder.pdf.PdfViewer
    android:id="@+id/pdf_viewer"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:containerBackgroundColor="@android:color/transparent" />
```

### onReady callback
```kotlin
pdfViewer.onReady {
    load(source)
}
```

### With PdfViewerContainer
```xml
<com.acutecoder.pdf.ui.PdfViewerContainer
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/pdf_viewer_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity2">

    <com.acutecoder.pdf.PdfViewer
        android:id="@+id/pdf_viewer"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</com.acutecoder.pdf.ui.PdfViewerContainer>
```

### With ToolBar and ScrollBar
```xml
<com.acutecoder.pdf.ui.PdfViewerContainer xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/pdf_viewer_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity2">

    <!-- id is mandatory, if not random int will be assigned by PdfViewerContainer-->
    <com.acutecoder.pdf.ui.PdfToolBar
        android:id="@+id/pdf_tool_bar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

    <com.acutecoder.pdf.PdfViewer
        android:id="@+id/pdf_viewer"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <com.acutecoder.pdf.ui.PdfScrollBar
        android:id="@+id/pdf_scroll_bar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

</com.acutecoder.pdf.ui.PdfViewerContainer>
```

### ToolBar back icon
```kotlin
val pdfToolBar = findViewById<PdfToolBar>(R.id.pdf_tool_bar)
pdfToolBar.back.setImageResource(com.acutecoder.pdf.ui.R.drawable.outline_arrow_back_24)
```

### Extended ToolBar
See [ExtendedToolBar.kt](/app/src/main/java/com/bhuvaneshw/pdfviewerdemo/ExtendedToolBar.kt)

### Colors
```xml
<com.acutecoder.pdf.ui.PdfViewerContainer xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  xmlns:tools="http://schemas.android.com/tools"
  android:id="@+id/container"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  tools:context=".MainActivity">

<!-- id is mandatory, if not random int will be assigned by PdfViewerContainer-->
  <com.acutecoder.pdf.ui.PdfToolBar
      android:id="@+id/toolbar"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      app:contentColor="@color/md_theme_onBackground" />

  <com.acutecoder.pdf.PdfViewer
      android:id="@+id/pdf_viewer"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:background="@color/md_theme_primaryContainer"
      app:containerBackgroundColor="@android:color/transparent" />

  <com.acutecoder.pdf.ui.PdfScrollBar
      android:id="@+id/pdf_scroll_bar"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      app:contentColor="@color/md_theme_onBackground"
      app:handleColor="@color/md_theme_background" />

</com.acutecoder.pdf.ui.PdfViewerContainer>
```

### Customize ScrollBar
```xml
<com.acutecoder.pdf.ui.PdfScrollBar
    android:id="@+id/pdf_scroll_bar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:useVerticalScrollBarForHorizontalMode="true" />
```
```kotlin
val pdfScrollBar = findViewById<PdfScrollBar>(R.id.pdf_scroll_bar)
pdfScrollBar.interactiveScrolling = false
```

### Loading indicator
```xml
<com.acutecoder.pdf.ui.PdfViewerContainer xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/pdf_viewer_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity2">

    <!-- id is mandatory, if not random int will be assigned by PdfViewerContainer-->
    <com.acutecoder.pdf.ui.PdfToolBar
        android:id="@+id/pdf_tool_bar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

    <com.acutecoder.pdf.PdfViewer
        android:id="@+id/pdf_viewer"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <com.acutecoder.pdf.ui.PdfScrollBar
        android:id="@+id/pdf_scroll_bar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

    <LinearLayout
        android:id="@+id/loading_indicator"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_below="@id/pdf_tool_bar"
        android:background="@android:color/white"
        android:gravity="center"
        android:visibility="gone">

        <ProgressBar
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />

    </LinearLayout>

</com.acutecoder.pdf.ui.PdfViewerContainer>
```

```kotlin
val pdfContainer = findViewById<PdfViewerContainer>(R.id.pdf_viewer_container)
val loadingIndicator = findViewById<LinearLayout>(R.id.loading_indicator)
pdfContainer.setAsLoadingIndicator(loadingIndicator)
```

### Without using PdfViewerContainer
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.acutecoder.pdf.ui.PdfToolBar
        android:id="@+id/pdf_tool_bar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:contentColor="@color/md_theme_onBackground" />

<!--    add below toolbar-->
    <com.acutecoder.pdf.PdfViewer
        android:id="@+id/pdf_viewer"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_below="@id/toolbar"
        android:background="@color/md_theme_primaryContainer"
        app:containerBackgroundColor="@android:color/transparent" />

<!--    set align parent right, don't add below toolbar-->
    <com.acutecoder.pdf.ui.PdfScrollBar
        android:id="@+id/pdf_scroll_bar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentEnd="true"
        app:contentColor="@color/md_theme_onBackground"
        app:handleColor="@color/md_theme_background" />

</RelativeLayout>
```

```kotlin
val pdfViewer = findViewById<PdfViewer>(R.id.pdf_viewer)
val source = "file:///android_asset/sample.pdf"

val pdfScrollBar = findViewById<PdfScrollBar>(R.id.pdf_scroll_bar)
val pdfToolBar = findViewById<PdfToolBar>(R.id.pdf_tool_bar)

pdfToolBar.setupWith(pdfViewer)
pdfScrollBar.setupWith(pdfViewer, pdfToolBar)
pdfViewer.onReady {
  load(source)
  toolbar.setFileName(fileName) // or toolbar.setTitle(title)
}
```

### Listener
You can add listener like
```kotlin
pdfViewer.addListener(PdfOnPageLoadFailed {  // Specific listener (Extension functions)
})
```
or
```kotlin
pdfViewer.addListener(object: PdfListener {
  // implement required methods
})
```
> [!NOTE]
> For Download listener see implementation in [PdfViewerActivity.kt](/app/src/main/java/com/bhuvaneshw/pdfviewerdemo/PdfViewerActivity.kt)

### See Also
1. [Implementations](README.md#3-see-also)
2. [PdfViewer public members](README.md#4-public-members)
