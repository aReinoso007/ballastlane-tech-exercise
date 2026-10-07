import type { Pokemon } from '../pokemon/types'
import type { PokemonUpdate } from './api'

/** Raw, string-based form state (inputs are always strings). */
export interface FormState {
  name: string
  height: string
  weight: string
  category: string
  description: string
  abilities: string
  localizedName: string
  region: string
  tags: string[]
}

export type FormErrors = Partial<Record<keyof FormState, string>>

export const splitList = (value: string): string[] =>
  value
    .split(',')
    .map((v) => v.trim())
    .filter(Boolean)

export function toFormState(pokemon: Pokemon): FormState {
  return {
    name: pokemon.name,
    height: String(pokemon.height),
    weight: String(pokemon.weight),
    category: pokemon.category ?? '',
    description: pokemon.description ?? '',
    abilities: pokemon.abilities.join(', '),
    localizedName: pokemon.localizedName ?? '',
    region: pokemon.region ?? '',
    tags: pokemon.tags
  }
}

const isPositiveInteger = (v: string) => /^\d+$/.test(v.trim()) && Number(v) > 0

/** Mirrors the backend rules so users get instant feedback; the server remains the source of truth. */
export function validate(form: FormState): FormErrors {
  const errors: FormErrors = {}
  if (!form.name.trim()) errors.name = 'Name is required'
  else if (form.name.trim().length > 100) errors.name = 'Name must be at most 100 characters'
  if (!isPositiveInteger(form.height)) errors.height = 'Height must be a positive whole number'
  if (!isPositiveInteger(form.weight)) errors.weight = 'Weight must be a positive whole number'
  if (form.category.length > 100) errors.category = 'Category must be at most 100 characters'
  if (form.description.length > 2000) errors.description = 'Description must be at most 2000 characters'
  const abilities = splitList(form.abilities)
  if (abilities.length < 1 || abilities.length > 10) errors.abilities = 'Provide between 1 and 10 abilities'
  else if (abilities.some((a) => a.length > 60)) errors.abilities = 'Each ability must be at most 60 characters'
  if (form.localizedName.length > 100) errors.localizedName = 'Localized name must be at most 100 characters'
  if (form.region.length > 50) errors.region = 'Region must be at most 50 characters'
  if (form.tags.length > 10) errors.tags = 'At most 10 tags'
  else if (form.tags.some((t) => t.length > 30)) errors.tags = 'Each tag must be at most 30 characters'
  return errors
}

const emptyToNull = (v: string) => (v.trim() === '' ? null : v.trim())

export function toUpdate(form: FormState): PokemonUpdate {
  return {
    name: form.name.trim(),
    height: Number(form.height),
    weight: Number(form.weight),
    category: emptyToNull(form.category),
    description: emptyToNull(form.description),
    abilities: splitList(form.abilities),
    localizedName: emptyToNull(form.localizedName),
    region: emptyToNull(form.region),
    tags: form.tags,
  }
}
