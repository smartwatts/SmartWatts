import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { 
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
  BoltIcon,
  HomeIcon,
  TruckIcon,
  TvIcon,
  LightBulbIcon
} from '@heroicons/react/24/outline';

interface ApplianceDetection {
  id: string;
  applianceName: string;
  applianceType: string;
  confidenceScore: number;
  powerConsumption: number;
  status: 'DETECTED' | 'UNKNOWN' | 'CONFIRMED' | 'REJECTED';
  detectionTime: string;
}

interface ApplianceRecognitionWidgetProps {
  deviceId: string;
}

const ApplianceRecognitionWidget: React.FC<ApplianceRecognitionWidgetProps> = ({ deviceId }) => {
  const [detections, setDetections] = useState<ApplianceDetection[]>([]);
  const [loading, setLoading] = useState(true);
  const [trainingMode, setTrainingMode] = useState(false);

  useEffect(() => {
    fetchApplianceDetections();
  }, [deviceId]);

  const fetchApplianceDetections = async () => {
    try {
      setLoading(true);
      const response = await fetch(`/api/v1/appliance-recognition/devices/${deviceId}/detections`);
      if (response.ok) {
        const data = await response.json();
        setDetections(data);
      }
    } catch (error) {
      console.error('Error fetching appliance detections:', error);
    } finally {
      setLoading(false);
    }
  };

  const getApplianceIcon = (type: string) => {
    switch (type) {
      case 'WASHING_MACHINE':
        return <HomeIcon className="h-5 w-5" />;
      case 'REFRIGERATOR':
        return <HomeIcon className="h-5 w-5" />;
      case 'AIR_CONDITIONER':
        return <HomeIcon className="h-5 w-5" />;
      case 'EV_CHARGER':
        return <TruckIcon className="h-5 w-5" />;
      case 'TELEVISION':
        return <TvIcon className="h-5 w-5" />;
      case 'LIGHTING':
        return <LightBulbIcon className="h-5 w-5" />;
      default:
        return <ExclamationTriangleIcon className="h-5 w-5" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'DETECTED':
        return 'bg-blue-100 text-blue-800';
      case 'CONFIRMED':
        return 'bg-green-100 text-green-800';
      case 'UNKNOWN':
        return 'bg-yellow-100 text-yellow-800';
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getConfidenceColor = (score: number) => {
    if (score >= 0.8) return 'text-green-600';
    if (score >= 0.6) return 'text-yellow-600';
    return 'text-red-600';
  };

  const confirmDetection = async (detectionId: string, confirmed: boolean, feedback?: string) => {
    try {
      const response = await fetch(`/api/v1/appliance-recognition/detections/${detectionId}/confirm`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          confirmed,
          userFeedback: feedback,
        }),
      });

      if (response.ok) {
        fetchApplianceDetections();
      }
    } catch (error) {
      console.error('Error confirming detection:', error);
    }
  };

  const trainAppliance = async (applianceName: string, applianceType: string) => {
    try {
      const response = await fetch(`/api/v1/appliance-recognition/devices/${deviceId}/train`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          applianceName,
          applianceType,
        }),
      });

      if (response.ok) {
        setTrainingMode(false);
        fetchApplianceDetections();
      }
    } catch (error) {
      console.error('Error training appliance:', error);
    }
  };

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <HomeIcon className="h-5 w-5" />
            Appliance Recognition
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center h-32">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <HomeIcon className="h-5 w-5" />
            Appliance Recognition
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setTrainingMode(!trainingMode)}
          >
            {trainingMode ? 'Cancel Training' : 'Train New Appliance'}
          </Button>
        </CardTitle>
      </CardHeader>
      <CardContent>
        {trainingMode && (
          <div className="mb-4 p-4 bg-blue-50 rounded-lg">
            <h4 className="font-medium mb-2">Train New Appliance</h4>
            <p className="text-sm text-gray-600 mb-3">
              Turn on the appliance you want to train and let it run for a few minutes.
            </p>
            <div className="flex gap-2">
              <Button size="sm" onClick={() => trainAppliance('My Washing Machine', 'WASHING_MACHINE')}>
                Train Washing Machine
              </Button>
              <Button size="sm" onClick={() => trainAppliance('My Air Conditioner', 'AIR_CONDITIONER')}>
                Train AC
              </Button>
            </div>
          </div>
        )}

        <div className="space-y-3">
          {detections.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              <HomeIcon className="h-12 w-12 mx-auto mb-2 opacity-50" />
              <p>No appliances detected yet</p>
              <p className="text-sm">Start using your appliances to see them here</p>
            </div>
          ) : (
            detections.map((detection) => (
              <div key={detection.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <div className="flex items-center gap-3">
                  {getApplianceIcon(detection.applianceType)}
                  <div>
                    <div className="font-medium">{detection.applianceName}</div>
                    <div className="text-sm text-gray-600">
                      {detection.powerConsumption}W • {detection.applianceType.replace('_', ' ')}
                    </div>
                  </div>
                </div>
                
                <div className="flex items-center gap-2">
                  <Badge className={getStatusColor(detection.status)}>
                    {detection.status}
                  </Badge>
                  
                  <div className="flex items-center gap-1">
                    <span className={`text-sm font-medium ${getConfidenceColor(detection.confidenceScore)}`}>
                      {Math.round(detection.confidenceScore * 100)}%
                    </span>
                    <Progress 
                      value={detection.confidenceScore * 100} 
                      className="w-16 h-2"
                    />
                  </div>
                  
                  {detection.status === 'DETECTED' && (
                    <div className="flex gap-1">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => confirmDetection(detection.id, true)}
                      >
                        <CheckCircleIcon className="h-4 w-4" />
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => confirmDetection(detection.id, false)}
                      >
                        <ExclamationTriangleIcon className="h-4 w-4" />
                      </Button>
                    </div>
                  )}
                </div>
              </div>
            ))
          )}
        </div>

        <div className="mt-4 pt-4 border-t">
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <ClockIcon className="h-4 w-4" />
            <span>Last updated: {new Date().toLocaleTimeString()}</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default ApplianceRecognitionWidget;
