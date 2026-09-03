package com.example.notecalc;

import java.util.List;

public class DashboardResult {
    public final List<Object> processedGroups;
    public final List<Object> processedAccounts;
    public final boolean isListEmpty;
    public final boolean hasGroups;
    public final boolean hasAccounts;

    public DashboardResult(List<Object> processedGroups, List<Object> processedAccounts, boolean isListEmpty, boolean hasGroups, boolean hasAccounts) {
        this.processedGroups = processedGroups;
        this.processedAccounts = processedAccounts;
        this.isListEmpty = isListEmpty;
        this.hasGroups = hasGroups;
        this.hasAccounts = hasAccounts;
    }
}
