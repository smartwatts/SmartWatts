import { useEffect } from 'react'
import { useRouter } from 'next/router'

export default function NewDashboard() {
  const router = useRouter()

  useEffect(() => {
    // Force redirect to dashboard with cache-busting
    router.replace('/dashboard?v=new&t=' + Date.now())
  }, [router])

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-gray-100 flex items-center justify-center">
      <div className="text-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Loading New Dashboard...</h1>
        <p className="text-gray-600">Redirecting to the enhanced energy management dashboard</p>
      </div>
    </div>
  )
}
