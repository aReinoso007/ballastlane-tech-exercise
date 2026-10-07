import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { page, signIn } from '../../test/fixtures'
import { renderApp } from '../../test/render'
import { API, server } from '../../test/server'
import { tokenStorage } from '../../shared/auth/tokenStorage'

const loginOk = http.post(`${API}/auth/login`, async ({ request }) => {
  const body = (await request.json()) as { username: string; password: string }
  if (body.username === 'demo' && body.password === 'Demo1234!') {
    return HttpResponse.json({
      token: 'jwt-token', tokenType: 'Bearer', expiresAt: new Date(Date.now() + 3_600_000).toISOString(),
      user: { id: 1, username: 'demo', email: 'demo@ballastlane.dev', role: 'USER' },
    })
  }
  return HttpResponse.json({ detail: 'Invalid username or password' }, { status: 401 })
})

describe('authentication', () => {
  it('redirects anonymous users from protected routes to login', async () => {
    renderApp('/my-pokemon')
    expect(await screen.findByRole('heading', { name: 'Sign in' })).toBeInTheDocument()
  })

  it('signs in, stores the token and returns to the originally requested page', async () => {
    server.use(loginOk, http.get(`${API}/local/pokemon`, () => HttpResponse.json(page([], 0, 10))))
    renderApp('/my-pokemon')

    await userEvent.type(await screen.findByLabelText('Username'), 'demo')
    await userEvent.type(screen.getByLabelText('Password'), 'Demo1234!')
    await userEvent.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByRole('heading', { name: 'My Pokemon' })).toBeInTheDocument()
    expect(tokenStorage.getToken()).toBe('jwt-token')
    expect(screen.getByText('Hi, demo')).toBeInTheDocument()
  })

  it('shows the server message when credentials are wrong', async () => {
    server.use(loginOk)
    renderApp('/login')

    await userEvent.type(await screen.findByLabelText('Username'), 'demo')
    await userEvent.type(screen.getByLabelText('Password'), 'wrong-password')
    await userEvent.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid username or password')
    expect(tokenStorage.getToken()).toBeNull()
  })

  it('signs out', async () => {
    signIn()
    server.use(http.get(`${API}/pokemon`, () => HttpResponse.json(page([], 0, 12))))
    renderApp('/')

    await userEvent.click(await screen.findByRole('button', { name: 'Sign out' }))

    expect(tokenStorage.getToken()).toBeNull()
    expect(await screen.findByRole('link', { name: 'Sign in' })).toBeInTheDocument()
  })

  it('registers and sends the user to login', async () => {
    server.use(
      http.post(`${API}/auth/register`, () =>
        HttpResponse.json({ id: 2, username: 'new', email: 'new@example.com', role: 'USER' }, { status: 201 }),
      ),
    )
    renderApp('/register')

    await userEvent.type(await screen.findByLabelText('Username'), 'new')
    await userEvent.type(screen.getByLabelText('Email'), 'new@example.com')
    await userEvent.type(screen.getByLabelText('Password'), 'Password1')
    await userEvent.click(screen.getByRole('button', { name: 'Create account' }))

    expect(await screen.findByText('Account created. You can sign in now.')).toBeInTheDocument()
  })

  it('lists registration validation errors from the API', async () => {
    server.use(
      http.post(`${API}/auth/register`, () =>
        HttpResponse.json({ detail: 'x', errors: ['username must be 3-30 characters', 'password is weak'] }, { status: 400 }),
      ),
    )
    renderApp('/register')

    await userEvent.type(await screen.findByLabelText('Username'), 'a')
    await userEvent.type(screen.getByLabelText('Email'), 'a@b.co')
    await userEvent.type(screen.getByLabelText('Password'), 'x')
    await userEvent.click(screen.getByRole('button', { name: 'Create account' }))

    expect(await screen.findByText('username must be 3-30 characters')).toBeInTheDocument()
    expect(screen.getByText('password is weak')).toBeInTheDocument()
  })
})
