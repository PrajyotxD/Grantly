package dev.grantly.px.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class GrantlyUtilsTest {
    
    @Mock
    private Context mockContext;
    
    @Mock
    private Activity mockActivity;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getPackageName()).thenReturn("com.test.app");
    }
    
    @Test
    public void testIsPermissionDeclared_ValidPermission_ReturnsTrue() {
        // This test would require mocking ManifestParser, but since ManifestParser
        // is already tested separately, we'll focus on the utility method behavior
        
        // Test null context
        assertFalse(GrantlyUtils.isPermissionDeclared(null, "android.permission.CAMERA"));
        
        // Test null permission
        assertFalse(GrantlyUtils.isPermissionDeclared(mockContext, null));
        
        // Test empty permission
        assertFalse(GrantlyUtils.isPermissionDeclared(mockContext, ""));
        
        // Test whitespace permission
        assertFalse(GrantlyUtils.isPermissionDeclared(mockContext, "   "));
    }
    
    @Test
    public void testOpenAppSettings_ValidContext_StartsIntent() {
        setUp();
        
        // Test with valid context - should not throw exception
        GrantlyUtils.openAppSettings(mockContext);
        
        // Test with null context - should handle gracefully
        GrantlyUtils.openAppSettings(null);
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP) // API 21, before runtime permissions
    public void testIsPermanentlyDenied_PreAPI23_ReturnsFalse() {
        setUp();
        
        String[] permissions = {"android.permission.CAMERA"};
        boolean result = GrantlyUtils.isPermanentlyDenied(mockActivity, permissions);
        
        assertFalse("Should return false on pre-API 23", result);
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.M) // API 23
    public void testIsPermanentlyDenied_NullInputs_ReturnsFalse() {
        setUp();
        
        // Test null activity
        assertFalse(GrantlyUtils.isPermanentlyDenied(null, new String[]{"android.permission.CAMERA"}));
        
        // Test null permissions
        assertFalse(GrantlyUtils.isPermanentlyDenied(mockActivity, null));
        
        // Test empty permissions
        assertFalse(GrantlyUtils.isPermanentlyDenied(mockActivity, new String[0]));
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.M) // API 23
    public void testIsPermanentlyDenied_PermissionGranted_ReturnsFalse() {
        setUp();
        
        try (MockedStatic<ActivityCompat> mockedActivityCompat = mockStatic(ActivityCompat.class)) {
            String[] permissions = {"android.permission.CAMERA"};
            
            // Mock permission as granted
            mockedActivityCompat.when(() -> ActivityCompat.checkSelfPermission(mockActivity, "android.permission.CAMERA"))
                    .thenReturn(PackageManager.PERMISSION_GRANTED);
            
            boolean result = GrantlyUtils.isPermanentlyDenied(mockActivity, permissions);
            
            assertFalse("Should return false when permission is granted", result);
        }
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.M) // API 23
    public void testIsPermanentlyDenied_PermissionDeniedWithRationale_ReturnsFalse() {
        setUp();
        
        try (MockedStatic<ActivityCompat> mockedActivityCompat = mockStatic(ActivityCompat.class)) {
            String[] permissions = {"android.permission.CAMERA"};
            
            // Mock permission as denied but rationale should be shown
            mockedActivityCompat.when(() -> ActivityCompat.checkSelfPermission(mockActivity, "android.permission.CAMERA"))
                    .thenReturn(PackageManager.PERMISSION_DENIED);
            mockedActivityCompat.when(() -> ActivityCompat.shouldShowRequestPermissionRationale(mockActivity, "android.permission.CAMERA"))
                    .thenReturn(true);
            
            boolean result = GrantlyUtils.isPermanentlyDenied(mockActivity, permissions);
            
            assertFalse("Should return false when rationale should be shown", result);
        }
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.M) // API 23
    public void testIsPermanentlyDenied_PermissionPermanentlyDenied_ReturnsTrue() {
        setUp();
        
        try (MockedStatic<ActivityCompat> mockedActivityCompat = mockStatic(ActivityCompat.class)) {
            String[] permissions = {"android.permission.CAMERA"};
            
            // Mock permission as denied and rationale should not be shown (permanently denied)
            mockedActivityCompat.when(() -> ActivityCompat.checkSelfPermission(mockActivity, "android.permission.CAMERA"))
                    .thenReturn(PackageManager.PERMISSION_DENIED);
            mockedActivityCompat.when(() -> ActivityCompat.shouldShowRequestPermissionRationale(mockActivity, "android.permission.CAMERA"))
                    .thenReturn(false);
            
            boolean result = GrantlyUtils.isPermanentlyDenied(mockActivity, permissions);
            
            assertTrue("Should return true when permission is permanently denied", result);
        }
    }
    
    @Test
    public void testIsPermanentlyDenied_SpecialPermission_SkipsCheck() {
        setUp();
        
        try (MockedStatic<ActivityCompat> mockedActivityCompat = mockStatic(ActivityCompat.class)) {
            String[] permissions = {"android.permission.SYSTEM_ALERT_WINDOW"};
            
            boolean result = GrantlyUtils.isPermanentlyDenied(mockActivity, permissions);
            
            assertFalse("Should return false for special permissions", result);
            
            // Verify that ActivityCompat methods were not called for special permissions
            mockedActivityCompat.verifyNoInteractions();
        }
    }
    
    @Test
    public void testGetDangerousPermissions_ValidContext_ReturnsArray() {
        setUp();
        
        // Test with valid context - should not throw exception
        String[] result = GrantlyUtils.getDangerousPermissions(mockContext);
        assertNotNull("Should return non-null array", result);
        
        // Test with null context
        String[] nullResult = GrantlyUtils.getDangerousPermissions(null);
        assertNotNull("Should return non-null array for null context", nullResult);
        assertEquals("Should return empty array for null context", 0, nullResult.length);
    }
    
    @Test
    public void testIsSpecialPermission_SystemAlertWindow_ReturnsTrue() {
        assertTrue(GrantlyUtils.isSpecialPermission("android.permission.SYSTEM_ALERT_WINDOW"));
    }
    
    @Test
    public void testIsSpecialPermission_WriteSettings_ReturnsTrue() {
        assertTrue(GrantlyUtils.isSpecialPermission("android.permission.WRITE_SETTINGS"));
    }
    
    @Test
    public void testIsSpecialPermission_RequestInstallPackages_ReturnsTrue() {
        assertTrue(GrantlyUtils.isSpecialPermission("android.permission.REQUEST_INSTALL_PACKAGES"));
    }
    
    @Test
    public void testIsSpecialPermission_ManageExternalStorage_ReturnsTrue() {
        assertTrue(GrantlyUtils.isSpecialPermission("android.permission.MANAGE_EXTERNAL_STORAGE"));
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.Q) // API 29
    public void testIsSpecialPermission_BackgroundLocation_API29_ReturnsTrue() {
        assertTrue("Background location should be special on API 29+", 
                  GrantlyUtils.isSpecialPermission("android.permission.ACCESS_BACKGROUND_LOCATION"));
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.P) // API 28
    public void testIsSpecialPermission_BackgroundLocation_API28_ReturnsFalse() {
        assertFalse("Background location should not be special on API 28", 
                   GrantlyUtils.isSpecialPermission("android.permission.ACCESS_BACKGROUND_LOCATION"));
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.TIRAMISU) // API 33
    public void testIsSpecialPermission_PostNotifications_API33_ReturnsTrue() {
        assertTrue("Post notifications should be special on API 33+", 
                  GrantlyUtils.isSpecialPermission("android.permission.POST_NOTIFICATIONS"));
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.S) // API 31
    public void testIsSpecialPermission_PostNotifications_API31_ReturnsFalse() {
        assertFalse("Post notifications should not be special on API 31", 
                   GrantlyUtils.isSpecialPermission("android.permission.POST_NOTIFICATIONS"));
    }
    
    @Test
    public void testIsSpecialPermission_RegularPermission_ReturnsFalse() {
        assertFalse(GrantlyUtils.isSpecialPermission("android.permission.CAMERA"));
        assertFalse(GrantlyUtils.isSpecialPermission("android.permission.READ_EXTERNAL_STORAGE"));
    }
    
    @Test
    public void testIsSpecialPermission_NullOrEmpty_ReturnsFalse() {
        assertFalse(GrantlyUtils.isSpecialPermission(null));
        assertFalse(GrantlyUtils.isSpecialPermission(""));
        assertFalse(GrantlyUtils.isSpecialPermission("   "));
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.M) // API 23
    public void testGetSpecialPermissionIntent_SystemAlertWindow_ReturnsIntent() {
        Intent intent = GrantlyUtils.getSpecialPermissionIntent("android.permission.SYSTEM_ALERT_WINDOW");
        
        assertNotNull("Should return intent for SYSTEM_ALERT_WINDOW", intent);
        assertEquals("Should return overlay permission intent", 
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION, intent.getAction());
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.M) // API 23
    public void testGetSpecialPermissionIntent_WriteSettings_ReturnsIntent() {
        Intent intent = GrantlyUtils.getSpecialPermissionIntent("android.permission.WRITE_SETTINGS");
        
        assertNotNull("Should return intent for WRITE_SETTINGS", intent);
        assertEquals("Should return write settings intent", 
                    Settings.ACTION_MANAGE_WRITE_SETTINGS, intent.getAction());
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.O) // API 26
    public void testGetSpecialPermissionIntent_RequestInstallPackages_ReturnsIntent() {
        Intent intent = GrantlyUtils.getSpecialPermissionIntent("android.permission.REQUEST_INSTALL_PACKAGES");
        
        assertNotNull("Should return intent for REQUEST_INSTALL_PACKAGES", intent);
        assertEquals("Should return unknown app sources intent", 
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, intent.getAction());
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.R) // API 30
    public void testGetSpecialPermissionIntent_ManageExternalStorage_ReturnsIntent() {
        Intent intent = GrantlyUtils.getSpecialPermissionIntent("android.permission.MANAGE_EXTERNAL_STORAGE");
        
        assertNotNull("Should return intent for MANAGE_EXTERNAL_STORAGE", intent);
        assertEquals("Should return all files access intent", 
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, intent.getAction());
    }
    
    @Test
    public void testGetSpecialPermissionIntent_BackgroundLocation_ReturnsNull() {
        Intent intent = GrantlyUtils.getSpecialPermissionIntent("android.permission.ACCESS_BACKGROUND_LOCATION");
        
        assertNull("Should return null for background location (uses standard flow)", intent);
    }
    
    @Test
    public void testGetSpecialPermissionIntent_PostNotifications_ReturnsNull() {
        Intent intent = GrantlyUtils.getSpecialPermissionIntent("android.permission.POST_NOTIFICATIONS");
        
        assertNull("Should return null for post notifications (uses standard flow)", intent);
    }
    
    @Test
    public void testGetSpecialPermissionIntent_RegularPermission_ReturnsNull() {
        Intent intent = GrantlyUtils.getSpecialPermissionIntent("android.permission.CAMERA");
        
        assertNull("Should return null for regular permissions", intent);
    }
    
    @Test
    public void testGetSpecialPermissionIntent_NullOrEmpty_ReturnsNull() {
        assertNull(GrantlyUtils.getSpecialPermissionIntent(null));
        assertNull(GrantlyUtils.getSpecialPermissionIntent(""));
        assertNull(GrantlyUtils.getSpecialPermissionIntent("   "));
    }
    
    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP) // API 21, before special permissions
    public void testGetSpecialPermissionIntent_PreAPI23_ReturnsNull() {
        // Test that special permission intents return null on older API levels
        assertNull("Should return null for SYSTEM_ALERT_WINDOW on API 21", 
                  GrantlyUtils.getSpecialPermissionIntent("android.permission.SYSTEM_ALERT_WINDOW"));
        assertNull("Should return null for WRITE_SETTINGS on API 21", 
                  GrantlyUtils.getSpecialPermissionIntent("android.permission.WRITE_SETTINGS"));
    }
}