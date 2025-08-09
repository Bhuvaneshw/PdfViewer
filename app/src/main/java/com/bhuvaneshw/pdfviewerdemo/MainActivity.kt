package com.bhuvaneshw.pdfviewerdemo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bhuvaneshw.pdf.js.WebViewSupport
import com.bhuvaneshw.pdfviewerdemo.databinding.ActivityMainBinding
import com.bhuvaneshw.pdfviewerdemo.databinding.UrlDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var view: ActivityMainBinding
    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        pref = getSharedPreferences("pref", MODE_PRIVATE)

        view = ActivityMainBinding.inflate(layoutInflater)
        setContentView(view.root)

        ViewCompat.setOnApplyWindowInsetsListener(view.container) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        view.useCompose.run {
            isChecked = pref.getBoolean("use_compose", false)
            setOnCheckedChangeListener { _, isChecked ->
                pref.edit { putBoolean("use_compose", isChecked) }
                updateComponents(useCompose = isChecked)
            }
        }

        view.enableBothViewers.run {
            isChecked = pref.getBoolean("enable_both_viewers", false)
            setOnCheckedChangeListener { _, isChecked ->
                pref.edit { putBoolean("enable_both_viewers", isChecked) }
                updateComponents(useCompose = view.useCompose.isChecked)
            }
        }

        view.fromAsset.setOnClickListener {
            startActivity(
                Intent(this, getViewerActivityClass()).apply {
                    putExtra("fileName", "sample.pdf")
                    putExtra("filePath", "asset://sample.pdf")
                }
            )
        }

        val openLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            result.data?.data?.let { uri ->
                startActivity(
                    Intent(this, getViewerActivityClass()).apply {
                        putExtra("fileUri", uri.toString())
                    }
                )
            }
        }
        view.fromStorage.setOnClickListener {
            openLauncher.launch(
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/pdf"
                }
            )
        }

        view.fromUrl.setOnClickListener {
            promptUrl { url ->
                startActivity(
                    Intent(this, getViewerActivityClass()).apply {
                        putExtra("fileName", guessFileNameFromUrl(url))
                        putExtra("fileUrl", url)
                    }
                )
            }
        }

        view.link.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, view.link.text.toString().toUri()))
        }
        view.librariesUsed.setOnClickListener {
            startActivity(Intent(this, UsedLibrariesActivity::class.java))
        }

        checkWebView()
    }

    private fun guessFileNameFromUrl(url: String): String? = URLUtil.guessFileName(
        url,
        null,
        MimeTypeMap
            .getSingleton()
            .getMimeTypeFromExtension(url.substringAfterLast("."))
    )

    private fun updateComponents(useCompose: Boolean) {
        setComponentEnabled(
            ComposePdfViewerActivity::class.java,
            useCompose || view.enableBothViewers.isChecked
        )
        setComponentEnabled(
            PdfViewerActivity::class.java,
            !useCompose || view.enableBothViewers.isChecked
        )
    }

    private fun getViewerActivityClass(): Class<*> {
        val useCompose = pref.getBoolean("use_compose", false)

        return if (useCompose) ComposePdfViewerActivity::class.java
        else PdfViewerActivity::class.java
    }

    private fun promptUrl(callback: (String) -> Unit) {
        val view = UrlDialogBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(this)
            .setTitle("Enter Pdf Url")
            .setView(view.root)
            .setPositiveButton("Load") { dialog, _ ->
                dialog.dismiss()
                val url = view.field.text.toString()
                if (URLUtil.isValidUrl(url)) callback(url)
                else toast("Enter valid url!")
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            startActivity(
                Intent(this, getViewerActivityClass()).apply {
                    putExtra("fileUri", intent.data.toString())
                }
            )
        }
    }

    private fun checkWebView() {
        val checkResult = WebViewSupport.check(this)

        when (checkResult) {
            WebViewSupport.CheckResult.REQUIRES_UPDATE -> showStrictUpdateDialog()
            WebViewSupport.CheckResult.UPDATE_RECOMMENDED -> showRecommendedUpdateDialog()
            WebViewSupport.CheckResult.NO_WEBVIEW_FOUND -> showInstallDialog()
            WebViewSupport.CheckResult.NO_ACTION_REQUIRED -> {}
        }
    }

    private fun showRecommendedUpdateDialog() {
        showDialog("Consider updating WebView/Chrome to v115", true)
    }

    private fun showStrictUpdateDialog() {
        showDialog("Please update WebView/Chrome to minimum v110! Recommended v115", false)
    }

    private fun showInstallDialog() {
        showDialog("Please install WebView/Chrome!", false)
    }

    private fun showDialog(message: String, cancellable: Boolean) {
        AlertDialog.Builder(this)
            .setTitle("WebView Support")
            .setMessage(message)
            .setPositiveButton(if (cancellable) "Later" else "Exit") { _, _ ->
                if (!cancellable) finishAffinity()
            }
            .create()
            .run {
                setCancelable(cancellable)
                show()
            }
    }
}
