import { NextApiRequest, NextApiResponse } from 'next'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  const { service, path, ...queryParams } = req.query
  
  if (!service || !path) {
    return res.status(400).json({ 
      error: 'MISSING_PARAMETERS',
      message: 'Missing service or path parameter',
      timestamp: new Date().toISOString(),
      path: req.url
    })
  }

  // Route all requests through API Gateway for proper load balancing and security
  const apiGatewayUrl = 'http://localhost:8080'
  
  const serviceUrls: { [key: string]: string } = {
    user: 'http://localhost:8081', // Direct to user service
    'user-service': 'http://localhost:8081', // Direct to user service
    energy: 'http://localhost:8082', // Direct to energy service
    'energy-service': 'http://localhost:8082', // Direct to energy service
    device: 'http://localhost:8083', // Direct to device service
    'device-service': 'http://localhost:8083', // Direct to device service
    analytics: 'http://localhost:8084', // Direct to analytics service
    'analytics-service': 'http://localhost:8084', // Direct to analytics service
    billing: apiGatewayUrl,
    'billing-service': apiGatewayUrl,
    facility: apiGatewayUrl,
    'facility-service': apiGatewayUrl,
    'feature-flags': 'http://localhost:8090', // Direct to feature flag service
    'feature-flag-service': 'http://localhost:8090', // Direct to feature flag service
    'appliance-monitoring': 'http://localhost:8087', // Direct to appliance monitoring service
    'appliance-monitoring-service': 'http://localhost:8087', // Direct to appliance monitoring service
    'device-verification': apiGatewayUrl,
    'device-verification-service': apiGatewayUrl,
    inventory: 'http://localhost:8081', // Direct to user service for inventory
    'inventory-service': 'http://localhost:8081', // Direct to user service for inventory
  }

  const baseUrl = serviceUrls[service as string]
  if (!baseUrl) {
    return res.status(400).json({ error: 'Invalid service' })
  }

  // Construct URL with query parameters
  // Add /api/v1 prefix for API Gateway routing if not already present
  let url = `${baseUrl}${path.startsWith('/api/v1') ? path : `/api/v1${path}`}`
  
  // Add query parameters
  const queryString = Object.keys(queryParams)
    .filter(key => key !== 'service' && key !== 'path')
    .map(key => `${key}=${queryParams[key]}`)
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
      message: `${service} service is temporarily unavailable`,
      timestamp: new Date().toISOString(),
      path: req.url,
      service,
      suggestedAction: 'Please try again in a few minutes'
    })
  }
} 