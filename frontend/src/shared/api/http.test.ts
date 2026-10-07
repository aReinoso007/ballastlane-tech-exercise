import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { API, server } from '../../test/server'
import { signIn } from '../../test/fixtures'
import { tokenStorage } from '../auth/tokenStorage'
import { ApiError, request } from './http'

describe('request', () => {
  it('sends the bearer token when a session exists', async () => {
    signIn()
    let header: string | null = null
    server.use(
      http.get(`${API}/ping`, ({ request: r }) => {
        header = r.headers.get('Authorization')
        return HttpResponse.json({ ok: true })
      }),
    )
    await request('/ping')
    expect(header).toBe('Bearer test-token')
  })

  it('maps problem+json responses to ApiError with field errors', async () => {
    server.use(
      http.post(`${API}/things`, () =>
        HttpResponse.json({ detail: 'Request validation failed', errors: ['name: must not be blank'] }, { status: 400 }),
      ),
    )
    const error = await request('/things', { method: 'POST', body: {} }).catch((e: unknown) => e)
    expect(error).toBeInstanceOf(ApiError)
    expect((error as ApiError).status).toBe(400)
    expect((error as ApiError).message).toBe('Request validation failed')
    expect((error as ApiError).errors).toEqual(['name: must not be blank'])
  })

  it('clears the session on 401 for authenticated calls', async () => {
    signIn()
    server.use(http.get(`${API}/secure`, () => HttpResponse.json({ detail: 'expired' }, { status: 401 })))
    await expect(request('/secure')).rejects.toBeInstanceOf(ApiError)
    expect(tokenStorage.getToken()).toBeNull()
  })

  it('handles 204 responses', async () => {
    server.use(http.delete(`${API}/x/1`, () => new HttpResponse(null, { status: 204 })))
    await expect(request('/x/1', { method: 'DELETE' })).resolves.toBeUndefined()
  })

  it('falls back to a generic message for non-JSON errors', async () => {
    server.use(http.get(`${API}/boom`, () => new HttpResponse('<html>oops</html>', { status: 500 })))
    await expect(request('/boom')).rejects.toMatchObject({ status: 500, message: 'Request failed (500)' })
  })

  it('reports network failures as status 0', async () => {
    server.use(http.get(`${API}/down`, () => HttpResponse.error()))
    await expect(request('/down')).rejects.toMatchObject({ status: 0 })
  })
})
