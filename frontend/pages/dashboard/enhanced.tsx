import React, { useState, useEffect } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { useFeatureFlags } from '@/hooks/useFeatureFlags';
import ApplianceRecognitionWidget from '@/components/dashboard/ApplianceRecognitionWidget';
import CircuitTreeView from '@/components/dashboard/CircuitTreeView';
import SolarArrayHeatmap from '@/components/dashboard/SolarArrayHeatmap';
import CommunityLeaderboardWidget from '@/components/dashboard/CommunityLeaderboardWidget';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
  Cog6ToothIcon as Settings, 
  EyeIcon as Eye, 
  EyeSlashIcon as EyeOff, 
  BoltIcon as Zap, 
  SunIcon as Sun, 
  CpuChipIcon as CircuitBoard, 
  TrophyIcon as Trophy,
  HomeIcon as WashingMachine,
  ArrowTrendingUpIcon as TrendingUp,
  ChartBarIcon as Activity
} from '@heroicons/react/24/outline';

interface DashboardData {
  totalConsumption: number;
  solarGeneration: number;
  efficiency: number;
  savings: number;
  deviceCount: number;
  applianceCount: number;
  circuitCount: number;
  lastUpdated: string;
}

const EnhancedDashboard: React.FC = () => {
  const { user } = useAuth();
  const { isFeatureEnabled } = useFeatureFlags();
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [proMode, setProMode] = useState(false);
  const [loading, setLoading] = useState(true);
  const [selectedDevice, setSelectedDevice] = useState<string>('');

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      // This would fetch from the enhanced dashboard API
      const mockData: DashboardData = {
        totalConsumption: 1250,
        solarGeneration: 800,
        efficiency: 85,
        savings: 150,
        deviceCount: 3,
        applianceCount: 12,
        circuitCount: 8,
        lastUpdated: new Date().toISOString()
      };
      setDashboardData(mockData);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatPower = (power: number) => {
    if (power >= 1000) {
      return `${(power / 1000).toFixed(1)}kW`;
    }
    return `${power.toFixed(0)}W`;
  };

  const formatCurrency = (amount: number) => {
    return `₦${amount.toFixed(0)}`;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Enhanced Dashboard</h1>
              <p className="text-sm text-gray-600">Advanced energy monitoring and analytics</p>
            </div>
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2">
                <span className="text-sm text-gray-600">Pro Mode</span>
                <Button
                  variant={proMode ? "default" : "outline"}
                  size="sm"
                  onClick={() => setProMode(!proMode)}
                >
                  {proMode ? <Eye className="h-4 w-4" /> : <EyeOff className="h-4 w-4" />}
                </Button>
              </div>
              <Button variant="outline" size="sm">
                <Settings className="h-4 w-4 mr-2" />
                Settings
              </Button>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Total Consumption</p>
                  <p className="text-2xl font-bold text-gray-900">
                    {dashboardData ? formatPower(dashboardData.totalConsumption) : '0W'}
                  </p>
                </div>
                <div className="p-3 bg-blue-100 rounded-full">
                  <Zap className="h-6 w-6 text-blue-600" />
                </div>
              </div>
              <div className="mt-4 flex items-center">
                <TrendingUp className="h-4 w-4 text-green-500 mr-1" />
                <span className="text-sm text-green-600">+5.2% from last month</span>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Solar Generation</p>
                  <p className="text-2xl font-bold text-gray-900">
                    {dashboardData ? formatPower(dashboardData.solarGeneration) : '0W'}
                  </p>
                </div>
                <div className="p-3 bg-yellow-100 rounded-full">
                  <Sun className="h-6 w-6 text-yellow-600" />
                </div>
              </div>
              <div className="mt-4 flex items-center">
                <TrendingUp className="h-4 w-4 text-green-500 mr-1" />
                <span className="text-sm text-green-600">+12.8% from last month</span>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Efficiency</p>
                  <p className="text-2xl font-bold text-gray-900">
                    {dashboardData ? `${dashboardData.efficiency}%` : '0%'}
                  </p>
                </div>
                <div className="p-3 bg-green-100 rounded-full">
                  <Activity className="h-6 w-6 text-green-600" />
                </div>
              </div>
              <div className="mt-4 flex items-center">
                <TrendingUp className="h-4 w-4 text-green-500 mr-1" />
                <span className="text-sm text-green-600">+2.1% from last month</span>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Monthly Savings</p>
                  <p className="text-2xl font-bold text-gray-900">
                    {dashboardData ? formatCurrency(dashboardData.savings) : '₦0'}
                  </p>
                </div>
                <div className="p-3 bg-purple-100 rounded-full">
                  <Trophy className="h-6 w-6 text-purple-600" />
                </div>
              </div>
              <div className="mt-4 flex items-center">
                <TrendingUp className="h-4 w-4 text-green-500 mr-1" />
                <span className="text-sm text-green-600">+8.5% from last month</span>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Feature Flags */}
        {proMode && (
          <div className="mb-6 p-4 bg-blue-50 rounded-lg">
            <div className="flex items-center gap-2 mb-2">
              <Badge variant="outline" className="bg-blue-100 text-blue-800">
                Pro Mode
              </Badge>
              <span className="text-sm text-blue-700">Advanced features enabled</span>
            </div>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
              <div className="flex items-center gap-2">
                <WashingMachine className="h-4 w-4" />
                <span>AI Appliance Recognition</span>
              </div>
              <div className="flex items-center gap-2">
                <CircuitBoard className="h-4 w-4" />
                <span>Circuit-Level Monitoring</span>
              </div>
              <div className="flex items-center gap-2">
                <Sun className="h-4 w-4" />
                <span>Solar Panel Monitoring</span>
              </div>
              <div className="flex items-center gap-2">
                <Trophy className="h-4 w-4" />
                <span>Community Benchmarking</span>
              </div>
            </div>
          </div>
        )}

        {/* Main Content Tabs */}
        <Tabs defaultValue="overview" className="space-y-6">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="appliances">Appliances</TabsTrigger>
            <TabsTrigger value="circuits">Circuits</TabsTrigger>
            <TabsTrigger value="solar">Solar</TabsTrigger>
          </TabsList>

          <TabsContent value="overview" className="space-y-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Appliance Recognition */}
              {isFeatureEnabled('AI_APPLIANCE_RECOGNITION') && (
                <ApplianceRecognitionWidget deviceId={selectedDevice} />
              )}

              {/* Community Leaderboard */}
              {isFeatureEnabled('COMMUNITY_BENCHMARKING') && (
                <CommunityLeaderboardWidget 
                  region={user?.location || 'Lagos'} 
                  userId={user?.id}
                />
              )}
            </div>
          </TabsContent>

          <TabsContent value="appliances" className="space-y-6">
            <div className="grid grid-cols-1 gap-6">
              {isFeatureEnabled('AI_APPLIANCE_RECOGNITION') && (
                <ApplianceRecognitionWidget deviceId={selectedDevice} />
              )}
            </div>
          </TabsContent>

          <TabsContent value="circuits" className="space-y-6">
            <div className="grid grid-cols-1 gap-6">
              {isFeatureEnabled('CIRCUIT_LEVEL_MONITORING') && (
                <CircuitTreeView deviceId={selectedDevice} />
              )}
            </div>
          </TabsContent>

          <TabsContent value="solar" className="space-y-6">
            <div className="grid grid-cols-1 gap-6">
              {isFeatureEnabled('SOLAR_PANEL_MONITORING') && (
                <SolarArrayHeatmap inverterId="solar-inverter-1" />
              )}
            </div>
          </TabsContent>
        </Tabs>

        {/* Pro Mode Raw Data */}
        {proMode && (
          <div className="mt-8">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Settings className="h-5 w-5" />
                  Raw Data (Pro Mode)
                </CardTitle>
              </CardHeader>
              <CardContent>
                <pre className="bg-gray-100 p-4 rounded-lg text-sm overflow-auto">
                  {JSON.stringify(dashboardData, null, 2)}
                </pre>
              </CardContent>
            </Card>
          </div>
        )}
      </div>
    </div>
  );
};

export default EnhancedDashboard;
