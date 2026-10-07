import { request } from '../../shared/api/http'
import type { Page } from '../../shared/api/types'
import type { Pokemon } from '../pokemon/types'

export interface PokemonUpdate {
  name: string
  height: number
  weight: number
  category: string | null
  description: string | null
  abilities: string[]
  localizedName: string | null
  region: string | null
  tags: string[]
}

export const fetchLocalPage = (page: number, size: number, signal?: AbortSignal): Promise<Page<Pokemon>> =>
  request(`/local/pokemon?page=${page}&size=${size}`, { signal })

export const fetchLocalPokemon = (id: number, signal?: AbortSignal): Promise<Pokemon> =>
  request(`/local/pokemon/${id}`, { signal })

export const syncPokemon = (idOrName: string): Promise<Pokemon> =>
  request(`/local/pokemon/${encodeURIComponent(idOrName)}/sync`, { method: 'POST' })

export const updateLocalPokemon = (id: number, update: PokemonUpdate): Promise<Pokemon> =>
  request(`/local/pokemon/${id}`, { method: 'PUT', body: update })

export const deleteLocalPokemon = (id: number): Promise<void> =>
  request(`/local/pokemon/${id}`, { method: 'DELETE' })

/** Tags used before on any local Pokemon, offered for reuse. */
export const fetchTags = (signal?: AbortSignal): Promise<string[]> => request('/local/tags', { signal })
