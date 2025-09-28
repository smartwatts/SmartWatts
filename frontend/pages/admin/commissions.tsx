import { useState, useEffect } from 'react'
import Layout from '../../components/Layout'
import {
  // CurrencyDollarIcon, // Replaced with BanknotesIcon
  ChartBarIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon,
  ClockIcon,
  CheckCircleIcon,
  XCircleIcon,
  ExclamationTriangleIcon,
  DocumentTextIcon,
  CalculatorIcon,
  BanknotesIcon,
  UserGroupIcon,
  CalendarIcon,
  ArrowPathIcon,
  FunnelIcon,
  MagnifyingGlassIcon,
} from '@heroicons/react/24/outline'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Area, AreaChart, BarChart, Bar, PieChart, Pie, Cell } from 'recharts'

interface CommissionTransaction {
  id: string
  partnerName: string
  partnerId: string
  amount: number
  status: 'pending' | 'approved' | 'rejected' | 'paid'
  type: 'installation' | 'maintenance' | 'consultation' | 'referral'
  deviceInfo: string
  date: string
  paymentDate?: string
  notes?: string
  // Enhanced fields for enterprise analytics
  commissionRate: number
  baseAmount: number
  performanceBonus: number
  customerSatisfaction: number
  completionTime: number
  qualityScore: number
}

interface CommissionStatistics {
  totalCommissions: number
  pendingAmount: number
  paidAmount: number
  rejectedAmount: number
  totalPartners: number
  averageCommission: number
  // Enhanced fields for enterprise analytics
  monthlyGrowth: number
  quarterlyGrowth: number
  topPerformingPartners: number
  averageProcessingTime: number
  commissionEfficiency: number
  customerRetentionRate: number
}

// New interfaces for enhanced analytics
interface CommissionAnalytics {
  monthlyTrends: {
    month: string
    totalCommissions: number
    paidAmount: number
    pendingAmount: number
    growth: number
  }[]
  partnerPerformance: {
    partnerId: string
    partnerName: string
    totalCommissions: number
    averageAmount: number
    successRate: number
    customerRating: number
  }[]
  typeDistribution: {
    type: string
    count: number
    totalAmount: number
    percentage: number
  }[]
}

interface PerformanceMetrics {
  processingEfficiency: number
  approvalRate: number
  averagePaymentTime: number
  qualityMetrics: {
    customerSatisfaction: number
    completionTime: number
    errorRate: number
  }
  financialMetrics: {
    roi: number
    costPerTransaction: number
    revenueGrowth: number
  }
}

interface RevenueForecasting {
  nextMonth: number
  nextQuarter: number
  nextYear: number
  confidenceLevel: number
  factors: string[]
}

export default function AdminCommissions() {
  const [transactions, setTransactions] = useState<CommissionTransaction[]>([])
  const [statistics, setStatistics] = useState<CommissionStatistics>({
    totalCommissions: 0,
    pendingAmount: 0,
    paidAmount: 0,
    rejectedAmount: 0,
    totalPartners: 0,
    averageCommission: 0,
    monthlyGrowth: 0,
    quarterlyGrowth: 0,
    topPerformingPartners: 0,
    averageProcessingTime: 0,
    commissionEfficiency: 0,
    customerRetentionRate: 0
  })
  const [loading, setLoading] = useState(true)
  const [filterStatus, setFilterStatus] = useState<string>('all')
  const [filterType, setFilterType] = useState<string>('all')
  const [dateRange, setDateRange] = useState<string>('30d')
  
  // New states for enhanced analytics
  const [commissionAnalytics, setCommissionAnalytics] = useState<CommissionAnalytics>({
    monthlyTrends: [],
    partnerPerformance: [],
    typeDistribution: []
  })
  const [performanceMetrics, setPerformanceMetrics] = useState<PerformanceMetrics>({
    processingEfficiency: 0,
    approvalRate: 0,
    averagePaymentTime: 0,
    qualityMetrics: {
      customerSatisfaction: 0,
      completionTime: 0,
      errorRate: 0
    },
    financialMetrics: {
      roi: 0,
      costPerTransaction: 0,
      revenueGrowth: 0
    }
  })
  const [revenueForecasting, setRevenueForecasting] = useState<RevenueForecasting>({
    nextMonth: 0,
    nextQuarter: 0,
    nextYear: 0,
    confidenceLevel: 0,
    factors: []
  })

  useEffect(() => {
    loadCommissionData()
  }, [])

  const loadCommissionData = async () => {
    try {
      setLoading(true)
      
      // Mock commission transactions with enhanced data
      const mockTransactions: CommissionTransaction[] = [
        {
          id: '1',
          partnerName: 'TechEnergy Solutions',
          partnerId: 'P001',
          amount: 15000,
          status: 'paid',
          type: 'installation',
          deviceInfo: 'Smart Meter + Solar Inverter',
          date: '2024-01-15',
          paymentDate: '2024-01-20',
          commissionRate: 15,
          baseAmount: 100000,
          performanceBonus: 2000,
          customerSatisfaction: 4.8,
          completionTime: 3,
          qualityScore: 95
        },
        {
          id: '2',
          partnerName: 'PowerGrid Pro',
          partnerId: 'P002',
          amount: 12000,
          status: 'pending',
          type: 'maintenance',
          deviceInfo: 'Generator Monitor System',
          date: '2024-01-18',
          commissionRate: 12,
          baseAmount: 80000,
          performanceBonus: 1500,
          customerSatisfaction: 4.6,
          completionTime: 2,
          qualityScore: 88
        },
        {
          id: '3',
          partnerName: 'EcoPower Systems',
          partnerId: 'P003',
          amount: 18000,
          status: 'approved',
          type: 'consultation',
          deviceInfo: 'Energy Audit + Recommendations',
          date: '2024-01-20',
          commissionRate: 18,
          baseAmount: 120000,
          performanceBonus: 3000,
          customerSatisfaction: 4.9,
          completionTime: 5,
          qualityScore: 97
        },
        {
          id: '4',
          partnerName: 'SmartWatts Nigeria',
          partnerId: 'P004',
          amount: 9000,
          status: 'rejected',
          type: 'referral',
          deviceInfo: 'Customer Referral',
          date: '2024-01-22',
          commissionRate: 10,
          baseAmount: 60000,
          performanceBonus: 0,
          customerSatisfaction: 3.2,
          completionTime: 1,
          qualityScore: 65
        },
        {
          id: '5',
          partnerName: 'EnergyTech Plus',
          partnerId: 'P005',
          amount: 22000,
          status: 'paid',
          type: 'installation',
          deviceInfo: 'Complete Solar + Battery System',
          date: '2024-01-25',
          commissionRate: 20,
          baseAmount: 150000,
          performanceBonus: 4000,
          customerSatisfaction: 4.7,
          completionTime: 7,
          qualityScore: 92
        }
      ]

      // Enhanced mock statistics
      const mockStatistics: CommissionStatistics = {
        totalCommissions: 76000,
        pendingAmount: 12000,
        paidAmount: 37000,
        rejectedAmount: 9000,
        totalPartners: 5,
        averageCommission: 15200,
        monthlyGrowth: 23.5,
        quarterlyGrowth: 67.8,
        topPerformingPartners: 3,
        averageProcessingTime: 4.2,
        commissionEfficiency: 87.3,
        customerRetentionRate: 94.2
      }

      // Mock analytics data
      const mockAnalytics: CommissionAnalytics = {
        monthlyTrends: [
          { month: 'Oct', totalCommissions: 45000, paidAmount: 38000, pendingAmount: 7000, growth: 12.5 },
          { month: 'Nov', totalCommissions: 52000, paidAmount: 45000, pendingAmount: 7000, growth: 15.6 },
          { month: 'Dec', totalCommissions: 61000, paidAmount: 52000, pendingAmount: 9000, growth: 17.3 },
          { month: 'Jan', totalCommissions: 76000, paidAmount: 37000, pendingAmount: 12000, growth: 24.6 }
        ],
        partnerPerformance: [
          { partnerId: 'P001', partnerName: 'TechEnergy Solutions', totalCommissions: 15000, averageAmount: 15000, successRate: 95, customerRating: 4.8 },
          { partnerId: 'P002', partnerName: 'PowerGrid Pro', totalCommissions: 12000, averageAmount: 12000, successRate: 88, customerRating: 4.6 },
          { partnerId: 'P003', partnerName: 'EcoPower Systems', totalCommissions: 18000, averageAmount: 18000, successRate: 97, customerRating: 4.9 },
          { partnerId: 'P004', partnerName: 'SmartWatts Nigeria', totalCommissions: 9000, averageAmount: 9000, successRate: 65, customerRating: 3.2 },
          { partnerId: 'P005', partnerName: 'EnergyTech Plus', totalCommissions: 22000, averageAmount: 22000, successRate: 92, customerRating: 4.7 }
        ],
        typeDistribution: [
          { type: 'Installation', count: 2, totalAmount: 37000, percentage: 48.7 },
          { type: 'Maintenance', count: 1, totalAmount: 12000, percentage: 15.8 },
          { type: 'Consultation', count: 1, totalAmount: 18000, percentage: 23.7 },
          { type: 'Referral', count: 1, totalAmount: 9000, percentage: 11.8 }
        ]
      }

      // Mock performance metrics
      const mockPerformance: PerformanceMetrics = {
        processingEfficiency: 87.3,
        approvalRate: 78.9,
        averagePaymentTime: 4.2,
        qualityMetrics: {
          customerSatisfaction: 4.44,
          completionTime: 3.6,
          errorRate: 2.1
        },
        financialMetrics: {
          roi: 156.7,
          costPerTransaction: 1250,
          revenueGrowth: 23.5
        }
      }

      // Mock revenue forecasting
      const mockForecasting: RevenueForecasting = {
        nextMonth: 89000,
        nextQuarter: 285000,
        nextYear: 1150000,
        confidenceLevel: 87.5,
        factors: [
          'Seasonal energy demand increase',
          'New partner onboarding',
          'Expanded service offerings',
          'Market penetration growth'
        ]
      }

      setTransactions(mockTransactions)
      setStatistics(mockStatistics)
      setCommissionAnalytics(mockAnalytics)
      setPerformanceMetrics(mockPerformance)
      setRevenueForecasting(mockForecasting)
    } catch (error) {
      console.error('Error loading commission data:', error)
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'paid': return 'text-green-500 bg-green-100 dark:bg-green-900/20'
      case 'pending': return 'text-yellow-500 bg-yellow-100 dark:bg-yellow-900/20'
      case 'approved': return 'text-blue-500 bg-blue-100 dark:bg-blue-900/20'
      case 'rejected': return 'text-red-500 bg-red-100 dark:bg-red-900/20'
      default: return 'text-gray-500 bg-gray-100 dark:bg-gray-900/20'
    }
  }

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'installation': return 'text-blue-500 bg-blue-100 dark:bg-blue-900/20'
      case 'maintenance': return 'text-green-500 bg-green-100 dark:bg-green-900/20'
      case 'consultation': return 'text-purple-500 bg-purple-100 dark:bg-purple-900/20'
      case 'referral': return 'text-orange-500 bg-orange-100 dark:bg-orange-900/20'
      default: return 'text-gray-500 bg-gray-100 dark:bg-gray-900/20'
    }
  }

  const handlePayment = (transactionId: string) => {
    setTransactions(prev => 
      prev.map(t => 
        t.id === transactionId 
          ? { ...t, status: 'paid', paymentDate: new Date().toISOString().split('T')[0] }
          : t
      )
    )
  }

  const filteredTransactions = transactions.filter(transaction => {
    if (filterStatus !== 'all' && transaction.status !== filterStatus) return false
    if (filterType !== 'all' && transaction.type !== filterType) return false
    return true
  })

  if (loading) {
    return (
      
        <Layout>
          <div className="min-h-screen flex items-center justify-center">
            <div className="relative">
              <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
              <div className="absolute inset-0 flex items-center justify-center">
                <BanknotesIcon className="h-8 w-8 text-blue-600 animate-pulse" />
              </div>
            </div>
          </div>
        </Layout>
      
    )
  }

  return (
    
      <Layout>
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
        {/* Header */}
        <div className="bg-gradient-to-r from-gray-800 via-blue-900 to-gray-800 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden mb-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold mb-2">Commission Management System</h1>
              <p className="text-blue-200 text-lg">Partner commission analytics and performance tracking</p>
              <div className="flex items-center mt-4 space-x-6">
                <div className="flex items-center space-x-2">
                  <ClockIcon className="h-5 w-5 text-blue-300" />
                  <span className="text-sm">Last updated: {new Date().toLocaleTimeString()}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <BanknotesIcon className="h-5 w-5 text-green-300" />
                  <span className="text-sm">Total Commissions: ₦{statistics.totalCommissions.toLocaleString()}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <ArrowTrendingUpIcon className="h-5 w-5 text-yellow-300" />
                  <span className="text-sm">Monthly Growth: {statistics.monthlyGrowth}%</span>
                </div>
                <div className="flex items-center space-x-2">
                  <UserGroupIcon className="h-5 w-5 text-purple-300" />
                  <span className="text-sm">Active Partners: {statistics.totalPartners}</span>
                </div>
              </div>
            </div>
            <div className="text-right">
              <div className="text-4xl font-bold text-blue-400">₦{statistics.totalCommissions.toLocaleString()}</div>
              <div className="text-sm text-blue-200">Total Commission Value</div>
            </div>
          </div>
        </div>

        {/* Key Performance Indicators */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-6 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Commissions</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">₦{statistics.totalCommissions.toLocaleString()}</p>
              </div>
              <div className="p-3 bg-blue-100 dark:bg-blue-900/20 rounded-full">
                <BanknotesIcon className="h-6 w-6 text-blue-600" />
              </div>
            </div>
            <div className="mt-4 flex items-center text-sm">
              <ArrowTrendingUpIcon className="h-4 w-4 text-green-500 mr-1" />
              <span className="text-green-600 dark:text-green-400">+{statistics.monthlyGrowth}%</span>
              <span className="text-gray-500 dark:text-gray-400 ml-1">from last month</span>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-6 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Pending Amount</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">₦{statistics.pendingAmount.toLocaleString()}</p>
              </div>
              <div className="p-3 bg-yellow-100 dark:bg-yellow-900/20 rounded-full">
                <ClockIcon className="h-6 w-6 text-yellow-600" />
              </div>
            </div>
            <div className="mt-4 flex items-center text-sm">
              <span className="text-gray-500 dark:text-gray-400">{((statistics.pendingAmount / statistics.totalCommissions) * 100).toFixed(1)}% of total</span>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-6 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Processing Efficiency</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{performanceMetrics.processingEfficiency}%</p>
              </div>
              <div className="p-3 bg-green-100 dark:bg-green-900/20 rounded-full">
                <ChartBarIcon className="h-6 w-6 text-green-600" />
              </div>
            </div>
            <div className="mt-4 flex items-center text-sm">
              <span className="text-gray-500 dark:text-gray-400">Target: 90%</span>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-6 border border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Customer Retention</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{statistics.customerRetentionRate}%</p>
              </div>
              <div className="p-3 bg-purple-100 dark:bg-purple-900/20 rounded-full">
                <UserGroupIcon className="h-6 w-6 text-purple-600" />
              </div>
            </div>
            <div className="mt-4 flex items-center text-sm">
              <span className="text-gray-500 dark:text-gray-400">Industry avg: 85%</span>
            </div>
          </div>
        </div>

        {/* Commission Analytics Overview */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 mb-8">
          <div className="p-6 border-b border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white">Commission Analytics Overview</h3>
                <p className="text-gray-600 dark:text-gray-400 mt-1">Performance trends and partner analytics</p>
              </div>
              <div className="flex items-center space-x-2">
                <ChartBarIcon className="h-5 w-5 text-blue-600" />
                <span className="text-sm font-medium text-blue-600">Real-time Analytics</span>
              </div>
            </div>
          </div>
          <div className="p-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Monthly Trends Chart */}
              <div>
                <h4 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Monthly Commission Trends</h4>
                <ResponsiveContainer width="100%" height={300}>
                  <AreaChart data={commissionAnalytics.monthlyTrends}>
                    <defs>
                      <linearGradient id="totalGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.8}/>
                        <stop offset="95%" stopColor="#3B82F6" stopOpacity={0.1}/>
                      </linearGradient>
                      <linearGradient id="paidGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#10B981" stopOpacity={0.8}/>
                        <stop offset="95%" stopColor="#10B981" stopOpacity={0.1}/>
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" />
                    <XAxis dataKey="month" stroke="#6B7280" />
                    <YAxis stroke="#6B7280" />
                    <Tooltip 
                      contentStyle={{ 
                        backgroundColor: '#1F2937', 
                        border: 'none', 
                        borderRadius: '8px',
                        color: '#F9FAFB'
                      }}
                    />
                    <Area 
                      type="monotone" 
                      dataKey="totalCommissions" 
                      stackId="1"
                      stroke="#3B82F6" 
                      fill="url(#totalGradient)" 
                      name="Total Commissions"
                    />
                    <Area 
                      type="monotone" 
                      dataKey="paidAmount" 
                      stackId="2"
                      stroke="#10B981" 
                      fill="url(#paidGradient)" 
                      name="Paid Amount"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>

              {/* Commission Type Distribution */}
              <div>
                <h4 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Commission Type Distribution</h4>
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={commissionAnalytics.typeDistribution}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={({ type, percentage }) => `${type}: ${percentage}%`}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="count"
                    >
                      {commissionAnalytics.typeDistribution.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={['#3B82F6', '#10B981', '#8B5CF6', '#F59E0B'][index % 4]} />
                      ))}
                    </Pie>
                    <Tooltip 
                      contentStyle={{ 
                        backgroundColor: '#1F2937', 
                        border: 'none', 
                        borderRadius: '8px',
                        color: '#F9FAFB'
                      }}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>
        </div>

        {/* Performance Metrics Dashboard */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 mb-8">
          <div className="p-6 border-b border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white">Performance Metrics Dashboard</h3>
                <p className="text-gray-600 dark:text-gray-400 mt-1">Quality metrics and financial performance indicators</p>
              </div>
              <div className="flex items-center space-x-2">
                <ArrowTrendingUpIcon className="h-5 w-5 text-green-600" />
                <span className="text-sm font-medium text-green-600">Performance Tracking</span>
              </div>
            </div>
          </div>
          <div className="p-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {/* Quality Metrics */}
              <div className="space-y-4">
                <h4 className="text-lg font-semibold text-gray-900 dark:text-white">Quality Metrics</h4>
                <div className="space-y-3">
                  <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Customer Satisfaction</span>
                    <span className="text-lg font-semibold text-gray-900 dark:text-white">{performanceMetrics.qualityMetrics.customerSatisfaction}/5.0</span>
                  </div>
                  <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Avg Completion Time</span>
                    <span className="text-lg font-semibold text-gray-900 dark:text-white">{performanceMetrics.qualityMetrics.completionTime} days</span>
                  </div>
                  <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Error Rate</span>
                    <span className="text-lg font-semibold text-gray-900 dark:text-white">{performanceMetrics.qualityMetrics.errorRate}%</span>
                  </div>
                </div>
              </div>

              {/* Financial Metrics */}
              <div className="space-y-4">
                <h4 className="text-lg font-semibold text-gray-900 dark:text-white">Financial Metrics</h4>
                <div className="space-y-3">
                  <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                    <span className="text-sm text-gray-600 dark:text-gray-400">ROI</span>
                    <span className="text-lg font-semibold text-green-600">{performanceMetrics.financialMetrics.roi}%</span>
                  </div>
                  <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Cost per Transaction</span>
                    <span className="text-lg font-semibold text-gray-900 dark:text-white">₦{performanceMetrics.financialMetrics.costPerTransaction}</span>
                  </div>
                  <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Revenue Growth</span>
                    <span className="text-lg font-semibold text-blue-600">{performanceMetrics.financialMetrics.revenueGrowth}%</span>
                  </div>
                </div>
              </div>

              {/* Processing Metrics */}
              <div className="space-y-4">
                <h4 className="text-lg font-semibold text-gray-900 dark:text-white">Processing Metrics</h4>
                <div className="space-y-3">
                  <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Processing Efficiency</span>
                    <span className="text-lg font-semibold text-green-600">{performanceMetrics.processingEfficiency}%</span>
                  </div>
                  <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Approval Rate</span>
                    <span className="text-lg font-semibold text-blue-600">{performanceMetrics.approvalRate}%</span>
                  </div>
                  <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Avg Payment Time</span>
                    <span className="text-lg font-semibold text-gray-900 dark:text-white">{performanceMetrics.averagePaymentTime} days</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Revenue Forecasting */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 mb-8">
          <div className="p-6 border-b border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white">Revenue Forecasting & Projections</h3>
                <p className="text-gray-600 dark:text-gray-400 mt-1">AI-powered revenue predictions and growth analysis</p>
              </div>
              <div className="flex items-center space-x-2">
                <CalculatorIcon className="h-5 w-5 text-purple-600" />
                <span className="text-sm font-medium text-purple-600">ML Forecasting</span>
              </div>
            </div>
          </div>
          <div className="p-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Forecast Chart */}
              <div>
                <h4 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Revenue Projections</h4>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={[
                    { period: 'Current', amount: statistics.totalCommissions },
                    { period: 'Next Month', amount: revenueForecasting.nextMonth },
                    { period: 'Next Quarter', amount: revenueForecasting.nextQuarter },
                    { period: 'Next Year', amount: revenueForecasting.nextYear }
                  ]}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" />
                    <XAxis dataKey="period" stroke="#6B7280" />
                    <YAxis stroke="#6B7280" />
                    <Tooltip 
                      contentStyle={{ 
                        backgroundColor: '#1F2937', 
                        border: 'none', 
                        borderRadius: '8px',
                        color: '#F9FAFB'
                      }}
                      formatter={(value) => [`₦${Number(value).toLocaleString()}`, 'Projected Revenue']}
                    />
                    <Bar dataKey="amount" fill="#8B5CF6" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>

              {/* Forecast Details */}
              <div>
                <h4 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Forecast Details</h4>
                <div className="space-y-4">
                  <div className="p-4 bg-purple-50 dark:bg-purple-900/20 rounded-lg border border-purple-200 dark:border-purple-800">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm font-medium text-purple-700 dark:text-purple-300">Confidence Level</span>
                      <span className="text-lg font-bold text-purple-700 dark:text-purple-300">{revenueForecasting.confidenceLevel}%</span>
                    </div>
                    <div className="w-full bg-purple-200 dark:bg-purple-700 rounded-full h-2">
                      <div 
                        className="bg-purple-600 h-2 rounded-full" 
                        style={{ width: `${revenueForecasting.confidenceLevel}%` }}
                      ></div>
                    </div>
                  </div>
                  
                  <div className="space-y-3">
                    <h5 className="font-medium text-gray-900 dark:text-white">Growth Factors</h5>
                    {revenueForecasting.factors.map((factor, index) => (
                      <div key={index} className="flex items-start space-x-3 p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                        <div className="w-2 h-2 bg-purple-400 rounded-full mt-2 flex-shrink-0"></div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">{factor}</p>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Commission Transactions */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700">
          <div className="p-6 border-b border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white">Commission Transactions</h3>
                <p className="text-gray-600 dark:text-gray-400 mt-1">Detailed view of all commission transactions and status</p>
              </div>
              <div className="flex items-center space-x-4">
                {/* Filters */}
                <div className="flex items-center space-x-2">
                  <FunnelIcon className="h-4 w-4 text-gray-500" />
                  <select
                    value={filterStatus}
                    onChange={(e) => setFilterStatus(e.target.value)}
                    className="text-sm border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                  >
                    <option value="all">All Status</option>
                    <option value="pending">Pending</option>
                    <option value="approved">Approved</option>
                    <option value="paid">Paid</option>
                    <option value="rejected">Rejected</option>
                  </select>
                </div>
                <div className="flex items-center space-x-2">
                  <FunnelIcon className="h-4 w-4 text-gray-500" />
                  <select
                    value={filterType}
                    onChange={(e) => setFilterType(e.target.value)}
                    className="text-sm border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                  >
                    <option value="all">All Types</option>
                    <option value="installation">Installation</option>
                    <option value="maintenance">Maintenance</option>
                    <option value="consultation">Consultation</option>
                    <option value="referral">Referral</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
          <div className="p-6">
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead className="bg-gray-50 dark:bg-gray-700">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Partner</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Type</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Amount</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Date</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Performance</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                  </tr>
                </thead>
                <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                  {filteredTransactions.map((transaction) => (
                    <tr key={transaction.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div>
                          <div className="text-sm font-medium text-gray-900 dark:text-white">{transaction.partnerName}</div>
                          <div className="text-sm text-gray-500 dark:text-gray-400">ID: {transaction.partnerId}</div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getTypeColor(transaction.type)}`}>
                          {transaction.type.charAt(0).toUpperCase() + transaction.type.slice(1)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900 dark:text-white">₦{transaction.amount.toLocaleString()}</div>
                        <div className="text-xs text-gray-500 dark:text-gray-400">{transaction.commissionRate}% rate</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(transaction.status)}`}>
                          {transaction.status.charAt(0).toUpperCase() + transaction.status.slice(1)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                        {new Date(transaction.date).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="space-y-1">
                          <div className="flex items-center space-x-2">
                            <span className="text-xs text-gray-500 dark:text-gray-400">Quality:</span>
                            <span className="text-xs font-medium text-gray-900 dark:text-white">{transaction.qualityScore}%</span>
                          </div>
                          <div className="flex items-center space-x-2">
                            <span className="text-xs text-gray-500 dark:text-gray-400">Satisfaction:</span>
                            <span className="text-xs font-medium text-gray-900 dark:text-white">{transaction.customerSatisfaction}/5.0</span>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        {transaction.status === 'approved' && (
                          <button
                            onClick={() => handlePayment(transaction.id)}
                            className="text-green-600 hover:text-green-900 dark:text-green-400 dark:hover:text-green-300 mr-3"
                          >
                            Mark Paid
                          </button>
                        )}
                        <button className="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300">
                          View Details
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
        </Layout>
      
  )
} 