import React, { useState, useEffect } from 'react'

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

interface FleetModalProps {
  fleet?: FleetItem | null
  onSave: (data: any) => void
  onCancel: () => void
}

const FLEET_TYPES = [
  'PASSENGER_VEHICLE', 'TRUCK', 'VAN', 'BUS', 'MOTORCYCLE', 'FORKLIFT', 'CRANE', 'EXCAVATOR', 'BULLDOZER', 'TRACTOR', 'TRAILER', 'BOAT', 'AIRCRAFT', 'OTHER'
]

const FLEET_STATUSES = [
  'OPERATIONAL', 'IN_USE', 'AVAILABLE', 'MAINTENANCE', 'REPAIR', 'OUT_OF_SERVICE', 'RETIRED', 'LOST', 'STOLEN', 'DAMAGED', 'UNDER_INSPECTION', 'RESERVED'
]

const FleetModal: React.FC<FleetModalProps> = ({ fleet, onSave, onCancel }) => {
  const [formData, setFormData] = useState({
    name: '',
    type: 'PASSENGER_VEHICLE',
    make: '',
    model: '',
    modelYear: new Date().getFullYear(),
    licensePlate: '',
    status: 'OPERATIONAL',
    currentFuelLevel: 0,
    fuelCapacity: 0,
    nextMaintenanceDate: '',
    insuranceExpiryDate: '',
    registrationExpiryDate: '',
    isActive: true
  })

  useEffect(() => {
    if (fleet) {
      setFormData({
        name: fleet.name || '',
        type: fleet.type || 'PASSENGER_VEHICLE',
        make: fleet.make || '',
        model: fleet.model || '',
        modelYear: fleet.modelYear || new Date().getFullYear(),
        licensePlate: fleet.licensePlate || '',
        status: fleet.status || 'OPERATIONAL',
        currentFuelLevel: fleet.currentFuelLevel || 0,
        fuelCapacity: fleet.fuelCapacity || 0,
        nextMaintenanceDate: fleet.nextMaintenanceDate ? fleet.nextMaintenanceDate.split('T')[0] : '',
        insuranceExpiryDate: fleet.insuranceExpiryDate ? fleet.insuranceExpiryDate.split('T')[0] : '',
        registrationExpiryDate: fleet.registrationExpiryDate ? fleet.registrationExpiryDate.split('T')[0] : '',
        isActive: fleet.isActive
      })
    }
  }, [fleet])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const data = {
      ...formData,
      modelYear: parseInt(formData.modelYear.toString()),
      currentFuelLevel: parseInt(formData.currentFuelLevel.toString()),
      fuelCapacity: parseInt(formData.fuelCapacity.toString())
    }
    onSave(data)
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
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
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Name *
          </label>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Type *
          </label>
          <select
            name="type"
            value={formData.type}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          >
            {FLEET_TYPES.map(type => (
              <option key={type} value={type}>
                {type.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Make *
          </label>
          <input
            type="text"
            name="make"
            value={formData.make}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Model *
          </label>
          <input
            type="text"
            name="model"
            value={formData.model}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Model Year *
          </label>
          <input
            type="number"
            name="modelYear"
            value={formData.modelYear}
            onChange={handleChange}
            min="1900"
            max={new Date().getFullYear() + 1}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            License Plate *
          </label>
          <input
            type="text"
            name="licensePlate"
            value={formData.licensePlate}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Status *
          </label>
          <select
            name="status"
            value={formData.status}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          >
            {FLEET_STATUSES.map(status => (
              <option key={status} value={status}>
                {status.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Current Fuel Level (L) *
          </label>
          <input
            type="number"
            name="currentFuelLevel"
            value={formData.currentFuelLevel}
            onChange={handleChange}
            min="0"
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Fuel Capacity (L) *
          </label>
          <input
            type="number"
            name="fuelCapacity"
            value={formData.fuelCapacity}
            onChange={handleChange}
            min="1"
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Next Maintenance Date
          </label>
          <input
            type="date"
            name="nextMaintenanceDate"
            value={formData.nextMaintenanceDate}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Insurance Expiry Date
          </label>
          <input
            type="date"
            name="insuranceExpiryDate"
            value={formData.insuranceExpiryDate}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Registration Expiry Date
          </label>
          <input
            type="date"
            name="registrationExpiryDate"
            value={formData.registrationExpiryDate}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div className="md:col-span-2">
          <label className="flex items-center">
            <input
              type="checkbox"
              name="isActive"
              checked={formData.isActive}
              onChange={handleChange}
              className="h-4 w-4 text-green-600 focus:ring-green-500 border-gray-300 rounded"
            />
            <span className="ml-2 text-sm text-gray-700 dark:text-gray-300">Active</span>
          </label>
        </div>
      </div>

      <div className="flex justify-end space-x-3 pt-4">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
        >
          Cancel
        </button>
        <button
          type="submit"
          className="px-4 py-2 text-sm font-medium text-white bg-green-600 border border-transparent rounded-lg hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
        >
          {fleet ? 'Update' : 'Create'} Fleet Item
        </button>
      </div>
    </form>
  )
}

export default FleetModal
