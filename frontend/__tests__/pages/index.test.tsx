import React from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import Index from '../../pages/index'

describe('Index Page (Landing Page)', () => {
  it('renders the landing page', () => {
    render(<Index />)

    expect(screen.getByText(/Nigeria's #1/i)).toBeInTheDocument()
    expect(screen.getAllByText(/AI-Powered/i).length).toBeGreaterThan(0)
  })

  it('displays navigation links', () => {
    render(<Index />)

    expect(screen.getAllByText('Features').length).toBeGreaterThan(0)
    expect(screen.getAllByText('Pricing').length).toBeGreaterThan(0)
    expect(screen.getAllByText('About').length).toBeGreaterThan(0)
    expect(screen.getAllByText('Contact').length).toBeGreaterThan(0)
  })

  it('displays sign in and sign up buttons', () => {
    render(<Index />)

    expect(screen.getByText('Sign In')).toBeInTheDocument()
    expect(screen.getAllByText(/Start Your Intelligent Energy Journey/i).length).toBeGreaterThan(0)
  })

  it('displays hero section', () => {
    render(<Index />)

    expect(screen.getByText(/Nigeria's #1/i)).toBeInTheDocument()
    expect(screen.getAllByText(/AI-Powered/i).length).toBeGreaterThan(0)
    // The full text might be split across multiple elements
    const platformText = screen.queryAllByText(/Energy Business Intelligence Platform/i)
    expect(platformText.length).toBeGreaterThan(0)
  })

  it('displays value propositions section', () => {
    render(<Index />)

    expect(screen.getByText('Beyond Monitoring - This is Energy Intelligence')).toBeInTheDocument()
    expect(screen.getByText('Complete Energy Visibility')).toBeInTheDocument()
    expect(screen.getByText('AI-Powered Cost Optimization')).toBeInTheDocument()
    expect(screen.getByText('Business Intelligence Dashboard')).toBeInTheDocument()
  })

  it('displays target market section', () => {
    render(<Index />)

    expect(screen.getByText('Built for Nigeria\'s Complex Energy Environment')).toBeInTheDocument()
    expect(screen.getByText('For Businesses')).toBeInTheDocument()
    expect(screen.getByText('For Homes')).toBeInTheDocument()
  })

  it('displays social proof section', () => {
    render(<Index />)

    expect(screen.getByText('Trusted by Forward-Thinking Organizations')).toBeInTheDocument()
    expect(screen.getByText('30%')).toBeInTheDocument()
    expect(screen.getByText('Average Cost Reduction')).toBeInTheDocument()
  })

  it('displays pricing section', () => {
    render(<Index />)

    expect(screen.getByText('Investment That Pays for Itself')).toBeInTheDocument()
    expect(screen.getByText('Essential Home')).toBeInTheDocument()
    expect(screen.getByText('Smart Home Plus')).toBeInTheDocument()
    expect(screen.getByText('SME Professional')).toBeInTheDocument()
    expect(screen.getByText('Enterprise')).toBeInTheDocument()
  })

  it('displays CTA section', () => {
    render(<Index />)

    expect(screen.getByText('Ready to Transform Your Energy Management?')).toBeInTheDocument()
    expect(screen.getByText(/Join the energy intelligence revolution/i)).toBeInTheDocument()
  })

  it('displays footer', () => {
    render(<Index />)

    expect(screen.getAllByText(/SmartWatts/i).length).toBeGreaterThan(0)
    expect(screen.getByText(/Product/i)).toBeInTheDocument()
    expect(screen.getAllByText(/Support/i).length).toBeGreaterThan(0)
    expect(screen.getByText(/Terms of Service/i)).toBeInTheDocument()
    expect(screen.getByText(/Privacy Policy/i)).toBeInTheDocument()
  })

  it('displays energy dashboard preview', () => {
    render(<Index />)

    expect(screen.getByText('Energy Dashboard')).toBeInTheDocument()
    expect(screen.getByText('Solar Generation')).toBeInTheDocument()
    expect(screen.getByText('Grid Consumption')).toBeInTheDocument()
    expect(screen.getByText('Monthly Savings')).toBeInTheDocument()
  })

  it('displays pricing tiers with correct information', () => {
    render(<Index />)

    // Check Essential Home pricing
    expect(screen.getByText('₦15,000')).toBeInTheDocument()
    expect(screen.getAllByText('/month').length).toBeGreaterThan(0)

    // Check Smart Home Plus pricing
    expect(screen.getByText('₦25,000')).toBeInTheDocument()
    expect(screen.getByText('Most Popular')).toBeInTheDocument()

    // Check SME Professional pricing
    expect(screen.getByText('₦45,000')).toBeInTheDocument()

    // Check Enterprise pricing
    expect(screen.getByText('Custom')).toBeInTheDocument()
  })
})

