package com.example.notecalc.storage.mappers;
import com.example.notecalc.storage.core.*;
import com.example.notecalc.accounts.models.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppStorageJsonMapper {

    public static JSONObject toJSONObject(AppStorage storage) throws JSONException {
        JSONObject root = new JSONObject();
        JSONArray groupsArray = new JSONArray();
        for (AccountGroup group : storage.groups) {
            groupsArray.put(AccountGroupJsonMapper.toJSONObject(group));
        }
        root.put("groups", groupsArray);

        JSONArray accountsArray = new JSONArray();
        for (Account account : storage.standaloneAccounts) {
            accountsArray.put(AccountJsonMapper.toJSONObject(account));
        }
        root.put("standaloneAccounts", accountsArray);

        JSONArray trashArray = new JSONArray();
        if (storage.trashAccounts != null) {
            for (Account account : storage.trashAccounts) {
                trashArray.put(AccountJsonMapper.toJSONObject(account));
            }
        }
        root.put("trashAccounts", trashArray);

        return root;
    }

    public static AppStorage fromJSONObject(JSONObject obj) throws JSONException {
        AppStorage storage = new AppStorage();
        if (obj.has("groups")) {
            JSONArray groupsArray = obj.getJSONArray("groups");
            for (int i = 0; i < groupsArray.length(); i++) {
                storage.groups.add(AccountGroupJsonMapper.fromJSONObject(groupsArray.getJSONObject(i)));
            }
        }
        if (obj.has("standaloneAccounts")) {
            JSONArray accountsArray = obj.getJSONArray("standaloneAccounts");
            for (int i = 0; i < accountsArray.length(); i++) {
                storage.standaloneAccounts.add(AccountJsonMapper.fromJSONObject(accountsArray.getJSONObject(i)));
            }
        }
        if (obj.has("trashAccounts")) {
            JSONArray trashArray = obj.getJSONArray("trashAccounts");
            for (int i = 0; i < trashArray.length(); i++) {
                storage.trashAccounts.add(AccountJsonMapper.fromJSONObject(trashArray.getJSONObject(i)));
            }
        }
        return storage;
    }
}