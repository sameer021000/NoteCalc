package com.example.notecalc.pdf;

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
        
        PdfPageHelper.PdfState state = new PdfPageHelper.PdfState();
        state.pageNum = pageTracker[0];
        state.canvas = null;
        state.page = null;
        state.y = bottomLimit + 100f; 
        
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
            
            if (state.canvas == null || state.y + 50f > bottomLimit) {
                PdfPageHelper.startNewPage(document, state, theme);
                state.canvas.drawText("Attachments Appendix", theme.margin, state.y + 15f, theme.appendixTitlePaint);
                state.y += 30f;
                state.canvas.drawLine(theme.margin, state.y, theme.pageWidth - theme.margin, state.y, theme.dividerPaint);
            }
            state.y += 20f;
            
            String recTitle = recordLabels.get(r);
            if (recTitle == null) recTitle = "Record: " + r.getDescription();
            state.canvas.drawText(recTitle, theme.margin, state.y + 12f, theme.appendixSubPaint);
            state.y += 20f;
            
            for (int i = 0; i < imgPaths.size(); i += 2) {
                if (state.y + 100f > bottomLimit) {
                    PdfPageHelper.startNewPage(document, state, theme);
                    state.canvas.drawText(recTitle + " (contd.)", theme.margin, state.y + 12f, theme.appendixSubPaint);
                    state.y += 20f;
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
                            
                            float spaceLeft = bottomLimit - state.y - 20f; 
                            if (drawH > spaceLeft && spaceLeft > 100f) {
                                float newScale = spaceLeft / bitmap.getHeight();
                                if(newScale < scale) {
                                    scale = newScale;
                                    drawW = (int) (bitmap.getWidth() * scale);
                                    drawH = (int) (bitmap.getHeight() * scale);
                                    drawX = x + (colWidth - drawW) / 2f;
                                }
                            }

                            android.graphics.Rect destRect = new android.graphics.Rect((int) drawX, (int) state.y, (int) (drawX + drawW), (int) (state.y + drawH));
                            state.canvas.drawBitmap(bitmap, null, destRect, null);
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
                            state.canvas.drawText(truncFn, fnX, state.y + drawH + 15f, theme.appendixSubPaint);
                            
                            if (drawH + 20f > rowMaxHeight) rowMaxHeight = drawH + 20f;
                        }
                    } catch (Exception e) {
                        android.util.Log.e("NoteCalc", "Error appending attachment", e);
                    }
                }
                state.y += rowMaxHeight + 15f;
            }
        }
        
        if (state.page != null) {
            state.canvas.drawText("Generated by NoteCalc  •  Page " + state.pageNum, 40f, bottomLimit + 25f, theme.appendixSubPaint);
            document.finishPage(state.page);
        }
        pageTracker[0] = state.pageNum;
    }
}
