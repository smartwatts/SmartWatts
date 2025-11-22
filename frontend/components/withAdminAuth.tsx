import React from 'react'
import { useAuth } from '../hooks/useAuth'
import { useRouter } from 'next/router'
import { useEffect } from 'react'
import { isAdmin } from '../utils/roles'

// Higher-Order Component for Admin Authentication
export function withAdminAuth<P extends object>(
  WrappedComponent: React.ComponentType<P>
) {
  return function AdminAuthenticatedComponent(props: P) {
    const { user, loading } = useAuth()
    const router = useRouter()

    useEffect(() => {
      if (!loading) {
        if (!user) {
          // Not authenticated - redirect to login
          router.push('/login')
        } else if (!isAdmin(user.role)) {
          // Not admin - redirect to dashboard with error message
          router.push('/dashboard?error=unauthorized')
        }
      }
    }, [user, loading, router])

    if (loading) {
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

    // Check authentication and admin role (ROLE_ADMIN or ROLE_ENTERPRISE_ADMIN)
    if (!user || !isAdmin(user.role)) {
      return null // Will redirect
    }

    // User is authenticated and is admin - render the component
    return <WrappedComponent {...props} />
  }
}

// Alternative: Hook-based admin authentication
export function useAdminAuth() {
  const { user, loading } = useAuth()
  const router = useRouter()

  useEffect(() => {
    if (!loading) {
      if (!user) {
        router.push('/login')
      } else if (!isAdmin(user.role)) {
        router.push('/dashboard?error=unauthorized')
      }
    }
  }, [user, loading, router])

  return {
    user,
    loading,
    isAdmin: isAdmin(user?.role),
    isAuthenticated: !!user
  }
}
