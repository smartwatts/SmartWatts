import React, { useEffect, useState } from 'react'
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
  MapPinIcon,
  Bars3Icon,
  XMarkIcon,
  SparklesIcon,
  LightBulbIcon,
  GlobeAltIcon
} from '@heroicons/react/24/outline'

export default function Index() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  useEffect(() => {
    // Handle smooth scrolling for anchor links
    const handleSmoothScroll = (e: Event) => {
      const target = e.target as HTMLAnchorElement;
      if (target.hash) {
        e.preventDefault();
        const element = document.querySelector(target.hash);
        if (element) {
          setMobileMenuOpen(false); // Close mobile menu on navigation
          setTimeout(() => {
            element.scrollIntoView({
              behavior: 'smooth',
              block: 'start',
            });
          }, 100);
        }
      }
    };

    const addListeners = () => {
      const links = document.querySelectorAll('a[href^="#"]');
      links.forEach(link => {
        link.addEventListener('click', handleSmoothScroll);
      });
    };

    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', addListeners);
    } else {
      addListeners();
    }

    return () => {
      const links = document.querySelectorAll('a[href^="#"]');
      links.forEach(link => {
        link.removeEventListener('click', handleSmoothScroll);
      });
    };
  }, []);

  // Prevent body scroll when mobile menu is open
  useEffect(() => {
    if (mobileMenuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [mobileMenuOpen]);

  return (
    <>
      <Head>
        <title>SmartWatts - AI-Powered Energy Intelligence Platform | Nigeria's Leading Energy Management Solution</title>
        <meta name="description" content="Transform your energy from your biggest expense into your most optimized asset. SmartWatts is Nigeria's first AI-powered energy business intelligence platform, reducing costs by up to 30% through intelligent optimization." />
        <meta name="keywords" content="energy management platform Nigeria, smart energy monitoring system, AI-powered energy analytics, business intelligence energy platform, solar grid generator optimization" />
        <meta property="og:title" content="SmartWatts - AI-Powered Energy Intelligence Platform" />
        <meta property="og:description" content="The Business Intelligence Platform That Turns Energy from Your Biggest Expense into Your Most Optimized Asset" />
        <meta property="og:type" content="website" />
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=5" />
        <style jsx global>{`
          html {
            scroll-behavior: smooth;
            scroll-padding-top: 80px;
          }
        `}</style>
      </Head>

      <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Navigation */}
        <nav className="fixed top-0 left-0 right-0 z-50 bg-white/95 dark:bg-gray-900/95 backdrop-blur-md border-b border-gray-200 dark:border-gray-700 shadow-sm">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex items-center justify-between h-16 md:h-20">
              {/* Logo */}
              <Link href="/" className="flex items-center group">
                <span className="text-4xl font-black tracking-tight transform hover:scale-105 transition-transform duration-300" style={{ fontFamily: 'Inter, system-ui, -apple-system, sans-serif', letterSpacing: '-0.02em' }}>
                  <span className="text-gray-900 dark:text-white">Smart</span>
                  <span className="text-green-500">Watts</span>
                </span>
              </Link>

              {/* Desktop Navigation */}
              <div className="hidden md:flex items-center space-x-8">
                <Link href="#features" className="text-gray-600 dark:text-gray-300 hover:text-green-600 dark:hover:text-green-400 font-medium transition-colors">
                  Features
                </Link>
                <Link href="#about" className="text-gray-600 dark:text-gray-300 hover:text-green-600 dark:hover:text-green-400 font-medium transition-colors">
                  About
                </Link>
                <Link href="/contact" className="text-gray-600 dark:text-gray-300 hover:text-green-600 dark:hover:text-green-400 font-medium transition-colors">
                  Contact
                </Link>
          </div>

              {/* Desktop CTA Buttons */}
              <div className="hidden md:flex items-center space-x-4">
            <Link 
              href="/login" 
                  className="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-medium transition-colors"
            >
              Sign In
            </Link>
            <Link 
              href="/register" 
                  className="bg-green-600 text-white px-5 py-2.5 rounded-lg font-semibold hover:bg-green-700 transition-all shadow-md hover:shadow-lg"
            >
                  Get Started
                </Link>
              </div>

              {/* Mobile Menu Button */}
              <button
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                className="md:hidden p-2 rounded-lg text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                aria-label="Toggle menu"
              >
                {mobileMenuOpen ? (
                  <XMarkIcon className="h-6 w-6" />
                ) : (
                  <Bars3Icon className="h-6 w-6" />
                )}
              </button>
            </div>
          </div>

          {/* Mobile Menu Overlay */}
          {mobileMenuOpen && (
            <div className="md:hidden fixed inset-0 top-16 bg-gray-900/50 backdrop-blur-sm z-40" onClick={() => setMobileMenuOpen(false)} />
          )}

          {/* Mobile Menu Drawer */}
          <div className={`md:hidden fixed top-16 right-0 h-[calc(100vh-4rem)] w-64 bg-white dark:bg-gray-900 border-l border-gray-200 dark:border-gray-700 shadow-xl transform transition-transform duration-300 ease-in-out z-50 ${mobileMenuOpen ? 'translate-x-0' : 'translate-x-full'}`}>
            <div className="flex flex-col h-full p-6 space-y-6">
              {/* Mobile Navigation Links */}
              <div className="space-y-4">
                <Link 
                  href="#features" 
                  className="block text-gray-700 dark:text-gray-300 hover:text-green-600 dark:hover:text-green-400 font-medium py-2 transition-colors"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Features
                </Link>
                <Link 
                  href="#about" 
                  className="block text-gray-700 dark:text-gray-300 hover:text-green-600 dark:hover:text-green-400 font-medium py-2 transition-colors"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  About
                </Link>
                <Link 
                  href="/contact" 
                  className="block text-gray-700 dark:text-gray-300 hover:text-green-600 dark:hover:text-green-400 font-medium py-2 transition-colors"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Contact
                </Link>
              </div>

              {/* Mobile CTA Buttons */}
              <div className="space-y-3 pt-6 border-t border-gray-200 dark:border-gray-700">
                <Link 
                  href="/login" 
                  className="block text-center text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-medium py-3 px-4 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-all"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Sign In
                </Link>
                <Link 
                  href="/register" 
                  className="block text-center bg-green-600 text-white px-4 py-3 rounded-lg font-semibold hover:bg-green-700 transition-all shadow-md"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Get Started
            </Link>
              </div>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
        <section className="relative pt-24 md:pt-32 pb-16 md:pb-24 bg-gradient-to-br from-white via-green-50/30 to-blue-50/30 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 overflow-hidden">
          {/* Background Pattern */}
          <div className="absolute inset-0 opacity-5">
            <div className="absolute inset-0" style={{ backgroundImage: 'radial-gradient(circle at 1px 1px, rgb(0, 135, 81) 1px, transparent 0)', backgroundSize: '40px 40px' }}></div>
          </div>

          <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            {/* Trust Badge */}
            <div className="flex justify-center mb-6">
              <div className="inline-flex items-center space-x-2 bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300 px-4 py-2 rounded-full text-sm font-medium">
                <ShieldCheckIcon className="w-4 h-4" />
                <span>NERC-Compliant • Made for Nigeria</span>
              </div>
            </div>

            {/* Main Headline */}
            <div className="text-center mb-6">
              <h1 className="text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-bold text-gray-900 dark:text-white leading-tight">
                Nigeria's #1{' '}
                <span className="text-blue-600 dark:text-blue-400">AI-Powered</span>{' '}
                <br className="hidden sm:block" />
                Energy Business Intelligence Platform
            </h1>
            </div>

            {/* Subheadline */}
            <div className="text-center mb-10 md:mb-12">
              <p className="text-xl text-gray-600 dark:text-gray-300 leading-relaxed max-w-4xl mx-auto">
                See, Control, and Save on all your power sources — Grid, Generator, Solar, and Inverter — with enterprise-grade AI energy intelligence designed for Nigerian businesses and homes.
              </p>
            </div>

            {/* Stats Row */}
            <div className="grid grid-cols-3 gap-4 md:gap-8 max-w-3xl mx-auto mb-10 md:mb-12">
              <div className="text-center">
                <div className="flex items-center justify-center mb-2">
                  <CurrencyDollarIcon className="w-6 h-6 md:w-8 md:h-8 text-green-600 dark:text-green-400 mr-2" />
                  <div className="text-2xl md:text-4xl font-bold text-green-600 dark:text-green-400">30%</div>
                </div>
                <div className="text-xs md:text-sm text-gray-600 dark:text-gray-400">Cost Savings</div>
              </div>
              <div className="text-center">
                <div className="flex items-center justify-center mb-2">
                  <ArrowTrendingUpIcon className="w-6 h-6 md:w-8 md:h-8 text-blue-600 dark:text-blue-400 mr-2" />
                  <div className="text-2xl md:text-4xl font-bold text-blue-600 dark:text-blue-400">₦2.5M</div>
                </div>
                <div className="text-xs md:text-sm text-gray-600 dark:text-gray-400">Avg. Yearly</div>
              </div>
              <div className="text-center">
                <div className="flex items-center justify-center mb-2">
                  <ClockIcon className="w-6 h-6 md:w-8 md:h-8 text-purple-600 dark:text-purple-400 mr-2" />
                  <div className="text-2xl md:text-4xl font-bold text-purple-600 dark:text-purple-400">24/7</div>
                </div>
                <div className="text-xs md:text-sm text-gray-600 dark:text-gray-400">Monitoring</div>
              </div>
            </div>

            {/* Dashboard Preview */}
            <div className="flex justify-center mb-8 md:mb-10">
              <div className="relative max-w-3xl w-full">
                <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl p-4 md:p-8 border border-gray-200 dark:border-gray-700">
                  <div className="space-y-4 md:space-y-6">
                    <div className="flex items-center justify-between">
                      <h3 className="text-base md:text-lg font-semibold text-gray-900 dark:text-white">Live Energy Dashboard</h3>
                      <div className="flex items-center space-x-2">
                        <div className="w-2 h-2 md:w-3 md:h-3 bg-green-500 rounded-full animate-pulse"></div>
                        <span className="text-xs md:text-sm text-gray-600 dark:text-gray-300">Live</span>
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-3 md:gap-4">
                      <div className="bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 rounded-lg p-3 md:p-4">
                        <div className="flex items-center mb-1">
                          <SunIcon className="w-4 h-4 md:w-5 md:h-5 text-blue-600 dark:text-blue-400 mr-2" />
                          <div className="text-xl md:text-2xl font-bold text-blue-600 dark:text-blue-400">2.4kW</div>
                        </div>
                        <div className="text-xs md:text-sm text-gray-600 dark:text-gray-300">Solar Generation</div>
                      </div>
                      <div className="bg-gradient-to-br from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 rounded-lg p-3 md:p-4">
                        <div className="flex items-center mb-1">
                          <BoltIcon className="w-4 h-4 md:w-5 md:h-5 text-green-600 dark:text-green-400 mr-2" />
                          <div className="text-xl md:text-2xl font-bold text-green-600 dark:text-green-400">1.8kW</div>
                        </div>
                        <div className="text-xs md:text-sm text-gray-600 dark:text-gray-300">Grid Usage</div>
                      </div>
                    </div>
                    <div className="bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-600 rounded-lg p-3 md:p-4">
                      <div className="flex items-center justify-between mb-1">
                        <div className="flex items-center">
                          <ChartPieIcon className="w-5 h-5 md:w-6 md:h-6 text-gray-700 dark:text-gray-300 mr-2" />
                          <div className="text-2xl md:text-3xl font-bold text-gray-900 dark:text-white">₦45,230</div>
                        </div>
                        <ArrowTrendingUpIcon className="w-5 h-5 md:w-6 md:h-6 text-green-600 dark:text-green-400" />
                      </div>
                      <div className="text-xs md:text-sm text-gray-600 dark:text-gray-300">Monthly Savings</div>
                      <div className="text-xs text-green-600 dark:text-green-400 flex items-center">
                        <StarIcon className="w-3 h-3 mr-1" />
                        +28% vs last month
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* CTA Buttons */}
            <div className="text-center">
            <div className="flex flex-col sm:flex-row items-center justify-center space-y-4 sm:space-y-0 sm:space-x-6">
              <Link 
                href="/register" 
                  className="w-full sm:w-auto bg-green-600 text-white px-6 md:px-8 py-3 md:py-4 rounded-lg font-semibold text-base md:text-lg hover:bg-green-700 transition-all flex items-center justify-center shadow-lg hover:shadow-xl"
              >
                  Start Your Free Trial
                <ArrowRightIcon className="ml-2 h-5 w-5" />
              </Link>
                <Link
                  href="/contact"
                  className="w-full sm:w-auto border-2 border-green-600 text-green-600 dark:text-green-400 px-6 md:px-8 py-3 md:py-4 rounded-lg font-semibold text-base md:text-lg hover:bg-green-50 dark:hover:bg-green-900/20 transition-all flex items-center justify-center"
                >
                  <PlayIcon className="mr-2 h-5 w-5" />
                  Watch Demo
                </Link>
            </div>
          </div>
        </div>
      </section>

        {/* Features Section */}
        <section id="features" className="py-16 md:py-20 bg-white dark:bg-gray-900">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-12 md:mb-16">
              <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-4">
                Beyond Monitoring — This is Energy Intelligence
            </h2>
              <p className="text-lg md:text-xl text-gray-600 dark:text-gray-300 max-w-3xl mx-auto">
                Transform raw energy data into actionable business intelligence that drives real cost savings and operational efficiency.
            </p>
          </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 md:gap-8">
              {/* Complete Energy Visibility */}
              <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 md:p-8 shadow-lg border border-gray-200 dark:border-gray-700 hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
                <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-xl flex items-center justify-center mb-6">
                  <EyeIcon className="w-6 h-6 text-blue-600 dark:text-blue-400" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Complete Energy Visibility</h3>
                <p className="text-gray-600 dark:text-gray-300 mb-4">
                  Unified view of all energy sources in real-time. Track consumption, costs, and efficiency across solar, grid, and generator systems.
                </p>
                <ul className="space-y-2 text-sm text-gray-600 dark:text-gray-300">
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2 flex-shrink-0" />
                    Real-time multi-source monitoring
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2 flex-shrink-0" />
                    Cost tracking per energy source
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2 flex-shrink-0" />
                    Efficiency scoring and alerts
                  </li>
                </ul>
              </div>

              {/* AI-Powered Cost Optimization */}
              <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 md:p-8 shadow-lg border border-gray-200 dark:border-gray-700 hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
                <div className="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-xl flex items-center justify-center mb-6">
                  <CpuChipIcon className="w-6 h-6 text-green-600 dark:text-green-400" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-4">AI-Powered Cost Optimization</h3>
                <p className="text-gray-600 dark:text-gray-300 mb-4">
                  Intelligent recommendations for when to use each energy source. Predictive analytics for maintenance and efficiency improvements.
                </p>
                <ul className="space-y-2 text-sm text-gray-600 dark:text-gray-300">
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2 flex-shrink-0" />
                    Smart switching recommendations
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2 flex-shrink-0" />
                    Predictive maintenance alerts
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2 flex-shrink-0" />
                    Automated waste detection
                  </li>
                </ul>
            </div>

              {/* Business Intelligence Dashboard */}
              <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 md:p-8 shadow-lg border border-gray-200 dark:border-gray-700 hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
                <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/30 rounded-xl flex items-center justify-center mb-6">
                  <ChartBarIcon className="w-6 h-6 text-purple-600 dark:text-purple-400" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Business Intelligence Dashboard</h3>
                <p className="text-gray-600 dark:text-gray-300 mb-4">
                  Executive-level reporting with actionable insights. Forecasting and budgeting tools with 3, 6, 12-month projections.
                </p>
                <ul className="space-y-2 text-sm text-gray-600 dark:text-gray-300">
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2 flex-shrink-0" />
                    Executive KPI dashboards
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2 flex-shrink-0" />
                    Budget planning and forecasting
                  </li>
                  <li className="flex items-center">
                    <CheckIcon className="w-4 h-4 text-green-500 mr-2 flex-shrink-0" />
                    NERC-compliant reporting
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </section>

        {/* Target Market Section */}
        <section className="py-16 md:py-20 bg-gray-50 dark:bg-gray-800">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-12 md:mb-16">
              <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-4">
                Built for Nigeria's Complex Energy Environment
              </h2>
              <p className="text-lg md:text-xl text-gray-600 dark:text-gray-300 max-w-3xl mx-auto">
                Designed specifically for Nigeria's unique energy challenges and opportunities.
              </p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 md:gap-12">
              {/* For Businesses */}
              <div className="bg-white dark:bg-gray-900 rounded-2xl p-6 md:p-8 shadow-lg">
                <div className="flex items-center mb-6">
                  <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-xl flex items-center justify-center mr-4">
                    <BuildingOfficeIcon className="w-6 h-6 text-blue-600 dark:text-blue-400" />
                  </div>
                  <h3 className="text-xl md:text-2xl font-bold text-gray-900 dark:text-white">For Businesses</h3>
                </div>
                <p className="text-base md:text-lg text-gray-600 dark:text-gray-300 mb-6">
                  Stop energy budget surprises. Get the visibility and control you need to optimize your biggest operational expense.
                </p>
                <div className="space-y-4">
                  <div className="flex items-start">
                    <CheckIcon className="w-5 h-5 text-green-500 mr-3 mt-1 flex-shrink-0" />
                    <div>
                      <h4 className="font-semibold text-gray-900 dark:text-white">Multi-location Management</h4>
                      <p className="text-sm text-gray-600 dark:text-gray-300">Monitor and optimize energy across all your facilities</p>
                    </div>
                  </div>
                  <div className="flex items-start">
                    <DocumentTextIcon className="w-5 h-5 text-green-500 mr-3 mt-1 flex-shrink-0" />
                    <div>
                      <h4 className="font-semibold text-gray-900 dark:text-white">Advanced Reporting</h4>
                      <p className="text-sm text-gray-600 dark:text-gray-300">Executive dashboards with detailed cost breakdowns</p>
                    </div>
                  </div>
                  <div className="flex items-start">
                    <CogIcon className="w-5 h-5 text-green-500 mr-3 mt-1 flex-shrink-0" />
                    <div>
                      <h4 className="font-semibold text-gray-900 dark:text-white">API Integration</h4>
                      <p className="text-sm text-gray-600 dark:text-gray-300">Connect with your existing business systems</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* For Homes */}
              <div className="bg-white dark:bg-gray-900 rounded-2xl p-6 md:p-8 shadow-lg">
                <div className="flex items-center mb-6">
                  <div className="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-xl flex items-center justify-center mr-4">
                    <HomeIcon className="w-6 h-6 text-green-600 dark:text-green-400" />
                  </div>
                  <h3 className="text-xl md:text-2xl font-bold text-gray-900 dark:text-white">For Homes</h3>
                </div>
                <p className="text-base md:text-lg text-gray-600 dark:text-gray-300 mb-6">
                  Take control of your power costs with smart decisions about when to use solar, grid, or generator power.
                </p>
                <div className="space-y-4">
                  <div className="flex items-start">
                    <WrenchScrewdriverIcon className="w-5 h-5 text-green-500 mr-3 mt-1 flex-shrink-0" />
                    <div>
                      <h4 className="font-semibold text-gray-900 dark:text-white">Smart Home Integration</h4>
                      <p className="text-sm text-gray-600 dark:text-gray-300">Connect with your existing solar and inverter systems</p>
                    </div>
                  </div>
                  <div className="flex items-start">
                    <ExclamationTriangleIcon className="w-5 h-5 text-green-500 mr-3 mt-1 flex-shrink-0" />
                    <div>
                      <h4 className="font-semibold text-gray-900 dark:text-white">Cost Optimization</h4>
                      <p className="text-sm text-gray-600 dark:text-gray-300">AI recommendations for maximum savings</p>
                    </div>
                  </div>
                  <div className="flex items-start">
                    <DevicePhoneMobileIcon className="w-5 h-5 text-green-500 mr-3 mt-1 flex-shrink-0" />
                    <div>
                      <h4 className="font-semibold text-gray-900 dark:text-white">Mobile App</h4>
                      <p className="text-sm text-gray-600 dark:text-gray-300">Monitor and control from anywhere</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* About Section */}
        <section id="about" className="py-16 md:py-20 bg-white dark:bg-gray-900">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-12 md:mb-16">
              <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-4">
                About SmartWatts
              </h2>
              <p className="text-lg md:text-xl text-gray-600 dark:text-gray-300 max-w-3xl mx-auto">
                Nigeria's first AI-powered energy business intelligence platform
              </p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center mb-12">
              <div>
                <h3 className="text-2xl md:text-3xl font-bold text-gray-900 dark:text-white mb-6">
                  Our Mission
                </h3>
                <p className="text-base md:text-lg text-gray-600 dark:text-gray-300 mb-4">
                  SmartWatts was built to solve one of Nigeria's biggest challenges: unpredictable and expensive energy costs. We understand that businesses and homes in Nigeria deal with multiple power sources daily — grid, generator, solar, and inverter — making it nearly impossible to track costs and optimize usage.
                </p>
                <p className="text-base md:text-lg text-gray-600 dark:text-gray-300 mb-4">
                  Our platform transforms this complexity into clarity, giving you complete visibility and control over your energy consumption. Using advanced AI and machine learning, we provide actionable insights that reduce costs by an average of 30%.
                </p>
                <div className="flex items-start space-x-3 bg-green-50 dark:bg-green-900/20 p-4 rounded-lg">
                  <SparklesIcon className="w-6 h-6 text-green-600 dark:text-green-400 flex-shrink-0 mt-1" />
                  <div>
                    <h4 className="font-semibold text-gray-900 dark:text-white mb-1">Built for Nigeria, By Nigerians</h4>
                    <p className="text-sm text-gray-600 dark:text-gray-300">
                      We understand NERC tariffs, DisCo billing, prepaid tokens, and the unique challenges of Nigeria's energy landscape.
                    </p>
                  </div>
              </div>
              </div>

              <div className="space-y-6">
                <div className="bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 rounded-2xl p-6 border border-blue-200 dark:border-blue-700">
                  <div className="flex items-center mb-4">
                    <div className="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center mr-3">
                      <LightBulbIcon className="w-6 h-6 text-white" />
              </div>
                    <h4 className="text-lg font-bold text-gray-900 dark:text-white">Why We Built SmartWatts</h4>
            </div>
                  <p className="text-sm text-gray-700 dark:text-gray-300">
                    Energy shouldn't be your biggest unknown expense. Nigerian businesses spend an average of 40% of operational costs on power, yet most have no clear visibility into where that money goes. We're changing that.
              </p>
            </div>

                <div className="bg-gradient-to-br from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 rounded-2xl p-6 border border-green-200 dark:border-green-700">
                  <div className="flex items-center mb-4">
                    <div className="w-10 h-10 bg-green-600 rounded-lg flex items-center justify-center mr-3">
                      <GlobeAltIcon className="w-6 h-6 text-white" />
                    </div>
                    <h4 className="text-lg font-bold text-gray-900 dark:text-white">Our Technology</h4>
              </div>
                  <p className="text-sm text-gray-700 dark:text-gray-300">
                    Enterprise-grade AI, edge computing for offline functionality, real-time MQTT communications, and seamless integration with all major energy hardware used in Nigeria.
              </p>
            </div>

                <div className="bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-900/20 dark:to-purple-800/20 rounded-2xl p-6 border border-purple-200 dark:border-purple-700">
                  <div className="flex items-center mb-4">
                    <div className="w-10 h-10 bg-purple-600 rounded-lg flex items-center justify-center mr-3">
                      <ShieldCheckIcon className="w-6 h-6 text-white" />
                    </div>
                    <h4 className="text-lg font-bold text-gray-900 dark:text-white">NERC-Compliant & Secure</h4>
              </div>
                  <p className="text-sm text-gray-700 dark:text-gray-300">
                    Full compliance with NERC regulations and NDPR data protection standards. Your energy data is encrypted, secure, and belongs to you.
                  </p>
                </div>
              </div>
            </div>

            {/* Team/Credibility Section */}
            <div className="bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-800 dark:to-gray-700 rounded-2xl p-8 text-center">
              <h3 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">Trusted by Forward-Thinking Organizations</h3>
              <p className="text-lg text-gray-600 dark:text-gray-300 max-w-3xl mx-auto mb-8">
                Join businesses across Nigeria that are already saving thousands on energy costs with SmartWatts
              </p>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                <div className="text-center">
                  <div className="text-4xl font-bold text-green-600 dark:text-green-400 mb-2">30%</div>
                  <div className="text-gray-600 dark:text-gray-300">Average Cost Reduction</div>
                  </div>
                <div className="text-center">
                  <div className="text-4xl font-bold text-blue-600 dark:text-blue-400 mb-2">₦2.5M</div>
                  <div className="text-gray-600 dark:text-gray-300">Average Annual Savings</div>
                  </div>
                <div className="text-center">
                  <div className="text-4xl font-bold text-purple-600 dark:text-purple-400 mb-2">3-6</div>
                  <div className="text-gray-600 dark:text-gray-300">Months Payback Period</div>
                </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
        <section className="py-16 md:py-20 bg-gradient-to-r from-green-600 to-green-700 dark:from-green-700 dark:to-green-800">
          <div className="max-w-4xl mx-auto text-center px-4 sm:px-6 lg:px-8">
            <h2 className="text-3xl md:text-4xl font-bold text-white mb-6">
              Ready to Transform Your Energy Management?
            </h2>
            <p className="text-lg md:text-xl text-green-100 mb-8">
              Join the energy intelligence revolution. Start your free trial today and see how much you can save.
            </p>
            <div className="flex flex-col sm:flex-row items-center justify-center space-y-4 sm:space-y-0 sm:space-x-6">
              <Link 
                href="/register" 
                className="w-full sm:w-auto bg-white text-green-600 px-6 md:px-8 py-3 md:py-4 rounded-lg font-semibold text-base md:text-lg hover:bg-gray-100 transition-all flex items-center justify-center shadow-lg hover:shadow-xl"
              >
                Start Free Trial
                <ArrowRightIcon className="ml-2 h-5 w-5" />
              </Link>
              <Link 
                href="/contact" 
                className="w-full sm:w-auto border-2 border-white text-white px-6 md:px-8 py-3 md:py-4 rounded-lg font-semibold text-base md:text-lg hover:bg-white/10 transition-all flex items-center justify-center"
              >
                Contact Sales
              </Link>
          </div>

            {/* Test Credentials Info */}
            <div className="mt-12 pt-8 border-t border-green-500">
              <h3 className="text-xl font-semibold text-white mb-4">Try SmartWatts Now</h3>
              <p className="text-green-100 mb-4">Use these test credentials to explore the platform, or create your own account:</p>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                <div className="bg-white/10 backdrop-blur-sm rounded-lg p-4">
                  <div className="font-semibold text-white mb-2">Admin Account</div>
                  <div className="text-green-100">admin@mysmartwatts.com</div>
                  <div className="text-green-100">Password: password</div>
                </div>
                <div className="bg-white/10 backdrop-blur-sm rounded-lg p-4">
                  <div className="font-semibold text-white mb-2">Test User</div>
                  <div className="text-green-100">test@mysmartwatts.com</div>
                  <div className="text-green-100">Password: password</div>
                </div>
                <div className="bg-white/10 backdrop-blur-sm rounded-lg p-4">
                  <div className="font-semibold text-white mb-2">Household Demo</div>
                  <div className="text-green-100">household@smartwatts.com</div>
                  <div className="text-green-100">Password: Household123!</div>
                </div>
              </div>
              <p className="text-green-100 mt-4 text-sm">
                Or <Link href="/register" className="underline hover:text-white">create your own account</Link> — it's free!
              </p>
            </div>
        </div>
      </section>

      {/* Footer */}
        <footer className="bg-gray-900 dark:bg-gray-950 text-white py-12 md:py-16">
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
                <div className="space-y-2 text-sm text-gray-400">
                  <div className="flex items-start">
                    <MapPinIcon className="w-4 h-4 mr-2 mt-1 flex-shrink-0" />
                    <span>Plot 20, Prince Adelowo Adedeji Street, Off Admiralty Way, Lekki Phase 1, Lagos, Nigeria</span>
                  </div>
                  <div className="flex items-center">
                    <PhoneIcon className="w-4 h-4 mr-2 flex-shrink-0" />
                    <span>+234 (0) 123 456 7890</span>
                  </div>
                  <div className="flex items-center">
                    <EnvelopeIcon className="w-4 h-4 mr-2 flex-shrink-0" />
                    <span>info@mysmartwatts.com</span>
                  </div>
                </div>
              </div>
              
            <div>
              <h3 className="font-semibold mb-4">Product</h3>
                <ul className="space-y-2 text-gray-400 text-sm">
                  <li><Link href="#features" className="hover:text-white transition-colors">Features</Link></li>
                  <li><Link href="#about" className="hover:text-white transition-colors">About</Link></li>
                  <li><Link href="/contact" className="hover:text-white transition-colors">Demo</Link></li>
                  <li><Link href="/contact" className="hover:text-white transition-colors">API</Link></li>
              </ul>
            </div>
              
            <div>
              <h3 className="font-semibold mb-4">Support</h3>
                <ul className="space-y-2 text-gray-400 text-sm">
                  <li><Link href="/contact" className="hover:text-white transition-colors">Contact Us</Link></li>
                  <li><Link href="/contact" className="hover:text-white transition-colors">Help Center</Link></li>
                  <li><Link href="/contact" className="hover:text-white transition-colors">Documentation</Link></li>
                  <li><Link href="/contact" className="hover:text-white transition-colors">Status</Link></li>
              </ul>
            </div>
            </div>
            
            <div className="border-t border-gray-800 mt-12 pt-8 flex flex-col md:flex-row items-center justify-between">
              <p className="text-gray-400 text-sm">
                © {new Date().getFullYear()} Citizenshield Technologies Limited. All rights reserved.
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
