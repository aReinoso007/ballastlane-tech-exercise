export interface PokemonSummary {
  id: number
  name: string
  spriteUrl: string | null
  category: string | null
  weight: number
  abilities: string[]
}

export interface Stat {
  name: string
  baseStat: number
}

export interface EvolutionStage {
  id: number
  name: string
  spriteUrl: string | null
  stage: number
  evolvesFrom: string | null
}

/** Full representation, shared by the remote detail and locally stored records. */
export interface Pokemon {
  id: number
  name: string
  spriteUrl: string | null
  category: string | null
  description: string | null
  height: number
  weight: number
  abilities: string[]
  stats: Stat[]
  evolutions: EvolutionStage[]
  localizedName: string | null
  region: string | null
  tags: string[]
}
