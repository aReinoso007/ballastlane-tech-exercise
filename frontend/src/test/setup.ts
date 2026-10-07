import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterAll, afterEach, beforeAll, vi } from 'vitest'
import { server } from './server'
import { tokenStorage } from '../shared/auth/tokenStorage'

beforeAll(() => {
  window.scrollTo = vi.fn()
  server.listen({ onUnhandledRequest: 'error' })
})
afterEach(() => {
  cleanup()
  server.resetHandlers()
  tokenStorage.clear()
  window.localStorage.clear()
})
afterAll(() => server.close())
