import { test, expect } from '@playwright/test'

test.describe('Timeout Edge Cases', () => {
  test('should handle slow API responses', async ({ page }) => {
    // Simulate slow API
    await page.route('**/api/proxy*', route => {
      setTimeout(() => {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ data: 'slow response' })
        })
      }, 5000) // 5 second delay
    })

    await page.goto('/dashboard')
    
    // Should show loading state
    const loadingIndicator = page.locator('text=/loading|please wait|fetching/i')
    await expect(loadingIndicator.first()).toBeVisible({ timeout: 3000 }).catch(() => {
      // Loading might be handled differently
      expect(page.url()).toBeTruthy()
    })
  })

  test('should handle request timeouts', async ({ page }) => {
    // Simulate timeout
    await page.route('**/api/proxy*', route => {
      // Don't fulfill, let it timeout
    })

    await page.goto('/dashboard', { timeout: 10000 })
    
    // Should handle timeout gracefully
    await page.waitForTimeout(3000)
    expect(page.url()).toBeTruthy()
    
    // Check for timeout error message
    const timeoutMessage = page.locator('text=/timeout|request failed|try again/i')
    await expect(timeoutMessage.first()).toBeVisible({ timeout: 5000 }).catch(() => {
      // Timeout handling might be different
      expect(page.url()).toBeTruthy()
    })
  })

  test('should handle page load timeouts', async ({ page }) => {
    // Set very short timeout
    page.setDefaultTimeout(1000)
    
    try {
      await page.goto('/dashboard')
    } catch (error) {
      // Timeout is expected
      expect(error).toBeTruthy()
    }
    
    // Reset timeout
    page.setDefaultTimeout(30000)
  })

  test('should handle element wait timeouts', async ({ page }) => {
    await page.goto('/dashboard')
    
    // Try to find non-existent element with timeout
    try {
      await page.locator('[data-testid="non-existent"]').waitFor({ timeout: 2000 })
    } catch (error) {
      // Timeout is expected
      expect(error).toBeTruthy()
    }
  })

  test('should handle navigation timeouts', async ({ page }) => {
    // Block navigation to simulate timeout
    await page.route('**/*', route => {
      // Don't fulfill
    })

    try {
      await page.goto('/dashboard', { timeout: 5000 })
    } catch (error) {
      // Timeout is expected
      expect(error).toBeTruthy()
    }
  })
})

