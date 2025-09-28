import React, { useState, useEffect } from 'react'

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

interface SpaceModalProps {
  space?: Space | null
  onSave: (data: any) => void
  onCancel: () => void
}

const SPACE_TYPES = [
  'OFFICE', 'CONFERENCE_ROOM', 'MEETING_ROOM', 'BREAK_ROOM', 'KITCHEN', 'RESTROOM', 'STORAGE_ROOM', 'SERVER_ROOM', 'LABORATORY', 'WORKSHOP',
  'PARKING_SPACE', 'OUTDOOR_AREA', 'RECEPTION_AREA', 'LOBBY', 'CORRIDOR', 'STAIRWELL', 'ELEVATOR', 'UTILITY_ROOM', 'MAINTENANCE_ROOM', 'SECURITY_ROOM', 'OTHER'
]

const SPACE_STATUSES = [
  'AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE', 'OUT_OF_SERVICE', 'RESTRICTED', 'UNDER_RENOVATION', 'CLEANING', 'OTHER'
]

const SpaceModal: React.FC<SpaceModalProps> = ({ space, onSave, onCancel }) => {
  const [formData, setFormData] = useState({
    name: '',
    type: 'OFFICE',
    status: 'AVAILABLE',
    building: '',
    floor: '',
    room: '',
    capacity: 1,
    area: 0,
    isActive: true
  })

  useEffect(() => {
    if (space) {
      setFormData({
        name: space.name || '',
        type: space.type || 'OFFICE',
        status: space.status || 'AVAILABLE',
        building: space.building || '',
        floor: space.floor || '',
        room: space.room || '',
        capacity: space.capacity || 1,
        area: space.area || 0,
        isActive: space.isActive
      })
    }
  }, [space])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const data = {
      ...formData,
      capacity: parseInt(formData.capacity.toString()),
      area: parseFloat(formData.area.toString())
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
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
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
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          >
            {SPACE_TYPES.map(type => (
              <option key={type} value={type}>
                {type.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
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
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          >
            {SPACE_STATUSES.map(status => (
              <option key={status} value={status}>
                {status.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Building *
          </label>
          <input
            type="text"
            name="building"
            value={formData.building}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Floor *
          </label>
          <input
            type="text"
            name="floor"
            value={formData.floor}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Room *
          </label>
          <input
            type="text"
            name="room"
            value={formData.room}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Capacity *
          </label>
          <input
            type="number"
            name="capacity"
            value={formData.capacity}
            onChange={handleChange}
            min="1"
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Area (sq ft) *
          </label>
          <input
            type="number"
            name="area"
            value={formData.area}
            onChange={handleChange}
            min="0"
            step="0.01"
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div className="md:col-span-2">
          <label className="flex items-center">
            <input
              type="checkbox"
              name="isActive"
              checked={formData.isActive}
              onChange={handleChange}
              className="h-4 w-4 text-purple-600 focus:ring-purple-500 border-gray-300 rounded"
            />
            <span className="ml-2 text-sm text-gray-700 dark:text-gray-300">Active</span>
          </label>
        </div>
      </div>

      <div className="flex justify-end space-x-3 pt-4">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
        >
          Cancel
        </button>
        <button
          type="submit"
          className="px-4 py-2 text-sm font-medium text-white bg-purple-600 border border-transparent rounded-lg hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
        >
          {space ? 'Update' : 'Create'} Space
        </button>
      </div>
    </form>
  )
}

export default SpaceModal
