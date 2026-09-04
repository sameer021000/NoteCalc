package com.example.notecalc;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private String title;
    private List<Record> records;
    private long lastModified;
    private boolean pinned;
    private boolean hasBudget;
    private List<Record> budgetRecords;
    private boolean isArchived;

    public Account(String title) {
        this.title = title;
        this.records = new ArrayList<>();
        this.budgetRecords = new ArrayList<>();
        this.lastModified = System.currentTimeMillis();
        this.pinned = false;
        this.hasBudget = false;
        this.isArchived = false;
    }

    public Account(String title, List<Record> records, long lastModified) {
        this.title = title;
        this.records = records;
        this.budgetRecords = new ArrayList<>();
        this.lastModified = lastModified;
        this.pinned = false;
        this.hasBudget = false;
        this.isArchived = false;
    }

    public Account(String title, List<Record> records, long lastModified, boolean pinned) {
        this.title = title;
        this.records = records;
        this.budgetRecords = new ArrayList<>();
        this.lastModified = lastModified;
        this.pinned = pinned;
        this.hasBudget = false;
        this.isArchived = false;
    }

    public Account(String title, List<Record> records, long lastModified, boolean pinned, boolean hasBudget, List<Record> budgetRecords) {
        this.title = title;
        this.records = records;
        this.lastModified = lastModified;
        this.pinned = pinned;
        this.hasBudget = hasBudget;
        this.budgetRecords = budgetRecords != null ? budgetRecords : new ArrayList<>();
        this.isArchived = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.lastModified = System.currentTimeMillis();
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
        this.lastModified = System.currentTimeMillis();
    }

    public long getLastModified() {
        return lastModified;
    }

    public void updateLastModified() {
        this.lastModified = System.currentTimeMillis();
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

    public double calculateTotal() {
        double total = 0;
        for (Record record : records) {
            total += record.getAmount();
        }
        return total;
    }

    public boolean hasBudget() {
        return hasBudget;
    }

    public void setHasBudget(boolean hasBudget) {
        this.hasBudget = hasBudget;
        this.lastModified = System.currentTimeMillis();
    }

    public List<Record> getBudgetRecords() {
        return budgetRecords;
    }

    public void setBudgetRecords(List<Record> budgetRecords) {
        this.budgetRecords = budgetRecords;
        this.lastModified = System.currentTimeMillis();
    }

    public double calculateTotalBudget() {
        double total = 0;
        for (Record record : budgetRecords) {
            total += record.getAmount();
        }
        return total;
    }

    public double calculateRemainingPurse() {
        return calculateTotalBudget() - calculateTotal();
    }

}
