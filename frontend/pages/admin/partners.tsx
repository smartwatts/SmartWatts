import React, { useState, useEffect } from 'react'
import Layout from '../../components/Layout'
import {
  UserPlusIcon,
  QrCodeIcon,
  CheckCircleIcon,
  XCircleIcon,
  ClockIcon,
  BanknotesIcon,
  ChartBarIcon,
  EyeIcon,
  FunnelIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon,
  ShieldCheckIcon,
  BuildingOfficeIcon,
  MapPinIcon,
  PhoneIcon,
  EnvelopeIcon,
} from '@heroicons/react/24/outline'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell, AreaChart, Area } from 'recharts'

interface Partner {
  id: string
  partnerId: string
  partnerName: string
  partnerType: string
  contactPerson: string
  email: string
  phone: string
  city: string
  state: string
  isVerified: boolean
  verificationStatus: string
  totalInstallations: number
  totalCommission: number
  commissionRate: number
  lastActivity: string
  createdAt: string
  performanceScore: number
  customerSatisfaction: number
  responseTime: number
  completionRate: number
  revenueGrowth: number
  marketShare: number
}

interface PartnerStatistics {
  totalPartners: number
  verifiedPartners: number
  pendingPartners: number
  totalCommission: number
  monthlyInstallations: number
  averagePerformanceScore: number
  totalRevenue: number
  marketCoverage: number
  partnerGrowth: number
  customerRetention: number
}

interface PartnerPerformance {
  partnerId: string
  month: string
  installations: number
  revenue: number
  commission: number
  performanceScore: number
}

interface GeographicDistribution {
  state: string
  partnerCount: number
  totalRevenue: number
  marketShare: number
}

export default function AdminPartners() {
  const [partners, setPartners] = useState<Partner[]>([])
  const [statistics, setStatistics] = useState<PartnerStatistics | null>(null)
  const [loading, setLoading] = useState(true)
  const [selectedPartner, setSelectedPartner] = useState<Partner | null>(null)
  const [showQRModal, setShowQRModal] = useState(false)
  const [filterType, setFilterType] = useState<string>('all')
  const [searchTerm, setSearchTerm] = useState('')
  const [partnerPerformance, setPartnerPerformance] = useState<PartnerPerformance[]>([])
  const [geographicDistribution, setGeographicDistribution] = useState<GeographicDistribution[]>([])

  // Mock data for demonstration
  useEffect(() => {
    const mockPartners: Partner[] = [
      {
        id: '1',
        partnerId: 'INST001',
        partnerName: 'SolarTech Solutions',
        partnerType: 'INSTALLER',
        contactPerson: 'John Doe',
        email: 'john@solartech.com',
        phone: '+234 801 234 5678',
        city: 'Lagos',
        state: 'Lagos',
        isVerified: true,
        verificationStatus: 'APPROVED',
        totalInstallations: 45,
        totalCommission: 125000,
        commissionRate: 5.0,
        lastActivity: '2024-01-15T10:30:00Z',
        createdAt: '2023-12-01T09:00:00Z',
        performanceScore: 92,
        customerSatisfaction: 4.8,
        responseTime: 2.5,
        completionRate: 96,
        revenueGrowth: 23,
        marketShare: 18
      },
      {
        id: '2',
        partnerId: 'FIN001',
        partnerName: 'Green Finance Ltd',
        partnerType: 'FINANCE_PROVIDER',
        contactPerson: 'Sarah Johnson',
        email: 'sarah@greenfinance.com',
        phone: '+234 802 345 6789',
        city: 'Abuja',
        state: 'FCT',
        isVerified: true,
        verificationStatus: 'APPROVED',
        totalInstallations: 23,
        totalCommission: 89000,
        commissionRate: 7.5,
        lastActivity: '2024-01-14T15:45:00Z',
        createdAt: '2023-11-15T14:30:00Z',
        performanceScore: 88,
        customerSatisfaction: 4.6,
        responseTime: 4.2,
        completionRate: 91,
        revenueGrowth: 18,
        marketShare: 12
      },
      {
        id: '3',
        partnerId: 'INS001',
        partnerName: 'Secure Energy Insurance',
        partnerType: 'INSURANCE_PROVIDER',
        contactPerson: 'Michael Chen',
        email: 'michael@secureenergy.com',
        phone: '+234 803 456 7890',
        city: 'Port Harcourt',
        state: 'Rivers',
        isVerified: true,
        verificationStatus: 'APPROVED',
        totalInstallations: 67,
        totalCommission: 156000,
        commissionRate: 3.5,
        lastActivity: '2024-01-15T16:20:00Z',
        createdAt: '2023-10-20T11:15:00Z',
        performanceScore: 95,
        customerSatisfaction: 4.9,
        responseTime: 1.8,
        completionRate: 98,
        revenueGrowth: 31,
        marketShare: 25
      },
      {
        id: '4',
        partnerId: 'INST002',
        partnerName: 'EcoPower Installations',
        partnerType: 'INSTALLER',
        contactPerson: 'David Wilson',
        email: 'david@ecopower.com',
        phone: '+234 804 567 8901',
        city: 'Kano',
        state: 'Kano',
        isVerified: false,
        verificationStatus: 'PENDING',
        totalInstallations: 12,
        totalCommission: 34000,
        commissionRate: 5.5,
        lastActivity: '2024-01-13T09:15:00Z',
        createdAt: '2024-01-05T08:30:00Z',
        performanceScore: 76,
        customerSatisfaction: 4.2,
        responseTime: 6.8,
        completionRate: 84,
        revenueGrowth: 8,
        marketShare: 6
      },
      {
        id: '5',
        partnerId: 'TECH001',
        partnerName: 'SmartGrid Technologies',
        partnerType: 'TECHNOLOGY_PROVIDER',
        contactPerson: 'Lisa Rodriguez',
        email: 'lisa@smartgrid.com',
        phone: '+234 805 678 9012',
        city: 'Ibadan',
        state: 'Oyo',
        isVerified: true,
        verificationStatus: 'APPROVED',
        totalInstallations: 89,
        totalCommission: 234000,
        commissionRate: 4.0,
        lastActivity: '2024-01-15T14:10:00Z',
        createdAt: '2023-09-10T10:45:00Z',
        performanceScore: 97,
        customerSatisfaction: 4.9,
        responseTime: 1.2,
        completionRate: 99,
        revenueGrowth: 42,
        marketShare: 32
      }
    ]

    const mockStatistics: PartnerStatistics = {
      totalPartners: 5,
      verifiedPartners: 4,
      pendingPartners: 1,
      totalCommission: 598000,
      monthlyInstallations: 236,
      averagePerformanceScore: 89.6,
      totalRevenue: 2980000,
      marketCoverage: 78,
      partnerGrowth: 25,
      customerRetention: 94
    }

    const mockPartnerPerformance: PartnerPerformance[] = [
      { partnerId: 'INST001', month: 'Jan', installations: 45, revenue: 125000, commission: 6250, performanceScore: 92 },
      { partnerId: 'FIN001', month: 'Jan', installations: 23, revenue: 89000, commission: 6675, performanceScore: 88 },
      { partnerId: 'INS001', month: 'Jan', installations: 67, revenue: 156000, commission: 5460, performanceScore: 95 },
      { partnerId: 'INST002', month: 'Jan', installations: 12, revenue: 34000, commission: 1870, performanceScore: 76 },
      { partnerId: 'TECH001', month: 'Jan', installations: 89, revenue: 234000, commission: 9360, performanceScore: 97 }
    ]

    const mockGeographicDistribution: GeographicDistribution[] = [
      { state: 'Lagos', partnerCount: 2, totalRevenue: 359000, marketShare: 28 },
      { state: 'FCT', partnerCount: 1, totalRevenue: 89000, marketShare: 12 },
      { state: 'Rivers', partnerCount: 1, totalRevenue: 156000, marketShare: 25 },
      { state: 'Kano', partnerCount: 1, totalRevenue: 34000, marketShare: 6 },
      { state: 'Oyo', partnerCount: 1, totalRevenue: 234000, marketShare: 32 }
    ]

    setPartners(mockPartners)
    setStatistics(mockStatistics)
    setPartnerPerformance(mockPartnerPerformance)
    setGeographicDistribution(mockGeographicDistribution)
    setLoading(false)
  }, [])

  const handleVerifyPartner = (partnerId: string) => {
    setPartners(prev => prev.map(partner => 
      partner.id === partnerId 
        ? { ...partner, isVerified: true, verificationStatus: 'APPROVED' }
        : partner
    ))
  }

  const handleRejectPartner = (partnerId: string) => {
    setPartners(prev => prev.map(partner => 
      partner.id === partnerId 
        ? { ...partner, isVerified: false, verificationStatus: 'REJECTED' }
        : partner
    ))
  }

  const getPartnerTypeColor = (type: string) => {
    switch (type) {
      case 'INSTALLER': return 'bg-blue-100 text-blue-800'
      case 'FINANCE_PROVIDER': return 'bg-green-100 text-green-800'
      case 'INSURANCE_PROVIDER': return 'bg-purple-100 text-purple-800'
      case 'TECHNOLOGY_PROVIDER': return 'bg-orange-100 text-orange-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'APPROVED': return 'bg-green-100 text-green-800'
      case 'PENDING': return 'bg-yellow-100 text-yellow-800'
      case 'REJECTED': return 'bg-red-100 text-red-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getPerformanceColor = (score: number) => {
    if (score >= 90) return 'text-green-600 bg-green-100 dark:bg-green-900/20'
    if (score >= 80) return 'text-blue-600 bg-blue-100 dark:bg-blue-900/20'
    if (score >= 70) return 'text-yellow-600 bg-yellow-100 dark:bg-yellow-900/20'
    return 'text-red-600 bg-red-100 dark:bg-red-900/20'
  }

  const filteredPartners = partners.filter(partner => {
    const matchesType = filterType === 'all' || partner.partnerType === filterType
    const matchesSearch = partner.partnerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         partner.contactPerson.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         partner.city.toLowerCase().includes(searchTerm.toLowerCase())
    return matchesType && matchesSearch
  })

  if (loading) {
    return (
      
        <Layout>
          <div className="flex items-center justify-center h-64">
            <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
          </div>
        </Layout>
      
    )
  }

  return (
    
      <Layout>
        <div className="space-y-6 animate-fade-in">
          {/* Header */}
          <div className="bg-gradient-to-r from-gray-800 via-blue-900 to-gray-800 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-3xl font-bold mb-2">Partner Management Dashboard</h1>
                <p className="text-blue-200 text-lg">Comprehensive partner analytics and performance monitoring</p>
                <div className="flex items-center mt-4 space-x-6">
                  <div className="flex items-center space-x-2">
                    <UserPlusIcon className="h-5 w-5 text-green-300" />
                    <span className="text-sm">{statistics?.totalPartners} Total Partners</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <CheckCircleIcon className="h-5 w-5 text-blue-300" />
                    <span className="text-sm">{statistics?.verifiedPartners} Verified</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <BanknotesIcon className="h-5 w-5 text-yellow-300" />
                    <span className="text-sm">₦{statistics?.totalCommission?.toLocaleString()} Commission</span>
                  </div>
                </div>
              </div>
              <div className="hidden md:block">
                <div className="w-24 h-24 bg-white/10 rounded-full flex items-center justify-center">
                  <UserPlusIcon className="h-12 w-12 text-white" />
                </div>
              </div>
            </div>
          </div>

          {/* Key Performance Metrics */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Total Partners</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{statistics?.totalPartners}</p>
                </div>
                <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/20 rounded-xl flex items-center justify-center">
                  <UserPlusIcon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Avg Performance</p>
                  <p className="text-2xl font-bold text-green-600 dark:text-green-400">{statistics?.averagePerformanceScore}%</p>
                </div>
                <div className="w-12 h-12 bg-green-100 dark:bg-green-900/20 rounded-xl flex items-center justify-center">
                  <ChartBarIcon className="h-6 w-6 text-green-600 dark:text-green-400" />
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Total Revenue</p>
                  <p className="text-2xl font-bold text-purple-600 dark:text-purple-400">₦{statistics?.totalRevenue?.toLocaleString()}</p>
                </div>
                <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/20 rounded-xl flex items-center justify-center">
                  <BanknotesIcon className="h-6 w-6 text-purple-600 dark:text-purple-400" />
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Market Coverage</p>
                  <p className="text-2xl font-bold text-orange-600 dark:text-orange-400">{statistics?.marketCoverage}%</p>
                </div>
                <div className="w-12 h-12 bg-orange-100 dark:bg-orange-900/20 rounded-xl flex items-center justify-center">
                  <MapPinIcon className="h-6 w-6 text-orange-600 dark:text-orange-400" />
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Customer Retention</p>
                  <p className="text-2xl font-bold text-indigo-600 dark:text-indigo-400">{statistics?.customerRetention}%</p>
                </div>
                <div className="w-12 h-12 bg-indigo-100 dark:bg-indigo-900/20 rounded-xl flex items-center justify-center">
                  <ArrowTrendingUpIcon className="h-6 w-6 text-indigo-600 dark:text-indigo-400" />
                </div>
              </div>
            </div>
          </div>

          {/* Partner Performance Chart */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Partner Performance Overview</h2>
                <p className="text-gray-600 dark:text-gray-400">Performance scores and revenue distribution across partners</p>
              </div>
            </div>
            
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={partnerPerformance}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                  <XAxis dataKey="partnerId" stroke="#9CA3AF" />
                  <YAxis stroke="#9CA3AF" />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: '#1F2937', 
                      border: '1px solid #374151',
                      borderRadius: '8px',
                      color: '#F9FAFB'
                    }}
                  />
                  <Bar dataKey="performanceScore" fill="#10B981" name="Performance Score (%)" />
                  <Bar dataKey="revenue" fill="#3B82F6" name="Revenue (₦)" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Geographic Distribution */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Geographic Market Distribution</h2>
                <p className="text-gray-600 dark:text-gray-400">Partner coverage and revenue distribution by state</p>
              </div>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
              {geographicDistribution.map((region, index) => (
                <div key={index} className="text-center p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                  <h3 className="font-semibold text-gray-900 dark:text-white mb-2">{region.state}</h3>
                  <div className="space-y-2">
                    <div>
                      <p className="text-sm text-gray-500 dark:text-gray-400">Partners</p>
                      <p className="text-lg font-bold text-blue-600 dark:text-blue-400">{region.partnerCount}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500 dark:text-gray-400">Revenue</p>
                      <p className="text-lg font-bold text-green-600 dark:text-green-400">₦{region.totalRevenue.toLocaleString()}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500 dark:text-gray-400">Market Share</p>
                      <p className="text-lg font-bold text-purple-600 dark:text-purple-400">{region.marketShare}%</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Filters and Search */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
            <div className="flex flex-col md:flex-row gap-4 mb-6">
              <div className="flex-1">
                <input
                  type="text"
                  placeholder="Search partners..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                />
              </div>
              <div className="flex gap-2">
                <select
                  value={filterType}
                  onChange={(e) => setFilterType(e.target.value)}
                  className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                >
                  <option value="all">All Types</option>
                  <option value="INSTALLER">Installers</option>
                  <option value="FINANCE_PROVIDER">Finance Providers</option>
                  <option value="INSURANCE_PROVIDER">Insurance Providers</option>
                  <option value="TECHNOLOGY_PROVIDER">Technology Providers</option>
                </select>
              </div>
            </div>

            {/* Partners Table */}
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-200 dark:border-gray-700">
                    <th className="text-left py-3 px-4 font-semibold text-gray-900 dark:text-white">Partner</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-900 dark:text-white">Type</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-900 dark:text-white">Performance</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-900 dark:text-white">Installations</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-900 dark:text-white">Commission</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-900 dark:text-white">Status</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-900 dark:text-white">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredPartners.map((partner) => (
                    <tr key={partner.id} className="border-b border-gray-100 dark:border-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700">
                      <td className="py-4 px-4">
                        <div>
                          <div className="font-medium text-gray-900 dark:text-white">{partner.partnerName}</div>
                          <div className="text-sm text-gray-500 dark:text-gray-400">{partner.contactPerson}</div>
                          <div className="text-sm text-gray-500 dark:text-gray-400">{partner.email}</div>
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getPartnerTypeColor(partner.partnerType)}`}>
                          {partner.partnerType.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="py-4 px-4">
                        <div className="flex items-center space-x-2">
                          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getPerformanceColor(partner.performanceScore)}`}>
                            {partner.performanceScore}%
                          </span>
                        </div>
                        <div className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                          Satisfaction: {partner.customerSatisfaction}/5.0
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        <div className="text-gray-900 dark:text-white font-medium">{partner.totalInstallations}</div>
                        <div className="text-sm text-gray-500 dark:text-gray-400">
                          {partner.completionRate}% completion
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        <div className="text-gray-900 dark:text-white font-medium">₦{partner.totalCommission.toLocaleString()}</div>
                        <div className="text-sm text-gray-500 dark:text-gray-400">
                          {partner.commissionRate}% rate
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(partner.verificationStatus)}`}>
                          {partner.verificationStatus}
                        </span>
                      </td>
                      <td className="py-4 px-4">
                        <div className="flex space-x-2">
                          <button
                            onClick={() => setSelectedPartner(partner)}
                            className="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
                          >
                            <EyeIcon className="h-5 w-5" />
                          </button>
                          {!partner.isVerified && (
                            <>
                              <button
                                onClick={() => handleVerifyPartner(partner.id)}
                                className="text-green-600 hover:text-green-800 dark:text-green-400 dark:hover:text-green-300"
                              >
                                <CheckCircleIcon className="h-5 w-5" />
                              </button>
                              <button
                                onClick={() => handleRejectPartner(partner.id)}
                                className="text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300"
                              >
                                <XCircleIcon className="h-5 w-5" />
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </Layout>
    
  )
} 