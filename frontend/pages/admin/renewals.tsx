import React, { useState } from 'react'
import Head from 'next/head'
import AdminRoute from '../../components/AdminRoute'
import Layout from '../../components/Layout'
import { 
  ArrowPathIcon, 
  ClockIcon, 
  ExclamationTriangleIcon,
  PlusIcon,
  MagnifyingGlassIcon,
  FunnelIcon,
  ArrowDownTrayIcon,
  EyeIcon,
  PencilIcon,
  CheckCircleIcon,
  XMarkIcon,
  CurrencyDollarIcon,
  CalendarIcon
} from '@heroicons/react/24/outline'

interface Renewal {
  id: string
  customerId: string
  customerName: string
  customerEmail: string
  subscriptionPlan: string
  currentPeriod: {
    start: string
    end: string
  }
  renewalDate: string
  status: 'Upcoming' | 'Due' | 'Overdue' | 'Renewed' | 'Cancelled' | 'Expired'
  amount: number
  currency: string
  paymentMethod: string
  autoRenewal: boolean
  lastPayment: string
  nextBilling: string
  daysUntilRenewal: number
  renewalHistory: RenewalHistory[]
  notes: string
  createdBy: string
  createdAt: string
}

interface RenewalHistory {
  id: string
  renewalDate: string
  amount: number
  status: 'Success' | 'Failed' | 'Pending'
  paymentMethod: string
  notes: string
}

export default function Renewals() {
  return (
    <AdminRoute>
      <Layout>
        <RenewalsContent />
      </Layout>
    </AdminRoute>
  )
}

function RenewalsContent() {
  const [searchTerm, setSearchTerm] = useState('')
  const [filterStatus, setFilterStatus] = useState('All')
  const [filterPlan, setFilterPlan] = useState('All')
  const [selectedRenewal, setSelectedRenewal] = useState<Renewal | null>(null)

  // Mock data
  const renewals: Renewal[] = [
    {
      id: '1',
      customerId: 'CUST-001',
      customerName: 'Lagos State Government',
      customerEmail: 'aisha.bello@lagosstate.gov.ng',
      subscriptionPlan: 'Enterprise Pro',
      currentPeriod: {
        start: '2023-01-15',
        end: '2024-01-15'
      },
      renewalDate: '2024-01-15',
      status: 'Renewed',
      amount: 450000,
      currency: 'NGN',
      paymentMethod: 'Bank Transfer',
      autoRenewal: true,
      lastPayment: '2024-01-15',
      nextBilling: '2025-01-15',
      daysUntilRenewal: 0,
      renewalHistory: [
        {
          id: '1',
          renewalDate: '2024-01-15',
          amount: 450000,
          status: 'Success',
          paymentMethod: 'Bank Transfer',
          notes: 'Automatic renewal successful'
        }
      ],
      notes: 'Government contract - annual renewal',
      createdBy: 'System',
      createdAt: '2023-01-15'
    },
    {
      id: '2',
      customerId: 'CUST-002',
      customerName: 'Dangote Industries Ltd',
      customerEmail: 'ibrahim.dangote@dangote.com',
      subscriptionPlan: 'Enterprise Ultimate',
      currentPeriod: {
        start: '2023-04-20',
        end: '2024-04-20'
      },
      renewalDate: '2024-04-20',
      status: 'Upcoming',
      amount: 750000,
      currency: 'NGN',
      paymentMethod: 'Credit Card',
      autoRenewal: true,
      lastPayment: '2023-04-20',
      nextBilling: '2024-04-20',
      daysUntilRenewal: 95,
      renewalHistory: [
        {
          id: '2',
          renewalDate: '2023-04-20',
          amount: 750000,
          status: 'Success',
          paymentMethod: 'Credit Card',
          notes: 'Initial subscription'
        }
      ],
      notes: 'High-value customer - priority renewal',
      createdBy: 'Sales Team',
      createdAt: '2023-04-20'
    },
    {
      id: '3',
      customerId: 'CUST-003',
      customerName: 'TechHub Nigeria',
      customerEmail: 'fatima@techhub.ng',
      subscriptionPlan: 'SME Professional',
      currentPeriod: {
        start: '2023-11-10',
        end: '2024-02-10'
      },
      renewalDate: '2024-02-10',
      status: 'Due',
      amount: 85000,
      currency: 'NGN',
      paymentMethod: 'Bank Transfer',
      autoRenewal: false,
      lastPayment: '2023-11-10',
      nextBilling: '2024-02-10',
      daysUntilRenewal: -5,
      renewalHistory: [
        {
          id: '3',
          renewalDate: '2023-11-10',
          amount: 85000,
          status: 'Success',
          paymentMethod: 'Bank Transfer',
          notes: 'Initial subscription'
        }
      ],
      notes: 'Startup customer - manual renewal required',
      createdBy: 'Sales Team',
      createdAt: '2023-11-10'
    },
    {
      id: '4',
      customerId: 'CUST-004',
      customerName: 'Abuja Residential Complex',
      customerEmail: 'john.okoro@email.com',
      subscriptionPlan: 'Residential Plus',
      currentPeriod: {
        start: '2023-12-15',
        end: '2024-01-15'
      },
      renewalDate: '2024-01-15',
      status: 'Overdue',
      amount: 25000,
      currency: 'NGN',
      paymentMethod: 'Bank Transfer',
      autoRenewal: false,
      lastPayment: '2023-12-15',
      nextBilling: '2024-01-15',
      daysUntilRenewal: -15,
      renewalHistory: [
        {
          id: '4',
          renewalDate: '2023-12-15',
          amount: 25000,
          status: 'Success',
          paymentMethod: 'Bank Transfer',
          notes: 'Initial subscription'
        }
      ],
      notes: 'Payment reminder sent - awaiting response',
      createdBy: 'Sales Team',
      createdAt: '2023-12-15'
    },
    {
      id: '5',
      customerId: 'CUST-005',
      customerName: 'Kano Manufacturing Co.',
      customerEmail: 'musa.ibrahim@kanomanufacturing.com',
      subscriptionPlan: 'Enterprise Standard',
      currentPeriod: {
        start: '2023-08-30',
        end: '2024-02-15'
      },
      renewalDate: '2024-02-15',
      status: 'Cancelled',
      amount: 320000,
      currency: 'NGN',
      paymentMethod: 'Bank Transfer',
      autoRenewal: false,
      lastPayment: '2023-08-30',
      nextBilling: 'N/A',
      daysUntilRenewal: 0,
      renewalHistory: [
        {
          id: '5',
          renewalDate: '2023-08-30',
          amount: 320000,
          status: 'Success',
          paymentMethod: 'Bank Transfer',
          notes: 'Initial subscription'
        }
      ],
      notes: 'Customer cancelled due to budget constraints',
      createdBy: 'Sales Team',
      createdAt: '2023-08-30'
    },
    {
      id: '6',
      customerId: 'CUST-006',
      customerName: 'Port Harcourt Energy Co.',
      customerEmail: 'info@phcenergy.com',
      subscriptionPlan: 'Enterprise Pro',
      currentPeriod: {
        start: '2022-06-01',
        end: '2023-06-01'
      },
      renewalDate: '2023-06-01',
      status: 'Expired',
      amount: 380000,
      currency: 'NGN',
      paymentMethod: 'Bank Transfer',
      autoRenewal: false,
      lastPayment: '2022-06-01',
      nextBilling: 'N/A',
      daysUntilRenewal: -220,
      renewalHistory: [
        {
          id: '6',
          renewalDate: '2022-06-01',
          amount: 380000,
          status: 'Success',
          paymentMethod: 'Bank Transfer',
          notes: 'Initial subscription'
        }
      ],
      notes: 'Subscription expired - no renewal attempted',
      createdBy: 'Sales Team',
      createdAt: '2022-06-01'
    }
  ]

  const filteredRenewals = renewals.filter(renewal => {
    const matchesSearch = renewal.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         renewal.customerEmail.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         renewal.subscriptionPlan.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = filterStatus === 'All' || renewal.status === filterStatus
    const matchesPlan = filterPlan === 'All' || renewal.subscriptionPlan === filterPlan
    return matchesSearch && matchesStatus && matchesPlan
  })

  const totalRenewals = renewals.length
  const upcomingRenewals = renewals.filter(renewal => renewal.status === 'Upcoming').length
  const dueRenewals = renewals.filter(renewal => renewal.status === 'Due').length
  const overdueRenewals = renewals.filter(renewal => renewal.status === 'Overdue').length
  const totalRevenue = renewals.filter(renewal => renewal.status === 'Renewed').reduce((sum, renewal) => sum + renewal.amount, 0)

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'Renewed': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'Upcoming': return 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
      case 'Due': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
      case 'Overdue': return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
      case 'Cancelled': return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
      case 'Expired': return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getPlanColor = (plan: string) => {
    switch (plan) {
      case 'Enterprise Ultimate': return 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400'
      case 'Enterprise Pro': return 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
      case 'Enterprise Standard': return 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900/20 dark:text-indigo-400'
      case 'SME Professional': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'Residential Plus': return 'bg-orange-100 text-orange-800 dark:bg-orange-900/20 dark:text-orange-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getDaysColor = (days: number) => {
    if (days < 0) return 'text-red-600 dark:text-red-400'
    if (days <= 7) return 'text-yellow-600 dark:text-yellow-400'
    if (days <= 30) return 'text-orange-600 dark:text-orange-400'
    return 'text-green-600 dark:text-green-400'
  }

  return (
    <>
      <Head>
        <title>Renewal Management - SmartWatts Admin</title>
        <meta name="description" content="Manage subscription renewals and billing" />
      </Head>

      <div className="space-y-6">
        {/* Header */}
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Renewal Management</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage subscription renewals and billing</p>
          </div>
          <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2">
            <PlusIcon className="h-5 w-5" />
            <span>Process Renewal</span>
          </button>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
                <ArrowPathIcon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Renewals</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{totalRenewals}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-yellow-100 dark:bg-yellow-900/20 rounded-lg">
                <ExclamationTriangleIcon className="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Due Soon</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{dueRenewals}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-red-100 dark:bg-red-900/20 rounded-lg">
                <ClockIcon className="h-6 w-6 text-red-600 dark:text-red-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Overdue</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{overdueRenewals}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 dark:bg-green-900/20 rounded-lg">
                <CurrencyDollarIcon className="h-6 w-6 text-green-600 dark:text-green-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Renewal Revenue</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">₦{(totalRevenue / 1000000).toFixed(1)}M</p>
              </div>
            </div>
          </div>
        </div>

        {/* Filters */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Search</label>
              <div className="relative">
                <MagnifyingGlassIcon className="h-5 w-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search renewals..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10 w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Status</label>
              <select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
                className="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="All">All Status</option>
                <option value="Upcoming">Upcoming</option>
                <option value="Due">Due</option>
                <option value="Overdue">Overdue</option>
                <option value="Renewed">Renewed</option>
                <option value="Cancelled">Cancelled</option>
                <option value="Expired">Expired</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Plan</label>
              <select
                value={filterPlan}
                onChange={(e) => setFilterPlan(e.target.value)}
                className="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="All">All Plans</option>
                <option value="Enterprise Ultimate">Enterprise Ultimate</option>
                <option value="Enterprise Pro">Enterprise Pro</option>
                <option value="Enterprise Standard">Enterprise Standard</option>
                <option value="SME Professional">SME Professional</option>
                <option value="Residential Plus">Residential Plus</option>
              </select>
            </div>

            <div className="flex items-end space-x-2">
              <button className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2">
                <FunnelIcon className="h-4 w-4" />
                <span>Filter</span>
              </button>
              <button className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2">
                <ArrowDownTrayIcon className="h-4 w-4" />
                <span>Export</span>
              </button>
            </div>
          </div>
        </div>

        {/* Renewals Table */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
              <thead className="bg-gray-50 dark:bg-gray-700">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Customer</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Plan</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Current Period</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Renewal Date</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Amount</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Days</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Auto Renewal</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                {filteredRenewals.map((renewal) => (
                  <tr key={renewal.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900 dark:text-white">{renewal.customerName}</div>
                        <div className="text-sm text-gray-500 dark:text-gray-400">{renewal.customerEmail}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getPlanColor(renewal.subscriptionPlan)}`}>
                        {renewal.subscriptionPlan}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      <div>
                        <div>{new Date(renewal.currentPeriod.start).toLocaleDateString()}</div>
                        <div className="text-gray-500 dark:text-gray-400">to {new Date(renewal.currentPeriod.end).toLocaleDateString()}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      {new Date(renewal.renewalDate).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(renewal.status)}`}>
                        {renewal.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      ₦{renewal.amount.toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`text-sm font-medium ${getDaysColor(renewal.daysUntilRenewal)}`}>
                        {renewal.daysUntilRenewal > 0 ? `+${renewal.daysUntilRenewal}` : renewal.daysUntilRenewal}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                        renewal.autoRenewal 
                          ? 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
                          : 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
                      }`}>
                        {renewal.autoRenewal ? 'Yes' : 'No'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex space-x-2">
                        <button className="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300">
                          <EyeIcon className="h-4 w-4" />
                        </button>
                        <button className="text-indigo-600 hover:text-indigo-900 dark:text-indigo-400 dark:hover:text-indigo-300">
                          <PencilIcon className="h-4 w-4" />
                        </button>
                        <button className="text-green-600 hover:text-green-900 dark:text-green-400 dark:hover:text-green-300">
                          <CheckCircleIcon className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </>
  )
}
