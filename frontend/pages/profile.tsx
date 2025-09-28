import React, { useState } from 'react'
import Layout from '../components/Layout'
import ProtectedRoute from '../components/ProtectedRoute'
import {
  UserIcon,
  EnvelopeIcon,
  PhoneIcon,
  MapPinIcon,
  ShieldCheckIcon,
  CogIcon,
  BellIcon,
  KeyIcon,
} from '@heroicons/react/24/outline'

interface ProfileData {
  firstName: string
  lastName: string
  email: string
  phone: string
  address: string
  city: string
  state: string
  country: string
  notifications: {
    email: boolean
    sms: boolean
    push: boolean
  }
  preferences: {
    theme: 'light' | 'dark' | 'auto'
    language: string
    timezone: string
  }
}

export default function Profile() {
  const [isEditing, setIsEditing] = useState(false)
  const [profileData, setProfileData] = useState<ProfileData>({
    firstName: 'Test',
    lastName: 'User',
    email: 'test@example.com',
    phone: '+234 801 234 5678',
    address: '123 Test Street',
    city: 'Lagos',
    state: 'Lagos',
    country: 'Nigeria',
    notifications: {
      email: true,
      sms: false,
      push: true,
    },
    preferences: {
      theme: 'light',
      language: 'English',
      timezone: 'Africa/Lagos',
    },
  })

  const handleSave = async () => {
    try {
      // Mock profile update
      console.log('Profile updated:', profileData)
      setIsEditing(false)
    } catch (error) {
      console.error('Error updating profile:', error)
    }
  }

  const handleCancel = () => {
    setProfileData({
      firstName: 'Test',
      lastName: 'User',
      email: 'test@example.com',
      phone: '+234 801 234 5678',
      address: '123 Test Street',
      city: 'Lagos',
      state: 'Lagos',
      country: 'Nigeria',
      notifications: {
        email: true,
        sms: false,
        push: true,
      },
      preferences: {
        theme: 'light',
        language: 'English',
        timezone: 'Africa/Lagos',
      },
    })
    setIsEditing(false)
  }

  return (
    <ProtectedRoute>
      <Layout>
        <div className="space-y-6 animate-fade-in">
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-600 via-blue-700 to-blue-600 dark:from-gray-800 dark:via-blue-900 dark:to-gray-800 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden">
          <div className="absolute inset-0 overflow-hidden">
            <div className="absolute -top-40 -right-40 w-80 h-80 bg-blue-500/10 rounded-full blur-3xl animate-pulse"></div>
            <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-blue-500/10 rounded-full blur-3xl animate-pulse" style={{animationDelay: '2s'}}></div>
          </div>
          
          <div className="relative z-10">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-4xl font-bold mb-2 animate-fade-in">
                  Profile & Settings ⚙️
                </h1>
                <p className="text-blue-200 text-lg animate-fade-in" style={{animationDelay: '0.2s'}}>
                  Manage your account preferences and security
                </p>
                <div className="flex items-center mt-6 space-x-6 animate-fade-in" style={{animationDelay: '0.4s'}}>
                  <div className="flex items-center space-x-2 bg-white/10 backdrop-blur-sm rounded-xl px-4 py-2">
                    <ShieldCheckIcon className="h-5 w-5 text-green-300" />
                    <span className="text-sm font-medium">Account Secure</span>
                  </div>
                  <div className="flex items-center space-x-2 bg-white/10 backdrop-blur-sm rounded-xl px-4 py-2">
                    <BellIcon className="h-5 w-5 text-blue-300" />
                    <span className="text-sm font-medium">Notifications Active</span>
                  </div>
                </div>
              </div>
              <div className="hidden md:block">
                <div className="w-32 h-32 bg-white/10 backdrop-blur-sm rounded-full flex items-center justify-center shadow-2xl transform hover:scale-110 transition-all duration-300">
                  <UserIcon className="h-16 w-16 text-white" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Personal Information */}
          <div className="bg-white dark:bg-gray-800/80 backdrop-blur-sm rounded-3xl shadow-xl border border-gray-200 dark:border-gray-700 hover:shadow-2xl transition-all duration-300">
            <div className="p-8 border-b border-gray-200 dark:border-gray-700">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">Personal Information</h3>
                  <p className="text-gray-600 dark:text-gray-400">Update your personal details</p>
                </div>
                <button
                  onClick={() => setIsEditing(!isEditing)}
                  className="bg-gradient-to-r from-blue-600 to-blue-700 text-white px-4 py-2 rounded-xl transition-all duration-200 hover:from-blue-700 hover:to-blue-800"
                >
                  {isEditing ? 'Cancel' : 'Edit'}
                </button>
              </div>
            </div>
            <div className="p-8">
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-600 dark:text-gray-400 mb-2">First Name</label>
                    <input
                      type="text"
                      value={profileData.firstName}
                      onChange={(e) => setProfileData({...profileData, firstName: e.target.value})}
                      disabled={!isEditing}
                      className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50 disabled:cursor-not-allowed"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-600 dark:text-gray-400 mb-2">Last Name</label>
                    <input
                      type="text"
                      value={profileData.lastName}
                      onChange={(e) => setProfileData({...profileData, lastName: e.target.value})}
                      disabled={!isEditing}
                      className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50 disabled:cursor-not-allowed"
                    />
                  </div>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-600 dark:text-gray-400 mb-2">Email Address</label>
                  <input
                    type="email"
                    value={profileData.email}
                    onChange={(e) => setProfileData({...profileData, email: e.target.value})}
                    disabled={!isEditing}
                    className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50 disabled:cursor-not-allowed"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-600 dark:text-gray-400 mb-2">Phone Number</label>
                  <input
                    type="tel"
                    value={profileData.phone}
                    onChange={(e) => setProfileData({...profileData, phone: e.target.value})}
                    disabled={!isEditing}
                    className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50 disabled:cursor-not-allowed"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-600 dark:text-gray-400 mb-2">Address</label>
                  <input
                    type="text"
                    value={profileData.address}
                    onChange={(e) => setProfileData({...profileData, address: e.target.value})}
                    disabled={!isEditing}
                    className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50 disabled:cursor-not-allowed"
                  />
                </div>
                
                {isEditing && (
                  <div className="flex space-x-4">
                    <button
                      onClick={handleSave}
                      className="flex-1 bg-gradient-to-r from-green-600 to-green-700 text-white font-semibold py-3 px-4 rounded-xl transition-all duration-200 hover:from-green-700 hover:to-green-800 transform hover:scale-[1.02]"
                    >
                      Save Changes
                    </button>
                    <button
                      onClick={handleCancel}
                      className="flex-1 border border-gray-600 text-gray-300 font-semibold py-3 px-4 rounded-xl transition-all duration-200 hover:border-red-500 hover:text-red-400 hover:bg-red-500/10"
                    >
                      Cancel
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Security & Notifications */}
          <div className="space-y-6">
            {/* Security Settings */}
            <div className="bg-gray-800/80 backdrop-blur-sm rounded-3xl shadow-xl border border-gray-700 hover:shadow-2xl transition-all duration-300">
              <div className="p-8 border-b border-gray-700">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-2xl font-bold text-white mb-2">Security Settings</h3>
                    <p className="text-gray-400">Manage your account security</p>
                  </div>
                  <ShieldCheckIcon className="h-8 w-8 text-blue-400" />
                </div>
              </div>
              <div className="p-8">
                <div className="space-y-4">
                  <div className="flex items-center justify-between p-4 bg-gray-700/50 rounded-2xl border border-gray-600">
                    <div className="flex items-center space-x-3">
                      <KeyIcon className="h-5 w-5 text-blue-400" />
                      <div>
                        <h4 className="text-white font-medium">Two-Factor Authentication</h4>
                        <p className="text-sm text-gray-400">Add an extra layer of security</p>
                      </div>
                    </div>
                    <button className="bg-gradient-to-r from-blue-600 to-blue-700 text-white px-4 py-2 rounded-xl text-sm transition-all duration-200 hover:from-blue-700 hover:to-blue-800">
                      Enable
                    </button>
                  </div>
                  
                  <div className="flex items-center justify-between p-4 bg-gray-700/50 rounded-2xl border border-gray-600">
                    <div className="flex items-center space-x-3">
                      <CogIcon className="h-5 w-5 text-green-400" />
                      <div>
                        <h4 className="text-white font-medium">Change Password</h4>
                        <p className="text-sm text-gray-400">Update your account password</p>
                      </div>
                    </div>
                    <button className="border border-gray-600 text-gray-300 px-4 py-2 rounded-xl text-sm transition-all duration-200 hover:border-blue-500 hover:text-blue-400">
                      Update
                    </button>
                  </div>
                </div>
              </div>
            </div>

                      {/* Notification Preferences */}
          <div className="bg-white dark:bg-gray-800/80 backdrop-blur-sm rounded-3xl shadow-xl border border-gray-200 dark:border-gray-700 hover:shadow-2xl transition-all duration-300">
              <div className="p-8 border-b border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">Notifications</h3>
                    <p className="text-gray-600 dark:text-gray-400">Configure your notification preferences</p>
                  </div>
                  <BellIcon className="h-8 w-8 text-blue-600 dark:text-blue-400" />
                </div>
              </div>
              <div className="p-8">
                <div className="space-y-4">
                  <div className="flex items-center justify-between p-4 bg-gray-100 dark:bg-gray-700/50 rounded-2xl border border-gray-200 dark:border-gray-600">
                    <div className="flex items-center space-x-3">
                      <EnvelopeIcon className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                      <div>
                        <h4 className="text-gray-900 dark:text-white font-medium">Email Notifications</h4>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Receive updates via email</p>
                      </div>
                    </div>
                    <label className="relative inline-flex items-center cursor-pointer">
                      <input 
                        type="checkbox" 
                        checked={profileData.notifications.email} 
                        onChange={(e) => setProfileData({
                          ...profileData, 
                          notifications: {
                            ...profileData.notifications,
                            email: e.target.checked
                          }
                        })}
                        className="sr-only peer" 
                      />
                      <div className="w-11 h-6 bg-gray-600 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-800 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                    </label>
                  </div>
                  
                  <div className="flex items-center justify-between p-4 bg-gray-100 dark:bg-gray-700/50 rounded-2xl border border-gray-200 dark:border-gray-600">
                    <div className="flex items-center space-x-3">
                      <PhoneIcon className="h-5 w-5 text-green-600 dark:text-green-400" />
                      <div>
                        <h4 className="text-gray-900 dark:text-white font-medium">SMS Notifications</h4>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Receive updates via SMS</p>
                      </div>
                    </div>
                    <label className="relative inline-flex items-center cursor-pointer">
                      <input 
                        type="checkbox" 
                        checked={profileData.notifications.sms} 
                        onChange={(e) => setProfileData({
                          ...profileData, 
                          notifications: {
                            ...profileData.notifications,
                            sms: e.target.checked
                          }
                        })}
                        className="sr-only peer" 
                      />
                      <div className="w-11 h-6 bg-gray-600 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-800 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                    </label>
                  </div>
                  
                  <div className="flex items-center justify-between p-4 bg-gray-100 dark:bg-gray-700/50 rounded-2xl border border-gray-200 dark:border-gray-600">
                    <div className="flex items-center space-x-3">
                      <BellIcon className="h-5 w-5 text-yellow-600 dark:text-yellow-400" />
                      <div>
                        <h4 className="text-gray-900 dark:text-white font-medium">Push Notifications</h4>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Receive in-app notifications</p>
                      </div>
                    </div>
                    <label className="relative inline-flex items-center cursor-pointer">
                      <input 
                        type="checkbox" 
                        checked={profileData.notifications.push} 
                        onChange={(e) => setProfileData({
                          ...profileData, 
                          notifications: {
                            ...profileData.notifications,
                            push: e.target.checked
                          }
                        })}
                        className="sr-only peer" 
                      />
                      <div className="w-11 h-6 bg-gray-600 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-800 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                    </label>
                  </div>
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