import { NextApiRequest, NextApiResponse } from 'next'

const EDGE_GATEWAY_BASE_URL = process.env.EDGE_GATEWAY_URL || 'http://localhost:8088'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  const { path } = req.query
  const pathString = Array.isArray(path) ? path.join('/') : path || ''
  
  try {
    const url = `${EDGE_GATEWAY_BASE_URL}/api/v1/${pathString}`
    
    // Handle different HTTP methods
    const method = req.method || 'GET'
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    }

    // Add authorization header if present
    if (req.headers.authorization) {
      headers.Authorization = req.headers.authorization
    }

    const fetchOptions: RequestInit = {
      method,
      headers,
    }

    // Add body for POST, PUT, PATCH requests
    if (['POST', 'PUT', 'PATCH'].includes(method) && req.body) {
      fetchOptions.body = JSON.stringify(req.body)
    }

    // Add query parameters
    const queryParams = new URLSearchParams()
    Object.entries(req.query).forEach(([key, value]) => {
      if (key !== 'path' && value) {
        queryParams.append(key, Array.isArray(value) ? value[0] : value)
      }
    })

    const finalUrl = queryParams.toString() ? `${url}?${queryParams.toString()}` : url

    console.log(`Edge Gateway Proxy: ${method} ${finalUrl}`)

    const response = await fetch(finalUrl, fetchOptions)
    
    if (!response.ok) {
      console.error(`Edge Gateway Proxy Error: ${response.status} ${response.statusText}`)
      return res.status(response.status).json({
        error: `Edge Gateway request failed: ${response.status} ${response.statusText}`,
        path: pathString,
        method,
        status: response.status
      })
    }

    const data = await response.json()
    
    // Log successful responses for debugging
    console.log(`Edge Gateway Proxy Success: ${method} ${pathString}`, {
      status: response.status,
      dataKeys: Object.keys(data)
    })

    return res.status(response.status).json(data)

  } catch (error) {
    console.error('Edge Gateway Proxy Error:', error)
    
    // Return mock data for testing when edge gateway is not available
    if (pathString === 'health') {
      return res.status(200).json({
        status: 'UP',
        service: 'edge-gateway',
        timestamp: new Date().toISOString(),
        version: '1.0.0',
        note: 'Mock response - Edge Gateway not available'
      })
    }

    if (pathString === 'devices/status') {
      return res.status(200).json({
        status: 'OK',
        deviceCount: 0,
        connectedDevices: [],
        note: 'Mock response - Edge Gateway not available'
      })
    }

    if (pathString === 'energy/status') {
      return res.status(200).json({
        status: 'OK',
        energyData: [],
        lastUpdate: new Date().toISOString(),
        note: 'Mock response - Edge Gateway not available'
      })
    }

    if (pathString === 'analytics/status') {
      return res.status(200).json({
        status: 'OK',
        analyticsData: [],
        lastUpdate: new Date().toISOString(),
        note: 'Mock response - Edge Gateway not available'
      })
    }

    if (pathString === 'mqtt/publish') {
      return res.status(200).json({
        status: 'OK',
        message: 'MQTT message published successfully',
        topic: req.body?.topic || 'unknown',
        timestamp: new Date().toISOString(),
        note: 'Mock response - Edge Gateway not available'
      })
    }

    if (pathString === 'modbus/read') {
      return res.status(200).json({
        status: 'OK',
        deviceId: req.body?.deviceId || 'unknown',
        register: req.body?.register || 0,
        value: Math.floor(Math.random() * 1000) + 1000, // Mock value
        timestamp: new Date().toISOString(),
        note: 'Mock response - Edge Gateway not available'
      })
    }

    if (pathString === 'ml/forecast') {
      return res.status(200).json({
        status: 'OK',
        deviceId: req.body?.deviceId || 'unknown',
        forecast: {
          period: req.body?.forecastPeriod || '1h',
          predictedConsumption: Math.random() * 5 + 2,
          confidence: 0.85,
          factors: ['Historical patterns', 'Weather conditions', 'Time of day']
        },
        timestamp: new Date().toISOString(),
        note: 'Mock response - Edge Gateway not available'
      })
    }

    if (pathString === 'storage/status') {
      return res.status(200).json({
        status: 'OK',
        storageType: 'H2 Database',
        dataCount: 0,
        lastSync: new Date().toISOString(),
        note: 'Mock response - Edge Gateway not available'
      })
    }

    if (pathString === 'storage/local/data') {
      return res.status(200).json([])
    }

    // Default error response
    return res.status(500).json({
      error: 'Edge Gateway proxy error',
      message: error instanceof Error ? error.message : 'Unknown error',
      path: pathString,
      method: req.method,
      note: 'Mock responses available for testing when Edge Gateway is offline'
    })
  }
}
