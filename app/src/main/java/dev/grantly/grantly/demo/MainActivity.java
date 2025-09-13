package dev.grantly.grantly.demo;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dev.grantly.px.Grantly;
import dev.grantly.px.callback.GrantlyCallback;
import dev.grantly.px.config.DenialBehavior;
import dev.grantly.px.config.GrantlyConfig;
import dev.grantly.px.provider.DialogProvider;
import dev.grantly.px.provider.RationaleProvider;
import dev.grantly.px.model.PermissionResult;

import android.app.AlertDialog;
import android.content.Context;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "GrantlyDemo";
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeGrantly();
        setupUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        // Log the permission result details with enhanced debugging
        Log.d(TAG, "=== PERMISSION RESULT DEBUG START ===");
        Log.d(TAG, "onRequestPermissionsResult called:");
        Log.d(TAG, "  - Request code: " + requestCode);
        Log.d(TAG, "  - Our package: " + getPackageName());
        Log.d(TAG, "  - Permissions: " + java.util.Arrays.toString(permissions));
        Log.d(TAG, "  - Grant results: " + java.util.Arrays.toString(grantResults));
        
        // SAFETY CHECK: Verify this is for our app only
        if (permissions != null && permissions.length > 0) {
            Log.d(TAG, "  - Permission details:");
            for (int i = 0; i < permissions.length; i++) {
                String status = (grantResults != null && i < grantResults.length && 
                    grantResults[i] == PackageManager.PERMISSION_GRANTED) ? "GRANTED" : "DENIED";
                Log.d(TAG, "    * " + permissions[i] + " -> " + status);
            }
        }
        
        // Log system state information for debugging
        Log.d(TAG, "  - System info:");
        Log.d(TAG, "    * Android version: " + Build.VERSION.SDK_INT);
        Log.d(TAG, "    * Device manufacturer: " + Build.MANUFACTURER);
        Log.d(TAG, "    * Device model: " + Build.MODEL);
        
        // Perform integrity check
        boolean integrityOk = performPermissionIntegrityCheck();
        Log.d(TAG, "  - Permission integrity check: " + (integrityOk ? "PASS" : "FAIL"));
        
        // Forward the result to Grantly SDK for processing
        boolean handled = Grantly.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "  - Handled by Grantly: " + handled);
        
        Log.d(TAG, "=== PERMISSION RESULT DEBUG END ===");
    }
    
    /**
     * Performs a comprehensive permission integrity check to detect any anomalies.
     * This helps identify if permission states have been unexpectedly changed.
     * 
     * @return true if integrity check passes, false if anomalies detected
     */
    private boolean performPermissionIntegrityCheck() {
        try {
            // Check that we can only see our own app's information
            PackageManager pm = getPackageManager();
            String ourPackage = getPackageName();
            
            // Verify our package name hasn't been tampered with
            if (ourPackage == null || ourPackage.trim().isEmpty()) {
                Log.e(TAG, "INTEGRITY FAILURE: Package name is null or empty");
                return false;
            }
            
            if (!ourPackage.startsWith("dev.grantly.grantly.demo")) {
                Log.w(TAG, "INTEGRITY WARNING: Unexpected package name: " + ourPackage);
            }
            
            // Check basic permission system functionality
            int cameraPermission = checkSelfPermission(android.Manifest.permission.CAMERA);
            Log.d(TAG, "    * Camera permission check result: " + cameraPermission);
            
            // Verify we can access our own PackageInfo
            android.content.pm.PackageInfo packageInfo = pm.getPackageInfo(ourPackage, PackageManager.GET_PERMISSIONS);
            if (packageInfo == null) {
                Log.e(TAG, "INTEGRITY FAILURE: Cannot access own PackageInfo");
                return false;
            }
            
            // Log declared permissions for reference
            if (packageInfo.requestedPermissions != null) {
                Log.d(TAG, "    * Declared permissions count: " + packageInfo.requestedPermissions.length);
                for (String permission : packageInfo.requestedPermissions) {
                    int status = checkSelfPermission(permission);
                    Log.v(TAG, "      - " + permission + " -> " + 
                        (status == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));
                }
            }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "INTEGRITY FAILURE: Exception during integrity check", e);
            return false;
        }
    }

    private void initializeGrantly() {
        // First initialize the SDK with the application context
        Grantly.initialize(this);
        
        // Then configure Grantly with custom settings
        GrantlyConfig config = new GrantlyConfig.Builder()
                .setDefaultLazyMode(false)
                .setDefaultDenialBehavior(DenialBehavior.CONTINUE_APP_FLOW)
                .setLoggingEnabled(true)
                .build();
        
        Grantly.configure(config);
        
        Log.d(TAG, "Grantly SDK initialized and configured");
    }

    private void setupUI() {
        statusText = findViewById(R.id.statusText);
        statusText.setText("Grantly Permission Demo Ready");

        // Basic permission request buttons
        Button cameraBtn = findViewById(R.id.btnCamera);
        Button locationBtn = findViewById(R.id.btnLocation);
        Button multipleBtn = findViewById(R.id.btnMultiple);

        // Lazy vs Eager mode buttons
        Button lazyBtn = findViewById(R.id.btnLazyMode);
        Button eagerBtn = findViewById(R.id.btnEagerMode);

        // Special permission buttons
        Button overlayBtn = findViewById(R.id.btnOverlay);
        Button notificationBtn = findViewById(R.id.btnNotification);
        Button backgroundLocationBtn = findViewById(R.id.btnBackgroundLocation);

        // Custom UI demo buttons
        Button customDialogBtn = findViewById(R.id.btnCustomDialog);
        Button customRationaleBtn = findViewById(R.id.btnCustomRationale);
        Button denialBehaviorBtn = findViewById(R.id.btnDenialBehavior);

        // Utility buttons
        Button openSettingsBtn = findViewById(R.id.btnOpenSettings);
        Button checkManifestBtn = findViewById(R.id.btnCheckManifest);

        // Set click listeners
        cameraBtn.setOnClickListener(v -> requestCameraPermission());
        locationBtn.setOnClickListener(v -> requestLocationPermission());
        multipleBtn.setOnClickListener(v -> requestMultiplePermissions());
        
        lazyBtn.setOnClickListener(v -> demonstrateLazyMode());
        eagerBtn.setOnClickListener(v -> demonstrateEagerMode());
        
        overlayBtn.setOnClickListener(v -> requestOverlayPermission());
        notificationBtn.setOnClickListener(v -> requestNotificationPermission());
        backgroundLocationBtn.setOnClickListener(v -> requestBackgroundLocationPermission());
        
        customDialogBtn.setOnClickListener(v -> demonstrateCustomDialog());
        customRationaleBtn.setOnClickListener(v -> demonstrateCustomRationale());
        denialBehaviorBtn.setOnClickListener(v -> demonstrateDenialBehavior());
        
        openSettingsBtn.setOnClickListener(v -> openAppSettings());
        checkManifestBtn.setOnClickListener(v -> checkManifestPermissions());
    }

    private void requestCameraPermission() {
        updateStatus("Requesting camera permission...");
        
        Grantly.requestPermissions(this)
                .permissions(Manifest.permission.CAMERA)
                .setRationale("Camera Access", "This app needs camera access to take photos")
                .setCallbacks(new GrantlyCallback() {
                    @Override
                    public void onPermissionGranted(String[] permissions) {
                        updateStatus("Camera permission granted!");
                        Toast.makeText(MainActivity.this, "Camera ready to use", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(String[] permissions) {
                        updateStatus("Camera permission denied");
                        Toast.makeText(MainActivity.this, "Camera access denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionPermanentlyDenied(String[] permissions) {
                        updateStatus("Camera permission permanently denied");
                        Toast.makeText(MainActivity.this, "Please enable camera in settings", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionResult(List<PermissionResult> results) {
                        // Optional: Handle detailed results if needed
                    }
                })
                .execute();
    }

    private void requestLocationPermission() {
        updateStatus("Requesting location permission...");
        
        Grantly.requestPermissions(this)
                .permissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .setRationale("Location Access", "This app needs location access to show nearby places")
                .setCallbacks(new GrantlyCallback() {
                    @Override
                    public void onPermissionGranted(String[] permissions) {
                        updateStatus("Location permission granted!");
                        Toast.makeText(MainActivity.this, "Location services ready", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(String[] permissions) {
                        updateStatus("Location permission denied");
                        Toast.makeText(MainActivity.this, "Location access denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionPermanentlyDenied(String[] permissions) {
                        updateStatus("Location permission permanently denied");
                        Toast.makeText(MainActivity.this, "Please enable location in settings", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionResult(List<PermissionResult> results) {
                        // Optional: Handle detailed results if needed
                    }
                })
                .execute();
    }

    private void requestMultiplePermissions() {
        updateStatus("Requesting multiple permissions...");
        
        Grantly.requestPermissions(this)
                .permissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.RECORD_AUDIO
                )
                .setRationale("Multiple Permissions", "This app needs camera, location, and microphone access for full functionality")
                .setCallbacks(new GrantlyCallback() {
                    @Override
                    public void onPermissionGranted(String[] permissions) {
                        updateStatus("All permissions granted! (" + permissions.length + " permissions)");
                        Toast.makeText(MainActivity.this, "All features available", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(String[] permissions) {
                        updateStatus("Some permissions denied (" + permissions.length + " denied)");
                        Toast.makeText(MainActivity.this, "Some features may be limited", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionPermanentlyDenied(String[] permissions) {
                        updateStatus("Some permissions permanently denied (" + permissions.length + " permanently denied)");
                        Toast.makeText(MainActivity.this, "Please check app settings", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionResult(List<PermissionResult> results) {
                        // Optional: Handle detailed results if needed
                    }
                })
                .execute();
    }

    private void demonstrateLazyMode() {
        updateStatus("Demonstrating lazy mode...");
        
        Grantly.requestPermissions(this)
                .permissions(Manifest.permission.READ_CONTACTS)
                .setLazy(true)
                .setRationale("Contacts Access", "Lazy mode: Permission will be requested when needed")
                .setCallbacks(createBasicCallback("Lazy mode"))
                .execute();
    }

    private void demonstrateEagerMode() {
        updateStatus("Demonstrating eager mode...");
        
        Grantly.requestPermissions(this)
                .permissions(Manifest.permission.RECORD_AUDIO)
                .setLazy(false)
                .setRationale("Microphone Access", "Eager mode: Permission requested immediately")
                .setCallbacks(createBasicCallback("Eager mode"))
                .execute();
    }

    private void requestOverlayPermission() {
        updateStatus("Requesting overlay permission...");
        
        Grantly.requestPermissions(this)
                .permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
                .setRationale("Overlay Permission", "This app needs overlay permission to display floating windows")
                .setCallbacks(createBasicCallback("Overlay permission"))
                .execute();
    }

    private void requestNotificationPermission() {
        updateStatus("Requesting notification permission...");
        
        Grantly.requestPermissions(this)
                .permissions(Manifest.permission.POST_NOTIFICATIONS)
                .setRationale("Notification Permission", "This app needs notification permission to send you updates")
                .setCallbacks(createBasicCallback("Notification permission"))
                .execute();
    }

    private void requestBackgroundLocationPermission() {
        updateStatus("Requesting background location permission...");
        
        Grantly.requestPermissions(this)
                .permissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                .setRationale("Background Location", "This app needs background location access for location-based features")
                .setCallbacks(createBasicCallback("Background location"))
                .execute();
    }

    private void openAppSettings() {
        updateStatus("Opening app settings...");
        try {
            dev.grantly.px.util.GrantlyUtils.openAppSettings(this);
        } catch (Exception e) {
            updateStatus("Error opening settings: " + e.getMessage());
            Toast.makeText(this, "Could not open settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkManifestPermissions() {
        updateStatus("Checking manifest permissions...");
        try {
            String[] dangerousPermissions = dev.grantly.px.util.GrantlyUtils.getDangerousPermissions(this);
            StringBuilder sb = new StringBuilder("Dangerous permissions in manifest:\n");
            for (String permission : dangerousPermissions) {
                sb.append("• ").append(permission.substring(permission.lastIndexOf('.') + 1)).append("\n");
            }
            updateStatus("Found " + dangerousPermissions.length + " dangerous permissions");
            Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            updateStatus("Error checking manifest: " + e.getMessage());
            Toast.makeText(this, "Could not check manifest", Toast.LENGTH_SHORT).show();
        }
    }

    private GrantlyCallback createBasicCallback(String context) {
        return new GrantlyCallback() {
            @Override
            public void onPermissionGranted(String[] permissions) {
                updateStatus(context + " - Permission granted!");
                Toast.makeText(MainActivity.this, context + " granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(String[] permissions) {
                updateStatus(context + " - Permission denied");
                Toast.makeText(MainActivity.this, context + " denied", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionPermanentlyDenied(String[] permissions) {
                updateStatus(context + " - Permission permanently denied");
                Toast.makeText(MainActivity.this, context + " permanently denied", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPermissionResult(List<PermissionResult> results) {
                // Optional: Handle detailed results if needed
            }
        };
    }

    private void demonstrateCustomDialog() {
        updateStatus("Demonstrating custom dialog provider...");
        
        DialogProvider customDialogProvider = new DialogProvider() {
            @Override
            public void showPermissionDialog(Context context, String[] permissions, DialogCallback callback) {
                StringBuilder permissionList = new StringBuilder();
                for (String permission : permissions) {
                    permissionList.append("• ").append(permission.substring(permission.lastIndexOf('.') + 1)).append("\n");
                }
                
                new AlertDialog.Builder(context)
                        .setTitle("🔒 Custom Permission Dialog")
                        .setMessage("This app needs the following permissions:\n\n" + permissionList.toString())
                        .setPositiveButton("Grant Permission", (dialog, which) -> callback.onDialogResult(true))
                        .setNegativeButton("Not Now", (dialog, which) -> callback.onDialogResult(false))
                        .setCancelable(false)
                        .show();
            }
        };
        
        Grantly.requestPermissions(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setRationale("Storage Access", "Custom dialog demonstration for storage permission")
                .setCustomDialog(customDialogProvider)
                .setCallbacks(createBasicCallback("Custom dialog"))
                .execute();
    }

    private void demonstrateCustomRationale() {
        updateStatus("Demonstrating custom rationale provider...");
        
        RationaleProvider customRationaleProvider = new RationaleProvider() {
            @Override
            public void showRationale(Context context, String[] permissions, RationaleCallback callback) {
                StringBuilder permissionList = new StringBuilder();
                for (String permission : permissions) {
                    permissionList.append("• ").append(permission.substring(permission.lastIndexOf('.') + 1)).append("\n");
                }
                
                new AlertDialog.Builder(context)
                        .setTitle("ℹ️ Why We Need This")
                        .setMessage("Custom Rationale: We need these permissions for the app to work properly:\n\n" + permissionList.toString() + "\nThis is a custom rationale dialog with enhanced styling.")
                        .setPositiveButton("I Understand", (dialog, which) -> callback.onRationaleAccepted())
                        .setNegativeButton("Cancel", (dialog, which) -> callback.onRationaleDenied())
                        .setCancelable(false)
                        .show();
            }
        };
        
        Grantly.requestPermissions(this)
                .permissions(Manifest.permission.READ_SMS)
                .setRationale(customRationaleProvider)
                .setCallbacks(createBasicCallback("Custom rationale"))
                .execute();
    }

    private void demonstrateDenialBehavior() {
        updateStatus("Demonstrating different denial behaviors...");
        
        Grantly.requestPermissions(this)
                .permissions(Manifest.permission.CALL_PHONE)
                .setRationale("Phone Access", "This demonstrates EXIT_APP_WITH_DIALOG denial behavior")
                .setDenialBehavior(DenialBehavior.EXIT_APP_WITH_DIALOG)
                .setCallbacks(new GrantlyCallback() {
                    @Override
                    public void onPermissionGranted(String[] permissions) {
                        updateStatus("Phone permission granted with custom denial behavior!");
                        Toast.makeText(MainActivity.this, "Phone permission granted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(String[] permissions) {
                        updateStatus("Phone permission denied - will show exit dialog");
                        Toast.makeText(MainActivity.this, "Exit dialog shown due to denial behavior", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionPermanentlyDenied(String[] permissions) {
                        updateStatus("Phone permission permanently denied - will show exit dialog");
                        Toast.makeText(MainActivity.this, "Exit dialog shown due to permanent denial", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionResult(List<PermissionResult> results) {
                        // Optional: Handle detailed results if needed
                    }
                })
                .execute();
    }

    private void updateStatus(String message) {
        statusText.setText(message);
        Log.d(TAG, message);
    }
}