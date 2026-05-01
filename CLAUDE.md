# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SIPConnect is an Android application (Kotlin + Jetpack Compose) that integrates with a SIPConnectServer to handle SIP device registration, incoming call notifications via FCM, and SMS messaging. It can act as the device's default SMS manager, bridging GSM SMS and server-routed messages.

## Build Commands

```powershell
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug build to connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single test class
./gradlew test --tests "com.example.sipconnect.ExampleUnitTest"
```

## Architecture

The app uses a standard Android layered architecture with Jetpack Compose for UI.

### Presentation Layer
- `MainActivity.kt` — Single-activity host with bottom nav (Call History, Inbox, Settings)
- `SettingsScreen.kt` — Server IP, SIP credentials (user1/user2), and AppMode configuration
- `CallHistoryScreen.kt` — Paginated call log display
- `InboxScreen.kt` — SMS conversation list (paged)
- `IncomingCallActivity.kt` — Full-screen incoming call UI launched by FCM notification
- `ComposeSmsActivity.kt` / `NewMessageScreen.kt` — SMS composition

### Data Layer
- `AppDatabase.kt` — Room database (current version 5) with three entities: `CallLog`, `SmsLog`, `MessageEntity`
- `SmsRepository.kt` — Unified SMS business logic; reads/writes both system SMS database and Room DB
- `SharedPreferences.kt` — App-wide config: server IP, port, SIP credentials, `AppMode`

### Network Layer
- `RetrofitClient.kt` — Retrofit 3.0 HTTP client (configurable base URL from settings)
- `ApiService.kt` — REST endpoints: register device, send SMS alert to server, send GSM SMS, SIP restart, get FCM token
- `RESTData.kt` — Request/response data models

### Firebase & Notifications
- `MyFirebaseService.kt` — FCM message handler; routes payloads to call/SMS handlers, stores messages in Room DB, and resolves contact names

### SMS Module
- `SmsReceiver.kt` / `MmsReceiver.kt` — Broadcast receivers for incoming SMS/MMS
- `SmsSender.kt` / `DefaultSmsHelper.kt` — Outgoing SMS and default app management
- `HeadlessSmsSendService.kt` — Required service for default SMS app

### Utilities
- `ContactHelper.kt` — Phone number normalization (libphonenumber) and contact name lookup
- `PermissionsHelper.kt` — Runtime permission handling

## Key Concepts

**AppMode** — Controls app behavior; set in Settings:
- `NORMAL` — Basic call/SMS notifications only
- `CLIENT` — Registers with SIPConnectServer, receives server-routed notifications
- `SERVER` — Acts as SMS gateway, forwards received GSM SMS to server
- `SMS_MANAGER` — Full default SMS app with conversation UI

**Dual SIP user support** — Settings hold credentials for `user1` and `user2`; both can be registered with the server simultaneously.

**Hybrid SMS storage** — Incoming messages are stored in both the system SMS database (when set as default SMS app) and the local Room DB (`MessageEntity`). `SmsRepository` abstracts which source to read from.

**Version code** — Derived from `git rev-list --count HEAD` at build time (see `app/build.gradle.kts`).

## Database Migrations

Room migrations live in `AppDatabase.kt`. Current schema version is **5**. When adding new columns or tables, add a `Migration` object and register it in `Room.databaseBuilder(…).addMigrations(…)`.

## Dependencies

Managed via version catalog at `gradle/libs.versions.toml`. Key libraries:
- Compose BOM + Material3
- Firebase Messaging
- Retrofit 3.0 + Gson
- Room 2.7.2 (KSP for annotation processing)
- Paging 3.3.6 (call history and inbox lists)
- libphonenumber (phone number formatting/normalization)
