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
                        storage.standaloneAccounts.add(Account.fromJSONObject(array.getJSONObject(i)));
                    }
                } else if (json instanceof JSONObject) {
                    // New format
                    JSONObject obj = (JSONObject) json;
                    if (obj.has("groups")) {
                        JSONArray groupsArray = obj.getJSONArray("groups");
                        for (int i = 0; i < groupsArray.length(); i++) {
                            storage.groups.add(AccountGroup.fromJSONObject(groupsArray.getJSONObject(i)));
                        }
                    }
                    if (obj.has("standaloneAccounts")) {
                        JSONArray accountsArray = obj.getJSONArray("standaloneAccounts");
                        for (int i = 0; i < accountsArray.length(); i++) {
                            storage.standaloneAccounts.add(Account.fromJSONObject(accountsArray.getJSONObject(i)));
                        }
                    }
                }
            }
        } catch (IOException | JSONException e) {
            android.util.Log.e("StorageHelper", "Error loading storage", e);
        }
        return storage;
    }

    public static void saveAppStorage(Context context, AppStorage storage) {
        try {
            JSONObject root = new JSONObject();
            
            JSONArray groupsArray = new JSONArray();
            for (AccountGroup group : storage.groups) {
                groupsArray.put(group.toJSONObject());
            }
            root.put("groups", groupsArray);
            
            JSONArray accountsArray = new JSONArray();
            for (Account account : storage.standaloneAccounts) {
                accountsArray.put(account.toJSONObject());
            }
            root.put("standaloneAccounts", accountsArray);
            
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
