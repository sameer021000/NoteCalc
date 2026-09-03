package com.example.notecalc;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class EditorThemeHelper {
    public static void applyEditorTheme(MainActivity activity, View formContainer, View tableHeader, EditText editTitle, EditText editDesc, EditText editAmount, TextView btnDate, TextView btnCancelEdit, TextView btnAdd, TextView btnSave) {
        editTitle.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgSecondaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                6.0f
        ));

        if (formContainer != null) {
            formContainer.setBackground(ResponsiveUI.createRoundedBg(
                    activity,
                    ThemeManager.getBgSecondaryColor(activity),
                    ThemeManager.getBorderColor(activity),
                    1.0f,
                    8.0f
            ));
        }

        tableHeader.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgSecondaryColor(activity),
                0,
                0,
                4.0f
        ));

        editDesc.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgPrimaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                4.0f
        ));

        editAmount.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgPrimaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                4.0f
        ));

        if (activity.editRemarksField != null) {
            activity.editRemarksField.setBackground(ResponsiveUI.createRoundedBg(
                    activity,
                    ThemeManager.getBgPrimaryColor(activity),
                    ThemeManager.getBorderColor(activity),
                    1.0f,
                    4.0f
            ));
        }
        
        if (activity.editCategoryField != null) {
            activity.editCategoryField.setBackground(ResponsiveUI.createRoundedBg(
                    activity,
                    ThemeManager.getBgPrimaryColor(activity),
                    ThemeManager.getBorderColor(activity),
                    1.0f,
                    4.0f
            ));
        }

        btnDate.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgPrimaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                4.0f
        ));

        btnCancelEdit.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                activity.getColor(R.color.error_red),
                activity.getColor(R.color.error_red),
                0f,
                4.0f
        ));
        btnCancelEdit.setTextColor(activity.getColor(R.color.text_on_accent));
        btnCancelEdit.setTypeface(null, android.graphics.Typeface.BOLD);

        btnAdd.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getPrimaryAccentColor(activity),
                0,
                0,
                4.0f
        ));

        btnSave.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getPrimaryAccentColor(activity),
                0,
                0,
                6.0f
        ));
    }
}
