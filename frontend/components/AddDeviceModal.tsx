import React, { useState, useEffect } from 'react'
import { XMarkIcon, DevicePhoneMobileIcon, WifiIcon, CogIcon } from '@heroicons/react/24/outline'
// import { deviceApi } from '../utils/api'

interface AddDeviceModalProps {
  isOpen: boolean;
  onClose: () => void;
  onDeviceAdded: () => void;
}

interface DeviceFormData {
  name: string;
  deviceId: string;
  serialNumber: string;
  manufacturer: string;
  model: string;
  deviceType: string;
  protocol: string;
  location: string;
  notes: string;
  samplePayload: string;
  verificationNotes: string;
}

const deviceTypes = [
  { value: 'SMART_METER', label: 'Smart Meter', icon: DevicePhoneMobileIcon },
  { value: 'SOLAR_INVERTER', label: 'Solar Inverter', icon: CogIcon },
  { value: 'GENERATOR_MONITOR', label: 'Generator Monitor', icon: CogIcon },
  { value: 'ENVIRONMENTAL_SENSOR', label: 'Environmental Sensor', icon: CogIcon },
  { value: 'CURRENT_TRANSFORMER', label: 'Current Transformer', icon: CogIcon },
  { value: 'VOLTAGE_TRANSFORMER', label: 'Voltage Transformer', icon: CogIcon },
  { value: 'POWER_QUALITY_MONITOR', label: 'Power Quality Monitor', icon: CogIcon },
  { value: 'BATTERY_MONITOR', label: 'Battery Monitor', icon: CogIcon },
  { value: 'GATEWAY_DEVICE', label: 'Gateway Device', icon: CogIcon },
  { value: 'CUSTOM_DEVICE', label: 'Custom Device', icon: CogIcon },
]

const protocols = [
  { value: 'MQTT', label: 'MQTT' },
  { value: 'MODBUS_TCP', label: 'Modbus TCP' },
  { value: 'MODBUS_RTU', label: 'Modbus RTU' },
  { value: 'HTTP_REST', label: 'HTTP REST' },
  { value: 'WEBSOCKET', label: 'WebSocket' },
  { value: 'CUSTOM', label: 'Custom' },
]

export default function AddDeviceModal({ isOpen, onClose, onDeviceAdded }: AddDeviceModalProps) {
  const [formData, setFormData] = useState<DeviceFormData>({
    name: '',
    deviceId: '',
    serialNumber: '',
    manufacturer: '',
    model: '',
    deviceType: '',
    protocol: '',
    location: '',
    notes: '',
    samplePayload: '',
    verificationNotes: ''
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [verificationRequired, setVerificationRequired] = useState(false);
  const [trustLevel, setTrustLevel] = useState<string>('UNVERIFIED');

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      // First, register the device
      const deviceResponse = await fetch('/api/proxy/device-service/api/v1/devices', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: formData.name,
          deviceId: formData.deviceId,
          serialNumber: formData.serialNumber,
          manufacturer: formData.manufacturer,
          model: formData.model,
          deviceType: formData.deviceType,
          protocol: formData.protocol,
          location: formData.location,
          notes: formData.notes,
          userId: '550e8400-e29b-41d4-a716-446655440000' // Mock user ID
        }),
      });

      if (!deviceResponse.ok) {
        throw new Error('Failed to register device');
      }

      const device = await deviceResponse.json();

      // If device requires verification, submit verification request
      if (verificationRequired) {
        const verificationResponse = await fetch('/api/proxy/device-service/api/v1/device-verification/submit', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            deviceId: device.id,
            samplePayload: formData.samplePayload,
            notes: formData.verificationNotes,
            brand: formData.manufacturer,
            model: formData.model,
            preferredProtocol: formData.protocol
          }),
        });

        if (!verificationResponse.ok) {
          throw new Error('Failed to submit verification request');
        }
      }

      onDeviceAdded();
      onClose();
      resetForm();
    } catch (error) {
      console.error('Error adding device:', error);
      alert('Failed to add device. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      deviceId: '',
      serialNumber: '',
      manufacturer: '',
      model: '',
      deviceType: '',
      protocol: '',
      location: '',
      notes: '',
      samplePayload: '',
      verificationNotes: ''
    });
    setVerificationRequired(false);
    setTrustLevel('UNVERIFIED');
  };

  // Check if device requires verification based on manufacturer
  useEffect(() => {
    const isSmartWattsOEM = formData.manufacturer.toLowerCase().includes('smartwatts') ||
                           formData.manufacturer.toLowerCase().includes('smartwatts oem') ||
                           formData.manufacturer.toLowerCase().includes('smartwatts certified') ||
                           formData.deviceId.startsWith('SW_') ||
                           formData.deviceId.startsWith('OEM_') ||
                           formData.serialNumber.startsWith('SW');
    
    setVerificationRequired(!isSmartWattsOEM);
    setTrustLevel(isSmartWattsOEM ? 'OEM_LOCKED' : 'UNVERIFIED');
  }, [formData.manufacturer, formData.deviceId, formData.serialNumber]);

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-gray-800 rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="p-6 border-b border-gray-700">
          <div className="flex items-center justify-between">
            <h2 className="text-2xl font-bold text-white">Add New Device</h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-white transition-colors"
              aria-label="Close"
            >
              <XMarkIcon className="h-6 w-6" />
            </button>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Basic Information */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-white border-b border-gray-700 pb-2">
                Basic Information
              </h3>
              
              <div>
                <label htmlFor="device-name" className="block text-sm font-medium text-gray-300 mb-2">
                  Device Name *
                </label>
                <input
                  id="device-name"
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  required
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Enter device name"
                />
              </div>

              <div>
                <label htmlFor="device-id" className="block text-sm font-medium text-gray-300 mb-2">
                  Device ID
                </label>
                <input
                  id="device-id"
                  type="text"
                  name="deviceId"
                  value={formData.deviceId}
                  onChange={handleInputChange}
                  required
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Enter unique device identifier"
                />
              </div>

              <div>
                <label htmlFor="serial-number" className="block text-sm font-medium text-gray-300 mb-2">
                  Serial Number
                </label>
                <input
                  id="serial-number"
                  type="text"
                  name="serialNumber"
                  value={formData.serialNumber}
                  onChange={handleInputChange}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Enter device serial number"
                />
              </div>

              <div>
                <label htmlFor="manufacturer" className="block text-sm font-medium text-gray-300 mb-2">
                  Manufacturer
                </label>
                <input
                  id="manufacturer"
                  type="text"
                  name="manufacturer"
                  value={formData.manufacturer}
                  onChange={handleInputChange}
                  required
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="e.g., SmartWatts, Shelly, IoTaWatt"
                />
              </div>

              <div>
                <label htmlFor="model" className="block text-sm font-medium text-gray-300 mb-2">
                  Model
                </label>
                <input
                  id="model"
                  type="text"
                  name="model"
                  value={formData.model}
                  onChange={handleInputChange}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Enter device model"
                />
              </div>

              <div>
                <label htmlFor="device-type" className="block text-sm font-medium text-gray-300 mb-2">
                  Device Type *
                </label>
                <select
                  id="device-type"
                  name="deviceType"
                  value={formData.deviceType}
                  onChange={handleInputChange}
                  required
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  {deviceTypes.map(type => (
                    <option key={type.value} value={type.value}>
                      {type.label}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="location" className="block text-sm font-medium text-gray-300 mb-2">
                  Location
                </label>
                <input
                  id="location"
                  type="text"
                  name="location"
                  value={formData.location}
                  onChange={handleInputChange}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="e.g., Lagos, Nigeria or Building A, Floor 2"
                />
              </div>

              <div>
                <label htmlFor="notes" className="block text-sm font-medium text-gray-300 mb-2">
                  Notes
                </label>
                <textarea
                  id="notes"
                  name="notes"
                  value={formData.notes}
                  onChange={handleInputChange}
                  rows={3}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Additional notes about the device..."
                />
              </div>
            </div>

            {/* Protocol Selection */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-white border-b border-gray-700 pb-2">
                Communication Protocol
              </h3>
              
              <div>
                <label htmlFor="protocol" className="block text-sm font-medium text-gray-300 mb-2">
                  Protocol *
                </label>
                <select
                  id="protocol"
                  name="protocol"
                  value={formData.protocol}
                  onChange={handleInputChange}
                  required
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  {protocols.map(protocol => (
                    <option key={protocol.value} value={protocol.value}>
                      {protocol.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          {/* Device Details */}
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-white border-b border-gray-700 pb-2">
              Device Details
            </h3>
            
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">
                  Manufacturer
                </label>
                <input
                  type="text"
                  name="manufacturer"
                  value={formData.manufacturer}
                  onChange={handleInputChange}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="e.g., Schneider Electric"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">
                  Model
                </label>
                <input
                  type="text"
                  name="model"
                  value={formData.model}
                  onChange={handleInputChange}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="e.g., Acti9 iC60N"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">
                  Serial Number
                </label>
                <input
                  type="text"
                  name="serialNumber"
                  value={formData.serialNumber}
                  onChange={handleInputChange}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Device serial number"
                />
              </div>
            </div>
          </div>

          {/* Trust Level Display */}
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-white border-b border-gray-700 pb-2">
              Device Trust Level
            </h3>
            
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Trust Level
              </label>
              <div className={`px-3 py-2 rounded-md text-sm font-medium ${
                trustLevel === 'OEM_LOCKED' 
                  ? 'bg-green-100 text-green-800 border border-green-200' 
                  : 'bg-yellow-100 text-yellow-800 border border-yellow-200'
              }`}>
                {trustLevel === 'OEM_LOCKED' ? '✅ OEM Locked - Immediate Access' : '⚠️ Unverified - Requires Manual Verification'}
              </div>
              <p className="text-xs text-gray-400 mt-1">
                {trustLevel === 'OEM_LOCKED' 
                  ? 'This device will have immediate access to SmartWatts analytics and insights.'
                  : 'This device must be manually verified by SmartWatts team before data processing.'
                }
              </p>
            </div>

            {verificationRequired && (
              <>
                <div className="mb-4">
                  <label htmlFor="sample-payload" className="block text-sm font-medium text-gray-300 mb-2">
                    Sample Payload <span className="text-red-500">*</span>
                  </label>
                  <textarea
                    id="sample-payload"
                    name="samplePayload"
                    value={formData.samplePayload}
                    onChange={handleInputChange}
                    className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    rows={4}
                    placeholder="Paste a sample JSON payload or data output from your device..."
                    required
                  />
                  <p className="text-xs text-gray-400 mt-1">
                    Required for verification. Include sample data output from your device.
                  </p>
                </div>

                <div className="mb-4">
                  <label htmlFor="verification-notes" className="block text-sm font-medium text-gray-300 mb-2">
                    Verification Notes
                  </label>
                  <textarea
                    id="verification-notes"
                    name="verificationNotes"
                    value={formData.verificationNotes}
                    onChange={handleInputChange}
                    className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    rows={3}
                    placeholder="Additional information about your device setup, firmware version, etc."
                  />
                  <p className="text-xs text-gray-400 mt-1">
                    Optional: Help our team understand your device configuration.
                  </p>
                </div>

                <div className="bg-blue-900 border border-blue-700 rounded-md p-4 mb-4">
                  <div className="flex">
                    <div className="flex-shrink-0">
                      <svg className="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                      </svg>
                    </div>
                    <div className="ml-3">
                      <h3 className="text-sm font-medium text-blue-300">
                        Verification Required
                      </h3>
                      <div className="mt-2 text-sm text-blue-200">
                        <p>
                          This device requires manual verification by the SmartWatts team. 
                          Your verification request will be reviewed within 24-48 hours.
                        </p>
                        <p className="mt-2">
                          <strong>What happens next:</strong>
                        </p>
                        <ul className="list-disc list-inside mt-1 space-y-1">
                          <li>Submit verification request with sample payload</li>
                          <li>SmartWatts team reviews device capabilities</li>
                          <li>If approved, you'll receive device credentials</li>
                          <li>Configure device with credentials to start data collection</li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </>
            )}

            {!verificationRequired && (
              <div className="bg-green-900 border border-green-700 rounded-md p-4 mb-4">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <svg className="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-green-300">
                      OEM Device - Immediate Access
                    </h3>
                    <div className="mt-2 text-sm text-green-200">
                      <p>
                        This SmartWatts OEM device will have immediate access to all platform features.
                        No verification required - you can start using it right away.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Action Buttons */}
          <div className="flex justify-end space-x-4 pt-6 border-t border-gray-700">
            <button
              type="button"
              onClick={onClose}
              className="px-6 py-2 border border-gray-600 text-gray-300 rounded-lg hover:border-gray-500 hover:text-white transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-6 py-2 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-lg hover:from-blue-700 hover:to-blue-800 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isSubmitting ? 'Processing...' : verificationRequired ? 'Submit for Verification' : 'Add Device'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
