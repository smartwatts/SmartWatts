# SmartWatts Manual Testing Guide

This guide provides step-by-step procedures for manually testing all SmartWatts features before production deployment.

## Prerequisites

- Staging environment deployed and accessible
- Test user accounts created
- Test devices configured
- Browser developer tools available

## Testing Procedures

### 1. User Registration and Login

#### Registration Flow
1. Navigate to `/register`
2. Fill in all required fields:
   - Email: test@example.com
   - Password: Password123!
   - First Name: Test
   - Last Name: User
   - Phone Number: 1234567890
   - Address: 123 Main St
   - City: Lagos
   - State: Lagos
   - Country: Nigeria
3. Select property type and energy provider
4. Click "Register"
5. **Expected**: Success message, redirect to dashboard or login

#### Login Flow
1. Navigate to `/login`
2. Enter email and password
3. Click "Login"
4. **Expected**: Success message, redirect to dashboard

#### Password Reset Flow
1. Navigate to `/login`
2. Click "Forgot Password"
3. Enter email address
4. **Expected**: Password reset email sent
5. Check email for reset link
6. Click reset link
7. Enter new password
8. **Expected**: Password reset successful, redirect to login

### 2. Device Management Workflows

#### Device Registration
1. Navigate to `/devices` or `/device-management`
2. Click "Add Device"
3. Fill in device information:
   - Device Name: Test Smart Meter
   - Device ID: SW_001
   - Serial Number: SW12345
   - Manufacturer: SmartWatts
   - Model: SW-Meter-2024
   - Device Type: Smart Meter
   - Protocol: MQTT
   - Location: Lagos, Nigeria
4. Click "Add Device"
5. **Expected**: Device added successfully, appears in device list

#### Device Activation
1. Navigate to device list
2. Find unactivated device
3. Click "Activate" or "Verify"
4. Follow activation workflow
5. **Expected**: Device activated, status changes to "Online"

#### Device Configuration
1. Navigate to device list
2. Click on a device
3. Click "Configure" or "Settings"
4. Update device settings
5. Save changes
6. **Expected**: Settings saved, device updated

### 3. Dashboard and Analytics Features

#### Dashboard Access
1. Login to application
2. Navigate to `/dashboard`
3. **Expected**: Dashboard loads with widgets

#### Energy Monitoring
1. Navigate to `/energy` or dashboard energy widget
2. **Expected**: Energy consumption data displayed
3. Change date range
4. **Expected**: Data updates for selected range
5. View different views (daily, weekly, monthly)
6. **Expected**: Data updates correctly

#### Analytics Features
1. Navigate to `/analytics`
2. **Expected**: Analytics charts and insights displayed
3. Filter by device or time period
4. **Expected**: Analytics update correctly
5. Export data (if available)
6. **Expected**: Data exported successfully

### 4. Billing and Payment Flows

#### Bill Generation
1. Navigate to `/billing`
2. **Expected**: Bills displayed for user
3. View bill details
4. **Expected**: Bill breakdown displayed correctly

#### Payment Processing
1. Navigate to billing page
2. Click "Pay Bill" or "Purchase Token"
3. Enter payment details (test mode)
4. Submit payment
5. **Expected**: Payment processed, confirmation displayed

#### MYTO Tariff Calculation
1. Navigate to billing page
2. View tariff information
3. **Expected**: MYTO tariffs displayed correctly
4. Verify calculations
5. **Expected**: Calculations match expected values

### 5. PWA Installation and Offline Mode

#### PWA Installation (Desktop)
1. Open application in Chrome/Edge
2. Look for install prompt or menu option
3. Click "Install" or "Add to Home Screen"
4. **Expected**: PWA installs, icon appears on desktop/home screen

#### PWA Installation (Mobile)
1. Open application in mobile browser
2. Look for install prompt
3. Tap "Install" or "Add to Home Screen"
4. **Expected**: PWA installs, icon appears on home screen

#### Offline Mode
1. Install PWA
2. Open PWA
3. Enable airplane mode or disconnect internet
4. **Expected**: Offline indicator appears
5. Navigate to different pages
6. **Expected**: Cached pages load, offline data displayed
7. Reconnect internet
8. **Expected**: Data syncs, online indicator appears

### 6. Mobile-Specific Features

#### Touch Interactions
1. Open application on mobile device
2. Test all touch interactions:
   - Tap buttons
   - Swipe navigation
   - Pinch to zoom
   - Long press menus
3. **Expected**: All interactions work correctly

#### Responsive Design
1. Open application on mobile device
2. Navigate through all pages
3. **Expected**: Layout adapts to mobile screen
4. Rotate device
5. **Expected**: Layout adjusts correctly

#### Mobile Navigation
1. Open application on mobile device
2. Test navigation menu
3. **Expected**: Menu opens/closes correctly
4. Navigate between pages
5. **Expected**: Navigation works smoothly

### 7. Appliance Monitoring

#### Appliance Recognition
1. Navigate to appliance monitoring page
2. **Expected**: Detected appliances displayed
3. View appliance details
4. **Expected**: Appliance information displayed correctly

#### Appliance Confirmation
1. Navigate to appliance monitoring page
2. Find unconfirmed appliance
3. Click "Confirm" or "Reject"
4. **Expected**: Appliance status updates

### 8. Solar and Circuit Management

#### Solar Panel Monitoring
1. Navigate to solar monitoring page
2. **Expected**: Solar panel data displayed
3. View per-panel information
4. **Expected**: Individual panel data displayed

#### Circuit Tree View
1. Navigate to circuit management page
2. **Expected**: Circuit tree displayed
3. Expand/collapse circuit nodes
4. **Expected**: Tree navigation works correctly

### 9. Community Features

#### Leaderboard
1. Navigate to community/leaderboard page
2. **Expected**: Leaderboard displayed
3. View rankings
4. **Expected**: Rankings displayed correctly

#### Benchmarking
1. Navigate to benchmarking page
2. **Expected**: Comparison data displayed
3. View regional comparisons
4. **Expected**: Comparisons displayed correctly

### 10. Error Handling

#### Network Errors
1. Open application
2. Disconnect internet
3. Try to perform actions
4. **Expected**: Error messages displayed, graceful degradation

#### API Errors
1. Open browser developer tools
2. Simulate API errors (network tab)
3. Try to perform actions
4. **Expected**: Error messages displayed, user-friendly messages

#### Form Validation
1. Navigate to any form
2. Submit form with invalid data
3. **Expected**: Validation errors displayed
4. Correct errors and resubmit
5. **Expected**: Form submits successfully

## Testing Checklist

### Desktop Browsers
- [ ] Chrome (latest)
- [ ] Firefox (latest)
- [ ] Safari (latest)
- [ ] Edge (latest)

### Mobile Browsers
- [ ] Android Chrome
- [ ] iOS Safari

### PWA
- [ ] Desktop installation
- [ ] Mobile installation
- [ ] Offline mode
- [ ] Background sync

### Features
- [ ] User registration
- [ ] User login
- [ ] Password reset
- [ ] Device management
- [ ] Energy monitoring
- [ ] Analytics
- [ ] Billing
- [ ] Appliance monitoring
- [ ] Solar monitoring
- [ ] Circuit management
- [ ] Community features

## Reporting Issues

When reporting issues, include:
1. Browser and version
2. Device and OS
3. Steps to reproduce
4. Expected behavior
5. Actual behavior
6. Screenshots (if applicable)
7. Console errors (if any)

