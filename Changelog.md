# 📜 Changelog

All notable changes to **NoteCalc** will be documented in this file.

---

## [1.5.0] - 2026-08-27

### ✨ Added

* **List and Group Archiving**: You can now archive old lists and groups to declutter your dashboard! Archived items become completely read-only to prevent accidental changes, ensuring your past financial data remains securely intact.
* **Archive View**: Dedicated Archive screen to view, sort, and search through all of your archived lists and groups safely separated from active items.

### 🎨 Improved

* **Quick Delete**: Bypassed the confirmation dialog when bulk-deleting 2 or fewer records for a faster workflow.
* **Tips & Features**: Added a new tip card detailing how to manage Archived items.
* **Bulk Transfer Safety**: Prevented users from accidentally cutting or copying active records into archived lists.

---

## [1.4.0] - 2026-08-25

### ✨ Added

* **Group PDF Export**: You can now download all lists within a specific group as a single combined PDF by long-pressing the group from the dashboard.
* **What's New Dialog**: Added a beautiful, premium "What's New" rainbow card dialog to view recent changelogs, accessible by tapping the version number in Settings.
* **Filter Record Count**: The UI now dynamically displays the total number of matching records whenever a search or filter is applied.

### 🎨 Improved

* **Settings Screen UI**: Overhauled the Settings screen layout for a cleaner, more modern look.
* **Unified Touch Feedback**: Standardized the premium "bounce and ripple" touch animations across all buttons throughout the application.
* **Code Quality & String Migration**: Migrated all hardcoded strings into dedicated string resource files and resolved all layout warnings across screens for better localization and cleaner code.

### 🐛 Fixed

* **PDF Generation Crash**: Fixed a major crash occurring when navigating away from the app after generating a PDF by offloading PDF generation to background threads and ensuring resources are safely closed.

---

## [1.3.0] - 2026-08-19

### ✨ Added

* **File Attachments**: Added the ability to attach up to 3 files (images, PDFs, and documents) per record.
* **PDF Attachments Appendix**: Attached images are now magically embedded directly into PDF exports as a beautiful 2-column image appendix.
* **PDF Attachment Names**: Included paperclip-prefixed attachment names beneath remarks in the main PDF tables.
* **PDF Timestamp Column**: Added an exact time of entry column next to the Date column on PDFs.
* **In-App Camera**: Integrated a direct **Take Photo** option when attaching a file, generating auto-timestamped images securely in internal storage.
* **Attachment Viewing**: Attachments are displayed as beautifully themed chips within records and can be opened directly using Android's native file viewer.

### 🎨 Improved

* **Custom Attach Dialog**: Introduced a sleek, highly themed custom dialog for choosing between **Take Photo** and **Choose File**.
* **Bulk Actions Menu Scrollability**: Wrapped the unified bulk actions popup in a scrollable view so options are never cut off on smaller screens.
* **Dynamic Filter Icon**: The **Filter by Category** icon now dynamically changes color to reflect its active filtering state.
* **Interactive Elements**: Enhanced the layout, text styling, and interactive scale animations for the **Attach File** and form minimize buttons.
* **Swipe-to-Delete Conflict**: Fixed a critical conflict where horizontally scrolling through attachment chips could accidentally trigger the swipe-to-delete action on the entire record.
* **PDF Layout & Logic**: Overhauled PDF generation to properly swap Budget & Expenses tables, dynamically expand row height based on attachment count, and smartly truncate overly long file names.

### 🐛 Fixed

* **Sorting Logic**: Fixed a bug where toggling between Budget and Expenses modes failed to retain or properly sort list records.
* **Budget Copy/Cut Persistence**: Fixed a bug where budget records moved between lists would appear out of order until manually resorted.
* **Timestamp Persistence**: Fixed a bug where record timestamps would accidentally reset to zero when the app was fully closed.

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
* **Advanced Filtering**: Search through records by description or remarks, and filter by exact dates or amount ranges. The UI will dynamically display the total number of matching records found.

### 🎨 Improved

* **Premium UI**: Implemented custom rounded layouts, dynamic `StateListDrawable` feedback, and edge-to-edge system insets.
* **Dashboard Optimization**: Added interactive empty states and refined the header/sorting UI.
* **Code Stability**: Extensive refactoring to resolve all IDE warnings, replace deprecated methods, and optimize rendering performance.
* **Dialog Flow**: Enhanced confirmation dialogs and context menus for actions like Bulk Delete, Moving Accounts, and Group Management.