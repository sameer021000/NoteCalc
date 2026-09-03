package com.example.notecalc;

import android.graphics.Paint;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppUtils {

    public static String getCurrentDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

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

    public static String formatDateCompact(String ddMMYYYY) {
        try {
            String[] parts = ddMMYYYY.split("-");
            if (parts.length != 3) return ddMMYYYY;
            String day = parts[0];
            int monthNum = Integer.parseInt(parts[1]);
            String year = parts[2];
            String yy = year.length() >= 2 ? year.substring(year.length() - 2) : year;
            String[] monthAbbr = {"Jan","Feb","Mar","Apr","May","Jun",
                                   "Jul","Aug","Sep","Oct","Nov","Dec"};
            if (monthNum < 1 || monthNum > 12) return ddMMYYYY;
            return day + monthAbbr[monthNum - 1] + yy;
        } catch (Exception e) {
            return ddMMYYYY;
        }
    }

    public static void resequentializeRecords(List<Record> records) {
        if (records == null || records.isEmpty()) return;
        List<Record> copy = new ArrayList<>(records);
        copy.sort(java.util.Comparator.comparingInt(Record::getOriginalIndex));
        for (int i = 0; i < copy.size(); i++) {
            copy.get(i).setOriginalIndex(i);
        }
    }
}
