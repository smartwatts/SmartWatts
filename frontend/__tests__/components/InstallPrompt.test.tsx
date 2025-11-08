import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import '@testing-library/jest-dom'
import InstallPrompt from '../../components/InstallPrompt'
import * as pwaUtils from '../../utils/pwa-utils'

jest.mock('../../utils/pwa-utils')

describe('InstallPrompt', () => {
  const mockShowInstallPrompt = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
    localStorage.clear()
    ;(pwaUtils.usePWA as jest.Mock) = jest.fn(() => ({
      canInstall: true,
      isInstalled: false,
      showInstallPrompt: mockShowInstallPrompt,
    }))
  })

  it('renders install prompt when app can be installed', () => {
    render(<InstallPrompt />)

    expect(screen.getByText('Install SmartWatts')).toBeInTheDocument()
    expect(screen.getByText(/Get quick access to your energy dashboard with offline support/)).toBeInTheDocument()
  })

  it('does not render when app is already installed', () => {
    ;(pwaUtils.usePWA as jest.Mock) = jest.fn(() => ({
      canInstall: true,
      isInstalled: true,
      showInstallPrompt: mockShowInstallPrompt,
    }))

    const { container } = render(<InstallPrompt />)
    expect(container.firstChild).toBeNull()
  })

  it('does not render when app cannot be installed', () => {
    ;(pwaUtils.usePWA as jest.Mock) = jest.fn(() => ({
      canInstall: false,
      isInstalled: false,
      showInstallPrompt: mockShowInstallPrompt,
    }))

    const { container } = render(<InstallPrompt />)
    expect(container.firstChild).toBeNull()
  })

  it('does not render when prompt was previously dismissed', () => {
    localStorage.setItem('pwa-install-dismissed', 'true')

    const { container } = render(<InstallPrompt />)
    expect(container.firstChild).toBeNull()
  })

  it('calls showInstallPrompt when install button is clicked', async () => {
    mockShowInstallPrompt.mockResolvedValue(true)

    render(<InstallPrompt />)

    const installButton = screen.getByText('Install')
    fireEvent.click(installButton)

    expect(mockShowInstallPrompt).toHaveBeenCalled()
  })

  it('shows installing state during installation', async () => {
    mockShowInstallPrompt.mockImplementation(() => new Promise((resolve) => setTimeout(() => resolve(true), 100)))

    render(<InstallPrompt />)

    const installButton = screen.getByText('Install')
    await waitFor(() => {
      fireEvent.click(installButton)
    })

    await waitFor(() => {
      expect(screen.getByText('Installing...')).toBeInTheDocument()
    })
    expect(installButton).toBeDisabled()
  })

  it('dismisses prompt when "Not now" is clicked', () => {
    render(<InstallPrompt />)

    const notNowButton = screen.getByText('Not now')
    fireEvent.click(notNowButton)

    expect(localStorage.getItem('pwa-install-dismissed')).toBe('true')
  })

  it('dismisses prompt permanently when close button is clicked', () => {
    render(<InstallPrompt />)

    const closeButton = screen.getAllByRole('button').find(
      (btn) => btn.querySelector('svg')
    )
    if (closeButton) {
      fireEvent.click(closeButton)
    }

    expect(localStorage.getItem('pwa-install-dismissed')).toBe('true')
    expect(localStorage.getItem('pwa-install-dismissed-permanent')).toBe('true')
  })

  it('hides prompt after successful installation', async () => {
    mockShowInstallPrompt.mockResolvedValue(true)

    const { container } = render(<InstallPrompt />)

    const installButton = screen.getByText('Install')
    fireEvent.click(installButton)

    await waitFor(() => {
      expect(container.firstChild).toBeNull()
    })
  })

  it('displays platform information', () => {
    render(<InstallPrompt />)

    expect(screen.getByText('Desktop')).toBeInTheDocument()
    expect(screen.getByText('Mobile')).toBeInTheDocument()
    expect(screen.getByText('Offline support')).toBeInTheDocument()
  })
})

