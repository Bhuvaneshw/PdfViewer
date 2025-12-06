package com.bhuvaneshw.pdf.compose.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.bhuvaneshw.pdf.PdfEditor
import com.bhuvaneshw.pdf.PdfListener
import com.bhuvaneshw.pdf.PdfViewer
import com.bhuvaneshw.pdf.PdfViewer.PageSpreadMode
import com.bhuvaneshw.pdf.compose.MatchState
import com.bhuvaneshw.pdf.compose.PdfState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A composable function that provides a toolbar for the PDF viewer that provides various controls
 * for navigation, finding text, editing, and more.
 *
 * @param pdfState The state of the PDF viewer.
 * @param title The title to be displayed on the toolbar.
 * @param modifier The modifier to be applied to the toolbar.
 * @param toolBarState The state of the toolbar.
 * @param onBack A lambda to be invoked when the back button is pressed.
 * @param fileName A lambda that returns the name of the file.
 * @param contentColor The color of the content on the toolbar.
 * @param backIcon A composable that represents the back icon.
 * @param showEditor A boolean to indicate whether to show the editor tools.
 * @param pickColor A lambda to be invoked when a color is picked.
 * @param dropDownMenu A composable that represents the dropdown menu.
 * @param onPlaceIcons A composable that places editor and find icons. Additional icons can be added using this.
 * @param openOutlineView A lambda invoked to open the outline view.
 *
 * @see PdfState
 * @see PdfToolBarState
 * @see PdfToolBarBackIcon
 * @see PickColor
 * @see PdfToolBarMenu
 * @see com.bhuvaneshw.pdf.compose.PdfViewer
 */
@Composable
fun PdfToolBar(
    pdfState: PdfState,
    title: String,
    modifier: Modifier = Modifier,
    toolBarState: PdfToolBarState = rememberToolBarState(),
    onBack: (() -> Unit)? = null,
    fileName: (() -> String)? = null,
    contentColor: Color? = null,
    backIcon: (PdfToolBarBackIcon)? = defaultToolBarBackIcon(contentColor, onBack),
    showEditor: Boolean = false,
    pickColor: PickColor? = null,
    dropDownMenu: PdfToolBarMenu = defaultToolBarDropDownMenu(),
    onPlaceIcons: PlaceIcons = defaultIconsPosition(),
    openOutlineView: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val toolBarScope = PdfToolBarScope(
        pdfState = pdfState,
        toolBarState = toolBarState,
    )

    DisposableEffect(pdfState.pdfViewer) {
        val listener = object : PdfListener {
            override fun onEditorModeStateChange(state: PdfEditor.EditorModeState) {
                toolBarState.isTextHighlighterOn = state.isTextHighlighterOn
                toolBarState.isEditorFreeTextOn = state.isEditorFreeTextOn
                toolBarState.isEditorInkOn = state.isEditorInkOn
                toolBarState.isEditorStampOn = state.isEditorStampOn
            }
        }
        pdfState.pdfViewer?.addListener(listener)

        onDispose {
            pdfState.pdfViewer?.removeListener(listener)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        backIcon?.invoke(toolBarScope)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
                .weight(1f),
            color = contentColor ?: Color.Unspecified,
        )

        if (showEditor) AnimatedVisibility(
            visible = toolBarState.isEditorOpen,
            enter = slideIn { IntOffset(it.width / 25, 0) } + fadeIn(),
            exit = slideOut { IntOffset(it.width / 50, 0) } + fadeOut(),
        ) {
            toolBarScope.Editor(
                contentColor = contentColor ?: Color.Unspecified,
                pickColor = pickColor,
                modifier = Modifier.fillMaxWidth()
            )
        }

        AnimatedVisibility(
            visible = toolBarState.isFindBarOpen,
            enter = slideIn { IntOffset(it.width / 25, 0) } + fadeIn(),
            exit = slideOut { IntOffset(it.width / 50, 0) } + fadeOut(),
        ) {
            toolBarScope.FindBar(
                contentColor = contentColor ?: Color.Unspecified,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!toolBarScope.toolBarState.isFindBarOpen && !toolBarState.isEditorOpen) {
            onPlaceIcons({
                if (showEditor) {
                    toolBarScope.ToolBarIcon(
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit",
                        isEnabled = !pdfState.loadingState.isLoading,
                        onClick = { toolBarState.isEditorOpen = true },
                        tint = contentColor ?: Color.Unspecified,
                    )
                }
            }, {
                toolBarScope.ToolBarIcon(
                    icon = Icons.Default.Search,
                    contentDescription = "Find",
                    isEnabled = !pdfState.loadingState.isLoading,
                    onClick = { toolBarState.isFindBarOpen = true },
                    tint = contentColor ?: Color.Unspecified,
                )
            })

            Box {
                var showMoreOptions by remember { mutableStateOf(false) }

                var showZoom by remember { mutableStateOf(false) }
                var showGoToPage by remember { mutableStateOf(false) }
                var showScrollMode by remember { mutableStateOf(false) }
                var showPageSingleArrangement by remember { mutableStateOf(false) }
                var showSplitMode by remember { mutableStateOf(false) }
                var showAlignMode by remember { mutableStateOf(false) }
                var showSnapPage by remember { mutableStateOf(false) }
                var showDocumentProperties by remember { mutableStateOf(false) }
                var showAttachments by remember { mutableStateOf(false) }
                val onDismiss = { showMoreOptions = false }

                toolBarScope.ToolBarIcon(
                    icon = Icons.Default.MoreVert,
                    contentDescription = "More",
                    isEnabled = !pdfState.loadingState.isLoading,
                    onClick = { showMoreOptions = true },
                    tint = contentColor ?: Color.Unspecified,
                )

                DropdownMenu(
                    expanded = showMoreOptions,
                    onDismissRequest = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    dropDownMenu(onDismiss) { filtered ->
                        filtered.forEach { filteredItem ->
                            val pdfViewer = pdfState.pdfViewer ?: return@forEach

                            DropdownMenuItem(
                                menuItem = PdfToolBarMenuItem.SAVE,
                                filteredItem = filteredItem,
                                onClick = {
                                    pdfViewer.downloadFile()
                                    onDismiss()
                                }
                            )
                            DropdownMenuItem(
                                menuItem = PdfToolBarMenuItem.ZOOM,
                                filteredItem = filteredItem,
                                text = pdfViewer.currentPageScaleValue.formatZoom(pdfViewer.currentPageScale),
                                onClick = {
                                    showZoom = true
                                    onDismiss()
                                }
                            )

                            DropdownMenuItem(
                                menuItem = PdfToolBarMenuItem.GO_TO_PAGE,
                                filteredItem = filteredItem,
                                onClick = {
                                    showGoToPage = true
                                    onDismiss()
                                }
                            )
                            DropdownMenuItem(
                                menuItem = PdfToolBarMenuItem.ROTATE_CLOCK_WISE,
                                filteredItem = filteredItem,
                                onClick = {
                                    pdfViewer.rotateClockWise()
                                    onDismiss()
                                }
                            )
                            DropdownMenuItem(
                                menuItem = PdfToolBarMenuItem.ROTATE_ANTI_CLOCK_WISE,
                                filteredItem = filteredItem,
                                onClick = {
                                    pdfViewer.rotateCounterClockWise()
                                    onDismiss()
                                }
                            )

                            DropdownMenuItem(
                                menuItem = PdfToolBarMenuItem.SCROLL_MODE,
                                filteredItem = filteredItem,
                                onClick = {
                                    showScrollMode = true
                                    onDismiss()
                                }
                            )

                            val showSingleArrangementMenu = remember {
                                pdfState.scrollMode.let { it == PdfViewer.PageScrollMode.VERTICAL || it == PdfViewer.PageScrollMode.HORIZONTAL }
                                        && pdfState.spreadMode == PageSpreadMode.NONE
                            }
                            if (showSingleArrangementMenu)
                                DropdownMenuItem(
                                    menuItem = PdfToolBarMenuItem.CUSTOM_PAGE_ARRANGEMENT,
                                    filteredItem = filteredItem,
                                    onClick = {
                                        showPageSingleArrangement = true
                                        onDismiss()
                                    }
                                )
                            DropdownMenuItem(
                                menuItem = PdfToolBarMenuItem.SPREAD_MODE,
                                filteredItem = filteredItem,
                                onClick = {
                                    showSplitMode = true
                                    onDismiss()
                                }
                            )
                            DropdownMenuItem(
                                menuItem = PdfToolBarMenuItem.ALIGN_MODE,
                                filteredItem = filteredItem,
                                onClick = {
                                    showAlignMode = true
                                    onDismiss()
                                }
                            )
                            DropdownMenuItem(
                                menuItem = PdfToolBarMenuItem.SNAP_PAGE,
                                filteredItem = filteredItem,
                                onClick = {
                                    showSnapPage = true
                                    onDismiss()
                                }
                            )
                            DropdownMenuItem(
                                menuItem = PdfToolBarMenuItem.PROPERTIES,
                                filteredItem = filteredItem,
                                onClick = {
                                    showDocumentProperties = true
                                    onDismiss()
                                }
                            )
                            openOutlineView?.let {
                                DropdownMenuItem(
                                    menuItem = PdfToolBarMenuItem.OUTLINE,
                                    filteredItem = filteredItem,
                                    onClick = {
                                        openOutlineView()
                                        onDismiss()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                menuItem = PdfToolBarMenuItem.ATTACHMENTS,
                                filteredItem = filteredItem,
                                onClick = {
                                    showAttachments = true
                                    onDismiss()
                                }
                            )
                        }
                    }
                }

                if (showZoom)
                    ZoomDialog(pdfState = pdfState, onDismiss = { showZoom = false })
                if (showGoToPage)
                    GoToPageDialog(
                        pdfState = pdfState,
                        onDismiss = { showGoToPage = false })
                if (showScrollMode)
                    ScrollModeDialog(
                        pdfState = pdfState,
                        onDismiss = { showScrollMode = false })
                if (showPageSingleArrangement)
                    SinglePageArrangementDialog(
                        pdfState = pdfState,
                        onDismiss = { showPageSingleArrangement = false }
                    )
                if (showSplitMode)
                    SplitModeDialog(
                        pdfState = pdfState,
                        onDismiss = { showSplitMode = false })
                if (showAlignMode)
                    AlignModeDialog(
                        pdfState = pdfState,
                        onDismiss = { showAlignMode = false })
                if (showSnapPage)
                    SnapPageDialog(
                        pdfState = pdfState,
                        onDismiss = { showSnapPage = false })
                if (showDocumentProperties)
                    DocumentPropertiesDialog(
                        pdfState = pdfState,
                        fileName = fileName,
                        onDismiss = { showDocumentProperties = false },
                    )
                if (showAttachments)
                    AttachmentsDialog(
                        pdfState = pdfState,
                        scope = scope,
                        onDismiss = { showAttachments = false },
                        contentColor = contentColor,
                    )
            }
        }
    }
}

@Composable
private fun PdfToolBarScope.Editor(
    contentColor: Color,
    modifier: Modifier,
    pickColor: PickColor?,
) {
    val density = LocalDensity.current
    val popupY = remember { with(density) { 60.dp.toPx() }.roundToInt() }

    LaunchedEffect(toolBarState.isTextHighlighterOn) {
        pdfState.pdfViewer?.editor?.textHighlighterOn = toolBarState.isTextHighlighterOn
        if (!toolBarState.isTextHighlighterOn) {
            pdfState.pdfViewer?.removeTextSelection()
        }
    }
    LaunchedEffect(toolBarState.isEditorFreeTextOn) {
        pdfState.pdfViewer?.editor?.freeTextOn = toolBarState.isEditorFreeTextOn
    }
    LaunchedEffect(toolBarState.isEditorInkOn) {
        pdfState.pdfViewer?.editor?.inkOn = toolBarState.isEditorInkOn
    }
    LaunchedEffect(toolBarState.isEditorStampOn) {
        pdfState.pdfViewer?.editor?.stampOn = toolBarState.isEditorStampOn
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        AnimatedVisibility(
            visible = toolBarState.isTextHighlighterOn,
            enter = slideIn { IntOffset(it.width / 25, 0) } + fadeIn(),
            exit = slideOut { IntOffset(it.width / 50, 0) } + fadeOut(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            HighlightOptions(popupY, contentColor)
        }

        AnimatedVisibility(
            visible = toolBarState.isEditorFreeTextOn,
            enter = slideIn { IntOffset(it.width / 25, 0) } + fadeIn(),
            exit = slideOut { IntOffset(it.width / 50, 0) } + fadeOut(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            FreeTextOptions(popupY, contentColor, pickColor)
        }

        AnimatedVisibility(
            visible = toolBarState.isEditorInkOn,
            enter = slideIn { IntOffset(it.width / 25, 0) } + fadeIn(),
            exit = slideOut { IntOffset(it.width / 50, 0) } + fadeOut(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            InkOptions(popupY, contentColor, pickColor)
        }

        AnimatedVisibility(
            visible = toolBarState.isEditorStampOn,
            enter = slideIn { IntOffset(it.width / 25, 0) } + fadeIn(),
            exit = slideOut { IntOffset(it.width / 50, 0) } + fadeOut(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            StampOptions(contentColor)
        }

        if (toolBarState.isNothingOn()) {
            Text(
                text = "Edit",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp)
                    .weight(1f),
                color = contentColor,
            )
            MainIcons(contentColor)
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun PdfToolBarState.isNothingOn(): Boolean {
    return !(isTextHighlighterOn || isEditorFreeTextOn || isEditorInkOn || isEditorStampOn)
}

@Composable
private fun PdfToolBarScope.HighlightOptions(popupY: Int, contentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Highlight",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp),
            color = contentColor,
        )
        Spacer(Modifier.weight(1f))

        UndoRedoButtons()

        PopupSlider(
            icon = painterResource(R.drawable.baseline_thickness_24),
            text = "Thickness",
            contentDescription = "Thickness",
            value = pdfState.editor.highlightThickness,
            onValueChange = { pdfState.pdfViewer?.editor?.highlightThickness = it },
            range = 8f..24f,
            steps = 16,
            popupY = popupY,
        )

        ColorItemPicker(
            selectedColor = pdfState.editor.highlightColor,
            highlightEditorColors = pdfState.highlightEditorColors,
            onChangeColor = {
                pdfState.pdfViewer?.editor?.highlightColor = it.toArgb()
            },
            modifier = Modifier.padding(12.dp),
            borderColor = contentColor,
            popupY = popupY,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    pdfState.pdfViewer?.editor?.run { showAllHighlights = !showAllHighlights }
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Show All",
                modifier = Modifier.padding(end = 6.dp),
                fontSize = 14.sp
            )

            Switch(
                checked = pdfState.editor.showAllHighlights,
                onCheckedChange = { pdfState.pdfViewer?.editor?.showAllHighlights = it },
            )
        }
    }
}

@Composable
private fun PdfToolBarScope.FreeTextOptions(
    popupY: Int,
    contentColor: Color,
    pickColor: PickColor?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Text",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp),
            color = contentColor,
        )
        Spacer(Modifier.weight(1f))

        UndoRedoButtons()

        PopupSlider(
            icon = painterResource(R.drawable.baseline_text_fields_24),
            text = "Font Size",
            contentDescription = "Font Size",
            value = pdfState.editor.freeFontSize,
            onValueChange = { pdfState.pdfViewer?.editor?.freeFontSize = it },
            range = 5f..100f,
            steps = 95,
            popupY = popupY,
        )

        ColorItem(
            selectedColor = pdfState.editor.freeFontColor,
            borderColor = contentColor,
            onClick = {
                pickColor?.invoke(pdfState.editor.freeFontColor) { color ->
                    pdfState.pdfViewer?.editor?.freeFontColor = color.toArgb()
                }
            },
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun PdfToolBarScope.InkOptions(
    popupY: Int,
    contentColor: Color,
    pickColor: PickColor?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Draw",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp),
            color = contentColor,
        )
        Spacer(Modifier.weight(1f))

        UndoRedoButtons()

        PopupSlider(
            icon = painterResource(R.drawable.baseline_thickness_24),
            text = "Thickness",
            contentDescription = "Thickness",
            value = pdfState.editor.inkThickness,
            onValueChange = { pdfState.pdfViewer?.editor?.inkThickness = it },
            range = 1f..20f,
            steps = 19,
            popupY = popupY,
        )

        PopupSlider(
            icon = painterResource(R.drawable.baseline_opacity_24),
            text = "Opacity",
            contentDescription = "Opacity",
            value = pdfState.editor.inkOpacity,
            onValueChange = { pdfState.pdfViewer?.editor?.inkOpacity = it },
            range = 1f..100f,
            steps = 99,
            popupY = popupY,
        )

        ColorItem(
            selectedColor = pdfState.editor.inkColor,
            borderColor = contentColor,
            onClick = {
                pickColor?.invoke(pdfState.editor.inkColor) { color ->
                    pdfState.pdfViewer?.editor?.inkColor = color.toArgb()
                }
            },
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun PdfToolBarScope.StampOptions(contentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Add/Edit Images",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp),
            color = contentColor,
        )
        Spacer(Modifier.weight(1f))

        UndoRedoButtons()

        ToolBarIcon(
            painter = painterResource(R.drawable.outline_add_24),
            contentDescription = "Add Stamp",
            isEnabled = true,
            tint = MaterialTheme.colorScheme.onBackground,
            onClick = { pdfState.pdfViewer?.editor?.clickAddStamp() }
        )
    }
}

@Composable
private fun PdfToolBarScope.MainIcons(contentColor: Color) {
    ToolBarIcon(
        painter = painterResource(R.drawable.baseline_highlight_24),
        contentDescription = "Highlight",
        isEnabled = true,
        onClick = {
            toolBarState.isTextHighlighterOn = true
            toolBarState.isEditorFreeTextOn = false
            toolBarState.isEditorInkOn = false
            toolBarState.isEditorStampOn = false
        },
        tint = contentColor,
    )
    ToolBarIcon(
        painter = painterResource(R.drawable.baseline_text_fields_24),
        contentDescription = "Free Text",
        isEnabled = true,
        onClick = {
            toolBarState.isTextHighlighterOn = false
            toolBarState.isEditorFreeTextOn = true
            toolBarState.isEditorInkOn = false
            toolBarState.isEditorStampOn = false
        },
        tint = contentColor,
    )
    ToolBarIcon(
        painter = painterResource(R.drawable.baseline_draw_24),
        contentDescription = "Draw",
        isEnabled = true,
        onClick = {
            toolBarState.isTextHighlighterOn = false
            toolBarState.isEditorFreeTextOn = false
            toolBarState.isEditorInkOn = true
            toolBarState.isEditorStampOn = false
        },
        tint = contentColor,
    )
    ToolBarIcon(
        painter = painterResource(R.drawable.baseline_image_24),
        contentDescription = "Add Image",
        isEnabled = true,
        onClick = {
            toolBarState.isTextHighlighterOn = false
            toolBarState.isEditorFreeTextOn = false
            toolBarState.isEditorInkOn = false
            toolBarState.isEditorStampOn = true
        },
        tint = contentColor,
    )
}

@Composable
private fun PdfToolBarScope.UndoRedoButtons() {
    ToolBarIcon(
        painter = painterResource(R.drawable.baseline_undo_24),
        contentDescription = "Undo",
        isEnabled = true,
        tint = MaterialTheme.colorScheme.onBackground,
        onClick = { pdfState.pdfViewer?.editor?.undo() }
    )

    ToolBarIcon(
        painter = painterResource(R.drawable.baseline_redo_24),
        contentDescription = "Redo",
        isEnabled = true,
        tint = MaterialTheme.colorScheme.onBackground,
        onClick = { pdfState.pdfViewer?.editor?.redo() }
    )
}

@Composable
private fun PdfToolBarScope.PopupSlider(
    icon: Painter,
    text: String,
    contentDescription: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    steps: Int,
    range: ClosedFloatingPointRange<Float>,
    popupY: Int,
) {
    var showPicker by remember { mutableStateOf(false) }

    ToolBarIcon(
        painter = icon,
        contentDescription = contentDescription,
        onClick = { showPicker = !showPicker },
        isEnabled = true,
        tint = MaterialTheme.colorScheme.onBackground,
    )

    if (showPicker) {
        Popup(
            properties = PopupProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true
            ),
            alignment = Alignment.TopEnd,
            offset = IntOffset(x = 0, y = popupY),
            onDismissRequest = { showPicker = false }
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(12.dp)
                    .widthIn(min = 80.dp, max = 220.dp)
            ) {
                Text(text = text)

                Slider(
                    value = value.toFloat(),
                    onValueChange = { onValueChange(it.roundToInt()) },
                    steps = steps,
                    valueRange = range,
                )
            }
        }
    }
}

@Composable
private fun ColorItemPicker(
    selectedColor: Color,
    borderColor: Color,
    highlightEditorColors: List<Pair<String, Color>>,
    onChangeColor: (Color) -> Unit,
    popupY: Int,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }

    ColorItem(
        modifier = modifier,
        selectedColor = selectedColor,
        borderColor = borderColor,
        onClick = { showPicker = !showPicker }
    )

    if (showPicker) {
        Popup(
            properties = PopupProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true
            ),
            alignment = Alignment.TopEnd,
            offset = IntOffset(x = 0, y = popupY),
            onDismissRequest = { showPicker = false }
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(12.dp)
                    .widthIn(min = 80.dp, max = 220.dp)
            ) {
                Text(text = "Highlight Color")
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(36.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    items(highlightEditorColors) {
                        ColorItem(
                            modifier = Modifier.padding(6.dp),
                            selectedColor = it.second,
                            borderColor = borderColor,
                            onClick = {
                                onChangeColor(it.second)
                                showPicker = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorItem(
    selectedColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(selectedColor)
            .border(1.25.dp, borderColor, CircleShape)
            .clickable { onClick() }
            .padding(3.dp)
    )
}

@Composable
private fun PdfToolBarScope.FindBar(contentColor: Color, modifier: Modifier) {
    var searchTerm by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    LaunchedEffect(pdfState.matchState) {
        pdfState.matchState.run {
            if (this is MatchState.Completed && !found && searchTerm.isNotEmpty())
                Toast.makeText(context, "No match found!", Toast.LENGTH_SHORT).show()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            pdfState.pdfViewer?.findController?.stopFind()
            pdfState.clearFind()
        }
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = searchTerm,
            onValueChange = { searchTerm = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (searchTerm.isNotEmpty()) {
                    pdfState.pdfViewer?.findController?.startFind(searchTerm)
                    keyboard?.hide()
                }
            }),
            singleLine = true,
            textStyle = TextStyle(fontSize = 16.sp, color = contentColor),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .weight(1f),
            decorationBox = { textField ->
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    textField()
                    this@Row.AnimatedVisibility(
                        visible = searchTerm.isEmpty(),
                        enter = slideIn { IntOffset(0, -it.height) } + fadeIn(),
                        exit = slideOut { IntOffset(0, -it.height) } + fadeOut(),
                    ) {
                        Text(
                            text = "Find",
                            modifier = Modifier.alpha(0.6f),
                            fontSize = 16.sp,
                            color = contentColor,
                        )
                    }
                }
            },
        )

        AnimatedVisibility(
            visible = pdfState.matchState.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(28.dp)
                    .padding(4.dp),
                strokeWidth = 3.dp,
            )
        }

        Text(
            text = pdfState.matchState.run { "$current to $total" },
            modifier = Modifier.padding(4.dp),
            fontSize = 14.sp,
            color = contentColor,
        )

        ToolBarIcon(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Find Previous",
            isEnabled = true,
            onClick = { pdfState.pdfViewer?.findController?.findPrevious() },
            tint = contentColor,
        )
        ToolBarIcon(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Find Next",
            isEnabled = true,
            onClick = { pdfState.pdfViewer?.findController?.findNext() },
            tint = contentColor,
        )
    }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
        delay(100)
        keyboard?.show()
    }
}

@Composable
private fun ZoomDialog(pdfState: PdfState, onDismiss: () -> Unit) {
    val displayOptions = arrayOf(
        "Automatic", "Page Fit", "Page Width", "Actual Size",
        "50%", "75%", "100%", "125%", "150%", "200%", "300%", "400%"
    )
    val options = arrayOf(
        Zoom.AUTOMATIC.value, Zoom.PAGE_FIT.value,
        Zoom.PAGE_WIDTH.value, Zoom.ACTUAL_SIZE.value,
        "0.5", "0.75", "1", "1.25", "1.5", "2", "3", "4"
    )
    val pdfViewer = pdfState.pdfViewer ?: run { onDismiss(); return }
    val selectedIndex = findSelectedOption(options, pdfViewer.currentPageScaleValue)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Zoom Level",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 450.dp)) {
                itemsIndexed(displayOptions) { index, option ->
                    RadioButtonItem(
                        selectedIndex = selectedIndex,
                        index = index,
                        text = option,
                        onSelectIndex = { which ->
                            when (which) {
                                0 -> pdfViewer.zoomTo(PdfViewer.Zoom.AUTOMATIC)
                                1 -> pdfViewer.zoomTo(PdfViewer.Zoom.PAGE_FIT)
                                2 -> pdfViewer.zoomTo(PdfViewer.Zoom.PAGE_WIDTH)
                                3 -> pdfViewer.zoomTo(PdfViewer.Zoom.ACTUAL_SIZE)
                                else -> pdfViewer.scalePageTo(
                                    scale = options[which].toFloatOrNull() ?: 1f
                                )
                            }
                            onDismiss()
                        },
                    )
                }
            }
        }
    )
}

@Composable
private fun GoToPageDialog(pdfState: PdfState, onDismiss: () -> Unit) {
    var pageNumber by remember { mutableStateOf("") }

    val select: () -> Unit = {
        pageNumber.toIntOrNull()?.let { pdfState.pdfViewer?.goToPage(it) }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Go to page",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        },
        confirmButton = { TextButton(onClick = select) { Text("Go") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            BasicTextField(
                value = pageNumber,
                onValueChange = { pageNumber = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(onGo = { select() }),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                decorationBox = { textField ->
                    Box(Modifier.fillMaxWidth()) {
                        textField()
                        AnimatedVisibility(
                            visible = pageNumber.isEmpty(),
                            enter = slideIn { IntOffset(0, -it.height) } + fadeIn(),
                            exit = slideOut { IntOffset(0, -it.height) } + fadeOut(),
                        ) {
                            Text(
                                text = "Page Number",
                                modifier = Modifier.alpha(0.6f),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            )
        }
    )
}

@Composable
private fun ScrollModeDialog(pdfState: PdfState, onDismiss: () -> Unit) {
    val displayOptions = arrayOf(
        "Vertical", "Horizontal", "Wrapped", "Single Page"
    )
    val options = arrayOf(
        PdfViewer.PageScrollMode.VERTICAL.name,
        PdfViewer.PageScrollMode.HORIZONTAL.name,
        PdfViewer.PageScrollMode.WRAPPED.name,
        PdfViewer.PageScrollMode.SINGLE_PAGE.name
    )
    val pdfViewer = pdfState.pdfViewer ?: run { onDismiss(); return }
    val selectedIndex = findSelectedOption(options, pdfViewer.pageScrollMode.name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Page Scroll Mode",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            LazyColumn {
                itemsIndexed(displayOptions) { index, option ->
                    RadioButtonItem(
                        selectedIndex = selectedIndex,
                        index = index,
                        text = option,
                        onSelectIndex = { which ->
                            pdfViewer.pageScrollMode =
                                PdfViewer.PageScrollMode.valueOf(options[which])
                            onDismiss()
                        },
                    )
                }
            }
        }
    )
}

@Composable
private fun SinglePageArrangementDialog(pdfState: PdfState, onDismiss: () -> Unit) {
    val pdfViewer = pdfState.pdfViewer ?: run { onDismiss(); return }
    var isChecked by remember { mutableStateOf(pdfViewer.singlePageArrangement) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Single Page Arrangement",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = {
            TextButton(onClick = {
                pdfViewer.singlePageArrangement = isChecked
                onDismiss()
            }) { Text(text = "Done") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isChecked = !isChecked }
                    .padding(start = 12.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Enable", modifier = Modifier.weight(1f))

                Switch(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it }
                )
            }
        }
    )
}

@Composable
private fun SplitModeDialog(pdfState: PdfState, onDismiss: () -> Unit) {
    val displayOptions = arrayOf(
        "None", "Odd", "Even"
    )
    val options = arrayOf(
        PageSpreadMode.NONE.name,
        PageSpreadMode.ODD.name,
        PageSpreadMode.EVEN.name
    )
    val pdfViewer = pdfState.pdfViewer ?: run { onDismiss(); return }
    val selectedIndex = findSelectedOption(options, pdfViewer.pageSpreadMode.name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Page Split Mode",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            LazyColumn {
                itemsIndexed(displayOptions) { index, option ->
                    RadioButtonItem(
                        selectedIndex = selectedIndex,
                        index = index,
                        text = option,
                        onSelectIndex = { which ->
                            pdfViewer.pageSpreadMode =
                                PageSpreadMode.valueOf(options[which])
                            onDismiss()
                        },
                    )
                }
            }
        }
    )
}

@Composable
private fun AlignModeDialog(pdfState: PdfState, onDismiss: () -> Unit) {
    val pdfViewer = pdfState.pdfViewer ?: run { onDismiss(); return }
    val displayOptions = buildList {
        add("Default")
        if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.VERTICAL && pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.WRAPPED))
            add("Center Vertically")
        if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.HORIZONTAL))
            add("Center Horizontally")
        if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode == PdfViewer.PageScrollMode.SINGLE_PAGE))
            add("Center Both")
    }.toTypedArray()
    val options = buildList {
        add(PdfViewer.PageAlignMode.DEFAULT.name)
        if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.VERTICAL && pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.WRAPPED))
            add(PdfViewer.PageAlignMode.CENTER_VERTICAL.name)
        if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.HORIZONTAL))
            add(PdfViewer.PageAlignMode.CENTER_HORIZONTAL.name)
        if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode == PdfViewer.PageScrollMode.SINGLE_PAGE))
            add(PdfViewer.PageAlignMode.CENTER_BOTH.name)
    }.toTypedArray()
    val selectedIndex = findSelectedOption(options, pdfViewer.pageAlignMode.name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Page Align Mode",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            LazyColumn {
                itemsIndexed(displayOptions) { index, option ->
                    RadioButtonItem(
                        selectedIndex = selectedIndex,
                        index = index,
                        text = option,
                        onSelectIndex = { which ->
                            pdfViewer.pageAlignMode =
                                PdfViewer.PageAlignMode.valueOf(options[which])
                            onDismiss()
                        },
                    )
                }
            }
        }
    )
}

@Composable
private fun SnapPageDialog(pdfState: PdfState, onDismiss: () -> Unit) {
    val pdfViewer = pdfState.pdfViewer ?: run { onDismiss(); return }
    var isChecked by remember { mutableStateOf(pdfViewer.snapPage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Snap Page",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = {
            TextButton(onClick = {
                pdfViewer.snapPage = isChecked
                onDismiss()
            }) { Text(text = "Done") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isChecked = !isChecked }
                    .padding(start = 12.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Enable", modifier = Modifier.weight(1f))

                Switch(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it }
                )
            }
        }
    )
}

@Composable
private fun DocumentPropertiesDialog(
    pdfState: PdfState,
    fileName: (() -> String)?,
    onDismiss: () -> Unit
) {
    val properties = pdfState.pdfViewer?.properties ?: run { onDismiss(); return }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Document Properties",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Close") } },
        text = {
            LazyColumn {
                propertyItem("File Name", fileName?.invoke()?.ifBlank { "-" } ?: "-")
                propertyItem("File Size", properties.fileSize.formatToSize())
                propertyItem("Title", properties.title)
                propertyItem("Subject", properties.subject)
                propertyItem("Author", properties.author)
                propertyItem("Creator", properties.creator)
                propertyItem("Producer", properties.producer)
                propertyItem("Creation Date", properties.creationDate.formatToDate())
                propertyItem("Modified Date", properties.modifiedDate.formatToDate())
                propertyItem("Keywords", properties.keywords)
                propertyItem("Pdf Version", properties.pdfFormatVersion)
                propertyItem("Fast Webview", properties.isLinearized.toString())
            }
        }
    )
}

@Composable
private fun AttachmentsDialog(
    pdfState: PdfState,
    scope: CoroutineScope,
    onDismiss: () -> Unit,
    contentColor: Color? = null,
) {
    val attachments = pdfState.pdfViewer?.attachments ?: run { onDismiss(); return }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Attachments",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Close") } },
        text = {
            if (attachments.isEmpty()) {
                Text(
                    text = "No attachments!", modifier = Modifier.padding(8.dp),
                    color = contentColor ?: Color.Unspecified,
                )
            } else {
                PdfOutlineLazyColumn(
                    items = attachments,
                    onItemClick = {
                        scope.launch { pdfState.pdfViewer?.ui?.performSidebarTreeItemClick(it.id) }
                    },
                    arrowResId = R.drawable.outline_download_24,
                    contentColor = contentColor,
                )
            }
        }
    )
}

private fun LazyListScope.propertyItem(name: String, value: String) {
    item {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = name, modifier = Modifier.weight(2f))
            Text(text = ": $value", modifier = Modifier.weight(3f))
        }
    }
}

@Composable
private fun RadioButtonItem(
    selectedIndex: Int,
    index: Int,
    text: String,
    onSelectIndex: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onSelectIndex(index) }
            .padding(start = 12.dp)
    ) {
        Text(text = text, modifier = Modifier.weight(1f))

        RadioButton(
            selected = selectedIndex == index,
            onClick = { onSelectIndex(index) }
        )
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
internal fun PdfToolBarScope.ToolBarIcon(
    icon: ImageVector,
    contentDescription: String,
    isEnabled: Boolean,
    tint: Color,
    onClick: (() -> Unit)? = null,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = Modifier
            .clip(CircleShape)
            .let {
                if (onClick != null) it.clickable(enabled = isEnabled, onClick = onClick)
                else it
            }
            .padding(8.dp),
        tint = tint.copy(alpha = if (isEnabled) 1f else 0.6f)
    )
}

@Suppress("UnusedReceiverParameter")
@Composable
internal fun PdfToolBarScope.ToolBarIcon(
    painter: Painter,
    contentDescription: String,
    isEnabled: Boolean,
    tint: Color,
    onClick: (() -> Unit)? = null,
) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = Modifier
            .clip(CircleShape)
            .let {
                if (onClick != null) it.clickable(enabled = isEnabled, onClick = onClick)
                else it
            }
            .padding(8.dp),
        tint = tint.copy(alpha = if (isEnabled) 1f else 0.6f)
    )
}

internal fun defaultToolBarBackIcon(
    contentColor: Color?,
    onBack: (() -> Unit)?
): PdfToolBarBackIcon {
    return {
        ToolBarIcon(
            icon = Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = "Back",
            onClick = {
                if (!toolBarState.handleBackPressed()) {
                    onBack?.invoke()
                }
            },
            isEnabled = true,
            tint = contentColor ?: Color.Unspecified
        )
    }
}

internal fun defaultToolBarDropDownMenu(): PdfToolBarMenu {
    return { _, defaultMenus ->
        defaultMenus(PdfToolBarMenuItem.entries)
    }
}

internal fun defaultIconsPosition(): PlaceIcons = { editorIcon, findIcon ->
    editorIcon()
    findIcon()
}

/**
 * A typealias for a composable that represents the back icon on the toolbar.
 */
typealias PdfToolBarBackIcon = @Composable PdfToolBarScope.() -> Unit

/**
 * A typealias for a composable that represents the dropdown menu on the toolbar.
 *
 * @see PdfToolBarMenuItem
 */
typealias PdfToolBarMenu = @Composable (onDismiss: () -> Unit, defaultMenus: @Composable (filtered: List<PdfToolBarMenuItem>) -> Unit) -> Unit

/**
 * A typealias for a function that picks a color.
 */
typealias PickColor = (previousColor: Color, (color: Color) -> Unit) -> Unit

/**
 * A typealias for a function that places editor and find icons.
 * @param editorIcon A composable that represents the editor icon.
 * @param findIcon A composable that represents the find icon.
 */
typealias PlaceIcons = @Composable (editorIcon: @Composable () -> Unit, findIcon: @Composable () -> Unit) -> Unit

@Composable
private fun DropdownMenuItem(
    menuItem: PdfToolBarMenuItem,
    onClick: () -> Unit,
    filteredItem: PdfToolBarMenuItem,
    text: String = menuItem.displayName,
) {
    if (filteredItem == menuItem) DropdownMenuItem(
        text = {
            Text(
                text = text,
                modifier = Modifier.padding(start = 6.dp, end = 18.dp)
            )
        },
        onClick = onClick
    )
}
