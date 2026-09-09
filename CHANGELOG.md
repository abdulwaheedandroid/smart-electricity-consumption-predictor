# Changelog

All notable changes to Smart Electricity Consumption Predictor are documented in this file.
The project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Authentication and navigation

- Use a FirebaseUI Email/Google login landing screen; remove the obsolete custom email forms.
- Deliver FirebaseUI results to an Activity-scoped auth ViewModel and route from session state,
  without replayed navigation events or temporary Activity callbacks.
- Check profiles after authentication and app restart: existing profiles open Home, missing
  profiles open setup, and failed reads offer Retry / Sign out without enabling profile creation.
- Return profile edits to the existing Home entry. Profile deletion clears the protected stack
  and deleted form fields while retaining the Firebase Authentication account.
- Clear the authenticated stack on sign-out and expose progress/errors on Home and profile screens.
- Reuse prototype FirebaseUI light/dark, button, password/recovery, and inset styling.
- Add auth/profile state tests, routing-policy tests, and device back-stack tests.
- Preserve Home, Appliance Management, and the existing Firestore schema.

Device regression checklist: Email/Google success and cancellation, rotation while FirebaseUI
is open, password recovery, light/dark and keyboard/inset rendering, app restart with an existing
or missing profile, failed profile reads and retry/sign-out, profile create/edit/delete, Back after
deletion/logout, and Home-to-Appliances navigation. Device tests require a connected emulator/device.

### Architecture

- Migrated dependency injection to Hilt and removed the ServiceLocator.
- Provided FirebaseAuth and FirebaseFirestore through Hilt.
- Bound AuthRepository and ProfileRepository to their implementations through Hilt.
- Migrated AuthViewModel and ProfileViewModel to `@HiltViewModel` constructor injection.
- Updated Compose navigation to obtain ViewModels with `hiltViewModel()`.

## [1.2.0] - 2026-08-13

### Added

- Appliance Management with add, view, edit, and delete operations.
- Delete confirmation and cancellation for appliance removal.
- Appliance search by name.
- Appliance input validation and unit tests.

### Firestore

- Stored appliances at `users/{uid}/appliances/{applianceId}`.
- Added and deployed ownership-scoped Firestore security rules for appliances.
- Manually tested Firestore appliance access and security behavior.

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
