import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import QRCodeScanner from '../../components/QRCodeScanner'

// Mock mediaDevices
const mockGetUserMedia = jest.fn()
Object.defineProperty(navigator, 'mediaDevices', {
  writable: true,
  value: {
    getUserMedia: mockGetUserMedia
  }
})

// Mock window.prompt
const mockPrompt = jest.fn()
window.prompt = mockPrompt

describe('QRCodeScanner', () => {
  const mockOnScan = jest.fn()
  const mockOnClose = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
    mockGetUserMedia.mockClear()
    mockPrompt.mockClear()
  })

  describe('Rendering', () => {
    it('should not render when isOpen is false', () => {
      render(<QRCodeScanner isOpen={false} onScan={mockOnScan} onClose={mockOnClose} />)
      expect(screen.queryByText(/scan partner qr code/i)).not.toBeInTheDocument()
    })

    it('should render when isOpen is true', () => {
      render(<QRCodeScanner isOpen={true} onScan={mockOnScan} onClose={mockOnClose} />)
      expect(screen.getByText(/scan partner qr code/i)).toBeInTheDocument()
    })

    it('should render close button', () => {
      render(<QRCodeScanner isOpen={true} onScan={mockOnScan} onClose={mockOnClose} />)
      expect(screen.getByRole('button', { name: /close/i })).toBeInTheDocument()
    })
  })

  describe('Camera Access', () => {
    it('should request camera access when opened', async () => {
      const mockStream = {
        getTracks: () => []
      }
      mockGetUserMedia.mockResolvedValue(mockStream)

      render(<QRCodeScanner isOpen={true} onScan={mockOnScan} onClose={mockOnClose} />)

      await waitFor(() => {
        expect(mockGetUserMedia).toHaveBeenCalledWith({
          video: { facingMode: 'environment' }
        })
      })
    })

    it('should handle camera access error', async () => {
      mockGetUserMedia.mockRejectedValue(new Error('Permission denied'))

      render(<QRCodeScanner isOpen={true} onScan={mockOnScan} onClose={mockOnClose} />)

      await waitFor(() => {
        expect(screen.getByText(/unable to access camera/i)).toBeInTheDocument()
      })
    })
  })

  describe('Manual Input', () => {
    it('should allow manual input', async () => {
      mockPrompt.mockReturnValue('partner-123')
      const user = userEvent.setup()

      render(<QRCodeScanner isOpen={true} onScan={mockOnScan} onClose={mockOnClose} />)

      const manualInputButton = screen.getByText(/enter partner id manually/i)
      await user.click(manualInputButton)

      expect(mockPrompt).toHaveBeenCalledWith('Enter Partner ID:')
      expect(mockOnScan).toHaveBeenCalledWith('https://onboard.mysmartwatts.com/register?partner=partner-123')
    })
  })

  describe('Close', () => {
    it('should call onClose when close button is clicked', async () => {
      const user = userEvent.setup()
      render(<QRCodeScanner isOpen={true} onScan={mockOnScan} onClose={mockOnClose} />)

      const closeButton = screen.getByRole('button', { name: /close/i })
      await user.click(closeButton)

      expect(mockOnClose).toHaveBeenCalled()
    })
  })
})

