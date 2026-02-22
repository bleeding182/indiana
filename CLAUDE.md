# CLAUDE.md

- Do not comment on your own actions on every step, be concise
- Skip any praises, and comments on how everything became better, say "Done!" when the work is finished and keep the summary _short_
- Use :theme module components instead of Material 3 directly
- Create missing components in :theme module

## Project Overview

Indiana is an Android app built with Kotlin and Jetpack Compose integrating the public API of Bitrise.io
It shows an overview of the users projects, builds, and build artifacts with an option to download and install them.

## Architecture

### Multi-Module Structure
- **app**: Main application module with UI screens, navigation, and dependency injection
- **api**: Auto-generated API client for Bitrise API using OpenAPI generator
- **theme**: Shared UI components and theming system
- **lint**: Custom Android lint checks (consumed via `lintChecks`)
- **apidoc** / **apidoc-annotations**: KSP processor that generates `api.md` component catalog for :theme
- **build-setup**: Gradle convention plugin (`indiana.convention`) for shared build config

### Key Architectural Components
- **Dagger Hilt**: Dependency injection framework used throughout the app
- **Navigation 3**: Uses AndroidX Navigation3 with custom `AppBackStack` for navigation management
- **Room Database**: Local data persistence for projects and user preferences
- **Retrofit + kotlinx.serialization**: Network layer for API communication
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

### Screen Implementation
Each screen follows a consistent pattern:
- Graph object implementing `NavKey` for navigation
- Route composable for the UI
- ViewModel for state management (using `@HiltViewModel`)
- Repository pattern for data access

## Build Configuration
- Min SDK: 24 / Target SDK: 36
- Kotlin: 2.3.10
- Compose BOM: 2026.02.00 (alpha)
- Uses Gradle version catalogs (`gradle/libs.versions.toml`)
- Convention plugin in `build-setup/` applies shared SDK, Java 17, and Kotlin config