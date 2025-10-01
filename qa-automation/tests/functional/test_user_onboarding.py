"""
SmartWatts Edge Functional Tests - User Onboarding
"""

import pytest
import asyncio
from playwright.async_api import expect
from tests.utils.config import TestConfig
from tests.fixtures.mock_services import MockCloudService


class TestUserOnboarding:
    """Test user onboarding and account creation in offline mode."""
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_first_time_setup_wizard_loads(self, page, test_config):
        """Test that first-time setup wizard loads correctly."""
        # Navigate to the application
        await page.goto(f"{test_config.api_base_url}")
        
        # Wait for the setup wizard to appear
        await expect(page.locator("text=Welcome to SmartWatts")).to_be_visible()
        await expect(page.locator("text=Let's get you started")).to_be_visible()
        
        # Verify setup wizard elements
        await expect(page.locator("input[placeholder*='email']")).to_be_visible()
        await expect(page.locator("input[type='password']")).to_be_visible()
        await expect(page.locator("button:has-text('Create Account')")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_new_user_account_creation(self, page, test_config, mock_user_data):
        """Test creating a new user account in offline mode."""
        # Navigate to registration page
        await page.goto(f"{test_config.api_base_url}/register")
        
        # Fill registration form
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", mock_user_data["password"])
        await page.fill("input[name='confirmPassword']", mock_user_data["password"])
        await page.fill("input[name='firstName']", mock_user_data["firstName"])
        await page.fill("input[name='lastName']", mock_user_data["lastName"])
        await page.fill("input[name='phoneNumber']", mock_user_data["phoneNumber"])
        
        # Submit registration
        await page.click("button[type='submit']")
        
        # Verify success message
        await expect(page.locator("text=Account created successfully")).to_be_visible()
        await expect(page.locator("text=Please verify your email")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_local_credential_storage(self, page, test_config, mock_user_data, test_database):
        """Test that credentials are stored locally with proper hashing."""
        # Create user account
        await page.goto(f"{test_config.api_base_url}/register")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", mock_user_data["password"])
        await page.fill("input[name='confirmPassword']", mock_user_data["password"])
        await page.fill("input[name='firstName']", mock_user_data["firstName"])
        await page.fill("input[name='lastName']", mock_user_data["lastName"])
        await page.click("button[type='submit']")
        
        # Verify user is stored in local database
        user = test_database.get_user_by_email(mock_user_data["email"])
        assert user is not None
        assert user["email"] == mock_user_data["email"]
        assert user["first_name"] == mock_user_data["firstName"]
        assert user["last_name"] == mock_user_data["lastName"]
        
        # Verify password is hashed (not plain text)
        assert user["password"] != mock_user_data["password"]
        assert len(user["password"]) > 50  # BCrypt hash length
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_offline_login_functionality(self, page, test_config, mock_user_data, test_database):
        """Test login functionality without internet connectivity."""
        # Create user first
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",  # "password"
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Navigate to login page
        await page.goto(f"{test_config.api_base_url}/login")
        
        # Fill login form
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        
        # Submit login
        await page.click("button[type='submit']")
        
        # Verify successful login
        await expect(page.locator("text=Welcome to your dashboard")).to_be_visible()
        await expect(page.locator(f"text={mock_user_data['firstName']}")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_password_reset_offline_mode(self, page, test_config, mock_user_data, test_database):
        """Test password reset flow in offline mode."""
        # Create user first
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Navigate to forgot password page
        await page.goto(f"{test_config.api_base_url}/forgot-password")
        
        # Enter email
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.click("button[type='submit']")
        
        # Verify reset email sent message
        await expect(page.locator("text=Password reset instructions sent")).to_be_visible()
        
        # Verify reset token is stored locally
        # (In a real implementation, this would be stored in the database)
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_role_based_access_control(self, page, test_config, test_database):
        """Test role-based access control works locally."""
        # Create admin user
        admin_id = test_database.insert_user({
            "username": "admin",
            "email": "admin@mysmartwatts.com",
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": "Admin",
            "last_name": "User",
            "phone_number": "+2341234567890",
            "role": "ROLE_ENTERPRISE_ADMIN",
            "is_active": True
        })
        
        # Create regular user
        user_id = test_database.insert_user({
            "username": "user",
            "email": "user@mysmartwatts.com",
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": "Regular",
            "last_name": "User",
            "phone_number": "+2341234567891",
            "role": "ROLE_USER",
            "is_active": True
        })
        
        # Test admin access
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", "admin@mysmartwatts.com")
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Verify admin can access admin dashboard
        await page.goto(f"{test_config.api_base_url}/admin/dashboard")
        await expect(page.locator("text=Admin Dashboard")).to_be_visible()
        
        # Test regular user access
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", "user@mysmartwatts.com")
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Verify regular user cannot access admin dashboard
        await page.goto(f"{test_config.api_base_url}/admin/dashboard")
        await expect(page.locator("text=Access Denied")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_dashboard_loads_correctly(self, page, test_config, mock_user_data, test_database):
        """Test that dashboard loads correctly in different browsers."""
        # Create user and login
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Verify dashboard elements
        await expect(page.locator("text=Energy Intelligence Dashboard")).to_be_visible()
        await expect(page.locator("text=Current Load")).to_be_visible()
        await expect(page.locator("text=Efficiency")).to_be_visible()
        await expect(page.locator("text=Solar Generation")).to_be_visible()
        await expect(page.locator("text=Cost Savings")).to_be_visible()
        
        # Verify charts are present
        await expect(page.locator("canvas")).to_be_visible()
        
        # Verify navigation menu
        await expect(page.locator("text=Dashboard")).to_be_visible()
        await expect(page.locator("text=Devices")).to_be_visible()
        await expect(page.locator("text=Analytics")).to_be_visible()
        await expect(page.locator("text=Profile")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_mobile_responsive_design(self, page, test_config, mock_user_data, test_database):
        """Test dashboard responsiveness on mobile devices."""
        # Set mobile viewport
        await page.set_viewport_size({"width": 375, "height": 667})
        
        # Create user and login
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Verify mobile layout
        await expect(page.locator("text=Energy Intelligence Dashboard")).to_be_visible()
        
        # Check if mobile menu is present
        mobile_menu = page.locator("button[aria-label='Toggle menu']")
        if await mobile_menu.is_visible():
            await mobile_menu.click()
            await expect(page.locator("text=Dashboard")).to_be_visible()
            await expect(page.locator("text=Devices")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_tablet_responsive_design(self, page, test_config, mock_user_data, test_database):
        """Test dashboard responsiveness on tablet devices."""
        # Set tablet viewport
        await page.set_viewport_size({"width": 768, "height": 1024})
        
        # Create user and login
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Verify tablet layout
        await expect(page.locator("text=Energy Intelligence Dashboard")).to_be_visible()
        
        # Check if sidebar is visible
        await expect(page.locator("text=Dashboard")).to_be_visible()
        await expect(page.locator("text=Devices")).to_be_visible()
        await expect(page.locator("text=Analytics")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_offline_validation_simulation(self, page, test_config, mock_cloud_service):
        """Test offline validation simulation with mock cloud service."""
        # Start mock cloud service
        await mock_cloud_service.start_server()
        
        try:
            # Navigate to registration
            await page.goto(f"{test_config.api_base_url}/register")
            
            # Fill registration form
            await page.fill("input[name='email']", "test@mysmartwatts.com")
            await page.fill("input[name='password']", "TestPassword123!")
            await page.fill("input[name='confirmPassword']", "TestPassword123!")
            await page.fill("input[name='firstName']", "Test")
            await page.fill("input[name='lastName']", "User")
            await page.fill("input[name='phoneNumber']", "+2341234567890")
            
            # Submit registration
            await page.click("button[type='submit']")
            
            # Verify validation required message
            await expect(page.locator("text=Please verify your email")).to_be_visible()
            
            # Get verification code from mock service
            verification_code = mock_cloud_service.get_verification_code("test@mysmartwatts.com")
            assert verification_code is not None
            
            # Navigate to verification page
            await page.goto(f"{test_config.api_base_url}/verify-email")
            
            # Enter verification code
            await page.fill("input[name='code']", verification_code)
            await page.fill("input[name='email']", "test@mysmartwatts.com")
            await page.click("button[type='submit']")
            
            # Verify successful verification
            await expect(page.locator("text=Email verified successfully")).to_be_visible()
            
        finally:
            await mock_cloud_service.stop_server()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_user_session_persistence(self, page, test_config, mock_user_data, test_database):
        """Test that user sessions persist across device reboots."""
        # Create user and login
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Verify login successful
        await expect(page.locator("text=Welcome to your dashboard")).to_be_visible()
        
        # Simulate device reboot by clearing browser storage
        await page.context.clear_cookies()
        await page.reload()
        
        # Verify user is still logged in (session persisted)
        await expect(page.locator("text=Welcome to your dashboard")).to_be_visible()
        await expect(page.locator(f"text={mock_user_data['firstName']}")).to_be_visible()
