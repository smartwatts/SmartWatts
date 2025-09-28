import React, { useState } from 'react'
import Head from 'next/head'
import AdminRoute from '../../components/AdminRoute'
import Layout from '../../components/Layout'
import { 
  ChartBarIcon, 
  CurrencyDollarIcon, 
  UserGroupIcon,
  PlusIcon,
  MagnifyingGlassIcon,
  FunnelIcon,
  ArrowDownTrayIcon,
  EyeIcon,
  PencilIcon,
  CheckCircleIcon,
  XMarkIcon,
  ClockIcon,
  ArrowTrendingUpIcon
} from '@heroicons/react/24/outline'

interface Sale {
  id: string
  invoiceNumber: string
  customer: string
  customerType: 'Enterprise' | 'SME' | 'Residential'
  contactPerson: string
  email: string
  phone: string
  saleDate: string
  status: 'Quote' | 'Pending' | 'Confirmed' | 'In Progress' | 'Completed' | 'Cancelled'
  totalAmount: number
  currency: string
  items: SaleItem[]
  paymentTerms: string
  paymentStatus: 'Pending' | 'Partial' | 'Paid' | 'Overdue'
  salesRep: string
  commission: number
  discount: number
  notes: string
  createdAt: string
}

interface SaleItem {
  id: string
  name: string
  sku: string
  quantity: number
  unitPrice: number
  totalPrice: number
  category: string
}

export default function Sales() {
  return (
    <AdminRoute>
      <Layout>
        <SalesContent />
      </Layout>
    </AdminRoute>
  )
}

function SalesContent() {
  const [searchTerm, setSearchTerm] = useState('')
  const [filterStatus, setFilterStatus] = useState('All')
  const [filterCustomerType, setFilterCustomerType] = useState('All')
  const [selectedSale, setSelectedSale] = useState<Sale | null>(null)

  // Mock data
  const sales: Sale[] = [
    {
      id: '1',
      invoiceNumber: 'INV-2024-001',
      customer: 'Lagos State Government',
      customerType: 'Enterprise',
      contactPerson: 'Dr. Aisha Bello',
      email: 'aisha.bello@lagosstate.gov.ng',
      phone: '+234 802 123 4567',
      saleDate: '2024-01-15',
      status: 'Completed',
      totalAmount: 4500000,
      currency: 'NGN',
      items: [
        {
          id: '1',
          name: 'Smart Energy Meter - 3 Phase',
          sku: 'SEM-3P-001',
          quantity: 20,
          unitPrice: 125000,
          totalPrice: 2500000,
          category: 'Smart Meters'
        },
        {
          id: '2',
          name: 'Energy Monitoring System',
          sku: 'EMS-PRO-002',
          quantity: 1,
          unitPrice: 2000000,
          totalPrice: 2000000,
          category: 'Monitoring Devices'
        }
      ],
      paymentTerms: 'Net 30',
      paymentStatus: 'Paid',
      salesRep: 'John Adebayo',
      commission: 225000,
      discount: 0,
      notes: 'Government contract - priority delivery',
      createdAt: '2024-01-15'
    },
    {
      id: '2',
      invoiceNumber: 'INV-2024-002',
      customer: 'Dangote Industries Ltd',
      customerType: 'Enterprise',
      contactPerson: 'Mr. Ibrahim Dangote',
      email: 'ibrahim.dangote@dangote.com',
      phone: '+234 803 987 6543',
      saleDate: '2024-01-20',
      status: 'In Progress',
      totalAmount: 7500000,
      currency: 'NGN',
      items: [
        {
          id: '3',
          name: 'Solar Inverter - 5KW Hybrid',
          sku: 'SI-5KW-H-002',
          quantity: 10,
          unitPrice: 450000,
          totalPrice: 4500000,
          category: 'Solar Inverters'
        },
        {
          id: '4',
          name: 'Battery Storage System - 10KWh',
          sku: 'BSS-10KWH-005',
          quantity: 5,
          unitPrice: 600000,
          totalPrice: 3000000,
          category: 'Solar Inverters'
        }
      ],
      paymentTerms: 'Net 45',
      paymentStatus: 'Partial',
      salesRep: 'Sarah Mohammed',
      commission: 375000,
      discount: 150000,
      notes: 'Bulk order - installation in progress',
      createdAt: '2024-01-20'
    },
    {
      id: '3',
      invoiceNumber: 'INV-2024-003',
      customer: 'TechHub Nigeria',
      customerType: 'SME',
      contactPerson: 'Ms. Fatima Ahmed',
      email: 'fatima@techhub.ng',
      phone: '+234 805 456 7890',
      saleDate: '2024-01-25',
      status: 'Confirmed',
      totalAmount: 850000,
      currency: 'NGN',
      items: [
        {
          id: '5',
          name: 'Smart Plug - WiFi Enabled',
          sku: 'SP-WIFI-004',
          quantity: 50,
          unitPrice: 15000,
          totalPrice: 750000,
          category: 'Accessories'
        },
        {
          id: '6',
          name: 'Energy Monitoring Sensor',
          sku: 'EMS-001',
          quantity: 4,
          unitPrice: 25000,
          totalPrice: 100000,
          category: 'Sensors'
        }
      ],
      paymentTerms: 'Net 15',
      paymentStatus: 'Pending',
      salesRep: 'Michael Okoro',
      commission: 42500,
      discount: 0,
      notes: 'Startup customer - standard terms',
      createdAt: '2024-01-25'
    },
    {
      id: '4',
      invoiceNumber: 'INV-2024-004',
      customer: 'Abuja Residential Complex',
      customerType: 'Residential',
      contactPerson: 'Mr. John Okoro',
      email: 'john.okoro@email.com',
      phone: '+234 807 123 4567',
      saleDate: '2024-01-28',
      status: 'Quote',
      totalAmount: 250000,
      currency: 'NGN',
      items: [
        {
          id: '7',
          name: 'Smart Energy Meter - Single Phase',
          sku: 'SEM-1P-007',
          quantity: 8,
          unitPrice: 75000,
          totalPrice: 600000,
          category: 'Smart Meters'
        }
      ],
      paymentTerms: 'Net 30',
      paymentStatus: 'Pending',
      salesRep: 'Grace Okafor',
      commission: 12500,
      discount: 50000,
      notes: 'Residential package - awaiting approval',
      createdAt: '2024-01-28'
    },
    {
      id: '5',
      invoiceNumber: 'INV-2024-005',
      customer: 'Kano Manufacturing Co.',
      customerType: 'Enterprise',
      contactPerson: 'Alhaji Musa Ibrahim',
      email: 'musa.ibrahim@kanomanufacturing.com',
      phone: '+234 809 876 5432',
      saleDate: '2024-01-30',
      status: 'Cancelled',
      totalAmount: 3200000,
      currency: 'NGN',
      items: [
        {
          id: '8',
          name: 'Industrial Energy Monitor',
          sku: 'IEM-001',
          quantity: 2,
          unitPrice: 1600000,
          totalPrice: 3200000,
          category: 'Monitoring Devices'
        }
      ],
      paymentTerms: 'Net 60',
      paymentStatus: 'Pending',
      salesRep: 'David Adebayo',
      commission: 0,
      discount: 0,
      notes: 'Customer cancelled due to budget constraints',
      createdAt: '2024-01-30'
    }
  ]

  const filteredSales = sales.filter(sale => {
    const matchesSearch = sale.invoiceNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         sale.customer.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         sale.contactPerson.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = filterStatus === 'All' || sale.status === filterStatus
    const matchesCustomerType = filterCustomerType === 'All' || sale.customerType === filterCustomerType
    return matchesSearch && matchesStatus && matchesCustomerType
  })

  const totalSales = sales.length
  const completedSales = sales.filter(sale => sale.status === 'Completed').length
  const totalRevenue = sales.filter(sale => sale.status === 'Completed').reduce((sum, sale) => sum + sale.totalAmount, 0)
  const pendingSales = sales.filter(sale => sale.status === 'Pending' || sale.status === 'Quote').length
  const totalCommission = sales.reduce((sum, sale) => sum + sale.commission, 0)

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'Completed': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'In Progress': return 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
      case 'Confirmed': return 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400'
      case 'Pending': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
      case 'Quote': return 'bg-orange-100 text-orange-800 dark:bg-orange-900/20 dark:text-orange-400'
      case 'Cancelled': return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getPaymentStatusColor = (status: string) => {
    switch (status) {
      case 'Paid': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'Partial': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
      case 'Pending': return 'bg-orange-100 text-orange-800 dark:bg-orange-900/20 dark:text-orange-400'
      case 'Overdue': return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getCustomerTypeColor = (type: string) => {
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
        <title>Sales Management - SmartWatts Admin</title>
        <meta name="description" content="Manage sales, customers, and revenue" />
      </Head>

      <div className="space-y-6">
        {/* Header */}
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Sales Management</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage sales, customers, and revenue</p>
          </div>
          <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2">
            <PlusIcon className="h-5 w-5" />
            <span>New Sale</span>
          </button>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
                <ChartBarIcon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Sales</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{totalSales}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 dark:bg-green-900/20 rounded-lg">
                <ArrowTrendingUpIcon className="h-6 w-6 text-green-600 dark:text-green-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Completed</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{completedSales}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-purple-100 dark:bg-purple-900/20 rounded-lg">
                <CurrencyDollarIcon className="h-6 w-6 text-purple-600 dark:text-purple-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Revenue</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">₦{(totalRevenue / 1000000).toFixed(1)}M</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-orange-100 dark:bg-orange-900/20 rounded-lg">
                <ClockIcon className="h-6 w-6 text-orange-600 dark:text-orange-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Pending</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{pendingSales}</p>
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
                  placeholder="Search sales..."
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
                <option value="Quote">Quote</option>
                <option value="Pending">Pending</option>
                <option value="Confirmed">Confirmed</option>
                <option value="In Progress">In Progress</option>
                <option value="Completed">Completed</option>
                <option value="Cancelled">Cancelled</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Customer Type</label>
              <select
                value={filterCustomerType}
                onChange={(e) => setFilterCustomerType(e.target.value)}
                className="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="All">All Types</option>
                <option value="Enterprise">Enterprise</option>
                <option value="SME">SME</option>
                <option value="Residential">Residential</option>
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

        {/* Sales Table */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
              <thead className="bg-gray-50 dark:bg-gray-700">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Invoice</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Customer</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Type</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Sale Date</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Amount</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Payment</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Sales Rep</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                {filteredSales.map((sale) => (
                  <tr key={sale.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900 dark:text-white">{sale.invoiceNumber}</div>
                        <div className="text-sm text-gray-500 dark:text-gray-400">{sale.items.length} items</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm text-gray-900 dark:text-white">{sale.customer}</div>
                        <div className="text-sm text-gray-500 dark:text-gray-400">{sale.contactPerson}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getCustomerTypeColor(sale.customerType)}`}>
                        {sale.customerType}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      {new Date(sale.saleDate).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(sale.status)}`}>
                        {sale.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      ₦{sale.totalAmount.toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getPaymentStatusColor(sale.paymentStatus)}`}>
                        {sale.paymentStatus}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      {sale.salesRep}
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
