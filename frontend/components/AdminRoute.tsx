import React, { useEffect, useState } from 'react'
import { useAuth } from '../hooks/useAuth'
import { useRouter } from 'next/router'
import { isAdmin } from '../utils/roles'

interface AdminRouteProps {
  children: React.ReactNode
}

export default function AdminRoute({ children }: AdminRouteProps) {
  const { user, loading } = useAuth()
  const router = useRouter()
  const [isRedirecting, setIsRedirecting] = useState(false)

  // Check if user has admin privileges (ROLE_ADMIN or ROLE_ENTERPRISE_ADMIN)
  const hasAdminAccess = isAdmin(user?.role)

  useEffect(() => {
    // Only redirect if not loading and user is determined and not already redirecting
    if (!loading && !isRedirecting) {
      if (!user) {
        setIsRedirecting(true)
        const replacePromise = router.replace('/login')
        if (replacePromise && typeof replacePromise.finally === 'function') {
          replacePromise.finally(() => setIsRedirecting(false))
        } else {
          // Fallback if finally is not available
          Promise.resolve(replacePromise).then(() => setIsRedirecting(false)).catch(() => setIsRedirecting(false))
        }
        return
      }
      if (!hasAdminAccess) {
        setIsRedirecting(true)
        const replacePromise = router.replace('/dashboard?error=unauthorized')
        if (replacePromise && typeof replacePromise.finally === 'function') {
          replacePromise.finally(() => setIsRedirecting(false))
        } else {
          // Fallback if finally is not available
          Promise.resolve(replacePromise).then(() => setIsRedirecting(false)).catch(() => setIsRedirecting(false))
        }
        return
      }
    }
  }, [user, loading, hasAdminAccess, router, isRedirecting])

  // Show loading state during authentication check or redirecting
  if (loading || isRedirecting) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-gray-100 dark:from-gray-900 dark:via-blue-900 dark:to-gray-800 flex items-center justify-center" role="status" aria-live="polite" aria-busy="true">
        <div className="relative">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600" aria-hidden="true"></div>
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="w-8 h-8 bg-blue-600 rounded-full animate-pulse" aria-hidden="true"></div>
          </div>
          <span className="sr-only">Loading...</span>
        </div>
      </div>
    )
  }

  // Check authentication and admin access
  if (!user) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-gray-100 dark:from-gray-900 dark:via-blue-900 dark:to-gray-800 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600 dark:text-gray-400">Redirecting to login...</p>
        </div>
      </div>
    )
  }

  if (!hasAdminAccess) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-gray-100 dark:from-gray-900 dark:via-blue-900 dark:to-gray-800 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600 dark:text-gray-400">Redirecting to dashboard...</p>
        </div>
      </div>
    )
  }

  return <>{children}</>
}
