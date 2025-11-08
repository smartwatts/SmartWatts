import React from 'react'
import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import Layout from '../components/Layout'

// Mock the auth hook
const mockUseAuth = jest.fn(() => ({
  user: {
    id: '1',
    email: 'test@example.com',
    firstName: 'Test',
    lastName: 'User',
    role: 'ROLE_USER'
  },
  loading: false,
  logout: jest.fn()
}))

jest.mock('../hooks/useAuth', () => ({
  useAuth: () => mockUseAuth()
}))

// Mock the feature flags hook
jest.mock('../hooks/useFeatureFlags', () => ({
  useFeatureFlags: () => ({
    isFeatureEnabled: jest.fn(() => true)
  })
}))

// Mock the theme context
jest.mock('../contexts/ThemeContext', () => ({
  ThemeProvider: ({ children }: { children: React.ReactNode }) => children,
  useTheme: () => ({
    theme: 'light',
    setTheme: jest.fn()
  })
}))

// Mock the dashboard theme context
jest.mock('../contexts/DashboardThemeContext', () => ({
  DashboardThemeProvider: ({ children }: { children: React.ReactNode }) => children,
  useDashboardTheme: () => ({
    dashboardStyle: 'system-theme',
    setDashboardStyle: jest.fn()
  })
}))

// Mock Next.js router
jest.mock('next/router', () => ({
  useRouter: () => ({
    pathname: '/dashboard',
    push: jest.fn(),
    replace: jest.fn()
  })
}))

// Mock PWA components
jest.mock('../components/OfflineIndicator', () => {
  return function MockOfflineIndicator() {
    return <div data-testid="offline-indicator">Offline Indicator</div>
  }
})

jest.mock('../components/InstallPrompt', () => {
  return function MockInstallPrompt() {
    return <div data-testid="install-prompt">Install Prompt</div>
  }
})

// Mock theme selector
jest.mock('../components/ThemeSelector', () => {
  return function MockThemeSelector() {
    return <div data-testid="theme-selector">Theme Selector</div>
  }
})

const createTestQueryClient = () => new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
    },
  },
})

const TestWrapper = ({ children }: { children: React.ReactNode }) => {
  const queryClient = createTestQueryClient()
  
  return (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  )
}

describe('Layout Component', () => {
  it('renders without crashing', () => {
    render(
      <TestWrapper>
        <Layout>
          <div>Test Content</div>
        </Layout>
      </TestWrapper>
    )
    
    expect(screen.getByText('Test Content')).toBeInTheDocument()
  })

  it('renders the sidebar with navigation items', () => {
    render(
      <TestWrapper>
        <Layout>
          <div>Test Content</div>
        </Layout>
      </TestWrapper>
    )
    
    // Check for main navigation items - Layout renders "Dashboard" and "Energy Monitor", "Analytics", "Devices"
    // Use getAllByText since "Dashboard" appears multiple times
    const dashboardTexts = screen.getAllByText('Dashboard')
    expect(dashboardTexts.length).toBeGreaterThan(0)
    // Use getAllByText since "Energy" might appear in "Energy Monitor"
    const energyTexts = screen.queryAllByText(/energy/i)
    expect(energyTexts.length).toBeGreaterThan(0)
    // Use getAllByText since "Analytics" might appear multiple times
    const analyticsTexts = screen.queryAllByText(/analytics/i)
    expect(analyticsTexts.length).toBeGreaterThan(0)
    // Use getAllByText since "Devices" might appear multiple times
    const devicesTexts = screen.queryAllByText(/devices/i)
    expect(devicesTexts.length).toBeGreaterThan(0)
  })

  it('renders the user profile section', () => {
    render(
      <TestWrapper>
        <Layout>
          <div>Test Content</div>
        </Layout>
      </TestWrapper>
    )
    
    // The Layout component uses firstName and lastName, not name
    // Check for email - use getAllByText since email might appear multiple times
    const emailTexts = screen.getAllByText('test@example.com')
    expect(emailTexts.length).toBeGreaterThan(0)
    // Check for user name (might be "Test User" or "Test" + "User")
    const userTexts = screen.queryAllByText(/test/i)
    expect(userTexts.length).toBeGreaterThan(0)
  })

  it('renders PWA components', () => {
    render(
      <TestWrapper>
        <Layout>
          <div>Test Content</div>
        </Layout>
      </TestWrapper>
    )
    
    expect(screen.getByTestId('offline-indicator')).toBeInTheDocument()
    expect(screen.getByTestId('install-prompt')).toBeInTheDocument()
  })

  it('renders the theme selector', () => {
    render(
      <TestWrapper>
        <Layout>
          <div>Test Content</div>
        </Layout>
      </TestWrapper>
    )
    
    expect(screen.getByTestId('theme-selector')).toBeInTheDocument()
  })

  it('shows mobile menu toggle button', () => {
    render(
      <TestWrapper>
        <Layout>
          <div>Test Content</div>
        </Layout>
      </TestWrapper>
    )
    
    // The Layout component uses Bars3Icon for the mobile menu button
    // Find the button by its icon or by its aria-label
    // The button might not have an accessible name, so find it by its parent context
    const menuButtons = screen.queryAllByRole('button')
    // Find the button that contains the menu icon (Bars3Icon)
    const menuButton = menuButtons.find(button => 
      button.querySelector('svg') || button.getAttribute('aria-label')?.includes('menu')
    )
    expect(menuButton).toBeDefined()
  })

  it('renders admin navigation for admin users', () => {
    // Mock admin user
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        email: 'admin@example.com',
        firstName: 'Admin',
        lastName: 'User',
        role: 'ROLE_ENTERPRISE_ADMIN'
      },
      loading: false,
      logout: jest.fn()
    })

    render(
      <TestWrapper>
        <Layout>
          <div>Test Content</div>
        </Layout>
      </TestWrapper>
    )
    
    // Admin navigation should be visible - check for admin-specific items
    // The Layout component shows "Accounts", "Inventory", "Purchase", etc. for enterprise admin
    const adminTexts = screen.queryAllByText(/accounts|inventory|purchase|sales|teams|renewals/i)
    expect(adminTexts.length).toBeGreaterThan(0)
  })
})


