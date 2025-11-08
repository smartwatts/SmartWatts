import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '../../../components/ui/tabs'

describe('Tabs Components', () => {
  describe('Tabs', () => {
    it('should render with children', () => {
      render(
        <Tabs>
          <TabsList>
            <TabsTrigger value="tab1">Tab 1</TabsTrigger>
          </TabsList>
        </Tabs>
      )
      expect(screen.getByText('Tab 1')).toBeInTheDocument()
    })

    it('should use defaultValue when provided', () => {
      render(
        <Tabs defaultValue="tab1">
          <TabsList>
            <TabsTrigger value="tab1">Tab 1</TabsTrigger>
          </TabsList>
          <TabsContent value="tab1">Content 1</TabsContent>
        </Tabs>
      )
      expect(screen.getByText('Content 1')).toBeInTheDocument()
    })

    it('should use controlled value when provided', () => {
      render(
        <Tabs value="tab2">
          <TabsList>
            <TabsTrigger value="tab1">Tab 1</TabsTrigger>
            <TabsTrigger value="tab2">Tab 2</TabsTrigger>
          </TabsList>
          <TabsContent value="tab1">Content 1</TabsContent>
          <TabsContent value="tab2">Content 2</TabsContent>
        </Tabs>
      )
      expect(screen.getByText('Content 2')).toBeInTheDocument()
      expect(screen.queryByText('Content 1')).not.toBeInTheDocument()
    })

    it('should call onValueChange when value changes', async () => {
      const handleValueChange = jest.fn()
      const user = userEvent.setup()
      render(
        <Tabs onValueChange={handleValueChange}>
          <TabsList>
            <TabsTrigger value="tab1">Tab 1</TabsTrigger>
            <TabsTrigger value="tab2">Tab 2</TabsTrigger>
          </TabsList>
        </Tabs>
      )

      const tab2 = screen.getByText('Tab 2')
      await user.click(tab2)

      expect(handleValueChange).toHaveBeenCalledWith('tab2')
    })
  })

  describe('TabsList', () => {
    it('should render with children', () => {
      render(
        <Tabs>
          <TabsList>
            <TabsTrigger value="tab1">Tab 1</TabsTrigger>
          </TabsList>
        </Tabs>
      )
      expect(screen.getByText('Tab 1')).toBeInTheDocument()
    })

    it('should apply default list classes', () => {
      render(
        <Tabs>
          <TabsList>Test</TabsList>
        </Tabs>
      )
      const list = screen.getByText('Test').closest('div')
      expect(list).toHaveClass('inline-flex', 'h-10', 'items-center', 'justify-center')
    })
  })

  describe('TabsTrigger', () => {
    it('should render with children', () => {
      render(
        <Tabs>
          <TabsList>
            <TabsTrigger value="tab1">Tab 1</TabsTrigger>
          </TabsList>
        </Tabs>
      )
      expect(screen.getByText('Tab 1')).toBeInTheDocument()
    })

    it('should be active when value matches', () => {
      render(
        <Tabs defaultValue="tab1">
          <TabsList>
            <TabsTrigger value="tab1">Tab 1</TabsTrigger>
          </TabsList>
        </Tabs>
      )
      const trigger = screen.getByText('Tab 1')
      expect(trigger).toHaveClass('bg-background', 'text-foreground', 'shadow-sm')
    })

    it('should not be active when value does not match', () => {
      render(
        <Tabs defaultValue="tab1">
          <TabsList>
            <TabsTrigger value="tab1">Tab 1</TabsTrigger>
            <TabsTrigger value="tab2">Tab 2</TabsTrigger>
          </TabsList>
        </Tabs>
      )
      const trigger = screen.getByText('Tab 2')
      expect(trigger).not.toHaveClass('bg-background', 'shadow-sm')
    })

    it('should throw error when used outside Tabs', () => {
      // Suppress console.error for this test
      const originalError = console.error
      console.error = jest.fn()

      expect(() => {
        render(<TabsTrigger value="tab1">Tab 1</TabsTrigger>)
      }).toThrow('TabsTrigger must be used within a Tabs component')

      console.error = originalError
    })
  })

  describe('TabsContent', () => {
    it('should render content when value matches', () => {
      render(
        <Tabs defaultValue="tab1">
          <TabsList>
            <TabsTrigger value="tab1">Tab 1</TabsTrigger>
          </TabsList>
          <TabsContent value="tab1">Content 1</TabsContent>
        </Tabs>
      )
      expect(screen.getByText('Content 1')).toBeInTheDocument()
    })

    it('should not render content when value does not match', () => {
      render(
        <Tabs defaultValue="tab1">
          <TabsList>
            <TabsTrigger value="tab1">Tab 1</TabsTrigger>
            <TabsTrigger value="tab2">Tab 2</TabsTrigger>
          </TabsList>
          <TabsContent value="tab1">Content 1</TabsContent>
          <TabsContent value="tab2">Content 2</TabsContent>
        </Tabs>
      )
      expect(screen.getByText('Content 1')).toBeInTheDocument()
      expect(screen.queryByText('Content 2')).not.toBeInTheDocument()
    })

    it('should throw error when used outside Tabs', () => {
      // Suppress console.error for this test
      const originalError = console.error
      console.error = jest.fn()

      expect(() => {
        render(<TabsContent value="tab1">Content 1</TabsContent>)
      }).toThrow('TabsContent must be used within a Tabs component')

      console.error = originalError
    })
  })

  describe('Tab Switching', () => {
    it('should switch tabs when trigger is clicked', async () => {
      const user = userEvent.setup()
      render(
        <Tabs defaultValue="tab1">
          <TabsList>
            <TabsTrigger value="tab1">Tab 1</TabsTrigger>
            <TabsTrigger value="tab2">Tab 2</TabsTrigger>
          </TabsList>
          <TabsContent value="tab1">Content 1</TabsContent>
          <TabsContent value="tab2">Content 2</TabsContent>
        </Tabs>
      )

      expect(screen.getByText('Content 1')).toBeInTheDocument()
      expect(screen.queryByText('Content 2')).not.toBeInTheDocument()

      const tab2 = screen.getByText('Tab 2')
      await user.click(tab2)

      expect(screen.queryByText('Content 1')).not.toBeInTheDocument()
      expect(screen.getByText('Content 2')).toBeInTheDocument()
    })
  })
})

