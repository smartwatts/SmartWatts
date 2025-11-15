import { NextApiRequest, NextApiResponse } from 'next'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  const { service, path, ...queryParams } = req.query
  
  // Normalize query parameters: Next.js query params can be string | string[] | undefined
  const serviceString: string | undefined = Array.isArray(service) ? service[0] : (service || undefined)
  const pathString: string = Array.isArray(path) ? path.join('/') : (path || '')
  
  if (!serviceString || !pathString) {
    return res.status(400).json({ 
      error: 'MISSING_PARAMETERS',
      message: 'Missing service or path parameter',
      timestamp: new Date().toISOString(),
      path: req.url
    })
  }
  
  // TypeScript now knows serviceString is a string after the check above
  // Use non-null assertion since we've validated it above
  const validatedService: string = serviceString as string
  const validatedPath: string = pathString

  // Route all requests through API Gateway for proper load balancing and security
  // Use environment variable for backend URL, fallback to localhost for local development
  const apiGatewayUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
  const backendBaseUrl = apiGatewayUrl.replace(/\/$/, '') // Remove trailing slash
  
  // For services that go through API Gateway, use the gateway URL
  // For direct service access, construct URLs based on the backend base URL
  // In production (App Service), all services should go through the API Gateway on the VM
  const serviceUrls: { [key: string]: string } = {
    // All services route through API Gateway for production (VM deployment)
    user: backendBaseUrl,
    'user-service': backendBaseUrl,
    energy: backendBaseUrl,
    'energy-service': backendBaseUrl,
    device: backendBaseUrl,
    'device-service': backendBaseUrl,
    analytics: backendBaseUrl,
    'analytics-service': backendBaseUrl,
    billing: backendBaseUrl,
    'billing-service': backendBaseUrl,
    facility: backendBaseUrl,
    'facility-service': backendBaseUrl,
    'feature-flags': backendBaseUrl,
    'feature-flag-service': backendBaseUrl,
    'appliance-monitoring': backendBaseUrl,
    'appliance-monitoring-service': backendBaseUrl,
    'device-verification': backendBaseUrl,
    'device-verification-service': backendBaseUrl,
    inventory: backendBaseUrl,
    'inventory-service': backendBaseUrl,
  }

  const baseUrl = serviceUrls[validatedService]
  if (!baseUrl) {
    return res.status(400).json({ error: 'Invalid service' })
  }

  // Construct URL with query parameters
  // Add /api/v1 prefix for API Gateway routing if not already present
  const normalizedPath = validatedPath.startsWith('/api/v1') ? validatedPath : `/api/v1${validatedPath}`
  let url = `${baseUrl}${normalizedPath}`
  
  // Add query parameters
  const queryString = Object.keys(queryParams)
    .filter(key => key !== 'service' && key !== 'path')
    .map(key => {
      const value = queryParams[key]
      // Normalize query param value: can be string | string[] | undefined
      const normalizedValue = Array.isArray(value) ? value[0] : value || ''
      return `${key}=${encodeURIComponent(normalizedValue)}`
    })
    .join('&')
  
  if (queryString) {
    url += (url.includes('?') ? '&' : '?') + queryString
  }
  
  try {
    const response = await fetch(url, {
      method: req.method,
      headers: {
        'Content-Type': 'application/json',
        ...(req.headers.authorization && { Authorization: req.headers.authorization }),
      },
      body: req.method !== 'GET' ? JSON.stringify(req.body) : undefined,
    })

    // Check if response has content before parsing JSON
    const contentType = response.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
      const data = await response.json()
      res.status(response.status).json(data)
    } else {
      // Handle non-JSON responses
      const text = await response.text()
      res.status(response.status).send(text)
    }
  } catch (error: any) {
    console.error('Proxy error:', error)
    
    // Return standardized error response
    res.status(503).json({ 
      error: 'SERVICE_UNAVAILABLE',
      message: `${validatedService} service is temporarily unavailable`,
      timestamp: new Date().toISOString(),
      path: req.url,
      service: validatedService,
      suggestedAction: 'Please try again in a few minutes'
    })
  }
} 