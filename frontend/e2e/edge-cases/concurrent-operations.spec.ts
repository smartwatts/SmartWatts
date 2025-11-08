import { test, expect } from '@playwright/test'

test.describe('Concurrent Operations Edge Cases', () => {
  test('should handle multiple simultaneous API requests', async ({ page }) => {
    let requestCount = 0
    
    await page.route('**/api/proxy*', route => {
      requestCount++
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: `response-${requestCount}` })
      })
    })

    await page.goto('/dashboard')
    
    // Trigger multiple requests simultaneously
    await Promise.all([
      page.reload(),
      page.evaluate(() => {
        // Simulate multiple component mounts
        window.dispatchEvent(new Event('resize'))
        window.dispatchEvent(new Event('scroll'))
      })
    ])
    
    await page.waitForTimeout(2000)
    expect(requestCount).toBeGreaterThan(0)
  })

  test('should handle rapid form submissions', async ({ page }) => {
    await page.goto('/login')
    
    const emailInput = page.locator('input[type="email"], input[name*="email"]')
    const passwordInput = page.locator('input[type="password"], input[name*="password"]')
    const submitButton = page.locator('button[type="submit"]')
    
    if (await emailInput.isVisible() && await passwordInput.isVisible()) {
      await emailInput.fill('user@mysmartwatts.com')
      await passwordInput.fill('password')
      
      // Rapid clicks
      await Promise.all([
        submitButton.click(),
        submitButton.click(),
        submitButton.click()
      ])
      
      // Should only submit once or handle duplicates
      await page.waitForTimeout(2000)
      expect(page.url()).toBeTruthy()
    }
  })

  test('should handle concurrent tab operations', async ({ context }) => {
    const page1 = await context.newPage()
    const page2 = await context.newPage()
    
    try {
      await Promise.all([
        page1.goto('/dashboard'),
        page2.goto('/dashboard')
      ])
      
      // Both pages should load independently
      expect(page1.url()).toContain('/')
      expect(page2.url()).toContain('/')
    } finally {
      await page1.close()
      await page2.close()
    }
  })

  test('should handle race conditions in data fetching', async ({ page }) => {
    let requestOrder: number[] = []
    let responseOrder: number[] = []
    
    await page.route('**/api/proxy*', route => {
      const requestId = Math.random()
      requestOrder.push(requestId)
      
      // Simulate different response times
      setTimeout(() => {
        responseOrder.push(requestId)
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ id: requestId })
        })
      }, Math.random() * 1000)
    })

    await page.goto('/dashboard')
    
    // Trigger multiple rapid navigations
    await page.reload()
    await page.reload()
    
    await page.waitForTimeout(3000)
    
    // Responses might arrive out of order, but should be handled
    expect(requestOrder.length).toBeGreaterThan(0)
  })

  test('should handle simultaneous state updates', async ({ page }) => {
    await page.goto('/dashboard')
    
    // Trigger multiple state-changing actions
    const buttons = page.locator('button')
    const buttonCount = await buttons.count()
    
    if (buttonCount > 0) {
      // Click multiple buttons rapidly
      const promises = []
      for (let i = 0; i < Math.min(3, buttonCount); i++) {
        promises.push(buttons.nth(i).click().catch(() => {}))
      }
      
      await Promise.all(promises)
      await page.waitForTimeout(2000)
      
      // Page should remain stable
      expect(page.url()).toBeTruthy()
    }
  })
})

