package com.bhuvaneshw.pdf.compose.ui

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

            if (pdfState.loadingState.isLoading) loadingIndicator?.invoke(
                PdfContainerBoxScope(pdfState, this)
            )
        }
    }

    if (passwordDialogEnabled && pdfState.passwordRequired)
        PasswordDialog(pdfState)
    if (printDialogEnabled && pdfState.printState.isLoading)
        PrintDialog(pdfState)
}

@Composable
fun PdfContainerBoxScope.PdfViewer(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    onCreateViewer: (PdfViewer.() -> Unit)? = null,
    onReady: OnReadyCallback = DefaultOnReadyCallback(),
) {
    com.bhuvaneshw.pdf.compose.PdfViewer(
        pdfState = pdfState,
        modifier = modifier,
        containerColor = containerColor,
        onCreateViewer = onCreateViewer,
        onReady = onReady,
    )
}

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
) {
    PdfToolBar(
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
    )
}

@Composable
fun PdfContainerBoxScope.PdfScrollBar(
    parentSize: IntSize,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.Black,
    handleColor: Color = Color(0xfff1f1f1),
    interactiveScrolling: Boolean = true,
    useVerticalScrollBarForHorizontalMode: Boolean = false,
) {
    PdfScrollBar(
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
