package com.example.notecalc.pdf;

import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import com.example.notecalc.AppUtils;
import com.example.notecalc.Record;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfTableHelper {

    public static void drawRecordTable(PdfDocument document, PdfPageHelper.PdfState state, PdfThemeHelper.PdfTheme theme, List<Record> records, String tableName, double totalAmt, String accountTitle, float colSno, float colDate, float colTime, float colAmount, float colDesc, float rowHeight) {
        float bottomLimit = theme.pageHeight - theme.margin;
        int contentWidth = theme.pageWidth - theme.margin * 2;
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        float amountColEndX = theme.margin + colSno + colDesc + colDate + colTime + colAmount;
        
        if (tableName != null) {
            if (state.y + 50f > bottomLimit) {
                PdfPageHelper.startNewPage(document, state, theme);
            }
            state.y += 10f;
            state.canvas.drawText(tableName, theme.margin, state.y + 15f, theme.titlePaint);
            state.y += 25f;
        }

        state.canvas.drawRect(theme.margin, state.y, theme.pageWidth - theme.margin, state.y + rowHeight, theme.tableHeaderBgPaint);
        state.canvas.drawText("S.No",        theme.margin + 4,                       state.y + 15f, theme.accentPaint);
        state.canvas.drawText("Description", theme.margin + colSno + 4,              state.y + 15f, theme.accentPaint);
        state.canvas.drawText("Date",        theme.margin + colSno + colDesc + 4,    state.y + 15f, theme.accentPaint);
        state.canvas.drawText("Time",        theme.margin + colSno + colDesc + colDate + 4, state.y + 15f, theme.accentPaint);
        float amountHeaderX = amountColEndX - theme.accentPaint.measureText("Amount") - 4f;
        state.canvas.drawText("Amount",      amountHeaderX,                state.y + 15f, theme.accentPaint);
        state.y += rowHeight;
        state.canvas.drawLine(theme.margin, state.y, theme.pageWidth - theme.margin, state.y, theme.dividerPaint);

        for (int i = 0; i < records.size(); i++) {
            Record tmpRec = records.get(i);
            String tRem = tmpRec.getRemarks();
            String tCat = tmpRec.getCategory();
            String tCombined = "";
            if (tCat != null && !tCat.isEmpty()) tCombined += "[" + tCat + "] ";
            if (tRem != null && !tRem.isEmpty()) tCombined += tRem;
            boolean tHasRem = !tCombined.isEmpty();
            List<String> tAtt = tmpRec.getAttachments();
            int numFiles = (tAtt != null) ? tAtt.size() : 0;
            float actualRowHeight = rowHeight;
            if (tHasRem) actualRowHeight += 14f;
            actualRowHeight += (12f * numFiles);

            if (state.y + actualRowHeight > bottomLimit - 10f) {
                PdfPageHelper.startNewPage(document, state, theme);

                state.canvas.drawText(accountTitle + " (contd.)", theme.margin, state.y + 13f, theme.subPaint);
                state.y += 20f;
                state.canvas.drawLine(theme.margin, state.y + 2f, theme.pageWidth - theme.margin, state.y + 2f, theme.dividerPaint);
                state.y += 10f;

                state.canvas.drawRect(theme.margin, state.y, theme.pageWidth - theme.margin, state.y + rowHeight, theme.tableHeaderBgPaint);
                state.canvas.drawText("S.No",        theme.margin + 4,                       state.y + 15f, theme.accentPaint);
                state.canvas.drawText("Description", theme.margin + colSno + 4,              state.y + 15f, theme.accentPaint);
                state.canvas.drawText("Date",        theme.margin + colSno + colDesc + 4,    state.y + 15f, theme.accentPaint);
                state.canvas.drawText("Time",        theme.margin + colSno + colDesc + colDate + 4, state.y + 15f, theme.accentPaint);
                float ahx = amountColEndX - theme.accentPaint.measureText("Amount") - 4f;
                state.canvas.drawText("Amount",      ahx,                              state.y + 15f, theme.accentPaint);
                state.y += rowHeight;
                state.canvas.drawLine(theme.margin, state.y, theme.pageWidth - theme.margin, state.y, theme.dividerPaint);
            }

            Record rec = records.get(i);
            Paint rowBg = (i % 2 == 0) ? theme.rowEvenPaint : theme.rowOddPaint;

            String recRemarks = rec.getRemarks();
            String cat = rec.getCategory();
            String combinedNotes = "";
            if (cat != null && !cat.isEmpty()) combinedNotes += "[" + cat + "] ";
            if (recRemarks != null && !recRemarks.isEmpty()) combinedNotes += recRemarks;
            boolean hasRemarks = !combinedNotes.isEmpty();
            
            List<String> attachments = rec.getAttachments();
            List<String> fileNames = new ArrayList<>();
            if (attachments != null && !attachments.isEmpty()) {
                for (int j = 0; j < attachments.size(); j++) {
                    String path = attachments.get(j);
                    String fileName = path;
                    int lastSlash = path.lastIndexOf('/');
                    if (lastSlash != -1 && lastSlash < path.length() - 1) fileName = path.substring(lastSlash + 1);
                    fileNames.add(fileName);
                }
            }
            float actualRowHeightCalc = rowHeight;
            if (hasRemarks) actualRowHeightCalc += 14f;
            actualRowHeightCalc += (12f * fileNames.size());

            state.canvas.drawRect(theme.margin, state.y, theme.pageWidth - theme.margin, state.y + actualRowHeightCalc, rowBg);
            state.canvas.drawText(String.valueOf(i + 1), theme.margin + 4, state.y + 15f, theme.cellMutedPaint);

            String desc = rec.getDescription();
            while (desc.length() > 1 && theme.cellPaint.measureText(desc) > colDesc - 8f) {
                desc = desc.substring(0, desc.length() - 1);
            }
            if (!desc.equals(rec.getDescription())) desc += "…";
            state.canvas.drawText(desc, theme.margin + colSno + 4, state.y + 15f, theme.cellPaint);

            float currentY = state.y + 27f;
            if (hasRemarks) {
                String truncRemarks = combinedNotes;
                while (truncRemarks.length() > 1 && theme.cellMutedPaint.measureText(truncRemarks) > colDesc - 8f) {
                    truncRemarks = truncRemarks.substring(0, truncRemarks.length() - 1);
                }
                if (!truncRemarks.equals(combinedNotes)) truncRemarks += "…";
                state.canvas.drawText(truncRemarks, theme.margin + colSno + 4, currentY, theme.cellMutedPaint);
            } else {
                currentY -= 14f;
            }
            currentY += 12f;
            for (String fn : fileNames) {
                String truncFn = "\uD83D\uDCCE " + fn;
                while (truncFn.length() > 1 && theme.cellMutedPaint.measureText(truncFn) > colDesc - 8f) {
                    truncFn = truncFn.substring(0, truncFn.length() - 1);
                }
                if (!truncFn.equals("\uD83D\uDCCE " + fn)) truncFn += "…";
                state.canvas.drawText(truncFn, theme.margin + colSno + 4, currentY, theme.cellMutedPaint);
                currentY += 12f;
            }

            state.canvas.drawText(AppUtils.formatDateCompact(rec.getDate()), theme.margin + colSno + colDesc + 4, state.y + 15f, theme.cellMutedPaint);
            String timeStr = rec.getTimestampMillis() > 0 ? timeSdf.format(new Date(rec.getTimestampMillis())) : "-";
            state.canvas.drawText(timeStr, theme.margin + colSno + colDesc + colDate + 4, state.y + 15f, theme.cellMutedPaint);

            String amtStr = String.format(Locale.getDefault(), "%.2f", rec.getAmount());
            float amtX = amountColEndX - theme.cellPaint.measureText(amtStr) - 4f;
            state.canvas.drawText(amtStr, amtX, state.y + 15f, theme.cellPaint);

            state.y += actualRowHeightCalc;
            state.canvas.drawLine(theme.margin, state.y, theme.pageWidth - theme.margin, state.y, theme.dividerPaint);
        }

        if (state.y + rowHeight + 30f > bottomLimit) {
            PdfPageHelper.startNewPage(document, state, theme);
        }

        state.y += 4f;
        state.canvas.drawRect(theme.margin, state.y, theme.pageWidth - theme.margin, state.y + rowHeight, theme.totalBgPaint);
        String label = tableName == null ? "TOTAL SELECTED" : "TOTAL";
        state.canvas.drawText(label, theme.margin + 4, state.y + 15f, theme.totalTextPaint);
        String totalStr = String.format(Locale.getDefault(), "%.2f", totalAmt);
        float totalX = theme.margin + contentWidth - theme.totalTextPaint.measureText(totalStr) - 4f;
        state.canvas.drawText(totalStr, totalX, state.y + 15f, theme.totalTextPaint);
        state.y += rowHeight + 16f;
    }
}
