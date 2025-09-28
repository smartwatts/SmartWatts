import React, { useState, useEffect } from 'react'
import { XMarkIcon } from '@heroicons/react/24/outline'

export default function TermsOfService() {
  const [isPopup, setIsPopup] = useState(false)
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
    setIsPopup(typeof window !== 'undefined' && window.opener)
  }, [])

  const handleClose = () => {
    if (isPopup) {
      window.close()
    } else {
      window.history.back()
    }
  }

  if (!mounted) {
    return null
  }

  const content = (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 py-12">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Header */}
          <div className="text-center mb-12">
            <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-4">
              Terms of Service
            </h1>
            <p className="text-lg text-gray-600 dark:text-gray-400">
              Last updated: {new Date().toLocaleDateString()}
            </p>
          </div>

          {/* Content */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-8 space-y-8">
            
            {/* Introduction */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                1. Introduction
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  Welcome to SmartWatts ("we," "our," or "us"). These Terms of Service ("Terms") govern your use of our energy management platform and services (collectively, the "Service") operated by SmartWatts Limited, a company registered in Nigeria.
                </p>
                <p>
                  By accessing or using our Service, you agree to be bound by these Terms. If you disagree with any part of these terms, you may not access the Service.
                </p>
              </div>
            </section>

            {/* Acceptance of Terms */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                2. Acceptance of Terms
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  By creating an account, accessing, or using our Service, you acknowledge that you have read, understood, and agree to be bound by these Terms and our Privacy Policy. These Terms constitute a legally binding agreement between you and SmartWatts.
                </p>
              </div>
            </section>

            {/* Description of Service */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                3. Description of Service
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  SmartWatts provides an AI-powered energy monitoring and optimization platform that includes:
                </p>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Real-time energy consumption monitoring</li>
                  <li>AI-powered appliance recognition and analysis</li>
                  <li>Solar panel monitoring and optimization</li>
                  <li>Circuit-level energy management</li>
                  <li>Cost analysis and savings recommendations</li>
                  <li>Community benchmarking and insights</li>
                  <li>Mobile and web dashboard access</li>
                </ul>
              </div>
            </section>

            {/* User Accounts */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                4. User Accounts
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  To use our Service, you must create an account by providing accurate and complete information. You are responsible for:
                </p>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Maintaining the confidentiality of your account credentials</li>
                  <li>All activities that occur under your account</li>
                  <li>Notifying us immediately of any unauthorized use</li>
                  <li>Ensuring your account information remains accurate and up-to-date</li>
                </ul>
                <p>
                  We reserve the right to suspend or terminate accounts that violate these Terms or engage in fraudulent activity.
                </p>
              </div>
            </section>

            {/* Data and Privacy */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                5. Data Collection and Privacy
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  We collect and process your data in accordance with our Privacy Policy and applicable Nigerian data protection laws, including the Nigeria Data Protection Regulation (NDPR). By using our Service, you consent to:
                </p>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Collection of energy consumption data from your devices</li>
                  <li>Processing of personal information for service provision</li>
                  <li>Use of anonymized data for analytics and improvement</li>
                  <li>Sharing of data with authorized third-party service providers</li>
                </ul>
              </div>
            </section>

            {/* Service Availability */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                6. Service Availability
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  While we strive to provide continuous service availability, we do not guarantee uninterrupted access. The Service may be temporarily unavailable due to:
                </p>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Scheduled maintenance and updates</li>
                  <li>Technical difficulties or system failures</li>
                  <li>Force majeure events beyond our control</li>
                  <li>Internet connectivity issues</li>
                </ul>
                <p>
                  We will make reasonable efforts to notify users of planned maintenance and minimize service disruptions.
                </p>
              </div>
            </section>

            {/* User Responsibilities */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                7. User Responsibilities
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>You agree to:</p>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Use the Service only for lawful purposes</li>
                  <li>Not attempt to reverse engineer or compromise our systems</li>
                  <li>Not share your account credentials with others</li>
                  <li>Comply with all applicable laws and regulations</li>
                  <li>Respect the intellectual property rights of SmartWatts and third parties</li>
                  <li>Not use the Service to transmit malicious code or harmful content</li>
                </ul>
              </div>
            </section>

            {/* Intellectual Property */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                8. Intellectual Property
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  The Service and its original content, features, and functionality are owned by SmartWatts and are protected by international copyright, trademark, patent, trade secret, and other intellectual property laws.
                </p>
                <p>
                  You may not copy, modify, distribute, sell, or lease any part of our Service without our express written permission.
                </p>
              </div>
            </section>

            {/* Limitation of Liability */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                9. Limitation of Liability
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  To the maximum extent permitted by law, SmartWatts shall not be liable for any indirect, incidental, special, consequential, or punitive damages, including but not limited to loss of profits, data, or use, arising out of or relating to your use of the Service.
                </p>
                <p>
                  Our total liability to you for any damages shall not exceed the amount you paid us for the Service in the twelve (12) months preceding the claim.
                </p>
              </div>
            </section>

            {/* Termination */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                10. Termination
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  We may terminate or suspend your account immediately, without prior notice, for conduct that we believe violates these Terms or is harmful to other users, us, or third parties.
                </p>
                <p>
                  You may terminate your account at any time by contacting us. Upon termination, your right to use the Service will cease immediately.
                </p>
              </div>
            </section>

            {/* Governing Law */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                11. Governing Law
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  These Terms shall be governed by and construed in accordance with the laws of the Federal Republic of Nigeria. Any disputes arising from these Terms shall be subject to the exclusive jurisdiction of the courts of Nigeria.
                </p>
              </div>
            </section>

            {/* Changes to Terms */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                12. Changes to Terms
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  We reserve the right to modify these Terms at any time. We will notify users of any material changes via email or through the Service. Your continued use of the Service after such modifications constitutes acceptance of the updated Terms.
                </p>
              </div>
            </section>

            {/* Contact Information */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                13. Contact Information
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  If you have any questions about these Terms of Service, please contact us through our website.
                </p>
              </div>
            </section>

          </div>
        </div>
      </div>
  )

  if (isPopup) {
    return (
      <div className="fixed inset-0 bg-white z-50 overflow-auto">
        {/* Popup Header */}
        <div className="sticky top-0 bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between">
          <h1 className="text-lg font-semibold text-gray-900">Terms of Service</h1>
          <button
            onClick={handleClose}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <XMarkIcon className="h-5 w-5 text-gray-500" />
          </button>
        </div>
        {content}
      </div>
    )
  }

  return content
}
