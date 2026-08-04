package com.example.notecalc;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppStorage {
    public List<AccountGroup> groups = new ArrayList<>();
    public List<Account> standaloneAccounts = new ArrayList<>();

    public JSONObject toJSONObject() throws JSONException {
        JSONObject root = new JSONObject();
        JSONArray groupsArray = new JSONArray();
        for (AccountGroup group : groups) {
            groupsArray.put(group.toJSONObject());
        }
        root.put("groups", groupsArray);
        
        JSONArray accountsArray = new JSONArray();
        for (Account account : standaloneAccounts) {
            accountsArray.put(account.toJSONObject());
        }
        root.put("standaloneAccounts", accountsArray);
        return root;
    }

    public static AppStorage fromJSONObject(JSONObject obj) throws JSONException {
        AppStorage storage = new AppStorage();
        if (obj.has("groups")) {
            JSONArray groupsArray = obj.getJSONArray("groups");
            for (int i = 0; i < groupsArray.length(); i++) {
                storage.groups.add(AccountGroup.fromJSONObject(groupsArray.getJSONObject(i)));
            }
        }
        if (obj.has("standaloneAccounts")) {
            JSONArray accountsArray = obj.getJSONArray("standaloneAccounts");
            for (int i = 0; i < accountsArray.length(); i++) {
                storage.standaloneAccounts.add(Account.fromJSONObject(accountsArray.getJSONObject(i)));
            }
        }
        return storage;
    }
}
