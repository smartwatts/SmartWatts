import React from 'react'
import { render, screen } from '@testing-library/react'
import { Progress } from '../../../components/ui/progress'

describe('Progress', () => {
  describe('Rendering', () => {
    it('should render progress bar', () => {
      render(<Progress value={50} />)
      const progress = screen.getByRole('progressbar', { hidden: true })
      expect(progress).toBeInTheDocument()
    })

    it('should render with default value of 0', () => {
      render(<Progress />)
      const progress = screen.getByRole('progressbar', { hidden: true })
      expect(progress).toBeInTheDocument()
    })
  })

  describe('Value Display', () => {
    it('should display progress at 0%', () => {
      render(<Progress value={0} />)
      const progress = screen.getByRole('progressbar', { hidden: true })
      const innerBar = progress.querySelector('div')
      expect(innerBar).toHaveStyle({ transform: 'translateX(-100%)' })
    })

    it('should display progress at 50%', () => {
      render(<Progress value={50} />)
      const progress = screen.getByRole('progressbar', { hidden: true })
      const innerBar = progress.querySelector('div')
      expect(innerBar).toHaveStyle({ transform: 'translateX(-50%)' })
    })

    it('should display progress at 100%', () => {
      render(<Progress value={100} />)
      const progress = screen.getByRole('progressbar', { hidden: true })
      const innerBar = progress.querySelector('div')
      expect(innerBar).toHaveStyle({ transform: 'translateX(-0%)' })
    })
  })

  describe('Custom Max Value', () => {
    it('should calculate progress with custom max', () => {
      render(<Progress value={25} max={50} />)
      const progress = screen.getByRole('progressbar', { hidden: true })
      const innerBar = progress.querySelector('div')
      // 25/50 = 50%, so translateX(-50%)
      expect(innerBar).toHaveStyle({ transform: 'translateX(-50%)' })
    })

    it('should handle value greater than max', () => {
      render(<Progress value={150} max={100} />)
      const progress = screen.getByRole('progressbar', { hidden: true })
      const innerBar = progress.querySelector('div')
      // Should cap at 100%
      expect(innerBar).toHaveStyle({ transform: 'translateX(-0%)' })
    })
  })

  describe('Styling', () => {
    it('should apply default progress classes', () => {
      render(<Progress value={50} />)
      const progress = screen.getByRole('progressbar', { hidden: true })
      expect(progress).toHaveClass('relative', 'h-4', 'w-full', 'overflow-hidden', 'rounded-full', 'bg-secondary')
    })

    it('should support custom className', () => {
      render(<Progress value={50} className="custom-class" />)
      const progress = screen.getByRole('progressbar', { hidden: true })
      expect(progress).toHaveClass('custom-class')
    })
  })

  describe('Edge Cases', () => {
    it('should handle negative values', () => {
      render(<Progress value={-10} />)
      const progress = screen.getByRole('progressbar', { hidden: true })
      const innerBar = progress.querySelector('div')
      // Negative values should result in 0% or be clamped
      expect(innerBar).toBeInTheDocument()
    })

    it('should handle undefined value', () => {
      render(<Progress value={undefined} />)
      const progress = screen.getByRole('progressbar', { hidden: true })
      expect(progress).toBeInTheDocument()
    })
  })
})

