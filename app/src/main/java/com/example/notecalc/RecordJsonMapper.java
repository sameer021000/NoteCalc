package com.example.notecalc;

import org.json.JSONException;
import org.json.JSONObject;

public class RecordJsonMapper {

    public static JSONObject toJSONObject(Record record) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("description", record.getDescription());
        obj.put("amount", record.getAmount());
        obj.put("date", record.getDate());
        obj.put("remarks", record.getRemarks());
        obj.put("category", record.getCategory());
        obj.put("originalIndex", record.getOriginalIndex());
        obj.put("timestampMillis", record.getTimestampMillis());
        org.json.JSONArray attachmentsArray = new org.json.JSONArray();
        if (record.getAttachments() != null) {
            for (String att : record.getAttachments()) {
                attachmentsArray.put(att);
            }
        }
        obj.put("attachments", attachmentsArray);
        return obj;
    }

    public static Record fromJSONObject(JSONObject obj) throws JSONException {
        String description = obj.getString("description");
        double amount = obj.getDouble("amount");
        String date = obj.getString("date");
        date = AppUtils.formatToDdMmYyyy(date);
        String remarks = obj.optString("remarks", "");
        String category = obj.optString("category", "");
        int originalIndex = obj.optInt("originalIndex", -1);
        long timestampMillis = obj.optLong("timestampMillis", 0);
        Record r = new Record(description, amount, date);
        r.setRemarks(remarks);
        r.setCategory(category);
        r.setOriginalIndex(originalIndex);
        r.setTimestampMillis(timestampMillis);
        
        org.json.JSONArray attachmentsArray = obj.optJSONArray("attachments");
        if (attachmentsArray != null) {
            java.util.List<String> atts = new java.util.ArrayList<>();
            for (int i = 0; i < attachmentsArray.length(); i++) {
                atts.add(attachmentsArray.optString(i));
            }
            r.setAttachments(atts);
        }
        
        return r;
    }
}
