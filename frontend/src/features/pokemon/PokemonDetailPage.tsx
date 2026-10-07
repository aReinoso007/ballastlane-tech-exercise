import { useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ApiError } from '../../shared/api/http'
import { capitalize, formatHeight, formatWeight } from '../../shared/lib/format'
import { Alert } from '../../shared/ui/Alert'
import { Button } from '../../shared/ui/Button'
import { QueryError } from '../../shared/ui/QueryState'
import { Spinner } from '../../shared/ui/Spinner'
import { useToast } from '../../shared/ui/useToast'
import { useAuth } from '../auth/useAuth'
import { useLocalPokemonIds, useSyncPokemon } from '../local/hooks'
import { EvolutionChain } from './EvolutionChain'
import { Sprite } from './Sprite'
import { StatBars } from './StatBars'
import { usePokemonDetail, usePrefetchPokemon } from './hooks'

export function PokemonDetailPage() {
  const { idOrName = '' } = useParams()
  const { data: pokemon, error, isPending, isPlaceholderData, refetch } = usePokemonDetail(idOrName)
  const prefetch = usePrefetchPokemon()

  // Once a Pokemon is on screen, warm its whole evolution line in the background.
  useEffect(() => {
    if (!pokemon || isPlaceholderData) return
    pokemon.evolutions.forEach((stage) => {
      if (stage.name !== pokemon.name) prefetch(stage.name)
    })
  }, [pokemon, isPlaceholderData, prefetch])

  if (isPending) return <Spinner label="Loading Pokemon" />
  if (error) {
    if (error instanceof ApiError && error.status === 404) {
      return (
        <div className="py-10 text-center">
          <h1 className="text-2xl font-bold">Pokemon not found</h1>
          <p className="mt-2 text-slate-600">We could not find “{idOrName}”.</p>
          <Link to="/" className="mt-4 inline-block font-medium text-brand hover:underline">
            Back to the list
          </Link>
        </div>
      )
    }
    return <QueryError error={error} onRetry={() => void refetch()} />
  }

  return (
    <article
      className={`space-y-6 transition-opacity ${isPlaceholderData ? 'opacity-60' : ''}`}
      aria-busy={isPlaceholderData}
    >
      <Link to="/" className="text-sm font-medium text-brand hover:underline">
        ← Back to list
      </Link>

      <header className="grid gap-6 rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-200 md:grid-cols-[16rem_1fr]">
        <Sprite src={pokemon.spriteUrl} alt={capitalize(pokemon.name)} className="mx-auto h-56 w-56" />
        <div className="space-y-3">
          <p className="text-sm text-slate-500">#{String(pokemon.id).padStart(4, '0')}</p>
          <h1 className="text-3xl font-bold">{capitalize(pokemon.name)}</h1>
          {pokemon.category && <p className="text-slate-600">{pokemon.category}</p>}
          {pokemon.description && <p className="text-slate-800">{pokemon.description}</p>}
          <dl className="grid grid-cols-2 gap-4 text-sm sm:max-w-xs">
            <div>
              <dt className="text-slate-500">Height</dt>
              <dd className="font-semibold">{formatHeight(pokemon.height)}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Weight</dt>
              <dd className="font-semibold">{formatWeight(pokemon.weight)}</dd>
            </div>
          </dl>
          <ul className="flex flex-wrap gap-2" aria-label="Abilities">
            {pokemon.abilities.map((a) => (
              <li key={a} className="rounded-full bg-slate-100 px-3 py-1 text-xs text-slate-700">
                {capitalize(a)}
              </li>
            ))}
          </ul>
          <SaveLocally key={pokemon.id} pokemonId={pokemon.id} name={pokemon.name} />
        </div>
      </header>

      <div className="grid gap-6 md:grid-cols-2">
        <section className="rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-200" aria-labelledby="stats-title">
          <h2 id="stats-title" className="mb-4 text-lg font-semibold">
            Base stats
          </h2>
          <StatBars stats={pokemon.stats} />
        </section>
        <section className="rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-200" aria-labelledby="evo-title">
          <h2 id="evo-title" className="mb-4 text-lg font-semibold">
            Evolution
          </h2>
          <EvolutionChain stages={pokemon.evolutions} currentName={pokemon.name} />
        </section>
      </div>
    </article>
  )
}

function SaveLocally({ pokemonId, name }: { pokemonId: number; name: string }) {
  const { isAuthenticated } = useAuth()
  const { notify } = useToast()
  const sync = useSyncPokemon()
  const ids = useLocalPokemonIds(isAuthenticated)

  if (!isAuthenticated) {
    return (
      <p className="text-sm text-slate-600">
        <Link to="/login" state={{ from: `/pokemon/${name}` }} className="font-medium text-brand hover:underline">
          Sign in
        </Link>{' '}
        to save this Pokemon to your local Pokedex.
      </p>
    )
  }

  const alreadySaved = ids.data?.has(pokemonId) || sync.isSuccess
  if (alreadySaved) {
    return (
      <div className="flex flex-wrap items-center gap-3">
        <span className="rounded-full bg-green-100 px-3 py-1 text-sm font-medium text-green-800">Saved locally</span>
        <Link to={`/my-pokemon/${pokemonId}/edit`} className="text-sm font-medium text-brand hover:underline">
          Customize
        </Link>
      </div>
    )
  }

  return (
    <div className="space-y-2">
      <Button
        disabled={sync.isPending || ids.isPending}
        onClick={() =>
          sync.mutate(name, {
            onSuccess: () => notify(`${capitalize(name)} saved to your Pokedex`),
            onError: (e) => notify(e instanceof ApiError ? e.message : 'Could not save this Pokemon', 'error'),
          })
        }
      >
        {sync.isPending ? 'Saving…' : 'Save to my Pokedex'}
      </Button>
      {sync.error && <Alert tone="error">{sync.error.message}</Alert>}
    </div>
  )
}
