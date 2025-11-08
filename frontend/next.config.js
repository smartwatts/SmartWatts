/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  swcMinify: true,
  output: 'standalone', // Required for Azure Static Web Apps
  images: {
    domains: ['localhost', 'smartwatts.ng'],
    unoptimized: true, // Required for Azure Static Web Apps
  },
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
    NEXT_PUBLIC_WS_URL: process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080',
  },
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'Referrer-Policy',
            value: 'origin-when-cross-origin',
          },
        ],
      },
    ];
  },
  async rewrites() {
    // For Azure Static Web Apps, use environment variable for API URL
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
    return [
      {
        source: '/api/proxy/:path*',
        destination: `${apiUrl}/api/proxy/:path*`,
      },
    ];
  },
  // Azure Static Web Apps configuration
  trailingSlash: false,
  poweredByHeader: false,
}

module.exports = nextConfig 