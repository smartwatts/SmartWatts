import React, { useState } from 'react'
import Head from 'next/head'
import AdminRoute from '../../components/AdminRoute'
import Layout from '../../components/Layout'
import { 
  BuildingOffice2Icon, 
  UserGroupIcon, 
  CreditCardIcon, 
  ChartBarIcon,
  PlusIcon,
  MagnifyingGlassIcon,
  FunnelIcon,
  ArrowDownTrayIcon,
  EyeIcon,
  PencilIcon,
  TrashIcon
} from '@heroicons/react/24/outline'

interface Account {
  id: string
  name: string
  type: 'Enterprise' | 'SME' | 'Residential'
  status: 'Active' | 'Suspended' | 'Pending' | 'Cancelled'
  contactPerson: string
  email: string
  phone: string
  address: string
  city: string
  state: string
  subscriptionPlan: string
  monthlyRevenue: number
  lastPayment: string
  nextBilling: string
  devices: number
  energySavings: number
  createdAt: string
}

export default function Accounts() {
  return (
    <AdminRoute>
      <Layout>
        <AccountsContent />
      </Layout>
    </AdminRoute>
  )
}

function AccountsContent() {
  const [searchTerm, setSearchTerm] = useState('')
  const [filterType, setFilterType] = useState('All')
  const [filterStatus, setFilterStatus] = useState('All')
  const [selectedAccount, setSelectedAccount] = useState<Account | null>(null)

  // Mock data
  const accounts: Account[] = [
    {
      id: '1',
      name: 'Lagos State Government',
      type: 'Enterprise',
      status: 'Active',
      contactPerson: 'Dr. Aisha Bello',
      email: 'aisha.bello@lagosstate.gov.ng',
      phone: '+234 802 123 4567',
      address: 'Alausa Secretariat, Ikeja',
      city: 'Lagos',
      state: 'Lagos',
      subscriptionPlan: 'Enterprise Pro',
      monthlyRevenue: 450000,
      lastPayment: '2024-01-15',
      nextBilling: '2024-02-15',
      devices: 125,
      energySavings: 35,
      createdAt: '2023-06-15'
    },
    {
      id: '2',
      name: 'Dangote Industries Ltd',
      type: 'Enterprise',
      status: 'Active',
      contactPerson: 'Mr. Ibrahim Dangote',
      email: 'ibrahim.dangote@dangote.com',
      phone: '+234 803 987 6543',
      address: 'Dangote Tower, Victoria Island',
      city: 'Lagos',
      state: 'Lagos',
      subscriptionPlan: 'Enterprise Ultimate',
      monthlyRevenue: 750000,
      lastPayment: '2024-01-10',
      nextBilling: '2024-02-10',
      devices: 250,
      energySavings: 42,
      createdAt: '2023-04-20'
    },
    {
      id: '3',
      name: 'TechHub Nigeria',
      type: 'SME',
      status: 'Active',
      contactPerson: 'Ms. Fatima Ahmed',
      email: 'fatima@techhub.ng',
      phone: '+234 805 456 7890',
      address: 'Yaba Tech Hub, Lagos',
      city: 'Lagos',
      state: 'Lagos',
      subscriptionPlan: 'SME Professional',
      monthlyRevenue: 85000,
      lastPayment: '2024-01-20',
      nextBilling: '2024-02-20',
      devices: 15,
      energySavings: 28,
      createdAt: '2023-11-10'
    },
    {
      id: '4',
      name: 'Abuja Residential Complex',
      type: 'Residential',
      status: 'Pending',
      contactPerson: 'Mr. John Okoro',
      email: 'john.okoro@email.com',
      phone: '+234 807 123 4567',
      address: 'Asokoro District, Abuja',
      city: 'Abuja',
      state: 'FCT',
      subscriptionPlan: 'Residential Plus',
      monthlyRevenue: 25000,
      lastPayment: '2024-01-05',
      nextBilling: '2024-02-05',
      devices: 8,
      energySavings: 22,
      createdAt: '2023-12-15'
    },
    {
      id: '5',
      name: 'Kano Manufacturing Co.',
      type: 'Enterprise',
      status: 'Suspended',
      contactPerson: 'Alhaji Musa Ibrahim',
      email: 'musa.ibrahim@kanomanufacturing.com',
      phone: '+234 809 876 5432',
      address: 'Industrial Area, Kano',
      city: 'Kano',
      state: 'Kano',
      subscriptionPlan: 'Enterprise Standard',
      monthlyRevenue: 320000,
      lastPayment: '2023-12-15',
      nextBilling: '2024-02-15',
      devices: 85,
      energySavings: 38,
      createdAt: '2023-08-30'
    }
  ]

  const filteredAccounts = accounts.filter(account => {
    const matchesSearch = account.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         account.contactPerson.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         account.email.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesType = filterType === 'All' || account.type === filterType
    const matchesStatus = filterStatus === 'All' || account.status === filterStatus
    return matchesSearch && matchesType && matchesStatus
  })

  const totalRevenue = accounts.reduce((sum, account) => sum + account.monthlyRevenue, 0)
  const activeAccounts = accounts.filter(account => account.status === 'Active').length
  const totalDevices = accounts.reduce((sum, account) => sum + account.devices, 0)
  const averageSavings = accounts.reduce((sum, account) => sum + account.energySavings, 0) / accounts.length

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'Active': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'Pending': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
      case 'Suspended': return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
      case 'Cancelled': return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'Enterprise': return 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
      case 'SME': return 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400'
      case 'Residential': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  return (
    <>
      <Head>
        <title>Account Management - SmartWatts Admin</title>
        <meta name="description" content="Manage customer accounts, subscriptions, and billing" />
      </Head>

      <div className="space-y-6">
        {/* Header */}
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Account Management</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage customer accounts, subscriptions, and billing</p>
          </div>
          <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2">
            <PlusIcon className="h-5 w-5" />
            <span>Add Account</span>
          </button>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
                <BuildingOffice2Icon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Accounts</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{accounts.length}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 dark:bg-green-900/20 rounded-lg">
                <ChartBarIcon className="h-6 w-6 text-green-600 dark:text-green-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Active Accounts</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{activeAccounts}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-purple-100 dark:bg-purple-900/20 rounded-lg">
                <CreditCardIcon className="h-6 w-6 text-purple-600 dark:text-purple-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Monthly Revenue</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">₦{(totalRevenue / 1000000).toFixed(1)}M</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-orange-100 dark:bg-orange-900/20 rounded-lg">
                <UserGroupIcon className="h-6 w-6 text-orange-600 dark:text-orange-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Devices</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{totalDevices}</p>
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
                  placeholder="Search accounts..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10 w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Account Type</label>
              <select
                value={filterType}
                onChange={(e) => setFilterType(e.target.value)}
                className="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="All">All Types</option>
                <option value="Enterprise">Enterprise</option>
                <option value="SME">SME</option>
                <option value="Residential">Residential</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Status</label>
              <select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
                className="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="All">All Status</option>
                <option value="Active">Active</option>
                <option value="Pending">Pending</option>
                <option value="Suspended">Suspended</option>
                <option value="Cancelled">Cancelled</option>
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

        {/* Accounts Table */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
              <thead className="bg-gray-50 dark:bg-gray-700">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Account</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Type</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Contact</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Revenue</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Devices</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Savings</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                {filteredAccounts.map((account) => (
                  <tr key={account.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900 dark:text-white">{account.name}</div>
                        <div className="text-sm text-gray-500 dark:text-gray-400">{account.city}, {account.state}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getTypeColor(account.type)}`}>
                        {account.type}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(account.status)}`}>
                        {account.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm text-gray-900 dark:text-white">{account.contactPerson}</div>
                        <div className="text-sm text-gray-500 dark:text-gray-400">{account.email}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      ₦{account.monthlyRevenue.toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      {account.devices}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      {account.energySavings}%
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex space-x-2">
                        <button className="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300">
                          <EyeIcon className="h-4 w-4" />
                        </button>
                        <button className="text-indigo-600 hover:text-indigo-900 dark:text-indigo-400 dark:hover:text-indigo-300">
                          <PencilIcon className="h-4 w-4" />
                        </button>
                        <button className="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300">
                          <TrashIcon className="h-4 w-4" />
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
