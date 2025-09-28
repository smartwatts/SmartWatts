import React, { useState, useEffect } from 'react'
import Layout from '../../components/Layout'
import Head from 'next/head'
import {
  CogIcon,
  CheckCircleIcon,
  XCircleIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
} from '@heroicons/react/24/outline'

interface Feature {
  id: string
  name: string
  description: string
  category: 'core' | 'premium' | 'enterprise'
  enabled: boolean
  required: boolean
  price?: number
}

interface Customer {
  id: string
  name: string
  email: string
  plan: 'basic' | 'premium' | 'enterprise'
  addons: {
    [featureId: string]: {
      enabled: boolean
      paid: boolean
      startDate?: string
      endDate?: string
    }
  }
}

export default function FeatureManagement() {
  return (
    
      <Layout>
        <FeatureManagementContent />
      </Layout>
    
  )
}

function FeatureManagementContent() {
  const [features, setFeatures] = useState<Feature[]>([
    // Core Features (always enabled)
    {
      id: 'basic_monitoring',
      name: 'Basic Monitoring',
      description: 'Essential energy monitoring and device tracking',
      category: 'core',
      enabled: true,
      required: true
    },
    {
      id: 'basic_analytics',
      name: 'Basic Analytics',
      description: 'Standard reporting and data visualization',
      category: 'core',
      enabled: true,
      required: true
    },
    {
      id: 'device_management',
      name: 'Device Management',
      description: 'Register and manage IoT devices',
      category: 'core',
      enabled: true,
      required: true
    },
    
    // Premium Add-ons (toggleable)
    {
      id: 'facility360',
      name: 'Facility360',
      description: 'Comprehensive facility management and monitoring',
      category: 'premium',
      enabled: false,
      required: false,
      price: 50
    },
    {
      id: 'billing_dashboard',
      name: 'Billing Dashboard',
      description: 'Advanced billing and payment management',
      category: 'premium',
      enabled: false,
      required: false,
      price: 30
    },
    {
      id: 'partner_services',
      name: 'Partner Services',
      description: 'Integration with third-party service providers',
      category: 'premium',
      enabled: false,
      required: false,
      price: 25
    },
    {
      id: 'appliance_monitoring',
      name: 'Appliance Monitoring',
      description: 'Individual appliance energy tracking and optimization',
      category: 'premium',
      enabled: false,
      required: false,
      price: 40
    },
    
    // Enterprise Features (toggleable)
    {
      id: 'advanced_analytics',
      name: 'Advanced Analytics',
      description: 'AI-powered insights and predictive analytics',
      category: 'enterprise',
      enabled: false,
      required: false,
      price: 100
    },
    {
      id: 'api_access',
      name: 'API Access',
      description: 'Full API access for custom integrations',
      category: 'enterprise',
      enabled: false,
      required: false,
      price: 75
    },
    {
      id: 'custom_integrations',
      name: 'Custom Integrations',
      description: 'Bespoke integration development and support',
      category: 'enterprise',
      enabled: false,
      required: false,
      price: 150
    }
  ])

  const [loading, setLoading] = useState(false)
  const [selectedCustomer, setSelectedCustomer] = useState<string | null>(null)
  const [customers, setCustomers] = useState<Customer[]>([
    {
      id: 'customer-1',
      name: 'John Doe',
      email: 'john@example.com',
      plan: 'basic',
      addons: {
        facility360: { enabled: false, paid: false },
        billing_dashboard: { enabled: true, paid: true, startDate: '2024-01-01', endDate: '2024-12-31' },
        partner_services: { enabled: false, paid: false },
        appliance_monitoring: { enabled: false, paid: false },
        advanced_analytics: { enabled: false, paid: false },
        api_access: { enabled: false, paid: false },
        custom_integrations: { enabled: false, paid: false }
      }
    },
    {
      id: 'customer-2',
      name: 'Jane Smith',
      email: 'jane@company.com',
      plan: 'premium',
      addons: {
        facility360: { enabled: true, paid: true, startDate: '2024-01-15', endDate: '2024-12-31' },
        billing_dashboard: { enabled: true, paid: true, startDate: '2024-01-01', endDate: '2024-12-31' },
        partner_services: { enabled: true, paid: true, startDate: '2024-02-01', endDate: '2024-12-31' },
        appliance_monitoring: { enabled: false, paid: false },
        advanced_analytics: { enabled: false, paid: false },
        api_access: { enabled: false, paid: false },
        custom_integrations: { enabled: false, paid: false }
      }
    },
    {
      id: 'customer-3',
      name: 'Mike Johnson',
      email: 'mike@enterprise.com',
      plan: 'enterprise',
      addons: {
        facility360: { enabled: true, paid: true, startDate: '2024-01-01', endDate: '2024-12-31' },
        billing_dashboard: { enabled: true, paid: true, startDate: '2024-01-01', endDate: '2024-12-31' },
        partner_services: { enabled: true, paid: true, startDate: '2024-01-01', endDate: '2024-12-31' },
        appliance_monitoring: { enabled: true, paid: true, startDate: '2024-01-01', endDate: '2024-12-31' },
        advanced_analytics: { enabled: true, paid: true, startDate: '2024-01-01', endDate: '2024-12-31' },
        api_access: { enabled: true, paid: true, startDate: '2024-01-01', endDate: '2024-12-31' },
        custom_integrations: { enabled: true, paid: true, startDate: '2024-01-01', endDate: '2024-12-31' }
      }
    }
  ])

  const toggleFeature = async (featureId: string) => {
    setLoading(true)
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500))
      
      setFeatures(prev => prev.map(feature => 
        feature.id === featureId 
          ? { ...feature, enabled: !feature.enabled }
          : feature
      ))
    } catch (error) {
      console.error('Failed to toggle feature:', error)
    } finally {
      setLoading(false)
    }
  }

  const toggleCustomerAddon = async (customerId: string, featureId: string) => {
    setLoading(true)
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500))
      
      setCustomers(prev => prev.map(customer => {
        if (customer.id === customerId) {
          const currentAddon = customer.addons[featureId]
          return {
            ...customer,
            addons: {
              ...customer.addons,
              [featureId]: {
                ...currentAddon,
                enabled: !currentAddon.enabled,
                paid: !currentAddon.enabled ? true : currentAddon.paid, // Auto-mark as paid when enabling
                startDate: !currentAddon.enabled ? new Date().toISOString().split('T')[0] : currentAddon.startDate,
                endDate: !currentAddon.enabled ? new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0] : currentAddon.endDate
              }
            }
          }
        }
        return customer
      }))
    } catch (error) {
      console.error('Failed to toggle customer addon:', error)
    } finally {
      setLoading(false)
    }
  }

  const markAddonAsPaid = async (customerId: string, featureId: string) => {
    setLoading(true)
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500))
      
      setCustomers(prev => prev.map(customer => {
        if (customer.id === customerId) {
          return {
            ...customer,
            addons: {
              ...customer.addons,
              [featureId]: {
                ...customer.addons[featureId],
                paid: true,
                startDate: customer.addons[featureId].startDate || new Date().toISOString().split('T')[0],
                endDate: customer.addons[featureId].endDate || new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
              }
            }
          }
        }
        return customer
      }))
    } catch (error) {
      console.error('Failed to mark addon as paid:', error)
    } finally {
      setLoading(false)
    }
  }

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case 'core':
        return <CogIcon className="h-5 w-5" />
      case 'premium':
        return <CheckCircleIcon className="h-5 w-5" />
      case 'enterprise':
        return <ExclamationTriangleIcon className="h-5 w-5" />
      default:
        return <InformationCircleIcon className="h-5 w-5" />
    }
  }

  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'core':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
      case 'premium':
        return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'enterprise':
        return 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400'
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getCategoryBgColor = (category: string) => {
    switch (category) {
      case 'core':
        return 'bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-900 dark:to-blue-800'
      case 'premium':
        return 'bg-gradient-to-br from-green-50 to-green-100 dark:from-green-900 dark:to-green-800'
      case 'enterprise':
        return 'bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-900 dark:to-purple-800'
      default:
        return 'bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800'
    }
  }

  const coreFeatures = features.filter(f => f.category === 'core')
  const premiumFeatures = features.filter(f => f.category === 'premium')
  const enterpriseFeatures = features.filter(f => f.category === 'enterprise')

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <Head>
        <title>Feature Management - SmartWatts</title>
      </Head>
      
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          Feature Management
        </h1>
        <p className="mt-2 text-gray-600 dark:text-gray-300">
          Manage feature flags, addons, and user access controls across the system
        </p>
      </div>

      {/* Feature Categories */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Core Features */}
        <div className={`${getCategoryBgColor('core')} p-6 rounded-lg`}>
          <div className="flex items-center mb-4">
            {getCategoryIcon('core')}
            <h3 className="text-xl font-semibold text-blue-900 dark:text-blue-100 ml-2">
              Core Features
            </h3>
          </div>
          <p className="text-blue-800 dark:text-blue-200 text-sm mb-4">
            Essential features included with all plans
          </p>
          <div className="space-y-3">
            {coreFeatures.map((feature) => (
              <div key={feature.id} className="flex items-center justify-between p-3 bg-white/50 dark:bg-blue-800/30 rounded-lg">
                <div className="flex-1">
                  <div className="font-medium text-blue-900 dark:text-blue-100">
                    {feature.name}
                  </div>
                  <div className="text-xs text-blue-700 dark:text-blue-300">
                    {feature.description}
                  </div>
                </div>
                <div className="flex items-center">
                  <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400">
                    Always On
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Premium Add-ons */}
        <div className={`${getCategoryBgColor('premium')} p-6 rounded-lg`}>
          <div className="flex items-center mb-4">
            {getCategoryIcon('premium')}
            <h3 className="text-xl font-semibold text-green-900 dark:text-green-100 ml-2">
              Premium Add-ons
            </h3>
          </div>
          <p className="text-green-800 dark:text-green-200 text-sm mb-4">
            Optional features that can be enabled/disabled
          </p>
          <div className="space-y-3">
            {premiumFeatures.map((feature) => (
              <div key={feature.id} className="flex items-center justify-between p-3 bg-white/50 dark:bg-green-800/30 rounded-lg">
                <div className="flex-1">
                  <div className="font-medium text-green-900 dark:text-green-100">
                    {feature.name}
                  </div>
                  <div className="text-xs text-green-700 dark:text-green-300">
                    {feature.description}
                  </div>
                  {feature.price && (
                    <div className="text-xs text-green-600 dark:text-green-400 font-semibold">
                      ₦{feature.price}/month
                    </div>
                  )}
                </div>
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => toggleFeature(feature.id)}
                    disabled={loading}
                    className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 ${
                      feature.enabled ? 'bg-green-600' : 'bg-gray-200 dark:bg-gray-700'
                    }`}
                  >
                    <span
                      className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                        feature.enabled ? 'translate-x-6' : 'translate-x-1'
                      }`}
                    />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Enterprise Features */}
        <div className={`${getCategoryBgColor('enterprise')} p-6 rounded-lg`}>
          <div className="flex items-center mb-4">
            {getCategoryIcon('enterprise')}
            <h3 className="text-xl font-semibold text-purple-900 dark:text-purple-100 ml-2">
              Enterprise Features
            </h3>
          </div>
          <p className="text-purple-800 dark:text-purple-200 text-sm mb-4">
            Advanced features for enterprise customers
          </p>
          <div className="space-y-3">
            {enterpriseFeatures.map((feature) => (
              <div key={feature.id} className="flex items-center justify-between p-3 bg-white/50 dark:bg-purple-800/30 rounded-lg">
                <div className="flex-1">
                  <div className="font-medium text-purple-900 dark:text-purple-100">
                    {feature.name}
                  </div>
                  <div className="text-xs text-purple-700 dark:text-purple-300">
                    {feature.description}
                  </div>
                  {feature.price && (
                    <div className="text-xs text-purple-600 dark:text-purple-400 font-semibold">
                      ₦{feature.price}/month
                    </div>
                  )}
                </div>
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => toggleFeature(feature.id)}
                    disabled={loading}
                    className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 ${
                      feature.enabled ? 'bg-purple-600' : 'bg-gray-200 dark:bg-gray-700'
                    }`}
                  >
                    <span
                      className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                        feature.enabled ? 'translate-x-6' : 'translate-x-1'
                      }`}
                    />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Feature Summary */}
      <div className="mt-8 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          Feature Summary
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600 dark:text-blue-400">
              {coreFeatures.length}
            </div>
            <div className="text-sm text-gray-600 dark:text-gray-400">Core Features</div>
            <div className="text-xs text-gray-500 dark:text-gray-500">Always Enabled</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600 dark:text-green-400">
              {premiumFeatures.filter(f => f.enabled).length}/{premiumFeatures.length}
            </div>
            <div className="text-sm text-gray-600 dark:text-gray-400">Premium Add-ons</div>
            <div className="text-xs text-gray-500 dark:text-gray-500">Toggleable</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-purple-600 dark:text-purple-400">
              {enterpriseFeatures.filter(f => f.enabled).length}/{enterpriseFeatures.length}
            </div>
            <div className="text-sm text-gray-600 dark:text-gray-400">Enterprise Features</div>
            <div className="text-xs text-gray-500 dark:text-gray-500">Toggleable</div>
          </div>
        </div>
      </div>

      {/* Customer Addon Management */}
      <div className="mt-8 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          Customer Addon Management
        </h3>
        <p className="text-gray-600 dark:text-gray-300 mb-6">
          Enable/disable addons for specific customers regardless of their plan. Customers can access any addon as long as they're paying for it.
        </p>

        {/* Customer Selection */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Select Customer
          </label>
          <select
            value={selectedCustomer || ''}
            onChange={(e) => setSelectedCustomer(e.target.value)}
            className="w-full max-w-md px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">Choose a customer...</option>
            {customers.map((customer) => (
              <option key={customer.id} value={customer.id}>
                {customer.name} ({customer.email}) - {customer.plan.toUpperCase()} Plan
              </option>
            ))}
          </select>
        </div>

        {/* Customer Addon Management */}
        {selectedCustomer && (
          <div className="space-y-6">
            {(() => {
              const customer = customers.find(c => c.id === selectedCustomer)
              if (!customer) return null

              return (
                <>
                  {/* Customer Info */}
                  <div className="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                    <h4 className="font-semibold text-gray-900 dark:text-white">
                      {customer.name} ({customer.email})
                    </h4>
                    <p className="text-sm text-gray-600 dark:text-gray-300">
                      Current Plan: <span className="font-medium">{customer.plan.toUpperCase()}</span>
                    </p>
                  </div>

                  {/* Addon Controls */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* Premium Add-ons */}
                    <div>
                      <h5 className="font-semibold text-gray-900 dark:text-white mb-3">Premium Add-ons</h5>
                      <div className="space-y-3">
                        {premiumFeatures.map((feature) => {
                          const addon = customer.addons[feature.id]
                          return (
                            <div key={feature.id} className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                              <div className="flex-1">
                                <div className="font-medium text-gray-900 dark:text-white">
                                  {feature.name}
                                </div>
                                <div className="text-xs text-gray-600 dark:text-gray-400">
                                  ₦{feature.price}/month
                                </div>
                                {addon.enabled && (
                                  <div className="text-xs text-gray-500 dark:text-gray-500">
                                    {addon.startDate} - {addon.endDate}
                                  </div>
                                )}
                              </div>
                              <div className="flex items-center space-x-2">
                                {!addon.paid && addon.enabled && (
                                  <button
                                    onClick={() => markAddonAsPaid(customer.id, feature.id)}
                                    className="px-2 py-1 text-xs bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400 rounded hover:bg-yellow-200 dark:hover:bg-yellow-900/30"
                                  >
                                    Mark Paid
                                  </button>
                                )}
                                <button
                                  onClick={() => toggleCustomerAddon(customer.id, feature.id)}
                                  disabled={loading}
                                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 ${
                                    addon.enabled ? 'bg-green-600' : 'bg-gray-200 dark:bg-gray-600'
                                  }`}
                                >
                                  <span
                                    className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                                      addon.enabled ? 'translate-x-6' : 'translate-x-1'
                                    }`}
                                  />
                                </button>
                              </div>
                            </div>
                          )
                        })}
                      </div>
                    </div>

                    {/* Enterprise Features */}
                    <div>
                      <h5 className="font-semibold text-gray-900 dark:text-white mb-3">Enterprise Features</h5>
                      <div className="space-y-3">
                        {enterpriseFeatures.map((feature) => {
                          const addon = customer.addons[feature.id]
                          return (
                            <div key={feature.id} className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                              <div className="flex-1">
                                <div className="font-medium text-gray-900 dark:text-white">
                                  {feature.name}
                                </div>
                                <div className="text-xs text-gray-600 dark:text-gray-400">
                                  ₦{feature.price}/month
                                </div>
                                {addon.enabled && (
                                  <div className="text-xs text-gray-500 dark:text-gray-500">
                                    {addon.startDate} - {addon.endDate}
                                  </div>
                                )}
                              </div>
                              <div className="flex items-center space-x-2">
                                {!addon.paid && addon.enabled && (
                                  <button
                                    onClick={() => markAddonAsPaid(customer.id, feature.id)}
                                    className="px-2 py-1 text-xs bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400 rounded hover:bg-yellow-200 dark:hover:bg-yellow-900/30"
                                  >
                                    Mark Paid
                                  </button>
                                )}
                                <button
                                  onClick={() => toggleCustomerAddon(customer.id, feature.id)}
                                  disabled={loading}
                                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 ${
                                    addon.enabled ? 'bg-purple-600' : 'bg-gray-200 dark:bg-gray-600'
                                  }`}
                                >
                                  <span
                                    className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                                      addon.enabled ? 'translate-x-6' : 'translate-x-1'
                                    }`}
                                  />
                                </button>
                              </div>
                            </div>
                          )
                        })}
                      </div>
                    </div>
                  </div>

                  {/* Customer Addon Summary */}
                  <div className="bg-blue-50 dark:bg-blue-900/20 p-4 rounded-lg">
                    <h5 className="font-semibold text-blue-900 dark:text-blue-100 mb-2">Customer Addon Summary</h5>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                      <div>
                        <div className="font-medium text-blue-800 dark:text-blue-200">
                          {Object.values(customer.addons).filter(addon => addon.enabled).length}
                        </div>
                        <div className="text-blue-600 dark:text-blue-400">Active Addons</div>
                      </div>
                      <div>
                        <div className="font-medium text-blue-800 dark:text-blue-200">
                          {Object.values(customer.addons).filter(addon => addon.paid && addon.enabled).length}
                        </div>
                        <div className="text-blue-600 dark:text-blue-400">Paid Addons</div>
                      </div>
                      <div>
                        <div className="font-medium text-blue-800 dark:text-blue-200">
                          ₦{Object.entries(customer.addons).reduce((total, [featureId, addon]) => {
                            if (addon.enabled && addon.paid) {
                              const feature = features.find(f => f.id === featureId)
                              return total + (feature?.price || 0)
                            }
                            return total
                          }, 0)}
                        </div>
                        <div className="text-blue-600 dark:text-blue-400">Monthly Revenue</div>
                      </div>
                      <div>
                        <div className="font-medium text-blue-800 dark:text-blue-200">
                          {Object.values(customer.addons).filter(addon => addon.enabled && !addon.paid).length}
                        </div>
                        <div className="text-blue-600 dark:text-blue-400">Unpaid Active</div>
                      </div>
                    </div>
                  </div>
                </>
              )
            })()}
          </div>
        )}
      </div>
    </div>
  )
}
