import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { 
  Sun, 
  AlertTriangle, 
  CheckCircle, 
  Activity,
  Thermometer,
  Zap,
  RefreshCw
} from 'lucide-react';

interface SolarPanel {
  panelId: string;
  panelNumber: number;
  voltage: number;
  current: number;
  power: number;
  temperature: number;
  status: 'OPTIMAL' | 'GOOD' | 'UNDERPERFORMING' | 'FAULT';
  efficiency: number;
  hasFault: boolean;
}

interface SolarString {
  stringId: string;
  stringNumber: number;
  voltage: number;
  current: number;
  power: number;
  status: 'OPTIMAL' | 'GOOD' | 'UNDERPERFORMING' | 'FAULT';
  efficiency: number;
}

interface SolarArrayData {
  strings: SolarString[];
  panels: SolarPanel[];
  totalGeneration: number;
  averageEfficiency: number;
  faultCount: number;
}

interface SolarArrayHeatmapProps {
  inverterId: string;
}

const SolarArrayHeatmap: React.FC<SolarArrayHeatmapProps> = ({ inverterId }) => {
  const [arrayData, setArrayData] = useState<SolarArrayData | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedView, setSelectedView] = useState<'panels' | 'strings'>('panels');

  useEffect(() => {
    fetchSolarArrayData();
    const interval = setInterval(fetchSolarArrayData, 30000); // Update every 30 seconds
    return () => clearInterval(interval);
  }, [inverterId]);

  const fetchSolarArrayData = async () => {
    try {
      setLoading(true);
      const response = await fetch(`/api/v1/solar/inverters/${inverterId}/heatmap`);
      if (response.ok) {
        const data = await response.json();
        setArrayData(data);
      }
    } catch (error) {
      console.error('Error fetching solar array data:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPTIMAL':
        return 'bg-green-500';
      case 'GOOD':
        return 'bg-blue-500';
      case 'UNDERPERFORMING':
        return 'bg-yellow-500';
      case 'FAULT':
        return 'bg-red-500';
      default:
        return 'bg-gray-500';
    }
  };

  const getStatusTextColor = (status: string) => {
    switch (status) {
      case 'OPTIMAL':
        return 'text-green-800';
      case 'GOOD':
        return 'text-blue-800';
      case 'UNDERPERFORMING':
        return 'text-yellow-800';
      case 'FAULT':
        return 'text-red-800';
      default:
        return 'text-gray-800';
    }
  };

  const formatPower = (power: number) => {
    if (power >= 1000) {
      return `${(power / 1000).toFixed(1)}kW`;
    }
    return `${power.toFixed(0)}W`;
  };

  const formatEfficiency = (efficiency: number) => {
    return `${(efficiency * 100).toFixed(1)}%`;
  };

  const renderPanelGrid = () => {
    if (!arrayData) return null;

    // Group panels by string
    const panelsByString = arrayData.panels.reduce((acc, panel) => {
      const stringNumber = Math.floor((panel.panelNumber - 1) / 6) + 1; // Assuming 6 panels per string
      if (!acc[stringNumber]) {
        acc[stringNumber] = [];
      }
      acc[stringNumber].push(panel);
      return acc;
    }, {} as Record<number, SolarPanel[]>);

    return (
      <div className="space-y-4">
        {Object.entries(panelsByString).map(([stringNumber, panels]) => (
          <div key={stringNumber} className="space-y-2">
            <div className="flex items-center gap-2">
              <h4 className="font-medium">String {stringNumber}</h4>
              <Badge variant="outline">
                {panels.length} panels
              </Badge>
            </div>
            <div className="grid grid-cols-6 gap-2">
              {panels.map((panel) => (
                <div
                  key={panel.panelId}
                  className={`relative p-3 rounded-lg border-2 transition-all hover:scale-105 cursor-pointer ${
                    panel.hasFault ? 'border-red-500' : 'border-gray-200'
                  }`}
                  style={{
                    backgroundColor: getStatusColor(panel.status),
                    opacity: panel.hasFault ? 0.7 : 1
                  }}
                >
                  <div className="text-center">
                    <div className={`text-xs font-bold ${getStatusTextColor(panel.status)}`}>
                      P{panel.panelNumber}
                    </div>
                    <div className={`text-xs ${getStatusTextColor(panel.status)}`}>
                      {formatPower(panel.power)}
                    </div>
                    <div className={`text-xs ${getStatusTextColor(panel.status)}`}>
                      {formatEfficiency(panel.efficiency)}
                    </div>
                    {panel.hasFault && (
                      <AlertTriangle className="h-4 w-4 text-red-800 mx-auto mt-1" />
                    )}
                  </div>
                  
                  {/* Temperature indicator */}
                  <div className="absolute top-1 right-1">
                    <div className="flex items-center gap-1">
                      <Thermometer className="h-3 w-3" />
                      <span className="text-xs">{panel.temperature}°C</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    );
  };

  const renderStringView = () => {
    if (!arrayData) return null;

    return (
      <div className="grid grid-cols-2 gap-4">
        {arrayData.strings.map((string) => (
          <div
            key={string.stringId}
            className={`p-4 rounded-lg border-2 transition-all hover:scale-105 cursor-pointer ${
              string.status === 'FAULT' ? 'border-red-500' : 'border-gray-200'
            }`}
            style={{
              backgroundColor: getStatusColor(string.status),
              opacity: string.status === 'FAULT' ? 0.7 : 1
            }}
          >
            <div className="text-center">
              <div className={`text-lg font-bold ${getStatusTextColor(string.status)}`}>
                String {string.stringNumber}
              </div>
              <div className={`text-sm ${getStatusTextColor(string.status)}`}>
                {formatPower(string.power)}
              </div>
              <div className={`text-sm ${getStatusTextColor(string.status)}`}>
                {formatEfficiency(string.efficiency)}
              </div>
              <div className={`text-xs ${getStatusTextColor(string.status)}`}>
                {string.voltage}V • {string.current}A
              </div>
              {string.status === 'FAULT' && (
                <AlertTriangle className="h-5 w-5 text-red-800 mx-auto mt-2" />
              )}
            </div>
          </div>
        ))}
      </div>
    );
  };

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Sun className="h-5 w-5" />
            Solar Array Heatmap
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

  if (!arrayData) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Sun className="h-5 w-5" />
            Solar Array Heatmap
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-gray-500">
            <Sun className="h-12 w-12 mx-auto mb-2 opacity-50" />
            <p>No solar array data available</p>
            <p className="text-sm">Configure your solar inverter to see the heatmap</p>
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
            <Sun className="h-5 w-5" />
            Solar Array Heatmap
          </div>
          <div className="flex items-center gap-2">
            <div className="flex bg-gray-100 rounded-lg p-1">
              <Button
                variant={selectedView === 'panels' ? 'default' : 'ghost'}
                size="sm"
                onClick={() => setSelectedView('panels')}
              >
                Panels
              </Button>
              <Button
                variant={selectedView === 'strings' ? 'default' : 'ghost'}
                size="sm"
                onClick={() => setSelectedView('strings')}
              >
                Strings
              </Button>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={fetchSolarArrayData}
            >
              <RefreshCw className="h-4 w-4" />
            </Button>
          </div>
        </CardTitle>
      </CardHeader>
      <CardContent>
        {/* Summary Stats */}
        <div className="grid grid-cols-4 gap-4 mb-6">
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">
              {formatPower(arrayData.totalGeneration)}
            </div>
            <div className="text-sm text-gray-600">Total Generation</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">
              {formatEfficiency(arrayData.averageEfficiency)}
            </div>
            <div className="text-sm text-gray-600">Avg Efficiency</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-red-600">
              {arrayData.faultCount}
            </div>
            <div className="text-sm text-gray-600">Faults</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-gray-600">
              {arrayData.panels.length}
            </div>
            <div className="text-sm text-gray-600">Total Panels</div>
          </div>
        </div>

        {/* Heatmap */}
        <div className="mb-4">
          {selectedView === 'panels' ? renderPanelGrid() : renderStringView()}
        </div>

        {/* Legend */}
        <div className="flex items-center justify-between pt-4 border-t">
          <div className="flex items-center gap-4 text-sm">
            <div className="flex items-center gap-1">
              <div className="w-3 h-3 bg-green-500 rounded"></div>
              <span>Optimal</span>
            </div>
            <div className="flex items-center gap-1">
              <div className="w-3 h-3 bg-blue-500 rounded"></div>
              <span>Good</span>
            </div>
            <div className="flex items-center gap-1">
              <div className="w-3 h-3 bg-yellow-500 rounded"></div>
              <span>Underperforming</span>
            </div>
            <div className="flex items-center gap-1">
              <div className="w-3 h-3 bg-red-500 rounded"></div>
              <span>Fault</span>
            </div>
          </div>
          <div className="flex items-center gap-1 text-sm text-gray-600">
            <Activity className="h-4 w-4" />
            <span>Live data</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default SolarArrayHeatmap;
