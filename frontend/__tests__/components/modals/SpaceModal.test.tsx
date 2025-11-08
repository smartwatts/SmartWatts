import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SpaceModal from '../../../components/modals/SpaceModal'

describe('SpaceModal', () => {
  const mockOnSave = jest.fn()
  const mockOnCancel = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Rendering', () => {
    it('should render all form fields', () => {
      render(<SpaceModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      expect(screen.getByLabelText(/name/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/type/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/status/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/building/i)).toBeInTheDocument()
    })

    it('should render with existing space data', () => {
      const mockSpace = {
        id: 1,
        name: 'Test Space',
        type: 'OFFICE',
        status: 'AVAILABLE',
        building: 'Building A',
        floor: '1',
        room: '101',
        capacity: 10,
        area: 100,
        isActive: true,
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z'
      }

      render(<SpaceModal space={mockSpace} onSave={mockOnSave} onCancel={mockOnCancel} />)

      expect(screen.getByDisplayValue('Test Space')).toBeInTheDocument()
      expect(screen.getByDisplayValue('Building A')).toBeInTheDocument()
    })
  })

  describe('Form Interactions', () => {
    it('should update form fields when user types', async () => {
      const user = userEvent.setup()
      render(<SpaceModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const nameInput = screen.getByLabelText(/name/i)
      await user.type(nameInput, 'New Space')

      expect(nameInput).toHaveValue('New Space')
    })

    it('should update space type when selected', async () => {
      const user = userEvent.setup()
      render(<SpaceModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const typeSelect = screen.getByLabelText(/type/i)
      await user.selectOptions(typeSelect, 'CONFERENCE_ROOM')

      expect(typeSelect).toHaveValue('CONFERENCE_ROOM')
    })
  })

  describe('Form Submission', () => {
    it('should call onSave with form data when submitted', async () => {
      const user = userEvent.setup()
      render(<SpaceModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      // Fill all required fields
      await user.type(screen.getByLabelText(/name/i), 'Test Space')
      await user.type(screen.getByLabelText(/building/i), 'Building A')
      await user.type(screen.getByLabelText(/floor/i), '1')
      await user.type(screen.getByLabelText(/room/i), '101')
      await user.clear(screen.getByLabelText(/capacity/i))
      await user.type(screen.getByLabelText(/capacity/i), '10')
      await user.clear(screen.getByLabelText(/area/i))
      await user.type(screen.getByLabelText(/area/i), '100')
      
      const submitButton = screen.getByRole('button', { name: /create space/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSave).toHaveBeenCalledWith(
          expect.objectContaining({
            name: 'Test Space'
          })
        )
      })
    })

    it('should parse numeric fields correctly', async () => {
      const user = userEvent.setup()
      render(<SpaceModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      // Fill all required fields
      await user.type(screen.getByLabelText(/name/i), 'Test Space')
      await user.type(screen.getByLabelText(/building/i), 'Building A')
      await user.type(screen.getByLabelText(/floor/i), '1')
      await user.type(screen.getByLabelText(/room/i), '101')
      const capacityInput = screen.getByLabelText(/capacity/i)
      await user.clear(capacityInput)
      await user.type(capacityInput, '20')
      await user.clear(screen.getByLabelText(/area/i))
      await user.type(screen.getByLabelText(/area/i), '100')
      
      const submitButton = screen.getByRole('button', { name: /create space/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSave).toHaveBeenCalledWith(
          expect.objectContaining({
            capacity: 20
          })
        )
      })
    })
  })

  describe('Cancel', () => {
    it('should call onCancel when cancel button is clicked', () => {
      render(<SpaceModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const cancelButton = screen.getByRole('button', { name: /cancel/i })
      fireEvent.click(cancelButton)

      expect(mockOnCancel).toHaveBeenCalled()
    })
  })
})

