package com.example.notecalc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String getCurrentDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(new Date());
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

    public static String formatToDdMmYyyy(String dateStr) {
        if (dateStr == null) return "";
        if (dateStr.matches("\\d{2}-\\d{2}-\\d{4}")) {
            return dateStr;
        }
        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                Date date = parser.parse(dateStr);
                if (date != null) {
                    return formatter.format(date);
                }
            } catch (Exception ignored) {}
        }
        return dateStr;
    }
}
