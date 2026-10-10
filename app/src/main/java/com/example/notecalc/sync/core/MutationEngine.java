package com.example.notecalc.sync.core;
import com.example.notecalc.accounts.models.Account;
import com.example.notecalc.accounts.models.AccountGroup;
import com.example.notecalc.storage.core.AppStorage;
import java.util.List;

public class MutationEngine {

    /**
     * Performs a two-way sync deep merge between Local and Remote storage.
     * It uses the lastSyncTimestamp to infer deletions without needing tombstones.
     * 
     * @return true if the merged result is different from remote (needs push), false otherwise.
     */
    public static boolean mergeStorage(android.content.Context context, AppStorage local, AppStorage remote) {
        long lastSyncTimestamp = com.example.notecalc.sync.models.SyncConfig.getLastSyncTimestamp(context);
        boolean needsPush = false;
        
        // 1. Merge Standalone Accounts
        List<Account> mergedStandalone = new java.util.ArrayList<>();
        
        // Check remote standalone accounts
        for (Account rAcc : remote.standaloneAccounts) {
            Account lAcc = findAccountInList(local.standaloneAccounts, rAcc);
            if (lAcc != null) {
                // Exists in both
                if (lAcc.getLastModified() >= rAcc.getLastModified()) {
                    mergedStandalone.add(lAcc);
                    if (lAcc.getLastModified() > rAcc.getLastModified()) needsPush = true;
                } else {
                    mergedStandalone.add(rAcc);
                }
            } else {
                // Exists in Remote, not in Local
                if (rAcc.getLastModified() > lastSyncTimestamp) {
                    // Added remotely after last sync -> Pull it
                    mergedStandalone.add(rAcc);
                } else {
                    // Existed remotely before last sync, but missing locally -> Deleted locally -> Push deletion
                    needsPush = true;
                }
            }
        }
        
        // Check local standalone accounts missing from remote
        for (Account lAcc : local.standaloneAccounts) {
            Account rAcc = findAccountInList(remote.standaloneAccounts, lAcc);
            if (rAcc == null) {
                // Exists in Local, not in Remote
                if (lAcc.getLastModified() > lastSyncTimestamp) {
                    // Added locally after last sync -> Push it
                    mergedStandalone.add(lAcc);
                    needsPush = true;
                }
                // Else: Existed locally before last sync, but missing remotely -> Deleted remotely -> Drop it
            }
        }
        
        // 2. Merge Groups
        List<AccountGroup> mergedGroups = new java.util.ArrayList<>();
        
        for (AccountGroup rGroup : remote.groups) {
            AccountGroup lGroup = findGroup(local.groups, rGroup);
            if (lGroup != null) {
                // Exists in both, merge accounts inside the group
                AccountGroup mergedGroup = new AccountGroup(lGroup.getTitle());
                mergedGroup.setUid(lGroup.getUid());
                mergedGroup.setDeleted(lGroup.getLastModified() >= rGroup.getLastModified() ? lGroup.isDeleted() : rGroup.isDeleted());
                mergedGroup.setPinned(lGroup.getLastModified() >= rGroup.getLastModified() ? lGroup.isPinned() : rGroup.isPinned());
                mergedGroup.setArchived(lGroup.getLastModified() >= rGroup.getLastModified() ? lGroup.isArchived() : rGroup.isArchived());
                mergedGroup.setSortMode(lGroup.getLastModified() >= rGroup.getLastModified() ? lGroup.getSortMode() : rGroup.getSortMode());
                mergedGroup.setSortAscending(lGroup.getLastModified() >= rGroup.getLastModified() ? lGroup.isSortAscending() : rGroup.isSortAscending());
                // Force update last modified to the newest
                if (rGroup.getLastModified() > lGroup.getLastModified()) {
                    mergedGroup.updateLastModified(); // We'll just bump it
                }
                
                // Merge accounts inside this group
                List<Account> mergedAccounts = mergeAccountsList(lGroup.getAccounts(), rGroup.getAccounts(), lastSyncTimestamp);
                mergedGroup.setAccounts(mergedAccounts);
                
                // Check if we need push (if local had newer stuff)
                if (lGroup.getLastModified() > rGroup.getLastModified() || hasLocalNewerAccounts(lGroup.getAccounts(), rGroup.getAccounts())) {
                    needsPush = true;
                }
                
                mergedGroups.add(mergedGroup);
            } else {
                // Group exists in Remote, not Local
                if (rGroup.getLastModified() > lastSyncTimestamp) {
                    mergedGroups.add(rGroup);
                } else {
                    needsPush = true; // Deleted locally
                }
            }
        }
        
        for (AccountGroup lGroup : local.groups) {
            AccountGroup rGroup = findGroup(remote.groups, lGroup);
            if (rGroup == null) {
                // Exists in Local, not Remote
                if (lGroup.getLastModified() > lastSyncTimestamp) {
                    mergedGroups.add(lGroup);
                    needsPush = true;
                }
            }
        }
        
        // Apply merged results back to local storage object
        local.standaloneAccounts.clear();
        local.standaloneAccounts.addAll(mergedStandalone);
        
        local.groups.clear();
        local.groups.addAll(mergedGroups);
        
        return needsPush;
    }
    
    private static List<Account> mergeAccountsList(List<Account> localList, List<Account> remoteList, long lastSyncTimestamp) {
        List<Account> merged = new java.util.ArrayList<>();
        for (Account rAcc : remoteList) {
            Account lAcc = findAccountInList(localList, rAcc);
            if (lAcc != null) {
                merged.add(lAcc.getLastModified() >= rAcc.getLastModified() ? lAcc : rAcc);
            } else {
                if (rAcc.getLastModified() > lastSyncTimestamp) merged.add(rAcc);
            }
        }
        for (Account lAcc : localList) {
            if (findAccountInList(remoteList, lAcc) == null) {
                if (lAcc.getLastModified() > lastSyncTimestamp) merged.add(lAcc);
            }
        }
        return merged;
    }
    
    private static boolean hasLocalNewerAccounts(List<Account> localList, List<Account> remoteList) {
        for (Account lAcc : localList) {
            Account rAcc = findAccountInList(remoteList, lAcc);
            if (rAcc != null && lAcc.getLastModified() > rAcc.getLastModified()) return true;
            if (rAcc == null) return true; // Local has an account remote doesn't
        }
        return false;
    }

    private static Account findAccountInList(List<Account> list, Account target) {
        for (Account a : list) {
            if (a.getUid() != null && target.getUid() != null && a.getUid().equals(target.getUid())) return a;
        }
        return null;
    }
    
    private static AccountGroup findGroup(List<AccountGroup> groups, AccountGroup target) {
        for (AccountGroup g : groups) {
            if (g.getUid() != null && target.getUid() != null && g.getUid().equals(target.getUid())) return g;
        }
        return null;
    }
}




