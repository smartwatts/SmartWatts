import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import '@testing-library/jest-dom'
import ServiceUnavailable from '../../components/ServiceUnavailable'

describe('ServiceUnavailable', () => {
  const mockOnRetry = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
    delete (window as any).location
    ;(window as any).location = { reload: jest.fn(), href: '' }
  })

  it('renders service unavailable message', () => {
    render(<ServiceUnavailable service="API Service" />)

    expect(screen.getByText('API Service Unavailable')).toBeInTheDocument()
    expect(screen.getByText(/API Service is temporarily unavailable/)).toBeInTheDocument()
  })

  it('renders with default service name', () => {
    render(<ServiceUnavailable />)

    expect(screen.getByText('Service Unavailable')).toBeInTheDocument()
  })

  it('shows offline indicator when showOfflineIndicator is true', () => {
    render(<ServiceUnavailable showOfflineIndicator={true} />)

    expect(screen.getByText("You're Offline")).toBeInTheDocument()
    expect(screen.getByText(/Please check your internet connection/)).toBeInTheDocument()
  })

  it('calls onRetry when retry button is clicked', () => {
    render(<ServiceUnavailable onRetry={mockOnRetry} />)

    const retryButton = screen.getByText('Try Again')
    fireEvent.click(retryButton)

    expect(mockOnRetry).toHaveBeenCalled()
  })

  it('reloads page when retry is clicked without onRetry callback', () => {
    render(<ServiceUnavailable />)

    const retryButton = screen.getByText('Try Again')
    fireEvent.click(retryButton)

    expect(window.location.reload).toHaveBeenCalled()
  })

  it('shows "Go to Dashboard" button when not offline', () => {
    render(<ServiceUnavailable />)

    expect(screen.getByText('Go to Dashboard')).toBeInTheDocument()
  })

  it('does not show "Go to Dashboard" button when offline', () => {
    render(<ServiceUnavailable showOfflineIndicator={true} />)

    expect(screen.queryByText('Go to Dashboard')).not.toBeInTheDocument()
  })

  it('navigates to dashboard when "Go to Dashboard" is clicked', () => {
    render(<ServiceUnavailable />)

    const dashboardButton = screen.getByText('Go to Dashboard')
    fireEvent.click(dashboardButton)

    expect(window.location.href).toBe('/dashboard')
  })

  it('displays correct icon for service unavailable', () => {
    const { container } = render(<ServiceUnavailable />)
    const icon = container.querySelector('svg')
    expect(icon).toBeInTheDocument()
  })

  it('displays correct icon for offline state', () => {
    const { container } = render(<ServiceUnavailable showOfflineIndicator={true} />)
    const icon = container.querySelector('svg')
    expect(icon).toBeInTheDocument()
  })
})

