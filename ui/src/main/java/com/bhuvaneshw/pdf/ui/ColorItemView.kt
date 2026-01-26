package com.bhuvaneshw.pdf.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt

/**
 * A custom view that displays a circular color item.
 *
 * This view draws a circle filled with a specified [color] and an optional border.
 */
class ColorItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * The fill color of the circle.
     */
    @ColorInt
    var color: Int = Color.GRAY
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * The border color of the circle.
     */
    @ColorInt
    var borderColor: Int = Color.BLACK
        set(value) {
            field = value
            postInvalidate()
        }

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom

        val diameter = availableWidth.coerceAtMost(availableHeight)
        val radius = diameter / 2f

        val centerX = paddingLeft + (availableWidth / 2f)
        val centerY = paddingTop + (availableHeight / 2f)

        fillPaint.color = this@ColorItemView.color
        borderPaint.color = this@ColorItemView.borderColor

        canvas.drawCircle(centerX, centerY, radius, fillPaint)
        canvas.drawCircle(centerX, centerY, radius, borderPaint)
    }

}
