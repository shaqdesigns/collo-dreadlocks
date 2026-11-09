import axios from 'axios'

export function apiError(cause: unknown, fallback: string): string {
  if (axios.isAxiosError(cause)) {
    const data = cause.response?.data
    if (typeof data === 'string' && data.trim().length > 0) {
      return data
    }
    if (data && typeof data === 'object') {
      const record = data as { message?: unknown; detail?: unknown; error?: unknown }
      for (const value of [record.message, record.detail, record.error]) {
        if (typeof value === 'string' && value.length > 0) {
          return value
        }
      }
    }
  }
  return fallback
}
