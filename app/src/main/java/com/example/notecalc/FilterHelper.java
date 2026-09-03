package com.example.notecalc;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class FilterHelper {


    /**
     * Syncs the "select all" header checkbox state based on visible displayRecords selection.
     * States: unchecked (none selected), checked (all selected), indeterminate (partial).
     */
    @android.annotation.SuppressLint("SetTextI18n")

    public static boolean isFilterActive(MainActivity activity) {
        if (activity.recordsAdapter != null && !activity.recordsAdapter.filterCategories.isEmpty()) return true;
        if (activity.currentRecordSearchQuery != null && !activity.currentRecordSearchQuery.trim().isEmpty()) return true;
        if (activity.getFilterDateFrom() != null || activity.getFilterDateTo() != null) return true;
        return activity.getFilterAmountFrom() != null || activity.getFilterAmountTo() != null;
    }

@SuppressWarnings({"Convert2Diamond", "ExtractMethodRecommender"})
    public static void showCategoryFilterDialog(MainActivity activity, Account account, android.widget.ImageView btnFilterIcon) {
        java.util.Set<String> uniqueCats = new java.util.HashSet<>();
        for (Record r : account.getRecords()) {
            if (r.getCategory() != null && !r.getCategory().isEmpty()) {
                uniqueCats.add(r.getCategory());
            }
        }
        if (uniqueCats.isEmpty()) {
            Toast.makeText(activity, activity.getString(R.string.auto_no_categories_availa_1), Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> catList = new ArrayList<>(uniqueCats);
        java.util.Collections.sort(catList);
        
        boolean[] checkedItems = new boolean[catList.size()];
        for (int i = 0; i < catList.size(); i++) {
            if (activity.recordsAdapter != null && activity.recordsAdapter.filterCategories.contains(catList.get(i))) {
                checkedItems[i] = true;
            }
        }

        android.app.Dialog dialog = new android.app.Dialog(activity);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(20 * activity.getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);
        
        TextView title = new TextView(activity);
        title.setText(activity.getString(R.string.auto_filter_by_category_12));
        title.setTextSize(20);
        title.setTextColor(activity.getColor(R.color.text_primary));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, pad);
        root.addView(title);
        
        android.widget.ListView listView = new android.widget.ListView(activity);
        listView.setDividerHeight(0);
        
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(activity, android.R.layout.simple_list_item_multiple_choice, catList) {
            @androidx.annotation.NonNull
            @Override
            public android.view.View getView(int position, @androidx.annotation.Nullable android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                if (view instanceof android.widget.CheckedTextView) {
                    android.widget.CheckedTextView ctv = (android.widget.CheckedTextView) view;
                    ctv.setTextColor(activity.getColor(R.color.text_primary));
                    ctv.setCheckMarkTintList(android.content.res.ColorStateList.valueOf(ThemeManager.getPrimaryAccentColor(activity)));
                    int ipads = (int)(12 * activity.getResources().getDisplayMetrics().density);
                    ctv.setPadding(ipads, ipads, ipads, ipads);
                }
                view.setBackgroundColor(ThemeManager.getBgPrimaryColor(activity));
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setChoiceMode(android.widget.ListView.CHOICE_MODE_MULTIPLE);
        for (int i = 0; i < checkedItems.length; i++) {
            listView.setItemChecked(i, checkedItems[i]);
        }
        
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        root.addView(listView, listParams);
        
        LinearLayout btnLayout = new LinearLayout(activity);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setGravity(android.view.Gravity.END);
        btnLayout.setPadding(0, pad, 0, 0);
        
        android.widget.Button btnClear = new android.widget.Button(activity);
        btnClear.setText(activity.getString(R.string.auto_clear_all_13));
        btnClear.setTextColor(activity.getColor(R.color.text_tertiary));
        btnClear.setBackground(ResponsiveUI.createRippleRoundedBg(activity, android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT, 0f, 4f));
        
        android.widget.Button btnApply = new android.widget.Button(activity);
        btnApply.setText(activity.getString(R.string.auto_apply_14));
        btnApply.setTextColor(ThemeManager.getPrimaryAccentColor(activity));
        btnApply.setBackground(ResponsiveUI.createRippleRoundedBg(activity, android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT, 0f, 4f));
        
        btnLayout.addView(btnClear);
        btnLayout.addView(btnApply);
        root.addView(btnLayout);
        
        dialog.setContentView(root);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 16.0f));
            // Set max height if needed
            dialog.getWindow().setLayout((int)(300 * activity.getResources().getDisplayMetrics().density), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        
        ResponsiveUI.setupClickable(btnApply, true, () -> {
            java.util.Set<String> selected = new java.util.HashSet<>();
            android.util.SparseBooleanArray checked = listView.getCheckedItemPositions();
            for (int i = 0; i < catList.size(); i++) {
                if (checked.get(i)) selected.add(catList.get(i));
            }
            if (activity.recordsAdapter != null) {
                activity.recordsAdapter.setFilterCategories(selected);
            }
            if (selected.isEmpty()) {
                btnFilterIcon.setColorFilter(ThemeManager.getSecondaryAccentColor(activity));
            } else {
                btnFilterIcon.setColorFilter(ThemeManager.getPrimaryAccentColor(activity));
            }
            dialog.dismiss();
        });
        
        ResponsiveUI.setupClickable(btnClear, true, () -> {
            if (activity.recordsAdapter != null) {
                activity.recordsAdapter.setFilterCategories(new java.util.HashSet<>());
            }
            btnFilterIcon.setColorFilter(ThemeManager.getSecondaryAccentColor(activity));
            dialog.dismiss();
        });
        
        dialog.show();
    }
public static void showDateRangeFilterDialog(MainActivity activity) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_date_range_dialog, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        TextView tvFrom = dialogView.findViewById(R.id.dialog_date_from);
        TextView tvTo = dialogView.findViewById(R.id.dialog_date_to);
        TextView btnClear = dialogView.findViewById(R.id.btn_dialog_clear);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnApply = dialogView.findViewById(R.id.btn_dialog_apply);

        // Style dialog
        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.5f, 12f));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 6f));
        tvFrom.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        tvTo.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        btnClear.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        btnCancel.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        btnApply.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), ThemeManager.getPrimaryAccentColor(activity), 0f, 4f));
        btnApply.setTextColor(activity.getColor(R.color.text_primary));

        // Track temp selections for activity dialog session
        final String[] tempFrom = {activity.getFilterDateFrom()};
        final String[] tempTo = {activity.getFilterDateTo()};

        // Populate with current filter values if active
        tvFrom.setText(tempFrom[0] != null ? tempFrom[0] : "Select Date");
        tvTo.setText(tempTo[0] != null ? tempTo[0] : "Select Date");

        // Helper to pick a date and update a TextView
        Runnable pickFrom = () -> {
            Calendar cal = Calendar.getInstance();
            if (tempFrom[0] != null) {
                try {
                    java.util.Date parsed = new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(tempFrom[0]);
                    if (parsed != null) cal.setTime(parsed);
                } catch (Exception ignored) {}
            }
            new android.app.DatePickerDialog(activity, (view, year, month, day) -> {
                String picked = String.format(Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year);
                tempFrom[0] = picked;
                tvFrom.setText(picked);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        };
        Runnable pickTo = () -> {
            Calendar cal = Calendar.getInstance();
            if (tempTo[0] != null) {
                try {
                    java.util.Date parsed = new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(tempTo[0]);
                    if (parsed != null) cal.setTime(parsed);
                } catch (Exception ignored) {}
            }
            new android.app.DatePickerDialog(activity, (view, year, month, day) -> {
                String picked = String.format(Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year);
                tempTo[0] = picked;
                tvTo.setText(picked);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        };

        tvFrom.setOnClickListener(v -> pickFrom.run());
        tvTo.setOnClickListener(v -> pickTo.run());

        ResponsiveUI.setupClickable(btnClear, true, () -> {
            activity.setFilterDateFrom(null);
            activity.setFilterDateTo(null);
            if (activity.recordsAdapter != null) activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            EditorSortHelper.updateDateHeaderIndicator(activity);
            dialog.dismiss();
        });
        ResponsiveUI.setupClickable(btnCancel, true, dialog::dismiss);
        ResponsiveUI.setupClickable(btnApply, true, () -> {
            activity.setFilterDateFrom(tempFrom[0]);
            activity.setFilterDateTo(tempTo[0]);
            if (activity.recordsAdapter != null) activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            EditorSortHelper.updateDateHeaderIndicator(activity);
            dialog.dismiss();
        });

        ResponsiveUI.applyResponsiveness(dialogView);
        dialog.show();
    }
public static void showAmountRangeFilterDialog(MainActivity activity) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_amount_range_dialog, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        android.widget.EditText etFrom = dialogView.findViewById(R.id.dialog_amount_from);
        android.widget.EditText etTo = dialogView.findViewById(R.id.dialog_amount_to);
        TextView btnClear = dialogView.findViewById(R.id.btn_dialog_clear);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnApply = dialogView.findViewById(R.id.btn_dialog_apply);

        // Style dialog
        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.5f, 12f));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 6f));
        etFrom.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        etTo.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        btnClear.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        btnCancel.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        btnApply.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), ThemeManager.getPrimaryAccentColor(activity), 0f, 4f));
        btnApply.setTextColor(activity.getColor(R.color.text_primary));

        // Populate with current filter values if active
        if (activity.getFilterAmountFrom() != null) etFrom.setText(String.format(Locale.getDefault(), "%.2f", activity.getFilterAmountFrom()));
        if (activity.getFilterAmountTo() != null) etTo.setText(String.format(Locale.getDefault(), "%.2f", activity.getFilterAmountTo()));

        ResponsiveUI.setupClickable(btnClear, true, () -> {
            activity.setFilterAmountFrom(null);
            activity.setFilterAmountTo(null);
            if (activity.recordsAdapter != null) activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            EditorSortHelper.updateAmountHeaderIndicator(activity);
            dialog.dismiss();
        });
        ResponsiveUI.setupClickable(btnCancel, true, dialog::dismiss);
        ResponsiveUI.setupClickable(btnApply, true, () -> {
            String fromStr = etFrom.getText().toString().trim();
            String toStr = etTo.getText().toString().trim();
            activity.setFilterAmountFrom(fromStr.isEmpty() ? null : Double.parseDouble(fromStr));
            activity.setFilterAmountTo(toStr.isEmpty() ? null : Double.parseDouble(toStr));
            if (activity.recordsAdapter != null) activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            EditorSortHelper.updateAmountHeaderIndicator(activity);
            dialog.dismiss();
        });

        ResponsiveUI.applyResponsiveness(dialogView);
        dialog.show();
    }
}
