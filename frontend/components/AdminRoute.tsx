import React from 'react'
import { useAuth } from '../hooks/useAuth'
import { useRouter } from 'next/router'
import { useEffect } from 'react'

interface AdminRouteProps {
  children: React.ReactNode
}

export default function AdminRoute({ children }: AdminRouteProps) {
  const { user, loading } = useAuth()
  const router = useRouter()

  // Check if user is enterprise admin (only ROLE_ENTERPRISE_ADMIN has admin access)
  const isAdmin = user?.role === 'ROLE_ENTERPRISE_ADMIN'
  
  // Add debugging
  console.log('AdminRoute - loading:', loading, 'user:', user, 'isAdmin:', isAdmin)

  useEffect(() => {
    if (!loading) {
      if (!user) {
        // Not authenticated - redirect to login
        router.replace('/login')
      } else if (!isAdmin) {
        // Not admin - redirect to dashboard with error message
        router.replace('/dashboard?error=unauthorized')
      }
    }
  }, [user, loading, isAdmin]) // Removed router from dependencies to prevent re-runs

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-gray-100 dark:from-gray-900 dark:via-blue-900 dark:to-gray-800 flex items-center justify-center">
        <div className="relative">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="w-8 h-8 bg-blue-600 rounded-full animate-pulse"></div>
          </div>
        </div>
      </div>
    )
  }

  // Check authentication and admin access
  if (!user || !isAdmin) {
    return null // Will redirect
  }

  return <>{children}</>
}
