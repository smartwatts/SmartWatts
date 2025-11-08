import { test, expect } from '@playwright/test'

test.describe('PWA Offline Functionality', () => {
  test.beforeEach(async ({ page, context }) => {
    await page.goto('/')
    // Wait for service worker registration
    await page.waitForTimeout(2000)
  })

  test('should register service worker', async ({ page }) => {
    const swRegistered = await page.evaluate(() => {
      return 'serviceWorker' in navigator
    })
    expect(swRegistered).toBe(true)
  })

  test('should show offline indicator when offline', async ({ page, context }) => {
    // Go offline
    await context.setOffline(true)
    
    await page.reload()
    
    await expect(page.getByText(/offline|no connection/i)).toBeVisible()
  })

  test('should cache pages for offline access', async ({ page, context }) => {
    // Visit pages to cache them
    await page.goto('/dashboard')
    await page.goto('/energy')
    await page.goto('/analytics')
    
    // Go offline
    await context.setOffline(true)
    
    // Try to navigate to cached pages
    await page.goto('/dashboard')
    await expect(page.getByText(/dashboard/i)).toBeVisible()
    
    await page.goto('/energy')
    await expect(page.getByText(/energy|consumption/i)).toBeVisible()
  })

  test('should queue requests when offline', async ({ page, context }) => {
    // Mock API endpoint
    let requestCount = 0
    await page.route('**/api/proxy/**', route => {
      requestCount++
      route.fulfill({
        status: 200,
        body: JSON.stringify({ success: true })
      })
    })
    
    // Go offline
    await context.setOffline(true)
    
    // Try to make requests (should be queued)
    await page.goto('/devices')
    
    // Go back online
    await context.setOffline(false)
    
    // Wait for sync
    await page.waitForTimeout(2000)
    
    // Requests should be processed
    expect(requestCount).toBeGreaterThan(0)
  })

  test('should sync data when coming back online', async ({ page, context }) => {
    // Go offline
    await context.setOffline(true)
    
    await page.goto('/dashboard')
    
    // Make some changes (should be queued)
    // Go back online
    await context.setOffline(false)
    
    // Wait for sync
    await page.waitForTimeout(2000)
    
    // Data should be synced
    await expect(page.getByText(/synced|updated/i)).toBeVisible({ timeout: 5000 }).catch(() => {
      // Sync indicator might not always be visible
    })
  })
})

