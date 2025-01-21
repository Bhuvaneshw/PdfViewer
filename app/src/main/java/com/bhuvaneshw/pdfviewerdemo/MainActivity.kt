package com.bhuvaneshw.pdfviewerdemo

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.webkit.URLUtil
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
                pref.edit().putBoolean("use_compose", isChecked).apply()
            }
        }

        view.fromAsset.setOnClickListener {
            startActivity(
                Intent(this, getViewerActivityClass()).apply {
                    putExtra("fileName", "sample.pdf")
                    putExtra("fileSize", 271804L)
                    putExtra("filePath", "file:///android_asset/sample.pdf")

//                    Direct file access is not allowed in latest Android versions unless you have Manifest.permission.MANAGE_EXTERNAL_STORAGE permission
//                    putExtra("filePath", "file://Download/sample.pdf")
                }
            )
        }

        val openLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            result?.data?.data?.let { uri ->
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
                        putExtra("fileUrl", url)
                    }
                )
            }
        }

        view.link.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(view.link.text.toString())))
        }
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
}
