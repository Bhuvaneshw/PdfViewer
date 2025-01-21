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
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import com.bhuvaneshw.pdf.PdfListener
import com.bhuvaneshw.pdf.PdfViewer
import kotlin.random.Random

class PdfViewerContainer : RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    var pdfViewer: PdfViewer? = null; private set
    var pdfToolBar: PdfToolBar? = null; private set
    var pdfScrollBar: PdfScrollBar? = null; private set
    var alertDialogBuilder: () -> AlertDialog.Builder = { AlertDialog.Builder(context) }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        when (child) {
            is PdfViewer -> {
                this.pdfViewer = child
                child.addListener(PasswordDialogListener())
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

            pdfViewer?.let { pdfViewer ->
                pdfViewer.ui.passwordDialog.getLabelText { title ->
                    @SuppressLint("InflateParams")
                    val root =
                        LayoutInflater.from(context).inflate(R.layout.pdf_go_to_page_dialog, null)
                    val field: EditText =
                        root.findViewById<EditText?>(R.id.go_to_page_field).apply {
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
