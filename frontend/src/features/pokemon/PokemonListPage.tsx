import { useSearchParams } from 'react-router-dom'
import { Pagination } from '../../shared/ui/Pagination'
import { QueryError } from '../../shared/ui/QueryState'
import { Spinner } from '../../shared/ui/Spinner'
import { PokemonCard } from './PokemonCard'
import { usePokemonPage } from './hooks'

const PAGE_SIZE = 12

export function PokemonListPage() {
  const [params, setParams] = useSearchParams()
  const requested = Number.parseInt(params.get('page') ?? '1', 10)
  const page = Number.isFinite(requested) && requested > 0 ? requested - 1 : 0

  const { data, error, isPending, isFetching, refetch } = usePokemonPage(page, PAGE_SIZE)

  function goToPage(next: number) {
    setParams({ page: String(next + 1) })
    window.scrollTo({ top: 0 })
  }

  return (
    <section>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Pokemon</h1>
        <p className="text-sm text-slate-600">Browse the catalog, or search by name or number from the top bar.</p>
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
