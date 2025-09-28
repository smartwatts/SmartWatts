import React, { useState } from 'react'
import Head from 'next/head'
import AdminRoute from '../../components/AdminRoute'
import Layout from '../../components/Layout'
import { 
  ShoppingCartIcon, 
  CurrencyDollarIcon, 
  TruckIcon,
  PlusIcon,
  MagnifyingGlassIcon,
  FunnelIcon,
  ArrowDownTrayIcon,
  EyeIcon,
  PencilIcon,
  CheckCircleIcon,
  XMarkIcon,
  ClockIcon
} from '@heroicons/react/24/outline'

interface PurchaseOrder {
  id: string
  orderNumber: string
  supplier: string
  supplierContact: string
  supplierEmail: string
  orderDate: string
  expectedDelivery: string
  actualDelivery?: string
  status: 'Pending' | 'Approved' | 'Ordered' | 'Shipped' | 'Delivered' | 'Cancelled'
  totalAmount: number
  currency: string
  items: PurchaseItem[]
  paymentTerms: string
  paymentStatus: 'Pending' | 'Partial' | 'Paid' | 'Overdue'
  notes: string
  createdBy: string
  approvedBy?: string
  createdAt: string
}

interface PurchaseItem {
  id: string
  name: string
  sku: string
  quantity: number
  unitPrice: number
  totalPrice: number
  category: string
}

export default function Purchase() {
  return (
    <AdminRoute>
      <Layout>
        <PurchaseContent />
      </Layout>
    </AdminRoute>
  )
}

function PurchaseContent() {
  const [searchTerm, setSearchTerm] = useState('')
  const [filterStatus, setFilterStatus] = useState('All')
  const [filterSupplier, setFilterSupplier] = useState('All')
  const [selectedOrder, setSelectedOrder] = useState<PurchaseOrder | null>(null)

  // Mock data
  const purchaseOrders: PurchaseOrder[] = [
    {
      id: '1',
      orderNumber: 'PO-2024-001',
      supplier: 'Nigerian Energy Solutions Ltd',
      supplierContact: 'Mr. Adebayo Ogunlesi',
      supplierEmail: 'adebayo@nesolutions.ng',
      orderDate: '2024-01-15',
      expectedDelivery: '2024-02-15',
      actualDelivery: '2024-02-12',
      status: 'Delivered',
      totalAmount: 2500000,
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
        }
      ],
      paymentTerms: 'Net 30',
      paymentStatus: 'Paid',
      notes: 'Urgent delivery required for Lagos State project',
      createdBy: 'Admin User',
      approvedBy: 'Finance Manager',
      createdAt: '2024-01-15'
    },
    {
      id: '2',
      orderNumber: 'PO-2024-002',
      supplier: 'SolarTech Nigeria',
      supplierContact: 'Ms. Fatima Ibrahim',
      supplierEmail: 'fatima@solartech.ng',
      orderDate: '2024-01-20',
      expectedDelivery: '2024-02-20',
      status: 'Shipped',
      totalAmount: 1800000,
      currency: 'NGN',
      items: [
        {
          id: '2',
          name: 'Solar Inverter - 5KW Hybrid',
          sku: 'SI-5KW-H-002',
          quantity: 4,
          unitPrice: 450000,
          totalPrice: 1800000,
          category: 'Solar Inverters'
        }
      ],
      paymentTerms: 'Net 45',
      paymentStatus: 'Pending',
      notes: 'Standard delivery acceptable',
      createdBy: 'Inventory Manager',
      approvedBy: 'Operations Director',
      createdAt: '2024-01-20'
    },
    {
      id: '3',
      orderNumber: 'PO-2024-003',
      supplier: 'IoT Solutions Africa',
      supplierContact: 'Dr. Chinedu Okoro',
      supplierEmail: 'chinedu@iotsolutions.africa',
      orderDate: '2024-01-25',
      expectedDelivery: '2024-02-25',
      status: 'Ordered',
      totalAmount: 500000,
      currency: 'NGN',
      items: [
        {
          id: '3',
          name: 'Energy Monitoring Sensor',
          sku: 'EMS-001',
          quantity: 20,
          unitPrice: 25000,
          totalPrice: 500000,
          category: 'Sensors'
        }
      ],
      paymentTerms: 'Net 15',
      paymentStatus: 'Pending',
      notes: 'Bulk order for inventory replenishment',
      createdBy: 'Procurement Officer',
      createdAt: '2024-01-25'
    },
    {
      id: '4',
      orderNumber: 'PO-2024-004',
      supplier: 'Smart Home Nigeria',
      supplierContact: 'Mr. Tunde Adebayo',
      supplierEmail: 'tunde@smarthome.ng',
      orderDate: '2024-01-28',
      expectedDelivery: '2024-02-28',
      status: 'Pending',
      totalAmount: 750000,
      currency: 'NGN',
      items: [
        {
          id: '4',
          name: 'Smart Plug - WiFi Enabled',
          sku: 'SP-WIFI-004',
          quantity: 50,
          unitPrice: 15000,
          totalPrice: 750000,
          category: 'Accessories'
        }
      ],
      paymentTerms: 'Net 30',
      paymentStatus: 'Pending',
      notes: 'Awaiting approval from Finance',
      createdBy: 'Procurement Officer',
      createdAt: '2024-01-28'
    },
    {
      id: '5',
      orderNumber: 'PO-2024-005',
      supplier: 'Power Storage Solutions',
      supplierContact: 'Engr. Aisha Mohammed',
      supplierEmail: 'aisha@powerstorage.ng',
      orderDate: '2024-01-30',
      expectedDelivery: '2024-03-01',
      status: 'Approved',
      totalAmount: 4250000,
      currency: 'NGN',
      items: [
        {
          id: '5',
          name: 'Battery Storage System - 10KWh',
          sku: 'BSS-10KWH-005',
          quantity: 5,
          unitPrice: 850000,
          totalPrice: 4250000,
          category: 'Solar Inverters'
        }
      ],
      paymentTerms: 'Net 60',
      paymentStatus: 'Pending',
      notes: 'High-value order - requires CEO approval',
      createdBy: 'Operations Director',
      approvedBy: 'CEO',
      createdAt: '2024-01-30'
    }
  ]

  const filteredOrders = purchaseOrders.filter(order => {
    const matchesSearch = order.orderNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         order.supplier.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         order.supplierContact.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = filterStatus === 'All' || order.status === filterStatus
    const matchesSupplier = filterSupplier === 'All' || order.supplier === filterSupplier
    return matchesSearch && matchesStatus && matchesSupplier
  })

  const totalOrders = purchaseOrders.length
  const pendingOrders = purchaseOrders.filter(order => order.status === 'Pending').length
  const totalValue = purchaseOrders.reduce((sum, order) => sum + order.totalAmount, 0)
  const overdueOrders = purchaseOrders.filter(order => 
    order.paymentStatus === 'Overdue' || 
    (order.status === 'Delivered' && order.paymentStatus === 'Pending')
  ).length

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'Delivered': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'Shipped': return 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
      case 'Ordered': return 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400'
      case 'Approved': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
      case 'Pending': return 'bg-orange-100 text-orange-800 dark:bg-orange-900/20 dark:text-orange-400'
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

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'Delivered': return <CheckCircleIcon className="h-4 w-4" />
      case 'Shipped': return <TruckIcon className="h-4 w-4" />
      case 'Ordered': return <ShoppingCartIcon className="h-4 w-4" />
      case 'Approved': return <CheckCircleIcon className="h-4 w-4" />
      case 'Pending': return <ClockIcon className="h-4 w-4" />
      case 'Cancelled': return <XMarkIcon className="h-4 w-4" />
      default: return <ClockIcon className="h-4 w-4" />
    }
  }

  return (
    <>
      <Head>
        <title>Purchase Management - SmartWatts Admin</title>
        <meta name="description" content="Manage purchase orders, suppliers, and procurement" />
      </Head>

      <div className="space-y-6">
        {/* Header */}
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Purchase Management</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage purchase orders, suppliers, and procurement</p>
          </div>
          <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2">
            <PlusIcon className="h-5 w-5" />
            <span>New Purchase Order</span>
          </button>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
                <ShoppingCartIcon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Orders</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{totalOrders}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-orange-100 dark:bg-orange-900/20 rounded-lg">
                <ClockIcon className="h-6 w-6 text-orange-600 dark:text-orange-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Pending Orders</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{pendingOrders}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 dark:bg-green-900/20 rounded-lg">
                <CurrencyDollarIcon className="h-6 w-6 text-green-600 dark:text-green-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Value</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">₦{(totalValue / 1000000).toFixed(1)}M</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-red-100 dark:bg-red-900/20 rounded-lg">
                <XMarkIcon className="h-6 w-6 text-red-600 dark:text-red-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Overdue</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{overdueOrders}</p>
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
                  placeholder="Search orders..."
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
                <option value="Pending">Pending</option>
                <option value="Approved">Approved</option>
                <option value="Ordered">Ordered</option>
                <option value="Shipped">Shipped</option>
                <option value="Delivered">Delivered</option>
                <option value="Cancelled">Cancelled</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Supplier</label>
              <select
                value={filterSupplier}
                onChange={(e) => setFilterSupplier(e.target.value)}
                className="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="All">All Suppliers</option>
                <option value="Nigerian Energy Solutions Ltd">Nigerian Energy Solutions Ltd</option>
                <option value="SolarTech Nigeria">SolarTech Nigeria</option>
                <option value="IoT Solutions Africa">IoT Solutions Africa</option>
                <option value="Smart Home Nigeria">Smart Home Nigeria</option>
                <option value="Power Storage Solutions">Power Storage Solutions</option>
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

        {/* Purchase Orders Table */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
              <thead className="bg-gray-50 dark:bg-gray-700">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Order</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Supplier</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Order Date</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Delivery</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Amount</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Payment</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                {filteredOrders.map((order) => (
                  <tr key={order.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900 dark:text-white">{order.orderNumber}</div>
                        <div className="text-sm text-gray-500 dark:text-gray-400">{order.items.length} items</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm text-gray-900 dark:text-white">{order.supplier}</div>
                        <div className="text-sm text-gray-500 dark:text-gray-400">{order.supplierContact}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      {new Date(order.orderDate).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900 dark:text-white">
                        Expected: {new Date(order.expectedDelivery).toLocaleDateString()}
                      </div>
                      {order.actualDelivery && (
                        <div className="text-sm text-green-600 dark:text-green-400">
                          Delivered: {new Date(order.actualDelivery).toLocaleDateString()}
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(order.status)}`}>
                        {getStatusIcon(order.status)}
                        <span className="ml-1">{order.status}</span>
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      ₦{order.totalAmount.toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getPaymentStatusColor(order.paymentStatus)}`}>
                        {order.paymentStatus}
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
