import React, { useEffect, useState } from 'react'
import Layout from '../../components/Layout'
import {
  CheckCircleIcon,
  XCircleIcon,
  ClockIcon,
  ExclamationTriangleIcon,
  EyeIcon,
  CheckIcon,
  XMarkIcon,
  ClockIcon as ClockIconSolid,
  ShieldCheckIcon,
  DocumentTextIcon,
  UserIcon,
  CalendarIcon,
  DeviceTabletIcon,
} from '@heroicons/react/24/outline'
import { ChartBarIcon } from '@heroicons/react/24/solid'

interface DeviceVerification {
  id: string
  deviceId: string
  deviceName: string
  deviceType: string
  manufacturer: string
  model: string
  userId: string
  userName: string
  userEmail: string
  verificationStatus: 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'SUSPENDED'
  trustLevel: 'OEM_LOCKED' | 'UNVERIFIED'
  verificationNotes?: string
  samplePayload?: string
  verificationRequestDate: string
  verificationReviewDate?: string
  verificationReviewer?: string
  location: string
  notes?: string
}

interface VerificationStats {
  totalPending: number
  totalUnderReview: number
  totalApproved: number
  totalRejected: number
  totalSuspended: number
  totalOEM: number
  totalThirdParty: number
}

export default function AdminVerification() {
  const [verifications, setVerifications] = useState<DeviceVerification[]>([])
  const [stats, setStats] = useState<VerificationStats>({
    totalPending: 0,
    totalUnderReview: 0,
    totalApproved: 0,
    totalRejected: 0,
    totalSuspended: 0,
    totalOEM: 0,
    totalThirdParty: 0
  })
  const [loading, setLoading] = useState(true)
  const [selectedVerification, setSelectedVerification] = useState<DeviceVerification | null>(null)
  const [reviewModalOpen, setReviewModalOpen] = useState(false)
  const [reviewNotes, setReviewNotes] = useState('')
  const [reviewStatus, setReviewStatus] = useState<'APPROVED' | 'REJECTED' | 'SUSPENDED'>('APPROVED')
  const [filterStatus, setFilterStatus] = useState<string>('ALL')
  const [searchTerm, setSearchTerm] = useState('')

  useEffect(() => {
    loadVerifications()
    loadStats()
  }, [])

  const loadVerifications = async () => {
    try {
      // Load real data from API
      const response = await fetch('/api/proxy?service=device-verification&endpoint=/api/v1/device-verifications')
      if (!response.ok) {
        throw new Error('Failed to load verifications')
      }
      const verifications: DeviceVerification[] = await response.json()

      setVerifications(verifications)
      setLoading(false)
    } catch (error) {
      console.error('Error loading verifications:', error)
      setLoading(false)
    }
  }

  const loadStats = async () => {
    try {
      // Load real stats from API
      const response = await fetch('/api/proxy?service=device-verification&endpoint=/api/v1/device-verifications/stats')
      if (!response.ok) {
        throw new Error('Failed to load stats')
      }
      const stats: VerificationStats = await response.json()
      setStats(stats)
    } catch (error) {
      console.error('Error loading stats:', error)
    }
  }

  const handleReview = (verification: DeviceVerification) => {
    setSelectedVerification(verification)
    setReviewModalOpen(true)
    setReviewNotes(verification.verificationNotes || '')
    setReviewStatus(verification.verificationStatus === 'APPROVED' ? 'APPROVED' : 
                   verification.verificationStatus === 'REJECTED' ? 'REJECTED' : 'SUSPENDED')
  }

  const submitReview = async () => {
    if (!selectedVerification) return

    try {
      // Submit review to API
      const response = await fetch(`/api/proxy?service=device-verification&endpoint=/api/v1/device-verifications/${selectedVerification.id}/review`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          status: reviewStatus,
          notes: reviewNotes
        })
      })

      if (!response.ok) {
        throw new Error('Failed to submit review')
      }

      // Reload data
      await loadVerifications()
      await loadStats()

      setReviewModalOpen(false)
      setSelectedVerification(null)
      setReviewNotes('')
    } catch (error) {
      console.error('Error submitting review:', error)
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'APPROVED':
        return <CheckCircleIcon className="h-5 w-5 text-green-500" />
      case 'REJECTED':
        return <XCircleIcon className="h-5 w-5 text-red-500" />
      case 'PENDING':
        return <ClockIcon className="h-5 w-5 text-yellow-500" />
      case 'UNDER_REVIEW':
        return <ClockIconSolid className="h-5 w-5 text-blue-500" />
      case 'SUSPENDED':
        return <ExclamationTriangleIcon className="h-5 w-5 text-orange-500" />
      default:
        return <ClockIcon className="h-5 w-5 text-gray-500" />
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'APPROVED':
        return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'REJECTED':
        return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
      case 'UNDER_REVIEW':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
      case 'SUSPENDED':
        return 'bg-orange-100 text-orange-800 dark:bg-orange-900/20 dark:text-orange-400'
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getTrustLevelColor = (trustLevel: string) => {
    switch (trustLevel) {
      case 'OEM_LOCKED':
        return 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400'
      case 'UNVERIFIED':
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const filteredVerifications = verifications.filter(verification => {
    const matchesStatus = filterStatus === 'ALL' || verification.verificationStatus === filterStatus
    const matchesSearch = verification.deviceName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         verification.deviceId.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         verification.userName.toLowerCase().includes(searchTerm.toLowerCase())
    return matchesStatus && matchesSearch
  })

  if (loading) {
    return (
      
        <Layout>
          <div className="flex items-center justify-center h-64">
            <div className="relative">
              <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
              <div className="absolute inset-0 flex items-center justify-center">
                <ShieldCheckIcon className="h-8 w-8 text-blue-600 animate-pulse" />
              </div>
            </div>
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
                <h1 className="text-3xl font-bold mb-2">Device Verification Dashboard</h1>
                <p className="text-blue-200 text-lg">Review and manage device verification requests</p>
                <div className="flex items-center mt-4 space-x-6">
                  <div className="flex items-center space-x-2">
                    <ClockIcon className="h-5 w-5 text-yellow-300" />
                    <span className="text-sm">{stats.totalPending} Pending</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <ClockIconSolid className="h-5 w-5 text-blue-300" />
                    <span className="text-sm">{stats.totalUnderReview} Under Review</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <ShieldCheckIcon className="h-5 w-5 text-green-300" />
                    <span className="text-sm">{stats.totalApproved} Approved</span>
                  </div>
                </div>
              </div>
              <div className="hidden md:block">
                <div className="w-24 h-24 bg-white/10 rounded-full flex items-center justify-center">
                  <ShieldCheckIcon className="h-12 w-12 text-white" />
                </div>
              </div>
            </div>
          </div>

          {/* Statistics Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Pending Review</p>
                  <p className="text-2xl font-bold text-yellow-600 dark:text-yellow-400">{stats.totalPending}</p>
                </div>
                <div className="w-12 h-12 bg-yellow-100 dark:bg-yellow-900/20 rounded-xl flex items-center justify-center">
                  <ClockIcon className="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
                </div>
              </div>
            </div>

            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Under Review</p>
                  <p className="text-2xl font-bold text-blue-600 dark:text-blue-400">{stats.totalUnderReview}</p>
                </div>
                <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/20 rounded-xl flex items-center justify-center">
                  <ClockIconSolid className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                </div>
              </div>
            </div>

            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Approved</p>
                  <p className="text-2xl font-bold text-green-600 dark:text-green-400">{stats.totalApproved}</p>
                </div>
                <div className="w-12 h-12 bg-green-100 dark:bg-green-900/20 rounded-xl flex items-center justify-center">
                  <CheckCircleIcon className="h-6 w-6 text-green-600 dark:text-green-400" />
                </div>
              </div>
            </div>

            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">OEM Devices</p>
                  <p className="text-2xl font-bold text-purple-600 dark:text-purple-400">{stats.totalOEM}</p>
                </div>
                <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/20 rounded-xl flex items-center justify-center">
                  <DeviceTabletIcon className="h-6 w-6 text-purple-600 dark:text-purple-400" />
                </div>
              </div>
            </div>
          </div>

          {/* Filters and Search */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
            <div className="flex flex-col md:flex-row gap-4">
              <div className="flex-1">
                <label htmlFor="search" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Search Devices
                </label>
                <input
                  type="text"
                  id="search"
                  placeholder="Search by device name, ID, or user..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                />
              </div>
              <div>
                <label htmlFor="status-filter" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Filter by Status
                </label>
                <select
                  id="status-filter"
                  value={filterStatus}
                  onChange={(e) => setFilterStatus(e.target.value)}
                  className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                >
                  <option value="ALL">All Statuses</option>
                  <option value="PENDING">Pending</option>
                  <option value="UNDER_REVIEW">Under Review</option>
                  <option value="APPROVED">Approved</option>
                  <option value="REJECTED">Rejected</option>
                  <option value="SUSPENDED">Suspended</option>
                </select>
              </div>
            </div>
          </div>

          {/* Verification Requests Table */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                Device Verification Requests ({filteredVerifications.length})
              </h3>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead className="bg-gray-50 dark:bg-gray-700">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                      Device
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                      User
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                      Trust Level
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                      Request Date
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                  {filteredVerifications.map((verification) => (
                    <tr key={verification.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div>
                          <div className="text-sm font-medium text-gray-900 dark:text-white">
                            {verification.deviceName}
                          </div>
                          <div className="text-sm text-gray-500 dark:text-gray-400">
                            {verification.deviceId} • {verification.manufacturer} {verification.model}
                          </div>
                          <div className="text-xs text-gray-400 dark:text-gray-500">
                            {verification.location}
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="w-8 h-8 bg-blue-100 dark:bg-blue-900/20 rounded-full flex items-center justify-center">
                            <UserIcon className="h-4 w-4 text-blue-600 dark:text-blue-400" />
                          </div>
                          <div className="ml-3">
                            <div className="text-sm font-medium text-gray-900 dark:text-white">
                              {verification.userName}
                            </div>
                            <div className="text-sm text-gray-500 dark:text-gray-400">
                              {verification.userEmail}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          {getStatusIcon(verification.verificationStatus)}
                          <span className={`ml-2 px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(verification.verificationStatus)}`}>
                            {verification.verificationStatus.replace('_', ' ')}
                          </span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-2 py-1 text-xs font-medium rounded-full ${getTrustLevelColor(verification.trustLevel)}`}>
                          {verification.trustLevel.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                        <div className="flex items-center">
                          <CalendarIcon className="h-4 w-4 mr-1" />
                          {new Date(verification.verificationRequestDate).toLocaleDateString()}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <div className="flex space-x-2">
                          <button
                            onClick={() => handleReview(verification)}
                            className="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300"
                          >
                            <EyeIcon className="h-5 w-5" />
                          </button>
                          {verification.verificationStatus === 'PENDING' && (
                            <button
                              onClick={() => handleReview(verification)}
                              className="text-green-600 hover:text-green-900 dark:text-green-400 dark:hover:text-green-300"
                            >
                              <CheckIcon className="h-5 w-5" />
                            </button>
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

        {/* Review Modal */}
        {reviewModalOpen && selectedVerification && (
          <div className="fixed inset-0 z-50 overflow-y-auto">
            <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
              <div className="fixed inset-0 transition-opacity" aria-hidden="true">
                <div className="absolute inset-0 bg-gray-500 opacity-75"></div>
              </div>

              <div className="inline-block align-bottom bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-2xl sm:w-full">
                <div className="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                  <div className="sm:flex sm:items-start">
                    <div className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-blue-100 dark:bg-blue-900/20 sm:mx-0 sm:h-10 sm:w-10">
                      <ShieldCheckIcon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                    </div>
                    <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left w-full">
                      <h3 className="text-lg leading-6 font-medium text-gray-900 dark:text-white">
                        Review Device Verification
                      </h3>
                      <div className="mt-2">
                        <div className="mb-4">
                          <h4 className="font-medium text-gray-900 dark:text-white mb-2">
                            {selectedVerification.deviceName}
                          </h4>
                          <div className="text-sm text-gray-500 dark:text-gray-400 space-y-1">
                            <p><strong>Device ID:</strong> {selectedVerification.deviceId}</p>
                            <p><strong>Manufacturer:</strong> {selectedVerification.manufacturer} {selectedVerification.model}</p>
                            <p><strong>User:</strong> {selectedVerification.userName} ({selectedVerification.userEmail})</p>
                            <p><strong>Location:</strong> {selectedVerification.location}</p>
                            <p><strong>Current Status:</strong> {selectedVerification.verificationStatus}</p>
                          </div>
                        </div>

                        {selectedVerification.samplePayload && (
                          <div className="mb-4">
                            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                              Sample Payload
                            </label>
                            <div className="bg-gray-50 dark:bg-gray-700 p-3 rounded-lg">
                              <pre className="text-xs text-gray-800 dark:text-gray-200 overflow-x-auto">
                                {selectedVerification.samplePayload}
                              </pre>
                            </div>
                          </div>
                        )}

                        <div className="mb-4">
                          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Verification Decision
                          </label>
                          <select
                            value={reviewStatus}
                            onChange={(e) => setReviewStatus(e.target.value as 'APPROVED' | 'REJECTED' | 'SUSPENDED')}
                            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                          >
                            <option value="APPROVED">Approve</option>
                            <option value="REJECTED">Reject</option>
                            <option value="SUSPENDED">Suspend</option>
                          </select>
                        </div>

                        <div className="mb-4">
                          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Review Notes
                          </label>
                          <textarea
                            value={reviewNotes}
                            onChange={(e) => setReviewNotes(e.target.value)}
                            rows={4}
                            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                            placeholder="Enter your review notes..."
                          />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="bg-gray-50 dark:bg-gray-700 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                  <button
                    type="button"
                    onClick={submitReview}
                    className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-blue-600 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:ml-3 sm:w-auto sm:text-sm"
                  >
                    Submit Review
                  </button>
                  <button
                    type="button"
                    onClick={() => setReviewModalOpen(false)}
                    className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 dark:border-gray-600 shadow-sm px-4 py-2 bg-white dark:bg-gray-800 text-base font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </Layout>
    
  )
}
