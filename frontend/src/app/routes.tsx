import { Route, Routes } from 'react-router-dom'
import { LoginPage } from '../features/auth/LoginPage'
import { ProtectedRoute } from '../features/auth/ProtectedRoute'
import { RegisterPage } from '../features/auth/RegisterPage'
import { LocalPokemonEditPage } from '../features/local/LocalPokemonEditPage'
import { LocalPokemonPage } from '../features/local/LocalPokemonPage'
import { PokemonDetailPage } from '../features/pokemon/PokemonDetailPage'
import { PokemonListPage } from '../features/pokemon/PokemonListPage'
import { Layout } from './Layout'
import { NotFoundPage } from './NotFoundPage'

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<PokemonListPage />} />
        <Route path="pokemon/:idOrName" element={<PokemonDetailPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="my-pokemon" element={<LocalPokemonPage />} />
          <Route path="my-pokemon/:id/edit" element={<LocalPokemonEditPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
