import React, { createContext, useContext, useState, useEffect } from 'react'

type DashboardStyle = 'dark' | 'appliance-monitoring' | 'system-default' | 'landing-page' | 'professional-intelligence'

interface DashboardThemeContextType {
  dashboardStyle: DashboardStyle
  setDashboardStyle: (style: DashboardStyle) => void
}

const DashboardThemeContext = createContext<DashboardThemeContextType | undefined>(undefined)

export const useDashboardTheme = () => {
  const context = useContext(DashboardThemeContext)
  if (context === undefined) {
    throw new Error('useDashboardTheme must be used within a DashboardThemeProvider')
  }
  return context
}

export const DashboardThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [dashboardStyle, setDashboardStyle] = useState<DashboardStyle>(() => {
    // Initialize from localStorage if available, otherwise use 'professional-intelligence' as default
    if (typeof window !== 'undefined') {
      const savedStyle = localStorage.getItem('dashboardStyle') as DashboardStyle
      return savedStyle && ['dark', 'appliance-monitoring', 'system-default', 'landing-page', 'professional-intelligence'].includes(savedStyle) ? savedStyle : 'appliance-monitoring'
    }
    return 'appliance-monitoring'
  })

  // Load dashboard style from localStorage on mount (client-side only)
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const savedStyle = localStorage.getItem('dashboardStyle') as DashboardStyle
      if (savedStyle && ['dark', 'appliance-monitoring', 'system-default', 'landing-page', 'professional-intelligence'].includes(savedStyle)) {
        setDashboardStyle(savedStyle)
      }
    }
  }, [])

  // Save dashboard style to localStorage whenever it changes
  useEffect(() => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('dashboardStyle', dashboardStyle)
    }
  }, [dashboardStyle])

  const value = {
    dashboardStyle,
    setDashboardStyle,
  }

  return (
    <DashboardThemeContext.Provider value={value}>
      {children}
    </DashboardThemeContext.Provider>
  )
}
