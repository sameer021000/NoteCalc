package com.example.notecalc;

import android.widget.Toast;

public class EditorAttachmentsDialogHelper {
    public static void setupAttachmentsDialog(MainActivity activity) {
        if (activity.btnAttachFile != null) {
            ResponsiveUI.setupClickable(activity.btnAttachFile, true, () -> {
                if (activity.tempAttachments.size() >= 3) {
                    Toast.makeText(activity, activity.getString(R.string.auto_max_3_files_allowed_2), Toast.LENGTH_SHORT).show();
                    return;
                }
                
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
                android.view.View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_dialog_attach_file, null);
                builder.setView(dialogView);
                
                final androidx.appcompat.app.AlertDialog dialog = builder.create();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                }
                
                android.view.View dialogRoot = dialogView.findViewById(R.id.dialog_root);
                dialogRoot.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), 0, 0, 16.0f));
                
                android.widget.TextView btnTakePhoto = dialogView.findViewById(R.id.btn_take_photo);
                android.widget.TextView btnChooseFile = dialogView.findViewById(R.id.btn_choose_file);
                
                btnTakePhoto.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), 0, 0, 8.0f));
                btnChooseFile.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), 0, 0, 8.0f));
                
                ResponsiveUI.setupClickable(btnTakePhoto, false, () -> {
                    dialog.dismiss();
                    try {
                        java.io.File attachmentsDir = new java.io.File(activity.getFilesDir(), "attachments");
                        if (!attachmentsDir.exists()) { boolean ignored = attachmentsDir.mkdirs(); }
                        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
                        java.io.File imageFile = new java.io.File(attachmentsDir, "IMG_" + timeStamp + ".jpg");
                        activity.currentPhotoPath = imageFile.getAbsolutePath();
                        android.net.Uri photoURI = androidx.core.content.FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", imageFile);
                        android.content.Intent takePictureIntent = new android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
                        activity.startActivityForResult(takePictureIntent, MainActivity.REQUEST_CODE_CAMERA);
                    } catch (Exception e) {
                        android.widget.Toast.makeText(activity, "Could not start camera", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
                
                ResponsiveUI.setupClickable(btnChooseFile, false, () -> {
                    dialog.dismiss();
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    String[] mimeTypes = {"image/jpeg", "image/png", "image/jpg", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
                    intent.putExtra(android.content.Intent.EXTRA_MIME_TYPES, mimeTypes);
                    activity.startActivityForResult(intent, MainActivity.REQUEST_CODE_ATTACH);
                });
                
                dialog.show();
            });
        }
    }
}
