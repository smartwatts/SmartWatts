import React from 'react'
import { render, screen } from '@testing-library/react'
import DeviceStatusCard from '../../components/DeviceStatusCard'

describe('DeviceStatusCard', () => {
  describe('Rendering', () => {
    it('should render with title and count', () => {
      render(<DeviceStatusCard title="Active Devices" count={10} icon="📱" color="green" />)

      expect(screen.getByText('Active Devices')).toBeInTheDocument()
      expect(screen.getByText('10')).toBeInTheDocument()
    })

    it('should render with icon', () => {
      render(<DeviceStatusCard title="Active Devices" count={10} icon="📱" color="green" />)

      expect(screen.getByText('📱')).toBeInTheDocument()
    })
  })

  describe('Color Variants', () => {
    it('should apply blue color classes', () => {
      const { container } = render(<DeviceStatusCard title="Test" count={5} icon="🔵" color="blue" />)

      // The color classes are applied to the outer div
      const card = container.querySelector('.bg-blue-50')
      expect(card).toHaveClass('bg-blue-50', 'border-blue-200', 'text-blue-800')
    })

    it('should apply green color classes', () => {
      const { container } = render(<DeviceStatusCard title="Test" count={5} icon="🟢" color="green" />)

      // The color classes are applied to the outer div
      const card = container.querySelector('.bg-green-50')
      expect(card).toHaveClass('bg-green-50', 'border-green-200', 'text-green-800')
    })

    it('should apply yellow color classes', () => {
      const { container } = render(<DeviceStatusCard title="Test" count={5} icon="🟡" color="yellow" />)

      const card = container.querySelector('.bg-yellow-50')
      expect(card).toHaveClass('bg-yellow-50', 'border-yellow-200', 'text-yellow-800')
    })

    it('should apply red color classes', () => {
      const { container } = render(<DeviceStatusCard title="Test" count={5} icon="🔴" color="red" />)

      const card = container.querySelector('.bg-red-50')
      expect(card).toHaveClass('bg-red-50', 'border-red-200', 'text-red-800')
    })

    it('should apply purple color classes', () => {
      const { container } = render(<DeviceStatusCard title="Test" count={5} icon="🟣" color="purple" />)

      const card = container.querySelector('.bg-purple-50')
      expect(card).toHaveClass('bg-purple-50', 'border-purple-200', 'text-purple-800')
    })
  })

  describe('Icon Colors', () => {
    it('should apply blue icon color classes', () => {
      render(<DeviceStatusCard title="Test" count={5} icon="🔵" color="blue" />)

      const iconContainer = screen.getByText('🔵').closest('div')
      expect(iconContainer).toHaveClass('bg-blue-100', 'text-blue-600')
    })

    it('should apply green icon color classes', () => {
      render(<DeviceStatusCard title="Test" count={5} icon="🟢" color="green" />)

      const iconContainer = screen.getByText('🟢').closest('div')
      expect(iconContainer).toHaveClass('bg-green-100', 'text-green-600')
    })
  })

  describe('Count Display', () => {
    it('should display zero count', () => {
      render(<DeviceStatusCard title="Test" count={0} icon="📱" color="blue" />)

      expect(screen.getByText('0')).toBeInTheDocument()
    })

    it('should display large counts', () => {
      render(<DeviceStatusCard title="Test" count={9999} icon="📱" color="blue" />)

      expect(screen.getByText('9999')).toBeInTheDocument()
    })
  })
})

