# NoteCalc 📓🧮

![Android](https://img.shields.io/badge/Platform-Android-green)
![Java](https://img.shields.io/badge/Language-Java-blue)
![Version](https://img.shields.io/badge/Version-1.3.0-orange)
![Status](https://img.shields.io/badge/Status-Active-success)

**NoteCalc** is a premium, offline-first Android expense tracker and calculator designed to bridge the gap between simple note-taking and structured financial tracking. It combines a modern interface with powerful tools such as dual tracking modes, file attachments, PDF reporting, JSON backups, advanced filtering, and an organized dashboard for an intuitive accounting experience.

---

## 📌 App Information

* **Current Version:** v1.3.0
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
* **Advanced Filtering**: Search through records by description or remarks, and filter by exact dates or amount ranges.
* **Bulk Actions & Transfers**: Seamlessly cut or copy selected records between lists, delete or export records, and quickly create a new list from your selection.
* **Isolated Category Tracking**: Category dropdown suggestions are isolated to the currently active list, keeping autocomplete clean and relevant.
* **Dynamic PDF Sorting**: Sort PDF statements by Serial Number, Description, Date, or Amount before generating the report.

### 📎 **File Attachments**

* **Multiple Attachments**: Attach up to 3 files to each record, including images, PDFs, and documents.
* **In-App Camera**: Capture photos directly from the attachment interface without leaving NoteCalc.
* **Automatic Timestamps**: Photos captured through the in-app camera are automatically timestamped and securely stored in the app's internal storage.
* **Attachment Chips**: Attached files are displayed as beautifully themed chips directly within records.
* **Native File Viewing**: Open attached files directly through Android's native file viewer.

### 🎯 **Reporting & Backup**

* **PDF Export Engine**: Generate beautiful, multi-table, paginated PDF reports of your accounts directly on your device.
* **Flexible PDF Export**: Export individual accounts, all data at once, or generate clean, budget-free statements for a specific selection of records.
* **Privacy-First & Offline**: No cloud syncing is forced upon you. Your data remains stored locally on your device.
* **JSON Import / Export**: Easily back up your entire workspace to a JSON file and restore it whenever you switch devices or need to recover your data.

### 🎨 **Premium UI/UX**

* **Modern Aesthetics**: Features rounded layouts, smooth animations, custom styling, and clean Material Design. Edge-to-edge window insets provide an immersive visual experience.
* **Dynamic Interactions**: Fluid `StateListDrawable` feedback provides responsive visual interactions throughout the application.
* **Themed Dialogs**: Custom dialogs are designed to maintain a consistent and polished visual language across the application.
* **Interactive Attachments**: Attachment controls and form actions feature refined text styling, scale animations, and responsive touch interactions.
* **Dynamic Filter State**: The **Filter by Category** icon dynamically changes appearance to clearly indicate when category filtering is active.
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

* **v1.3.0** – File attachments, in-app camera, attachment viewing, improved bulk actions, dynamic filtering, and UI refinements.
* **v1.2.0** – Dynamic PDF sorting, improved Cut workflow, dialog interactions, and date sorting accuracy.
* **v1.1.0** – Record transfer, selective PDF export, isolated categories, unified bulk actions, and themed dialogs.
* **v1.0.0** – Initial release with expense/budget tracking, groups, PDF export, JSON backup, filtering, and premium UI.

📜 **[View the complete changelog](Changelog.md)**
