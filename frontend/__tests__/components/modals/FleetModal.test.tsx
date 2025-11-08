import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import FleetModal from '../../../components/modals/FleetModal'

describe('FleetModal', () => {
  const mockOnSave = jest.fn()
  const mockOnCancel = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Rendering', () => {
    it('should render all form fields', () => {
      render(<FleetModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      expect(screen.getByLabelText(/name/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/type/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/make/i)).toBeInTheDocument()
      // Use getAllByLabelText for "model" since there are multiple (Model and Model Year)
      const modelLabels = screen.getAllByLabelText(/model/i)
      expect(modelLabels.length).toBeGreaterThan(0)
      expect(screen.getByLabelText(/license plate/i)).toBeInTheDocument()
    })

    it('should render with existing fleet data', () => {
      const mockFleet = {
        id: 1,
        name: 'Test Vehicle',
        type: 'PASSENGER_VEHICLE',
        make: 'Toyota',
        model: 'Camry',
        modelYear: 2020,
        licensePlate: 'ABC-123',
        status: 'OPERATIONAL',
        isActive: true,
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z'
      }

      render(<FleetModal fleet={mockFleet} onSave={mockOnSave} onCancel={mockOnCancel} />)

      expect(screen.getByDisplayValue('Test Vehicle')).toBeInTheDocument()
      expect(screen.getByDisplayValue('Toyota')).toBeInTheDocument()
    })
  })

  describe('Form Interactions', () => {
    it('should update form fields when user types', async () => {
      const user = userEvent.setup()
      render(<FleetModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const nameInput = screen.getByLabelText(/name/i)
      await user.type(nameInput, 'New Vehicle')

      expect(nameInput).toHaveValue('New Vehicle')
    })

    it('should update fleet type when selected', async () => {
      const user = userEvent.setup()
      render(<FleetModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const typeSelect = screen.getByLabelText(/type/i)
      await user.selectOptions(typeSelect, 'TRUCK')

      expect(typeSelect).toHaveValue('TRUCK')
    })
  })

  describe('Form Submission', () => {
    it('should call onSave with form data when submitted', async () => {
      const user = userEvent.setup()
      render(<FleetModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      // Fill all required fields
      await user.type(screen.getByLabelText(/name/i), 'Test Vehicle')
      await user.type(screen.getByLabelText(/make/i), 'Toyota')
      // Use id directly to avoid ambiguity with "Model" and "Model Year"
      const modelInput = document.getElementById('fleet-model')
      if (modelInput) {
        await user.type(modelInput as HTMLElement, 'Camry')
      }
      await user.type(screen.getByLabelText(/license plate/i), 'ABC-123')
      await user.clear(screen.getByLabelText(/current fuel level/i))
      await user.type(screen.getByLabelText(/current fuel level/i), '50')
      await user.clear(screen.getByLabelText(/fuel capacity/i))
      await user.type(screen.getByLabelText(/fuel capacity/i), '80')
      
      const submitButton = screen.getByRole('button', { name: /create fleet item/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSave).toHaveBeenCalledWith(
          expect.objectContaining({
            name: 'Test Vehicle'
          })
        )
      })
    })

    it('should parse numeric fields correctly', async () => {
      const user = userEvent.setup()
      render(<FleetModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      // Fill all required fields
      await user.type(screen.getByLabelText(/name/i), 'Test Vehicle')
      await user.type(screen.getByLabelText(/make/i), 'Toyota')
      // Use id directly to avoid ambiguity with "Model" and "Model Year"
      const modelInput = document.getElementById('fleet-model')
      if (modelInput) {
        await user.type(modelInput as HTMLElement, 'Camry')
      }
      await user.type(screen.getByLabelText(/license plate/i), 'ABC-123')
      await user.clear(screen.getByLabelText(/current fuel level/i))
      await user.type(screen.getByLabelText(/current fuel level/i), '50')
      const fuelCapacityInput = screen.getByLabelText(/fuel capacity/i)
      await user.clear(fuelCapacityInput)
      await user.type(fuelCapacityInput, '100')
      
      const submitButton = screen.getByRole('button', { name: /create fleet item/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSave).toHaveBeenCalledWith(
          expect.objectContaining({
            fuelCapacity: 100
          })
        )
      })
    })
  })

  describe('Cancel', () => {
    it('should call onCancel when cancel button is clicked', () => {
      render(<FleetModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const cancelButton = screen.getByRole('button', { name: /cancel/i })
      fireEvent.click(cancelButton)

      expect(mockOnCancel).toHaveBeenCalled()
    })
  })
})

