package com.bhuvaneshw.pdf

open class PdfException(message: String) : RuntimeException(message)

class AbortException(message: String) : PdfException(message)
class InvalidPDFException(message: String) : PdfException(message)
class RenderingCancelledException(message: String) : PdfException(message)
class ResponseException(message: String) : PdfException(message)

class PdfViewerNotInitializedException : PdfException("Pdf Viewer not yet initialized!")

@Suppress("NOTHING_TO_INLINE")
internal inline fun exceptionFrom(message: String, type: String): PdfException {
    return when (type) {
        "AbortException" -> AbortException(message)
        "InvalidPDFException" -> InvalidPDFException(message)
        "RenderingCancelledException" -> RenderingCancelledException(message)
        "ResponseException" -> ResponseException(message)
        else -> PdfException(message)
    }
}
