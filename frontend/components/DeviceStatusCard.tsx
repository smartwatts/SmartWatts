import React from 'react';

interface DeviceStatusCardProps {
  title: string;
  count: number;
  icon: string;
  color: 'blue' | 'green' | 'yellow' | 'red' | 'purple';
}

const DeviceStatusCard: React.FC<DeviceStatusCardProps> = ({ title, count, icon, color }) => {
  const getColorClasses = (color: string) => {
    switch (color) {
      case 'blue':
        return 'bg-blue-50 border-blue-200 text-blue-800';
      case 'green':
        return 'bg-green-50 border-green-200 text-green-800';
      case 'yellow':
        return 'bg-yellow-50 border-yellow-200 text-yellow-800';
      case 'red':
        return 'bg-red-50 border-red-200 text-red-800';
      case 'purple':
        return 'bg-purple-50 border-purple-200 text-purple-800';
      default:
        return 'bg-gray-50 border-gray-200 text-gray-800';
    }
  };

  const getIconColorClasses = (color: string) => {
    switch (color) {
      case 'blue':
        return 'bg-blue-100 text-blue-600';
      case 'green':
        return 'bg-green-100 text-green-600';
      case 'yellow':
        return 'bg-yellow-100 text-yellow-600';
      case 'red':
        return 'bg-red-100 text-red-600';
      case 'purple':
        return 'bg-purple-100 text-purple-600';
      default:
        return 'bg-gray-100 text-gray-600';
    }
  };

  return (
    <div className={`bg-white overflow-hidden shadow rounded-lg border ${getColorClasses(color)}`}>
      <div className="p-5">
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${getIconColorClasses(color)}`}>
              <span className="text-lg">{icon}</span>
            </div>
          </div>
          <div className="ml-5 w-0 flex-1">
            <dl>
              <dt className="text-sm font-medium text-gray-500 truncate">{title}</dt>
              <dd className="text-3xl font-bold text-gray-900">{count}</dd>
            </dl>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DeviceStatusCard;
