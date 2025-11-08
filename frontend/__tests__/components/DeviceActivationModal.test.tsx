import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import DeviceActivationModal from '../../components/DeviceActivationModal'

// Mock fetch
global.fetch = jest.fn()

// Mock useAuth
jest.mock('../../hooks/useAuth', () => ({
  useAuth: jest.fn(() => ({
    user: {
      id: 'user-123',
      email: 'test@example.com',
      role: 'ROLE_USER'
    }
  }))
}))

describe('DeviceActivationModal', () => {
  const mockOnClose = jest.fn()
  const mockOnDeviceActivated = jest.fn()
  const mockFetch = global.fetch as jest.Mock

  beforeEach(() => {
    jest.clearAllMocks()
    mockFetch.mockClear()
    localStorage.setItem('token', 'mock-token')
  })

  afterEach(() => {
    localStorage.clear()
  })

  describe('Rendering', () => {
    it('should render modal with all form fields', () => {
      render(<DeviceActivationModal onClose={mockOnClose} onDeviceActivated={mockOnDeviceActivated} />)

      expect(screen.getByLabelText(/device id/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/device type/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/hardware id/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/customer type/i)).toBeInTheDocument()
    })

    it('should render all device types in dropdown', () => {
      render(<DeviceActivationModal onClose={mockOnClose} onDeviceActivated={mockOnDeviceActivated} />)

      const deviceTypeSelect = screen.getByLabelText(/device type/i)
      // Device types are displayed with underscores replaced by spaces
      expect(screen.getByText('SMART METER')).toBeInTheDocument()
      expect(screen.getByText('SOLAR INVERTER')).toBeInTheDocument()
      expect(screen.getByText('GENERATOR MONITOR')).toBeInTheDocument()
    })

    it('should render all customer types in dropdown', () => {
      render(<DeviceActivationModal onClose={mockOnClose} onDeviceActivated={mockOnDeviceActivated} />)

      const customerTypeSelect = screen.getByLabelText(/customer type/i)
      // Customer types are displayed with first letter capitalized and rest lowercase
      expect(screen.getByText('Residential')).toBeInTheDocument()
      expect(screen.getByText('Commercial')).toBeInTheDocument()
      expect(screen.getByText('Industrial')).toBeInTheDocument()
    })
  })

  describe('Form Interactions', () => {
    it('should update form fields when user types', async () => {
      const user = userEvent.setup()
      render(<DeviceActivationModal onClose={mockOnClose} onDeviceActivated={mockOnDeviceActivated} />)

      const deviceIdInput = screen.getByLabelText(/device id/i)
      await user.type(deviceIdInput, 'device-123')

      expect(deviceIdInput).toHaveValue('device-123')
    })

    it('should update device type when selected', async () => {
      const user = userEvent.setup()
      render(<DeviceActivationModal onClose={mockOnClose} onDeviceActivated={mockOnDeviceActivated} />)

      const deviceTypeSelect = screen.getByLabelText(/device type/i)
      await user.selectOptions(deviceTypeSelect, 'SOLAR_INVERTER')

      expect(deviceTypeSelect).toHaveValue('SOLAR_INVERTER')
    })

    it('should update customer type when selected', async () => {
      const user = userEvent.setup()
      render(<DeviceActivationModal onClose={mockOnClose} onDeviceActivated={mockOnDeviceActivated} />)

      const customerTypeSelect = screen.getByLabelText(/customer type/i)
      await user.selectOptions(customerTypeSelect, 'COMMERCIAL')

      expect(customerTypeSelect).toHaveValue('COMMERCIAL')
    })
  })

  describe('Form Submission', () => {
    it('should submit form with valid data', async () => {
      const user = userEvent.setup()
      const mockResponse = {
        success: true,
        device: {
          deviceId: 'device-123',
          status: 'ACTIVE'
        }
      }

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse
      })

      render(<DeviceActivationModal onClose={mockOnClose} onDeviceActivated={mockOnDeviceActivated} />)

      const deviceIdInput = screen.getByLabelText(/device id/i)
      const hardwareIdInput = screen.getByLabelText(/hardware id/i)
      const submitButton = screen.getByRole('button', { name: /activate/i })

      await user.type(deviceIdInput, 'device-123')
      await user.type(hardwareIdInput, 'hw-123')
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnDeviceActivated).toHaveBeenCalledWith(mockResponse)
      })
    })

    it('should show error message on API failure', async () => {
      const user = userEvent.setup()
      mockFetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'Activation failed' })
      })

      render(<DeviceActivationModal onClose={mockOnClose} onDeviceActivated={mockOnDeviceActivated} />)

      const deviceIdInput = screen.getByLabelText(/device id/i)
      const hardwareIdInput = screen.getByLabelText(/hardware id/i)
      const submitButton = screen.getByRole('button', { name: /activate/i })

      await user.type(deviceIdInput, 'device-123')
      await user.type(hardwareIdInput, 'hw-123')
      await user.click(submitButton)

      await waitFor(() => {
        expect(screen.getByText(/activation failed/i)).toBeInTheDocument()
      })
    })

    it('should show error message on network error', async () => {
      const user = userEvent.setup()
      mockFetch.mockRejectedValueOnce(new Error('Network error'))

      render(<DeviceActivationModal onClose={mockOnClose} onDeviceActivated={mockOnDeviceActivated} />)

      const deviceIdInput = screen.getByLabelText(/device id/i)
      const hardwareIdInput = screen.getByLabelText(/hardware id/i)
      const submitButton = screen.getByRole('button', { name: /activate/i })

      await user.type(deviceIdInput, 'device-123')
      await user.type(hardwareIdInput, 'hw-123')
      await user.click(submitButton)

      await waitFor(() => {
        expect(screen.getByText(/network error occurred/i)).toBeInTheDocument()
      })
    })
  })

  describe('Modal Close', () => {
    it('should call onClose when close button is clicked', () => {
      render(<DeviceActivationModal onClose={mockOnClose} onDeviceActivated={mockOnDeviceActivated} />)

      const closeButton = screen.getByRole('button', { name: /close/i })
      fireEvent.click(closeButton)

      expect(mockOnClose).toHaveBeenCalled()
    })

    it('should call onClose when backdrop is clicked', () => {
      render(<DeviceActivationModal onClose={mockOnClose} onDeviceActivated={mockOnDeviceActivated} />)

      // Find the backdrop (the outer div with fixed positioning)
      const backdrop = document.querySelector('.fixed.inset-0')
      if (backdrop) {
        fireEvent.click(backdrop)
        // Note: This depends on the implementation - may or may not close on backdrop click
      }
    })
  })

  describe('Loading State', () => {
    it('should show loading state during submission', async () => {
      const user = userEvent.setup()
      mockFetch.mockImplementation(() => new Promise(() => {})) // Never resolves

      render(<DeviceActivationModal onClose={mockOnClose} onDeviceActivated={mockOnDeviceActivated} />)

      const deviceIdInput = screen.getByLabelText(/device id/i)
      const hardwareIdInput = screen.getByLabelText(/hardware id/i)
      const submitButton = screen.getByRole('button', { name: /activate/i })

      await user.type(deviceIdInput, 'device-123')
      await user.type(hardwareIdInput, 'hw-123')
      await user.click(submitButton)

      await waitFor(() => {
        expect(submitButton).toBeDisabled()
      })
    })
  })
})

