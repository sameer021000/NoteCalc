# 📜 Changelog

All notable changes to **NoteCalc** will be documented in this file.

---

## [1.3.0] - 2026-08-19

### ✨ Added

* **File Attachments**: Added the ability to attach up to 3 files (images, PDFs, and documents) per record.
* **In-App Camera**: Integrated a direct **Take Photo** option when attaching a file, generating auto-timestamped images securely in internal storage.
* **Attachment Viewing**: Attachments are displayed as beautifully themed chips within records and can be opened directly using Android's native file viewer.

### 🎨 Improved

* **Custom Attach Dialog**: Introduced a sleek, highly themed custom dialog for choosing between **Take Photo** and **Choose File**.
* **Bulk Actions Menu Scrollability**: Wrapped the unified bulk actions popup in a scrollable view so options are never cut off on smaller screens.
* **Dynamic Filter Icon**: The **Filter by Category** icon now dynamically changes color to reflect its active filtering state.
* **Interactive Elements**: Enhanced the layout, text styling, and interactive scale animations for the **Attach File** and form minimize buttons.
* **Swipe-to-Delete Conflict**: Fixed a critical conflict where horizontally scrolling through attachment chips could accidentally trigger the swipe-to-delete action on the entire record.

---

## [1.2.0] - 2026-08-09

### ✨ Added

* **Dynamic PDF Sorting**: Added a beautifully themed dialog before exporting PDFs to let you sort records by S.No, Description, Date, or Amount.

### 🎨 Improved

* **Cut Operation Workflow**: Instantly updates the visual list and intelligently re-sequentializes the remaining S.Nos after a cut operation is performed.
* **Dialog Interactions**: Added smooth `StateListDrawable` touch feedback to the Cancel and Export buttons within the new sorting dialog.
* **Date Accuracy**: Fixed a bug where dates in PDF sorting were being parsed alphabetically rather than chronologically.

---

## [1.1.0] - 2026-08-07

### ✨ Added

* **Record Transfer**: New ability to Cut and Copy selected records across different lists.
* **Selective PDF Export**: Generate clean, budget-free PDF statements from a specific selection of records.
* **Isolated Categories**: Category dropdown suggestions are now cleanly isolated to the currently active list.

### 🎨 Improved

* **UI Streamlining**: Unified all bulk actions (Filter, Cut, Copy, Delete, Export) into a single, beautifully themed popup menu.
* **Themed Dialogs**: Re-styled list creation and transfer dialogs to perfectly match the application's premium UI.
* **Layout Fixes**: Fixed truncation issues with the bulk selection total text.

---

## [1.0.0] - 2026-08-04

### ✨ Added

* **Dual Mode Tracking**: Core functionality introduced for tracking both Expenses and Budgets, with dynamic total calculations.
* **Groups & Standalone Accounts**: Create organized folders for accounts or keep them standalone.
* **PDF Export Engine**: Capability to generate and export paginated, multi-table PDF reports for individual accounts or the entire app.
* **JSON Backup System**: Added secure, offline-first JSON Import and Export capabilities via the Settings screen.
* **Advanced Filtering & Sorting**: Added dynamic search bars (including remarks), date range filters, and amount range filters.

### 🎨 Improved

* **Premium UI**: Implemented custom rounded layouts, dynamic `StateListDrawable` feedback, and edge-to-edge system insets.
* **Dashboard Optimization**: Added interactive empty states and refined the header/sorting UI.
* **Code Stability**: Extensive refactoring to resolve all IDE warnings, replace deprecated methods, and optimize rendering performance.
* **Dialog Flow**: Enhanced confirmation dialogs and context menus for actions like Bulk Delete, Moving Accounts, and Group Management.