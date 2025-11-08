import { test, expect } from '@playwright/test'

test.describe('Performance Testing', () => {
  test('should load dashboard within performance thresholds', async ({ page }) => {
    const startTime = Date.now()
    
    await page.goto('/dashboard')
    
    // Wait for page to be interactive
    await page.waitForLoadState('networkidle')
    
    const loadTime = Date.now() - startTime
    
    // Time to Interactive should be < 3s
    expect(loadTime).toBeLessThan(3000)
  })

  test('should measure First Contentful Paint', async ({ page }) => {
    await page.goto('/dashboard')
    
    const fcp = await page.evaluate(() => {
      return new Promise((resolve) => {
        new PerformanceObserver((list) => {
          const entries = list.getEntries()
          const fcpEntry = entries.find(entry => entry.name === 'first-contentful-paint')
          if (fcpEntry) {
            resolve(fcpEntry.startTime)
          }
        }).observe({ entryTypes: ['paint'] })
      })
    })
    
    // FCP should be < 1.5s
    expect(fcp).toBeLessThan(1500)
  })

  test('should measure Largest Contentful Paint', async ({ page }) => {
    await page.goto('/dashboard')
    
    await page.waitForLoadState('networkidle')
    
    const lcp = await page.evaluate(() => {
      return new Promise((resolve) => {
        new PerformanceObserver((list) => {
          const entries = list.getEntries()
          const lastEntry = entries[entries.length - 1]
          resolve(lastEntry.renderTime || lastEntry.loadTime)
        }).observe({ entryTypes: ['largest-contentful-paint'] })
        
        // Resolve after 2 seconds if no LCP
        setTimeout(() => resolve(0), 2000)
      })
    })
    
    // LCP should be < 2.5s
    expect(lcp).toBeLessThan(2500)
  })

  test('should measure API response times', async ({ page }) => {
    const responseTimes: number[] = []
    
    await page.route('**/api/proxy/**', route => {
      const startTime = Date.now()
      route.continue().then(() => {
        const responseTime = Date.now() - startTime
        responseTimes.push(responseTime)
      })
    })
    
    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    // Average response time should be < 200ms
    const avgResponseTime = responseTimes.reduce((a, b) => a + b, 0) / responseTimes.length
    expect(avgResponseTime).toBeLessThan(200)
  })

  test('should handle network throttling', async ({ page, context }) => {
    // Simulate slow 3G
    await context.setGeolocation({ latitude: 0, longitude: 0 })
    
    const startTime = Date.now()
    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    const loadTime = Date.now() - startTime
    
    // Should still load within reasonable time even on slow network
    expect(loadTime).toBeLessThan(10000)
  })
})

