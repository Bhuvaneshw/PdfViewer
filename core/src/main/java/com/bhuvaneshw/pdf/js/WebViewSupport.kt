package com.bhuvaneshw.pdf.js

import android.content.Context
import androidx.webkit.WebViewCompat

object WebViewSupport {

    fun check(context: Context): CheckResult {
        val version = getWebViewVersion(context)

        // https://github.com/mozilla/pdf.js/wiki/frequently-asked-questions#modern-build
        // https://docs.signageos.io/hc/en-us/articles/4405381554578-Browser-WebKit-and-Chromium-versions-by-each-Platform#h_01HABYXXZMDMS644M0BXH43GYD
        return when {
            version == null -> CheckResult.NO_WEBVIEW_FOUND
            version < 110 -> CheckResult.REQUIRES_UPDATE
            version < 115 -> CheckResult.UPDATE_RECOMMENDED
            else -> CheckResult.NO_ACTION_REQUIRED
        }
    }

    private fun getWebViewVersion(context: Context): Int? {
        return try {
            WebViewCompat.getCurrentWebViewPackage(context)
                ?.versionName
                ?.split(".")[0]
                ?.toIntOrNull()
        } catch (_: Exception) {
            null
        }
    }

    enum class CheckResult {
        NO_ACTION_REQUIRED,
        REQUIRES_UPDATE,
        UPDATE_RECOMMENDED,
        NO_WEBVIEW_FOUND
    }
}
