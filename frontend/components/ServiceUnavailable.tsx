import React from 'react'
import { ExclamationTriangleIcon, ArrowPathIcon, WifiIcon } from '@heroicons/react/24/outline'

interface ServiceUnavailableProps {
  service?: string
  onRetry?: () => void
  showOfflineIndicator?: boolean
}

export default function ServiceUnavailable({ 
  service = 'Service', 
  onRetry,
  showOfflineIndicator = false 
}: ServiceUnavailableProps) {
  const handleRetry = () => {
    if (onRetry) {
      onRetry()
    } else {
      window.location.reload()
    }
  }

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6 text-center">
      <div className="flex justify-center mb-4">
        {showOfflineIndicator ? (
          <WifiIcon className="h-12 w-12 text-yellow-500" />
        ) : (
          <ExclamationTriangleIcon className="h-12 w-12 text-red-500" />
        )}
      </div>
      
      <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
        {showOfflineIndicator ? 'You\'re Offline' : `${service} Unavailable`}
      </h3>
      
      <p className="text-gray-600 dark:text-gray-400 mb-4">
        {showOfflineIndicator 
          ? 'Please check your internet connection and try again.'
          : `${service} is temporarily unavailable. Please try again in a few minutes.`
        }
      </p>
      
      <div className="space-y-3">
        <button
          onClick={handleRetry}
          className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200 flex items-center justify-center space-x-2"
        >
          <ArrowPathIcon className="h-4 w-4" />
          <span>Try Again</span>
        </button>
        
        {!showOfflineIndicator && (
          <button
            onClick={() => window.location.href = '/dashboard'}
            className="w-full bg-gray-600 hover:bg-gray-700 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200"
          >
            Go to Dashboard
          </button>
        )}
      </div>
    </div>
  )
}








