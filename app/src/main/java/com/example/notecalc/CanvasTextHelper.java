package com.example.notecalc;

import android.graphics.Paint;
import java.util.ArrayList;
import java.util.List;

public class CanvasTextHelper {

    public static List<String> wrapText(String text, Paint paint, float maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        int start = 0;
        int length = text.length();

        while (start < length) {
            while (start < length && text.charAt(start) == ' ') {
                start++;
            }
            if (start >= length) {
                break;
            }

            int count = paint.breakText(text, start, length, true, maxWidth, null);
            if (count <= 0) {
                count = 1;
            }

            if (start + count >= length) {
                lines.add(text.substring(start));
                break;
            }

            int end = start + count;
            int lastSpace = text.lastIndexOf(' ', end);

            if (lastSpace > start) {
                lines.add(text.substring(start, lastSpace));
                start = lastSpace + 1;
            } else {
                lines.add(text.substring(start, end));
                start = end;
            }
        }

        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }
}
