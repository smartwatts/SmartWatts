import { test, expect } from '@playwright/test'

test.describe('Device Management Visual Regression', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'test-token')
    })
  })

  test('should match device list page', async ({ page }) => {
    await page.goto('/devices')
    await page.waitForLoadState('networkidle')
    
    await expect(page).toHaveScreenshot('device-list.png', {
      fullPage: true,
      maxDiffPixels: 100
    })
  })

  test('should match device registration form', async ({ page }) => {
    await page.goto('/devices')
    await page.waitForLoadState('networkidle')
    
    // Open device registration modal
    const addButton = page.locator('text=/add device|register device|new device/i')
    if (await addButton.isVisible()) {
      await addButton.click()
      await page.waitForTimeout(1000)
      
      const modal = page.locator('[role="dialog"], .modal, [class*="modal"]')
      if (await modal.isVisible()) {
        await expect(modal).toHaveScreenshot('device-registration-form.png', {
          maxDiffPixels: 100
        })
      }
    }
  })

  test('should match device details page', async ({ page }) => {
    await page.goto('/devices')
    await page.waitForLoadState('networkidle')
    
    // Click on first device if available
    const deviceCard = page.locator('[data-testid="device-card"], .device-card').first()
    if (await deviceCard.isVisible()) {
      await deviceCard.click()
      await page.waitForLoadState('networkidle')
      
      await expect(page).toHaveScreenshot('device-details.png', {
        fullPage: true,
        maxDiffPixels: 100
      })
    }
  })

  test('should match device status indicators', async ({ page }) => {
    await page.goto('/devices')
    await page.waitForLoadState('networkidle')
    
    // Screenshot device status badges
    const statusBadges = page.locator('[data-testid="device-status"], .status-badge, [class*="status"]')
    if (await statusBadges.count() > 0) {
      await expect(statusBadges.first()).toHaveScreenshot('device-status-badge.png', {
        maxDiffPixels: 50
      })
    }
  })
})

