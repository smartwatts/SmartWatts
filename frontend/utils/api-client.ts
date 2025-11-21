// SmartWatts API Client with Retry Logic and Circuit Breaker
// Implements exponential backoff, circuit breaker pattern, and error handling

interface ApiClientConfig {
  baseUrl: string
  timeout: number
  retryAttempts: number
  retryDelay: number
  circuitBreakerThreshold: number
  circuitBreakerTimeout: number
}

interface RequestConfig {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  headers?: Record<string, string>
  body?: any
  timeout?: number
  retryAttempts?: number
}

interface CircuitBreakerState {
  state: 'CLOSED' | 'OPEN' | 'HALF_OPEN'
  failureCount: number
  lastFailureTime: number
  successCount: number
}

class ApiClient {
  private config: ApiClientConfig
  private circuitBreakers: Map<string, CircuitBreakerState> = new Map()

  constructor(config: Partial<ApiClientConfig> = {}) {
    // Get API URL from environment or use current origin for relative URLs
    // In production, NEXT_PUBLIC_API_URL should be set at build time
    // For client-side, we use relative URLs that go through Next.js API routes
    let baseUrl = ''
    if (typeof window !== 'undefined') {
      // Client-side: use relative URLs (goes through Next.js /api/proxy)
      baseUrl = ''
    } else {
      // Server-side: use environment variable or fallback
      baseUrl = process.env.NEXT_PUBLIC_API_URL || 'https://api-gateway-3daykcsw5a-ew.a.run.app'
    }
    
    // Ensure we never use localhost in production
    if (baseUrl.includes('localhost') || baseUrl.includes('127.0.0.1')) {
      baseUrl = typeof window !== 'undefined' ? '' : 'https://api-gateway-3daykcsw5a-ew.a.run.app'
    }
    
    this.config = {
      baseUrl,
      timeout: 10000,
      retryAttempts: 3,
      retryDelay: 1000,
      circuitBreakerThreshold: 5,
      circuitBreakerTimeout: 60000,
      ...config
    }
  }

  private getCircuitBreakerState(service: string): CircuitBreakerState {
    if (!this.circuitBreakers.has(service)) {
      this.circuitBreakers.set(service, {
        state: 'CLOSED',
        failureCount: 0,
        lastFailureTime: 0,
        successCount: 0
      })
    }
    return this.circuitBreakers.get(service)!
  }

  private updateCircuitBreaker(service: string, success: boolean) {
    const state = this.getCircuitBreakerState(service)
    
    if (success) {
      state.successCount++
      if (state.state === 'HALF_OPEN' && state.successCount >= 3) {
        state.state = 'CLOSED'
        state.failureCount = 0
        state.successCount = 0
      }
    } else {
      state.failureCount++
      state.lastFailureTime = Date.now()
      
      if (state.failureCount >= this.config.circuitBreakerThreshold) {
        state.state = 'OPEN'
      }
    }
  }

  private isCircuitBreakerOpen(service: string): boolean {
    const state = this.getCircuitBreakerState(service)
    
    if (state.state === 'OPEN') {
      if (Date.now() - state.lastFailureTime > this.config.circuitBreakerTimeout) {
        state.state = 'HALF_OPEN'
        state.successCount = 0
        return false
      }
      return true
    }
    
    return false
  }

  private async delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms))
  }

  private async fetchWithTimeout(url: string, options: RequestInit, timeout: number): Promise<Response> {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), timeout)

    try {
      const response = await fetch(url, {
        ...options,
        signal: controller.signal
      })
      clearTimeout(timeoutId)
      return response
    } catch (error) {
      clearTimeout(timeoutId)
      throw error
    }
  }

  async request<T = any>(
    endpoint: string, 
    config: RequestConfig = { method: 'GET' }
  ): Promise<T> {
    // Extract service name from endpoint (e.g., '/test' -> 'test', '/api/users' -> 'api')
    const parts = endpoint.split('/').filter(p => p)
    const service = parts[0] || 'unknown'
    
    // Check circuit breaker
    if (this.isCircuitBreakerOpen(service)) {
      throw new Error(`Circuit breaker is OPEN for ${service} service`)
    }

    const url = `${this.config.baseUrl}${endpoint}`
    const timeout = config.timeout || this.config.timeout
    const retryAttempts = config.retryAttempts || this.config.retryAttempts

    let lastError: Error | null = null

    for (let attempt = 0; attempt <= retryAttempts; attempt++) {
      try {
        const response = await this.fetchWithTimeout(url, {
          method: config.method,
          headers: {
            'Content-Type': 'application/json',
            ...config.headers
          },
          body: config.body ? JSON.stringify(config.body) : undefined
        }, timeout)

        // Update circuit breaker on success
        this.updateCircuitBreaker(service, true)

        if (!response.ok) {
          // Don't retry on client errors (4xx)
          if (response.status >= 400 && response.status < 500) {
            const errorData = await response.json().catch(() => ({}))
            throw new Error(errorData.message || `HTTP ${response.status}`)
          }
          
          // Retry on server errors (5xx)
          if (response.status >= 500) {
            throw new Error(`HTTP ${response.status}`)
          }
        }

        const data = await response.json()
        return data

      } catch (error) {
        lastError = error as Error
        
        // Don't retry on certain errors
        if (error instanceof Error && (
          error.name === 'AbortError' || 
          error.message.includes('Circuit breaker') ||
          error.message.includes('HTTP 4')
        )) {
          break
        }

        // Update circuit breaker on failure
        this.updateCircuitBreaker(service, false)

        // Wait before retry (exponential backoff)
        if (attempt < retryAttempts) {
          const delay = this.config.retryDelay * Math.pow(2, attempt)
          await this.delay(delay)
        }
      }
    }

    throw lastError || new Error('Request failed after all retry attempts')
  }

  // Convenience methods
  async get<T = any>(endpoint: string, headers?: Record<string, string>): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET', headers })
  }

  async post<T = any>(endpoint: string, body?: any, headers?: Record<string, string>): Promise<T> {
    return this.request<T>(endpoint, { method: 'POST', body, headers })
  }

  async put<T = any>(endpoint: string, body?: any, headers?: Record<string, string>): Promise<T> {
    return this.request<T>(endpoint, { method: 'PUT', body, headers })
  }

  async delete<T = any>(endpoint: string, headers?: Record<string, string>): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE', headers })
  }

  // Proxy method for backward compatibility
  async proxy(service: string, path: string, config: RequestConfig = { method: 'GET' }): Promise<any> {
    const endpoint = `/api/proxy?service=${service}&path=${path}`
    return this.request(endpoint, config)
  }

  // Health check method
  async healthCheck(service: string): Promise<boolean> {
    try {
      await this.get(`/api/${service}/health`)
      return true
    } catch {
      return false
    }
  }

  // Get circuit breaker status
  getCircuitBreakerStatus(): Record<string, CircuitBreakerState> {
    const status: Record<string, CircuitBreakerState> = {}
    this.circuitBreakers.forEach((state, service) => {
      status[service] = { ...state }
    })
    return status
  }

  // Reset circuit breaker
  resetCircuitBreaker(service: string): void {
    this.circuitBreakers.delete(service)
  }
}

// Create singleton instance
export const apiClient = new ApiClient()

// Export class for custom instances
export { ApiClient }

// Export types
export type { ApiClientConfig, RequestConfig, CircuitBreakerState }


