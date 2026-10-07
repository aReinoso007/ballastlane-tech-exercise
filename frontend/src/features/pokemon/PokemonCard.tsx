import { Link } from 'react-router-dom'
import { capitalize, formatWeight } from '../../shared/lib/format'
import { Sprite } from './Sprite'
import type { PokemonSummary } from './types'

export function PokemonCard({ pokemon }: { pokemon: PokemonSummary }) {
  return (
    <li>
      <Link
        to={`/pokemon/${pokemon.name}`}
        className="flex h-full flex-col rounded-xl bg-white p-4 shadow-sm ring-1 ring-slate-200 transition hover:shadow-md hover:ring-brand focus-visible:outline-2 focus-visible:outline-brand"
      >
        <div className="flex items-center justify-between text-xs text-slate-500">
          <span>#{String(pokemon.id).padStart(4, '0')}</span>
          <span>{formatWeight(pokemon.weight)}</span>
        </div>
        <Sprite src={pokemon.spriteUrl} alt={capitalize(pokemon.name)} className="mx-auto h-28 w-28" />
        <h3 className="mt-2 text-center text-lg font-semibold">{capitalize(pokemon.name)}</h3>
        {pokemon.category && <p className="text-center text-sm text-slate-500">{pokemon.category}</p>}
        <ul className="mt-3 flex flex-wrap justify-center gap-1" aria-label="Abilities">
          {pokemon.abilities.map((ability) => (
            <li key={ability} className="rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-700">
              {capitalize(ability)}
            </li>
          ))}
        </ul>
      </Link>
    </li>
  )
}
