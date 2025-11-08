import React from 'react'
import { render, screen } from '@testing-library/react'
import { Card, CardHeader, CardContent, CardTitle } from '../../../components/ui/card'

describe('Card Components', () => {
  describe('Card', () => {
    it('should render with children', () => {
      render(<Card>Card content</Card>)
      expect(screen.getByText('Card content')).toBeInTheDocument()
    })

    it('should apply default card classes', () => {
      render(<Card>Test</Card>)
      const card = screen.getByText('Test').closest('div')
      expect(card).toHaveClass('rounded-lg', 'border', 'bg-card', 'shadow-sm')
    })

    it('should support custom className', () => {
      render(<Card className="custom-class">Test</Card>)
      const card = screen.getByText('Test').closest('div')
      expect(card).toHaveClass('custom-class')
    })
  })

  describe('CardHeader', () => {
    it('should render with children', () => {
      render(
        <Card>
          <CardHeader>Header content</CardHeader>
        </Card>
      )
      expect(screen.getByText('Header content')).toBeInTheDocument()
    })

    it('should apply default header classes', () => {
      render(
        <Card>
          <CardHeader>Test</CardHeader>
        </Card>
      )
      const header = screen.getByText('Test').closest('div')
      expect(header).toHaveClass('flex', 'flex-col', 'space-y-1.5', 'p-6')
    })
  })

  describe('CardContent', () => {
    it('should render with children', () => {
      render(
        <Card>
          <CardContent>Content</CardContent>
        </Card>
      )
      expect(screen.getByText('Content')).toBeInTheDocument()
    })

    it('should apply default content classes', () => {
      render(
        <Card>
          <CardContent>Test</CardContent>
        </Card>
      )
      const content = screen.getByText('Test').closest('div')
      expect(content).toHaveClass('p-6', 'pt-0')
    })
  })

  describe('CardTitle', () => {
    it('should render with children', () => {
      render(
        <Card>
          <CardHeader>
            <CardTitle>Title</CardTitle>
          </CardHeader>
        </Card>
      )
      expect(screen.getByText('Title')).toBeInTheDocument()
    })

    it('should render as h3 element', () => {
      render(
        <Card>
          <CardHeader>
            <CardTitle>Test</CardTitle>
          </CardHeader>
        </Card>
      )
      const title = screen.getByText('Test')
      expect(title.tagName).toBe('H3')
    })

    it('should apply default title classes', () => {
      render(
        <Card>
          <CardHeader>
            <CardTitle>Test</CardTitle>
          </CardHeader>
        </Card>
      )
      const title = screen.getByText('Test')
      expect(title).toHaveClass('text-2xl', 'font-semibold')
    })
  })

  describe('Card Composition', () => {
    it('should compose Card with all sub-components', () => {
      render(
        <Card>
          <CardHeader>
            <CardTitle>Card Title</CardTitle>
          </CardHeader>
          <CardContent>Card content goes here</CardContent>
        </Card>
      )

      expect(screen.getByText('Card Title')).toBeInTheDocument()
      expect(screen.getByText('Card content goes here')).toBeInTheDocument()
    })
  })
})

