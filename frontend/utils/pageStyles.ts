import { useDashboardTheme } from '../contexts/DashboardThemeContext'

export interface PageStyles {
  header: string
  headerText: string
  card: string
  cardIcon: string
  chart: string
  kpiCard: string
  kpiCardAlt: string
  kpiCardWarm: string
  kpiCardPurple: string
  section: string
  button: string
  buttonSecondary: string
  input: string
  table: string
  tableHeader: string
  tableRow: string
}

export const usePageStyles = (): PageStyles => {
  try {
    const { dashboardStyle } = useDashboardTheme()
    
    switch (dashboardStyle) {
      case 'landing-page':
        return {
          header: 'bg-gradient-to-br from-blue-50 via-white to-green-50 rounded-3xl shadow-2xl border border-blue-200 p-8 text-gray-900 relative overflow-hidden',
          headerText: 'text-gray-600',
          card: 'bg-white rounded-2xl shadow-xl border border-blue-100 p-6 transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          cardIcon: 'text-blue-600',
          chart: 'bg-white rounded-2xl shadow-xl border border-blue-100 p-6 transform hover:scale-105 transition-all duration-300',
          kpiCard: 'bg-gradient-to-br from-blue-500 to-blue-600 rounded-2xl shadow-xl p-4 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardAlt: 'bg-gradient-to-br from-green-500 to-green-600 rounded-2xl shadow-xl p-4 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardWarm: 'bg-gradient-to-br from-orange-500 to-orange-600 rounded-2xl shadow-xl p-4 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardPurple: 'bg-gradient-to-br from-purple-500 to-purple-600 rounded-2xl shadow-xl p-4 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          section: 'bg-white rounded-2xl shadow-xl border border-blue-100 p-6 mb-6',
          button: 'bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white px-6 py-3 rounded-xl font-medium shadow-lg hover:shadow-xl transform hover:scale-105 transition-all duration-300',
          buttonSecondary: 'bg-gradient-to-r from-gray-600 to-gray-700 hover:from-gray-700 hover:to-gray-800 text-white px-6 py-3 rounded-xl font-medium shadow-lg hover:shadow-xl transform hover:scale-105 transition-all duration-300',
          input: 'w-full px-4 py-3 border border-blue-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent shadow-sm',
          table: 'w-full bg-white rounded-2xl shadow-xl border border-blue-100 overflow-hidden',
          tableHeader: 'bg-gradient-to-r from-blue-50 to-blue-100 px-6 py-4 text-left text-sm font-semibold text-blue-900',
          tableRow: 'border-b border-blue-100 hover:bg-blue-50 transition-colors duration-200'
        }
      
      case 'system-theme':
        return {
          header: 'bg-gradient-to-r from-gray-800 via-blue-900 to-gray-800 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden',
          headerText: 'text-blue-200',
          card: 'bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-gray-200 dark:border-gray-700 p-6',
          cardIcon: 'text-blue-500',
          chart: 'bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-gray-200 dark:border-gray-700 p-6',
          kpiCard: 'bg-green-50 dark:bg-green-900/20 rounded-xl p-4 text-center',
          kpiCardAlt: 'bg-blue-50 dark:bg-blue-900/20 rounded-xl p-4 text-center',
          kpiCardWarm: 'bg-purple-50 dark:bg-purple-900/20 rounded-xl p-4 text-center',
          kpiCardPurple: 'bg-purple-50 dark:bg-purple-900/20 rounded-xl p-4 text-center',
          section: 'bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-gray-200 dark:border-gray-700 p-6 mb-6',
          button: 'bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2 transition-colors duration-200',
          buttonSecondary: 'bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2 transition-colors duration-200',
          input: 'w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white',
          table: 'w-full bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-gray-200 dark:border-gray-700 overflow-hidden',
          tableHeader: 'bg-gray-50 dark:bg-gray-700 px-6 py-4 text-left text-sm font-semibold text-gray-900 dark:text-white',
          tableRow: 'border-b border-gray-200 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors duration-200'
        }
      case 'system-default':
        return {
          header: 'bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-8',
          headerText: 'text-gray-900 dark:text-white',
          card: 'bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6',
          cardIcon: 'text-gray-600 dark:text-gray-400',
          chart: 'bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6',
          kpiCard: 'bg-gray-50 dark:bg-gray-700 rounded-lg p-4 text-center',
          kpiCardAlt: 'bg-gray-50 dark:bg-gray-700 rounded-lg p-4 text-center',
          kpiCardWarm: 'bg-gray-50 dark:bg-gray-700 rounded-lg p-4 text-center',
          kpiCardPurple: 'bg-gray-50 dark:bg-gray-700 rounded-lg p-4 text-center',
          section: 'bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6 mb-6',
          button: 'bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors duration-200',
          buttonSecondary: 'bg-white hover:bg-gray-50 text-gray-700 border border-gray-300 px-4 py-2 rounded-md text-sm font-medium transition-colors duration-200',
          input: 'w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white',
          table: 'w-full bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden',
          tableHeader: 'bg-gray-50 dark:bg-gray-700 px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider',
          tableRow: 'border-b border-gray-200 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors duration-200'
        }
      
      default: // dark theme
        return {
          header: 'bg-gradient-to-r from-gray-800 via-blue-900 to-gray-800 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden',
          headerText: 'text-blue-200',
          card: 'bg-gray-800 rounded-2xl shadow-xl border border-gray-700 p-6 transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          cardIcon: 'text-blue-400',
          chart: 'bg-gray-800 rounded-2xl shadow-xl border border-gray-700 p-6 transform hover:scale-105 transition-all duration-300',
          kpiCard: 'bg-gradient-to-br from-blue-600 to-blue-700 rounded-2xl shadow-xl p-4 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardAlt: 'bg-gradient-to-br from-green-600 to-green-700 rounded-2xl shadow-xl p-4 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardWarm: 'bg-gradient-to-br from-orange-600 to-orange-700 rounded-2xl shadow-xl p-4 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardPurple: 'bg-gradient-to-br from-purple-600 to-purple-700 rounded-2xl shadow-xl p-4 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          section: 'bg-gray-800 rounded-2xl shadow-xl border border-gray-700 p-6 mb-6',
          button: 'bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white px-6 py-3 rounded-xl font-medium shadow-lg hover:shadow-xl transform hover:scale-105 transition-all duration-300',
          buttonSecondary: 'bg-gradient-to-r from-gray-600 to-gray-700 hover:from-gray-700 hover:to-gray-800 text-white px-6 py-3 rounded-xl font-medium shadow-lg hover:shadow-xl transform hover:scale-105 transition-all duration-300',
          input: 'w-full px-4 py-3 border border-gray-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent shadow-sm bg-gray-700 text-white',
          table: 'w-full bg-gray-800 rounded-2xl shadow-xl border border-gray-700 overflow-hidden',
          tableHeader: 'bg-gradient-to-r from-gray-700 to-gray-600 px-6 py-4 text-left text-sm font-semibold text-white',
          tableRow: 'border-b border-gray-700 hover:bg-gray-700 transition-colors duration-200'
        }
    }
  } catch (error) {
    // Fallback to dark theme if context is not available
    // console.warn('DashboardThemeProvider context not available, using fallback dark theme')
    return {
      header: 'bg-gradient-to-r from-gray-800 via-blue-900 to-gray-800 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden',
      headerText: 'text-blue-200',
      card: 'bg-gray-800 rounded-2xl shadow-xl border border-gray-700 p-6 transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
      cardIcon: 'text-blue-400',
      chart: 'bg-gray-800 rounded-2xl shadow-xl border border-gray-700 p-6 transform hover:scale-105 transition-all duration-300',
      kpiCard: 'bg-gradient-to-br from-blue-600 to-blue-700 rounded-2xl shadow-xl p-4 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
      kpiCardAlt: 'bg-gradient-to-br from-green-600 to-green-700 rounded-2xl shadow-xl p-4 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
      kpiCardWarm: 'bg-gradient-to-br from-orange-600 to-orange-700 rounded-2xl shadow-xl p-4 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
      kpiCardPurple: 'bg-gradient-to-br from-purple-600 to-purple-700 rounded-2xl shadow-xl p-4 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
      section: 'bg-gray-800 rounded-2xl shadow-xl border border-gray-700 p-6 mb-6',
      button: 'bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white px-6 py-3 rounded-xl font-medium shadow-lg hover:shadow-xl transform hover:scale-105 transition-all duration-300',
      buttonSecondary: 'bg-gradient-to-r from-gray-600 to-gray-700 hover:from-gray-700 hover:to-gray-800 text-white px-6 py-3 rounded-xl font-medium shadow-lg hover:shadow-xl transform hover:scale-105 transition-all duration-300',
      input: 'w-full px-4 py-3 border border-gray-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent shadow-sm bg-gray-700 text-white',
      table: 'w-full bg-gray-800 rounded-2xl shadow-xl border border-gray-700 overflow-hidden',
      tableHeader: 'bg-gradient-to-r from-gray-700 to-gray-600 px-6 py-4 text-left text-sm font-semibold text-white',
      tableRow: 'border-b border-gray-700 hover:bg-gray-700 transition-colors duration-200'
    }
  }
}
