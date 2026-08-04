# NoteCalc 📓🧮

![Android](https://img.shields.io/badge/Platform-Android-green)
![Java](https://img.shields.io/badge/Language-Java-blue)
![Version](https://img.shields.io/badge/Version-1.0.0-orange)
![Status](https://img.shields.io/badge/Status-Active-success)

**NoteCalc** is a premium, offline-first Android expense tracker and calculator designed to bridge the gap between simple note-taking and structured financial tracking. It combines a stunning, modern interface with powerful tools like dual tracking modes, detailed PDF exports, and an organized dashboard for an intuitive accounting experience.

---

## 📌 App Information

- **Current Version:** v1.0.0
- **Minimum SDK:** Android 7.0 (Nougat)
- **Language:** Java
- **Last Updated:** August 2026

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
*   **Dual Modes (Expenses & Budgets)**: Track both your outgoing expenses and incoming budgets seamlessly. The app automatically calculates totals and dynamically displays balances.
*   **Custom Groups & Accounts**: Organize your financial data effortlessly. Create standalone accounts (e.g., "Trip to Paris", "Monthly Groceries") or group related accounts together under custom folders.
*   **Smart Dashboard**: A beautifully designed management screen featuring dynamic search, quick sorting (Latest, Title, Total Amount), and interactive empty states.
*   **Advanced Filtering**: Search through records by description or remarks, and filter by exact date or amount ranges.

### 🎯 **Reporting & Backup**
*   **PDF Export Engine**: Generate beautiful, multi-table, paginated PDF reports of your accounts directly on your device, complete with footers and dynamic headers. Support for exporting individual accounts or all data at once.
*   **Privacy-First & Offline**: No cloud syncing forced upon you. All data is securely stored locally on your device via SharedPreferences using an optimized JSON serialization structure.
*   **JSON Import / Export**: Easily backup your entire workspace to a JSON file and restore it whenever you switch devices or need to recover data.

### 🎨 **Premium UI/UX**
*   **Modern Aesthetics**: Features glassmorphism, rounded layouts, smooth animations, and clean Material Design. Edge-to-edge window insets provide an immersive visual experience.
*   **Dynamic Interactions**: Fluid `StateListDrawables` for instant tactile feedback on touch interactions.
*   **Intuitive Editor**: Add records quickly using a refined form, supporting custom dates, optional remarks, and auto-sorting based on your preferences.

---

## 📱 Screenshots

|                          Dashboard Mode                          |                          Account Editor                          |
|:----------------------------------------------------------------:|:----------------------------------------------------------------:|
|  ![Dashboard](screenshots/notecalc_dashboard.jpg)                |  ![Editor](screenshots/notecalc_editor.jpg)                      |
| ![PDF Export](screenshots/notecalc_pdf_export.jpg)               | ![Settings](screenshots/notecalc_settings.jpg)                   |

---

## 🛠️ Tech Stack

*   **Language**: Java
*   **UI**: Native Android XML Layouts, Custom Programmatic Drawables, Material Components
*   **Persistence**: SharedPreferences with custom JSON (`org.json.JSONObject`) serialization
*   **PDF Generation**: Native `android.graphics.pdf.PdfDocument`
*   **Compatibility**: Android 7.0 (Nougat) and above

---

## 📥 Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/sameer021000/NoteCalc.git
    ```
2.  **Open in Android Studio**:
    *   File > Open > Select the cloned directory.
3.  **Build & Run**:
    *   Sync Gradle files.
    *   Run on an emulator or physical Android device.
