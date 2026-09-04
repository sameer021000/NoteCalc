package com.example.notecalc;

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
}
