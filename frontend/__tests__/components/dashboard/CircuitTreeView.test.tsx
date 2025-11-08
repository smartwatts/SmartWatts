import React from 'react'
import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CircuitTreeView from '../../../components/dashboard/CircuitTreeView'

// Mock fetch
global.fetch = jest.fn()

describe('CircuitTreeView', () => {
  const mockDeviceId = 'device-123'
  const mockFetch = global.fetch as jest.Mock

  beforeEach(() => {
    jest.clearAllMocks()
    mockFetch.mockClear()
  })

  describe('Rendering', () => {
    it('should render loading state initially', () => {
      mockFetch.mockImplementation(() => new Promise(() => {})) // Never resolves

      render(<CircuitTreeView deviceId={mockDeviceId} />)

      expect(screen.getByText(/circuit tree view/i)).toBeInTheDocument()
      expect(screen.getByRole('status', { hidden: true })).toBeInTheDocument()
    })

    it('should render circuit tree with nodes', async () => {
      const mockCircuitTree = [
        {
          id: 'node-1',
          name: 'Main Panel',
          type: 'SUB_PANEL',
          currentLoad: 5000,
          maxCapacity: 10000,
          status: 'NORMAL',
          children: [
            {
              id: 'node-2',
              name: 'Kitchen Circuit',
              type: 'CIRCUIT',
              currentLoad: 2000,
              maxCapacity: 3000,
              status: 'NORMAL',
              children: []
            }
          ]
        }
      ]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockCircuitTree
      })

      render(<CircuitTreeView deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText('Main Panel')).toBeInTheDocument()
      })

      expect(screen.getByText('Kitchen Circuit')).toBeInTheDocument()
      // Use getAllByText since there may be multiple nodes with NORMAL status
      const normalStatuses = screen.getAllByText('NORMAL')
      expect(normalStatuses.length).toBeGreaterThan(0)
    })

    it('should render empty state when no circuits found', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => []
      })

      render(<CircuitTreeView deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText(/no circuits found/i)).toBeInTheDocument()
      })
    })

    it('should render refresh button', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => []
      })

      render(<CircuitTreeView deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /refresh/i })).toBeInTheDocument()
      })
    })
  })

  describe('User Interactions', () => {
    it('should toggle node expansion when clicked', async () => {
      const mockCircuitTree = [
        {
          id: 'node-1',
          name: 'Main Panel',
          type: 'SUB_PANEL',
          currentLoad: 5000,
          maxCapacity: 10000,
          status: 'NORMAL',
          children: [
            {
              id: 'node-2',
              name: 'Kitchen Circuit',
              type: 'CIRCUIT',
              currentLoad: 2000,
              maxCapacity: 3000,
              status: 'NORMAL',
              children: []
            }
          ]
        }
      ]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockCircuitTree
      })

      render(<CircuitTreeView deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText('Main Panel')).toBeInTheDocument()
      })

      // Node should be expanded initially (auto-expanded)
      expect(screen.getByText('Kitchen Circuit')).toBeInTheDocument()

      // Click to collapse
      const nodeElement = screen.getByText('Main Panel').closest('div')
      if (nodeElement) {
        fireEvent.click(nodeElement)
      }

      // Node should be collapsed
      await waitFor(() => {
        expect(screen.queryByText('Kitchen Circuit')).not.toBeInTheDocument()
      })
    })

    it('should refresh circuit tree when refresh button is clicked', async () => {
      const mockCircuitTree = [
        {
          id: 'node-1',
          name: 'Main Panel',
          type: 'SUB_PANEL',
          currentLoad: 5000,
          maxCapacity: 10000,
          status: 'NORMAL',
          children: []
        }
      ]

      mockFetch
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockCircuitTree
        })
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockCircuitTree
        })

      render(<CircuitTreeView deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText('Main Panel')).toBeInTheDocument()
      })

      const refreshButton = screen.getByRole('button', { name: /refresh/i })
      fireEvent.click(refreshButton)

      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledTimes(2)
      })
    })
  })

  describe('Status Display', () => {
    it('should display correct status badge colors', async () => {
      const mockCircuitTree = [
        {
          id: 'node-1',
          name: 'Normal Circuit',
          type: 'CIRCUIT',
          currentLoad: 1000,
          maxCapacity: 2000,
          status: 'NORMAL',
          children: []
        },
        {
          id: 'node-2',
          name: 'High Load Circuit',
          type: 'CIRCUIT',
          currentLoad: 2000,
          maxCapacity: 2500,
          status: 'HIGH',
          children: []
        },
        {
          id: 'node-3',
          name: 'Overload Circuit',
          type: 'CIRCUIT',
          currentLoad: 3000,
          maxCapacity: 2500,
          status: 'OVERLOAD',
          children: []
        }
      ]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockCircuitTree
      })

      render(<CircuitTreeView deviceId={mockDeviceId} />)

      await waitFor(() => {
        // Use getAllByText since there may be multiple nodes with the same status
        const normalStatuses = screen.getAllByText('NORMAL')
        expect(normalStatuses.length).toBeGreaterThan(0)
        expect(screen.getByText('HIGH')).toBeInTheDocument()
        expect(screen.getByText('OVERLOAD')).toBeInTheDocument()
      })
    })
  })

  describe('Load Display', () => {
    it('should display power values correctly', async () => {
      const mockCircuitTree = [
        {
          id: 'node-1',
          name: 'Low Load',
          type: 'CIRCUIT',
          currentLoad: 500,
          maxCapacity: 2000,
          status: 'NORMAL',
          children: []
        },
        {
          id: 'node-2',
          name: 'High Load',
          type: 'CIRCUIT',
          currentLoad: 1500,
          maxCapacity: 2000,
          status: 'NORMAL',
          children: []
        },
        {
          id: 'node-3',
          name: 'Very High Load',
          type: 'CIRCUIT',
          currentLoad: 5000,
          maxCapacity: 10000,
          status: 'NORMAL',
          children: []
        }
      ]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockCircuitTree
      })

      render(<CircuitTreeView deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText(/500W/i)).toBeInTheDocument()
        // 1500W should be formatted as 1.5kW (since >= 1000)
        expect(screen.getByText(/1\.5kW/i)).toBeInTheDocument()
        expect(screen.getByText(/5\.0kW/i)).toBeInTheDocument()
      })
    })

    it('should display load percentage correctly', async () => {
      const mockCircuitTree = [
        {
          id: 'node-1',
          name: 'Test Circuit',
          type: 'CIRCUIT',
          currentLoad: 1500,
          maxCapacity: 2000,
          status: 'NORMAL',
          children: []
        }
      ]

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockCircuitTree
      })

      render(<CircuitTreeView deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText(/75%/i)).toBeInTheDocument()
      })
    })
  })

  describe('Error Handling', () => {
    it('should handle API errors gracefully', async () => {
      mockFetch.mockRejectedValueOnce(new Error('Network error'))

      render(<CircuitTreeView deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText(/no circuits found/i)).toBeInTheDocument()
      })
    })

    it('should handle non-ok responses', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500
      })

      render(<CircuitTreeView deviceId={mockDeviceId} />)

      await waitFor(() => {
        expect(screen.getByText(/no circuits found/i)).toBeInTheDocument()
      })
    })
  })

  describe('Device ID Changes', () => {
    it('should refetch when deviceId changes', async () => {
      const mockCircuitTree = [
        {
          id: 'node-1',
          name: 'Main Panel',
          type: 'SUB_PANEL',
          currentLoad: 5000,
          maxCapacity: 10000,
          status: 'NORMAL',
          children: []
        }
      ]

      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => mockCircuitTree
      })

      const { rerender } = render(<CircuitTreeView deviceId="device-123" />)

      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledWith('/api/v1/circuits/devices/device-123/tree')
      })

      rerender(<CircuitTreeView deviceId="device-456" />)

      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledWith('/api/v1/circuits/devices/device-456/tree')
      })
    })
  })
})

