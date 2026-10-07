import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../../shared/api/http'
import { capitalize } from '../../shared/lib/format'
import { Alert } from '../../shared/ui/Alert'
import { Button } from '../../shared/ui/Button'
import { QueryError } from '../../shared/ui/QueryState'
import { Spinner } from '../../shared/ui/Spinner'
import { TextField } from '../../shared/ui/TextField'
import { useToast } from '../../shared/ui/useToast'
import { EvolutionChain } from '../pokemon/EvolutionChain'
import { Sprite } from '../pokemon/Sprite'
import { StatBars } from '../pokemon/StatBars'
import type { Pokemon } from '../pokemon/types'
import { toFormState, toUpdate, validate, type FormErrors, type FormState } from './form'
import { useLocalPokemon, useUpdatePokemon } from './hooks'

export function LocalPokemonEditPage() {
  const { id } = useParams()
  const pokemonId = Number.parseInt(id ?? '', 10)
  const { data, error, isPending, refetch } = useLocalPokemon(pokemonId)

  if (!Number.isFinite(pokemonId)) return <NotFound />
  if (isPending) return <Spinner label="Loading Pokemon" />
  if (error) {
    if (error instanceof ApiError && error.status === 404) return <NotFound />
    return <QueryError error={error} onRetry={() => void refetch()} />
  }
  return <EditForm pokemon={data} />
}

function NotFound() {
  return (
    <div className="py-10 text-center">
      <h1 className="text-2xl font-bold">Pokemon not found</h1>
      <p className="mt-2 text-slate-600">It is not stored in your local database.</p>
      <Link to="/my-pokemon" className="mt-4 inline-block font-medium text-brand hover:underline">
        Back to My Pokemon
      </Link>
    </div>
  )
}

function EditForm({ pokemon }: { pokemon: Pokemon }) {
  const navigate = useNavigate()
  const { notify } = useToast()
  const update = useUpdatePokemon(pokemon.id)
  const [form, setForm] = useState<FormState>(() => toFormState(pokemon))
  const [errors, setErrors] = useState<FormErrors>({})
  const [serverErrors, setServerErrors] = useState<string[]>([])

  const set = (key: keyof FormState) => (value: string) => setForm((f) => ({ ...f, [key]: value }))

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    setServerErrors([])
    const found = validate(form)
    setErrors(found)
    if (Object.keys(found).length > 0) return

    update.mutate(toUpdate(form), {
      onSuccess: () => {
        notify(`${capitalize(form.name.trim())} updated`)
        navigate('/my-pokemon')
      },
      onError: (e) => {
        if (e instanceof ApiError) setServerErrors(e.errors.length > 0 ? e.errors : [e.message])
        else setServerErrors(['Something went wrong. Please try again.'])
      },
    })
  }

  return (
    <div className="space-y-6">
      <Link to="/my-pokemon" className="text-sm font-medium text-brand hover:underline">
        ← Back to My Pokemon
      </Link>
      <div className="grid gap-6 lg:grid-cols-[1fr_20rem]">
        <form onSubmit={onSubmit} noValidate
          className="space-y-4 rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-200" aria-label="Edit Pokemon">
          <h1 className="text-2xl font-bold">Edit {capitalize(pokemon.name)}</h1>
          {serverErrors.length > 0 && (
            <Alert tone="error">
              <ul className="list-disc pl-4">
                {serverErrors.map((e) => (
                  <li key={e}>{e}</li>
                ))}
              </ul>
            </Alert>
          )}

          <fieldset className="grid gap-4 sm:grid-cols-3">
            <legend className="mb-2 text-sm font-semibold text-slate-500">Pokemon data</legend>
            <div className="sm:col-span-3">
              <TextField label="Name" value={form.name} onChange={set('name')} error={errors.name} required />
            </div>
            <TextField label="Height (dm)" inputMode="numeric" value={form.height} onChange={set('height')} error={errors.height} />
            <TextField label="Weight (hg)" inputMode="numeric" value={form.weight} onChange={set('weight')} error={errors.weight} />
            <TextField label="Category" value={form.category} onChange={set('category')} error={errors.category} />
            <div className="sm:col-span-3">
              <TextField label="Description" multiline value={form.description} onChange={set('description')} error={errors.description} />
            </div>
            <div className="sm:col-span-3">
              <TextField label="Abilities" value={form.abilities} onChange={set('abilities')} error={errors.abilities}
                hint="Comma separated, e.g. overgrow, chlorophyll" />
            </div>
          </fieldset>

          <fieldset className="grid gap-4 sm:grid-cols-2">
            <legend className="mb-2 text-sm font-semibold text-slate-500">Your own fields</legend>
            <TextField label="Localized name" value={form.localizedName} onChange={set('localizedName')} error={errors.localizedName} />
            <TextField label="Region" value={form.region} onChange={set('region')} error={errors.region} />
            <div className="sm:col-span-2">
              <TextField label="Tags" value={form.tags} onChange={set('tags')} error={errors.tags}
                hint="Comma separated, e.g. starter, favourite" />
            </div>
          </fieldset>

          <div className="flex justify-end gap-3">
            <Button variant="secondary" onClick={() => navigate('/my-pokemon')}>
              Cancel
            </Button>
            <Button type="submit" disabled={update.isPending}>
              {update.isPending ? 'Saving…' : 'Save changes'}
            </Button>
          </div>
        </form>

        <aside className="space-y-6">
          <div className="rounded-xl bg-white p-6 text-center shadow-sm ring-1 ring-slate-200">
            <Sprite src={pokemon.spriteUrl} alt={capitalize(pokemon.name)} className="mx-auto h-40 w-40" />
          </div>
          <div className="rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
            <h2 className="mb-3 text-sm font-semibold text-slate-500">Base stats (read only)</h2>
            <StatBars stats={pokemon.stats} />
          </div>
          <div className="rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
            <h2 className="mb-3 text-sm font-semibold text-slate-500">Evolution (read only)</h2>
            <EvolutionChain stages={pokemon.evolutions} currentName={pokemon.name} />
          </div>
        </aside>
      </div>
    </div>
  )
}
