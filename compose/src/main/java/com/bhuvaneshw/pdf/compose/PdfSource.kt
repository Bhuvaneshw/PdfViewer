package com.bhuvaneshw.pdf.compose

import android.net.Uri
import androidx.core.net.toUri

sealed class PdfSource {

    data class Asset(val assetPath: String) : PdfSource()

    data class ContentUri(val contentUri: Uri) : PdfSource() {
        constructor(contentUri: String) : this(contentUri.toUri())
    }

    data class File(val file: java.io.File) : PdfSource() {
        constructor(filePath: String) : this(java.io.File(filePath))
    }

    data class Plain(val source: String) : PdfSource() {
        constructor(source: Uri) : this(source.toString())
    }

    data class Url(val url: String) : PdfSource() {
        constructor(url: Uri) : this(url.toString())
    }
}
