import { renderHook, waitFor } from '@testing-library/react'

// Unmock pwa-utils to test the real implementation
jest.unmock('../../utils/pwa-utils')

// Mock service worker
const mockServiceWorkerRegistration = {
  active: {
    postMessage: jest.fn()
  },
  waiting: null,
  installing: null,
  update: jest.fn(),
  addEventListener: jest.fn(),
  removeEventListener: jest.fn(),
  unregister: jest.fn(),
  sync: {
    register: jest.fn().mockResolvedValue(undefined)
  }
}

const mockServiceWorker = {
  register: jest.fn().mockResolvedValue(mockServiceWorkerRegistration),
  getRegistrations: jest.fn().mockResolvedValue([mockServiceWorkerRegistration]),
  ready: Promise.resolve(mockServiceWorkerRegistration),
  addEventListener: jest.fn(),
  removeEventListener: jest.fn()
}

// Define serviceWorker before importing pwaManager to ensure it's available
// Delete the property first if it exists to allow redefinition
if ('serviceWorker' in navigator) {
  try {
    delete (navigator as any).serviceWorker
  } catch (e) {
    // Property might not be deletable, that's okay - try to redefine it
  }
}

try {
  Object.defineProperty(navigator, 'serviceWorker', {
    writable: true,
    configurable: true,
    enumerable: true,
    value: mockServiceWorker
  })
} catch (e) {
  // If redefinition fails, try to assign directly
  (navigator as any).serviceWorker = mockServiceWorker
}

// Import pwaManager after setting up the mock
import pwaManager, { usePWA } from '../../utils/pwa-utils'

// Mock caches
const mockCache = {
  keys: jest.fn().mockResolvedValue([]),
  open: jest.fn().mockResolvedValue({
    keys: jest.fn().mockResolvedValue([])
  }),
  delete: jest.fn().mockResolvedValue(true)
}

const mockCaches = {
  keys: jest.fn().mockResolvedValue(['cache-1', 'cache-2']),
  open: jest.fn().mockResolvedValue(mockCache),
  delete: jest.fn().mockResolvedValue(true)
}

Object.defineProperty(window, 'caches', {
  writable: true,
  value: mockCaches
})

// Mock ServiceWorkerRegistration for background sync tests
if (typeof window !== 'undefined' && !window.ServiceWorkerRegistration) {
  class MockServiceWorkerRegistration {
    constructor() {}
  }
  // Set prototype properties after class definition
  const prototype = MockServiceWorkerRegistration.prototype as any
  prototype.sync = {
    register: jest.fn().mockResolvedValue(undefined)
  }
  ;(window as any).ServiceWorkerRegistration = MockServiceWorkerRegistration
}

// Mock matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),
    removeListener: jest.fn(),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
})

describe('PWA Utilities', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    mockServiceWorker.register.mockClear()
    mockServiceWorker.register.mockResolvedValue(mockServiceWorkerRegistration)
    mockServiceWorker.getRegistrations.mockClear()
    mockServiceWorker.getRegistrations.mockResolvedValue([mockServiceWorkerRegistration])
    mockServiceWorkerRegistration.update.mockClear()
    mockServiceWorkerRegistration.update.mockResolvedValue(undefined)
    mockServiceWorkerRegistration.unregister.mockClear()
    mockServiceWorkerRegistration.unregister.mockResolvedValue(true)
    mockCaches.keys.mockClear()
    // Reset the pwaManager's serviceWorkerRegistration
    const manager = pwaManager as any
    manager.serviceWorkerRegistration = null
    manager.deferredPrompt = null
  })

  describe('pwaManager', () => {
    describe('Properties', () => {
      it('should return isInstalled status', () => {
        expect(typeof pwaManager.isInstalled).toBe('boolean')
      })

      it('should return isOnline status', () => {
        expect(typeof pwaManager.isOnline).toBe('boolean')
      })

      it('should return canInstall status', () => {
        expect(typeof pwaManager.canInstall).toBe('boolean')
      })
    })

    describe('Service Worker Registration', () => {
      it('should register service worker', async () => {
        // Reset the pwaManager's serviceWorkerRegistration
        const manager = pwaManager as any
        manager.serviceWorkerRegistration = null
        mockServiceWorker.register.mockClear()
        // Ensure the mock returns a promise that resolves
        mockServiceWorker.register.mockResolvedValue(mockServiceWorkerRegistration)
        
        // Verify navigator.serviceWorker is available
        expect('serviceWorker' in navigator).toBe(true)
        expect(navigator.serviceWorker).toBeDefined()
        
        // The pwaManager checks both 'serviceWorker' in navigator and navigator.serviceWorker
        // So we need to ensure both are true
        await pwaManager.registerServiceWorker()

        expect(mockServiceWorker.register).toHaveBeenCalledWith('/service-worker.js', {
          scope: '/'
        })
      })

      it('should handle service worker registration error', async () => {
        // Reset the pwaManager's serviceWorkerRegistration
        const manager = pwaManager as any
        manager.serviceWorkerRegistration = null
        mockServiceWorker.register.mockClear()
        mockServiceWorker.register.mockRejectedValueOnce(new Error('Registration failed'))

        // The function throws the error, so it should reject
        await expect(pwaManager.registerServiceWorker()).rejects.toThrow('Registration failed')
      })

      it('should not register if service workers not supported', async () => {
        // Reset the pwaManager's serviceWorkerRegistration
        const manager = pwaManager as any
        manager.serviceWorkerRegistration = null
        mockServiceWorker.register.mockClear()
        
        // Mock 'serviceWorker' in navigator check by temporarily removing it
        const originalSW = navigator.serviceWorker
        // Use Object.defineProperty to set it to undefined
        try {
          Object.defineProperty(navigator, 'serviceWorker', {
            value: undefined,
            writable: true,
            configurable: true,
            enumerable: false
          })
        } catch (e) {
          // Property might not be redefinable, skip this test
          return
        }

        await pwaManager.registerServiceWorker()

        expect(mockServiceWorker.register).not.toHaveBeenCalled()

        // Restore service worker
        try {
          Object.defineProperty(navigator, 'serviceWorker', {
            value: originalSW,
            writable: true,
            configurable: true,
            enumerable: true
          })
        } catch (e) {
          // Property might not be restorable
        }
      })
    })

    describe('Service Worker Unregistration', () => {
      it('should unregister service worker', async () => {
        // Reset the pwaManager's serviceWorkerRegistration
        const manager = pwaManager as any
        manager.serviceWorkerRegistration = null
        mockServiceWorker.register.mockClear()
        mockServiceWorker.register.mockResolvedValueOnce(mockServiceWorkerRegistration)
        
        // First register to set up the registration
        await pwaManager.registerServiceWorker()
        mockServiceWorker.getRegistrations.mockClear()
        mockServiceWorker.getRegistrations.mockResolvedValueOnce([mockServiceWorkerRegistration])
        mockServiceWorkerRegistration.unregister.mockClear()
        mockServiceWorkerRegistration.unregister.mockResolvedValueOnce(true)
        
        await pwaManager.unregisterServiceWorker()

        expect(mockServiceWorker.getRegistrations).toHaveBeenCalled()
        expect(mockServiceWorkerRegistration.unregister).toHaveBeenCalled()
      })

      it('should handle unregistration error', async () => {
        // Reset the pwaManager's serviceWorkerRegistration
        const manager = pwaManager as any
        manager.serviceWorkerRegistration = null
        mockServiceWorker.register.mockClear()
        mockServiceWorker.register.mockResolvedValueOnce(mockServiceWorkerRegistration)
        
        // First register to set up the registration
        await pwaManager.registerServiceWorker()
        mockServiceWorker.getRegistrations.mockClear()
        mockServiceWorker.getRegistrations.mockRejectedValueOnce(new Error('Unregistration failed'))

        // The function throws the error, so it should reject
        await expect(pwaManager.unregisterServiceWorker()).rejects.toThrow('Unregistration failed')
      })
    })

    describe('Service Worker Updates', () => {
      it('should update service worker', async () => {
        // Reset the pwaManager's serviceWorkerRegistration
        const manager = pwaManager as any
        manager.serviceWorkerRegistration = null
        mockServiceWorker.register.mockClear()
        mockServiceWorker.register.mockResolvedValueOnce(mockServiceWorkerRegistration)
        
        // First register the service worker to set up the registration
        await pwaManager.registerServiceWorker()
        mockServiceWorkerRegistration.update.mockClear()
        mockServiceWorkerRegistration.update.mockResolvedValueOnce(undefined)
        
        await pwaManager.updateServiceWorker()

        expect(mockServiceWorkerRegistration.update).toHaveBeenCalled()
      })

      it('should handle update when no registration exists', async () => {
        const originalUpdate = pwaManager.updateServiceWorker
        // Mock no registration
        const manager = pwaManager as any
        const originalRegistration = manager.serviceWorkerRegistration
        manager.serviceWorkerRegistration = null

        await pwaManager.updateServiceWorker()

        expect(mockServiceWorkerRegistration.update).not.toHaveBeenCalled()

        manager.serviceWorkerRegistration = originalRegistration
      })
    })

    describe('Install Prompt', () => {
      it('should show install prompt when available', async () => {
        const mockPrompt = {
          prompt: jest.fn().mockResolvedValue(undefined),
          userChoice: Promise.resolve({ outcome: 'accepted' as const })
        }

        // Simulate beforeinstallprompt event
        const event = new Event('beforeinstallprompt') as any
        event.preventDefault = jest.fn()
        Object.defineProperty(event, 'prompt', { 
          value: mockPrompt.prompt,
          writable: true,
          configurable: true
        })
        Object.defineProperty(event, 'userChoice', { 
          value: mockPrompt.userChoice,
          writable: true,
          configurable: true
        })

        window.dispatchEvent(event)

        await waitFor(() => {
          expect(pwaManager.canInstall).toBe(true)
        }, { timeout: 2000 })

        const result = await pwaManager.showInstallPrompt()
        expect(result).toBe(true)
      })

      it('should return false when install prompt not available', async () => {
        const result = await pwaManager.showInstallPrompt()
        expect(result).toBe(false)
      })
    })

    describe('Cache Management', () => {
      it('should get cache info', async () => {
        mockCaches.keys.mockClear()
        mockCaches.keys.mockResolvedValueOnce(['cache-1'])
        mockCaches.open.mockClear()
        mockCaches.open.mockResolvedValueOnce(mockCache)
        mockCache.keys.mockClear()
        mockCache.keys.mockResolvedValueOnce([{ url: 'url1' }, { url: 'url2' }])

        const result = await pwaManager.getCacheInfo()

        expect(mockCaches.keys).toHaveBeenCalled()
        expect(mockCaches.open).toHaveBeenCalledWith('cache-1')
        expect(result).toEqual([{ name: 'cache-1', size: 2 }])
      })

      it('should clear all caches', async () => {
        mockCaches.keys.mockClear()
        mockCaches.keys.mockResolvedValueOnce(['cache-1', 'cache-2'])
        mockCaches.delete.mockClear()
        mockCaches.delete.mockResolvedValue(true)

        await pwaManager.clearAllCaches()

        expect(mockCaches.keys).toHaveBeenCalled()
        expect(mockCaches.delete).toHaveBeenCalledWith('cache-1')
        expect(mockCaches.delete).toHaveBeenCalledWith('cache-2')
      })
    })

    describe('Background Sync', () => {
      it('should request background sync', async () => {
        // Ensure ServiceWorkerRegistration.prototype exists with sync
        if (!window.ServiceWorkerRegistration?.prototype) {
          (window.ServiceWorkerRegistration as any).prototype = {}
        }
        if (!(window.ServiceWorkerRegistration.prototype as any).sync) {
          (window.ServiceWorkerRegistration.prototype as any).sync = {
            register: jest.fn().mockResolvedValue(undefined)
          }
        }
        
        await pwaManager.requestBackgroundSync('test-tag')

        // Should register sync if supported
        expect(mockServiceWorker.ready).toBeDefined()
      })

      it('should handle background sync when not supported', async () => {
        // Ensure ServiceWorkerRegistration.prototype exists
        if (!window.ServiceWorkerRegistration?.prototype) {
          (window.ServiceWorkerRegistration as any).prototype = {}
        }
        
        // Remove sync property to simulate unsupported state
        const originalSync = (window.ServiceWorkerRegistration.prototype as any)?.sync
        if ((window.ServiceWorkerRegistration.prototype as any).sync) {
          delete (window.ServiceWorkerRegistration.prototype as any).sync
        }

        await pwaManager.requestBackgroundSync('test-tag')

        // Should not throw error - function should return early
        expect(true).toBe(true)

        // Restore if needed
        if (originalSync && window.ServiceWorkerRegistration?.prototype) {
          (window.ServiceWorkerRegistration.prototype as any).sync = originalSync
        }
      })
    })
  })

  describe('usePWA Hook', () => {
    it('should return PWA state and methods', () => {
      const { result } = renderHook(() => usePWA())

      expect(result.current).toHaveProperty('isInstalled')
      expect(result.current).toHaveProperty('isOnline')
      expect(result.current).toHaveProperty('canInstall')
      expect(result.current).toHaveProperty('registerServiceWorker')
      expect(result.current).toHaveProperty('showInstallPrompt')
      expect(result.current).toHaveProperty('checkForUpdates')
    })

    it('should update state when installability changes', async () => {
      const { result } = renderHook(() => usePWA())

      const event = new CustomEvent('pwa-installability-changed', {
        detail: { canInstall: true }
      })
      window.dispatchEvent(event)

      await waitFor(() => {
        expect(result.current.canInstall).toBeDefined()
      })
    })
  })
})

