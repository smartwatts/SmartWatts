import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import AdminRoute from '../../components/AdminRoute'

// Mock Next.js router
const mockReplace = jest.fn(() => Promise.resolve(true))
mockReplace.finally = jest.fn((callback) => {
  Promise.resolve(mockReplace()).then(callback).catch(callback)
  return Promise.resolve()
})
jest.mock('next/router', () => ({
  useRouter: () => ({
    replace: mockReplace,
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

describe('AdminRoute', () => {
  const { useAuth } = require('../../hooks/useAuth')

  beforeEach(() => {
    jest.clearAllMocks()
    mockReplace.mockClear()
  })

  describe('Rendering', () => {
    it('should show loading state when loading', () => {
      useAuth.mockReturnValue({
        user: null,
        loading: true
      })

      render(
        <AdminRoute>
          <div>Admin Content</div>
        </AdminRoute>
      )

      expect(screen.getByRole('status', { hidden: true })).toBeInTheDocument()
    })

    it('should render children when user is enterprise admin', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'admin-123',
          email: 'admin@example.com',
          role: 'ROLE_ENTERPRISE_ADMIN'
        },
        loading: false
      })

      render(
        <AdminRoute>
          <div>Admin Content</div>
        </AdminRoute>
      )

      expect(screen.getByText('Admin Content')).toBeInTheDocument()
    })
  })

  describe('Authorization Checks', () => {
    it('should redirect to login when user is not authenticated', async () => {
      useAuth.mockReturnValue({
        user: null,
        loading: false
      })

      render(
        <AdminRoute>
          <div>Admin Content</div>
        </AdminRoute>
      )

      await waitFor(() => {
        expect(mockReplace).toHaveBeenCalledWith('/login')
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

      render(
        <AdminRoute>
          <div>Admin Content</div>
        </AdminRoute>
      )

      await waitFor(() => {
        expect(mockReplace).toHaveBeenCalledWith('/dashboard?error=unauthorized')
      })
    })

    it('should not redirect when user is enterprise admin', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'admin-123',
          email: 'admin@example.com',
          role: 'ROLE_ENTERPRISE_ADMIN'
        },
        loading: false
      })

      render(
        <AdminRoute>
          <div>Admin Content</div>
        </AdminRoute>
      )

      expect(mockReplace).not.toHaveBeenCalled()
    })
  })

  describe('Role Checks', () => {
    it('should allow ROLE_ENTERPRISE_ADMIN', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'admin-123',
          email: 'admin@example.com',
          role: 'ROLE_ENTERPRISE_ADMIN'
        },
        loading: false
      })

      render(
        <AdminRoute>
          <div>Admin Content</div>
        </AdminRoute>
      )

      expect(screen.getByText('Admin Content')).toBeInTheDocument()
    })

    it('should deny ROLE_USER', async () => {
      useAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'user@example.com',
          role: 'ROLE_USER'
        },
        loading: false
      })

      render(
        <AdminRoute>
          <div>Admin Content</div>
        </AdminRoute>
      )

      await waitFor(() => {
        expect(mockReplace).toHaveBeenCalledWith('/dashboard?error=unauthorized')
      })
    })

    it('should deny ROLE_BUSINESS_ADMIN', async () => {
      useAuth.mockReturnValue({
        user: {
          id: 'business-admin-123',
          email: 'business@example.com',
          role: 'ROLE_BUSINESS_ADMIN'
        },
        loading: false
      })

      render(
        <AdminRoute>
          <div>Admin Content</div>
        </AdminRoute>
      )

      await waitFor(() => {
        expect(mockReplace).toHaveBeenCalledWith('/dashboard?error=unauthorized')
      })
    })
  })
})

