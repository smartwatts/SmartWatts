import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import FeatureUpgradeModal from '../../components/FeatureUpgradeModal'

describe('FeatureUpgradeModal', () => {
  const mockOnClose = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Rendering', () => {
    it('should not render when isOpen is false', () => {
      render(
        <FeatureUpgradeModal
          isOpen={false}
          onClose={mockOnClose}
          featureName="Test Feature"
          featureDescription="Test Description"
          currentPlan="Free"
        />
      )

      expect(screen.queryByText(/upgrade to access/i)).not.toBeInTheDocument()
    })

    it('should render when isOpen is true', () => {
      render(
        <FeatureUpgradeModal
          isOpen={true}
          onClose={mockOnClose}
          featureName="Test Feature"
          featureDescription="Test Description"
          currentPlan="Free"
        />
      )

      expect(screen.getByText(/upgrade to access test feature/i)).toBeInTheDocument()
      expect(screen.getByText('Test Description')).toBeInTheDocument()
    })

    it('should display all plan options', () => {
      render(
        <FeatureUpgradeModal
          isOpen={true}
          onClose={mockOnClose}
          featureName="Test Feature"
          featureDescription="Test Description"
          currentPlan="Free"
        />
      )

      // Use getAllByText since "Premium Plan" appears in both heading and button
      const premiumPlans = screen.getAllByText(/premium plan/i)
      expect(premiumPlans.length).toBeGreaterThan(0)
      // Use getAllByText since "Business Plan" appears in both heading and button
      const businessPlans = screen.getAllByText(/business plan/i)
      expect(businessPlans.length).toBeGreaterThan(0)
    })

    it('should display recommended badge on Premium plan', () => {
      render(
        <FeatureUpgradeModal
          isOpen={true}
          onClose={mockOnClose}
          featureName="Test Feature"
          featureDescription="Test Description"
          currentPlan="Free"
        />
      )

      expect(screen.getByText(/recommended/i)).toBeInTheDocument()
    })

    it('should display plan features', () => {
      render(
        <FeatureUpgradeModal
          isOpen={true}
          onClose={mockOnClose}
          featureName="Test Feature"
          featureDescription="Test Description"
          currentPlan="Free"
        />
      )

      expect(screen.getByText(/all freemium features/i)).toBeInTheDocument()
      expect(screen.getByText(/enhanced analytics/i)).toBeInTheDocument()
    })
  })

  describe('Close', () => {
    it('should call onClose when close button is clicked', () => {
      render(
        <FeatureUpgradeModal
          isOpen={true}
          onClose={mockOnClose}
          featureName="Test Feature"
          featureDescription="Test Description"
          currentPlan="Free"
        />
      )

      const closeButton = screen.getByRole('button', { name: /close/i })
      fireEvent.click(closeButton)

      expect(mockOnClose).toHaveBeenCalled()
    })
  })

  describe('Plan Selection', () => {
    it('should display plan prices', () => {
      render(
        <FeatureUpgradeModal
          isOpen={true}
          onClose={mockOnClose}
          featureName="Test Feature"
          featureDescription="Test Description"
          currentPlan="Free"
        />
      )

      expect(screen.getByText(/₦5,000/i)).toBeInTheDocument()
      expect(screen.getByText(/₦15,000/i)).toBeInTheDocument()
    })

    it('should display upgrade buttons for each plan', () => {
      render(
        <FeatureUpgradeModal
          isOpen={true}
          onClose={mockOnClose}
          featureName="Test Feature"
          featureDescription="Test Description"
          currentPlan="Free"
        />
      )

      // Buttons say "Choose {plan.name}" not "Upgrade"
      expect(screen.getByRole('button', { name: /choose premium plan/i })).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /choose business plan/i })).toBeInTheDocument()
    })
  })
})

