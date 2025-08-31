package com.bhuvaneshw.pdf.resource

import java.net.HttpURLConnection
import java.net.URL

interface NetworkResourceHandler {
    fun open(url: String): NetworkResource
}

class DefaultNetworkResourceHandler(
    private val beforeConnect: (HttpURLConnection.() -> Unit)? = null,
) : NetworkResourceHandler {

    override fun open(url: String): NetworkResource {
        val url = URL(url)
        val connection = url.openConnection() as HttpURLConnection

        beforeConnect?.invoke(connection)
        connection.connect()

        return NetworkResource(
            mimeType = connection.contentType ?: "application/octet-stream",
            encoding = connection.contentEncoding ?: "UTF-8",
            inputStream = connection.inputStream,
        )
    }

}
