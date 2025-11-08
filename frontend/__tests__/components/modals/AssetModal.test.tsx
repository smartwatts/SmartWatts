import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AssetModal from '../../../components/modals/AssetModal'

describe('AssetModal', () => {
  const mockOnSave = jest.fn()
  const mockOnCancel = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Rendering', () => {
    it('should render all form fields', () => {
      render(<AssetModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      expect(screen.getByLabelText(/asset code/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/description/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/asset type/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/status/i)).toBeInTheDocument()
    })

    it('should render with existing asset data', () => {
      const mockAsset = {
        id: 1,
        assetCode: 'ASSET-001',
        name: 'Test Asset',
        description: 'Test Description',
        assetType: 'ELECTRICAL_EQUIPMENT',
        status: 'OPERATIONAL',
        location: 'Building A',
        isActive: true,
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z'
      }

      render(<AssetModal asset={mockAsset} onSave={mockOnSave} onCancel={mockOnCancel} />)

      expect(screen.getByDisplayValue('ASSET-001')).toBeInTheDocument()
      expect(screen.getByDisplayValue('Test Asset')).toBeInTheDocument()
    })
  })

  describe('Form Interactions', () => {
    it('should update form fields when user types', async () => {
      const user = userEvent.setup()
      render(<AssetModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const nameInput = screen.getByLabelText(/name/i)
      await user.type(nameInput, 'New Asset')

      expect(nameInput).toHaveValue('New Asset')
    })

    it('should update asset type when selected', async () => {
      const user = userEvent.setup()
      render(<AssetModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const assetTypeSelect = screen.getByLabelText(/asset type/i)
      await user.selectOptions(assetTypeSelect, 'HVAC_SYSTEM')

      expect(assetTypeSelect).toHaveValue('HVAC_SYSTEM')
    })

    it('should update status when selected', async () => {
      const user = userEvent.setup()
      render(<AssetModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const statusSelect = screen.getByLabelText(/status/i)
      await user.selectOptions(statusSelect, 'MAINTENANCE')

      expect(statusSelect).toHaveValue('MAINTENANCE')
    })
  })

  describe('Form Submission', () => {
    it('should call onSave with form data when submitted', async () => {
      const user = userEvent.setup()
      render(<AssetModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      // Fill all required fields
      await user.type(screen.getByLabelText(/asset code/i), 'ASSET-001')
      await user.type(screen.getByLabelText(/name/i), 'Test Asset')
      await user.type(screen.getByLabelText(/location/i), 'Building A')
      
      const submitButton = screen.getByRole('button', { name: /create asset/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSave).toHaveBeenCalledWith(
          expect.objectContaining({
            name: 'Test Asset'
          })
        )
      })
    })

    it('should parse numeric fields correctly', async () => {
      const user = userEvent.setup()
      render(<AssetModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      // Fill all required fields
      await user.type(screen.getByLabelText(/asset code/i), 'ASSET-002')
      await user.type(screen.getByLabelText(/name/i), 'Test Asset')
      await user.type(screen.getByLabelText(/location/i), 'Building A')
      const purchaseCostInput = screen.getByLabelText(/purchase cost/i)
      await user.type(purchaseCostInput, '1000')
      
      const submitButton = screen.getByRole('button', { name: /create asset/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(mockOnSave).toHaveBeenCalledWith(
          expect.objectContaining({
            purchaseCost: 1000
          })
        )
      })
    })
  })

  describe('Cancel', () => {
    it('should call onCancel when cancel button is clicked', () => {
      render(<AssetModal onSave={mockOnSave} onCancel={mockOnCancel} />)

      const cancelButton = screen.getByRole('button', { name: /cancel/i })
      fireEvent.click(cancelButton)

      expect(mockOnCancel).toHaveBeenCalled()
    })
  })
})

