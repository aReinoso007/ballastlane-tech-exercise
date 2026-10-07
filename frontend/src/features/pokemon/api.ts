import { request } from '../../shared/api/http'
import type { Page } from '../../shared/api/types'
import type { Pokemon, PokemonName, PokemonSummary } from './types'

export function fetchPokemonPage(page: number, size: number, signal?: AbortSignal): Promise<Page<PokemonSummary>> {
  return request(`/pokemon?page=${page}&size=${size}`, { signal })
}

export function fetchPokemon(idOrName: string, signal?: AbortSignal): Promise<Pokemon> {
  return request(`/pokemon/${encodeURIComponent(idOrName)}`, { signal })
}

export function searchPokemon(query: string, limit: number, signal?: AbortSignal): Promise<PokemonName[]> {
  return request(`/pokemon/search?q=${encodeURIComponent(query)}&limit=${limit}`, { signal })
}
