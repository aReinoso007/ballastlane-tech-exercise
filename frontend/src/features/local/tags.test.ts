import { describe, expect, it } from 'vitest'
import { addTag, isNewTag, suggestTags } from './tags'

const library = ['Favourite', 'sleepy', 'starter']

describe('tag helpers', () => {
  it('reuses the stored spelling of a known tag whatever case is typed', () => {
    expect(addTag([], 'FAVOURITE', library)).toEqual(['Favourite'])
  })

  it('adds an unknown tag as typed, trimmed', () => {
    expect(addTag(['starter'], '  brand new ', library)).toEqual(['starter', 'brand new'])
  })

  it('never adds the same tag twice, ignoring case', () => {
    expect(addTag(['Favourite'], 'favourite', library)).toEqual(['Favourite'])
  })

  it('ignores blank input', () => {
    expect(addTag(['a'], '   ', library)).toEqual(['a'])
  })

  it('suggests unselected library tags that contain what is typed', () => {
    expect(suggestTags(library, ['sleepy'], '')).toEqual(['Favourite', 'starter'])
    expect(suggestTags(library, [], 'ST')).toEqual(['starter'])
    expect(suggestTags(library, [], 'zzz')).toEqual([])
  })

  it('knows when typing would create a new tag', () => {
    expect(isNewTag(library, [], 'brand new')).toBe(true)
    expect(isNewTag(library, [], 'FAVOURITE')).toBe(false)
    expect(isNewTag(library, ['mine'], 'Mine')).toBe(false)
    expect(isNewTag(library, [], '  ')).toBe(false)
  })
})
