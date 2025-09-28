import React, { useState } from 'react'
import Head from 'next/head'
import AdminRoute from '../../components/AdminRoute'
import Layout from '../../components/Layout'
import { 
  UserGroupIcon, 
  ChartBarIcon, 
  ClockIcon,
  PlusIcon,
  MagnifyingGlassIcon,
  FunnelIcon,
  ArrowDownTrayIcon,
  EyeIcon,
  PencilIcon,
  TrashIcon,
  CheckCircleIcon,
  XMarkIcon,
  StarIcon
} from '@heroicons/react/24/outline'

interface TeamMember {
  id: string
  name: string
  email: string
  phone: string
  role: 'Sales Manager' | 'Sales Rep' | 'Technical Support' | 'Customer Success' | 'Field Engineer' | 'Admin'
  department: 'Sales' | 'Support' | 'Engineering' | 'Operations' | 'Admin'
  status: 'Active' | 'Inactive' | 'On Leave' | 'Terminated'
  joinDate: string
  lastActive: string
  performance: {
    rating: number
    salesTarget: number
    salesAchieved: number
    customerSatisfaction: number
    tasksCompleted: number
    totalTasks: number
  }
  location: string
  manager: string
  team: string
  skills: string[]
  certifications: string[]
  avatar?: string
}

export default function Teams() {
  return (
    <AdminRoute>
      <Layout>
        <TeamsContent />
      </Layout>
    </AdminRoute>
  )
}

function TeamsContent() {
  const [searchTerm, setSearchTerm] = useState('')
  const [filterRole, setFilterRole] = useState('All')
  const [filterDepartment, setFilterDepartment] = useState('All')
  const [filterStatus, setFilterStatus] = useState('All')
  const [selectedMember, setSelectedMember] = useState<TeamMember | null>(null)

  // Mock data
  const teamMembers: TeamMember[] = [
    {
      id: '1',
      name: 'John Adebayo',
      email: 'john.adebayo@smartwatts.ng',
      phone: '+234 802 123 4567',
      role: 'Sales Manager',
      department: 'Sales',
      status: 'Active',
      joinDate: '2023-01-15',
      lastActive: '2024-01-15',
      performance: {
        rating: 4.8,
        salesTarget: 10000000,
        salesAchieved: 12500000,
        customerSatisfaction: 4.9,
        tasksCompleted: 45,
        totalTasks: 50
      },
      location: 'Lagos',
      manager: 'CEO',
      team: 'Enterprise Sales',
      skills: ['Sales Management', 'Customer Relations', 'Energy Solutions', 'Leadership'],
      certifications: ['Certified Sales Professional', 'Energy Management Certification']
    },
    {
      id: '2',
      name: 'Sarah Mohammed',
      email: 'sarah.mohammed@smartwatts.ng',
      phone: '+234 803 987 6543',
      role: 'Sales Rep',
      department: 'Sales',
      status: 'Active',
      joinDate: '2023-03-20',
      lastActive: '2024-01-14',
      performance: {
        rating: 4.6,
        salesTarget: 5000000,
        salesAchieved: 6200000,
        customerSatisfaction: 4.7,
        tasksCompleted: 38,
        totalTasks: 42
      },
      location: 'Abuja',
      manager: 'John Adebayo',
      team: 'Enterprise Sales',
      skills: ['Sales', 'Customer Relations', 'Solar Solutions', 'Negotiation'],
      certifications: ['Solar Installation Certification']
    },
    {
      id: '3',
      name: 'Michael Okoro',
      email: 'michael.okoro@smartwatts.ng',
      phone: '+234 805 456 7890',
      role: 'Sales Rep',
      department: 'Sales',
      status: 'Active',
      joinDate: '2023-06-10',
      lastActive: '2024-01-15',
      performance: {
        rating: 4.4,
        salesTarget: 3000000,
        salesAchieved: 2800000,
        customerSatisfaction: 4.5,
        tasksCompleted: 32,
        totalTasks: 40
      },
      location: 'Lagos',
      manager: 'John Adebayo',
      team: 'SME Sales',
      skills: ['Sales', 'Customer Relations', 'Smart Home Solutions'],
      certifications: ['Smart Home Technology Certification']
    },
    {
      id: '4',
      name: 'Grace Okafor',
      email: 'grace.okafor@smartwatts.ng',
      phone: '+234 807 123 4567',
      role: 'Technical Support',
      department: 'Support',
      status: 'Active',
      joinDate: '2023-02-28',
      lastActive: '2024-01-15',
      performance: {
        rating: 4.9,
        salesTarget: 0,
        salesAchieved: 0,
        customerSatisfaction: 4.8,
        tasksCompleted: 48,
        totalTasks: 50
      },
      location: 'Lagos',
      manager: 'Support Manager',
      team: 'Technical Support',
      skills: ['Technical Support', 'Energy Systems', 'Troubleshooting', 'Customer Service'],
      certifications: ['Certified Energy Technician', 'Customer Service Excellence']
    },
    {
      id: '5',
      name: 'David Adebayo',
      email: 'david.adebayo@smartwatts.ng',
      phone: '+234 809 876 5432',
      role: 'Field Engineer',
      department: 'Engineering',
      status: 'Active',
      joinDate: '2023-04-15',
      lastActive: '2024-01-14',
      performance: {
        rating: 4.7,
        salesTarget: 0,
        salesAchieved: 0,
        customerSatisfaction: 4.6,
        tasksCompleted: 42,
        totalTasks: 45
      },
      location: 'Abuja',
      manager: 'Engineering Manager',
      team: 'Field Operations',
      skills: ['Installation', 'Maintenance', 'Energy Systems', 'Safety'],
      certifications: ['Electrical Engineering', 'Safety Certification', 'Solar Installation']
    },
    {
      id: '6',
      name: 'Fatima Ibrahim',
      email: 'fatima.ibrahim@smartwatts.ng',
      phone: '+234 811 234 5678',
      role: 'Customer Success',
      department: 'Support',
      status: 'On Leave',
      joinDate: '2023-08-01',
      lastActive: '2024-01-10',
      performance: {
        rating: 4.5,
        salesTarget: 0,
        salesAchieved: 0,
        customerSatisfaction: 4.7,
        tasksCompleted: 35,
        totalTasks: 40
      },
      location: 'Kano',
      manager: 'Support Manager',
      team: 'Customer Success',
      skills: ['Customer Success', 'Account Management', 'Energy Solutions'],
      certifications: ['Customer Success Certification']
    },
    {
      id: '7',
      name: 'Chinedu Okoro',
      email: 'chinedu.okoro@smartwatts.ng',
      phone: '+234 813 456 7890',
      role: 'Sales Rep',
      department: 'Sales',
      status: 'Inactive',
      joinDate: '2023-05-20',
      lastActive: '2023-12-15',
      performance: {
        rating: 3.8,
        salesTarget: 2000000,
        salesAchieved: 1500000,
        customerSatisfaction: 4.2,
        tasksCompleted: 28,
        totalTasks: 35
      },
      location: 'Port Harcourt',
      manager: 'John Adebayo',
      team: 'Regional Sales',
      skills: ['Sales', 'Customer Relations'],
      certifications: []
    }
  ]

  const filteredMembers = teamMembers.filter(member => {
    const matchesSearch = member.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         member.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         member.role.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesRole = filterRole === 'All' || member.role === filterRole
    const matchesDepartment = filterDepartment === 'All' || member.department === filterDepartment
    const matchesStatus = filterStatus === 'All' || member.status === filterStatus
    return matchesSearch && matchesRole && matchesDepartment && matchesStatus
  })

  const totalMembers = teamMembers.length
  const activeMembers = teamMembers.filter(member => member.status === 'Active').length
  const averageRating = teamMembers.reduce((sum, member) => sum + member.performance.rating, 0) / teamMembers.length
  const totalSalesTarget = teamMembers.reduce((sum, member) => sum + member.performance.salesTarget, 0)
  const totalSalesAchieved = teamMembers.reduce((sum, member) => sum + member.performance.salesAchieved, 0)

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'Active': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'Inactive': return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
      case 'On Leave': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
      case 'Terminated': return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getRoleColor = (role: string) => {
    switch (role) {
      case 'Sales Manager': return 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
      case 'Sales Rep': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'Technical Support': return 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400'
      case 'Customer Success': return 'bg-orange-100 text-orange-800 dark:bg-orange-900/20 dark:text-orange-400'
      case 'Field Engineer': return 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900/20 dark:text-indigo-400'
      case 'Admin': return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getPerformanceColor = (rating: number) => {
    if (rating >= 4.5) return 'text-green-600 dark:text-green-400'
    if (rating >= 4.0) return 'text-yellow-600 dark:text-yellow-400'
    return 'text-red-600 dark:text-red-400'
  }

  return (
    <>
      <Head>
        <title>Team Management - SmartWatts Admin</title>
        <meta name="description" content="Manage team members, performance, and departments" />
      </Head>

      <div className="space-y-6">
        {/* Header */}
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Team Management</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage team members, performance, and departments</p>
          </div>
          <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2">
            <PlusIcon className="h-5 w-5" />
            <span>Add Member</span>
          </button>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
                <UserGroupIcon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Members</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{totalMembers}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 dark:bg-green-900/20 rounded-lg">
                <CheckCircleIcon className="h-6 w-6 text-green-600 dark:text-green-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Active Members</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{activeMembers}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-purple-100 dark:bg-purple-900/20 rounded-lg">
                <StarIcon className="h-6 w-6 text-purple-600 dark:text-purple-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Avg Rating</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{averageRating.toFixed(1)}</p>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-orange-100 dark:bg-orange-900/20 rounded-lg">
                <StarIcon className="h-6 w-6 text-orange-600 dark:text-orange-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Sales Achievement</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">
                  {totalSalesTarget > 0 ? Math.round((totalSalesAchieved / totalSalesTarget) * 100) : 0}%
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Filters */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Search</label>
              <div className="relative">
                <MagnifyingGlassIcon className="h-5 w-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search members..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10 w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Role</label>
              <select
                value={filterRole}
                onChange={(e) => setFilterRole(e.target.value)}
                className="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="All">All Roles</option>
                <option value="Sales Manager">Sales Manager</option>
                <option value="Sales Rep">Sales Rep</option>
                <option value="Technical Support">Technical Support</option>
                <option value="Customer Success">Customer Success</option>
                <option value="Field Engineer">Field Engineer</option>
                <option value="Admin">Admin</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Department</label>
              <select
                value={filterDepartment}
                onChange={(e) => setFilterDepartment(e.target.value)}
                className="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="All">All Departments</option>
                <option value="Sales">Sales</option>
                <option value="Support">Support</option>
                <option value="Engineering">Engineering</option>
                <option value="Operations">Operations</option>
                <option value="Admin">Admin</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Status</label>
              <select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
                className="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="All">All Status</option>
                <option value="Active">Active</option>
                <option value="Inactive">Inactive</option>
                <option value="On Leave">On Leave</option>
                <option value="Terminated">Terminated</option>
              </select>
            </div>

            <div className="flex items-end space-x-2">
              <button className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2">
                <FunnelIcon className="h-4 w-4" />
                <span>Filter</span>
              </button>
              <button className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2">
                <ArrowDownTrayIcon className="h-4 w-4" />
                <span>Export</span>
              </button>
            </div>
          </div>
        </div>

        {/* Team Members Table */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
              <thead className="bg-gray-50 dark:bg-gray-700">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Member</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Role</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Department</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Performance</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Sales Target</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Location</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                {filteredMembers.map((member) => (
                  <tr key={member.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-10 w-10">
                          <div className="h-10 w-10 rounded-full bg-gradient-to-r from-blue-500 to-green-500 flex items-center justify-center">
                            <span className="text-sm font-medium text-white">
                              {member.name.split(' ').map(n => n[0]).join('')}
                            </span>
                          </div>
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900 dark:text-white">{member.name}</div>
                          <div className="text-sm text-gray-500 dark:text-gray-400">{member.email}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getRoleColor(member.role)}`}>
                        {member.role}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      {member.department}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(member.status)}`}>
                        {member.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <span className={`text-sm font-medium ${getPerformanceColor(member.performance.rating)}`}>
                          {member.performance.rating}
                        </span>
                        <StarIcon className="h-4 w-4 text-yellow-400 ml-1" />
                        <div className="ml-2 text-xs text-gray-500 dark:text-gray-400">
                          {member.performance.tasksCompleted}/{member.performance.totalTasks} tasks
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {member.performance.salesTarget > 0 ? (
                        <div>
                          <div className="text-sm text-gray-900 dark:text-white">
                            ₦{(member.performance.salesAchieved / 1000000).toFixed(1)}M / ₦{(member.performance.salesTarget / 1000000).toFixed(1)}M
                          </div>
                          <div className="text-xs text-gray-500 dark:text-gray-400">
                            {Math.round((member.performance.salesAchieved / member.performance.salesTarget) * 100)}% achieved
                          </div>
                        </div>
                      ) : (
                        <span className="text-sm text-gray-500 dark:text-gray-400">N/A</span>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      {member.location}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex space-x-2">
                        <button className="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300">
                          <EyeIcon className="h-4 w-4" />
                        </button>
                        <button className="text-indigo-600 hover:text-indigo-900 dark:text-indigo-400 dark:hover:text-indigo-300">
                          <PencilIcon className="h-4 w-4" />
                        </button>
                        <button className="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300">
                          <TrashIcon className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </>
  )
}
