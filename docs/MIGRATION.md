

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

### 💬 Why the Change?

Just making things a bit more personal and better aligned with ongoing development. New namespace, cleaner name, same smooth PDF experience. 😎

**If you hit any snags, feel free to open an issue!**
