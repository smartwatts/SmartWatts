import { test, expect } from '@playwright/test'

test.describe('Household Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    // Mock authentication
    await page.goto('/login')
    await page.route('**/api/proxy?service=user&path=/users/login', route => {
      route.fulfill({
        status: 200,
        body: JSON.stringify({
          accessToken: 'test-token',
          userId: 'user-123',
          username: 'user',
          email: 'user@mysmartwatts.com',
          role: 'ROLE_USER',
          active: true
        })
      })
    })
    await page.getByLabel(/email/i).fill('user@mysmartwatts.com')
    await page.getByLabel(/password/i).fill('password')
    await page.getByRole('button', { name: /sign in/i }).click()
    await page.waitForURL(/dashboard/)
  })

  test('should display household dashboard', async ({ page }) => {
    await page.goto('/dashboard/household')
    await expect(page.getByText(/household|dashboard/i)).toBeVisible()
  })

  test('should display energy consumption widget', async ({ page }) => {
    await page.route('**/api/proxy/**/energy/**', route => {
      route.fulfill({
        status: 200,
        body: JSON.stringify({
          consumption: 1000,
          cost: 5000,
          timestamp: new Date().toISOString()
        })
      })
    })

    await page.goto('/dashboard/household')
    await expect(page.getByText(/energy|consumption/i)).toBeVisible()
  })

  test('should display appliance recognition widget', async ({ page }) => {
    await page.route('**/api/proxy/**/appliance/**', route => {
      route.fulfill({
        status: 200,
        body: JSON.stringify([])
      })
    })

    await page.goto('/dashboard/household')
    await expect(page.getByText(/appliance|recognition/i)).toBeVisible()
  })

  test('should switch between time periods', async ({ page }) => {
    await page.goto('/dashboard/household')
    
    // Test daily view
    await page.getByRole('button', { name: /daily|today/i }).click()
    await expect(page.getByText(/daily|today/i)).toBeVisible()
    
    // Test weekly view
    await page.getByRole('button', { name: /weekly|week/i }).click()
    await expect(page.getByText(/weekly|week/i)).toBeVisible()
    
    // Test monthly view
    await page.getByRole('button', { name: /monthly|month/i }).click()
    await expect(page.getByText(/monthly|month/i)).toBeVisible()
  })
})

