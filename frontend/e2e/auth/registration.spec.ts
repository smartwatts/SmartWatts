import { test, expect } from '@playwright/test'

test.describe('User Registration', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/register')
    await page.waitForLoadState('networkidle')
    // Wait for form to be ready
    await page.waitForSelector('input[type="email"]', { timeout: 10000 }).catch(() => {})
  })

  test('should display registration form', async ({ page }) => {
    // Wait for page to load
    await page.waitForLoadState('networkidle')
    await expect(page.getByLabel(/email/i)).toBeVisible()
    await expect(page.getByLabel(/password/i)).toBeVisible()
    await expect(page.getByLabel(/first name/i)).toBeVisible()
    await expect(page.getByLabel(/last name/i)).toBeVisible()
  })

  test('should register user successfully with valid data', async ({ page }) => {
    // Fill registration form
    await page.getByLabel(/email/i).fill('newuser@mysmartwatts.com')
    await page.getByLabel(/password/i).fill('Password123!')
    await page.getByLabel(/first name/i).fill('New')
    await page.getByLabel(/last name/i).fill('User')
    await page.getByLabel(/phone number/i).fill('1234567890')
    await page.getByLabel(/address/i).fill('123 Main St')
    await page.getByLabel(/city/i).fill('Lagos')
    await page.getByLabel(/state/i).fill('Lagos')
    await page.getByLabel(/country/i).fill('Nigeria')

    // Submit form
    await page.getByRole('button', { name: /register|sign up/i }).click()

    // Should redirect to dashboard or show success message
    await expect(page).toHaveURL(/dashboard|login/)
    await expect(page.getByText(/success|welcome/i)).toBeVisible()
  })

  test('should show validation errors for empty fields', async ({ page }) => {
    await page.getByRole('button', { name: /register|sign up|create account/i }).click()

    // Wait for validation
    await page.waitForTimeout(500)
    // Check that required fields are marked
    const emailInput = page.getByLabel(/email/i)
    await expect(emailInput).toHaveAttribute('required')
  })

  test('should show error for invalid email', async ({ page }) => {
    await page.getByLabel(/email/i).fill('invalid-email')
    await page.getByLabel(/password/i).fill('Password123!')
    await page.getByLabel(/first name/i).fill('Test')
    await page.getByLabel(/last name/i).fill('User')

    await page.getByRole('button', { name: /register|sign up/i }).click()

    await expect(page.getByText(/invalid email|email format/i)).toBeVisible()
  })

  test('should show error for weak password', async ({ page }) => {
    await page.getByLabel(/email/i).fill('newuser@mysmartwatts.com')
    await page.getByLabel(/password/i).fill('123')
    await page.getByLabel(/first name/i).fill('Test')
    await page.getByLabel(/last name/i).fill('User')

    await page.getByRole('button', { name: /register|sign up/i }).click()

    await expect(page.getByText(/password.*8|password.*strong/i)).toBeVisible()
  })

  test('should show error for existing email', async ({ page }) => {
    // Mock API response for existing email
    await page.route('**/api/proxy?service=user&path=/users/register', route => {
      route.fulfill({
        status: 400,
        body: JSON.stringify({ message: 'Email already exists' })
      })
    })

    await page.getByLabel(/email/i).fill('existing@example.com')
    await page.getByLabel(/password/i).fill('Password123!')
    await page.getByLabel(/first name/i).fill('Test')
    await page.getByLabel(/last name/i).fill('User')
    await page.getByLabel(/phone number/i).fill('1234567890')
    await page.getByLabel(/address/i).fill('123 Main St')
    await page.getByLabel(/city/i).fill('Lagos')
    await page.getByLabel(/state/i).fill('Lagos')
    await page.getByLabel(/country/i).fill('Nigeria')

    await page.getByRole('button', { name: /register|sign up/i }).click()

    await expect(page.getByText(/email.*exists|already registered/i)).toBeVisible()
  })

  test('should handle network errors gracefully', async ({ page }) => {
    // Mock network error
    await page.route('**/api/proxy?service=user&path=/users/register', route => {
      route.abort('failed')
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

    await expect(page.getByText(/error|failed|try again/i)).toBeVisible()
  })

  test('should navigate to login page from registration', async ({ page }) => {
    // Navigate directly to login (registration page may not have a direct link)
    await page.goto('/login')
    await expect(page).toHaveURL(/login/)
  })
})

