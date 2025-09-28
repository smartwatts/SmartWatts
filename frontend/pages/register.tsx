import React, { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useAuth } from '../hooks/useAuth'
import { EyeIcon, EyeSlashIcon, BoltIcon, UserIcon, PhoneIcon, EnvelopeIcon, LockClosedIcon, HomeIcon, MapPinIcon, BuildingOfficeIcon, XMarkIcon } from '@heroicons/react/24/outline'
import Link from 'next/link'

const registerSchema = z.object({
  firstName: z.string().min(2, 'First name must be at least 2 characters'),
  lastName: z.string().min(2, 'Last name must be at least 2 characters'),
  email: z.string().email('Invalid email address'),
  phoneNumber: z.string().min(10, 'Phone number must be at least 10 characters'),
  address: z.string().min(10, 'Address must be at least 10 characters'),
  city: z.string().min(2, 'City must be at least 2 characters'),
  state: z.string().min(2, 'State must be at least 2 characters'),
  country: z.string().default('Nigeria'),
  propertyType: z.enum(['residential', 'commercial', 'industrial', 'mixed_use'], {
    errorMap: () => ({ message: 'Please select a property type' })
  }),
  propertySize: z.enum(['small', 'medium', 'large', 'extra_large'], {
    errorMap: () => ({ message: 'Please select your property size' })
  }),
  buildingType: z.enum([
    'apartment', 'duplex', 'bungalow', 'mansion', 
    'office_building', 'retail_space', 'shopping_mall', 'restaurant', 'hotel',
    'warehouse', 'factory', 'manufacturing_plant', 'processing_facility',
    'residential_commercial', 'office_retail', 'apartment_office', 'other'
  ], {
    errorMap: () => ({ message: 'Please select your building type' })
  }),
  numberOfRooms: z.string().min(1, 'Please enter number of rooms'),
  numberOfFloors: z.string().min(1, 'Please enter number of floors'),
  hasSolar: z.boolean().default(false),
  hasGenerator: z.boolean().default(false),
  hasInverter: z.boolean().default(false),
  currentMeterType: z.enum(['prepaid', 'postpaid', 'both', 'unknown'], {
    errorMap: () => ({ message: 'Please select your current meter type' })
  }),
  energyProvider: z.string().min(1, 'Please select your DISCO'),
  monthlyEnergyBill: z.enum(['0-10000', '10000-25000', '25000-50000', '50000-100000', '100000-200000', '200000+'], {
    errorMap: () => ({ message: 'Please select your monthly energy bill range' })
  }),
  password: z.string().min(8, 'Password must be at least 8 characters'),
  confirmPassword: z.string(),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
})

type RegisterForm = z.infer<typeof registerSchema>

export default function Register() {
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [isPopup, setIsPopup] = useState(false)
  const [mounted, setMounted] = useState(false)
  const { register: registerUser } = useAuth()

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

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
  })

  // Watch property type changes and reset dependent fields
  const propertyType = watch('propertyType')
  React.useEffect(() => {
    if (propertyType) {
      setValue('propertySize', undefined as any)
      setValue('buildingType', undefined as any)
    }
  }, [propertyType, setValue])

  if (!mounted) {
    return null
  }

  const onSubmit = async (data: RegisterForm) => {
    setIsLoading(true)
    try {
      await registerUser({
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        phoneNumber: data.phoneNumber,
        address: data.address,
        city: data.city,
        state: data.state,
        country: data.country,
        propertyType: data.propertyType,
        propertySize: data.propertySize,
        buildingType: data.buildingType,
        numberOfRooms: data.numberOfRooms,
        numberOfFloors: data.numberOfFloors,
        hasSolar: data.hasSolar,
        hasGenerator: data.hasGenerator,
        hasInverter: data.hasInverter,
        currentMeterType: data.currentMeterType,
        energyProvider: data.energyProvider,
        monthlyEnergyBill: data.monthlyEnergyBill,
        password: data.password,
      })
    } catch (error) {
      console.error('Registration error:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const content = (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-blue-900 to-slate-900 relative overflow-hidden">
      {/* Background decorative elements */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-gradient-to-br from-blue-500/20 to-purple-500/20 rounded-full blur-3xl"></div>
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-gradient-to-br from-green-500/20 to-blue-500/20 rounded-full blur-3xl"></div>
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-gradient-to-br from-yellow-500/10 to-orange-500/10 rounded-full blur-3xl"></div>
      </div>

      <div className="relative z-10 max-w-lg w-full mx-4">
        {/* Logo and Header */}
        <div className="text-center mb-8 animate-fade-in">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-blue-500 to-green-500 rounded-2xl mb-6 shadow-lg">
            <BoltIcon className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-400 to-green-400 bg-clip-text text-transparent mb-2">
            SmartWatts
          </h1>
          <p className="text-gray-300 text-lg font-medium">
            Create your account
          </p>
          <p className="text-gray-400 text-sm mt-1">
            Start monitoring your energy consumption today
          </p>
        </div>

        {/* Registration Form */}
        <div className="bg-slate-800/90 backdrop-blur-sm rounded-2xl shadow-2xl border border-slate-700/50 p-8 animate-slide-up">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {/* Name Fields */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label htmlFor="firstName" className="block text-sm font-medium text-gray-300">
                  First Name
                </label>
                <div className="relative">
                  <input
                    {...register('firstName')}
                    id="firstName"
                    name="firstName"
                    type="text"
                    required
                    className="w-full px-4 py-3 pl-10 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-white/50 backdrop-blur-sm placeholder-gray-400"
                    placeholder="John"
                  />
                  <UserIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                </div>
                {errors.firstName && (
                  <p className="text-sm text-red-500 flex items-center mt-1">
                    <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                    {errors.firstName.message}
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <label htmlFor="lastName" className="block text-sm font-medium text-gray-300">
                  Last Name
                </label>
                <div className="relative">
                  <input
                    {...register('lastName')}
                    id="lastName"
                    name="lastName"
                    type="text"
                    required
                    className="w-full px-4 py-3 pl-10 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-white/50 backdrop-blur-sm placeholder-gray-400"
                    placeholder="Doe"
                  />
                  <UserIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                </div>
                {errors.lastName && (
                  <p className="text-sm text-red-500 flex items-center mt-1">
                    <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                    {errors.lastName.message}
                  </p>
                )}
              </div>
            </div>

            {/* Email Field */}
            <div className="space-y-2">
              <label htmlFor="email" className="block text-sm font-medium text-gray-300">
                Email Address
              </label>
              <div className="relative">
                <input
                  {...register('email')}
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                  className="w-full px-4 py-3 pl-10 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-white/50 backdrop-blur-sm placeholder-gray-400"
                  placeholder="john@example.com"
                />
                <EnvelopeIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
              </div>
              {errors.email && (
                <p className="text-sm text-red-500 flex items-center mt-1">
                  <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                  {errors.email.message}
                </p>
              )}
            </div>

            {/* Phone Number Field */}
            <div className="space-y-2">
              <label htmlFor="phoneNumber" className="block text-sm font-medium text-gray-300">
                Phone Number
              </label>
              <div className="relative">
                <input
                  {...register('phoneNumber')}
                  id="phoneNumber"
                  name="phoneNumber"
                  type="tel"
                  required
                  className="w-full px-4 py-3 pl-10 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-white/50 backdrop-blur-sm placeholder-gray-400"
                  placeholder="+234 801 234 5678"
                />
                <PhoneIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
              </div>
              {errors.phoneNumber && (
                <p className="text-sm text-red-500 flex items-center mt-1">
                  <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                  {errors.phoneNumber.message}
                </p>
              )}
            </div>

            {/* Address Section */}
            <div className="space-y-4">
              <div className="flex items-center space-x-2 mb-4">
                <HomeIcon className="h-5 w-5 text-blue-600" />
                <h3 className="text-lg font-semibold text-gray-200">Property Information</h3>
              </div>

              {/* Address Field */}
              <div className="space-y-2">
                <label htmlFor="address" className="block text-sm font-medium text-gray-300">
                  Street Address
                </label>
                <div className="relative">
                  <input
                    {...register('address')}
                    id="address"
                    name="address"
                    type="text"
                    required
                    className="w-full px-4 py-3 pl-10 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-white/50 backdrop-blur-sm placeholder-gray-400"
                    placeholder="123 Main Street, Apartment 4B"
                  />
                  <MapPinIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                </div>
                {errors.address && (
                  <p className="text-sm text-red-500 flex items-center mt-1">
                    <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                    {errors.address.message}
                  </p>
                )}
              </div>

              {/* City, State, Postal Code */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="space-y-2">
                  <label htmlFor="city" className="block text-sm font-medium text-gray-300">
                    City
                  </label>
                  <input
                    {...register('city')}
                    id="city"
                    name="city"
                    type="text"
                    required
                    className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-white/50 backdrop-blur-sm placeholder-gray-400"
                    placeholder="Lagos"
                  />
                  {errors.city && (
                    <p className="text-sm text-red-500 flex items-center mt-1">
                      <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                      {errors.city.message}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <label htmlFor="state" className="block text-sm font-medium text-gray-300">
                    State
                  </label>
                  <input
                    {...register('state')}
                    id="state"
                    name="state"
                    type="text"
                    required
                    className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-white/50 backdrop-blur-sm placeholder-gray-400"
                    placeholder="Lagos"
                  />
                  {errors.state && (
                    <p className="text-sm text-red-500 flex items-center mt-1">
                      <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                      {errors.state.message}
                    </p>
                  )}
                </div>

              </div>

              {/* Country */}
              <div className="space-y-2">
                <label htmlFor="country" className="block text-sm font-medium text-gray-300">
                  Country
                </label>
                <select
                  {...register('country')}
                  id="country"
                  name="country"
                  required
                  defaultValue="Nigeria"
                  className="w-full px-4 py-3 border border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-slate-700/50 backdrop-blur-sm text-white"
                >
                  <option value="Nigeria">Nigeria</option>
                </select>
                {errors.country && (
                  <p className="text-sm text-red-500 flex items-center mt-1">
                    <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                    {errors.country.message}
                  </p>
                )}
              </div>

              {/* Property Type */}
              <div className="space-y-2">
                <label htmlFor="propertyType" className="block text-sm font-medium text-gray-300">
                  Property Type
                </label>
                <select
                  {...register('propertyType')}
                  id="propertyType"
                  name="propertyType"
                  required
                  className="w-full px-4 py-3 border border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-slate-700/50 backdrop-blur-sm text-white"
                >
                  <option value="">Select Property Type</option>
                  <option value="residential">Residential (Home/Apartment)</option>
                  <option value="commercial">Commercial (Office/Retail)</option>
                  <option value="industrial">Industrial (Factory/Warehouse)</option>
                  <option value="mixed_use">Mixed Use (Residential + Commercial)</option>
                </select>
                {errors.propertyType && (
                  <p className="text-sm text-red-500 flex items-center mt-1">
                    <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                    {errors.propertyType.message}
                  </p>
                )}
              </div>

              {/* Property Size */}
              <div className="space-y-2">
                <label htmlFor="propertySize" className="block text-sm font-medium text-gray-300">
                  Property Size
                </label>
                <select
                  {...register('propertySize')}
                  id="propertySize"
                  name="propertySize"
                  required
                  className="w-full px-4 py-3 border border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-slate-700/50 backdrop-blur-sm text-white"
                >
                  <option value="">Select Property Size</option>
                  {watch('propertyType') === 'residential' && (
                    <>
                      <option value="small">Small (1-2 rooms, &lt; 50 sqm)</option>
                      <option value="medium">Medium (3-4 rooms, 50-150 sqm)</option>
                      <option value="large">Large (5-6 rooms, 150-300 sqm)</option>
                      <option value="extra_large">Extra Large (7+ rooms, 300+ sqm)</option>
                    </>
                  )}
                  {watch('propertyType') === 'commercial' && (
                    <>
                      <option value="small">Small Office (&lt; 100 sqm)</option>
                      <option value="medium">Medium Office (100-500 sqm)</option>
                      <option value="large">Large Office (500-2000 sqm)</option>
                      <option value="extra_large">Extra Large Office (2000+ sqm)</option>
                    </>
                  )}
                  {watch('propertyType') === 'industrial' && (
                    <>
                      <option value="small">Small Facility (&lt; 1000 sqm)</option>
                      <option value="medium">Medium Facility (1000-5000 sqm)</option>
                      <option value="large">Large Facility (5000-20000 sqm)</option>
                      <option value="extra_large">Extra Large Facility (20000+ sqm)</option>
                    </>
                  )}
                  {watch('propertyType') === 'mixed_use' && (
                    <>
                      <option value="small">Small Mixed Use (&lt; 200 sqm)</option>
                      <option value="medium">Medium Mixed Use (200-1000 sqm)</option>
                      <option value="large">Large Mixed Use (1000-5000 sqm)</option>
                      <option value="extra_large">Extra Large Mixed Use (5000+ sqm)</option>
                    </>
                  )}
                  {!watch('propertyType') && (
                    <>
                      <option value="small">Small</option>
                      <option value="medium">Medium</option>
                      <option value="large">Large</option>
                      <option value="extra_large">Extra Large</option>
                    </>
                  )}
                </select>
                {errors.propertySize && (
                  <p className="text-sm text-red-500 flex items-center mt-1">
                    <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                    {errors.propertySize.message}
                  </p>
                )}
              </div>

              {/* Building Type */}
              <div className="space-y-2">
                <label htmlFor="buildingType" className="block text-sm font-medium text-gray-300">
                  Building Type
                </label>
                <select
                  {...register('buildingType')}
                  id="buildingType"
                  name="buildingType"
                  required
                  className="w-full px-4 py-3 border border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-slate-700/50 backdrop-blur-sm text-white"
                >
                  <option value="">Select Building Type</option>
                  {watch('propertyType') === 'residential' && (
                    <>
                      <option value="apartment">Apartment</option>
                      <option value="duplex">Duplex</option>
                      <option value="bungalow">Bungalow</option>
                      <option value="mansion">Mansion</option>
                      <option value="other">Other</option>
                    </>
                  )}
                  {watch('propertyType') === 'commercial' && (
                    <>
                      <option value="office_building">Office Building</option>
                      <option value="retail_space">Retail Space</option>
                      <option value="shopping_mall">Shopping Mall</option>
                      <option value="restaurant">Restaurant</option>
                      <option value="hotel">Hotel</option>
                      <option value="other">Other</option>
                    </>
                  )}
                  {watch('propertyType') === 'industrial' && (
                    <>
                      <option value="factory">Factory</option>
                      <option value="warehouse">Warehouse</option>
                      <option value="manufacturing_plant">Manufacturing Plant</option>
                      <option value="processing_facility">Processing Facility</option>
                      <option value="other">Other</option>
                    </>
                  )}
                  {watch('propertyType') === 'mixed_use' && (
                    <>
                      <option value="residential_commercial">Residential + Commercial</option>
                      <option value="office_retail">Office + Retail</option>
                      <option value="apartment_office">Apartment + Office</option>
                      <option value="other">Other</option>
                    </>
                  )}
                  {!watch('propertyType') && (
                    <>
                      <option value="apartment">Apartment</option>
                      <option value="duplex">Duplex</option>
                      <option value="bungalow">Bungalow</option>
                      <option value="mansion">Mansion</option>
                      <option value="office_building">Office Building</option>
                      <option value="warehouse">Warehouse</option>
                      <option value="factory">Factory</option>
                      <option value="other">Other</option>
                    </>
                  )}
                </select>
                {errors.buildingType && (
                  <p className="text-sm text-red-500 flex items-center mt-1">
                    <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                    {errors.buildingType.message}
                  </p>
                )}
              </div>

              {/* Number of Rooms and Floors */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label htmlFor="numberOfRooms" className="block text-sm font-medium text-gray-300">
                    Number of Rooms
                  </label>
                  <input
                    {...register('numberOfRooms')}
                    id="numberOfRooms"
                    name="numberOfRooms"
                    type="number"
                    min="1"
                    required
                    className="w-full px-4 py-3 pl-10 border border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-slate-700/50 backdrop-blur-sm text-white placeholder-gray-400"
                    placeholder="e.g., 4"
                  />
                  {errors.numberOfRooms && (
                    <p className="text-sm text-red-500 flex items-center mt-1">
                      <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                      {errors.numberOfRooms.message}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <label htmlFor="numberOfFloors" className="block text-sm font-medium text-gray-300">
                    Number of Floors
                  </label>
                  <input
                    {...register('numberOfFloors')}
                    id="numberOfFloors"
                    name="numberOfFloors"
                    type="number"
                    min="1"
                    required
                    className="w-full px-4 py-3 pl-10 border border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-slate-700/50 backdrop-blur-sm text-white placeholder-gray-400"
                    placeholder="e.g., 2"
                  />
                  {errors.numberOfFloors && (
                    <p className="text-sm text-red-500 flex items-center mt-1">
                      <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                      {errors.numberOfFloors.message}
                    </p>
                  )}
                </div>
              </div>
            </div>

            {/* Current Energy Setup Section */}
            <div className="space-y-4">
              <div className="flex items-center space-x-2 mb-4">
                <BoltIcon className="h-5 w-5 text-green-600" />
                <h3 className="text-lg font-semibold text-gray-200">Current Energy Setup</h3>
              </div>

              {/* Current Meter Type */}
              <div className="space-y-2">
                <label htmlFor="currentMeterType" className="block text-sm font-medium text-gray-300">
                  Current Meter Type
                </label>
                <select
                  {...register('currentMeterType')}
                  id="currentMeterType"
                  name="currentMeterType"
                  required
                  className="w-full px-4 py-3 border border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-slate-700/50 backdrop-blur-sm text-white"
                >
                  <option value="">Select Current Meter Type</option>
                  <option value="prepaid">Prepaid Meter</option>
                  <option value="postpaid">Postpaid Meter</option>
                  <option value="both">Both Prepaid & Postpaid</option>
                  <option value="unknown">Unknown/Not Sure</option>
                </select>
                {errors.currentMeterType && (
                  <p className="text-sm text-red-500 flex items-center mt-1">
                    <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                    {errors.currentMeterType.message}
                  </p>
                )}
              </div>

              {/* Energy Equipment Checkboxes */}
              <div className="space-y-3">
                <p className="text-sm font-medium text-gray-300">Do you currently have any of the following?</p>
                
                <div className="space-y-2">
                  <label className="flex items-center space-x-3">
                    <input
                      {...register('hasSolar')}
                      type="checkbox"
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-slate-600 bg-slate-700 rounded transition-colors duration-200"
                    />
                    <span className="text-sm text-gray-300">Solar Panels/Inverter System</span>
                  </label>
                </div>

                <div className="space-y-2">
                  <label className="flex items-center space-x-3">
                    <input
                      {...register('hasGenerator')}
                      type="checkbox"
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-slate-600 bg-slate-700 rounded transition-colors duration-200"
                    />
                    <span className="text-sm text-gray-300">Generator (Petrol/Diesel)</span>
                  </label>
                </div>

                <div className="space-y-2">
                  <label className="flex items-center space-x-3">
                    <input
                      {...register('hasInverter')}
                      type="checkbox"
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-slate-600 bg-slate-700 rounded transition-colors duration-200"
                    />
                    <span className="text-sm text-gray-300">Inverter System (Battery Backup)</span>
                  </label>
                </div>
              </div>
            </div>

            {/* Energy Information Section */}
            <div className="space-y-4">
              <div className="flex items-center space-x-2 mb-4">
                <BoltIcon className="h-5 w-5 text-green-600" />
                <h3 className="text-lg font-semibold text-gray-200">Energy Information</h3>
              </div>

              {/* Energy Provider */}
              <div className="space-y-2">
                <label htmlFor="energyProvider" className="block text-sm font-medium text-gray-300">
                  Energy Provider (DISCO)
                </label>
                <div className="relative">
                  <select
                    {...register('energyProvider')}
                    id="energyProvider"
                    name="energyProvider"
                    required
                    className="w-full px-4 py-3 pl-10 border border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-slate-700/50 backdrop-blur-sm text-white placeholder-gray-400"
                  >
                    <option value="">Select your DISCO</option>
                    <option value="Eko Electricity Distribution Company">Eko Electricity Distribution Company (EKEDC)</option>
                    <option value="Ibadan Electricity Distribution Company">Ibadan Electricity Distribution Company (IBEDC)</option>
                    <option value="Ikeja Electric">Ikeja Electric (IE)</option>
                    <option value="Kaduna Electric">Kaduna Electric (KEDCO)</option>
                    <option value="Kano Electricity Distribution Company">Kano Electricity Distribution Company (KEDCO)</option>
                    <option value="Jos Electricity Distribution Company">Jos Electricity Distribution Company (JEDCO)</option>
                    <option value="Port Harcourt Electricity Distribution Company">Port Harcourt Electricity Distribution Company (PHED)</option>
                    <option value="Abuja Electricity Distribution Company">Abuja Electricity Distribution Company (AEDC)</option>
                    <option value="Enugu Electricity Distribution Company">Enugu Electricity Distribution Company (EEDC)</option>
                    <option value="Yola Electricity Distribution Company">Yola Electricity Distribution Company (YEDC)</option>
                    <option value="Benin Electricity Distribution Company">Benin Electricity Distribution Company (BEDC)</option>
                    <option value="Kaduna Electricity Distribution Company">Kaduna Electricity Distribution Company (KEDCO)</option>
                    <option value="Other">Other (Please specify in comments)</option>
                  </select>
                  <BuildingOfficeIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                </div>
                {errors.energyProvider && (
                  <p className="text-sm text-red-500 flex items-center mt-1">
                    <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                    {errors.energyProvider.message}
                  </p>
                )}
              </div>

              {/* Monthly Energy Bill */}
              <div className="space-y-2">
                <label htmlFor="monthlyEnergyBill" className="block text-sm font-medium text-gray-300">
                  Monthly Energy Bill Range (₦)
                </label>
                <div className="relative">
                  <select
                    {...register('monthlyEnergyBill')}
                    id="monthlyEnergyBill"
                    name="monthlyEnergyBill"
                    required
                    className="w-full px-4 py-3 pl-10 border border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-slate-700/50 backdrop-blur-sm text-white placeholder-gray-400"
                  >
                    <option value="">Select your monthly bill range</option>
                    <option value="0-10000">₦0 - ₦10,000 (Low consumption)</option>
                    <option value="10000-25000">₦10,000 - ₦25,000 (Moderate consumption)</option>
                    <option value="25000-50000">₦25,000 - ₦50,000 (High consumption)</option>
                    <option value="50000-100000">₦50,000 - ₦100,000 (Very high consumption)</option>
                    <option value="100000-200000">₦100,000 - ₦200,000 (Commercial level)</option>
                    <option value="200000+">₦200,000+ (Industrial level)</option>
                  </select>
                  <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 font-medium">₦</span>
                </div>
                {errors.monthlyEnergyBill && (
                  <p className="text-sm text-red-500 flex items-center mt-1">
                    <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                    {errors.monthlyEnergyBill.message}
                  </p>
                )}
              </div>
            </div>

            {/* Password Fields */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label htmlFor="password" className="block text-sm font-medium text-gray-300">
                  Password
                </label>
                <div className="relative">
                  <input
                    {...register('password')}
                    id="password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="new-password"
                    required
                    className="w-full px-4 py-3 pl-10 pr-10 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-white/50 backdrop-blur-sm placeholder-gray-400"
                    placeholder="••••••••"
                  />
                  <LockClosedIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                  <button
                    type="button"
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors duration-200"
                    onClick={() => setShowPassword(!showPassword)}
                  >
                    {showPassword ? (
                      <EyeSlashIcon className="h-5 w-5" />
                    ) : (
                      <EyeIcon className="h-5 w-5" />
                    )}
                  </button>
                </div>
                {errors.password && (
                  <p className="text-sm text-red-500 flex items-center mt-1">
                    <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                    {errors.password.message}
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-300">
                  Confirm Password
                </label>
                <div className="relative">
                  <input
                    {...register('confirmPassword')}
                    id="confirmPassword"
                    name="confirmPassword"
                    type={showConfirmPassword ? 'text' : 'password'}
                    autoComplete="new-password"
                    required
                    className="w-full px-4 py-3 pl-10 pr-10 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-white/50 backdrop-blur-sm placeholder-gray-400"
                    placeholder="••••••••"
                  />
                  <LockClosedIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                  <button
                    type="button"
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors duration-200"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  >
                    {showConfirmPassword ? (
                      <EyeSlashIcon className="h-5 w-5" />
                    ) : (
                      <EyeIcon className="h-5 w-5" />
                    )}
                  </button>
                </div>
                {errors.confirmPassword && (
                  <p className="text-sm text-red-500 flex items-center mt-1">
                    <span className="w-1 h-1 bg-red-500 rounded-full mr-2"></span>
                    {errors.confirmPassword.message}
                  </p>
                )}
              </div>
            </div>

            {/* Terms Agreement */}
            <div className="flex items-start space-x-3">
              <input
                id="agree-terms"
                name="agree-terms"
                type="checkbox"
                required
                className="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-slate-600 bg-slate-700 rounded transition-colors duration-200"
              />
              <label htmlFor="agree-terms" className="text-sm text-gray-300">
                I agree to the{' '}
                <button
                  type="button"
                  onClick={() => window.open('/terms-of-service', 'terms', 'width=800,height=600,scrollbars=yes,resizable=yes')}
                  className="font-medium text-blue-600 hover:text-blue-500 transition-colors duration-200 underline"
                >
                  Terms of Service
                </button>{' '}
                and{' '}
                <button
                  type="button"
                  onClick={() => window.open('/privacy-policy', 'privacy', 'width=800,height=600,scrollbars=yes,resizable=yes')}
                  className="font-medium text-blue-600 hover:text-blue-500 transition-colors duration-200 underline"
                >
                  Privacy Policy
                </button>
              </label>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-gradient-to-r from-blue-600 to-green-600 hover:from-blue-700 hover:to-green-700 text-white font-semibold py-3 px-4 rounded-xl transition-all duration-200 transform hover:scale-[1.02] focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none shadow-lg hover:shadow-xl"
            >
              {isLoading ? (
                <div className="flex items-center justify-center">
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                  Creating account...
                </div>
              ) : (
                'Create account'
              )}
            </button>

            {/* Divider */}

            {/* Sign In Link */}
            <div className="text-center">
              <p className="text-sm text-gray-300">
                Already have an account?{' '}
                {isPopup ? (
                  <button
                    onClick={() => {
                      if (window.opener) {
                        window.opener.location.href = '/login'
                      } else {
                        window.location.href = '/login'
                      }
                    }}
                    className="font-semibold text-blue-600 hover:text-blue-500 transition-colors duration-200 underline"
                  >
                    Sign in
                  </button>
                ) : (
                  <Link href="/login" className="font-semibold text-blue-600 hover:text-blue-500 transition-colors duration-200">
                    Sign in
                  </Link>
                )}
              </p>
            </div>
          </form>
        </div>

        {/* Footer */}
        <div className="text-center mt-8 text-sm text-gray-400">
          <p>© {new Date().getFullYear()} SmartWatts. All rights reserved.</p>
          <div className="mt-2">
            <button
              onClick={() => window.open('/contact', 'contact', 'width=800,height=600,scrollbars=yes,resizable=yes')}
              className="text-blue-400 hover:text-blue-300 transition-colors underline"
            >
              Contact Us
            </button>
          </div>
        </div>
      </div>
    </div>
  )

  if (isPopup) {
    return (
      <div className="fixed inset-0 bg-slate-900 z-50 overflow-auto">
        {/* Popup Header */}
        <div className="sticky top-0 bg-slate-800 border-b border-slate-700 px-4 py-3 flex items-center justify-between">
          <h1 className="text-lg font-semibold text-white">Create Account</h1>
          <button
            onClick={handleClose}
            className="p-2 hover:bg-slate-700 rounded-lg transition-colors"
          >
            <XMarkIcon className="h-5 w-5 text-gray-400" />
          </button>
        </div>
        {content}
      </div>
    )
  }

  return content
} 