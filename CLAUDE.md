# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Indiana is an Android app built with Kotlin and Jetpack Compose that allows users to browse and download Android artifacts built with Bitrise. The app serves as a "hunter for built artifacts" providing a mobile interface to manage Bitrise builds.

## Architecture

### Multi-Module Structure
- **app**: Main application module with UI screens, navigation, and dependency injection
- **api**: Auto-generated API client for Bitrise API using OpenAPI generator
- **theme**: Shared UI components and theming system

### Key Architectural Components
- **Dagger Hilt**: Dependency injection framework used throughout the app
- **Navigation 3**: Uses AndroidX Navigation3 with custom `AppBackStack` for navigation management
- **Room Database**: Local data persistence for projects and user preferences
- **Retrofit + Moshi**: Network layer for API communication
- **Firebase**: Analytics, crashlytics, and performance monitoring (configurable)

### Navigation Architecture
The app uses a custom navigation system built on Navigation3:
- `AppBackStack` (`app/src/main/java/com/davidmedenjak/indiana/AppBackStack.kt`): Central navigation controller
- `SessionManager` (`app/src/main/java/com/davidmedenjak/indiana/session/SessionManager.kt`): Handles authentication state
- Screen graphs are defined as `NavKey` objects with corresponding routes

### Data Layer
- **Room Database**: `AppDatabase` with `ProjectDao` for local data storage
- **API Layer**: Auto-generated from OpenAPI spec (`api/bitrise.json`)
- **User Settings**: SharedPreferences wrapper for app configuration

## Common Development Commands

### Build Commands
```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build artifacts
./gradlew clean
```

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest

# Run specific test
./gradlew test --tests "com.davidmedenjak.indiana.SpecificTest"
```

### Installation Commands
```bash
# Install debug build
./gradlew installDebug

# Uninstall debug build
./gradlew uninstallDebug
```

### API Code Generation
The API module auto-generates Kotlin client code from the OpenAPI specification:
```bash
# Regenerate API client (happens automatically during build)
./gradlew :api:openApiGenerate
```

## Key Development Patterns

### Dependency Injection
All major components use Hilt for DI:
- `@HiltAndroidApp` on the Application class
- `@AndroidEntryPoint` on Activities and Fragments
- `@Module` and `@InstallIn` for providing dependencies

### Screen Implementation
Each screen follows a consistent pattern:
- Graph object implementing `NavKey` for navigation
- Route composable for the UI
- ViewModel for state management (using `@HiltViewModel`)
- Repository pattern for data access

### State Management
- ViewModels use `StateFlow` and `MutableStateFlow` for reactive state
- UI state is collected with `collectAsStateWithLifecycle()`
- Authentication state is managed globally through `SessionManager`

## Firebase Configuration
The app includes Firebase services that are configurable through `UserSettings`:
- Analytics: Can be enabled/disabled per user preference
- Crashlytics: Configurable crash reporting
- Performance: Performance monitoring toggle

## Theme System
The theme module provides:
- Custom design system components in `theme/src/main/java/com/davidmedenjak/indiana/theme/ui/`
- Atomic design pattern (atoms, molecules)
- Material 3 theming with custom colors and typography

## Authentication Flow
1. User enters Bitrise API token in `AuthRoute`
2. `SessionManager.authenticate()` validates the token
3. On success, navigation switches to `ProjectsGraph`
4. Token is stored in `UserSettings` for persistence

## Database Schema
- `ProjectEntity`: Stores project information
- `ProjectLastViewed`: Tracks recently viewed projects
- Room database with KTX coroutines support

## Build Configuration
- Target SDK: 36
- Min SDK: 24
- Kotlin version: 2.2.0
- Compose BOM: 2025.06.01
- Uses Gradle version catalogs (`gradle/libs.versions.toml`)