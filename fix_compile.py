import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. ImageButton -> Fix the import
if 'import android.widget.ImageButton;' not in content:
    content = content.replace('import android.widget.ImageView;', 'import android.widget.ImageView;\nimport android.widget.ImageButton;')

# 2. r.getTimestamp() -> convert from dd-MM-yyyy to long using SimpleDateFormat
timestamp_logic_search = '''                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(r.getTimestamp());'''
timestamp_logic_replace = '''                Calendar c = Calendar.getInstance();
                try {
                    java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                    c.setTime(format.parse(r.getDate()));
                } catch(Exception ex) {
                    c.setTimeInMillis(System.currentTimeMillis());
                }'''
content = content.replace(timestamp_logic_search, timestamp_logic_replace)

# r.getTimestamp() condition
content = content.replace('if (r.getTimestamp() >= startTime) {', 
'''long rTs = 0;
            try {
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                rTs = format.parse(r.getDate()).getTime();
            } catch(Exception ex) {}
            if (rTs >= startTime) {''')

# 3. Double.parseDouble(r.getAmount()) -> r.getAmount() is already double!
content = content.replace('double amt = Double.parseDouble(r.getAmount());', 'double amt = r.getAmount();')
content = content.replace('expenses += Double.parseDouble(r.getAmount());', 'expenses += r.getAmount();')
content = content.replace('double budget = Double.parseDouble(allRecords.get(0).getAmount());', 'double budget = allRecords.get(0).getAmount();')

# 4. account.isBudgetMode() -> account.hasBudget()
content = content.replace('if (account.isBudgetMode()) {', 'if (account.hasBudget()) {')

# 5. Fix Budget Logic -> calculateTotalBudget()!
budget_logic_search = '''            if (allRecords.size() > 0) {
                double budget = allRecords.get(0).getAmount();
                if (budget > 0) {
                    double expenses = 0;
                    for (Record r : filtered) {
                        if (r.getOriginalIndex() != 0) {
                            expenses += r.getAmount();
                        }
                    }
                    double pct = (expenses / budget) * 100.0;
                    tvBudgetPct.setText(String.format(java.util.Locale.getDefault(), "%.1f%% of budget spent", pct));
                } else {
                    tvBudgetPct.setText("0% of budget spent");
                }
            }'''
budget_logic_replace = '''            double budget = account.calculateTotalBudget();
            if (budget > 0) {
                double expenses = 0;
                for (Record r : filtered) {
                    expenses += r.getAmount();
                }
                double pct = (expenses / budget) * 100.0;
                tvBudgetPct.setText(String.format(java.util.Locale.getDefault(), "%.1f%% of budget spent", pct));
            } else {
                tvBudgetPct.setText("0% of budget spent");
            }'''
content = content.replace(budget_logic_search, budget_logic_replace)

# 6. showEditor doesn't take Account? Let's check showEditor signature in MainActivity
