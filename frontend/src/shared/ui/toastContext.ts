import { createContext } from 'react'

export type ToastTone = 'success' | 'error'

export interface ToastApi {
  notify: (message: string, tone?: ToastTone) => void
}

export const ToastContext = createContext<ToastApi | null>(null)
