package com.bhuvaneshw.pdfviewerdemo

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bhuvaneshw.pdf.compose.PdfLoadingState

fun Activity.getDataFromIntent(): Pair<String, String>? {
    val filePath: String
    val fileName: String

    // View from other apps (from intent filter)
    if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
        filePath = intent.data.toString()
        fileName = intent.data!!.getFileName(this)
    } else {
        // Path from asset, url or android uri
        filePath = intent.extras?.getString("filePath")
            ?: intent.extras?.getString("fileUrl")
                    ?: intent.extras?.getString("fileUri")
                    ?: return null

        fileName = intent.extras?.getString("fileUri")?.toUri()?.getFileName(this)
            ?: intent.extras?.getString("fileName") ?: ""
    }

    return filePath to fileName
}

fun Uri.getFileName(context: Context): String {
    var name = "UNKNOWN"
    val cursor: Cursor? = context.contentResolver.query(
        this,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }
    }

    return name
}

fun Activity.setFullscreen(fullscreen: Boolean) {
    val window = window ?: return
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)

    insetsController.apply {
        systemBarsBehavior = if (fullscreen) {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            show(WindowInsetsCompat.Type.statusBars())
            show(WindowInsetsCompat.Type.navigationBars())
            WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }

}

fun <T> Activity.setComponentEnabled(componentClass: Class<T>, enable: Boolean) {
    packageManager.setComponentEnabledSetting(
        ComponentName(this, componentClass),
        if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    )
}

fun Exception.formatToString(): String {
    return "${this::class.java.simpleName}: $message"
}

fun PdfLoadingState.Error.formatToString(): String {
    return "${exception::class.java.simpleName}: $message"
}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
