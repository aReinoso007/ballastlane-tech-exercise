import { createContext } from 'react'
import type { Session } from '../../shared/auth/tokenStorage'

export interface AuthContextValue {
  session: Session | null
  isAuthenticated: boolean
  signIn: (username: string, password: string) => Promise<void>
  signOut: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
