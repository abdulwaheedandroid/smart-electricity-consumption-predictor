# Changelog

All notable changes to Smart Electricity Consumption Predictor are documented in this file.
The project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

No unreleased changes are currently documented.

## [1.1.0] - 2026-08-03

### Added

- User Profile setup, viewing, editing, and deletion backed by Cloud Firestore.
- Profile validation for full name, age, gender, and cell number.
- Read-only authenticated email and internally managed Firebase Authentication UID.
- Loading, saving, deletion confirmation, retry, and user-friendly error states.
- Server-managed `createdAt` and `updatedAt` profile timestamps.

### Changed

- Authenticated users are routed to Profile Setup when `users/{uid}` is missing and to Home when a profile exists.
- Profile deletion removes only `users/{uid}` and returns the authenticated user to Profile Setup.

### Fixed

- Firestore failures are handled separately from a missing profile document.
- Duplicate profile save and delete submissions are prevented while an operation is active.

### Security

- Added and deployed UID-scoped Firestore rules so authenticated users can access only their own `users/{uid}` profile document.
- Restricted profile documents to the approved schema and preserved `createdAt` during updates.

### Architecture

- Added a profile repository and ViewModel using coroutines, immutable `StateFlow` UI state, and the existing service-locator dependency approach.
- Kept Firebase operations out of Composable functions.

## [1.0.0] - 2026-07-30

### Added

- Email and password registration and login with Firebase Authentication.
- Google Sign-In through FirebaseUI.
- Session restoration, splash routing, and authenticated sign-out.
- Authentication form validation, password visibility controls, loading states, and user-friendly errors.

### Fixed

- Finalized the Google Sign-In result flow and FirebaseUI integration.

### Architecture

- Established MVVM authentication using repository abstractions, coroutines, immutable `StateFlow` UI state, and Compose navigation.
