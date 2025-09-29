import React from 'react'
import { useTheme } from '../contexts/ThemeContext'
import { useDashboardTheme } from '../contexts/DashboardThemeContext'
import { SunIcon, MoonIcon, ComputerDesktopIcon } from '@heroicons/react/24/outline'

interface ThemeOption {
  id: string
  name: string
  appTheme: 'light' | 'dark' | 'auto'
  dashboardStyle: 'dark' | 'appliance-monitoring' | 'system-default'
  icon: React.ComponentType<{ className?: string }>
}

const themeOptions: ThemeOption[] = [
  {
    id: 'dark',
    name: 'Dark Theme',
    appTheme: 'dark',
    dashboardStyle: 'dark',
    icon: MoonIcon
  },
  {
    id: 'appliance-monitoring',
    name: 'Appliance Monitoring',
    appTheme: 'light',
    dashboardStyle: 'appliance-monitoring',
    icon: SunIcon
  },
  {
    id: 'system-default',
    name: 'System Default',
    appTheme: 'auto',
    dashboardStyle: 'system-default',
    icon: SunIcon
  }
]

export default function ThemeSelector() {
  const { theme, setTheme } = useTheme()
  const { dashboardStyle, setDashboardStyle } = useDashboardTheme()

  const handleThemeChange = (selectedThemeId: string) => {
    const selectedTheme = themeOptions.find(t => t.id === selectedThemeId)
    if (selectedTheme) {
      setTheme(selectedTheme.appTheme)
      setDashboardStyle(selectedTheme.dashboardStyle)
    }
  }

  const getCurrentThemeId = () => {
    // Find the theme that matches current app theme and dashboard style
    const matchingTheme = themeOptions.find(t => 
      t.appTheme === theme && t.dashboardStyle === dashboardStyle
    )
    
    if (matchingTheme) {
      return matchingTheme.id
    }
    
    // Fallback logic
    if (theme === 'dark') {
      return 'dark'
    } else {
      // For light theme, return appliance-monitoring
      return 'appliance-monitoring'
    }
  }

  const getThemeIcon = (themeId: string) => {
    const theme = themeOptions.find(t => t.id === themeId)
    return theme ? <theme.icon className="h-4 w-4 text-blue-500" /> : <ComputerDesktopIcon className="h-4 w-4 text-gray-500" />
  }

  return (
    <div className="relative">
      <select
        value={getCurrentThemeId()}
        onChange={(e) => handleThemeChange(e.target.value)}
        className="appearance-none bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 pr-8 text-sm text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent hover:border-gray-400 dark:hover:border-gray-500 transition-colors cursor-pointer"
      >
        {themeOptions.map((themeOption) => (
          <option key={themeOption.id} value={themeOption.id} className="text-gray-900 dark:text-white">
            {themeOption.name}
          </option>
        ))}
      </select>
      <div className="absolute inset-y-0 right-0 flex items-center pr-2 pointer-events-none">
        {getThemeIcon(getCurrentThemeId())}
      </div>
    </div>
  )
} 