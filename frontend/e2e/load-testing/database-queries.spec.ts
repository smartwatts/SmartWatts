import { test, expect } from '@playwright/test'

test.describe('Database Query Performance Testing', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'test-token')
    })
  })

  test('should measure query response times', async ({ page }) => {
    const queryTimes: number[] = []
    
    page.on('response', response => {
      if (response.url().includes('/api/proxy')) {
        const timing = response.timing()
        const queryTime = timing.responseEnd - timing.requestStart
        queryTimes.push(queryTime)
      }
    })
    
    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    if (queryTimes.length > 0) {
      const avgQueryTime = queryTimes.reduce((a, b) => a + b, 0) / queryTimes.length
      const maxQueryTime = Math.max(...queryTimes)
      
      console.log(`Average query time: ${avgQueryTime}ms`)
      console.log(`Max query time: ${maxQueryTime}ms`)
      
      // Average should be under 500ms
      expect(avgQueryTime).toBeLessThan(500)
      // Max should be under 2 seconds
      expect(maxQueryTime).toBeLessThan(2000)
    }
  })

  test('should handle complex queries efficiently', async ({ page }) => {
    const startTime = Date.now()
    
    await page.goto('/dashboard')
    
    // Wait for all data to load
    await page.waitForLoadState('networkidle')
    
    // Check for data visualization (charts, tables)
    const charts = page.locator('canvas, [data-testid="chart"]')
    const tables = page.locator('table, [data-testid="table"]')
    
    await Promise.all([
      charts.first().waitFor({ timeout: 5000 }).catch(() => {}),
      tables.first().waitFor({ timeout: 5000 }).catch(() => {})
    ])
    
    const loadTime = Date.now() - startTime
    
    console.log(`Complex query load time: ${loadTime}ms`)
    
    // Should load within 5 seconds
    expect(loadTime).toBeLessThan(5000)
  })

  test('should handle paginated queries', async ({ page }) => {
    await page.goto('/devices')
    await page.waitForLoadState('networkidle')
    
    const queryTimes: number[] = []
    
    page.on('response', response => {
      if (response.url().includes('/api/proxy') && response.url().includes('page')) {
        const timing = response.timing()
        queryTimes.push(timing.responseEnd - timing.requestStart)
      }
    })
    
    // Navigate through pages
    const nextButton = page.locator('text=/next|>|next page/i')
    for (let i = 0; i < 3 && await nextButton.isVisible(); i++) {
      await nextButton.click()
      await page.waitForTimeout(1000)
    }
    
    if (queryTimes.length > 0) {
      const avgQueryTime = queryTimes.reduce((a, b) => a + b, 0) / queryTimes.length
      console.log(`Average paginated query time: ${avgQueryTime}ms`)
      
      // Paginated queries should be fast
      expect(avgQueryTime).toBeLessThan(300)
    }
  })
})

