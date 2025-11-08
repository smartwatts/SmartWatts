import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import { withAdminAuth, useAdminAuth } from '../../components/withAdminAuth'

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
jest.mock('../../hooks/useAuth', () => ({
  useAuth: jest.fn(() => ({
    user: null,
    loading: false,
    login: jest.fn(),
    register: jest.fn(),
    logout: jest.fn(),
    updateProfile: jest.fn()
  }))
}))

describe('withAdminAuth HOC', () => {
  const { useAuth } = require('../../hooks/useAuth')

  beforeEach(() => {
    jest.clearAllMocks()
    mockPush.mockClear()
  })

  describe('HOC Wrapper', () => {
    const TestComponent = ({ message }: { message: string }) => (
      <div>Test Component: {message}</div>
    )

    it('should show loading state when loading', () => {
      useAuth.mockReturnValue({
        user: null,
        loading: true
      })

      const ProtectedComponent = withAdminAuth(TestComponent)
      render(<ProtectedComponent message="Hello" />)

      expect(screen.getByRole('status', { hidden: true })).toBeInTheDocument()
    })

    it('should render wrapped component when user is enterprise admin', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'admin-123',
          email: 'admin@example.com',
          role: 'ROLE_ENTERPRISE_ADMIN'
        },
        loading: false
      })

      const ProtectedComponent = withAdminAuth(TestComponent)
      render(<ProtectedComponent message="Hello" />)

      expect(screen.getByText('Test Component: Hello')).toBeInTheDocument()
    })

    it('should redirect to login when user is not authenticated', async () => {
      useAuth.mockReturnValue({
        user: null,
        loading: false
      })

      const ProtectedComponent = withAdminAuth(TestComponent)
      render(<ProtectedComponent message="Hello" />)

      await waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith('/login')
      })
    })

    it('should redirect to dashboard when user is not admin', async () => {
      useAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'user@example.com',
          role: 'ROLE_USER'
        },
        loading: false
      })

      const ProtectedComponent = withAdminAuth(TestComponent)
      render(<ProtectedComponent message="Hello" />)

      await waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith('/dashboard?error=unauthorized')
      })
    })
  })

  describe('useAdminAuth Hook', () => {
    it('should return admin status for enterprise admin', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'admin-123',
          email: 'admin@example.com',
          role: 'ROLE_ENTERPRISE_ADMIN'
        },
        loading: false
      })

      const TestComponent = () => {
        const { isAdmin, isAuthenticated } = useAdminAuth()
        return (
          <div>
            <div data-testid="is-admin">{isAdmin ? 'true' : 'false'}</div>
            <div data-testid="is-authenticated">{isAuthenticated ? 'true' : 'false'}</div>
          </div>
        )
      }

      render(<TestComponent />)

      expect(screen.getByTestId('is-admin')).toHaveTextContent('true')
      expect(screen.getByTestId('is-authenticated')).toHaveTextContent('true')
    })

    it('should return false for non-admin user', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'user@example.com',
          role: 'ROLE_USER'
        },
        loading: false
      })

      const TestComponent = () => {
        const { isAdmin, isAuthenticated } = useAdminAuth()
        return (
          <div>
            <div data-testid="is-admin">{isAdmin ? 'true' : 'false'}</div>
            <div data-testid="is-authenticated">{isAuthenticated ? 'true' : 'false'}</div>
          </div>
        )
      }

      render(<TestComponent />)

      expect(screen.getByTestId('is-admin')).toHaveTextContent('false')
      expect(screen.getByTestId('is-authenticated')).toHaveTextContent('true')
    })

    it('should return false when user is not authenticated', () => {
      useAuth.mockReturnValue({
        user: null,
        loading: false
      })

      const TestComponent = () => {
        const { isAdmin, isAuthenticated } = useAdminAuth()
        return (
          <div>
            <div data-testid="is-admin">{isAdmin ? 'true' : 'false'}</div>
            <div data-testid="is-authenticated">{isAuthenticated ? 'true' : 'false'}</div>
          </div>
        )
      }

      render(<TestComponent />)

      expect(screen.getByTestId('is-admin')).toHaveTextContent('false')
      expect(screen.getByTestId('is-authenticated')).toHaveTextContent('false')
    })
  })
})

