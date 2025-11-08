import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { 
  Trophy, 
  Medal, 
  Award, 
  TrendingUp, 
  Users, 
  Zap,
  Sun,
  Leaf,
  RefreshCw,
  ChevronDown,
  ChevronUp
} from 'lucide-react';

interface LeaderboardEntry {
  rank: number;
  userId: string;
  value: number;
  region: string;
  metric: string;
}

interface RegionalStats {
  average: number;
  median: number;
  percentile25: number;
  percentile75: number;
  percentile90: number;
  sampleSize: number;
}

interface CommunityLeaderboardData {
  region: string;
  metricType: string;
  topPerformers: LeaderboardEntry[];
  regionalStats: RegionalStats;
  generatedAt: string;
}

interface CommunityLeaderboardWidgetProps {
  region: string;
  userId?: string;
}

const CommunityLeaderboardWidget: React.FC<CommunityLeaderboardWidgetProps> = ({ 
  region, 
  userId 
}) => {
  const [leaderboardData, setLeaderboardData] = useState<CommunityLeaderboardData | null>(null);
  const [userRanking, setUserRanking] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [selectedMetric, setSelectedMetric] = useState('ENERGY_EFFICIENCY');
  const [expanded, setExpanded] = useState(false);

  useEffect(() => {
    fetchLeaderboardData();
    if (userId) {
      fetchUserRanking();
    }
  }, [region, selectedMetric, userId]);

  const fetchLeaderboardData = async () => {
    try {
      setLoading(true);
      const response = await fetch(`/api/v1/community/leaderboard/${region}?metricType=${selectedMetric}&limit=10`);
      if (response.ok) {
        const data = await response.json();
        setLeaderboardData(data);
      }
    } catch (error) {
      console.error('Error fetching leaderboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchUserRanking = async () => {
    if (!userId) return;
    
    try {
      const response = await fetch(`/api/v1/community/benchmark/${region}/user/${userId}`);
      if (response.ok) {
        const data = await response.json();
        setUserRanking(data);
      }
    } catch (error) {
      console.error('Error fetching user ranking:', error);
    }
  };

  const getRankIcon = (rank: number) => {
    switch (rank) {
      case 1:
        return <Trophy className="h-5 w-5 text-yellow-500" />;
      case 2:
        return <Medal className="h-5 w-5 text-gray-400" />;
      case 3:
        return <Award className="h-5 w-5 text-amber-600" />;
      default:
        return <span className="text-sm font-bold text-gray-600">#{rank}</span>;
    }
  };

  const getMetricIcon = (metric: string) => {
    switch (metric) {
      case 'ENERGY_EFFICIENCY':
        return <Zap className="h-4 w-4" />;
      case 'SOLAR_UTILIZATION':
        return <Sun className="h-4 w-4" />;
      case 'ENERGY_SAVINGS':
        return <Leaf className="h-4 w-4" />;
      default:
        return <TrendingUp className="h-4 w-4" />;
    }
  };

  const getMetricLabel = (metric: string) => {
    switch (metric) {
      case 'ENERGY_EFFICIENCY':
        return 'Energy Efficiency';
      case 'SOLAR_UTILIZATION':
        return 'Solar Utilization';
      case 'ENERGY_SAVINGS':
        return 'Energy Savings';
      default:
        return metric.replace('_', ' ');
    }
  };

  const formatValue = (value: number, metric: string) => {
    switch (metric) {
      case 'ENERGY_EFFICIENCY':
        return `${value.toFixed(1)}%`;
      case 'SOLAR_UTILIZATION':
        return `${value.toFixed(1)}%`;
      case 'ENERGY_SAVINGS':
        return `${value.toFixed(0)} kWh`;
      default:
        return value.toFixed(1);
    }
  };

  const getRankingColor = (ranking: string) => {
    switch (ranking) {
      case 'TOP_10_PERCENT':
        return 'text-green-600';
      case 'TOP_20_PERCENT':
        return 'text-blue-600';
      case 'ABOVE_AVERAGE':
        return 'text-yellow-600';
      case 'AVERAGE':
        return 'text-gray-600';
      case 'BELOW_AVERAGE':
        return 'text-red-600';
      default:
        return 'text-gray-600';
    }
  };

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Trophy className="h-5 w-5" />
            Community Leaderboard
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center h-32" role="status" aria-live="polite" aria-busy="true">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" aria-hidden="true"></div>
            <span className="sr-only">Loading leaderboard...</span>
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
            <Trophy className="h-5 w-5" />
            Community Leaderboard
          </div>
          <div className="flex items-center gap-2">
            <div className="flex bg-gray-100 rounded-lg p-1">
              <Button
                variant={selectedMetric === 'ENERGY_EFFICIENCY' ? 'default' : 'ghost'}
                size="sm"
                onClick={() => setSelectedMetric('ENERGY_EFFICIENCY')}
              >
                <Zap className="h-3 w-3 mr-1" />
                Efficiency
              </Button>
              <Button
                variant={selectedMetric === 'SOLAR_UTILIZATION' ? 'default' : 'ghost'}
                size="sm"
                onClick={() => setSelectedMetric('SOLAR_UTILIZATION')}
              >
                <Sun className="h-3 w-3 mr-1" />
                Solar
              </Button>
              <Button
                variant={selectedMetric === 'ENERGY_SAVINGS' ? 'default' : 'ghost'}
                size="sm"
                onClick={() => setSelectedMetric('ENERGY_SAVINGS')}
              >
                <Leaf className="h-3 w-3 mr-1" />
                Savings
              </Button>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={fetchLeaderboardData}
            >
              <RefreshCw className="h-4 w-4" />
            </Button>
          </div>
        </CardTitle>
      </CardHeader>
      <CardContent>
        {/* User Ranking */}
        {userRanking && (
          <div className="mb-6 p-4 bg-blue-50 rounded-lg">
            <div className="flex items-center justify-between">
              <div>
                <h4 className="font-medium text-blue-900">Your Ranking</h4>
                <p className={`text-sm ${getRankingColor(userRanking.ranking)}`}>
                  {userRanking.ranking.replace(/_/g, ' ')} in {region}
                </p>
                <p className="text-xs text-blue-700">
                  {userRanking.percentile}th percentile
                </p>
              </div>
              <div className="text-right">
                <div className="text-2xl font-bold text-blue-600">
                  {formatValue(userRanking.userMetrics.efficiencyScore, selectedMetric)}
                </div>
                <div className="text-xs text-blue-700">
                  Your {getMetricLabel(selectedMetric)}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Regional Stats */}
        {leaderboardData && (
          <div className="mb-4 p-3 bg-gray-50 rounded-lg">
            <div className="flex items-center justify-between text-sm">
              <div className="flex items-center gap-2">
                <Users className="h-4 w-4" />
                <span>{leaderboardData.regionalStats.sampleSize} households in {region}</span>
              </div>
              <div className="flex items-center gap-4">
                <div>
                  <span className="text-gray-600">Avg: </span>
                  <span className="font-medium">
                    {formatValue(leaderboardData.regionalStats.average, selectedMetric)}
                  </span>
                </div>
                <div>
                  <span className="text-gray-600">Median: </span>
                  <span className="font-medium">
                    {formatValue(leaderboardData.regionalStats.median, selectedMetric)}
                  </span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Leaderboard */}
        {leaderboardData && (
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <h4 className="font-medium">Top Performers</h4>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setExpanded(!expanded)}
              >
                {expanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
              </Button>
            </div>
            
            <div className="space-y-1">
              {(expanded ? leaderboardData.topPerformers : leaderboardData.topPerformers.slice(0, 5)).map((entry) => (
                <div key={entry.rank} className="flex items-center justify-between p-2 bg-gray-50 rounded-lg">
                  <div className="flex items-center gap-3">
                    {getRankIcon(entry.rank)}
                    <div>
                      <div className="font-medium">
                        {entry.rank <= 3 ? `Top Performer #${entry.rank}` : `User #${entry.userId}`}
                      </div>
                      <div className="text-xs text-gray-600">{region}</div>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="font-bold">
                      {formatValue(entry.value, selectedMetric)}
                    </div>
                    <div className="text-xs text-gray-600">
                      {getMetricLabel(selectedMetric)}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Recommendations */}
        {userRanking && userRanking.recommendations && (
          <div className="mt-4 pt-4 border-t">
            <h5 className="font-medium mb-2">Recommendations</h5>
            <ul className="space-y-1">
              {userRanking.recommendations.map((recommendation: string, index: number) => (
                <li key={index} className="text-sm text-gray-600 flex items-start gap-2">
                  <span className="text-blue-500 mt-1">•</span>
                  <span>{recommendation}</span>
                </li>
              ))}
            </ul>
          </div>
        )}

        <div className="mt-4 pt-4 border-t text-xs text-gray-500 text-center">
          Data updated: {leaderboardData ? new Date(leaderboardData.generatedAt).toLocaleString() : 'Never'}
        </div>
      </CardContent>
    </Card>
  );
};

export default CommunityLeaderboardWidget;
