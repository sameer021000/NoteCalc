package com.example.notecalc;

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

    public static boolean validateAccountTitle(MainActivity activity, String title) {
        if (title.isEmpty()) {
            Toast.makeText(activity, activity.getString(R.string.auto_list_title_cannot_be_6), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (EditorUIHelper.isDuplicateTitle(activity, title)) {
            Toast.makeText(activity, activity.getString(R.string.auto_a_list_with_this_tit_7), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
