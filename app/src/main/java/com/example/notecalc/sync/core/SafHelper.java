package com.example.notecalc.sync.core;

import android.content.Context;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SafHelper {

    public static DocumentFile getRootFolder(Context context, String uriString) {
        if (uriString == null || uriString.isEmpty()) return null;
        try {
            Uri uri = Uri.parse(uriString);
            DocumentFile root = DocumentFile.fromTreeUri(context, uri);
            if (root != null && root.exists() && root.canWrite()) {
                return root;
            }
        } catch (Exception e) {
            android.util.Log.e("SafHelper", "Error accessing root SAF folder", e);
        }
        return null;
    }

    public static DocumentFile getOrCreateDirectory(DocumentFile parent, String dirName) {
        if (parent == null) return null;
        DocumentFile dir = parent.findFile(dirName);
        if (dir != null && dir.isDirectory()) {
            return dir;
        }
        if (dir != null && !dir.isDirectory()) {
            return null; // Name collision with a file
        }
        return parent.createDirectory(dirName);
    }

    public static void writeTextFile(Context context, DocumentFile parent, String fileName, String content) {
        if (parent == null) return;
        try {
            DocumentFile file = parent.findFile(fileName);
            if (file == null) {
                file = parent.createFile("application/json", fileName);
            }
            if (file != null && file.canWrite()) {
                // "wt" mode truncates the file if it already exists
                try (OutputStream os = context.getContentResolver().openOutputStream(file.getUri(), "wt")) {
                    if (os != null) {
                        os.write(content.getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SafHelper", "Failed to write SAF text file", e);
        }
    }

    public static String readTextFile(Context context, DocumentFile file) {
        if (file == null || !file.exists() || !file.canRead()) return null;
        StringBuilder sb = new StringBuilder();
        try (InputStream is = context.getContentResolver().openInputStream(file.getUri());
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            android.util.Log.e("SafHelper", "Failed to read SAF text file", e);
        }
        return null;
    }

    public static List<DocumentFile> listFiles(DocumentFile parent) {
        List<DocumentFile> result = new ArrayList<>();
        if (parent != null && parent.isDirectory()) {
            DocumentFile[] files = parent.listFiles();
            for (DocumentFile f : files) {
                if (f.isFile()) {
                    result.add(f);
                }
            }
        }
        return result;
    }

    public static void copyFileToSaf(Context context, java.io.File sourceFile, DocumentFile destFolder, String mimeType) {
        if (sourceFile == null || !sourceFile.exists() || destFolder == null) return;
        try {
            DocumentFile file = destFolder.findFile(sourceFile.getName());
            if (file == null) {
                file = destFolder.createFile(mimeType != null ? mimeType : "application/octet-stream", sourceFile.getName());
            }
            if (file != null && file.canWrite()) {
                try (InputStream is = new java.io.FileInputStream(sourceFile);
                     OutputStream os = context.getContentResolver().openOutputStream(file.getUri(), "wt")) {
                    if (os != null) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = is.read(buffer)) != -1) {
                            os.write(buffer, 0, read);
                        }
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SafHelper", "Failed to copy file to SAF: " + sourceFile.getName(), e);
        }
    }

    public static void copyFileFromSaf(Context context, DocumentFile sourceSafFile, java.io.File destFolder) {
        if (sourceSafFile == null || !sourceSafFile.exists() || !sourceSafFile.canRead() || destFolder == null) return;
        String fileName = sourceSafFile.getName();
        if (fileName == null) return;
        if (!destFolder.exists() && !destFolder.mkdirs()) return;
        try {
            java.io.File destFile = new java.io.File(destFolder, fileName);
            try (InputStream is = context.getContentResolver().openInputStream(sourceSafFile.getUri());
                 OutputStream os = new java.io.FileOutputStream(destFile)) {
                if (is != null) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SafHelper", "Failed to copy file from SAF: " + fileName, e);
        }
    }
}
