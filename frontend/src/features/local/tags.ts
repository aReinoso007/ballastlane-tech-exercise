/** Case-insensitive comparison key for tags: "Favourite" and "favourite" are the same tag. */
const key = (tag: string) => tag.trim().toLowerCase()

/**
 * Adds a typed tag to the selection. A tag that already exists in the library (any letter case) reuses
 * the stored spelling; a tag already selected is not added twice; blank input is ignored.
 */
export function addTag(selected: string[], typed: string, library: string[]): string[] {
  const clean = typed.trim()
  if (!clean) return selected
  if (selected.some((t) => key(t) === key(clean))) return selected
  const spelling = library.find((t) => key(t) === key(clean)) ?? clean
  return [...selected, spelling]
}

/** Library tags not selected yet, filtered by what is being typed (substring, any case). */
export function suggestTags(library: string[], selected: string[], typed: string): string[] {
  const needle = key(typed)
  return library.filter((tag) => !selected.some((s) => key(s) === key(tag)) && key(tag).includes(needle))
}

/** True when the typed text is not yet a known or selected tag, i.e. saving would create it. */
export function isNewTag(library: string[], selected: string[], typed: string): boolean {
  const k = key(typed)
  return k !== '' && ![...library, ...selected].some((t) => key(t) === k)
}
