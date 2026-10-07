import { describe, expect, it } from 'vitest'
import { splitList, toFormState, toUpdate, validate, type FormState } from './form'
import type { Pokemon } from '../pokemon/types'

const pokemon: Pokemon = {
  id: 1, name: 'bulbasaur', spriteUrl: null, category: 'Seed Pokémon', description: 'desc', height: 7, weight: 69,
  abilities: ['overgrow', 'chlorophyll'], stats: [], evolutions: [], localizedName: null, region: null, tags: ['starter'],
}

const valid = (): FormState => toFormState(pokemon)

describe('local pokemon form', () => {
  it('round-trips a Pokemon through form state', () => {
    expect(toUpdate(valid())).toEqual({
      name: 'bulbasaur', height: 7, weight: 69, category: 'Seed Pokémon', description: 'desc',
      abilities: ['overgrow', 'chlorophyll'], localizedName: null, region: null, tags: ['starter'],
    })
  })

  it('splits comma separated lists ignoring blanks', () => {
    expect(splitList(' a, b ,, c ')).toEqual(['a', 'b', 'c'])
  })

  it('accepts valid input', () => {
    expect(validate(valid())).toEqual({})
  })

  it('flags invalid fields', () => {
    const errors = validate({
      ...valid(), name: ' ', height: '0', weight: '-3', abilities: '',
      tags: Array.from({ length: 11 }, (_, i) => `t${i}`), region: 'x'.repeat(51),
    })
    expect(Object.keys(errors).sort()).toEqual(['abilities', 'height', 'name', 'region', 'tags', 'weight'])
  })

  it('rejects decimals and non numeric measurements', () => {
    expect(validate({ ...valid(), height: '1.5' }).height).toBeDefined()
    expect(validate({ ...valid(), weight: 'abc' }).weight).toBeDefined()
  })

  it('sends blank optional fields as null', () => {
    const update = toUpdate({ ...valid(), category: '  ', region: '', tags: [] })
    expect(update.category).toBeNull()
    expect(update.region).toBeNull()
    expect(update.tags).toEqual([])
  })
})
