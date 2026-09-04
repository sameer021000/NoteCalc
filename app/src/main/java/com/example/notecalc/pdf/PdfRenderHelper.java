package com.example.notecalc.pdf;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import com.example.notecalc.Account;
import com.example.notecalc.AppUtils;
import com.example.notecalc.MainActivity;
import com.example.notecalc.Record;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class PdfRenderHelper {

    public static void appendAccountToPdf(MainActivity activity, PdfDocument document, Account account, int[] pageTracker, PdfSortOrder sortOrder) {
        PdfThemeHelper.PdfTheme theme = new PdfThemeHelper.PdfTheme(activity);
        
        int contentWidth = theme.pageWidth - theme.margin * 2;
        float bottomLimit = theme.pageHeight - theme.margin;

        float colSno    = 38f;
        float colDate   = 70f;
        float colTime   = 55f;
        float colAmount = 60f;
        float colDesc   = contentWidth - colSno - colDate - colTime - colAmount;
        float rowHeight = 22f;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String lastMod = sdf.format(new Date(account.getLastModified()));
        
        List<Record> expRecords = new ArrayList<>(account.getRecords());
        expRecords.sort((r1, r2) -> {
            if (sortOrder == PdfSortOrder.DESCRIPTION) {
                String d1 = r1.getDescription() == null ? "" : r1.getDescription();
                String d2 = r2.getDescription() == null ? "" : r2.getDescription();
                return d1.compareToIgnoreCase(d2);
            }
            if (sortOrder == PdfSortOrder.DATE) {
                try {
                    SimpleDateFormat sortSdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date d1 = r1.getDate() == null ? null : sortSdf.parse(r1.getDate());
                    Date d2 = r2.getDate() == null ? null : sortSdf.parse(r2.getDate());
                    int c = (d1 != null && d2 != null) ? d1.compareTo(d2) : 0;
                    if (c == 0) c = Long.compare(r1.getTimestampMillis(), r2.getTimestampMillis());
                    return c;
                } catch (Exception e) {
                    String d1 = r1.getDate() == null ? "" : r1.getDate();
                    String d2 = r2.getDate() == null ? "" : r2.getDate();
                    return d1.compareTo(d2);
                }
            }
            if (sortOrder == PdfSortOrder.AMOUNT) return Double.compare(r1.getAmount(), r2.getAmount());
            return Integer.compare(r1.getOriginalIndex(), r2.getOriginalIndex());
        });
        
        List<Record> budRecords = new ArrayList<>(account.getBudgetRecords());
        budRecords.sort((r1, r2) -> {
            if (sortOrder == PdfSortOrder.DESCRIPTION) {
                String d1 = r1.getDescription() == null ? "" : r1.getDescription();
                String d2 = r2.getDescription() == null ? "" : r2.getDescription();
                return d1.compareToIgnoreCase(d2);
            }
            if (sortOrder == PdfSortOrder.DATE) {
                try {
                    SimpleDateFormat sortSdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date d1 = r1.getDate() == null ? null : sortSdf.parse(r1.getDate());
                    Date d2 = r2.getDate() == null ? null : sortSdf.parse(r2.getDate());
                    int c = (d1 != null && d2 != null) ? d1.compareTo(d2) : 0;
                    if (c == 0) c = Long.compare(r1.getTimestampMillis(), r2.getTimestampMillis());
                    return c;
                } catch (Exception e) {
                    String d1 = r1.getDate() == null ? "" : r1.getDate();
                    String d2 = r2.getDate() == null ? "" : r2.getDate();
                    return d1.compareTo(d2);
                }
            }
            if (sortOrder == PdfSortOrder.AMOUNT) return Double.compare(r1.getAmount(), r2.getAmount());
            return Integer.compare(r1.getOriginalIndex(), r2.getOriginalIndex());
        });
        
        List<List<Record>> allRecordLists = new ArrayList<>();
        List<String> listNames = new ArrayList<>();
        List<Double> listTotals = new ArrayList<>();
        
        if (!budRecords.isEmpty()) {
            allRecordLists.add(budRecords);
            listNames.add("Budgets");
            double bt = 0; for(Record r: budRecords) bt += r.getAmount();
            listTotals.add(bt);
        }
        if (!expRecords.isEmpty() || budRecords.isEmpty()) {
            allRecordLists.add(expRecords);
            listNames.add(budRecords.isEmpty() ? null : "Expenses");
            listTotals.add(account.calculateTotal());
        }

        int pageNum = pageTracker[0];
        Canvas canvas;
        PdfDocument.Page page;
        float y;

        pageNum++;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(theme.pageWidth, theme.pageHeight, pageNum).create();
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        canvas.drawRect(0, 0, theme.pageWidth, theme.pageHeight, theme.bgPaint);
        y = theme.margin;

        String titleText = account.getTitle();
        float titleLineHeight = 28f;
        float maxTitleWidth = contentWidth - 15f;
        List<String> titleLines = AppUtils.wrapText(titleText, theme.titlePaint, maxTitleWidth);
        for (String line : titleLines) {
            canvas.drawText(line, theme.margin, y + 22f, theme.titlePaint);
            y += titleLineHeight;
        }
        y += 4f;

        String subtitle = "Last modified: " + lastMod + "  |  Items: " + (expRecords.size() + budRecords.size());
        double budget = account.calculateTotalBudget();
        if (budget > 0) {
            subtitle += "  |  Budget: " + String.format(Locale.getDefault(), "%.2f", budget);
        }
        canvas.drawText(subtitle, theme.margin, y + 13f, theme.subPaint);
        y += 20f;

        canvas.drawLine(theme.margin, y + 4f, theme.pageWidth - theme.margin, y + 4f, theme.dividerPaint);
        y += 16f;

        for (int listIdx = 0; listIdx < allRecordLists.size(); listIdx++) {
            List<Record> records = allRecordLists.get(listIdx);
            String tableName = listNames.get(listIdx);
            double totalAmt = listTotals.get(listIdx);
            
            if (tableName != null) {
                if (y + 50f > bottomLimit) {
                    document.finishPage(page);
                    pageNum++;
                    pageInfo = new PdfDocument.PageInfo.Builder(theme.pageWidth, theme.pageHeight, pageNum).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    canvas.drawRect(0, 0, theme.pageWidth, theme.pageHeight, theme.bgPaint);
                    y = theme.margin;
                }
                y += 10f;
                canvas.drawText(tableName, theme.margin, y + 15f, theme.titlePaint);
                y += 25f;
            }

            canvas.drawRect(theme.margin, y, theme.pageWidth - theme.margin, y + rowHeight, theme.tableHeaderBgPaint);
            canvas.drawText("S.No",        theme.margin + 4,                       y + 15f, theme.accentPaint);
            canvas.drawText("Description", theme.margin + colSno + 4,              y + 15f, theme.accentPaint);
            canvas.drawText("Date",        theme.margin + colSno + colDesc + 4,    y + 15f, theme.accentPaint);
            canvas.drawText("Time",        theme.margin + colSno + colDesc + colDate + 4, y + 15f, theme.accentPaint);
            float amountHeaderX = theme.margin + colSno + colDesc + colDate + colTime + colAmount - theme.accentPaint.measureText("Amount") - 4f;
            canvas.drawText("Amount",      amountHeaderX,                y + 15f, theme.accentPaint);
            y += rowHeight;
            canvas.drawLine(theme.margin, y, theme.pageWidth - theme.margin, y, theme.dividerPaint);

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

                if (y + actualRowHeight > bottomLimit - 10f) {
                    document.finishPage(page);
                    pageNum++;
                    pageInfo = new PdfDocument.PageInfo.Builder(theme.pageWidth, theme.pageHeight, pageNum).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    canvas.drawRect(0, 0, theme.pageWidth, theme.pageHeight, theme.bgPaint);
                    y = theme.margin;

                    canvas.drawText(account.getTitle() + " (contd.)", theme.margin, y + 13f, theme.subPaint);
                    y += 20f;
                    canvas.drawLine(theme.margin, y + 2f, theme.pageWidth - theme.margin, y + 2f, theme.dividerPaint);
                    y += 10f;

                    canvas.drawRect(theme.margin, y, theme.pageWidth - theme.margin, y + rowHeight, theme.tableHeaderBgPaint);
                    canvas.drawText("S.No",        theme.margin + 4,                       y + 15f, theme.accentPaint);
                    canvas.drawText("Description", theme.margin + colSno + 4,              y + 15f, theme.accentPaint);
                    canvas.drawText("Date",        theme.margin + colSno + colDesc + 4,    y + 15f, theme.accentPaint);
                    canvas.drawText("Time",        theme.margin + colSno + colDesc + colDate + 4, y + 15f, theme.accentPaint);
                    float ahx = theme.margin + colSno + colDesc + colDate + colTime + colAmount - theme.accentPaint.measureText("Amount") - 4f;
                    canvas.drawText("Amount",      ahx,                              y + 15f, theme.accentPaint);
                    y += rowHeight;
                    canvas.drawLine(theme.margin, y, theme.pageWidth - theme.margin, y, theme.dividerPaint);
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

                canvas.drawRect(theme.margin, y, theme.pageWidth - theme.margin, y + actualRowHeightCalc, rowBg);
                canvas.drawText(String.valueOf(i + 1), theme.margin + 4, y + 15f, theme.cellMutedPaint);

                String desc = rec.getDescription();
                while (desc.length() > 1 && theme.cellPaint.measureText(desc) > colDesc - 8f) {
                    desc = desc.substring(0, desc.length() - 1);
                }
                if (!desc.equals(rec.getDescription())) desc += "…";
                canvas.drawText(desc, theme.margin + colSno + 4, y + 15f, theme.cellPaint);

                float currentY = y + 27f;
                if (hasRemarks) {
                    String truncRemarks = combinedNotes;
                    while (truncRemarks.length() > 1 && theme.cellMutedPaint.measureText(truncRemarks) > colDesc - 8f) {
                        truncRemarks = truncRemarks.substring(0, truncRemarks.length() - 1);
                    }
                    if (!truncRemarks.equals(combinedNotes)) truncRemarks += "…";
                    canvas.drawText(truncRemarks, theme.margin + colSno + 4, currentY, theme.cellMutedPaint);
                    currentY += 12f;
                } else {
                    currentY -= 14f;
                    currentY += 12f;
                }
                for (String fn : fileNames) {
                    String truncFn = "\uD83D\uDCCE " + fn;
                    while (truncFn.length() > 1 && theme.cellMutedPaint.measureText(truncFn) > colDesc - 8f) {
                        truncFn = truncFn.substring(0, truncFn.length() - 1);
                    }
                    if (!truncFn.equals("\uD83D\uDCCE " + fn)) truncFn += "…";
                    canvas.drawText(truncFn, theme.margin + colSno + 4, currentY, theme.cellMutedPaint);
                    currentY += 12f;
                }

                canvas.drawText(AppUtils.formatDateCompact(rec.getDate()), theme.margin + colSno + colDesc + 4, y + 15f, theme.cellMutedPaint);
                String timeStr = rec.getTimestampMillis() > 0 ? timeSdf.format(new Date(rec.getTimestampMillis())) : "-";
                canvas.drawText(timeStr, theme.margin + colSno + colDesc + colDate + 4, y + 15f, theme.cellMutedPaint);

                String amtStr = String.format(Locale.getDefault(), "%.2f", rec.getAmount());
                float amtX = theme.margin + colSno + colDesc + colDate + colTime + colAmount - theme.cellPaint.measureText(amtStr) - 4f;
                canvas.drawText(amtStr, amtX, y + 15f, theme.cellPaint);

                y += actualRowHeightCalc;
                canvas.drawLine(theme.margin, y, theme.pageWidth - theme.margin, y, theme.dividerPaint);
            }

            if (y + rowHeight + 30f > bottomLimit) {
                document.finishPage(page);
                pageNum++;
                pageInfo = new PdfDocument.PageInfo.Builder(theme.pageWidth, theme.pageHeight, pageNum).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                canvas.drawRect(0, 0, theme.pageWidth, theme.pageHeight, theme.bgPaint);
                y = theme.margin;
            }

            y += 4f;
            canvas.drawRect(theme.margin, y, theme.pageWidth - theme.margin, y + rowHeight, theme.totalBgPaint);
            canvas.drawText("TOTAL", theme.margin + 4, y + 15f, theme.totalTextPaint);
            String totalStr = String.format(Locale.getDefault(), "%.2f", totalAmt);
            float totalX = theme.margin + contentWidth - theme.totalTextPaint.measureText(totalStr) - 4f;
            canvas.drawText(totalStr, totalX, y + 15f, theme.totalTextPaint);
            y += rowHeight + 16f;
        }

        if (y + 20f > bottomLimit) {
            document.finishPage(page);
            pageNum++;
            PdfDocument.PageInfo pi = new PdfDocument.PageInfo.Builder(595, 842, pageNum).create();
            page = document.startPage(pi);
            canvas = page.getCanvas();
            canvas.drawRect(0, 0, 595, 842, theme.bgPaint);
            y = 40f;
        }
        canvas.drawText("Generated by NoteCalc  •  " + lastMod + "  •  Page " + pageNum, 40f, y + 12f, theme.subPaint);

        document.finishPage(page);
        pageTracker[0] = pageNum;

        LinkedHashMap<Record, String> recordLabels = new LinkedHashMap<>();
        for (int listIdx = 0; listIdx < allRecordLists.size(); listIdx++) {
            List<Record> list = allRecordLists.get(listIdx);
            String prefix = listNames.get(listIdx) != null ? listNames.get(listIdx) + " - " : "";
            for (int j = 0; j < list.size(); j++) {
                recordLabels.put(list.get(j), prefix + "S.No " + (j + 1) + ": " + list.get(j).getDescription());
            }
        }
        PdfAppendixHelper.appendAttachmentsAppendixToPdf(activity, document, recordLabels, pageTracker);
    }

    public static void appendSelectedRecordsToPdf(MainActivity activity, PdfDocument document, Account account, List<Record> selectedRecords, int[] pageTracker, PdfSortOrder sortOrder) {
        PdfThemeHelper.PdfTheme theme = new PdfThemeHelper.PdfTheme(activity);

        int contentWidth = theme.pageWidth - theme.margin * 2;
        float bottomLimit = theme.pageHeight - theme.margin;

        float colSno    = 38f;
        float colDate   = 70f;
        float colTime   = 55f;
        float colAmount = 60f;
        float colDesc   = contentWidth - colSno - colDate - colTime - colAmount;
        float rowHeight = 22f;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String lastMod = sdf.format(new Date(account.getLastModified()));
        
        List<Record> recordsToPrint = new ArrayList<>(selectedRecords);
        recordsToPrint.sort((r1, r2) -> {
            if (sortOrder == PdfSortOrder.DESCRIPTION) {
                String d1 = r1.getDescription() == null ? "" : r1.getDescription();
                String d2 = r2.getDescription() == null ? "" : r2.getDescription();
                return d1.compareToIgnoreCase(d2);
            }
            if (sortOrder == PdfSortOrder.DATE) {
                try {
                    SimpleDateFormat sortSdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date d1 = r1.getDate() == null ? null : sortSdf.parse(r1.getDate());
                    Date d2 = r2.getDate() == null ? null : sortSdf.parse(r2.getDate());
                    int c = (d1 != null && d2 != null) ? d1.compareTo(d2) : 0;
                    if (c == 0) c = Long.compare(r1.getTimestampMillis(), r2.getTimestampMillis());
                    return c;
                } catch (Exception e) {
                    String d1 = r1.getDate() == null ? "" : r1.getDate();
                    String d2 = r2.getDate() == null ? "" : r2.getDate();
                    return d1.compareTo(d2);
                }
            }
            if (sortOrder == PdfSortOrder.AMOUNT) return Double.compare(r1.getAmount(), r2.getAmount());
            return Integer.compare(r1.getOriginalIndex(), r2.getOriginalIndex());
        });
        
        double totalAmt = 0;
        for (Record r : recordsToPrint) totalAmt += r.getAmount();

        int pageNum = pageTracker[0];
        Canvas canvas;
        PdfDocument.Page page;
        float y;

        pageNum++;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(theme.pageWidth, theme.pageHeight, pageNum).create();
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        canvas.drawRect(0, 0, theme.pageWidth, theme.pageHeight, theme.bgPaint);
        y = theme.margin;

        String titleText = account.getTitle() + " (Selected)";
        float titleLineHeight = 28f;
        float maxTitleWidth = contentWidth - 15f;
        List<String> titleLines = AppUtils.wrapText(titleText, theme.titlePaint, maxTitleWidth);
        for (String line : titleLines) {
            canvas.drawText(line, theme.margin, y + 22f, theme.titlePaint);
            y += titleLineHeight;
        }
        y += 4f;

        String subtitle = "Exported: " + sdf.format(new Date()) + "  |  Items: " + recordsToPrint.size();
        canvas.drawText(subtitle, theme.margin, y + 13f, theme.subPaint);
        y += 20f;

        canvas.drawLine(theme.margin, y + 4f, theme.pageWidth - theme.margin, y + 4f, theme.dividerPaint);
        y += 16f;

        canvas.drawRect(theme.margin, y, theme.pageWidth - theme.margin, y + rowHeight, theme.tableHeaderBgPaint);
        canvas.drawText("S.No",        theme.margin + 4,                       y + 15f, theme.accentPaint);
        canvas.drawText("Description", theme.margin + colSno + 4,              y + 15f, theme.accentPaint);
        canvas.drawText("Date",        theme.margin + colSno + colDesc + 4,    y + 15f, theme.accentPaint);
        canvas.drawText("Time",        theme.margin + colSno + colDesc + colDate + 4, y + 15f, theme.accentPaint);
        float amountHeaderX = theme.margin + colSno + colDesc + colDate + colTime + colAmount - theme.accentPaint.measureText("Amount") - 4f;
        canvas.drawText("Amount",      amountHeaderX,                y + 15f, theme.accentPaint);
        y += rowHeight;
        canvas.drawLine(theme.margin, y, theme.pageWidth - theme.margin, y, theme.dividerPaint);

        for (int i = 0; i < recordsToPrint.size(); i++) {
            Record tmpRec = recordsToPrint.get(i);
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

            if (y + actualRowHeight > bottomLimit - 10f) {
                document.finishPage(page);
                pageNum++;
                pageInfo = new PdfDocument.PageInfo.Builder(theme.pageWidth, theme.pageHeight, pageNum).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                canvas.drawRect(0, 0, theme.pageWidth, theme.pageHeight, theme.bgPaint);
                y = theme.margin;

                canvas.drawText(account.getTitle() + " (contd.)", theme.margin, y + 13f, theme.subPaint);
                y += 20f;
                canvas.drawLine(theme.margin, y + 2f, theme.pageWidth - theme.margin, y + 2f, theme.dividerPaint);
                y += 10f;

                canvas.drawRect(theme.margin, y, theme.pageWidth - theme.margin, y + rowHeight, theme.tableHeaderBgPaint);
                canvas.drawText("S.No",        theme.margin + 4,                       y + 15f, theme.accentPaint);
                canvas.drawText("Description", theme.margin + colSno + 4,              y + 15f, theme.accentPaint);
                canvas.drawText("Date",        theme.margin + colSno + colDesc + 4,    y + 15f, theme.accentPaint);
                canvas.drawText("Time",        theme.margin + colSno + colDesc + colDate + 4, y + 15f, theme.accentPaint);
                float ahx = theme.margin + colSno + colDesc + colDate + colTime + colAmount - theme.accentPaint.measureText("Amount") - 4f;
                canvas.drawText("Amount",      ahx,                              y + 15f, theme.accentPaint);
                y += rowHeight;
                canvas.drawLine(theme.margin, y, theme.pageWidth - theme.margin, y, theme.dividerPaint);
            }

            Record rec = recordsToPrint.get(i);
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

            canvas.drawRect(theme.margin, y, theme.pageWidth - theme.margin, y + actualRowHeightCalc, rowBg);
            canvas.drawText(String.valueOf(i + 1), theme.margin + 4, y + 15f, theme.cellMutedPaint);

            String desc = rec.getDescription();
            while (desc.length() > 1 && theme.cellPaint.measureText(desc) > colDesc - 8f) {
                desc = desc.substring(0, desc.length() - 1);
            }
            if (!desc.equals(rec.getDescription())) desc += "…";
            canvas.drawText(desc, theme.margin + colSno + 4, y + 15f, theme.cellPaint);

            float currentY = y + 27f;
            if (hasRemarks) {
                String truncRemarks = combinedNotes;
                while (truncRemarks.length() > 1 && theme.cellMutedPaint.measureText(truncRemarks) > colDesc - 8f) {
                    truncRemarks = truncRemarks.substring(0, truncRemarks.length() - 1);
                }
                if (!truncRemarks.equals(combinedNotes)) truncRemarks += "…";
                canvas.drawText(truncRemarks, theme.margin + colSno + 4, currentY, theme.cellMutedPaint);
                currentY += 12f;
            } else {
                currentY -= 14f;
                currentY += 12f;
            }
            for (String fn : fileNames) {
                String truncFn = "\uD83D\uDCCE " + fn;
                while (truncFn.length() > 1 && theme.cellMutedPaint.measureText(truncFn) > colDesc - 8f) {
                    truncFn = truncFn.substring(0, truncFn.length() - 1);
                }
                if (!truncFn.equals("\uD83D\uDCCE " + fn)) truncFn += "…";
                canvas.drawText(truncFn, theme.margin + colSno + 4, currentY, theme.cellMutedPaint);
                currentY += 12f;
            }

            canvas.drawText(AppUtils.formatDateCompact(rec.getDate()), theme.margin + colSno + colDesc + 4, y + 15f, theme.cellMutedPaint);
            String timeStr = rec.getTimestampMillis() > 0 ? timeSdf.format(new Date(rec.getTimestampMillis())) : "-";
            canvas.drawText(timeStr, theme.margin + colSno + colDesc + colDate + 4, y + 15f, theme.cellMutedPaint);

            String amtStr = String.format(Locale.getDefault(), "%.2f", rec.getAmount());
            float amtX = theme.margin + colSno + colDesc + colDate + colTime + colAmount - theme.cellPaint.measureText(amtStr) - 4f;
            canvas.drawText(amtStr, amtX, y + 15f, theme.cellPaint);

            y += actualRowHeightCalc;
            canvas.drawLine(theme.margin, y, theme.pageWidth - theme.margin, y, theme.dividerPaint);
        }

        if (y + rowHeight + 30f > bottomLimit) {
            document.finishPage(page);
            pageNum++;
            pageInfo = new PdfDocument.PageInfo.Builder(theme.pageWidth, theme.pageHeight, pageNum).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            canvas.drawRect(0, 0, theme.pageWidth, theme.pageHeight, theme.bgPaint);
            y = theme.margin;
        }

        y += 4f;
        canvas.drawRect(theme.margin, y, theme.pageWidth - theme.margin, y + rowHeight, theme.totalBgPaint);
        canvas.drawText("TOTAL SELECTED", theme.margin + 4, y + 15f, theme.totalTextPaint);
        String totalStr = String.format(Locale.getDefault(), "%.2f", totalAmt);
        float totalX = theme.margin + contentWidth - theme.totalTextPaint.measureText(totalStr) - 4f;
        canvas.drawText(totalStr, totalX, y + 15f, theme.totalTextPaint);
        y += rowHeight + 16f;

        if (y + 20f > bottomLimit) {
            document.finishPage(page);
            pageNum++;
            PdfDocument.PageInfo pi = new PdfDocument.PageInfo.Builder(595, 842, pageNum).create();
            page = document.startPage(pi);
            canvas = page.getCanvas();
            canvas.drawRect(0, 0, 595, 842, theme.bgPaint);
            y = 40f;
        }
        canvas.drawText("Generated by NoteCalc  •  " + lastMod + "  •  Page " + pageNum, 40f, y + 12f, theme.subPaint);

        document.finishPage(page);
        pageTracker[0] = pageNum;

        LinkedHashMap<Record, String> recordLabels = new LinkedHashMap<>();
        for (int j = 0; j < recordsToPrint.size(); j++) {
            recordLabels.put(recordsToPrint.get(j), "S.No " + (j + 1) + ": " + recordsToPrint.get(j).getDescription());
        }
        PdfAppendixHelper.appendAttachmentsAppendixToPdf(activity, document, recordLabels, pageTracker);
    }
}
