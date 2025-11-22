/**
 * Role utility functions for SmartWatts
 * 
 * Role Hierarchy:
 * - ROLE_ENTERPRISE_ADMIN: Ultimate admin privileges (system administration)
 * - ROLE_ADMIN: Basic admin privileges (can access admin dashboard)
 * - ROLE_USER: Regular user privileges
 */

/**
 * Check if a user has admin privileges (either ROLE_ADMIN or ROLE_ENTERPRISE_ADMIN)
 * @param role - The user's role
 * @returns true if the user has admin privileges
 */
export function isAdmin(role: string | undefined | null): boolean {
  return role === 'ROLE_ADMIN' || role === 'ROLE_ENTERPRISE_ADMIN'
}

/**
 * Check if a user has enterprise admin privileges (ROLE_ENTERPRISE_ADMIN only)
 * This role has ultimate privileges over ROLE_ADMIN
 * @param role - The user's role
 * @returns true if the user has enterprise admin privileges
 */
export function isEnterpriseAdmin(role: string | undefined | null): boolean {
  return role === 'ROLE_ENTERPRISE_ADMIN'
}

/**
 * Check if a user has basic admin privileges (ROLE_ADMIN only, not enterprise)
 * @param role - The user's role
 * @returns true if the user has basic admin privileges
 */
export function isBasicAdmin(role: string | undefined | null): boolean {
  return role === 'ROLE_ADMIN'
}

/**
 * Get the admin level of a role
 * @param role - The user's role
 * @returns 'enterprise' | 'admin' | 'user' | null
 */
export function getAdminLevel(role: string | undefined | null): 'enterprise' | 'admin' | 'user' | null {
  if (isEnterpriseAdmin(role)) return 'enterprise'
  if (isAdmin(role)) return 'admin'
  if (role === 'ROLE_USER') return 'user'
  return null
}

