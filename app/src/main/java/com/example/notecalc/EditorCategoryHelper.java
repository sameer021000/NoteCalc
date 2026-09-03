package com.example.notecalc;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EditorCategoryHelper {
    public static void setupCategoryDropdown(MainActivity activity) {
        if (activity.editCategoryField != null) {
            java.util.Set<String> catSet = new java.util.HashSet<>();
            if (activity.currentEditingAccount != null) {
                for (Record r : activity.currentEditingAccount.getRecords()) {
                    if (!r.getCategory().isEmpty()) catSet.add(r.getCategory());
                }
            }
            java.util.List<String> catList = new java.util.ArrayList<>(catSet);
            java.util.Collections.sort(catList);
            android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line, catList) {
                @androidx.annotation.NonNull
                @Override
                public android.view.View getView(int position, @androidx.annotation.Nullable android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
                    android.view.View coreView = super.getView(position, null, parent);
                    
                    LinearLayout container = new LinearLayout(activity);
                    container.setOrientation(LinearLayout.VERTICAL);
                    
                    if (coreView instanceof TextView) {
                        ((TextView) coreView).setTextColor(ThemeManager.getSecondaryAccentColor(activity));
                        int hPad = (int)(12 * activity.getResources().getDisplayMetrics().density);
                        int vPad = (int)(8 * activity.getResources().getDisplayMetrics().density);
                        coreView.setPadding(hPad, vPad, hPad, vPad);
                        coreView.setMinimumHeight(0);
                        android.view.ViewGroup.LayoutParams params = coreView.getLayoutParams();
                        if (params != null) {
                            params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
                            coreView.setLayoutParams(params);
                        }
                    }
                    
                    container.addView(coreView);
                    
                    if (position < getCount() - 1) {
                        View divider = new View(activity);
                        divider.setBackgroundColor(ThemeManager.getBorderColor(activity));
                        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        container.addView(divider, divParams);
                    }
                    
                    container.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    return container;
                }
            };
            activity.editCategoryField.setAdapter(catAdapter);
            
            activity.editCategoryField.setDropDownBackgroundDrawable(ResponsiveUI.createRoundedBg(
                    activity,
                    ThemeManager.getBgSecondaryColor(activity),
                    ThemeManager.getBorderColor(activity),
                    1.0f,
                    8.0f
            ));
            activity.editCategoryField.setDropDownHeight((int) (180 * activity.getResources().getDisplayMetrics().density));
            
            activity.editCategoryField.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    activity.editCategoryField.showDropDown();
                }
            });
            activity.editCategoryField.setOnClickListener(v -> activity.editCategoryField.showDropDown());
        }
    }
}
