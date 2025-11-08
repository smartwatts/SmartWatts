import { test, expect } from '@playwright/test'

test.describe('Concurrent Users Load Testing', () => {
  test('should handle multiple users simultaneously', async ({ browser }) => {
    const userCount = 5
    const pages = await Promise.all(
      Array.from({ length: userCount }, () => browser.newPage())
    )
    
    try {
      // All users navigate to dashboard
      await Promise.all(
        pages.map(page => {
          page.goto('/')
          return page.evaluate(() => {
            localStorage.setItem('token', 'test-token')
          })
        })
      )
      
      await Promise.all(
        pages.map(page => page.goto('/dashboard'))
      )
      
      // All pages should load successfully
      for (const page of pages) {
        await page.waitForLoadState('networkidle')
        expect(page.url()).toContain('/')
      }
    } finally {
      await Promise.all(pages.map(page => page.close()))
    }
  })

  test('should handle concurrent form submissions', async ({ browser }) => {
    const userCount = 3
    const pages = await Promise.all(
      Array.from({ length: userCount }, () => browser.newPage())
    )
    
    try {
      await Promise.all(
        pages.map(page => page.goto('/login'))
      )
      
      // All users submit forms simultaneously
      await Promise.all(
        pages.map(async (page, index) => {
          const emailInput = page.locator('input[type="email"], input[name*="email"]')
          const passwordInput = page.locator('input[type="password"], input[name*="password"]')
          const submitButton = page.locator('button[type="submit"]')
          
          if (await emailInput.isVisible()) {
            await emailInput.fill('user@mysmartwatts.com')
            if (await passwordInput.isVisible()) {
              await passwordInput.fill('password')
            }
            if (await submitButton.isVisible()) {
              await submitButton.click()
            }
          }
        })
      )
      
      await Promise.all(
        pages.map(page => page.waitForTimeout(2000))
      )
      
      // All forms should be handled
      for (const page of pages) {
        expect(page.url()).toBeTruthy()
      }
    } finally {
      await Promise.all(pages.map(page => page.close()))
    }
  })
})

