# NoteCalc 📓🧮

![Android](https://img.shields.io/badge/Platform-Android-green)
![Java](https://img.shields.io/badge/Language-Java-blue)
![Version](https://img.shields.io/badge/Version-1.4.0-orange)
![Status](https://img.shields.io/badge/Status-Active-success)

**NoteCalc** is a premium, offline-first Android expense tracker and calculator designed to bridge the gap between simple note-taking and structured financial tracking. It combines a modern interface with powerful tools such as dual tracking modes, file attachments, detailed PDF reporting, JSON backups, advanced filtering, and an organized dashboard for an intuitive accounting experience.

---

## 📌 App Information

* **Current Version:** v1.4.0
* **Minimum SDK:** Android 7.0 (Nougat)
* **Target SDK:** Android 15 (API 36)
* **Language:** Java
* **Last Updated:** August 2026

---

## 📥 Download APK

🔽 **Latest Version**
[Download NoteCalc (Latest)](https://github.com/sameer021000/NoteCalc/releases/latest)

📜 **All Versions**
[View All Releases](https://github.com/sameer021000/NoteCalc/releases)

> ⚠️ Enable "Install from Unknown Sources" in Android settings before installing the APK.

---

## ✨ Key Features

### 🚀 **Advanced Financial Tracking**

* **Dual Modes (Expenses & Budgets)**: Track both your outgoing expenses and incoming budgets seamlessly. The app automatically calculates totals and dynamically displays balances.
* **Custom Groups & Accounts**: Organize your financial data effortlessly. Create standalone accounts (e.g., "Trip to Paris", "Monthly Groceries") or group related accounts together under custom folders.
* **Smart Dashboard**: A beautifully designed management screen featuring dynamic search, quick sorting (Latest, Title, Total Amount), and interactive empty states.
* **Advanced Filtering**: Search through records by description or remarks, and filter by exact dates or amount ranges. The UI will dynamically display the total number of matching records found.
* **Bulk Actions & Transfers**: Seamlessly cut or copy selected records between lists, delete or export records, and quickly create a new list from your selection.
* **Isolated Category Tracking**: Category dropdown suggestions are isolated to the currently active list, keeping autocomplete clean and relevant.
* **Dynamic PDF Sorting**: Sort PDF statements by Serial Number, Description, Date, or Amount before generating the report.

### 📎 **File Attachments**

* **Multiple Attachments**: Attach up to 3 files to each record, including images, PDFs, and documents.
* **In-App Camera**: Capture photos directly from the attachment interface without leaving NoteCalc.
* **Automatic Timestamps**: Photos captured through the in-app camera are automatically timestamped and securely stored in the app's internal storage.
* **Attachment Chips**: Attached files are displayed as beautifully themed chips directly within records.
* **Native File Viewing**: Open attached files directly through Android's native file viewer.
* **PDF Attachment Integration**: Attached images are embedded directly into exported PDFs in a dedicated 2-column image appendix.
* **Attachment Names in PDFs**: Attachment names are included beneath record remarks with a paperclip indicator for easy reference.

### 📄 **PDF Reporting**

* **PDF Export Engine**: Generate beautiful, multi-table, paginated PDF reports of your accounts directly on your device.
* **Flexible PDF Export**: Export individual accounts, all data at once, or download an entire group's lists as a single PDF. You can also generate clean, budget-free statements for a specific selection of records.
* **Dynamic PDF Sorting**: Sort records by S.No, Description, Date, or Amount before generating a report.
* **Timestamp Reporting**: PDF reports include the exact time of entry alongside the record date.
* **Attachment Appendix**: Images attached to records are automatically embedded into a dedicated 2-column appendix within the exported PDF.
* **Attachment References**: File names are displayed beneath remarks in the main PDF tables for easy identification.
* **Adaptive PDF Layout**: PDF rows dynamically expand based on the number of attachments, while overly long file names are intelligently truncated to maintain a clean layout.
* **Budget & Expense Tables**: PDF generation intelligently handles Budget and Expenses tables and maintains the appropriate table structure for each mode.

### 💾 **Backup & Privacy**

* **Privacy-First & Offline**: No cloud syncing is forced upon you. Your data remains stored locally on your device.
* **JSON Import / Export**: Easily back up your entire workspace to a JSON file and restore it whenever you switch devices or need to recover data.
* **Local File Storage**: Attachments captured or selected within the app are managed locally, keeping your records and associated files available without requiring cloud storage.

### 🎨 **Premium UI/UX**

* **Modern Aesthetics**: Features rounded layouts, smooth animations, custom styling, and clean Material Design. Edge-to-edge window insets provide an immersive visual experience.
* **Dynamic Interactions**: Fluid `StateListDrawable` feedback provides responsive visual interactions throughout the application.
* **Themed Dialogs**: Custom dialogs are designed to maintain a consistent and polished visual language across the application.
* **Custom Attachment Dialog**: Quickly choose between **Take Photo** and **Choose File** through a dedicated themed attachment dialog.
* **Interactive Attachments**: Attachment controls and form actions feature refined text styling, scale animations, and responsive touch interactions.
* **Dynamic Filter State**: The **Filter by Category** icon dynamically changes color to clearly indicate when category filtering is active.
* **Scrollable Actions Menu**: The bulk actions menu adapts to smaller screens so available actions remain accessible.
* **Intuitive Editor**: Add records quickly using a refined form supporting custom dates, optional remarks, attachments, and other record details.

---

## 📱 Screenshots

|                     Dashboard                     |                       Group Dashboard                      |
| :-----------------------------------------------: | :--------------------------------------------------------: |
|   ![Dashboard](screenshots/dashboard_screen.jpg)  | ![Group Dashboard](screenshots/group_dashboard_screen.jpg) |
|                 **Expenses Mode**                 |                       **Budget Mode**                      |
| ![Expenses Mode](screenshots/expenses_screen.jpg) |        ![Budget Mode](screenshots/budget_screen.jpg)       |
|                   **Settings 1**                  |                       **Settings 2**                       |
|  ![Settings 1](screenshots/settings_screen1.jpg)  |       ![Settings 2](screenshots/settings_screen2.jpg)      |

---

## 🛠️ Tech Stack

* **Language**: Java
* **UI**: Native Android XML Layouts, Custom Programmatic Drawables, Material Components
* **Persistence**: SharedPreferences with custom JSON (`org.json.JSONObject`) serialization
* **File Storage**: Android internal storage for locally managed attachments
* **File Handling**: Android native file picker and file viewer integration
* **Camera**: Android in-app camera integration
* **PDF Generation**: Native `android.graphics.pdf.PdfDocument`
* **Compatibility**: Android 7.0 (Nougat) and above

---

## 📥 Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/sameer021000/NoteCalc.git
   ```

2. **Open in Android Studio:**

   * File > Open > Select the cloned directory.

3. **Build & Run:**

   * Sync Gradle files.
   * Run on an emulator or physical Android device.

---

## 📋 Version History

* **v1.4.0** – Added Group PDF Export, a new interactive "What's New" rainbow card dialog, overhauled the Settings UI, unified premium touch feedback across the app, migrated strings for localization, and fixed background PDF generation crashes.
* **v1.3.0** – Added file attachments, in-app camera, PDF attachment integration, PDF timestamps, improved PDF layouts, dynamic filtering, enhanced bulk actions, and multiple sorting and persistence fixes.
* **v1.2.0** – Added dynamic PDF sorting and improved Cut workflow, dialog interactions, and date sorting accuracy.
* **v1.1.0** – Added record transfer, selective PDF export, isolated categories, unified bulk actions, and themed dialogs.
* **v1.0.0** – Initial release with expense/budget tracking, groups, PDF export, JSON backup, filtering, and premium UI.

📜 **[View the complete changelog](Changelog.md)**
