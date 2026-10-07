import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { ApiError } from '../../shared/api/http'
import { capitalize } from '../../shared/lib/format'
import { Button } from '../../shared/ui/Button'
import { ConfirmDialog } from '../../shared/ui/ConfirmDialog'
import { Pagination } from '../../shared/ui/Pagination'
import { QueryError } from '../../shared/ui/QueryState'
import { Spinner } from '../../shared/ui/Spinner'
import { useToast } from '../../shared/ui/useToast'
import { Sprite } from '../pokemon/Sprite'
import type { Pokemon } from '../pokemon/types'
import { useDeletePokemon, useLocalPage } from './hooks'

const PAGE_SIZE = 10

export function LocalPokemonPage() {
  const [params, setParams] = useSearchParams()
  const requested = Number.parseInt(params.get('page') ?? '1', 10)
  const page = Number.isFinite(requested) && requested > 0 ? requested - 1 : 0
  const { data, error, isPending, isFetching, refetch } = useLocalPage(page, PAGE_SIZE)
  const remove = useDeletePokemon()
  const { notify } = useToast()
  const [toDelete, setToDelete] = useState<Pokemon | null>(null)

  function confirmDelete() {
    if (!toDelete) return
    const target = toDelete
    remove.mutate(target.id, {
      onSuccess: () => {
        notify(`${capitalize(target.name)} removed`)
        // Step back if the last item of the last page was removed.
        if (data && data.items.length === 1 && page > 0) setParams({ page: String(page) })
      },
      onError: (e) => notify(e instanceof ApiError ? e.message : 'Could not delete', 'error'),
      onSettled: () => setToDelete(null),
    })
  }

  return (
    <section>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">My Pokemon</h1>
        <p className="text-sm text-slate-600">
          Pokemon replicated into the local database. Add your own names, regions and tags.{' '}
          <Link to="/" className="font-medium text-brand hover:underline">
            Browse more
          </Link>
        </p>
      </div>

      {isPending && <Spinner label="Loading your Pokemon" />}
      {error && !data && <QueryError error={error} onRetry={() => void refetch()} />}

      {data && data.items.length === 0 && (
        <p className="rounded-xl bg-white p-8 text-center text-slate-600 ring-1 ring-slate-200">
          Nothing saved yet. Open a Pokemon and choose “Save to my Pokedex”.
        </p>
      )}

      {data && data.items.length > 0 && (
        <>
          <ul className={`space-y-3 ${isFetching ? 'opacity-60' : ''}`}>
            {data.items.map((p) => (
              <li key={p.id}
                className="flex flex-col gap-4 rounded-xl bg-white p-4 shadow-sm ring-1 ring-slate-200 sm:flex-row sm:items-center">
                <Sprite src={p.spriteUrl} alt={capitalize(p.name)} className="h-20 w-20 shrink-0" />
                <div className="min-w-0 flex-1">
                  <h2 className="text-lg font-semibold">
                    <Link to={`/pokemon/${p.name}`} className="hover:underline">
                      {capitalize(p.name)}
                    </Link>
                    {p.localizedName && <span className="ml-2 text-base font-normal text-slate-500">“{p.localizedName}”</span>}
                  </h2>
                  <p className="text-sm text-slate-600">
                    #{String(p.id).padStart(4, '0')}
                    {p.category ? ` · ${p.category}` : ''}
                    {p.region ? ` · ${p.region}` : ''}
                  </p>
                  {p.tags.length > 0 && (
                    <ul className="mt-2 flex flex-wrap gap-1" aria-label="Tags">
                      {p.tags.map((t) => (
                        <li key={t} className="rounded-full bg-amber-100 px-2 py-0.5 text-xs text-amber-900">
                          {t}
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
                <div className="flex gap-2">
                  <Link to={`/my-pokemon/${p.id}/edit`}
                    className="inline-flex items-center rounded-lg bg-white px-4 py-2 text-sm font-semibold ring-1 ring-slate-300 hover:bg-slate-50"
                    aria-label={`Edit ${capitalize(p.name)}`}>
                    Edit
                  </Link>
                  <Button variant="danger" onClick={() => setToDelete(p)} aria-label={`Delete ${capitalize(p.name)}`}>
                    Delete
                  </Button>
                </div>
              </li>
            ))}
          </ul>
          <Pagination page={data.page} totalPages={data.totalPages}
            onChange={(next) => setParams({ page: String(next + 1) })} disabled={isFetching} />
        </>
      )}

      {toDelete && (
        <ConfirmDialog
          title={`Delete ${capitalize(toDelete.name)}?`}
          message="It will be removed from your local database. You can sync it again later."
          confirmLabel="Delete"
          busy={remove.isPending}
          onConfirm={confirmDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </section>
  )
}
