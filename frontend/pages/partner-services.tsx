import React, { useState } from 'react'
import Layout from '../components/Layout'
import ProtectedRoute from '../components/ProtectedRoute'
import {
  ShieldCheckIcon,
  CreditCardIcon,
  SunIcon,
  BoltIcon,
  CheckCircleIcon,
  InformationCircleIcon,
} from '@heroicons/react/24/outline'

interface InsuranceProduct {
  id: string
  name: string
  description: string
  coverage: string[]
  price: string
  provider: string
  icon: React.ComponentType<any>
  color: string
  features: string[]
}

interface FinanceProduct {
  id: string
  name: string
  description: string
  terms: string[]
  rate: string
  provider: string
  icon: React.ComponentType<any>
  color: string
  benefits: string[]
}

export default function PartnerServices() {
  const [selectedCategory, setSelectedCategory] = useState<'insurance' | 'finance'>('insurance')
  const [selectedProduct, setSelectedProduct] = useState<string | null>(null)

  const insuranceProducts: InsuranceProduct[] = [
    {
      id: 'equipment-only',
      name: 'Equipment Protection',
      description: 'Comprehensive coverage for your SmartWatts devices and energy equipment',
      coverage: ['Inverter damage', 'Battery protection', 'Smart plug coverage', 'Gateway protection'],
      price: '₦2,500/month',
      provider: 'AXA Mansard',
      icon: ShieldCheckIcon,
      color: 'blue',
      features: ['24/7 claims support', 'Quick replacement', 'No deductibles', 'Covers surge damage']
    },
    {
      id: 'home-equipment',
      name: 'Home + Equipment Bundle',
      description: 'Complete protection for your home and energy system',
      coverage: ['Home insurance', 'Equipment coverage', 'Fire protection', 'Theft coverage'],
      price: '₦5,000/month',
      provider: 'Leadway Assurance',
      icon: ShieldCheckIcon,
      color: 'green',
      features: ['Full home coverage', 'Equipment included', 'Natural disaster protection', 'Liability coverage']
    },
    {
      id: 'premium-coverage',
      name: 'Premium Energy Protection',
      description: 'Advanced coverage for commercial and high-value installations',
      coverage: ['Commercial equipment', 'Business interruption', 'Data loss protection', 'Professional liability'],
      price: '₦8,500/month',
      provider: 'ARM Insurance',
      icon: ShieldCheckIcon,
      color: 'purple',
      features: ['Commercial coverage', 'Business interruption', 'Professional liability', '24/7 support']
    }
  ]

  const financeProducts: FinanceProduct[] = [
    {
      id: 'solar-financing',
      name: 'Solar Installation Financing',
      description: 'Get your solar system with zero upfront payment',
      terms: ['0% down payment', '12-36 months', 'Flexible terms', 'Quick approval'],
      rate: '15% APR',
      provider: 'SunFi',
      icon: SunIcon,
      color: 'orange',
      benefits: ['No upfront cost', 'Energy savings', 'Tax benefits', 'Quick installation']
    },
    {
      id: 'equipment-financing',
      name: 'Smart Equipment Financing',
      description: 'Finance your SmartWatts devices and energy upgrades',
      terms: ['Flexible terms', 'Low interest', 'Quick approval', 'No collateral'],
      rate: '12% APR',
      provider: 'Carbon',
      icon: CreditCardIcon,
      color: 'blue',
      benefits: ['Immediate access', 'Build credit', 'Flexible payments', 'No hidden fees']
    },
    {
      id: 'energy-upgrade',
      name: 'Energy System Upgrade',
      description: 'Finance complete energy system upgrades and improvements',
      terms: ['Up to ₦5M', '24-60 months', 'Competitive rates', 'Fast processing'],
      rate: '18% APR',
      provider: 'Paylater',
      icon: BoltIcon,
      color: 'yellow',
      benefits: ['Large amounts', 'Long terms', 'Competitive rates', 'Quick processing']
    }
  ]

  const handleProductSelect = (productId: string) => {
    setSelectedProduct(productId)
    // Here you would typically open a modal or redirect to partner application
    console.log('Selected product:', productId)
    // You can use selectedProduct here for further processing
    alert(`Application form for ${productId} would open here`)
  }

  const getIconColor = (color: string) => {
    switch (color) {
      case 'blue': return 'text-blue-600'
      case 'green': return 'text-green-600'
      case 'purple': return 'text-purple-600'
      case 'orange': return 'text-orange-600'
      case 'yellow': return 'text-yellow-600'
      default: return 'text-gray-600'
    }
  }

  const getBgColor = (color: string) => {
    switch (color) {
      case 'blue': return 'bg-blue-50 dark:bg-blue-900/20'
      case 'green': return 'bg-green-50 dark:bg-green-900/20'
      case 'purple': return 'bg-purple-50 dark:bg-purple-900/20'
      case 'orange': return 'bg-orange-50 dark:bg-orange-900/20'
      case 'yellow': return 'bg-yellow-50 dark:bg-yellow-900/20'
      default: return 'bg-gray-50 dark:bg-gray-900/20'
    }
  }

  return (
    <ProtectedRoute>
      <Layout>
        <div className="space-y-6 animate-fade-in">
        {/* Header */}
        <div className="bg-gradient-to-r from-gray-800 via-blue-900 to-gray-800 dark:from-gray-900 dark:via-blue-900 dark:to-gray-800 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold mb-2">Partner Services</h1>
              <p className="text-blue-200 dark:text-blue-300 text-lg">Protect your investment and finance your energy upgrades</p>
              <div className="flex items-center mt-4 space-x-6">
                <div className="flex items-center space-x-2">
                  <ShieldCheckIcon className="h-5 w-5 text-green-300" />
                  <span className="text-sm">Trusted Partners</span>
                </div>
                <div className="flex items-center space-x-2">
                  <CheckCircleIcon className="h-5 w-5 text-blue-300" />
                  <span className="text-sm">NDPR Compliant</span>
                </div>
                <div className="flex items-center space-x-2">
                  <InformationCircleIcon className="h-5 w-5 text-yellow-300" />
                  <span className="text-sm">Commission Earned</span>
                </div>
              </div>
            </div>
            <div className="hidden md:block">
              <div className="w-24 h-24 bg-white/10 rounded-full flex items-center justify-center">
                <ShieldCheckIcon className="h-12 w-12 text-white" />
              </div>
            </div>
          </div>
        </div>

        {/* Category Tabs */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700">
          <div className="flex border-b border-gray-200 dark:border-gray-700">
            <button
              onClick={() => setSelectedCategory('insurance')}
              className={`flex-1 px-6 py-4 text-sm font-medium transition-colors ${
                selectedCategory === 'insurance'
                  ? 'text-blue-600 dark:text-blue-400 border-b-2 border-blue-600 dark:border-blue-400'
                  : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
              }`}
            >
              <ShieldCheckIcon className="h-5 w-5 inline mr-2" />
              Insurance Protection
            </button>
            <button
              onClick={() => setSelectedCategory('finance')}
              className={`flex-1 px-6 py-4 text-sm font-medium transition-colors ${
                selectedCategory === 'finance'
                  ? 'text-blue-600 dark:text-blue-400 border-b-2 border-blue-600 dark:border-blue-400'
                  : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
              }`}
            >
              <CreditCardIcon className="h-5 w-5 inline mr-2" />
              Financing Options
            </button>
          </div>
        </div>

        {/* Products Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {(selectedCategory === 'insurance' ? insuranceProducts : financeProducts).map((product) => {
            const IconComponent = product.icon
            return (
              <div
                key={product.id}
                className={`bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 hover:shadow-xl transition-all duration-300 cursor-pointer ${getBgColor(product.color)}`}
                onClick={() => handleProductSelect(product.id)}
              >
                <div className="p-6">
                  <div className="flex items-center justify-between mb-4">
                    <div className={`w-12 h-12 ${getBgColor(product.color)} rounded-xl flex items-center justify-center`}>
                      <IconComponent className={`h-6 w-6 ${getIconColor(product.color)}`} />
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium text-gray-500 dark:text-gray-400">{product.provider}</p>
                      <p className="text-lg font-bold text-gray-900 dark:text-white">{product.price || product.rate}</p>
                    </div>
                  </div>
                  
                  <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-2">{product.name}</h3>
                  <p className="text-gray-600 dark:text-gray-300 mb-4">{product.description}</p>
                  
                  <div className="space-y-2">
                    <h4 className="font-semibold text-gray-900 dark:text-white">
                      {selectedCategory === 'insurance' ? 'Coverage:' : 'Terms:'}
                    </h4>
                    <ul className="space-y-1">
                      {(selectedCategory === 'insurance' ? product.coverage : product.terms).map((item, index) => (
                        <li key={index} className="flex items-center text-sm text-gray-600 dark:text-gray-300">
                          <CheckCircleIcon className="h-4 w-4 text-green-500 mr-2 flex-shrink-0" />
                          {item}
                        </li>
                      ))}
                    </ul>
                  </div>
                  
                  <div className="mt-6 pt-4 border-t border-gray-200 dark:border-gray-700">
                    <h4 className="font-semibold text-gray-900 dark:text-white mb-2">
                      {selectedCategory === 'insurance' ? 'Features:' : 'Benefits:'}
                    </h4>
                    <div className="flex flex-wrap gap-2">
                      {(selectedCategory === 'insurance' ? product.features : product.benefits).map((item, index) => (
                        <span
                          key={index}
                          className="px-2 py-1 bg-gray-100 dark:bg-gray-700 text-xs text-gray-600 dark:text-gray-300 rounded-full"
                        >
                          {item}
                        </span>
                      ))}
                    </div>
                  </div>
                  
                  <button className="w-full mt-6 bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-4 rounded-xl transition-colors">
                    Apply Now
                  </button>
                </div>
              </div>
            )
          })}
        </div>

        {/* Information Section */}
        <div className="bg-blue-50 dark:bg-blue-900/20 rounded-2xl p-6 border border-blue-200 dark:border-blue-800">
          <div className="flex items-start space-x-4">
            <InformationCircleIcon className="h-6 w-6 text-blue-600 dark:text-blue-400 flex-shrink-0 mt-1" />
            <div>
              <h3 className="text-lg font-semibold text-blue-900 dark:text-blue-100 mb-2">
                How Partner Services Work
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm text-blue-800 dark:text-blue-200">
                <div>
                  <h4 className="font-semibold mb-1">1. Select Service</h4>
                  <p>Choose insurance protection or financing that fits your needs</p>
                </div>
                <div>
                  <h4 className="font-semibold mb-1">2. Apply Securely</h4>
                  <p>Fill out the application form with your details</p>
                </div>
                <div>
                  <h4 className="font-semibold mb-1">3. Get Protected</h4>
                  <p>Receive coverage or financing within 24-48 hours</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  </ProtectedRoute>
  )
} 