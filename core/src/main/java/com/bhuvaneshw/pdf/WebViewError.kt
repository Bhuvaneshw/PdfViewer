package com.bhuvaneshw.pdf

data class WebViewError(
    val errorCode: Int?,
    val description: String?,
    val failingUrl: String?,
    val isForMainFrame: Boolean? = null,
)
