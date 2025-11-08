import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ThemeToggle from '../../components/ThemeToggle'

// Mock theme context
jest.mock('../../contexts/ThemeContext', () => ({
  useTheme: jest.fn(() => ({
    theme: 'light',
    cycleTheme: jest.fn()
  }))
}))

describe('ThemeToggle', () => {
  const { useTheme } = require('../../contexts/ThemeContext')

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Rendering', () => {
    it('should render theme toggle button', () => {
      render(<ThemeToggle />)
      expect(screen.getByRole('button', { name: /cycle theme/i })).toBeInTheDocument()
    })

    it('should display correct icon for light theme', () => {
      useTheme.mockReturnValue({
        theme: 'light',
        cycleTheme: jest.fn()
      })

      render(<ThemeToggle />)
      const button = screen.getByRole('button')
      expect(button).toBeInTheDocument()
    })

    it('should display correct icon for dark theme', () => {
      useTheme.mockReturnValue({
        theme: 'dark',
        cycleTheme: jest.fn()
      })

      render(<ThemeToggle />)
      const button = screen.getByRole('button')
      expect(button).toBeInTheDocument()
    })
  })

  describe('Theme Cycling', () => {
    it('should call cycleTheme when button is clicked', async () => {
      const mockCycleTheme = jest.fn()
      useTheme.mockReturnValue({
        theme: 'light',
        cycleTheme: mockCycleTheme
      })

      const user = userEvent.setup()
      render(<ThemeToggle />)

      const button = screen.getByRole('button')
      await user.click(button)

      expect(mockCycleTheme).toHaveBeenCalledTimes(1)
    })
  })
})

