package dev.grantly.px.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Build;

import dev.grantly.px.exception.PermissionNotDeclaredException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class ManifestParserTest {
    
    @Mock
    private Context mockContext;
    
    @Mock
    private PackageManager mockPackageManager;
    
    private ManifestParser manifestParser;
    private static final String TEST_PACKAGE_NAME = "com.test.app";
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        manifestParser = new ManifestParser();
        
        when(mockContext.getPackageName()).thenReturn(TEST_PACKAGE_NAME);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
    }
    
    @Test
    public void testGetDeclaredPermissions_ReturnsCorrectPermissions() throws Exception {
        // Arrange
        String[] expectedPermissions = {
            "android.permission.CAMERA",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.INTERNET"
        };
        
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.requestedPermissions = expectedPermissions;
        
        when(mockPackageManager.getPackageInfo(eq(TEST_PACKAGE_NAME), eq(PackageManager.GET_PERMISSIONS)))
            .thenReturn(packageInfo);
        
        // Act
        Set<String> result = manifestParser.getDeclaredPermissions(mockContext);
        
        // Assert
        assertEquals(3, result.size());
        assertTrue(result.contains("android.permission.CAMERA"));
        assertTrue(result.contains("android.permission.READ_EXTERNAL_STORAGE"));
        assertTrue(result.contains("android.permission.INTERNET"));
    }
    
    @Test
    public void testGetDeclaredPermissions_EmptyManifest_ReturnsEmptySet() throws Exception {
        // Arrange
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.requestedPermissions = null;
        
        when(mockPackageManager.getPackageInfo(eq(TEST_PACKAGE_NAME), eq(PackageManager.GET_PERMISSIONS)))
            .thenReturn(packageInfo);
        
        // Act
        Set<String> result = manifestParser.getDeclaredPermissions(mockContext);
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test(expected = SecurityException.class)
    public void testGetDeclaredPermissions_PackageNotFound_ThrowsSecurityException() throws Exception {
        // Arrange
        when(mockPackageManager.getPackageInfo(eq(TEST_PACKAGE_NAME), eq(PackageManager.GET_PERMISSIONS)))
            .thenThrow(new PackageManager.NameNotFoundException());
        
        // Act
        manifestParser.getDeclaredPermissions(mockContext);
    }
    
    @Test
    public void testGetDeclaredPermissions_UsesCaching() throws Exception {
        // Arrange
        String[] permissions = {"android.permission.CAMERA"};
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.requestedPermissions = permissions;
        
        when(mockPackageManager.getPackageInfo(eq(TEST_PACKAGE_NAME), eq(PackageManager.GET_PERMISSIONS)))
            .thenReturn(packageInfo);
        
        // Act
        Set<String> result1 = manifestParser.getDeclaredPermissions(mockContext);
        Set<String> result2 = manifestParser.getDeclaredPermissions(mockContext);
        
        // Assert
        assertEquals(result1, result2);
        verify(mockPackageManager, times(1)).getPackageInfo(anyString(), anyInt());
    }
    
    @Test
    public void testGetDangerousPermissions_FiltersDangerousPermissions() throws Exception {
        // Arrange
        String[] allPermissions = {
            "android.permission.CAMERA",           // dangerous
            "android.permission.INTERNET",         // normal
            "android.permission.READ_EXTERNAL_STORAGE" // dangerous
        };
        
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.requestedPermissions = allPermissions;
        
        when(mockPackageManager.getPackageInfo(eq(TEST_PACKAGE_NAME), eq(PackageManager.GET_PERMISSIONS)))
            .thenReturn(packageInfo);
        
        // Mock dangerous permissions
        PermissionInfo cameraPermInfo = new PermissionInfo();
        cameraPermInfo.protectionLevel = PermissionInfo.PROTECTION_DANGEROUS;
        when(mockPackageManager.getPermissionInfo("android.permission.CAMERA", 0))
            .thenReturn(cameraPermInfo);
        
        PermissionInfo internetPermInfo = new PermissionInfo();
        internetPermInfo.protectionLevel = PermissionInfo.PROTECTION_NORMAL;
        when(mockPackageManager.getPermissionInfo("android.permission.INTERNET", 0))
            .thenReturn(internetPermInfo);
        
        PermissionInfo storagePermInfo = new PermissionInfo();
        storagePermInfo.protectionLevel = PermissionInfo.PROTECTION_DANGEROUS;
        when(mockPackageManager.getPermissionInfo("android.permission.READ_EXTERNAL_STORAGE", 0))
            .thenReturn(storagePermInfo);
        
        // Act
        Set<String> result = manifestParser.getDangerousPermissions(mockContext);
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("android.permission.CAMERA"));
        assertTrue(result.contains("android.permission.READ_EXTERNAL_STORAGE"));
        assertFalse(result.contains("android.permission.INTERNET"));
    }
    
    @Test
    public void testIsPermissionDeclared_DeclaredPermission_ReturnsTrue() throws Exception {
        // Arrange
        String[] permissions = {"android.permission.CAMERA"};
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.requestedPermissions = permissions;
        
        when(mockPackageManager.getPackageInfo(eq(TEST_PACKAGE_NAME), eq(PackageManager.GET_PERMISSIONS)))
            .thenReturn(packageInfo);
        
        // Act
        boolean result = manifestParser.isPermissionDeclared(mockContext, "android.permission.CAMERA");
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testIsPermissionDeclared_UndeclaredPermission_ReturnsFalse() throws Exception {
        // Arrange
        String[] permissions = {"android.permission.CAMERA"};
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.requestedPermissions = permissions;
        
        when(mockPackageManager.getPackageInfo(eq(TEST_PACKAGE_NAME), eq(PackageManager.GET_PERMISSIONS)))
            .thenReturn(packageInfo);
        
        // Act
        boolean result = manifestParser.isPermissionDeclared(mockContext, "android.permission.LOCATION");
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testIsPermissionDeclared_NullPermission_ReturnsFalse() throws Exception {
        // Act
        boolean result = manifestParser.isPermissionDeclared(mockContext, null);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testIsPermissionDeclared_EmptyPermission_ReturnsFalse() throws Exception {
        // Act
        boolean result = manifestParser.isPermissionDeclared(mockContext, "");
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testValidatePermissions_AllDeclared_NoException() throws Exception {
        // Arrange
        String[] permissions = {"android.permission.CAMERA", "android.permission.INTERNET"};
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.requestedPermissions = permissions;
        
        when(mockPackageManager.getPackageInfo(eq(TEST_PACKAGE_NAME), eq(PackageManager.GET_PERMISSIONS)))
            .thenReturn(packageInfo);
        
        // Act & Assert (no exception should be thrown)
        manifestParser.validatePermissions(mockContext, permissions);
    }
    
    @Test(expected = PermissionNotDeclaredException.class)
    public void testValidatePermissions_SomeUndeclared_ThrowsException() throws Exception {
        // Arrange
        String[] declaredPermissions = {"android.permission.CAMERA"};
        String[] requestedPermissions = {"android.permission.CAMERA", "android.permission.LOCATION"};
        
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.requestedPermissions = declaredPermissions;
        
        when(mockPackageManager.getPackageInfo(eq(TEST_PACKAGE_NAME), eq(PackageManager.GET_PERMISSIONS)))
            .thenReturn(packageInfo);
        
        // Act
        manifestParser.validatePermissions(mockContext, requestedPermissions);
    }
    
    @Test
    public void testValidatePermissions_NullArray_NoException() throws Exception {
        // Act & Assert (no exception should be thrown)
        manifestParser.validatePermissions(mockContext, null);
    }
    
    @Test
    public void testValidatePermissions_EmptyArray_NoException() throws Exception {
        // Act & Assert (no exception should be thrown)
        manifestParser.validatePermissions(mockContext, new String[0]);
    }
    
    @Test
    public void testClearCache_ClearsInternalCache() throws Exception {
        // Arrange
        String[] permissions = {"android.permission.CAMERA"};
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.requestedPermissions = permissions;
        
        when(mockPackageManager.getPackageInfo(eq(TEST_PACKAGE_NAME), eq(PackageManager.GET_PERMISSIONS)))
            .thenReturn(packageInfo);
        
        // Act
        manifestParser.getDeclaredPermissions(mockContext); // Populate cache
        manifestParser.clearCache();
        manifestParser.getDeclaredPermissions(mockContext); // Should call PackageManager again
        
        // Assert
        verify(mockPackageManager, times(2)).getPackageInfo(anyString(), anyInt());
    }
}