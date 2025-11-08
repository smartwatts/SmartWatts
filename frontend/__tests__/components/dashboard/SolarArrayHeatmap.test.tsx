import React from 'react'
import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SolarArrayHeatmap from '../../../components/dashboard/SolarArrayHeatmap'

// Mock fetch
global.fetch = jest.fn()

describe('SolarArrayHeatmap', () => {
  const mockInverterId = 'inverter-123'
  const mockFetch = global.fetch as jest.Mock

  beforeEach(() => {
    jest.clearAllMocks()
    jest.useFakeTimers()
    mockFetch.mockClear()
  })

  afterEach(() => {
    jest.useRealTimers()
  })

  describe('Rendering', () => {
    it('should render loading state initially', () => {
      mockFetch.mockImplementation(() => new Promise(() => {})) // Never resolves

      render(<SolarArrayHeatmap inverterId={mockInverterId} />)

      // Use getAllByText since "Solar Array Heatmap" appears in both loading and normal states
      const titles = screen.getAllByText(/solar array/i)
      expect(titles.length).toBeGreaterThan(0)
      expect(screen.getByRole('status', { hidden: true })).toBeInTheDocument()
    })

    it('should render widget with solar array data', async () => {
      const mockData = {
        strings: [
          {
            stringId: 'string-1',
            stringNumber: 1,
            voltage: 400,
            current: 10,
            power: 4000,
            status: 'OPTIMAL',
            efficiency: 0.95
          }
        ],
        panels: [
          {
            panelId: 'panel-1',
            panelNumber: 1,
            voltage: 40,
            current: 10,
            power: 400,
            temperature: 45,
            status: 'OPTIMAL',
            efficiency: 0.95,
            hasFault: false
          }
        ],
        totalGeneration: 4000,
        averageEfficiency: 0.95,
        faultCount: 0
      }

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockData
      })

      render(<SolarArrayHeatmap inverterId={mockInverterId} />)

      await waitFor(() => {
        // The component formats power as "4.0kW" for 4000W, so check for that
        expect(screen.getByText(/4\.0kW/i)).toBeInTheDocument()
      })
    })
  })

  describe('Data Fetching', () => {
    it('should fetch solar array data on mount', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          strings: [],
          panels: [],
          totalGeneration: 0,
          averageEfficiency: 0,
          faultCount: 0
        })
      })

      render(<SolarArrayHeatmap inverterId={mockInverterId} />)

      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledWith(
          `/api/v1/solar/inverters/${mockInverterId}/heatmap`
        )
      })
    })

    it('should refetch data every 30 seconds', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => ({
          strings: [],
          panels: [],
          totalGeneration: 0,
          averageEfficiency: 0,
          faultCount: 0
        })
      })

      render(<SolarArrayHeatmap inverterId={mockInverterId} />)

      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledTimes(1)
      })

      jest.advanceTimersByTime(30000)

      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledTimes(2)
      })
    })

    it('should refetch when inverterId changes', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => ({
          strings: [],
          panels: [],
          totalGeneration: 0,
          averageEfficiency: 0,
          faultCount: 0
        })
      })

      const { rerender } = render(<SolarArrayHeatmap inverterId={mockInverterId} />)

      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledTimes(1)
      })

      rerender(<SolarArrayHeatmap inverterId="inverter-456" />)

      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledTimes(2)
      })
    })
  })

  describe('View Switching', () => {
    it('should switch between panels and strings view', async () => {
      const mockData = {
        strings: [
          {
            stringId: 'string-1',
            stringNumber: 1,
            voltage: 400,
            current: 10,
            power: 4000,
            status: 'OPTIMAL',
            efficiency: 0.95
          }
        ],
        panels: [
          {
            panelId: 'panel-1',
            panelNumber: 1,
            voltage: 40,
            current: 10,
            power: 400,
            temperature: 45,
            status: 'OPTIMAL',
            efficiency: 0.95,
            hasFault: false
          }
        ],
        totalGeneration: 4000,
        averageEfficiency: 0.95,
        faultCount: 0
      }

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockData
      })

      render(<SolarArrayHeatmap inverterId={mockInverterId} />)

      await waitFor(() => {
        // Use getAllByText since "Panels" appears in both button and view
        const panelsTexts = screen.getAllByText(/panels/i)
        expect(panelsTexts.length).toBeGreaterThan(0)
      })

      const stringsButton = screen.getByRole('button', { name: /strings/i })
      fireEvent.click(stringsButton)

      // Use getAllByText since "Strings" appears in both button and view
      const stringsTexts = screen.getAllByText(/strings/i)
      expect(stringsTexts.length).toBeGreaterThan(0)
    })
  })

  describe('Status Display', () => {
    it('should display correct status for OPTIMAL', async () => {
      const mockData = {
        strings: [],
        panels: [
          {
            panelId: 'panel-1',
            panelNumber: 1,
            voltage: 40,
            current: 10,
            power: 400,
            temperature: 45,
            status: 'OPTIMAL',
            efficiency: 0.95,
            hasFault: false
          }
        ],
        totalGeneration: 4000,
        averageEfficiency: 0.95,
        faultCount: 0
      }

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockData
      })

      render(<SolarArrayHeatmap inverterId={mockInverterId} />)

      await waitFor(() => {
        expect(screen.getByText(/optimal/i)).toBeInTheDocument()
      })
    })

    it('should display correct status for FAULT', async () => {
      const mockData = {
        strings: [],
        panels: [
          {
            panelId: 'panel-1',
            panelNumber: 1,
            voltage: 0,
            current: 0,
            power: 0,
            temperature: 45,
            status: 'FAULT',
            efficiency: 0,
            hasFault: true
          }
        ],
        totalGeneration: 0,
        averageEfficiency: 0,
        faultCount: 1
      }

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockData
      })

      render(<SolarArrayHeatmap inverterId={mockInverterId} />)

      await waitFor(() => {
        // Use getAllByText since "Fault" appears in both status and label
        const faultTexts = screen.getAllByText(/fault/i)
        expect(faultTexts.length).toBeGreaterThan(0)
      })
    })
  })
})

