import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import EmptyState, { EmptyStates } from '../../components/EmptyState'

describe('EmptyState', () => {
  describe('Rendering', () => {
    it('should render with title and description', () => {
      render(
        <EmptyState
          title="No Data"
          description="There is no data available"
        />
      )
      expect(screen.getByText('No Data')).toBeInTheDocument()
      expect(screen.getByText('There is no data available')).toBeInTheDocument()
    })

    it('should render with custom icon', () => {
      const CustomIcon = () => <div data-testid="custom-icon">Icon</div>
      render(
        <EmptyState
          title="Test"
          description="Test description"
          icon={CustomIcon}
        />
      )
      expect(screen.getByTestId('custom-icon')).toBeInTheDocument()
    })
  })

  describe('Actions', () => {
    it('should render primary action button', async () => {
      const mockAction = jest.fn()
      const user = userEvent.setup()
      render(
        <EmptyState
          title="Test"
          description="Test description"
          action={{
            label: 'Add Item',
            onClick: mockAction
          }}
        />
      )

      const actionButton = screen.getByRole('button', { name: /add item/i })
      await user.click(actionButton)

      expect(mockAction).toHaveBeenCalledTimes(1)
    })

    it('should render secondary action button', async () => {
      const mockSecondaryAction = jest.fn()
      const user = userEvent.setup()
      render(
        <EmptyState
          title="Test"
          description="Test description"
          secondaryAction={{
            label: 'Cancel',
            onClick: mockSecondaryAction
          }}
        />
      )

      const secondaryButton = screen.getByRole('button', { name: /cancel/i })
      await user.click(secondaryButton)

      expect(mockSecondaryAction).toHaveBeenCalledTimes(1)
    })

    it('should render refresh button when showRefresh is true', async () => {
      const mockRefresh = jest.fn()
      const user = userEvent.setup()
      render(
        <EmptyState
          title="Test"
          description="Test description"
          showRefresh={true}
          onRefresh={mockRefresh}
        />
      )

      const refreshButton = screen.getByRole('button', { name: /refresh/i })
      await user.click(refreshButton)

      expect(mockRefresh).toHaveBeenCalledTimes(1)
    })
  })

  describe('Predefined Empty States', () => {
    it('should render EnergyData empty state', () => {
      render(<EmptyStates.EnergyData />)
      expect(screen.getByText(/no energy data/i)).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /add device/i })).toBeInTheDocument()
    })

    it('should render SolarData empty state', () => {
      render(<EmptyStates.SolarData />)
      expect(screen.getByText(/no solar data/i)).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /add solar inverter/i })).toBeInTheDocument()
    })

    it('should render Devices empty state', () => {
      render(<EmptyStates.Devices />)
      expect(screen.getByText(/no devices connected/i)).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /add device/i })).toBeInTheDocument()
    })

    it('should render Analytics empty state', () => {
      render(<EmptyStates.Analytics />)
      expect(screen.getByText(/no analytics data/i)).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /refresh/i })).toBeInTheDocument()
    })
  })
})

