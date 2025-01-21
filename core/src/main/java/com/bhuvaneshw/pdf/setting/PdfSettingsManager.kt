package com.bhuvaneshw.pdf.setting

import com.bhuvaneshw.pdf.PdfOnPageLoadSuccess
import com.bhuvaneshw.pdf.PdfUnstableApi
import com.bhuvaneshw.pdf.PdfViewer

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
                PdfViewer.Zoom.entries.find { it.value == currentPageScaleValue }?.let {
                    save(defaultScaleIncluded, KEY_DEFAULT_SCALE, it.floatValue)
                } ?: save(defaultScaleIncluded, KEY_DEFAULT_SCALE, currentPageScale)

                save(scrollModeIncluded, KEY_SCROLL_MODE, pageScrollMode)
                save(spreadModeIncluded, KEY_SPREAD_MODE, pageSpreadMode)
                save(rotationIncluded, KEY_ROTATION, pageRotation)

                save(snapPageIncluded, KEY_SNAP_PAGE, snapPage)

                save(alignModeIncluded, KEY_ALIGN_MODE, pageAlignMode)
                save(
                    singlePageArrangementIncluded,
                    KEY_SINGLE_PAGE_ARRANGEMENT,
                    singlePageArrangement
                )
                @OptIn(PdfUnstableApi::class)
                save(scrollSpeedLimitIncluded, scrollSpeedLimit)

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

                    singlePageArrangement =
                        getBoolean(KEY_SINGLE_PAGE_ARRANGEMENT, singlePageArrangement)

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
        value: PdfViewer.ScrollSpeedLimit
    ) {
        if (included) when (value) {
            is PdfViewer.ScrollSpeedLimit.AdaptiveFling -> {
                save(KEY_SCROLL_SPEED_LIMIT, value.limit)
                save(KEY_FLING_THRESHOLD, value.flingThreshold)
                save(KEY_FLING_ON_ZOOMED_IN, 2)
            }

            is PdfViewer.ScrollSpeedLimit.Fixed -> {
                save(KEY_SCROLL_SPEED_LIMIT, value.limit)
                save(KEY_FLING_THRESHOLD, value.flingThreshold)
                save(KEY_FLING_ON_ZOOMED_IN, if (value.canFling) 1 else 0)
            }

            PdfViewer.ScrollSpeedLimit.None -> {
                save(KEY_SCROLL_SPEED_LIMIT, -1f)
                save(KEY_FLING_THRESHOLD, -1f)
                save(KEY_FLING_ON_ZOOMED_IN, -1)
            }

        }
        else {
            remove(KEY_SCROLL_SPEED_LIMIT)
            remove(KEY_FLING_THRESHOLD)
        }
    }

    private fun PdfSettingsSaver.getScrollSpeedLimit(): PdfViewer.ScrollSpeedLimit {
        val limit = getFloat(KEY_SCROLL_SPEED_LIMIT, -1f)
        val flingThreshold = getFloat(KEY_FLING_THRESHOLD, -1f)
        val flingOnZoomedIn = getInt(KEY_FLING_ON_ZOOMED_IN, -1)

        return if (limit > 0f && flingThreshold > 0f) {
            if (flingOnZoomedIn == 2)
                PdfViewer.ScrollSpeedLimit.AdaptiveFling(limit, flingThreshold)
            else PdfViewer.ScrollSpeedLimit.Fixed(limit, flingThreshold, flingOnZoomedIn == 1)
        } else PdfViewer.ScrollSpeedLimit.None
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
        private const val KEY_FLING_THRESHOLD = "fling_threshold"
        private const val KEY_FLING_ON_ZOOMED_IN = "fling_on_zoomed_in"
    }
}
