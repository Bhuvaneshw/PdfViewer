package com.bhuvaneshw.pdf.resource

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import java.net.HttpURLConnection
import java.net.URL

internal class NetworkResourceLoader(
    onError: (Exception) -> Unit,
) : ResourceLoader {

    companion object {
        const val PATH = "/network/"
    }

    private val assetLoader = WebViewAssetLoader.Builder()
        .setDomain(ResourceLoader.RESOURCE_DOMAIN)
        .addPathHandler(
            PATH,
            NetworkUriPathHandler(onError)
        )
        .build()

    override fun canHandle(uri: Uri) =
        uri.host == ResourceLoader.RESOURCE_DOMAIN && uri.path?.startsWith(PATH) == true

    override fun shouldInterceptRequest(uri: Uri): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(uri)
    }

    override fun createSharableUri(context: Context, authority: String, source: String): Uri? = null

}

private class NetworkUriPathHandler(
    private val onError: (Exception) -> Unit,
) : WebViewAssetLoader.PathHandler {

    @SuppressLint("UseKtx")
    override fun handle(path: String): WebResourceResponse? {
        return try {
            val uri = Uri.decode(path)
            val url = URL(uri)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            val mimeType = connection.contentType ?: "application/octet-stream"
            val inputStream = connection.inputStream

            WebResourceResponse(mimeType, "UTF-8", inputStream)
        } catch (e: Exception) {
            onError(e)
            null
        }
    }

}
