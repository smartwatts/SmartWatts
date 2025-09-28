import React, { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useFeatureFlags } from '../hooks/useFeatureFlags';
import Layout from '../components/Layout';
import DeviceActivationModal from '../components/DeviceActivationModal';
import DeviceList from '../components/DeviceList';
import DeviceStatusCard from '../components/DeviceStatusCard';

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

const DeviceManagement: React.FC = () => {
  const { user, loading } = useAuth();
  const { isFeatureEnabled } = useFeatureFlags();
  const [devices, setDevices] = useState<Device[]>([]);
  const [showActivationModal, setShowActivationModal] = useState(false);
  const [loadingDevices, setLoadingDevices] = useState(false);

  // Check if user has access to device management
  const hasDeviceManagementAccess = isFeatureEnabled('DEVICE_MANAGEMENT');

  useEffect(() => {
    if (user && hasDeviceManagementAccess) {
      fetchDevices();
    }
  }, [user, hasDeviceManagementAccess]);

  const fetchDevices = async () => {
    setLoadingDevices(true);
    try {
      const response = await fetch('/api/proxy?service=device-verification&path=/devices', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });
      
      if (response.ok) {
        const data = await response.json();
        setDevices(data.devices || []);
      }
    } catch (error) {
      console.error('Failed to fetch devices:', error);
    } finally {
      setLoadingDevices(false);
    }
  };

  const handleDeviceActivated = (newDevice: Device) => {
    setDevices(prev => [...prev, newDevice]);
    setShowActivationModal(false);
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex justify-center items-center min-h-screen">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
        </div>
      </Layout>
    );
  }

  if (!user) {
    return (
      <Layout>
        <div className="text-center py-12">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">Authentication Required</h1>
          <p className="text-gray-600">Please log in to access device management.</p>
        </div>
      </Layout>
    );
  }

  if (!hasDeviceManagementAccess) {
    return (
      <Layout>
        <div className="text-center py-12">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">Feature Not Available</h1>
          <p className="text-gray-600 mb-6">Device management is not available in your current plan.</p>
          <button className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors">
            Upgrade Plan
          </button>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Device Management</h1>
          <p className="text-gray-600">Manage and monitor your SmartWatts devices</p>
        </div>

        {/* Device Status Overview */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <DeviceStatusCard
            title="Total Devices"
            count={devices.length}
            icon="📱"
            color="blue"
          />
          <DeviceStatusCard
            title="Active Devices"
            count={devices.filter(d => d.status === 'ACTIVE').length}
            icon="✅"
            color="green"
          />
          <DeviceStatusCard
            title="Expired Devices"
            count={devices.filter(d => d.status === 'EXPIRED').length}
            icon="⚠️"
            color="yellow"
          />
        </div>

        {/* Action Buttons */}
        <div className="flex justify-between items-center mb-6">
          <div className="flex space-x-4">
            <button
              onClick={() => setShowActivationModal(true)}
              className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
            >
              <span>➕</span>
              <span>Activate New Device</span>
            </button>
            <button
              onClick={fetchDevices}
              disabled={loadingDevices}
              className="bg-gray-600 text-white px-6 py-3 rounded-lg hover:bg-gray-700 transition-colors disabled:opacity-50"
            >
              {loadingDevices ? '🔄' : '🔄'} Refresh
            </button>
          </div>
        </div>

        {/* Device List */}
        <DeviceList 
          devices={devices} 
          loading={loadingDevices}
          onDeviceUpdated={fetchDevices}
        />

        {/* Device Activation Modal */}
        {showActivationModal && (
          <DeviceActivationModal
            onClose={() => setShowActivationModal(false)}
            onDeviceActivated={handleDeviceActivated}
          />
        )}
      </div>
    </Layout>
  );
};

export default DeviceManagement;
