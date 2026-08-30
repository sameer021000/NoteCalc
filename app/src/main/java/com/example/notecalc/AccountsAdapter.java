package com.example.notecalc;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.widget.Toast;
import android.widget.LinearLayout;
import java.util.Date;
import java.text.SimpleDateFormat;
import android.graphics.Color;


    @android.annotation.SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public class AccountsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final MainActivity activity;

    public AccountsAdapter(MainActivity activity) {
        this.activity = activity;
    }
        private static final int TYPE_ACCOUNT = 0;
        private static final int TYPE_GROUP = 1;

        private final List<Object> displayItems = new ArrayList<>();

        void setFilter(List<Object> sortedSource, String query) {
            displayItems.clear();
            for (Object item : sortedSource) {
                if (item instanceof Account) {
                    Account account = (Account) item;
                    if (!query.isEmpty() && !account.getTitle().toLowerCase(Locale.getDefault()).contains(query)) {
                        continue;
                    }
                    displayItems.add(account);
                } else if (item instanceof AccountGroup) {
                    AccountGroup group = (AccountGroup) item;
                    if (!query.isEmpty()) {
                        boolean matchesGroupTitle = group.getTitle().toLowerCase(Locale.getDefault()).contains(query);
                        if (matchesGroupTitle) {
                            displayItems.add(group);
                        }
                        
                        for (Account acc : group.getAccounts()) {
                            if (acc.getTitle().toLowerCase(Locale.getDefault()).contains(query)) {
                                displayItems.add(acc);
                            }
                        }
                    } else {
                        displayItems.add(group);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (displayItems.get(position) instanceof AccountGroup) return TYPE_GROUP;
            return TYPE_ACCOUNT;
        }

        @androidx.annotation.NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            if (viewType == TYPE_GROUP) {
                View rowView = LayoutInflater.from(activity).inflate(R.layout.item_group, parent, false);
                ResponsiveUI.applyResponsiveness(rowView);
                return new GroupViewHolder(rowView);
            } else {
                View rowView = LayoutInflater.from(activity).inflate(R.layout.item_account, parent, false);
                ResponsiveUI.applyResponsiveness(rowView);
                return new AccountViewHolder(rowView);
            }
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull RecyclerView.ViewHolder holder, int position) {
            Object item = displayItems.get(position);
            
            if (holder instanceof AccountViewHolder) {
                AccountViewHolder accHolder = (AccountViewHolder) holder;
                Account account = (Account) item;

                accHolder.tvTitle.setText(account.getTitle());
                accHolder.tvTotal.setText(String.format(Locale.getDefault(), "%.2f", account.calculateTotal()));

                if (accHolder.tvPurse != null) {
                    if (account.hasBudget()) {
                        accHolder.tvPurse.setVisibility(View.VISIBLE);
                        accHolder.tvPurse.setText(String.format(Locale.getDefault(), "Bal : %.2f", account.calculateRemainingPurse()));
                    } else {
                        accHolder.tvPurse.setVisibility(View.GONE);
                    }
                }

                int itemsSize = account.getRecords().size();
                accHolder.tvItemsCount.setText(String.format(Locale.getDefault(), "%d %s", itemsSize, itemsSize == 1 ? "item" : "items"));

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                accHolder.tvDate.setText(sdf.format(new Date(account.getLastModified())));

                accHolder.itemView.setBackground(ResponsiveUI.createCardSelector(activity));



                accHolder.btnPinAccount.setImageResource(account.isPinned() ? R.drawable.ic_pin_filled : R.drawable.ic_pin);
                accHolder.btnPinAccount.setImageTintList(ResponsiveUI.createIconTintSelector(
                        account.isPinned() ? ThemeManager.getSecondaryAccentColor(activity) : activity.getColor(R.color.text_tertiary),
                        ThemeManager.getSecondaryAccentColor(activity)
                ));
                accHolder.btnPinAccount.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#15FFFFFF"), 6.0f));

                final AccountGroup accountParentGroup = getAccountParentGroup(account);

                ResponsiveUI.setupClickable(accHolder.itemView, false, () -> {
                    activity.currentViewGroup = accountParentGroup;
                    activity.openEditor(account);
                }, () -> MenuHelper.showAccountPopupMenu(activity, accHolder.itemView, account));

                if (accHolder.btnMoveAccount != null) {
                    if (accountParentGroup != null) {
                        accHolder.btnMoveAccount.setImageResource(R.drawable.ic_move_folder_out);
                    } else {
                        accHolder.btnMoveAccount.setImageResource(R.drawable.ic_move_folder);
                    }
                    accHolder.btnMoveAccount.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#15FFFFFF"), 6.0f));
                    accHolder.btnMoveAccount.setImageTintList(ResponsiveUI.createIconTintSelector(
                            activity.getColor(R.color.text_tertiary),
                            ThemeManager.getSecondaryAccentColor(activity)
                    ));
                    ResponsiveUI.setupClickable(accHolder.btnMoveAccount, false, () -> {
                        if (accountParentGroup != null) {
                            // Move out of group (back to standalone)
                            accountParentGroup.getAccounts().remove(account);
                            activity.appStorage.standaloneAccounts.add(account);
                            StorageHelper.saveAppStorage(activity, activity.appStorage);
                            DashboardHelper.refreshDashboardList(activity);
                            Toast.makeText(activity, activity.getString(R.string.auto_moved_to_dashboard_8), Toast.LENGTH_SHORT).show();
                        } else {
                            // Move into a group
                            if (activity.appStorage.groups.isEmpty()) {
                                Toast.makeText(activity, activity.getString(R.string.auto_no_groups_available__9), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            

                            
                            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
                            View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_dialog_move_group, null);
                            builder.setView(dialogView);
                            
                            final androidx.appcompat.app.AlertDialog dialog = builder.create();
                            if (dialog.getWindow() != null) {
                                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                            }
                            
                            View dialogRoot = dialogView.findViewById(R.id.dialog_root);
                            LinearLayout detailsContainer = dialogView.findViewById(R.id.details_container);
                            TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
                            
                            dialogRoot.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.5f, 12f));
                            detailsContainer.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 6f));
                            btnCancel.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#20EF4444"), 4.0f));
                            btnCancel.setTextColor(activity.getColor(R.color.error_red));
                            
                            List<AccountGroup> targetGroups = new ArrayList<>();
                            for (AccountGroup g : activity.appStorage.groups) {
                                if (g.isArchived() == account.isArchived()) targetGroups.add(g);
                            }
                            
                            if (targetGroups.isEmpty()) {
                                Toast.makeText(activity, activity.getString(R.string.auto_no_groups_available__9), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                            
                            for (int i = 0; i < targetGroups.size(); i++) {
                                final AccountGroup selectedGroup = targetGroups.get(i);
                                TextView tvGroup = new TextView(activity);
                                tvGroup.setText(selectedGroup.getTitle());
                                tvGroup.setTextColor(activity.getColor(R.color.text_primary));
                                tvGroup.setTextSize(16f);
                                tvGroup.setPadding(32, 24, 32, 24);
                                tvGroup.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#15FFFFFF"), 4.0f));
                                ResponsiveUI.setupClickable(tvGroup, false, () -> {
                                    activity.appStorage.standaloneAccounts.remove(account);
                                    selectedGroup.getAccounts().add(account);
                                    selectedGroup.updateLastModified();
                                    StorageHelper.saveAppStorage(activity, activity.appStorage);
                                    DashboardHelper.refreshDashboardList(activity);
                                    Toast.makeText(activity, "Moved to " + selectedGroup.getTitle(), Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                });
                                detailsContainer.addView(tvGroup);
                                
                                if (i < targetGroups.size() - 1) {
                                    View divider = new View(activity);
                                    divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                                    divider.setBackgroundColor(ThemeManager.getBorderColor(activity));
                                    detailsContainer.addView(divider);
                                }
                            }
                            
                            ResponsiveUI.setupClickable(btnCancel, false, dialog::cancel);
                            dialog.show();
                        }
                    });
                }
                accHolder.btnPinAccount.setVisibility(View.VISIBLE);
                accHolder.btnPinAccount.setImageResource(R.drawable.ic_pin); // Use same icon, just tint different? Wait, maybe just keep icon.
                accHolder.btnPinAccount.setImageTintList(ResponsiveUI.createIconTintSelector(
                        account.isPinned() ? ThemeManager.getPrimaryAccentColor(activity) : activity.getColor(R.color.text_tertiary),
                        ThemeManager.getSecondaryAccentColor(activity)
                ));
                ResponsiveUI.setupClickable(accHolder.btnPinAccount, false, () -> {
                    account.setPinned(!account.isPinned());
                    StorageHelper.saveAppStorage(activity, activity.appStorage);
                    DashboardHelper.refreshDashboardList(activity);
                });
            } else if (holder instanceof GroupViewHolder) {
                GroupViewHolder grpHolder = (GroupViewHolder) holder;
                AccountGroup group = (AccountGroup) item;
                
                grpHolder.tvTitle.setText(group.getTitle());
                long latestDate = group.getLastModified();
                for (Account a : group.getAccounts()) {
                    if (a.getLastModified() > latestDate) {
                        latestDate = a.getLastModified();
                    }
                }
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                grpHolder.tvDate.setText(sdf.format(new Date(latestDate)));
                
                int listCount = group.getAccounts().size();
                grpHolder.tvAccounts.setText(listCount + (listCount == 1 ? " List" : " Lists"));
                
                grpHolder.itemView.setBackground(ResponsiveUI.createCardSelector(activity));
                
                grpHolder.btnDeleteGroup.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#20EF4444"), 6.0f));
                grpHolder.btnDeleteGroup.setImageTintList(ResponsiveUI.createIconTintSelector(
                        activity.getColor(R.color.error_red),
                        Color.parseColor("#FF6B6B")
                ));
                
                ResponsiveUI.setupClickable(grpHolder.itemView, false, () -> {
                    activity.currentViewGroup = group;
                    DashboardHelper.showDashboard(activity); // Refresh dashboard into group view
                }, () -> MenuHelper.showGroupPopupMenu(activity, grpHolder.itemView, group));
                
                ResponsiveUI.setupClickable(grpHolder.btnDeleteGroup, false, () -> DialogHelper.showDeleteGroupConfirmation(activity, group));
            }
        }

        @Override
        public int getItemCount() {
            return displayItems.size();
        }

        private AccountGroup getAccountParentGroup(Account account) {
            if (activity.currentViewGroup != null) {
                return activity.currentViewGroup;
            }
            for (AccountGroup g : activity.appStorage.groups) {
                if (g.getAccounts().contains(account)) {
                    return g;
                }
            }
            return null;
        }

        static class AccountViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvTotal, tvDate, tvItemsCount, tvPurse;
            ImageView btnPinAccount, btnMoveAccount;

            AccountViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.text_account_title);
                tvTotal = itemView.findViewById(R.id.text_account_total);
                tvPurse = itemView.findViewById(R.id.text_account_purse);
                tvDate = itemView.findViewById(R.id.text_account_date);
                tvItemsCount = itemView.findViewById(R.id.text_account_items_count);

                btnPinAccount = itemView.findViewById(R.id.btn_pin_account);
                btnMoveAccount = itemView.findViewById(R.id.btn_move_account);
            }
        }
        
        static class GroupViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvAccounts, tvDate;
            ImageView btnDeleteGroup, btnPinGroup;
            
            GroupViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.text_group_title);
                tvAccounts = itemView.findViewById(R.id.text_group_accounts);
                tvDate = itemView.findViewById(R.id.text_group_date);
                btnDeleteGroup = itemView.findViewById(R.id.btn_delete_group);
                btnPinGroup = itemView.findViewById(R.id.btn_pin_group);
            }
        }
    }

