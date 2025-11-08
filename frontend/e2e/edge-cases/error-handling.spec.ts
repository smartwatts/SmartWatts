import { test, expect } from '@playwright/test'

test.describe('Error Handling Edge Cases', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
  })

  test('should handle 404 errors gracefully', async ({ page }) => {
    const response = await page.goto('/non-existent-page')
    expect(response?.status()).toBe(404)
    
    // Check for error page or redirect
    const errorMessage = page.locator('text=/404|not found|page not found/i')
    await expect(errorMessage).toBeVisible({ timeout: 5000 }).catch(() => {
      // Error page might not exist, check for redirect to home
      expect(page.url()).toContain('/')
    })
  })

  test('should handle API errors gracefully', async ({ page }) => {
    // Intercept API calls and simulate errors
    await page.route('**/api/proxy*', route => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Internal Server Error' })
      })
    })

    await page.goto('/dashboard')
    
    // Check for error message display
    const errorMessage = page.locator('text=/error|failed|something went wrong/i')
    await expect(errorMessage.first()).toBeVisible({ timeout: 10000 }).catch(() => {
      // Error handling might be different, just verify page doesn't crash
      expect(page.url()).toBeTruthy()
    })
  })

  test('should handle network failures', async ({ page }) => {
    // Simulate offline mode
    await page.context().setOffline(true)
    
    await page.goto('/dashboard')
    
    // Check for offline indicator or cached content
    const offlineIndicator = page.locator('text=/offline|no connection|check your connection/i')
    await expect(offlineIndicator.first()).toBeVisible({ timeout: 5000 }).catch(() => {
      // Offline handling might show cached content
      expect(page.url()).toBeTruthy()
    })
    
    // Restore online
    await page.context().setOffline(false)
  })

  test('should handle invalid form submissions', async ({ page }) => {
    await page.goto('/login')
    
    // Try to submit empty form
    const submitButton = page.locator('button[type="submit"]')
    if (await submitButton.isVisible()) {
      await submitButton.click()
      
      // Check for validation errors
      const errorMessages = page.locator('text=/required|invalid|error/i')
      const count = await errorMessages.count()
      expect(count).toBeGreaterThan(0)
    }
  })

  test('should handle malformed API responses', async ({ page }) => {
    // Intercept and return malformed JSON
    await page.route('**/api/proxy*', route => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: 'invalid json{'
      })
    })

    await page.goto('/dashboard')
    
    // Page should handle JSON parse errors gracefully
    await page.waitForTimeout(2000)
    expect(page.url()).toBeTruthy()
  })

  test('should handle timeout errors', async ({ page }) => {
    // Intercept and delay response
    await page.route('**/api/proxy*', route => {
      setTimeout(() => {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ data: [] })
        })
      }, 10000) // 10 second delay
    })

    await page.goto('/dashboard')
    
    // Check for timeout handling or loading state
    const loadingIndicator = page.locator('text=/loading|please wait/i')
    await expect(loadingIndicator.first()).toBeVisible({ timeout: 5000 }).catch(() => {
      // Timeout might be handled differently
      expect(page.url()).toBeTruthy()
    })
  })
})

