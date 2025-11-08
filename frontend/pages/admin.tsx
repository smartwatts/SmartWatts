import { useEffect } from 'react'
import { useRouter } from 'next/router'
import AdminRoute from '../components/AdminRoute'

export default function Admin() {
  const router = useRouter()

  useEffect(() => {
    // Redirect to admin dashboard
    router.replace('/admin/dashboard')
  }, [router])

  return (
    <AdminRoute>
      <div className="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-gray-100 dark:from-gray-900 dark:via-blue-900 dark:to-gray-800 flex items-center justify-center">
        <div className="relative">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="w-8 h-8 bg-blue-600 rounded-full animate-pulse"></div>
          </div>
        </div>
      </div>
    </AdminRoute>
  )
}

