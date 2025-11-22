'use client'

import React, { useState, useEffect } from 'react'
import { useRouter } from 'next/router'
import { useAuth } from '../hooks/useAuth'
import {
  HomeIcon,
  BuildingOfficeIcon,
  BuildingOffice2Icon,
  CogIcon,
  CheckIcon
} from '@heroicons/react/24/outline'

interface DashboardMode {
  id: string
  name: string
  description: string
  icon: React.ComponentType<{ className?: string }>
  path: string
  targetUsers: string[]
  features: string[]
}

const dashboardModes: DashboardMode[] = [
  {
    id: 'household',
    name: 'Household',
    description: 'Simple energy monitoring for homes',
    icon: HomeIcon,
    path: '/dashboard/household',
    targetUsers: ['ROLE_USER', 'ROLE_HOUSEHOLD'],
    features: [
      'Multi-source energy tracking (grid, solar, generator)',
      'Prepaid token management',
      'Generator health monitoring',
      'Voltage quality indicators',
      'Top 5 energy-consuming appliances',
      'Simple cost tracking with MYTO tariffs'
    ]
  },
  {
    id: 'business',
    name: 'Business/SME',
    description: 'Multi-location energy management',
    icon: BuildingOfficeIcon,
    path: '/dashboard/business',
    targetUsers: ['ROLE_BUSINESS_ADMIN', 'ROLE_SME_ADMIN'],
    features: [
      'Multi-location energy management',
      'Cost per location/department',
      'Peak demand management',
      'Equipment health monitoring',
      'Predictive maintenance alerts',
      'ROI calculator for efficiency upgrades',
      'ESG compliance reporting'
    ]
  },
  {
    id: 'enterprise',
    name: 'Enterprise',
    description: 'Advanced analytics and multi-tenant management',
    icon: BuildingOffice2Icon,
    path: '/dashboard/enterprise',
    targetUsers: ['ROLE_ENTERPRISE_ADMIN', 'ROLE_ADMIN', 'ROLE_SYSTEM_ADMIN'],
    features: [
      'Advanced analytics and forecasting',
      'Multi-tenant management',
      'Custom reporting and exports',
      'API integration capabilities',
      'Grid stability analysis',
      'Demand response participation',
      'Carbon tracking and credits'
    ]
  }
]

interface DashboardModeSelectorProps {
  currentMode?: string
  onModeChange?: (mode: string) => void
  showDescription?: boolean
  compact?: boolean
}

export default function DashboardModeSelector({
  currentMode,
  onModeChange,
  showDescription = true,
  compact = false
}: DashboardModeSelectorProps) {
  const [selectedMode, setSelectedMode] = useState<string>(currentMode || 'household')
  const [userRole, setUserRole] = useState<string>('ROLE_USER')
  const router = useRouter()
  const { user } = useAuth()

  useEffect(() => {
    if (user?.role) {
      setUserRole(user.role)
    }
  }, [user])

  // Auto-detect appropriate dashboard mode based on user role
  useEffect(() => {
    if (!currentMode && userRole) {
      const appropriateMode = dashboardModes.find(mode => 
        mode.targetUsers.includes(userRole)
      )
      if (appropriateMode) {
        setSelectedMode(appropriateMode.id)
      }
    }
  }, [userRole, currentMode])

  const handleModeChange = (modeId: string) => {
    setSelectedMode(modeId)
    if (onModeChange) {
      onModeChange(modeId)
    }
    
    const mode = dashboardModes.find(m => m.id === modeId)
    if (mode) {
      router.push(mode.path)
    }
  }

  // Filter modes based on user role
  // ROLE_BUSINESS_ADMIN and ROLE_SME_ADMIN should see both household and business modes
  // ROLE_ENTERPRISE_ADMIN, ROLE_ADMIN, and ROLE_SYSTEM_ADMIN should see all modes
  const availableModes = dashboardModes.filter(mode => {
    if (userRole === 'ROLE_SYSTEM_ADMIN' || userRole === 'ROLE_ENTERPRISE_ADMIN' || userRole === 'ROLE_ADMIN') {
      return true // See all modes
    }
    if (userRole === 'ROLE_BUSINESS_ADMIN' || userRole === 'ROLE_SME_ADMIN') {
      // Business admins can see household and business modes
      return mode.id === 'household' || mode.id === 'business'
    }
    return mode.targetUsers.includes(userRole)
  })

  if (compact) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-4">
        <div className="flex items-center space-x-2 mb-4">
          <CogIcon className="h-5 w-5 text-gray-600 dark:text-gray-400" />
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Dashboard Mode</h3>
        </div>
        <div className="grid grid-cols-1 gap-2">
          {availableModes.map((mode) => {
            const Icon = mode.icon
            const isSelected = selectedMode === mode.id
            return (
              <button
                key={mode.id}
                onClick={() => handleModeChange(mode.id)}
                className={`flex items-center space-x-3 p-3 rounded-lg transition-colors duration-200 ${
                  isSelected
                    ? 'bg-blue-100 dark:bg-blue-900/20 border-2 border-blue-500'
                    : 'bg-gray-50 dark:bg-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 border-2 border-transparent'
                }`}
              >
                <Icon className={`h-5 w-5 ${
                  isSelected ? 'text-blue-600 dark:text-blue-400' : 'text-gray-600 dark:text-gray-400'
                }`} />
                <div className="text-left">
                  <p className={`font-medium ${
                    isSelected ? 'text-blue-900 dark:text-blue-100' : 'text-gray-900 dark:text-white'
                  }`}>
                    {mode.name}
                  </p>
                  <p className={`text-sm ${
                    isSelected ? 'text-blue-700 dark:text-blue-300' : 'text-gray-600 dark:text-gray-400'
                  }`}>
                    {mode.description}
                  </p>
                </div>
                {isSelected && (
                  <CheckIcon className="h-5 w-5 text-blue-600 dark:text-blue-400 ml-auto" />
                )}
              </button>
            )
          })}
        </div>
      </div>
    )
  }

  return (
    <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-8">
      <div className="text-center mb-8">
        <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
          Choose Your Dashboard
        </h2>
        <p className="text-gray-600 dark:text-gray-400">
          Select the dashboard mode that best fits your needs
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {availableModes.map((mode) => {
          const Icon = mode.icon
          const isSelected = selectedMode === mode.id
          return (
            <div
              key={mode.id}
              onClick={() => handleModeChange(mode.id)}
              className={`relative cursor-pointer rounded-2xl p-6 transition-all duration-300 ${
                isSelected
                  ? 'bg-blue-50 dark:bg-blue-900/20 border-2 border-blue-500 shadow-lg transform scale-105'
                  : 'bg-gray-50 dark:bg-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 border-2 border-transparent hover:border-gray-300 dark:hover:border-gray-600 hover:shadow-lg'
              }`}
            >
              {isSelected && (
                <div className="absolute top-4 right-4">
                  <div className="w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center">
                    <CheckIcon className="h-4 w-4 text-white" />
                  </div>
                </div>
              )}

              <div className="text-center">
                <div className={`inline-flex p-4 rounded-2xl mb-4 ${
                  isSelected ? 'bg-blue-100 dark:bg-blue-800' : 'bg-gray-200 dark:bg-gray-600'
                }`}>
                  <Icon className={`h-8 w-8 ${
                    isSelected ? 'text-blue-600 dark:text-blue-400' : 'text-gray-600 dark:text-gray-400'
                  }`} />
                </div>

                <h3 className={`text-xl font-bold mb-2 ${
                  isSelected ? 'text-blue-900 dark:text-blue-100' : 'text-gray-900 dark:text-white'
                }`}>
                  {mode.name}
                </h3>

                <p className={`text-sm mb-4 ${
                  isSelected ? 'text-blue-700 dark:text-blue-300' : 'text-gray-600 dark:text-gray-400'
                }`}>
                  {mode.description}
                </p>

                {showDescription && (
                  <div className="text-left">
                    <h4 className="font-semibold text-gray-900 dark:text-white mb-2">Key Features:</h4>
                    <ul className="text-sm text-gray-600 dark:text-gray-400 space-y-1">
                      {mode.features.slice(0, 3).map((feature, index) => (
                        <li key={index} className="flex items-start">
                          <span className="text-blue-500 mr-2">•</span>
                          <span>{feature}</span>
                        </li>
                      ))}
                      {mode.features.length > 3 && (
                        <li className="text-blue-600 dark:text-blue-400 font-medium">
                          +{mode.features.length - 3} more features
                        </li>
                      )}
                    </ul>
                  </div>
                )}
              </div>
            </div>
          )
        })}
      </div>

      <div className="mt-8 text-center">
        <button
          onClick={() => handleModeChange(selectedMode)}
          className="bg-blue-600 hover:bg-blue-700 text-white font-medium py-3 px-8 rounded-xl transition-colors duration-200"
        >
          Continue with {dashboardModes.find(m => m.id === selectedMode)?.name}
        </button>
      </div>
    </div>
  )
}

// Hook for dashboard mode management
export function useDashboardMode() {
  const [currentMode, setCurrentMode] = useState<string>('household')
  const [userRole, setUserRole] = useState<string>('ROLE_USER')
  const { user } = useAuth()

  useEffect(() => {
    if (user?.role) {
      setUserRole(user.role)
    }
  }, [user])

  useEffect(() => {
    // Auto-detect appropriate dashboard mode based on user role
    const appropriateMode = dashboardModes.find(mode => 
      mode.targetUsers.includes(userRole)
    )
    if (appropriateMode) {
      setCurrentMode(appropriateMode.id)
    }
  }, [userRole])

  const changeMode = (modeId: string) => {
    setCurrentMode(modeId)
  }

  const getCurrentModeInfo = () => {
    return dashboardModes.find(mode => mode.id === currentMode)
  }

  const getAvailableModes = () => {
    return dashboardModes.filter(mode => 
      mode.targetUsers.includes(userRole) || userRole === 'ROLE_SYSTEM_ADMIN' || userRole === 'ROLE_ADMIN'
    )
  }

  return {
    currentMode,
    changeMode,
    getCurrentModeInfo,
    getAvailableModes,
    userRole
  }
}


