package com.example.notecalc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccountJsonMapper {

    public static JSONObject toJSONObject(Account account) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("title", account.getTitle());
        obj.put("lastModified", account.getLastModified());
        obj.put("pinned", account.isPinned());
        obj.put("isArchived", account.isArchived());
        
        JSONArray recordsArray = new JSONArray();
        for (Record record : account.getRecords()) {
            recordsArray.put(RecordJsonMapper.toJSONObject(record));
        }
        obj.put("records", recordsArray);
        
        obj.put("hasBudget", account.hasBudget());
        JSONArray budgetArray = new JSONArray();
        for (Record record : account.getBudgetRecords()) {
            budgetArray.put(RecordJsonMapper.toJSONObject(record));
        }
        obj.put("budgetRecords", budgetArray);
        
        return obj;
    }

    public static Account fromJSONObject(JSONObject obj) throws JSONException {
        String title = obj.getString("title");
        long lastModified = obj.optLong("lastModified", System.currentTimeMillis());
        boolean pinned = obj.optBoolean("pinned", false);
        boolean isArchived = obj.optBoolean("isArchived", false);
        
        List<Record> records = new ArrayList<>();
        JSONArray recordsArray = obj.getJSONArray("records");
        for (int i = 0; i < recordsArray.length(); i++) {
            records.add(RecordJsonMapper.fromJSONObject(recordsArray.getJSONObject(i)));
        }
        
        boolean hasBudget = obj.optBoolean("hasBudget", false);
        List<Record> budgetRecords = new ArrayList<>();
        if (obj.has("budgetRecords")) {
            JSONArray budgetArray = obj.getJSONArray("budgetRecords");
            for (int i = 0; i < budgetArray.length(); i++) {
                budgetRecords.add(RecordJsonMapper.fromJSONObject(budgetArray.getJSONObject(i)));
            }
        }
        
        Account acc = new Account(title, records, lastModified, pinned, hasBudget, budgetRecords);
        acc.setArchived(isArchived);
        return acc;
    }
}
