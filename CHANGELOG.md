# Changelog

All notable changes to the Grantly Android Permission SDK will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial development of Grantly SDK

## [1.0.0] - 2024-09-14

### Added
- **Core Permission Management**
  - Automatic manifest parsing for dangerous permissions
  - Runtime permission request handling for Android 6.0+
  - Support for both Activity and Fragment contexts
  - Lazy and eager permission request modes

- **Special Permission Support**
  - System overlay permissions (SYSTEM_ALERT_WINDOW)
  - Write settings permissions (WRITE_SETTINGS)
  - Install packages permissions (REQUEST_INSTALL_PACKAGES)
  - Manage external storage permissions (MANAGE_EXTERNAL_STORAGE)
  - Background location permissions (ACCESS_BACKGROUND_LOCATION)
  - Notification permissions for Android 13+ (POST_NOTIFICATIONS)

- **Customizable UI Components**
  - Default permission dialogs with Material Design
  - Customizable rationale dialogs
  - Custom dialog provider interface
  - Custom toast provider interface
  - Themeable UI components

- **Configuration System**
  - Global SDK configuration via GrantlyConfig
  - Per-request configuration overrides
  - Configurable denial behaviors
  - Default rationale messages and titles

- **Error Handling**
  - Comprehensive exception hierarchy
  - PermissionNotDeclaredException for undeclared permissions
  - PermissionRequestInProgressException for concurrent requests
  - InvalidConfigurationException for configuration issues
  - Detailed error messages and debugging information

- **Utility Functions**
  - Permission state checking utilities
  - Manifest permission parsing
  - App settings navigation
  - Permission display name and description mapping
  - Special permission detection

- **Testing Support**
  - Unit test framework integration
  - Mock-friendly architecture
  - Test utilities for permission scenarios

- **Documentation**
  - Comprehensive README with usage examples
  - Complete API documentation
  - ProGuard configuration guide
  - Contributing guidelines
  - Code of conduct

- **Build and Distribution**
  - AAR library packaging
  - ProGuard/R8 consumer rules
  - Maven publishing configuration
  - GitHub Packages support
  - Gradle build optimizations

### Technical Details
- **Minimum SDK**: API 23 (Android 6.0)
- **Target SDK**: API 34 (Android 14)
- **Compile SDK**: API 36
- **Java Version**: 11
- **Dependencies**: AndroidX AppCompat, Material Components

### Architecture
- **Package Structure**: `dev.grantly.px.*`
- **Core Classes**: Grantly, PermissionRequest, GrantlyConfig
- **Provider Interfaces**: DialogProvider, RationaleProvider, ToastProvider
- **Exception Hierarchy**: GrantlyException base class with specific subtypes
- **Utility Classes**: GrantlyUtils, GrantlyLogger

### Supported Permissions
- Camera (CAMERA)
- Microphone (RECORD_AUDIO)
- Location (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION)
- Storage (READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, MANAGE_EXTERNAL_STORAGE)
- Contacts (READ_CONTACTS, WRITE_CONTACTS)
- Phone (CALL_PHONE, READ_PHONE_STATE)
- SMS (SEND_SMS, READ_SMS)
- Calendar (READ_CALENDAR, WRITE_CALENDAR)
- Notifications (POST_NOTIFICATIONS)
- System overlay (SYSTEM_ALERT_WINDOW)
- Write settings (WRITE_SETTINGS)
- Install packages (REQUEST_INSTALL_PACKAGES)

### Known Issues
- None at initial release

### Migration Notes
- This is the initial release, no migration required

---

## Release Notes Format

### Types of Changes
- **Added** for new features
- **Changed** for changes in existing functionality
- **Deprecated** for soon-to-be removed features
- **Removed** for now removed features
- **Fixed** for any bug fixes
- **Security** for vulnerability fixes

### Version Numbering
- **MAJOR** version for incompatible API changes
- **MINOR** version for backward-compatible functionality additions
- **PATCH** version for backward-compatible bug fixes

### Links
- [Unreleased]: https://github.com/PrajyotxD/Grantly/compare/v1.0.0...HEAD
- [1.0.0]: https://github.com/PrajyotxD/Grantly/releases/tag/v1.0.0