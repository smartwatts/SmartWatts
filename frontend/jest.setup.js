import '@testing-library/jest-dom'

// Mock Next.js router
jest.mock('next/router', () => ({
  useRouter() {
    return {
      route: '/',
      pathname: '/',
      query: {},
      asPath: '/',
      push: jest.fn(),
      pop: jest.fn(),
      reload: jest.fn(),
      back: jest.fn(),
      prefetch: jest.fn().mockResolvedValue(undefined),
      beforePopState: jest.fn(),
      events: {
        on: jest.fn(),
        off: jest.fn(),
        emit: jest.fn(),
      },
      isFallback: false,
    }
  },
}))

// Mock Next.js Image component
jest.mock('next/image', () => ({
  __esModule: true,
  default: (props) => {
    // eslint-disable-next-line @next/next/no-img-element
    return <img {...props} />
  },
}))

// Mock Next.js Link component
jest.mock('next/link', () => ({
  __esModule: true,
  default: ({ children, href, ...props }) => {
    return (
      <a href={href} {...props}>
        {children}
      </a>
    )
  },
}))

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
}
global.localStorage = localStorageMock

// Mock sessionStorage
const sessionStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
}
global.sessionStorage = sessionStorageMock

// Mock fetch
global.fetch = jest.fn()

// Mock IntersectionObserver
global.IntersectionObserver = class IntersectionObserver {
  constructor() {}
  disconnect() {}
  observe() {}
  unobserve() {}
}

// Mock ResizeObserver
global.ResizeObserver = class ResizeObserver {
  constructor() {}
  disconnect() {}
  observe() {}
  unobserve() {}
}

// Mock matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(), // deprecated
    removeListener: jest.fn(), // deprecated
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
})

// Mock service worker
Object.defineProperty(navigator, 'serviceWorker', {
  writable: true,
  value: {
    register: jest.fn().mockResolvedValue({
      installing: null,
      waiting: null,
      active: null,
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
    }),
    getRegistrations: jest.fn().mockResolvedValue([]),
    ready: Promise.resolve({
      installing: null,
      waiting: null,
      active: null,
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
    }),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
  },
})

// Mock PWA utilities
jest.mock('./utils/pwa-utils', () => ({
  __esModule: true,
  default: {
    isInstalled: false,
    isOnline: true,
    canInstall: false,
    installPrompt: null,
    registerServiceWorker: jest.fn().mockResolvedValue(undefined),
    unregisterServiceWorker: jest.fn().mockResolvedValue(undefined),
    updateServiceWorker: jest.fn().mockResolvedValue(undefined),
    showInstallPrompt: jest.fn().mockResolvedValue(false),
    checkForUpdates: jest.fn().mockResolvedValue(false),
    requestBackgroundSync: jest.fn().mockResolvedValue(undefined),
    sendMessageToServiceWorker: jest.fn().mockResolvedValue(undefined),
    getCacheInfo: jest.fn().mockResolvedValue([]),
    clearAllCaches: jest.fn().mockResolvedValue(undefined),
  },
  usePWA: () => ({
    isInstalled: false,
    isOnline: true,
    canInstall: false,
    installPrompt: null,
    registerServiceWorker: jest.fn().mockResolvedValue(undefined),
    unregisterServiceWorker: jest.fn().mockResolvedValue(undefined),
    updateServiceWorker: jest.fn().mockResolvedValue(undefined),
    showInstallPrompt: jest.fn().mockResolvedValue(false),
    checkForUpdates: jest.fn().mockResolvedValue(false),
    requestBackgroundSync: jest.fn().mockResolvedValue(undefined),
    sendMessageToServiceWorker: jest.fn().mockResolvedValue(undefined),
    getCacheInfo: jest.fn().mockResolvedValue([]),
    clearAllCaches: jest.fn().mockResolvedValue(undefined),
  }),
}))

// Mock react-hot-toast
jest.mock('react-hot-toast', () => ({
  __esModule: true,
  default: {
    success: jest.fn(),
    error: jest.fn(),
    loading: jest.fn(),
    dismiss: jest.fn(),
  },
  Toaster: () => <div data-testid="toaster" />,
}))

// Mock @tanstack/react-query
jest.mock('@tanstack/react-query', () => ({
  __esModule: true,
  QueryClient: jest.fn().mockImplementation(() => ({
    getQueryData: jest.fn(),
    setQueryData: jest.fn(),
    invalidateQueries: jest.fn(),
    refetchQueries: jest.fn(),
  })),
  QueryClientProvider: ({ children }) => children,
  useQuery: jest.fn(() => ({
    data: undefined,
    isLoading: false,
    isError: false,
    error: null,
    refetch: jest.fn(),
  })),
  useMutation: jest.fn(() => ({
    mutate: jest.fn(),
    mutateAsync: jest.fn(),
    isLoading: false,
    isError: false,
    error: null,
  })),
  useQueryClient: jest.fn(() => ({
    invalidateQueries: jest.fn(),
    refetchQueries: jest.fn(),
  })),
}))

// Clean up after each test
afterEach(() => {
  jest.clearAllMocks()
  localStorageMock.clear()
  sessionStorageMock.clear()
})


