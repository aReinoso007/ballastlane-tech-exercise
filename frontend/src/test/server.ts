import { setupServer } from 'msw/node'

export const API = 'http://localhost:3999/api'

export const server = setupServer()
