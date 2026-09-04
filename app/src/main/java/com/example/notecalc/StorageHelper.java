package com.example.notecalc;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StorageHelper {
    private static final String FILE_NAME = "accounts.json";

    public static AppStorage loadAppStorage(Context context) {
        AppStorage storage = new AppStorage();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            return storage;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            int size = fis.available();
            byte[] bytes = new byte[size];
            int read = fis.read(bytes);
            if (read > 0) {
                String jsonStr = new String(bytes, StandardCharsets.UTF_8);
                Object json = new JSONTokener(jsonStr).nextValue();
                
                if (json instanceof JSONArray) {
                    // Legacy migration: It's an array of accounts
                    JSONArray array = (JSONArray) json;
                    for (int i = 0; i < array.length(); i++) {
                        storage.standaloneAccounts.add(AccountJsonMapper.fromJSONObject(array.getJSONObject(i)));
                    }
                } else if (json instanceof JSONObject) {
                    // New format
                    JSONObject obj = (JSONObject) json;
                    
                    // Use the dedicated mapper to handle all groups and accounts mapping!
                    AppStorage loadedStorage = AppStorageJsonMapper.fromJSONObject(obj);
                    storage.groups.addAll(loadedStorage.groups);
                    storage.standaloneAccounts.addAll(loadedStorage.standaloneAccounts);
                }
            }
        } catch (IOException | JSONException e) {
            android.util.Log.e("StorageHelper", "Error loading storage", e);
        }
        return storage;
    }

    public static void saveAppStorage(Context context, AppStorage storage) {
        try {
            // Let the dedicated mapper handle all the heavy lifting!
            JSONObject root = AppStorageJsonMapper.toJSONObject(storage);
            String jsonStr = root.toString();
            File file = new File(context.getFilesDir(), FILE_NAME);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(jsonStr.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException | JSONException e) {
            android.util.Log.e("StorageHelper", "Error saving storage", e);
        }
    }
}
