import { useQueryClient } from '@tanstack/react-query'
import { useId, useState, type FormEvent, type KeyboardEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { capitalize } from '../../shared/lib/format'
import { useDebouncedValue } from '../../shared/lib/useDebouncedValue'
import { searchOptions, usePokemonSearch } from './hooks'
import type { PokemonName } from './types'

/**
 * Header search box. Suggests Pokemon while typing and forgives typos ("pikachuu", "charzard"),
 * so users never have to know the exact spelling.
 */
export function PokemonSearch() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const listId = useId()
  const [text, setText] = useState('')
  const [open, setOpen] = useState(false)
  const [active, setActive] = useState(-1)
  const [message, setMessage] = useState<string | null>(null)

  const term = text.trim()
  const debounced = useDebouncedValue(term, 200)
  const { data: suggestions = [], isFetching } = usePokemonSearch(debounced)
  const shown = term ? suggestions : []
  const settled = term === debounced && !isFetching

  function openPokemon(name: string) {
    setText('')
    setOpen(false)
    setActive(-1)
    setMessage(null)
    navigate(`/pokemon/${encodeURIComponent(name)}`)
  }

  async function submit(event?: FormEvent) {
    event?.preventDefault()
    if (!term) return
    if (active >= 0 && shown[active]) return openPokemon(shown[active].name)
    try {
      const matches = await queryClient.fetchQuery(searchOptions(term))
      if (matches.length > 0) return openPokemon(matches[0].name)
      setMessage(`No Pokemon found for “${term}”`)
      setOpen(true)
    } catch {
      // The suggestion service is unavailable: still allow an exact name or number.
      openPokemon(term.toLowerCase())
    }
  }

  function onKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === 'ArrowDown') {
      event.preventDefault()
      setOpen(true)
      setActive((i) => (shown.length ? (i + 1) % shown.length : -1))
    } else if (event.key === 'ArrowUp') {
      event.preventDefault()
      setActive((i) => (shown.length ? (i <= 0 ? shown.length - 1 : i - 1) : -1))
    } else if (event.key === 'Enter' && !event.nativeEvent.isComposing) {
      event.preventDefault()
      void submit()
    } else if (event.key === 'Escape') {
      setOpen(false)
      setActive(-1)
    }
  }

  const noMatches = message ?? (term && settled && shown.length === 0 ? `No Pokemon found for “${term}”` : null)
  const expanded = open && term.length > 0 && (shown.length > 0 || noMatches !== null)

  return (
    <form role="search" onSubmit={submit} className="relative order-last w-full sm:order-none sm:max-w-sm sm:flex-1">
      <label htmlFor={`${listId}-input`} className="sr-only">
        Search Pokemon by name or number
      </label>
      <input
        id={`${listId}-input`}
        type="search"
        role="combobox"
        aria-expanded={expanded}
        aria-controls={listId}
        aria-autocomplete="list"
        aria-activedescendant={active >= 0 ? `${listId}-${active}` : undefined}
        autoComplete="off"
        spellCheck={false}
        value={text}
        placeholder="Search Pokemon, e.g. pikachu or 25"
        onChange={(e) => {
          setText(e.target.value)
          setOpen(true)
          setActive(-1)
          setMessage(null)
        }}
        onFocus={() => setOpen(true)}
        onBlur={() => setOpen(false)}
        onKeyDown={onKeyDown}
        className="w-full rounded-lg border border-white/30 bg-white px-3 py-1.5 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-2 focus:outline-white"
      />
      {expanded && (
        <ul
          id={listId}
          role="listbox"
          aria-label="Suggestions"
          className="absolute inset-x-0 top-full z-50 mt-1 overflow-hidden rounded-lg bg-white text-slate-900 shadow-lg ring-1 ring-slate-200"
        >
          {shown.map((item, index) => (
            <Suggestion
              key={item.id}
              item={item}
              id={`${listId}-${index}`}
              selected={index === active}
              onHover={() => setActive(index)}
              onChoose={() => openPokemon(item.name)}
            />
          ))}
          {shown.length === 0 && noMatches && <li className="px-3 py-2 text-sm text-slate-500">{noMatches}</li>}
        </ul>
      )}
    </form>
  )
}

function Suggestion({
  item,
  id,
  selected,
  onHover,
  onChoose,
}: {
  item: PokemonName
  id: string
  selected: boolean
  onHover: () => void
  onChoose: () => void
}) {
  return (
    <li
      id={id}
      role="option"
      aria-selected={selected}
      // Prevent the input from losing focus (and closing the list) before the click registers.
      onMouseDown={(e) => e.preventDefault()}
      onMouseEnter={onHover}
      onClick={onChoose}
      className={`flex cursor-pointer items-center gap-3 px-3 py-1.5 text-sm ${selected ? 'bg-red-50' : ''}`}
    >
      {item.spriteUrl ? (
        <img src={item.spriteUrl} alt="" width={32} height={32} className="h-8 w-8 object-contain" />
      ) : (
        <span className="h-8 w-8 rounded-full bg-slate-100" aria-hidden="true" />
      )}
      <span className="font-medium">{capitalize(item.name)}</span>
      <span className="ml-auto text-xs text-slate-500">#{String(item.id).padStart(4, '0')}</span>
    </li>
  )
}
