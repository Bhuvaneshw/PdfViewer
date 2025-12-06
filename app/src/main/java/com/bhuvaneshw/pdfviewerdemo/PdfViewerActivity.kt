package com.bhuvaneshw.pdfviewerdemo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bhuvaneshw.pdf.PdfEditor
import com.bhuvaneshw.pdf.PdfListener
import com.bhuvaneshw.pdf.PdfUnstableApi
import com.bhuvaneshw.pdf.addListener
import com.bhuvaneshw.pdf.callIfScrollSpeedLimitIsEnabled
import com.bhuvaneshw.pdf.callSafely
import com.bhuvaneshw.pdf.print.DefaultPdfPrintAdapter
import com.bhuvaneshw.pdf.setting.PdfSettingsManager
import com.bhuvaneshw.pdf.sharedPdfSettingsManager
import com.bhuvaneshw.pdfviewerdemo.databinding.ActivityPdfViewerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var view: ActivityPdfViewerBinding
    private var fullscreen = false
    private lateinit var pdfSettingsManager: PdfSettingsManager
    private var selectedColor = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        view = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(view.root)

        ViewCompat.setOnApplyWindowInsetsListener(view.container.mainView) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(view.pdfOutlineView) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pdfSettingsManager = sharedPdfSettingsManager("PdfSettings", MODE_PRIVATE)
            .also { it.includeAll() }

        val (filePath, fileName) = getDataFromIntent() ?: run {
            toast("No source available!")
            finish()
            return
        }

        view.pdfViewer.onReady {
//            minPageScale = PdfViewer.Zoom.PAGE_WIDTH.floatValue
//            maxPageScale = 5f
//            defaultPageScale = PdfViewer.Zoom.PAGE_WIDTH.floatValue
//            editor.highlightColor = Color.BLUE
            editor.applyHighlightColorOnTextSelection = true
            pdfSettingsManager.restore(this)
            load(filePath)
            if (filePath.isNotBlank())
                view.pdfToolBar.setFileName(fileName)

            ariaLabel = fileName.split(".")[0]
            ariaRoleDescription = "Pdf"
        }

        view.pdfToolBar.alertDialogBuilder = { MaterialAlertDialogBuilder(this) }
        view.pdfToolBar.onBack = onBackPressedDispatcher::onBackPressed
        view.pdfToolBar.pickColor = { onPickColor ->
            ColorPickerDialog.newBuilder()
                .setColor(selectedColor)
                .create().apply {
                    setColorPickerDialogListener(object : ColorPickerDialogListener {
                        override fun onColorSelected(dialogId: Int, color: Int) {
                            selectedColor = color
                            onPickColor(color)
                        }

                        override fun onDialogDismissed(dialogId: Int) {}
                    })
                    show(supportFragmentManager, "color-picker-dialog")
                }
        }
        view.container.alertDialogBuilder = view.pdfToolBar.alertDialogBuilder
        view.pdfViewer.pdfPrintAdapter = DefaultPdfPrintAdapter(this).apply {
            defaultFileName = fileName
        }
        view.pdfViewer.addListener(DownloadPdfListener(fileName))
        view.pdfViewer.addListener(ImagePickerListener(this))
        view.container.setAsLoadingIndicator(view.loader)

        onBackPressedDispatcher.addCallback(this) {
            when {
                view.pdfToolBar.handleBackPressed() -> {
                    // Handled by toolbar
                }

                view.container.handleBackPressed() -> {
                    // Handled by container
                }

                view.pdfViewer.editor.hasUnsavedChanges -> showSaveDialog()
                else -> {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        view.pdfViewer.run {
//            highlightEditorColors = listOf("blue" to Color.BLUE, "black" to Color.BLACK)
            addListener(
                onPageLoadFailed = {
                    toast(it.formatToString())
                    finish()
                },
                onLinkClick = { link ->
                    startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
                },
                onProgressChange = { progress ->
                    view.progressBar.isIndeterminate = false
                    view.progressBar.progress = (progress * 100).toInt()
                },
                onAnnotationEditor = { type ->
                    when (type) {
                        is PdfEditor.AnnotationEventType.Saved -> {
                            view.pdfToolBar.setFileName(fileName)
                        }

                        is PdfEditor.AnnotationEventType.Unsaved -> {
                            view.pdfToolBar.setFileName("*$fileName")
                        }

                        else -> {}
                    }
                }
            )
        }

        view.pdfViewer.addListener(object : PdfListener {
            @OptIn(PdfUnstableApi::class)
            override fun onSingleClick() {
                view.pdfViewer.callSafely { // Helpful if you are using scrollSpeedLimit or skip if editing pdf
                    if (!view.pdfToolBar.isEditorBarVisible()) {
                        fullscreen = !fullscreen
                        setFullscreen(fullscreen)
                        view.container.animateToolBar(!fullscreen)
                    }
                }
            }

            @OptIn(PdfUnstableApi::class)
            override fun onDoubleClick() {
                view.pdfViewer.run {
                    callSafely { // Helpful if you are using scrollSpeedLimit or skip if editing pdf
                        if (!view.pdfToolBar.isEditorBarVisible()) {
                            val originalCurrentPage = currentPage

                            if (!isZoomInMinScale()) zoomToMinimum()
                            else zoomToMaximum()

                            callIfScrollSpeedLimitIsEnabled {
                                goToPage(originalCurrentPage)
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onPause() {
        pdfSettingsManager.save(view.pdfViewer)
        super.onPause()
    }

    override fun onDestroy() {
        pdfSettingsManager.save(view.pdfViewer)
        super.onDestroy()
    }

    private fun showSaveDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Document modified!")
            .setMessage("Do you want to save the document before exiting?")
            .setPositiveButton("Save") { _, _ ->
                view.pdfViewer.downloadFile()
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .show()
    }

    inner class DownloadPdfListener(private val pdfTitle: String) : PdfListener {
        private var bytes: ByteArray? = null
        private val saveFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                bytes?.let { pdfAsBytes ->
                    if (result.resultCode == RESULT_OK) {
                        result.data?.data?.let { uri ->
                            try {
                                contentResolver.openOutputStream(uri)?.use { it.write(pdfAsBytes) }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }

        override fun onDownload(
            fileBytes: ByteArray,
            fileName: String?,
            mimeType: String?
        ) {
            bytes = fileBytes

            saveFileLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mimeType ?: "application/pdf"
                putExtra(
                    Intent.EXTRA_TITLE, if (mimeType == "application/pdf") pdfTitle else fileName
                )
            })
        }
    }

}
