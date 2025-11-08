import React, { useState, useEffect } from 'react'
import { XMarkIcon, ChevronRightIcon, ChevronLeftIcon } from '@heroicons/react/24/outline'

interface TutorialStep {
  id: string
  title: string
  description: string
  target?: string
  position?: 'top' | 'bottom' | 'left' | 'right'
}

interface OnboardingTutorialProps {
  onComplete?: () => void
  onSkip?: () => void
}

const defaultSteps: TutorialStep[] = [
  {
    id: 'welcome',
    title: 'Welcome to SmartWatts!',
    description: 'Let\'s take a quick tour of your energy monitoring dashboard. This will only take a minute.',
    position: 'bottom'
  },
  {
    id: 'dashboard',
    title: 'Dashboard Overview',
    description: 'Your dashboard shows real-time energy consumption, costs, and key metrics at a glance.',
    target: '[data-tutorial="dashboard"]',
    position: 'bottom'
  },
  {
    id: 'devices',
    title: 'Device Management',
    description: 'Add and manage your energy monitoring devices here. Connect smart meters, inverters, and more.',
    target: '[data-tutorial="devices"]',
    position: 'bottom'
  },
  {
    id: 'analytics',
    title: 'Analytics & Reports',
    description: 'View detailed energy analytics, consumption patterns, and generate reports to optimize your usage.',
    target: '[data-tutorial="analytics"]',
    position: 'bottom'
  },
  {
    id: 'billing',
    title: 'Billing & Costs',
    description: 'Track your energy costs, view bills, and get savings recommendations to reduce your expenses.',
    target: '[data-tutorial="billing"]',
    position: 'bottom'
  },
  {
    id: 'complete',
    title: 'You\'re All Set!',
    description: 'You now know the basics. Start monitoring your energy consumption and save money today!',
    position: 'bottom'
  }
]

export default function OnboardingTutorial({ onComplete, onSkip }: OnboardingTutorialProps) {
  const [currentStep, setCurrentStep] = useState(0)
  const [isVisible, setIsVisible] = useState(false)
  const [targetElement, setTargetElement] = useState<HTMLElement | null>(null)

  useEffect(() => {
    // Check if user has completed tutorial
    const tutorialCompleted = localStorage.getItem('onboarding-tutorial-completed')
    if (!tutorialCompleted) {
      setIsVisible(true)
      updateTargetElement()
    }
  }, [])

  useEffect(() => {
    if (isVisible) {
      updateTargetElement()
    }
  }, [currentStep, isVisible])

  const updateTargetElement = () => {
    const step = defaultSteps[currentStep]
    if (step?.target) {
      const element = document.querySelector(step.target) as HTMLElement
      setTargetElement(element)
      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center' })
        element.style.transition = 'all 0.3s ease'
        element.style.transform = 'scale(1.05)'
        element.style.zIndex = '1000'
        setTimeout(() => {
          element.style.transform = 'scale(1)'
        }, 300)
      }
    } else {
      setTargetElement(null)
    }
  }

  const handleNext = () => {
    if (currentStep < defaultSteps.length - 1) {
      setCurrentStep(currentStep + 1)
    } else {
      handleComplete()
    }
  }

  const handlePrevious = () => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1)
    }
  }

  const handleSkip = () => {
    localStorage.setItem('onboarding-tutorial-completed', 'true')
    setIsVisible(false)
    if (onSkip) {
      onSkip()
    }
  }

  const handleComplete = () => {
    localStorage.setItem('onboarding-tutorial-completed', 'true')
    setIsVisible(false)
    if (onComplete) {
      onComplete()
    }
  }

  if (!isVisible) {
    return null
  }

  const step = defaultSteps[currentStep]
  const isFirstStep = currentStep === 0
  const isLastStep = currentStep === defaultSteps.length - 1

  return (
    <>
      {/* Overlay */}
      <div className="fixed inset-0 bg-black/50 z-40" onClick={handleSkip} />

      {/* Tutorial Card */}
      <div
        className={`fixed z-50 bg-white dark:bg-gray-800 rounded-lg shadow-xl p-6 max-w-md ${
          targetElement
            ? 'transform -translate-y-1/2'
            : 'top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2'
        }`}
        style={
          targetElement
            ? {
                top: `${targetElement.offsetTop + targetElement.offsetHeight + 20}px`,
                left: `${targetElement.offsetLeft}px`
              }
            : {}
        }
      >
        {/* Header */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <span className="text-sm font-medium text-gray-500 dark:text-gray-400">
              Step {currentStep + 1} of {defaultSteps.length}
            </span>
          </div>
          <button
            onClick={handleSkip}
            className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <XMarkIcon className="h-5 w-5" />
          </button>
        </div>

        {/* Progress Bar */}
        <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2 mb-4">
          <div
            className="bg-blue-600 h-2 rounded-full transition-all duration-300"
            style={{ width: `${((currentStep + 1) / defaultSteps.length) * 100}%` }}
          />
        </div>

        {/* Content */}
        <div className="mb-6">
          <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
            {step.title}
          </h3>
          <p className="text-gray-600 dark:text-gray-300">{step.description}</p>
        </div>

        {/* Actions */}
        <div className="flex items-center justify-between">
          <button
            onClick={handlePrevious}
            disabled={isFirstStep}
            className={`px-4 py-2 rounded-md text-sm font-medium ${
              isFirstStep
                ? 'text-gray-400 cursor-not-allowed'
                : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'
            }`}
          >
            <ChevronLeftIcon className="h-5 w-5 inline mr-1" />
            Previous
          </button>

          <div className="flex space-x-2">
            {!isLastStep && (
              <button
                onClick={handleSkip}
                className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-md"
              >
                Skip
              </button>
            )}
            <button
              onClick={handleNext}
              className="px-4 py-2 bg-blue-600 text-white rounded-md text-sm font-medium hover:bg-blue-700 transition-colors"
            >
              {isLastStep ? 'Get Started' : 'Next'}
              {!isLastStep && <ChevronRightIcon className="h-5 w-5 inline ml-1" />}
            </button>
          </div>
        </div>
      </div>
    </>
  )
}


