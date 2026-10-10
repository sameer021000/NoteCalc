package com.example.notecalc.accounts.models;
import java.util.ArrayList;
import java.util.List;

public class AccountGroup {
    private String uid = java.util.UUID.randomUUID().toString();
    private String title;
    private List<Account> accounts;
    private long lastModified;
    private int sortMode = 2; // Default to 2 (Latest)
    private boolean sortAscending = false; // Default to false (Latest first)
    private boolean pinned = false;
    private boolean isArchived = false;
    private boolean isDeleted = false;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public AccountGroup(String title) {
        this.title = title;
        this.accounts = new ArrayList<>();
        this.lastModified = System.currentTimeMillis();
        this.isDeleted = false;
    }

    public AccountGroup(String title, List<Account> accounts, long lastModified) {
        this.title = title;
        this.accounts = accounts;
        this.lastModified = lastModified;
        this.isDeleted = false;
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

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
        this.lastModified = System.currentTimeMillis();
    }

}