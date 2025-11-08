import React, { useState, useRef, useEffect } from 'react'
import { QrCodeIcon, CameraIcon, XMarkIcon } from '@heroicons/react/24/outline'

interface QRCodeScannerProps {
  onScan: (data: string) => void
  onClose: () => void
  isOpen: boolean
}

const QRCodeScanner: React.FC<QRCodeScannerProps> = ({ onScan, onClose, isOpen }) => {
  const [isScanning, setIsScanning] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const videoRef = useRef<HTMLVideoElement>(null)
  const streamRef = useRef<MediaStream | null>(null)

  useEffect(() => {
    if (isOpen && !isScanning) {
      startScanning()
    }
    return () => {
      stopScanning()
    }
  }, [isOpen])

  const startScanning = async () => {
    try {
      setError(null)
      setIsScanning(true)
      
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' }
      })
      
      if (videoRef.current) {
        videoRef.current.srcObject = stream
        streamRef.current = stream
      }
    } catch (err) {
      setError('Unable to access camera. Please check permissions.')
      setIsScanning(false)
    }
  }

  const stopScanning = () => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => track.stop())
      streamRef.current = null
    }
    setIsScanning(false)
  }

  const handleManualInput = () => {
    const partnerId = prompt('Enter Partner ID:')
    if (partnerId) {
      onScan(`https://onboard.mysmartwatts.com/register?partner=${partnerId}`)
    }
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl max-w-md w-full">
        <div className="p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
              Scan Partner QR Code
            </h3>
            <button
              onClick={onClose}
              className="p-2 rounded-lg text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
              aria-label="Close"
            >
              <XMarkIcon className="h-5 w-5" />
            </button>
          </div>

          <div className="relative">
            <div className="aspect-square bg-gray-100 dark:bg-gray-700 rounded-lg overflow-hidden">
              {isScanning ? (
                <video
                  ref={videoRef}
                  autoPlay
                  playsInline
                  className="w-full h-full object-cover"
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center">
                  <CameraIcon className="h-12 w-12 text-gray-400" />
                </div>
              )}
            </div>

            {/* Scanning overlay */}
            {isScanning && (
              <div className="absolute inset-0 flex items-center justify-center">
                <div className="w-48 h-48 border-2 border-blue-500 rounded-lg relative">
                  <div className="absolute inset-0 border-2 border-blue-500 rounded-lg animate-pulse"></div>
                </div>
              </div>
            )}
          </div>

          {error && (
            <div className="mt-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
              <p className="text-sm text-red-600 dark:text-red-400">{error}</p>
            </div>
          )}

          <div className="mt-4 space-y-3">
            <button
              onClick={handleManualInput}
              className="w-full bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 font-medium py-3 px-4 rounded-lg transition-colors"
            >
              Enter Partner ID Manually
            </button>
            
            <div className="text-center">
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Point your camera at the partner QR code or enter the Partner ID manually
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default QRCodeScanner 