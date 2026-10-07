import { useId, useState, type KeyboardEvent } from 'react'
import { addTag, isNewTag, suggestTags } from './tags'

interface Props {
  label: string
  value: string[]
  onChange: (tags: string[]) => void
  /** Tags used before; picking one reuses it, anything else typed becomes a new tag when saved. */
  library: string[]
  error?: string
  hint?: string
  maxLength?: number
}

export function TagInput({ label, value, onChange, library, error, hint, maxLength = 30 }: Props) {
  const id = useId()
  const listId = `${id}-list`
  const [text, setText] = useState('')
  const [open, setOpen] = useState(false)
  const [active, setActive] = useState(-1)

  const matches = suggestTags(library, value, text)
  const creating = isNewTag(library, value, text)
  const options = [...matches, ...(creating ? [text.trim()] : [])]
  const expanded = open && options.length > 0

  function commit(typed: string) {
    onChange(addTag(value, typed, library))
    setText('')
    setActive(-1)
  }

  function onKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === 'Enter' || event.key === ',') {
      event.preventDefault() // never submit the form from the tag box
      if (event.key === 'Enter' && active >= 0 && options[active]) commit(options[active])
      else commit(text)
    } else if (event.key === 'ArrowDown') {
      event.preventDefault()
      setOpen(true)
      setActive((i) => (options.length ? (i + 1) % options.length : -1))
    } else if (event.key === 'ArrowUp') {
      event.preventDefault()
      setActive((i) => (options.length ? (i <= 0 ? options.length - 1 : i - 1) : -1))
    } else if (event.key === 'Escape') {
      setOpen(false)
      setActive(-1)
    } else if (event.key === 'Backspace' && text === '' && value.length > 0) {
      onChange(value.slice(0, -1))
    }
  }

  const border = error ? 'border-red-500' : 'border-slate-300'
  return (
    <div className="relative">
      <label htmlFor={id} className="block text-sm font-medium text-slate-700">
        {label}
      </label>
      <div className={`mt-1 flex flex-wrap items-center gap-1.5 rounded-lg border bg-white px-2 py-1.5 shadow-sm focus-within:outline-2 focus-within:outline-brand ${border}`}>
        {value.map((tag) => (
          <span key={tag} className="inline-flex items-center gap-1 rounded-full bg-slate-100 py-0.5 pl-2.5 pr-1 text-xs text-slate-800">
            {tag}
            <button
              type="button"
              aria-label={`Remove ${tag}`}
              onClick={() => onChange(value.filter((t) => t !== tag))}
              className="flex h-4 w-4 items-center justify-center rounded-full text-slate-500 hover:bg-slate-300 hover:text-slate-900"
            >
              ×
            </button>
          </span>
        ))}
        <input
          id={id}
          role="combobox"
          aria-expanded={expanded}
          aria-controls={listId}
          aria-autocomplete="list"
          aria-activedescendant={active >= 0 ? `${id}-${active}` : undefined}
          aria-invalid={error ? true : undefined}
          aria-describedby={error ? `${id}-error` : hint ? `${id}-hint` : undefined}
          autoComplete="off"
          maxLength={maxLength}
          value={text}
          placeholder={value.length === 0 ? 'Add a tag…' : ''}
          onChange={(e) => {
            setText(e.target.value)
            setOpen(true)
            setActive(-1)
          }}
          onFocus={() => setOpen(true)}
          onClick={() => setOpen(true)}
          onBlur={() => {
            setOpen(false)
            if (text.trim()) commit(text) // keep what the user typed instead of silently dropping it
          }}
          onKeyDown={onKeyDown}
          className="min-w-[8rem] flex-1 border-0 bg-transparent px-1 py-0.5 text-sm outline-none"
        />
      </div>
      {expanded && (
        <ul id={listId} role="listbox" aria-label={`${label} suggestions`}
          className="absolute inset-x-0 z-40 mt-1 max-h-56 overflow-auto rounded-lg bg-white py-1 shadow-lg ring-1 ring-slate-200">
          {options.map((option, index) => {
            const isCreate = creating && index === options.length - 1
            return (
              <li
                key={`${isCreate ? 'new' : 'old'}-${option}`}
                id={`${id}-${index}`}
                role="option"
                aria-selected={index === active}
                onMouseDown={(e) => e.preventDefault()}
                onMouseEnter={() => setActive(index)}
                onClick={() => commit(option)}
                className={`cursor-pointer px-3 py-1.5 text-sm ${index === active ? 'bg-red-50' : ''}`}
              >
                {isCreate ? (
                  <>
                    Create “<span className="font-medium">{option}</span>”
                  </>
                ) : (
                  option
                )}
              </li>
            )
          })}
        </ul>
      )}
      {error ? (
        <p id={`${id}-error`} className="mt-1 text-xs text-red-700">
          {error}
        </p>
      ) : hint ? (
        <p id={`${id}-hint`} className="mt-1 text-xs text-slate-500">
          {hint}
        </p>
      ) : null}
    </div>
  )
}
