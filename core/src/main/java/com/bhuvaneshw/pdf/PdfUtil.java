package com.bhuvaneshw.pdf;

import org.jetbrains.annotations.Nullable;

import kotlin.Unit;

public class PdfUtil {

    public static void onReady(PdfViewer viewer, PdfOnReadyListener listener) {
        viewer.onReady(viewer1 -> {
            listener.onReady();
            return Unit.INSTANCE;
        });
    }

    public static void getActualScaleFor(PdfViewer viewer, PdfOnGetScaleListener listener) {
        viewer.getActualScaleFor(PdfViewer.Zoom.ACTUAL_SIZE, scale -> {
            listener.onValue(scale);
            return Unit.INSTANCE;
        });

    }

    public interface PdfOnReadyListener {
        void onReady();
    }

    public interface PdfOnGetScaleListener {
        void onValue(@Nullable Float scale);
    }
}

