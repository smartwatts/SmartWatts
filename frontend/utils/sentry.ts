// Optional Sentry integration - only initializes if packages are installed
// This file will not cause build errors if @sentry/react is not installed
// To use Sentry, install: npm install @sentry/react @sentry/tracing

export function initSentry() {
  // Only initialize in production
  if (process.env.NODE_ENV !== 'production') {
    return;
  }

  // Check if Sentry DSN is configured
  if (!process.env.NEXT_PUBLIC_SENTRY_DSN) {
    console.log('[Sentry] DSN not configured, skipping initialization');
    return;
  }

  // Try to initialize Sentry if available
  // Using try-catch with require to avoid build errors if packages aren't installed
  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const Sentry = require('@sentry/react');
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const { BrowserTracing } = require('@sentry/tracing');

    Sentry.init({
      dsn: process.env.NEXT_PUBLIC_SENTRY_DSN,
      environment: process.env.NODE_ENV,
      integrations: [
        new BrowserTracing(),
        new Sentry.Replay({
          maskAllText: true,
          blockAllMedia: true,
        }),
      ],
      tracesSampleRate: 1.0,
      replaysSessionSampleRate: 0.1,
      replaysOnErrorSampleRate: 1.0,
      beforeSend(event: any, hint: any) {
        // Filter out non-critical errors
        if (event.level === 'warning') {
          return null;
        }
        return event;
      },
    });
    console.log('[Sentry] Initialized successfully');
  } catch (error) {
    // Sentry packages not installed - this is fine, just log and continue
    console.log('[Sentry] Not available - packages not installed. Install with: npm install @sentry/react @sentry/tracing');
  }
}







