export interface SessionUser {
  id: number
  username: string
  email: string
  role: string
}

export interface Session {
  token: string
  expiresAt: string
  user: SessionUser
}

const KEY = 'pokedex.session'
const listeners = new Set<() => void>()

function isExpired(session: Session): boolean {
  return new Date(session.expiresAt).getTime() <= Date.now()
}

function load(): Session | null {
  try {
    const raw = window.localStorage.getItem(KEY)
    if (!raw) return null
    const session = JSON.parse(raw) as Session
    if (!session.token || isExpired(session)) {
      window.localStorage.removeItem(KEY)
      return null
    }
    return session
  } catch {
    return null
  }
}

// Cached so useSyncExternalStore receives a stable snapshot reference.
let current: Session | null = load()

function emit() {
  listeners.forEach((l) => l())
}

export const tokenStorage = {
  getSession: (): Session | null => current,
  getToken: (): string | null => current?.token ?? null,
  set(session: Session) {
    current = session
    window.localStorage.setItem(KEY, JSON.stringify(session))
    emit()
  },
  clear() {
    if (current === null) return
    current = null
    window.localStorage.removeItem(KEY)
    emit()
  },
  subscribe(listener: () => void) {
    listeners.add(listener)
    return () => listeners.delete(listener)
  },
}
