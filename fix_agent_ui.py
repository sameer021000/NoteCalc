import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add imports at the top
import_block = '''import android.widget.Toast;
import com.example.notecalc.ncagent.*;
import com.example.notecalc.ncagent.parser.*;
import android.widget.CheckBox;
import android.widget.ScrollView;
import java.util.List;
import java.util.ArrayList;'''
content = content.replace('import android.widget.Toast;', import_block)

# 2. Fix ThemeManager text colors
content = content.replace('ThemeManager.getTextPrimaryColor(this)', 'getColor(R.color.text_primary)')
content = content.replace('ThemeManager.getTextSecondaryColor(this)', 'getColor(R.color.text_secondary)')

# 3. Fix save/refresh methods
refresh_search = '''            saveCurrentAccount();
            updateDashboardAccounts();
            refreshRecordsList();
            updateTotal();'''
refresh_replace = '''            StorageHelper.saveAppStorage(MainActivity.this, appStorage);
            populateRecordsList();'''
content = content.replace(refresh_search, refresh_replace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("UI code fixed")
