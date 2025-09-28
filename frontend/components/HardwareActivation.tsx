import React, { useState } from 'react'
import {
  BoltIcon,
  CogIcon,
  WrenchScrewdriverIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon,
  QrCodeIcon,
  CameraIcon,
  XMarkIcon,
} from '@heroicons/react/24/outline'

interface HardwareDevice {
  id: string
  deviceType: string
  serialNumber: string
  modelNumber: string
  macAddress: string
  isCertified: boolean
  isActivated: boolean
  activationToken: string
  partnerId?: string
}

interface HardwareActivationProps {
  onActivationComplete: (devices: HardwareDevice[]) => void
  onClose: () => void
}

const HardwareActivation: React.FC<HardwareActivationProps> = ({ onActivationComplete, onClose }) => {
  const [currentStep, setCurrentStep] = useState(1)
  const [devices, setDevices] = useState<HardwareDevice[]>([])
  const [activationToken, setActivationToken] = useState('')
  const [partnerId, setPartnerId] = useState('')
  const [showQRScanner, setShowQRScanner] = useState(false)
  const [loading, setLoading] = useState(false)

  const requiredDevices = [
    { type: 'GATEWAY', name: 'SmartWatts Gateway', icon: BoltIcon, required: true },
    { type: 'PZEM_016', name: 'PZEM-016 Energy Meter', icon: CogIcon, required: true },
    { type: 'SMART_PLUG', name: 'Smart Plug', icon: WrenchScrewdriverIcon, required: false },
    { type: 'RELAY', name: 'Relay Switch', icon: WrenchScrewdriverIcon, required: false },
    { type: 'CT_CLAMP', name: 'CT Clamp (100A)', icon: WrenchScrewdriverIcon, required: false },
  ]

  const handleQRScan = (data: string) => {
    // Extract partner ID from QR code data
    const urlParams = new URLSearchParams(data.split('?')[1])
    const scannedPartnerId = urlParams.get('partner')
    if (scannedPartnerId) {
      setPartnerId(scannedPartnerId)
    }
    setShowQRScanner(false)
  }

  const handleActivation = async () => {
    setLoading(true)
    
    try {
      // Simulate API call for device activation
      await new Promise(resolve => setTimeout(resolve, 2000))
      
      const activatedDevices = devices.map(device => ({
        ...device,
        isActivated: true,
        partnerId: partnerId || undefined
      }))
      
      setDevices(activatedDevices)
      setCurrentStep(3)
    } catch (error) {
      console.error('Activation failed:', error)
    } finally {
      setLoading(false)
    }
  }

  const addDevice = (deviceType: string) => {
    const device: HardwareDevice = {
      id: Date.now().toString(),
      deviceType,
      serialNumber: `SN${Date.now()}`,
      modelNumber: `SW-${deviceType}`,
      macAddress: `00:1B:44:11:3A:B${Math.floor(Math.random() * 9)}`,
      isCertified: true,
      isActivated: false,
      activationToken: Math.random().toString(36).substring(2, 15)
    }
    
    setDevices(prev => [...prev, device])
  }

  const removeDevice = (deviceId: string) => {
    setDevices(prev => prev.filter(device => device.id !== deviceId))
  }

  const getDeviceIcon = (deviceType: string) => {
    switch (deviceType) {
      case 'GATEWAY': return BoltIcon
      case 'PZEM_016': return CogIcon
      default: return WrenchScrewdriverIcon
    }
  }

  const getDeviceStatus = (device: HardwareDevice) => {
    if (!device.isCertified) return 'uncertified'
    if (device.isActivated) return 'activated'
    return 'pending'
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'activated': return 'text-green-600 dark:text-green-400'
      case 'pending': return 'text-yellow-600 dark:text-yellow-400'
      case 'uncertified': return 'text-red-600 dark:text-red-400'
      default: return 'text-gray-600 dark:text-gray-400'
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'activated': return CheckCircleIcon
      case 'pending': return ExclamationTriangleIcon
      case 'uncertified': return XMarkIcon
      default: return ExclamationTriangleIcon
    }
  }

  return (
    <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          {/* Header */}
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
                Hardware Activation
              </h2>
              <p className="text-gray-600 dark:text-gray-400">
                Step {currentStep} of 3: {currentStep === 1 ? 'Add Devices' : currentStep === 2 ? 'Activate' : 'Complete'}
              </p>
            </div>
            <button
              onClick={onClose}
              className="p-2 rounded-lg text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
            >
              <XMarkIcon className="h-6 w-6" />
            </button>
          </div>

          {/* Progress Bar */}
          <div className="mb-8">
            <div className="flex items-center justify-between mb-2">
              {[1, 2, 3].map((step) => (
                <div key={step} className="flex items-center">
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${
                    step <= currentStep
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-200 dark:bg-gray-700 text-gray-500 dark:text-gray-400'
                  }`}>
                    {step}
                  </div>
                  {step < 3 && (
                    <div className={`w-16 h-1 mx-2 ${
                      step < currentStep ? 'bg-blue-600' : 'bg-gray-200 dark:bg-gray-700'
                    }`} />
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Step 1: Add Devices */}
          {currentStep === 1 && (
            <div className="space-y-6">
              <div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                  Add Your SmartWatts Devices
                </h3>
                <p className="text-gray-600 dark:text-gray-400 mb-6">
                  Add all your SmartWatts certified devices. The Gateway is required for system activation.
                </p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {requiredDevices.map((device) => {
                  const IconComponent = device.icon
                  const isAdded = devices.some(d => d.deviceType === device.type)
                  
                  return (
                    <div
                      key={device.type}
                      className={`p-4 rounded-lg border-2 transition-all cursor-pointer ${
                        isAdded
                          ? 'border-green-500 bg-green-50 dark:bg-green-900/20'
                          : 'border-gray-200 dark:border-gray-700 hover:border-blue-500'
                      }`}
                      onClick={() => !isAdded && addDevice(device.type)}
                    >
                      <div className="flex items-center space-x-3">
                        <IconComponent className="h-6 w-6 text-gray-600 dark:text-gray-400" />
                        <div className="flex-1">
                          <h4 className="font-medium text-gray-900 dark:text-white">
                            {device.name}
                          </h4>
                          <p className="text-sm text-gray-500 dark:text-gray-400">
                            {device.required ? 'Required' : 'Optional'}
                          </p>
                        </div>
                        {isAdded && (
                          <CheckCircleIcon className="h-6 w-6 text-green-500" />
                        )}
                      </div>
                    </div>
                  )
                })}
              </div>

              {devices.length > 0 && (
                <div className="mt-6">
                  <h4 className="font-semibold text-gray-900 dark:text-white mb-3">Added Devices:</h4>
                  <div className="space-y-2">
                    {devices.map((device) => {
                      const IconComponent = getDeviceIcon(device.deviceType)
                      return (
                        <div key={device.id} className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                          <div className="flex items-center space-x-3">
                            <IconComponent className="h-5 w-5 text-gray-600 dark:text-gray-400" />
                            <div>
                              <p className="font-medium text-gray-900 dark:text-white">{device.deviceType}</p>
                              <p className="text-sm text-gray-500 dark:text-gray-400">{device.macAddress}</p>
                            </div>
                          </div>
                          <button
                            onClick={() => removeDevice(device.id)}
                            className="text-red-600 dark:text-red-400 hover:text-red-800 dark:hover:text-red-300"
                          >
                            <XMarkIcon className="h-5 w-5" />
                          </button>
                        </div>
                      )
                    })}
                  </div>
                </div>
              )}

              <div className="flex justify-between pt-6">
                <button
                  onClick={onClose}
                  className="px-4 py-2 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={() => setCurrentStep(2)}
                  disabled={!devices.some(d => d.deviceType === 'GATEWAY')}
                  className="px-6 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed text-white rounded-lg transition-colors"
                >
                  Continue
                </button>
              </div>
            </div>
          )}

          {/* Step 2: Activation */}
          {currentStep === 2 && (
            <div className="space-y-6">
              <div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                  Activate Your Devices
                </h3>
                <p className="text-gray-600 dark:text-gray-400 mb-6">
                  Enter your activation token and optionally scan a partner QR code.
                </p>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Activation Token
                  </label>
                  <input
                    type="text"
                    value={activationToken}
                    onChange={(e) => setActivationToken(e.target.value)}
                    placeholder="Enter your activation token"
                    className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Partner ID (Optional)
                  </label>
                  <div className="flex space-x-2">
                    <input
                      type="text"
                      value={partnerId}
                      onChange={(e) => setPartnerId(e.target.value)}
                      placeholder="Enter partner ID or scan QR code"
                      className="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                    <button
                      onClick={() => setShowQRScanner(true)}
                      className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
                    >
                      <CameraIcon className="h-5 w-5" />
                    </button>
                  </div>
                </div>
              </div>

              <div className="flex justify-between pt-6">
                <button
                  onClick={() => setCurrentStep(1)}
                  className="px-4 py-2 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 transition-colors"
                >
                  Back
                </button>
                <button
                  onClick={handleActivation}
                  disabled={!activationToken || loading}
                  className="px-6 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed text-white rounded-lg transition-colors"
                >
                  {loading ? 'Activating...' : 'Activate Devices'}
                </button>
              </div>
            </div>
          )}

          {/* Step 3: Complete */}
          {currentStep === 3 && (
            <div className="space-y-6">
              <div className="text-center">
                <CheckCircleIcon className="h-16 w-16 text-green-500 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                  Activation Complete!
                </h3>
                <p className="text-gray-600 dark:text-gray-400">
                  Your SmartWatts devices have been successfully activated.
                </p>
              </div>

              <div className="space-y-3">
                {devices.map((device) => {
                  const IconComponent = getDeviceIcon(device.deviceType)
                  const StatusIcon = getStatusIcon(getDeviceStatus(device))
                  
                  return (
                    <div key={device.id} className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                      <div className="flex items-center space-x-3">
                        <IconComponent className="h-5 w-5 text-gray-600 dark:text-gray-400" />
                        <div>
                          <p className="font-medium text-gray-900 dark:text-white">{device.deviceType}</p>
                          <p className="text-sm text-gray-500 dark:text-gray-400">{device.macAddress}</p>
                        </div>
                      </div>
                      <StatusIcon className={`h-5 w-5 ${getStatusColor(getDeviceStatus(device))}`} />
                    </div>
                  )
                })}
              </div>

              <div className="flex justify-center pt-6">
                <button
                  onClick={() => {
                    onActivationComplete(devices)
                    onClose()
                  }}
                  className="px-6 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors"
                >
                  Complete Setup
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* QR Scanner Modal */}
      {showQRScanner && (
        <div className="fixed inset-0 z-60 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl max-w-md w-full">
            <div className="p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                  Scan Partner QR Code
                </h3>
                <button
                  onClick={() => setShowQRScanner(false)}
                  className="p-2 rounded-lg text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
                >
                  <XMarkIcon className="h-5 w-5" />
                </button>
              </div>
              
              <div className="aspect-square bg-gray-100 dark:bg-gray-700 rounded-lg flex items-center justify-center">
                <div className="text-center">
                  <QrCodeIcon className="h-12 w-12 text-gray-400 mx-auto mb-2" />
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    QR Scanner would be implemented here
                  </p>
                </div>
              </div>
              
              <div className="mt-4 text-center">
                <button
                  onClick={() => {
                    setPartnerId('DEMO001')
                    setShowQRScanner(false)
                  }}
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
                >
                  Use Demo Partner ID
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default HardwareActivation 