package com.bhuvaneshw.pdf.resource

import java.io.InputStream

data class NetworkResource(
    val mimeType: String,
    val encoding: String,
    val inputStream: InputStream,
)
