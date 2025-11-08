import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AddDeviceModal from '../../components/AddDeviceModal'

// Mock fetch
global.fetch = jest.fn()

describe('AddDeviceModal', () => {
  const mockOnClose = jest.fn()
  const mockOnDeviceAdded = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
    ;(global.fetch as jest.Mock).mockClear()
  })

  describe('Rendering', () => {
    it('should not render when isOpen is false', () => {
      render(<AddDeviceModal isOpen={false} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      expect(screen.queryByText('Add New Device')).not.toBeInTheDocument()
    })

    it('should render when isOpen is true', () => {
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      expect(screen.getByText('Add New Device')).toBeInTheDocument()
    })

    it('should render all form fields', () => {
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      expect(screen.getByLabelText(/device name/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/device id/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/serial number/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/manufacturer/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/model/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/device type/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/protocol/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/location/i)).toBeInTheDocument()
      // Use getAllByLabelText since there are two "Notes" fields (Notes and Verification Notes)
      const notesFields = screen.getAllByLabelText(/notes/i)
      expect(notesFields.length).toBeGreaterThan(0)
    })

    it('should render all device types in dropdown', () => {
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const deviceTypeSelect = screen.getByLabelText(/device type/i)
      fireEvent.click(deviceTypeSelect)
      
      expect(screen.getByText('Smart Meter')).toBeInTheDocument()
      expect(screen.getByText('Solar Inverter')).toBeInTheDocument()
      expect(screen.getByText('Generator Monitor')).toBeInTheDocument()
      expect(screen.getByText('Custom Device')).toBeInTheDocument()
    })

    it('should render all protocols in dropdown', () => {
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const protocolSelect = screen.getByLabelText(/protocol/i)
      fireEvent.click(protocolSelect)
      
      expect(screen.getByText('MQTT')).toBeInTheDocument()
      expect(screen.getByText('Modbus TCP')).toBeInTheDocument()
      expect(screen.getByText('Modbus RTU')).toBeInTheDocument()
      expect(screen.getByText('HTTP REST')).toBeInTheDocument()
      expect(screen.getByText('WebSocket')).toBeInTheDocument()
      expect(screen.getByText('Custom')).toBeInTheDocument()
    })
  })

  describe('Form Interactions', () => {
    it('should update form fields when user types', async () => {
      const user = userEvent.setup()
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const nameInput = screen.getByLabelText(/device name/i)
      await user.type(nameInput, 'Test Device')
      
      expect(nameInput).toHaveValue('Test Device')
    })

    it('should update device type when selected', async () => {
      const user = userEvent.setup()
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const deviceTypeSelect = screen.getByLabelText(/device type/i)
      await user.selectOptions(deviceTypeSelect, 'SMART_METER')
      
      expect(deviceTypeSelect).toHaveValue('SMART_METER')
    })

    it('should update protocol when selected', async () => {
      const user = userEvent.setup()
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const protocolSelect = screen.getByLabelText(/protocol/i)
      await user.selectOptions(protocolSelect, 'MQTT')
      
      expect(protocolSelect).toHaveValue('MQTT')
    })

    it('should close modal when close button is clicked', () => {
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const closeButton = screen.getByRole('button', { name: /close/i })
      fireEvent.click(closeButton)
      
      expect(mockOnClose).toHaveBeenCalledTimes(1)
    })

    it('should close modal when cancel button is clicked', () => {
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const cancelButton = screen.getByRole('button', { name: /cancel/i })
      fireEvent.click(cancelButton)
      
      expect(mockOnClose).toHaveBeenCalledTimes(1)
    })
  })

  describe('Form Validation', () => {
    it('should require device name', async () => {
      const user = userEvent.setup()
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const submitButton = screen.getByRole('button', { name: /add device|submit for verification/i })
      await user.click(submitButton)
      
      const nameInput = screen.getByLabelText(/device name/i)
      expect(nameInput).toBeRequired()
    })

    it('should require device ID', async () => {
      const user = userEvent.setup()
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const submitButton = screen.getByRole('button', { name: /add device|submit for verification/i })
      await user.click(submitButton)
      
      const deviceIdInput = screen.getByLabelText(/device id/i)
      expect(deviceIdInput).toBeRequired()
    })

    it('should require device type', async () => {
      const user = userEvent.setup()
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const submitButton = screen.getByRole('button', { name: /add device|submit for verification/i })
      await user.click(submitButton)
      
      const deviceTypeSelect = screen.getByLabelText(/device type/i)
      expect(deviceTypeSelect).toBeRequired()
    })

    it('should require protocol', async () => {
      const user = userEvent.setup()
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const submitButton = screen.getByRole('button', { name: /add device|submit for verification/i })
      await user.click(submitButton)
      
      const protocolSelect = screen.getByLabelText(/protocol/i)
      expect(protocolSelect).toBeRequired()
    })
  })

  describe('Device Verification Logic', () => {
    it('should show OEM locked status for SmartWatts OEM devices', async () => {
      const user = userEvent.setup()
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const manufacturerInput = screen.getAllByLabelText(/manufacturer/i)[0]
      await user.type(manufacturerInput, 'SmartWatts')
      
      await waitFor(() => {
        expect(screen.getByText(/OEM Locked/i)).toBeInTheDocument()
      })
    })

    it('should show unverified status for non-OEM devices', async () => {
      const user = userEvent.setup()
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const manufacturerInput = screen.getAllByLabelText(/manufacturer/i)[0]
      await user.type(manufacturerInput, 'Other Manufacturer')
      
      await waitFor(() => {
        expect(screen.getByText(/Unverified/i)).toBeInTheDocument()
      })
    })

    it('should show verification fields for non-OEM devices', async () => {
      const user = userEvent.setup()
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const manufacturerInput = screen.getAllByLabelText(/manufacturer/i)[0]
      await user.type(manufacturerInput, 'Other Manufacturer')
      
      await waitFor(() => {
        expect(screen.getByLabelText(/sample payload/i)).toBeInTheDocument()
        expect(screen.getByLabelText(/verification notes/i)).toBeInTheDocument()
      })
    })

    it('should not show verification fields for OEM devices', async () => {
      const user = userEvent.setup()
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const manufacturerInput = screen.getAllByLabelText(/manufacturer/i)[0]
      await user.type(manufacturerInput, 'SmartWatts')
      
      await waitFor(() => {
        expect(screen.queryByLabelText(/sample payload/i)).not.toBeInTheDocument()
      })
    })

    it('should detect OEM device by device ID prefix', async () => {
      const user = userEvent.setup()
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const deviceIdInput = screen.getByLabelText(/device id/i)
      await user.type(deviceIdInput, 'SW_12345')
      
      await waitFor(() => {
        expect(screen.getByText(/OEM Locked/i)).toBeInTheDocument()
      })
    })

    it('should detect OEM device by serial number prefix', async () => {
      const user = userEvent.setup()
      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      const serialNumberInput = screen.getByLabelText(/serial number/i)
      await user.type(serialNumberInput, 'SW12345')
      
      await waitFor(() => {
        expect(screen.getByText(/OEM Locked/i)).toBeInTheDocument()
      })
    })
  })

  describe('Form Submission', () => {
    it('should submit form successfully for OEM device', async () => {
      const user = userEvent.setup()
      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ id: 'device-123', name: 'Test Device' })
      })

      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      // Fill form
      await user.type(screen.getByLabelText(/device name/i), 'Test Device')
      await user.type(screen.getByLabelText(/device id/i), 'SW_12345')
      await user.type(screen.getAllByLabelText(/manufacturer/i)[0], 'SmartWatts')
      await user.selectOptions(screen.getByLabelText(/device type/i), 'SMART_METER')
      await user.selectOptions(screen.getByLabelText(/protocol/i), 'MQTT')
      
      // Submit
      const submitButton = screen.getByRole('button', { name: /add device/i })
      await user.click(submitButton)
      
      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(
          '/api/proxy/device-service/api/v1/devices',
          expect.objectContaining({
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
          })
        )
      })
      
      await waitFor(() => {
        expect(mockOnDeviceAdded).toHaveBeenCalled()
        expect(mockOnClose).toHaveBeenCalled()
      })
    })

    it('should submit form with verification for non-OEM device', async () => {
      const user = userEvent.setup()
      ;(global.fetch as jest.Mock)
        .mockResolvedValueOnce({
          ok: true,
          json: async () => ({ id: 'device-123', name: 'Test Device' })
        })
        .mockResolvedValueOnce({
          ok: true,
          json: async () => ({ id: 'verification-123' })
        })

      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      // Fill form
      await user.type(screen.getByLabelText(/device name/i), 'Test Device')
      await user.type(screen.getByLabelText(/device id/i), 'OTHER_12345')
      await user.type(screen.getAllByLabelText(/manufacturer/i)[0], 'Other Manufacturer')
      await user.selectOptions(screen.getByLabelText(/device type/i), 'SMART_METER')
      await user.selectOptions(screen.getByLabelText(/protocol/i), 'MQTT')
      
      // Wait for verification fields to appear
      await waitFor(() => {
        expect(screen.getByLabelText(/sample payload/i)).toBeInTheDocument()
      })
      
      const samplePayloadInput = screen.getByLabelText(/sample payload/i)
      // Use fireEvent.change instead of user.type for JSON strings to avoid special character issues
      fireEvent.change(samplePayloadInput, { target: { value: '{"test": "data"}' } })
      
      // Submit
      const submitButton = screen.getByRole('button', { name: /submit for verification/i })
      await user.click(submitButton)
      
      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledTimes(2)
        expect(mockOnDeviceAdded).toHaveBeenCalled()
        expect(mockOnClose).toHaveBeenCalled()
      })
    })

    it('should handle device registration error', async () => {
      const user = userEvent.setup()
      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ error: 'Bad request' })
      })

      // Mock alert
      const alertSpy = jest.spyOn(window, 'alert').mockImplementation(() => {})

      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      // Fill form
      await user.type(screen.getByLabelText(/device name/i), 'Test Device')
      await user.type(screen.getByLabelText(/device id/i), 'SW_12345')
      await user.type(screen.getAllByLabelText(/manufacturer/i)[0], 'SmartWatts')
      await user.selectOptions(screen.getByLabelText(/device type/i), 'SMART_METER')
      await user.selectOptions(screen.getByLabelText(/protocol/i), 'MQTT')
      
      // Submit
      const submitButton = screen.getByRole('button', { name: /add device/i })
      await user.click(submitButton)
      
      await waitFor(() => {
        expect(alertSpy).toHaveBeenCalledWith('Failed to add device. Please try again.')
      }, { timeout: 3000 })
      
      alertSpy.mockRestore()
    })

    it('should handle verification submission error', async () => {
      const user = userEvent.setup()
      ;(global.fetch as jest.Mock)
        .mockResolvedValueOnce({
          ok: true,
          json: async () => ({ id: 'device-123', name: 'Test Device' })
        })
        .mockResolvedValueOnce({
          ok: false,
          status: 400,
          json: async () => ({ error: 'Bad request' })
        })

      const alertSpy = jest.spyOn(window, 'alert').mockImplementation(() => {})

      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      // Fill form
      await user.type(screen.getByLabelText(/device name/i), 'Test Device')
      await user.type(screen.getByLabelText(/device id/i), 'OTHER_12345')
      await user.type(screen.getAllByLabelText(/manufacturer/i)[0], 'Other Manufacturer')
      await user.selectOptions(screen.getByLabelText(/device type/i), 'SMART_METER')
      await user.selectOptions(screen.getByLabelText(/protocol/i), 'MQTT')
      
      await waitFor(() => {
        expect(screen.getByLabelText(/sample payload/i)).toBeInTheDocument()
      })
      
      const samplePayloadInput = screen.getByLabelText(/sample payload/i)
      // Use fireEvent.change instead of user.type for JSON strings to avoid special character issues
      fireEvent.change(samplePayloadInput, { target: { value: '{"test": "data"}' } })
      
      // Submit
      const submitButton = screen.getByRole('button', { name: /submit for verification/i })
      await user.click(submitButton)
      
      await waitFor(() => {
        expect(alertSpy).toHaveBeenCalledWith('Failed to add device. Please try again.')
      }, { timeout: 3000 })
      
      alertSpy.mockRestore()
    })

    it('should disable submit button while submitting', async () => {
      const user = userEvent.setup()
      ;(global.fetch as jest.Mock).mockImplementation(() => 
        new Promise(resolve => setTimeout(() => resolve({
          ok: true,
          json: async () => ({ id: 'device-123' })
        }), 100))
      )

      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      // Fill form
      await user.type(screen.getByLabelText(/device name/i), 'Test Device')
      await user.type(screen.getByLabelText(/device id/i), 'SW_12345')
      await user.type(screen.getAllByLabelText(/manufacturer/i)[0], 'SmartWatts')
      await user.selectOptions(screen.getByLabelText(/device type/i), 'SMART_METER')
      await user.selectOptions(screen.getByLabelText(/protocol/i), 'MQTT')
      
      // Submit
      const submitButton = screen.getByRole('button', { name: /add device/i })
      
      // Click and wait for state to update
      await user.click(submitButton)
      
      // Wait for isSubmitting state to update
      await waitFor(() => {
        const updatedButton = screen.getByRole('button', { name: /processing/i })
        expect(updatedButton).toBeDisabled()
      }, { timeout: 2000 })
    })
  })

  describe('Form Reset', () => {
    it('should reset form after successful submission', async () => {
      const user = userEvent.setup()
      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ id: 'device-123', name: 'Test Device' })
      })

      render(<AddDeviceModal isOpen={true} onClose={mockOnClose} onDeviceAdded={mockOnDeviceAdded} />)
      
      // Fill form with required fields
      await user.type(screen.getByLabelText(/device name/i), 'Test Device')
      await user.type(screen.getByLabelText(/device id/i), 'SW_12345')
      await user.type(screen.getAllByLabelText(/manufacturer/i)[0], 'SmartWatts')
      await user.selectOptions(screen.getByLabelText(/device type/i), 'SMART_METER')
      await user.selectOptions(screen.getByLabelText(/protocol/i), 'MQTT')
      
      // Submit
      const submitButton = screen.getByRole('button', { name: /add device/i })
      await user.click(submitButton)
      
      // Wait for async submission to complete and modal to close
      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalled()
      }, { timeout: 3000 })
      
      // Wait for callbacks to be called
      await waitFor(() => {
        expect(mockOnDeviceAdded).toHaveBeenCalled()
        expect(mockOnClose).toHaveBeenCalled()
      }, { timeout: 2000 })
    })
  })
})

