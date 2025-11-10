// SmartWatts PWA Utilities
// Service worker registration and PWA functionality

export interface PWAInstallPrompt {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

export interface PWAUtils {
  isInstalled: boolean;
  isOnline: boolean;
  canInstall: boolean;
  installPrompt: PWAInstallPrompt | null;
  registerServiceWorker: () => Promise<void>;
  unregisterServiceWorker: () => Promise<void>;
  updateServiceWorker: () => Promise<void>;
  showInstallPrompt: () => Promise<boolean>;
  checkForUpdates: () => Promise<boolean>;
}

class PWAManager implements PWAUtils {
  private deferredPrompt: PWAInstallPrompt | null = null;
  private serviceWorkerRegistration: ServiceWorkerRegistration | null = null;
  private onlineListeners: Array<(isOnline: boolean) => void> = [];

  constructor() {
    if (typeof window !== 'undefined') {
      this.setupEventListeners();
      this.checkInstallability();
    }
  }

  get isInstalled(): boolean {
    if (typeof window === 'undefined') return false;
    return window.matchMedia('(display-mode: standalone)').matches ||
           (window.navigator as any).standalone === true;
  }

  get isOnline(): boolean {
    if (typeof navigator === 'undefined') return true;
    return navigator.onLine;
  }

  get canInstall(): boolean {
    return this.deferredPrompt !== null;
  }

  get installPrompt(): PWAInstallPrompt | null {
    return this.deferredPrompt;
  }

  private setupEventListeners(): void {
    if (typeof window === 'undefined') return;
    
    // Listen for install prompt
    window.addEventListener('beforeinstallprompt', (e) => {
      e.preventDefault();
      // TypeScript doesn't recognize beforeinstallprompt event type, so we use unknown first
      this.deferredPrompt = e as unknown as PWAInstallPrompt;
      this.notifyInstallabilityChange();
    });

    // Listen for app installed
    window.addEventListener('appinstalled', () => {
      console.log('[PWA] App was installed');
      this.deferredPrompt = null;
      this.notifyInstallabilityChange();
    });

    // Listen for online/offline status
    window.addEventListener('online', () => {
      this.notifyOnlineStatusChange(true);
    });

    window.addEventListener('offline', () => {
      this.notifyOnlineStatusChange(false);
    });

    // Listen for service worker updates
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.addEventListener('controllerchange', () => {
        console.log('[PWA] Service worker controller changed');
        this.checkForUpdates();
      });
    }
  }

  private checkInstallability(): void {
    if (typeof window === 'undefined') return;
    
    // Check if app is already installed
    if (this.isInstalled) {
      this.deferredPrompt = null;
    }
  }

  private notifyInstallabilityChange(): void {
    // Dispatch custom event for installability changes
    window.dispatchEvent(new CustomEvent('pwa-installability-changed', {
      detail: { canInstall: this.canInstall }
    }));
  }

  private notifyOnlineStatusChange(isOnline: boolean): void {
    this.onlineListeners.forEach(listener => listener(isOnline));
    
    // Dispatch custom event for online status changes
    window.dispatchEvent(new CustomEvent('pwa-online-status-changed', {
      detail: { isOnline }
    }));
  }

  async registerServiceWorker(): Promise<void> {
    // Check if serviceWorker exists in navigator (using 'in' operator)
    if (!('serviceWorker' in navigator) || !navigator.serviceWorker) {
      console.warn('[PWA] Service workers not supported');
      return;
    }

    try {
      const registration = await navigator.serviceWorker.register('/service-worker.js', {
        scope: '/'
      });

      this.serviceWorkerRegistration = registration;
      console.log('[PWA] Service worker registered successfully');

      // Check for updates
      registration.addEventListener('updatefound', () => {
        console.log('[PWA] Service worker update found');
        this.checkForUpdates();
      });

    } catch (error) {
      console.error('[PWA] Service worker registration failed:', error);
      throw error;
    }
  }

  async unregisterServiceWorker(): Promise<void> {
    // Check if serviceWorker exists in navigator (using 'in' operator)
    if (!('serviceWorker' in navigator) || !navigator.serviceWorker) {
      return;
    }

    try {
      const registrations = await navigator.serviceWorker.getRegistrations();
      await Promise.all(registrations.map(registration => registration.unregister()));
      console.log('[PWA] Service worker unregistered');
    } catch (error) {
      console.error('[PWA] Service worker unregistration failed:', error);
      throw error;
    }
  }

  async updateServiceWorker(): Promise<void> {
    if (!this.serviceWorkerRegistration) {
      console.warn('[PWA] No service worker registration found');
      return;
    }

    try {
      await this.serviceWorkerRegistration.update();
      console.log('[PWA] Service worker update initiated');
    } catch (error) {
      console.error('[PWA] Service worker update failed:', error);
      throw error;
    }
  }

  async showInstallPrompt(): Promise<boolean> {
    if (!this.deferredPrompt) {
      console.warn('[PWA] Install prompt not available');
      return false;
    }

    try {
      await this.deferredPrompt.prompt();
      const choiceResult = await this.deferredPrompt.userChoice;
      
      if (choiceResult.outcome === 'accepted') {
        console.log('[PWA] User accepted the install prompt');
        return true;
      } else {
        console.log('[PWA] User dismissed the install prompt');
        return false;
      }
    } catch (error) {
      console.error('[PWA] Install prompt failed:', error);
      return false;
    }
  }

  async checkForUpdates(): Promise<boolean> {
    if (!this.serviceWorkerRegistration) {
      return false;
    }

    try {
      await this.serviceWorkerRegistration.update();
      
      // Check if there's a waiting service worker
      if (this.serviceWorkerRegistration.waiting) {
        console.log('[PWA] Update available');
        return true;
      }
      
      return false;
    } catch (error) {
      console.error('[PWA] Update check failed:', error);
      return false;
    }
  }

  // Add listener for online status changes
  addOnlineStatusListener(listener: (isOnline: boolean) => void): () => void {
    this.onlineListeners.push(listener);
    
    // Return unsubscribe function
    return () => {
      const index = this.onlineListeners.indexOf(listener);
      if (index > -1) {
        this.onlineListeners.splice(index, 1);
      }
    };
  }

  // Request background sync
  async requestBackgroundSync(tag: string): Promise<void> {
    if (!('serviceWorker' in navigator) || !('sync' in window.ServiceWorkerRegistration.prototype)) {
      console.warn('[PWA] Background sync not supported');
      return;
    }

    try {
      const registration = await navigator.serviceWorker.ready;
      // TypeScript doesn't recognize sync property, so we use type assertion
      const syncManager = (registration as any).sync;
      if (syncManager) {
        await syncManager.register(tag);
        console.log(`[PWA] Background sync registered: ${tag}`);
      } else {
        console.warn('[PWA] Background sync not available');
      }
    } catch (error) {
      console.error('[PWA] Background sync registration failed:', error);
      throw error;
    }
  }

  // Send message to service worker
  async sendMessageToServiceWorker(message: any): Promise<void> {
    if (!('serviceWorker' in navigator)) {
      return;
    }

    try {
      const registration = await navigator.serviceWorker.ready;
      if (registration.active) {
        registration.active.postMessage(message);
      }
    } catch (error) {
      console.error('[PWA] Failed to send message to service worker:', error);
    }
  }

  // Get cache storage info
  async getCacheInfo(): Promise<{ name: string; size: number }[]> {
    if (!('caches' in window)) {
      return [];
    }

    try {
      const cacheNames = await caches.keys();
      const cacheInfo = await Promise.all(
        cacheNames.map(async (name) => {
          const cache = await caches.open(name);
          const keys = await cache.keys();
          return { name, size: keys.length };
        })
      );
      return cacheInfo;
    } catch (error) {
      console.error('[PWA] Failed to get cache info:', error);
      return [];
    }
  }

  // Clear all caches
  async clearAllCaches(): Promise<void> {
    if (!('caches' in window)) {
      return;
    }

    try {
      const cacheNames = await caches.keys();
      await Promise.all(
        cacheNames.map(name => caches.delete(name))
      );
      console.log('[PWA] All caches cleared');
    } catch (error) {
      console.error('[PWA] Failed to clear caches:', error);
      throw error;
    }
  }
}

// Create singleton instance
export const pwaManager = new PWAManager();

// Export default instance
export default pwaManager;

// React hook for PWA functionality
export function usePWA() {
  const [isInstalled, setIsInstalled] = React.useState(pwaManager.isInstalled);
  const [isOnline, setIsOnline] = React.useState(pwaManager.isOnline);
  const [canInstall, setCanInstall] = React.useState(pwaManager.canInstall);

  React.useEffect(() => {
    // Listen for installability changes
    const handleInstallabilityChange = () => {
      setCanInstall(pwaManager.canInstall);
    };

    // Listen for online status changes
    const unsubscribe = pwaManager.addOnlineStatusListener(setIsOnline);

    // Listen for custom events
    window.addEventListener('pwa-installability-changed', handleInstallabilityChange);
    window.addEventListener('pwa-online-status-changed', handleInstallabilityChange);

    return () => {
      unsubscribe();
      window.removeEventListener('pwa-installability-changed', handleInstallabilityChange);
      window.removeEventListener('pwa-online-status-changed', handleInstallabilityChange);
    };
  }, []);

  return {
    isInstalled,
    isOnline,
    canInstall,
    installPrompt: pwaManager.installPrompt,
    registerServiceWorker: pwaManager.registerServiceWorker.bind(pwaManager),
    unregisterServiceWorker: pwaManager.unregisterServiceWorker.bind(pwaManager),
    updateServiceWorker: pwaManager.updateServiceWorker.bind(pwaManager),
    showInstallPrompt: pwaManager.showInstallPrompt.bind(pwaManager),
    checkForUpdates: pwaManager.checkForUpdates.bind(pwaManager),
    requestBackgroundSync: pwaManager.requestBackgroundSync.bind(pwaManager),
    sendMessageToServiceWorker: pwaManager.sendMessageToServiceWorker.bind(pwaManager),
    getCacheInfo: pwaManager.getCacheInfo.bind(pwaManager),
    clearAllCaches: pwaManager.clearAllCaches.bind(pwaManager)
  };
}

// Import React for the hook
import React from 'react';
