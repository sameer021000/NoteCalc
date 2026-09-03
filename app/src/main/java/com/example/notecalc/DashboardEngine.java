package com.example.notecalc;

import java.util.ArrayList;
import java.util.List;

public class DashboardEngine {
    public static DashboardResult process(AppStorage appStorage, AccountGroup currentViewGroup, int sortMode, boolean dashboardSortAsc, boolean groupSortAsc) {
        List<Object> combinedGroups = new ArrayList<>();
        List<Object> combinedAccounts = new ArrayList<>();
        
        boolean isListEmpty = appStorage.groups.isEmpty() && appStorage.standaloneAccounts.isEmpty();

        if (currentViewGroup != null) {
            combinedAccounts.addAll(applyDashboardSort(ArchiveHelper.getVisibleAccounts(currentViewGroup.getAccounts()), sortMode, dashboardSortAsc));
        } else {
            List<AccountGroup> sortedGroups = new ArrayList<>(ArchiveHelper.getVisibleGroups(appStorage.groups));
            sortedGroups.sort((a, b) -> {
                if (a.isPinned() != b.isPinned()) return a.isPinned() ? -1 : 1;
                int titleCompare = a.getTitle().compareToIgnoreCase(b.getTitle());
                return groupSortAsc ? titleCompare : -titleCompare;
            });
            combinedGroups.addAll(sortedGroups);
            combinedAccounts.addAll(applyDashboardSort(ArchiveHelper.getVisibleAccounts(appStorage.standaloneAccounts), sortMode, dashboardSortAsc));
        }

        // hasGroups and hasAccounts will be calculated by the View Binder after setting the adapters' filters
        return new DashboardResult(combinedGroups, combinedAccounts, isListEmpty, false, false);
    }

    private static List<Account> applyDashboardSort(List<Account> source, int mode, boolean asc) {
        List<Account> sorted = new ArrayList<>(source);
        
        sorted.sort((a, b) -> {
            if (a.isPinned() != b.isPinned()) {
                return a.isPinned() ? -1 : 1;
            }
            
            int result;
            if (mode == 0) { 
                result = a.getTitle().compareToIgnoreCase(b.getTitle());
            } else if (mode == 1) { 
                result = Double.compare(a.calculateTotal(), b.calculateTotal());
            } else { 
                result = Long.compare(a.getLastModified(), b.getLastModified());
            }
            return asc ? result : -result;
        });
        return sorted;
    }
}
