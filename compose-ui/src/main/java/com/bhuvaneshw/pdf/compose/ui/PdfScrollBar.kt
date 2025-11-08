package com.bhuvaneshw.pdf.compose.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhuvaneshw.pdf.compose.PdfState
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * A composable that displays a scrollbar for the PDF viewer that can be dragged to navigate through pages.
 * It can be either vertical or horizontal, and shows the current page number.
 *
 * @param pdfState The state of the PDF viewer.
 * @param parentSize The size of the parent container.
 * @param modifier The modifier to be applied to the scrollbar.
 * @param contentColor The color of the content on the scrollbar.
 * @param handleColor The color of the scrollbar handle.
 * @param interactiveScrolling If true, the [com.bhuvaneshw.pdf.compose.PdfViewer] will scroll as the user drags the scrollbar handle.
 * If false, the [com.bhuvaneshw.pdf.compose.PdfViewer] will only scroll when the user releases the handle.
 * @param useVerticalScrollBarForHorizontalMode If true, a vertical scrollbar is used even when the PDF is in a horizontal scroll mode.
 * @param animationSpec The animation specification for the scrollbar's fade-in and fade-out animations.
 *
 * @see com.bhuvaneshw.pdf.compose.PdfState
 * @see com.bhuvaneshw.pdf.compose.PdfViewer
 */
@Composable
fun PdfContainerBoxScope.PdfScrollBar(
    pdfState: PdfState,
    parentSize: IntSize,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.Black,
    handleColor: Color = Color(0xfff1f1f1),
    interactiveScrolling: Boolean = true,
    useVerticalScrollBarForHorizontalMode: Boolean = false,
    animationSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
) {
    var visible by remember { mutableStateOf(true) }
    var mySize by remember { mutableStateOf(IntSize(1, 1)) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var isHorizontalScroll by remember { mutableStateOf(false) }

    LaunchedEffect(pdfState.scrollState) {
        visible = true
        if (!isDragging) {
            offsetX = (pdfState.scrollState.ratio * (parentSize.width - mySize.width))
            offsetY = (pdfState.scrollState.ratio * (parentSize.height - mySize.height))
        }
        delay(1200)
        if (!isDragging)
            visible = false
    }

    LaunchedEffect(pdfState.scrollState.isHorizontalScroll) {
        val newValue =
            pdfState.scrollState.isHorizontalScroll && !useVerticalScrollBarForHorizontalMode
        if (isHorizontalScroll != newValue)
            isHorizontalScroll = newValue
    }

    LaunchedEffect(pdfState.loadingState.isInitialized) {
        if (pdfState.loadingState.isInitialized && pdfState.pdfViewer?.ui?.viewerScrollbar == true) {
            pdfState.pdfViewer?.ui?.viewerScrollbar = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec),
        exit = fadeOut(animationSpec),
        modifier = modifier
            .align(if (isHorizontalScroll) Alignment.BottomStart else Alignment.TopEnd)
            .offset {
                IntOffset(
                    x = if (isHorizontalScroll) offsetX.toInt() else 0,
                    y = if (isHorizontalScroll) 0 else offsetY.toInt()
                )
            }
            .onGloballyPositioned { mySize = it.size }
    ) {
        if (isHorizontalScroll) HorizontalScrollBar(
            interactiveScrolling = interactiveScrolling,
            pdfState = pdfState,
            offsetX = offsetX,
            parentWidth = parentSize.width,
            myWidth = mySize.width,
            handleColor = handleColor,
            contentColor = contentColor,
            setIsDragging = { isDragging = it },
            setOffsetX = { offsetX = it }
        )
        else VerticalScrollBar(
            interactiveScrolling = interactiveScrolling,
            pdfState = pdfState,
            offsetY = offsetY,
            parentHeight = parentSize.height,
            myHeight = mySize.height,
            handleColor = handleColor,
            contentColor = contentColor,
            setIsDragging = { isDragging = it },
            setOffsetY = { offsetY = it }
        )
    }
}

@Composable
private fun HorizontalScrollBar(
    interactiveScrolling: Boolean,
    pdfState: PdfState,
    offsetX: Float,
    parentWidth: Int,
    myWidth: Int,
    handleColor: Color,
    contentColor: Color,
    setIsDragging: (Boolean) -> Unit,
    setOffsetX: (Float) -> Unit,
) {
    DisposableEffect(Unit) { onDispose { setIsDragging(false) } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics {
            contentDescription = "Page ${pdfState.currentPage} of ${pdfState.pagesCount}"
        }
    ) {
        val currentPage =
            if (interactiveScrolling) pdfState.currentPage
            else {
                val ratio = offsetX / (parentWidth - myWidth)
                (ratio * (pdfState.pagesCount - 1)).checkNaN(1f).roundToInt() + 1
            }

        Text(
            text = "$currentPage/${pdfState.pagesCount}",
            modifier = Modifier
                .clip(CircleShape)
                .background(handleColor)
                .padding(horizontal = 8.dp, vertical = 1.dp)
                .clearAndSetSemantics {},
            fontSize = 12.sp,
            color = contentColor
        )

        Icon(
            painter = painterResource(R.drawable.baseline_drag_indicator_horizontal_24),
            contentDescription = null,
            modifier = Modifier
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        setIsDragging(true)
                        val newOffsetX =
                            (offsetX + delta).coerceIn(0f, (parentWidth - myWidth).toFloat())
                        val newRatio = newOffsetX / (parentWidth - myWidth)

                        if (interactiveScrolling)
                            pdfState.pdfViewer?.scrollToRatio(newRatio)
                        setOffsetX(newOffsetX)
                    },
                    onDragStopped = {
                        if (!interactiveScrolling) {
                            val newRatio = offsetX / (parentWidth - myWidth)
                            val newCurrentPage =
                                (newRatio * (pdfState.pagesCount - 1)).toInt() + 1
                            pdfState.pdfViewer?.scrollToRatio(newRatio)
                            pdfState.pdfViewer?.goToPage(newCurrentPage)
                        }
                        setIsDragging(false)
                    }
                )
                .padding(top = 4.dp)
                .size(width = 48.dp, height = 36.dp)
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(handleColor)
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp, bottom = 4.dp),
            tint = contentColor,
        )
    }
}

@Composable
private fun VerticalScrollBar(
    interactiveScrolling: Boolean,
    pdfState: PdfState,
    offsetY: Float,
    parentHeight: Int,
    myHeight: Int,
    handleColor: Color,
    contentColor: Color,
    setIsDragging: (Boolean) -> Unit,
    setOffsetY: (Float) -> Unit,
) {
    DisposableEffect(Unit) { onDispose { setIsDragging(false) } }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics {
            contentDescription = "Page ${pdfState.currentPage} of ${pdfState.pagesCount}"
        }
    ) {
        val currentPage =
            if (interactiveScrolling) pdfState.currentPage
            else {
                val ratio = offsetY / (parentHeight - myHeight)
                (ratio * (pdfState.pagesCount - 1)).checkNaN(1f).roundToInt() + 1
            }

        Text(
            text = "$currentPage/${pdfState.pagesCount}",
            modifier = Modifier
                .clip(CircleShape)
                .background(handleColor)
                .padding(horizontal = 8.dp, vertical = 1.dp)
                .clearAndSetSemantics { },
            fontSize = 12.sp,
            color = contentColor
        )

        Icon(
            painter = painterResource(R.drawable.baseline_drag_indicator_24),
            contentDescription = null,
            modifier = Modifier
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        setIsDragging(true)
                        val newOffsetY =
                            (offsetY + delta).coerceIn(0f, (parentHeight - myHeight).toFloat())
                        val newRatio = newOffsetY / (parentHeight - myHeight)

                        if (interactiveScrolling)
                            pdfState.pdfViewer?.scrollToRatio(newRatio)
                        setOffsetY(newOffsetY)
                    },
                    onDragStopped = {
                        if (!interactiveScrolling) {
                            val newRatio = offsetY / (parentHeight - myHeight)
                            val newCurrentPage =
                                (newRatio * (pdfState.pagesCount - 1)).toInt() + 1
                            pdfState.pdfViewer?.scrollToRatio(newRatio)
                            pdfState.pdfViewer?.goToPage(newCurrentPage)
                        }
                        setIsDragging(false)
                    }
                )
                .padding(start = 4.dp)
                .size(height = 48.dp, width = 38.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .background(handleColor)
                .padding(vertical = 8.dp)
                .padding(start = 8.dp, end = 4.dp),
            tint = contentColor,
        )
    }
}
