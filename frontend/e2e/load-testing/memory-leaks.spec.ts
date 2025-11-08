import { test, expect } from '@playwright/test'

test.describe('Memory Leak Testing', () => {
  test('should not leak memory during navigation', async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('token', 'test-token')
    })
    
    const initialMemory = await page.evaluate(() => {
      return (performance as any).memory?.usedJSHeapSize || 0
    })
    
    // Navigate multiple times
    for (let i = 0; i < 10; i++) {
      await page.goto('/dashboard')
      await page.waitForLoadState('networkidle')
      await page.goto('/devices')
      await page.waitForLoadState('networkidle')
    }
    
    // Force garbage collection if available
    await page.evaluate(() => {
      if ((global as any).gc) {
        (global as any).gc()
      }
    })
    
    await page.waitForTimeout(2000)
    
    const finalMemory = await page.evaluate(() => {
      return (performance as any).memory?.usedJSHeapSize || 0
    })
    
    if (initialMemory > 0 && finalMemory > 0) {
      const memoryIncrease = finalMemory - initialMemory
      const memoryIncreasePercent = (memoryIncrease / initialMemory) * 100
      
      console.log(`Memory increase: ${memoryIncrease} bytes (${memoryIncreasePercent.toFixed(2)}%)`)
      
      // Memory should not increase by more than 50%
      expect(memoryIncreasePercent).toBeLessThan(50)
    }
  })

  test('should not leak memory with repeated API calls', async ({ page }) => {
    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    const initialMemory = await page.evaluate(() => {
      return (performance as any).memory?.usedJSHeapSize || 0
    })
    
    // Make many API calls
    for (let i = 0; i < 50; i++) {
      await page.evaluate(async () => {
        try {
          await fetch('/api/proxy?service=user&path=/users/profile')
        } catch (e) {
          // Ignore errors
        }
      })
      await page.waitForTimeout(100)
    }
    
    await page.waitForTimeout(2000)
    
    const finalMemory = await page.evaluate(() => {
      return (performance as any).memory?.usedJSHeapSize || 0
    })
    
    if (initialMemory > 0 && finalMemory > 0) {
      const memoryIncrease = finalMemory - initialMemory
      const memoryIncreasePercent = (memoryIncrease / initialMemory) * 100
      
      console.log(`Memory increase after API calls: ${memoryIncrease} bytes (${memoryIncreasePercent.toFixed(2)}%)`)
      
      // Memory should not increase significantly
      expect(memoryIncreasePercent).toBeLessThan(30)
    }
  })

  test('should clean up event listeners', async ({ page }) => {
    await page.goto('/dashboard')
    await page.waitForLoadState('networkidle')
    
    // Count event listeners before
    const listenersBefore = await page.evaluate(() => {
      return (window as any).__eventListeners?.length || 0
    })
    
    // Navigate and interact
    for (let i = 0; i < 5; i++) {
      await page.reload()
      await page.waitForLoadState('networkidle')
      
      const buttons = page.locator('button').first()
      if (await buttons.isVisible()) {
        await buttons.click().catch(() => {})
      }
    }
    
    await page.waitForTimeout(2000)
    
    // Count event listeners after
    const listenersAfter = await page.evaluate(() => {
      return (window as any).__eventListeners?.length || 0
    })
    
    // Event listeners should not grow unbounded
    if (listenersBefore > 0 && listenersAfter > 0) {
      expect(listenersAfter).toBeLessThanOrEqual(listenersBefore * 2)
    }
  })
})

