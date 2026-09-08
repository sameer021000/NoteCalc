package com.example.notecalc.tools.transfer;
import com.example.notecalc.storage.core.*;
import com.example.notecalc.core.ui.*;
import com.example.notecalc.accounts.models.*;
import com.example.notecalc.dashboard.*;
import com.example.notecalc.records.models.Record;
import com.example.notecalc.*;
import android.view.View;
import android.widget.TextView;
import java.util.List;
import java.util.ArrayList;

public class TransferHelper {
    @android.annotation.SuppressLint("SetTextI18n")
    public static void showTransferDialog(MainActivity activity, List<Record> selectedRecords, boolean isCut) {
        List<Account> targetAccounts = StorageHelper.getValidTransferTargets(activity.appStorage, activity.currentEditingAccount);

        List<String> accountNames = new ArrayList<>();
        for (Account a : targetAccounts) {
            accountNames.add(a.getTitle());
        }
        accountNames.sort(String.CASE_INSENSITIVE_ORDER);
        List<String> names = new ArrayList<>();
        names.add("Create New List");
        names.addAll(accountNames);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_dialog_transfer, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        if (dialogRoot != null) {
            dialogRoot.setBackground(ResponsiveUI.createRoundedBg(
                    activity,
                    ThemeManager.getBgSecondaryColor(activity),
                    ThemeManager.getBorderColor(activity),
                    1.5f,
                    12f
            ));
        }

        TextView title = dialogView.findViewById(R.id.dialog_title);
        title.setText(isCut ? "Cut to..." : "Copy to...");

        android.widget.LinearLayout container = dialogView.findViewById(R.id.transfer_list_container);

        for (int i = 0; i < names.size(); i++) {
            final int index = i;
            TextView item = new TextView(activity);
            item.setText(names.get(i));
            item.setTextSize(16f);
            int padding = (int) (16 * activity.getResources().getDisplayMetrics().density);
            item.setPadding(padding, padding, padding, padding);

            if (i == 0) {
                item.setTextColor(ThemeManager.getPrimaryAccentColor(activity));
                item.setTypeface(null, android.graphics.Typeface.BOLD);
                item.setText("+  " + names.get(i));
                item.setBackground(ResponsiveUI.createRippleRoundedBg(
                        activity,
                        ThemeManager.getBgPrimaryColor(activity),
                        ThemeManager.getPrimaryAccentColor(activity),
                        1.5f,
                        6f
                ));
            } else {
                item.setTextColor(activity.getResources().getColor(R.color.text_primary, activity.getTheme()));
                item.setBackground(ResponsiveUI.createRippleRoundedBg(
                        activity,
                        ThemeManager.getBgPrimaryColor(activity),
                        ThemeManager.getBorderColor(activity),
                        1.0f,
                        6f
                ));
            }

            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = (int) (8 * activity.getResources().getDisplayMetrics().density);
            item.setLayoutParams(params);

            ResponsiveUI.setupClickable(item, true, () -> {
                dialog.dismiss();
                if (index == 0) { // Create New List
                    showNewListTitleDialog(activity, selectedRecords, isCut);
                } else {
                    Account target = null;
                    String selectedName = names.get(index);
                    for (Account a : targetAccounts) {
                        if (a.getTitle().equals(selectedName)) {
                            target = a;
                            break;
                        }
                    }
                    if (target != null) {
                        TransferEngine.executeTransfer(activity, selectedRecords, target, isCut);
                    }
                }
            });
            container.addView(item);
        }

        View btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        btnCancel.setBackground(ResponsiveUI.createRippleRoundedBg(
                activity,
                ThemeManager.getBgPrimaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                6f
        ));
        ResponsiveUI.setupClickable(btnCancel, true, dialog::dismiss);

        dialog.show();
    }
public static void showNewListTitleDialog(MainActivity activity, List<Record> selectedRecords, boolean isCut) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_dialog_new_list, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        if (dialogRoot != null) {
            dialogRoot.setBackground(ResponsiveUI.createRoundedBg(
                    activity,
                    ThemeManager.getBgSecondaryColor(activity),
                    ThemeManager.getBorderColor(activity),
                    1.5f,
                    12f
            ));
        }

        final android.widget.EditText input = dialogView.findViewById(R.id.edit_new_list_title);
        input.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgPrimaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                6f
        ));

        View btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        btnCancel.setBackground(ResponsiveUI.createRippleRoundedBg(
                activity,
                ThemeManager.getBgPrimaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                6f
        ));
        ResponsiveUI.setupClickable(btnCancel, true, dialog::dismiss);

        View btnCreate = dialogView.findViewById(R.id.btn_dialog_create);
        btnCreate.setBackground(ResponsiveUI.createRippleRoundedBg(
                activity,
                ThemeManager.getPrimaryAccentColor(activity),
                0,
                0f,
                6f
        ));
        ResponsiveUI.setupClickable(btnCreate, true, () -> {
            String title = input.getText().toString().trim();
            if (title.isEmpty()) {
                android.widget.Toast.makeText(activity, "Title cannot be empty", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            // Check if title exists
            if (StorageHelper.doesAccountExist(activity.appStorage, title)) {
                android.widget.Toast.makeText(activity, "List with activity title already exists", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            Account newAccount = new Account(title);
            activity.appStorage.standaloneAccounts.add(0, newAccount);
            TransferEngine.executeTransfer(activity, selectedRecords, newAccount, isCut);
            DashboardHelper.showDashboard(activity);
        });

        dialog.show();
    }
}