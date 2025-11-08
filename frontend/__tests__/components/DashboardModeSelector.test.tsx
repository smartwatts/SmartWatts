import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import DashboardModeSelector, { useDashboardMode } from '../../components/DashboardModeSelector'

// Mock Next.js router
const mockPush = jest.fn()
jest.mock('next/router', () => ({
  useRouter: () => ({
    push: mockPush,
    route: '/',
    pathname: '/',
    query: {},
    asPath: '/'
  })
}))

// Mock useAuth hook
const mockUseAuth = jest.fn(() => ({
  user: {
    id: 'user-123',
    email: 'test@example.com',
    firstName: 'Test',
    lastName: 'User',
    role: 'ROLE_USER',
    isActive: true,
    createdAt: '2024-01-01T00:00:00Z'
  },
  loading: false,
  login: jest.fn(),
  register: jest.fn(),
  logout: jest.fn(),
  updateProfile: jest.fn()
}))

jest.mock('../../hooks/useAuth', () => ({
  useAuth: () => mockUseAuth()
}))

describe('DashboardModeSelector', () => {
  const mockOnModeChange = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
    mockPush.mockClear()
    // Reset to default user role
    mockUseAuth.mockReturnValue({
      user: {
        id: 'user-123',
        email: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
        role: 'ROLE_USER',
        isActive: true,
        createdAt: '2024-01-01T00:00:00Z'
      },
      loading: false,
      login: jest.fn(),
      register: jest.fn(),
      logout: jest.fn(),
      updateProfile: jest.fn()
    })
  })

  describe('Rendering', () => {
    it('should render all available dashboard modes', () => {
      // Mock SYSTEM_ADMIN to see all modes
      mockUseAuth.mockReturnValue({
        user: {
          id: 'admin-123',
          email: 'admin@example.com',
          firstName: 'Admin',
          lastName: 'User',
          role: 'ROLE_SYSTEM_ADMIN',
          isActive: true,
          createdAt: '2024-01-01T00:00:00Z'
        },
        loading: false,
        login: jest.fn(),
        register: jest.fn(),
        logout: jest.fn(),
        updateProfile: jest.fn()
      })

      render(<DashboardModeSelector />)

      expect(screen.getByText(/choose your dashboard/i)).toBeInTheDocument()
      // Use getAllByText since "Household" appears in both card and button
      const householdTexts = screen.getAllByText(/household/i)
      expect(householdTexts.length).toBeGreaterThan(0)
      // Use getAllByText since "Business" appears in both card and button
      const businessTexts = screen.getAllByText(/business/i)
      expect(businessTexts.length).toBeGreaterThan(0)
      // Use getAllByText since "Enterprise" appears in both card and button
      const enterpriseTexts = screen.getAllByText(/enterprise/i)
      expect(enterpriseTexts.length).toBeGreaterThan(0)
    })

    it('should render in compact mode', () => {
      render(<DashboardModeSelector compact={true} />)

      expect(screen.getByText(/dashboard mode/i)).toBeInTheDocument()
      expect(screen.getByText(/household/i)).toBeInTheDocument()
    })

    it('should highlight selected mode', () => {
      // Mock SYSTEM_ADMIN to see all modes
      mockUseAuth.mockReturnValue({
        user: {
          id: 'admin-123',
          email: 'admin@example.com',
          firstName: 'Admin',
          lastName: 'User',
          role: 'ROLE_SYSTEM_ADMIN',
          isActive: true,
          createdAt: '2024-01-01T00:00:00Z'
        },
        loading: false,
        login: jest.fn(),
        register: jest.fn(),
        logout: jest.fn(),
        updateProfile: jest.fn()
      })

      render(<DashboardModeSelector currentMode="business" />)

      // Find the card div (not the button text) - it should have border-blue-500 class
      const businessTexts = screen.getAllByText(/business/i)
      // The card is the parent div of the text
      const businessCard = businessTexts[0].closest('div')?.closest('div')
      // Check if any parent has the border-blue-500 class
      let hasBorder = false
      let current = businessTexts[0].parentElement
      while (current) {
        if (current.classList.contains('border-blue-500')) {
          hasBorder = true
          break
        }
        current = current.parentElement
      }
      expect(hasBorder).toBe(true)
    })
  })

  describe('Mode Selection', () => {
    it('should call onModeChange when mode is selected', async () => {
      const user = userEvent.setup()
      // Mock SYSTEM_ADMIN to see all modes
      mockUseAuth.mockReturnValue({
        user: {
          id: 'admin-123',
          email: 'admin@example.com',
          firstName: 'Admin',
          lastName: 'User',
          role: 'ROLE_SYSTEM_ADMIN',
          isActive: true,
          createdAt: '2024-01-01T00:00:00Z'
        },
        loading: false,
        login: jest.fn(),
        register: jest.fn(),
        logout: jest.fn(),
        updateProfile: jest.fn()
      })

      render(<DashboardModeSelector onModeChange={mockOnModeChange} />)

      // Find the card div (not the button text) - click the first business text's parent card
      const businessTexts = screen.getAllByText(/business/i)
      const businessCard = businessTexts[0].closest('div')?.closest('div')
      if (businessCard) {
        await user.click(businessCard as HTMLElement)
      }

      await waitFor(() => {
        expect(mockOnModeChange).toHaveBeenCalledWith('business')
      })
    })

    it('should navigate to selected mode path', async () => {
      const user = userEvent.setup()
      // Mock SYSTEM_ADMIN to see all modes
      mockUseAuth.mockReturnValue({
        user: {
          id: 'admin-123',
          email: 'admin@example.com',
          firstName: 'Admin',
          lastName: 'User',
          role: 'ROLE_SYSTEM_ADMIN',
          isActive: true,
          createdAt: '2024-01-01T00:00:00Z'
        },
        loading: false,
        login: jest.fn(),
        register: jest.fn(),
        logout: jest.fn(),
        updateProfile: jest.fn()
      })

      render(<DashboardModeSelector />)

      // Find the card div (not the button text) - click the first business text's parent card
      const businessTexts = screen.getAllByText(/business/i)
      const businessCard = businessTexts[0].closest('div')?.closest('div')
      if (businessCard) {
        await user.click(businessCard as HTMLElement)
      }

      await waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith('/dashboard/business')
      })
    })

    it('should update selected mode state', async () => {
      const user = userEvent.setup()
      // Mock SYSTEM_ADMIN to see all modes
      mockUseAuth.mockReturnValue({
        user: {
          id: 'admin-123',
          email: 'admin@example.com',
          firstName: 'Admin',
          lastName: 'User',
          role: 'ROLE_SYSTEM_ADMIN',
          isActive: true,
          createdAt: '2024-01-01T00:00:00Z'
        },
        loading: false,
        login: jest.fn(),
        register: jest.fn(),
        logout: jest.fn(),
        updateProfile: jest.fn()
      })

      render(<DashboardModeSelector />)

      // Find the card div (not the button text) - click the first enterprise text's parent card
      const enterpriseTexts = screen.getAllByText(/enterprise/i)
      const enterpriseCard = enterpriseTexts[0].closest('div')?.closest('div')
      if (enterpriseCard) {
        await user.click(enterpriseCard as HTMLElement)
      }

      await waitFor(() => {
        // Check if any parent has the border-blue-500 class
        const selectedTexts = screen.getAllByText(/enterprise/i)
        let hasBorder = false
        let current = selectedTexts[0].parentElement
        while (current) {
          if (current.classList.contains('border-blue-500')) {
            hasBorder = true
            break
          }
          current = current.parentElement
        }
        expect(hasBorder).toBe(true)
      })
    })
  })

  describe('Role-Based Access', () => {
    it('should show only household mode for ROLE_USER', () => {
      // Default is already ROLE_USER, but let's be explicit
      mockUseAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'user@example.com',
          firstName: 'Regular',
          lastName: 'User',
          role: 'ROLE_USER',
          isActive: true,
          createdAt: '2024-01-01T00:00:00Z'
        },
        loading: false,
        login: jest.fn(),
        register: jest.fn(),
        logout: jest.fn(),
        updateProfile: jest.fn()
      })

      render(<DashboardModeSelector />)

      // Use getAllByText since "Household" appears in both card and button
      const householdTexts = screen.getAllByText(/household/i)
      expect(householdTexts.length).toBeGreaterThan(0)
      // Business and Enterprise should not be visible for regular users
      expect(screen.queryByText(/business/i)).not.toBeInTheDocument()
      expect(screen.queryByText(/enterprise/i)).not.toBeInTheDocument()
    })

    it('should show household and business modes for ROLE_BUSINESS_ADMIN', async () => {
      mockUseAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'business@example.com',
          firstName: 'Business',
          lastName: 'Admin',
          role: 'ROLE_BUSINESS_ADMIN',
          isActive: true,
          createdAt: '2024-01-01T00:00:00Z'
        },
        loading: false,
        login: jest.fn(),
        register: jest.fn(),
        logout: jest.fn(),
        updateProfile: jest.fn()
      })

      render(<DashboardModeSelector />)

      // Wait for the component to update based on the user role
      await waitFor(() => {
        // Use getAllByText since "Household" appears in both card and button
        const householdTexts = screen.getAllByText(/household/i)
        expect(householdTexts.length).toBeGreaterThan(0)
      })

      // Use getAllByText since "Business" appears in both card and button
      const businessTexts = screen.getAllByText(/business/i)
      expect(businessTexts.length).toBeGreaterThan(0)
      expect(screen.queryByText(/enterprise/i)).not.toBeInTheDocument()
    })

    it('should show all modes for ROLE_SYSTEM_ADMIN', () => {
      mockUseAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'admin@example.com',
          firstName: 'System',
          lastName: 'Admin',
          role: 'ROLE_SYSTEM_ADMIN',
          isActive: true,
          createdAt: '2024-01-01T00:00:00Z'
        },
        loading: false,
        login: jest.fn(),
        register: jest.fn(),
        logout: jest.fn(),
        updateProfile: jest.fn()
      })

      render(<DashboardModeSelector />)

      // Use getAllByText since "Household" appears in both card and button
      const householdTexts = screen.getAllByText(/household/i)
      expect(householdTexts.length).toBeGreaterThan(0)
      // Use getAllByText since "Business" appears in both card and button
      const businessTexts = screen.getAllByText(/business/i)
      expect(businessTexts.length).toBeGreaterThan(0)
      // Use getAllByText since "Enterprise" appears in both card and button
      const enterpriseTexts = screen.getAllByText(/enterprise/i)
      expect(enterpriseTexts.length).toBeGreaterThan(0)
    })
  })

  describe('Auto-Detection', () => {
    it('should auto-select appropriate mode based on user role', () => {
      mockUseAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'business@example.com',
          firstName: 'Business',
          lastName: 'Admin',
          role: 'ROLE_BUSINESS_ADMIN',
          isActive: true,
          createdAt: '2024-01-01T00:00:00Z'
        },
        loading: false,
        login: jest.fn(),
        register: jest.fn(),
        logout: jest.fn(),
        updateProfile: jest.fn()
      })

      render(<DashboardModeSelector />)

      // Should auto-select business mode - check if any parent has the border-blue-500 class
      const businessTexts = screen.getAllByText(/business/i)
      let hasBorder = false
      let current = businessTexts[0].parentElement
      while (current) {
        if (current.classList.contains('border-blue-500')) {
          hasBorder = true
          break
        }
        current = current.parentElement
      }
      expect(hasBorder).toBe(true)
    })
  })

  describe('Features Display', () => {
    it('should display key features for each mode', () => {
      render(<DashboardModeSelector showDescription={true} />)

      expect(screen.getByText(/key features/i)).toBeInTheDocument()
      expect(screen.getByText(/multi-source energy tracking/i)).toBeInTheDocument()
    })

    it('should not display features in compact mode', () => {
      render(<DashboardModeSelector compact={true} />)

      expect(screen.queryByText(/key features/i)).not.toBeInTheDocument()
    })
  })

  describe('Continue Button', () => {
    it('should render continue button', () => {
      render(<DashboardModeSelector />)

      expect(screen.getByRole('button', { name: /continue with/i })).toBeInTheDocument()
    })

    it('should navigate when continue button is clicked', async () => {
      const user = userEvent.setup()
      render(<DashboardModeSelector />)

      const continueButton = screen.getByRole('button', { name: /continue with/i })
      await user.click(continueButton)

      await waitFor(() => {
        expect(mockPush).toHaveBeenCalled()
      })
    })
  })

  describe('useDashboardMode Hook', () => {
    it('should return current mode', () => {
      const TestComponent = () => {
        const { currentMode } = useDashboardMode()
        return <div data-testid="current-mode">{currentMode}</div>
      }

      render(<TestComponent />)

      expect(screen.getByTestId('current-mode')).toHaveTextContent('household')
    })

    it('should provide changeMode function', () => {
      const TestComponent = () => {
        const { currentMode, changeMode } = useDashboardMode()
        return (
          <div>
            <div data-testid="current-mode">{currentMode}</div>
            <button onClick={() => changeMode('business')}>Change to Business</button>
          </div>
        )
      }

      render(<TestComponent />)

      const button = screen.getByText(/change to business/i)
      fireEvent.click(button)

      expect(screen.getByTestId('current-mode')).toHaveTextContent('business')
    })

    it('should provide getCurrentModeInfo function', () => {
      const TestComponent = () => {
        const { getCurrentModeInfo } = useDashboardMode()
        const modeInfo = getCurrentModeInfo()
        return <div data-testid="mode-name">{modeInfo?.name}</div>
      }

      render(<TestComponent />)

      expect(screen.getByTestId('mode-name')).toHaveTextContent('Household')
    })

    it('should provide getAvailableModes function', () => {
      const TestComponent = () => {
        const { getAvailableModes } = useDashboardMode()
        const modes = getAvailableModes()
        return <div data-testid="mode-count">{modes.length}</div>
      }

      render(<TestComponent />)

      expect(screen.getByTestId('mode-count')).toHaveTextContent('1')
    })
  })
})

