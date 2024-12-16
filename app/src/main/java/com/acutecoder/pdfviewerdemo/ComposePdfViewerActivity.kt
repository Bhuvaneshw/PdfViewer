package com.acutecoder.pdfviewerdemo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.acutecoder.pdf.PdfOnScrollModeChange
import com.acutecoder.pdf.PdfViewer
import com.acutecoder.pdf.setting.PdfSettingsManager
import com.acutecoder.pdf.setting.sharedPdfSettingsManager
import com.acutecoder.pdfviewer.compose.PdfContainer
import com.acutecoder.pdfviewer.compose.PdfScrollBar
import com.acutecoder.pdfviewer.compose.PdfState
import com.acutecoder.pdfviewer.compose.PdfToolBar
import com.acutecoder.pdfviewer.compose.PdfViewer
import com.acutecoder.pdfviewer.compose.rememberPdfState
import com.acutecoder.pdfviewerdemo.ui.theme.PdfViewerComposeDemoTheme

class ComposePdfViewerActivity : ComponentActivity() {
    private lateinit var pdfSettingsManager: PdfSettingsManager
    private var pdfViewer: PdfViewer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val filePath: String
        val fileName: String
        pdfSettingsManager = sharedPdfSettingsManager("PdfSettings", MODE_PRIVATE)

        // View from other apps (from intent filter)
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            filePath = intent.data.toString()
            fileName = intent.data!!.getFileName(this)
        } else {
            // Path from asset, url or android uri
            filePath = intent.extras?.getString("filePath")
                ?: intent.extras?.getString("fileUrl")
                ?: intent.extras?.getString("fileUri")
                ?: run {
                    toast("No source available!")
                    finish()
                    return
                }

            fileName = intent.extras?.getString("fileUri")
                ?.let { uri -> Uri.parse(uri).getFileName(this) }
                ?: intent.extras?.getString("fileName") ?: ""
        }

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
        pdfViewer?.let { pdfSettingsManager.save(it, savePageNumber = true) }
        super.onPause()
    }

    override fun onDestroy() {
        pdfViewer?.let { pdfSettingsManager.save(it, savePageNumber = true) }
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
    val state = rememberPdfState(url = url)

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            toast(it)
            finish()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            state.pdfViewer?.let { pdfSettingsManager.save(it, savePageNumber = true) }
            setPdfViewer(null)
        }
    }

    PdfContainer(
        state = state,
        pdfViewer = {
            PdfViewer(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                onReady = {
                    pdfSettingsManager.restore(this)
                    setPdfViewer(this)
                }
            )
        },
        pdfToolBar = {
            PdfToolBar(
                title = title,
                onBack = { finish() },
                contentColor = MaterialTheme.colorScheme.onBackground,
                dropDownMenu = { onDismiss, defaultMenus ->
                    ExtendedTooBarMenus(
                        state,
                        onDismiss,
                        defaultMenus
                    )
                },
            )
        },
        pdfScrollBar = { parentHeight ->
            PdfScrollBar(
                parentHeight = parentHeight,
                contentColor = MaterialTheme.colorScheme.onBackground,
                handleColor = MaterialTheme.colorScheme.background
            )
        },
        loader = {
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

@Composable
private fun Activity.ExtendedTooBarMenus(
    state: PdfState,
    onDismiss: () -> Unit,
    defaultMenus: @Composable () -> Unit
) {
    var showZoomLimitDialog by remember { mutableStateOf(false) }
    val dropDownModifier = Modifier.padding(start = 6.dp, end = 18.dp)

    if (state.pdfViewer?.currentUrl?.startsWith("file:///android_asset") == false)
        DropdownMenuItem(
            text = { Text(text = "Open in other app", modifier = dropDownModifier) },
            onClick = {
                val uri = Uri.parse(state.pdfViewer?.currentUrl ?: return@DropdownMenuItem)
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
    defaultMenus()

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
    val state = rememberPdfState("file:///android_asset/test.pdf")

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            toast(it)
            finish()
        }
    }

    PdfContainer(
        state = state,
        pdfViewer = {
            var showPageButtons by remember { mutableStateOf(false) }
            var showPageNumber by remember { mutableStateOf(false) }

            PdfViewer(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                onReady = {
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
                            .clickable { state.pdfViewer?.goToPreviousPage() }
                            .padding(8.dp)
                    )
                    Text(text = "${state.currentPage} of ${state.pagesCount}")
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                            .clickable { state.pdfViewer?.goToNextPage() }
                            .padding(8.dp)
                    )
                }
            } else if (showPageNumber) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                ) {
                    Text(text = "${state.currentPage} of ${state.pagesCount}")
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
        pdfScrollBar = { parentHeight ->
            PdfScrollBar(
                parentHeight = parentHeight,
                contentColor = MaterialTheme.colorScheme.onBackground,
                handleColor = MaterialTheme.colorScheme.background
            )
        },
        loader = {
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