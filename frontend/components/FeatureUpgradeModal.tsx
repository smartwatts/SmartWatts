import React from 'react'
import { XMarkIcon, StarIcon, CheckIcon } from '@heroicons/react/24/outline'

interface FeatureUpgradeModalProps {
  isOpen: boolean
  onClose: () => void
  featureName: string
  featureDescription: string
  currentPlan: string
}

export default function FeatureUpgradeModal({
  isOpen,
  onClose,
  featureName,
  featureDescription,
  currentPlan
}: FeatureUpgradeModalProps) {
  if (!isOpen) return null

  const plans = [
    {
      name: 'Premium Plan',
      price: '₦5,000',
      duration: 'per month',
      features: [
        'All Freemium features',
        featureName,
        'Enhanced analytics',
        'Custom reports',
        'API access',
        'Priority support',
        'Up to 20 devices',
        'Multi-user access'
      ],
      recommended: true
    },
    {
      name: 'Business Plan',
      price: '₦15,000',
      duration: 'per month',
      features: [
        'All Premium features',
        'Unlimited devices',
        'Multi-site management',
        'Custom integrations',
        'Dedicated support',
        'White-label options'
      ],
      recommended: false
    }
  ]

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
        <div className="relative transform overflow-hidden rounded-lg bg-white dark:bg-gray-800 px-4 pb-4 pt-5 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-2xl sm:p-6">
          {/* Header */}
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                Upgrade to Access {featureName}
              </h3>
              <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                {featureDescription}
              </p>
            </div>
            <button
              onClick={onClose}
              className="rounded-md bg-gray-100 dark:bg-gray-700 p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
            >
              <XMarkIcon className="h-5 w-5" />
            </button>
          </div>

          {/* Plans */}
          <div className="space-y-4">
            {plans.map((plan, index) => (
              <div
                key={index}
                className={`relative rounded-lg border-2 p-6 transition-all hover:shadow-lg ${
                  plan.recommended
                    ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                    : 'border-gray-200 dark:border-gray-600'
                }`}
              >
                {plan.recommended && (
                  <div className="absolute -top-3 left-6">
                    <span className="inline-flex items-center rounded-full bg-blue-600 px-3 py-1 text-xs font-medium text-white">
                      <StarIcon className="mr-1 h-3 w-3" />
                      Recommended
                    </span>
                  </div>
                )}

                <div className="flex items-center justify-between mb-4">
                  <h4 className="text-lg font-semibold text-gray-900 dark:text-white">
                    {plan.name}
                  </h4>
                  <div className="text-right">
                    <div className="text-2xl font-bold text-gray-900 dark:text-white">
                      {plan.price}
                    </div>
                    <div className="text-sm text-gray-600 dark:text-gray-400">
                      {plan.duration}
                    </div>
                  </div>
                </div>

                <ul className="space-y-2 mb-6">
                  {plan.features.map((feature, featureIndex) => (
                    <li key={featureIndex} className="flex items-center text-sm">
                      <CheckIcon className="mr-2 h-4 w-4 text-green-500" />
                      {feature}
                    </li>
                  ))}
                </ul>

                <button
                  className={`w-full rounded-lg px-4 py-2 font-medium transition-colors ${
                    plan.recommended
                      ? 'bg-blue-600 text-white hover:bg-blue-700'
                      : 'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-white hover:bg-gray-200 dark:hover:bg-gray-600'
                  }`}
                >
                  Choose {plan.name}
                </button>
              </div>
            ))}
          </div>

          {/* Footer */}
          <div className="mt-6 text-center">
            <p className="text-xs text-gray-500 dark:text-gray-400">
              Current plan: {currentPlan} • Upgrade anytime • Cancel anytime
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
