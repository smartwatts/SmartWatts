import React, { createContext, useContext, useEffect, useState } from 'react'
import { useRouter } from 'next/router'
import toast from 'react-hot-toast'

interface User {
  id: string
  email: string
  firstName: string
  lastName: string
  role: string
  isActive: boolean
  createdAt: string
  location?: string
}

interface AuthContextType {
  user: User | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (userData: RegisterData) => Promise<void>
  logout: () => void
  updateProfile: (data: Partial<User>) => Promise<void>
}

interface RegisterData {
  email: string
  password: string
  firstName: string
  lastName: string
  phoneNumber: string
  address: string
  city: string
  state: string
  country: string
  propertyType: string
  propertySize: string
  buildingType: string
  numberOfRooms: string
  numberOfFloors: string
  hasSolar: boolean
  hasGenerator: boolean
  hasInverter: boolean
  currentMeterType: string
  energyProvider: string
  monthlyEnergyBill: string
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}


export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const router = useRouter()

  // Helper function to add auth token to requests
  const getAuthHeaders = () => {
    if (typeof window === 'undefined') {
      return {
        'Content-Type': 'application/json',
      }
    }
    const token = localStorage.getItem('token')
    return {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
    }
  }

  // Check if user is authenticated on mount
  useEffect(() => {
    // Only run on client side to prevent hydration issues
    if (typeof window === 'undefined') {
      setLoading(false)
      return
    }

    const token = localStorage.getItem('token')
    if (token) {
      // Check if it's a mock token for development
      if (token === 'mock-jwt-token-12345') {
        // For mock token, we need to call the profile API to get the correct user data
        // This will use the stored user data from the login response
        checkAuth()
      }
      
      // Validate token format (basic check)
      if (token.length < 10) {
        console.warn('Invalid token format detected, clearing')
        localStorage.removeItem('token')
        setLoading(false)
        router.push('/login')
        return
      }
      
      checkAuth()
    } else {
      setLoading(false)
      // Redirect to login if on a protected route
      const protectedRoutes = ['/dashboard', '/energy', '/analytics', '/devices', '/billing', '/profile', '/partner-services']
      if (protectedRoutes.includes(router.pathname)) {
        router.push('/login')
      }
    }
  }, []) // Only run once on mount

  const checkAuth = async () => {
    try {
      // Only run on client side
      if (typeof window === 'undefined') {
        setLoading(false)
        return
      }

      setLoading(true) // Set loading to true when starting auth check
      const token = localStorage.getItem('token')
      
      if (!token || token.length < 10) {
        console.warn('checkAuth: Invalid or missing token')
        setUser(null)
        setLoading(false)
        localStorage.removeItem('token')
        router.push('/login')
        return
      }

      // Try to validate the token with the backend
      const response = await fetch('/api/proxy?service=user&path=/users/profile', {
        method: 'GET',
        headers: getAuthHeaders(),
      })

      if (response.ok) {
        const userData = await response.json()
        // Additional validation - ensure user data is complete
        if (userData && userData.id && userData.email) {
          setUser(userData)
        } else {
          console.warn('checkAuth: Incomplete user data received')
          localStorage.removeItem('token')
          setUser(null)
          toast.error('Invalid user data. Please log in again.')
          router.push('/login')
        }
      } else if (response.status === 401) {
        // Token is invalid, clear it
        console.warn('checkAuth: 401 Unauthorized, clearing token')
        localStorage.removeItem('token')
        setUser(null)
        toast.error('Session expired. Please log in again.')
        router.push('/login')
      } else {
        // Profile endpoint failed - clear token and require re-authentication
        console.warn('checkAuth: Profile endpoint failed, requiring re-authentication')
        localStorage.removeItem('token')
        setUser(null)
        toast.error('Authentication failed. Please log in again.')
        router.push('/login')
      }
    } catch (error) {
      console.warn('Error in checkAuth:', error)
      // On error, clear token and redirect to login
      localStorage.removeItem('token')
      setUser(null)
      toast.error('Authentication error. Please log in again.')
      router.push('/login')
    } finally {
      setLoading(false)
    }
  }

  const login = async (email: string, password: string) => {
    try {
      // Only run on client side
      if (typeof window === 'undefined') {
        throw new Error('Login can only be performed on client side')
      }

      const response = await fetch('/api/proxy?service=user&path=/users/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ usernameOrEmail: email, password }),
      })
      
      if (!response.ok) {
        let errorData
        try {
          errorData = await response.json()
        } catch (e) {
          // If response is not JSON, use status text
          throw new Error(`Login failed: ${response.statusText || 'Invalid credentials'}`)
        }
        
        // Provide more helpful error messages
        const errorMessage = errorData.message || 'Login failed'
        if (errorMessage.toLowerCase().includes('invalid password')) {
          throw new Error('Invalid password. Please check your password and try again. For test accounts, use: admin@mysmartwatts.com / password')
        } else if (errorMessage.toLowerCase().includes('user not found')) {
          throw new Error('User not found. Please check your email/username. For test accounts, use: admin@mysmartwatts.com')
        }
        throw new Error(errorMessage)
      }
      
      const data = await response.json()
      const { accessToken, userId, username, email: userEmail, role, active } = data
      
      // Transform backend response to match frontend User interface
      const userData: User = {
        id: userId,
        email: userEmail,
        firstName: username.includes('@') ? username.split('@')[0] : (username.split('user')[0] || username), // Extract first name from email or username
        lastName: username.includes('@') ? '' : (username.split('user')[1] || ''), // Extract last name from username if not email
        role: role,
        isActive: active,
        createdAt: new Date().toISOString(),
      }
      
      localStorage.setItem('token', accessToken)
      
      // Set admin email for admin override if applicable
      if (userEmail === 'admin@mysmartwatts.com') {
        localStorage.setItem('adminEmail', userEmail)
      }
      
      setUser(userData)
      
      toast.success('Login successful!')
      
      // Force redirect using window.location for immediate effect
      window.location.href = '/dashboard'
    } catch (error: any) {
      const message = error.message || 'Login failed'
      toast.error(message)
      throw error
    }
  }

  const register = async (userData: RegisterData) => {
    try {
      // Generate username from email (take part before @)
      const username = userData.email.split('@')[0].toLowerCase().replace(/[^a-z0-9_]/g, '_')
      
      // Ensure phone number is in correct format (+234XXXXXXXXXX)
      let phoneNumber = userData.phoneNumber.trim()
      if (!phoneNumber.startsWith('+234')) {
        // If it starts with 0, replace with +234
        if (phoneNumber.startsWith('0')) {
          phoneNumber = '+234' + phoneNumber.substring(1)
        } else if (phoneNumber.startsWith('234')) {
          phoneNumber = '+' + phoneNumber
        } else {
          phoneNumber = '+234' + phoneNumber
        }
      }
      
      // Prepare payload matching backend UserDto requirements
      const payload = {
        username: username,
        email: userData.email,
        password: userData.password,
        firstName: userData.firstName,
        lastName: userData.lastName,
        phoneNumber: phoneNumber,
        // Optional fields that backend accepts
        address: userData.address || null,
        city: userData.city || null,
        state: userData.state || null,
        country: userData.country || null,
      }
      
      const response = await fetch('/api/proxy?service=user&path=/users/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Registration failed' }))
        const errorMessage = errorData.message || errorData.error || 'Registration failed'
        throw new Error(errorMessage)
      }
      
      const data = await response.json()
      // Note: Registration might not return a token immediately
      // We'll need to handle this based on the actual API response
      if (data.accessToken) {
        localStorage.setItem('token', data.accessToken)
        setUser(data.user)
        toast.success('Registration successful!')
        router.push('/dashboard')
      } else {
        toast.success('Registration successful! Please log in.')
        router.push('/login')
      }
    } catch (error: any) {
      const message = error.message || 'Registration failed'
      toast.error(message)
      throw error
    }
  }

  const logout = () => {
    // Only run on client side
    if (typeof window !== 'undefined') {
      // Clear all authentication data
      localStorage.removeItem('token')
    }
    setUser(null)
    setLoading(false)
    
    // Force redirect to login page
    router.push('/login')
    toast.success('Logged out successfully')
  }

  const updateProfile = async (data: Partial<User>) => {
    try {
      const response = await fetch('/api/proxy?service=user&path=/users/profile', {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(data),
      })
      
      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.message || 'Profile update failed')
      }
      
      const userData = await response.json()
      setUser(userData)
      toast.success('Profile updated successfully')
    } catch (error: any) {
      const message = error.message || 'Profile update failed'
      toast.error(message)
      throw error
    }
  }

  const value = {
    user: user ? {
      ...user,
      // Use the role from the API response (no override needed since proxy handles this correctly)
      role: user.role
    } : null,
    loading,
    login,
    register,
    logout,
    updateProfile,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
} 