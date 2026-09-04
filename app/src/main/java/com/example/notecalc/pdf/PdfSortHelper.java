package com.example.notecalc.pdf;

import com.example.notecalc.Record;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfSortHelper {

    public static void sortRecords(List<Record> records, PdfSortOrder sortOrder) {
        records.sort((r1, r2) -> {
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
    }
}
