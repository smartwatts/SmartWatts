import React, { useState, useEffect } from 'react'
import Layout from '../components/Layout'
import ProtectedRoute from '../components/ProtectedRoute'
import { useFeatureFlags } from '../hooks/useFeatureFlags'
import { 
  BoltIcon, 
  ChartBarIcon, 
  CogIcon, 
  BuildingOffice2Icon, 
  CreditCardIcon, 
  ShieldCheckIcon,
  CheckIcon,
  XMarkIcon,
  ExclamationTriangleIcon,
  ArrowUpCircleIcon
} from '@heroicons/react/24/outline'

interface FeatureFlag {
  id: string
  featureKey: string
  featureName: string
  description: string
  isGloballyEnabled: boolean
  isPaidFeature: boolean
  featureCategory: string
  createdAt: string
  updatedAt: string
}

interface UserAccess {
  userId: string
  currentPlan: string
  enabledFeatures: string[]
  disabledFeatures: string[]
  hasActiveSubscription: boolean
}

export default function FeatureManagement() {
  const [featureFlags, setFeatureFlags] = useState<FeatureFlag[]>([])
  const [userAccess, setUserAccess] = useState<UserAccess | null>(null)
  const [loading, setLoading] = useState(true)
  const { isFeatureEnabled } = useFeatureFlags()

  useEffect(() => {
    fetchFeatureData()
  }, [])

  const fetchFeatureData = async () => {
    try {
      setLoading(true)
      
      // Fetch feature flags
      const featuresResponse = await fetch('/api/proxy?service=feature-flags&path=/features')
      if (featuresResponse.ok) {
        const features = await featuresResponse.json()
        setFeatureFlags(features)
      }

      // Fetch user access
      const userResponse = await fetch('/api/proxy?service=feature-flags&path=/user-access/current')
      if (userResponse.ok) {
        const access = await userResponse.json()
        setUserAccess(access)
      }
    } catch (error) {
      console.error('Error fetching feature data:', error)
    } finally {
      setLoading(false)
    }
  }

  const getFeatureIcon = (featureKey: string) => {
    switch (featureKey) {
      case 'FACILITY360': return BuildingOffice2Icon
      case 'BILLING_DASHBOARD': return CreditCardIcon
      case 'ADVANCED_ANALYTICS': return ChartBarIcon
      case 'DEVICE_MANAGEMENT': return CogIcon
      case 'API_ACCESS': return ShieldCheckIcon
      case 'PARTNER_SERVICES': return ShieldCheckIcon
      case 'APPLIANCE_MONITORING': return BoltIcon
      default: return CogIcon
    }
  }

  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'MANAGEMENT': return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300'
      case 'ANALYTICS': return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300'
      case 'DEVICES': return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-300'
      case 'INTEGRATION': return 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-300'
      case 'PARTNERSHIPS': return 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900 dark:text-indigo-300'
      case 'MONITORING': return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-300'
    }
  }

  const getFeatureStatus = (featureKey: string) => {
    if (!userAccess) return 'unknown'
    
    if (userAccess.enabledFeatures.includes(featureKey)) {
      return 'enabled'
    } else if (userAccess.disabledFeatures.includes(featureKey)) {
      return 'disabled'
    }
    return 'unknown'
  }

  if (loading) {
    return (
      <ProtectedRoute>
        <Layout>
          <div className="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-gray-100 dark:from-gray-900 dark:via-blue-900 dark:to-gray-800 flex items-center justify-center">
            <div className="relative">
              <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
              <div className="absolute inset-0 flex items-center justify-center">
                <div className="w-8 h-8 bg-blue-600 rounded-full animate-pulse"></div>
              </div>
            </div>
          </div>
        </Layout>
      </ProtectedRoute>
    )
  }

  return (
    <ProtectedRoute>
      <Layout>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
              Feature Management
            </h1>
            <p className="text-gray-600 dark:text-gray-400">
              View your current feature access and upgrade options
            </p>
          </div>

          {/* Current Plan Status */}
          {userAccess && (
            <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm mb-8">
              <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
                <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
                  Current Plan: {userAccess.currentPlan}
                </h2>
                <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                  {userAccess.hasActiveSubscription ? 'Active subscription' : 'No active subscription'}
                </p>
              </div>
              <div className="p-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="bg-green-50 dark:bg-green-900/20 rounded-lg p-4">
                    <div className="flex items-center">
                      <CheckIcon className="h-5 w-5 text-green-600 dark:text-green-400 mr-2" />
                      <span className="text-sm font-medium text-green-800 dark:text-green-200">
                        {userAccess.enabledFeatures.length} Features Enabled
                      </span>
                    </div>
                  </div>
                  <div className="bg-yellow-50 dark:bg-yellow-900/20 rounded-lg p-4">
                    <div className="flex items-center">
                      <ExclamationTriangleIcon className="h-5 w-5 text-yellow-600 dark:text-yellow-400 mr-2" />
                      <span className="text-sm font-medium text-yellow-800 dark:text-yellow-200">
                        {userAccess.disabledFeatures.length} Features Locked
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Feature List */}
          <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm">
            <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
              <div className="flex items-center justify-between">
                <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
                  Available Features
                </h2>
                <button
                  onClick={fetchFeatureData}
                  className="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                  title="Refresh"
                >
                  <ArrowUpCircleIcon className="h-5 w-5" />
                </button>
              </div>
              <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                All available features and their current status
              </p>
            </div>
            
            <div className="p-6">
              <div className="space-y-4">
                {featureFlags.map((flag) => {
                  const Icon = getFeatureIcon(flag.featureKey)
                  const status = getFeatureStatus(flag.featureKey)
                  const isEnabled = status === 'enabled'
                  
                  return (
                    <div
                      key={flag.id}
                      className={`flex items-center justify-between p-4 rounded-lg border ${
                        isEnabled 
                          ? 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-700'
                          : 'bg-gray-50 dark:bg-gray-700 border-gray-200 dark:border-gray-600'
                      }`}
                    >
                      <div className="flex items-center space-x-4">
                        <div className={`p-2 rounded-lg ${
                          isEnabled 
                            ? 'bg-green-100 dark:bg-green-900'
                            : 'bg-gray-100 dark:bg-gray-800'
                        }`}>
                          <Icon className={`h-5 w-5 ${
                            isEnabled 
                              ? 'text-green-600 dark:text-green-400'
                              : 'text-gray-400 dark:text-gray-500'
                          }`} />
                        </div>
                        <div>
                          <h3 className="font-medium text-gray-900 dark:text-white">
                            {flag.featureName}
                          </h3>
                          <p className="text-sm text-gray-600 dark:text-gray-400">
                            {flag.description}
                          </p>
                          <div className="flex items-center space-x-2 mt-2">
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getCategoryColor(flag.featureCategory)}`}>
                              {flag.featureCategory}
                            </span>
                            {flag.isPaidFeature && (
                              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300">
                                PREMIUM
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                      
                      <div className="flex items-center space-x-4">
                        <div className="text-right">
                          <div className="flex items-center justify-end">
                            {isEnabled ? (
                              <CheckIcon className="h-5 w-5 text-green-600 dark:text-green-400 mr-2" />
                            ) : (
                              <XMarkIcon className="h-5 w-5 text-gray-400 dark:text-gray-500 mr-2" />
                            )}
                            <span className={`text-sm font-medium ${
                              isEnabled 
                                ? 'text-green-600 dark:text-green-400'
                                : 'text-gray-500 dark:text-gray-400'
                            }`}>
                              {isEnabled ? 'Enabled' : 'Locked'}
                            </span>
                          </div>
                          <p className="text-xs text-gray-500 dark:text-gray-400">
                            {flag.isPaidFeature ? 'Premium Feature' : 'Free Feature'}
                          </p>
                        </div>
                        
                        {!isEnabled && flag.isPaidFeature && (
                          <button className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors">
                            Upgrade
                          </button>
                        )}
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>
          </div>

          {/* Upgrade CTA */}
          <div className="mt-8 bg-gradient-to-r from-blue-600 to-green-600 rounded-xl p-8 text-center text-white">
            <h3 className="text-2xl font-bold mb-4">
              Unlock All Features
            </h3>
            <p className="text-xl mb-6 opacity-90">
              Upgrade to Premium to access advanced analytics, device management, and more
            </p>
            <button className="bg-white text-blue-600 px-8 py-3 rounded-xl font-semibold text-lg hover:bg-gray-100 transition-all duration-200 transform hover:scale-105">
              Upgrade Now
            </button>
          </div>
        </div>
      </Layout>
    </ProtectedRoute>
  )
}
