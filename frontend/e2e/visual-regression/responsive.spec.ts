import { test, expect } from '@playwright/test'

test.describe('Responsive Design Visual Regression', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'test-token')
    })
  })

  test('should match mobile viewport', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 }) // iPhone SE
    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    await expect(page).toHaveScreenshot('dashboard-mobile.png', {
      fullPage: true,
      maxDiffPixels: 100
    })
  })

  test('should match tablet viewport', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 }) // iPad
    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    await expect(page).toHaveScreenshot('dashboard-tablet.png', {
      fullPage: true,
      maxDiffPixels: 100
    })
  })

  test('should match desktop viewport', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 }) // Desktop
    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    await expect(page).toHaveScreenshot('dashboard-desktop.png', {
      fullPage: true,
      maxDiffPixels: 100
    })
  })

  test('should match large desktop viewport', async ({ page }) => {
    await page.setViewportSize({ width: 2560, height: 1440 }) // Large Desktop
    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    await expect(page).toHaveScreenshot('dashboard-large-desktop.png', {
      fullPage: true,
      maxDiffPixels: 100
    })
  })

  test('should match landscape mobile viewport', async ({ page }) => {
    await page.setViewportSize({ width: 667, height: 375 }) // Landscape iPhone
    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    await expect(page).toHaveScreenshot('dashboard-landscape-mobile.png', {
      fullPage: true,
      maxDiffPixels: 100
    })
  })
})

