import { userApi, energyApi, deviceApi, analyticsApi, billingApi, facilityApi } from '../../utils/api'

// Mock fetch
global.fetch = jest.fn()

describe('API Utilities', () => {
  const mockFetch = global.fetch as jest.Mock

  beforeEach(() => {
    jest.clearAllMocks()
    mockFetch.mockClear()
  })

  describe('userApi', () => {
    it('should call login endpoint with correct data', async () => {
      const mockResponse = { token: 'test-token', user: { id: '1' } }
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const result = await userApi.login({ email: 'test@example.com', password: 'password' })

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=user&path=/users/login',
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email: 'test@example.com', password: 'password' })
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call register endpoint with correct data', async () => {
      const mockResponse = { success: true }
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const registerData = { email: 'test@example.com', password: 'password', firstName: 'Test' }
      const result = await userApi.register(registerData)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=user&path=/users/register',
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(registerData)
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call getProfile endpoint', async () => {
      const mockResponse = { id: '1', email: 'test@example.com' }
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const result = await userApi.getProfile()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=user&path=/users/profile',
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call getUsers with query parameters', async () => {
      const mockResponse = [{ id: '1' }, { id: '2' }]
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const params = { page: '1', limit: '10' }
      const result = await userApi.getUsers(params)

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/proxy?service=user&path=/users?page=1&limit=10'),
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })
  })

  describe('energyApi', () => {
    it('should call getReadings endpoint', async () => {
      const mockResponse = [{ id: '1', consumption: 100 }]
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const result = await energyApi.getReadings()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=energy&path=/energy/readings',
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call getCurrentReading endpoint', async () => {
      const mockResponse = { consumption: 100, timestamp: '2024-01-01' }
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const result = await energyApi.getCurrentReading()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=energy&path=/energy/readings/current',
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call getConsumption with query parameters', async () => {
      const mockResponse = { total: 1000 }
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const params = { startDate: '2024-01-01', endDate: '2024-01-31' }
      const result = await energyApi.getConsumption(params)

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/proxy?service=energy&path=/energy/consumption'),
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })
  })

  describe('deviceApi', () => {
    it('should call getDevices endpoint', async () => {
      const mockResponse = [{ id: '1', name: 'Device 1' }]
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const result = await deviceApi.getDevices()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=device&path=/devices',
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call getDevice with id', async () => {
      const mockResponse = { id: '1', name: 'Device 1' }
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const result = await deviceApi.getDevice('1')

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=device&path=/devices/1',
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call createDevice with data', async () => {
      const mockResponse = { id: '1', name: 'New Device' }
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const deviceData = { name: 'New Device', type: 'SMART_METER' }
      const result = await deviceApi.createDevice(deviceData)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=device&path=/devices/register',
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(deviceData)
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call updateDevice with id and data', async () => {
      const mockResponse = { id: '1', name: 'Updated Device' }
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const updateData = { name: 'Updated Device' }
      const result = await deviceApi.updateDevice('1', updateData)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=device&path=/devices/1',
        expect.objectContaining({
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(updateData)
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call deleteDevice with id', async () => {
      const mockResponse = { success: true }
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const result = await deviceApi.deleteDevice('1')

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=device&path=/devices/1',
        expect.objectContaining({
          method: 'DELETE',
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })
  })

  describe('analyticsApi', () => {
    it('should call getAnalytics endpoint', async () => {
      const mockResponse = { insights: [] }
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const result = await analyticsApi.getAnalytics()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=analytics&path=/analytics',
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call getUsagePatterns with query parameters', async () => {
      const mockResponse = { patterns: [] }
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const params = { period: 'monthly' }
      const result = await analyticsApi.getUsagePatterns(params)

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/proxy?service=analytics&path=/analytics/patterns'),
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })
  })

  describe('billingApi', () => {
    it('should call getBills endpoint', async () => {
      const mockResponse = [{ id: '1', amount: 100 }]
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const result = await billingApi.getBills()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=billing&path=/bills',
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call payBill with id and payment data', async () => {
      const mockResponse = { success: true, transactionId: 'tx-123' }
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const paymentData = { method: 'card', amount: 100 }
      const result = await billingApi.payBill('1', paymentData)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=billing&path=/bills/1/pay',
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(paymentData)
        })
      )
      expect(result).toEqual(mockResponse)
    })
  })

  describe('facilityApi', () => {
    it('should call getAssets endpoint', async () => {
      const mockResponse = [{ id: '1', name: 'Asset 1' }]
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const result = await facilityApi.getAssets()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=facility&path=/api/v1/assets',
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call createAsset with data', async () => {
      const mockResponse = { id: '1', name: 'New Asset' }
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const assetData = { name: 'New Asset', type: 'ELECTRICAL_EQUIPMENT' }
      const result = await facilityApi.createAsset(assetData)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=facility&path=/api/v1/assets',
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(assetData)
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call getFleet endpoint', async () => {
      const mockResponse = [{ id: '1', name: 'Vehicle 1' }]
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const result = await facilityApi.getFleet()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=facility&path=/api/v1/fleet',
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call getSpaces endpoint', async () => {
      const mockResponse = [{ id: '1', name: 'Space 1' }]
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const result = await facilityApi.getSpaces()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=facility&path=/api/v1/spaces',
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })

    it('should call getWorkOrders endpoint', async () => {
      const mockResponse = [{ id: '1', title: 'Work Order 1' }]
      mockFetch.mockResolvedValueOnce({
        json: async () => mockResponse
      })

      const result = await facilityApi.getWorkOrders()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/proxy?service=facility&path=/api/v1/work-orders',
        expect.objectContaining({
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockResponse)
    })
  })
})

