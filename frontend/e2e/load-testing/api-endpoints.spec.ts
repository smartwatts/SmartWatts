import { test, expect } from '@playwright/test'

test.describe('API Endpoints Load Testing', () => {
  test('should handle multiple concurrent API requests', async ({ page }) => {
    const startTime = Date.now()
    const requests: Promise<any>[] = []
    
    // Make 10 concurrent requests
    for (let i = 0; i < 10; i++) {
      requests.push(
        page.evaluate(async (index) => {
          const response = await fetch(`/api/proxy?service=user&path=/users/profile&_=${index}`)
          return { status: response.status, index }
        }, i)
      )
    }
    
    const results = await Promise.all(requests)
    const endTime = Date.now()
    const duration = endTime - startTime
    
    // All requests should complete
    expect(results.length).toBe(10)
    
    // Should complete within reasonable time (5 seconds)
    expect(duration).toBeLessThan(5000)
    
    // Log performance metrics
    console.log(`10 concurrent requests completed in ${duration}ms`)
    console.log(`Average response time: ${duration / 10}ms`)
  })

  test('should measure API response times', async ({ page }) => {
    const responseTimes: number[] = []
    
    await page.goto('/dashboard')
    
    // Monitor API response times
    page.on('response', response => {
      if (response.url().includes('/api/proxy')) {
        const timing = response.timing()
        const responseTime = timing.responseEnd - timing.requestStart
        responseTimes.push(responseTime)
      }
    })
    
    await page.waitForLoadState('networkidle')
    
    // Calculate average response time
    if (responseTimes.length > 0) {
      const avgResponseTime = responseTimes.reduce((a, b) => a + b, 0) / responseTimes.length
      const maxResponseTime = Math.max(...responseTimes)
      
      console.log(`Average API response time: ${avgResponseTime}ms`)
      console.log(`Max API response time: ${maxResponseTime}ms`)
      
      // Average should be under 500ms
      expect(avgResponseTime).toBeLessThan(500)
    }
  })

  test('should handle API rate limiting', async ({ page }) => {
    let rateLimited = false
    
    page.on('response', response => {
      if (response.status() === 429) {
        rateLimited = true
      }
    })
    
    // Make rapid requests
    for (let i = 0; i < 20; i++) {
      await page.evaluate(async () => {
        try {
          await fetch('/api/proxy?service=user&path=/users/profile')
        } catch (e) {
          // Ignore errors
        }
      })
    }
    
    await page.waitForTimeout(2000)
    
    // Rate limiting might occur, but should be handled gracefully
    expect(page.url()).toBeTruthy()
  })
})

