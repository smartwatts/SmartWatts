# IDE Refresh Guide for Spring Security Test Dependencies

## Problem
The IDE may show errors for `org.springframework.security.test` imports even though the dependencies are correctly configured in `build.gradle` files. This is typically an IDE indexing/sync issue, not an actual build problem.

## Permanent Solution

### 1. Verification Script
We've created a verification script (`scripts/verify-test-dependencies.sh`) that ensures all services have the required test dependencies. This prevents the recurring issue of missing Spring Security test dependencies.

Run the verification script to ensure all services have the required dependencies:

```bash
cd backend
./scripts/verify-test-dependencies.sh
```

### 3. IDE Refresh Steps

#### IntelliJ IDEA
1. **File → Invalidate Caches / Restart**
   - Select "Invalidate and Restart"
   - This will clear the IDE cache and reindex all files

2. **Gradle Sync**
   - Open the Gradle tool window (View → Tool Windows → Gradle)
   - Click the "Reload All Gradle Projects" button (circular arrow icon)

3. **Manual Refresh**
   - Right-click on the project root → "Reload Gradle Project"

#### VS Code / Cursor
1. **Reload Window**
   - Press `Cmd+Shift+P` (Mac) or `Ctrl+Shift+P` (Windows/Linux)
   - Type "Reload Window" and select it

2. **Java Extension Pack**
   - Ensure the Java Extension Pack is installed
   - The extension should automatically sync with Gradle

3. **Manual Gradle Refresh**
   - Open the Command Palette (`Cmd+Shift+P`)
   - Type "Java: Clean Java Language Server Workspace"
   - Restart the IDE

### 4. Gradle Refresh
If the IDE still shows errors, force Gradle to refresh dependencies:

```bash
cd backend
for service in user-service device-service energy-service analytics-service billing-service facility-service feature-flag-service appliance-monitoring-service; do
    cd $service
    ./gradlew clean build --refresh-dependencies
    cd ..
done
```

### 5. Verify Build Works
Even if the IDE shows errors, verify that the actual build works:

```bash
cd backend/billing-service
./gradlew clean build
```

If the build succeeds, the issue is purely an IDE indexing problem.

## Prevention

### Verification Script
The verification script (`scripts/verify-test-dependencies.sh`) ensures all services have:
- `org.springframework.boot:spring-boot-starter-test`
- `org.springframework.security:spring-security-test`

Run this script regularly (especially after adding new services) to ensure consistency.

## Troubleshooting

### Issue: IDE still shows errors after refresh
**Solution**: 
1. Close the IDE completely
2. Delete `.idea` folder (IntelliJ) or `.vscode` folder (VS Code)
3. Reopen the IDE
4. Let it reindex the project

### Issue: Build fails with "cannot find symbol"
**Solution**:
1. Check that `build.gradle` has the dependency:
   ```gradle
   testImplementation 'org.springframework.security:spring-security-test'
   ```
2. Run `./gradlew clean build --refresh-dependencies`
3. If still failing, check the Spring Boot version compatibility

### Issue: Dependencies are in build.gradle but IDE doesn't recognize them
**Solution**:
1. This is an IDE indexing issue, not a build issue
2. Follow the IDE refresh steps above
3. The build should still work even if the IDE shows errors

## Summary

The permanent solution includes:
1. ✅ Verification script to check all services (`scripts/verify-test-dependencies.sh`)
2. ✅ IDE refresh documentation
3. ✅ Gradle refresh commands

This ensures that:
- All services have consistent test dependencies
- The IDE properly recognizes dependencies
- New services can be verified automatically
- The issue doesn't recur



