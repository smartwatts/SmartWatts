import { test, expect } from '@playwright/test'

test.describe('Billing Calculations', () => {
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

  test('should display billing page', async ({ page }) => {
    await page.goto('/billing')
    await expect(page.getByText(/billing|bill/i)).toBeVisible()
  })

  test('should calculate MYTO tariffs correctly', async ({ page }) => {
    await page.route('**/api/proxy/**/billing/**', route => {
      route.fulfill({
        status: 200,
        body: JSON.stringify({
          consumption: 1000,
          tariff: 50,
          total: 50000,
          breakdown: {
            grid: 30000,
            solar: 15000,
            generator: 5000
          }
        })
      })
    })

    await page.goto('/billing')
    await expect(page.getByText(/50000|50,000/i)).toBeVisible()
  })

  test('should display bill history', async ({ page }) => {
    await page.route('**/api/proxy/**/bills/**', route => {
      route.fulfill({
        status: 200,
        body: JSON.stringify([
          {
            id: 'bill-1',
            amount: 50000,
            period: '2024-01',
            status: 'PAID'
          }
        ])
      })
    })

    await page.goto('/billing')
    await expect(page.getByText(/bill|history/i)).toBeVisible()
  })

  test('should display cost breakdown by source', async ({ page }) => {
    await page.route('**/api/proxy/**/billing/**', route => {
      route.fulfill({
        status: 200,
        body: JSON.stringify({
          breakdown: {
            grid: 30000,
            solar: 15000,
            generator: 5000
          }
        })
      })
    })

    await page.goto('/billing')
    await expect(page.getByText(/grid|solar|generator/i)).toBeVisible()
  })
})

