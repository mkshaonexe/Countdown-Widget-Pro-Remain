# Product Requirement Document (PRD): Countdown Widget Pro Remain

## 1. Executive Summary
**Product Name:** Countdown Widget Pro Remain
**Platform:** Android (Native)
**Goal:** Create the ultimate countdown widget application that addresses current market gaps: reliability, deep customization, and seamless synchronization.
**Target Audience:** Power users, students, professionals, and anyone needing reliable event tracking.

## 2. Market Analysis & Missing Features
Research into current top competitors ("Big Days", "Time Until", "Countdown Widget") reveals significant user pain points:
*   **Unreliable Widgets:** Widgets often freeze or fail to update at midnight.
*   **Limited Customization:** Users want more than just "dark/light" themes. They demand granular control over fonts, colors (Material You), and backgrounds.
*   **No "Smart" Context:** Widgets are static. Users want widgets that change based on urgency (e.g., turning red 1 day before).
*   **Lack of Seconds:** Many widgets only show days, missing the excitement of "seconds" precision.
*   **Poor Sync:** Lack of cross-device sync or reliable calendar import.
*   **Intrusive Model:** Heavy ads and expensive subscriptions for basic features.

## 3. Functional Requirements

### 3.1. Core Application
*   **Event Management:**
    *   Create, Edit, Delete countdown events.
    *   Support for "Count Up" (Time Since) for tracking streaks (e.g., "Days Since Quitting Smoking").
    *   Recurring events (Annual, Monthly, Weekly).
    *   **Calendar Import:** Permission-based import from Google Calendar.
*   **Dashboard:**
    *   Grid/List view of active countdowns.
    *   Sorting (Soonest, Alphabetical, Created Date).
    *   **Search:** Quick find for specific events.

### 3.2. Widget System (The Core Value)
*   **Technology:** built using `Jetpack Glance` for modern, responsive, and battery-efficient widgets.
*   **Types:**
    *   **Simple Card (1x1, 2x1):** Days only.
    *   **Detailed Card (4x1, 4x2):** Days, Hours, Minutes.
    *   **Full Grid (4x3, 4x4):** Multiple events in one widget.
    *   **Precise:** Option to show seconds (refreshes frequently when screen is on).
*   **Differentiation - "Smart Widget":**
    *   Widget background changes color as deadline approaches (Green -> Yellow -> Red).
    *   "Urgency Mode": Shows seconds only in the final 24 hours.

### 3.3. Customization Engine
*   **Material You Integration:** Auto-extract colors from user wallpaper.
*   **Manual Overrides:**
    *   Custom Background Images (with blur/dim controls).
    *   Font Selection (Google Fonts integration).
    *   Icon Packs for event categories.

### 3.4. Notifications
*   **Milestone Alerts:** 1 month, 1 week, 1 day, 1 hour, "The Moment".
*   **Sticky Notification:** Optional persistent notification for the most urgent event.

### 3.5. Data & Sync
*   **Local:** Android Room Database (Offline first).
*   **Cloud:** Google Sign-In + Firebase Firestore for cross-device sync.
*   **Backup:** JSON Export/Import for manual backups.

## 4. UI/UX Guidelines
*   **Theme:** Dark Mode by default (OLED black compliant). High contrast for readability.
*   **Style:** Material Design 3 (Cards, rounded corners, floating action buttons).
*   **Reference Concept:**
    ![Proposed UI Concept](file:///C:/Users/MK Shaon/.gemini/antigravity/brain/45aeb6aa-632d-48a6-bb4b-27e84ccb5a0d/countdown_app_ui_mockup_1768776986104.png)
*   **Visual Hierarchy:** The "Time Remaining" is the hero element. Title is secondary.

## 5. Technical Architecture
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose
*   **Widget Framework:** Jetpack Glance
*   **Database:** Room (SQLite)
*   **Sync:** Firebase / WorkManager
*   **Architecture Pattern:** MVVM (Model-View-ViewModel) + Clean Architecture.

## 6. Implementation Roadmap
1.  **Phase 1: MVP Core** - App shell, Room DB, Basic CRUD, One basic Widget.
2.  **Phase 2: Widget Engine** - Glance implementation, sizing, basic customization.
3.  **Phase 3: Advanced Features** - Notifications, Imports, "Smart" logic.
4.  **Phase 4: Polish & Sync** - Cloud backup, Animations, Final UI Polish.

## 7. Monetization Strategy
*   **Freemium:** Core features free.
*   **Pro (One-time purchase):** Cloud Sync, Unlimited Widgets, Full icon pack access.
