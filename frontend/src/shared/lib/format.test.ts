import { describe, expect, it } from 'vitest'
import { capitalize, formatHeight, formatWeight, statLabel } from './format'

describe('format helpers', () => {
  it('capitalizes hyphenated names', () => {
    expect(capitalize('bulbasaur')).toBe('Bulbasaur')
    expect(capitalize('mr-mime')).toBe('Mr Mime')
  })

  it('converts PokeAPI units', () => {
    expect(formatWeight(69)).toBe('6.9 kg')
    expect(formatHeight(7)).toBe('0.7 m')
  })

  it('labels known stats and falls back for unknown ones', () => {
    expect(statLabel('special-attack')).toBe('Sp. Atk')
    expect(statLabel('hp')).toBe('HP')
    expect(statLabel('mystery-stat')).toBe('Mystery Stat')
  })
})
