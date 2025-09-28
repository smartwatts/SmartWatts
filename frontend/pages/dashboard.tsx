import React, { useEffect } from 'react'
import { useRouter } from 'next/router'
import Layout from '../components/Layout'
import ProtectedRoute from '../components/ProtectedRoute'
import DashboardClient from '../components/DashboardClient'
import { useAuth } from '../hooks/useAuth'
import Head from 'next/head'

export default function Dashboard() {
  const { user } = useAuth()
  const router = useRouter()

  useEffect(() => {
    // Redirect super admins to admin dashboard
    if (user?.role === 'ROLE_ENTERPRISE_ADMIN') {
      router.push('/admin/dashboard')
    }
  }, [user, router])

  // Don't render customer dashboard for super admins
  if (user?.role === 'ROLE_ENTERPRISE_ADMIN') {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600 dark:text-gray-400">Redirecting to admin dashboard...</p>
        </div>
      </div>
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
          <DashboardClient />
        </Layout>
      </ProtectedRoute>
    </>
  )
}