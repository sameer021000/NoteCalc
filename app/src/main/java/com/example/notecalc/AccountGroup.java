package com.example.notecalc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccountGroup {
    private String title;
    private List<Account> accounts;
    private long lastModified;
    private int sortMode = 2; // Default to 2 (Latest)
    private boolean sortAscending = false; // Default to false (Latest first)
    private boolean pinned = false;
    private boolean isArchived = false;

    public AccountGroup(String title) {
        this.title = title;
        this.accounts = new ArrayList<>();
        this.lastModified = System.currentTimeMillis();
    }

    public AccountGroup(String title, List<Account> accounts, long lastModified) {
        this.title = title;
        this.accounts = accounts;
        this.lastModified = lastModified;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.lastModified = System.currentTimeMillis();
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        this.lastModified = System.currentTimeMillis();
    }

    public long getLastModified() {
        return lastModified;
    }

    public void updateLastModified() {
        this.lastModified = System.currentTimeMillis();
    }

    public int getSortMode() {
        return sortMode;
    }

    public void setSortMode(int sortMode) {
        this.sortMode = sortMode;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(boolean sortAscending) {
        this.sortAscending = sortAscending;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        this.isArchived = archived;
        this.lastModified = System.currentTimeMillis();
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("title", title);
        obj.put("lastModified", lastModified);
        
        JSONArray accountsArray = new JSONArray();
        for (Account account : accounts) {
            accountsArray.put(AccountJsonMapper.toJSONObject(account));
        }
        obj.put("accounts", accountsArray);
        obj.put("sortMode", sortMode);
        obj.put("sortAscending", sortAscending);
        obj.put("pinned", pinned);
        obj.put("isArchived", isArchived);
        return obj;
    }

    public static AccountGroup fromJSONObject(JSONObject obj) throws JSONException {
        String title = obj.getString("title");
        long lastModified = obj.optLong("lastModified", System.currentTimeMillis());
        
        List<Account> accounts = new ArrayList<>();
        if (obj.has("accounts")) {
            JSONArray accountsArray = obj.getJSONArray("accounts");
            for (int i = 0; i < accountsArray.length(); i++) {
                accounts.add(AccountJsonMapper.fromJSONObject(accountsArray.getJSONObject(i)));
            }
        }
        AccountGroup group = new AccountGroup(title, accounts, lastModified);
        group.setSortMode(obj.optInt("sortMode", 2));
        group.setSortAscending(obj.optBoolean("sortAscending", false));
        group.setPinned(obj.optBoolean("pinned", false));
        group.setArchived(obj.optBoolean("isArchived", false));
        return group;
    }
}
