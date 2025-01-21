package com.bhuvaneshw.pdf

data class PdfDocumentProperties(
    val title: String,
    val subject: String,
    val author: String,
    val creator: String,
    val producer: String,
    val creationDate: String,
    val modifiedDate: String,
    val keywords: String,
    val language: String,
    val pdfFormatVersion: String,
    val fileSize: Long,
    val isLinearized: Boolean,
    val encryptFilterName: String,
    val isAcroFormPresent: Boolean,
    val isCollectionPresent: Boolean,
    val isSignaturesPresent: Boolean,
    val isXFAPresent: Boolean,
    val customJson: String,
)