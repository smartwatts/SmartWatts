import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import WorkOrderModal from '../../../components/modals/WorkOrderModal'

describe('WorkOrderModal', () => {
  const mockOnSave = jest.fn()
  const mockOnCancel = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Rendering', () => {
    it('should render all form fields', () => {
      render(<WorkOrderModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      expect(screen.getByLabelText(/title/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/description/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/type/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/priority/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/status/i)).toBeInTheDocument()
    })

    it('should render with existing work order data', () => {
      const mockWorkOrder = {
        id: 1,
        title: 'Test Work Order',
        description: 'Test Description',
        type: 'PREVENTIVE_MAINTENANCE',
        priority: 'HIGH',
        status: 'DRAFT',
        assignedTo: 'user-123',
        dueDate: '2024-12-31T00:00:00Z',
        estimatedHours: 8,
        isActive: true,
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z'
      }

      render(<WorkOrderModal workOrder={mockWorkOrder} onSave={mockOnSave} onCancel={mockOnCancel} />)

      expect(screen.getByDisplayValue('Test Work Order')).toBeInTheDocument()
      expect(screen.getByDisplayValue('Test Description')).toBeInTheDocument()
    })
  })

  describe('Form Interactions', () => {
    it('should update form fields when user types', async () => {
      const user = userEvent.setup()
      render(<WorkOrderModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const titleInput = screen.getByLabelText(/title/i)
      await user.type(titleInput, 'New Work Order')

      expect(titleInput).toHaveValue('New Work Order')
    })

    it('should update work order type when selected', async () => {
      const user = userEvent.setup()
      render(<WorkOrderModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const typeSelect = screen.getByLabelText(/type/i)
      await user.selectOptions(typeSelect, 'EMERGENCY_REPAIR')

      expect(typeSelect).toHaveValue('EMERGENCY_REPAIR')
    })

    it('should update priority when selected', async () => {
      const user = userEvent.setup()
      render(<WorkOrderModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const prioritySelect = screen.getByLabelText(/priority/i)
      await user.selectOptions(prioritySelect, 'CRITICAL')

      expect(prioritySelect).toHaveValue('CRITICAL')
    })
  })

  describe('Form Submission', () => {
    it('should call onSave with form data when submitted', async () => {
      const user = userEvent.setup()
      render(<WorkOrderModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      // Fill all required fields
      await user.type(screen.getByLabelText(/title/i), 'Test Work Order')
      await user.type(screen.getByLabelText(/description/i), 'Test Description')
      await user.type(screen.getByLabelText(/assigned to/i), 'John Doe')
      await user.type(screen.getByLabelText(/due date/i), '2024-12-31')
      
      const submitButton = screen.getByRole('button', { name: /create work order/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSave).toHaveBeenCalledWith(
          expect.objectContaining({
            title: 'Test Work Order'
          })
        )
      })
    })

    it('should parse numeric fields correctly', async () => {
      const user = userEvent.setup()
      render(<WorkOrderModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      // Fill all required fields
      await user.type(screen.getByLabelText(/title/i), 'Test Work Order')
      await user.type(screen.getByLabelText(/description/i), 'Test Description')
      await user.type(screen.getByLabelText(/assigned to/i), 'John Doe')
      await user.type(screen.getByLabelText(/due date/i), '2024-12-31')
      const estimatedHoursInput = screen.getByLabelText(/estimated hours/i)
      await user.clear(estimatedHoursInput)
      await user.type(estimatedHoursInput, '5')
      const actualHoursInput = screen.getByLabelText(/actual hours/i)
      await user.clear(actualHoursInput)
      await user.type(actualHoursInput, '4.5')
      
      const submitButton = screen.getByRole('button', { name: /create work order/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSave).toHaveBeenCalledWith(
          expect.objectContaining({
            estimatedHours: 5,
            actualHours: 4.5
          })
        )
      })
    })
  })

  describe('Cancel', () => {
    it('should call onCancel when cancel button is clicked', () => {
      render(<WorkOrderModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const cancelButton = screen.getByRole('button', { name: /cancel/i })
      fireEvent.click(cancelButton)

      expect(mockOnCancel).toHaveBeenCalled()
    })
  })
})

