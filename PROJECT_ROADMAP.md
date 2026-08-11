# Smart Electricity Consumption Predictor

## Project Status

This document tracks the development progress of the application.

---

# Phase 1 — Project Setup

Status: ✅ Completed

- Android Studio Project
- Git Repository
- Firebase Project
- Gradle Configuration
- Material 3
- Compose Setup

---

# Phase 2 — Authentication

Status: ✅ Completed

Features

- Splash Screen
- Login
- Registration
- Logout
- Session Restoration
- Google Sign-In
- Form Validation
- Firebase Error Handling

---

# Phase 3 — User Profile

Status: ✅ Completed

Features

- Profile Setup
- Profile Existence Check
- Create Profile
- View Profile
- Update Profile
- Delete Firestore Profile
- Profile Validation
- Created and Updated Timestamp Handling
- Loading, Retry, and Error Handling
- Firestore Integration at `users/{uid}`

Deleting a profile removes only the authenticated user's Firestore document. It does not
delete the Firebase Authentication account.

---

# Phase 4 — Appliance Management

Status: Planned

Features

- Add Appliance
- Edit Appliance
- Delete Appliance
- Appliance Categories
- Power Rating
- Daily Usage

---

# Phase 5 — Consumption Tracking

Status: Planned

Features

- Daily Consumption
- Weekly Consumption
- Monthly Consumption
- Historical Data

---

# Phase 6 — Prediction Engine

Status: Planned

Features

- Data Collection
- Prediction Model
- Consumption Forecast
- Estimated Monthly Cost

---

# Phase 7 — Reports

Status: Planned

Features

- Charts
- Graphs
- Usage History
- Export Reports

---

# Phase 8 — Settings

Status: Planned

Features

- Theme
- Notifications
- Profile Settings
- Logout

---

# Completed Architecture

- Hilt configured as the project dependency-injection mechanism
- FirebaseAuth and FirebaseFirestore provided through Hilt
- AuthRepository and ProfileRepository bound through Hilt
- AuthViewModel and ProfileViewModel use `@HiltViewModel` constructor injection
- Compose navigation uses `hiltViewModel()`
- ServiceLocator removed

---

# Firestore Collections

users

appliances

consumption_logs

predictions

---

# Current Sprint

Home Dashboard and Application Navigation

Tasks

- Design the Home Dashboard
- Refine authenticated application navigation
- Preserve completed authentication and profile routing

---

# Definition of Done

A feature is considered complete only when

- Implementation finished
- Code reviewed
- BUILD SUCCESSFUL
- Runtime tested
- No regressions introduced
- Git commit completed

---

# Future Improvements

- Unit Testing
- UI Testing
- Offline Cache
- WorkManager
- CI/CD
- Crashlytics
- Analytics

These are intentionally postponed until the core project is complete.
