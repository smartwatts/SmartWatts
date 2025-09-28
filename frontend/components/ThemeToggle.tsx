import React from 'react'
import { SunIcon, MoonIcon, ComputerDesktopIcon } from '@heroicons/react/24/outline'
import { useTheme } from '../contexts/ThemeContext'

const ThemeToggle: React.FC = () => {
  const { theme, cycleTheme } = useTheme()

  const getThemeIcon = () => {
    switch (theme) {
      case 'light':
        return <SunIcon className="h-5 w-5 text-yellow-500" />
      case 'dark':
        return <MoonIcon className="h-5 w-5 text-blue-600" />
      case 'auto':
        return <ComputerDesktopIcon className="h-5 w-5 text-purple-500" />
      default:
        return <SunIcon className="h-5 w-5 text-yellow-500" />
    }
  }

  const getThemeTooltip = () => {
    switch (theme) {
      case 'light':
        return 'Light Theme'
      case 'dark':
        return 'Dark Theme'
      case 'auto':
        return 'Auto Theme (System)'
      default:
        return 'Toggle Theme'
    }
  }

  return (
    <button
      onClick={cycleTheme}
      className="p-2 rounded-lg bg-gray-100 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 hover:bg-gray-200 dark:hover:bg-gray-700 transition-all duration-300 shadow-sm hover:shadow-md"
      aria-label="Cycle theme"
      title={getThemeTooltip()}
    >
      {getThemeIcon()}
    </button>
  )
}

export default ThemeToggle 