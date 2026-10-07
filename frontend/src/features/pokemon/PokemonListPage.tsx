import { useState, type FormEvent } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Button } from '../../shared/ui/Button'
import { Pagination } from '../../shared/ui/Pagination'
import { QueryError } from '../../shared/ui/QueryState'
import { Spinner } from '../../shared/ui/Spinner'
import { PokemonCard } from './PokemonCard'
import { usePokemonPage } from './hooks'

const PAGE_SIZE = 12

export function PokemonListPage() {
  const [params, setParams] = useSearchParams()
  const navigate = useNavigate()
  const requested = Number.parseInt(params.get('page') ?? '1', 10)
  const page = Number.isFinite(requested) && requested > 0 ? requested - 1 : 0
  const [search, setSearch] = useState('')

  const { data, error, isPending, isFetching, refetch } = usePokemonPage(page, PAGE_SIZE)

  function goToPage(next: number) {
    setParams({ page: String(next + 1) })
    window.scrollTo({ top: 0 })
  }

  function onSearch(event: FormEvent) {
    event.preventDefault()
    const term = search.trim().toLowerCase()
    if (term) navigate(`/pokemon/${encodeURIComponent(term)}`)
  }

  return (
    <section>
      <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold">Pokemon</h1>
          <p className="text-sm text-slate-600">Browse the catalog and open any Pokemon for details.</p>
        </div>
        <form onSubmit={onSearch} role="search" className="flex gap-2">
          <label htmlFor="pokemon-search" className="sr-only">
            Find by name or number
          </label>
          <input id="pokemon-search" value={search} onChange={(e) => setSearch(e.target.value)}
            placeholder="Name or number, e.g. pikachu"
            className="w-56 rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm focus:outline-2 focus:outline-brand" />
          <Button type="submit" disabled={!search.trim()}>
            Go
          </Button>
        </form>
      </div>

      {isPending && <Spinner label="Loading Pokemon" />}
      {error && !data && <QueryError error={error} onRetry={() => void refetch()} />}

      {data && (
        <>
          <ul className={`grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 ${isFetching ? 'opacity-60' : ''}`}
            aria-busy={isFetching}>
            {data.items.map((pokemon) => (
              <PokemonCard key={pokemon.id} pokemon={pokemon} />
            ))}
          </ul>
          {data.items.length === 0 && <p className="py-10 text-center text-slate-500">No Pokemon on this page.</p>}
          <Pagination page={data.page} totalPages={data.totalPages} onChange={goToPage} disabled={isFetching} />
        </>
      )}
    </section>
  )
}
