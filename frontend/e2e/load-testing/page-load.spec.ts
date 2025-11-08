import { test, expect } from '@playwright/test'

test.describe('Page Load Performance Testing', () => {
  test('should measure dashboard load time', async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'test-token')
    })
    
    const startTime = Date.now()
    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    const loadTime = Date.now() - startTime
    
    console.log(`Dashboard load time: ${loadTime}ms`)
    
    // Should load within 3 seconds
    expect(loadTime).toBeLessThan(3000)
  })

  test('should measure time to interactive', async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'test-token')
    })
    
    const navigationStart = await page.evaluate(() => performance.timing.navigationStart)
    
    await page.goto('/dashboard')
    
    // Wait for interactive elements
    await page.waitForSelector('button, a, input', { timeout: 5000 })
    
    const timeToInteractive = await page.evaluate((navStart) => {
      return performance.now() - (performance.timing.navigationStart - navStart)
    }, navigationStart)
    
    console.log(`Time to interactive: ${timeToInteractive}ms`)
    
    // Should be interactive within 2 seconds
    expect(timeToInteractive).toBeLessThan(2000)
  })

  test('should measure first contentful paint', async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'test-token')
    })
    
    await page.goto('/dashboard')
    
    const fcp = await page.evaluate(() => {
      const paintEntries = performance.getEntriesByType('paint')
      const fcpEntry = paintEntries.find(entry => entry.name === 'first-contentful-paint')
      return fcpEntry ? fcpEntry.startTime : null
    })
    
    if (fcp) {
      console.log(`First Contentful Paint: ${fcp}ms`)
      // Should paint within 1.5 seconds
      expect(fcp).toBeLessThan(1500)
    }
  })

  test('should measure largest contentful paint', async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'test-token')
    })
    
    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    const lcp = await page.evaluate(() => {
      return new Promise((resolve) => {
        new PerformanceObserver((list) => {
          const entries = list.getEntries()
          const lastEntry = entries[entries.length - 1]
          resolve(lastEntry.startTime)
        }).observe({ entryTypes: ['largest-contentful-paint'] })
        
        setTimeout(() => resolve(null), 5000)
      })
    })
    
    if (lcp) {
      console.log(`Largest Contentful Paint: ${lcp}ms`)
      // Should paint within 2.5 seconds
      expect(lcp).toBeLessThan(2500)
    }
  })
})

