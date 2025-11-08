import React, { useState, useEffect } from 'react'
import { XMarkIcon } from '@heroicons/react/24/outline'

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

interface AssetModalProps {
  asset?: Asset | null
  onSave: (data: any) => void
  onCancel: () => void
}

const ASSET_TYPES = [
  'ELECTRICAL_EQUIPMENT', 'HVAC_SYSTEM', 'PLUMBING_SYSTEM', 'SECURITY_SYSTEM', 'LIFE_SAFETY_SYSTEM', 'FABRIC_SERVICE',
  'CLEANING_EQUIPMENT', 'CATERING_EQUIPMENT', 'RECEPTION_EQUIPMENT', 'WASTE_MANAGEMENT_EQUIPMENT', 'MAIL_LOGISTICS_EQUIPMENT',
  'FURNITURE', 'OFFICE_EQUIPMENT', 'WORKSTATION_EQUIPMENT', 'IT_EQUIPMENT', 'COMMUNICATION_EQUIPMENT', 'PRESENTATION_EQUIPMENT',
  'VEHICLE', 'TRANSPORT_EQUIPMENT', 'MACHINERY', 'TOOLS', 'OTHER'
]

const ASSET_STATUSES = [
  'OPERATIONAL', 'MAINTENANCE', 'REPAIR', 'OUT_OF_SERVICE', 'RETIRED', 'LOST', 'STOLEN', 'DAMAGED', 'UNDER_INSPECTION', 'PENDING_APPROVAL'
]

const AssetModal: React.FC<AssetModalProps> = ({ asset, onSave, onCancel }) => {
  const [formData, setFormData] = useState({
    assetCode: '',
    name: '',
    description: '',
    assetType: 'ELECTRICAL_EQUIPMENT',
    status: 'OPERATIONAL',
    location: '',
    building: '',
    floor: '',
    room: '',
    manufacturer: '',
    model: '',
    serialNumber: '',
    installationDate: '',
    purchaseCost: '',
    currentValue: '',
    isActive: true
  })

  useEffect(() => {
    if (asset) {
      setFormData({
        assetCode: asset.assetCode || '',
        name: asset.name || '',
        description: asset.description || '',
        assetType: asset.assetType || 'ELECTRICAL_EQUIPMENT',
        status: asset.status || 'OPERATIONAL',
        location: asset.location || '',
        building: asset.building || '',
        floor: asset.floor || '',
        room: asset.room || '',
        manufacturer: asset.manufacturer || '',
        model: asset.model || '',
        serialNumber: asset.serialNumber || '',
        installationDate: asset.installationDate ? asset.installationDate.split('T')[0] : '',
        purchaseCost: asset.purchaseCost?.toString() || '',
        currentValue: asset.currentValue?.toString() || '',
        isActive: asset.isActive
      })
    }
  }, [asset])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const data = {
      ...formData,
      purchaseCost: formData.purchaseCost ? parseFloat(formData.purchaseCost) : null,
      currentValue: formData.currentValue ? parseFloat(formData.currentValue) : null
    }
    onSave(data)
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value
    }))
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label htmlFor="asset-code" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Asset Code *
          </label>
          <input
            id="asset-code"
            type="text"
            name="assetCode"
            value={formData.assetCode}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="asset-name" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Name *
          </label>
          <input
            id="asset-name"
            type="text"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div className="md:col-span-2">
          <label htmlFor="asset-description" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Description
          </label>
          <textarea
            id="asset-description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="asset-type" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Asset Type *
          </label>
          <select
            id="asset-type"
            name="assetType"
            value={formData.assetType}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          >
            {ASSET_TYPES.map(type => (
              <option key={type} value={type}>
                {type.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="asset-status" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Status *
          </label>
          <select
            id="asset-status"
            name="status"
            value={formData.status}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          >
            {ASSET_STATUSES.map(status => (
              <option key={status} value={status}>
                {status.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="asset-location" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Location *
          </label>
          <input
            id="asset-location"
            type="text"
            name="location"
            value={formData.location}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="asset-building" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Building
          </label>
          <input
            id="asset-building"
            type="text"
            name="building"
            value={formData.building}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="asset-floor" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Floor
          </label>
          <input
            id="asset-floor"
            type="text"
            name="floor"
            value={formData.floor}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="asset-room" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Room
          </label>
          <input
            id="asset-room"
            type="text"
            name="room"
            value={formData.room}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="asset-manufacturer" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Manufacturer
          </label>
          <input
            id="asset-manufacturer"
            type="text"
            name="manufacturer"
            value={formData.manufacturer}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="asset-model" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Model
          </label>
          <input
            id="asset-model"
            type="text"
            name="model"
            value={formData.model}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="asset-serial-number" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Serial Number
          </label>
          <input
            id="asset-serial-number"
            type="text"
            name="serialNumber"
            value={formData.serialNumber}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="asset-installation-date" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Installation Date
          </label>
          <input
            id="asset-installation-date"
            type="date"
            name="installationDate"
            value={formData.installationDate}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="purchase-cost" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Purchase Cost
          </label>
          <input
            id="purchase-cost"
            type="number"
            name="purchaseCost"
            value={formData.purchaseCost}
            onChange={handleChange}
            step="0.01"
            min="0"
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="asset-current-value" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Current Value
          </label>
          <input
            id="asset-current-value"
            type="number"
            name="currentValue"
            value={formData.currentValue}
            onChange={handleChange}
            step="0.01"
            min="0"
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div className="md:col-span-2">
          <label className="flex items-center">
            <input
              type="checkbox"
              name="isActive"
              checked={formData.isActive}
              onChange={handleChange}
              className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <span className="ml-2 text-sm text-gray-700 dark:text-gray-300">Active</span>
          </label>
        </div>
      </div>

      <div className="flex justify-end space-x-3 pt-4">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        >
          Cancel
        </button>
        <button
          type="submit"
          className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        >
          {asset ? 'Update' : 'Create'} Asset
        </button>
      </div>
    </form>
  )
}

export default AssetModal
