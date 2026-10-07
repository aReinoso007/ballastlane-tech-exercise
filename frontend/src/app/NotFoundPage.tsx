import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <div className="py-10 text-center">
      <h1 className="text-2xl font-bold">Page not found</h1>
      <Link to="/" className="mt-4 inline-block font-medium text-brand hover:underline">
        Back to the Pokedex
      </Link>
    </div>
  )
}
