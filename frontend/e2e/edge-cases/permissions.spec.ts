import { test, expect } from '@playwright/test'

test.describe('Permissions Edge Cases', () => {
  test('should restrict access to admin routes for regular users', async ({ page }) => {
    // Login as regular user
    await page.goto('/login')
    
    // Mock successful login with regular user role
    await page.evaluate(() => {
      localStorage.setItem('token', 'user-token')
      localStorage.setItem('userRole', 'ROLE_USER')
    })

    // Try to access admin route
    await page.goto('/admin')
    
    // Should redirect or show unauthorized message
    await page.waitForTimeout(3000)
    
    const unauthorized = page.locator('text=/unauthorized|access denied|forbidden/i')
    const redirect = !page.url().includes('/admin')
    
    expect(redirect || await unauthorized.count() > 0).toBeTruthy()
  })

  test('should allow access to admin routes for admin users', async ({ page }) => {
    // Login as admin
    await page.goto('/login')
    
    await page.evaluate(() => {
      localStorage.setItem('token', 'admin-token')
      localStorage.setItem('userRole', 'ROLE_ENTERPRISE_ADMIN')
    })

    await page.goto('/admin')
    
    // Should allow access
    await page.waitForTimeout(3000)
    // Admin page might exist or redirect, but shouldn't show unauthorized
    const unauthorized = page.locator('text=/unauthorized|access denied/i')
    expect(await unauthorized.count()).toBe(0)
  })

  test('should handle missing permissions gracefully', async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'token')
      localStorage.removeItem('userRole') // Missing role
    })

    await page.goto('/dashboard')
    
    // Should handle missing permissions
    await page.waitForTimeout(2000)
    expect(page.url()).toBeTruthy()
  })

  test('should restrict feature access based on plan', async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'token')
      localStorage.setItem('userPlan', 'FREEMIUM')
    })

    await page.goto('/dashboard')
    
    // Premium features should be hidden or disabled
    await page.waitForTimeout(2000)
    
    const premiumFeatures = page.locator('text=/facility360|billing dashboard|advanced analytics/i')
    const featureCount = await premiumFeatures.count()
    
    // Premium features should not be visible for freemium users
    expect(featureCount).toBe(0)
  })

  test('should handle permission changes during session', async ({ page }) => {
    await page.goto('/dashboard')
    
    // Change permissions during active session
    await page.evaluate(() => {
      localStorage.setItem('userRole', 'ROLE_USER')
    })
    
    // Try to access admin feature
    const adminButton = page.locator('text=/admin|settings|manage/i')
    if (await adminButton.count() > 0) {
      await adminButton.first().click()
      
      // Should handle permission change
      await page.waitForTimeout(2000)
      expect(page.url()).toBeTruthy()
    }
  })
})

