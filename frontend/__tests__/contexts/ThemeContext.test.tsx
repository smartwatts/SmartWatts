import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import { renderHook, act } from '@testing-library/react'
import { ThemeProvider, useTheme } from '../../contexts/ThemeContext'

// Mock matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),
    removeListener: jest.fn(),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
})

describe('ThemeContext', () => {
  beforeEach(() => {
    localStorage.clear()
    jest.clearAllMocks()
  })

  describe('ThemeProvider', () => {
    it('should provide theme context to children', () => {
      const TestComponent = () => {
        const { theme } = useTheme()
        return <div data-testid="theme">{theme}</div>
      }

      render(
        <ThemeProvider>
          <TestComponent />
        </ThemeProvider>
      )

      expect(screen.getByTestId('theme')).toHaveTextContent('dark')
    })

    it('should load theme from localStorage on mount', () => {
      localStorage.setItem('theme', 'light')

      const TestComponent = () => {
        const { theme } = useTheme()
        return <div data-testid="theme">{theme}</div>
      }

      render(
        <ThemeProvider>
          <TestComponent />
        </ThemeProvider>
      )

      expect(screen.getByTestId('theme')).toHaveTextContent('light')
    })

    it('should save theme to localStorage when changed', () => {
      const TestComponent = () => {
        const { theme, setTheme } = useTheme()
        return (
          <div>
            <div data-testid="theme">{theme}</div>
            <button onClick={() => setTheme('light')}>Set Light</button>
          </div>
        )
      }

      render(
        <ThemeProvider>
          <TestComponent />
        </ThemeProvider>
      )

      const button = screen.getByText('Set Light')
      fireEvent.click(button)

      expect(localStorage.getItem('theme')).toBe('light')
    })
  })

  describe('useTheme Hook', () => {
    it('should throw error when used outside provider', () => {
      // Suppress console.error for this test
      const originalError = console.error
      console.error = jest.fn()

      expect(() => {
        renderHook(() => useTheme())
      }).toThrow('useTheme must be used within a ThemeProvider')

      console.error = originalError
    })

    it('should return current theme', () => {
      const { result } = renderHook(() => useTheme(), {
        wrapper: ThemeProvider
      })

      expect(result.current.theme).toBe('dark')
      expect(result.current.currentTheme).toBe('dark')
    })

    it('should update theme when setTheme is called', () => {
      const { result } = renderHook(() => useTheme(), {
        wrapper: ThemeProvider
      })

      act(() => {
        result.current.setTheme('light')
      })

      expect(result.current.theme).toBe('light')
      expect(result.current.currentTheme).toBe('light')
    })

    it('should cycle through themes', () => {
      const { result } = renderHook(() => useTheme(), {
        wrapper: ThemeProvider
      })

      // Start with dark (index 1 in ['light', 'dark', 'auto'])
      expect(result.current.theme).toBe('dark')

      // Cycle to auto (next index: 2)
      act(() => {
        result.current.cycleTheme()
      })
      expect(result.current.theme).toBe('auto')

      // Cycle to light (next index: 0)
      act(() => {
        result.current.cycleTheme()
      })
      expect(result.current.theme).toBe('light')

      // Cycle back to dark (next index: 1)
      act(() => {
        result.current.cycleTheme()
      })
      expect(result.current.theme).toBe('dark')
    })

    it('should apply theme to document element', () => {
      renderHook(() => useTheme(), {
        wrapper: ThemeProvider
      })

      expect(document.documentElement.classList.contains('dark')).toBe(true)
    })

    it('should use system preference when theme is auto', () => {
      const mockMatchMedia = jest.fn().mockImplementation(query => ({
        matches: true, // System prefers dark
        media: query,
        onchange: null,
        addListener: jest.fn(),
        removeListener: jest.fn(),
        addEventListener: jest.fn(),
        removeEventListener: jest.fn(),
        dispatchEvent: jest.fn(),
      }))

      window.matchMedia = mockMatchMedia

      const { result } = renderHook(() => useTheme(), {
        wrapper: ThemeProvider
      })

      act(() => {
        result.current.setTheme('auto')
      })

      expect(result.current.currentTheme).toBe('dark')
    })
  })
})

