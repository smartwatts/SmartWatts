import React, { useState, useEffect } from 'react'
import { usePWA } from '../utils/pwa-utils'
import { XMarkIcon, DevicePhoneMobileIcon, ComputerDesktopIcon } from '@heroicons/react/24/outline'

interface InstallPromptProps {
  className?: string
}

export default function InstallPrompt({ className = '' }: InstallPromptProps) {
  const { canInstall, isInstalled, showInstallPrompt } = usePWA()
  const [showPrompt, setShowPrompt] = useState(false)
  const [isInstalling, setIsInstalling] = useState(false)

  useEffect(() => {
    // Show prompt if app can be installed and is not already installed
    if (canInstall && !isInstalled) {
      // Check if user has previously dismissed the prompt
      const dismissed = localStorage.getItem('pwa-install-dismissed')
      if (!dismissed) {
        setShowPrompt(true)
      }
    }
  }, [canInstall, isInstalled])

  const handleInstall = async () => {
    try {
      setIsInstalling(true)
      const success = await showInstallPrompt()
      if (success) {
        setShowPrompt(false)
      }
    } catch (error) {
      console.error('Install prompt failed:', error)
    } finally {
      setIsInstalling(false)
    }
  }

  const handleDismiss = () => {
    setShowPrompt(false)
    // Remember dismissal for this session
    localStorage.setItem('pwa-install-dismissed', 'true')
  }

  const handleDismissPermanently = () => {
    setShowPrompt(false)
    // Remember dismissal permanently
    localStorage.setItem('pwa-install-dismissed', 'true')
    localStorage.setItem('pwa-install-dismissed-permanent', 'true')
  }

  if (!showPrompt || isInstalled) {
    return null
  }

  return (
    <div className={`fixed bottom-4 left-4 right-4 z-50 max-w-sm mx-auto ${className}`}>
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 p-4">
        <div className="flex items-start space-x-3">
          <div className="flex-shrink-0">
            <div className="w-10 h-10 bg-blue-100 dark:bg-blue-900 rounded-lg flex items-center justify-center">
              <DevicePhoneMobileIcon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
            </div>
          </div>
          
          <div className="flex-1 min-w-0">
            <h3 className="text-sm font-semibold text-gray-900 dark:text-white">
              Install SmartWatts
            </h3>
            <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
              Get quick access to your energy dashboard with offline support.
            </p>
            
            <div className="flex items-center space-x-2 mt-3">
              <button
                onClick={handleInstall}
                disabled={isInstalling}
                className="bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white text-sm font-medium px-3 py-1.5 rounded-md transition-colors duration-200"
              >
                {isInstalling ? 'Installing...' : 'Install'}
              </button>
              
              <button
                onClick={handleDismiss}
                className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 text-sm font-medium px-3 py-1.5 rounded-md transition-colors duration-200"
              >
                Not now
              </button>
            </div>
          </div>
          
          <button
            onClick={handleDismissPermanently}
            className="flex-shrink-0 text-gray-400 hover:text-gray-600 dark:text-gray-500 dark:hover:text-gray-300 transition-colors duration-200"
          >
            <XMarkIcon className="h-5 w-5" />
          </button>
        </div>
        
        <div className="mt-3 pt-3 border-t border-gray-200 dark:border-gray-700">
          <div className="flex items-center space-x-4 text-xs text-gray-500 dark:text-gray-400">
            <div className="flex items-center space-x-1">
              <ComputerDesktopIcon className="h-3 w-3" />
              <span>Desktop</span>
            </div>
            <div className="flex items-center space-x-1">
              <DevicePhoneMobileIcon className="h-3 w-3" />
              <span>Mobile</span>
            </div>
            <span>•</span>
            <span>Offline support</span>
          </div>
        </div>
      </div>
    </div>
  )
}









