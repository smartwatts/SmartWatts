import React, { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'react-hot-toast'
import { useFeatureFlags } from '../hooks/useFeatureFlags'
import FeatureUpgradeModal from '../components/FeatureUpgradeModal'
import { 
  BuildingOffice2Icon, 
  TruckIcon, 
  MapPinIcon, 
  ClipboardDocumentListIcon,
  PlusIcon,
  PencilIcon,
  TrashIcon,
  EyeIcon,
  XMarkIcon
} from '@heroicons/react/24/outline'
import { facilityApi } from '../utils/api'
import Layout from '../components/Layout'
import AssetModal from '../components/modals/AssetModal'
import FleetModal from '../components/modals/FleetModal'
import SpaceModal from '../components/modals/SpaceModal'
import WorkOrderModal from '../components/modals/WorkOrderModal'

// Constants for enums
const ASSET_TYPES = [
  'ELECTRICAL_EQUIPMENT', 'HVAC_SYSTEM', 'PLUMBING_SYSTEM', 'SECURITY_SYSTEM', 'LIFE_SAFETY_SYSTEM', 'FABRIC_SERVICE',
  'CLEANING_EQUIPMENT', 'CATERING_EQUIPMENT', 'RECEPTION_EQUIPMENT', 'WASTE_MANAGEMENT_EQUIPMENT', 'MAIL_LOGISTICS_EQUIPMENT',
  'FURNITURE', 'OFFICE_EQUIPMENT', 'WORKSTATION_EQUIPMENT', 'IT_EQUIPMENT', 'COMMUNICATION_EQUIPMENT', 'PRESENTATION_EQUIPMENT',
  'VEHICLE', 'TRANSPORT_EQUIPMENT', 'MACHINERY', 'TOOLS', 'OTHER'
]

const ASSET_STATUSES = [
  'OPERATIONAL', 'MAINTENANCE', 'REPAIR', 'OUT_OF_SERVICE', 'RETIRED', 'LOST', 'STOLEN', 'DAMAGED', 'UNDER_INSPECTION', 'PENDING_APPROVAL'
]

const FLEET_TYPES = [
  'PASSENGER_VEHICLE', 'TRUCK', 'VAN', 'BUS', 'MOTORCYCLE', 'FORKLIFT', 'CRANE', 'EXCAVATOR', 'BULLDOZER', 'TRACTOR', 'TRAILER', 'BOAT', 'AIRCRAFT', 'OTHER'
]

const FLEET_STATUSES = [
  'OPERATIONAL', 'IN_USE', 'AVAILABLE', 'MAINTENANCE', 'REPAIR', 'OUT_OF_SERVICE', 'RETIRED', 'LOST', 'STOLEN', 'DAMAGED', 'UNDER_INSPECTION', 'RESERVED'
]

const SPACE_TYPES = [
  'OFFICE', 'CONFERENCE_ROOM', 'MEETING_ROOM', 'BREAK_ROOM', 'KITCHEN', 'RESTROOM', 'STORAGE_ROOM', 'SERVER_ROOM', 'LABORATORY', 'WORKSHOP',
  'PARKING_SPACE', 'OUTDOOR_AREA', 'RECEPTION_AREA', 'LOBBY', 'CORRIDOR', 'STAIRWELL', 'ELEVATOR', 'UTILITY_ROOM', 'MAINTENANCE_ROOM', 'SECURITY_ROOM', 'OTHER'
]

const SPACE_STATUSES = [
  'AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE', 'OUT_OF_SERVICE', 'RESTRICTED', 'UNDER_RENOVATION', 'CLEANING', 'OTHER'
]

const WORK_ORDER_TYPES = [
  'PREVENTIVE_MAINTENANCE', 'CORRECTIVE_MAINTENANCE', 'EMERGENCY_REPAIR', 'INSPECTION', 'INSTALLATION', 'REPLACEMENT', 'UPGRADE', 'CLEANING', 'CALIBRATION', 'TESTING', 'OTHER'
]

const WORK_ORDER_PRIORITIES = [
  'LOW', 'MEDIUM', 'HIGH', 'CRITICAL', 'EMERGENCY'
]

const WORK_ORDER_STATUSES = [
  'DRAFT', 'SUBMITTED', 'APPROVED', 'ASSIGNED', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED', 'REJECTED'
]

// Types for facility entities
interface Asset {
  id: number
  assetCode: string
  name: string
  description: string
  assetType: string
  status: string
  location: string
  building?: string
  floor?: string
  room?: string
  manufacturer?: string
  model?: string
  serialNumber?: string
  installationDate?: string
  purchaseCost?: number
  currentValue?: number
  isActive: boolean
  createdAt: string
  updatedAt: string
}

interface FleetItem {
  id: number
  name: string
  type: string
  make: string
  model: string
  modelYear: number
  licensePlate: string
  status: string
  currentFuelLevel: number
  fuelCapacity: number
  nextMaintenanceDate: string
  insuranceExpiryDate: string
  registrationExpiryDate: string
  isActive: boolean
  createdAt: string
  updatedAt: string
}

interface Space {
  id: number
  name: string
  type: string
  status: string
  building: string
  floor: string
  room: string
  capacity: number
  area: number
  isActive: boolean
  createdAt: string
  updatedAt: string
}

interface WorkOrder {
  id: number
  title: string
  description: string
  type: string
  priority: string
  status: string
  assignedTo: string
  dueDate: string
  estimatedHours: number
  actualHours?: number
  isActive: boolean
  createdAt: string
  updatedAt: string
}

const FacilityPage = () => {
  const { isFeatureEnabled } = useFeatureFlags()
  const [activeTab, setActiveTab] = useState('assets')
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<any>(null)
  const [modalType, setModalType] = useState<'asset' | 'fleet' | 'space' | 'workorder'>('asset')
  const [showUpgradeModal, setShowUpgradeModal] = useState(false)
  
  const queryClient = useQueryClient()

  // Queries with safe data handling
  const { data: assetsData, isLoading: assetsLoading, error: assetsError } = useQuery({
    queryKey: ['facility', 'assets'],
    queryFn: async () => {
      try {
        const result = await facilityApi.getAssets()
        return Array.isArray(result) ? result : []
      } catch (error) {
        console.error('Failed to fetch assets:', error)
        toast.error('Failed to load assets')
        return []
      }
    },
    retry: 1
  })

  const { data: fleetData, isLoading: fleetLoading, error: fleetError } = useQuery({
    queryKey: ['facility', 'fleet'],
    queryFn: async () => {
      try {
        const result = await facilityApi.getFleet()
        return Array.isArray(result) ? result : []
      } catch (error) {
        console.error('Failed to fetch fleet:', error)
        toast.error('Failed to load fleet data')
        return []
      }
    },
    retry: 1
  })

  const { data: spacesData, isLoading: spacesLoading, error: spacesError } = useQuery({
    queryKey: ['facility', 'spaces'],
    queryFn: async () => {
      try {
        const result = await facilityApi.getSpaces()
        return Array.isArray(result) ? result : []
      } catch (error) {
        console.error('Failed to fetch spaces:', error)
        toast.error('Failed to load spaces')
        return []
      }
    },
    retry: 1
  })

  const { data: workOrdersData, isLoading: workOrdersLoading, error: workOrdersError } = useQuery({
    queryKey: ['facility', 'workorders'],
    queryFn: async () => {
      try {
        const result = await facilityApi.getWorkOrders()
        return Array.isArray(result) ? result : []
      } catch (error) {
        console.error('Failed to fetch work orders:', error)
        toast.error('Failed to load work orders')
        return []
      }
    },
    retry: 1
  })

  // Ensure data is always an array
  const assets = Array.isArray(assetsData) ? assetsData : []
  const fleet = Array.isArray(fleetData) ? fleetData : []
  const spaces = Array.isArray(spacesData) ? spacesData : []
  const workOrders = Array.isArray(workOrdersData) ? workOrdersData : []

  // Mock data for development when backend is not available
  const mockAssets = [
    {
      id: 1,
      assetCode: 'HVAC-001',
      name: 'Main HVAC System',
      description: 'Central heating and cooling system',
      assetType: 'HVAC',
      status: 'OPERATIONAL',
      location: 'Main Building',
      building: 'Main Building',
      floor: '1st Floor',
      room: 'Mechanical Room',
      manufacturer: 'Carrier',
      model: '48TC',
      serialNumber: 'HVAC-2024-001',
      installationDate: '2024-01-15',
      purchaseCost: 25000,
      currentValue: 22000,
      isActive: true,
      createdAt: '2024-01-15T10:00:00Z',
      updatedAt: '2024-01-15T10:00:00Z'
    }
  ]

  const mockFleet = [
    {
      id: 1,
      name: 'Maintenance Van',
      type: 'VAN',
      make: 'Ford',
      model: 'Transit',
      modelYear: 2023,
      licensePlate: 'ABC-123',
      status: 'ACTIVE',
      currentFuelLevel: 75,
      fuelCapacity: 80,
      nextMaintenanceDate: '2024-06-15',
      insuranceExpiryDate: '2024-12-31',
      registrationExpiryDate: '2024-12-31',
      isActive: true,
      createdAt: '2024-01-15T10:00:00Z',
      updatedAt: '2024-01-15T10:00:00Z'
    }
  ]

  const mockSpaces = [
    {
      id: 1,
      name: 'Conference Room A',
      type: 'MEETING_ROOM',
      status: 'AVAILABLE',
      building: 'Main Building',
      floor: '2nd Floor',
      room: '201',
      capacity: 20,
      area: 150,
      isActive: true,
      createdAt: '2024-01-15T10:00:00Z',
      updatedAt: '2024-01-15T10:00:00Z'
    }
  ]

  const mockWorkOrders = [
    {
      id: 1,
      title: 'HVAC Maintenance',
      description: 'Regular maintenance of main HVAC system',
      type: 'PREVENTIVE',
      priority: 'MEDIUM',
      status: 'IN_PROGRESS',
      assignedTo: 'John Smith',
      dueDate: '2024-06-15',
      estimatedHours: 4,
      actualHours: 2,
      isActive: true,
      createdAt: '2024-01-15T10:00:00Z',
      updatedAt: '2024-01-15T10:00:00Z'
    }
  ]

  // Use mock data if all services are failing and we're in development
  const useMockData = process.env.NODE_ENV === 'development' && 
    assetsError && fleetError && spacesError && workOrdersError

  const displayAssets = useMockData ? mockAssets : assets
  const displayFleet = useMockData ? mockFleet : fleet
  const displaySpaces = useMockData ? mockSpaces : spaces
  const displayWorkOrders = useMockData ? mockWorkOrders : workOrders

  // Mutations
  const createAssetMutation = useMutation({
    mutationFn: facilityApi.createAsset,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['facility', 'assets'] })
      toast.success('Asset created successfully')
      setIsModalOpen(false)
    },
    onError: (error) => {
      toast.error('Failed to create asset')
      console.error('Create asset error:', error)
    }
  })

  const createFleetMutation = useMutation({
    mutationFn: facilityApi.createFleetItem,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['facility', 'fleet'] })
      toast.success('Fleet item created successfully')
      setIsModalOpen(false)
    },
    onError: (error) => {
      toast.error('Failed to create fleet item')
      console.error('Create fleet error:', error)
    }
  })

  const createSpaceMutation = useMutation({
    mutationFn: facilityApi.createSpace,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['facility', 'spaces'] })
      toast.success('Space created successfully')
      setIsModalOpen(false)
    },
    onError: (error) => {
      toast.error('Failed to create space')
      console.error('Create space error:', error)
    }
  })

  const createWorkOrderMutation = useMutation({
    mutationFn: facilityApi.createWorkOrder,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['facility', 'workorders'] })
      toast.success('Work order created successfully')
      setIsModalOpen(false)
    },
    onError: (error) => {
      toast.error('Failed to create work order')
      console.error('Create work order error:', error)
    }
  })

  const handleCreate = (type: 'asset' | 'fleet' | 'space' | 'workorder') => {
    setModalType(type)
    setEditingItem(null)
    setIsModalOpen(true)
  }

  const handleEdit = (item: any, type: 'asset' | 'fleet' | 'space' | 'workorder') => {
    setModalType(type)
    setEditingItem(item)
    setIsModalOpen(true)
  }

  const handleDelete = async (id: number, type: 'asset' | 'fleet' | 'space' | 'workorder') => {
    if (!confirm('Are you sure you want to delete this item?')) return

    try {
      switch (type) {
        case 'asset':
          await facilityApi.deleteAsset(id.toString())
          queryClient.invalidateQueries({ queryKey: ['facility', 'assets'] })
          break
        case 'fleet':
          await facilityApi.deleteFleetItem(id.toString())
          queryClient.invalidateQueries({ queryKey: ['facility', 'fleet'] })
          break
        case 'space':
          await facilityApi.deleteSpace(id.toString())
          queryClient.invalidateQueries({ queryKey: ['facility', 'spaces'] })
          break
        case 'workorder':
          await facilityApi.deleteWorkOrder(id.toString())
          queryClient.invalidateQueries({ queryKey: ['facility', 'workorders'] })
          break
      }
      toast.success('Item deleted successfully')
    } catch (error) {
      toast.error('Failed to delete item')
      console.error('Delete error:', error)
    }
  }

  const handleSaveAsset = async (data: any) => {
    try {
      if (editingItem) {
        await facilityApi.updateAsset(editingItem.id.toString(), data)
        toast.success('Asset updated successfully')
      } else {
        await facilityApi.createAsset(data)
        toast.success('Asset created successfully')
      }
      queryClient.invalidateQueries({ queryKey: ['facility', 'assets'] })
      setIsModalOpen(false)
      setEditingItem(null)
    } catch (error) {
      toast.error('Failed to save asset')
      console.error('Save asset error:', error)
    }
  }

  const handleSaveFleet = async (data: any) => {
    try {
      if (editingItem) {
        await facilityApi.updateFleetItem(editingItem.id.toString(), data)
        toast.success('Fleet item updated successfully')
      } else {
        await facilityApi.createFleetItem(data)
        toast.success('Fleet item created successfully')
      }
      queryClient.invalidateQueries({ queryKey: ['facility', 'fleet'] })
      setIsModalOpen(false)
      setEditingItem(null)
    } catch (error) {
      toast.error('Failed to save fleet item')
      console.error('Save fleet error:', error)
    }
  }

  const handleSaveSpace = async (data: any) => {
    try {
      if (editingItem) {
        await facilityApi.updateSpace(editingItem.id.toString(), data)
        toast.success('Space updated successfully')
      } else {
        await facilityApi.createSpace(data)
        toast.success('Space created successfully')
      }
      queryClient.invalidateQueries({ queryKey: ['facility', 'spaces'] })
      setIsModalOpen(false)
      setEditingItem(null)
    } catch (error) {
      toast.error('Failed to save space')
      console.error('Save space error:', error)
    }
  }

  const handleSaveWorkOrder = async (data: any) => {
    try {
      if (editingItem) {
        await facilityApi.updateWorkOrder(editingItem.id.toString(), data)
        toast.success('Work order updated successfully')
      } else {
        await facilityApi.createWorkOrder(data)
        toast.success('Work order created successfully')
      }
      queryClient.invalidateQueries({ queryKey: ['facility', 'workorders'] })
      setIsModalOpen(false)
      setEditingItem(null)
    } catch (error) {
      toast.error('Failed to save work order')
      console.error('Save work order error:', error)
    }
  }

  const tabs = [
    { id: 'assets', name: 'Assets', icon: BuildingOffice2Icon, count: displayAssets.length },
    { id: 'fleet', name: 'Fleet', icon: TruckIcon, count: displayFleet.length },
    { id: 'spaces', name: 'Spaces', icon: MapPinIcon, count: displaySpaces.length },
    { id: 'workorders', name: 'Work Orders', icon: ClipboardDocumentListIcon, count: displayWorkOrders.length },
  ]

  const renderAssetsTab = () => (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h3 className="text-2xl font-bold text-gray-900 dark:text-white">Asset Management</h3>
          <p className="text-gray-600 dark:text-gray-400 mt-1">Manage and track facility assets</p>
        </div>
        <button
          onClick={() => handleCreate('asset')}
          className="bg-gradient-to-r from-blue-600 to-blue-700 text-white px-6 py-3 rounded-xl hover:from-blue-700 hover:to-blue-800 flex items-center gap-2 font-medium shadow-lg hover:shadow-xl transition-all duration-200"
        >
          <PlusIcon className="w-5 h-5" />
          Add Asset
        </button>
      </div>
      
      {assetsLoading ? (
        <div className="space-y-6">
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-3"></div>
            <p className="text-gray-600 dark:text-gray-400 text-sm">Loading assets...</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3].map((i) => (
              <div key={i} className="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 animate-pulse">
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <div className="h-6 bg-gray-200 dark:bg-gray-700 rounded mb-2"></div>
                    <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-24"></div>
                  </div>
                  <div className="h-6 w-20 bg-gray-200 dark:bg-gray-700 rounded-full"></div>
                </div>
                <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded mb-4"></div>
                <div className="space-y-2 mb-4">
                  {[1, 2, 3].map((j) => (
                    <div key={j} className="flex items-center">
                      <div className="h-4 w-16 bg-gray-200 dark:bg-gray-700 rounded mr-2"></div>
                      <div className="h-4 flex-1 bg-gray-200 dark:bg-gray-700 rounded"></div>
                    </div>
                  ))}
                </div>
                <div className="flex gap-2 pt-3 border-t border-gray-100 dark:border-gray-700">
                  <div className="flex-1 h-8 bg-gray-200 dark:bg-gray-700 rounded-lg"></div>
                  <div className="flex-1 h-8 bg-gray-200 dark:bg-gray-700 rounded-lg"></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : assetsError ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-red-100 dark:bg-red-900 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <p className="text-red-600 dark:text-red-400 font-medium mb-2">Failed to load assets</p>
          <button 
            onClick={() => window.location.reload()} 
            className="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300 underline"
          >
            Retry
          </button>
        </div>
      ) : !Array.isArray(assets) ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-yellow-100 dark:bg-yellow-900 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-yellow-600 dark:text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <p className="text-red-600 dark:text-red-400 font-medium mb-2">Invalid assets data format</p>
          <p className="text-sm text-gray-500 dark:text-gray-400">Expected array, got: {typeof assets}</p>
        </div>
      ) : displayAssets.length === 0 ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-gray-100 dark:bg-gray-700 rounded-full flex items-center justify-center mx-auto mb-4">
            <BuildingOffice2Icon className="w-8 h-8 text-gray-400 dark:text-gray-500" />
          </div>
          <p className="text-gray-500 dark:text-gray-400 font-medium mb-2">No assets found</p>
          <p className="text-sm text-gray-400 dark:text-gray-500">Get started by adding your first asset</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {displayAssets.map((asset: Asset) => (
            <div key={asset.id} className="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm hover:shadow-lg hover:scale-[1.02] hover:border-blue-300 dark:hover:border-blue-600 transition-all duration-300 group cursor-pointer">
              <div className="flex justify-between items-start mb-4">
                <div className="flex-1">
                  <h4 className="font-bold text-lg text-gray-900 dark:text-white mb-1">{asset.name}</h4>
                  <p className="text-sm text-gray-600 dark:text-gray-400">{asset.assetCode}</p>
                </div>
                <span className={`px-3 py-1.5 text-xs font-semibold rounded-full border ${
                  asset.status === 'OPERATIONAL' ? 'bg-green-50 text-green-700 border-green-200 dark:bg-green-900/30 dark:text-green-300 dark:border-green-700' :
                  asset.status === 'MAINTENANCE' ? 'bg-yellow-50 text-yellow-700 border-yellow-200 dark:bg-yellow-900/30 dark:text-yellow-300 dark:border-yellow-700' :
                  asset.status === 'REPAIR' ? 'bg-orange-50 text-orange-700 border-orange-200 dark:bg-orange-900/30 dark:text-orange-300 dark:border-orange-700' :
                  asset.status === 'OUT_OF_SERVICE' ? 'bg-red-50 text-red-700 border-red-200 dark:bg-red-900/30 dark:text-red-300 dark:border-red-700' :
                  'bg-gray-50 text-gray-700 border-gray-200 dark:bg-gray-900/30 dark:text-gray-300 dark:border-gray-700'
                }`}>
                  {asset.status.replace(/_/g, ' ')}
                </span>
              </div>
              
              <p className="text-sm text-gray-600 dark:text-gray-400 mb-4 line-clamp-2">{asset.description}</p>
              
              <div className="space-y-2 mb-4">
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">Type:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{asset.assetType}</span>
                </div>
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">Location:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{asset.location}</span>
                </div>
                {asset.manufacturer && (
                  <div className="flex items-center text-sm">
                    <span className="text-gray-500 dark:text-gray-400 w-16">Make:</span>
                    <span className="text-gray-900 dark:text-white font-medium">{asset.manufacturer}</span>
                  </div>
                )}
                {asset.model && (
                  <div className="flex items-center text-sm">
                    <span className="text-gray-500 dark:text-gray-400 w-16">Model:</span>
                    <span className="text-gray-900 dark:text-white font-medium">{asset.model}</span>
                  </div>
                )}
              </div>
              
              <div className="flex gap-2 pt-3 border-t border-gray-100 dark:border-gray-700">
                <button
                  onClick={() => handleEdit(asset, 'asset')}
                  className="flex-1 bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 hover:bg-blue-100 dark:hover:bg-blue-900/30 hover:scale-105 py-2 px-3 rounded-lg text-sm font-medium transition-all duration-200 flex items-center justify-center gap-2 group"
                >
                  <PencilIcon className="w-4 h-4 group-hover:rotate-12 transition-transform duration-200" />
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(asset.id, 'asset')}
                  className="flex-1 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/30 hover:scale-105 py-2 px-3 rounded-lg text-sm font-medium transition-all duration-200 flex items-center justify-center gap-2 group"
                >
                  <TrashIcon className="w-4 h-4 group-hover:scale-110 transition-transform duration-200" />
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )

  const renderFleetTab = () => (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h3 className="text-2xl font-bold text-gray-900 dark:text-white">Fleet Management</h3>
          <p className="text-gray-600 dark:text-gray-400 mt-1">Manage vehicles and equipment</p>
        </div>
        <button
          onClick={() => handleCreate('fleet')}
          className="bg-gradient-to-r from-green-600 to-green-700 text-white px-6 py-3 rounded-xl hover:from-green-700 hover:to-green-800 flex items-center gap-2 shadow-lg hover:shadow-xl transition-all duration-200"
        >
          <PlusIcon className="w-5 h-5" />
          Add Vehicle
        </button>
      </div>
      
      {fleetLoading ? (
        <div className="space-y-6">
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-green-600 mx-auto mb-3"></div>
            <p className="text-gray-600 dark:text-gray-400 text-sm">Loading fleet...</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3].map((i) => (
              <div key={i} className="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 animate-pulse">
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <div className="h-6 bg-gray-200 dark:bg-gray-700 rounded mb-2"></div>
                    <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-32"></div>
                  </div>
                  <div className="h-6 w-20 bg-gray-200 dark:bg-gray-700 rounded-full"></div>
                </div>
                <div className="space-y-2 mb-4">
                  {[1, 2, 3, 4].map((j) => (
                    <div key={j} className="flex items-center">
                      <div className="h-4 w-16 bg-gray-200 dark:bg-gray-700 rounded mr-2"></div>
                      <div className="h-4 flex-1 bg-gray-200 dark:bg-gray-700 rounded"></div>
                    </div>
                  ))}
                </div>
                <div className="flex gap-2 pt-3 border-t border-gray-100 dark:border-gray-700">
                  <div className="flex-1 h-8 bg-gray-200 dark:bg-gray-700 rounded-lg"></div>
                  <div className="flex-1 h-8 bg-gray-200 dark:bg-gray-700 rounded-lg"></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : fleetError ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-red-100 dark:bg-red-900 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <p className="text-red-600 dark:text-red-400 font-medium mb-2">Failed to load fleet data</p>
          <button 
            onClick={() => window.location.reload()} 
            className="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300 underline"
          >
            Retry
          </button>
        </div>
      ) : !Array.isArray(displayFleet) ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-yellow-100 dark:bg-yellow-900 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-yellow-600 dark:text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <p className="text-red-600 dark:text-red-400 font-medium mb-2">Invalid fleet data format</p>
          <p className="text-sm text-gray-500 dark:text-gray-400">Expected array, got: {typeof displayFleet}</p>
        </div>
      ) : displayFleet.length === 0 ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-gray-100 dark:bg-gray-700 rounded-full flex items-center justify-center mx-auto mb-4">
            <TruckIcon className="w-8 h-8 text-gray-400 dark:text-gray-500" />
          </div>
          <p className="text-gray-500 dark:text-gray-400 font-medium mb-2">No fleet items found</p>
          <p className="text-sm text-gray-400 dark:text-gray-500">Get started by adding your first vehicle</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {displayFleet.map((item: FleetItem) => (
            <div key={item.id} className="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm hover:shadow-lg hover:scale-[1.02] hover:border-green-300 dark:hover:border-green-600 transition-all duration-300 group cursor-pointer">
              <div className="flex justify-between items-start mb-4">
                <div className="flex-1">
                  <h4 className="font-bold text-lg text-gray-900 dark:text-white mb-1">{item.name}</h4>
                  <p className="text-sm text-gray-600 dark:text-gray-400">{item.make} {item.model} ({item.modelYear})</p>
                </div>
                <span className={`px-3 py-1.5 text-xs font-semibold rounded-full border ${
                  item.status === 'OPERATIONAL' ? 'bg-green-50 text-green-700 border-green-200 dark:bg-green-900/30 dark:text-green-300 dark:border-green-700' :
                  item.status === 'IN_USE' ? 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-900/30 dark:text-blue-300 dark:border-blue-700' :
                  item.status === 'AVAILABLE' ? 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-900/30 dark:text-emerald-300 dark:border-emerald-700' :
                  item.status === 'MAINTENANCE' ? 'bg-yellow-50 text-yellow-700 border-yellow-200 dark:bg-yellow-900/30 dark:text-yellow-300 dark:border-yellow-700' :
                  item.status === 'REPAIR' ? 'bg-orange-50 text-orange-700 border-orange-200 dark:bg-orange-900/30 dark:text-orange-300 dark:border-orange-700' :
                  item.status === 'OUT_OF_SERVICE' ? 'bg-red-50 text-red-700 border-red-200 dark:bg-red-900/30 dark:text-red-300 dark:border-red-700' :
                  'bg-gray-50 text-gray-700 border-gray-200 dark:bg-gray-900/30 dark:text-gray-300 dark:border-gray-700'
                }`}>
                  {item.status.replace(/_/g, ' ')}
                </span>
              </div>

              <div className="space-y-2 mb-4">
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">License:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{item.licensePlate}</span>
                </div>
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">Type:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{item.type}</span>
                </div>
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">Fuel:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{item.currentFuelLevel}/{item.fuelCapacity}L</span>
                </div>
                {item.nextMaintenanceDate && (
                  <div className="flex items-center text-sm">
                    <span className="text-gray-500 dark:text-gray-400 w-16">Next Service:</span>
                    <span className="text-gray-900 dark:text-white font-medium">{new Date(item.nextMaintenanceDate).toLocaleDateString()}</span>
                  </div>
                )}
                {item.insuranceExpiryDate && (
                  <div className="flex items-center text-sm">
                    <span className="text-gray-500 dark:text-gray-400 w-16">Insurance:</span>
                    <span className="text-gray-900 dark:text-white font-medium">{new Date(item.insuranceExpiryDate).toLocaleDateString()}</span>
                  </div>
                )}
              </div>

              <div className="flex gap-2 pt-3 border-t border-gray-100 dark:border-gray-700">
                <button
                  onClick={() => handleEdit(item, 'fleet')}
                  className="flex-1 bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 hover:bg-blue-100 dark:hover:bg-blue-900/30 hover:scale-105 py-2 px-3 rounded-lg text-sm font-medium transition-all duration-200 flex items-center justify-center gap-2 group"
                >
                  <PencilIcon className="w-4 h-4 group-hover:rotate-12 transition-transform duration-200" />
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(item.id, 'fleet')}
                  className="flex-1 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/30 hover:scale-105 py-2 px-3 rounded-lg text-sm font-medium transition-all duration-200 flex items-center justify-center gap-2 group"
                >
                  <TrashIcon className="w-4 h-4 group-hover:scale-110 transition-transform duration-200" />
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )

  const renderSpacesTab = () => (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Space Management</h3>
        <button
          onClick={() => handleCreate('space')}
          className="bg-gradient-to-r from-purple-600 to-purple-700 text-white px-6 py-3 rounded-xl hover:from-purple-700 hover:to-purple-800 flex items-center gap-2 font-medium shadow-md transition-all duration-200"
        >
          <PlusIcon className="w-5 h-5" />
          Add Space
        </button>
      </div>
      
      {spacesLoading ? (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto mb-4"></div>
          <p className="text-gray-600 dark:text-gray-400">Loading spaces...</p>
        </div>
      ) : spacesError ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-red-100 dark:bg-red-900 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <p className="text-red-600 dark:text-red-400 font-medium mb-2">Failed to load spaces data</p>
          <button 
            onClick={() => window.location.reload()} 
            className="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300 underline"
          >
            Retry
          </button>
        </div>
      ) : !Array.isArray(displaySpaces) ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-yellow-100 dark:bg-yellow-900 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-yellow-600 dark:text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <p className="text-red-600 dark:text-red-400 font-medium mb-2">Invalid spaces data format</p>
          <p className="text-sm text-gray-500 dark:text-gray-400">Expected array, got: {typeof displaySpaces}</p>
        </div>
      ) : displaySpaces.length === 0 ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-gray-100 dark:bg-gray-700 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-gray-400 dark:text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
            </svg>
          </div>
          <p className="text-gray-500 dark:text-gray-400 font-medium mb-2">No spaces found</p>
          <p className="text-sm text-gray-400 dark:text-gray-500">Get started by adding your first space</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {displaySpaces.map((space: Space) => (
            <div key={space.id} className="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm hover:shadow-md transition-all duration-200">
              <div className="flex justify-between items-start mb-4">
                <div className="flex-1">
                  <h4 className="font-bold text-lg text-gray-900 dark:text-white mb-1">{space.name}</h4>
                  <p className="text-sm text-gray-600 dark:text-gray-400">{space.type}</p>
                </div>
                <span className={`px-3 py-1 text-xs font-semibold rounded-full ${
                  space.status === 'AVAILABLE' ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' :
                  space.status === 'OCCUPIED' ? 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200' :
                  'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                }`}>
                  {space.status}
                </span>
              </div>

              <div className="space-y-2 mb-4">
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">Building:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{space.building}</span>
                </div>
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">Floor:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{space.floor}</span>
                </div>
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">Room:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{space.room}</span>
                </div>
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">Capacity:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{space.capacity} people</span>
                </div>
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">Area:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{space.area} m²</span>
                </div>
              </div>

              <div className="flex gap-2 pt-3 border-t border-gray-100 dark:border-gray-700">
                <button
                  onClick={() => handleEdit(space, 'space')}
                  className="flex-1 bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 hover:bg-blue-100 dark:hover:bg-blue-900/30 py-2 px-3 rounded-lg text-sm font-medium transition-colors duration-200 flex items-center justify-center gap-2"
                >
                  <PencilIcon className="w-4 h-4" />
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(space.id, 'space')}
                  className="flex-1 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/30 py-2 px-3 rounded-lg text-sm font-medium transition-colors duration-200 flex items-center justify-center gap-2"
                >
                  <TrashIcon className="w-4 h-4" />
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )

  const renderWorkOrdersTab = () => (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Work Order Management</h3>
        <button
          onClick={() => handleCreate('workorder')}
          className="bg-gradient-to-r from-orange-600 to-orange-700 text-white px-6 py-3 rounded-xl hover:from-orange-700 hover:to-orange-800 flex items-center gap-2 font-medium shadow-md transition-all duration-200"
        >
          <PlusIcon className="w-5 h-5" />
          Create Work Order
        </button>
      </div>
      
      {workOrdersLoading ? (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-600 mx-auto mb-4"></div>
          <p className="text-gray-600 dark:text-gray-400">Loading work orders...</p>
        </div>
      ) : workOrdersError ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-red-100 dark:bg-red-900 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <p className="text-red-600 dark:text-red-400 font-medium mb-2">Failed to load work orders data</p>
          <button 
            onClick={() => window.location.reload()} 
            className="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300 underline"
          >
            Retry
          </button>
        </div>
      ) : !Array.isArray(displayWorkOrders) ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-yellow-100 dark:bg-yellow-900 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-yellow-600 dark:text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <p className="text-red-600 dark:text-red-400 font-medium mb-2">Invalid work orders data format</p>
          <p className="text-sm text-gray-500 dark:text-gray-400">Expected array, got: {typeof displayWorkOrders}</p>
        </div>
      ) : displayWorkOrders.length === 0 ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-gray-100 dark:bg-gray-700 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-gray-400 dark:text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
            </svg>
          </div>
          <p className="text-gray-500 dark:text-gray-400 font-medium mb-2">No work orders found</p>
          <p className="text-sm text-gray-400 dark:text-gray-500">Get started by creating your first work order</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {displayWorkOrders.map((order: WorkOrder) => (
            <div key={order.id} className="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm hover:shadow-md transition-all duration-200">
              <div className="flex justify-between items-start mb-4">
                <div className="flex-1">
                  <h4 className="font-bold text-lg text-gray-900 dark:text-white mb-1">{order.title}</h4>
                  <p className="text-sm text-gray-600 dark:text-gray-400 line-clamp-2">{order.description}</p>
                </div>
                <div className="flex flex-col gap-2 ml-4">
                  <span className={`px-3 py-1 text-xs font-semibold rounded-full ${
                    order.priority === 'HIGH' ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200' :
                    order.priority === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200' :
                    'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                  }`}>
                    {order.priority}
                  </span>
                  <span className={`px-3 py-1 text-xs font-semibold rounded-full ${
                    order.status === 'COMPLETED' ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' :
                    order.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200' :
                    'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
                  }`}>
                    {order.status}
                  </span>
                </div>
              </div>

              <div className="space-y-2 mb-4">
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">Type:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{order.type}</span>
                </div>
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">Assigned:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{order.assignedTo}</span>
                </div>
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">Due:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{new Date(order.dueDate).toLocaleDateString()}</span>
                </div>
                <div className="flex items-center text-sm">
                  <span className="text-gray-500 dark:text-gray-400 w-16">Hours:</span>
                  <span className="text-gray-900 dark:text-white font-medium">{order.estimatedHours}h</span>
                </div>
                {order.actualHours && (
                  <div className="flex items-center text-sm">
                    <span className="text-gray-500 dark:text-gray-400 w-16">Actual:</span>
                    <span className="text-gray-900 dark:text-white font-medium">{order.actualHours}h</span>
                  </div>
                )}
              </div>

              <div className="flex gap-2 pt-3 border-t border-gray-100 dark:border-gray-700">
                <button
                  onClick={() => handleEdit(order, 'workorder')}
                  className="flex-1 bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 hover:bg-blue-100 dark:hover:bg-blue-900/30 py-2 px-3 rounded-lg text-sm font-medium transition-colors duration-200 flex items-center justify-center gap-2"
                >
                  <PencilIcon className="w-4 h-4" />
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(order.id, 'workorder')}
                  className="flex-1 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/30 py-2 px-3 rounded-lg text-sm font-medium transition-colors duration-200 flex items-center justify-center gap-2"
                >
                  <TrashIcon className="w-4 h-4" />
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )

  // Check if Facility360 feature is enabled
  if (!isFeatureEnabled('FACILITY360')) {
    return (
      <Layout>
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center">
            <div className="w-24 h-24 mx-auto mb-6 bg-gray-100 dark:bg-gray-800 rounded-full flex items-center justify-center">
              <BuildingOffice2Icon className="w-12 h-12 text-gray-400" />
            </div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">
              Facility360 is a Premium Feature
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mb-8 max-w-md">
              Upgrade your plan to access advanced facility management features including asset tracking, 
              fleet management, work orders, and space optimization.
            </p>
            <button
              onClick={() => setShowUpgradeModal(true)}
              className="bg-blue-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors"
            >
              Upgrade Plan
            </button>
          </div>
        </div>

        <FeatureUpgradeModal
          isOpen={showUpgradeModal}
          onClose={() => setShowUpgradeModal(false)}
          featureName="Facility360"
          featureDescription="Advanced facility management with asset tracking, fleet management, and work orders"
          currentPlan="Freemium"
        />
      </Layout>
    )
  }

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-4">
            <div className="p-3 bg-gradient-to-r from-blue-600 to-blue-700 rounded-xl">
              <BuildingOffice2Icon className="h-8 w-8 text-white" />
            </div>
            <div>
              <h1 className="text-4xl font-bold text-gray-900 dark:text-white">Facility Management</h1>
              <p className="text-lg text-gray-600 dark:text-gray-400">
                Comprehensive facility management and optimization platform
              </p>
            </div>
          </div>
          
          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
            <div className="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm">
              <div className="flex items-center">
                <div className="p-2 bg-blue-100 dark:bg-blue-900 rounded-lg">
                  <BuildingOffice2Icon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Assets</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{displayAssets.length}</p>
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm">
              <div className="flex items-center">
                <div className="p-2 bg-green-100 dark:bg-green-900 rounded-lg">
                  <TruckIcon className="h-6 w-6 text-green-600 dark:text-green-400" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Fleet Items</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{displayFleet.length}</p>
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm">
              <div className="flex items-center">
                <div className="p-2 bg-purple-100 dark:bg-purple-900 rounded-lg">
                  <MapPinIcon className="h-6 w-6 text-purple-600 dark:text-purple-400" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Spaces</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{displaySpaces.length}</p>
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm">
              <div className="flex items-center">
                <div className="p-2 bg-orange-100 dark:bg-orange-900 rounded-lg">
                  <ClipboardDocumentListIcon className="h-6 w-6 text-orange-600 dark:text-orange-400" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Work Orders</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{displayWorkOrders.length}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Service Status Indicator */}
          {(assetsError || fleetError || spacesError || workOrdersError) && (
            <>
              {useMockData && (
                <div className="mt-4 p-4 bg-green-50 border border-green-200 rounded-lg">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <svg className="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                      </svg>
                    </div>
                    <div className="ml-3">
                      <h3 className="text-sm font-medium text-green-800">
                        Development Mode - Using Mock Data
                      </h3>
                      <p className="mt-1 text-sm text-green-700">
                        The facility service is not running, so mock data is being displayed. This allows you to see and test the UI while developing.
                      </p>
                    </div>
                  </div>
                </div>
              )}
              
              <div className="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-yellow-800">
                      Facility Service Connection Issue
                    </h3>
                    <div className="mt-2 text-sm text-yellow-700">
                      <p>
                        Unable to connect to the facility service. Please ensure the backend service is running.
                      </p>
                      <p className="mt-1">
                        If you're developing locally, start the facility-service with: 
                        <code className="ml-1 px-2 py-1 bg-yellow-100 rounded text-xs">
                          cd backend/facility-service && ./gradlew bootRun
                        </code>
                      </p>
                      {useMockData && (
                        <p className="mt-2 text-green-700 font-medium">
                          ✓ Using mock data for development - you can see the UI even without the backend running
                        </p>
                      )}
                      
                      {/* Debug Information */}
                      <details className="mt-3">
                        <summary className="cursor-pointer text-yellow-800 font-medium">
                          Debug Information
                        </summary>
                        <div className="mt-2 text-xs bg-yellow-100 p-2 rounded">
                          <p><strong>Assets:</strong> {assetsError ? 'Error' : 'OK'} - Type: {typeof assetsData}, Length: {Array.isArray(assetsData) ? assetsData.length : 'N/A'}</p>
                          <p><strong>Fleet:</strong> {fleetError ? 'Error' : 'OK'} - Type: {typeof fleetData}, Length: {Array.isArray(fleetData) ? fleetData.length : 'N/A'}</p>
                          <p><strong>Spaces:</strong> {spacesError ? 'Error' : 'OK'} - Type: {typeof spacesData}, Length: {Array.isArray(spacesData) ? spacesData.length : 'N/A'}</p>
                          <p><strong>Work Orders:</strong> {workOrdersError ? 'Error' : 'OK'} - Type: {typeof workOrdersData}, Length: {Array.isArray(workOrdersData) ? workOrdersData.length : 'N/A'}</p>
                        </div>
                      </details>
                    </div>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>

        {/* Tabs */}
        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-1 mb-6 shadow-sm">
          <nav className="flex space-x-1">
            {tabs.map((tab) => {
              const Icon = tab.icon
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex-1 py-3 px-4 rounded-lg font-medium text-sm flex items-center justify-center gap-2 transition-all duration-200 ${
                    activeTab === tab.id
                      ? 'bg-blue-600 text-white shadow-md'
                      : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-gray-700'
                  }`}
                >
                  <Icon className="w-5 h-5" />
                  {tab.name}
                  <span className={`py-1 px-2 rounded-full text-xs font-semibold ${
                    activeTab === tab.id
                      ? 'bg-white/20 text-white'
                      : 'bg-gray-100 dark:bg-gray-600 text-gray-700 dark:text-gray-300'
                  }`}>
                    {tab.count}
                  </span>
                </button>
              )
            })}
          </nav>
        </div>

        {/* Tab Content */}
        <div className="min-h-96">
          {activeTab === 'assets' && renderAssetsTab()}
          {activeTab === 'fleet' && renderFleetTab()}
          {activeTab === 'spaces' && renderSpacesTab()}
          {activeTab === 'workorders' && renderWorkOrdersTab()}
        </div>

        {/* Global Loading State */}
        {(assetsLoading && fleetLoading && spacesLoading && workOrdersLoading) && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-in fade-in duration-200">
            <div className="bg-white dark:bg-gray-800 p-8 rounded-2xl shadow-2xl border border-gray-200 dark:border-gray-600 animate-in zoom-in-95 duration-200">
              <div className="flex flex-col items-center space-y-4">
                <div className="relative">
                  <div className="animate-spin rounded-full h-12 w-12 border-4 border-blue-200 border-t-blue-600"></div>
                  <div className="absolute inset-0 rounded-full border-4 border-transparent border-t-blue-400 animate-pulse"></div>
                </div>
                <div className="text-center">
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-1">Loading Facility Data</h3>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Please wait while we fetch your information...</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Modal Components for Create/Edit */}
        {isModalOpen && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-in fade-in duration-200">
            <div className="bg-white dark:bg-gray-800 p-6 rounded-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto shadow-2xl border border-gray-200 dark:border-gray-600 animate-in zoom-in-95 duration-200">
              <div className="flex justify-between items-center mb-6">
                <h3 className="text-xl font-bold text-gray-900 dark:text-white">
                  {editingItem ? 'Edit' : 'Create'} {modalType === 'asset' ? 'Asset' : 
                   modalType === 'fleet' ? 'Fleet Item' : 
                   modalType === 'space' ? 'Space' : 'Work Order'}
                </h3>
                <button
                  onClick={() => setIsModalOpen(false)}
                  className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  <XMarkIcon className="w-6 h-6" />
                </button>
              </div>
              
              {modalType === 'asset' && (
                <AssetModal 
                  asset={editingItem}
                  onSave={handleSaveAsset}
                  onCancel={() => setIsModalOpen(false)}
                />
              )}
              
              {modalType === 'fleet' && (
                <FleetModal 
                  fleet={editingItem}
                  onSave={handleSaveFleet}
                  onCancel={() => setIsModalOpen(false)}
                />
              )}
              
              {modalType === 'space' && (
                <SpaceModal 
                  space={editingItem}
                  onSave={handleSaveSpace}
                  onCancel={() => setIsModalOpen(false)}
                />
              )}
              
              {modalType === 'workorder' && (
                <WorkOrderModal 
                  workOrder={editingItem}
                  onSave={handleSaveWorkOrder}
                  onCancel={() => setIsModalOpen(false)}
                />
              )}
            </div>
          </div>
        )}

        {/* Feature Upgrade Modal */}
        <FeatureUpgradeModal
          isOpen={showUpgradeModal}
          onClose={() => setShowUpgradeModal(false)}
          featureName="Facility360"
          featureDescription="Advanced facility management with asset tracking, fleet management, and work orders"
          currentPlan="Freemium"
        />
      </div>
    </Layout>
  )
}

export default FacilityPage
