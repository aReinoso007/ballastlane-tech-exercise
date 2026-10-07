import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../features/auth/useAuth'
import { Button } from '../shared/ui/Button'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-1.5 text-sm font-medium ${isActive ? 'bg-white/20' : 'hover:bg-white/10'}`

export function Layout() {
  const { session, isAuthenticated, signOut } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="flex min-h-screen flex-col">
      <header className="bg-brand text-white shadow">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-2 px-4 py-3">
          <Link to="/" className="text-xl font-extrabold tracking-tight">
            Pokedex
          </Link>
          <nav aria-label="Main" className="flex flex-wrap items-center gap-1">
            <NavLink to="/" end className={linkClass}>
              Browse
            </NavLink>
            {isAuthenticated && (
              <NavLink to="/my-pokemon" className={linkClass}>
                My Pokemon
              </NavLink>
            )}
            {isAuthenticated ? (
              <>
                <span className="ml-2 hidden text-sm sm:inline">Hi, {session?.user.username}</span>
                <Button variant="secondary" className="ml-2 !py-1"
                  onClick={() => {
                    signOut()
                    navigate('/')
                  }}>
                  Sign out
                </Button>
              </>
            ) : (
              <>
                <NavLink to="/login" className={linkClass}>
                  Sign in
                </NavLink>
                <NavLink to="/register" className={linkClass}>
                  Register
                </NavLink>
              </>
            )}
          </nav>
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6">
        <Outlet />
      </main>
      <footer className="py-4 text-center text-xs text-slate-500">
        Data from{' '}
        <a href="https://pokeapi.co" className="underline" target="_blank" rel="noreferrer">
          PokeAPI
        </a>
      </footer>
    </div>
  )
}
