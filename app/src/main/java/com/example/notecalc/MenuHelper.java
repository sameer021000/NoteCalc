package com.example.notecalc;

import android.view.View;

public class MenuHelper {

    public static void showAccountPopupMenu(MainActivity activity, View anchor, Account account) {
        MenuAccountHelper.showPopupMenu(activity, anchor, account);
    }

    public static void showGroupPopupMenu(MainActivity activity, View anchor, AccountGroup group) {
        MenuGroupHelper.showPopupMenu(activity, anchor, group);
    }

    public static void showBulkActionsMenu(MainActivity activity, View anchor) {
        MenuBulkActionsHelper.showMenu(activity, anchor);
    }
}
