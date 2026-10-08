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
        return root;
    }

    private static String resolveConflict(String title, java.util.Set<String> existingNames) {
        String newTitle = title;
        int counter = 1;
        while (existingNames.contains(newTitle.trim().toLowerCase())) {
            newTitle = title + " (" + counter + ")";
            counter++;
        }
        return newTitle;
    }

    public static AppStorage fromJSONObject(JSONObject obj) throws JSONException {
        AppStorage storage = new AppStorage();
        java.util.Set<String> dashboardNames = new java.util.HashSet<>();
        
        if (obj.has("groups")) {
            JSONArray groupsArray = obj.getJSONArray("groups");
            for (int i = 0; i < groupsArray.length(); i++) {
                AccountGroup group = AccountGroupJsonMapper.fromJSONObject(groupsArray.getJSONObject(i));
                String resolvedGroupTitle = resolveConflict(group.getTitle(), dashboardNames);
                if (!resolvedGroupTitle.equals(group.getTitle())) {
                    group.setTitle(resolvedGroupTitle);
                }
                dashboardNames.add(group.getTitle().trim().toLowerCase());
                
                java.util.Set<String> groupNames = new java.util.HashSet<>();
                for (Account account : group.getAccounts()) {
                    String resolvedAccTitle = resolveConflict(account.getTitle(), groupNames);
                    if (!resolvedAccTitle.equals(account.getTitle())) {
                        account.setTitle(resolvedAccTitle);
                    }
                    groupNames.add(account.getTitle().trim().toLowerCase());
                }
                
                storage.groups.add(group);
            }
        }
        if (obj.has("standaloneAccounts")) {
            JSONArray accountsArray = obj.getJSONArray("standaloneAccounts");
            for (int i = 0; i < accountsArray.length(); i++) {
                Account account = AccountJsonMapper.fromJSONObject(accountsArray.getJSONObject(i));
                String resolvedAccTitle = resolveConflict(account.getTitle(), dashboardNames);
                if (!resolvedAccTitle.equals(account.getTitle())) {
                    account.setTitle(resolvedAccTitle);
                }
                dashboardNames.add(account.getTitle().trim().toLowerCase());
                storage.standaloneAccounts.add(account);
            }
        }
        return storage;
    }
}