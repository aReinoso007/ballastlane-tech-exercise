import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { fetchPokemon, fetchPokemonPage } from './api'

export const pokemonKeys = {
  list: (page: number, size: number) => ['pokemon', 'list', page, size] as const,
  detail: (idOrName: string) => ['pokemon', 'detail', idOrName] as const,
}

export function usePokemonPage(page: number, size: number) {
  return useQuery({
    queryKey: pokemonKeys.list(page, size),
    queryFn: ({ signal }) => fetchPokemonPage(page, size, signal),
    placeholderData: keepPreviousData,
    staleTime: 5 * 60_000,
  })
}

export function usePokemonDetail(idOrName: string) {
  return useQuery({
    queryKey: pokemonKeys.detail(idOrName),
    queryFn: ({ signal }) => fetchPokemon(idOrName, signal),
    staleTime: 5 * 60_000,
  })
}
