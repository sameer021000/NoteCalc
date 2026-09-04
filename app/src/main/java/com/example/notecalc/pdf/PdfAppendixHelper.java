package com.example.notecalc.pdf;

import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import com.example.notecalc.MainActivity;
import com.example.notecalc.Record;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class PdfAppendixHelper {

    public static void appendAttachmentsAppendixToPdf(MainActivity activity, PdfDocument document, LinkedHashMap<Record, String> recordLabels, int[] pageTracker) {
        List<Record> recordsWithImages = new ArrayList<>();
        for (Record r : recordLabels.keySet()) {
            List<String> atts = r.getAttachments();
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
        
        PdfThemeHelper.PdfTheme theme = new PdfThemeHelper.PdfTheme(activity);
        
        float bottomLimit = theme.pageHeight - theme.margin;
        
        int pageNum = pageTracker[0];
        Canvas canvas = null;
        PdfDocument.Page page = null;
        float y = bottomLimit + 100f; 
        
        int colWidth = (theme.pageWidth - (theme.margin * 2) - 15) / 2;
        int maxImgHeight = 350;
        
        for (Record r : recordsWithImages) {
            List<String> imgPaths = new ArrayList<>();
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
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(theme.pageWidth, theme.pageHeight, pageNum).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                canvas.drawRect(0, 0, theme.pageWidth, theme.pageHeight, theme.bgPaint);
                y = theme.margin;
                canvas.drawText("Attachments Appendix", theme.margin, y + 15f, theme.appendixTitlePaint);
                y += 30f;
                canvas.drawLine(theme.margin, y, theme.pageWidth - theme.margin, y, theme.dividerPaint);
            }
            y += 20f;
            
            String recTitle = recordLabels.get(r);
            if (recTitle == null) recTitle = "Record: " + r.getDescription();
            canvas.drawText(recTitle, theme.margin, y + 12f, theme.appendixSubPaint);
            y += 20f;
            
            for (int i = 0; i < imgPaths.size(); i += 2) {
                if (y + 100f > bottomLimit) {
                    document.finishPage(page);
                    pageNum++;
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(theme.pageWidth, theme.pageHeight, pageNum).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    canvas.drawRect(0, 0, theme.pageWidth, theme.pageHeight, theme.bgPaint);
                    y = theme.margin;
                    canvas.drawText(recTitle + " (contd.)", theme.margin, y + 12f, theme.appendixSubPaint);
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
                            float x = theme.margin + (c * (colWidth + 15));
                            
                            float drawX = x + (colWidth - drawW) / 2f;
                            
                            float spaceLeft = bottomLimit - y - 20f; 
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
                            
                            String fileName = path;
                            int lastSlash = path.lastIndexOf('/');
                            if (lastSlash != -1 && lastSlash < path.length() - 1) fileName = path.substring(lastSlash + 1);
                            
                            String truncFn = fileName;
                            while (truncFn.length() > 1 && theme.appendixSubPaint.measureText(truncFn) > colWidth - 8f) {
                                truncFn = truncFn.substring(0, truncFn.length() - 1);
                            }
                            if (!truncFn.equals(fileName)) truncFn += "…";
                            
                            float fnX = x + (colWidth - theme.appendixSubPaint.measureText(truncFn)) / 2f;
                            canvas.drawText(truncFn, fnX, y + drawH + 15f, theme.appendixSubPaint);
                            
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
            canvas.drawText("Generated by NoteCalc  •  Page " + pageNum, 40f, bottomLimit + 25f, theme.appendixSubPaint);
            document.finishPage(page);
        }
        pageTracker[0] = pageNum;
    }
}
