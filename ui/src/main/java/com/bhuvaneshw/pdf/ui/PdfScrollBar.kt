package com.bhuvaneshw.pdf.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import com.bhuvaneshw.pdf.PdfListener
import com.bhuvaneshw.pdf.PdfViewer
import java.util.Timer
import java.util.TimerTask
import kotlin.math.roundToInt

class PdfScrollBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @SuppressLint("InflateParams")
    private val rootVertical =
        LayoutInflater.from(context).inflate(R.layout.pdf_scrollbar_vertical, null)

    @SuppressLint("InflateParams")
    private val rootHorizontal =
        LayoutInflater.from(context).inflate(R.layout.pdf_scrollbar_horizontal, null)
    private var root = rootVertical

    private val timer = Timer()
    private var timerTask: TimerTask? = null
    private var isSetupDone = false
    private val scrollModeChangeListeners = mutableListOf<(isHorizontalScroll: Boolean) -> Unit>()
    var useVerticalScrollBarForHorizontalMode = false
    var isHorizontalScroll = false
        private set(value) {
            val newValue = value && !useVerticalScrollBarForHorizontalMode
            if (field == newValue) return
            field = newValue
            applyScrollMode(newValue)
        }

    var hideDelayMillis = 2000L
    var animationDuration = 250L
    var interactiveScrolling = true

    val pageNumberInfo: TextView
        get() = root.findViewById(R.id.page_number_info)
    val dragHandle: ImageView
        get() = root.findViewById(R.id.drag_handle)

    init {
        addView(root)

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.PdfScrollBar, defStyleAttr, 0) {
                val contentColor = getColor(
                    R.styleable.PdfScrollBar_contentColor,
                    Color.BLACK
                )
                val handleColor = getColor(
                    R.styleable.PdfScrollBar_handleColor,
                    0xfff1f1f1.toInt()
                )
                val useVerticalScrollBarForHorizontalMode = getBoolean(
                    R.styleable.PdfScrollBar_useVerticalScrollBarForHorizontalMode,
                    useVerticalScrollBarForHorizontalMode
                )
                setContentColor(contentColor, handleColor)
                this@PdfScrollBar.useVerticalScrollBarForHorizontalMode =
                    useVerticalScrollBarForHorizontalMode
            }
        }

        @SuppressLint("SetTextI18n")
        if (isInEditMode) pageNumberInfo.text = "1/3"
        else visibility = GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupWith(pdfViewer: PdfViewer, toolBar: PdfToolBar? = null, force: Boolean = false) {
        if (isSetupDone && !force) return
        isSetupDone = true

        pdfViewer.post {
            val dragListenerX = DragListenerX(
                targetView = this,
                parentWidth = pdfViewer.width,
                onScrollChange = { x ->
                    val ratio = x / (pdfViewer.width - width)
                    pdfViewer.scrollToRatio(ratio)
                },
                onUpdatePageInfoForNonInteractiveMode = { y ->
                    timerTask?.cancel()
                    if (visibility != VISIBLE) animateShow()
                    startTimer()

                    val ratio = x / (pdfViewer.width - width)
                    val pageNumber =
                        (ratio * (pdfViewer.pagesCount - 1)).checkNaN(1f).roundToInt() + 1
                    updatePageNumber(pageNumber, pdfViewer.pagesCount)
                }
            )
            val dragListenerY = DragListenerY(
                targetView = this,
                parentHeight = pdfViewer.height,
                topHeight = toolBar?.height ?: 0,
                onScrollChange = { y ->
                    val ratio = (y - (toolBar?.height ?: 0)) / (pdfViewer.height - height)
                    pdfViewer.scrollToRatio(ratio)
                },
                onUpdatePageInfoForNonInteractiveMode = { y ->
                    timerTask?.cancel()
                    if (visibility != VISIBLE) animateShow()
                    startTimer()

                    val ratio = (y - (toolBar?.height ?: 0)) / (pdfViewer.height - height)
                    val pageNumber =
                        (ratio * (pdfViewer.pagesCount - 1)).checkNaN(1f).roundToInt() + 1
                    updatePageNumber(pageNumber, pdfViewer.pagesCount)
                }
            )

            pdfViewer.onReady { ui.viewerScrollbar = false }
            rootHorizontal.findViewById<View>(R.id.drag_handle).setOnTouchListener(dragListenerX)
            rootVertical.findViewById<View>(R.id.drag_handle).setOnTouchListener(dragListenerY)

            pdfViewer.addListener(object : PdfListener {
                override fun onPageChange(pageNumber: Int) {
                    updatePageNumber(pageNumber, pdfViewer.pagesCount)
                }

                override fun onPageLoadStart() {
                    visibility = GONE
                }

                override fun onPageLoadSuccess(pagesCount: Int) {
                    updatePageNumber(pdfViewer.currentPage, pdfViewer.pagesCount)
                    pdfViewer.scrollTo(0)
                }

                override fun onScrollChange(
                    currentOffset: Int,
                    totalOffset: Int,
                    isHorizontalScroll: Boolean
                ) {
                    timerTask?.cancel()
                    if (visibility != VISIBLE) animateShow()
                    startTimer()
                    this@PdfScrollBar.isHorizontalScroll = isHorizontalScroll
                    updatePageNumber(pdfViewer.currentPage, pdfViewer.pagesCount)

                    if (!dragListenerY.isDragging && !dragListenerX.isDragging) {
                        val ratio = currentOffset.toFloat() / totalOffset.toFloat()
                        if (this@PdfScrollBar.isHorizontalScroll) {
                            val left = (pdfViewer.width - width) * ratio
                            translationX = left
                            translationY = 0f
                        } else {
                            val top = (pdfViewer.height - height) * ratio
                            translationY = (toolBar?.height ?: 0) + top
                            translationX = 0f
                        }
                    }
                }
            })
        }
    }

    fun addScrollModeChangeListener(listener: (isHorizontalScroll: Boolean) -> Unit) {
        scrollModeChangeListeners.add(listener)
    }

    fun removeScrollModeChangeListener(listener: (isHorizontalScroll: Boolean) -> Unit) {
        scrollModeChangeListeners.remove(listener)
    }

    fun setContentColor(@ColorInt contentColor: Int, @ColorInt handleColor: Int) {
        rootVertical.findViewById<TextView>(R.id.page_number_info).setTextColor(contentColor)
        rootHorizontal.findViewById<TextView>(R.id.page_number_info).setTextColor(contentColor)
        rootVertical.findViewById<TextView>(R.id.page_number_info).setBgTintModes(handleColor)
        rootHorizontal.findViewById<TextView>(R.id.page_number_info).setBgTintModes(handleColor)
        rootVertical.findViewById<ImageView>(R.id.drag_handle).setTintModes(contentColor)
        rootHorizontal.findViewById<ImageView>(R.id.drag_handle).setTintModes(contentColor)
        rootVertical.findViewById<ImageView>(R.id.drag_handle).setBgTintModes(handleColor)
        rootHorizontal.findViewById<ImageView>(R.id.drag_handle).setBgTintModes(handleColor)
    }

    @SuppressLint("SetTextI18n")
    private fun updatePageNumber(pageNumber: Int, count: Int) {
        pageNumberInfo.text = "$pageNumber/$count"
        root.contentDescription = resources.getString(R.string.pdf_scroll_bar, pageNumber, count)
    }

    private fun startTimer() {
        timerTask = object : TimerTask() {
            override fun run() {
                animateHide()
            }
        }
        timer.schedule(timerTask, hideDelayMillis)
    }

    private fun animateShow() {
        post {
            visibility = VISIBLE
            animate()
                .alpha(1f)
                .setDuration(animationDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        alpha = 1f
                    }
                })
                .start()
        }
    }

    private fun animateHide() {
        post {
            animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        visibility = GONE
                        alpha = 0f
                    }
                })
                .start()
        }
    }

    private fun applyScrollMode(isHorizontalScroll: Boolean) {
        rootVertical.removeFromParent()
        rootHorizontal.removeFromParent()
        root = if (isHorizontalScroll) rootHorizontal else rootVertical
        addView(root)
        scrollModeChangeListeners.forEach { it(isHorizontalScroll) }
    }

    inner class DragListenerX(
        private var targetView: View,
        private val parentWidth: Int,
        private val onScrollChange: (x: Float) -> Unit,
        private val onUpdatePageInfoForNonInteractiveMode: (x: Float) -> Unit,
    ) : OnTouchListener {
        var isDragging: Boolean = false
        private var dX: Float = 0f

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = targetView.x - event.rawX
                    isDragging = true
                }

                MotionEvent.ACTION_MOVE -> {
                    (event.rawX + dX - width / 2).tryCoerceIn(
                        min = 0f,
                        max = parentWidth.toFloat() - width
                    ) { x ->
                        targetView.translationX = x
                        if (interactiveScrolling)
                            onScrollChange(x)
                        else onUpdatePageInfoForNonInteractiveMode(x)
                    }
                }

                else -> {
                    if (!interactiveScrolling)
                        onScrollChange(targetView.translationX)

                    isDragging = false
                    startTimer()

                    return false
                }
            }
            return true
        }
    }

    inner class DragListenerY(
        private var targetView: View,
        private val parentHeight: Int,
        private val topHeight: Int,
        private val onScrollChange: (y: Float) -> Unit,
        private val onUpdatePageInfoForNonInteractiveMode: (y: Float) -> Unit,
    ) : OnTouchListener {
        var isDragging: Boolean = false
        private var dY: Float = 0f

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dY = targetView.y - event.rawY
                    isDragging = true
                }

                MotionEvent.ACTION_MOVE -> {
                    (event.rawY + dY - height / 2).tryCoerceIn(
                        min = topHeight.toFloat(),
                        max = topHeight.toFloat() + parentHeight.toFloat() - height
                    ) { y ->
                        targetView.translationY = y
                        if (interactiveScrolling)
                            onScrollChange(y)
                        else onUpdatePageInfoForNonInteractiveMode(y)
                    }
                }

                else -> {
                    if (!interactiveScrolling)
                        onScrollChange(targetView.translationY)

                    isDragging = false
                    startTimer()

                    return false
                }
            }
            return true
        }
    }

}
