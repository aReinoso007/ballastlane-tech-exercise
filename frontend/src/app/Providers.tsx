import { QueryClientProvider, type QueryClient } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from '../features/auth/AuthProvider'
import { ToastProvider } from '../shared/ui/ToastProvider'

/** Everything except the router, so tests can supply their own (memory) router. */
export function CoreProviders({ client, children }: { client: QueryClient; children: ReactNode }) {
  return (
    <QueryClientProvider client={client}>
      <AuthProvider>
        <ToastProvider>{children}</ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  )
}

export function BrowserProviders({ client, children }: { client: QueryClient; children: ReactNode }) {
  return (
    <BrowserRouter>
      <CoreProviders client={client}>{children}</CoreProviders>
    </BrowserRouter>
  )
}
