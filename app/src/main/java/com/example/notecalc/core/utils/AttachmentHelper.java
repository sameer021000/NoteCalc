package com.example.notecalc.core.utils;
import com.example.notecalc.core.ui.*;
import com.example.notecalc.*;
import android.view.View;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AttachmentHelper {
    public static void handleActivityResult(MainActivity activity, int requestCode, int resultCode, android.content.Intent data) {
        if (requestCode == MainActivity.REQUEST_CODE_CAMERA) {
            if (resultCode == android.app.Activity.RESULT_OK && activity.currentPhotoPath != null) {
                activity.tempAttachments.add(activity.currentPhotoPath);
                renderEditorAttachments(activity);
            } else if (activity.currentPhotoPath != null) {
                java.io.File f = new java.io.File(activity.currentPhotoPath);
                if (f.exists() && !f.delete()) android.util.Log.w("NoteCalc", "Failed to delete temp file");
            }
            activity.currentPhotoPath = null;
            return;
        }

        if (requestCode == MainActivity.REQUEST_CODE_ATTACH && resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null) {
            android.net.Uri uri = data.getData();
            try {
                java.io.File attachmentsDir = new java.io.File(activity.getFilesDir(), "attachments");
                if (!attachmentsDir.exists()) { boolean ignored = attachmentsDir.mkdirs(); }

                String originalName = "attachment_" + System.currentTimeMillis();
                try (android.database.Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if (index != -1) {
                            String tempName = cursor.getString(index);
                            if (tempName != null) {
                                int lastSlash = tempName.lastIndexOf('/');
                                String nameOnly = (lastSlash != -1) ? tempName.substring(lastSlash + 1) : tempName;
                                originalName = nameOnly.replaceAll("[/\\\\:*?\"<>|]", "_");
                            }
                        }
                    }
                }

                java.io.File destFile = new java.io.File(attachmentsDir, originalName);
                if (destFile.exists() || destFile.createNewFile()) {
                    try (java.io.InputStream in = activity.getContentResolver().openInputStream(uri);
                         java.io.FileOutputStream out = new java.io.FileOutputStream(destFile)) {
                        if (in == null) throw new java.io.IOException("Failed to open input stream");
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    }
                    activity.tempAttachments.add(destFile.getAbsolutePath());
                    renderEditorAttachments(activity);
                }
            } catch (Exception e) {
                android.util.Log.e("NoteCalc", "Failed to attach file", e);
                Toast.makeText(activity, activity.getString(R.string.auto_failed_to_attach_fil_11), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @android.annotation.SuppressLint("SetTextI18n")
    public static void renderEditorAttachments(MainActivity activity) {
        if (activity.attachmentsContainer == null) return;
        activity.attachmentsContainer.removeAllViews();
        if (activity.tempAttachments.isEmpty()) {
            activity.attachmentsContainer.setVisibility(View.GONE);
            if (activity.btnAttachFile != null) activity.btnAttachFile.setAlpha(1.0f);
        } else {
            activity.attachmentsContainer.setVisibility(View.VISIBLE);
            if (activity.btnAttachFile != null) activity.btnAttachFile.setAlpha(!activity.tempAttachments.isEmpty() ? 0.5f : 1.0f);

            if (!activity.tempAttachments.isEmpty()) {
                String filePath = activity.tempAttachments.get(0);
                java.io.File f = new java.io.File(filePath);
                String name = f.getName();

                LinearLayout chipContainer = new LinearLayout(activity);
                chipContainer.setOrientation(LinearLayout.HORIZONTAL);
                chipContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);
                chipContainer.setBackground(ResponsiveUI.createButtonSelector(activity, ThemeManager.getBgSecondaryColor(activity), 8.0f));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                chipContainer.setLayoutParams(lp);

                TextView chip = new TextView(activity);
                chip.setText((filePath.endsWith(".pdf") || filePath.endsWith(".doc") || filePath.endsWith(".docx") ? "\uD83D\uDCC4 " : "\uD83D\uDDBC ") + name);
                chip.setTextSize(12);
                chip.setTextColor(activity.getColor(R.color.text_primary));
                chip.setPadding(16, 10, 8, 10);
                chip.setSingleLine(true);
                chip.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
                chip.setMarqueeRepeatLimit(-1);
                chip.setHorizontallyScrolling(true);
                chip.setSelected(true);
                LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                chip.setLayoutParams(chipLp);

                ResponsiveUI.setupClickable(chip, false, () -> {
                    try {
                        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", f);
                        android.content.Intent viewIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        viewIntent.setDataAndType(uri, activity.getContentResolver().getType(uri));
                        if (viewIntent.getType() == null) {
                            if (filePath.toLowerCase().endsWith(".pdf")) viewIntent.setDataAndType(uri, "application/pdf");
                            else if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".png")) viewIntent.setDataAndType(uri, "image/*");
                            else viewIntent.setDataAndType(uri, "*/*");
                        }
                        viewIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        activity.startActivity(viewIntent);
                    } catch (Exception e) {
                        Toast.makeText(activity, activity.getString(R.string.auto_cannot_open_file_10), Toast.LENGTH_SHORT).show();
                    }
                });

                TextView closeBtn = new TextView(activity);
                closeBtn.setText(" ✕ ");
                closeBtn.setTextSize(12);
                closeBtn.setTextColor(activity.getColor(R.color.error_red));
                closeBtn.setPadding(8, 10, 16, 10);
                LinearLayout.LayoutParams closeLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                closeBtn.setLayoutParams(closeLp);

                ResponsiveUI.setupClickable(closeBtn, false, () -> {
                    activity.tempAttachments.clear();
                    renderEditorAttachments(activity);
                });

                chipContainer.addView(chip);
                chipContainer.addView(closeBtn);
                activity.attachmentsContainer.addView(chipContainer);
            }
        }
    }
}
