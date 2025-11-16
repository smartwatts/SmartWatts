import React, { useEffect } from 'react'
import Head from 'next/head'
import Link from 'next/link'
import { 
  BoltIcon, 
  ChartBarIcon, 
  CogIcon, 
  SunIcon, 
  ShieldCheckIcon, 
  DevicePhoneMobileIcon,
  ArrowRightIcon,
  CheckIcon,
  PlayIcon,
  StarIcon,
  BuildingOfficeIcon,
  HomeIcon,
  CurrencyDollarIcon,
  ClockIcon,
  ExclamationTriangleIcon,
  ChartPieIcon,
  CpuChipIcon,
  WrenchScrewdriverIcon,
  EyeIcon,
  ArrowTrendingUpIcon,
  DocumentTextIcon,
  PhoneIcon,
  EnvelopeIcon,
  MapPinIcon
} from '@heroicons/react/24/outline'

export default function Index() {
  useEffect(() => {
    // Handle smooth scrolling for anchor links
    const handleSmoothScroll = (e: Event) => {
      const target = e.target as HTMLAnchorElement;
      if (target.hash) {
        e.preventDefault();
        const element = document.querySelector(target.hash);
        if (element) {
          // Add a small delay to ensure the page is ready
          setTimeout(() => {
            element.scrollIntoView({
              behavior: 'smooth',
              block: 'start',
            });
          }, 100);
        }
      }
    };

    // Add event listeners to all anchor links with a small delay
    const addListeners = () => {
      const links = document.querySelectorAll('a[href^="#"]');
      links.forEach(link => {
        link.addEventListener('click', handleSmoothScroll);
      });
    };

    // Wait for DOM to be ready
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', addListeners);
    } else {
      addListeners();
    }

    // Cleanup
    return () => {
      const links = document.querySelectorAll('a[href^="#"]');
      links.forEach(link => {
        link.removeEventListener('click', handleSmoothScroll);
      });
    };
  }, []);

  return (
    <>
      <Head>
        <title>SmartWatts - AI-Powered Energy Intelligence Platform | Nigeria's Leading Energy Management Solution</title>
        <meta name="description" content="Transform your energy from your biggest expense into your most optimized asset. SmartWatts is Nigeria's first AI-powered energy business intelligence platform, reducing costs by up to 30% through intelligent optimization." />
        <meta name="keywords" content="energy management platform Nigeria, smart energy monitoring system, AI-powered energy analytics, business intelligence energy platform, solar grid generator optimization" />
        <meta property="og:title" content="SmartWatts - AI-Powered Energy Intelligence Platform" />
        <meta property="og:description" content="The Business Intelligence Platform That Turns Energy from Your Biggest Expense into Your Most Optimized Asset" />
        <meta property="og:type" content="website" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <style jsx global>{`
          html {
            scroll-behavior: smooth;
            scroll-padding-top: 100px;
          }
        `}</style>
      </Head>

      <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Navigation */}
        <nav className="relative z-50 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex items-center justify-between h-16">
              <div className="flex items-center">
                <span className="text-4xl font-black tracking-tight transform hover:scale-105 transition-transform duration-300" style={{ fontFamily: 'Inter, system-ui, -apple-system, sans-serif', letterSpacing: '-0.02em' }}>
                  <span className="text-white">Smart</span><span className="text-green-500">Watts</span>
                </span>
            </div>
              <div className="hidden md:flex items-center space-x-8">
                <Link href="#features" className="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-medium transition-colors">Features</Link>
                {/* <Link href="#pricing" className="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-medium transition-colors">Pricing</Link> */}
                <Link href="#about" className="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-medium transition-colors">About</Link>
                <Link href="/contact" className="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-medium transition-colors">Contact</Link>
          </div>
          <div className="flex items-center space-x-4">
            <Link 
              href="/login" 
                  className="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-medium transition-colors"
            >
              Sign In
            </Link>
            <Link 
              href="/register" 
                  className="bg-green-600 text-white px-6 py-2 rounded-lg font-semibold hover:bg-green-700 transition-colors"
            >
                  Start Your Intelligent Energy Journey
            </Link>
              </div>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
        <section className="relative bg-gradient-to-br from-gray-50 dark:from-gray-800 to-blue-50 dark:to-gray-700 py-20 lg:py-32">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            {/* Headline - Full Width */}
            <div className="text-center mb-8">
              <h1 className="text-4xl lg:text-6xl font-bold text-gray-900 dark:text-white leading-tight">
                Nigeria's #1{' '}
                <span className="text-blue-600 dark:text-blue-400">AI-Powered</span>{' '}
                Energy Business Intelligence Platform
            </h1>
            </div>

            {/* Subheadline - Below Headline */}
            <div className="text-center mb-12">
              <p className="text-xl text-gray-600 dark:text-gray-300 leading-relaxed max-w-4xl mx-auto">
                See, Control, and Save on all your power sources — Grid, Generator, Solar, and Inverter — with enterprise-grade AI energy intelligence designed for Nigerian businesses and homes.
              </p>
            </div>

            {/* Dashboard Visual - Below Subheadline */}
            <div className="flex justify-center mb-12">
              <div className="relative max-w-2xl w-full">
                <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl p-8 border border-gray-200 dark:border-gray-700">
                  <div className="space-y-6">
                    <div className="flex items-center justify-between">
                      <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Energy Dashboard</h3>
                      <div className="flex items-center space-x-2">
                        <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                        <span className="text-sm text-gray-600 dark:text-gray-300">Live</span>
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div className="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
                        <div className="text-2xl font-bold text-blue-600 dark:text-blue-400">2.4kW</div>
                        <div className="text-sm text-gray-600 dark:text-gray-300">Solar Generation</div>
                      </div>
                      <div className="bg-green-50 dark:bg-green-900/20 rounded-lg p-4">
                        <div className="text-2xl font-bold text-green-600 dark:text-green-400">1.8kW</div>
                        <div className="text-sm text-gray-600 dark:text-gray-300">Grid Consumption</div>
                      </div>
                    </div>
                    <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                      <div className="text-3xl font-bold text-gray-900 dark:text-white">₦45,230</div>
                      <div className="text-sm text-gray-600 dark:text-gray-300">Monthly Savings</div>
                      <div className="text-xs text-green-600 dark:text-green-400">+28% vs last month</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* CTAs - Below Visual */}
            <div className="text-center">
            <div className="flex flex-col sm:flex-row items-center justify-center space-y-4 sm:space-y-0 sm:space-x-6">
              <Link 
                href="/register" 
                  className="bg-green-600 text-white px-8 py-4 rounded-lg font-semibold text-lg hover:bg-green-700 transition-colors flex items-center shadow-lg"
              >
                  Start Your Intelligent Energy Journey
                <ArrowRightIcon className="ml-2 h-5 w-5" />
              </Link>
                <button className="flex items-center text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 font-semibold text-lg">
                  <PlayIcon className="mr-2 h-6 w-6" />
                  Take Control of Your Energy Costs with AI-Powered Intelligence
                </button>
            </div>
          </div>
        </div>
      </section>

        {/* Value Propositions Section */}
        <section id="features" className="py-20 bg-white dark:bg-gray-800">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
              <h2 className="text-4xl font-bold text-gray-900 dark:text-white dark:text-white mb-4">
                Beyond Monitoring - This is Energy Intelligence
            </h2>
              <p className="text-xl text-gray-600 dark:text-gray-300 dark:text-gray-300 max-w-3xl mx-auto">
                Transform raw energy data into actionable business intelligence that drives real cost savings and operational efficiency.
            </p>
          </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              {/* Complete Energy Visibility */}
              <div className="bg-white dark:bg-gray-700 rounded-2xl p-8 shadow-lg border border-gray-200 dark:border-gray-600 hover:shadow-xl transition-all duration-300">
              <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center mb-6">
                  <EyeIcon className="w-6 h-6 text-blue-600" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white dark:text-white mb-4">Complete Energy Visibility</h3>
                <p className="text-gray-600 dark:text-gray-300 dark:text-gray-300 mb-4">
                  Unified view of all energy sources in real-time. Track consumption, costs, and efficiency across solar, grid, and generator systems.
                </p>
                <ul className="space-y-2 text-sm text-gray-600 dark:text-gray-300">
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2" />
                    Real-time multi-source monitoring
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2" />
                    Cost tracking per energy source
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2" />
                    Efficiency scoring and alerts
                  </li>
                </ul>
              </div>

              {/* AI-Powered Cost Optimization */}
              <div className="bg-white dark:bg-gray-700 rounded-2xl p-8 shadow-lg border border-gray-200 dark:border-gray-600 hover:shadow-xl transition-all duration-300">
                <div className="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center mb-6">
                  <CpuChipIcon className="w-6 h-6 text-green-600" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-4">AI-Powered Cost Optimization</h3>
                <p className="text-gray-600 dark:text-gray-300 mb-4">
                  Intelligent recommendations for when to use each energy source. Predictive analytics for maintenance and efficiency improvements.
                </p>
                <ul className="space-y-2 text-sm text-gray-600 dark:text-gray-300">
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2" />
                    Smart switching recommendations
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2" />
                    Predictive maintenance alerts
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2" />
                    Automated waste detection
                  </li>
                </ul>
            </div>

              {/* Business Intelligence Dashboard */}
              <div className="bg-white dark:bg-gray-700 rounded-2xl p-8 shadow-lg border border-gray-200 dark:border-gray-600 hover:shadow-xl transition-all duration-300">
                <div className="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center mb-6">
                  <ChartBarIcon className="w-6 h-6 text-purple-600" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Business Intelligence Dashboard</h3>
                <p className="text-gray-600 dark:text-gray-300 mb-4">
                  Executive-level reporting with actionable insights. Forecasting and budgeting tools with 3, 6, 12-month projections.
                </p>
                <ul className="space-y-2 text-sm text-gray-600 dark:text-gray-300">
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2" />
                    Executive KPI dashboards
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2" />
                    Budget planning and forecasting
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2" />
                    NERC-compliant reporting
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </section>

        {/* Target Market Section */}
        <section className="py-20 bg-gray-50 dark:bg-gray-900">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-16">
              <h2 className="text-4xl font-bold text-gray-900 dark:text-white mb-4">
                Built for Nigeria's Complex Energy Environment
              </h2>
              <p className="text-xl text-gray-600 dark:text-gray-300 max-w-3xl mx-auto">
                Designed specifically for Nigeria's unique energy challenges and opportunities.
              </p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
              {/* For Businesses */}
              <div className="bg-white dark:bg-gray-800 rounded-2xl p-8 shadow-lg">
                <div className="flex items-center mb-6">
                  <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center mr-4">
                    <BuildingOfficeIcon className="w-6 h-6 text-blue-600" />
                  </div>
                  <h3 className="text-2xl font-bold text-gray-900 dark:text-white">For Businesses</h3>
                </div>
                <p className="text-lg text-gray-600 dark:text-gray-300 mb-6">
                  Stop energy budget surprises. Get the visibility and control you need to optimize your biggest operational expense.
                </p>
                <div className="space-y-4">
                  <div className="flex items-start">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3 mt-1" />
                    <div>
                      <h4 className="font-semibold text-gray-900 dark:text-white">Multi-location Management</h4>
                      <p className="text-gray-600 dark:text-gray-300">Monitor and optimize energy across all your facilities</p>
                    </div>
                  </div>
                  <div className="flex items-start">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3 mt-1" />
                    <div>
                      <h4 className="font-semibold text-gray-900 dark:text-white">Advanced Reporting</h4>
                      <p className="text-gray-600 dark:text-gray-300">Executive dashboards with detailed cost breakdowns</p>
                    </div>
                  </div>
                  <div className="flex items-start">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3 mt-1" />
                    <div>
                      <h4 className="font-semibold text-gray-900 dark:text-white">API Integration</h4>
                      <p className="text-gray-600 dark:text-gray-300">Connect with your existing business systems</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* For Homes */}
              <div className="bg-white dark:bg-gray-800 rounded-2xl p-8 shadow-lg">
                <div className="flex items-center mb-6">
                  <div className="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center mr-4">
                    <HomeIcon className="w-6 h-6 text-green-600" />
                  </div>
                  <h3 className="text-2xl font-bold text-gray-900 dark:text-white">For Homes</h3>
                </div>
                <p className="text-lg text-gray-600 dark:text-gray-300 mb-6">
                  Take control of your power costs with smart decisions about when to use solar, grid, or generator power.
                </p>
                <div className="space-y-4">
                  <div className="flex items-start">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3 mt-1" />
                    <div>
                      <h4 className="font-semibold text-gray-900 dark:text-white">Smart Home Integration</h4>
                      <p className="text-gray-600 dark:text-gray-300">Connect with your existing solar and inverter systems</p>
                    </div>
                  </div>
                  <div className="flex items-start">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3 mt-1" />
                    <div>
                      <h4 className="font-semibold text-gray-900 dark:text-white">Cost Optimization</h4>
                      <p className="text-gray-600 dark:text-gray-300">AI recommendations for maximum savings</p>
                    </div>
                  </div>
                  <div className="flex items-start">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3 mt-1" />
                    <div>
                      <h4 className="font-semibold text-gray-900 dark:text-white">Mobile App</h4>
                      <p className="text-gray-600 dark:text-gray-300">Monitor and control from anywhere</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Social Proof Section */}
        <section className="py-20 bg-white dark:bg-gray-800">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-16">
              <h2 className="text-4xl font-bold text-gray-900 dark:text-white mb-4">
                Trusted by Forward-Thinking Organizations
              </h2>
              <p className="text-xl text-gray-600 dark:text-gray-300">
                Join businesses already saving thousands on energy costs
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-16">
              <div className="text-center">
                <div className="text-4xl font-bold text-green-600 mb-2">30%</div>
                <div className="text-gray-600 dark:text-gray-300">Average Cost Reduction</div>
              </div>
              <div className="text-center">
                <div className="text-4xl font-bold text-blue-600 mb-2">₦2.5M</div>
                <div className="text-gray-600 dark:text-gray-300">Average Annual Savings</div>
              </div>
              <div className="text-center">
                <div className="text-4xl font-bold text-purple-600 mb-2">3-6</div>
                <div className="text-gray-600 dark:text-gray-300">Months Payback Period</div>
              </div>
            </div>

            <div className="bg-gray-50 dark:bg-gray-700 rounded-2xl p-8 text-center">
              <h3 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">Designed for Nigeria</h3>
              <p className="text-lg text-gray-600 dark:text-gray-300 max-w-3xl mx-auto">
                Built specifically for Nigeria's unique energy challenges, regulatory environment, and market conditions. 
                Our local expertise ensures maximum value and compliance.
              </p>
            </div>
              </div>
        </section>

        {/* Pricing Section - DISABLED */}
        {/* 
        <section id="pricing" className="py-20 bg-gray-50 dark:bg-gray-900">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-16">
              <h2 className="text-4xl font-bold text-gray-900 dark:text-white mb-4">
                Investment That Pays for Itself
              </h2>
              <p className="text-xl text-gray-600 dark:text-gray-300 max-w-3xl mx-auto">
                Choose the plan that fits your energy management needs. All plans include our core AI-powered optimization features.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
              <div className="bg-white dark:bg-gray-800 rounded-2xl p-8 shadow-lg border border-gray-200 dark:border-gray-600">
                <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-2">Essential Home</h3>
                <div className="text-3xl font-bold text-gray-900 dark:text-white mb-4">₦15,000<span className="text-lg text-gray-600 dark:text-gray-300">/month</span></div>
                <ul className="space-y-3 mb-8">
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Full energy monitoring</span>
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Basic analytics</span>
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Mobile app access</span>
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Email support</span>
                  </li>
                </ul>
                <Link href="/register" className="w-full bg-gray-900 text-white py-3 rounded-lg font-semibold text-center block hover:bg-gray-800 transition-colors">
                  Get Started
                </Link>
              </div>

              <div className="bg-white dark:bg-gray-800 rounded-2xl p-8 shadow-lg border-2 border-green-500 relative">
                <div className="absolute -top-4 left-1/2 transform -translate-x-1/2">
                  <span className="bg-green-500 text-white px-4 py-1 rounded-full text-sm font-semibold">Most Popular</span>
                </div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-2">Smart Home Plus</h3>
                <div className="text-3xl font-bold text-gray-900 dark:text-white mb-4">₦25,000<span className="text-lg text-gray-600 dark:text-gray-300">/month</span></div>
                <ul className="space-y-3 mb-8">
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Everything in Essential</span>
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Advanced AI insights</span>
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Forecasting & optimization</span>
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Priority support</span>
                  </li>
                </ul>
                <Link href="/register" className="w-full bg-green-600 text-white py-3 rounded-lg font-semibold text-center block hover:bg-green-700 transition-colors">
                  Start Free Trial
                </Link>
              </div>

              <div className="bg-white dark:bg-gray-800 rounded-2xl p-8 shadow-lg border border-gray-200 dark:border-gray-600">
                <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-2">SME Professional</h3>
                <div className="text-3xl font-bold text-gray-900 dark:text-white mb-4">₦45,000<span className="text-lg text-gray-600 dark:text-gray-300">/month</span></div>
                <ul className="space-y-3 mb-8">
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Multi-location management</span>
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Advanced reporting</span>
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">API access</span>
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Dedicated support</span>
                  </li>
                </ul>
                <Link href="/contact" className="w-full bg-gray-900 text-white py-3 rounded-lg font-semibold text-center block hover:bg-gray-800 transition-colors">
                  Contact Sales
                </Link>
              </div>

              <div className="bg-white dark:bg-gray-800 rounded-2xl p-8 shadow-lg border border-gray-200 dark:border-gray-600">
                <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-2">Enterprise</h3>
                <div className="text-3xl font-bold text-gray-900 dark:text-white mb-4">Custom<span className="text-lg text-gray-600 dark:text-gray-300"> pricing</span></div>
                <ul className="space-y-3 mb-8">
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Custom deployment</span>
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Dedicated support</span>
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">Compliance modules</span>
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3" />
                    <span className="text-gray-600 dark:text-gray-300">White-label options</span>
                  </li>
                </ul>
                <Link href="/contact" className="w-full bg-gray-900 text-white py-3 rounded-lg font-semibold text-center block hover:bg-gray-800 transition-colors">
                  Contact Sales
                </Link>
              </div>
            </div>

            <div className="mt-16 text-center">
              <div className="bg-green-50 dark:bg-green-900/20 rounded-2xl p-8 max-w-4xl mx-auto">
                <h3 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">Special Offers</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                  <div>
                    <h4 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">New Customer Incentive</h4>
                    <p className="text-gray-600 dark:text-gray-300">Get 3 months free when you purchase SmartWatts hardware</p>
                  </div>
                  <div>
                    <h4 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">14-Day Trial</h4>
                    <p className="text-gray-600 dark:text-gray-300">Available for homes with compatible inverters</p>
                  </div>
                </div>
            </div>
          </div>
        </div>
      </section>
        */}

      {/* CTA Section */}
        <section className="py-20 bg-gradient-to-r from-blue-600 to-blue-800">
          <div className="max-w-4xl mx-auto text-center px-4 sm:px-6 lg:px-8">
            <h2 className="text-4xl font-bold text-white mb-6">
              Ready to Transform Your Energy Management?
            </h2>
            <p className="text-xl text-blue-100 mb-8">
              Join the energy intelligence revolution. Start your free trial today and see how much you can save.
            </p>
            <div className="flex flex-col sm:flex-row items-center justify-center space-y-4 sm:space-y-0 sm:space-x-6">
              <Link 
                href="/register" 
                className="bg-green-600 text-white px-8 py-4 rounded-lg font-semibold text-lg hover:bg-green-700 transition-colors flex items-center shadow-lg"
              >
                Start Your Intelligent Energy Journey
                <ArrowRightIcon className="ml-2 h-5 w-5" />
              </Link>
              <Link 
                href="/contact" 
                className="border-2 border-white text-white px-8 py-4 rounded-lg font-semibold text-lg hover:bg-white hover:text-blue-600 transition-colors"
              >
                Contact Sales
              </Link>
          </div>
        </div>
      </section>

      {/* Footer */}
        <footer className="bg-gray-900 dark:bg-gray-800 text-white py-16">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
              <div className="col-span-1 md:col-span-2">
                <div className="flex items-center mb-4">
                  <span className="text-3xl font-black tracking-tight" style={{ fontFamily: 'Inter, system-ui, -apple-system, sans-serif', letterSpacing: '-0.02em' }}>
                    <span className="text-white">Smart</span><span className="text-green-500">Watts</span>
                  </span>
                </div>
                <p className="text-gray-400 mb-6 max-w-md">
                  Nigeria's first AI-powered energy business intelligence platform. 
                  Transforming energy from your biggest expense into your most optimized asset.
                </p>
                <div className="flex items-center space-x-4 text-sm text-gray-400">
                  <div className="flex items-center">
                    <MapPinIcon className="w-4 h-4 mr-2" />
                    <span>33 Parakou Street, Wuse2, Abuja</span>
                  </div>
                </div>
              </div>
              
            <div>
              <h3 className="font-semibold mb-4">Product</h3>
              <ul className="space-y-2 text-gray-400">
                  <li><Link href="#features" className="hover:text-white transition-colors">Features</Link></li>
                  {/* <li><Link href="#pricing" className="hover:text-white transition-colors">Pricing</Link></li> */}
                  <li><Link href="/contact" className="hover:text-white transition-colors">Demo</Link></li>
                  <li><Link href="/contact" className="hover:text-white transition-colors">API</Link></li>
              </ul>
            </div>
              
            <div>
              <h3 className="font-semibold mb-4">Support</h3>
              <ul className="space-y-2 text-gray-400">
                  <li><Link href="/contact" className="hover:text-white transition-colors">Contact Us</Link></li>
                  <li><Link href="/contact" className="hover:text-white transition-colors">Help Center</Link></li>
                  <li><Link href="/contact" className="hover:text-white transition-colors">Documentation</Link></li>
                  <li><Link href="/contact" className="hover:text-white transition-colors">Status</Link></li>
              </ul>
            </div>
            </div>
            
            <div className="border-t border-gray-800 mt-12 pt-8 flex flex-col md:flex-row items-center justify-between">
              <p className="text-gray-400 text-sm">
                © {new Date().getFullYear()} SmartWatts Limited. All rights reserved.
              </p>
              <div className="flex items-center space-x-6 mt-4 md:mt-0">
                <Link href="/terms-of-service" className="text-gray-400 hover:text-white text-sm transition-colors">
                  Terms of Service
                </Link>
                <Link href="/privacy-policy" className="text-gray-400 hover:text-white text-sm transition-colors">
                  Privacy Policy
                </Link>
          </div>
          </div>
        </div>
      </footer>
    </div>
    </>
  )
} 