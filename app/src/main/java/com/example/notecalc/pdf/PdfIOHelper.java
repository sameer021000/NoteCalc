package com.example.notecalc.pdf;

import com.example.notecalc.MainActivity;
import android.graphics.pdf.PdfDocument;
import android.app.Dialog;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;

public class PdfIOHelper {

    public static void saveAndOpenPdf(MainActivity activity, PdfDocument document, String fileName, Dialog progressDialog) {
        File pdfDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (pdfDir == null) {
            activity.runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(activity, "Cannot access documents directory.", Toast.LENGTH_LONG).show();
            });
            document.close();
            return;
        }
        if (!pdfDir.exists() && !pdfDir.mkdirs()) {
            activity.runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(activity, "Failed to create directory.", Toast.LENGTH_LONG).show();
            });
            document.close();
            return;
        }
        File file = new File(pdfDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
        } catch (Exception e) {
            android.util.Log.e("NoteCalc", "Failed to write PDF", e);
        }
        try { document.close(); } catch (Exception ignored) {}

        activity.runOnUiThread(() -> {
            progressDialog.dismiss();
            try {
                Uri uri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".fileprovider", file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                if (intent.resolveActivity(activity.getPackageManager()) == null) {
                    Toast.makeText(activity, "No PDF viewer installed. Please install one from the Play Store.", Toast.LENGTH_LONG).show();
                    return;
                }

                activity.startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(activity, "Failed to open PDF: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
