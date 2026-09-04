package com.example.notecalc;

import com.example.notecalc.pdf.PdfSortOrder;
import com.example.notecalc.pdf.PdfExportHelper;
import java.util.List;

public class PdfHelper {

    public static void generateAndOpenAllPdf(MainActivity activity) {
        PdfExportHelper.generateAndOpenAllPdf(activity);
    }

    public static void generateAndOpenGroupPdf(MainActivity activity, AccountGroup group, PdfSortOrder sortOrder) {
        PdfExportHelper.generateAndOpenGroupPdf(activity, group, sortOrder);
    }

    public static void generateAndOpenPdf(MainActivity activity, Account account, PdfSortOrder sortOrder) {
        PdfExportHelper.generateAndOpenPdf(activity, account, sortOrder);
    }

    public static void generateAndOpenSelectedPdf(MainActivity activity, List<Record> selectedRecords, PdfSortOrder sortOrder) {
        PdfExportHelper.generateAndOpenSelectedPdf(activity, selectedRecords, sortOrder);
    }
}
