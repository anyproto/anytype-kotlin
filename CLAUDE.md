# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Anytype is an Android client for a local-first, block-based collaborative workspace. The app uses Clean Architecture with sophisticated modular design, Jetpack Compose (hybrid with traditional Views), and integrates with a Go backend via JNI middleware.

## Common Development Commands

### Building
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew clean build            # Clean and rebuild
```

### Testing
```bash
make test_debug_all              # Run all unit tests (preferred)
./gradlew testDebugUnitTest      # Run debug unit tests
make compile_android_test_sources # Compile instrumentation tests
make pr_check                    # Full PR validation
```

### Code Quality
```bash
./gradlew lintDebug              # Run lint on debug variant
./gradlew lintFix                # Auto-fix lint issues
./gradlew check                  # Run all checks (lint + tests)
```

### Middleware
```bash
make update_mw                   # Update Go middleware
make setup_local_mw              # Setup local middleware development
```

## Architecture Overview

### Module Structure
- **30+ Gradle modules** with feature-based organization
- **Clean Architecture**: `domain/` (use cases) → `data/` (repositories) → `presentation/` (ViewModels)
- **Feature modules**: `feature-chats`, `feature-object-type`, `feature-properties`, etc.
- **Core modules**: `core-ui`, `core-models`, `core-utils`

### Key Components
- **Dependency Injection**: Sophisticated Dagger 2 setup with `MainComponent` as root
- **UI**: Jetpack Compose + traditional Views (gradual migration)
- **State Management**: ViewModels with StateFlow/LiveData
- **Backend**: Go middleware integration via JNI and Protocol Buffers

### Data Flow
```
UI (Compose/Views) → ViewModels → Use Cases → Repositories → Middleware (Go)
```

## Key Entry Points

- `/app/src/main/java/com/anytypeio/anytype/app/AndroidApplication.kt` - Application setup
- `/app/src/main/java/com/anytypeio/anytype/di/main/MainComponent.kt` - DI root component
- `/app/src/main/java/com/anytypeio/anytype/di/common/ComponentManager.kt` - Component lifecycle management

## Development Guidelines

### Adding New Features
1. Create feature module following `feature-*` pattern
2. Implement Clean Architecture layers (domain → data → presentation)
3. Study existing ViewModels in `/presentation/` for patterns
4. Use `ComponentManager` for DI component lifecycle

### UI Development
- Follow design system in `/core-ui/`
- Use existing Compose components where possible
- Maintain consistency with hybrid UI approach
- Check `/docs/design_system.md` for guidelines

### Testing
- Unit tests: Follow patterns in existing test directories
- Use Robolectric for Android unit tests
- UI tests: Use Espresso for traditional Views, Compose Testing for Compose screens

## Critical Notes

- **Java 17 required** - Ensure correct JDK version
- **Middleware dependency** - Go backend integration requires special setup
- **Configuration files needed**: `github.properties`, `apikeys.properties`
- **Module boundaries** - Respect Clean Architecture separation
- **DI complexity** - Study `ComponentManager` before modifying DI setup

## Special Considerations

- **Local-first architecture** - Changes must work offline
- **Block-based editor** - Complex state management with real-time collaboration
- **Protocol Buffers** - Backend communication uses protobuf definitions
- **Incremental Compose adoption** - Maintain compatibility with existing Views