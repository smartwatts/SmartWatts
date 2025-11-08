import { test, expect } from '@playwright/test'

test.describe('Device Registration', () => {
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
    
    await page.goto('/devices')
  })

  test('should register Smart Meter device successfully', async ({ page }) => {
    // Mock device registration
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

    await page.getByRole('button', { name: /add.*device|new.*device/i }).click()
    await page.getByLabel(/device name/i).fill('Test Smart Meter')
    await page.getByLabel(/device id/i).fill('SW_001')
    await page.getByLabel(/manufacturer/i).fill('SmartWatts')
    await page.getByLabel(/device type/i).selectOption('SMART_METER')
    await page.getByLabel(/protocol/i).selectOption('MQTT')
    await page.getByRole('button', { name: /add.*device|submit/i }).click()

    await expect(page.getByText(/success|device.*added/i)).toBeVisible()
    await expect(page.getByText('Test Smart Meter')).toBeVisible()
  })

  test('should register Solar Inverter device', async ({ page }) => {
    await page.route('**/api/proxy/device-service/api/v1/devices', route => {
      route.fulfill({
        status: 201,
        body: JSON.stringify({
          id: 'device-124',
          name: 'Test Solar Inverter',
          deviceId: 'SW_002',
          deviceType: 'SOLAR_INVERTER',
          protocol: 'MODBUS_TCP',
          status: 'ONLINE'
        })
      })
    })

    await page.getByRole('button', { name: /add.*device|new.*device/i }).click()
    await page.getByLabel(/device name/i).fill('Test Solar Inverter')
    await page.getByLabel(/device id/i).fill('SW_002')
    await page.getByLabel(/manufacturer/i).fill('SmartWatts')
    await page.getByLabel(/device type/i).selectOption('SOLAR_INVERTER')
    await page.getByLabel(/protocol/i).selectOption('MODBUS_TCP')
    await page.getByRole('button', { name: /add.*device|submit/i }).click()

    await expect(page.getByText(/success|device.*added/i)).toBeVisible()
  })

  test('should show verification required for non-OEM devices', async ({ page }) => {
    await page.getByRole('button', { name: /add.*device|new.*device/i }).click()
    await page.getByLabel(/device name/i).fill('Other Device')
    await page.getByLabel(/device id/i).fill('OTHER_001')
    await page.getByLabel(/manufacturer/i).fill('Other Manufacturer')
    await page.getByLabel(/device type/i).selectOption('SMART_METER')
    await page.getByLabel(/protocol/i).selectOption('MQTT')

    await expect(page.getByText(/verification.*required|unverified/i)).toBeVisible()
    await expect(page.getByLabel(/sample payload/i)).toBeVisible()
  })
})

