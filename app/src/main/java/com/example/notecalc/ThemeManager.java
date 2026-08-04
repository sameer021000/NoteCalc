package com.example.notecalc;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREF_NAME = "ThemePrefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_ACCENT_COLOR = "accent_color";

    public static final int MODE_SYSTEM = 0;
    public static final int MODE_DARK = 1;
    public static final int MODE_LIGHT = 2;

    public static final String ACCENT_BLUE = "Blue";
    public static final String ACCENT_GREEN = "Green";
    public static final String ACCENT_PURPLE = "Purple";
    public static final String ACCENT_YELLOW = "Yellow";
    public static final String ACCENT_ORANGE = "Orange";
    public static final String ACCENT_PINK = "Pink";

    public static void applyTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // 1. Apply Light/Dark Mode
        int mode = prefs.getInt(KEY_THEME_MODE, MODE_SYSTEM);
        switch (mode) {
            case MODE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case MODE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        // 2. Apply Accent Color Theme to the Context
        String accent = prefs.getString(KEY_ACCENT_COLOR, ACCENT_BLUE);
        int themeId;
        switch (accent) {
            case ACCENT_GREEN: themeId = R.style.Theme_NoteCalc_Green; break;
            case ACCENT_PURPLE: themeId = R.style.Theme_NoteCalc_Purple; break;
            case ACCENT_YELLOW: themeId = R.style.Theme_NoteCalc_Yellow; break;
            case ACCENT_ORANGE: themeId = R.style.Theme_NoteCalc_Orange; break;
            case ACCENT_PINK: themeId = R.style.Theme_NoteCalc_Pink; break;
            case ACCENT_BLUE:
            default:
                themeId = R.style.Theme_NoteCalc_Blue;
                break;
        }
        context.setTheme(themeId);
    }

    public static void setDarkMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
        applyTheme(context);
    }

    public static int getDarkMode(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_THEME_MODE, MODE_SYSTEM);
    }

    public static void setAccentColor(Context context, String accent) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ACCENT_COLOR, accent).apply();
        applyTheme(context);
    }

    public static String getAccentColorName(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_ACCENT_COLOR, ACCENT_BLUE);
    }

    // Resolves ?attr/colorAccentPrimary for current context
    public static int getPrimaryAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccentPrimary, typedValue, true);
        return typedValue.data;
    }

    // Resolves ?attr/colorAccentSecondary for current context
    public static int getSecondaryAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccentSecondary, typedValue, true);
        return typedValue.data;
    }

    public static int getBgPrimaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorBgPrimary, typedValue, true);
        return typedValue.data;
    }
    public static int getBgSecondaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorBgSecondary, typedValue, true);
        return typedValue.data;
    }
    public static int getBgTertiaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorBgTertiary, typedValue, true);
        return typedValue.data;
    }
    public static int getBorderColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorBorder, typedValue, true);
        return typedValue.data;
    }
}
