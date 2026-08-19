package com.example.notecalc;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Record {
    private String description;
    private double amount;
    private String date;
    private int originalIndex = -1;
    private java.util.List<String> attachments = new java.util.ArrayList<>();
    private String remarks = "";
    private boolean selected = false;
    private String category = "";
    private long timestampMillis;

    public Record(String description, double amount, String date) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.timestampMillis = System.currentTimeMillis();
    }
    
    public long getTimestampMillis() { return timestampMillis; }
    public void setTimestampMillis(long timestampMillis) { this.timestampMillis = timestampMillis; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

        public java.util.List<String> getAttachments() { return attachments; }
    public void setAttachments(java.util.List<String> attachments) { this.attachments = attachments != null ? attachments : new java.util.ArrayList<>(); }

    public int getOriginalIndex() {
        return originalIndex;
    }

    public void setOriginalIndex(int originalIndex) {
        this.originalIndex = originalIndex;
    }

    public String getRemarks() {
        return remarks == null ? "" : remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }


    public String getCategory() {
        return category == null ? "" : category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("description", description);
        obj.put("amount", amount);
        obj.put("date", date);
        obj.put("remarks", getRemarks());
        obj.put("category", getCategory());
        obj.put("originalIndex", originalIndex);
        org.json.JSONArray attachmentsArray = new org.json.JSONArray();
        if (attachments != null) {
            for (String att : attachments) {
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
        date = formatToDdMmYyyy(date);
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

    private static String formatToDdMmYyyy(String dateStr) {
        if (dateStr == null) return "";
        if (dateStr.matches("\\d{2}-\\d{2}-\\d{4}")) {
            return dateStr;
        }
        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                Date date = parser.parse(dateStr);
                if (date != null) {
                    return formatter.format(date);
                }
            } catch (Exception ignored) {}
        }
        return dateStr;
    }
}
