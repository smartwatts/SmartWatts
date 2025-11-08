import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import ErrorBoundary from '../../components/ErrorBoundary'

// Component that throws an error
const ThrowError = ({ shouldThrow }: { shouldThrow: boolean }) => {
  if (shouldThrow) {
    throw new Error('Test error')
  }
  return <div>No error</div>
}

describe('ErrorBoundary', () => {
  // Suppress console.error for error boundary tests
  const originalError = console.error
  beforeAll(() => {
    console.error = jest.fn()
  })

  afterAll(() => {
    console.error = originalError
  })

  describe('Rendering', () => {
    it('should render children when no error', () => {
      render(
        <ErrorBoundary>
          <div>Test content</div>
        </ErrorBoundary>
      )
      expect(screen.getByText('Test content')).toBeInTheDocument()
    })

    it('should render error UI when error occurs', () => {
      render(
        <ErrorBoundary>
          <ThrowError shouldThrow={true} />
        </ErrorBoundary>
      )
      expect(screen.getByText(/something went wrong/i)).toBeInTheDocument()
    })

    it('should render custom fallback when provided', () => {
      const customFallback = <div>Custom error message</div>
      render(
        <ErrorBoundary fallback={customFallback}>
          <ThrowError shouldThrow={true} />
        </ErrorBoundary>
      )
      expect(screen.getByText('Custom error message')).toBeInTheDocument()
    })
  })

  describe('Error Recovery', () => {
    it('should retry when retry button is clicked', async () => {
      // Create a component that can be controlled
      let shouldThrow = true
      const ThrowErrorControlled = () => {
        if (shouldThrow) {
          throw new Error('Test error')
        }
        return <div>No error</div>
      }

      const { rerender } = render(
        <ErrorBoundary>
          <ThrowErrorControlled />
        </ErrorBoundary>
      )

      expect(screen.getByText(/something went wrong/i)).toBeInTheDocument()

      // Set shouldThrow to false before clicking retry
      shouldThrow = false
      
      const retryButton = screen.getByRole('button', { name: /try again/i })
      fireEvent.click(retryButton)

      // Re-render with shouldThrow=false after retry
      rerender(
        <ErrorBoundary>
          <ThrowErrorControlled />
        </ErrorBoundary>
      )

      // Wait for the component to render after error boundary resets
      await waitFor(() => {
        // The error boundary resets state, so children should render
        expect(screen.queryByText(/something went wrong/i)).not.toBeInTheDocument()
        expect(screen.getByText('No error')).toBeInTheDocument()
      }, { timeout: 2000 })
    })
  })

  describe('Error Details', () => {
    it('should show error details in development mode', () => {
      const originalEnv = process.env.NODE_ENV
      process.env.NODE_ENV = 'development'

      render(
        <ErrorBoundary>
          <ThrowError shouldThrow={true} />
        </ErrorBoundary>
      )

      expect(screen.getByText(/error details/i)).toBeInTheDocument()

      process.env.NODE_ENV = originalEnv
    })
  })
})

