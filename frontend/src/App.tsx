import { useState } from 'react'
import { BrowserProviders } from './app/Providers'
import { createQueryClient } from './app/queryClient'
import { AppRoutes } from './app/routes'

export default function App() {
  const [client] = useState(createQueryClient)
  return (
    <BrowserProviders client={client}>
      <AppRoutes />
    </BrowserProviders>
  )
}
