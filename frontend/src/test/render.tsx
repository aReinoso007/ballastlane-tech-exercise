import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { CoreProviders } from '../app/Providers'
import { createQueryClient } from '../app/queryClient'
import { AppRoutes } from '../app/routes'

export function renderApp(route = '/') {
  const client = createQueryClient()
  client.setDefaultOptions({ queries: { retry: false, refetchOnWindowFocus: false } })
  return render(
    <MemoryRouter initialEntries={[route]}>
      <CoreProviders client={client}>
        <AppRoutes />
      </CoreProviders>
    </MemoryRouter>,
  )
}
