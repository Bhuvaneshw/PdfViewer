package com.bhuvaneshw.pdf.print

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument

class DefaultPdfPrintAdapter(context: Context) : PdfPrintAdapter(context) {

    override fun onRenderStart() {}
    override fun onRenderEnd() {}

    override fun onRenderPage(
        canvas: Canvas,
        info: PdfDocument.PageInfo,
        bitmap: Bitmap
    ) {
        canvas.drawColor(Color.WHITE)

        val scaled = scaleBitmapToFit(
            bitmap = bitmap,
            maxWidth = info.pageWidth.toFloat(),
            maxHeight = info.pageHeight.toFloat()
        )
        val x = (info.pageWidth - scaled.width) / 2f
        val y = (info.pageHeight - scaled.height) / 2f

        canvas.drawBitmap(scaled, x, y, null)
    }

    @SuppressLint("UseKtx")
    private fun scaleBitmapToFit(bitmap: Bitmap, maxWidth: Float, maxHeight: Float): Bitmap {
        val scale = minOf(
            1f, minOf(maxWidth / bitmap.width, maxHeight / bitmap.height)
        )
        val width = (bitmap.width * scale).toInt()
        val height = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}