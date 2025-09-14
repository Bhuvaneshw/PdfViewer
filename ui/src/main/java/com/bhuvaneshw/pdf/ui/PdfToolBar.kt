package com.bhuvaneshw.pdf.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.bhuvaneshw.pdf.PdfDocumentProperties
import com.bhuvaneshw.pdf.PdfListener
import com.bhuvaneshw.pdf.PdfViewer
import com.bhuvaneshw.pdf.PdfViewer.PageSpreadMode

open class PdfToolBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val layoutInflater = LayoutInflater.from(context)
    protected lateinit var pdfViewer: PdfViewer; private set

    var onBack: (() -> Unit)? = null
    var alertDialogBuilder: () -> AlertDialog.Builder = { AlertDialog.Builder(context) }
    var showDialog: (Dialog) -> Unit = { dialog -> dialog.show() }
    private var fileName: String? = null

    @SuppressLint("InflateParams")
    private val root = layoutInflater.inflate(R.layout.pdf_toolbar, null)

    @ColorInt
    var contentColor: Int = Color.BLACK
        set(value) {
            field = value
            applyContentColor(value)
        }
    var popupBackgroundColor: Int = Color.WHITE
    var pickColor: ((onPickColor: (Int) -> Unit) -> Unit)? = null

    val back: ImageView = root.findViewById(R.id.back)
    val title: TextView = root.findViewById(R.id.title)
    val find: ImageView = root.findViewById(R.id.find)
    val more: ImageView = root.findViewById(R.id.more)
    val findBar: LinearLayout = root.findViewById(R.id.find_bar)
    val findEditText: EditText = root.findViewById(R.id.find_edit_text)
    val findProgressBar: ProgressBar = root.findViewById(R.id.find_progress_bar)
    val findInfo: TextView = root.findViewById(R.id.find_info)
    val findPrevious: ImageView = root.findViewById(R.id.find_previous)
    val findNext: ImageView = root.findViewById(R.id.find_next)
    val edit: ImageView = root.findViewById(R.id.edit)
    val editorBar: LinearLayout = root.findViewById(R.id.editor_bar)
    val highlightBar: LinearLayout = root.findViewById(R.id.highlight_bar)
    val freeTextBar: LinearLayout = root.findViewById(R.id.free_text_bar)
    val inkBar: LinearLayout = root.findViewById(R.id.ink_bar)
    val stampBar: LinearLayout = root.findViewById(R.id.stamp_bar)
    val undo: ImageView = root.findViewById(R.id.undo)
    val redo: ImageView = root.findViewById(R.id.redo)
    val editTitle: TextView = root.findViewById(R.id.edit_title)
    val highlight: ImageView = root.findViewById(R.id.highlight)
    val freeText: ImageView = root.findViewById(R.id.free_text)
    val ink: ImageView = root.findViewById(R.id.ink)
    val stamp: ImageView = root.findViewById(R.id.stamp)

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    val showAllHighlights: Switch = root.findViewById(R.id.show_all_highlights)
    val highlightThickness: ImageView = root.findViewById(R.id.highlight_thickness)
    val highlightColor: ColorItemView = root.findViewById(R.id.highlight_color)
    val freeFontSize: ImageView = root.findViewById(R.id.free_font_size)
    val freeFontColor: ColorItemView = root.findViewById(R.id.free_font_color)
    val inkThickness: ImageView = root.findViewById(R.id.ink_thickness)
    val inkOpacity: ImageView = root.findViewById(R.id.ink_opacity)
    val inkColor: ColorItemView = root.findViewById(R.id.ink_color)

    init {
        initListeners()

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.PdfToolBar, defStyleAttr, 0) {
                val contentColor = getColor(
                    R.styleable.PdfToolBar_contentColor,
                    Color.BLACK
                )
                val showEditor = getBoolean(R.styleable.PdfToolBar_showEditor, false)
                this@PdfToolBar.contentColor = contentColor
                edit.run { visibility = if (showEditor) VISIBLE else GONE }
                popupBackgroundColor =
                    getColor(R.styleable.PdfToolBar_popupBackgroundColor, Color.WHITE)
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addView(root)
    }

    @SuppressLint("SetTextI18n")
    fun setupWith(pdfViewer: PdfViewer) {
        if (this::pdfViewer.isInitialized && this.pdfViewer == pdfViewer) return
        this.pdfViewer = pdfViewer
        initSecondaryMenu()

        pdfViewer.addListener(object : PdfListener {
            private var total = 0

            override fun onPageLoadStart() {
                find.isEnabled = false
                more.isEnabled = false
                edit.isEnabled = false
                setFindBarVisible(false)
            }

            override fun onPageLoadSuccess(pagesCount: Int) {
                find.isEnabled = true
                more.isEnabled = true
                edit.isEnabled = true
            }

            override fun onLoadProperties(properties: PdfDocumentProperties) {
                if (title.text.isBlank())
                    setTitle(properties.title)
            }

            override fun onFindMatchChange(current: Int, total: Int) {
                this.total = total
                findInfo.text = "$current of $total"
            }

            override fun onFindMatchStart() {
                findProgressBar.visibility = VISIBLE
            }

            override fun onFindMatchComplete(found: Boolean) {
                if (!found)
                    Toast.makeText(context, "No match found!", Toast.LENGTH_SHORT).show()
                findProgressBar.visibility = GONE
            }
        })

        find.isEnabled = false
        more.isEnabled = false
        edit.isEnabled = false

        showAllHighlights.isChecked = pdfViewer.editor.showAllHighlights
        showAllHighlights.setOnCheckedChangeListener { _, isChecked ->
            pdfViewer.editor.showAllHighlights = isChecked
        }
    }

    fun setFileName(name: String, setAsTitle: Boolean = true) {
        this.fileName = name
        if (setAsTitle)
            setTitle(name)
    }

    fun setTitle(title: String) {
        this.title.text = title
    }

    fun isFindBarVisible() = findBar.isVisible

    fun setFindBarVisible(visible: Boolean) {
        findBar.visibility = if (visible) VISIBLE else GONE
        findEditText.setText("")
        findInfo.text = ""

        if (visible) findEditText.requestKeyboard()
        else {
            pdfViewer.findController.stopFind()
            findEditText.hideKeyboard()
        }
    }

    fun setEditorBarVisible(visible: Boolean) {
        editorBar.visibility = if (visible) VISIBLE else GONE
        setEditorMainIconsVisible(mainIconsVisible = true, undoRedoVisible = false)
        highlightBar.visibility = GONE
        freeTextBar.visibility = GONE
        inkBar.visibility = GONE
        stampBar.visibility = GONE
        pdfViewer.editor.textHighlighterOn = false
        pdfViewer.editor.freeTextOn = false
        pdfViewer.editor.inkOn = false
    }

    fun isEditorBarVisible() = editorBar.isVisible

    @SuppressLint("SetTextI18n")
    fun setHighlightBarVisible(visible: Boolean) {
        editTitle.text = "Highlight"
        highlightBar.visibility = if (visible) VISIBLE else GONE
        pdfViewer.editor.textHighlighterOn = visible
        freeTextBar.visibility = GONE
        inkBar.visibility = GONE
        stampBar.visibility = GONE
        showAllHighlights.isChecked = pdfViewer.editor.showAllHighlights
        highlightColor.color = pdfViewer.editor.highlightColor
        setEditorMainIconsVisible(mainIconsVisible = false, undoRedoVisible = true)
    }

    fun isHighlightBarVisible() = highlightBar.isVisible

    @SuppressLint("SetTextI18n")
    fun setFreeTextBarVisible(visible: Boolean) {
        editTitle.text = "Text"
        highlightBar.visibility = GONE
        freeTextBar.visibility = if (visible) VISIBLE else GONE
        pdfViewer.editor.freeTextOn = visible
        inkBar.visibility = GONE
        stampBar.visibility = GONE
        freeFontColor.color = pdfViewer.editor.freeFontColor
        setEditorMainIconsVisible(mainIconsVisible = false, undoRedoVisible = true)
    }

    fun isFreeTextBarVisible() = freeTextBar.isVisible

    @SuppressLint("SetTextI18n")
    fun setInkBarVisible(visible: Boolean) {
        editTitle.text = "Draw"
        highlightBar.visibility = GONE
        freeTextBar.visibility = GONE
        inkBar.visibility = if (visible) VISIBLE else GONE
        stampBar.visibility = GONE
        pdfViewer.editor.inkOn = visible
        highlightColor.color = pdfViewer.editor.highlightColor
        inkColor.color = pdfViewer.editor.inkColor
        setEditorMainIconsVisible(mainIconsVisible = false, undoRedoVisible = true)
    }

    fun isInkBarVisible() = inkBar.isVisible

    @SuppressLint("SetTextI18n")
    fun setStampBarVisible(visible: Boolean) {
        editTitle.text = "Add/Edit Images"
        highlightBar.visibility = GONE
        freeTextBar.visibility = GONE
        inkBar.visibility = GONE
        stampBar.visibility = if (visible) VISIBLE else GONE
        pdfViewer.editor.stampOn = visible
        setEditorMainIconsVisible(mainIconsVisible = false, undoRedoVisible = true)
    }

    fun isStampBarVisible() = stampBar.isVisible

    @SuppressLint("SetTextI18n")
    private fun setEditorMainIconsVisible(mainIconsVisible: Boolean, undoRedoVisible: Boolean) {
        if (mainIconsVisible) {
            editTitle.text = "Edit"
            highlight.visibility = VISIBLE
            freeText.visibility = VISIBLE
            ink.visibility = VISIBLE
            stamp.visibility = VISIBLE
        } else {
            highlight.visibility = GONE
            freeText.visibility = GONE
            ink.visibility = GONE
            stamp.visibility = GONE
        }
        if (undoRedoVisible) {
            undo.visibility = VISIBLE
            redo.visibility = VISIBLE
        } else {
            undo.visibility = GONE
            redo.visibility = GONE
        }
    }

    private fun applyContentColor(@ColorInt contentColor: Int) {
        find.setTintModes(contentColor)
        more.setTintModes(contentColor)
        back.setTintModes(contentColor)
        title.setTextColor(contentColor)
        findEditText.setTextColor(contentColor)
        findNext.setTintModes(contentColor)
        findPrevious.setTintModes(contentColor)
        edit.setTintModes(contentColor)
        undo.setTintModes(contentColor)
        redo.setTintModes(contentColor)
        editTitle.setTextColor(contentColor)
        highlight.setTintModes(contentColor)
        freeText.setTintModes(contentColor)
        ink.setTintModes(contentColor)
        stamp.setTintModes(contentColor)
        highlightThickness.setTintModes(contentColor)
        freeFontSize.setTintModes(contentColor)
        inkThickness.setTintModes(contentColor)
        inkOpacity.setTintModes(contentColor)
        highlightColor.borderColor = contentColor
        freeFontColor.borderColor = contentColor
        inkColor.borderColor = contentColor
    }

    @SuppressLint("SetTextI18n")
    private fun initListeners() {
        back.setOnClickListener {
            if (!handleBackPressed()) {
                onBack?.invoke()
            }
        }

        find.setOnClickListener { setFindBarVisible(true) }
        findNext.setOnClickListener { pdfViewer.findController.findNext() }
        findPrevious.setOnClickListener { pdfViewer.findController.findPrevious() }
        edit.setOnClickListener { setEditorBarVisible(true) }
        highlight.setOnClickListener { setHighlightBarVisible(true) }
        freeText.setOnClickListener { setFreeTextBarVisible(true) }
        ink.setOnClickListener { setInkBarVisible(true) }
        stamp.setOnClickListener { setStampBarVisible(true) }

        findEditText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString()
                if (query.isNotEmpty()) {
                    pdfViewer.findController.startFind(query)
                    findEditText.hideKeyboard()
                }
                true
            } else false
        }

        undo.setOnClickListener { pdfViewer.editor.undo() }
        redo.setOnClickListener { pdfViewer.editor.redo() }

        highlightThickness.setOnClickListener {
            popup(it, popupBackgroundColor) {
                val paddingValue = context.dpToPx(12)
                addView(TextView(context).apply {
                    text = "Thickness"
                    setTextColor(contentColor)
                    setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
                })
                addView(
                    SeekBar(context).apply {
                        max = 16
                        progress = pdfViewer.editor.highlightThickness - 8
                        setOnSeekBarChangeListener(onSeekBarChangeListener { newProgress ->
                            pdfViewer.editor.highlightThickness = newProgress + 8
                        })
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            outlineSpotShadowColor = contentColor
                            outlineAmbientShadowColor = contentColor
                        }
                        setPadding(paddingValue, 0, paddingValue, paddingValue)
                    },
                    LinearLayout.LayoutParams(
                        context.dpToPx(220),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }

        highlightColor.setOnClickListener {
            popup(it, popupBackgroundColor) { dismiss ->
                val paddingValue = context.dpToPx(12)
                addView(TextView(context).apply {
                    text = "Highlight Color"
                    setTextColor(contentColor)
                    setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
                })
                addView(
                    ColorItemGrid(context, pdfViewer.highlightEditorColors, contentColor) { color ->
                        pdfViewer.editor.highlightColor = color
                        highlightColor.color = color
                        dismiss()
                    }
                )
            }
        }

        freeFontSize.setOnClickListener {
            popup(it, popupBackgroundColor) {
                val paddingValue = context.dpToPx(12)
                addView(TextView(context).apply {
                    text = "Font Size"
                    setTextColor(contentColor)
                    setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
                })
                addView(
                    SeekBar(context).apply {
                        max = 95
                        progress = pdfViewer.editor.freeFontSize - 5
                        setOnSeekBarChangeListener(onSeekBarChangeListener { newProgress ->
                            pdfViewer.editor.freeFontSize = newProgress + 5
                        })
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            outlineSpotShadowColor = contentColor
                            outlineAmbientShadowColor = contentColor
                        }
                        setPadding(paddingValue, 0, paddingValue, paddingValue)
                    },
                    LinearLayout.LayoutParams(
                        context.dpToPx(220),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }

        freeFontColor.setOnClickListener {
            pickColor?.invoke { color ->
                pdfViewer.editor.freeFontColor = color
                freeFontColor.color = color
            }
        }

        inkThickness.setOnClickListener {
            popup(it, popupBackgroundColor) {
                val paddingValue = context.dpToPx(12)
                addView(TextView(context).apply {
                    text = "Thickness"
                    setTextColor(contentColor)
                    setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
                })
                addView(
                    SeekBar(context).apply {
                        max = 19
                        progress = pdfViewer.editor.inkThickness - 1
                        setOnSeekBarChangeListener(onSeekBarChangeListener { newProgress ->
                            pdfViewer.editor.inkThickness = newProgress + 1
                        })
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            outlineSpotShadowColor = contentColor
                            outlineAmbientShadowColor = contentColor
                        }
                        setPadding(paddingValue, 0, paddingValue, paddingValue)
                    },
                    LinearLayout.LayoutParams(
                        context.dpToPx(220),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }

        inkOpacity.setOnClickListener {
            popup(it, popupBackgroundColor) {
                val paddingValue = context.dpToPx(12)
                addView(TextView(context).apply {
                    text = "Opacity"
                    setTextColor(contentColor)
                    setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
                })
                addView(
                    SeekBar(context).apply {
                        max = 99
                        progress = pdfViewer.editor.inkOpacity - 1
                        setOnSeekBarChangeListener(onSeekBarChangeListener { newProgress ->
                            pdfViewer.editor.inkOpacity = newProgress + 1
                        })
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            outlineSpotShadowColor = contentColor
                            outlineAmbientShadowColor = contentColor
                        }
                        setPadding(paddingValue, 0, paddingValue, paddingValue)
                    },
                    LinearLayout.LayoutParams(
                        context.dpToPx(220),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }

        inkColor.setOnClickListener {
            pickColor?.invoke { color ->
                pdfViewer.editor.inkColor = color
                inkColor.color = color
            }
        }
    }

    fun handleBackPressed(): Boolean {
        when {
            isHighlightBarVisible() -> {
                if (pdfViewer.editor.applyHighlightColorOnTextSelection) {
                    pdfViewer.resetTextSelectionColor()
                    pdfViewer.removeTextSelection()
                }
                setHighlightBarVisible(false)
                setEditorBarVisible(true)
            }

            isFreeTextBarVisible() -> {
                setFreeTextBarVisible(false)
                setEditorBarVisible(true)
            }

            isInkBarVisible() -> {
                setInkBarVisible(false)
                setEditorBarVisible(true)
            }

            isStampBarVisible() -> {
                setStampBarVisible(false)
                setEditorBarVisible(true)
            }

            isEditorBarVisible() -> setEditorBarVisible(false)
            isFindBarVisible() -> setFindBarVisible(false)
            else -> return false
        }

        return true
    }

    private fun initSecondaryMenu() {
        more.setOnClickListener {
            val popupMenu = getPopupMenu(more)
            popupMenu.setOnMenuItemClickListener(this::handlePopupMenuItemClick)
            popupMenu.show()
        }
    }

    protected open fun handlePopupMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            PdfToolBarMenuItem.DOWNLOAD.id -> pdfViewer.downloadFile()
            PdfToolBarMenuItem.ZOOM.id -> showZoomDialog()
            PdfToolBarMenuItem.GO_TO_PAGE.id -> showGoToPageDialog()
            PdfToolBarMenuItem.ROTATE_CLOCK_WISE.id -> pdfViewer.rotateClockWise()
            PdfToolBarMenuItem.ROTATE_ANTI_CLOCK_WISE.id -> pdfViewer.rotateCounterClockWise()
            PdfToolBarMenuItem.SCROLL_MODE.id -> showScrollModeDialog()
            PdfToolBarMenuItem.SINGLE_PAGE_ARRANGEMENT.id -> showSinglePageArrangementDialog()
            PdfToolBarMenuItem.SPREAD_MODE.id -> showSpreadModeDialog()
            PdfToolBarMenuItem.ALIGN_MODE.id -> showAlignModeDialog()
            PdfToolBarMenuItem.SNAP_PAGE.id -> showSnapPageDialog()
            PdfToolBarMenuItem.PROPERTIES.id -> showPropertiesDialog()
        }

        return item.itemId < PdfToolBarMenuItem.entries.size
    }

    protected open fun getPopupMenu(anchorView: View): PopupMenu {
        return addDefaultMenus(PopupMenu(context, anchorView))
    }

    protected open fun addDefaultMenus(
        popupMenu: PopupMenu,
        filter: (menuItem: PdfToolBarMenuItem) -> Boolean = { true },
    ): PopupMenu {
        return popupMenu.apply {
            addMenu("Download", PdfToolBarMenuItem.DOWNLOAD, filter)
            addMenu(
                pdfViewer.currentPageScaleValue.formatZoom(pdfViewer.currentPageScale),
                PdfToolBarMenuItem.ZOOM,
                filter
            )
            addMenu("Go to page", PdfToolBarMenuItem.GO_TO_PAGE, filter)
            addMenu("Rotate Clockwise", PdfToolBarMenuItem.ROTATE_CLOCK_WISE, filter)
            addMenu("Rotate Anti Clockwise", PdfToolBarMenuItem.ROTATE_ANTI_CLOCK_WISE, filter)
            addMenu("Scroll Mode", PdfToolBarMenuItem.SCROLL_MODE, filter)
            if (pdfViewer.pageScrollMode.let { it == PdfViewer.PageScrollMode.VERTICAL || it == PdfViewer.PageScrollMode.HORIZONTAL }
                && pdfViewer.pageSpreadMode == PageSpreadMode.NONE)
                addMenu(
                    "Single Page Arrangement",
                    PdfToolBarMenuItem.SINGLE_PAGE_ARRANGEMENT,
                    filter
                )
            addMenu("Split Mode", PdfToolBarMenuItem.SPREAD_MODE, filter)
            addMenu("Align Mode", PdfToolBarMenuItem.ALIGN_MODE, filter)
            addMenu("Snap Page", PdfToolBarMenuItem.SNAP_PAGE, filter)
            addMenu("Properties", PdfToolBarMenuItem.PROPERTIES, filter)
        }
    }

    private fun PopupMenu.addMenu(
        title: String,
        item: PdfToolBarMenuItem,
        filter: (menuItem: PdfToolBarMenuItem) -> Boolean,
    ) {
        if (filter(item))
            menu.add(Menu.NONE, item.id, Menu.NONE, title)
    }

    private fun showZoomDialog() {
        val displayOptions = arrayOf(
            "Automatic", "Page Fit", "Page Width", "Actual Size",
            "50%", "75%", "100%", "125%", "150%", "200%", "300%", "400%"
        )
        val options = arrayOf(
            Zoom.AUTOMATIC.value, Zoom.PAGE_FIT.value,
            Zoom.PAGE_WIDTH.value, Zoom.ACTUAL_SIZE.value,
            "0.5", "0.75", "1", "1.25", "1.5", "2", "3", "4"
        )

        val dialog = alertDialogBuilder()
            .setTitle("Select Zoom Level")
            .setSingleChoiceItems(
                displayOptions,
                findSelectedOption(options, pdfViewer.currentPageScaleValue)
            ) { dialog, which ->
                when (which) {
                    0 -> pdfViewer.zoomTo(PdfViewer.Zoom.AUTOMATIC)
                    1 -> pdfViewer.zoomTo(PdfViewer.Zoom.PAGE_FIT)
                    2 -> pdfViewer.zoomTo(PdfViewer.Zoom.PAGE_WIDTH)
                    3 -> pdfViewer.zoomTo(PdfViewer.Zoom.ACTUAL_SIZE)
                    else -> pdfViewer.scalePageTo(scale = options[which].toFloatOrNull() ?: 1f)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        showDialog(dialog)
    }

    private fun showGoToPageDialog() {
        @SuppressLint("InflateParams")
        val root = layoutInflater.inflate(R.layout.pdf_go_to_page_dialog, null)
        val field: EditText = root.findViewById(R.id.go_to_page_field)

        val gotTo: (String, DialogInterface) -> Unit = { pageNumber, dialog ->
            pdfViewer.goToPage(pageNumber.toIntOrNull() ?: pdfViewer.currentPage)
            dialog.dismiss()
        }

        val dialog = alertDialogBuilder()
            .setTitle("Go to page")
            .setView(root)
            .setPositiveButton("Go") { dialog, _ ->
                gotTo(field.text.toString(), dialog)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        field.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                gotTo(field.text.toString(), dialog)
                true
            } else {
                false
            }
        }
        dialog.setOnShowListener {
            field.postDelayed({
                field.requestKeyboard()
            }, 500)
        }
        showDialog(dialog)
    }

    private fun showScrollModeDialog() {
        val displayOptions = arrayOf(
            "Vertical", "Horizontal", "Wrapped", "Single Page"
        )
        val options = arrayOf(
            PdfViewer.PageScrollMode.VERTICAL.name,
            PdfViewer.PageScrollMode.HORIZONTAL.name,
            PdfViewer.PageScrollMode.WRAPPED.name,
            PdfViewer.PageScrollMode.SINGLE_PAGE.name
        )

        val dialog = alertDialogBuilder()
            .setTitle("Select Page Scroll Mode")
            .setSingleChoiceItems(
                displayOptions,
                findSelectedOption(options, pdfViewer.pageScrollMode.name)
            ) { dialog, which ->
                pdfViewer.pageScrollMode = PdfViewer.PageScrollMode.valueOf(options[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        showDialog(dialog)
    }

    private fun showSinglePageArrangementDialog() {
        val root = layoutInflater.inflate(R.layout.pdf_snap_page_dialog, null)
        val switch = root.findViewById<SwitchCompat>(R.id.snap_page)
        switch.isChecked = pdfViewer.singlePageArrangement

        alertDialogBuilder()
            .setTitle("Single Page Arrangement")
            .setView(root)
            .setPositiveButton("Done") { dialog, _ ->
                pdfViewer.singlePageArrangement = switch.isChecked
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showSpreadModeDialog() {
        val displayOptions = arrayOf(
            "None", "Odd", "Even"
        )
        val options = arrayOf(
            PageSpreadMode.NONE.name,
            PageSpreadMode.ODD.name,
            PageSpreadMode.EVEN.name
        )

        val dialog = alertDialogBuilder()
            .setTitle("Select Page Split Mode")
            .setSingleChoiceItems(
                displayOptions,
                findSelectedOption(options, pdfViewer.pageSpreadMode.name)
            ) { dialog, which ->
                pdfViewer.pageSpreadMode = PageSpreadMode.valueOf(options[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        showDialog(dialog)
    }

    private fun showAlignModeDialog() {
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

        val dialog = alertDialogBuilder()
            .setTitle("Select Page Align Mode")
            .setSingleChoiceItems(
                displayOptions,
                findSelectedOption(options, pdfViewer.pageAlignMode.name)
            ) { dialog, which ->
                pdfViewer.pageAlignMode = PdfViewer.PageAlignMode.valueOf(options[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        showDialog(dialog)
    }

    private fun showSnapPageDialog() {
        val root = layoutInflater.inflate(R.layout.pdf_snap_page_dialog, null)
        val switch = root.findViewById<SwitchCompat>(R.id.snap_page)
        switch.isChecked = pdfViewer.snapPage

        alertDialogBuilder()
            .setTitle("Snap Page")
            .setView(root)
            .setPositiveButton("Done") { dialog, _ ->
                pdfViewer.snapPage = switch.isChecked
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showPropertiesDialog() {
        alertDialogBuilder()
            .setTitle("Document Properties")
            .let {
                pdfViewer.properties?.let { properties ->
                    it.setPropertiesView(properties)
                } ?: it.setMessage("Properties not loaded yet!")
            }
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun AlertDialog.Builder.setPropertiesView(properties: PdfDocumentProperties): AlertDialog.Builder {
        @SuppressLint("InflateParams")
        val root = layoutInflater.inflate(R.layout.pdf_properties_dialog, null)

        root.find(R.id.file_name).text = this@PdfToolBar.fileName?.ifBlank { "-" } ?: "-"
        root.find(R.id.file_size).text = properties.fileSize.formatToSize()
        root.find(R.id.title).text = properties.title
        root.find(R.id.subject).text = properties.subject
        root.find(R.id.author).text = properties.author
        root.find(R.id.creator).text = properties.creator
        root.find(R.id.producer).text = properties.producer
        root.find(R.id.creation_date).text = properties.creationDate.formatToDate()
        root.find(R.id.modified_date).text = properties.modifiedDate.formatToDate()
        root.find(R.id.keywords).text = properties.keywords
        root.find(R.id.language).text = properties.language
        root.find(R.id.pdf_format_version).text = properties.pdfFormatVersion
        root.find(R.id.is_linearized).text = properties.isLinearized.toString()

        return setView(root)
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun View.find(id: Int): TextView {
    return findViewById(id)
}

private fun popup(
    view: View,
    backgroundColor: Int,
    content: LinearLayout.(dismiss: () -> Unit) -> Unit
) {
    val popup = PopupWindow(view.context)
    popup.contentView = LinearLayout(view.context).apply {
        setBackgroundResource(R.drawable.pdf_popup_bg)
        setBgTintModes(backgroundColor)
        orientation = LinearLayout.VERTICAL
        val paddingValue = context.dpToPx(12)
        setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
        content(popup::dismiss)
    }
    popup.isOutsideTouchable = true
    popup.setBackgroundDrawable(null)
    popup.showAsDropDown(view)
}

private fun onSeekBarChangeListener(callback: (newProgress: Int) -> Unit) =
    object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(
            seekBar: SeekBar?,
            progress: Int,
            fromUser: Boolean
        ) {
            callback(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

