# Countdown Widget Pro Remain

<div align="center">

**The Ultimate Android Countdown Widget Application**

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)
![Material Design 3](https://img.shields.io/badge/Design-Material%203-orange.svg)

*Never miss an important moment with beautiful, reliable countdown widgets on your home screen*

</div>

## 📋 Overview

Countdown Widget Pro Remain is a native Android application that addresses the critical gaps in existing countdown apps: **reliability**, **deep customization**, and **smart context awareness**. Built with modern Android development tools, it provides pixel-perfect widgets that update reliably and adapt intelligently to event urgency.

### ✨ Key Features

- 🎯 **Flexible Event Management** - Create countdowns or count-ups (streaks) with full CRUD operations
- 🔁 **Recurring Events** - Support for weekly, monthly, and yearly repetitions
- 📅 **Calendar Import** - Seamlessly import events from Google Calendar
- 🎨 **Material You Integration** - Dynamic theming that adapts to your wallpaper
- 📱 **Multiple Widget Layouts** - 1x1, 2x1, 4x1, 4x2, 4x3, and list modes
- 🧠 **Smart Context** - Widgets change color based on urgency (Green → Yellow → Red)
- ⏱️ **Urgency Mode** - Shows seconds automatically in the final 24 hours
- 🔔 **Milestone Notifications** - Alerts at 1 month, 1 week, 1 day, 1 hour, and the moment
- 💾 **Backup & Restore** - Export/import events via JSON for data safety
- 🌙 **OLED Dark Mode** - True black design for battery efficiency on OLED screens

## 🏗️ Architecture

This project follows **Clean Architecture** principles with **MVVM** pattern:

```
┌─────────────────┐
│   UI Layer      │  Jetpack Compose + Material 3
│  (View/Screen)  │
└────────┬────────┘
         │
┌────────▼────────┐
│  ViewModel      │  State management + Business logic
└────────┬────────┘
         │
┌────────▼────────┐
│  Repository     │  Data coordination layer
└────────┬────────┘
         │
┌────────▼────────┐
│   Room DB       │  Local persistence (SQLite)
└─────────────────┘
```

### Tech Stack

| Component | Technology |
|-----------|------------|
| **Language** | Kotlin 1.9+ |
| **UI Framework** | Jetpack Compose |
| **Widget Framework** | Jetpack Glance (AppWidget) |
| **Database** | Room (SQLite) |
| **Async** | Kotlin Coroutines + Flow |
| **Background Work** | WorkManager |
| **Dependency Injection** | Manual (via Application class) |
| **Design System** | Material Design 3 |
| **Build Tool** | Gradle 8.x (KTS) |

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: Version 11 or later
- **Android SDK**: 
  - Minimum SDK: 24 (Android 7.0)
  - Target SDK: 36 (Android 15)
  - Compile SDK: 36

### Build Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/Countdown-Widget-Pro-Remain.git
   cd Countdown-Widget-Pro-Remain
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio should automatically sync Gradle files
   - If not, click `File > Sync Project with Gradle Files`

4. **Build the project**
   ```bash
   ./gradlew assembleDebug  # or .\gradlew assembleDebug on Windows
   ```

5. **Run on device/emulator**
   - Connect an Android device or start an emulator
   - Click the "Run" button in Android Studio (or press Shift+F10)

### Debug APK

To build a debug APK manually:
```bash
./gradlew clean assembleDebug
```
The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## 📦 Project Structure

```
com.countdown.widgetproremain/
│
├── data/
│   ├── local/          # Room database and DAOs
│   ├── model/          # Data models (CountdownEvent)
│   └── repository/     # Repository implementations
│
├── ui/
│   ├── addedit/        # Add/Edit event screen
│   ├── home/           # Main event list screen
│   ├── import/         # Calendar import screen
│   ├── navigation/     # Navigation graph
│   ├── settings/       # Settings screen
│   ├── theme/          # Material 3 theme configuration
│   ├── viewmodel/      # ViewModels for state management
│   └── widget/         # Widget configuration activity
│
├── util/
│   ├── BackupManager   # JSON export/import logic
│   ├── DateUtils       # Date/time formatting utilities
│   └── NotificationHelper # Notification creation
│
├── widget/
│   ├── CountdownWidget       # Glance widget implementation
│   └── CountdownWidgetReceiver # AppWidget receiver
│
├── worker/
│   └── CountdownWorker # Background tasks for notifications
│
├── CountdownApplication # Application class
└── MainActivity        # Main entry point
```

## 🎨 Features in Detail

### Smart Widget System

Widgets automatically adapt to event urgency:

- **Green Background**: >7 days remaining (Safe)
- **Yellow/Amber Background**: 1-7 days remaining (Warning)
- **Red Background**: <24 hours remaining (Urgent)
- **Blue Background**: Count-up events (Streaks)

### Widget Layouts

| Size | Description | Content |
|------|-------------|---------|
| **1x1** | Small Square | Days only |
| **2x1** | Horizontal Rectangle | Title + Days |
| **4x1** | Wide Rectangle | Title + Days + Hours |
| **4x2** | Detailed Card | Days + Hours + Minutes (+ Seconds if urgent) |
| **4x3+** | List Mode | Multiple events in vertical list |

### Notification System

Powered by WorkManager for reliability:

- **Milestone Alerts**: 30 days, 7 days, 1 day, 1 hour, "The Moment"
- **Sticky Notification**: Optional persistent notification for most urgent event
- **Smart Scheduling**: Notifications only trigger once per milestone

## 🧪 Testing

### Manual Testing Checklist

1. ✅ Create, edit, and delete countdown events
2. ✅ Test all widget sizes on home screen
3. ✅ Toggle Smart Colors in widget configuration
4. ✅ Import events from Google Calendar
5. ✅ Export/Import backup JSON files
6. ✅ Verify milestone notifications trigger correctly
7. ✅ Test dark mode and Material You theming

### Known Issues

- No automated tests currently implemented (planned for v2.0)
- Widget previews in picker may not show live data

## 📖 Usage Guide

### Creating a Countdown

1. Tap the **+** button on the home screen
2. Enter event title and select target date/time
3. Choose a color theme
4. Optionally enable "Count Up" for tracking streaks
5. Tap "Save"

### Adding a Widget

1. Long-press on home screen → Select "Widgets"
2. Find "Countdown Widget Pro Remain"
3. Drag desired size to home screen
4. Select event from list
5. Toggle "Show Seconds" or "Smart Colors" as desired
6. Confirm

### Backup & Restore

**Export:**
1. Navigate to Settings (⋮ menu)
2. Tap "Export All Events"
3. Choose save location
4. JSON file will be created with timestamp

**Import:**
1. Settings → "Import Events"
2. Select previously saved JSON file
3. Events will be restored (IDs regenerated)

## 🛣️ Roadmap

- [x] **Phase 1** - Core MVP (CRUD, Room DB, Basic Widget)
- [x] **Phase 2** - Widget Engine (Glance, Multiple Sizes)
- [x] **Phase 3** - Advanced Features (Notifications, Calendar Import, Smart Logic)
- [x] **Phase 4** - Polish & Backup (JSON Export/Import, UI Refinement)
- [ ] **Phase 5** - Cloud Sync (Firebase integration, cross-device sync)
- [ ] **Phase 6** - Pro Features (Custom fonts, icon packs, backgrounds)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

### Development Guidelines

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Test on multiple Android versions before submitting

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with ❤️ using Jetpack Compose and Material Design 3
- Inspired by user feedback on existing countdown apps
- Special thanks to the Android development community

---

<div align="center">

**Made with Kotlin & Jetpack Compose**

[Report Bug](https://github.com/yourusername/Countdown-Widget-Pro-Remain/issues) · [Request Feature](https://github.com/yourusername/Countdown-Widget-Pro-Remain/issues)

</div>
