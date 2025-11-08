import React, { useState, useEffect } from 'react'
import { useRouter } from 'next/router'
import Layout from '../components/Layout'
import { useAuth } from '../hooks/useAuth'
import { useFeatureFlags } from '../hooks/useFeatureFlags'
import {
  BoltIcon,
  CogIcon,
  ExclamationTriangleIcon,
  WrenchScrewdriverIcon,
  CloudIcon,
  SunIcon,
  ChartBarIcon,
  PlusIcon,
} from '@heroicons/react/24/outline'

interface Appliance {
  id: string
  applianceName: string
  applianceType: string
  manufacturer: string
  model: string
  location: string
  ratedPowerWatts: number
  isActive: boolean
}

interface ApplianceReading {
  id: string
  applianceId: string
  timestamp: string
  realTimePowerWatts: number
  voltageVolts: number
  currentAmps: number
  efficiencyPercentage: number
  temperatureCelsius: number
  operatingStatus: string
  anomalyDetected: boolean
  maintenanceAlert: boolean
}

interface WeatherData {
  temperatureCelsius: number
  humidityPercentage: number
  solarIrradianceWm2: number
  weatherCondition: string
  energyImpactScore: number
  seasonalAdjustmentFactor: number
}

export default function ApplianceMonitoring() {
  const { user } = useAuth()
  const router = useRouter()
  const { isFeatureEnabled } = useFeatureFlags()
  const [appliances, setAppliances] = useState<Appliance[]>([])
  const [readings, setReadings] = useState<ApplianceReading[]>([])
  const [weatherData, setWeatherData] = useState<WeatherData | null>(null)
  const [loading, setLoading] = useState(true)
  const [showAddAppliance, setShowAddAppliance] = useState(false)
  const [selectedRoom, setSelectedRoom] = useState('')
  const [selectedApplianceType, setSelectedApplianceType] = useState('')

  // Room-specific appliance suggestions
  const roomApplianceSuggestions = {
    // Home locations
    'LIVING_ROOM': ['Television', 'Air Conditioner', 'Fan', 'Lighting', 'Sound System', 'Gaming Console'],
    'BEDROOM': ['Air Conditioner', 'Fan', 'Lighting', 'Television', 'Charging Station', 'Alarm Clock'],
    'KITCHEN': ['Refrigerator', 'Microwave', 'Oven', 'Dishwasher', 'Blender', 'Coffee Maker', 'Water Heater'],
    'BATHROOM': ['Water Heater', 'Lighting', 'Exhaust Fan', 'Hair Dryer', 'Electric Shaver'],
    'DINING_ROOM': ['Lighting', 'Fan', 'Air Conditioner', 'Chandelier'],
    'GARAGE': ['Lighting', 'Power Tools', 'Battery Charger', 'Air Compressor'],
    'OUTDOOR': ['Lighting', 'Water Pump', 'Security Camera', 'Pool Equipment'],
    
    // Office & Commercial locations
    'HOME_OFFICE': ['Computer', 'Monitor', 'Printer', 'Lighting', 'Fan', 'Air Conditioner', 'Desk Lamp', 'Router'],
    'CORPORATE_OFFICE': ['Computer', 'Monitor', 'Printer', 'Lighting', 'Air Conditioner', 'Projector', 'Phone System', 'Router'],
    'COWORKING_SPACE': ['Computer', 'Monitor', 'Lighting', 'Air Conditioner', 'Printer', 'Coffee Machine', 'Phone Booth'],
    'MEETING_ROOM': ['Projector', 'Display Screen', 'Lighting', 'Air Conditioner', 'Phone System', 'Whiteboard', 'Video Conferencing'],
    'CONFERENCE_ROOM': ['Projector', 'Display Screen', 'Lighting', 'Air Conditioner', 'Phone System', 'Video Conferencing', 'Sound System'],
    'RECEPTION': ['Computer', 'Monitor', 'Lighting', 'Air Conditioner', 'Phone System', 'Printer', 'Security System'],
    'BREAK_ROOM': ['Refrigerator', 'Microwave', 'Coffee Machine', 'Water Dispenser', 'Lighting', 'Air Conditioner'],
    'SERVER_ROOM': ['Server Rack', 'UPS', 'Cooling System', 'Lighting', 'Fire Suppression', 'Network Switch', 'Router'],
    'STORAGE_ROOM': ['Lighting', 'Air Conditioner', 'Security Camera', 'Motion Sensor', 'Shelving System'],
    'RETAIL_STORE': ['POS System', 'Lighting', 'Air Conditioner', 'Security Camera', 'Cash Register', 'Display Screens'],
    'RESTAURANT': ['Refrigerator', 'Oven', 'Microwave', 'Coffee Machine', 'Lighting', 'Air Conditioner', 'POS System'],
    'WAREHOUSE': ['Forklift Charger', 'Lighting', 'Air Conditioner', 'Security Camera', 'Conveyor Belt', 'Loading Equipment'],
    'FACTORY': ['Production Equipment', 'Lighting', 'Air Conditioner', 'Safety Systems', 'Conveyor Belt', 'Quality Control'],
    'LABORATORY': ['Lab Equipment', 'Fume Hood', 'Lighting', 'Air Conditioner', 'Safety Systems', 'Microscope', 'Centrifuge'],
    'MEDICAL_CLINIC': ['Medical Equipment', 'Lighting', 'Air Conditioner', 'Patient Monitor', 'X-Ray Machine', 'Ultrasound'],
    'SCHOOL': ['Computer', 'Projector', 'Lighting', 'Air Conditioner', 'Interactive Whiteboard', 'Sound System'],
    'HOTEL': ['Air Conditioner', 'Lighting', 'Television', 'Mini Refrigerator', 'Coffee Machine', 'Safe', 'Phone'],
    'GYM': ['Treadmill', 'Exercise Bike', 'Lighting', 'Air Conditioner', 'Sound System', 'Water Dispenser', 'Security Camera']
  }

  const handleCloseModal = () => {
    setShowAddAppliance(false)
    setSelectedRoom('')
    setSelectedApplianceType('')
  }

  useEffect(() => {
    // Redirect super admins to admin dashboard
    if (user?.role === 'ROLE_ENTERPRISE_ADMIN') {
      router.replace('/admin/dashboard')
      return
    }
    
    if (user && isFeatureEnabled('APPLIANCE_MONITORING')) {
      loadApplianceData()
      loadWeatherData()
      loadReadingsData()
    }
  }, [user, router]) // Removed isFeatureEnabled from dependencies to prevent infinite loop

  // Don't render customer pages for super admins
  if (user?.role === 'ROLE_ENTERPRISE_ADMIN') {
    return null
  }

  const loadApplianceData = async () => {
    setLoading(true)
    try {
      const token = localStorage.getItem('token')
      const response = await fetch('/api/proxy?service=appliance-monitoring&path=/appliances/user/' + user?.id, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (response.ok) {
        const data = await response.json()
        setAppliances(data)
      } else {
        console.error('Failed to load appliance data:', response.status, response.statusText)
        setAppliances([])
      }
    } catch (error) {
      console.error('Error loading appliance data:', error)
      setAppliances([])
    } finally {
      setLoading(false)
    }
  }

  const loadReadingsData = async () => {
    try {
      const token = localStorage.getItem('token')
      
      // First get all appliances for the user
      const appliancesResponse = await fetch(`/api/proxy?service=appliance-monitoring&path=/appliances/user/${user?.id}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (appliancesResponse.ok) {
        const appliances = await appliancesResponse.json()
        
        // Get readings for each appliance
        const readingsPromises = appliances.map(async (appliance: any) => {
          const now = new Date()
          const startTime = new Date(now.getTime() - 24 * 60 * 60 * 1000) // Last 24 hours
          
          try {
            const readingsResponse = await fetch(
              `/api/proxy?service=appliance-monitoring&path=/appliances/${appliance.id}/readings?startTime=${startTime.toISOString()}&endTime=${now.toISOString()}`,
              {
                headers: {
                  'Authorization': `Bearer ${token}`,
                  'Content-Type': 'application/json'
                }
              }
            )
            
            if (readingsResponse.ok) {
              const readings = await readingsResponse.json()
              return readings.map((reading: any) => ({
                ...reading,
                applianceId: appliance.id,
                applianceName: appliance.applianceName
              }))
            }
            return []
          } catch (error) {
            console.error(`Error loading readings for appliance ${appliance.id}:`, error)
            return []
          }
        })
        
        const allReadings = await Promise.all(readingsPromises)
        const flatReadings = allReadings.flat()
        setReadings(flatReadings)
      } else {
        console.error('Failed to load appliances:', appliancesResponse.status, appliancesResponse.statusText)
        setReadings([])
      }
    } catch (error) {
      console.error('Error loading readings data:', error)
      setReadings([])
    }
  }

  const loadWeatherData = async () => {
    try {
      const token = localStorage.getItem('token')
      // Default to Lagos coordinates for demo
      const response = await fetch('/api/proxy?service=appliance-monitoring&path=/weather/lagos&latitude=6.5244&longitude=3.3792', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (response.ok) {
        const data = await response.json()
        setWeatherData(data)
      } else {
        console.error('Failed to load weather data:', response.status, response.statusText)
        setWeatherData(null)
      }
    } catch (error) {
      console.error('Error loading weather data:', error)
      setWeatherData(null)
    }
  }

  if (!isFeatureEnabled('APPLIANCE_MONITORING')) {
    return (
      <Layout>
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center">
            <CogIcon className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">Feature Not Available</h3>
            <p className="mt-1 text-sm text-gray-500">
              Appliance monitoring is not available in your current plan.
            </p>
          </div>
        </div>
      </Layout>
    )
  }

  if (loading) {
    return (
      <Layout>
        <div className="min-h-screen flex items-center justify-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
        </div>
      </Layout>
    )
  }

  return (
    <Layout>
      <div className="space-y-6 animate-fade-in">
        {/* Header */}
        <div className="bg-gradient-to-r from-gray-800 via-blue-900 to-gray-800 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold mb-2">Appliance Monitoring</h1>
              <p className="text-blue-200 text-lg">Real-time power consumption tracking and efficiency analysis</p>
              <div className="flex items-center mt-4 space-x-6">
                <div className="flex items-center space-x-2">
                  <BoltIcon className="h-5 w-5 text-blue-300" />
                  <span className="text-sm">Active Appliances: {appliances.filter(a => a.isActive).length}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <CloudIcon className="h-5 w-5 text-green-300" />
                  <span className="text-sm">Weather Integration: Active</span>
                </div>
              </div>
            </div>
            <div className="hidden md:block">
              <div className="w-24 h-24 bg-white/10 rounded-full flex items-center justify-center">
                <CogIcon className="h-12 w-12 text-white" />
              </div>
            </div>
          </div>
        </div>

        {/* Weather Integration Card */}
        {weatherData && (
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Weather Integration</h2>
              <CloudIcon className="h-8 w-8 text-blue-500" />
            </div>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="text-center">
                <p className="text-sm text-gray-500 dark:text-gray-400">Temperature</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">
                  {weatherData.temperatureCelsius}°C
                </p>
              </div>
              <div className="text-center">
                <p className="text-sm text-gray-500 dark:text-gray-400">Humidity</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">
                  {weatherData.humidityPercentage}%
                </p>
              </div>
              <div className="text-center">
                <p className="text-sm text-gray-500 dark:text-gray-400">Solar Irradiance</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">
                  {weatherData.solarIrradianceWm2} W/m²
                </p>
              </div>
              <div className="text-center">
                <p className="text-sm text-gray-500 dark:text-gray-400">Energy Impact</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">
                  {weatherData.energyImpactScore}/100
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Appliances Overview */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6 border border-gray-200 dark:border-gray-700">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Your Appliances</h2>
            <button
              onClick={() => setShowAddAppliance(true)}
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2 transition-colors duration-200"
            >
              <PlusIcon className="h-5 w-5" />
              <span>Add Appliance</span>
            </button>
          </div>

          {appliances.length === 0 ? (
            <div className="text-center py-12">
              <CogIcon className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900 dark:text-white">No appliances yet</h3>
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                Start monitoring your appliances by adding your first one.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {appliances.map((appliance) => (
                <ApplianceCard key={appliance.id} appliance={appliance} />
              ))}
            </div>
          )}
        </div>

        {/* Efficiency Analytics */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6 border border-gray-200 dark:border-gray-700">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Efficiency Analytics</h2>
            <ChartBarIcon className="h-8 w-8 text-green-500" />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center p-4 bg-green-50 dark:bg-green-900/20 rounded-xl">
              <div className="text-3xl font-bold text-green-600 dark:text-green-400">87%</div>
              <div className="text-sm text-green-600 dark:text-green-400">Average Efficiency</div>
            </div>
            <div className="text-center p-4 bg-blue-50 dark:bg-blue-900/20 rounded-xl">
              <div className="text-3xl font-bold text-blue-600 dark:text-blue-400">2.4 kW</div>
              <div className="text-sm text-blue-600 dark:text-blue-400">Total Power</div>
            </div>
            <div className="text-center p-4 bg-purple-50 dark:bg-purple-900/20 rounded-xl">
              <div className="text-3xl font-bold text-purple-600 dark:text-purple-400">₦1,240</div>
              <div className="text-sm text-purple-600 dark:text-purple-400">Monthly Cost</div>
            </div>
          </div>
        </div>

        {/* Add Appliance Modal */}
        {showAddAppliance && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl p-6 w-full max-w-md mx-4">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Add New Appliance</h3>
                <button
                  onClick={handleCloseModal}
                  className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              
              <form className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Location
                  </label>
                  <select 
                    value={selectedRoom}
                    onChange={(e) => setSelectedRoom(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                  >
                    <option value="">Select location</option>
                    <optgroup label="Home">
                      <option value="LIVING_ROOM">Living Room</option>
                      <option value="BEDROOM">Bedroom</option>
                      <option value="KITCHEN">Kitchen</option>
                      <option value="BATHROOM">Bathroom</option>
                      <option value="DINING_ROOM">Dining Room</option>
                      <option value="GARAGE">Garage</option>
                      <option value="OUTDOOR">Outdoor</option>
                    </optgroup>
                    <optgroup label="Office & Commercial">
                      <option value="HOME_OFFICE">Home Office</option>
                      <option value="CORPORATE_OFFICE">Corporate Office</option>
                      <option value="COWORKING_SPACE">Coworking Space</option>
                      <option value="MEETING_ROOM">Meeting Room</option>
                      <option value="CONFERENCE_ROOM">Conference Room</option>
                      <option value="RECEPTION">Reception Area</option>
                      <option value="BREAK_ROOM">Break Room</option>
                      <option value="SERVER_ROOM">Server Room</option>
                      <option value="STORAGE_ROOM">Storage Room</option>
                      <option value="RETAIL_STORE">Retail Store</option>
                      <option value="RESTAURANT">Restaurant</option>
                      <option value="WAREHOUSE">Warehouse</option>
                      <option value="FACTORY">Factory/Production</option>
                      <option value="LABORATORY">Laboratory</option>
                      <option value="MEDICAL_CLINIC">Medical Clinic</option>
                      <option value="SCHOOL">School/Educational</option>
                      <option value="HOTEL">Hotel</option>
                      <option value="GYM">Gym/Fitness Center</option>
                    </optgroup>
                    <optgroup label="Other">
                      <option value="OTHER">Other</option>
                    </optgroup>
                  </select>
                </div>

                {/* Room-specific appliance suggestions */}
                {selectedRoom && roomApplianceSuggestions[selectedRoom as keyof typeof roomApplianceSuggestions] && (
                  <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Suggested Appliances for {selectedRoom.replace('_', ' ').toLowerCase()}
                    </label>
                    <div className="flex flex-wrap gap-2">
                      {roomApplianceSuggestions[selectedRoom as keyof typeof roomApplianceSuggestions].map((appliance) => (
                        <button
                          key={appliance}
                          type="button"
                          onClick={() => setSelectedApplianceType(appliance.toUpperCase().replace(' ', '_'))}
                          className="px-3 py-1 text-xs bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 rounded-full hover:bg-blue-200 dark:hover:bg-blue-900/50 transition-colors"
                        >
                          {appliance}
                        </button>
                      ))}
                    </div>
                  </div>
                )}
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Appliance Type
                  </label>
                  <select 
                    value={selectedApplianceType}
                    onChange={(e) => setSelectedApplianceType(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                  >
                    <option value="">Select type</option>
                    <option value="AIR_CONDITIONER">Air Conditioner</option>
                    <option value="REFRIGERATOR">Refrigerator</option>
                    <option value="WASHING_MACHINE">Washing Machine</option>
                    <option value="DISHWASHER">Dishwasher</option>
                    <option value="WATER_HEATER">Water Heater</option>
                    <option value="LIGHTING">Lighting</option>
                    <option value="TELEVISION">Television</option>
                    <option value="COMPUTER">Computer/Laptop</option>
                    <option value="FAN">Fan</option>
                    <option value="MICROWAVE">Microwave</option>
                    <option value="OVEN">Oven</option>
                    <option value="BLENDER">Blender</option>
                    <option value="COFFEE_MAKER">Coffee Maker</option>
                    <option value="SOUND_SYSTEM">Sound System</option>
                    <option value="GAMING_CONSOLE">Gaming Console</option>
                    <option value="CHARGING_STATION">Charging Station</option>
                    <option value="ALARM_CLOCK">Alarm Clock</option>
                    <option value="EXHAUST_FAN">Exhaust Fan</option>
                    <option value="HAIR_DRYER">Hair Dryer</option>
                    <option value="ELECTRIC_SHAVER">Electric Shaver</option>
                    <option value="CHANDELIER">Chandelier</option>
                    <option value="MONITOR">Monitor</option>
                    <option value="PRINTER">Printer</option>
                    <option value="POWER_TOOLS">Power Tools</option>
                    <option value="BATTERY_CHARGER">Battery Charger</option>
                    <option value="AIR_COMPRESSOR">Air Compressor</option>
                    <option value="WATER_PUMP">Water Pump</option>
                    <option value="SECURITY_CAMERA">Security Camera</option>
                    <option value="POOL_EQUIPMENT">Pool Equipment</option>
                    <option value="PROJECTOR">Projector</option>
                    <option value="DISPLAY_SCREEN">Display Screen</option>
                    <option value="PHONE_SYSTEM">Phone System</option>
                    <option value="VIDEO_CONFERENCING">Video Conferencing</option>
                    <option value="WHITEBOARD">Whiteboard</option>
                    <option value="SOUND_SYSTEM">Sound System</option>
                    <option value="ROUTER">Router</option>
                    <option value="NETWORK_SWITCH">Network Switch</option>
                    <option value="UPS">UPS (Uninterruptible Power Supply)</option>
                    <option value="SERVER_RACK">Server Rack</option>
                    <option value="COOLING_SYSTEM">Cooling System</option>
                    <option value="FIRE_SUPPRESSION">Fire Suppression</option>
                    <option value="MOTION_SENSOR">Motion Sensor</option>
                    <option value="POS_SYSTEM">POS System</option>
                    <option value="CASH_REGISTER">Cash Register</option>
                    <option value="FORKLIFT_CHARGER">Forklift Charger</option>
                    <option value="CONVEYOR_BELT">Conveyor Belt</option>
                    <option value="LOADING_EQUIPMENT">Loading Equipment</option>
                    <option value="PRODUCTION_EQUIPMENT">Production Equipment</option>
                    <option value="SAFETY_SYSTEMS">Safety Systems</option>
                    <option value="QUALITY_CONTROL">Quality Control Equipment</option>
                    <option value="LAB_EQUIPMENT">Lab Equipment</option>
                    <option value="FUME_HOOD">Fume Hood</option>
                    <option value="MICROSCOPE">Microscope</option>
                    <option value="CENTRIFUGE">Centrifuge</option>
                    <option value="MEDICAL_EQUIPMENT">Medical Equipment</option>
                    <option value="PATIENT_MONITOR">Patient Monitor</option>
                    <option value="X_RAY_MACHINE">X-Ray Machine</option>
                    <option value="ULTRASOUND">Ultrasound</option>
                    <option value="INTERACTIVE_WHITEBOARD">Interactive Whiteboard</option>
                    <option value="TREADMILL">Treadmill</option>
                    <option value="EXERCISE_BIKE">Exercise Bike</option>
                    <option value="WATER_DISPENSER">Water Dispenser</option>
                    <option value="SAFE">Safe</option>
                    <option value="PHONE">Phone</option>
                    <option value="DESK_LAMP">Desk Lamp</option>
                    <option value="PHONE_BOOTH">Phone Booth</option>
                    <option value="SHELVING_SYSTEM">Shelving System</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Appliance Name
                  </label>
                  <input
                    type="text"
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                    placeholder="e.g., Main AC, Master Bedroom TV"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Manufacturer
                  </label>
                  <input
                    type="text"
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                    placeholder="e.g., Samsung"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Model
                  </label>
                  <input
                    type="text"
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                    placeholder="e.g., AR12TXHAAWK"
                  />
                </div>
                
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Rated Power (Watts)
                  </label>
                  <input
                    type="number"
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                    placeholder="e.g., 2000"
                  />
                </div>
                
                <div className="flex space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={handleCloseModal}
                    className="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                  >
                    Add Appliance
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </Layout>
  )
}

function ApplianceCard({ appliance }: { appliance: Appliance }) {
  const [isExpanded, setIsExpanded] = useState(false)

  return (
    <div className="bg-gray-50 dark:bg-gray-700 rounded-xl p-4 border border-gray-200 dark:border-gray-600">
      <div className="flex items-center justify-between mb-3">
        <h3 className="font-semibold text-gray-900 dark:text-white">{appliance.applianceName || 'Unknown Appliance'}</h3>
        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
          appliance.isActive 
            ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
            : 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        }`}>
          {appliance.isActive ? 'Active' : 'Inactive'}
        </span>
      </div>
      
      <div className="space-y-2 text-sm text-gray-600 dark:text-gray-300">
        <p><span className="font-medium">Type:</span> {appliance.applianceType?.replace('_', ' ') || 'Unknown'}</p>
        <p><span className="font-medium">Name:</span> {appliance.applianceName || 'Unknown'}</p>
        <p><span className="font-medium">Power:</span> {appliance.ratedPowerWatts || 0}W</p>
      </div>

      <div className="mt-4 flex space-x-2">
        <button
          onClick={() => setIsExpanded(!isExpanded)}
          className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded-lg text-sm transition-colors duration-200"
        >
          {isExpanded ? 'Hide Details' : 'View Details'}
        </button>
      </div>

      {isExpanded && (
        <div className="mt-4 pt-4 border-t border-gray-200 dark:border-gray-600">
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p className="text-gray-500 dark:text-gray-400">Current Power</p>
              <p className="font-medium text-gray-900 dark:text-white">1,250W</p>
            </div>
            <div>
              <p className="text-gray-500 dark:text-gray-400">Efficiency</p>
              <p className="font-medium text-gray-900 dark:text-white">89%</p>
            </div>
            <div>
              <p className="text-gray-500 dark:text-gray-400">Temperature</p>
              <p className="font-medium text-gray-900 dark:text-white">45°C</p>
            </div>
            <div>
              <p className="text-gray-500 dark:text-gray-400">Status</p>
              <p className="font-medium text-green-600 dark:text-green-400">Normal</p>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
