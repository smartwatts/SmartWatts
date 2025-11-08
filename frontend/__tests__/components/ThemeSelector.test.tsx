import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ThemeSelector from '../../components/ThemeSelector'

// Mock theme contexts
jest.mock('../../contexts/ThemeContext', () => ({
  useTheme: jest.fn(() => ({
    theme: 'light',
    setTheme: jest.fn()
  }))
}))

jest.mock('../../contexts/DashboardThemeContext', () => ({
  useDashboardTheme: jest.fn(() => ({
    dashboardStyle: 'system-default',
    setDashboardStyle: jest.fn()
  }))
}))

describe('ThemeSelector', () => {
  const { useTheme } = require('../../contexts/ThemeContext')
  const { useDashboardTheme } = require('../../contexts/DashboardThemeContext')

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Rendering', () => {
    it('should render theme selector', () => {
      render(<ThemeSelector />)
      expect(screen.getByRole('combobox')).toBeInTheDocument()
    })

    it('should display all theme options', () => {
      render(<ThemeSelector />)
      const select = screen.getByRole('combobox')
      expect(select).toHaveValue('system-default')
    })
  })

  describe('Theme Selection', () => {
    it('should call setTheme and setDashboardStyle when theme changes', async () => {
      const mockSetTheme = jest.fn()
      const mockSetDashboardStyle = jest.fn()

      useTheme.mockReturnValue({
        theme: 'light',
        setTheme: mockSetTheme
      })

      useDashboardTheme.mockReturnValue({
        dashboardStyle: 'system-default',
        setDashboardStyle: mockSetDashboardStyle
      })

      const user = userEvent.setup()
      render(<ThemeSelector />)

      const select = screen.getByRole('combobox')
      await user.selectOptions(select, 'dark')

      expect(mockSetTheme).toHaveBeenCalledWith('dark')
      expect(mockSetDashboardStyle).toHaveBeenCalledWith('dark')
    })
  })
})

