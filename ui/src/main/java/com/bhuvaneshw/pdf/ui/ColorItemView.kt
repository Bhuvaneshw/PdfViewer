package com.bhuvaneshw.pdf.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt

class ColorItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    @ColorInt
    var color: Int = Color.GRAY
        set(value) {
            field = value
            postInvalidate()
        }
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
