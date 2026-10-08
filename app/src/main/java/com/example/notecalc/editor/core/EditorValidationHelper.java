package com.example.notecalc.editor.core;
import com.example.notecalc.accounts.models.*;
import com.example.notecalc.*;
import android.widget.Toast;

public class EditorValidationHelper {

    public static Double validateRecordInput(MainActivity activity, String desc, String amountStr) {
        if (desc.isEmpty()) {
            Toast.makeText(activity, activity.getString(R.string.auto_please_enter_a_descr_3), Toast.LENGTH_SHORT).show();
            return null;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(activity, activity.getString(R.string.auto_amount_must_be_posit_4), Toast.LENGTH_SHORT).show();
                return null;
            }
            return amount;
        } catch (NumberFormatException e) {
            Toast.makeText(activity, activity.getString(R.string.auto_please_enter_a_valid_5), Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    public static boolean isDuplicateTitle(MainActivity activity, String title, AccountGroup targetGroup) {
        String trimmedTitle = title.trim();
        String originalTitle = (activity.currentEditingAccount != null) ? activity.originalTitle : null;

        if (targetGroup != null) {
            // Check within the specific group block
            for (Account acc : targetGroup.getAccounts()) {
                if (acc.getTitle().equalsIgnoreCase(originalTitle)) continue;
                if (acc.getTitle().equalsIgnoreCase(trimmedTitle)) return true;
            }
        } else {
            // Check within the dashboard block (standalone accounts + groups)
            for (Account acc : activity.appStorage.standaloneAccounts) {
                if (acc.getTitle().equalsIgnoreCase(originalTitle)) continue;
                if (acc.getTitle().equalsIgnoreCase(trimmedTitle)) return true;
            }
            for (AccountGroup group : activity.appStorage.groups) {
                if (group.getTitle().equalsIgnoreCase(trimmedTitle)) return true; // Groups don't have originalTitle here since we're validating an Account, not a Group. Wait, if we are renaming a group, maybe we do. We will assume group renaming validation is handled elsewhere or uses this. If validateAccountTitle is just for lists, this works.
            }
        }
        return false;
    }

    public static boolean validateAccountTitle(MainActivity activity, String title, AccountGroup targetGroup) {
        if (title.isEmpty()) {
            Toast.makeText(activity, activity.getString(R.string.auto_list_title_cannot_be_6), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (isDuplicateTitle(activity, title, targetGroup)) {
            String blockName = targetGroup != null ? "the group '" + targetGroup.getTitle() + "'" : "the Dashboard";
            Toast.makeText(activity, "A list with this name already exists in " + blockName + ". Please choose a different name.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}