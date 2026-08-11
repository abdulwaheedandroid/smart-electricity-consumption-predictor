# Smart Electricity Consumption Predictor

A native Android application that helps users manage household appliances, monitor electricity consumption, and estimate future energy usage through a machine-learning-based prediction system.

> This project is currently under active development.

---

## Overview

Smart Electricity Consumption Predictor is an Android application developed as a university project and portfolio application.

The system is designed to help users:

- Create and manage a secure account
- Maintain a personal profile
- Add and manage household appliances
- Record appliance usage
- Calculate electricity consumption
- Review daily, weekly, and monthly usage
- Estimate electricity costs
- Predict future electricity consumption
- View reports and usage trends

---

## Current Project Status

| Module | Status |
|---|---|
| Project Setup | Complete |
| Firebase Setup | Complete |
| Email and Password Authentication | Complete |
| Form Validation and Error Handling | Complete |
| Session Restoration | Complete |
| Logout | Complete |
| Google Sign-In | Complete |
| User Profile | Complete |
| Appliance Management | Planned |
| Consumption Tracking | Planned |
| Prediction Engine | Planned |
| Reports and Analytics | Planned |
| Settings | Planned |

For detailed development progress, see [`PROJECT_ROADMAP.md`](PROJECT_ROADMAP.md).

---

## Features

### Authentication

- Email and password registration
- Email and password login
- Firebase Authentication
- Login form validation
- User-friendly authentication errors
- Password visibility controls
- Session restoration
- Secure logout
- Google Sign-In integration through FirebaseUI

### User Profile

Implemented and runtime-tested features:

- Profile setup for authenticated users
- Profile-existence check after email/password or Google authentication
- Create, read, view, update, and delete profile data in Cloud Firestore
- Immutable `StateFlow` UI state with validation, loading, retry, and error feedback
- Server timestamp handling that preserves `createdAt` and refreshes `updatedAt`
- Explicit confirmation before deleting the Firestore profile
- Navigation to Profile Setup when an authenticated user has no profile
- Navigation to Home when an authenticated user has an existing profile

The profile document is stored at `users/{uid}`, where `uid` is the authenticated
Firebase user's UID. The UID is used internally as the document ID and is not displayed
on the profile form. Email is obtained from Firebase Authentication and displayed as a
read-only field.

### Appliance Management

Planned features:

- Add household appliances
- Edit appliance information
- Delete appliances
- Assign appliance categories
- Record power ratings
- Record estimated daily usage

### Electricity Consumption Tracking

Planned features:

- Calculate appliance-level consumption
- Track daily consumption
- Track weekly consumption
- Track monthly consumption
- Maintain historical usage records
- Estimate electricity costs

### Prediction Engine

Planned features:

- Prepare consumption data for prediction
- Predict future electricity usage
- Estimate monthly consumption
- Estimate expected electricity cost
- Identify usage trends

### Reports and Analytics

Planned features:

- Electricity usage summaries
- Historical usage reports
- Charts and visualizations
- Appliance-level comparisons
- Prediction results
- Cost estimates

---

## Technology Stack

### Android

- Kotlin
- Jetpack Compose
- Material Design 3
- Android Jetpack
- Navigation Compose
- ViewModel
- StateFlow
- Kotlin Coroutines
- Hilt dependency injection

### Architecture

- MVVM
- Repository pattern
- Unidirectional UI state
- Separation of UI, presentation, and data responsibilities
- Hilt for dependency injection

### Backend

- Firebase Authentication
- Cloud Firestore
- FirebaseUI Authentication

### Development Tools

- Android Studio
- Gradle Kotlin DSL
- Git
- GitHub
- OpenAI Codex
- GitHub Copilot

---

## Architecture

The project follows the MVVM architectural pattern.

```text
Compose UI
    ↓
ViewModel
    ↓
Repository
    ↓
Firebase Authentication / Cloud Firestore
```

### UI Layer

Responsible for:

- Compose screens
- Reusable UI components
- User interaction
- Displaying immutable UI state
- Navigation events

### Presentation Layer

Responsible for:

- ViewModels
- StateFlow-based UI state
- Form validation
- Business rules
- Coordinating repository operations

### Data Layer

Responsible for:

- Firebase Authentication operations
- Cloud Firestore operations
- Data models
- Repository implementations
- Mapping backend errors to application-friendly results

Composable functions must not access Firebase APIs directly.

### Dependency Injection

Hilt is the project's dependency-injection mechanism. The application is initialized with
`@HiltAndroidApp`, and Compose navigation obtains ViewModels with `hiltViewModel()`.

- `FirebaseAuth` and `FirebaseFirestore` are provided through Hilt.
- `AuthRepository` and `ProfileRepository` are bound to their implementations through Hilt.
- `AuthViewModel` and `ProfileViewModel` use `@HiltViewModel` and constructor injection.
- The former `ServiceLocator` has been completely removed.

---

## Planned Project Structure

The exact structure may evolve as new modules are implemented.

```text
app/src/main/java/<application-package>/
│
├── data/
│   ├── model/
│   └── repository/
│
├── navigation/
│
├── ui/
│   ├── components/
│   ├── screens/
│   │   ├── auth/
│   │   ├── profile/
│   │   ├── appliances/
│   │   ├── consumption/
│   │   ├── prediction/
│   │   └── settings/
│   └── theme/
│
├── utils/
│
├── viewmodel/
│
└── MainActivity.kt
```

---

## Firestore Data Model

The Firestore schema will evolve as modules are implemented.

### Users Collection

```text
users/{uid}
```

Implemented fields:

```text
uid
fullName
email
age
gender
cellNumber
createdAt
updatedAt
```

The document ID matches the authenticated Firebase user's UID. UID is retained internally
and is not displayed in the UI. Email is sourced from Firebase Authentication and displayed
read-only.

### Other Planned Collections

```text
appliances
consumption_logs
predictions
```

A detailed schema will be added after each Firestore module is finalized.

---

## Application Flow

```text
Splash
  ↓
Authentication Check
  ├── Unauthenticated → Login / Registration
  └── Authenticated
         ↓
    Profile Check
      ├── Missing Profile → Profile Setup → Home
      └── Existing Profile → Home
         ↓
  Appliance Management
         ↓
  Consumption Tracking
         ↓
  Prediction and Reports
```

---

## Screens

### Implemented

- Splash screen
- Login screen
- Registration screen
- Home screen
- Profile setup screen
- Profile view and edit screen
- Delete profile confirmation

### Planned

- Appliance list screen
- Add appliance screen
- Edit appliance screen
- Consumption dashboard
- Usage history screen
- Prediction dashboard
- Reports screen
- Settings screen

---

## Screenshots

Screenshots will be added as the user interface is finalized.

Suggested location:

```text
docs/screenshots/
```

Example future layout:

```markdown
![Login Screen](docs/screenshots/login-screen.png)
![Registration Screen](docs/screenshots/registration-screen.png)
![Dashboard](docs/screenshots/dashboard.png)
```

---

## Getting Started

### Prerequisites

Install:

- Android Studio
- JDK 17
- Android SDK required by the project
- Git

You will also need access to a Firebase project.

### Clone the Repository

```bash
git clone <your-repository-url>
cd <repository-folder>
```

Replace `<your-repository-url>` and `<repository-folder>` with the actual repository details.

### Firebase Configuration

1. Create or open the Firebase project.
2. Register the Android application using the correct package name.
3. Add the required SHA-1 and SHA-256 fingerprints.
4. Enable Email/Password authentication.
5. Enable Google authentication when testing Google Sign-In.
6. Create a Cloud Firestore database.
7. Download the latest `google-services.json`.
8. Place it inside:

```text
app/google-services.json
```

> Do not publish private credentials or sensitive configuration outside the intended Firebase configuration workflow.

### Build the Project

Open the project in Android Studio and allow Gradle to synchronize.

Alternatively, run:

#### Windows

```bash
gradlew.bat build
```

#### macOS or Linux

```bash
./gradlew build
```

---

## Runtime Authentication Regression Checklist

Continue verifying the completed authentication module against these regression cases:

- Registration with a valid email and password
- Invalid email validation
- Empty email validation
- Empty password validation
- Minimum password-length validation
- Login with valid credentials
- Error message for invalid credentials
- Session restoration after reopening the app
- Logout and return to the login screen
- Google Sign-In
- Google-authenticated user appears in Firebase Authentication

---

## Development Guidelines

Repository-level development instructions are documented in:

[`AGENTS.md`](AGENTS.md)

Important principles include:

- Inspect existing code before modifying it
- Preserve completed functionality
- Follow MVVM
- Avoid duplicate classes, ViewModels, repositories, and navigation graphs
- Keep changes focused on the requested feature
- Use the existing Hilt dependency-injection setup; do not introduce another DI mechanism
- Build the project after changes
- Consider a feature complete only after runtime testing

---

## Definition of Done

A feature is complete only when:

- Implementation is finished
- Architecture remains consistent
- Validation and error handling are included
- The project builds successfully
- Runtime behavior is tested
- Existing functionality still works
- Documentation is updated where required
- Changes are committed to Git

---

## Documentation Maintenance

The following files should be reviewed after major project changes:

- `README.md`
- `AGENTS.md`
- `PROJECT_ROADMAP.md`

Documentation should be updated whenever:

- A module is completed
- A major dependency is added or removed
- Architecture changes
- Firestore collections or fields change
- Authentication behavior changes
- Build requirements change
- New setup steps are introduced

---

## Roadmap

1. Build the Home Dashboard and application navigation
2. Build Appliance Management
3. Add Consumption Tracking
4. Implement electricity cost calculations
5. Build the Prediction Engine
6. Add Reports and Analytics
7. Complete Settings
8. Expand automated testing
9. Add screenshots and diagrams
10. Prepare the final university submission

---

## Future Improvements

Potential improvements after the core application is complete:

- Unit testing
- Compose UI testing
- Offline caching
- WorkManager
- Firebase Crashlytics
- Analytics
- CI/CD
- Accessibility improvements
- Localization
- Dark theme
- Exportable reports

These enhancements are intentionally postponed until the core functionality is stable.

---

## Author

**Abdul Waheed**

Android Developer  
Oracle Certified Java Programmer

---

## Project Purpose

This application is being developed for educational, research, and portfolio purposes.

---

## License

No public license has been selected yet.

Unless a license is added, all rights remain with the project author.
