/// <reference types="next" />

interface Window {
  gtag?: (
    command: string,
    targetId: string | Date | { [key: string]: any },
    config?: { [key: string]: any }
  ) => void
}

