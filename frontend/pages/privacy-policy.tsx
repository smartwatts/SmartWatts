import React, { useState, useEffect } from 'react'
import { XMarkIcon } from '@heroicons/react/24/outline'

export default function PrivacyPolicy() {
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
              Privacy Policy
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
                  SmartWatts Limited ("we," "our," or "us") is committed to protecting your privacy and personal data. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our energy management platform and services.
                </p>
                <p>
                  This policy complies with the Nigeria Data Protection Regulation (NDPR) and other applicable data protection laws. By using our Service, you consent to the data practices described in this policy.
                </p>
              </div>
            </section>

            {/* Information We Collect */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                2. Information We Collect
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">2.1 Personal Information</h3>
                <p>We collect personal information that you provide directly to us, including:</p>
                <ul className="list-disc pl-6 space-y-2">
                  <li><strong>Account Information:</strong> Name, email address, phone number, and password</li>
                  <li><strong>Property Information:</strong> Address, city, state, postal code, and property type</li>
                  <li><strong>Energy Information:</strong> Energy provider (DISCO), monthly energy bill range, and consumption patterns</li>
                  <li><strong>Payment Information:</strong> Billing details and payment history (processed securely through third-party providers)</li>
                </ul>

                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3 mt-6">2.2 Energy Data</h3>
                <p>We collect energy-related data to provide our services:</p>
                <ul className="list-disc pl-6 space-y-2">
                  <li><strong>Consumption Data:</strong> Real-time energy usage from your devices and appliances</li>
                  <li><strong>Device Information:</strong> Types, models, and specifications of your energy-consuming devices</li>
                  <li><strong>Solar Data:</strong> Solar panel performance, generation data, and inverter information</li>
                  <li><strong>Circuit Data:</strong> Circuit-level energy consumption and load profiles</li>
                  <li><strong>Environmental Data:</strong> Weather conditions and their impact on energy consumption</li>
                </ul>

                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3 mt-6">2.3 Technical Information</h3>
                <p>We automatically collect certain technical information:</p>
                <ul className="list-disc pl-6 space-y-2">
                  <li><strong>Device Information:</strong> IP address, browser type, operating system, and device identifiers</li>
                  <li><strong>Usage Data:</strong> How you interact with our Service, pages visited, and features used</li>
                  <li><strong>Log Data:</strong> Server logs, error reports, and performance metrics</li>
                  <li><strong>Location Data:</strong> General geographic location for service optimization</li>
                </ul>
              </div>
            </section>

            {/* How We Use Information */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                3. How We Use Your Information
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>We use your information for the following purposes:</p>
                
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3 mt-6">3.1 Service Provision</h3>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Provide energy monitoring and management services</li>
                  <li>Generate insights and recommendations for energy optimization</li>
                  <li>Process payments and manage your account</li>
                  <li>Provide customer support and technical assistance</li>
                </ul>

                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3 mt-6">3.2 Analytics and Improvement</h3>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Analyze energy consumption patterns and trends</li>
                  <li>Improve our algorithms and AI models</li>
                  <li>Develop new features and services</li>
                  <li>Conduct research and development</li>
                </ul>

                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3 mt-6">3.3 Communication</h3>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Send service-related notifications and updates</li>
                  <li>Provide energy-saving tips and recommendations</li>
                  <li>Send marketing communications (with your consent)</li>
                  <li>Respond to your inquiries and support requests</li>
                </ul>
              </div>
            </section>

            {/* Data Sharing */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                4. Information Sharing and Disclosure
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>We may share your information in the following circumstances:</p>
                
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3 mt-6">4.1 Service Providers</h3>
                <p>We share information with trusted third-party service providers who assist us in operating our Service:</p>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Cloud hosting and data storage providers</li>
                  <li>Payment processing services</li>
                  <li>Analytics and monitoring services</li>
                  <li>Customer support platforms</li>
                  <li>Energy data processing partners</li>
                </ul>

                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3 mt-6">4.2 Legal Requirements</h3>
                <p>We may disclose information when required by law or to:</p>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Comply with legal obligations or court orders</li>
                  <li>Protect our rights, property, or safety</li>
                  <li>Prevent fraud or illegal activities</li>
                  <li>Protect the rights and safety of our users</li>
                </ul>

                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3 mt-6">4.3 Business Transfers</h3>
                <p>In the event of a merger, acquisition, or sale of assets, your information may be transferred to the acquiring entity.</p>

                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3 mt-6">4.4 Anonymized Data</h3>
                <p>We may share aggregated, anonymized data that cannot identify you for research, analytics, and industry insights.</p>
              </div>
            </section>

            {/* Data Security */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                5. Data Security
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>We implement appropriate technical and organizational measures to protect your personal data:</p>
                <ul className="list-disc pl-6 space-y-2">
                  <li><strong>Encryption:</strong> Data is encrypted in transit and at rest using industry-standard protocols</li>
                  <li><strong>Access Controls:</strong> Strict access controls and authentication mechanisms</li>
                  <li><strong>Regular Audits:</strong> Regular security assessments and vulnerability testing</li>
                  <li><strong>Staff Training:</strong> Comprehensive data protection training for all employees</li>
                  <li><strong>Incident Response:</strong> Procedures for handling data breaches and security incidents</li>
                </ul>
                <p>
                  However, no method of transmission over the internet or electronic storage is 100% secure. While we strive to protect your data, we cannot guarantee absolute security.
                </p>
              </div>
            </section>

            {/* Your Rights */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                6. Your Data Protection Rights
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>Under the NDPR and applicable data protection laws, you have the following rights:</p>
                <ul className="list-disc pl-6 space-y-2">
                  <li><strong>Right of Access:</strong> Request copies of your personal data</li>
                  <li><strong>Right to Rectification:</strong> Correct inaccurate or incomplete data</li>
                  <li><strong>Right to Erasure:</strong> Request deletion of your personal data</li>
                  <li><strong>Right to Restrict Processing:</strong> Limit how we process your data</li>
                  <li><strong>Right to Data Portability:</strong> Receive your data in a structured format</li>
                  <li><strong>Right to Object:</strong> Object to processing based on legitimate interests</li>
                  <li><strong>Right to Withdraw Consent:</strong> Withdraw consent for data processing</li>
                </ul>
                <p>
                  To exercise these rights, please contact us at privacy@mysmartwatts.com. We will respond to your request within 30 days.
                </p>
              </div>
            </section>

            {/* Data Retention */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                7. Data Retention
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>We retain your personal data for as long as necessary to:</p>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Provide our services to you</li>
                  <li>Comply with legal obligations</li>
                  <li>Resolve disputes and enforce agreements</li>
                  <li>Improve our services and develop new features</li>
                </ul>
                <p>
                  Energy consumption data is typically retained for 7 years for analytical purposes. Account information is retained for the duration of your account plus 3 years for legal compliance.
                </p>
              </div>
            </section>

            {/* Cookies and Tracking */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                8. Cookies and Tracking Technologies
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>We use cookies and similar technologies to:</p>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Remember your preferences and settings</li>
                  <li>Analyze how you use our Service</li>
                  <li>Provide personalized content and recommendations</li>
                  <li>Improve our Service performance and functionality</li>
                </ul>
                <p>
                  You can control cookie settings through your browser preferences. However, disabling cookies may affect the functionality of our Service.
                </p>
              </div>
            </section>

            {/* International Transfers */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                9. International Data Transfers
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  Your data may be transferred to and processed in countries other than Nigeria. When we transfer data internationally, we ensure appropriate safeguards are in place, including:
                </p>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Adequacy decisions by relevant data protection authorities</li>
                  <li>Standard contractual clauses approved by the relevant authorities</li>
                  <li>Binding corporate rules and certification schemes</li>
                  <li>Other appropriate safeguards as required by law</li>
                </ul>
              </div>
            </section>

            {/* Children's Privacy */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                10. Children's Privacy
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  Our Service is not intended for children under 13 years of age. We do not knowingly collect personal information from children under 13. If we become aware that we have collected personal information from a child under 13, we will take steps to delete such information.
                </p>
              </div>
            </section>

            {/* Changes to Privacy Policy */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                11. Changes to This Privacy Policy
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  We may update this Privacy Policy from time to time. We will notify you of any material changes by:
                </p>
                <ul className="list-disc pl-6 space-y-2">
                  <li>Posting the updated policy on our website</li>
                  <li>Sending you an email notification</li>
                  <li>Providing notice through our Service</li>
                </ul>
                <p>
                  Your continued use of our Service after any changes constitutes acceptance of the updated Privacy Policy.
                </p>
              </div>
            </section>

            {/* Contact Information */}
            <section>
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
                12. Contact Us
              </h2>
              <div className="prose prose-gray dark:prose-invert max-w-none">
                <p>
                  If you have any questions about this Privacy Policy or our data practices, please contact us through our website.
                </p>
                <p className="mt-4">
                  You also have the right to lodge a complaint with the Nigeria Data Protection Commission if you believe your data protection rights have been violated.
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
          <h1 className="text-lg font-semibold text-gray-900">Privacy Policy</h1>
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
