import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import AuthGuard from '../../components/AuthGuard'

// Mock Next.js router
const mockReplace = jest.fn()
const mockEvents = {
  on: jest.fn(),
  off: jest.fn()
}

jest.mock('next/router', () => ({
  useRouter: () => ({
    replace: mockReplace,
    events: mockEvents,
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

describe('AuthGuard', () => {
  const { useAuth } = require('../../hooks/useAuth')

  beforeEach(() => {
    jest.clearAllMocks()
    mockReplace.mockClear()
    mockEvents.on.mockClear()
    mockEvents.off.mockClear()
    localStorage.clear()
  })

  describe('Rendering', () => {
    it('should show loading state when loading', () => {
      useAuth.mockReturnValue({
        user: null,
        loading: true
      })

      render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      )

      expect(screen.getByText(/verifying authentication/i)).toBeInTheDocument()
    })

    it('should render children when user is authenticated', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'test@example.com',
          role: 'ROLE_USER'
        },
        loading: false
      })
      localStorage.setItem('token', 'valid-token-12345')

      render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      )

      expect(screen.getByText('Protected Content')).toBeInTheDocument()
    })
  })

  describe('Authentication Checks', () => {
    it('should redirect to login when no token', () => {
      useAuth.mockReturnValue({
        user: null,
        loading: false
      })
      localStorage.removeItem('token')

      render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      )

      expect(mockReplace).toHaveBeenCalledWith('/login')
    })

    it('should redirect to login when token is too short', () => {
      useAuth.mockReturnValue({
        user: null,
        loading: false
      })
      localStorage.setItem('token', 'short')

      render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      )

      expect(mockReplace).toHaveBeenCalledWith('/login')
    })

    it('should redirect to login when user is null', () => {
      useAuth.mockReturnValue({
        user: null,
        loading: false
      })
      localStorage.setItem('token', 'valid-token-12345')

      render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      )

      expect(mockReplace).toHaveBeenCalledWith('/login')
    })
  })

  describe('Route Change Events', () => {
    it('should listen to route change events', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'test@example.com',
          role: 'ROLE_USER'
        },
        loading: false
      })
      localStorage.setItem('token', 'valid-token-12345')

      render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      )

      expect(mockEvents.on).toHaveBeenCalledWith('routeChangeStart', expect.any(Function))
      expect(mockEvents.on).toHaveBeenCalledWith('routeChangeComplete', expect.any(Function))
    })

    it('should cleanup event listeners on unmount', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'test@example.com',
          role: 'ROLE_USER'
        },
        loading: false
      })
      localStorage.setItem('token', 'valid-token-12345')

      const { unmount } = render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      )

      unmount()

      expect(mockEvents.off).toHaveBeenCalledWith('routeChangeStart', expect.any(Function))
      expect(mockEvents.off).toHaveBeenCalledWith('routeChangeComplete', expect.any(Function))
    })
  })
})

