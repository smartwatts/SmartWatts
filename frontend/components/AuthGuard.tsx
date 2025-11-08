import React, { useEffect, useState } from 'react'
import { useRouter } from 'next/router'
import { useAuth } from '../hooks/useAuth'

interface AuthGuardProps {
  children: React.ReactNode
}

export default function AuthGuard({ children }: AuthGuardProps) {
  const { user, loading } = useAuth()
  const router = useRouter()
  const [isAuthorized, setIsAuthorized] = useState(false)

  // Prevent browser caching and ensure fresh authentication check
  useEffect(() => {
    // Clear any cached authentication state
    setIsAuthorized(false)
    
    // Force fresh authentication check on every mount
    const checkAuthImmediately = () => {
      const token = localStorage.getItem('token')
      
      if (!token || token.length < 10) {
        localStorage.removeItem('token')
        router.replace('/login')
        return
      }

      // Check user authentication
      if (!loading) {
        if (!user) {
          localStorage.removeItem('token')
          router.replace('/login')
          return
        }
        
        // User exists and token is valid
        setIsAuthorized(true)
      }
    }

    // Run immediate check
    checkAuthImmediately()

    // Also listen for route changes to re-check authentication
    const handleRouteChange = () => {
      checkAuthImmediately()
    }

    router.events.on('routeChangeStart', handleRouteChange)
    router.events.on('routeChangeComplete', handleRouteChange)

    // Cleanup
    return () => {
      router.events.off('routeChangeStart', handleRouteChange)
      router.events.off('routeChangeComplete', handleRouteChange)
    }
  }, [user, loading, router])

  // BLOCK EVERYTHING until authorized
  if (!isAuthorized) {
    if (loading) {
      return (
        <div className="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-gray-100 dark:from-gray-900 dark:via-blue-900 dark:to-gray-800 flex items-center justify-center">
          <div className="text-center">
            <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-xl text-gray-600 dark:text-gray-300">Verifying authentication...</p>
          </div>
        </div>
      )
    }
    
    // Not loading but not authorized - redirect
    router.replace('/login')
    return null
  }

  // Final safety check
  if (!user) {
    localStorage.removeItem('token')
    router.replace('/login')
    return null
  }

  return <>{children}</>
}
