import { request } from '../../shared/api/http'
import type { Page } from '../../shared/api/types'
import type { Pokemon, PokemonSummary } from './types'

export function fetchPokemonPage(page: number, size: number, signal?: AbortSignal): Promise<Page<PokemonSummary>> {
  return request(`/pokemon?page=${page}&size=${size}`, { signal })
}

export function fetchPokemon(idOrName: string, signal?: AbortSignal): Promise<Pokemon> {
  return request(`/pokemon/${encodeURIComponent(idOrName)}`, { signal })
}
