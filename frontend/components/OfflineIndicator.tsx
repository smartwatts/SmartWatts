import React, { useState, useEffect } from 'react'
import { WifiIcon, ExclamationTriangleIcon } from '@heroicons/react/24/outline'
import { usePWA } from '../utils/pwa-utils'

interface OfflineIndicatorProps {
  className?: string
}

export default function OfflineIndicator({ className = '' }: OfflineIndicatorProps) {
  const { isOnline } = usePWA()
  const [showIndicator, setShowIndicator] = useState(false)

  useEffect(() => {
    if (!isOnline) {
      setShowIndicator(true)
    } else {
      // Hide indicator after a short delay when coming back online
      const timer = setTimeout(() => {
        setShowIndicator(false)
      }, 2000)
      return () => clearTimeout(timer)
    }
  }, [isOnline])

  if (!showIndicator) {
    return null
  }

  return (
    <div className={`fixed top-0 left-0 right-0 z-50 ${className}`} data-testid="offline-indicator">
      <div className={`px-4 py-2 text-center text-sm font-medium transition-all duration-300 ${
        isOnline 
          ? 'bg-green-100 text-green-800 border-b border-green-200' 
          : 'bg-yellow-100 text-yellow-800 border-b border-yellow-200'
      }`}>
        <div className="flex items-center justify-center space-x-2">
          {isOnline ? (
            <>
              <WifiIcon className="h-4 w-4" />
              <span>You're back online! Data is syncing...</span>
            </>
          ) : (
            <>
              <ExclamationTriangleIcon className="h-4 w-4" />
              <span>You're offline. Some features may not be available.</span>
            </>
          )}
        </div>
      </div>
    </div>
  )
}


