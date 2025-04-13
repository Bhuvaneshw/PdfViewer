
# PdfViewer
A lightweight **Android PDF viewer library** powered by Mozilla's [PDF.js](https://github.com/mozilla/pdf.js), offering seamless PDF rendering and interactive features. Supports both Jetpack Compose and Xml.

## Migrating to v1.1.0

In version `v1.1.0`, the library packages have been renamed to reflect the new namespace. If you're upgrading from an older version, you'll need to update your import statements accordingly.

### 📦 What Changed?

| Old Package                           | New Package                     |  
|---------------------------------------|---------------------------------|  
| `com.acutecoder.pdf`                  | `com.bhuvaneshw.pdf`            |  
| `com.acutecoder.pdf.ui`               | `com.bhuvaneshw.pdf.ui`         |  
| `com.acutecoder.pdfviewer.compose`    | `com.bhuvaneshw.pdf.compose`    |  
| `com.acutecoder.pdfviewer.compose.ui` | `com.bhuvaneshw.pdf.compose.ui` |  

### 🛠 What You Need to Do

- Open your project
- Search for any imports starting with `com.acutecoder.`
- Replace them with the shiny new `com.bhuvaneshw.` equivalents
- Also update any usage of `pdfviewer` (`com.acutecoder.pdfviewer`) in package paths or module references to just `pdf` (`com.bhuvaneshw.pdf`)
- That’s it—you’re good to go! 🚀

### 🔁 Migration Note for `PdfViewer.load("file://")`

The generic `PdfViewer.load(...)` method is **still available** and can be used. However:

- ✅ `file:///android_asset/...` is still supported and will now be internally transformed to `"asset://..."`, so it continues to work.
- ❌ Other `file://` paths are **no longer supported** when passed to `load(...)` directly.

### 🛠 What You Need to Do

- Replace all `file:///android_asset/` with `asset://` for consistency
- Use alternative methods for direct file access

### ✅ New explicit methods available:

```kotlin
// Load from a URL
PdfViewer.loadFromUrl("https://example.com/sample.pdf")

// Load from an Android system URI (e.g. from a document picker)
PdfViewer.loadFromFileUri(uri)

// Load from the assets folder
PdfViewer.loadFromAsset("some_folder/sample.pdf")
```

### 💬 Why the Change?

Just making things a bit more personal and better aligned with ongoing development. New namespace, cleaner name, more predictable API, same smooth PDF experience. 😎

**If you hit any snags, feel free to open an issue!**
