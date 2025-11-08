import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';

interface DeviceActivationModalProps {
  onClose: () => void;
  onDeviceActivated: (device: any) => void;
}

interface DeviceActivationForm {
  deviceId: string;
  deviceType: string;
  hardwareId: string;
  customerType: string;
  firmwareHash?: string;
  firmwareVersion?: string;
  locationLat?: number;
  locationLng?: number;
}

const DeviceActivationModal: React.FC<DeviceActivationModalProps> = ({
  onClose,
  onDeviceActivated,
}) => {
  const { user } = useAuth();
  const [formData, setFormData] = useState<DeviceActivationForm>({
    deviceId: '',
    deviceType: 'SMART_METER',
    hardwareId: '',
    customerType: 'RESIDENTIAL',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const deviceTypes = [
    'SMART_METER',
    'SOLAR_INVERTER',
    'GENERATOR_MONITOR',
    'BATTERY_STORAGE',
    'LOAD_CONTROLLER',
    'ENERGY_MONITOR',
    'GRID_TIE_INVERTER',
    'MICROGRID_CONTROLLER',
    'DEMAND_RESPONSE_DEVICE',
    'ENERGY_ANALYZER',
  ];

  const customerTypes = ['RESIDENTIAL', 'COMMERCIAL', 'INDUSTRIAL'];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await fetch('/api/proxy?service=device-verification&path=/activate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        const result = await response.json();
        if (result.success) {
          onDeviceActivated(result);
        } else {
          setError(result.message || 'Device activation failed');
        }
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Device activation failed');
      }
    } catch (error) {
      setError('Network error occurred');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleNumberInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value ? parseFloat(value) : undefined,
    }));
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-8 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Activate New Device</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 text-2xl font-bold"
            aria-label="Close"
          >
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Device ID */}
          <div>
            <label htmlFor="deviceId" className="block text-sm font-medium text-gray-700 mb-2">
              Device ID *
            </label>
            <input
              type="text"
              id="deviceId"
              name="deviceId"
              value={formData.deviceId}
              onChange={handleInputChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter unique device identifier"
            />
          </div>

          {/* Device Type */}
          <div>
            <label htmlFor="deviceType" className="block text-sm font-medium text-gray-700 mb-2">
              Device Type *
            </label>
            <select
              id="deviceType"
              name="deviceType"
              value={formData.deviceType}
              onChange={handleInputChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {deviceTypes.map(type => (
                <option key={type} value={type}>
                  {type.replace(/_/g, ' ')}
                </option>
              ))}
            </select>
          </div>

          {/* Hardware ID */}
          <div>
            <label htmlFor="hardwareId" className="block text-sm font-medium text-gray-700 mb-2">
              Hardware ID *
            </label>
            <input
              type="text"
              id="hardwareId"
              name="hardwareId"
              value={formData.hardwareId}
              onChange={handleInputChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter hardware serial number or MAC address"
            />
          </div>

          {/* Customer Type */}
          <div>
            <label htmlFor="customerType" className="block text-sm font-medium text-gray-700 mb-2">
              Customer Type *
            </label>
            <select
              id="customerType"
              name="customerType"
              value={formData.customerType}
              onChange={handleInputChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {customerTypes.map(type => (
                <option key={type} value={type}>
                  {type.charAt(0) + type.slice(1).toLowerCase()}
                </option>
              ))}
            </select>
            <p className="text-sm text-gray-500 mt-1">
              {formData.customerType === 'RESIDENTIAL' 
                ? 'Residential customers get 12 months validity'
                : 'Commercial/Industrial customers get 3 months validity initially'
              }
            </p>
          </div>

          {/* Firmware Hash */}
          <div>
            <label htmlFor="firmwareHash" className="block text-sm font-medium text-gray-700 mb-2">
              Firmware Hash
            </label>
            <input
              type="text"
              id="firmwareHash"
              name="firmwareHash"
              value={formData.firmwareHash || ''}
              onChange={handleInputChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Optional: Enter firmware hash for verification"
            />
          </div>

          {/* Firmware Version */}
          <div>
            <label htmlFor="firmwareVersion" className="block text-sm font-medium text-gray-700 mb-2">
              Firmware Version
            </label>
            <input
              type="text"
              id="firmwareVersion"
              name="firmwareVersion"
              value={formData.firmwareVersion || ''}
              onChange={handleInputChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Optional: Enter firmware version"
            />
          </div>

          {/* Location Coordinates */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="locationLat" className="block text-sm font-medium text-gray-700 mb-2">
                Latitude
              </label>
              <input
                type="number"
                id="locationLat"
                name="locationLat"
                step="any"
                value={formData.locationLat || ''}
                onChange={handleNumberInputChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Optional: Enter latitude"
              />
            </div>
            <div>
              <label htmlFor="locationLng" className="block text-sm font-medium text-gray-700 mb-2">
                Longitude
              </label>
              <input
                type="number"
                id="locationLng"
                name="locationLng"
                step="any"
                value={formData.locationLng || ''}
                onChange={handleNumberInputChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Optional: Enter longitude"
              />
            </div>
          </div>

          {/* Error Display */}
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-md p-4">
              <p className="text-red-800 text-sm">{error}</p>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex justify-end space-x-4 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors disabled:opacity-50"
            >
              {loading ? 'Activating...' : 'Activate Device'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default DeviceActivationModal;
