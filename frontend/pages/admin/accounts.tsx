import React, { useState, useEffect } from 'react'
import Head from 'next/head'
import AdminRoute from '../../components/AdminRoute'
import Layout from '../../components/Layout'
import { useAuth } from '../../hooks/useAuth'
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
  const { user } = useAuth()
  const [searchTerm, setSearchTerm] = useState('')
  const [filterType, setFilterType] = useState('All')
  const [filterStatus, setFilterStatus] = useState('All')
  const [selectedAccount, setSelectedAccount] = useState<Account | null>(null)
  const [accounts, setAccounts] = useState<Account[]>([])
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState({
    totalAccounts: 0,
    activeAccounts: 0,
    totalRevenue: 0,
    totalDevices: 0,
    averageSavings: 0
  })
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [showViewModal, setShowViewModal] = useState(false)
  const [editingAccount, setEditingAccount] = useState<Account | null>(null)
  const [viewingAccount, setViewingAccount] = useState<Account | null>(null)
  const [newAccount, setNewAccount] = useState({
    name: '',
    type: 'Enterprise' as 'Enterprise' | 'SME' | 'Residential',
    status: 'Pending' as 'Active' | 'Suspended' | 'Pending' | 'Cancelled',
    contactPerson: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    state: '',
    subscriptionPlan: '',
    monthlyRevenue: 0,
    devices: 0,
    energySavings: 0
  })
  const [editAccount, setEditAccount] = useState({
    name: '',
    type: 'Enterprise' as 'Enterprise' | 'SME' | 'Residential',
    status: 'Pending' as 'Active' | 'Suspended' | 'Pending' | 'Cancelled',
    contactPerson: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    state: '',
    subscriptionPlan: '',
    monthlyRevenue: 0,
    devices: 0,
    energySavings: 0
  })
  const [selectedAccounts, setSelectedAccounts] = useState<string[]>([])
  const [sortField, setSortField] = useState('name')
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc')
  const [currentPage, setCurrentPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const [itemsPerPage] = useState(10)
  const [showAdvancedSearch, setShowAdvancedSearch] = useState(false)
  const [advancedFilters, setAdvancedFilters] = useState({
    revenueMin: '',
    revenueMax: '',
    devicesMin: '',
    devicesMax: '',
    savingsMin: '',
    savingsMax: '',
    createdFrom: '',
    createdTo: ''
  })

  // Load accounts data
  const loadAccounts = async () => {
    try {
      console.log('🔄 Loading accounts data...')
      setLoading(true)
      const token = localStorage.getItem('token')
      console.log('🔑 Token available:', !!token)
      
      // Load accounts
      console.log('📡 Fetching accounts from API...')
      const accountsResponse = await fetch('/api/proxy?service=user&path=/api/v1/accounts', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      console.log('📊 Accounts response status:', accountsResponse.status)
      
      if (accountsResponse.ok) {
        const accountsData = await accountsResponse.json()
        console.log('✅ Accounts data received:', accountsData)
        setAccounts(accountsData.content || [])
      } else {
        console.error('❌ Failed to load accounts:', accountsResponse.status, accountsResponse.statusText)
        setAccounts([])
      }
      
      // Load stats
      const statsResponse = await fetch('/api/proxy?service=user&path=/api/v1/accounts/stats', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (statsResponse.ok) {
        const statsData = await statsResponse.json()
        setStats(statsData)
      } else {
        console.error('Failed to load stats:', statsResponse.status, statsResponse.statusText)
      }
    } catch (error) {
      console.error('Error loading accounts:', error)
      setAccounts([])
    } finally {
      setLoading(false)
    }
  }

  // Search accounts
  const searchAccounts = async () => {
    try {
      setLoading(true)
      const token = localStorage.getItem('token')
      
      const queryParams = new URLSearchParams({
        query: searchTerm,
        ...(filterType !== 'All' && { type: filterType }),
        ...(filterStatus !== 'All' && { status: filterStatus }),
        ...(advancedFilters.revenueMin && { revenueMin: advancedFilters.revenueMin }),
        ...(advancedFilters.revenueMax && { revenueMax: advancedFilters.revenueMax }),
        ...(advancedFilters.devicesMin && { devicesMin: advancedFilters.devicesMin }),
        ...(advancedFilters.devicesMax && { devicesMax: advancedFilters.devicesMax }),
        ...(advancedFilters.savingsMin && { savingsMin: advancedFilters.savingsMin }),
        ...(advancedFilters.savingsMax && { savingsMax: advancedFilters.savingsMax }),
        ...(advancedFilters.createdFrom && { createdFrom: advancedFilters.createdFrom }),
        ...(advancedFilters.createdTo && { createdTo: advancedFilters.createdTo }),
        page: (currentPage - 1).toString(),
        size: itemsPerPage.toString(),
        sort: `${sortField},${sortDirection}`
      })
      
      const response = await fetch(`/api/proxy?service=user&path=/api/v1/accounts/search?${queryParams}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (response.ok) {
        const data = await response.json()
        setAccounts(data.content || [])
        setTotalPages(data.totalPages || 1)
        setStats({
          totalAccounts: data.totalElements || 0,
          activeAccounts: data.content?.filter((acc: Account) => acc.status === 'Active').length || 0,
          totalRevenue: data.content?.reduce((sum: number, acc: Account) => sum + acc.monthlyRevenue, 0) || 0,
          totalDevices: data.content?.reduce((sum: number, acc: Account) => sum + acc.devices, 0) || 0,
          averageSavings: data.content?.length > 0 ? data.content.reduce((sum: number, acc: Account) => sum + acc.energySavings, 0) / data.content.length : 0
        })
      } else {
        console.error('Failed to search accounts:', response.status, response.statusText)
        setAccounts([])
      }
    } catch (error) {
      console.error('Error searching accounts:', error)
      setAccounts([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    console.log('🔍 useEffect triggered - user:', user)
    if (user?.id) {
      console.log('👤 User ID available, loading accounts...')
      loadAccounts()
    } else {
      console.log('❌ No user ID available')
    }
  }, [user?.id])

  useEffect(() => {
    if (searchTerm || filterType !== 'All' || filterStatus !== 'All' || showAdvancedSearch) {
      searchAccounts()
    } else if (user?.id) {
      loadAccounts()
    }
  }, [searchTerm, filterType, filterStatus, showAdvancedSearch])

  useEffect(() => {
    if (user?.id) {
      searchAccounts()
    }
  }, [currentPage, sortField, sortDirection])

  // Use accounts directly since filtering is done on the backend
  const filteredAccounts = accounts

  // Use stats from API
  const totalRevenue = stats.totalRevenue
  const activeAccounts = stats.activeAccounts
  const totalDevices = stats.totalDevices
  const averageSavings = stats.averageSavings

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

  // Button handlers
  const handleAddAccount = () => {
    setShowAddModal(true)
  }

  const handleCloseAddModal = () => {
    setShowAddModal(false)
    setNewAccount({
      name: '',
      type: 'Enterprise',
      status: 'Pending',
      contactPerson: '',
      email: '',
      phone: '',
      address: '',
      city: '',
      state: '',
      subscriptionPlan: '',
      monthlyRevenue: 0,
      devices: 0,
      energySavings: 0
    })
  }

  const handleSubmitAddAccount = async (e: React.FormEvent) => {
    e.preventDefault()
    
    try {
      // Prepare the account data for API
      const accountData = {
        name: newAccount.name,
        type: newAccount.type.toUpperCase(), // Convert to uppercase for backend enum
        status: newAccount.status.toUpperCase(), // Convert to uppercase for backend enum
        contactPerson: newAccount.contactPerson,
        email: newAccount.email,
        phone: newAccount.phone || null,
        address: newAccount.address || null,
        city: newAccount.city || null,
        state: newAccount.state || null,
        subscriptionPlan: newAccount.subscriptionPlan || null,
        monthlyRevenue: newAccount.monthlyRevenue,
        devices: newAccount.devices,
        energySavings: newAccount.energySavings
      }

      console.log('🔄 Creating new account:', accountData)

      // Call the API to create the account
      const response = await fetch('/api/proxy?service=user&path=/api/v1/accounts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(accountData)
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.message || 'Failed to create account')
      }

      const createdAccount = await response.json()
      console.log('✅ Account created successfully:', createdAccount)

      // Add to local state
      setAccounts(prev => [createdAccount, ...prev])
      
      // Close modal and reset form
      handleCloseAddModal()
      
      alert('Account added successfully!')
    } catch (error) {
      console.error('Error adding account:', error)
      alert(`Error adding account: ${error instanceof Error ? error.message : 'Please try again.'}`)
    }
  }

  const handleExport = () => {
    // Export accounts data as CSV
    const csvContent = [
      ['Name', 'Type', 'Status', 'Contact Person', 'Email', 'Phone', 'Monthly Revenue', 'Devices', 'Energy Savings'],
      ...accounts.map(account => [
        account.name,
        account.type,
        account.status,
        account.contactPerson,
        account.email,
        account.phone,
        account.monthlyRevenue.toString(),
        account.devices.toString(),
        account.energySavings.toString()
      ])
    ].map(row => row.join(',')).join('\n')
    
    const blob = new Blob([csvContent], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `accounts-${new Date().toISOString().split('T')[0]}.csv`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    
    alert('Accounts exported successfully!')
  }

  const handleViewAccount = (account: Account) => {
    setViewingAccount(account)
    setShowViewModal(true)
  }

  const handleEditAccount = (account: Account) => {
    setEditingAccount(account)
    setEditAccount({
      name: account.name,
      type: account.type,
      status: account.status,
      contactPerson: account.contactPerson,
      email: account.email,
      phone: account.phone,
      address: account.address,
      city: account.city,
      state: account.state,
      subscriptionPlan: account.subscriptionPlan,
      monthlyRevenue: account.monthlyRevenue,
      devices: account.devices,
      energySavings: account.energySavings
    })
    setShowEditModal(true)
  }

  const handleCloseEditModal = () => {
    setShowEditModal(false)
    setEditingAccount(null)
    setEditAccount({
      name: '',
      type: 'Enterprise',
      status: 'Pending',
      contactPerson: '',
      email: '',
      phone: '',
      address: '',
      city: '',
      state: '',
      subscriptionPlan: '',
      monthlyRevenue: 0,
      devices: 0,
      energySavings: 0
    })
  }

  const handleCloseViewModal = () => {
    setShowViewModal(false)
    setViewingAccount(null)
  }

  const handleSubmitEditAccount = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!editingAccount) return
    
    try {
      const accountData = {
        name: editAccount.name,
        type: editAccount.type.toUpperCase(),
        status: editAccount.status.toUpperCase(),
        contactPerson: editAccount.contactPerson,
        email: editAccount.email,
        phone: editAccount.phone || null,
        address: editAccount.address || null,
        city: editAccount.city || null,
        state: editAccount.state || null,
        subscriptionPlan: editAccount.subscriptionPlan || null,
        monthlyRevenue: editAccount.monthlyRevenue,
        devices: editAccount.devices,
        energySavings: editAccount.energySavings
      }

      console.log('🔄 Updating account:', editingAccount.id, accountData)

      const response = await fetch(`/api/proxy?service=user&path=/api/v1/accounts/${editingAccount.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(accountData)
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.message || 'Failed to update account')
      }

      const updatedAccount = await response.json()
      console.log('✅ Account updated successfully:', updatedAccount)

      // Update local state
      setAccounts(prev => prev.map(acc => acc.id === editingAccount.id ? updatedAccount : acc))
      
      // Close modal and reset form
      handleCloseEditModal()
      
      alert('Account updated successfully!')
    } catch (error) {
      console.error('Error updating account:', error)
      alert(`Error updating account: ${error instanceof Error ? error.message : 'Please try again.'}`)
    }
  }

  const handleDeleteAccount = async (account: Account) => {
    if (window.confirm(`Are you sure you want to delete ${account.name}?`)) {
      try {
        const token = localStorage.getItem('token')
        const response = await fetch(`/api/proxy?service=user&path=/api/v1/accounts/${account.id}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        })
        
        if (response.ok) {
          console.log('Account deleted successfully')
          loadAccounts() // Reload accounts
        } else {
          console.error('Failed to delete account:', response.status, response.statusText)
        }
      } catch (error) {
        console.error('Error deleting account:', error)
      }
    }
  }

  // Bulk actions
  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedAccounts(filteredAccounts.map(acc => acc.id))
    } else {
      setSelectedAccounts([])
    }
  }

  const handleSelectAccount = (accountId: string, checked: boolean) => {
    if (checked) {
      setSelectedAccounts(prev => [...prev, accountId])
    } else {
      setSelectedAccounts(prev => prev.filter(id => id !== accountId))
    }
  }

  const handleBulkDelete = async () => {
    if (selectedAccounts.length === 0) return
    
    if (window.confirm(`Are you sure you want to delete ${selectedAccounts.length} account(s)?`)) {
      try {
        const token = localStorage.getItem('token')
        const deletePromises = selectedAccounts.map(id => 
          fetch(`/api/proxy?service=user&path=/api/v1/accounts/${id}`, {
            method: 'DELETE',
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            }
          })
        )
        
        await Promise.all(deletePromises)
        setAccounts(prev => prev.filter(acc => !selectedAccounts.includes(acc.id)))
        setSelectedAccounts([])
        alert(`${selectedAccounts.length} account(s) deleted successfully!`)
      } catch (error) {
        console.error('Error deleting accounts:', error)
        alert('Error deleting accounts')
      }
    }
  }

  const handleBulkStatusUpdate = async (newStatus: string) => {
    if (selectedAccounts.length === 0) return
    
    try {
      const token = localStorage.getItem('token')
      const updatePromises = selectedAccounts.map(id => 
        fetch(`/api/proxy?service=user&path=/api/v1/accounts/${id}`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ status: newStatus.toUpperCase() })
        })
      )
      
      await Promise.all(updatePromises)
      setAccounts(prev => prev.map(acc => 
        selectedAccounts.includes(acc.id) 
          ? { ...acc, status: newStatus as any }
          : acc
      ))
      setSelectedAccounts([])
      alert(`${selectedAccounts.length} account(s) status updated to ${newStatus}!`)
    } catch (error) {
      console.error('Error updating account status:', error)
      alert('Error updating account status')
    }
  }

  // Sorting
  const handleSort = (field: string) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc')
    } else {
      setSortField(field)
      setSortDirection('asc')
    }
  }

  // Refresh functionality
  const handleRefresh = () => {
    loadAccounts()
  }

  // Advanced search
  const handleAdvancedSearch = () => {
    searchAccounts()
  }

  const clearFilters = () => {
    setSearchTerm('')
    setFilterType('All')
    setFilterStatus('All')
    setAdvancedFilters({
      revenueMin: '',
      revenueMax: '',
      devicesMin: '',
      devicesMax: '',
      savingsMin: '',
      savingsMax: '',
      createdFrom: '',
      createdTo: ''
    })
    loadAccounts()
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
          <button 
            onClick={handleAddAccount}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2"
          >
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
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
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

            <div className="flex flex-wrap gap-2">
              <button 
                onClick={() => setShowAdvancedSearch(!showAdvancedSearch)}
                className="bg-gray-600 hover:bg-gray-700 text-white px-2 py-2 rounded-lg flex items-center space-x-1 text-xs sm:text-sm"
              >
                <FunnelIcon className="h-3 w-3 sm:h-4 sm:w-4" />
                <span className="hidden sm:inline">{showAdvancedSearch ? 'Hide' : 'Advanced'} Search</span>
                <span className="sm:hidden">{showAdvancedSearch ? 'Hide' : 'Advanced'}</span>
              </button>
              <button 
                onClick={handleRefresh}
                className="bg-blue-600 hover:bg-blue-700 text-white px-2 py-2 rounded-lg flex items-center space-x-1 text-xs sm:text-sm"
              >
                <svg className="h-3 w-3 sm:h-4 sm:w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                <span className="hidden sm:inline">Refresh</span>
              </button>
              <button 
                onClick={handleExport}
                className="bg-green-600 hover:bg-green-700 text-white px-2 py-2 rounded-lg flex items-center space-x-1 text-xs sm:text-sm"
              >
                <ArrowDownTrayIcon className="h-3 w-3 sm:h-4 sm:w-4" />
                <span>Export</span>
              </button>
            </div>
          </div>
        </div>

        {/* Bulk Actions Toolbar */}
        {selectedAccounts.length > 0 && (
          <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between space-y-3 sm:space-y-0">
              <div className="flex items-center space-x-4">
                <span className="text-sm font-medium text-blue-900 dark:text-blue-100">
                  {selectedAccounts.length} account(s) selected
                </span>
                <button
                  onClick={() => setSelectedAccounts([])}
                  className="text-sm text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
                >
                  Clear selection
                </button>
              </div>
              <div className="flex flex-col sm:flex-row items-stretch sm:items-center space-y-2 sm:space-y-0 sm:space-x-2">
                <select
                  onChange={(e) => handleBulkStatusUpdate(e.target.value)}
                  className="text-sm border border-blue-300 dark:border-blue-600 rounded px-2 py-1 bg-white dark:bg-gray-800 text-gray-900 dark:text-white"
                >
                  <option value="">Bulk Status Update</option>
                  <option value="Active">Set to Active</option>
                  <option value="Pending">Set to Pending</option>
                  <option value="Suspended">Set to Suspended</option>
                  <option value="Cancelled">Set to Cancelled</option>
                </select>
                <button
                  onClick={handleBulkDelete}
                  className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded text-sm"
                >
                  Delete Selected
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Advanced Search Panel */}
        {showAdvancedSearch && (
          <div className="bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-4">
            <div className="space-y-4">
              {/* Range Filters Row */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Revenue Range (₦)
                  </label>
                  <div className="flex space-x-2">
                    <input
                      type="number"
                      placeholder="Min"
                      value={advancedFilters.revenueMin}
                      onChange={(e) => setAdvancedFilters({...advancedFilters, revenueMin: e.target.value})}
                      className="flex-1 px-2 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white text-sm"
                    />
                    <input
                      type="number"
                      placeholder="Max"
                      value={advancedFilters.revenueMax}
                      onChange={(e) => setAdvancedFilters({...advancedFilters, revenueMax: e.target.value})}
                      className="flex-1 px-2 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white text-sm"
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Devices Range
                  </label>
                  <div className="flex space-x-2">
                    <input
                      type="number"
                      placeholder="Min"
                      value={advancedFilters.devicesMin}
                      onChange={(e) => setAdvancedFilters({...advancedFilters, devicesMin: e.target.value})}
                      className="flex-1 px-2 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white text-sm"
                    />
                    <input
                      type="number"
                      placeholder="Max"
                      value={advancedFilters.devicesMax}
                      onChange={(e) => setAdvancedFilters({...advancedFilters, devicesMax: e.target.value})}
                      className="flex-1 px-2 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white text-sm"
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Savings Range (%)
                  </label>
                  <div className="flex space-x-2">
                    <input
                      type="number"
                      placeholder="Min"
                      value={advancedFilters.savingsMin}
                      onChange={(e) => setAdvancedFilters({...advancedFilters, savingsMin: e.target.value})}
                      className="flex-1 px-2 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white text-sm"
                    />
                    <input
                      type="number"
                      placeholder="Max"
                      value={advancedFilters.savingsMax}
                      onChange={(e) => setAdvancedFilters({...advancedFilters, savingsMax: e.target.value})}
                      className="flex-1 px-2 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white text-sm"
                    />
                  </div>
                </div>
              </div>
              
              {/* Date Range Row - Moved Below */}
              <div className="max-w-md">
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Created Date Range
                </label>
                <div className="flex space-x-2">
                  <div className="flex-1">
                    <label className="block text-xs text-gray-500 dark:text-gray-400 mb-1">From</label>
                    <input
                      type="date"
                      value={advancedFilters.createdFrom}
                      onChange={(e) => setAdvancedFilters({...advancedFilters, createdFrom: e.target.value})}
                      className="w-full px-2 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white text-sm"
                    />
                  </div>
                  <div className="flex-1">
                    <label className="block text-xs text-gray-500 dark:text-gray-400 mb-1">To</label>
                    <input
                      type="date"
                      value={advancedFilters.createdTo}
                      onChange={(e) => setAdvancedFilters({...advancedFilters, createdTo: e.target.value})}
                      className="w-full px-2 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white text-sm"
                    />
                  </div>
                </div>
              </div>
            </div>
            
            <div className="flex justify-end space-x-2 mt-4">
              <button
                onClick={clearFilters}
                className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md"
              >
                Clear Filters
              </button>
              <button
                onClick={handleAdvancedSearch}
                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md"
              >
                Apply Filters
              </button>
            </div>
          </div>
        )}

        {/* Accounts Table */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center h-64">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <span className="ml-2 text-gray-600 dark:text-gray-400">Loading accounts...</span>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
              <thead className="bg-gray-50 dark:bg-gray-700">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                    <input
                      type="checkbox"
                      checked={selectedAccounts.length === filteredAccounts.length && filteredAccounts.length > 0}
                      onChange={(e) => handleSelectAll(e.target.checked)}
                      className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                    />
                  </th>
                  <th 
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-600"
                    onClick={() => handleSort('name')}
                  >
                    <div className="flex items-center space-x-1">
                      <span>Account</span>
                      {sortField === 'name' && (
                        <span>{sortDirection === 'asc' ? '↑' : '↓'}</span>
                      )}
                    </div>
                  </th>
                  <th 
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-600"
                    onClick={() => handleSort('type')}
                  >
                    <div className="flex items-center space-x-1">
                      <span>Type</span>
                      {sortField === 'type' && (
                        <span>{sortDirection === 'asc' ? '↑' : '↓'}</span>
                      )}
                    </div>
                  </th>
                  <th 
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-600"
                    onClick={() => handleSort('status')}
                  >
                    <div className="flex items-center space-x-1">
                      <span>Status</span>
                      {sortField === 'status' && (
                        <span>{sortDirection === 'asc' ? '↑' : '↓'}</span>
                      )}
                    </div>
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Contact</th>
                  <th 
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-600"
                    onClick={() => handleSort('monthlyRevenue')}
                  >
                    <div className="flex items-center space-x-1">
                      <span>Revenue</span>
                      {sortField === 'monthlyRevenue' && (
                        <span>{sortDirection === 'asc' ? '↑' : '↓'}</span>
                      )}
                    </div>
                  </th>
                  <th 
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-600"
                    onClick={() => handleSort('devices')}
                  >
                    <div className="flex items-center space-x-1">
                      <span>Devices</span>
                      {sortField === 'devices' && (
                        <span>{sortDirection === 'asc' ? '↑' : '↓'}</span>
                      )}
                    </div>
                  </th>
                  <th 
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-600"
                    onClick={() => handleSort('energySavings')}
                  >
                    <div className="flex items-center space-x-1">
                      <span>Savings</span>
                      {sortField === 'energySavings' && (
                        <span>{sortDirection === 'asc' ? '↑' : '↓'}</span>
                      )}
                    </div>
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                {filteredAccounts.map((account) => (
                  <tr key={account.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <input
                        type="checkbox"
                        checked={selectedAccounts.includes(account.id)}
                        onChange={(e) => handleSelectAccount(account.id, e.target.checked)}
                        className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                      />
                    </td>
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
                        <button 
                          onClick={() => handleViewAccount(account)}
                          className="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300"
                          title="View Account"
                        >
                          <EyeIcon className="h-4 w-4" />
                        </button>
                        <button 
                          onClick={() => handleEditAccount(account)}
                          className="text-indigo-600 hover:text-indigo-900 dark:text-indigo-400 dark:hover:text-indigo-300"
                          title="Edit Account"
                        >
                          <PencilIcon className="h-4 w-4" />
                        </button>
                        <button 
                          onClick={() => handleDeleteAccount(account)}
                          className="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300"
                          title="Delete Account"
                        >
                          <TrashIcon className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="bg-white dark:bg-gray-800 px-4 py-3 flex items-center justify-between border-t border-gray-200 dark:border-gray-700 sm:px-6">
            <div className="flex-1 flex justify-between sm:hidden">
              <button
                onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
                disabled={currentPage === 1}
                className="relative inline-flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 text-sm font-medium rounded-md text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <button
                onClick={() => setCurrentPage(Math.min(totalPages, currentPage + 1))}
                disabled={currentPage === totalPages}
                className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 text-sm font-medium rounded-md text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
            <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
              <div>
                <p className="text-sm text-gray-700 dark:text-gray-300">
                  Showing page <span className="font-medium">{currentPage}</span> of{' '}
                  <span className="font-medium">{totalPages}</span>
                </p>
              </div>
              <div>
                <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                  <button
                    onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
                    disabled={currentPage === 1}
                    className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 text-sm font-medium text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <span className="sr-only">Previous</span>
                    <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                  </button>
                  
                  {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                    const page = i + 1;
                    return (
                      <button
                        key={page}
                        onClick={() => setCurrentPage(page)}
                        className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                          currentPage === page
                            ? 'z-10 bg-blue-50 dark:bg-blue-900/20 border-blue-500 dark:border-blue-400 text-blue-600 dark:text-blue-400'
                            : 'bg-white dark:bg-gray-800 border-gray-300 dark:border-gray-600 text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700'
                        }`}
                      >
                        {page}
                      </button>
                    );
                  })}
                  
                  <button
                    onClick={() => setCurrentPage(Math.min(totalPages, currentPage + 1))}
                    disabled={currentPage === totalPages}
                    className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 text-sm font-medium text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <span className="sr-only">Next</span>
                    <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                    </svg>
                  </button>
                </nav>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Add Account Modal */}
      {showAddModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
            <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Add New Account</h3>
            </div>
            
            <form onSubmit={handleSubmitAddAccount} className="p-6 space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Account Name *
                  </label>
                  <input
                    type="text"
                    required
                    value={newAccount.name}
                    onChange={(e) => setNewAccount({...newAccount, name: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter account name"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Account Type *
                  </label>
                  <select
                    required
                    value={newAccount.type}
                    onChange={(e) => setNewAccount({...newAccount, type: e.target.value as 'Enterprise' | 'SME' | 'Residential'})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  >
                    <option value="Enterprise">Enterprise</option>
                    <option value="SME">SME</option>
                    <option value="Residential">Residential</option>
                  </select>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Status *
                  </label>
                  <select
                    required
                    value={newAccount.status}
                    onChange={(e) => setNewAccount({...newAccount, status: e.target.value as 'Active' | 'Suspended' | 'Pending' | 'Cancelled'})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  >
                    <option value="Pending">Pending</option>
                    <option value="Active">Active</option>
                    <option value="Suspended">Suspended</option>
                    <option value="Cancelled">Cancelled</option>
                  </select>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Contact Person *
                  </label>
                  <input
                    type="text"
                    required
                    value={newAccount.contactPerson}
                    onChange={(e) => setNewAccount({...newAccount, contactPerson: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter contact person name"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Email *
                  </label>
                  <input
                    type="email"
                    required
                    value={newAccount.email}
                    onChange={(e) => setNewAccount({...newAccount, email: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter email address"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Phone
                  </label>
                  <input
                    type="tel"
                    value={newAccount.phone}
                    onChange={(e) => setNewAccount({...newAccount, phone: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter phone number"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Address
                  </label>
                  <input
                    type="text"
                    value={newAccount.address}
                    onChange={(e) => setNewAccount({...newAccount, address: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter address"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    City
                  </label>
                  <input
                    type="text"
                    value={newAccount.city}
                    onChange={(e) => setNewAccount({...newAccount, city: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter city"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    State
                  </label>
                  <input
                    type="text"
                    value={newAccount.state}
                    onChange={(e) => setNewAccount({...newAccount, state: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter state"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Subscription Plan
                  </label>
                  <input
                    type="text"
                    value={newAccount.subscriptionPlan}
                    onChange={(e) => setNewAccount({...newAccount, subscriptionPlan: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter subscription plan"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Monthly Revenue (₦)
                  </label>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={newAccount.monthlyRevenue}
                    onChange={(e) => setNewAccount({...newAccount, monthlyRevenue: parseFloat(e.target.value) || 0})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="0.00"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Number of Devices
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={newAccount.devices}
                    onChange={(e) => setNewAccount({...newAccount, devices: parseInt(e.target.value) || 0})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="0"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Energy Savings (%)
                  </label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.1"
                    value={newAccount.energySavings}
                    onChange={(e) => setNewAccount({...newAccount, energySavings: parseFloat(e.target.value) || 0})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="0.0"
                  />
                </div>
              </div>
              
              <div className="flex justify-end space-x-3 pt-4">
                <button
                  type="button"
                  onClick={handleCloseAddModal}
                  className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  Add Account
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Account Modal */}
      {showEditModal && editingAccount && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
            <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Edit Account: {editingAccount.name}</h3>
            </div>
            
            <form onSubmit={handleSubmitEditAccount} className="p-6 space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Account Name *
                  </label>
                  <input
                    type="text"
                    required
                    value={editAccount.name}
                    onChange={(e) => setEditAccount({...editAccount, name: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter account name"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Account Type *
                  </label>
                  <select
                    required
                    value={editAccount.type}
                    onChange={(e) => setEditAccount({...editAccount, type: e.target.value as 'Enterprise' | 'SME' | 'Residential'})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  >
                    <option value="Enterprise">Enterprise</option>
                    <option value="SME">SME</option>
                    <option value="Residential">Residential</option>
                  </select>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Status *
                  </label>
                  <select
                    required
                    value={editAccount.status}
                    onChange={(e) => setEditAccount({...editAccount, status: e.target.value as 'Active' | 'Suspended' | 'Pending' | 'Cancelled'})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  >
                    <option value="Pending">Pending</option>
                    <option value="Active">Active</option>
                    <option value="Suspended">Suspended</option>
                    <option value="Cancelled">Cancelled</option>
                  </select>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Contact Person *
                  </label>
                  <input
                    type="text"
                    required
                    value={editAccount.contactPerson}
                    onChange={(e) => setEditAccount({...editAccount, contactPerson: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter contact person name"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Email *
                  </label>
                  <input
                    type="email"
                    required
                    value={editAccount.email}
                    onChange={(e) => setEditAccount({...editAccount, email: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter email address"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Phone
                  </label>
                  <input
                    type="tel"
                    value={editAccount.phone}
                    onChange={(e) => setEditAccount({...editAccount, phone: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter phone number"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Address
                  </label>
                  <input
                    type="text"
                    value={editAccount.address}
                    onChange={(e) => setEditAccount({...editAccount, address: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter address"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    City
                  </label>
                  <input
                    type="text"
                    value={editAccount.city}
                    onChange={(e) => setEditAccount({...editAccount, city: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter city"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    State
                  </label>
                  <input
                    type="text"
                    value={editAccount.state}
                    onChange={(e) => setEditAccount({...editAccount, state: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter state"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Subscription Plan
                  </label>
                  <input
                    type="text"
                    value={editAccount.subscriptionPlan}
                    onChange={(e) => setEditAccount({...editAccount, subscriptionPlan: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="Enter subscription plan"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Monthly Revenue (₦)
                  </label>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={editAccount.monthlyRevenue}
                    onChange={(e) => setEditAccount({...editAccount, monthlyRevenue: parseFloat(e.target.value) || 0})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="0.00"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Number of Devices
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={editAccount.devices}
                    onChange={(e) => setEditAccount({...editAccount, devices: parseInt(e.target.value) || 0})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="0"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Energy Savings (%)
                  </label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.1"
                    value={editAccount.energySavings}
                    onChange={(e) => setEditAccount({...editAccount, energySavings: parseFloat(e.target.value) || 0})}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    placeholder="0.0"
                  />
                </div>
              </div>
              
              <div className="flex justify-end space-x-3 pt-4">
                <button
                  type="button"
                  onClick={handleCloseEditModal}
                  className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  Update Account
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* View Account Modal */}
      {showViewModal && viewingAccount && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
            <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Account Details: {viewingAccount.name}</h3>
              <button
                onClick={handleCloseViewModal}
                className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
              >
                <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            
            <div className="p-6">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div className="space-y-4">
                  <div>
                    <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Basic Information</h4>
                    <div className="mt-2 space-y-2">
                      <div>
                        <span className="text-sm text-gray-600 dark:text-gray-400">Account Name:</span>
                        <p className="text-sm font-medium text-gray-900 dark:text-white">{viewingAccount.name}</p>
                      </div>
                      <div>
                        <span className="text-sm text-gray-600 dark:text-gray-400">Type:</span>
                        <span className={`ml-2 inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getTypeColor(viewingAccount.type)}`}>
                          {viewingAccount.type}
                        </span>
                      </div>
                      <div>
                        <span className="text-sm text-gray-600 dark:text-gray-400">Status:</span>
                        <span className={`ml-2 inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(viewingAccount.status)}`}>
                          {viewingAccount.status}
                        </span>
                      </div>
                      <div>
                        <span className="text-sm text-gray-600 dark:text-gray-400">Created:</span>
                        <p className="text-sm text-gray-900 dark:text-white">
                          {new Date(viewingAccount.createdAt).toLocaleDateString()}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="space-y-4">
                  <div>
                    <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Contact Information</h4>
                    <div className="mt-2 space-y-2">
                      <div>
                        <span className="text-sm text-gray-600 dark:text-gray-400">Contact Person:</span>
                        <p className="text-sm font-medium text-gray-900 dark:text-white">{viewingAccount.contactPerson}</p>
                      </div>
                      <div>
                        <span className="text-sm text-gray-600 dark:text-gray-400">Email:</span>
                        <p className="text-sm text-gray-900 dark:text-white">{viewingAccount.email}</p>
                      </div>
                      <div>
                        <span className="text-sm text-gray-600 dark:text-gray-400">Phone:</span>
                        <p className="text-sm text-gray-900 dark:text-white">{viewingAccount.phone || 'Not provided'}</p>
                      </div>
                      <div>
                        <span className="text-sm text-gray-600 dark:text-gray-400">Location:</span>
                        <p className="text-sm text-gray-900 dark:text-white">
                          {viewingAccount.city}, {viewingAccount.state}
                        </p>
                      </div>
                      {viewingAccount.address && (
                        <div>
                          <span className="text-sm text-gray-600 dark:text-gray-400">Address:</span>
                          <p className="text-sm text-gray-900 dark:text-white">{viewingAccount.address}</p>
                        </div>
                      )}
                    </div>
                  </div>
                </div>

                <div className="space-y-4">
                  <div>
                    <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Business Information</h4>
                    <div className="mt-2 space-y-2">
                      <div>
                        <span className="text-sm text-gray-600 dark:text-gray-400">Subscription Plan:</span>
                        <p className="text-sm text-gray-900 dark:text-white">{viewingAccount.subscriptionPlan || 'Not specified'}</p>
                      </div>
                      <div>
                        <span className="text-sm text-gray-600 dark:text-gray-400">Monthly Revenue:</span>
                        <p className="text-sm font-medium text-gray-900 dark:text-white">₦{viewingAccount.monthlyRevenue.toLocaleString()}</p>
                      </div>
                      <div>
                        <span className="text-sm text-gray-600 dark:text-gray-400">Number of Devices:</span>
                        <p className="text-sm text-gray-900 dark:text-white">{viewingAccount.devices}</p>
                      </div>
                      <div>
                        <span className="text-sm text-gray-600 dark:text-gray-400">Energy Savings:</span>
                        <p className="text-sm text-gray-900 dark:text-white">{viewingAccount.energySavings}%</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div className="mt-6 flex justify-end space-x-3">
                <button
                  onClick={handleCloseViewModal}
                  className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500"
                >
                  Close
                </button>
                <button
                  onClick={() => {
                    handleCloseViewModal()
                    handleEditAccount(viewingAccount)
                  }}
                  className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  Edit Account
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
