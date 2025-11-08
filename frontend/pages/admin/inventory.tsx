import React, { useState, useEffect } from 'react'
import Head from 'next/head'
import AdminRoute from '../../components/AdminRoute'
import Layout from '../../components/Layout'
import { useAuth } from '../../hooks/useAuth'
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
  CheckCircleIcon,
  XMarkIcon
} from '@heroicons/react/24/outline'

interface InventoryItem {
  id: string
  name: string
  category: 'SMART_METERS' | 'SOLAR_INVERTERS' | 'MONITORING_DEVICES' | 'SENSORS' | 'ACCESSORIES'
  sku: string
  currentStock: number
  minStock: number
  maxStock: number
  unitPrice: number
  totalValue: number
  supplier: string
  lastRestocked: string
  nextRestock: string
  status: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK' | 'DISCONTINUED'
  location: string
  conditionType: 'NEW' | 'REFURBISHED' | 'USED'
  warranty: string
  description: string
  createdAt: string
  updatedAt: string
}

interface InventoryStats {
  totalItems: number
  totalValue: number
  lowStockItems: number
  outOfStockItems: number
  inStockItems: number
  discontinuedItems: number
  totalCategories: number
  totalSuppliers: number
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
  const { user } = useAuth()
  const [searchTerm, setSearchTerm] = useState('')
  const [filterCategory, setFilterCategory] = useState('All')
  const [filterStatus, setFilterStatus] = useState('All')
  const [selectedItem, setSelectedItem] = useState<InventoryItem | null>(null)
  const [inventoryItems, setInventoryItems] = useState<InventoryItem[]>([])
  const [stats, setStats] = useState<InventoryStats>({
    totalItems: 0,
    totalValue: 0,
    lowStockItems: 0,
    outOfStockItems: 0,
    inStockItems: 0,
    discontinuedItems: 0,
    totalCategories: 0,
    totalSuppliers: 0
  })
  const [loading, setLoading] = useState(true)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [itemsPerPage] = useState(10)
  const [sortField, setSortField] = useState('name')
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc')
  const [selectedItems, setSelectedItems] = useState<string[]>([])
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [showViewModal, setShowViewModal] = useState(false)
  const [showRestockModal, setShowRestockModal] = useState(false)
  const [editingItem, setEditingItem] = useState<InventoryItem | null>(null)
  const [editItem, setEditItem] = useState({
    name: '',
    sku: '',
    category: '',
    supplier: '',
    currentStock: 0,
    minStock: 0,
    maxStock: 100,
    unitPrice: 0,
    location: '',
    conditionType: 'NEW',
    warranty: ''
  })
  const [viewingItem, setViewingItem] = useState<InventoryItem | null>(null)
  const [restockQuantity, setRestockQuantity] = useState(0)
  const [showAdvancedSearch, setShowAdvancedSearch] = useState(false)
  const [showBulkRestockModal, setShowBulkRestockModal] = useState(false)
  const [showBulkStatusModal, setShowBulkStatusModal] = useState(false)
  const [bulkRestockQuantity, setBulkRestockQuantity] = useState(0)
  const [bulkStatus, setBulkStatus] = useState('IN_STOCK')
  const [lowStockAlerts, setLowStockAlerts] = useState<InventoryItem[]>([])
  const [showAlerts, setShowAlerts] = useState(false)
  const [advancedFilters, setAdvancedFilters] = useState({
    minPrice: '',
    maxPrice: '',
    minStock: '',
    maxStock: '',
    supplier: ''
  })
  const [newItem, setNewItem] = useState({
    name: '',
    sku: '',
    category: '',
    supplier: '',
    currentStock: 0,
    minStock: 0,
    maxStock: 100,
    unitPrice: 0,
    location: '',
    conditionType: 'NEW',
    warranty: ''
  })

  // Load inventory data
  const loadInventory = async () => {
    try {
      console.log('🔄 Loading inventory data...')
      setLoading(true)
      const token = localStorage.getItem('token')
      
      // Load inventory items
      const queryParams = new URLSearchParams({
        page: currentPage.toString(),
        size: itemsPerPage.toString(),
        sortBy: sortField,
        sortDir: sortDirection
      })
      
      if (searchTerm) {
        queryParams.append('query', searchTerm)
      }
      if (filterCategory !== 'All') {
        queryParams.append('category', filterCategory)
      }
      if (filterStatus !== 'All') {
        queryParams.append('status', filterStatus)
      }
      
      const response = await fetch(`/api/proxy?service=inventory&path=/api/v1/inventory/search?${queryParams}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (response.ok) {
        const data = await response.json()
        console.log('✅ Inventory data loaded:', data)
        setInventoryItems(data.content || [])
        setTotalPages(data.totalPages || 0)
      } else {
        console.error('Failed to load inventory:', response.status, response.statusText)
        setInventoryItems([])
      }
    } catch (error) {
      console.error('Error loading inventory:', error)
      setInventoryItems([])
    } finally {
      setLoading(false)
    }
  }

  // Load inventory statistics
  const loadStats = async () => {
    try {
      const token = localStorage.getItem('token')
      const response = await fetch('/api/proxy?service=inventory&path=/api/v1/inventory/stats', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (response.ok) {
        const data = await response.json()
        setStats(data)
      }
    } catch (error) {
      console.error('Error loading stats:', error)
    }
  }

  const loadAlerts = async () => {
    try {
      const token = localStorage.getItem('token')
      const response = await fetch('/api/proxy?service=inventory&path=/api/v1/inventory/search?status=LOW_STOCK&size=10', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (response.ok) {
        const data = await response.json()
        setLowStockAlerts(data.content || [])
      }
    } catch (error) {
      console.error('Error loading alerts:', error)
    }
  }

  // Load data on component mount and when filters change
  useEffect(() => {
    if (user?.id) {
      loadInventory()
      loadStats()
      loadAlerts()
    }
  }, [user?.id, currentPage, sortField, sortDirection, searchTerm, filterCategory, filterStatus])

  // Use items directly since filtering is done on the backend
  const filteredItems = inventoryItems

  // Helper functions
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'IN_STOCK': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'LOW_STOCK': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
      case 'OUT_OF_STOCK': return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
      case 'DISCONTINUED': return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'SMART_METERS': return 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
      case 'SOLAR_INVERTERS': return 'bg-orange-100 text-orange-800 dark:bg-orange-900/20 dark:text-orange-400'
      case 'MONITORING_DEVICES': return 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400'
      case 'SENSORS': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'ACCESSORIES': return 'bg-pink-100 text-pink-800 dark:bg-pink-900/20 dark:text-pink-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const formatCategoryName = (category: string) => {
    return category.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
  }

  const formatStatusName = (status: string) => {
    return status.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
  }

  // Event handlers
  const handleAddItem = async (e: React.FormEvent) => {
    e.preventDefault()
    
    try {
      const token = localStorage.getItem('token')
      
      // Prepare the item data for API
      const itemData = {
        name: newItem.name,
        sku: newItem.sku,
        category: newItem.category,
        supplier: newItem.supplier,
        currentStock: newItem.currentStock,
        minStock: newItem.minStock,
        maxStock: newItem.maxStock,
        unitPrice: newItem.unitPrice,
        location: newItem.location || null,
        condition: newItem.conditionType,
        warranty: newItem.warranty || null
      }

      console.log('🔄 Creating new inventory item:', itemData)

      // Call the API to create the item
      const response = await fetch('/api/proxy?service=inventory&path=/api/v1/inventory', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(itemData)
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.message || 'Failed to create inventory item')
      }

      const createdItem = await response.json()
      console.log('✅ Inventory item created successfully:', createdItem)

      // Add to local state
      setInventoryItems(prev => [createdItem, ...prev])
      
      // Close modal and reset form
      setShowAddModal(false)
      setNewItem({
        name: '',
        sku: '',
        category: '',
        supplier: '',
        currentStock: 0,
        minStock: 0,
        maxStock: 100,
        unitPrice: 0,
        location: '',
        conditionType: 'NEW',
        warranty: ''
      })
      
      // Reload stats
      loadStats()
      
      alert('Inventory item added successfully!')
    } catch (error) {
      console.error('Error adding inventory item:', error)
      alert(`Error adding inventory item: ${error instanceof Error ? error.message : 'Please try again.'}`)
    }
  }

  const handleOpenAddModal = () => {
    setShowAddModal(true)
  }

  const handleViewItem = (item: InventoryItem) => {
    setViewingItem(item)
    setShowViewModal(true)
  }

  const handleEditItem = (item: InventoryItem) => {
    setEditingItem(item)
    setEditItem({
      name: item.name,
      sku: item.sku,
      category: item.category,
      supplier: item.supplier,
      currentStock: item.currentStock,
      minStock: item.minStock,
      maxStock: item.maxStock,
      unitPrice: item.unitPrice,
      location: item.location || '',
      conditionType: item.conditionType || 'NEW',
      warranty: item.warranty || ''
    })
    setShowEditModal(true)
  }

  const handleRestockItem = (item: InventoryItem) => {
    setSelectedItem(item)
    setRestockQuantity(0)
    setShowRestockModal(true)
  }

  const handleDeleteItem = async (item: InventoryItem) => {
    if (window.confirm(`Are you sure you want to delete ${item.name}?`)) {
      try {
        const token = localStorage.getItem('token')
        const response = await fetch(`/api/proxy?service=inventory&path=/api/v1/inventory/${item.id}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        })

        if (response.ok) {
          loadInventory()
          loadStats()
          alert('Item deleted successfully!')
        } else {
          alert('Failed to delete item')
        }
      } catch (error) {
        console.error('Error deleting item:', error)
        alert('Error deleting item')
      }
    }
  }

  const handleRestock = async () => {
    if (!selectedItem || restockQuantity <= 0) return

    try {
      const token = localStorage.getItem('token')
      const response = await fetch(`/api/proxy?service=inventory&path=/api/v1/inventory/${selectedItem.id}/restock?quantity=${restockQuantity}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        loadInventory()
        loadStats()
        setShowRestockModal(false)
        setSelectedItem(null)
        setRestockQuantity(0)
        alert('Item restocked successfully!')
      } else {
        alert('Failed to restock item')
      }
    } catch (error) {
      console.error('Error restocking item:', error)
      alert('Error restocking item')
    }
  }

  const handleExport = () => {
    const csvContent = [
      ['Name', 'SKU', 'Category', 'Current Stock', 'Min Stock', 'Max Stock', 'Unit Price', 'Total Value', 'Supplier', 'Status', 'Location'],
      ...filteredItems.map(item => [
        item.name,
        item.sku,
        formatCategoryName(item.category),
        item.currentStock.toString(),
        item.minStock.toString(),
        item.maxStock.toString(),
        item.unitPrice.toString(),
        item.totalValue.toString(),
        item.supplier,
        formatStatusName(item.status),
        item.location
      ])
    ].map(row => row.join(',')).join('\n')

    const blob = new Blob([csvContent], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'inventory-export.csv'
    a.click()
    window.URL.revokeObjectURL(url)
    
    alert('Inventory exported successfully!')
  }

  const handleSort = (field: string) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc')
    } else {
      setSortField(field)
      setSortDirection('asc')
    }
  }

  const handleRefresh = () => {
    loadInventory()
    loadStats()
  }

  const handleSubmitEditItem = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!editingItem) return

    try {
      const token = localStorage.getItem('token')
      
      // Prepare the item data for API
      const itemData = {
        name: editItem.name,
        sku: editItem.sku,
        category: editItem.category,
        supplier: editItem.supplier,
        currentStock: editItem.currentStock,
        minStock: editItem.minStock,
        maxStock: editItem.maxStock,
        unitPrice: editItem.unitPrice,
        location: editItem.location || null,
        condition: editItem.conditionType,
        warranty: editItem.warranty || null
      }

      console.log('🔄 Updating inventory item:', editingItem.id, itemData)

      // Call the API to update the item
      const response = await fetch(`/api/proxy?service=inventory&path=/api/v1/inventory/${editingItem.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(itemData)
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.message || 'Failed to update inventory item')
      }

      const updatedItem = await response.json()
      console.log('✅ Inventory item updated successfully:', updatedItem)

      // Update local state
      setInventoryItems(prev => prev.map(item => 
        item.id === editingItem.id ? updatedItem : item
      ))
      
      // Close modal and reset form
      setShowEditModal(false)
      setEditingItem(null)
      setEditItem({
        name: '',
        sku: '',
        category: '',
        supplier: '',
        currentStock: 0,
        minStock: 0,
        maxStock: 100,
        unitPrice: 0,
        location: '',
        conditionType: 'NEW',
        warranty: ''
      })
      
      // Reload stats
      loadStats()
      
      alert('Inventory item updated successfully!')
    } catch (error) {
      console.error('Error updating inventory item:', error)
      alert(`Error updating inventory item: ${error instanceof Error ? error.message : 'Please try again.'}`)
    }
  }

  const handleCloseEditModal = () => {
    setShowEditModal(false)
    setEditingItem(null)
    setEditItem({
      name: '',
      sku: '',
      category: '',
      supplier: '',
      currentStock: 0,
      minStock: 0,
      maxStock: 100,
      unitPrice: 0,
      location: '',
      conditionType: 'NEW',
      warranty: ''
    })
  }

  // Bulk Operations
  const handleBulkRestock = () => {
    if (selectedItems.length === 0) {
      alert('Please select items to restock')
      return
    }
    setBulkRestockQuantity(0)
    setShowBulkRestockModal(true)
  }

  const handleBulkStatusUpdate = () => {
    if (selectedItems.length === 0) {
      alert('Please select items to update status')
      return
    }
    setBulkStatus('IN_STOCK')
    setShowBulkStatusModal(true)
  }

  const handleBulkDelete = async () => {
    if (selectedItems.length === 0) {
      alert('Please select items to delete')
      return
    }

    if (!confirm(`Are you sure you want to delete ${selectedItems.length} selected items? This action cannot be undone.`)) {
      return
    }

    try {
      const token = localStorage.getItem('token')
      
      // Delete items one by one (in a real app, you'd have a bulk delete endpoint)
      const deletePromises = selectedItems.map(itemId => 
        fetch(`/api/proxy?service=inventory&path=/api/v1/inventory/${itemId}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
      )

      const results = await Promise.allSettled(deletePromises)
      const failed = results.filter(result => result.status === 'rejected' || !result.value.ok)
      
      if (failed.length > 0) {
        console.error('Some deletions failed:', failed)
        alert(`Deleted ${selectedItems.length - failed.length} items successfully. ${failed.length} items failed to delete.`)
      } else {
        alert(`Successfully deleted ${selectedItems.length} items`)
      }

      // Update local state
      setInventoryItems(prev => prev.filter(item => !selectedItems.includes(item.id)))
      setSelectedItems([])
      
      // Reload stats
      loadStats()
    } catch (error) {
      console.error('Error deleting items:', error)
      alert(`Error deleting items: ${error instanceof Error ? error.message : 'Please try again.'}`)
    }
  }

  const handleSubmitBulkRestock = async () => {
    if (bulkRestockQuantity <= 0) {
      alert('Please enter a valid restock quantity')
      return
    }

    try {
      const token = localStorage.getItem('token')
      
      // Restock items one by one
      const restockPromises = selectedItems.map(itemId => 
        fetch(`/api/proxy?service=inventory&path=/api/v1/inventory/${itemId}/restock?quantity=${bulkRestockQuantity}`, {
          method: 'PATCH',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
      )

      const results = await Promise.allSettled(restockPromises)
      const failed = results.filter(result => result.status === 'rejected' || !result.value.ok)
      
      if (failed.length > 0) {
        console.error('Some restocks failed:', failed)
        alert(`Restocked ${selectedItems.length - failed.length} items successfully. ${failed.length} items failed to restock.`)
      } else {
        alert(`Successfully restocked ${selectedItems.length} items with ${bulkRestockQuantity} units each`)
      }

      // Reload data
      loadInventory()
      loadStats()
      setSelectedItems([])
      setShowBulkRestockModal(false)
    } catch (error) {
      console.error('Error restocking items:', error)
      alert(`Error restocking items: ${error instanceof Error ? error.message : 'Please try again.'}`)
    }
  }

  const handleSubmitBulkStatusUpdate = async () => {
    try {
      const token = localStorage.getItem('token')
      
      // Update status for items one by one
      const statusUpdatePromises = selectedItems.map(itemId => 
        fetch(`/api/proxy?service=inventory&path=/api/v1/inventory/${itemId}/status?status=${bulkStatus}`, {
          method: 'PATCH',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
      )

      const results = await Promise.allSettled(statusUpdatePromises)
      const failed = results.filter(result => result.status === 'rejected' || !result.value.ok)
      
      if (failed.length > 0) {
        console.error('Some status updates failed:', failed)
        alert(`Updated status for ${selectedItems.length - failed.length} items successfully. ${failed.length} items failed to update.`)
      } else {
        alert(`Successfully updated status for ${selectedItems.length} items to ${bulkStatus}`)
      }

      // Reload data
      loadInventory()
      loadStats()
      setSelectedItems([])
      setShowBulkStatusModal(false)
    } catch (error) {
      console.error('Error updating item status:', error)
      alert(`Error updating item status: ${error instanceof Error ? error.message : 'Please try again.'}`)
    }
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
            <button 
              onClick={handleOpenAddModal}
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2"
            >
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
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.totalItems}</p>
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
                <p className="text-2xl font-bold text-gray-900 dark:text-white">₦{(stats.totalValue / 1000000).toFixed(1)}M</p>
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
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.lowStockItems}</p>
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
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.outOfStockItems}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Low Stock Alerts */}
        {lowStockAlerts.length > 0 && (
          <div className="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-4 mb-6">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center space-x-2">
                <svg className="h-5 w-5 text-yellow-600 dark:text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
                <h3 className="text-lg font-medium text-yellow-800 dark:text-yellow-200">
                  Low Stock Alerts ({lowStockAlerts.length})
                </h3>
              </div>
              <button
                onClick={() => setShowAlerts(!showAlerts)}
                className="text-yellow-600 dark:text-yellow-400 hover:text-yellow-800 dark:hover:text-yellow-200"
              >
                {showAlerts ? (
                  <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                ) : (
                  <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                )}
              </button>
            </div>
            
            {showAlerts && (
              <div className="space-y-2">
                {lowStockAlerts.map((item) => (
                  <div key={item.id} className="flex items-center justify-between bg-white dark:bg-gray-800 rounded-md p-3 border border-yellow-200 dark:border-yellow-700">
                    <div className="flex items-center space-x-3">
                      <div className="w-2 h-2 bg-yellow-500 rounded-full"></div>
                      <div>
                        <p className="text-sm font-medium text-gray-900 dark:text-white">{item.name}</p>
                        <p className="text-xs text-gray-500 dark:text-gray-400">
                          SKU: {item.sku} | Current: {item.currentStock} | Min: {item.minStock}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center space-x-2">
                      <span className="text-sm text-yellow-600 dark:text-yellow-400 font-medium">
                        {item.currentStock <= item.minStock ? 'Critical' : 'Low Stock'}
                      </span>
                      <button
                        onClick={() => handleRestockItem(item)}
                        className="text-xs bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-300 px-2 py-1 rounded hover:bg-yellow-200 dark:hover:bg-yellow-900/50"
                      >
                        Restock
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

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
                <option value="SMART_METERS">Smart Meters</option>
                <option value="SOLAR_INVERTERS">Solar Inverters</option>
                <option value="MONITORING_DEVICES">Monitoring Devices</option>
                <option value="SENSORS">Sensors</option>
                <option value="ACCESSORIES">Accessories</option>
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
                <option value="IN_STOCK">In Stock</option>
                <option value="LOW_STOCK">Low Stock</option>
                <option value="OUT_OF_STOCK">Out of Stock</option>
                <option value="DISCONTINUED">Discontinued</option>
              </select>
            </div>

            <div className="flex flex-wrap items-end gap-2">
              <button
                onClick={() => setShowAdvancedSearch(!showAdvancedSearch)}
                className="bg-gray-600 hover:bg-gray-700 text-white px-3 py-2 rounded-lg flex items-center space-x-1 text-sm"
              >
                <FunnelIcon className="h-4 w-4" />
                <span>Advanced</span>
              </button>
              <button
                onClick={handleExport}
                className="bg-green-600 hover:bg-green-700 text-white px-3 py-2 rounded-lg flex items-center space-x-1 text-sm"
              >
                <ArrowDownTrayIcon className="h-4 w-4" />
                <span>Export</span>
              </button>
              <button
                onClick={handleRefresh}
                className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded-lg flex items-center space-x-1 text-sm"
              >
                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                <span>Refresh</span>
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
                {loading ? (
                  <tr>
                    <td colSpan={9} className="px-6 py-12 text-center">
                      <div className="flex justify-center">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                      </div>
                      <p className="mt-2 text-gray-500 dark:text-gray-400">Loading inventory...</p>
                    </td>
                  </tr>
                ) : filteredItems.length === 0 ? (
                  <tr>
                    <td colSpan={9} className="px-6 py-12 text-center">
                      <div className="text-gray-500 dark:text-gray-400">
                        <CubeIcon className="mx-auto h-12 w-12 text-gray-400" />
                        <h3 className="mt-2 text-sm font-medium text-gray-900 dark:text-white">No inventory items</h3>
                        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                          Get started by adding a new inventory item.
                        </p>
                        <div className="mt-6">
                          <button
                            onClick={handleOpenAddModal}
                            className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                          >
                            <PlusIcon className="h-4 w-4 mr-2" />
                            Add Item
                          </button>
                        </div>
                      </div>
                    </td>
                  </tr>
                ) : (
                  filteredItems.map((item) => (
                  <tr key={item.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900 dark:text-white">{item.name}</div>
                        <div className="text-sm text-gray-500 dark:text-gray-400">SKU: {item.sku}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getCategoryColor(item.category)}`}>
                          {formatCategoryName(item.category)}
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
                          {formatStatusName(item.status)}
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
                        {item.location || 'N/A'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex space-x-2">
                          <button
                            onClick={() => handleViewItem(item)}
                            className="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300"
                            title="View Details"
                          >
                          <EyeIcon className="h-4 w-4" />
                        </button>
                          <button
                            onClick={() => handleEditItem(item)}
                            className="text-indigo-600 hover:text-indigo-900 dark:text-indigo-400 dark:hover:text-indigo-300"
                            title="Edit Item"
                          >
                          <PencilIcon className="h-4 w-4" />
                        </button>
                          <button
                            onClick={() => handleRestockItem(item)}
                            className="text-green-600 hover:text-green-900 dark:text-green-400 dark:hover:text-green-300"
                            title="Restock Item"
                          >
                            <TruckIcon className="h-4 w-4" />
                        </button>
                          <button
                            onClick={() => handleDeleteItem(item)}
                            className="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300"
                            title="Delete Item"
                          >
                          <TrashIcon className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Advanced Search Panel */}
        {showAdvancedSearch && (
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-900 dark:text-white">Advanced Search</h3>
              <button
                onClick={() => setShowAdvancedSearch(false)}
                className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
              >
                <XMarkIcon className="h-5 w-5" />
              </button>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Min Price (₦)</label>
                <input
                  type="number"
                  value={advancedFilters.minPrice}
                  onChange={(e) => setAdvancedFilters({...advancedFilters, minPrice: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  placeholder="0"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Max Price (₦)</label>
                <input
                  type="number"
                  value={advancedFilters.maxPrice}
                  onChange={(e) => setAdvancedFilters({...advancedFilters, maxPrice: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  placeholder="1000000"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Min Stock</label>
                <input
                  type="number"
                  value={advancedFilters.minStock}
                  onChange={(e) => setAdvancedFilters({...advancedFilters, minStock: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  placeholder="0"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Max Stock</label>
                <input
                  type="number"
                  value={advancedFilters.maxStock}
                  onChange={(e) => setAdvancedFilters({...advancedFilters, maxStock: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  placeholder="1000"
                />
              </div>
              <div className="sm:col-span-2">
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Supplier</label>
                <input
                  type="text"
                  value={advancedFilters.supplier}
                  onChange={(e) => setAdvancedFilters({...advancedFilters, supplier: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  placeholder="Search by supplier name"
                />
              </div>
            </div>
            <div className="mt-4 flex justify-end space-x-3">
              <button
                onClick={() => setAdvancedFilters({minPrice: '', maxPrice: '', minStock: '', maxStock: '', supplier: ''})}
                className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md"
              >
                Clear Filters
              </button>
              <button
                onClick={() => {
                  loadInventory()
                  setShowAdvancedSearch(false)
                }}
                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md"
              >
                Apply Filters
              </button>
            </div>
          </div>
        )}

        {/* Bulk Actions Toolbar */}
        {selectedItems.length > 0 && (
          <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4 mb-6">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between space-y-3 sm:space-y-0">
              <div className="flex items-center space-x-4">
                <span className="text-sm font-medium text-blue-700 dark:text-blue-300">
                  {selectedItems.length} item{selectedItems.length !== 1 ? 's' : ''} selected
                </span>
                <button
                  onClick={() => setSelectedItems([])}
                  className="text-sm text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-200"
                >
                  Clear Selection
                </button>
              </div>
              <div className="flex flex-wrap gap-2">
                <button
                  onClick={handleBulkRestock}
                  className="px-3 py-1 text-sm bg-green-100 dark:bg-green-900/20 text-green-700 dark:text-green-300 hover:bg-green-200 dark:hover:bg-green-900/40 rounded-md border border-green-200 dark:border-green-800"
                >
                  Bulk Restock
                </button>
                <button
                  onClick={handleBulkStatusUpdate}
                  className="px-3 py-1 text-sm bg-yellow-100 dark:bg-yellow-900/20 text-yellow-700 dark:text-yellow-300 hover:bg-yellow-200 dark:hover:bg-yellow-900/40 rounded-md border border-yellow-200 dark:border-yellow-800"
                >
                  Update Status
                </button>
                <button
                  onClick={handleBulkDelete}
                  className="px-3 py-1 text-sm bg-red-100 dark:bg-red-900/20 text-red-700 dark:text-red-300 hover:bg-red-200 dark:hover:bg-red-900/40 rounded-md border border-red-200 dark:border-red-800"
                >
                  Delete Selected
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Add Item Modal */}
        {showAddModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
              <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Add New Inventory Item</h3>
              </div>
              <form onSubmit={handleAddItem} className="p-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Item Name *
                    </label>
                    <input
                      type="text"
                      required
                      value={newItem.name}
                      onChange={(e) => setNewItem({...newItem, name: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="Enter item name"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      SKU *
                    </label>
                    <input
                      type="text"
                      required
                      value={newItem.sku}
                      onChange={(e) => setNewItem({...newItem, sku: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="Enter SKU"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Category *
                    </label>
                    <select
                      required
                      value={newItem.category}
                      onChange={(e) => setNewItem({...newItem, category: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    >
                      <option value="">Select Category</option>
                      <option value="SMART_METERS">Smart Meters</option>
                      <option value="SOLAR_INVERTERS">Solar Inverters</option>
                      <option value="MONITORING_DEVICES">Monitoring Devices</option>
                      <option value="SENSORS">Sensors</option>
                      <option value="ACCESSORIES">Accessories</option>
                    </select>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Supplier *
                    </label>
                    <input
                      type="text"
                      required
                      value={newItem.supplier}
                      onChange={(e) => setNewItem({...newItem, supplier: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="Enter supplier name"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Current Stock *
                    </label>
                    <input
                      type="number"
                      min="0"
                      required
                      value={newItem.currentStock}
                      onChange={(e) => setNewItem({...newItem, currentStock: parseInt(e.target.value) || 0})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="0"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Min Stock *
                    </label>
                    <input
                      type="number"
                      min="0"
                      required
                      value={newItem.minStock}
                      onChange={(e) => setNewItem({...newItem, minStock: parseInt(e.target.value) || 0})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="0"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Max Stock *
                    </label>
                    <input
                      type="number"
                      min="0"
                      required
                      value={newItem.maxStock}
                      onChange={(e) => setNewItem({...newItem, maxStock: parseInt(e.target.value) || 100})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="100"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Unit Price (₦) *
                    </label>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      required
                      value={newItem.unitPrice}
                      onChange={(e) => setNewItem({...newItem, unitPrice: parseFloat(e.target.value) || 0})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="0.00"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Location
                    </label>
                    <input
                      type="text"
                      value={newItem.location}
                      onChange={(e) => setNewItem({...newItem, location: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="Enter location"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Condition
                    </label>
                    <select 
                      value={newItem.conditionType}
                      onChange={(e) => setNewItem({...newItem, conditionType: e.target.value as 'NEW' | 'REFURBISHED' | 'USED'})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    >
                      <option value="NEW">New</option>
                      <option value="REFURBISHED">Refurbished</option>
                      <option value="USED">Used</option>
                    </select>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Warranty
                    </label>
                    <input
                      type="text"
                      value={newItem.warranty}
                      onChange={(e) => setNewItem({...newItem, warranty: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="e.g., 1 Year, 2 Years"
                    />
                  </div>
                </div>
                
                <div className="mt-6 flex justify-end space-x-3">
                  <button
                    type="button"
                    onClick={() => setShowAddModal(false)}
                    className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md"
                  >
                    Add Item
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* View Item Modal */}
        {showViewModal && viewingItem && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
              <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Item Details</h3>
              </div>
              <div className="p-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Name</label>
                    <p className="text-sm text-gray-900 dark:text-white">{viewingItem.name}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">SKU</label>
                    <p className="text-sm text-gray-900 dark:text-white">{viewingItem.sku}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Category</label>
                    <p className="text-sm text-gray-900 dark:text-white">{formatCategoryName(viewingItem.category)}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Status</label>
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(viewingItem.status)}`}>
                      {formatStatusName(viewingItem.status)}
                    </span>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Current Stock</label>
                    <p className="text-sm text-gray-900 dark:text-white">{viewingItem.currentStock}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Min Stock</label>
                    <p className="text-sm text-gray-900 dark:text-white">{viewingItem.minStock}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Max Stock</label>
                    <p className="text-sm text-gray-900 dark:text-white">{viewingItem.maxStock}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Unit Price</label>
                    <p className="text-sm text-gray-900 dark:text-white">₦{viewingItem.unitPrice.toLocaleString()}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Total Value</label>
                    <p className="text-sm text-gray-900 dark:text-white">₦{viewingItem.totalValue.toLocaleString()}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Supplier</label>
                    <p className="text-sm text-gray-900 dark:text-white">{viewingItem.supplier}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Location</label>
                    <p className="text-sm text-gray-900 dark:text-white">{viewingItem.location || 'N/A'}</p>
                  </div>
                </div>
                <div className="mt-6 flex justify-end">
                  <button
                    onClick={() => setShowViewModal(false)}
                    className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md"
                  >
                    Close
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Edit Item Modal */}
        {showEditModal && editingItem && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
              <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Edit Inventory Item</h3>
              </div>
              <form onSubmit={handleSubmitEditItem} className="p-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Item Name *
                    </label>
                    <input
                      type="text"
                      required
                      value={editItem.name}
                      onChange={(e) => setEditItem({...editItem, name: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="Enter item name"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      SKU *
                    </label>
                    <input
                      type="text"
                      required
                      value={editItem.sku}
                      onChange={(e) => setEditItem({...editItem, sku: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="Enter SKU"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Category *
                    </label>
                    <select
                      required
                      value={editItem.category}
                      onChange={(e) => setEditItem({...editItem, category: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    >
                      <option value="">Select Category</option>
                      <option value="SMART_METERS">Smart Meters</option>
                      <option value="SOLAR_INVERTERS">Solar Inverters</option>
                      <option value="MONITORING_DEVICES">Monitoring Devices</option>
                      <option value="SENSORS">Sensors</option>
                      <option value="ACCESSORIES">Accessories</option>
                    </select>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Supplier *
                    </label>
                    <input
                      type="text"
                      required
                      value={editItem.supplier}
                      onChange={(e) => setEditItem({...editItem, supplier: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="Enter supplier name"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Current Stock *
                    </label>
                    <input
                      type="number"
                      min="0"
                      required
                      value={editItem.currentStock}
                      onChange={(e) => setEditItem({...editItem, currentStock: parseInt(e.target.value) || 0})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="0"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Min Stock *
                    </label>
                    <input
                      type="number"
                      min="0"
                      required
                      value={editItem.minStock}
                      onChange={(e) => setEditItem({...editItem, minStock: parseInt(e.target.value) || 0})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="0"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Max Stock *
                    </label>
                    <input
                      type="number"
                      min="0"
                      required
                      value={editItem.maxStock}
                      onChange={(e) => setEditItem({...editItem, maxStock: parseInt(e.target.value) || 100})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="100"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Unit Price (₦) *
                    </label>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      required
                      value={editItem.unitPrice}
                      onChange={(e) => setEditItem({...editItem, unitPrice: parseFloat(e.target.value) || 0})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="0.00"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Location
                    </label>
                    <input
                      type="text"
                      value={editItem.location}
                      onChange={(e) => setEditItem({...editItem, location: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="Enter location"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Condition
                    </label>
                    <select 
                      value={editItem.conditionType}
                      onChange={(e) => setEditItem({...editItem, conditionType: e.target.value as 'NEW' | 'REFURBISHED' | 'USED'})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    >
                      <option value="NEW">New</option>
                      <option value="REFURBISHED">Refurbished</option>
                      <option value="USED">Used</option>
                    </select>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Warranty
                    </label>
                    <input
                      type="text"
                      value={editItem.warranty}
                      onChange={(e) => setEditItem({...editItem, warranty: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                      placeholder="e.g., 1 Year, 2 Years"
                    />
                  </div>
                </div>
                
                <div className="mt-6 flex justify-end space-x-3">
                  <button
                    type="button"
                    onClick={handleCloseEditModal}
                    className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md"
                  >
                    Update Item
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Restock Modal */}
        {showRestockModal && selectedItem && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full mx-4">
              <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Restock Item</h3>
              </div>
              <div className="p-6">
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Restocking: {selectedItem.name}
                  </label>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Current Stock: {selectedItem.currentStock}
                  </label>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Quantity to Add
                  </label>
                  <input
                    type="number"
                    min="1"
                    value={restockQuantity}
                    onChange={(e) => setRestockQuantity(parseInt(e.target.value) || 0)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter quantity"
                  />
                </div>
                <div className="flex justify-end space-x-3">
                  <button
                    onClick={() => {
                      setShowRestockModal(false)
                      setSelectedItem(null)
                      setRestockQuantity(0)
                    }}
                    className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleRestock}
                    disabled={restockQuantity <= 0}
                    className="px-4 py-2 text-sm font-medium text-white bg-green-600 hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-md"
                  >
                    Restock
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Bulk Restock Modal */}
        {showBulkRestockModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full mx-4">
              <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Bulk Restock</h3>
              </div>
              <div className="p-6">
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                  Add the same quantity to all {selectedItems.length} selected items.
                </p>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Restock Quantity
                  </label>
                  <input
                    type="number"
                    min="1"
                    value={bulkRestockQuantity}
                    onChange={(e) => setBulkRestockQuantity(parseInt(e.target.value) || 0)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter quantity"
                  />
                </div>
                <div className="flex justify-end space-x-3">
                  <button
                    onClick={() => setShowBulkRestockModal(false)}
                    className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleSubmitBulkRestock}
                    className="px-4 py-2 text-sm font-medium text-white bg-green-600 hover:bg-green-700 rounded-md"
                  >
                    Restock All
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Bulk Status Update Modal */}
        {showBulkStatusModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full mx-4">
              <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Bulk Status Update</h3>
              </div>
              <div className="p-6">
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                  Update the status of all {selectedItems.length} selected items.
                </p>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    New Status
                  </label>
                  <select
                    value={bulkStatus}
                    onChange={(e) => setBulkStatus(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  >
                    <option value="IN_STOCK">In Stock</option>
                    <option value="LOW_STOCK">Low Stock</option>
                    <option value="OUT_OF_STOCK">Out of Stock</option>
                    <option value="DISCONTINUED">Discontinued</option>
                  </select>
                </div>
                <div className="flex justify-end space-x-3">
                  <button
                    onClick={() => setShowBulkStatusModal(false)}
                    className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleSubmitBulkStatusUpdate}
                    className="px-4 py-2 text-sm font-medium text-white bg-yellow-600 hover:bg-yellow-700 rounded-md"
                  >
                    Update All
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </>
  )
}
