// API endpoints using Next.js proxy to bypass CORS
export const userApi = {
  login: (data: { email: string; password: string }) =>
    fetch('/api/proxy?service=user&path=/users/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => res.json()),
  register: (data: any) => 
    fetch('/api/proxy?service=user&path=/users/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => res.json()),
  getProfile: () => 
    fetch('/api/proxy?service=user&path=/users/profile', {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getUsers: (params?: any) => 
    fetch(`/api/proxy?service=user&path=/users${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
}

export const energyApi = {
  getReadings: (params?: any) => 
    fetch(`/api/proxy?service=energy&path=/energy/readings${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getCurrentReading: () => 
    fetch('/api/proxy?service=energy&path=/energy/readings/current', {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getConsumption: (params?: any) => 
    fetch(`/api/proxy?service=energy&path=/energy/consumption${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getSources: () => 
    fetch('/api/proxy?service=energy&path=/energy/sources', {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
}

export const deviceApi = {
  getDevices: (params?: any) => 
    fetch(`/api/proxy?service=device&path=/devices${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getDevice: (id: string) => 
    fetch(`/api/proxy?service=device&path=/devices/${id}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  createDevice: (data: any) =>
    fetch('/api/proxy?service=device&path=/devices/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => res.json()),
  updateDevice: (id: string, data: any) =>
    fetch(`/api/proxy?service=device&path=/devices/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => res.json()),
  deleteDevice: (id: string) =>
    fetch(`/api/proxy?service=device&path=/devices/${id}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getDeviceEvents: (id: string, params?: any) =>
    fetch(`/api/proxy?service=device&path=/devices/${id}/events${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
}

export const analyticsApi = {
  getAnalytics: (params?: any) => 
    fetch(`/api/proxy?service=analytics&path=/analytics${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getUsagePatterns: (params?: any) => 
    fetch(`/api/proxy?service=analytics&path=/analytics/patterns${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getReports: (params?: any) => 
    fetch(`/api/proxy?service=analytics&path=/analytics/reports${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
}

export const billingApi = {
  getBills: (params?: any) => 
    fetch(`/api/proxy?service=billing&path=/bills${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getBill: (id: string) => 
    fetch(`/api/proxy?service=billing&path=/bills/${id}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getTariffs: (params?: any) => 
    fetch(`/api/proxy?service=billing&path=/bills/tariffs${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  payBill: (id: string, data: any) => 
    fetch(`/api/proxy?service=billing&path=/bills/${id}/pay`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => res.json()),
}

export const facilityApi = {
  // Asset Management
  getAssets: (params?: any) => 
    fetch(`/api/proxy?service=facility&path=/api/v1/assets${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getAsset: (id: string) => 
    fetch(`/api/proxy?service=facility&path=/api/v1/assets/${id}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  createAsset: (data: any) =>
    fetch('/api/proxy?service=facility&path=/api/v1/assets', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => res.json()),
  updateAsset: (id: string, data: any) =>
    fetch(`/api/proxy?service=facility&path=/api/v1/assets/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => res.json()),
  deleteAsset: (id: string) =>
    fetch(`/api/proxy?service=facility&path=/api/v1/assets/${id}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),

  // Fleet Management
  getFleet: (params?: any) => 
    fetch(`/api/proxy?service=facility&path=/api/v1/fleet${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getFleetItem: (id: string) => 
    fetch(`/api/proxy?service=facility&path=/api/v1/fleet/${id}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  createFleetItem: (data: any) =>
    fetch('/api/proxy?service=facility&path=/api/v1/fleet', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => res.json()),
  updateFleetItem: (id: string, data: any) =>
    fetch(`/api/proxy?service=facility&path=/api/v1/fleet/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => res.json()),
  deleteFleetItem: (id: string) =>
    fetch(`/api/proxy?service=facility&path=/api/v1/fleet/${id}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),

  // Space Management
  getSpaces: (params?: any) => 
    fetch(`/api/proxy?service=facility&path=/api/v1/spaces${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getSpace: (id: string) => 
    fetch(`/api/proxy?service=facility&path=/api/v1/spaces/${id}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  createSpace: (data: any) =>
    fetch('/api/proxy?service=facility&path=/api/v1/spaces', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => res.json()),
  updateSpace: (id: string, data: any) =>
    fetch(`/api/proxy?service=facility&path=/api/v1/spaces/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => res.json()),
  deleteSpace: (id: string) =>
    fetch(`/api/proxy?service=facility&path=/api/v1/spaces/${id}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),

  // Work Order Management
  getWorkOrders: (params?: any) => 
    fetch(`/api/proxy?service=facility&path=/api/v1/work-orders${params ? `?${new URLSearchParams(params)}` : ''}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  getWorkOrder: (id: string) => 
    fetch(`/api/proxy?service=facility&path=/api/v1/work-orders/${id}`, {
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
  createWorkOrder: (data: any) =>
    fetch('/api/proxy?service=facility&path=/api/v1/work-orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => res.json()),
  updateWorkOrder: (id: string, data: any) =>
    fetch(`/api/proxy?service=facility&path=/api/v1/work-orders/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => res.json()),
  deleteWorkOrder: (id: string) =>
    fetch(`/api/proxy?service=facility&path=/api/v1/work-orders/${id}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
    }).then(res => res.json()),
} 