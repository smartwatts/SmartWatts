import React from 'react'
import { render, screen } from '@testing-library/react'
import { Badge } from '../../../components/ui/badge'

describe('Badge', () => {
  describe('Rendering', () => {
    it('should render with children', () => {
      render(<Badge>Badge text</Badge>)
      expect(screen.getByText('Badge text')).toBeInTheDocument()
    })

    it('should render as div element', () => {
      render(<Badge>Test</Badge>)
      const badge = screen.getByText('Test')
      expect(badge.tagName).toBe('DIV')
    })
  })

  describe('Variants', () => {
    it('should apply default variant classes', () => {
      render(<Badge variant="default">Default</Badge>)
      const badge = screen.getByText('Default')
      expect(badge).toHaveClass('bg-primary', 'text-primary-foreground')
    })

    it('should apply secondary variant classes', () => {
      render(<Badge variant="secondary">Secondary</Badge>)
      const badge = screen.getByText('Secondary')
      expect(badge).toHaveClass('bg-secondary', 'text-secondary-foreground')
    })

    it('should apply destructive variant classes', () => {
      render(<Badge variant="destructive">Destructive</Badge>)
      const badge = screen.getByText('Destructive')
      expect(badge).toHaveClass('bg-destructive', 'text-destructive-foreground')
    })

    it('should apply outline variant classes', () => {
      render(<Badge variant="outline">Outline</Badge>)
      const badge = screen.getByText('Outline')
      expect(badge).toHaveClass('border', 'border-input')
    })
  })

  describe('Sizes', () => {
    it('should apply default size classes', () => {
      render(<Badge size="default">Default</Badge>)
      const badge = screen.getByText('Default')
      expect(badge).toHaveClass('px-2.5', 'py-0.5', 'text-xs')
    })

    it('should apply small size classes', () => {
      render(<Badge size="sm">Small</Badge>)
      const badge = screen.getByText('Small')
      expect(badge).toHaveClass('px-2', 'py-0.5', 'text-xs')
    })

    it('should apply large size classes', () => {
      render(<Badge size="lg">Large</Badge>)
      const badge = screen.getByText('Large')
      expect(badge).toHaveClass('px-3', 'py-1', 'text-sm')
    })
  })

  describe('Common Classes', () => {
    it('should apply common badge classes', () => {
      render(<Badge>Test</Badge>)
      const badge = screen.getByText('Test')
      expect(badge).toHaveClass('inline-flex', 'items-center', 'rounded-full', 'border', 'font-semibold')
    })
  })

  describe('Custom className', () => {
    it('should support custom className', () => {
      render(<Badge className="custom-class">Test</Badge>)
      const badge = screen.getByText('Test')
      expect(badge).toHaveClass('custom-class')
    })
  })
})

