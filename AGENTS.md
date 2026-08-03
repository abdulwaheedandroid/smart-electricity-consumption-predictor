# AGENTS.md

# Smart Electricity Consumption Predictor

This document defines the development standards for this repository.

---

# Project Overview

Smart Electricity Consumption Predictor is a native Android application developed using modern Android technologies.

Current Technology Stack

- Kotlin
- Jetpack Compose
- MVVM
- Material Design 3
- Firebase Authentication
- Cloud Firestore
- StateFlow
- Coroutines

The objective is to build a clean, maintainable, scalable application while following Android best practices.

---

# Development Philosophy

Before writing code:

1. Understand the existing implementation.
2. Reuse existing classes whenever possible.
3. Avoid unnecessary refactoring.
4. Keep changes minimal and focused.
5. Preserve existing functionality.

---

# Architecture

Follow MVVM Architecture.

UI Layer

- Compose Screens
- Navigation
- UI State

Presentation Layer

- ViewModels
- State Management
- Business Logic

Data Layer

- Firebase Authentication
- Cloud Firestore
- Repository Classes

Never access Firebase directly from a Composable.

---

# State Management

Use immutable UI state.

Expose:

StateFlow<UiState>

Never expose MutableStateFlow outside ViewModels.

Avoid mutable shared state.

---

# Navigation Rules

Reuse existing routes.

Do not create duplicate navigation graphs.

Navigation decisions should be driven by authentication state.

Avoid navigation logic inside repositories.

---

# Authentication

Current Features

- Email Registration
- Email Login
- Logout
- Session Restoration
- Google Sign-In (FirebaseUI)

Authentication is considered complete.

Do not redesign the authentication architecture unless explicitly requested.

---

# Firestore Rules

Profile Path

users/{authenticatedUid}

The document ID must be `FirebaseAuth.currentUser.uid`.

Document Structure

- uid
- fullName
- email
- age
- gender
- cellNumber
- createdAt
- updatedAt

One authenticated user must have exactly one profile document.

Firestore errors must remain distinct from Profile Not Found. A failed Firestore read must
never be interpreted as a missing profile document.

Deleting a profile deletes only `users/{uid}`. It must not delete the Firebase Authentication
account.

---

# Compose Guidelines

Composable functions should

- remain small
- be reusable
- be stateless whenever possible

Move business logic to ViewModels.

Avoid long composables.

---

# Kotlin Guidelines

Prefer

- data classes
- extension functions
- sealed classes
- immutable collections

Avoid

- unnecessary null assertions
- duplicated code
- deeply nested logic

---

# Dependency Management

Do not introduce

- Hilt
- Koin
- Dagger

unless explicitly requested.

Reuse the existing dependency creation approach.

Dependency injection will be migrated through a separate approved architecture task after
v1.1.0. Do not include that migration in profile, dashboard, or other feature work.

---

# Error Handling

Always provide user-friendly error messages.

Never expose raw Firebase exceptions to users.

---

# Build Requirements

Every implementation must

- Build successfully
- Resolve compilation errors
- Preserve existing features

Stop only after BUILD SUCCESSFUL.

---

# Git Guidelines

Each completed feature should have its own commit.

Example

feat(auth): complete Firebase authentication

fix(login): resolve loading state

feat(profile): add Firestore profile module

---

# Before Modifying Code

Always inspect the existing implementation.

List the files that will be modified.

Explain why each file requires changes.

---

# After Completing Work

Provide

1. Modified files
2. Summary of changes
3. Remaining manual steps
4. Runtime testing checklist

---

# Never Do

Do not

- redesign completed modules
- rename packages unnecessarily
- duplicate ViewModels
- duplicate repositories
- duplicate navigation graphs
- introduce new architecture
- migrate libraries
- modify unrelated files

Keep changes focused and minimal.
