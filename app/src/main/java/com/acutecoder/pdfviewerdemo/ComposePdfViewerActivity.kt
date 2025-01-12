package com.acutecoder.pdfviewerdemo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.acutecoder.pdf.PdfListener
import com.acutecoder.pdf.PdfOnScrollModeChange
import com.acutecoder.pdf.PdfUnstableApi
import com.acutecoder.pdf.PdfViewer
import com.acutecoder.pdf.callIfScrollSpeedLimitIsEnabled
import com.acutecoder.pdf.callWithScrollSpeedLimitDisabled
import com.acutecoder.pdf.setting.PdfSettingsManager
import com.acutecoder.pdf.sharedPdfSettingsManager
import com.acutecoder.pdfviewer.compose.CustomOnReadyCallback
import com.acutecoder.pdfviewer.compose.DefaultOnReadyCallback
import com.acutecoder.pdfviewer.compose.PdfLoadingState
import com.acutecoder.pdfviewer.compose.PdfState
import com.acutecoder.pdfviewer.compose.rememberPdfState
import com.acutecoder.pdfviewer.compose.ui.PdfScrollBar
import com.acutecoder.pdfviewer.compose.ui.PdfToolBar
import com.acutecoder.pdfviewer.compose.ui.PdfToolBarMenuItem
import com.acutecoder.pdfviewer.compose.ui.PdfViewer
import com.acutecoder.pdfviewer.compose.ui.PdfViewerContainer
import com.acutecoder.pdfviewer.compose.ui.rememberToolBarState
import com.acutecoder.pdfviewerdemo.ui.theme.PdfViewerComposeDemoTheme

class ComposePdfViewerActivity : ComponentActivity() {
    private lateinit var pdfSettingsManager: PdfSettingsManager
    private var pdfViewer: PdfViewer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pdfSettingsManager = sharedPdfSettingsManager("PdfSettings", MODE_PRIVATE)
            .also { it.includeAll() }

        // Path from asset, url or android uri
        val filePath = intent.extras?.getString("filePath")
            ?: intent.extras?.getString("fileUrl")
            ?: intent.extras?.getString("fileUri")
            ?: run {
                toast("No source available!")
                finish()
                return
            }

        val fileName = intent.extras?.getString("fileUri")
            ?.let { uri -> Uri.parse(uri).getFileName(this) }
            ?: intent.extras?.getString("fileName") ?: ""

        setContent {
            PdfViewerComposeDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MainScreen(
                            title = fileName,
                            url = filePath,
                            pdfSettingsManager = pdfSettingsManager,
                            setPdfViewer = {
                                pdfViewer = it
                            }
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
}

@Composable
private fun Activity.MainScreen(
    title: String,
    url: String,
    pdfSettingsManager: PdfSettingsManager,
    setPdfViewer: (PdfViewer?) -> Unit,
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
            pdfState.pdfViewer?.let { pdfSettingsManager.save(it) }
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
                    pdfSettingsManager.restore(this)
                    setPdfViewer(this)

                    addListener(object : PdfListener {
                        @OptIn(PdfUnstableApi::class)
                        override fun onSingleClick() {
                            callWithScrollSpeedLimitDisabled {  // Required only if you are using scrollSpeedLimit
                                fullscreen = !fullscreen
                            }
                        }

                        @OptIn(PdfUnstableApi::class)
                        override fun onDoubleClick() {
                            callWithScrollSpeedLimitDisabled { // Required only if you are using scrollSpeedLimit
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
                PdfToolBar(
                    title = title,
                    toolBarState = toolBarState,
                    onBack = { finish() },
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    dropDownMenu = { onDismiss, defaultMenus ->
                        ExtendedTooBarMenus(
                            pdfState,
                            onDismiss,
                            defaultMenus
                        )
                    },
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

@OptIn(PdfUnstableApi::class)
@Composable
private fun Activity.ExtendedTooBarMenus(
    state: PdfState,
    onDismiss: () -> Unit,
    defaultMenus: @Composable (filter: (PdfToolBarMenuItem) -> Boolean) -> Unit
) {
    var showZoomLimitDialog by remember { mutableStateOf(false) }
    val dropDownModifier = Modifier.padding(start = 6.dp, end = 18.dp)

    if (state.pdfViewer?.currentSource?.startsWith("file:///android_asset") == false)
        DropdownMenuItem(
            text = { Text(text = "Open in other app", modifier = dropDownModifier) },
            onClick = {
                val uri = Uri.parse(state.pdfViewer?.currentSource ?: return@DropdownMenuItem)
                startActivity(
                    Intent(Intent.ACTION_VIEW, uri).apply {
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                )
            }
        )
    DropdownMenuItem(
        text = { Text(text = "Zoom Limit", modifier = dropDownModifier) },
        onClick = { showZoomLimitDialog = true }
    )
    DropdownMenuItem(
        text = {
            Text(
                text =
                (if (state.pdfViewer?.scrollSpeedLimit == PdfViewer.ScrollSpeedLimit.None) "Enable" else "Disable")
                        + " scroll speed limit", modifier = dropDownModifier
            )
        },
        onClick = {
            if (state.pdfViewer?.scrollSpeedLimit == PdfViewer.ScrollSpeedLimit.None)
                state.pdfViewer?.scrollSpeedLimit = PdfViewer.ScrollSpeedLimit.AdaptiveFling()
            else state.pdfViewer?.scrollSpeedLimit = PdfViewer.ScrollSpeedLimit.None
            onDismiss()
        }
    )
    defaultMenus { true }

    if (showZoomLimitDialog)
        ZoomLimitDialog(state = state, onDismiss = { showZoomLimitDialog = false; onDismiss() })
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

                    addListener(PdfOnScrollModeChange { scrollMode ->
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
