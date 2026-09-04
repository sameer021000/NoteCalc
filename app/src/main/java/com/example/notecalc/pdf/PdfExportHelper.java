package com.example.notecalc.pdf;

import com.example.notecalc.Account;
import com.example.notecalc.AccountGroup;
import com.example.notecalc.PdfDialogHelper;
import com.example.notecalc.MainActivity;
import com.example.notecalc.Record;

public class PdfExportHelper {

    public static void generateAndOpenAllPdf(MainActivity activity) {
        android.app.Dialog progressDialog = PdfDialogHelper.showProgressDialog(activity);
        
        new Thread(() -> {
            android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
            try {
                int[] pageTracker = {0};
                boolean hasRecords = false;
                
                for (AccountGroup group : activity.appStorage.groups) {
                    for (Account account : group.getAccounts()) {
                        if (!account.getRecords().isEmpty()) {
                            PdfRenderHelper.appendAccountToPdf(activity, document, account, pageTracker, PdfSortOrder.SNO);
                            hasRecords = true;
                        }
                    }
                }
                for (Account account : activity.appStorage.standaloneAccounts) {
                    if (!account.getRecords().isEmpty()) {
                        PdfRenderHelper.appendAccountToPdf(activity, document, account, pageTracker, PdfSortOrder.SNO);
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

                PdfIOHelper.saveAndOpenPdf(activity, document, "All_Accounts_Export.pdf", progressDialog);
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
        android.app.Dialog progressDialog = PdfDialogHelper.showProgressDialog(activity);
        
        new Thread(() -> {
            android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
            try {
                int[] pageTracker = {0};
                boolean hasRecords = false;
                
                for (Account account : group.getAccounts()) {
                    if (!account.getRecords().isEmpty()) {
                        PdfRenderHelper.appendAccountToPdf(activity, document, account, pageTracker, sortOrder);
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

                PdfIOHelper.saveAndOpenPdf(activity, document, group.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + "_Export.pdf", progressDialog);
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
        android.app.Dialog progressDialog = PdfDialogHelper.showProgressDialog(activity);
        
        new Thread(() -> {
            android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
            try {
                int[] pageTracker = {0};
                PdfRenderHelper.appendAccountToPdf(activity, document, account, pageTracker, sortOrder);
                PdfIOHelper.saveAndOpenPdf(activity, document, account.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + ".pdf", progressDialog);
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

    public static void generateAndOpenSelectedPdf(MainActivity activity, java.util.List<Record> selectedRecords, PdfSortOrder sortOrder) {
        if (selectedRecords.isEmpty()) return;
        
        android.app.Dialog progressDialog = PdfDialogHelper.showProgressDialog(activity);
        
        new Thread(() -> {
            android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
            try {
                int[] pageTracker = {0};
                PdfRenderHelper.appendSelectedRecordsToPdf(activity, document, activity.currentEditingAccount, selectedRecords, pageTracker, sortOrder);
                PdfIOHelper.saveAndOpenPdf(activity, document, "Selected_Export.pdf", progressDialog);
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
}
