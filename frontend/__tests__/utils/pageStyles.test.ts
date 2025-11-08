import { renderHook } from '@testing-library/react'
import { usePageStyles } from '../../utils/pageStyles'
import { DashboardThemeProvider } from '../../contexts/DashboardThemeContext'

// Mock DashboardThemeContext
jest.mock('../../contexts/DashboardThemeContext', () => ({
  useDashboardTheme: jest.fn(() => ({
    dashboardStyle: 'system-default',
    setDashboardStyle: jest.fn()
  })),
  DashboardThemeProvider: ({ children }: any) => {
    const React = require('react')
    return React.createElement('div', null, children)
  }
}))

describe('usePageStyles', () => {
  const { useDashboardTheme } = require('../../contexts/DashboardThemeContext')

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Style Variants', () => {
    it('should return landing-page styles', () => {
      useDashboardTheme.mockReturnValue({
        dashboardStyle: 'landing-page',
        setDashboardStyle: jest.fn()
      })

      const { result } = renderHook(() => usePageStyles(), {
        wrapper: DashboardThemeProvider
      })

      expect(result.current.header).toContain('bg-gradient-to-br')
      expect(result.current.card).toContain('bg-white')
      expect(result.current.kpiCard).toContain('bg-gradient-to-br')
    })

    it('should return system-theme styles', () => {
      useDashboardTheme.mockReturnValue({
        dashboardStyle: 'system-theme',
        setDashboardStyle: jest.fn()
      })

      const { result } = renderHook(() => usePageStyles(), {
        wrapper: DashboardThemeProvider
      })

      expect(result.current.header).toContain('bg-gradient-to-r')
      expect(result.current.card).toContain('dark:bg-gray-800')
    })

    it('should return system-default styles', () => {
      useDashboardTheme.mockReturnValue({
        dashboardStyle: 'system-default',
        setDashboardStyle: jest.fn()
      })

      const { result } = renderHook(() => usePageStyles(), {
        wrapper: DashboardThemeProvider
      })

      expect(result.current.header).toContain('bg-white')
      expect(result.current.card).toContain('bg-white')
    })

    it('should return dark theme styles as default', () => {
      useDashboardTheme.mockReturnValue({
        dashboardStyle: 'dark',
        setDashboardStyle: jest.fn()
      })

      const { result } = renderHook(() => usePageStyles(), {
        wrapper: DashboardThemeProvider
      })

      expect(result.current.header).toContain('bg-gradient-to-r')
      expect(result.current.card).toContain('bg-gray-800')
    })

    it('should return professional-intelligence styles', () => {
      useDashboardTheme.mockReturnValue({
        dashboardStyle: 'professional-intelligence',
        setDashboardStyle: jest.fn()
      })

      const { result } = renderHook(() => usePageStyles(), {
        wrapper: DashboardThemeProvider
      })

      // Should fallback to dark theme
      expect(result.current.header).toBeDefined()
      expect(result.current.card).toBeDefined()
    })
  })

  describe('Style Properties', () => {
    it('should return all required style properties', () => {
      useDashboardTheme.mockReturnValue({
        dashboardStyle: 'system-default',
        setDashboardStyle: jest.fn()
      })

      const { result } = renderHook(() => usePageStyles(), {
        wrapper: DashboardThemeProvider
      })

      expect(result.current).toHaveProperty('header')
      expect(result.current).toHaveProperty('headerText')
      expect(result.current).toHaveProperty('card')
      expect(result.current).toHaveProperty('cardIcon')
      expect(result.current).toHaveProperty('chart')
      expect(result.current).toHaveProperty('kpiCard')
      expect(result.current).toHaveProperty('kpiCardAlt')
      expect(result.current).toHaveProperty('kpiCardWarm')
      expect(result.current).toHaveProperty('kpiCardPurple')
      expect(result.current).toHaveProperty('section')
      expect(result.current).toHaveProperty('button')
      expect(result.current).toHaveProperty('buttonSecondary')
      expect(result.current).toHaveProperty('input')
      expect(result.current).toHaveProperty('table')
      expect(result.current).toHaveProperty('tableHeader')
      expect(result.current).toHaveProperty('tableRow')
    })
  })

  describe('Error Handling', () => {
    it('should return fallback styles when context is not available', () => {
      useDashboardTheme.mockImplementation(() => {
        throw new Error('Context not available')
      })

      const { result } = renderHook(() => usePageStyles())

      // Should return fallback dark theme styles
      expect(result.current.header).toBeDefined()
      expect(result.current.card).toBeDefined()
      expect(result.current.header).toContain('bg-gradient-to-r')
    })
  })
})

