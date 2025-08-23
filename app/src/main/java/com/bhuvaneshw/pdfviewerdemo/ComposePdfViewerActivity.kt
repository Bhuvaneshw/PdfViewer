package com.bhuvaneshw.pdfviewerdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bhuvaneshw.pdf.PdfListener
import com.bhuvaneshw.pdf.PdfUnstableApi
import com.bhuvaneshw.pdf.PdfUnstablePrintApi
import com.bhuvaneshw.pdf.PdfViewer
import com.bhuvaneshw.pdf.addListener
import com.bhuvaneshw.pdf.callIfScrollSpeedLimitIsEnabled
import com.bhuvaneshw.pdf.callSafely
import com.bhuvaneshw.pdf.compose.CustomOnReadyCallback
import com.bhuvaneshw.pdf.compose.DefaultOnReadyCallback
import com.bhuvaneshw.pdf.compose.PdfLoadingState
import com.bhuvaneshw.pdf.compose.PdfState
import com.bhuvaneshw.pdf.compose.rememberPdfState
import com.bhuvaneshw.pdf.compose.ui.PdfScrollBar
import com.bhuvaneshw.pdf.compose.ui.PdfToolBar
import com.bhuvaneshw.pdf.compose.ui.PdfToolBarMenuItem
import com.bhuvaneshw.pdf.compose.ui.PdfViewer
import com.bhuvaneshw.pdf.compose.ui.PdfViewerContainer
import com.bhuvaneshw.pdf.compose.ui.rememberToolBarState
import com.bhuvaneshw.pdf.print.DefaultPdfPrintAdapter
import com.bhuvaneshw.pdf.setting.PdfSettingsManager
import com.bhuvaneshw.pdf.sharedPdfSettingsManager
import com.bhuvaneshw.pdfviewerdemo.ui.theme.PdfViewerComposeDemoTheme
import io.mhssn.colorpicker.ColorPickerDialog
import io.mhssn.colorpicker.ColorPickerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ComposePdfViewerActivity : ComponentActivity() {
    private lateinit var pdfSettingsManager: PdfSettingsManager
    private lateinit var downloadPdfListener: DownloadPdfListener
    private lateinit var imagePickerListener: ImagePickerListener
    private var pdfViewer: PdfViewer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        pdfSettingsManager = sharedPdfSettingsManager("PdfSettings", MODE_PRIVATE)
            .also { it.includeAll() }

        val (filePath, fileName) = getDataFromIntent() ?: run {
            toast("No source available!")
            finish()
            return
        }

        downloadPdfListener = DownloadPdfListener(fileName)
        imagePickerListener = ImagePickerListener(this)

        setContent {
            PdfViewerComposeDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MainScreen(
                            title = fileName,
                            url = filePath,
                            pdfSettingsManager = pdfSettingsManager,
                            setPdfViewer = { pdfViewer = it },
                            downloadPdfListener = downloadPdfListener,
                            imagePickerListener = imagePickerListener,
                        )
                    }
                }
            }
        }
    }

    override fun onPause() {
        pdfViewer?.let { pdfSettingsManager.save(it) }
        super.onPause()
    }

    override fun onDestroy() {
        pdfViewer?.let { pdfSettingsManager.save(it) }
        super.onDestroy()
    }

    @Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
    @Composable
    private fun MainScreenPreview() {
        MainScreen(
            title = "Preview",
            url = "",
            pdfSettingsManager = null,
            setPdfViewer = {},
            downloadPdfListener = downloadPdfListener,
            imagePickerListener = imagePickerListener,
        )
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

        override fun onSavePdf(pdfAsBytes: ByteArray) {
            bytes = pdfAsBytes

            saveFileLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
                putExtra(Intent.EXTRA_TITLE, pdfTitle)
            })
        }
    }

}

@OptIn(ExperimentalComposeUiApi::class, PdfUnstablePrintApi::class)
@Composable
private fun Activity.MainScreen(
    title: String,
    url: String,
    pdfSettingsManager: PdfSettingsManager?,
    setPdfViewer: (PdfViewer?) -> Unit,
    downloadPdfListener: ComposePdfViewerActivity.DownloadPdfListener,
    imagePickerListener: ImagePickerListener,
) {
    val pdfState = rememberPdfState(source = url)
    val toolBarState = rememberToolBarState()
    var fullscreen by remember { mutableStateOf(false) }

    LaunchedEffect(pdfState.loadingState) {
        pdfState.loadingState.let {
            if (it is PdfLoadingState.Error) {
                toast(it.errorMessage)
                finish()
            }
        }
    }

    LaunchedEffect(fullscreen) {
        setFullscreen(fullscreen)
    }

    DisposableEffect(Unit) {
        onDispose {
            pdfState.pdfViewer?.let { pdfSettingsManager?.save(it) }
            setPdfViewer(null)
        }
    }

    BackHandler {
        if (toolBarState.isFindBarOpen)
            toolBarState.isFindBarOpen = false
        else finish()
    }

    PdfViewerContainer(
        pdfState = pdfState,
        pdfViewer = {
            PdfViewer(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                onReady = DefaultOnReadyCallback {
                    pdfSettingsManager?.restore(this)
                    setPdfViewer(this)
                    pdfPrintAdapter = DefaultPdfPrintAdapter(context)

                    addListener(downloadPdfListener)
                    addListener(imagePickerListener)
                    addListener(object : PdfListener {
                        @OptIn(PdfUnstableApi::class)
                        override fun onSingleClick() {
                            callSafely {  // Helpful if you are using scrollSpeedLimit or skip if editing pdf
                                fullscreen = !fullscreen
                            }
                        }

                        @OptIn(PdfUnstableApi::class)
                        override fun onDoubleClick() {
                            callSafely { // Helpful if you are using scrollSpeedLimit or skip if editing pdf
                                val originalCurrentPage = currentPage

                                if (!isZoomInMinScale()) zoomToMinimum()
                                else zoomToMaximum()

                                callIfScrollSpeedLimitIsEnabled {
                                    goToPage(originalCurrentPage)
                                }
                            }
                        }
                    })
                }
            )
        },
        pdfToolBar = {
            AnimatedVisibility(
                visible = !fullscreen,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
            ) {
                var onPickColorCallback by remember { mutableStateOf<((Color) -> Unit)?>(null) }
                var showZoomLimitDialog by remember { mutableStateOf(false) }

                PdfToolBar(
                    title = title,
                    toolBarState = toolBarState,
                    onBack = { finish() },
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    showEditor = true,
                    dropDownMenu = { onDismiss, defaultMenus ->
                        ExtendedTooBarMenus(
                            pdfState = pdfState,
                            showZoomLimitDialog = { showZoomLimitDialog = it },
                            onDismiss = onDismiss,
                            defaultMenus = defaultMenus
                        )
                    },
                    pickColor = { onPickColor ->
                        onPickColorCallback = onPickColor
                    },
                )

                ColorPickerDialog(
                    show = onPickColorCallback != null,
                    type = ColorPickerType.Classic(),
                    onDismissRequest = {
                        onPickColorCallback = null
                    },
                    onPickedColor = {
                        onPickColorCallback?.invoke(it)
                        onPickColorCallback = null
                    }
                )

                if (showZoomLimitDialog)
                    ZoomLimitDialog(
                        state = pdfState,
                        onDismiss = { showZoomLimitDialog = false }
                    )
            }
        },
        pdfScrollBar = { parentSize ->
            PdfScrollBar(
                parentSize = parentSize,
                contentColor = MaterialTheme.colorScheme.onBackground,
                handleColor = MaterialTheme.colorScheme.background
            )
        },
        loadingIndicator = {
            AnimatedVisibility(
                visible = pdfState.loadingState.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (val loadingState = pdfState.loadingState) {
                        is PdfLoadingState.Initializing -> CircularProgressIndicator()
                        is PdfLoadingState.Loading -> CircularProgressIndicator(progress = { loadingState.progress })
                        else -> {}
                    }
                    Text(text = "Loading...")
                }
            }
        }
    )
}

@OptIn(PdfUnstableApi::class)
@Composable
private fun Activity.ExtendedTooBarMenus(
    pdfState: PdfState,
    showZoomLimitDialog: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    defaultMenus: @Composable (filtered: List<PdfToolBarMenuItem>) -> Unit
) {
    val dropDownModifier = Modifier.padding(start = 6.dp, end = 18.dp)
    val authority = "${BuildConfig.APPLICATION_ID}.file.provider"

    if (pdfState.pdfViewer?.createSharableUri(authority) != null) {
        DropdownMenuItem(
            text = { Text(text = "Share", modifier = dropDownModifier) },
            onClick = {
                pdfState.pdfViewer
                    ?.createSharableUri(authority)
                    ?.let {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, it)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share PDF using"))
                    } ?: toast("Unable to share pdf!")
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(text = "Open With", modifier = dropDownModifier) },
            onClick = {
                pdfState.pdfViewer
                    ?.createSharableUri(authority)
                    ?.let {
                        startActivity(Intent(Intent.ACTION_VIEW, it).apply {
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        })
                    } ?: toast("Unable to open pdf with other apps!")
                onDismiss()
            }
        )
    }

    DropdownMenuItem(
        text = { Text(text = "Print", modifier = dropDownModifier) },
        onClick = {
            pdfState.pdfViewer?.printFile()
            onDismiss()
        }
    )
    DropdownMenuItem(
        text = { Text(text = "Zoom Limit", modifier = dropDownModifier) },
        onClick = {
            showZoomLimitDialog(true)
            onDismiss()
        }
    )
    DropdownMenuItem(
        text = {
            Text(
                text = (
                        if (pdfState.pdfViewer?.scrollSpeedLimit == PdfViewer.ScrollSpeedLimit.None) "Enable"
                        else "Disable"
                        ) + " scroll speed limit", modifier = dropDownModifier
            )
        },
        onClick = {
            if (pdfState.pdfViewer?.scrollSpeedLimit == PdfViewer.ScrollSpeedLimit.None)
                pdfState.pdfViewer?.scrollSpeedLimit = PdfViewer.ScrollSpeedLimit.AdaptiveFling()
            else pdfState.pdfViewer?.scrollSpeedLimit = PdfViewer.ScrollSpeedLimit.None
            onDismiss()
        }
    )
    defaultMenus(PdfToolBarMenuItem.entries)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZoomLimitDialog(state: PdfState, onDismiss: () -> Unit) {
    val pdfViewer = state.pdfViewer ?: return
    val rangeSliderState = remember {
        RangeSliderState(
            activeRangeStart = pdfViewer.minPageScale,
            activeRangeEnd = pdfViewer.maxPageScale,
            valueRange = 0.1f..10f,
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Zoom Limit",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    pdfViewer.minPageScale = rangeSliderState.activeRangeStart
                    pdfViewer.maxPageScale = rangeSliderState.activeRangeEnd
                    pdfViewer.scalePageTo(pdfViewer.currentPageScale)
                    onDismiss()
                }
            ) { Text(text = "Done") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel") }
        },
        text = {
            RangeSlider(rangeSliderState = rangeSliderState)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangeSlider(rangeSliderState: RangeSliderState) {
    val startInteractionSource = remember { MutableInteractionSource() }
    val endInteractionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .padding(horizontal = 12.dp),
    ) {
        RangeSlider(
            state = rangeSliderState,
            startInteractionSource = startInteractionSource,
            endInteractionSource = endInteractionSource,
            startThumb = {
                Label(
                    label = {
                        PlainTooltip(
                            modifier = Modifier
                                .sizeIn(45.dp, 25.dp)
                                .wrapContentWidth()
                        ) {
                            Text("%.2f".format(rangeSliderState.activeRangeStart))
                        }
                    },
                    interactionSource = startInteractionSource
                ) {
                    SliderDefaults.Thumb(
                        interactionSource = startInteractionSource,
                    )
                }
            },
            endThumb = {
                Label(
                    label = {
                        PlainTooltip(
                            modifier = Modifier
                                .requiredSize(45.dp, 25.dp)
                                .wrapContentWidth()
                        ) {
                            Text("%.2f".format(rangeSliderState.activeRangeEnd))
                        }
                    },
                    interactionSource = endInteractionSource
                ) {
                    SliderDefaults.Thumb(
                        interactionSource = endInteractionSource,
                    )
                }
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    rangeSliderState = sliderState
                )
            }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "0.1")
            Text(text = "10")
        }
    }
}

@Composable
private fun Activity.MainScreenWithScrollModeSupport() {
    val pdfState = rememberPdfState("file:///android_asset/test.pdf")

    LaunchedEffect(pdfState.loadingState) {
        pdfState.loadingState.let {
            if (it is PdfLoadingState.Error) {
                toast(it.errorMessage)
                finish()
            }
        }
    }

    PdfViewerContainer(
        pdfState = pdfState,
        pdfViewer = {
            var showPageButtons by remember { mutableStateOf(false) }
            var showPageNumber by remember { mutableStateOf(false) }

            PdfViewer(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                onReady = CustomOnReadyCallback { loadSource ->
                    loadSource()

                    showPageButtons = pageScrollMode == PdfViewer.PageScrollMode.SINGLE_PAGE
                    showPageNumber = pageScrollMode == PdfViewer.PageScrollMode.SINGLE_PAGE ||
                            pageScrollMode == PdfViewer.PageScrollMode.HORIZONTAL

                    addListener(onScrollModeChange = { scrollMode ->
                        showPageButtons = scrollMode == PdfViewer.PageScrollMode.SINGLE_PAGE
                        showPageNumber = pageScrollMode == PdfViewer.PageScrollMode.SINGLE_PAGE ||
                                pageScrollMode == PdfViewer.PageScrollMode.HORIZONTAL
                    })
                }
            )

            if (showPageButtons) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                            .clickable { pdfState.pdfViewer?.goToPreviousPage() }
                            .padding(8.dp)
                    )
                    Text(text = "${pdfState.currentPage} of ${pdfState.pagesCount}")
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                            .clickable { pdfState.pdfViewer?.goToNextPage() }
                            .padding(8.dp)
                    )
                }
            } else if (showPageNumber) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                ) {
                    Text(text = "${pdfState.currentPage} of ${pdfState.pagesCount}")
                }
            }
        },
        pdfToolBar = {
            PdfToolBar(
                title = "Demo",
                onBack = { finish() },
                contentColor = MaterialTheme.colorScheme.onBackground,
            )
        },
        pdfScrollBar = { parentSize ->
            PdfScrollBar(
                parentSize = parentSize,
                contentColor = MaterialTheme.colorScheme.onBackground,
                handleColor = MaterialTheme.colorScheme.background
            )
        },
        loadingIndicator = {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Text(text = "Loading...")
            }
        }
    )
}
