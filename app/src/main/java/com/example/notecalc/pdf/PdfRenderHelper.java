package com.example.notecalc.pdf;

import android.graphics.pdf.PdfDocument;
import com.example.notecalc.Account;
import com.example.notecalc.MainActivity;
import com.example.notecalc.Record;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfRenderHelper {

    public static void appendAccountToPdf(MainActivity activity, PdfDocument document, Account account, int[] pageTracker, PdfSortOrder sortOrder) {
        PdfThemeHelper.PdfTheme theme = new PdfThemeHelper.PdfTheme(activity);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String lastMod = sdf.format(new Date(account.getLastModified()));
        
        List<Record> expRecords = new ArrayList<>(account.getRecords());
        PdfSortHelper.sortRecords(expRecords, sortOrder);
        
        List<Record> budRecords = new ArrayList<>(account.getBudgetRecords());
        PdfSortHelper.sortRecords(budRecords, sortOrder);
        
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

        String titleText = account.getTitle();
        String subtitle = "Last modified: " + lastMod + "  |  Items: " + (expRecords.size() + budRecords.size());
        double budget = account.calculateTotalBudget();
        if (budget > 0) {
            subtitle += "  |  Budget: " + String.format(Locale.getDefault(), "%.2f", budget);
        }

        PdfDocumentRenderer.renderDocument(activity, document, theme, titleText, subtitle, lastMod, 
                                           allRecordLists, listNames, listTotals, account.getTitle(), pageTracker);
    }

    public static void appendSelectedRecordsToPdf(MainActivity activity, PdfDocument document, Account account, List<Record> selectedRecords, int[] pageTracker, PdfSortOrder sortOrder) {
        PdfThemeHelper.PdfTheme theme = new PdfThemeHelper.PdfTheme(activity);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String lastMod = sdf.format(new Date(account.getLastModified()));
        
        List<Record> recordsToPrint = new ArrayList<>(selectedRecords);
        PdfSortHelper.sortRecords(recordsToPrint, sortOrder);
        
        double totalAmt = 0;
        for (Record r : recordsToPrint) totalAmt += r.getAmount();

        String titleText = account.getTitle() + " (Selected)";
        String subtitle = "Exported: " + sdf.format(new Date()) + "  |  Items: " + recordsToPrint.size();

        PdfDocumentRenderer.renderDocument(activity, document, theme, titleText, subtitle, lastMod, 
                                           Collections.singletonList(recordsToPrint), 
                                           Collections.singletonList(null), 
                                           Collections.singletonList(totalAmt), 
                                           account.getTitle(), pageTracker);
    }
}
