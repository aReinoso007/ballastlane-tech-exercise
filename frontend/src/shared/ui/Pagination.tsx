import { Button } from './Button'

interface Props {
  /** Zero-based current page. */
  page: number
  totalPages: number
  onChange: (page: number) => void
  disabled?: boolean
}

export function Pagination({ page, totalPages, onChange, disabled }: Props) {
  if (totalPages <= 1) return null
  return (
    <nav aria-label="Pagination" className="mt-6 flex items-center justify-center gap-4">
      <Button variant="secondary" onClick={() => onChange(page - 1)} disabled={disabled || page <= 0}>
        Previous
      </Button>
      <span className="text-sm text-slate-600" aria-live="polite">
        Page {page + 1} of {totalPages}
      </span>
      <Button variant="secondary" onClick={() => onChange(page + 1)} disabled={disabled || page >= totalPages - 1}>
        Next
      </Button>
    </nav>
  )
}
