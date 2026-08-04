import os
import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add Settings view initialization
init_settings_code = '''
    private android.view.View settingsView;

    private void initSettings() {
        settingsView = getLayoutInflater().inflate(R.layout.layout_settings, null);
        
        settingsView.findViewById(R.id.btn_settings_back).setOnClickListener(v -> closeSettings());
        
        android.widget.RadioGroup rgTheme = settingsView.findViewById(R.id.rg_theme_mode);
        int currentMode = ThemeManager.getDarkMode(this);
        if (currentMode == ThemeManager.MODE_DARK) rgTheme.check(R.id.rb_theme_dark);
        else if (currentMode == ThemeManager.MODE_LIGHT) rgTheme.check(R.id.rb_theme_light);
        else rgTheme.check(R.id.rb_theme_system);

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int newMode = ThemeManager.MODE_SYSTEM;
            if (checkedId == R.id.rb_theme_dark) newMode = ThemeManager.MODE_DARK;
            else if (checkedId == R.id.rb_theme_light) newMode = ThemeManager.MODE_LIGHT;
            ThemeManager.setDarkMode(this, newMode);
        });

        android.widget.LinearLayout llColors = settingsView.findViewById(R.id.ll_accent_colors);
        String[] colors = {ThemeManager.ACCENT_BLUE, ThemeManager.ACCENT_GREEN, ThemeManager.ACCENT_PURPLE, ThemeManager.ACCENT_RED, ThemeManager.ACCENT_YELLOW, ThemeManager.ACCENT_ORANGE, ThemeManager.ACCENT_PINK};
        String[] hexes = {"#0284C7", "#16A34A", "#9333EA", "#DC2626", "#CA8A04", "#EA580C", "#DB2777"};
        String active = ThemeManager.getAccentColorName(this);

        for (int i=0; i<colors.length; i++) {
            final String cName = colors[i];
            android.view.View circle = new android.view.View(this);
            android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(100, 100);
            lp.setMargins(16, 16, 16, 16);
            circle.setLayoutParams(lp);
            
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            gd.setColor(android.graphics.Color.parseColor(hexes[i]));
            if (cName.equals(active)) {
                gd.setStroke(8, ThemeManager.getSecondaryAccentColor(this));
            }
            circle.setBackground(gd);
            circle.setOnClickListener(v -> {
                ThemeManager.setAccentColor(this, cName);
                recreate();
            });
            llColors.addView(circle);
        }

        settingsView.findViewById(R.id.btn_export_json).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(android.content.Intent.EXTRA_TITLE, "NoteCalc_Backup.json");
            exportJsonLauncher.launch(intent);
        });

        settingsView.findViewById(R.id.btn_import_json).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            importJsonLauncher.launch(intent);
        });

        settingsView.findViewById(R.id.btn_export_pdf_all).setOnClickListener(v -> {
            generateAndOpenAllPdf();
        });
    }

    private void openSettings() {
        if (settingsView == null) initSettings();
        mainContainer.removeAllViews();
        mainContainer.addView(settingsView);
    }

    private void closeSettings() {
        mainContainer.removeAllViews();
        mainContainer.addView(dashboardView);
    }
'''

# 1. Insert initSettings and openSettings methods
if "private void initSettings" not in content:
    content = content.replace('private void renderDashboardItems() {', init_settings_code + '\n    private void renderDashboardItems() {')

# 2. Attach settings button listener
if "btn_settings" not in content:
    dashboard_btn_code = '''
        android.view.View btnSettings = dashboardView.findViewById(R.id.btn_settings);
        if(btnSettings != null) btnSettings.setOnClickListener(v -> openSettings());
        
        android.view.View btnCreateGroup = dashboardView.findViewById(R.id.btn_create_group);
'''
    content = content.replace('android.view.View btnCreateGroup = dashboardView.findViewById(R.id.btn_create_group);', dashboard_btn_code)
    # also check if it uses just View
    content = content.replace('View btnCreateGroup = dashboardView.findViewById(R.id.btn_create_group);', dashboard_btn_code.replace("android.view.View", "View"))

# 3. Add generateAndOpenAllPdf
pdf_all_code = '''
    private void generateAndOpenAllPdf() {
        android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
        int[] pageTracker = {0};
        boolean hasRecords = false;
        
        for (AccountGroup group : appStorage.getGroups()) {
            for (Account account : group.getAccounts()) {
                if (account.getRecords().size() > 0) {
                    appendAccountToPdf(document, account, pageTracker);
                    hasRecords = true;
                }
            }
        }
        for (Account account : appStorage.getStandaloneAccounts()) {
            if (account.getRecords().size() > 0) {
                appendAccountToPdf(document, account, pageTracker);
                hasRecords = true;
            }
        }
        
        if (!hasRecords) {
            android.widget.Toast.makeText(this, "No records found to export.", android.widget.Toast.LENGTH_SHORT).show();
            document.close();
            return;
        }

        try {
            java.io.File pdfDir = new java.io.File(getExternalFilesDir(null), "PDFs");
            if (!pdfDir.exists()) pdfDir.mkdirs();
            java.io.File file = new java.io.File(pdfDir, "All_Accounts_Export.pdf");
            document.writeTo(new java.io.FileOutputStream(file));
            document.close();

            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Failed to generate PDF", android.widget.Toast.LENGTH_SHORT).show();
            document.close();
        }
    }
'''
if "private void generateAndOpenAllPdf" not in content:
    content = content.replace('private void generateAndOpenPdf(Account account) {', pdf_all_code + '\n    private void generateAndOpenPdf(Account account) {')

# 4. Refactor generateAndOpenPdf to appendAccountToPdf
refactor_pdf = '''private void appendAccountToPdf(android.graphics.pdf.PdfDocument document, Account account, int[] pageTracker) {
        // --- Page dimensions (A4 at 72 dpi approx) ---
        int pageWidth  = 595;
        int pageHeight = 842;
        int margin     = 40;
'''
if "private void appendAccountToPdf" not in content:
    content = re.sub(
        r'private void generateAndOpenPdf\(Account account\) \{.*?PdfDocument document = new PdfDocument\(\);.*?int margin\s*=\s*40;',
        r'private void generateAndOpenPdf(Account account) {\n        android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();\n        int[] pageTracker = {0};\n        appendAccountToPdf(document, account, pageTracker);\n        try {\n            java.io.File pdfDir = new java.io.File(getExternalFilesDir(null), "PDFs");\n            if (!pdfDir.exists()) pdfDir.mkdirs();\n            java.io.File file = new java.io.File(pdfDir, account.getTitle().replaceAll("[\\\\\\\\/:*?\\\\\"<>|]", "_") + ".pdf");\n            document.writeTo(new java.io.FileOutputStream(file));\n            document.close();\n            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);\n            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);\n            intent.setDataAndType(uri, "application/pdf");\n            intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);\n            startActivity(intent);\n        } catch (Exception e) {\n            e.printStackTrace();\n            android.widget.Toast.makeText(this, "Failed to generate PDF", android.widget.Toast.LENGTH_SHORT).show();\n            document.close();\n        }\n    }\n\n    ' + refactor_pdf,
        content, flags=re.DOTALL
    )
    
    content = content.replace('int pageNum = 0;', 'int pageNum = pageTracker[0];')
    content = re.sub(
        r'document\.finishPage\(page\);\s*try\s*\{.*?startActivity\(intent\);\s*\}\s*catch\s*\(Exception e\)\s*\{.*?document\.close\(\);\s*\}',
        r'document.finishPage(page);\n        pageTracker[0] = pageNum;',
        content, flags=re.DOTALL
    )

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Modification complete.")
