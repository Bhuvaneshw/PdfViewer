package com.bhuvaneshw.pdf.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.withStyledAttributes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bhuvaneshw.pdf.PdfListener
import com.bhuvaneshw.pdf.PdfViewer
import com.bhuvaneshw.pdf.model.SideBarTreeItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A view that displays the outline of a PDF document in a tree-like structure, ideal for navigation
 * outlines in a PDF viewer.
 *
 * @see com.bhuvaneshw.pdf.PdfViewer
 * @see OutlineAdapter
 */
class PdfOutlineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val layoutInflater = LayoutInflater.from(context)
    private lateinit var pdfViewer: PdfViewer

    @SuppressLint("InflateParams")
    private val root = layoutInflater.inflate(R.layout.pdf_outline, null)

    /**
     * The [TextView] that displays the title of the outline view.
     */
    val title: TextView = root.findViewById(R.id.pdf_outline_title)

    /**
     * The [RecyclerView] that displays the list of outline items.
     */
    val itemsView: RecyclerView = root.findViewById(R.id.pdf_outline_recycler_view)

    /**
     * The color of the content (text and icons) in the outline view.
     */
    @ColorInt
    var contentColor: Int = Color.BLACK
        set(value) {
            field = value
            applyContentColor()
        }

    init {
        itemsView.layoutManager = LinearLayoutManager(context)
        itemsView.setHasFixedSize(true)

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.PdfOutlineView, defStyleAttr, 0) {
                val contentColor = getColor(
                    R.styleable.PdfOutlineView_contentColor,
                    Color.BLACK
                )
                this@PdfOutlineView.contentColor = contentColor
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addView(root, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    /**
     * Sets up the outline view with a [PdfViewer] instance.
     *
     * @param pdfViewer The [PdfViewer] to associate with this outline view.
     */
    fun setupWith(pdfViewer: PdfViewer) {
        if (this::pdfViewer.isInitialized && this.pdfViewer == pdfViewer) return
        this.pdfViewer = pdfViewer
        itemsView.adapter = OutlineAdapter(pdfViewer, contentColor)

        pdfViewer.addListener(object : PdfListener {
            override fun onLoadOutline(outline: List<SideBarTreeItem>) {
                title.text = if (outline.isEmpty()) "No Outline" else "Outline"
                (itemsView.adapter as OutlineAdapter).setOutlineItems(outline)
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun applyContentColor() {
        itemsView.adapter?.notifyDataSetChanged()
    }
}

internal class OutlineAdapter(
    private val pdfViewer: PdfViewer,
    @param:ColorInt private val contentColor: Int,
    @param:DrawableRes private val arrowResId: Int? = null,
) : RecyclerView.Adapter<OutlineAdapter.VH>() {

    private var tree: List<OutlineNode> = emptyList()
    private var flat: List<OutlineNode> = emptyList()
    private val scope = CoroutineScope(Dispatchers.Default)

    @SuppressLint("NotifyDataSetChanged")
    fun setOutlineItems(items: List<SideBarTreeItem>) {
        tree = buildOutlineTree(items)
        flat = tree.flattenTree()
        notifyDataSetChanged()
    }

    override fun getItemCount() = flat.size

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: VH, position: Int) {
        val node = flat[position]

        holder.arrow.setTintModes(contentColor)
        holder.text.text = node.item.title ?: ""
        holder.text.setTextColor(contentColor)

        holder.row.setPaddingRelative(node.level * 32, 0, 0, 0)

        val hasChildren = node.children.isNotEmpty()
        if (arrowResId != null) {
            holder.arrow.setImageResource(arrowResId)
        } else {
            holder.arrow.visibility = if (hasChildren) View.VISIBLE else View.INVISIBLE
            holder.arrow.rotation = if (node.expanded) 0f else -90f
        }

        holder.row.setOnClickListener {
            if (!hasChildren) {
                scope.launch { pdfViewer.ui.performSidebarTreeItemClick(node.item.id) }
                return@setOnClickListener
            }

            node.expanded = !node.expanded
            flat = tree.flattenTree()
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pdf_view_outline_item, parent, false)
        return VH(view)
    }

    internal class VH(v: View) : RecyclerView.ViewHolder(v) {
        val row: LinearLayout = v.findViewById(R.id.pdf_outline_row)
        val text: TextView = v.findViewById(R.id.pdf_outline_item_text)
        val arrow: ImageView = v.findViewById(R.id.pdf_outline_item_arrow)
    }
}
