package com.bhuvaneshw.pdf.compose.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhuvaneshw.pdf.PdfViewer
import com.bhuvaneshw.pdf.compose.DefaultOnReadyCallback
import com.bhuvaneshw.pdf.compose.OnReadyCallback
import com.bhuvaneshw.pdf.compose.PdfPrintState
import com.bhuvaneshw.pdf.compose.PdfState
import com.bhuvaneshw.pdf.compose.ui.PdfScrollBar as ActualPdfScrollBar
import com.bhuvaneshw.pdf.compose.ui.PdfToolBar as ActualPdfToolBar

/**
 * A container view that orchestrates interactions between a [PdfViewer], [ActualPdfToolBar], and [ActualPdfScrollBar].
 * It manages the display of a PDF viewer, a toolbar, a scrollbar, loading indicators, and dialogs for passwords and printing.
 *
 * @param pdfState The state of the PDF viewer, containing information about the document, page number, etc.
 * @param pdfViewer A composable that renders the PDF viewer within a [PdfContainerBoxScope].
 * @param modifier The modifier to be applied to the container.
 * @param pdfToolBar An optional composable for displaying a toolbar within a [PdfContainerBoxScope].
 * @param pdfScrollBar An optional composable for displaying a scrollbar within a [PdfContainerBoxScope].
 * @param loadingIndicator An optional composable to show while the PDF is loading within a [PdfContainerBoxScope].
 * @param passwordDialogEnabled Whether to show the password dialog if the PDF is encrypted.
 * @param printDialogEnabled Whether to show the print dialog when printing.
 * @see com.bhuvaneshw.pdf.compose.PdfViewer
 */
@Composable
fun PdfViewerContainer(
    pdfState: PdfState,
    pdfViewer: @Composable PdfContainerBoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    pdfToolBar: (@Composable PdfContainerScope.() -> Unit)? = null,
    pdfScrollBar: (@Composable PdfContainerBoxScope.(parentSize: IntSize) -> Unit)? = null,
    loadingIndicator: (@Composable PdfContainerBoxScope.() -> Unit)? = null,
    passwordDialogEnabled: Boolean = true,
    printDialogEnabled: Boolean = true,
) {
    var parentSize by remember { mutableStateOf(IntSize(1, 1)) }

    Column(modifier = modifier) {
        pdfToolBar?.invoke(PdfContainerScope(pdfState))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { parentSize = it.size }
        ) {
            pdfViewer(PdfContainerBoxScope(pdfState, this))
            pdfScrollBar?.let { scrollBar ->
                Box(modifier = Modifier.fillMaxSize()) {
                    scrollBar(PdfContainerBoxScope(pdfState, this), parentSize)
                }
            }

            loadingIndicator?.invoke(PdfContainerBoxScope(pdfState, this))
        }
    }

    if (passwordDialogEnabled && pdfState.passwordRequired)
        PasswordDialog(pdfState)
    if (printDialogEnabled && pdfState.printState.isLoading)
        PrintDialog(pdfState)
}

/**
 * This is a wrapper around [com.bhuvaneshw.pdf.compose.PdfViewer] within a [PdfContainerBoxScope].
 *
 * @param modifier The modifier to be applied to the viewer.
 * @param containerColor The background color of the PDF viewer.
 * @param factory A factory function to create a [PdfViewer] instance.
 * @param onCreateViewer A callback that is invoked when the [PdfViewer] is created.
 * @param onReady A callback that is invoked when the PDF document is loaded and ready.
 * @see com.bhuvaneshw.pdf.compose.PdfViewer
 */
@Composable
fun PdfContainerBoxScope.PdfViewer(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    factory: (context: Context) -> PdfViewer = { PdfViewer(context = it) },
    onCreateViewer: (PdfViewer.() -> Unit)? = null,
    onReady: OnReadyCallback = DefaultOnReadyCallback(),
) {
    com.bhuvaneshw.pdf.compose.PdfViewer(
        pdfState = pdfState,
        modifier = modifier,
        containerColor = containerColor,
        factory = factory,
        onCreateViewer = onCreateViewer,
        onReady = onReady,
    )
}

/**
 * This is a wrapper around [ActualPdfToolBar] within a [PdfContainerBoxScope].
 *
 * @param title The title to be displayed on the toolbar.
 * @param modifier The modifier to be applied to the toolbar.
 * @param toolBarState The state of the toolbar, such as visibility of search and edit modes.
 * @param onBack An optional callback to be invoked when the back button is pressed.
 * @param fileName A function that returns the name of the file to be used for printing or sharing.
 * @param contentColor The color of the toolbar content.
 * @param backIcon The back icon to be displayed on the toolbar.
 * @param showEditor Whether to show the editor tools on the toolbar.
 * @param pickColor A function to be invoked when a color is picked from the editor.
 * @param dropDownMenu The dropdown menu to be displayed on the toolbar.
 * @param onPlaceIcons A composable that places editor and find icons. Additional icons can be added using this.
 * @see ActualPdfToolBar
 */
@Composable
fun PdfContainerScope.PdfToolBar(
    title: String,
    modifier: Modifier = Modifier,
    toolBarState: PdfToolBarState = rememberToolBarState(),
    onBack: (() -> Unit)? = null,
    fileName: (() -> String)? = null,
    contentColor: Color? = null,
    backIcon: (PdfToolBarBackIcon)? = defaultToolBarBackIcon(contentColor, onBack),
    showEditor: Boolean = false,
    pickColor: ((onPickColor: (color: Color) -> Unit) -> Unit)? = null,
    dropDownMenu: PdfToolBarMenu = defaultToolBarDropDownMenu(),
    onPlaceIcons: PlaceIcons = defaultIconsPosition(),
) {
    ActualPdfToolBar(
        pdfState = pdfState,
        title = title,
        modifier = modifier,
        toolBarState = toolBarState,
        onBack = null,
        backIcon = backIcon,
        fileName = fileName,
        contentColor = contentColor,
        showEditor = showEditor,
        pickColor = pickColor,
        dropDownMenu = dropDownMenu,
        onPlaceIcons = onPlaceIcons,
    )
}

/**
 * This is a wrapper around [ActualPdfScrollBar] within a [PdfContainerBoxScope].
 *
 * @param parentSize The size of the parent container.
 * @param modifier The modifier to be applied to the scrollbar.
 * @param contentColor The color of the scrollbar track.
 * @param handleColor The color of the scrollbar handle.
 * @param interactiveScrolling Whether the scrollbar should be interactive.
 * @param useVerticalScrollBarForHorizontalMode Whether to use a vertical scrollbar for horizontal scrolling mode.
 * @see ActualPdfScrollBar
 */
@Composable
fun PdfContainerBoxScope.PdfScrollBar(
    parentSize: IntSize,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.Black,
    handleColor: Color = Color(0xfff1f1f1),
    interactiveScrolling: Boolean = true,
    useVerticalScrollBarForHorizontalMode: Boolean = false,
) {
    ActualPdfScrollBar(
        pdfState = pdfState,
        parentSize = parentSize,
        modifier = modifier,
        contentColor = contentColor,
        handleColor = handleColor,
        interactiveScrolling = interactiveScrolling,
        useVerticalScrollBarForHorizontalMode = useVerticalScrollBarForHorizontalMode,
    )
}

@Composable
private fun PasswordDialog(pdfState: PdfState) {
    var title by remember { mutableStateOf("Enter Password") }
    var password by remember { mutableStateOf("") }

    val select: () -> Unit = {
        if (password.isEmpty()) password = ""
        else pdfState.pdfViewer?.ui?.passwordDialog?.submitPassword(password)
    }

    LaunchedEffect(Unit) {
        pdfState.pdfViewer?.ui?.passwordDialog?.getLabelText {
            if (it != null) title = it.replace("\"", "")
        }
    }

    AlertDialog(
        onDismissRequest = { pdfState.pdfViewer?.ui?.passwordDialog?.cancel() },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        },
        confirmButton = { TextButton(onClick = select) { Text("Done") } },
        dismissButton = {
            TextButton(onClick = { pdfState.pdfViewer?.ui?.passwordDialog?.cancel() }) {
                Text(text = "Cancel")
            }
        },
        text = {
            BasicTextField(
                value = password,
                onValueChange = { password = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardActions = KeyboardActions(onDone = { select() }),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                decorationBox = { field ->
                    Box(Modifier.fillMaxWidth()) {
                        field()
                        AnimatedVisibility(
                            visible = password.isEmpty(),
                            enter = slideIn { IntOffset(0, -it.height) } + fadeIn(),
                            exit = slideOut { IntOffset(0, -it.height) } + fadeOut(),
                        ) {
                            Text(
                                text = "Password",
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
private fun PrintDialog(pdfState: PdfState) {
    AlertDialog(
        onDismissRequest = { pdfState.pdfViewer?.ui?.passwordDialog?.cancel() },
        title = {
            Text(
                text = "Preparing to print…",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    pdfState.pdfViewer?.ui?.printDialog?.cancel()
                }
            ) {
                Text("Cancel")
            }
        },
        text = {
            Column {
                val progress = pdfState.printState.let {
                    if (it is PdfPrintState.Loading) it.progress else 0f
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                )

                Text(
                    text = "${(progress * 100).toInt()}%",
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .padding(horizontal = 6.dp)
                        .align(Alignment.End)
                )
            }
        }
    )
}
