package com.example.notecalc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private String title;
    private List<Record> records;
    private long lastModified;
    private boolean pinned;
    private boolean hasBudget;
    private List<Record> budgetRecords;

    public Account(String title) {
        this.title = title;
        this.records = new ArrayList<>();
        this.budgetRecords = new ArrayList<>();
        this.lastModified = System.currentTimeMillis();
        this.pinned = false;
        this.hasBudget = false;
    }

    public Account(String title, List<Record> records, long lastModified) {
        this.title = title;
        this.records = records;
        this.budgetRecords = new ArrayList<>();
        this.lastModified = lastModified;
        this.pinned = false;
        this.hasBudget = false;
    }

    public Account(String title, List<Record> records, long lastModified, boolean pinned) {
        this.title = title;
        this.records = records;
        this.budgetRecords = new ArrayList<>();
        this.lastModified = lastModified;
        this.pinned = pinned;
        this.hasBudget = false;
    }

    public Account(String title, List<Record> records, long lastModified, boolean pinned, boolean hasBudget, List<Record> budgetRecords) {
        this.title = title;
        this.records = records;
        this.lastModified = lastModified;
        this.pinned = pinned;
        this.hasBudget = hasBudget;
        this.budgetRecords = budgetRecords != null ? budgetRecords : new ArrayList<>();
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

    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("title", title);
        obj.put("lastModified", lastModified);
        obj.put("pinned", pinned);
        
        JSONArray recordsArray = new JSONArray();
        for (Record record : records) {
            recordsArray.put(record.toJSONObject());
        }
        obj.put("records", recordsArray);
        
        obj.put("hasBudget", hasBudget);
        JSONArray budgetArray = new JSONArray();
        for (Record record : budgetRecords) {
            budgetArray.put(record.toJSONObject());
        }
        obj.put("budgetRecords", budgetArray);
        
        return obj;
    }

    public static Account fromJSONObject(JSONObject obj) throws JSONException {
        String title = obj.getString("title");
        long lastModified = obj.optLong("lastModified", System.currentTimeMillis());
        boolean pinned = obj.optBoolean("pinned", false);
        
        List<Record> records = new ArrayList<>();
        JSONArray recordsArray = obj.getJSONArray("records");
        for (int i = 0; i < recordsArray.length(); i++) {
            records.add(Record.fromJSONObject(recordsArray.getJSONObject(i)));
        }
        
        boolean hasBudget = obj.optBoolean("hasBudget", false);
        List<Record> budgetRecords = new ArrayList<>();
        if (obj.has("budgetRecords")) {
            JSONArray budgetArray = obj.getJSONArray("budgetRecords");
            for (int i = 0; i < budgetArray.length(); i++) {
                budgetRecords.add(Record.fromJSONObject(budgetArray.getJSONObject(i)));
            }
        }
        
        return new Account(title, records, lastModified, pinned, hasBudget, budgetRecords);
    }
}
