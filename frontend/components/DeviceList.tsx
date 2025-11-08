import React, { useState } from 'react';

interface Device {
  deviceId: string;
  deviceType: string;
  hardwareId: string;
  customerType: string;
  status: string;
  activatedAt: string;
  expiresAt: string;
  validityDays: number;
  trustCategory: string;
}

interface DeviceListProps {
  devices: Device[];
  loading: boolean;
  onDeviceUpdated: () => void;
}

const DeviceList: React.FC<DeviceListProps> = ({ devices, loading, onDeviceUpdated }) => {
  const [selectedDevice, setSelectedDevice] = useState<Device | null>(null);
  const [showDetails, setShowDetails] = useState(false);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'EXPIRED':
        return 'bg-red-100 text-red-800';
      case 'SUSPENDED':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getTrustCategoryColor = (category: string) => {
    switch (category) {
      case 'OEM_LOCKED':
        return 'bg-blue-100 text-blue-800';
      case 'OFFLINE_LOCKED':
        return 'bg-purple-100 text-purple-800';
      case 'UNVERIFIED':
        return 'bg-orange-100 text-orange-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getDaysUntilExpiry = (expiresAt: string) => {
    const expiryDate = new Date(expiresAt);
    const now = new Date();
    const diffTime = expiryDate.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  const getExpiryStatus = (expiresAt: string) => {
    const daysUntilExpiry = getDaysUntilExpiry(expiresAt);
    if (daysUntilExpiry < 0) {
      return { text: 'Expired', color: 'text-red-600' };
    } else if (daysUntilExpiry <= 30) {
      return { text: `Expires in ${daysUntilExpiry} days`, color: 'text-yellow-600' };
    } else {
      return { text: `Expires in ${daysUntilExpiry} days`, color: 'text-green-600' };
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12" role="status" aria-live="polite" aria-busy="true">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" aria-hidden="true"></div>
        <span className="sr-only">Loading devices...</span>
      </div>
    );
  }

  if (devices.length === 0) {
    return (
      <div className="text-center py-12">
        <div className="text-gray-400 text-6xl mb-4">📱</div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">No devices found</h3>
        <p className="text-gray-500">Get started by activating your first SmartWatts device.</p>
      </div>
    );
  }

  return (
    <div className="bg-white shadow rounded-lg">
      <div className="px-6 py-4 border-b border-gray-200">
        <h3 className="text-lg font-medium text-gray-900">Device List</h3>
      </div>
      
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Device
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Type
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Trust Level
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Expiry
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {devices.map((device) => {
              const expiryStatus = getExpiryStatus(device.expiresAt);
              return (
                <tr key={device.deviceId} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm font-medium text-gray-900">{device.deviceId}</div>
                      <div className="text-sm text-gray-500">{device.hardwareId}</div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {device.deviceType.replace(/_/g, ' ')}
                    </div>
                    <div className="text-sm text-gray-500">
                      {device.customerType}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(device.status)}`}>
                      {device.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getTrustCategoryColor(device.trustCategory)}`}>
                      {device.trustCategory.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {formatDate(device.expiresAt)}
                    </div>
                    <div className={`text-sm ${expiryStatus.color}`}>
                      {expiryStatus.text}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button
                      onClick={() => {
                        setSelectedDevice(device);
                        setShowDetails(true);
                      }}
                      className="text-blue-600 hover:text-blue-900 mr-4"
                    >
                      View Details
                    </button>
                    {device.status === 'EXPIRED' && (
                      <button
                        onClick={() => {
                          // Note: Device renewal functionality to be implemented in future release
                        }}
                        className="text-green-600 hover:text-green-900"
                      >
                        Renew
                      </button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Device Details Modal */}
      {showDetails && selectedDevice && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-gray-900">Device Details</h2>
              <button
                onClick={() => setShowDetails(false)}
                className="text-gray-400 hover:text-gray-600 text-2xl font-bold"
              >
                ×
              </button>
            </div>

            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Device ID</label>
                  <p className="text-sm text-gray-900">{selectedDevice.deviceId}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Hardware ID</label>
                  <p className="text-sm text-gray-900">{selectedDevice.hardwareId}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Device Type</label>
                  <p className="text-sm text-gray-900">{selectedDevice.deviceType.replace(/_/g, ' ')}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Customer Type</label>
                  <p className="text-sm text-gray-900">{selectedDevice.customerType}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Status</label>
                  <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(selectedDevice.status)}`}>
                    {selectedDevice.status}
                  </span>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Trust Category</label>
                  <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getTrustCategoryColor(selectedDevice.trustCategory)}`}>
                    {selectedDevice.trustCategory.replace(/_/g, ' ')}
                  </span>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Activated At</label>
                  <p className="text-sm text-gray-900">{formatDate(selectedDevice.activatedAt)}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Expires At</label>
                  <p className="text-sm text-gray-900">{formatDate(selectedDevice.expiresAt)}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Validity Days</label>
                  <p className="text-sm text-gray-900">{selectedDevice.validityDays} days</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Days Until Expiry</label>
                  <p className={`text-sm ${getExpiryStatus(selectedDevice.expiresAt).color}`}>
                    {getExpiryStatus(selectedDevice.expiresAt).text}
                  </p>
                </div>
              </div>
            </div>

            <div className="flex justify-end pt-6">
              <button
                onClick={() => setShowDetails(false)}
                className="px-6 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DeviceList;
