package com.bhuvaneshw.pdf.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import androidx.annotation.ColorInt

/**
 * A GridView that displays a list of colors for selection.
 *
 * This view is used to present a grid of colors to the user. When a color is selected,
 * the `onSelectColor` callback is invoked. This is typically used for color selection
 * in annotation tools.
 */
@SuppressLint("ViewConstructor")
class ColorItemGrid internal constructor(
    context: Context,
    private val highlightEditorColors: List<Pair<String, Int>>,
    @param:ColorInt private val borderColor: Int,
    private val onSelectColor: (color: Int) -> Unit
) : GridView(context) {

    init {
        numColumns = 8
        horizontalSpacing = 16
        verticalSpacing = 16
        adapter = ColorGridAdapter()
    }

    private inner class ColorGridAdapter : BaseAdapter() {

        override fun getCount(): Int = highlightEditorColors.size
        override fun getItem(position: Int): Pair<String, Int> = highlightEditorColors[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val colorItemView: ColorItemView = if (convertView == null) {
                ColorItemView(context).apply {
                    val size = context.dpToPx(40)
                    val paddingValue = context.dpToPx(8)
                    layoutParams = ViewGroup.LayoutParams(size, size)
                    this@apply.borderColor = this@ColorItemGrid.borderColor
                    setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
                }
            } else {
                convertView as ColorItemView
            }

            val (_, color) = getItem(position)
            colorItemView.color = color
            colorItemView.setOnClickListener {
                onSelectColor(color)
            }

            return colorItemView
        }
    }
}
