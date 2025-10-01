Feature: User Onboarding and Account Creation
  As a new user of SmartWatts Edge
  I want to create an account and access the system
  So that I can monitor my energy usage locally

  Background:
    Given the SmartWatts Edge device is running in offline mode
    And no internet connectivity is available
    And the local database is initialized

  Scenario: First-time setup wizard loads correctly
    Given I am a new user accessing the system for the first time
    When I navigate to the application
    Then I should see the welcome message "Welcome to SmartWatts"
    And I should see the setup wizard interface
    And I should see input fields for email and password
    And I should see a "Create Account" button

  Scenario: New user account creation in offline mode
    Given I am on the registration page
    When I enter my email address "test@mysmartwatts.com"
    And I enter my password "TestPassword123!"
    And I confirm my password "TestPassword123!"
    And I enter my first name "Test"
    And I enter my last name "User"
    And I enter my phone number "+2341234567890"
    And I click "Create Account"
    Then I should see "Account created successfully"
    And I should see "Please verify your email"
    And my account should be stored in the local database
    And my password should be hashed and salted

  Scenario: Local credential storage and validation
    Given I have created an account with email "test@mysmartwatts.com"
    When I attempt to login with email "test@mysmartwatts.com"
    And I enter the correct password "TestPassword123!"
    And I click "Login"
    Then I should be redirected to the dashboard
    And I should see "Welcome to your dashboard"
    And my session should be stored locally
    And I should be able to access the system without internet

  Scenario: Password reset flow in offline mode
    Given I have an existing account with email "test@mysmartwatts.com"
    When I click "Forgot Password"
    And I enter my email address "test@mysmartwatts.com"
    And I click "Send Reset Instructions"
    Then I should see "Password reset instructions sent"
    And a reset token should be generated locally
    And I should be able to reset my password using the local token

  Scenario: Role-based access control
    Given I have created an admin account with email "admin@mysmartwatts.com"
    And I have created a regular user account with email "user@mysmartwatts.com"
    When I login as admin with email "admin@mysmartwatts.com"
    Then I should have access to the admin dashboard
    And I should see admin-specific features
    When I login as regular user with email "user@mysmartwatts.com"
    Then I should not have access to admin features
    And I should see "Access Denied" when trying to access admin pages

  Scenario: Dashboard loads correctly on different devices
    Given I have logged in successfully
    When I access the dashboard on a desktop browser
    Then I should see the "Energy Intelligence Dashboard"
    And I should see current load information
    And I should see efficiency metrics
    And I should see solar generation data
    And I should see cost savings information
    And I should see interactive charts
    When I access the dashboard on a mobile device
    Then I should see a mobile-optimized layout
    And all elements should be properly sized for mobile
    When I access the dashboard on a tablet
    Then I should see a tablet-optimized layout
    And the sidebar should be visible

  Scenario: Offline validation simulation
    Given the mock cloud service is running
    When I register with email "test@mysmartwatts.com"
    Then I should receive a verification code via the mock email service
    When I enter the verification code
    And I click "Verify Email"
    Then I should see "Email verified successfully"
    And my account should be marked as verified
    And the verification should be stored locally

  Scenario: User session persistence across reboots
    Given I have logged in successfully
    And I have been using the system for some time
    When the device is rebooted
    And I access the application again
    Then I should still be logged in
    And my session should be restored
    And I should see my previous data
    And I should not need to login again

  Scenario: Multiple user accounts support
    Given I have created multiple user accounts
    When different users login simultaneously
    Then each user should see only their own data
    And user sessions should be isolated
    And data should not be shared between users
    And each user should have their own device pairings

  Scenario: Account data migration to cloud
    Given I have a local account with data
    And internet connectivity becomes available
    When the system attempts to sync with cloud services
    Then my local account should be migrated to cloud authentication
    And my local data should be uploaded to the cloud
    And I should be able to access my data from cloud services
    And the local account should remain functional for offline use
