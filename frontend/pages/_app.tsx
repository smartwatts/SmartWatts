import type { AppProps } from 'next/app'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'
import { AuthProvider } from '../hooks/useAuth'
import { ThemeProvider } from '../contexts/ThemeContext'
import { useRouter } from 'next/router'
import { useEffect } from 'react'
import pwaManager from '../utils/pwa-utils'
import '../styles/globals.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
})

export default function App({ Component, pageProps }: AppProps) {
  const router = useRouter()
  
  // Register service worker on app mount
  useEffect(() => {
    if (typeof window !== 'undefined') {
      pwaManager.registerServiceWorker().catch(console.error)
    }
  }, [])
  
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <AuthProvider>
          <Component {...pageProps} />
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 4000,
              style: {
                background: '#363636',
                color: '#fff',
              },
            }}
          />
        </AuthProvider>
      </ThemeProvider>
    </QueryClientProvider>
  )
} 