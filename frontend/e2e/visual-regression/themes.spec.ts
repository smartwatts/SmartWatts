import { test, expect } from '@playwright/test'

test.describe('Theme Visual Regression', () => {
  test.beforeEach(async ({ page }) => {
    // Mock authentication
    await page.route('**/api/proxy?service=user&path=/users/login', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          accessToken: 'mock-jwt-token-12345',
          userId: '1',
          username: 'user',
          email: 'user@mysmartwatts.com',
          role: 'ROLE_USER',
          active: true
        })
      })
    })

    await page.route('**/api/proxy?service=user&path=/users/profile', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: '1',
          email: 'user@mysmartwatts.com',
          firstName: 'User',
          lastName: 'SmartWatts',
          role: 'ROLE_USER',
          isActive: true,
          createdAt: new Date().toISOString()
        })
      })
    })

    // Login
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
    
    await page.getByLabel(/email/i).fill('user@mysmartwatts.com')
    await page.getByLabel(/password/i).fill('password')
    await page.getByRole('button', { name: /sign in|signing in/i }).click()
    
    // Wait for navigation to dashboard
    try {
      await page.waitForURL(/\/dashboard/, { timeout: 15000 })
    } catch (e) {
      await page.goto('/dashboard')
    }
    
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
  })

  test('should match light theme', async ({ page }) => {
    // Set light theme
    await page.evaluate(() => {
      localStorage.setItem('theme', 'light')
      document.documentElement.classList.remove('dark')
      document.documentElement.classList.add('light')
    })

    // Navigate to dashboard if not already there
    const currentUrl = page.url()
    if (!currentUrl.includes('/dashboard')) {
      await page.goto('/dashboard')
    }
    
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(3000)
    
    await expect(page).toHaveScreenshot('dashboard-light-theme.png', {
      fullPage: true,
      maxDiffPixels: 100
    })
  })

  test('should match dark theme', async ({ page }) => {
    // Set dark theme
    await page.evaluate(() => {
      localStorage.setItem('theme', 'dark')
      document.documentElement.classList.remove('light')
      document.documentElement.classList.add('dark')
    })

    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    await expect(page).toHaveScreenshot('dashboard-dark-theme.png', {
      fullPage: true,
      maxDiffPixels: 100
    })
  })

  test('should match professional intelligence theme', async ({ page }) => {
    // Set professional intelligence theme
    await page.evaluate(() => {
      localStorage.setItem('dashboardStyle', 'professional-intelligence')
    })

    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    await expect(page).toHaveScreenshot('dashboard-professional-theme.png', {
      fullPage: true,
      maxDiffPixels: 100
    })
  })

  test('should match landing page theme', async ({ page }) => {
    // Set landing page theme
    await page.evaluate(() => {
      localStorage.setItem('dashboardStyle', 'landing-page')
    })

    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    await expect(page).toHaveScreenshot('dashboard-landing-theme.png', {
      fullPage: true,
      maxDiffPixels: 100
    })
  })
})

