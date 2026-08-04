import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

bad_budget_logic = '''            if (allRecords.size() > 0) {
                double budget = Double.parseDouble(allRecords.get(0).getAmount());
                if (budget > 0) {
                    // Total expenses exclude the first record
                    double expenses = 0;
                    for (int i = 1; i < filtered.size(); i++) {
                        expenses += Double.parseDouble(filtered.get(i).getAmount());
                    }
                    double pct = (expenses / budget) * 100.0;
                    tvBudgetPct.setText(String.format(java.util.Locale.getDefault(), "%.1f%% of budget spent", pct));
                } else {
                    tvBudgetPct.setText("0% of budget spent");
                }
            }'''

good_budget_logic = '''            if (allRecords.size() > 0) {
                double budget = Double.parseDouble(allRecords.get(0).getAmount());
                if (budget > 0) {
                    double expenses = 0;
                    for (Record r : filtered) {
                        if (r.getOriginalIndex() != 0) { // Don't count the initial budget amount as an expense
                            expenses += Double.parseDouble(r.getAmount());
                        }
                    }
                    double pct = (expenses / budget) * 100.0;
                    tvBudgetPct.setText(String.format(java.util.Locale.getDefault(), "%.1f%% of budget spent", pct));
                } else {
                    tvBudgetPct.setText("0% of budget spent");
                }
            }'''

content = content.replace(bad_budget_logic, good_budget_logic)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Budget logic fixed!")
