import { test, expect } from '@playwright/test'

test.describe('Dashboard Visual Regression', () => {
  test.beforeEach(async ({ page }) => {
    // Mock authentication
    await page.route('**/api/proxy?service=user&path=/users/login', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          accessToken: 'mock-jwt-token-12345',
          userId: '1',
          username: 'user',
          email: 'user@mysmartwatts.com',
          role: 'ROLE_USER',
          active: true
        })
      })
    })

    await page.route('**/api/proxy?service=user&path=/users/profile', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: '1',
          email: 'user@mysmartwatts.com',
          firstName: 'User',
          lastName: 'SmartWatts',
          role: 'ROLE_USER',
          isActive: true,
          createdAt: new Date().toISOString()
        })
      })
    })

    // Login
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
    
    await page.getByLabel(/email/i).fill('user@mysmartwatts.com')
    await page.getByLabel(/password/i).fill('password')
    await page.getByRole('button', { name: /sign in|signing in/i }).click()
    
    // Wait for navigation to dashboard
    try {
      await page.waitForURL(/\/dashboard/, { timeout: 15000 })
    } catch (e) {
      await page.goto('/dashboard')
    }
    
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
  })

  test('should match dashboard screenshot', async ({ page }) => {
    // Ensure we're on dashboard
    const currentUrl = page.url()
    if (!currentUrl.includes('/dashboard')) {
      await page.goto('/dashboard')
    }
    
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(3000)
    
    // Take screenshot of entire dashboard
    await expect(page).toHaveScreenshot('dashboard-full.png', {
      fullPage: true,
      maxDiffPixels: 100
    })
  })

  test('should match dashboard widgets', async ({ page }) => {
    // Ensure we're on dashboard
    const currentUrl = page.url()
    if (!currentUrl.includes('/dashboard')) {
      await page.goto('/dashboard')
    }
    
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(3000)
    
    // Screenshot of KPI cards
    const kpiCards = page.locator('[data-testid="kpi-card"], .kpi-card, [class*="kpi"]')
    if (await kpiCards.count() > 0) {
      await expect(kpiCards.first()).toHaveScreenshot('dashboard-kpi-cards.png', {
        maxDiffPixels: 50
      })
    } else {
      // If no KPI cards found, take screenshot of page anyway
      await expect(page).toHaveScreenshot('dashboard-kpi-cards.png', {
        maxDiffPixels: 50
      })
    }
    
    // Screenshot of charts
    const charts = page.locator('canvas, [data-testid="chart"], [class*="chart"]')
    if (await charts.count() > 0) {
      await expect(charts.first()).toHaveScreenshot('dashboard-charts.png', {
        maxDiffPixels: 100
      })
    } else {
      // If no charts found, take screenshot of page anyway
      await expect(page).toHaveScreenshot('dashboard-charts.png', {
        maxDiffPixels: 100
      })
    }
  })

  test('should match dashboard loading state', async ({ page }) => {
    // Intercept and delay API calls (but not auth)
    await page.route('**/api/proxy*', async route => {
      const url = new URL(route.request().url())
      const path = url.searchParams.get('path')
      
      // Only delay dashboard data APIs, not auth
      if (path && !path.includes('/users/')) {
        await new Promise(resolve => setTimeout(resolve, 3000))
      }
      
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: [] })
      })
    })

    // Navigate to dashboard
    const currentUrl = page.url()
    if (!currentUrl.includes('/dashboard')) {
      await page.goto('/dashboard')
    }
    
    // Wait a bit for loading state to appear
    await page.waitForTimeout(500)
    
    // Capture loading state
    await expect(page).toHaveScreenshot('dashboard-loading.png', {
      maxDiffPixels: 50
    })
  })

  test('should match dashboard empty state', async ({ page }) => {
    // Mock empty data (but keep auth working)
    await page.route('**/api/proxy*', async route => {
      const url = new URL(route.request().url())
      const service = url.searchParams.get('service')
      const path = url.searchParams.get('path')
      
      // Keep auth responses working
      if (service === 'user' && path?.includes('/users/')) {
        await route.continue()
        return
      }
      
      // Return empty data for dashboard APIs
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([])
      })
    })

    // Navigate to dashboard
    const currentUrl = page.url()
    if (!currentUrl.includes('/dashboard')) {
      await page.goto('/dashboard')
    }
    
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(3000)
    
    await expect(page).toHaveScreenshot('dashboard-empty.png', {
      maxDiffPixels: 100
    })
  })
})

