import { request } from '../../shared/api/http'
import type { Session, SessionUser } from '../../shared/auth/tokenStorage'

interface LoginResponse {
  token: string
  tokenType: string
  expiresAt: string
  user: SessionUser
}

export async function login(username: string, password: string): Promise<Session> {
  const response = await request<LoginResponse>('/auth/login', {
    method: 'POST',
    body: { username, password },
  })
  return { token: response.token, expiresAt: response.expiresAt, user: response.user }
}

export function register(username: string, email: string, password: string): Promise<SessionUser> {
  return request<SessionUser>('/auth/register', {
    method: 'POST',
    body: { username, email, password },
  })
}
