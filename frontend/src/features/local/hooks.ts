import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  deleteLocalPokemon,
  fetchLocalPage,
  fetchLocalPokemon,
  fetchTags,
  syncPokemon,
  updateLocalPokemon,
  type PokemonUpdate,
} from './api'

export const localKeys = {
  all: ['local'] as const,
  list: (page: number, size: number) => ['local', 'list', page, size] as const,
  ids: ['local', 'ids'] as const,
  detail: (id: number) => ['local', 'detail', id] as const,
  tags: ['local', 'tags'] as const,
}

export function useLocalPage(page: number, size: number) {
  return useQuery({
    queryKey: localKeys.list(page, size),
    queryFn: ({ signal }) => fetchLocalPage(page, size, signal),
    placeholderData: keepPreviousData,
  })
}

/** Ids already stored locally; lets the detail page show "Saved" without probing for 404s. */
export function useLocalPokemonIds(enabled: boolean) {
  return useQuery({
    queryKey: localKeys.ids,
    queryFn: async ({ signal }) => new Set((await fetchLocalPage(0, 100, signal)).items.map((p) => p.id)),
    enabled,
  })
}

export function useLocalPokemon(id: number) {
  return useQuery({
    queryKey: localKeys.detail(id),
    queryFn: ({ signal }) => fetchLocalPokemon(id, signal),
  })
}

/** The tag library; new tags appear here after a Pokemon is saved with them (all `local` queries are invalidated). */
export function useTags() {
  return useQuery({ queryKey: localKeys.tags, queryFn: ({ signal }) => fetchTags(signal), staleTime: 60_000 })
}

export function useSyncPokemon() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: syncPokemon,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: localKeys.all }),
  })
}

export function useUpdatePokemon(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (update: PokemonUpdate) => updateLocalPokemon(id, update),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: localKeys.all }),
  })
}

export function useDeletePokemon() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: deleteLocalPokemon,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: localKeys.all }),
  })
}
