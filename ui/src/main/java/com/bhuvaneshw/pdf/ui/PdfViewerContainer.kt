package com.bhuvaneshw.pdf.ui

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bhuvaneshw.pdf.PdfListener
import com.bhuvaneshw.pdf.PdfViewer
import kotlin.random.Random

/**
 * A container view that orchestrates interactions between a [PdfViewer], [PdfToolBar], and [PdfScrollBar].
 *
 * It manages the display of a PDF viewer, a toolbar, a scrollbar, loading indicators, and dialogs for passwords and printing.
 *
 * @see com.bhuvaneshw.pdf.PdfViewer
 * @see PdfToolBar
 * @see PdfScrollBar
 */
class PdfViewerContainer : RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    /**
     * The [PdfViewer] instance contained within this layout. It is automatically detected when added as a child.
     * @see com.bhuvaneshw.pdf.PdfViewer
     */
    var pdfViewer: PdfViewer? = null; private set

    /**
     * The [PdfToolBar] instance for this container. It is automatically detected when added as a child.
     */
    var pdfToolBar: PdfToolBar? = null; private set

    /**
     * The [PdfScrollBar] instance for this container. It is automatically detected when added as a child.
     */
    var pdfScrollBar: PdfScrollBar? = null; private set

    /**
     * A builder for creating [AlertDialog] instances used for password prompts and print dialogs.
     * This allows for customization of the dialogs' appearance.
     */
    var alertDialogBuilder: () -> AlertDialog.Builder = { AlertDialog.Builder(context) }

    /**
     * Determines whether the password dialog is shown when a protected PDF is loaded.
     * Defaults to `true`.
     * @see com.bhuvaneshw.pdf.PdfListener.onPasswordDialogChange
     */
    var passwordDialogEnabled: Boolean = true

    /**
     * Determines whether the print dialog is shown when printing is initiated.
     * Defaults to `true`.
     * @see com.bhuvaneshw.pdf.PdfListener.onPrintProcessStart
     */
    var printDialogEnabled: Boolean = true

    /**
     * Adds a child view. This method is overridden to automatically detect and configure
     * [PdfViewer], [PdfToolBar], and [PdfScrollBar] children.
     */
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        when (child) {
            is PdfViewer -> {
                this.pdfViewer = child
                child.addListener(PasswordDialogListener())
                child.addListener(PrintDialogListener())
                setup()

                pdfToolBar?.let { toolBar ->
                    super.addView(child, index, params.apply {
                        if (this is LayoutParams) {
                            addRule(BELOW, toolBar.id)
                        }
                    })
                } ?: super.addView(child, index, params)
            }

            is PdfScrollBar -> {
                this.pdfScrollBar = child
                setup()

                super.addView(child, index, params.apply {
                    if (this is LayoutParams) {
                        addRule(ALIGN_PARENT_END)
                        if (isInEditMode)
                            pdfToolBar?.id?.let { addRule(BELOW, it) }
                        child.addScrollModeChangeListener { isHorizontal ->
                            if (isHorizontal) {
                                addRule(ALIGN_PARENT_BOTTOM)
                                removeRule(ALIGN_PARENT_END)
                            } else {
                                addRule(ALIGN_PARENT_END)
                                removeRule(ALIGN_PARENT_BOTTOM)
                            }
                        }
                    }
                })
            }

            is PdfToolBar -> {
                this.pdfToolBar = child.apply {
                    if (id == NO_ID) id = Random.nextInt()
                }
                super.addView(child, index, params)
                setup()

                context?.let {
                    if (it is Activity)
                        child.onBack = it::finish
                }
            }

            else -> super.addView(child, index, params)
        }
    }

    /**
     * Designates a view to be shown as a loading indicator while a PDF is loading.
     *
     * @param view The view to show and hide based on the loading state.
     * @see com.bhuvaneshw.pdf.PdfListener.onPageLoadStart
     * @see com.bhuvaneshw.pdf.PdfListener.onPageLoadSuccess
     */
    fun setAsLoadingIndicator(view: View) {
        pdfViewer?.addListener(object : PdfListener {
            override fun onPageLoadStart() {
                view.visibility = VISIBLE
            }

            override fun onPageLoadSuccess(pagesCount: Int) {
                view.visibility = GONE
            }
        })
    }

    private fun setup() {
        pdfViewer?.let { viewer ->
            pdfToolBar?.setupWith(viewer)
            pdfScrollBar?.setupWith(viewer, pdfToolBar)
        }
    }

    @Suppress("NOTHING_TO_INLINE", "FunctionName")
    private inline fun PasswordDialogListener() = object : PdfListener {
        var dialog: AlertDialog? = null

        override fun onPasswordDialogChange(isOpen: Boolean) {
            if (!isOpen) {
                dialog?.dismiss()
                dialog = null
                return
            }
            if (!passwordDialogEnabled) return

            pdfViewer?.let { pdfViewer ->
                pdfViewer.ui.passwordDialog.getLabelText { title ->
                    @SuppressLint("InflateParams")
                    val root =
                        LayoutInflater.from(context).inflate(R.layout.pdf_go_to_page_dialog, null)
                    val field: EditText =
                        root.findViewById<EditText>(R.id.go_to_page_field).apply {
                            inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            imeOptions = EditorInfo.IME_ACTION_DONE
                            hint = "Password"
                        }

                    val submitPassword: (String, DialogInterface) -> Unit = { password, dialog ->
                        if (password.isEmpty()) onPasswordDialogChange(true)
                        else pdfViewer.ui.passwordDialog.submitPassword(password)
                        dialog.dismiss()
                    }

                    dialog = alertDialogBuilder()
                        .setTitle(title?.replace("\"", ""))
                        .setView(root)
                        .setPositiveButton("Done") { dialog, _ ->
                            submitPassword(field.text.toString(), dialog)
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            pdfViewer.ui.passwordDialog.cancel()
                            dialog.dismiss()
                        }
                        .create()

                    field.setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            dialog?.let { submitPassword(field.text.toString(), it) }
                            true
                        } else {
                            false
                        }
                    }
                    dialog?.show()
                }
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE", "FunctionName")
    private inline fun PrintDialogListener() = object : PdfListener {
        var dialog: AlertDialog? = null
        var progressBar: ProgressBar? = null
        var progressText: TextView? = null

        override fun onPrintProcessStart() {
            if (printDialogEnabled) {
                pdfViewer?.let { pdfViewer ->
                    @SuppressLint("InflateParams")
                    val root = LayoutInflater.from(context).inflate(R.layout.pdf_print_dialog, null)
                    progressBar = root.findViewById(R.id.progress)
                    progressText = root.findViewById(R.id.progress_text)

                    dialog = alertDialogBuilder()
                        .setTitle("Preparing to print…")
                        .setView(root)
                        .setNegativeButton("Cancel") { dialog, _ ->
                            pdfViewer.ui.printDialog.cancel()
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onPrintProcessProgress(progress: Float) {
            progressBar?.let {
                if (it.isIndeterminate)
                    it.isIndeterminate = false
                it.progress = (progress * 100).toInt()
            }
            progressText?.text = "${(progress * 100).toInt()}%"
        }

        override fun onPrintProcessEnd() {
            dismissAndClear()
        }

        override fun onPrintCancelled() {
            dismissAndClear()
        }

        @Suppress("NOTHING_TO_INLINE")
        private inline fun dismissAndClear() {
            dialog?.dismiss()
            progressBar = null
            progressText = null
            dialog = null
        }
    }

    /**
     * Immediately shows the [PdfToolBar] if it exists.
     */
    fun showToolBar() {
        pdfToolBar?.let {
            pdfViewer?.let { viewer ->
                pdfScrollBar?.setupWith(viewer, pdfToolBar, true)
            }

            it.translationY = 0f
            pdfViewer?.layoutParams.let { params ->
                if (params is LayoutParams) {
                    params.addRule(BELOW, it.id)
                    pdfViewer?.requestLayout()
                }
            }
        }
    }

    /**
     * Immediately hides the [PdfToolBar] if it exists.
     */
    fun hideToolBar() {
        pdfToolBar?.let {
            pdfViewer?.let { viewer ->
                pdfScrollBar?.setupWith(viewer, null, true)
            }

            it.translationY = -it.height.toFloat()
            pdfViewer?.layoutParams.let { params ->
                if (params is LayoutParams) {
                    params.removeRule(BELOW)
                    pdfViewer?.requestLayout()
                }
            }
        }
    }

    /**
     * Animates the showing or hiding of the [PdfToolBar].
     *
     * @param show `true` to show the toolbar, `false` to hide it.
     * @param animDuration The duration of the animation in milliseconds.
     * @param onEnd A callback to invoke when the animation ends.
     */
    fun animateToolBar(
        show: Boolean,
        animDuration: Long = 150L,
        onEnd: (() -> Unit)? = null
    ) {
        pdfToolBar?.let { toolBar ->
            pdfViewer?.let { viewer ->
                pdfScrollBar?.setupWith(viewer, if (show) pdfToolBar else null, true)
            }

            toolBar.animate()
                .translationY(if (show) 0f else -height.toFloat())
                .setDuration(animDuration)
                .start()

            pdfViewer?.layoutParams.let { params ->
                if (params is LayoutParams) {
                    pdfViewer?.animate()
                        ?.translationY(if (show) toolBar.height.toFloat() else -toolBar.height.toFloat())
                        ?.setDuration(animDuration)
                        ?.onAnimateEnd {
                            if (show) params.addRule(BELOW, toolBar.id)
                            else params.removeRule(BELOW)
                            pdfViewer?.requestLayout()
                            pdfViewer?.translationY = 0f
                            onEnd?.invoke()
                        }
                        ?.start()
                }
            }
        }
    }

}

private fun ViewPropertyAnimator.onAnimateEnd(onEnd: () -> Unit): ViewPropertyAnimator {
    return this.setListener(object : AnimatorListener {
        override fun onAnimationStart(animation: Animator) {}
        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
        override fun onAnimationEnd(animation: Animator) {
            onEnd()
        }
    })
}
