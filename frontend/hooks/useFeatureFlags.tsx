import { useState, useEffect } from 'react'
import { useAuth } from './useAuth'

interface FeatureFlag {
  id: string
  featureKey: string
  featureName: string
  description: string
  isGloballyEnabled: boolean
  isPaidFeature: boolean
  featureCategory: string
}

interface UserAccess {
  userId: string
  currentPlan: string
  enabledFeatures: string[]
  disabledFeatures: string[]
  hasActiveSubscription: boolean
}

export function useFeatureFlags() {
  const { user } = useAuth()
  const [featureFlags, setFeatureFlags] = useState<FeatureFlag[]>([])
  const [userAccess, setUserAccess] = useState<UserAccess | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Plan-based feature mapping (fallback if API fails)
  const planFeatures: Record<string, string[]> = {
    'FREEMIUM': ['BASIC_MONITORING', 'BASIC_ANALYTICS'],
    'PREMIUM': ['BASIC_MONITORING', 'BASIC_ANALYTICS', 'FACILITY360', 'BILLING_DASHBOARD', 'PARTNER_SERVICES', 'APPLIANCE_MONITORING'],
    'ENTERPRISE': ['BASIC_MONITORING', 'BASIC_ANALYTICS', 'FACILITY360', 'BILLING_DASHBOARD', 'ADVANCED_ANALYTICS', 'API_ACCESS', 'PARTNER_SERVICES', 'DEVICE_MANAGEMENT', 'APPLIANCE_MONITORING']
  }

  useEffect(() => {
    if (user) {
      loadFeatureFlags()
      loadUserAccess()
    } else {
      setLoading(false)
    }
  }, [user])

  const loadFeatureFlags = async () => {
    try {
      const response = await fetch('/api/proxy?service=feature-flags&path=/features')
      if (response.ok) {
        const data = await response.json()
        setFeatureFlags(data)
      } else {
        console.warn('Failed to load feature flags, using fallback')
      }
    } catch (error) {
      console.warn('Error loading feature flags, using fallback:', error)
    }
  }

  const loadUserAccess = async () => {
    try {
      const response = await fetch(`/api/proxy?service=feature-flags&path=/user-access/${user?.id}`)
      if (response.ok) {
        const data = await response.json()
        setUserAccess(data)
      } else {
        console.warn('Failed to load user access, using fallback')
      }
    } catch (error) {
      console.warn('Error loading user access, using fallback:', error)
    } finally {
      setLoading(false)
    }
  }

  const isFeatureEnabled = (featureKey: string): boolean => {
    // ALWAYS hide premium/paid features from navigation for ALL users
    const premiumFeatures = [
      'FACILITY360', 
      'BILLING_DASHBOARD', 
      'PARTNER_SERVICES', 
      'ADVANCED_ANALYTICS', 
      'API_ACCESS'
    ]
    if (premiumFeatures.includes(featureKey)) {
      return false
    }

    if (!user) return false

    // ENTERPRISE ADMIN: Can access all system administration features
    if (user.role === 'ROLE_ENTERPRISE_ADMIN') {
      return true
    }

    // REGULAR USERS: Can access basic SmartWatts features
    const basicFeatures = [
      'BASIC_MONITORING', 
      'BASIC_ANALYTICS', 
      'DEVICE_MANAGEMENT', 
      'APPLIANCE_MONITORING'
    ]
    
    if (basicFeatures.includes(featureKey)) {
      return true
    }

    // For features without requiredFeature (like Energy Monitor, Analytics, Devices)
    // Allow access to regular users
    return true
  }

  // Separate function for admin feature management access
  const canManageFeature = (featureKey: string): boolean => {
    if (!user) return false
    
    // Enterprise admin users can manage all features
    if (user.role === 'ROLE_ENTERPRISE_ADMIN') {
      return true
    }
    
    // Regular users cannot manage features
    return false
  }

  const getFeatureFlags = () => featureFlags
  const getUserAccess = () => userAccess
  const isLoading = () => loading
  const getError = () => error

  return {
    featureFlags,
    userAccess,
    loading,
    error,
    isFeatureEnabled,
    canManageFeature,
    getFeatureFlags,
    getUserAccess,
    isLoading,
    getError
  }
}
