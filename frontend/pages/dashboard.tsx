import React, { useEffect, useState } from 'react'
import { useRouter } from 'next/router'
import Layout from '../components/Layout'
import ProtectedRoute from '../components/ProtectedRoute'
import DashboardClient from '../components/DashboardClient'
import DashboardModeSelector, { useDashboardMode } from '../components/DashboardModeSelector'
import { useAuth } from '../hooks/useAuth'
import { isAdmin } from '../utils/roles'
import Head from 'next/head'

export default function Dashboard() {
  const { user } = useAuth()
  const router = useRouter()
  const { currentMode, changeMode, getCurrentModeInfo } = useDashboardMode()
  const [showModeSelector, setShowModeSelector] = useState(false)

  useEffect(() => {
    // Redirect admins (ROLE_ADMIN or ROLE_ENTERPRISE_ADMIN) to admin dashboard
    if (isAdmin(user?.role)) {
      router.push('/admin/dashboard')
    }
  }, [user, router])

  // Don't render customer dashboard for admins
  if (isAdmin(user?.role)) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600 dark:text-gray-400">Redirecting to admin dashboard...</p>
        </div>
      </div>
    )
  }

  // Show mode selector if no specific mode is selected
  if (showModeSelector || !currentMode) {
    return (
      <ProtectedRoute>
        <Layout>
          <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center p-6">
            <DashboardModeSelector
              currentMode={currentMode}
              onModeChange={(mode) => {
                changeMode(mode)
                setShowModeSelector(false)
              }}
              showDescription={true}
            />
          </div>
        </Layout>
      </ProtectedRoute>
    )
  }

  return (
    <>
      <Head>
        {/* Prevent browser caching of this page */}
        <meta httpEquiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
        <meta httpEquiv="Pragma" content="no-cache" />
        <meta httpEquiv="Expires" content="0" />
        <title>Dashboard - SmartWatts</title>
      </Head>
      <ProtectedRoute>
        <Layout>
          <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
            {/* Mode Switcher */}
            <div className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 p-4">
              <div className="max-w-7xl mx-auto flex items-center justify-between">
                <div className="flex items-center space-x-4">
                  <h1 className="text-xl font-semibold text-gray-900 dark:text-white">
                    {getCurrentModeInfo()?.name} Dashboard
                  </h1>
                  <span className="text-sm text-gray-600 dark:text-gray-400">
                    {getCurrentModeInfo()?.description}
                  </span>
                </div>
                <button
                  onClick={() => setShowModeSelector(true)}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors duration-200"
                >
                  Switch Mode
                </button>
              </div>
            </div>
            
            {/* Dashboard Content */}
            <DashboardClient />
          </div>
        </Layout>
      </ProtectedRoute>
    </>
  )
}