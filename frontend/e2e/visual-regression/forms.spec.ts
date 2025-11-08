import { test, expect } from '@playwright/test'

test.describe('Forms Visual Regression', () => {
  test('should match login form', async ({ page }) => {
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
    
    await expect(page).toHaveScreenshot('login-form.png', {
      fullPage: true,
      maxDiffPixels: 100
    })
  })

  test('should match registration form', async ({ page }) => {
    await page.goto('/register')
    await page.waitForLoadState('networkidle')
    
    await expect(page).toHaveScreenshot('registration-form.png', {
      fullPage: true,
      maxDiffPixels: 100
    })
  })

  test('should match form validation errors', async ({ page }) => {
    await page.goto('/login')
    
    // Trigger validation
    const submitButton = page.locator('button[type="submit"]')
    if (await submitButton.isVisible()) {
      await submitButton.click()
      await page.waitForTimeout(500)
      
      await expect(page).toHaveScreenshot('form-validation-errors.png', {
        maxDiffPixels: 100
      })
    }
  })

  test('should match form with filled data', async ({ page }) => {
    await page.goto('/login')
    
    const emailInput = page.locator('input[type="email"], input[name*="email"]')
    const passwordInput = page.locator('input[type="password"], input[name*="password"]')
    
    if (await emailInput.isVisible()) {
      await emailInput.fill('user@mysmartwatts.com')
      if (await passwordInput.isVisible()) {
        await passwordInput.fill('password')
      }
      
      await expect(page).toHaveScreenshot('form-filled.png', {
        maxDiffPixels: 100
      })
    }
  })
})

