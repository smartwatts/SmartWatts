import React, { useState, useEffect } from 'react'

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

interface WorkOrderModalProps {
  workOrder?: WorkOrder | null
  onSave: (data: any) => void
  onCancel: () => void
}

const WORK_ORDER_TYPES = [
  'PREVENTIVE_MAINTENANCE', 'CORRECTIVE_MAINTENANCE', 'EMERGENCY_REPAIR', 'INSPECTION', 'INSTALLATION', 'REPLACEMENT', 'UPGRADE', 'CLEANING', 'CALIBRATION', 'TESTING', 'OTHER'
]

const WORK_ORDER_PRIORITIES = [
  'LOW', 'MEDIUM', 'HIGH', 'CRITICAL', 'EMERGENCY'
]

const WORK_ORDER_STATUSES = [
  'DRAFT', 'SUBMITTED', 'APPROVED', 'ASSIGNED', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED', 'REJECTED'
]

const WorkOrderModal: React.FC<WorkOrderModalProps> = ({ workOrder, onSave, onCancel }) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    type: 'PREVENTIVE_MAINTENANCE',
    priority: 'MEDIUM',
    status: 'DRAFT',
    assignedTo: '',
    dueDate: '',
    estimatedHours: 1,
    actualHours: '',
    isActive: true
  })

  useEffect(() => {
    if (workOrder) {
      setFormData({
        title: workOrder.title || '',
        description: workOrder.description || '',
        type: workOrder.type || 'PREVENTIVE_MAINTENANCE',
        priority: workOrder.priority || 'MEDIUM',
        status: workOrder.status || 'DRAFT',
        assignedTo: workOrder.assignedTo || '',
        dueDate: workOrder.dueDate ? workOrder.dueDate.split('T')[0] : '',
        estimatedHours: workOrder.estimatedHours || 1,
        actualHours: workOrder.actualHours?.toString() || '',
        isActive: workOrder.isActive
      })
    }
  }, [workOrder])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const data = {
      ...formData,
      estimatedHours: parseInt(formData.estimatedHours.toString()),
      actualHours: formData.actualHours ? parseFloat(formData.actualHours) : null
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
        <div className="md:col-span-2">
          <label htmlFor="work-order-title" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Title *
          </label>
          <input
            id="work-order-title"
            type="text"
            name="title"
            value={formData.title}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div className="md:col-span-2">
          <label htmlFor="work-order-description" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Description *
          </label>
          <textarea
            id="work-order-description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            rows={3}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="work-order-type" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Type *
          </label>
          <select
            id="work-order-type"
            name="type"
            value={formData.type}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          >
            {WORK_ORDER_TYPES.map(type => (
              <option key={type} value={type}>
                {type.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="work-order-priority" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Priority *
          </label>
          <select
            id="work-order-priority"
            name="priority"
            value={formData.priority}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          >
            {WORK_ORDER_PRIORITIES.map(priority => (
              <option key={priority} value={priority}>
                {priority}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="work-order-status" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Status *
          </label>
          <select
            id="work-order-status"
            name="status"
            value={formData.status}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          >
            {WORK_ORDER_STATUSES.map(status => (
              <option key={status} value={status}>
                {status.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="work-order-assigned-to" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Assigned To *
          </label>
          <input
            id="work-order-assigned-to"
            type="text"
            name="assignedTo"
            value={formData.assignedTo}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="work-order-due-date" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Due Date *
          </label>
          <input
            id="work-order-due-date"
            type="date"
            name="dueDate"
            value={formData.dueDate}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="work-order-estimated-hours" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Estimated Hours *
          </label>
          <input
            id="work-order-estimated-hours"
            type="number"
            name="estimatedHours"
            value={formData.estimatedHours}
            onChange={handleChange}
            min="0.5"
            step="0.5"
            required
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div>
          <label htmlFor="work-order-actual-hours" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Actual Hours
          </label>
          <input
            id="work-order-actual-hours"
            type="number"
            name="actualHours"
            value={formData.actualHours}
            onChange={handleChange}
            min="0"
            step="0.5"
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
          />
        </div>

        <div className="md:col-span-2">
          <label htmlFor="work-order-active" className="flex items-center">
            <input
              id="work-order-active"
              type="checkbox"
              name="isActive"
              checked={formData.isActive}
              onChange={handleChange}
              className="h-4 w-4 text-orange-600 focus:ring-orange-500 border-gray-300 rounded"
            />
            <span className="ml-2 text-sm text-gray-700 dark:text-gray-300">Active</span>
          </label>
        </div>
      </div>

      <div className="flex justify-end space-x-3 pt-4">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500"
        >
          Cancel
        </button>
        <button
          type="submit"
          className="px-4 py-2 text-sm font-medium text-white bg-orange-600 border border-transparent rounded-lg hover:bg-orange-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500"
        >
          {workOrder ? 'Update' : 'Create'} Work Order
        </button>
      </div>
    </form>
  )
}

export default WorkOrderModal
