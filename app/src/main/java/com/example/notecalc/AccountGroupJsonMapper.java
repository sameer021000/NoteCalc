package com.example.notecalc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccountGroupJsonMapper {

    public static JSONObject toJSONObject(AccountGroup group) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("title", group.getTitle());
        obj.put("lastModified", group.getLastModified());
        
        JSONArray accountsArray = new JSONArray();
        for (Account account : group.getAccounts()) {
            accountsArray.put(AccountJsonMapper.toJSONObject(account));
        }
        obj.put("accounts", accountsArray);
        obj.put("sortMode", group.getSortMode());
        obj.put("sortAscending", group.isSortAscending());
        obj.put("pinned", group.isPinned());
        obj.put("isArchived", group.isArchived());
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
