import React, { useState } from 'react'
import { useRouter } from 'next/router'
import Layout from '../../components/Layout'
import {
  ChartBarIcon,
  UsersIcon,
  BanknotesIcon,
  ArrowTrendingUpIcon,
  DeviceTabletIcon,
  GlobeAltIcon,
  ClockIcon,
  ExclamationTriangleIcon,
} from '@heroicons/react/24/outline'

interface BusinessMetrics {
  totalRevenue: number
  monthlyRecurringRevenue: number
  annualRecurringRevenue: number
  customerCount: number
  deviceCount: number
  dataProcessed: number
  growthRate: number
  churnRate: number
}

interface TimeSeriesData {
  month: string
  revenue: number
  users: number
  devices: number
}

export default function BusinessAnalytics() {
  return (
    <Layout>
      <BusinessAnalyticsContent />
    </Layout>
  )
}

function BusinessAnalyticsContent() {
  const router = useRouter()
  const [selectedPeriod, setSelectedPeriod] = useState<'7d' | '30d' | '90d' | '1y'>('30d')
  
  const [metrics, setMetrics] = useState<BusinessMetrics>({
    totalRevenue: 2847500,
    monthlyRecurringRevenue: 187500,
    annualRecurringRevenue: 2250000,
    customerCount: 15420,
    deviceCount: 45678,
    dataProcessed: 2.4,
    growthRate: 23.4,
    churnRate: 2.3
  })

  const [timeSeriesData] = useState<TimeSeriesData[]>([
    { month: 'Jan', revenue: 145000, users: 12000, devices: 42000 },
    { month: 'Feb', revenue: 158000, users: 12500, devices: 43000 },
    { month: 'Mar', revenue: 162000, users: 13000, devices: 44000 },
    { month: 'Apr', revenue: 175000, users: 13500, devices: 45000 },
    { month: 'May', revenue: 182000, users: 14000, devices: 46000 },
    { month: 'Jun', revenue: 187500, users: 15420, devices: 45678 },
  ])

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-NG', {
      style: 'currency',
      currency: 'NGN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount)
  }

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('en-US').format(num)
  }

  // Filter time series data based on selected period
  const getFilteredTimeSeriesData = () => {
    const allData = timeSeriesData
    switch (selectedPeriod) {
      case '7d':
        return allData.slice(-1) // Last 1 month (representing 7 days)
      case '30d':
        return allData.slice(-1) // Last 1 month
      case '90d':
        return allData.slice(-3) // Last 3 months
      case '1y':
        return allData // All 6 months (representing 1 year)
      default:
        return allData
    }
  }

  const filteredTimeSeriesData = getFilteredTimeSeriesData()

  // Calculate dynamic metrics based on selected period
  const getDynamicMetrics = () => {
    const currentData = filteredTimeSeriesData[filteredTimeSeriesData.length - 1]
    const previousData = filteredTimeSeriesData[filteredTimeSeriesData.length - 2]
    
    // Calculate Monthly and Annual Recurring Revenue based on selected period
    let monthlyRecurringRevenue = 0
    let annualRecurringRevenue = 0
    
    switch (selectedPeriod) {
      case '7d':
        // For 7 days, use most recent month (highest performance)
        monthlyRecurringRevenue = currentData ? currentData.revenue : 187500
        annualRecurringRevenue = monthlyRecurringRevenue * 12
        break
      case '30d':
        // For 30 days, use most recent month (highest performance)
        monthlyRecurringRevenue = currentData ? currentData.revenue : 187500
        annualRecurringRevenue = monthlyRecurringRevenue * 12
        break
      case '90d':
        // For 90 days, use average of last 3 months (good performance)
        const avgRevenue = filteredTimeSeriesData.reduce((sum, data) => sum + data.revenue, 0) / filteredTimeSeriesData.length
        monthlyRecurringRevenue = Math.round(avgRevenue)
        annualRecurringRevenue = monthlyRecurringRevenue * 12
        break
      case '1y':
        // For 1 year, use average of all months (conservative estimate)
        const yearlyAvgRevenue = filteredTimeSeriesData.reduce((sum, data) => sum + data.revenue, 0) / filteredTimeSeriesData.length
        monthlyRecurringRevenue = Math.round(yearlyAvgRevenue)
        annualRecurringRevenue = monthlyRecurringRevenue * 12
        break
      default:
        monthlyRecurringRevenue = 187500
        annualRecurringRevenue = 2250000
    }

    return {
      ...metrics,
      monthlyRecurringRevenue: Math.round(monthlyRecurringRevenue),
      annualRecurringRevenue: Math.round(annualRecurringRevenue)
    }
  }

  const dynamicMetrics = getDynamicMetrics()

  // Quick Actions handlers
  const handleExportReport = () => {
    // Generate and download analytics report
    const reportData = {
      period: selectedPeriod,
      metrics: dynamicMetrics,
      timeSeriesData: filteredTimeSeriesData,
      generatedAt: new Date().toISOString()
    }
    
    const dataStr = JSON.stringify(reportData, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(dataBlob)
    const link = document.createElement('a')
    link.href = url
    link.download = `smartwatts-analytics-${selectedPeriod}-${new Date().toISOString().split('T')[0]}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }

  const handleGrowthAnalysis = () => {
    // Navigate to a detailed growth analysis view or show modal
    alert('Growth Analysis: Detailed analysis would show trends, growth rates, and projections based on current data.')
  }

  const handleRiskAssessment = () => {
    // Navigate to risk assessment or show modal
    alert('Risk Assessment: Analysis would identify potential risks in revenue, customer churn, and system performance.')
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          Business Analytics
        </h1>
        <p className="mt-2 text-gray-600 dark:text-gray-300">
          System-wide business performance and growth metrics
        </p>
      </div>

      {/* Period Selector */}
      <div className="mb-8">
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Time Period</h3>
            <div className="flex space-x-2">
              {(['7d', '30d', '90d', '1y'] as const).map((period) => (
                <button
                  key={period}
                  onClick={() => setSelectedPeriod(period)}
                  className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                    selectedPeriod === period
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 text-gray-700 dark:bg-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                  }`}
                >
                  {period === '7d' ? '7 Days' : period === '30d' ? '30 Days' : period === '90d' ? '90 Days' : '1 Year'}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Key Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-green-100 text-green-600">
              <BanknotesIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Monthly Revenue</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">
                {formatCurrency(dynamicMetrics.monthlyRecurringRevenue)}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-blue-100 text-blue-600">
              <UsersIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Total Customers</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">
                {formatNumber(metrics.customerCount)}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-purple-100 text-purple-600">
              <DeviceTabletIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Active Devices</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">
                {formatNumber(metrics.deviceCount)}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-indigo-100 text-indigo-600">
              <ArrowTrendingUpIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Growth Rate</p>
              <p className="text-lg font-semibold text-green-600">
                +{metrics.growthRate}%
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Revenue and Growth Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Revenue Trend</h3>
          <div className="space-y-4">
            {filteredTimeSeriesData.map((data, index) => (
              <div key={data.month} className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="w-3 h-3 bg-blue-600 rounded-full"></div>
                  <span className="text-sm font-medium text-gray-900 dark:text-white">{data.month}</span>
                </div>
                <div className="text-right">
                  <div className="text-sm font-semibold text-gray-900 dark:text-white">
                    {formatCurrency(data.revenue)}
                  </div>
                  <div className="text-xs text-gray-500 dark:text-gray-400">
                    {index > 0 ? 
                      `${((data.revenue - filteredTimeSeriesData[index - 1].revenue) / filteredTimeSeriesData[index - 1].revenue * 100).toFixed(1)}%` : 
                      '0%'
                    } from previous
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">User & Device Growth</h3>
          <div className="space-y-4">
            {filteredTimeSeriesData.map((data, index) => (
              <div key={data.month} className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-gray-900 dark:text-white">{data.month}</span>
                  <span className="text-sm text-gray-500 dark:text-gray-400">
                    {index > 0 ? 
                      `${((data.users - filteredTimeSeriesData[index - 1].users) / filteredTimeSeriesData[index - 1].users * 100).toFixed(1)}%` : 
                      '0%'
                    } growth
                  </span>
                </div>
                <div className="flex space-x-4">
                  <div className="flex-1">
                    <div className="text-xs text-gray-500 dark:text-gray-400 mb-1">Users</div>
                    <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                      <div 
                        className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                        style={{ width: `${(data.users / Math.max(...filteredTimeSeriesData.map(d => d.users))) * 100}%` }}
                      ></div>
                    </div>
                  </div>
                  <div className="flex-1">
                    <div className="text-xs text-gray-500 dark:text-gray-400 mb-1">Devices</div>
                    <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                      <div 
                        className="bg-green-600 h-2 rounded-full transition-all duration-300"
                        style={{ width: `${(data.devices / Math.max(...filteredTimeSeriesData.map(d => d.devices))) * 100}%` }}
                      ></div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Business Insights */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Business Insights</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="text-center p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
            <div className="text-2xl font-bold text-green-600 dark:text-green-400">
              {formatCurrency(dynamicMetrics.annualRecurringRevenue)}
            </div>
            <div className="text-sm text-green-600 dark:text-green-400">Annual Recurring Revenue</div>
          </div>
          <div className="text-center p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
            <div className="text-2xl font-bold text-blue-600 dark:text-blue-400">
              {metrics.dataProcessed} TB
            </div>
            <div className="text-sm text-blue-600 dark:text-blue-400">Data Processed Today</div>
          </div>
          <div className="text-center p-4 bg-red-50 dark:bg-red-900/20 rounded-lg">
            <div className="text-2xl font-bold text-red-600 dark:text-red-400">
              {metrics.churnRate}%
            </div>
            <div className="text-sm text-red-600 dark:text-red-400">Customer Churn Rate</div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="mt-8 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Quick Actions</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <button 
            onClick={handleExportReport}
            className="flex items-center justify-center px-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <ChartBarIcon className="h-5 w-5 mr-2" />
            Export Report
          </button>
          <button 
            onClick={handleGrowthAnalysis}
            className="flex items-center justify-center px-4 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
          >
            <ArrowTrendingUpIcon className="h-5 w-5 mr-2" />
            Growth Analysis
          </button>
          <button 
            onClick={handleRiskAssessment}
            className="flex items-center justify-center px-4 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
          >
            <ExclamationTriangleIcon className="h-5 w-5 mr-2" />
            Risk Assessment
          </button>
        </div>
      </div>
    </div>
  )
}
