package com.example.notecalc;

import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.List;
import java.util.ArrayList;

public class FilterCategoryHelper {

    @SuppressWarnings({"Convert2Diamond", "ExtractMethodRecommender"})
    public static void showDialog(MainActivity activity, Account account, android.widget.ImageView btnFilterIcon) {
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
}
