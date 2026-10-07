import { keepPreviousData, queryOptions, useQuery, useQueryClient } from '@tanstack/react-query'
import { useCallback } from 'react'
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

const DETAIL_STALE_MS = 5 * 60_000

function detailOptions(idOrName: string) {
  return queryOptions({
    queryKey: pokemonKeys.detail(idOrName),
    queryFn: ({ signal }) => fetchPokemon(idOrName, signal),
    staleTime: DETAIL_STALE_MS,
  })
}

export function usePokemonDetail(idOrName: string) {
  // keepPreviousData keeps the current Pokemon on screen while the next one loads,
  // so moving along an evolution chain never flashes a blank page or spinner.
  return useQuery({ ...detailOptions(idOrName), placeholderData: keepPreviousData })
}

/** Warms the cache for a Pokemon so that navigating to it later is instant. */
export function usePrefetchPokemon() {
  const queryClient = useQueryClient()
  return useCallback(
    (idOrName: string) => {
      void queryClient.prefetchQuery(detailOptions(idOrName))
    },
    [queryClient],
  )
}
