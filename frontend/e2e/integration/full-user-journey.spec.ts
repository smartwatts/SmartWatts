import { test, expect } from '@playwright/test'

test.describe('Complete User Journey', () => {
  test('should complete full user journey from registration to billing', async ({ page }) => {
    // Step 1: Registration
    await page.goto('/register')
    await page.route('**/api/proxy?service=user&path=/users/register', route => {
      route.fulfill({
        status: 200,
        body: JSON.stringify({
          accessToken: 'test-token',
          user: {
            id: 'user-123',
            email: 'newuser@mysmartwatts.com',
            firstName: 'New',
            lastName: 'User',
            role: 'ROLE_USER',
            isActive: true
          }
        })
      })
    })

    await page.getByLabel(/email/i).fill('newuser@mysmartwatts.com')
    await page.getByLabel(/password/i).fill('Password123!')
    await page.getByLabel(/first name/i).fill('New')
    await page.getByLabel(/last name/i).fill('User')
    await page.getByLabel(/phone number/i).fill('1234567890')
    await page.getByLabel(/address/i).fill('123 Main St')
    await page.getByLabel(/city/i).fill('Lagos')
    await page.getByLabel(/state/i).fill('Lagos')
    await page.getByLabel(/country/i).fill('Nigeria')
    await page.getByRole('button', { name: /register|sign up/i }).click()

    await expect(page).toHaveURL(/dashboard/)

    // Step 2: Add Device
    await page.goto('/devices')
    await page.route('**/api/proxy/device-service/api/v1/devices', route => {
      route.fulfill({
        status: 201,
        body: JSON.stringify({
          id: 'device-123',
          name: 'Test Smart Meter',
          deviceId: 'SW_001',
          deviceType: 'SMART_METER',
          protocol: 'MQTT',
          status: 'ONLINE'
        })
      })
    })

    await page.getByRole('button', { name: /add.*device/i }).click()
    await page.getByLabel(/device name/i).fill('Test Smart Meter')
    await page.getByLabel(/device id/i).fill('SW_001')
    await page.getByLabel(/manufacturer/i).fill('SmartWatts')
    await page.getByLabel(/device type/i).selectOption('SMART_METER')
    await page.getByLabel(/protocol/i).selectOption('MQTT')
    await page.getByRole('button', { name: /add.*device/i }).click()

    await expect(page.getByText('Test Smart Meter')).toBeVisible()

    // Step 3: View Dashboard
    await page.goto('/dashboard')
    await expect(page.getByText(/dashboard|welcome/i)).toBeVisible()

    // Step 4: View Energy Data
    await page.goto('/energy')
    await expect(page.getByText(/energy|consumption/i)).toBeVisible()

    // Step 5: View Analytics
    await page.goto('/analytics')
    await expect(page.getByText(/analytics|insights/i)).toBeVisible()

    // Step 6: View Billing
    await page.goto('/billing')
    await expect(page.getByText(/billing|bill/i)).toBeVisible()

    // Step 7: Logout
    await page.getByRole('button', { name: /logout|sign out/i }).click()
    await expect(page).toHaveURL(/login/)
  })
})

