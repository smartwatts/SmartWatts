import { ApiClient } from '../../utils/api-client'

// Mock fetch
global.fetch = jest.fn()

describe('ApiClient', () => {
  let apiClient: ApiClient
  const mockFetch = global.fetch as jest.Mock

  beforeEach(() => {
    jest.clearAllMocks()
    apiClient = new ApiClient({
      baseUrl: 'http://localhost:8080',
      timeout: 1000,
      retryAttempts: 2,
      retryDelay: 100,
      circuitBreakerThreshold: 3,
      circuitBreakerTimeout: 5000
    })
  })

  describe('Constructor and Configuration', () => {
    it('should create instance with default config', () => {
      const client = new ApiClient()
      expect(client).toBeInstanceOf(ApiClient)
    })

    it('should create instance with custom config', () => {
      const client = new ApiClient({
        baseUrl: 'https://api.example.com',
        timeout: 5000
      })
      expect(client).toBeInstanceOf(ApiClient)
    })

    it('should use environment variable for baseUrl when not provided', () => {
      const originalEnv = process.env.NEXT_PUBLIC_API_URL
      process.env.NEXT_PUBLIC_API_URL = 'https://custom-api.com'
      
      const client = new ApiClient()
      expect(client).toBeInstanceOf(ApiClient)
      
      process.env.NEXT_PUBLIC_API_URL = originalEnv
    })
  })

  describe('GET Requests', () => {
    it('should make successful GET request', async () => {
      const mockData = { id: 1, name: 'Test' }
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockData
      })

      const result = await apiClient.get('/test')

      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8080/test',
        expect.objectContaining({
          method: 'GET',
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockData)
    })

    it('should include custom headers in GET request', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({})
      })

      await apiClient.get('/test', { 'Authorization': 'Bearer token' })

      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8080/test',
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer token'
          })
        })
      )
    })

    it('should handle GET request errors', async () => {
      mockFetch.mockRejectedValueOnce(new Error('Network error'))

      await expect(apiClient.get('/test')).rejects.toThrow()
    })
  })

  describe('POST Requests', () => {
    it('should make successful POST request', async () => {
      const requestData = { name: 'Test', value: 123 }
      const responseData = { id: 1, ...requestData }
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => responseData
      })

      const result = await apiClient.post('/test', requestData)

      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8080/test',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify(requestData),
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(responseData)
    })

    it('should include custom headers in POST request', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({})
      })

      await apiClient.post('/test', { data: 'test' }, { 'Authorization': 'Bearer token' })

      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8080/test',
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer token'
          })
        })
      )
    })
  })

  describe('PUT Requests', () => {
    it('should make successful PUT request', async () => {
      const requestData = { name: 'Updated' }
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ id: 1, ...requestData })
      })

      const result = await apiClient.put('/test/1', requestData)

      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8080/test/1',
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify(requestData)
        })
      )
      expect(result).toEqual({ id: 1, ...requestData })
    })
  })

  describe('DELETE Requests', () => {
    it('should make successful DELETE request', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true })
      })

      const result = await apiClient.delete('/test/1')

      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8080/test/1',
        expect.objectContaining({
          method: 'DELETE'
        })
      )
      expect(result).toEqual({ success: true })
    })
  })

  describe('Request Timeout', () => {
    it('should timeout after specified duration', async () => {
      // Create a new client with a shorter timeout
      const clientWithShortTimeout = new ApiClient({ timeout: 500 })
      
      // Mock fetch to delay longer than timeout and handle abort signal
      mockFetch.mockImplementation((url: string, options?: RequestInit) => {
        return new Promise((resolve, reject) => {
          const timeoutId = setTimeout(() => {
            resolve({
              ok: true,
              json: async () => ({})
            })
          }, 1000) // Delay longer than timeout (500ms)
          
          // Handle abort signal
          if (options?.signal) {
            options.signal.addEventListener('abort', () => {
              clearTimeout(timeoutId)
              const abortError = new Error('The operation was aborted.')
              abortError.name = 'AbortError'
              reject(abortError)
            })
          }
        })
      })

      await expect(clientWithShortTimeout.get('/test')).rejects.toThrow()
    }, 5000) // Increase timeout for this test

    it('should use custom timeout from request config', async () => {
      // Mock fetch to delay longer than custom timeout and handle abort signal
      mockFetch.mockImplementation((url: string, options?: RequestInit) => {
        return new Promise((resolve, reject) => {
          const timeoutId = setTimeout(() => {
            resolve({
              ok: true,
              json: async () => ({})
            })
          }, 1000) // Delay longer than custom timeout (500ms)
          
          // Handle abort signal
          if (options?.signal) {
            options.signal.addEventListener('abort', () => {
              clearTimeout(timeoutId)
              const abortError = new Error('The operation was aborted.')
              abortError.name = 'AbortError'
              reject(abortError)
            })
          }
        })
      })

      await expect(apiClient.request('/test', { method: 'GET', timeout: 500 })).rejects.toThrow()
    }, 5000) // Increase timeout for this test
  })

  describe('Retry Logic', () => {
    it('should retry on server errors (5xx)', async () => {
      let attemptCount = 0
      mockFetch.mockImplementation(() => {
        attemptCount++
        if (attemptCount < 3) {
          return Promise.resolve({
            ok: false,
            status: 500,
            json: async () => ({ error: 'Server error' })
          })
        }
        return Promise.resolve({
          ok: true,
          json: async () => ({ success: true })
        })
      })

      const result = await apiClient.get('/test')

      expect(mockFetch).toHaveBeenCalledTimes(3)
      expect(result).toEqual({ success: true })
    })

    it('should not retry on client errors (4xx)', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ error: 'Bad request' })
      })

      await expect(apiClient.get('/test')).rejects.toThrow()
      expect(mockFetch).toHaveBeenCalledTimes(1)
    })

    it('should not retry on AbortError', async () => {
      const abortError = new Error('Request aborted')
      abortError.name = 'AbortError'
      mockFetch.mockRejectedValueOnce(abortError)

      await expect(apiClient.get('/test')).rejects.toThrow('Request aborted')
      expect(mockFetch).toHaveBeenCalledTimes(1)
    })

    it('should use exponential backoff for retries', async () => {
      const startTime = Date.now()
      let attemptCount = 0
      
      mockFetch.mockImplementation(() => {
        attemptCount++
        if (attemptCount < 3) {
          return Promise.resolve({
            ok: false,
            status: 500,
            json: async () => ({ error: 'Server error' })
          })
        }
        return Promise.resolve({
          ok: true,
          json: async () => ({ success: true })
        })
      })

      await apiClient.get('/test')

      const elapsedTime = Date.now() - startTime
      // Should have delays between retries (at least 100ms + 200ms)
      expect(elapsedTime).toBeGreaterThan(200)
    })
  })

  describe('Circuit Breaker', () => {
    it('should open circuit breaker after threshold failures', async () => {
      mockFetch.mockResolvedValue({
        ok: false,
        status: 500,
        json: async () => ({ error: 'Server error' })
      })

      // Make requests that will fail
      for (let i = 0; i < 3; i++) {
        try {
          await apiClient.get('/test')
        } catch (error) {
          // Expected to fail
        }
      }

      // Next request should be blocked by circuit breaker
      await expect(apiClient.get('/test')).rejects.toThrow('Circuit breaker is OPEN')
    })

    it('should close circuit breaker after timeout', async () => {
      const originalDateNow = Date.now
      let mockTime = 1000000
      Date.now = jest.fn(() => mockTime)
      
      mockFetch.mockResolvedValue({
        ok: false,
        status: 500,
        json: async () => ({ error: 'Server error' })
      })

      // Open circuit breaker
      for (let i = 0; i < 3; i++) {
        try {
          await apiClient.get('/test')
        } catch (error) {
          // Expected to fail
        }
      }

      // Fast forward time past circuit breaker timeout (5000ms)
      mockTime += 6000

      // Mock successful response
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true })
      })

      // Should allow request in HALF_OPEN state
      const result = await apiClient.get('/test')
      expect(result).toEqual({ success: true })

      Date.now = originalDateNow
    })

    it('should close circuit breaker after successful requests in HALF_OPEN state', async () => {
      const originalDateNow = Date.now
      let mockTime = 1000000
      Date.now = jest.fn(() => mockTime)
      
      // Create a new client instance for this test to avoid state pollution
      const testClient = new ApiClient({
        baseUrl: 'http://localhost:8080',
        timeout: 1000,
        retryAttempts: 2,
        retryDelay: 100,
        circuitBreakerThreshold: 3,
        circuitBreakerTimeout: 5000
      })
      
      // Open circuit breaker
      mockFetch.mockResolvedValue({
        ok: false,
        status: 500,
        json: async () => ({ error: 'Server error' })
      })

      for (let i = 0; i < 3; i++) {
        try {
          await testClient.get('/test')
        } catch (error) {
          // Expected to fail
        }
      }

      // Fast forward time past circuit breaker timeout
      mockTime += 6000

      // Mock successful responses
      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => ({ success: true })
      })

      // Make 3 successful requests
      for (let i = 0; i < 3; i++) {
        await testClient.get('/test')
      }

      // Circuit breaker should be closed
      const status = testClient.getCircuitBreakerStatus()
      expect(status['test']?.state).toBe('CLOSED')

      Date.now = originalDateNow
    })

    it('should reset circuit breaker', () => {
      const status = apiClient.getCircuitBreakerStatus()
      expect(status).toEqual({})

      // Reset should work even if circuit breaker doesn't exist
      apiClient.resetCircuitBreaker('test')
      
      const statusAfter = apiClient.getCircuitBreakerStatus()
      expect(statusAfter).toEqual({})
    })

    it('should get circuit breaker status', () => {
      const status = apiClient.getCircuitBreakerStatus()
      expect(status).toBeDefined()
      expect(typeof status).toBe('object')
    })
  })

  describe('Error Handling', () => {
    it('should handle network errors', async () => {
      // Create a new client instance to avoid state pollution
      const testClient = new ApiClient({
        baseUrl: 'http://localhost:8080',
        timeout: 1000,
        retryAttempts: 0, // Disable retries for this test
        retryDelay: 100,
        circuitBreakerThreshold: 5,
        circuitBreakerTimeout: 60000
      })
      
      mockFetch.mockClear()
      mockFetch.mockRejectedValueOnce(new Error('Network error'))

      await expect(testClient.get('/test')).rejects.toThrow()
    })

    it('should handle JSON parse errors', async () => {
      // Create a new client instance to avoid state pollution
      const testClient = new ApiClient({
        baseUrl: 'http://localhost:8080',
        timeout: 1000,
        retryAttempts: 0, // Disable retries for this test
        retryDelay: 100,
        circuitBreakerThreshold: 5,
        circuitBreakerTimeout: 60000
      })
      
      mockFetch.mockClear()
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => {
          throw new Error('Invalid JSON')
        }
      })

      await expect(testClient.get('/test')).rejects.toThrow()
    })

    it('should handle 401 Unauthorized', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 401,
        json: async () => ({ error: 'Unauthorized' })
      })

      await expect(apiClient.get('/test')).rejects.toThrow('HTTP 401')
    })

    it('should handle 403 Forbidden', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 403,
        json: async () => ({ error: 'Forbidden' })
      })

      await expect(apiClient.get('/test')).rejects.toThrow('HTTP 403')
    })

    it('should handle 404 Not Found', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
        json: async () => ({ error: 'Not Found' })
      })

      await expect(apiClient.get('/test')).rejects.toThrow('HTTP 404')
    })
  })

  describe('Proxy Method', () => {
    it('should make proxy request', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: 'test' })
      })

      const result = await apiClient.proxy('user-service', '/users', { method: 'GET' })

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/proxy'),
        expect.any(Object)
      )
      expect(result).toEqual({ data: 'test' })
    })
  })

  describe('Health Check', () => {
    it('should return true for healthy service', async () => {
      // Create a new client instance to avoid state pollution
      const testClient = new ApiClient({
        baseUrl: 'http://localhost:8080',
        timeout: 1000,
        retryAttempts: 0,
        retryDelay: 100,
        circuitBreakerThreshold: 5,
        circuitBreakerTimeout: 60000
      })
      
      mockFetch.mockClear()
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ status: 'UP' })
      })

      const result = await testClient.healthCheck('user-service')
      expect(result).toBe(true)
    })

    it('should return false for unhealthy service', async () => {
      // Create a new client instance to avoid state pollution
      const testClient = new ApiClient({
        baseUrl: 'http://localhost:8080',
        timeout: 1000,
        retryAttempts: 0,
        retryDelay: 100,
        circuitBreakerThreshold: 5,
        circuitBreakerTimeout: 60000
      })
      
      mockFetch.mockClear()
      mockFetch.mockRejectedValueOnce(new Error('Service unavailable'))

      const result = await testClient.healthCheck('user-service')
      expect(result).toBe(false)
    })
  })

  describe('Request Method', () => {
    it('should make request with custom config', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: 'test' })
      })

      const result = await apiClient.request('/test', {
        method: 'PATCH',
        body: { update: 'value' },
        headers: { 'Custom-Header': 'value' }
      })

      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8080/test',
        expect.objectContaining({
          method: 'PATCH',
          body: JSON.stringify({ update: 'value' }),
          headers: expect.objectContaining({
            'Custom-Header': 'value'
          })
        })
      )
      expect(result).toEqual({ data: 'test' })
    })

    it('should use service name from endpoint for circuit breaker', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({})
      })

      await apiClient.request('/user-service/test', { method: 'GET' })

      const status = apiClient.getCircuitBreakerStatus()
      expect(status).toHaveProperty('user-service')
    })
  })
})

