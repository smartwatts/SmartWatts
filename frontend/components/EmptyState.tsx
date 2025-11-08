import React from 'react'
import { 
  ChartBarIcon, 
  BoltIcon, 
  SunIcon, 
  DevicePhoneMobileIcon,
  PlusIcon,
  ArrowPathIcon
} from '@heroicons/react/24/outline'

interface EmptyStateProps {
  title: string
  description: string
  icon?: React.ComponentType<{ className?: string }>
  action?: {
    label: string
    onClick: () => void
  }
  secondaryAction?: {
    label: string
    onClick: () => void
  }
  showRefresh?: boolean
  onRefresh?: () => void
}

export default function EmptyState({
  title,
  description,
  icon: Icon = ChartBarIcon,
  action,
  secondaryAction,
  showRefresh = false,
  onRefresh
}: EmptyStateProps) {
  return (
    <div className="text-center py-12 px-4">
      <div className="flex justify-center mb-4">
        <Icon className="h-12 w-12 text-gray-400" />
      </div>
      
      <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
        {title}
      </h3>
      
      <p className="text-gray-600 dark:text-gray-400 mb-6 max-w-md mx-auto">
        {description}
      </p>
      
      <div className="space-y-3">
        {action && (
          <button
            onClick={action.onClick}
            className="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200 flex items-center justify-center space-x-2 mx-auto"
          >
            <PlusIcon className="h-4 w-4" />
            <span>{action.label}</span>
          </button>
        )}
        
        {secondaryAction && (
          <button
            onClick={secondaryAction.onClick}
            className="bg-gray-600 hover:bg-gray-700 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200"
          >
            {secondaryAction.label}
          </button>
        )}
        
        {showRefresh && onRefresh && (
          <button
            onClick={onRefresh}
            className="bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 font-medium py-2 px-4 rounded-lg transition-colors duration-200 flex items-center justify-center space-x-2 mx-auto"
          >
            <ArrowPathIcon className="h-4 w-4" />
            <span>Refresh</span>
          </button>
        )}
      </div>
    </div>
  )
}

// Predefined empty states for common scenarios
export const EmptyStates = {
  EnergyData: () => (
    <EmptyState
      title="No Energy Data"
      description="Start monitoring your energy consumption by adding devices or connecting to your energy sources."
      icon={BoltIcon}
      action={{
        label: 'Add Device',
        onClick: () => window.location.href = '/devices'
      }}
    />
  ),
  
  SolarData: () => (
    <EmptyState
      title="No Solar Data"
      description="Connect your solar inverter to start tracking solar generation and optimize your renewable energy usage."
      icon={SunIcon}
      action={{
        label: 'Add Solar Inverter',
        onClick: () => window.location.href = '/devices?type=solar'
      }}
    />
  ),
  
  Devices: () => (
    <EmptyState
      title="No Devices Connected"
      description="Connect your first device to start monitoring your energy consumption and generation."
      icon={DevicePhoneMobileIcon}
      action={{
        label: 'Add Device',
        onClick: () => window.location.href = '/devices'
      }}
    />
  ),
  
  Analytics: () => (
    <EmptyState
      title="No Analytics Data"
      description="Analytics will appear once you have sufficient energy data. Keep monitoring to see insights and recommendations."
      icon={ChartBarIcon}
      showRefresh={true}
      onRefresh={() => window.location.reload()}
    />
  )
}


