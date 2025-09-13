# Grantly - Android Permission Management SDK

[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Grantly is a comprehensive Android Permission Management SDK that simplifies runtime permission handling across different Android versions. It provides automatic manifest parsing, customizable UI components, robust error handling, and support for special permissions.

## Features

- 🚀 **Automatic Manifest Parsing** - Automatically detects dangerous permissions from your AndroidManifest.xml
- 🎨 **Customizable UI** - Fully customizable dialogs, toasts, and rationale screens
- 🔧 **Flexible Configuration** - Support for lazy/eager requests and configurable denial behaviors
- 📱 **Special Permissions** - Built-in support for overlay, settings, location, and notification permissions
- 🛡️ **Robust Error Handling** - Comprehensive exception handling with meaningful error messages
- 📚 **Extensive Documentation** - Complete Javadoc and usage examples
- 🧪 **Thoroughly Tested** - Unit and integration tests across multiple Android versions

## Installation

### Gradle Setup

Add the following to your app-level `build.gradle` file:

```gradle
dependencies {
    implementation 'dev.grantly:grantly:1.0.0'
}
```

### Maven Setup

```xml
<dependency>
    <groupId>dev.grantly</groupId>
    <artifactId>grantly</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### Basic Permission Request

```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Simple permission request
        Grantly.requestPermissions(this)
            .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .setCallbacks(new GrantlyCallback() {
                @Override
                public void onPermissionGranted(String[] permissions) {
                    // Permissions granted - proceed with camera functionality
                    startCamera();
                }
                
                @Override
                public void onPermissionDenied(String[] permissions) {
                    // Handle denied permissions
                    showFeatureUnavailableMessage();
                }
                
                @Override
                public void onPermissionPermanentlyDenied(String[] permissions) {
                    // Guide user to app settings
                    showSettingsDialog();
                }
            })
            .execute();
    }
}
```

### Lazy Permission Requests

```java
// Request permissions only when needed
private void takePicture() {
    Grantly.requestPermissions(this)
        .permissions(Manifest.permission.CAMERA)
        .setLazy(true)
        .setRationale("Camera Access", "We need camera permission to take photos")
        .setCallbacks(new GrantlyCallback() {
            @Override
            public void onPermissionGranted(String[] permissions) {
                // Camera permission granted - take picture
                capturePhoto();
            }
            
            @Override
            public void onPermissionDenied(String[] permissions) {
                Toast.makeText(MainActivity.this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        })
        .execute();
}
```

## Advanced Usage

### Global Configuration

Configure default behaviors for your entire app:

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        GrantlyConfig config = new GrantlyConfig.Builder()
            .setDefaultLazyMode(true)
            .setDefaultDenialBehavior(DenialBehavior.CONTINUE_APP_FLOW)
            .setLoggingEnabled(BuildConfig.DEBUG)
            .setDefaultDialogTheme(R.style.MyCustomDialogTheme)
            .build();
            
        Grantly.configure(config);
    }
}
```

### Custom Rationale Dialog

```java
Grantly.requestPermissions(this)
    .permissions(Manifest.permission.ACCESS_FINE_LOCATION)
    .setRationale(new RationaleProvider() {
        @Override
        public void showRationale(Context context, String[] permissions, RationaleCallback callback) {
            new AlertDialog.Builder(context)
                .setTitle("Location Permission")
                .setMessage("We need location access to show nearby restaurants")
                .setPositiveButton("Allow", (dialog, which) -> callback.onRationaleAccepted())
                .setNegativeButton("Deny", (dialog, which) -> callback.onRationaleDenied())
                .show();
        }
    })
    .execute();
```

### Custom Dialog Provider

```java
Grantly.requestPermissions(this)
    .permissions(Manifest.permission.READ_CONTACTS)
    .setCustomDialog(new DialogProvider() {
        @Override
        public void showPermissionDialog(Context context, String[] permissions, DialogCallback callback) {
            // Show your custom permission dialog
            MyCustomDialog dialog = new MyCustomDialog(context);
            dialog.setOnResultListener(granted -> callback.onDialogResult(granted));
            dialog.show();
        }
    })
    .execute();
```

### Special Permissions

#### Overlay Permission (SYSTEM_ALERT_WINDOW)

```java
Grantly.requestPermissions(this)
    .permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
    .setCallbacks(new GrantlyCallback() {
        @Override
        public void onPermissionGranted(String[] permissions) {
            // Overlay permission granted
            showOverlayView();
        }
        
        @Override
        public void onPermissionDenied(String[] permissions) {
            // User denied overlay permission
            showOverlayUnavailableMessage();
        }
    })
    .execute();
```

#### Background Location (Android 10+)

```java
// First request foreground location, then background
Grantly.requestPermissions(this)
    .permissions(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    .setCallbacks(new GrantlyCallback() {
        @Override
        public void onPermissionGranted(String[] permissions) {
            // Now request background location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestBackgroundLocation();
            }
        }
    })
    .execute();

private void requestBackgroundLocation() {
    Grantly.requestPermissions(this)
        .permissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        .setRationale("Background Location", 
            "Allow background location to track your runs even when the app is closed")
        .execute();
}
```

#### Notification Permission (Android 13+)

```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Grantly.requestPermissions(this)
        .permissions(Manifest.permission.POST_NOTIFICATIONS)
        .setCallbacks(new GrantlyCallback() {
            @Override
            public void onPermissionGranted(String[] permissions) {
                // Notification permission granted
                scheduleNotifications();
            }
        })
        .execute();
}
```

## Customization

### Theming

Create custom styles for dialogs and toasts:

```xml
<!-- res/values/styles.xml -->
<style name="GrantlyDialogTheme" parent="Theme.AppCompat.Light.Dialog">
    <item name="colorPrimary">@color/your_primary_color</item>
    <item name="colorAccent">@color/your_accent_color</item>
</style>

<style name="GrantlyToastTheme">
    <item name="android:background">@drawable/custom_toast_background</item>
    <item name="android:textColor">@color/toast_text_color</item>
</style>
```

### Custom Layouts

Override default layouts by creating files with the same names:

```xml
<!-- res/layout/grantly_dialog_rationale.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="24dp">
    
    <TextView
        android:id="@+id/grantly_rationale_title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textSize="18sp"
        android:textStyle="bold" />
    
    <TextView
        android:id="@+id/grantly_rationale_message"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:textSize="14sp" />
    
    <!-- Add your custom views here -->
    
</LinearLayout>
```

### Denial Behaviors

Configure how the app should behave when permissions are denied:

```java
// Continue app flow (default)
.setDenialBehavior(DenialBehavior.CONTINUE_APP_FLOW)

// Disable specific features
.setDenialBehavior(DenialBehavior.DISABLE_FEATURE)

// Exit app with dialog
.setDenialBehavior(DenialBehavior.EXIT_APP_WITH_DIALOG)

// Exit app immediately
.setDenialBehavior(DenialBehavior.EXIT_APP_IMMEDIATELY)
```

## Utility Methods

Grantly provides helpful utility methods:

```java
// Check if permission is declared in manifest
boolean isDeclared = GrantlyUtils.isPermissionDeclared(context, Manifest.permission.CAMERA);

// Open app settings
GrantlyUtils.openAppSettings(context);

// Check if permissions are permanently denied
boolean isPermanent = GrantlyUtils.isPermanentlyDenied(activity, permissions);

// Get all dangerous permissions from manifest
String[] dangerous = GrantlyUtils.getDangerousPermissions(context);

// Check if permission requires special handling
boolean isSpecial = GrantlyUtils.isSpecialPermission(Manifest.permission.SYSTEM_ALERT_WINDOW);
```

## Error Handling

Grantly provides comprehensive error handling:

```java
try {
    Grantly.requestPermissions(this)
        .permissions("invalid.permission")
        .execute();
} catch (PermissionNotDeclaredException e) {
    // Permission not declared in manifest
    Log.e("Grantly", "Undeclared permissions: " + Arrays.toString(e.getUndeclaredPermissions()));
} catch (InvalidConfigurationException e) {
    // Invalid configuration
    Log.e("Grantly", "Configuration issue: " + e.getConfigurationIssue());
} catch (PermissionRequestInProgressException e) {
    // Another request is already in progress
    Log.e("Grantly", "Request already in progress: " + e.getActiveRequestId());
}
```

## ProGuard Configuration

Add these rules to your `proguard-rules.pro`:

```proguard
# Grantly SDK
-keep class dev.grantly.px.** { *; }
-keep interface dev.grantly.px.** { *; }

# Keep callback interfaces
-keep class * implements dev.grantly.px.callback.GrantlyCallback { *; }
-keep class * implements dev.grantly.px.provider.** { *; }

# Keep model classes
-keep class dev.grantly.px.model.** { *; }
-keep class dev.grantly.px.config.** { *; }

# Keep exception classes
-keep class dev.grantly.px.exception.** { *; }
```

## Testing

### Unit Testing

Grantly is designed to be testable. Mock the Android framework components:

```java
@RunWith(MockitoJUnitRunner.class)
public class PermissionTest {
    @Mock
    private Activity mockActivity;
    
    @Mock
    private PackageManager mockPackageManager;
    
    @Test
    public void testPermissionGranted() {
        // Mock permission as granted
        when(mockActivity.checkSelfPermission(Manifest.permission.CAMERA))
            .thenReturn(PackageManager.PERMISSION_GRANTED);
            
        // Test your permission logic
    }
}
```

### Integration Testing

Use Grantly's test utilities for integration tests:

```java
@RunWith(AndroidJUnit4.class)
public class GrantlyIntegrationTest {
    @Rule
    public ActivityTestRule<TestActivity> activityRule = 
        new ActivityTestRule<>(TestActivity.class);
    
    @Test
    public void testPermissionFlow() {
        // Test complete permission flow
        onView(withId(R.id.request_permission_button)).perform(click());
        
        // Verify rationale dialog appears
        onView(withText("Camera Permission")).check(matches(isDisplayed()));
    }
}
```

## Migration Guide

### From Other Permission Libraries

If you're migrating from other permission libraries:

#### From Dexter
```java
// Old Dexter code
Dexter.withActivity(this)
    .withPermission(Manifest.permission.CAMERA)
    .withListener(new PermissionListener() {
        @Override
        public void onPermissionGranted(PermissionGrantedResponse response) {
            // Permission granted
        }
    })
    .check();

// New Grantly code
Grantly.requestPermissions(this)
    .permissions(Manifest.permission.CAMERA)
    .setCallbacks(new GrantlyCallback() {
        @Override
        public void onPermissionGranted(String[] permissions) {
            // Permission granted
        }
    })
    .execute();
```

#### From PermissionsDispatcher
```java
// Old PermissionsDispatcher (annotation-based)
@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    @NeedsPermission(Manifest.permission.CAMERA)
    void showCamera() {
        // Camera logic
    }
}

// New Grantly code (programmatic)
public class MainActivity extends AppCompatActivity {
    private void showCamera() {
        Grantly.requestPermissions(this)
            .permissions(Manifest.permission.CAMERA)
            .setLazy(true)
            .setCallbacks(new GrantlyCallback() {
                @Override
                public void onPermissionGranted(String[] permissions) {
                    // Camera logic
                }
            })
            .execute();
    }
}
```

## Best Practices

### 1. Request Permissions Contextually
Always request permissions when the user is about to use a feature that requires them:

```java
// Good: Request when user taps "Take Photo"
private void onTakePhotoClicked() {
    Grantly.requestPermissions(this)
        .permissions(Manifest.permission.CAMERA)
        .setLazy(true)
        .execute();
}

// Avoid: Requesting all permissions upfront
```

### 2. Provide Clear Rationales
Explain why you need each permission:

```java
Grantly.requestPermissions(this)
    .permissions(Manifest.permission.ACCESS_FINE_LOCATION)
    .setRationale("Location Access", 
        "We need your location to show nearby restaurants and delivery options")
    .execute();
```

### 3. Handle All Permission States
Always implement all callback methods:

```java
.setCallbacks(new GrantlyCallback() {
    @Override
    public void onPermissionGranted(String[] permissions) {
        // Enable feature
    }
    
    @Override
    public void onPermissionDenied(String[] permissions) {
        // Disable feature gracefully
    }
    
    @Override
    public void onPermissionPermanentlyDenied(String[] permissions) {
        // Guide to settings
        GrantlyUtils.openAppSettings(MainActivity.this);
    }
})
```

### 4. Use Global Configuration
Set up global defaults in your Application class:

```java
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        GrantlyConfig config = new GrantlyConfig.Builder()
            .setDefaultLazyMode(true)
            .setLoggingEnabled(BuildConfig.DEBUG)
            .build();
            
        Grantly.configure(config);
    }
}
```



## Changelog

### Version 1.0.0
- Initial release
- Automatic manifest parsing
- Customizable UI components
- Special permission support
- Comprehensive error handling
- Full Android 6.0+ support

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup

1. Fork the repository
2. Clone your fork
3. Open in Android Studio
4. Make your changes
5. Submit a pull request

## License

```
Copyright 2025 Grantly

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Support

- 🐛 Issues: [GitHub Issues](https://github.com/PrajyotxD/Grantly/issues)
- 📖 Documentation: [Wiki](https://github.com/PrajyotxD/Grantly/wiki)
- 💬 Discussions: [GitHub Discussions](https://github.com/PrajyotxD/Grantly/discussions)
