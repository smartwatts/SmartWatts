import React, { useState } from 'react'
import Head from 'next/head'
import AdminRoute from '../../components/AdminRoute'
import Layout from '../../components/Layout'
import { 
  CubeIcon, 
  ChartBarIcon, 
  ExclamationTriangleIcon,
  PlusIcon,
  MagnifyingGlassIcon,
  FunnelIcon,
  ArrowDownTrayIcon,
  EyeIcon,
  PencilIcon,
  TrashIcon,
  TruckIcon,
  CheckCircleIcon
} from '@heroicons/react/24/outline'

interface InventoryItem {
  id: string
  name: string
  category: 'Smart Meters' | 'Solar Inverters' | 'Monitoring Devices' | 'Sensors' | 'Accessories'
  sku: string
  currentStock: number
  minStock: number
  maxStock: number
  unitPrice: number
  totalValue: number
  supplier: string
  lastRestocked: string
  nextRestock: string
  status: 'In Stock' | 'Low Stock' | 'Out of Stock' | 'Discontinued'
  location: string
  condition: 'New' | 'Refurbished' | 'Used'
  warranty: string
  createdAt: string
}

export default function Inventory() {
  return (
    <AdminRoute>
      <Layout>
        <InventoryContent />
      </Layout>
    </AdminRoute>
  )
}

function InventoryContent() {
  const [searchTerm, setSearchTerm] = useState('')
  const [filterCategory, setFilterCategory] = useState('All')
  const [filterStatus, setFilterStatus] = useState('All')
  const [selectedItem, setSelectedItem] = useState<InventoryItem | null>(null)

  // Mock data
  const inventoryItems: InventoryItem[] = [
    {
      id: '1',
      name: 'Smart Energy Meter - 3 Phase',
      category: 'Smart Meters',
      sku: 'SEM-3P-001',
      currentStock: 45,
      minStock: 10,
      maxStock: 100,
      unitPrice: 125000,
      totalValue: 5625000,
      supplier: 'Nigerian Energy Solutions Ltd',
      lastRestocked: '2024-01-10',
      nextRestock: '2024-02-15',
      status: 'In Stock',
      location: 'Warehouse A - Section 1',
      condition: 'New',
      warranty: '2 Years',
      createdAt: '2023-06-15'
    },
    {
      id: '2',
      name: 'Solar Inverter - 5KW Hybrid',
      category: 'Solar Inverters',
      sku: 'SI-5KW-H-002',
      currentStock: 8,
      minStock: 15,
      maxStock: 50,
      unitPrice: 450000,
      totalValue: 3600000,
      supplier: 'SolarTech Nigeria',
      lastRestocked: '2024-01-05',
      nextRestock: '2024-01-25',
      status: 'Low Stock',
      location: 'Warehouse B - Section 2',
      condition: 'New',
      warranty: '5 Years',
      createdAt: '2023-08-20'
    },
    {
      id: '3',
      name: 'Energy Monitoring Sensor',
      category: 'Sensors',
      sku: 'EMS-001',
      currentStock: 0,
      minStock: 20,
      maxStock: 200,
      unitPrice: 25000,
      totalValue: 0,
      supplier: 'IoT Solutions Africa',
      lastRestocked: '2023-12-20',
      nextRestock: '2024-01-30',
      status: 'Out of Stock',
      location: 'Warehouse A - Section 3',
      condition: 'New',
      warranty: '1 Year',
      createdAt: '2023-10-10'
    },
    {
      id: '4',
      name: 'Smart Plug - WiFi Enabled',
      category: 'Accessories',
      sku: 'SP-WIFI-004',
      currentStock: 120,
      minStock: 50,
      maxStock: 300,
      unitPrice: 15000,
      totalValue: 1800000,
      supplier: 'Smart Home Nigeria',
      lastRestocked: '2024-01-15',
      nextRestock: '2024-03-15',
      status: 'In Stock',
      location: 'Warehouse C - Section 1',
      condition: 'New',
      warranty: '1 Year',
      createdAt: '2023-11-05'
    },
    {
      id: '5',
      name: 'Battery Storage System - 10KWh',
      category: 'Solar Inverters',
      sku: 'BSS-10KWH-005',
      currentStock: 3,
      minStock: 5,
      maxStock: 25,
      unitPrice: 850000,
      totalValue: 2550000,
      supplier: 'Power Storage Solutions',
      lastRestocked: '2024-01-08',
      nextRestock: '2024-02-08',
      status: 'Low Stock',
      location: 'Warehouse B - Section 1',
      condition: 'New',
      warranty: '10 Years',
      createdAt: '2023-09-15'
    },
    {
      id: '6',
      name: 'Legacy Energy Meter - Single Phase',
      category: 'Smart Meters',
      sku: 'LEM-1P-006',
      currentStock: 0,
      minStock: 0,
      maxStock: 0,
      unitPrice: 75000,
      totalValue: 0,
      supplier: 'Classic Meters Ltd',
      lastRestocked: '2023-08-15',
      nextRestock: 'N/A',
      status: 'Discontinued',
      location: 'Discontinued Items',
      condition: 'Refurbished',
      warranty: '1 Year',
      createdAt: '2022-12-01'
    }
  ]

  const filteredItems = inventoryItems.filter(item => {
    const matchesSearch = item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         item.sku.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         item.supplier.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesCategory = filterCategory === 'All' || item.category === filterCategory
    const matchesStatus = filterStatus === 'All' || item.status === filterStatus
    return matchesSearch && matchesCategory && matchesStatus
  })

  const totalValue = inventoryItems.reduce((sum, item) => sum + item.totalValue, 0)
  const lowStockItems = inventoryItems.filter(item => item.status === 'Low Stock').length
  const outOfStockItems = inventoryItems.filter(item => item.status === 'Out of Stock').length
  const totalItems = inventoryItems.reduce((sum, item) => sum + item.currentStock, 0)

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'In Stock': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'Low Stock': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
      case 'Out of Stock': return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
      case 'Discontinued': return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'Smart Meters': return 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
      case 'Solar Inverters': return 'bg-orange-100 text-orange-800 dark:bg-orange-900/20 dark:text-orange-400'
      case 'Monitoring Devices': return 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400'
      case 'Sensors': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'Accessories': return 'bg-pink-100 text-pink-800 dark:bg-pink-900/20 dark:text-pink-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getStockStatus = (item: InventoryItem) => {
    if (item.currentStock === 0) return 'Out of Stock'
    if (item.currentStock <= item.minStock) return 'Low Stock'
    return 'In Stock'
  }

  return (
    <>
      <Head>
        <title>Inventory Management - SmartWatts Admin</title>
        <meta name="description" content="Manage inventory, stock levels, and suppliers" />
      </Head>

      <div className="space-y-6">
        {/* Header */}
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Inventory Management</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage inventory, stock levels, and suppliers</p>
          </div>
          <div className="flex space-x-3">
            <button className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2">
              <TruckIcon className="h-5 w-5" />
              <span>Restock</span>
            </button>
            <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2">
              <PlusIcon className="h-5 w-5" />
              <span>Add Item</span>
            </button>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
                <CubeIcon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Items</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{totalItems}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 dark:bg-green-900/20 rounded-lg">
                <ChartBarIcon className="h-6 w-6 text-green-600 dark:text-green-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Value</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">₦{(totalValue / 1000000).toFixed(1)}M</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-yellow-100 dark:bg-yellow-900/20 rounded-lg">
                <ExclamationTriangleIcon className="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Low Stock</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{lowStockItems}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-red-100 dark:bg-red-900/20 rounded-lg">
                <ExclamationTriangleIcon className="h-6 w-6 text-red-600 dark:text-red-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Out of Stock</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{outOfStockItems}</p>
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
                  placeholder="Search inventory..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10 w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Category</label>
              <select
                value={filterCategory}
                onChange={(e) => setFilterCategory(e.target.value)}
                className="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="All">All Categories</option>
                <option value="Smart Meters">Smart Meters</option>
                <option value="Solar Inverters">Solar Inverters</option>
                <option value="Monitoring Devices">Monitoring Devices</option>
                <option value="Sensors">Sensors</option>
                <option value="Accessories">Accessories</option>
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
                <option value="In Stock">In Stock</option>
                <option value="Low Stock">Low Stock</option>
                <option value="Out of Stock">Out of Stock</option>
                <option value="Discontinued">Discontinued</option>
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

        {/* Inventory Table */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
              <thead className="bg-gray-50 dark:bg-gray-700">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Item</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Category</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Stock</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Unit Price</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Total Value</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Supplier</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Location</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                {filteredItems.map((item) => (
                  <tr key={item.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900 dark:text-white">{item.name}</div>
                        <div className="text-sm text-gray-500 dark:text-gray-400">SKU: {item.sku}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getCategoryColor(item.category)}`}>
                        {item.category}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900 dark:text-white">
                        {item.currentStock} / {item.maxStock}
                      </div>
                      <div className="text-xs text-gray-500 dark:text-gray-400">
                        Min: {item.minStock}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(item.status)}`}>
                        {item.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      ₦{item.unitPrice.toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      ₦{item.totalValue.toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      {item.supplier}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      {item.location}
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
