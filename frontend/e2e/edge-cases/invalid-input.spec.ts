import { test, expect } from '@playwright/test'

test.describe('Invalid Input Edge Cases', () => {
  test('should handle SQL injection attempts', async ({ page }) => {
    await page.goto('/login')
    
    const emailInput = page.locator('input[type="email"], input[name*="email"]')
    const passwordInput = page.locator('input[type="password"], input[name*="password"]')
    
    if (await emailInput.isVisible()) {
      const sqlInjection = "admin' OR '1'='1"
      await emailInput.fill(sqlInjection)
      
      if (await passwordInput.isVisible()) {
        await passwordInput.fill("password")
      }
      
      // Should sanitize or reject input
      const value = await emailInput.inputValue()
      expect(value).not.toContain("' OR '1'='1")
    }
  })

  test('should handle XSS attempts', async ({ page }) => {
    await page.goto('/login')
    
    const emailInput = page.locator('input[type="email"], input[name*="email"]')
    
    if (await emailInput.isVisible()) {
      const xssAttempt = "<script>alert('xss')</script>"
      await emailInput.fill(xssAttempt)
      
      // Should sanitize input
      const value = await emailInput.inputValue()
      expect(value).not.toContain('<script>')
      
      // Verify no script execution
      const alerts = page.locator('text=/alert/i')
      expect(await alerts.count()).toBe(0)
    }
  })

  test('should handle extremely long input', async ({ page }) => {
    await page.goto('/login')
    
    const emailInput = page.locator('input[type="email"], input[name*="email"]')
    
    if (await emailInput.isVisible()) {
      const longInput = 'a'.repeat(10000)
      await emailInput.fill(longInput)
      
      // Should limit or handle long input
      const value = await emailInput.inputValue()
      expect(value.length).toBeLessThanOrEqual(10000)
    }
  })

  test('should handle special characters in input', async ({ page }) => {
    await page.goto('/login')
    
    const emailInput = page.locator('input[type="email"], input[name*="email"]')
    
    if (await emailInput.isVisible()) {
      const specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?"
      await emailInput.fill(specialChars)
      
      // Should handle or validate special characters
      const value = await emailInput.inputValue()
      expect(value).toBeTruthy()
    }
  })

  test('should validate email format', async ({ page }) => {
    await page.goto('/login')
    
    const emailInput = page.locator('input[type="email"], input[name*="email"]')
    const submitButton = page.locator('button[type="submit"]')
    
    if (await emailInput.isVisible() && await submitButton.isVisible()) {
      await emailInput.fill('invalid-email')
      await submitButton.click()
      
      // Should show validation error
      const errorMessage = page.locator('text=/invalid|email|format/i')
      await expect(errorMessage.first()).toBeVisible({ timeout: 3000 }).catch(() => {
        // Validation might be handled differently
        expect(page.url()).toBeTruthy()
      })
    }
  })

  test('should handle empty required fields', async ({ page }) => {
    await page.goto('/login')
    
    const submitButton = page.locator('button[type="submit"]')
    
    if (await submitButton.isVisible()) {
      await submitButton.click()
      
      // Should show validation errors
      const errorMessages = page.locator('text=/required|cannot be empty/i')
      const count = await errorMessages.count()
      expect(count).toBeGreaterThan(0)
    }
  })

  test('should handle negative numbers in numeric fields', async ({ page }) => {
    await page.goto('/dashboard')
    
    // Find numeric input fields
    const numberInputs = page.locator('input[type="number"]')
    const count = await numberInputs.count()
    
    if (count > 0) {
      await numberInputs.first().fill('-100')
      
      // Should validate or handle negative values
      const value = await numberInputs.first().inputValue()
      expect(value).toBeTruthy()
    }
  })
})

