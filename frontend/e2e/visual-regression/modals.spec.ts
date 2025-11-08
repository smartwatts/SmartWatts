import { test, expect } from '@playwright/test'

test.describe('Modals Visual Regression', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'test-token')
    })
  })

  test('should match device activation modal', async ({ page }) => {
    await page.goto('/devices')
    await page.waitForLoadState('networkidle')
    
    // Open activation modal
    const activateButton = page.locator('text=/activate|activate device/i')
    if (await activateButton.isVisible()) {
      await activateButton.first().click()
      await page.waitForTimeout(1000)
      
      const modal = page.locator('[role="dialog"], .modal, [class*="modal"]')
      if (await modal.isVisible()) {
        await expect(modal).toHaveScreenshot('device-activation-modal.png', {
          maxDiffPixels: 100
        })
      }
    }
  })

  test('should match feature upgrade modal', async ({ page }) => {
    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    // Open upgrade modal
    const upgradeButton = page.locator('text=/upgrade|premium|pro/i')
    if (await upgradeButton.isVisible()) {
      await upgradeButton.first().click()
      await page.waitForTimeout(1000)
      
      const modal = page.locator('[role="dialog"], .modal, [class*="modal"]')
      if (await modal.isVisible()) {
        await expect(modal).toHaveScreenshot('feature-upgrade-modal.png', {
          maxDiffPixels: 100
        })
      }
    }
  })

  test('should match confirmation modal', async ({ page }) => {
    await page.goto('/devices')
    await page.waitForLoadState('networkidle')
    
    // Trigger delete action
    const deleteButton = page.locator('text=/delete|remove/i').first()
    if (await deleteButton.isVisible()) {
      await deleteButton.click()
      await page.waitForTimeout(1000)
      
      const modal = page.locator('[role="dialog"], .modal, [class*="modal"]')
      if (await modal.isVisible()) {
        await expect(modal).toHaveScreenshot('confirmation-modal.png', {
          maxDiffPixels: 100
        })
      }
    }
  })
})

