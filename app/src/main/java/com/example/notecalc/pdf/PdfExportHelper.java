package com.example.notecalc.pdf;

import com.example.notecalc.Account;
import com.example.notecalc.AccountGroup;
import com.example.notecalc.DialogHelper;
import com.example.notecalc.MainActivity;
import com.example.notecalc.Record;

public class PdfExportHelper {

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
                PdfRenderHelper.appendAccountToPdf(activity, document, account, pageTracker, sortOrder);
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

    public static void generateAndOpenSelectedPdf(MainActivity activity, java.util.List<Record> selectedRecords, PdfSortOrder sortOrder) {
        if (selectedRecords.isEmpty()) return;
        
        android.app.Dialog progressDialog = DialogHelper.showProgressDialog(activity);
        
        new Thread(() -> {
            android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
            try {
                int[] pageTracker = {0};
                PdfRenderHelper.appendSelectedRecordsToPdf(activity, document, activity.currentEditingAccount, selectedRecords, pageTracker, sortOrder);
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
}
