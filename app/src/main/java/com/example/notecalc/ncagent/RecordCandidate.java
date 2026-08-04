package com.example.notecalc.ncagent;

public class RecordCandidate {
    private String description;
    private Double amount;
    private String date;
    private String category;
    private String remarks;
    
    public RecordCandidate() {}

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    
    @Override
    public String toString() {
        return "RecordCandidate{" +
                "description='" + description + '\'' +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", category='" + category + '\'' +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}
