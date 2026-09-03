package com.example.notecalc;

import com.example.notecalc.pdf.PdfSortOrder;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Date;
import android.graphics.Color;
import java.text.SimpleDateFormat;


public class PdfHelper {

    public static void generateAndOpenAllPdf(MainActivity activity) {
        android.app.Dialog progressDialog = DialogHelper.showProgressDialog(activity);
        
        new Thread(() -> {
            android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
            try {
                int[] pageTracker = {0};
                boolean hasRecords = false;
                
                for (AccountGroup group : activity.appStorage.groups) {
                    for (Account account : group.getAccounts()) {
                        if (!account.getRecords().isEmpty()) {
                            appendAccountToPdf(activity, document, account, pageTracker, PdfSortOrder.SNO);
                            hasRecords = true;
                        }
                    }
                }
                for (Account account : activity.appStorage.standaloneAccounts) {
                    if (!account.getRecords().isEmpty()) {
                        appendAccountToPdf(activity, document, account, pageTracker, PdfSortOrder.SNO);
                        hasRecords = true;
                    }
                }
                
                if (!hasRecords) {
                    activity.runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(activity, "No records found to export.", android.widget.Toast.LENGTH_SHORT).show();
                    });
                    document.close();
                    return;
                }

                java.io.File pdfDir = activity.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
                if (pdfDir == null) {
                    activity.runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(activity, "Cannot access documents directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                if (!pdfDir.exists() && !pdfDir.mkdirs()) {
                    activity.runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(activity, "Failed to create directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                java.io.File file = new java.io.File(pdfDir, "All_Accounts_Export.pdf");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    document.writeTo(fos);
                }
                try { document.close(); } catch (Exception ignored) {}

                activity.runOnUiThread(() -> {
                    progressDialog.dismiss();
                    try {
                        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".fileprovider", file);
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");
                        intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        // Check if any app can handle the PDF intent
                        if (intent.resolveActivity(activity.getPackageManager()) == null) {
                            android.widget.Toast.makeText(activity, "No PDF viewer installed. Please install one from the Play Store.", android.widget.Toast.LENGTH_LONG).show();
                            return;
                        }

                        activity.startActivity(intent);
                    } catch (Exception ex) {
                        android.widget.Toast.makeText(activity, "Failed to open PDF: " + ex.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("NoteCalc", "Failed to generate PDF", e);
                activity.runOnUiThread(() -> {
                    progressDialog.dismiss();
                    android.widget.Toast.makeText(activity, "Failed to generate PDF: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                });
                try { document.close(); } catch (Exception ignored) {}
            }
        }).start();
    }



    public static void generateAndOpenGroupPdf(MainActivity activity, AccountGroup group, PdfSortOrder sortOrder) {
        android.app.Dialog progressDialog = DialogHelper.showProgressDialog(activity);
        
        new Thread(() -> {
            android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
            try {
                int[] pageTracker = {0};
                boolean hasRecords = false;
                
                for (Account account : group.getAccounts()) {
                    if (!account.getRecords().isEmpty()) {
                        appendAccountToPdf(activity, document, account, pageTracker, sortOrder);
                        hasRecords = true;
                    }
                }
                
                if (!hasRecords) {
                    activity.runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(activity, "No records found to export in this group.", android.widget.Toast.LENGTH_SHORT).show();
                    });
                    document.close();
                    return;
                }

                java.io.File pdfDir = activity.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
                if (pdfDir == null) {
                    activity.runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(activity, "Cannot access documents directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                if (!pdfDir.exists() && !pdfDir.mkdirs()) {
                    activity.runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(activity, "Failed to create directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                java.io.File file = new java.io.File(pdfDir, group.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + "_Export.pdf");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    document.writeTo(fos);
                }
                try { document.close(); } catch (Exception ignored) {}

                activity.runOnUiThread(() -> {
                    progressDialog.dismiss();
                    try {
                        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".fileprovider", file);
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");
                        intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        // Check if any app can handle the PDF intent
                        if (intent.resolveActivity(activity.getPackageManager()) == null) {
                            android.widget.Toast.makeText(activity, "No PDF viewer installed. Please install one from the Play Store.", android.widget.Toast.LENGTH_LONG).show();
                            return;
                        }

                        activity.startActivity(intent);
                    } catch (Exception ex) {
                        android.widget.Toast.makeText(activity, "Failed to open PDF: " + ex.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("NoteCalc", "Failed to generate PDF", e);
                activity.runOnUiThread(() -> {
                    progressDialog.dismiss();
                    android.widget.Toast.makeText(activity, "Failed to generate PDF: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                });
                try { document.close(); } catch (Exception ignored) {}
            }
        }).start();
    }



    public static void generateAndOpenPdf(MainActivity activity, Account account, PdfSortOrder sortOrder) {
        android.app.Dialog progressDialog = DialogHelper.showProgressDialog(activity);
        
        new Thread(() -> {
            android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
            try {
                int[] pageTracker = {0};
                appendAccountToPdf(activity, document, account, pageTracker, sortOrder);
                java.io.File pdfDir = activity.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
                if (pdfDir == null) {
                    activity.runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(activity, "Cannot access documents directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                if (!pdfDir.exists() && !pdfDir.mkdirs()) {
                    activity.runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(activity, "Failed to create directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                java.io.File file = new java.io.File(pdfDir, account.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + ".pdf");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    document.writeTo(fos);
                }
                try { document.close(); } catch (Exception ignored) {}
                
                activity.runOnUiThread(() -> {
                    progressDialog.dismiss();
                    try {
                        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".fileprovider", file);
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");
                        intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        // Check if any app can handle the PDF intent
                        if (intent.resolveActivity(activity.getPackageManager()) == null) {
                            android.widget.Toast.makeText(activity, "No PDF viewer installed. Please install one from the Play Store.", android.widget.Toast.LENGTH_LONG).show();
                            return;
                        }

                        activity.startActivity(intent);
                    } catch (Exception ex) {
                        android.widget.Toast.makeText(activity, "Failed to open PDF: " + ex.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("NoteCalc", "Failed to generate PDF", e);
                activity.runOnUiThread(() -> {
                    progressDialog.dismiss();
                    android.widget.Toast.makeText(activity, "Failed to generate PDF: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                });
                try { document.close(); } catch (Exception ignored) {}
            }
        }).start();
    }



    public static void appendAccountToPdf(MainActivity activity, android.graphics.pdf.PdfDocument document, Account account, int[] pageTracker, PdfSortOrder sortOrder) {
        // --- Page dimensions (A4 at 72 dpi approx) ---
        int pageWidth  = 595;
        int pageHeight = 842;
        int margin     = 40;

        int contentWidth = pageWidth - margin * 2;
        float bottomLimit = pageHeight - margin;

        // --- Paints (reusable across pages) ---
        Paint bgPaint = new Paint();
        bgPaint.setColor(ThemeManager.getBgPrimaryColor(activity));

        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(activity.getColor(R.color.text_primary));
        titlePaint.setTextSize(22f);
        titlePaint.setFakeBoldText(true);

        Paint subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subPaint.setColor(activity.getColor(R.color.text_tertiary));
        subPaint.setTextSize(11f);

        Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accentPaint.setColor(ThemeManager.getSecondaryAccentColor(activity));
        accentPaint.setTextSize(11f);
        accentPaint.setFakeBoldText(true);

        Paint cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellPaint.setColor(activity.getColor(R.color.text_primary));
        cellPaint.setTextSize(10f);

        Paint cellMutedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellMutedPaint.setColor(activity.getColor(R.color.text_tertiary));
        cellMutedPaint.setTextSize(10f);

        Paint dividerPaint = new Paint();
        dividerPaint.setColor(ThemeManager.getBorderColor(activity));
        dividerPaint.setStrokeWidth(0.8f);

        Paint rowEvenPaint = new Paint();
        rowEvenPaint.setColor(ThemeManager.getBgSecondaryColor(activity));

        Paint rowOddPaint = new Paint();
        rowOddPaint.setColor(ThemeManager.getBgTertiaryColor(activity));

        Paint totalBgPaint = new Paint();
        totalBgPaint.setColor(ThemeManager.getPrimaryAccentColor(activity));

        Paint totalTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        totalTextPaint.setColor(Color.WHITE);
        totalTextPaint.setTextSize(11f);
        totalTextPaint.setFakeBoldText(true);

        Paint tableHeaderBgPaint = new Paint();
        tableHeaderBgPaint.setColor(ThemeManager.getBgSecondaryColor(activity));

        // --- Column widths ---
        float colSno    = 38f;
        float colDate   = 70f;
        float colTime   = 55f;
        float colAmount = 60f;
        float colDesc   = contentWidth - colSno - colDate - colTime - colAmount;
        float rowHeight = 22f;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String lastMod = sdf.format(new Date(account.getLastModified()));
        
        // Copy and sort the records chronologically to prevent jumbled PDFs
        List<Record> expRecords = new ArrayList<>(account.getRecords());
        expRecords.sort((r1, r2) -> {
            if (sortOrder == PdfSortOrder.DESCRIPTION) {
                String d1 = r1.getDescription() == null ? "" : r1.getDescription();
                String d2 = r2.getDescription() == null ? "" : r2.getDescription();
                return d1.compareToIgnoreCase(d2);
            }
            if (sortOrder == PdfSortOrder.DATE) {
                try {
                    java.text.SimpleDateFormat sortSdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                    java.util.Date d1 = r1.getDate() == null ? null : sortSdf.parse(r1.getDate());
                    java.util.Date d2 = r2.getDate() == null ? null : sortSdf.parse(r2.getDate());
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
                    java.text.SimpleDateFormat sortSdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                    java.util.Date d1 = r1.getDate() == null ? null : sortSdf.parse(r1.getDate());
                    java.util.Date d2 = r2.getDate() == null ? null : sortSdf.parse(r2.getDate());
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

        // --- Page tracking ---
        int pageNum = pageTracker[0];
        Canvas canvas;
        PdfDocument.Page page;
        float y;

        // ===== PAGE 1: Header + Table Header + Rows =====
        pageNum++;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
        y = margin;

        // --- Title with word-wrapping ---
        String titleText = account.getTitle();
        float titleLineHeight = 28f;
        float maxTitleWidth = contentWidth - 15f;
        List<String> titleLines = AppUtils.wrapText(titleText, titlePaint, maxTitleWidth);
        for (String line : titleLines) {
            canvas.drawText(line, margin, y + 22f, titlePaint);
            y += titleLineHeight;
        }
        y += 4f;

        // --- Subtitle: last modified + item count ---
        String subtitle = "Last modified: " + lastMod + "  |  Items: " + (expRecords.size() + budRecords.size());
        double budget = account.calculateTotalBudget();
        if (budget > 0) {
            subtitle += "  |  Budget: " + String.format(Locale.getDefault(), "%.2f", budget);
        }
        canvas.drawText(subtitle, margin, y + 13f, subPaint);
        y += 20f;

        // Divider under header
        canvas.drawLine(margin, y + 4f, pageWidth - margin, y + 4f, dividerPaint);
        y += 16f;

        for (int listIdx = 0; listIdx < allRecordLists.size(); listIdx++) {
            List<Record> records = allRecordLists.get(listIdx);
            String tableName = listNames.get(listIdx);
            double totalAmt = listTotals.get(listIdx);
            
            if (tableName != null) {
                if (y + 50f > bottomLimit) {
                    document.finishPage(page);
                    pageNum++;
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
                    y = margin;
                }
                y += 10f;
                canvas.drawText(tableName, margin, y + 15f, titlePaint);
                y += 25f;
            }

        // --- Draw table header row ---
        canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, tableHeaderBgPaint);
        canvas.drawText("S.No",        margin + 4,                       y + 15f, accentPaint);
        canvas.drawText("Description", margin + colSno + 4,              y + 15f, accentPaint);
        canvas.drawText("Date",        margin + colSno + colDesc + 4,    y + 15f, accentPaint);
        canvas.drawText("Time",        margin + colSno + colDesc + colDate + 4, y + 15f, accentPaint);
        float amountHeaderX = margin + colSno + colDesc + colDate + colTime + colAmount - accentPaint.measureText("Amount") - 4f;
        canvas.drawText("Amount",      amountHeaderX,                y + 15f, accentPaint);
        y += rowHeight;
        canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);

        // --- Record rows (paginated) ---
        for (int i = 0; i < records.size(); i++) {
            Record tmpRec = records.get(i);
            String tRem = tmpRec.getRemarks();
            String tCat = tmpRec.getCategory();
            String tCombined = "";
            if (tCat != null && !tCat.isEmpty()) tCombined += "[" + tCat + "] ";
            if (tRem != null && !tRem.isEmpty()) tCombined += tRem;
            boolean tHasRem = !tCombined.isEmpty();
            java.util.List<String> tAtt = tmpRec.getAttachments();
            int numFiles = (tAtt != null) ? tAtt.size() : 0;
            float actualRowHeight = rowHeight;
            if (tHasRem) actualRowHeight += 14f;
            actualRowHeight += (12f * numFiles);

            // Check if we need a new page (need space for row + potential total row + footer)
            if (y + actualRowHeight > bottomLimit - 10f) {
                // Finish current page
                document.finishPage(page);

                // Start new page
                pageNum++;
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
                y = margin;

                // Page continuation header
                canvas.drawText(account.getTitle() + " (contd.)", margin, y + 13f, subPaint);
                y += 20f;
                canvas.drawLine(margin, y + 2f, pageWidth - margin, y + 2f, dividerPaint);
                y += 10f;

                // Redraw table header on new page
                canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, tableHeaderBgPaint);
                canvas.drawText("S.No",        margin + 4,                       y + 15f, accentPaint);
                canvas.drawText("Description", margin + colSno + 4,              y + 15f, accentPaint);
                canvas.drawText("Date",        margin + colSno + colDesc + 4,    y + 15f, accentPaint);
                canvas.drawText("Time",        margin + colSno + colDesc + colDate + 4, y + 15f, accentPaint);
                float ahx = margin + colSno + colDesc + colDate + colTime + colAmount - accentPaint.measureText("Amount") - 4f;
                canvas.drawText("Amount",      ahx,                              y + 15f, accentPaint);
                y += rowHeight;
                canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);
            }

            Record rec = records.get(i);
            Paint rowBg = (i % 2 == 0) ? rowEvenPaint : rowOddPaint;

            // Check if remarks or category exist; add extra height if so
            String recRemarks = rec.getRemarks();
            String cat = rec.getCategory();
            String combinedNotes = "";
            if (cat != null && !cat.isEmpty()) combinedNotes += "[" + cat + "] ";
            if (recRemarks != null && !recRemarks.isEmpty()) combinedNotes += recRemarks;
            boolean hasRemarks = !combinedNotes.isEmpty();
            
            java.util.List<String> attachments = rec.getAttachments();
            java.util.List<String> fileNames = new java.util.ArrayList<>();
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

            canvas.drawRect(margin, y, pageWidth - margin, y + actualRowHeightCalc, rowBg);

            canvas.drawText(String.valueOf(i + 1), margin + 4, y + 15f, cellMutedPaint);

            // Truncate long descriptions to fit column width
            String desc = rec.getDescription();
            while (desc.length() > 1 && cellPaint.measureText(desc) > colDesc - 8f) {
                desc = desc.substring(0, desc.length() - 1);
            }
            if (!desc.equals(rec.getDescription())) desc += "…";
            canvas.drawText(desc, margin + colSno + 4, y + 15f, cellPaint);

            // Draw remarks/category below description if present
            float currentY = y + 27f;
            if (hasRemarks) {
                String truncRemarks = combinedNotes;
                while (truncRemarks.length() > 1 && cellMutedPaint.measureText(truncRemarks) > colDesc - 8f) {
                    truncRemarks = truncRemarks.substring(0, truncRemarks.length() - 1);
                }
                if (!truncRemarks.equals(combinedNotes)) truncRemarks += "…";
                canvas.drawText(truncRemarks, margin + colSno + 4, currentY, cellMutedPaint);
                currentY += 12f;
            } else {
                currentY -= 14f;
                currentY += 12f;
            }
            for (String fn : fileNames) {
                String truncFn = "\uD83D\uDCCE " + fn;
                while (truncFn.length() > 1 && cellMutedPaint.measureText(truncFn) > colDesc - 8f) {
                    truncFn = truncFn.substring(0, truncFn.length() - 1);
                }
                if (!truncFn.equals("\uD83D\uDCCE " + fn)) truncFn += "…";
                canvas.drawText(truncFn, margin + colSno + 4, currentY, cellMutedPaint);
                currentY += 12f;
            }

            canvas.drawText(AppUtils.formatDateCompact(rec.getDate()), margin + colSno + colDesc + 4, y + 15f, cellMutedPaint);
            String timeStr = rec.getTimestampMillis() > 0 ? timeSdf.format(new Date(rec.getTimestampMillis())) : "-";
            canvas.drawText(timeStr, margin + colSno + colDesc + colDate + 4, y + 15f, cellMutedPaint);

            // Right-align amount
            String amtStr = String.format(Locale.getDefault(), "%.2f", rec.getAmount());
            float amtX = margin + colSno + colDesc + colDate + colTime + colAmount - cellPaint.measureText(amtStr) - 4f;
            canvas.drawText(amtStr, amtX, y + 15f, cellPaint);

            y += actualRowHeightCalc;
            canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);
        }

        // --- Total row (check if it fits, otherwise new page) ---
        if (y + rowHeight + 30f > bottomLimit) {
            document.finishPage(page);
            pageNum++;
            pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
            y = margin;
        }

        y += 4f;
        canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, totalBgPaint);
        canvas.drawText("TOTAL", margin + 4, y + 15f, totalTextPaint);
        String totalStr = String.format(Locale.getDefault(), "%.2f", totalAmt);
        float totalX = margin + contentWidth - totalTextPaint.measureText(totalStr) - 4f;
        canvas.drawText(totalStr, totalX, y + 15f, totalTextPaint);
        y += rowHeight + 16f;

        } // End of tables loop

        // --- Footer ---
        if (y + 20f > bottomLimit) {
            document.finishPage(page);
            pageNum++;
            PdfDocument.PageInfo pi = new PdfDocument.PageInfo.Builder(595, 842, pageNum).create();
            page = document.startPage(pi);
            canvas = page.getCanvas();
            Paint bg = new Paint();
            bg.setColor(ThemeManager.getBgPrimaryColor(activity));
            canvas.drawRect(0, 0, 595, 842, bg);
            y = 40f;
        }
        canvas.drawText("Generated by NoteCalc  •  " + lastMod + "  •  Page " + pageNum, 40f, y + 12f, subPaint);

        document.finishPage(page);
        pageTracker[0] = pageNum;

        java.util.LinkedHashMap<Record, String> recordLabels = new java.util.LinkedHashMap<>();
        for (int listIdx = 0; listIdx < allRecordLists.size(); listIdx++) {
            java.util.List<Record> list = allRecordLists.get(listIdx);
            String prefix = listNames.get(listIdx) != null ? listNames.get(listIdx) + " - " : "";
            for (int j = 0; j < list.size(); j++) {
                recordLabels.put(list.get(j), prefix + "S.No " + (j + 1) + ": " + list.get(j).getDescription());
            }
        }
        appendAttachmentsAppendixToPdf(activity, document, recordLabels, pageTracker);
    }



    /**
     * Wraps text into multiple lines that fit within the given maxWidth using the given paint.
     * Uses breakText for precise measurement and supports character-level wrapping if a word exceeds maxWidth.
     */
    public static void appendAttachmentsAppendixToPdf(MainActivity activity, android.graphics.pdf.PdfDocument document, java.util.LinkedHashMap<Record, String> recordLabels, int[] pageTracker) {
        java.util.List<Record> recordsWithImages = new java.util.ArrayList<>();
        for (Record r : recordLabels.keySet()) {
            java.util.List<String> atts = r.getAttachments();
            if (atts != null) {
                boolean hasImg = false;
                for (String path : atts) {
                    String lower = path.toLowerCase();
                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
                        hasImg = true;
                        break;
                    } else if (path.startsWith("content://")) {
                        String mime = activity.getContentResolver().getType(android.net.Uri.parse(path));
                        if (mime != null && mime.startsWith("image/")) {
                            hasImg = true;
                            break;
                        }
                    }
                }
                if (hasImg) recordsWithImages.add(r);
            }
        }
        
        if (recordsWithImages.isEmpty()) return;
        
        int pageWidth = 595;
        int pageHeight = 842;
        int margin = 40;
        float bottomLimit = pageHeight - margin;
        
        android.graphics.Paint bgPaint = new android.graphics.Paint();
        bgPaint.setColor(ThemeManager.getBgPrimaryColor(activity));
        
        android.graphics.Paint titlePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(activity.getColor(R.color.text_primary));
        titlePaint.setTextSize(20f);
        titlePaint.setFakeBoldText(true);
        
        android.graphics.Paint subPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        subPaint.setColor(activity.getColor(R.color.text_tertiary));
        subPaint.setTextSize(12f);
        
        android.graphics.Paint dividerPaint = new android.graphics.Paint();
        dividerPaint.setColor(ThemeManager.getBorderColor(activity));
        dividerPaint.setStrokeWidth(0.8f);
        
        int pageNum = pageTracker[0];
        android.graphics.Canvas canvas = null;
        android.graphics.pdf.PdfDocument.Page page = null;
        float y = bottomLimit + 100f; 
        
        int colWidth = (pageWidth - (margin * 2) - 15) / 2;
        int maxImgHeight = 350;
        
        for (Record r : recordsWithImages) {
            java.util.List<String> imgPaths = new java.util.ArrayList<>();
            for (String path : r.getAttachments()) {
                String lower = path.toLowerCase();
                boolean isImg = lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp");
                if (!isImg && path.startsWith("content://")) {
                    String mime = activity.getContentResolver().getType(android.net.Uri.parse(path));
                    if (mime != null && mime.startsWith("image/")) isImg = true;
                }
                if (isImg) imgPaths.add(path);
            }
            if (imgPaths.isEmpty()) continue;
            
            if (canvas == null || y + 50f > bottomLimit) {
                if (page != null) document.finishPage(page);
                pageNum++;
                android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
                y = margin;
                canvas.drawText("Attachments Appendix", margin, y + 15f, titlePaint);
                y += 30f;
                canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);
                y += 20f;
            } else {
                y += 20f;
            }
            
            String recTitle = recordLabels.get(r);
            if (recTitle == null) recTitle = "Record: " + r.getDescription();
            canvas.drawText(recTitle, margin, y + 12f, subPaint);
            y += 20f;
            
            for (int i = 0; i < imgPaths.size(); i += 2) {
                if (y + 100f > bottomLimit) {
                    document.finishPage(page);
                    pageNum++;
                    android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
                    y = margin;
                    canvas.drawText(recTitle + " (contd.)", margin, y + 12f, subPaint);
                    y += 20f;
                }
                
                float rowMaxHeight = 0;
                for (int c = 0; c < 2 && i + c < imgPaths.size(); c++) {
                    String path = imgPaths.get(i + c);
                    try {
                        android.graphics.Bitmap bitmap = null;
                        if (path.startsWith("content://")) {
                            java.io.InputStream is = activity.getContentResolver().openInputStream(android.net.Uri.parse(path));
                            if (is != null) {
                                bitmap = android.graphics.BitmapFactory.decodeStream(is);
                                is.close();
                            }
                        } else {
                            bitmap = android.graphics.BitmapFactory.decodeFile(path);
                        }
                        
                        if (bitmap != null) {
                            float scale = Math.min((float) colWidth / bitmap.getWidth(), (float) maxImgHeight / bitmap.getHeight());
                            int drawW = (int) (bitmap.getWidth() * scale);
                            int drawH = (int) (bitmap.getHeight() * scale);
                            float x = margin + (c * (colWidth + 15));
                            
                            float drawX = x + (colWidth - drawW) / 2f;
                            
                            float spaceLeft = bottomLimit - y - 20f; // 20f extra for text
                            if (drawH > spaceLeft && spaceLeft > 100f) {
                                float newScale = spaceLeft / bitmap.getHeight();
                                if(newScale < scale) {
                                    scale = newScale;
                                    drawW = (int) (bitmap.getWidth() * scale);
                                    drawH = (int) (bitmap.getHeight() * scale);
                                    drawX = x + (colWidth - drawW) / 2f;
                                }
                            }

                            android.graphics.Rect destRect = new android.graphics.Rect((int) drawX, (int) y, (int) (drawX + drawW), (int) (y + drawH));
                            canvas.drawBitmap(bitmap, null, destRect, null);
                            bitmap.recycle();
                            
                            // Draw filename
                            String fileName = path;
                            int lastSlash = path.lastIndexOf('/');
                            if (lastSlash != -1 && lastSlash < path.length() - 1) fileName = path.substring(lastSlash + 1);
                            
                            String truncFn = fileName;
                            while (truncFn.length() > 1 && subPaint.measureText(truncFn) > colWidth - 8f) {
                                truncFn = truncFn.substring(0, truncFn.length() - 1);
                            }
                            if (!truncFn.equals(fileName)) truncFn += "…";
                            
                            float fnX = x + (colWidth - subPaint.measureText(truncFn)) / 2f;
                            canvas.drawText(truncFn, fnX, y + drawH + 15f, subPaint);
                            
                            if (drawH + 20f > rowMaxHeight) rowMaxHeight = drawH + 20f;
                        }
                    } catch (Exception e) {
                        android.util.Log.e("NoteCalc", "Error appending attachment", e);
                    }
                }
                y += rowMaxHeight + 15f;
            }
        }
        
        if (page != null) {
            canvas.drawText("Generated by NoteCalc  •  Page " + pageNum, 40f, bottomLimit + 25f, subPaint);
            document.finishPage(page);
        }
        pageTracker[0] = pageNum;
    }


    public static void generateAndOpenSelectedPdf(MainActivity activity, java.util.List<Record> selectedRecords, PdfSortOrder sortOrder) {
        if (selectedRecords.isEmpty()) return;
        
        android.app.Dialog progressDialog = DialogHelper.showProgressDialog(activity);
        
        new Thread(() -> {
            android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
            try {
                int[] pageTracker = {0};
                appendSelectedRecordsToPdf(activity, document, activity.currentEditingAccount, selectedRecords, pageTracker, sortOrder);
                java.io.File pdfDir = activity.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
                if (pdfDir == null) {
                    activity.runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(activity, "Cannot access documents directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                if (!pdfDir.exists()) pdfDir.mkdirs();
                java.io.File file = new java.io.File(pdfDir, "Selected_Export.pdf");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    document.writeTo(fos);
                }
                try { document.close(); } catch (Exception ignored) {}
                
                activity.runOnUiThread(() -> {
                    progressDialog.dismiss();
                    try {
                        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".fileprovider", file);
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");
                        intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        // Check if any app can handle the PDF intent
                        if (intent.resolveActivity(activity.getPackageManager()) == null) {
                            android.widget.Toast.makeText(activity, "No PDF viewer installed. Please install one from the Play Store.", android.widget.Toast.LENGTH_LONG).show();
                            return;
                        }

                        activity.startActivity(intent);
                    } catch (Exception ex) {
                        android.widget.Toast.makeText(activity, "Failed to open PDF: " + ex.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("NoteCalc", "Failed to generate PDF", e);
                activity.runOnUiThread(() -> {
                    progressDialog.dismiss();
                    android.widget.Toast.makeText(activity, "Failed to generate PDF: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                });
                try { document.close(); } catch (Exception ignored) {}
            }
        }).start();
    }


    public static void appendSelectedRecordsToPdf(MainActivity activity, android.graphics.pdf.PdfDocument document, Account account, java.util.List<Record> selectedRecords, int[] pageTracker, PdfSortOrder sortOrder) {
        // --- Page dimensions (A4 at 72 dpi approx) ---
        int pageWidth  = 595;
        int pageHeight = 842;
        int margin     = 40;
        int contentWidth = pageWidth - margin * 2;
        float bottomLimit = pageHeight - margin;

        // --- Paints (reusable across pages) ---
        android.graphics.Paint bgPaint = new android.graphics.Paint();
        bgPaint.setColor(ThemeManager.getBgPrimaryColor(activity));

        android.graphics.Paint titlePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(activity.getColor(R.color.text_primary));
        titlePaint.setTextSize(22f);
        titlePaint.setFakeBoldText(true);

        android.graphics.Paint subPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        subPaint.setColor(activity.getColor(R.color.text_tertiary));
        subPaint.setTextSize(11f);

        android.graphics.Paint accentPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        accentPaint.setColor(ThemeManager.getSecondaryAccentColor(activity));
        accentPaint.setTextSize(11f);
        accentPaint.setFakeBoldText(true);

        android.graphics.Paint cellPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        cellPaint.setColor(activity.getColor(R.color.text_primary));
        cellPaint.setTextSize(10f);

        android.graphics.Paint cellMutedPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        cellMutedPaint.setColor(activity.getColor(R.color.text_tertiary));
        cellMutedPaint.setTextSize(10f);

        android.graphics.Paint dividerPaint = new android.graphics.Paint();
        dividerPaint.setColor(ThemeManager.getBorderColor(activity));
        dividerPaint.setStrokeWidth(0.8f);

        android.graphics.Paint rowEvenPaint = new android.graphics.Paint();
        rowEvenPaint.setColor(ThemeManager.getBgSecondaryColor(activity));

        android.graphics.Paint rowOddPaint = new android.graphics.Paint();
        rowOddPaint.setColor(ThemeManager.getBgTertiaryColor(activity));

        android.graphics.Paint totalBgPaint = new android.graphics.Paint();
        totalBgPaint.setColor(ThemeManager.getPrimaryAccentColor(activity));

        android.graphics.Paint totalTextPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        totalTextPaint.setColor(android.graphics.Color.WHITE);
        totalTextPaint.setTextSize(11f);
        totalTextPaint.setFakeBoldText(true);

        android.graphics.Paint tableHeaderBgPaint = new android.graphics.Paint();
        tableHeaderBgPaint.setColor(ThemeManager.getBgSecondaryColor(activity));

        // --- Column widths ---
        float colSno    = 38f;
        float colDate   = 70f;
        float colTime   = 55f;
        float colAmount = 60f;
        float colDesc   = contentWidth - colSno - colDate - colTime - colAmount;
        float rowHeight = 22f;

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
        java.text.SimpleDateFormat timeSdf = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault());
        String lastMod = sdf.format(new java.util.Date(account.getLastModified()));
        
        java.util.List<Record> recordsToPrint = new java.util.ArrayList<>(selectedRecords);
        recordsToPrint.sort((r1, r2) -> {
            if (sortOrder == PdfSortOrder.DESCRIPTION) {
                String d1 = r1.getDescription() == null ? "" : r1.getDescription();
                String d2 = r2.getDescription() == null ? "" : r2.getDescription();
                return d1.compareToIgnoreCase(d2);
            }
            if (sortOrder == PdfSortOrder.DATE) {
                try {
                    java.text.SimpleDateFormat sortSdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                    java.util.Date d1 = r1.getDate() == null ? null : sortSdf.parse(r1.getDate());
                    java.util.Date d2 = r2.getDate() == null ? null : sortSdf.parse(r2.getDate());
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

        // --- Page tracking ---
        int pageNum = pageTracker[0];
        android.graphics.Canvas canvas;
        android.graphics.pdf.PdfDocument.Page page;
        float y;

        pageNum++;
        android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
        y = margin;

        String titleText = account.getTitle() + " (Selected)";
        float titleLineHeight = 28f;
        float maxTitleWidth = contentWidth - 15f;
        java.util.List<String> titleLines = AppUtils.wrapText(titleText, titlePaint, maxTitleWidth);
        for (String line : titleLines) {
            canvas.drawText(line, margin, y + 22f, titlePaint);
            y += titleLineHeight;
        }
        y += 4f;

        String subtitle = "Exported: " + sdf.format(new java.util.Date()) + "  |  Items: " + recordsToPrint.size();
        canvas.drawText(subtitle, margin, y + 13f, subPaint);
        y += 20f;

        canvas.drawLine(margin, y + 4f, pageWidth - margin, y + 4f, dividerPaint);
        y += 16f;

        canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, tableHeaderBgPaint);
        canvas.drawText("S.No",        margin + 4,                       y + 15f, accentPaint);
        canvas.drawText("Description", margin + colSno + 4,              y + 15f, accentPaint);
        canvas.drawText("Date",        margin + colSno + colDesc + 4,    y + 15f, accentPaint);
        canvas.drawText("Time",        margin + colSno + colDesc + colDate + 4, y + 15f, accentPaint);
        float amountHeaderX = margin + colSno + colDesc + colDate + colTime + colAmount - accentPaint.measureText("Amount") - 4f;
        canvas.drawText("Amount",      amountHeaderX,                y + 15f, accentPaint);
        y += rowHeight;
        canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);

        for (int i = 0; i < recordsToPrint.size(); i++) {
            Record tmpRec = recordsToPrint.get(i);
            String tRem = tmpRec.getRemarks();
            String tCat = tmpRec.getCategory();
            String tCombined = "";
            if (tCat != null && !tCat.isEmpty()) tCombined += "[" + tCat + "] ";
            if (tRem != null && !tRem.isEmpty()) tCombined += tRem;
            boolean tHasRem = !tCombined.isEmpty();
            java.util.List<String> tAtt = tmpRec.getAttachments();
            int numFiles = (tAtt != null) ? tAtt.size() : 0;
            float actualRowHeight = rowHeight;
            if (tHasRem) actualRowHeight += 14f;
            actualRowHeight += (12f * numFiles);

            if (y + actualRowHeight > bottomLimit - 10f) {
                document.finishPage(page);
                pageNum++;
                pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
                y = margin;

                canvas.drawText(account.getTitle() + " (contd.)", margin, y + 13f, subPaint);
                y += 20f;
                canvas.drawLine(margin, y + 2f, pageWidth - margin, y + 2f, dividerPaint);
                y += 10f;

                canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, tableHeaderBgPaint);
                canvas.drawText("S.No",        margin + 4,                       y + 15f, accentPaint);
                canvas.drawText("Description", margin + colSno + 4,              y + 15f, accentPaint);
                canvas.drawText("Date",        margin + colSno + colDesc + 4,    y + 15f, accentPaint);
                canvas.drawText("Time",        margin + colSno + colDesc + colDate + 4, y + 15f, accentPaint);
                float ahx = margin + colSno + colDesc + colDate + colTime + colAmount - accentPaint.measureText("Amount") - 4f;
                canvas.drawText("Amount",      ahx,                              y + 15f, accentPaint);
                y += rowHeight;
                canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);
            }

            Record rec = recordsToPrint.get(i);
            android.graphics.Paint rowBg = (i % 2 == 0) ? rowEvenPaint : rowOddPaint;

            String recRemarks = rec.getRemarks();
            String cat = rec.getCategory();
            String combinedNotes = "";
            if (cat != null && !cat.isEmpty()) combinedNotes += "[" + cat + "] ";
            if (recRemarks != null && !recRemarks.isEmpty()) combinedNotes += recRemarks;
            boolean hasRemarks = !combinedNotes.isEmpty();
            
            java.util.List<String> attachments = rec.getAttachments();
            java.util.List<String> fileNames = new java.util.ArrayList<>();
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

            canvas.drawRect(margin, y, pageWidth - margin, y + actualRowHeightCalc, rowBg);

            canvas.drawText(String.valueOf(i + 1), margin + 4, y + 15f, cellMutedPaint);

            String desc = rec.getDescription();
            while (desc.length() > 1 && cellPaint.measureText(desc) > colDesc - 8f) {
                desc = desc.substring(0, desc.length() - 1);
            }
            if (!desc.equals(rec.getDescription())) desc += "…";
            canvas.drawText(desc, margin + colSno + 4, y + 15f, cellPaint);

            float currentY = y + 27f;
            if (hasRemarks) {
                String truncRemarks = combinedNotes;
                while (truncRemarks.length() > 1 && cellMutedPaint.measureText(truncRemarks) > colDesc - 8f) {
                    truncRemarks = truncRemarks.substring(0, truncRemarks.length() - 1);
                }
                if (!truncRemarks.equals(combinedNotes)) truncRemarks += "…";
                canvas.drawText(truncRemarks, margin + colSno + 4, currentY, cellMutedPaint);
                currentY += 12f;
            } else {
                currentY -= 14f;
                currentY += 12f;
            }
            for (String fn : fileNames) {
                String truncFn = "\uD83D\uDCCE " + fn;
                while (truncFn.length() > 1 && cellMutedPaint.measureText(truncFn) > colDesc - 8f) {
                    truncFn = truncFn.substring(0, truncFn.length() - 1);
                }
                if (!truncFn.equals("\uD83D\uDCCE " + fn)) truncFn += "…";
                canvas.drawText(truncFn, margin + colSno + 4, currentY, cellMutedPaint);
                currentY += 12f;
            }

            canvas.drawText(AppUtils.formatDateCompact(rec.getDate()), margin + colSno + colDesc + 4, y + 15f, cellMutedPaint);
            String timeStr = rec.getTimestampMillis() > 0 ? timeSdf.format(new java.util.Date(rec.getTimestampMillis())) : "-";
            canvas.drawText(timeStr, margin + colSno + colDesc + colDate + 4, y + 15f, cellMutedPaint);

            String amtStr = String.format(java.util.Locale.getDefault(), "%.2f", rec.getAmount());
            float amtX = margin + colSno + colDesc + colDate + colTime + colAmount - cellPaint.measureText(amtStr) - 4f;
            canvas.drawText(amtStr, amtX, y + 15f, cellPaint);

            y += actualRowHeightCalc;
            canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);
        }

        if (y + rowHeight + 30f > bottomLimit) {
            document.finishPage(page);
            pageNum++;
            pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
            y = margin;
        }

        y += 4f;
        canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, totalBgPaint);
        canvas.drawText("TOTAL SELECTED", margin + 4, y + 15f, totalTextPaint);
        String totalStr = String.format(java.util.Locale.getDefault(), "%.2f", totalAmt);
        float totalX = margin + contentWidth - totalTextPaint.measureText(totalStr) - 4f;
        canvas.drawText(totalStr, totalX, y + 15f, totalTextPaint);
        y += rowHeight + 16f;

        if (y + 20f > bottomLimit) {
            document.finishPage(page);
            pageNum++;
            android.graphics.pdf.PdfDocument.PageInfo pi = new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageNum).create();
            page = document.startPage(pi);
            canvas = page.getCanvas();
            android.graphics.Paint bg = new android.graphics.Paint();
            bg.setColor(ThemeManager.getBgPrimaryColor(activity));
            canvas.drawRect(0, 0, 595, 842, bg);
            y = 40f;
        }
        canvas.drawText("Generated by NoteCalc  •  " + lastMod + "  •  Page " + pageNum, 40f, y + 12f, subPaint);

        document.finishPage(page);
        pageTracker[0] = pageNum;

        java.util.LinkedHashMap<Record, String> recordLabels = new java.util.LinkedHashMap<>();
        for (int j = 0; j < recordsToPrint.size(); j++) {
            recordLabels.put(recordsToPrint.get(j), "S.No " + (j + 1) + ": " + recordsToPrint.get(j).getDescription());
        }
        appendAttachmentsAppendixToPdf(activity, document, recordLabels, pageTracker);
    }

}
