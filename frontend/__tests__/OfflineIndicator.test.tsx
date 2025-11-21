import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import OfflineIndicator from '../components/OfflineIndicator'

// Mock the PWA utils
const mockUsePWA = jest.fn()
jest.mock('../utils/pwa-utils', () => ({
  usePWA: () => mockUsePWA()
}))

describe('OfflineIndicator Component', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('renders nothing when online', () => {
    mockUsePWA.mockReturnValue({
      isOnline: true
    })

    const { container } = render(<OfflineIndicator />)
    expect(container.firstChild).toBeNull()
  })

  it('shows offline indicator when offline', () => {
    mockUsePWA.mockReturnValue({
      isOnline: false
    })

    render(<OfflineIndicator />)
    
    expect(screen.getByText("You're offline. Some features may not be available.")).toBeInTheDocument()
    expect(screen.getByTestId('offline-indicator')).toBeInTheDocument()
  })

  it('shows online indicator when coming back online', async () => {
    // Start offline
    mockUsePWA.mockReturnValue({
      isOnline: false
    })

    const { rerender } = render(<OfflineIndicator />)
    
    expect(screen.getByText("You're offline. Some features may not be available.")).toBeInTheDocument()

    // Come back online
    mockUsePWA.mockReturnValue({
      isOnline: true
    })

    rerender(<OfflineIndicator />)
    
    await waitFor(() => {
      expect(screen.getByText("You're back online! Data is syncing...")).toBeInTheDocument()
    })
  })

  it('applies custom className', () => {
    mockUsePWA.mockReturnValue({
      isOnline: false
    })

    render(<OfflineIndicator className="custom-class" />)
    
    const indicator = screen.getByTestId('offline-indicator')
    expect(indicator).toHaveClass('custom-class')
  })

  it('shows correct icons for online/offline states', () => {
    mockUsePWA.mockReturnValue({
      isOnline: false
    })

    render(<OfflineIndicator />)
    
    // Should show warning icon for offline
    expect(screen.getByTestId('offline-indicator')).toBeInTheDocument()
  })
})










