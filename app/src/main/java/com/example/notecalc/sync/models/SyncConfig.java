package com.example.notecalc.sync.models;

import android.content.Context;
import android.content.SharedPreferences;

public class SyncConfig {
    private static final String PREF_NAME = "sync_prefs";
    private static final String KEY_SYNC_ENABLED = "sync_enabled";
    private static final String KEY_SYNC_IMAGES = "sync_images_enabled";
    private static final String KEY_SAF_URI = "saf_uri_string";
    private static final String KEY_LAST_SYNC = "last_sync_timestamp";

    public static boolean isSyncEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_SYNC_ENABLED, false);
    }

    public static void setSyncEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_SYNC_ENABLED, enabled).apply();
    }

    public static boolean isSyncImagesEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_SYNC_IMAGES, false);
    }

    public static void setSyncImagesEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_SYNC_IMAGES, enabled).apply();
    }

    public static String getSafUriString(Context context) {
        return getPrefs(context).getString(KEY_SAF_URI, null);
    }

    public static void setSafUriString(Context context, String uri) {
        getPrefs(context).edit().putString(KEY_SAF_URI, uri).apply();
    }

    public static long getLastSyncTimestamp(Context context) {
        return getPrefs(context).getLong(KEY_LAST_SYNC, 0);
    }

    public static void setLastSyncTimestamp(Context context, long timestamp) {
        getPrefs(context).edit().putLong(KEY_LAST_SYNC, timestamp).apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
