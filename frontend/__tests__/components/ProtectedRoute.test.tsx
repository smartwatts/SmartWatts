import React from 'react'
import { render, screen } from '@testing-library/react'
import ProtectedRoute from '../../components/ProtectedRoute'

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

describe('ProtectedRoute', () => {
  const { useAuth } = require('../../hooks/useAuth')

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Rendering', () => {
    it('should show loading state when loading', () => {
      useAuth.mockReturnValue({
        user: null,
        loading: true
      })

      render(
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      )

      expect(screen.getByText(/loading/i)).toBeInTheDocument()
    })

    it('should show login message when user is not authenticated', () => {
      useAuth.mockReturnValue({
        user: null,
        loading: false
      })

      render(
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      )

      expect(screen.getByText(/please log in/i)).toBeInTheDocument()
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
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

      render(
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      )

      expect(screen.getByText('Protected Content')).toBeInTheDocument()
    })
  })

  describe('User States', () => {
    it('should handle authenticated user with ROLE_USER', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'test@example.com',
          role: 'ROLE_USER'
        },
        loading: false
      })

      render(
        <ProtectedRoute>
          <div>User Content</div>
        </ProtectedRoute>
      )

      expect(screen.getByText('User Content')).toBeInTheDocument()
    })

    it('should handle authenticated user with ROLE_ADMIN', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'admin-123',
          email: 'admin@example.com',
          role: 'ROLE_ADMIN'
        },
        loading: false
      })

      render(
        <ProtectedRoute>
          <div>Admin Content</div>
        </ProtectedRoute>
      )

      expect(screen.getByText('Admin Content')).toBeInTheDocument()
    })
  })
})

