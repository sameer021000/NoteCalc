package com.example.notecalc.ncagent.parser;

public class ContextManager {
    private String currentDateContext = null;
    private String currentCategoryContext = null;

    public void setDateContext(String dateText) {
        this.currentDateContext = dateText;
    }

    public void setCategoryContext(String categoryText) {
        this.currentCategoryContext = categoryText;
    }

    public String getCurrentDateContext() {
        return currentDateContext;
    }

    public String getCurrentCategoryContext() {
        return currentCategoryContext;
    }

    public void clear() {
        currentDateContext = null;
        currentCategoryContext = null;
    }
}
