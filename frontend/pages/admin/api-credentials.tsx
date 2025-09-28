import React, { useState } from 'react'
import Layout from '../../components/Layout'
import {
  KeyIcon,
  EyeIcon,
  EyeSlashIcon,
  PlusIcon,
  TrashIcon,
  DocumentDuplicateIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
} from '@heroicons/react/24/outline'

interface ApiToken {
  id: string
  customerId: string
  customerName: string
  customerEmail: string
  token: string
  name: string
  permissions: string[]
  createdAt: string
  lastUsed?: string
  expiresAt?: string
  isActive: boolean
  rateLimit: number
}

export default function ApiCredentials() {
  // Add error boundary to catch any issues
  try {
    return (
      <Layout>
        <ApiCredentialsContent />
      </Layout>
    )
  } catch (error) {
    console.error('Error in ApiCredentials:', error)
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-red-600 mb-4">Error Loading API Credentials</h1>
          <p className="text-gray-600">There was an error loading the API credentials page.</p>
        </div>
      </div>
    )
  }
}

function ApiCredentialsContent() {
  const [tokens, setTokens] = useState<ApiToken[]>([
    {
      id: 'token-1',
      customerId: 'customer-1',
      customerName: 'John Doe',
      customerEmail: 'john@example.com',
      token: 'sk_live_1234567890abcdef',
      name: 'Production API Key',
      permissions: ['read:energy', 'read:devices', 'read:analytics'],
      createdAt: '2024-01-15T10:30:00Z',
      lastUsed: '2024-01-20T14:22:00Z',
      expiresAt: '2024-12-31T23:59:59Z',
      isActive: true,
      rateLimit: 1000
    },
    {
      id: 'token-2',
      customerId: 'customer-2',
      customerName: 'Jane Smith',
      customerEmail: 'jane@company.com',
      token: 'sk_test_abcdef1234567890',
      name: 'Development API Key',
      permissions: ['read:energy', 'read:devices'],
      createdAt: '2024-01-10T09:15:00Z',
      lastUsed: '2024-01-19T16:45:00Z',
      isActive: true,
      rateLimit: 500
    }
  ])

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)
  const [selectedToken, setSelectedToken] = useState<string | null>(null)
  const [visibleTokens, setVisibleTokens] = useState<Set<string>>(new Set())
  const [newTokenData, setNewTokenData] = useState({
    customerId: '',
    name: '',
    permissions: [] as string[]
  })

  const generateApiToken = () => {
    const prefix = 'sk_live_'
    const randomBytes = Array.from({ length: 16 }, () => 
      Math.floor(Math.random() * 256).toString(16).padStart(2, '0')
    ).join('')
    return prefix + randomBytes
  }

  const createNewToken = async (customerId: string, name: string, permissions: string[]) => {
    // Customer data mapping
    const customerData = {
      'customer-1': { name: 'John Doe', email: 'john@example.com' },
      'customer-2': { name: 'Jane Smith', email: 'jane@company.com' },
      'customer-3': { name: 'Mike Johnson', email: 'mike@enterprise.com' }
    }
    
    const customer = customerData[customerId as keyof typeof customerData]
    
    const newToken: ApiToken = {
      id: `token-${Date.now()}`,
      customerId,
      customerName: customer?.name || 'Unknown Customer',
      customerEmail: customer?.email || 'unknown@example.com',
      token: generateApiToken(),
      name,
      permissions,
      createdAt: new Date().toISOString(),
      isActive: true,
      rateLimit: 1000
    }
    
    setTokens(prev => [...prev, newToken])
    setIsCreateModalOpen(false)
  }

  const toggleTokenVisibility = (tokenId: string) => {
    setVisibleTokens(prev => {
      const newSet = new Set(prev)
      if (newSet.has(tokenId)) {
        newSet.delete(tokenId)
      } else {
        newSet.add(tokenId)
      }
      return newSet
    })
  }

  const revokeToken = (tokenId: string) => {
    setTokens(prev => prev.map(token => 
      token.id === tokenId 
        ? { ...token, isActive: false }
        : token
    ))
  }

  const regenerateToken = (tokenId: string) => {
    setTokens(prev => prev.map(token => 
      token.id === tokenId 
        ? { ...token, token: generateApiToken(), lastUsed: undefined }
        : token
    ))
  }

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text)
    // You could add a toast notification here
  }

  const getStatusColor = (token: ApiToken) => {
    if (!token.isActive) return 'text-red-600 dark:text-red-400'
    if (token.expiresAt && new Date(token.expiresAt) < new Date()) return 'text-yellow-600 dark:text-yellow-400'
    return 'text-green-600 dark:text-green-400'
  }

  const getStatusIcon = (token: ApiToken) => {
    if (!token.isActive) return <ExclamationTriangleIcon className="h-4 w-4" />
    if (token.expiresAt && new Date(token.expiresAt) < new Date()) return <ClockIcon className="h-4 w-4" />
    return <CheckCircleIcon className="h-4 w-4" />
  }

  return (
    <div className="p-6">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          API Credentials Management
        </h1>
        <p className="text-gray-600 dark:text-gray-300 mt-2">
          Generate and manage API authentication tokens for customers with API access.
        </p>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400">
              <KeyIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {tokens.length}
              </div>
              <div className="text-sm text-gray-600 dark:text-gray-400">Total Tokens</div>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400">
              <CheckCircleIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {tokens.filter(t => t.isActive).length}
              </div>
              <div className="text-sm text-gray-600 dark:text-gray-400">Active Tokens</div>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400">
              <ClockIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {tokens.filter(t => t.expiresAt && new Date(t.expiresAt) < new Date()).length}
              </div>
              <div className="text-sm text-gray-600 dark:text-gray-400">Expired Tokens</div>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400">
              <ExclamationTriangleIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {tokens.filter(t => !t.isActive).length}
              </div>
              <div className="text-sm text-gray-600 dark:text-gray-400">Revoked Tokens</div>
            </div>
          </div>
        </div>
      </div>

      {/* Actions */}
      <div className="mb-6">
        <button
          onClick={() => setIsCreateModalOpen(true)}
          className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
        >
          <PlusIcon className="h-5 w-5" />
          <span>Generate New Token</span>
        </button>
      </div>

      {/* Tokens Table */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
            API Tokens
          </h3>
        </div>
        
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead className="bg-gray-50 dark:bg-gray-700">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  Customer
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  Token Name
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  API Token
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  Permissions
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  Last Used
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
              {tokens.map((token) => (
                <tr key={token.id}>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm font-medium text-gray-900 dark:text-white">
                        {token.customerName}
                      </div>
                      <div className="text-sm text-gray-500 dark:text-gray-400">
                        {token.customerEmail}
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900 dark:text-white">
                      {token.name}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center space-x-2">
                      <code className="text-sm bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded font-mono">
                        {visibleTokens.has(token.id) 
                          ? token.token 
                          : token.token.substring(0, 12) + '...'
                        }
                      </code>
                      <button
                        onClick={() => toggleTokenVisibility(token.id)}
                        className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                      >
                        {visibleTokens.has(token.id) ? (
                          <EyeSlashIcon className="h-4 w-4" />
                        ) : (
                          <EyeIcon className="h-4 w-4" />
                        )}
                      </button>
                      <button
                        onClick={() => copyToClipboard(token.token)}
                        className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                      >
                        <DocumentDuplicateIcon className="h-4 w-4" />
                      </button>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex flex-wrap gap-1">
                      {token.permissions.map((permission) => (
                        <span
                          key={permission}
                          className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400"
                        >
                          {permission}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className={`flex items-center space-x-1 ${getStatusColor(token)}`}>
                      {getStatusIcon(token)}
                      <span className="text-sm">
                        {!token.isActive ? 'Revoked' : 
                         token.expiresAt && new Date(token.expiresAt) < new Date() ? 'Expired' : 
                         'Active'}
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                    {token.lastUsed 
                      ? new Date(token.lastUsed).toLocaleDateString()
                      : 'Never'
                    }
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <div className="flex space-x-2">
                      <button
                        onClick={() => regenerateToken(token.id)}
                        className="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300"
                      >
                        Regenerate
                      </button>
                      <button
                        onClick={() => revokeToken(token.id)}
                        className="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300"
                      >
                        Revoke
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* API Documentation */}
      <div className="mt-8 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          API Usage Documentation
        </h3>
        <div className="space-y-4">
          <div>
            <h4 className="font-medium text-gray-900 dark:text-white mb-2">Authentication</h4>
            <p className="text-sm text-gray-600 dark:text-gray-300 mb-2">
              Include your API token in the Authorization header:
            </p>
            <code className="block bg-gray-100 dark:bg-gray-700 p-3 rounded text-sm font-mono">
              Authorization: Bearer sk_live_1234567890abcdef
            </code>
          </div>
          
          <div>
            <h4 className="font-medium text-gray-900 dark:text-white mb-2">Base URL</h4>
            <code className="block bg-gray-100 dark:bg-gray-700 p-3 rounded text-sm font-mono">
              https://www.mysmartwatts.com/api/v1
            </code>
          </div>

          <div>
            <h4 className="font-medium text-gray-900 dark:text-white mb-2">Example Request</h4>
            <code className="block bg-gray-100 dark:bg-gray-700 p-3 rounded text-sm font-mono">
              curl -H "Authorization: Bearer sk_live_1234567890abcdef" \<br/>
              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;https://www.mysmartwatts.com/api/v1/energy/consumption
            </code>
          </div>
        </div>
      </div>

      {/* Create Token Modal */}
      {isCreateModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full mx-4">
            <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                Generate New API Token
              </h3>
            </div>
            
            <div className="px-6 py-4 space-y-4">
              {/* Customer Selection */}
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Customer
                </label>
                <select
                  value={newTokenData.customerId}
                  onChange={(e) => setNewTokenData(prev => ({ ...prev, customerId: e.target.value }))}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select a customer...</option>
                  <option value="customer-1">John Doe (john@example.com)</option>
                  <option value="customer-2">Jane Smith (jane@company.com)</option>
                  <option value="customer-3">Mike Johnson (mike@enterprise.com)</option>
                </select>
              </div>

              {/* Token Name */}
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Token Name
                </label>
                <input
                  type="text"
                  value={newTokenData.name}
                  onChange={(e) => setNewTokenData(prev => ({ ...prev, name: e.target.value }))}
                  placeholder="e.g., Production API Key, Development Key"
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              {/* Permissions */}
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Permissions
                </label>
                <div className="space-y-2">
                  {[
                    { id: 'read:energy', label: 'Read Energy Data', description: 'Access to energy consumption and generation data' },
                    { id: 'read:devices', label: 'Read Device Data', description: 'Access to device status and configuration' },
                    { id: 'read:analytics', label: 'Read Analytics', description: 'Access to analytics and reporting data' },
                    { id: 'write:devices', label: 'Write Device Data', description: 'Modify device settings and configurations' },
                    { id: 'admin:users', label: 'Admin Users', description: 'Manage user accounts and permissions' }
                  ].map((permission) => (
                    <label key={permission.id} className="flex items-start space-x-3">
                      <input
                        type="checkbox"
                        checked={newTokenData.permissions.includes(permission.id)}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setNewTokenData(prev => ({
                              ...prev,
                              permissions: [...prev.permissions, permission.id]
                            }))
                          } else {
                            setNewTokenData(prev => ({
                              ...prev,
                              permissions: prev.permissions.filter(p => p !== permission.id)
                            }))
                          }
                        }}
                        className="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                      />
                      <div>
                        <div className="text-sm font-medium text-gray-900 dark:text-white">
                          {permission.label}
                        </div>
                        <div className="text-xs text-gray-500 dark:text-gray-400">
                          {permission.description}
                        </div>
                      </div>
                    </label>
                  ))}
                </div>
              </div>
            </div>

            <div className="px-6 py-4 border-t border-gray-200 dark:border-gray-700 flex justify-end space-x-3">
              <button
                onClick={() => {
                  setIsCreateModalOpen(false)
                  setNewTokenData({ customerId: '', name: '', permissions: [] })
                }}
                className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  if (newTokenData.customerId && newTokenData.name && newTokenData.permissions.length > 0) {
                    createNewToken(newTokenData.customerId, newTokenData.name, newTokenData.permissions)
                    setNewTokenData({ customerId: '', name: '', permissions: [] })
                  }
                }}
                disabled={!newTokenData.customerId || !newTokenData.name || newTokenData.permissions.length === 0}
                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
              >
                Generate Token
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
