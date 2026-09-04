package com.example.notecalc.pdf;

import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;

public class PdfPageHelper {

    public static class PdfState {
        public PdfDocument.Page page;
        public Canvas canvas;
        public float y;
        public int pageNum;
    }

    public static void startNewPage(PdfDocument document, PdfState state, PdfThemeHelper.PdfTheme theme) {
        if (state.page != null) {
            document.finishPage(state.page);
        }
        state.pageNum++;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(theme.pageWidth, theme.pageHeight, state.pageNum).create();
        state.page = document.startPage(pageInfo);
        state.canvas = state.page.getCanvas();
        state.canvas.drawRect(0, 0, theme.pageWidth, theme.pageHeight, theme.bgPaint);
        state.y = theme.margin;
    }
}
