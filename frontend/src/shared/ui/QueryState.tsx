import { ApiError } from '../api/http'
import { Alert } from './Alert'
import { Button } from './Button'

export function QueryError({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  const message = error instanceof ApiError ? error.message : 'Something went wrong while loading data.'
  return (
    <div className="space-y-3 py-6">
      <Alert tone="error">{message}</Alert>
      {onRetry && (
        <Button variant="secondary" onClick={onRetry}>
          Try again
        </Button>
      )}
    </div>
  )
}
