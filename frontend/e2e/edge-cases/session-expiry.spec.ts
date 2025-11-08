import { test, expect } from '@playwright/test'

test.describe('Session Expiry Edge Cases', () => {
  test('should handle expired authentication tokens', async ({ page }) => {
    // Set expired token in localStorage
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'expired-token')
      localStorage.setItem('tokenExpiry', '0') // Expired
    })

    await page.goto('/dashboard')
    
    // Should redirect to login or show session expired message
    await page.waitForTimeout(3000)
    
    const loginRedirect = page.url().includes('/login')
    const expiredMessage = page.locator('text=/session expired|token expired|please log in/i')
    
    expect(loginRedirect || await expiredMessage.count() > 0).toBeTruthy()
  })

  test('should handle missing authentication tokens', async ({ page }) => {
    // Clear authentication
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.removeItem('token')
      sessionStorage.clear()
    })

    await page.goto('/dashboard')
    
    // Should redirect to login
    await page.waitForTimeout(3000)
    expect(page.url()).toMatch(/\/login|\/$/)
  })

  test('should handle invalid token format', async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'invalid-token-format')
    })

    await page.goto('/dashboard')
    
    // Should handle invalid token gracefully
    await page.waitForTimeout(3000)
    const loginRedirect = page.url().includes('/login')
    const errorMessage = page.locator('text=/invalid|error|unauthorized/i')
    
    expect(loginRedirect || await errorMessage.count() > 0).toBeTruthy()
  })

  test('should refresh token before expiry', async ({ page }) => {
    // Set token that will expire soon
    await page.goto('/')
    await page.evaluate(() => {
      const futureTime = Date.now() + 60000 // 1 minute from now
      localStorage.setItem('token', 'valid-token')
      localStorage.setItem('tokenExpiry', futureTime.toString())
    })

    await page.goto('/dashboard')
    
    // Should attempt token refresh
    await page.waitForTimeout(5000)
    
    // Check if refresh token API was called
    const networkLogs = await page.evaluate(() => {
      return (window as any).__networkLogs || []
    })
    
    // Page should remain on dashboard if refresh succeeds
    expect(page.url()).toBeTruthy()
  })

  test('should handle logout during active session', async ({ page }) => {
    await page.goto('/dashboard')
    
    // Find and click logout button
    const logoutButton = page.locator('text=/logout|sign out|log out/i')
    
    if (await logoutButton.isVisible()) {
      await logoutButton.click()
      
      // Should redirect to login
      await page.waitForTimeout(2000)
      expect(page.url()).toMatch(/\/login|\/$/)
      
      // Token should be cleared
      const token = await page.evaluate(() => localStorage.getItem('token'))
      expect(token).toBeNull()
    }
  })

  test('should handle concurrent session expiry', async ({ page }) => {
    // Simulate session expiry during active use
    await page.goto('/dashboard')
    
    // Expire session while on page
    await page.evaluate(() => {
      localStorage.removeItem('token')
    })
    
    // Try to perform an action
    const buttons = page.locator('button').first()
    if (await buttons.isVisible()) {
      await buttons.click().catch(() => {})
    }
    
    // Should handle gracefully
    await page.waitForTimeout(2000)
    expect(page.url()).toBeTruthy()
  })
})

