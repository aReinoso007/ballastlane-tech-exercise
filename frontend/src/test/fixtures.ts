import type { Pokemon, PokemonSummary } from '../features/pokemon/types'
import { tokenStorage } from '../shared/auth/tokenStorage'

export const summary = (overrides: Partial<PokemonSummary> = {}): PokemonSummary => ({
  id: 1,
  name: 'bulbasaur',
  spriteUrl: 'https://img/1.png',
  category: 'Seed Pokémon',
  weight: 69,
  abilities: ['overgrow', 'chlorophyll'],
  ...overrides,
})

export const pokemon = (overrides: Partial<Pokemon> = {}): Pokemon => ({
  id: 1,
  name: 'bulbasaur',
  spriteUrl: 'https://img/1.png',
  category: 'Seed Pokémon',
  description: 'A strange seed was planted on its back at birth.',
  height: 7,
  weight: 69,
  abilities: ['overgrow', 'chlorophyll'],
  stats: [
    { name: 'hp', baseStat: 45 },
    { name: 'special-attack', baseStat: 65 },
  ],
  evolutions: [
    { id: 1, name: 'bulbasaur', spriteUrl: 'https://img/1.png', stage: 0, evolvesFrom: null },
    { id: 2, name: 'ivysaur', spriteUrl: 'https://img/2.png', stage: 1, evolvesFrom: 'bulbasaur' },
  ],
  localizedName: null,
  region: null,
  tags: [],
  ...overrides,
})

export function page<T>(items: T[], pageIndex = 0, size = 12, totalItems = items.length) {
  return { items, page: pageIndex, size, totalItems, totalPages: Math.ceil(totalItems / size) }
}

export function signIn() {
  tokenStorage.set({
    token: 'test-token',
    expiresAt: new Date(Date.now() + 3_600_000).toISOString(),
    user: { id: 1, username: 'alex', email: 'alex@example.com', role: 'USER' },
  })
}
