package com.acutecoder.pdf.setting

import com.acutecoder.pdf.PdfOnPageLoadSuccess
import com.acutecoder.pdf.PdfUnstableApi
import com.acutecoder.pdf.PdfViewer

class PdfSettingsManager(private val saver: PdfSettingsSaver) {

    var currentPageIncluded = false
    var minScaleIncluded = true
    var maxScaleIncluded = true
    var defaultScaleIncluded = true
    var scrollModeIncluded = true
    var spreadModeIncluded = true
    var rotationIncluded = true
    var snapPageIncluded = true

    // Unstable apis are excluded by default
    var alignModeIncluded = false
    var singlePageArrangementIncluded = false
    var scrollSpeedLimitIncluded = false

    fun includeAll() {
        currentPageIncluded = true
        minScaleIncluded = true
        maxScaleIncluded = true
        defaultScaleIncluded = true
        scrollModeIncluded = true
        spreadModeIncluded = true
        rotationIncluded = true
        snapPageIncluded = true

        alignModeIncluded = true
        singlePageArrangementIncluded = true
        scrollSpeedLimitIncluded = true
    }

    fun excludeAll() {
        currentPageIncluded = false
        minScaleIncluded = false
        maxScaleIncluded = false
        defaultScaleIncluded = false
        scrollModeIncluded = false
        spreadModeIncluded = false
        rotationIncluded = false
        snapPageIncluded = false

        alignModeIncluded = false
        singlePageArrangementIncluded = false
        scrollSpeedLimitIncluded = false
    }

    fun save(pdfViewer: PdfViewer) {
        saver.run {
            pdfViewer.run {
                save(currentPageIncluded, KEY_CURRENT_PAGE, currentPage)

                save(minScaleIncluded, KEY_MIN_SCALE, minPageScale)
                save(maxScaleIncluded, KEY_MAX_SCALE, maxPageScale)
                save(defaultScaleIncluded, KEY_DEFAULT_SCALE, currentPageScale)

                save(scrollModeIncluded, KEY_SCROLL_MODE, pageScrollMode)
                save(spreadModeIncluded, KEY_SPREAD_MODE, pageSpreadMode)
                save(rotationIncluded, KEY_ROTATION, pageRotation)

                save(snapPageIncluded, KEY_SNAP_PAGE, snapPage)

                @OptIn(PdfUnstableApi::class)
                save(alignModeIncluded, KEY_ALIGN_MODE, pageAlignMode)
                @OptIn(PdfUnstableApi::class)
                save(
                    singlePageArrangementIncluded,
                    KEY_SINGLE_PAGE_ARRANGEMENT,
                    singlePageArrangement
                )
                @OptIn(PdfUnstableApi::class)
                save(scrollSpeedLimitIncluded, KEY_SCROLL_SPEED_LIMIT, scrollSpeedLimit)

                apply()
            }
        }
    }

    fun restore(pdfViewer: PdfViewer) {
        saver.run {
            pdfViewer.run {
                addListener(PdfOnPageLoadSuccess { pagesCount ->
                    val currentPage = getInt(KEY_CURRENT_PAGE, -1)
                    if (currentPage != -1 && currentPage <= pagesCount)
                        goToPage(currentPage)

                    @OptIn(PdfUnstableApi::class)
                    singlePageArrangement =
                        getBoolean(KEY_SINGLE_PAGE_ARRANGEMENT, singlePageArrangement)

                    @OptIn(PdfUnstableApi::class)
                    pageAlignMode = getEnum(KEY_ALIGN_MODE, pageAlignMode)

                    @OptIn(PdfUnstableApi::class)
                    scrollSpeedLimit = getScrollSpeedLimit()
                })

                minPageScale = getFloat(KEY_MIN_SCALE, minPageScale)
                maxPageScale = getFloat(KEY_MAX_SCALE, maxPageScale)
                defaultPageScale = getFloat(KEY_DEFAULT_SCALE, defaultPageScale)

                pageScrollMode = getEnum(KEY_SCROLL_MODE, pageScrollMode)
                pageSpreadMode = getEnum(KEY_SPREAD_MODE, pageSpreadMode)
                pageRotation = getEnum(KEY_ROTATION, pageRotation)

                snapPage = getBoolean(KEY_SNAP_PAGE, snapPage)
            }
        }
    }

    fun reset() {
        saver.clearAll()
        saver.apply()
    }

    private fun PdfSettingsSaver.save(included: Boolean, key: String, value: Int) {
        if (included) save(key, value)
        else remove(key)
    }

    private fun PdfSettingsSaver.save(included: Boolean, key: String, value: Float) {
        if (included) save(key, value)
        else remove(key)
    }

    private fun PdfSettingsSaver.save(included: Boolean, key: String, value: Boolean) {
        if (included) save(key, value)
        else remove(key)
    }

    private fun <T : Enum<T>> PdfSettingsSaver.save(included: Boolean, key: String, value: T) {
        if (included) save(key, value.name)
        else remove(key)
    }

    private fun PdfSettingsSaver.save(
        included: Boolean,
        key: String,
        value: PdfViewer.ScrollSpeedLimit
    ) {
        if (included) save(
            key, when (value) {
                is PdfViewer.ScrollSpeedLimit.Fixed -> value.limit
                PdfViewer.ScrollSpeedLimit.None -> -1f
            }
        )
        else remove(key)
    }

    private fun PdfSettingsSaver.getScrollSpeedLimit(): PdfViewer.ScrollSpeedLimit {
        val limit = getFloat(KEY_SCROLL_SPEED_LIMIT, -1f)
        return if (limit > 0f) PdfViewer.ScrollSpeedLimit(limit)
        else PdfViewer.ScrollSpeedLimit()
    }

    companion object {
        private const val KEY_CURRENT_PAGE = "current_page"
        private const val KEY_MIN_SCALE = "min_scale"
        private const val KEY_MAX_SCALE = "max_scale"
        private const val KEY_DEFAULT_SCALE = "default_scale"
        private const val KEY_SCROLL_MODE = "scroll_mode"
        private const val KEY_SPREAD_MODE = "spread_mode"
        private const val KEY_ROTATION = "rotation"
        private const val KEY_SNAP_PAGE = "snap_page"
        private const val KEY_ALIGN_MODE = "center_align"
        private const val KEY_SINGLE_PAGE_ARRANGEMENT = "single_arrangement"
        private const val KEY_SCROLL_SPEED_LIMIT = "scroll_speed_limit"
    }
}
