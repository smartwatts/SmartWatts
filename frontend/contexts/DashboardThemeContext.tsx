import React, { createContext, useContext, useState, useEffect } from 'react'

type DashboardStyle = 'dark' | 'warm-beige' | 'landing-page'

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
    // Initialize from localStorage if available, otherwise use 'dark' as default
    if (typeof window !== 'undefined') {
      const savedStyle = localStorage.getItem('dashboardStyle') as DashboardStyle
      return savedStyle && ['dark', 'warm-beige', 'landing-page'].includes(savedStyle) ? savedStyle : 'dark'
    }
    return 'dark'
  })

  // Load dashboard style from localStorage on mount (client-side only)
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const savedStyle = localStorage.getItem('dashboardStyle') as DashboardStyle
      if (savedStyle && ['dark', 'warm-beige', 'landing-page'].includes(savedStyle)) {
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
