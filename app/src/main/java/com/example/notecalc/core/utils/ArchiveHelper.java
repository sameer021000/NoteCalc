package com.example.notecalc.core.utils;
import com.example.notecalc.accounts.models.*;
import java.util.ArrayList;
import java.util.List;

public class ArchiveHelper {

    public static boolean isShowingArchive = false;

    public static List<AccountGroup> getVisibleGroups(List<AccountGroup> allGroups) {
        List<AccountGroup> visibleGroups = new ArrayList<>();
        for (AccountGroup group : allGroups) {
            if (group.isArchived() == isShowingArchive) {
                visibleGroups.add(group);
            }
        }
        return visibleGroups;
    }

    public static List<Account> getVisibleAccounts(List<Account> allAccounts) {
        List<Account> visibleAccounts = new ArrayList<>();
        for (Account account : allAccounts) {
            if (account.isArchived() == isShowingArchive) {
                visibleAccounts.add(account);
            }
        }
        return visibleAccounts;
    }
}