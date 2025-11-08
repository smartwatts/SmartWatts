import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import { renderHook, act } from '@testing-library/react'
import { DashboardThemeProvider, useDashboardTheme } from '../../contexts/DashboardThemeContext'

describe('DashboardThemeContext', () => {
  beforeEach(() => {
    localStorage.clear()
    jest.clearAllMocks()
  })

  describe('DashboardThemeProvider', () => {
    it('should provide dashboard theme context to children', () => {
      const TestComponent = () => {
        const { dashboardStyle } = useDashboardTheme()
        return <div data-testid="dashboard-style">{dashboardStyle}</div>
      }

      render(
        <DashboardThemeProvider>
          <TestComponent />
        </DashboardThemeProvider>
      )

      expect(screen.getByTestId('dashboard-style')).toHaveTextContent('system-default')
    })

    it('should load dashboard style from localStorage on mount', () => {
      localStorage.setItem('dashboardStyle', 'professional-intelligence')

      const TestComponent = () => {
        const { dashboardStyle } = useDashboardTheme()
        return <div data-testid="dashboard-style">{dashboardStyle}</div>
      }

      render(
        <DashboardThemeProvider>
          <TestComponent />
        </DashboardThemeProvider>
      )

      expect(screen.getByTestId('dashboard-style')).toHaveTextContent('professional-intelligence')
    })

    it('should save dashboard style to localStorage when changed', () => {
      const TestComponent = () => {
        const { dashboardStyle, setDashboardStyle } = useDashboardTheme()
        return (
          <div>
            <div data-testid="dashboard-style">{dashboardStyle}</div>
            <button onClick={() => setDashboardStyle('professional-intelligence')}>
              Set Professional
            </button>
          </div>
        )
      }

      render(
        <DashboardThemeProvider>
          <TestComponent />
        </DashboardThemeProvider>
      )

      const button = screen.getByText('Set Professional')
      fireEvent.click(button)

      expect(localStorage.getItem('dashboardStyle')).toBe('professional-intelligence')
    })

    it('should use default style when localStorage value is invalid', () => {
      localStorage.setItem('dashboardStyle', 'invalid-style')

      const TestComponent = () => {
        const { dashboardStyle } = useDashboardTheme()
        return <div data-testid="dashboard-style">{dashboardStyle}</div>
      }

      render(
        <DashboardThemeProvider>
          <TestComponent />
        </DashboardThemeProvider>
      )

      expect(screen.getByTestId('dashboard-style')).toHaveTextContent('system-default')
    })
  })

  describe('useDashboardTheme Hook', () => {
    it('should throw error when used outside provider', () => {
      // Suppress console.error for this test
      const originalError = console.error
      console.error = jest.fn()

      expect(() => {
        renderHook(() => useDashboardTheme())
      }).toThrow('useDashboardTheme must be used within a DashboardThemeProvider')

      console.error = originalError
    })

    it('should return current dashboard style', () => {
      const { result } = renderHook(() => useDashboardTheme(), {
        wrapper: DashboardThemeProvider
      })

      expect(result.current.dashboardStyle).toBe('system-default')
    })

    it('should update dashboard style when setDashboardStyle is called', () => {
      const { result } = renderHook(() => useDashboardTheme(), {
        wrapper: DashboardThemeProvider
      })

      act(() => {
        result.current.setDashboardStyle('professional-intelligence')
      })

      expect(result.current.dashboardStyle).toBe('professional-intelligence')
      expect(localStorage.getItem('dashboardStyle')).toBe('professional-intelligence')
    })

    it('should support all dashboard style variants', () => {
      const { result } = renderHook(() => useDashboardTheme(), {
        wrapper: DashboardThemeProvider
      })

      const styles = ['dark', 'system-theme', 'system-default', 'landing-page', 'professional-intelligence']

      styles.forEach(style => {
        act(() => {
          result.current.setDashboardStyle(style as any)
        })
        expect(result.current.dashboardStyle).toBe(style)
      })
    })
  })
})

