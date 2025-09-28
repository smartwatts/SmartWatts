import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { 
  ChevronRight, 
  ChevronDown, 
  Zap, 
  Home, 
  PanelLeft,
  CircuitBoard,
  AlertTriangle,
  CheckCircle,
  Activity
} from 'lucide-react';

interface CircuitNode {
  id: string;
  name: string;
  type: 'DEVICE' | 'SUB_PANEL' | 'CIRCUIT';
  currentLoad: number;
  maxCapacity?: number;
  status?: string;
  children?: CircuitNode[];
}

interface CircuitTreeViewProps {
  deviceId: string;
}

const CircuitTreeView: React.FC<CircuitTreeViewProps> = ({ deviceId }) => {
  const [circuitTree, setCircuitTree] = useState<CircuitNode[]>([]);
  const [expandedNodes, setExpandedNodes] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchCircuitTree();
  }, [deviceId]);

  const fetchCircuitTree = async () => {
    try {
      setLoading(true);
      const response = await fetch(`/api/v1/circuits/devices/${deviceId}/tree`);
      if (response.ok) {
        const data = await response.json();
        setCircuitTree(data);
        // Auto-expand the main device node
        if (data.length > 0) {
          setExpandedNodes(new Set([data[0].id]));
        }
      }
    } catch (error) {
      console.error('Error fetching circuit tree:', error);
    } finally {
      setLoading(false);
    }
  };

  const toggleNode = (nodeId: string) => {
    const newExpanded = new Set(expandedNodes);
    if (newExpanded.has(nodeId)) {
      newExpanded.delete(nodeId);
    } else {
      newExpanded.add(nodeId);
    }
    setExpandedNodes(newExpanded);
  };

  const getNodeIcon = (type: string) => {
    switch (type) {
      case 'DEVICE':
        return <Home className="h-4 w-4" />;
      case 'SUB_PANEL':
        return <PanelLeft className="h-4 w-4" />;
      case 'CIRCUIT':
        return <CircuitBoard className="h-4 w-4" />;
      default:
        return <Zap className="h-4 w-4" />;
    }
  };

  const getStatusColor = (status?: string) => {
    switch (status) {
      case 'NORMAL':
        return 'bg-green-100 text-green-800';
      case 'HIGH':
        return 'bg-yellow-100 text-yellow-800';
      case 'OVERLOAD':
        return 'bg-red-100 text-red-800';
      case 'FAULT':
        return 'bg-red-100 text-red-800';
      case 'OFFLINE':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-blue-100 text-blue-800';
    }
  };

  const getLoadColor = (load: number, maxCapacity?: number) => {
    if (!maxCapacity) return 'text-gray-600';
    
    const percentage = (load / maxCapacity) * 100;
    if (percentage >= 90) return 'text-red-600';
    if (percentage >= 75) return 'text-yellow-600';
    if (percentage >= 50) return 'text-blue-600';
    return 'text-green-600';
  };

  const formatPower = (power: number) => {
    if (power >= 1000) {
      return `${(power / 1000).toFixed(1)}kW`;
    }
    return `${power.toFixed(0)}W`;
  };

  const renderNode = (node: CircuitNode, level: number = 0) => {
    const isExpanded = expandedNodes.has(node.id);
    const hasChildren = node.children && node.children.length > 0;
    const loadPercentage = node.maxCapacity ? (node.currentLoad / node.maxCapacity) * 100 : 0;

    return (
      <div key={node.id} className="select-none">
        <div
          className={`flex items-center gap-2 p-2 rounded-lg hover:bg-gray-50 cursor-pointer ${
            level > 0 ? 'ml-4' : ''
          }`}
          style={{ paddingLeft: `${level * 16 + 8}px` }}
          onClick={() => hasChildren && toggleNode(node.id)}
        >
          {hasChildren && (
            <div className="flex-shrink-0">
              {isExpanded ? (
                <ChevronDown className="h-4 w-4 text-gray-500" />
              ) : (
                <ChevronRight className="h-4 w-4 text-gray-500" />
              )}
            </div>
          )}
          
          <div className="flex-shrink-0">
            {getNodeIcon(node.type)}
          </div>
          
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2">
              <span className="font-medium truncate">{node.name}</span>
              {node.status && (
                <Badge className={getStatusColor(node.status)} size="sm">
                  {node.status}
                </Badge>
              )}
            </div>
            
            <div className="flex items-center gap-2 text-sm text-gray-600">
              <span className={getLoadColor(node.currentLoad, node.maxCapacity)}>
                {formatPower(node.currentLoad)}
              </span>
              {node.maxCapacity && (
                <>
                  <span>/</span>
                  <span>{formatPower(node.maxCapacity)}</span>
                  <span>({loadPercentage.toFixed(0)}%)</span>
                </>
              )}
            </div>
          </div>
          
          {node.maxCapacity && (
            <div className="flex-shrink-0 w-20">
              <Progress 
                value={loadPercentage} 
                className="h-2"
                style={{
                  backgroundColor: loadPercentage >= 90 ? '#ef4444' : 
                                 loadPercentage >= 75 ? '#f59e0b' : 
                                 loadPercentage >= 50 ? '#3b82f6' : '#10b981'
                }}
              />
            </div>
          )}
        </div>
        
        {isExpanded && hasChildren && (
          <div className="ml-4">
            {node.children!.map((child) => renderNode(child, level + 1))}
          </div>
        )}
      </div>
    );
  };

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <CircuitBoard className="h-5 w-5" />
            Circuit Tree View
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
            <CircuitBoard className="h-5 w-5" />
            Circuit Tree View
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={fetchCircuitTree}
          >
            <Activity className="h-4 w-4 mr-1" />
            Refresh
          </Button>
        </CardTitle>
      </CardHeader>
      <CardContent>
        {circuitTree.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            <CircuitBoard className="h-12 w-12 mx-auto mb-2 opacity-50" />
            <p>No circuits found</p>
            <p className="text-sm">Configure your circuit layout to see the tree view</p>
          </div>
        ) : (
          <div className="space-y-1">
            {circuitTree.map((node) => renderNode(node))}
          </div>
        )}
        
        <div className="mt-4 pt-4 border-t">
          <div className="flex items-center justify-between text-sm text-gray-600">
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-1">
                <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                <span>Normal</span>
              </div>
              <div className="flex items-center gap-1">
                <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
                <span>High</span>
              </div>
              <div className="flex items-center gap-1">
                <div className="w-3 h-3 bg-red-500 rounded-full"></div>
                <span>Overload</span>
              </div>
            </div>
            <div className="flex items-center gap-1">
              <CheckCircle className="h-4 w-4" />
              <span>All systems operational</span>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default CircuitTreeView;
