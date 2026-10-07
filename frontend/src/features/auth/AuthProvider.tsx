import { useQueryClient } from '@tanstack/react-query'
import { useCallback, useMemo, useSyncExternalStore, type ReactNode } from 'react'
import { tokenStorage } from '../../shared/auth/tokenStorage'
import { login } from './api'
import { AuthContext, type AuthContextValue } from './authContext'

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const session = useSyncExternalStore(tokenStorage.subscribe, tokenStorage.getSession)

  const signIn = useCallback(async (username: string, password: string) => {
    tokenStorage.set(await login(username, password))
  }, [])

  const signOut = useCallback(() => {
    tokenStorage.clear()
    // Local (per-user) data must never leak into the next session.
    queryClient.removeQueries({ queryKey: ['local'] })
  }, [queryClient])

  const value = useMemo<AuthContextValue>(
    () => ({ session, isAuthenticated: session !== null, signIn, signOut }),
    [session, signIn, signOut],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
