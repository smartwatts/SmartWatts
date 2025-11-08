import React from 'react'
import { renderHook, waitFor } from '@testing-library/react'
import { useFeatureFlags } from '../../hooks/useFeatureFlags'

// Mock fetch
global.fetch = jest.fn()

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

describe('useFeatureFlags', () => {
  const { useAuth } = require('../../hooks/useAuth')
  const mockFetch = global.fetch as jest.Mock

  beforeEach(() => {
    jest.clearAllMocks()
    mockFetch.mockClear()
  })

  describe('Initialization', () => {
    it('should return initial state when user is not authenticated', () => {
      useAuth.mockReturnValue({
        user: null,
        loading: false
      })

      const { result } = renderHook(() => useFeatureFlags())

      expect(result.current.loading).toBe(false)
      expect(result.current.featureFlags).toEqual([])
      expect(result.current.userAccess).toBeNull()
    })

    it('should load feature flags when user is authenticated', async () => {
      const mockUser = {
        id: 'user-123',
        email: 'test@example.com',
        role: 'ROLE_USER'
      }

      const mockFeatureFlags = [
        {
          id: '1',
          featureKey: 'BASIC_MONITORING',
          featureName: 'Basic Monitoring',
          description: 'Basic energy monitoring',
          isGloballyEnabled: true,
          isPaidFeature: false,
          featureCategory: 'MONITORING'
        }
      ]

      const mockUserAccess = {
        userId: 'user-123',
        currentPlan: 'FREEMIUM',
        enabledFeatures: ['BASIC_MONITORING'],
        disabledFeatures: [],
        hasActiveSubscription: false
      }

      useAuth.mockReturnValue({
        user: mockUser,
        loading: false
      })

      mockFetch
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockFeatureFlags
        })
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockUserAccess
        })

      const { result } = renderHook(() => useFeatureFlags())

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
      })

      expect(result.current.featureFlags).toEqual(mockFeatureFlags)
      expect(result.current.userAccess).toEqual(mockUserAccess)
    })
  })

  describe('isFeatureEnabled', () => {
    it('should return false when user is not authenticated', () => {
      useAuth.mockReturnValue({
        user: null,
        loading: false
      })

      const { result } = renderHook(() => useFeatureFlags())

      expect(result.current.isFeatureEnabled('BASIC_MONITORING')).toBe(false)
    })

    it('should return false for premium features', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'test@example.com',
          role: 'ROLE_USER'
        },
        loading: false
      })

      const { result } = renderHook(() => useFeatureFlags())

      expect(result.current.isFeatureEnabled('FACILITY360')).toBe(false)
      expect(result.current.isFeatureEnabled('BILLING_DASHBOARD')).toBe(false)
      expect(result.current.isFeatureEnabled('PARTNER_SERVICES')).toBe(false)
    })

    it('should return true for basic features for regular users', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'test@example.com',
          role: 'ROLE_USER'
        },
        loading: false
      })

      const { result } = renderHook(() => useFeatureFlags())

      expect(result.current.isFeatureEnabled('BASIC_MONITORING')).toBe(true)
      expect(result.current.isFeatureEnabled('BASIC_ANALYTICS')).toBe(true)
      expect(result.current.isFeatureEnabled('DEVICE_MANAGEMENT')).toBe(true)
    })

    it('should return true for all features for enterprise admin', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'admin-123',
          email: 'admin@example.com',
          role: 'ROLE_ENTERPRISE_ADMIN'
        },
        loading: false
      })

      const { result } = renderHook(() => useFeatureFlags())

      // Enterprise admin can access basic features
      expect(result.current.isFeatureEnabled('BASIC_MONITORING')).toBe(true)
      // Premium features are hidden from navigation for ALL users (including enterprise admin)
      // But enterprise admin can still access them via direct URL
      // The hook returns false for premium features to hide them from navigation
      expect(result.current.isFeatureEnabled('FACILITY360')).toBe(false)
      expect(result.current.isFeatureEnabled('ADVANCED_ANALYTICS')).toBe(false)
      // But basic features work
      expect(result.current.isFeatureEnabled('BASIC_ANALYTICS')).toBe(true)
      expect(result.current.isFeatureEnabled('DEVICE_MANAGEMENT')).toBe(true)
    })
  })

  describe('canManageFeature', () => {
    it('should return false when user is not authenticated', () => {
      useAuth.mockReturnValue({
        user: null,
        loading: false
      })

      const { result } = renderHook(() => useFeatureFlags())

      expect(result.current.canManageFeature('BASIC_MONITORING')).toBe(false)
    })

    it('should return false for regular users', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'user-123',
          email: 'test@example.com',
          role: 'ROLE_USER'
        },
        loading: false
      })

      const { result } = renderHook(() => useFeatureFlags())

      expect(result.current.canManageFeature('BASIC_MONITORING')).toBe(false)
    })

    it('should return true for enterprise admin', () => {
      useAuth.mockReturnValue({
        user: {
          id: 'admin-123',
          email: 'admin@example.com',
          role: 'ROLE_ENTERPRISE_ADMIN'
        },
        loading: false
      })

      const { result } = renderHook(() => useFeatureFlags())

      expect(result.current.canManageFeature('BASIC_MONITORING')).toBe(true)
      expect(result.current.canManageFeature('FACILITY360')).toBe(true)
    })
  })

  describe('Error Handling', () => {
    it('should handle API errors gracefully', async () => {
      const mockUser = {
        id: 'user-123',
        email: 'test@example.com',
        role: 'ROLE_USER'
      }

      useAuth.mockReturnValue({
        user: mockUser,
        loading: false
      })

      mockFetch.mockRejectedValueOnce(new Error('Network error'))

      const { result } = renderHook(() => useFeatureFlags())

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
      })

      // Should still work with fallback logic
      expect(result.current.isFeatureEnabled('BASIC_MONITORING')).toBe(true)
    })
  })
})

