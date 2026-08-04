import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix default sort order
sort_var_search = '''    private int expenseSortColumn = 0;
    private boolean expenseSortAscending = true;
    private int budgetSortColumn = 0;
    private boolean budgetSortAscending = true;'''
sort_var_replace = '''    private int expenseSortColumn = 0;
    private boolean expenseSortAscending = false;
    private int budgetSortColumn = 0;
    private boolean budgetSortAscending = false;'''
content = content.replace(sort_var_search, sort_var_replace)

sort_reset_search = '''        expenseSortColumn = 0;
        expenseSortAscending = true;
        budgetSortColumn = 0;
        budgetSortAscending = true;'''
sort_reset_replace = '''        expenseSortColumn = 0;
        expenseSortAscending = false;
        budgetSortColumn = 0;
        budgetSortAscending = false;'''
content = content.replace(sort_reset_search, sort_reset_replace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Fixes applied in Java")
