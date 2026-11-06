import axios from 'axios'

export function apiError(cause: unknown, fallback: string): string {
  if (axios.isAxiosError(cause)) {
    const message = (cause.response?.data as { message?: unknown } | undefined)?.message
    if (typeof message === 'string' && message.length > 0) {
      return message
    }
  }
  return fallback
}
