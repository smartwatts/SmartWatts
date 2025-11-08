import React, { useState, useEffect } from 'react'
import { useRouter } from 'next/router'
import Link from 'next/link'
import { useAuth } from '../hooks/useAuth'
import { useFeatureFlags } from '../hooks/useFeatureFlags'
import ThemeSelector from './ThemeSelector'
import { DashboardThemeProvider } from '../contexts/DashboardThemeContext'
import OfflineIndicator from './OfflineIndicator'
import InstallPrompt from './InstallPrompt'
import {
  HomeIcon,
  BoltIcon,
  CreditCardIcon,
  ChartBarIcon,
  CogIcon,
  UserIcon,
  BellIcon,
  Bars3Icon,
  XMarkIcon,
  ArrowRightOnRectangleIcon,
  ShieldCheckIcon,
  BuildingOffice2Icon,
  DevicePhoneMobileIcon,
  ServerIcon,
  UsersIcon,
  KeyIcon,
  CubeIcon,
  ShoppingCartIcon,
  UserGroupIcon,
  ArrowPathIcon,
} from '@heroicons/react/24/outline'

interface LayoutProps {
  children: React.ReactNode
}

export default function Layout({ children }: LayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const { user, loading, logout } = useAuth()
  const { isFeatureEnabled } = useFeatureFlags()
  const router = useRouter()


  // Dynamic navigation based on feature flags - memoized to prevent re-renders
  // MUST be called before any conditional returns to follow Rules of Hooks
  const navigation = React.useMemo(() => {
    const baseNavigation = [
      { name: 'Dashboard', href: '/dashboard', icon: HomeIcon, requiredFeature: null }
    ]

    // Enterprise admin navigation - SYSTEM ADMINISTRATION ONLY
    if (user?.role === 'ROLE_ENTERPRISE_ADMIN') {
      return [
        ...baseNavigation,
        { name: 'Accounts', href: '/admin/accounts', icon: BuildingOffice2Icon, requiredFeature: null },
        { name: 'Inventory', href: '/admin/inventory', icon: CubeIcon, requiredFeature: null },
        { name: 'Purchase', href: '/admin/purchase', icon: ShoppingCartIcon, requiredFeature: null },
        { name: 'Sales', href: '/admin/sales', icon: ChartBarIcon, requiredFeature: null },
        { name: 'Teams', href: '/admin/teams', icon: UserGroupIcon, requiredFeature: null },
        { name: 'Renewals', href: '/admin/renewals', icon: ArrowPathIcon, requiredFeature: null },
        { name: 'Business Analytics', href: '/admin/analytics', icon: ChartBarIcon, requiredFeature: null },
        { name: 'Feature Management', href: '/admin/features', icon: CogIcon, requiredFeature: null },
        { name: 'Device Management', href: '/admin/devices', icon: DevicePhoneMobileIcon, requiredFeature: null },
        { name: 'API Credentials', href: '/admin/api-credentials', icon: KeyIcon, requiredFeature: null },
        { name: 'User Management', href: '/admin/users', icon: UsersIcon, requiredFeature: null },
        { name: 'System Health', href: '/admin/health', icon: ServerIcon, requiredFeature: null },
        { name: 'System Configuration', href: '/admin/config', icon: CogIcon, requiredFeature: null },
        { name: 'Security & Compliance', href: '/admin/security', icon: ShieldCheckIcon, requiredFeature: null },
        { name: 'Profile', href: '/profile', icon: UserIcon, requiredFeature: null },
      ]
    }

    // Regular user navigation - customer-facing features only
    const conditionalNavigation = [
      { name: 'Energy Monitor', href: '/energy', icon: BoltIcon, requiredFeature: null },
      { name: 'Analytics', href: '/analytics', icon: ChartBarIcon, requiredFeature: null },
      { name: 'Devices', href: '/devices', icon: CogIcon, requiredFeature: null },
      { name: 'Appliance Monitoring', href: '/appliance-monitoring', icon: CogIcon, requiredFeature: 'APPLIANCE_MONITORING' },
      { name: 'Profile', href: '/profile', icon: UserIcon, requiredFeature: null },
    ]

    // For regular users, filter based on feature flags
    const filteredNavigation = conditionalNavigation.filter(item => {
      if (!item.requiredFeature) return true
      return isFeatureEnabled(item.requiredFeature)
    })

    return [...baseNavigation, ...filteredNavigation]
  }, [user?.role, isFeatureEnabled])

  // Show loading state during hydration to prevent mismatch
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600 dark:text-gray-400">Loading...</p>
        </div>
      </div>
    )
  }

  // BLOCK ALL RENDERING if not authenticated
  if (!user) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center max-w-md">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">Authentication Required</h1>
          <p className="text-gray-600 dark:text-gray-300 mb-4">Please log in to access this page.</p>
          <div className="space-y-4">
            <button 
              onClick={() => window.location.href = '/login'}
              className="w-full bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
            >
              Go to Login
            </button>
          </div>
        </div>
      </div>
    )
  }

  const currentUser = user

  if (!currentUser) {
    return (
      <DashboardThemeProvider>
        <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
          <main className="py-6">
            <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
              {children}
            </div>
          </main>
        </div>
      </DashboardThemeProvider>
    )
  }

  return (
    <DashboardThemeProvider>
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
        {/* Mobile sidebar overlay */}
        {sidebarOpen && (
          <div className="fixed inset-0 z-40 lg:hidden">
            <div className="fixed inset-0 bg-gray-600 bg-opacity-75" onClick={() => setSidebarOpen(false)} />
          </div>
        )}

        {/* Mobile sidebar */}
        <div className={`fixed inset-y-0 left-0 z-50 w-64 bg-white dark:bg-gray-900 transform transition-transform duration-300 ease-in-out lg:hidden flex flex-col ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}>
          {/* Mobile Sidebar Header */}
          <div className="flex items-center justify-between h-16 px-6 border-b border-gray-200 dark:border-gray-700 flex-shrink-0">
            <div className="flex items-center space-x-3">
              <div className="w-8 h-8 bg-gradient-to-r from-blue-500 to-blue-600 rounded-lg flex items-center justify-center">
                <BoltIcon className="h-5 w-5 text-white" />
              </div>
              <span className="text-xl font-bold text-gray-900 dark:text-white">SmartWatts</span>
            </div>
            <button
              onClick={() => setSidebarOpen(false)}
              className="lg:hidden p-2 rounded-md text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
            >
              <XMarkIcon className="h-6 w-6" />
            </button>
          </div>

          {/* Mobile Sidebar Navigation - Scrollable */}
          <nav className="flex-1 overflow-y-auto space-y-2 px-4 py-6">
            {navigation.map((item) => {
              const isActive = router.pathname === item.href
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  onClick={() => setSidebarOpen(false)}
                  className={`group flex items-center px-4 py-3 text-sm font-medium rounded-xl transition-all duration-200 ${
                    isActive
                      ? 'bg-gradient-to-r from-blue-600 to-blue-700 text-white shadow-lg'
                      : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white'
                  }`}
                >
                  <item.icon className={`mr-3 h-5 w-5 ${isActive ? 'text-white' : 'text-gray-500 dark:text-gray-400 group-hover:text-blue-600 dark:group-hover:text-blue-400'}`} />
                  {item.name}
                </Link>
              )
            })}
          </nav>

          {/* Mobile Sidebar Footer */}
          <div className="border-t border-gray-200 dark:border-gray-700 p-4 flex-shrink-0">
            <div className="flex items-center space-x-3 mb-4">
              <div className="w-8 h-8 bg-gradient-to-r from-green-500 to-green-600 rounded-full flex items-center justify-center">
                <UserIcon className="h-4 w-4 text-white" />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-900 dark:text-white">
                  {currentUser.firstName} {currentUser.lastName}
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400">{currentUser.email}</p>
              </div>
            </div>
            <button
              onClick={logout}
              className="w-full flex items-center justify-center px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
            >
              <ArrowRightOnRectangleIcon className="mr-2 h-4 w-4" />
              Sign Out
            </button>
          </div>
        </div>

        {/* Top navigation bar */}
        <div className="lg:pl-64">
          <div className="sticky top-0 z-30 bg-white/80 dark:bg-gray-900/80 backdrop-blur-xl border-b border-gray-200 dark:border-gray-700">
            <div className="flex h-16 items-center justify-between px-4 sm:px-6 lg:px-8">
              {/* Left side - Mobile menu button and page title */}
              <div className="flex items-center space-x-4">
                <button
                  onClick={() => setSidebarOpen(true)}
                  className="lg:hidden p-2 rounded-md text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  <Bars3Icon className="h-6 w-6" />
                </button>
                
                {/* Page title based on current route */}
                <div className="hidden sm:block">
                  <h1 className="text-lg font-semibold text-gray-900 dark:text-white">
                    {navigation.find(item => item.href === router.pathname)?.name || 'SmartWatts'}
                  </h1>
                </div>
              </div>

              {/* Right side - User menu and theme selector */}
              <div className="flex items-center space-x-4">
                <ThemeSelector />
                
                {/* Notifications */}
                <button className="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 relative">
                  <BellIcon className="h-6 w-6" />
                  <span className="absolute top-1 right-1 h-2 w-2 bg-red-500 rounded-full"></span>
                </button>

                {/* User menu - Desktop */}
                <div className="hidden lg:flex items-center space-x-3">
                  <div className="flex items-center space-x-3">
                    <div className="w-8 h-8 bg-gradient-to-r from-green-500 to-green-600 rounded-full flex items-center justify-center">
                      <UserIcon className="h-4 w-4 text-white" />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-900 dark:text-white">
                        {currentUser.firstName} {currentUser.lastName}
                      </p>
                      <p className="text-xs text-gray-500 dark:text-gray-400">{currentUser.email}</p>
                    </div>
                  </div>
                  <button
                    onClick={logout}
                    className="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                  >
                    <ArrowRightOnRectangleIcon className="h-5 w-5" />
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Page content */}
          <main className="p-4 sm:p-6 lg:p-8">
            {children}
          </main>
          
          {/* Footer */}
          <footer className="bg-gray-50 dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 mt-12">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
              <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
                {/* Company Info */}
                <div className="col-span-1 md:col-span-2">
                  <div className="flex items-center space-x-2 mb-4">
                    <div className="w-8 h-8 bg-gradient-to-r from-blue-500 to-green-500 rounded-lg flex items-center justify-center">
                      <BoltIcon className="h-5 w-5 text-white" />
                    </div>
                    <span className="text-xl font-bold text-gray-900 dark:text-white">SmartWatts</span>
                  </div>
                  <p className="text-gray-600 dark:text-gray-400 text-sm mb-4">
                    AI-powered energy monitoring and optimization platform for Nigeria. 
                    Monitor, analyze, and optimize your energy consumption with intelligent insights.
                  </p>
                  <p className="text-gray-500 dark:text-gray-500 text-xs">
                    © {new Date().getFullYear()} SmartWatts Limited. All rights reserved.
                  </p>
                </div>

                {/* Quick Links */}
                <div>
                  <h3 className="text-sm font-semibold text-gray-900 dark:text-white mb-4">Quick Links</h3>
                  <ul className="space-y-2">
                    <li>
                      <Link href="/dashboard" className="text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors">
                        Dashboard
                      </Link>
                    </li>
                    <li>
                      <Link href="/energy" className="text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors">
                        Energy Monitoring
                      </Link>
                    </li>
                    <li>
                      <Link href="/appliance-monitoring" className="text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors">
                        Appliance Monitoring
                      </Link>
                    </li>
                    <li>
                      <Link href="/billing" className="text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors">
                        Billing
                      </Link>
                    </li>
                  </ul>
                </div>

                {/* Get in Touch */}
                <div>
                  <h3 className="text-sm font-semibold text-gray-900 dark:text-white mb-4">Get in Touch</h3>
                  <div className="space-y-3">
                    <div>
                      <h4 className="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Office Address</h4>
                      <p className="text-xs text-gray-600 dark:text-gray-400">
                        33 Parakou Street off Aminu Kano Crescent<br />
                        Wuse2, Abuja, Nigeria
                      </p>
                    </div>
                    <div>
                      <h4 className="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Business Hours</h4>
                      <p className="text-xs text-gray-600 dark:text-gray-400">
                        Mon-Fri: 8AM-6PM<br />
                        Sat: 9AM-4PM, Sun: Closed
                      </p>
                    </div>
                    <div>
                      <h4 className="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Response Time</h4>
                      <p className="text-xs text-gray-600 dark:text-gray-400">
                        Within 24 hours
                      </p>
                    </div>
                  </div>
                </div>

                {/* Legal */}
                <div>
                  <h3 className="text-sm font-semibold text-gray-900 dark:text-white mb-4">Legal</h3>
                  <ul className="space-y-2">
                    <li>
                      <button
                        onClick={() => window.open('/terms-of-service', 'terms', 'width=800,height=600,scrollbars=yes,resizable=yes')}
                        className="text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors underline"
                      >
                        Terms of Service
                      </button>
                    </li>
                    <li>
                      <button
                        onClick={() => window.open('/privacy-policy', 'privacy', 'width=800,height=600,scrollbars=yes,resizable=yes')}
                        className="text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors underline"
                      >
                        Privacy Policy
                      </button>
                    </li>
                    <li>
                      <Link href="/contact" className="text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors">
                        Contact Us
                      </Link>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </footer>
        </div>

        {/* Desktop sidebar (hidden on mobile) */}
        <div className="hidden lg:flex lg:flex-col fixed inset-y-0 left-0 z-50 w-64 bg-white/95 dark:bg-gray-900/95 backdrop-blur-xl shadow-2xl border-r border-gray-200 dark:border-blue-500/20">
          {/* Desktop Sidebar Header */}
          <div className="flex items-center h-16 px-6 border-b border-gray-200 dark:border-gray-700 flex-shrink-0">
            <div className="flex items-center space-x-3">
              <div className="w-8 h-8 bg-gradient-to-r from-blue-500 to-blue-600 rounded-lg flex items-center justify-center">
                <BoltIcon className="h-5 w-5 text-white" />
              </div>
              <span className="text-xl font-bold text-gray-900 dark:text-white">SmartWatts</span>
            </div>
          </div>

          {/* Desktop Sidebar Navigation - Scrollable */}
          <nav className="flex-1 overflow-y-auto space-y-2 px-4 py-6">
            {navigation.map((item) => {
              const isActive = router.pathname === item.href
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={`group flex items-center px-4 py-3 text-sm font-medium rounded-xl transition-all duration-200 ${
                    isActive
                      ? 'bg-gradient-to-r from-blue-600 to-blue-700 text-white shadow-lg'
                      : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white'
                  }`}
                >
                  <item.icon className={`mr-3 h-5 w-5 ${isActive ? 'text-white' : 'text-gray-500 dark:text-gray-400 group-hover:text-blue-600 dark:group-hover:text-blue-400'}`} />
                  {item.name}
                </Link>
              )
            })}
          </nav>

          {/* Desktop Sidebar Footer */}
          <div className="border-t border-gray-200 dark:border-gray-700 p-4 flex-shrink-0">
            <div className="flex items-center space-x-3 mb-4">
              <div className="w-8 h-8 bg-gradient-to-r from-green-500 to-green-600 rounded-full flex items-center justify-center">
                <UserIcon className="h-4 w-4 text-white" />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-900 dark:text-white">
                  {currentUser.firstName} {currentUser.lastName}
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400">{currentUser.email}</p>
              </div>
            </div>
            <button
              onClick={logout}
              className="w-full flex items-center justify-center px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
            >
              <ArrowRightOnRectangleIcon className="mr-2 h-4 w-4" />
              Sign Out
            </button>
          </div>
        </div>

        {/* PWA Components */}
        <OfflineIndicator />
        <InstallPrompt />
      </div>
    </DashboardThemeProvider>
  )
} 