package com.bhuvaneshw.pdf

import android.content.Context
import com.bhuvaneshw.pdf.setting.PdfSettingsManager
import com.bhuvaneshw.pdf.setting.SharedPreferencePdfSettingsSaver

fun Context.sharedPdfSettingsManager(name: String, mode: Int = Context.MODE_PRIVATE) =
    PdfSettingsManager(SharedPreferencePdfSettingsSaver(this, name, mode))

@PdfUnstableApi
fun PdfViewer.callSafely(
    checkScrollSpeedLimit: Boolean = true,
    checkEditing: Boolean = true,
    block: ScrollSpeedLimitScope.() -> Unit
) {
    if (checkEditing && editor.isEditing) return

    val scope = ScrollSpeedLimitScope()

    if (checkScrollSpeedLimit && scrollSpeedLimit != PdfViewer.ScrollSpeedLimit.None) {
        val originalScrollSpeedLimit = scrollSpeedLimit
        scrollSpeedLimit = PdfViewer.ScrollSpeedLimit.None
        block.invoke(scope)
        scope.onEnabled?.invoke()
        scrollSpeedLimit = originalScrollSpeedLimit
    } else block.invoke(scope)
}

fun ScrollSpeedLimitScope.callIfScrollSpeedLimitIsEnabled(onEnabled: () -> Unit) {
    this.onEnabled = onEnabled
}

class ScrollSpeedLimitScope internal constructor(internal var onEnabled: (() -> Unit)? = null)
