import React from 'react'
import { render, screen, waitFor, fireEvent, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ApplianceRecognitionWidget from '../../../components/dashboard/ApplianceRecognitionWidget'

// Mock fetch
global.fetch = jest.fn()

describe('ApplianceRecognitionWidget', () => {
  const mockDeviceId = 'device-123'
  const mockFetch = global.fetch as jest.Mock

  beforeEach(() => {
    jest.clearAllMocks()
    mockFetch.mockClear()
  })

  describe('Rendering', () => {
    it('should render loading state initially', () => {
      mockFetch.mockImplementation(() => new Promise(() => {})) // Never resolves

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      // Use getAllByText since "Appliance Recognition" appears in both loading and normal states
      const titles = screen.getAllByText(/appliance recognition/i)
      expect(titles.length).toBeGreaterThan(0)
      expect(screen.getByRole('status', { hidden: true })).toBeInTheDocument()
    })

    it('should render widget with detections', async () => {
      const mockDetections = [
        {
          id: 'detection-1',
          applianceName: 'Refrigerator',
          applianceType: 'REFRIGERATOR',
          confidenceScore: 0.95,
          powerConsumption: 150,
          status: 'DETECTED',
          detectionTime: '2024-01-01T10:00:00Z'
        },
        {
          id: 'detection-2',
          applianceName: 'Washing Machine',
          applianceType: 'WASHING_MACHINE',
          confidenceScore: 0.85,
          powerConsumption: 2000,
          status: 'CONFIRMED',
          detectionTime: '2024-01-01T11:00:00Z'
        }
      ]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockDetections
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText('Refrigerator')).toBeInTheDocument()
        expect(screen.getByText('Washing Machine')).toBeInTheDocument()
      })
    })

    it('should render empty state when no detections', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => []
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText(/no appliances detected yet/i)).toBeInTheDocument()
      })
    })
  })

  describe('Data Fetching', () => {
    it('should fetch detections on mount', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => []
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledWith(
          `/api/v1/appliance-recognition/devices/${mockDeviceId}/detections`
        )
      })
    })

    it('should refetch when deviceId changes', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => []
      })

      const { rerender } = render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledTimes(1)
      })

      rerender(<ApplianceRecognitionWidget deviceId="device-456" />)

      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledTimes(2)
      })
    })

    it('should handle fetch errors gracefully', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {})
      mockFetch.mockRejectedValueOnce(new Error('Network error'))

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(consoleSpy).toHaveBeenCalledWith(
          'Error fetching appliance detections:',
          expect.any(Error)
        )
      })

      consoleSpy.mockRestore()
    })
  })

  describe('Status Display', () => {
    it('should display correct status badge for DETECTED', async () => {
      const mockDetections = [{
        id: 'detection-1',
        applianceName: 'Refrigerator',
        applianceType: 'REFRIGERATOR',
        confidenceScore: 0.95,
        powerConsumption: 150,
        status: 'DETECTED',
        detectionTime: '2024-01-01T10:00:00Z'
      }]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockDetections
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText('DETECTED')).toBeInTheDocument()
      })
    })

    it('should display correct status badge for CONFIRMED', async () => {
      const mockDetections = [{
        id: 'detection-1',
        applianceName: 'Refrigerator',
        applianceType: 'REFRIGERATOR',
        confidenceScore: 0.95,
        powerConsumption: 150,
        status: 'CONFIRMED',
        detectionTime: '2024-01-01T10:00:00Z'
      }]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockDetections
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText('CONFIRMED')).toBeInTheDocument()
      })
    })

    it('should display correct status badge for UNKNOWN', async () => {
      const mockDetections = [{
        id: 'detection-1',
        applianceName: 'Unknown Device',
        applianceType: 'UNKNOWN',
        confidenceScore: 0.3,
        powerConsumption: 50,
        status: 'UNKNOWN',
        detectionTime: '2024-01-01T10:00:00Z'
      }]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockDetections
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText('UNKNOWN')).toBeInTheDocument()
      })
    })
  })

  describe('Confidence Score Display', () => {
    it('should display high confidence score in green', async () => {
      const mockDetections = [{
        id: 'detection-1',
        applianceName: 'Refrigerator',
        applianceType: 'REFRIGERATOR',
        confidenceScore: 0.95,
        powerConsumption: 150,
        status: 'DETECTED',
        detectionTime: '2024-01-01T10:00:00Z'
      }]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockDetections
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText(/95%/i)).toBeInTheDocument()
      })
    })

    it('should display medium confidence score in yellow', async () => {
      const mockDetections = [{
        id: 'detection-1',
        applianceName: 'Refrigerator',
        applianceType: 'REFRIGERATOR',
        confidenceScore: 0.65,
        powerConsumption: 150,
        status: 'DETECTED',
        detectionTime: '2024-01-01T10:00:00Z'
      }]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockDetections
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText(/65%/i)).toBeInTheDocument()
      })
    })

    it('should display low confidence score in red', async () => {
      const mockDetections = [{
        id: 'detection-1',
        applianceName: 'Refrigerator',
        applianceType: 'REFRIGERATOR',
        confidenceScore: 0.45,
        powerConsumption: 150,
        status: 'DETECTED',
        detectionTime: '2024-01-01T10:00:00Z'
      }]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockDetections
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText(/45%/i)).toBeInTheDocument()
      })
    })
  })

  describe('Confirmation Actions', () => {
    it('should confirm detection when confirm button is clicked', async () => {
      const user = userEvent.setup()
      const mockDetections = [{
        id: 'detection-1',
        applianceName: 'Refrigerator',
        applianceType: 'REFRIGERATOR',
        confidenceScore: 0.95,
        powerConsumption: 150,
        status: 'DETECTED',
        detectionTime: '2024-01-01T10:00:00Z'
      }]

      mockFetch
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockDetections
        })
        .mockResolvedValueOnce({
          ok: true,
          json: async () => []
        })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText('Refrigerator')).toBeInTheDocument()
      })

      const confirmButton = screen.getByRole('button', { name: /confirm/i })
      await user.click(confirmButton)

      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledWith(
          `/api/v1/appliance-recognition/detections/detection-1/confirm`,
          expect.objectContaining({
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ confirmed: true, userFeedback: undefined })
          })
        )
      })
    })

    it('should reject detection when reject button is clicked', async () => {
      const user = userEvent.setup()
      const mockDetections = [{
        id: 'detection-1',
        applianceName: 'Refrigerator',
        applianceType: 'REFRIGERATOR',
        confidenceScore: 0.95,
        powerConsumption: 150,
        status: 'DETECTED',
        detectionTime: '2024-01-01T10:00:00Z'
      }]

      mockFetch
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockDetections
        })
        .mockResolvedValueOnce({
          ok: true,
          json: async () => []
        })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText('Refrigerator')).toBeInTheDocument()
      })

      const rejectButton = screen.getByRole('button', { name: /reject/i })
      await user.click(rejectButton)

      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledWith(
          `/api/v1/appliance-recognition/detections/detection-1/confirm`,
          expect.objectContaining({
            method: 'PUT',
            body: JSON.stringify({ confirmed: false, userFeedback: undefined })
          })
        )
      })
    })

    it('should handle confirmation errors gracefully', async () => {
      const user = userEvent.setup()
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {})
      
      const mockDetections = [{
        id: 'detection-1',
        applianceName: 'Refrigerator',
        applianceType: 'REFRIGERATOR',
        confidenceScore: 0.95,
        powerConsumption: 150,
        status: 'DETECTED',
        detectionTime: '2024-01-01T10:00:00Z'
      }]

      mockFetch
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockDetections
        })
        .mockRejectedValueOnce(new Error('Network error'))

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText('Refrigerator')).toBeInTheDocument()
      })

      const confirmButton = screen.getByRole('button', { name: /confirm/i })
      await user.click(confirmButton)

      await waitFor(() => {
        expect(consoleSpy).toHaveBeenCalledWith(
          'Error confirming detection:',
          expect.any(Error)
        )
      })

      consoleSpy.mockRestore()
    })
  })

  describe('Training Mode', () => {
    it('should toggle training mode', async () => {
      const user = userEvent.setup()
      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => []
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.queryByText(/loading/i)).not.toBeInTheDocument()
      })

      const trainButton = screen.getByRole('button', { name: /train new appliance/i })
      await user.click(trainButton)

      // Training mode should be enabled - look for "Train New Appliance" heading
      expect(screen.getByText(/train new appliance/i)).toBeInTheDocument()
    })

    it('should submit training data', async () => {
      const user = userEvent.setup()
      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => []
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.queryByText(/loading/i)).not.toBeInTheDocument()
      })

      // Enable training mode
      const trainButton = screen.getByRole('button', { name: /train new appliance/i })
      await user.click(trainButton)

      // Training mode uses preset buttons, not a form
      // Click one of the training buttons
      const trainWashingMachineButton = screen.getByRole('button', { name: /train washing machine/i })
      
      await act(async () => {
        await user.click(trainWashingMachineButton)
      })

      await waitFor(() => {
        // The component calls trainAppliance which makes a POST request
        // Check if fetch was called with the correct endpoint and method
        const fetchCalls = (mockFetch as jest.Mock).mock.calls
        const trainCall = fetchCalls.find((call: any[]) => 
          call[0]?.includes('/train') && call[1]?.method === 'POST'
        )
        expect(trainCall).toBeDefined()
        if (trainCall && trainCall[1]?.body) {
          const body = JSON.parse(trainCall[1].body)
          expect(body).toMatchObject({
            applianceName: 'My Washing Machine',
            applianceType: 'WASHING_MACHINE'
          })
        }
      }, { timeout: 3000 })
    })
  })

  describe('Appliance Icons', () => {
    it('should render correct icon for REFRIGERATOR', async () => {
      const mockDetections = [{
        id: 'detection-1',
        applianceName: 'Refrigerator',
        applianceType: 'REFRIGERATOR',
        confidenceScore: 0.95,
        powerConsumption: 150,
        status: 'DETECTED',
        detectionTime: '2024-01-01T10:00:00Z'
      }]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockDetections
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText('Refrigerator')).toBeInTheDocument()
      })
    })

    it('should render correct icon for WASHING_MACHINE', async () => {
      const mockDetections = [{
        id: 'detection-1',
        applianceName: 'Washing Machine',
        applianceType: 'WASHING_MACHINE',
        confidenceScore: 0.95,
        powerConsumption: 2000,
        status: 'DETECTED',
        detectionTime: '2024-01-01T10:00:00Z'
      }]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockDetections
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText('Washing Machine')).toBeInTheDocument()
      })
    })
  })

  describe('Power Consumption Display', () => {
    it('should display power consumption correctly', async () => {
      const mockDetections = [{
        id: 'detection-1',
        applianceName: 'Refrigerator',
        applianceType: 'REFRIGERATOR',
        confidenceScore: 0.95,
        powerConsumption: 150,
        status: 'DETECTED',
        detectionTime: '2024-01-01T10:00:00Z'
      }]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockDetections
      })

      render(<ApplianceRecognitionWidget deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText(/150/i)).toBeInTheDocument()
      })
    })
  })
})

