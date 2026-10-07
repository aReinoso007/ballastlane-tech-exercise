import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it } from 'vitest'
import { page, pokemon, signIn } from '../../test/fixtures'
import { renderApp } from '../../test/render'
import { API, server } from '../../test/server'

beforeEach(() => signIn())

describe('My Pokemon (US03/US04)', () => {
  it('lists local Pokemon with their custom fields', async () => {
    server.use(
      http.get(`${API}/local/pokemon`, () =>
        HttpResponse.json(page([pokemon({ localizedName: 'Bulbi', region: 'Kanto', tags: ['starter'] })], 0, 10)),
      ),
    )
    renderApp('/my-pokemon')

    expect(await screen.findByText('“Bulbi”')).toBeInTheDocument()
    expect(screen.getByText(/Kanto/)).toBeInTheDocument()
    expect(screen.getByText('starter')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Edit Bulbasaur' })).toHaveAttribute('href', '/my-pokemon/1/edit')
  })

  it('shows an empty state', async () => {
    server.use(http.get(`${API}/local/pokemon`, () => HttpResponse.json(page([], 0, 10))))
    renderApp('/my-pokemon')
    expect(await screen.findByText(/Nothing saved yet/)).toBeInTheDocument()
  })

  it('deletes after confirmation and refreshes the list', async () => {
    let items = [pokemon()]
    server.use(
      http.get(`${API}/local/pokemon`, () => HttpResponse.json(page(items, 0, 10))),
      http.delete(`${API}/local/pokemon/1`, () => {
        items = []
        return new HttpResponse(null, { status: 204 })
      }),
    )
    renderApp('/my-pokemon')

    await userEvent.click(await screen.findByRole('button', { name: 'Delete Bulbasaur' }))
    const dialog = screen.getByRole('dialog')
    expect(within(dialog).getByText('Delete Bulbasaur?')).toBeInTheDocument()
    await userEvent.click(within(dialog).getByRole('button', { name: 'Delete' }))

    expect(await screen.findByText(/Nothing saved yet/)).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('does not delete when the dialog is cancelled', async () => {
    let deleted = false
    server.use(
      http.get(`${API}/local/pokemon`, () => HttpResponse.json(page([pokemon()], 0, 10))),
      http.delete(`${API}/local/pokemon/1`, () => {
        deleted = true
        return new HttpResponse(null, { status: 204 })
      }),
    )
    renderApp('/my-pokemon')

    await userEvent.click(await screen.findByRole('button', { name: 'Delete Bulbasaur' }))
    await userEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Cancel' }))

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(deleted).toBe(false)
  })
})

describe('Edit local Pokemon (US04)', () => {
  const mockGet = () => server.use(http.get(`${API}/local/pokemon/1`, () => HttpResponse.json(pokemon())))

  it('pre-fills the form and sends a PUT with the edited values', async () => {
    mockGet()
    let body: unknown
    server.use(
      http.put(`${API}/local/pokemon/1`, async ({ request }) => {
        body = await request.json()
        return HttpResponse.json(pokemon({ region: 'Kanto' }))
      }),
      http.get(`${API}/local/pokemon`, () => HttpResponse.json(page([pokemon({ region: 'Kanto' })], 0, 10))),
    )
    renderApp('/my-pokemon/1/edit')

    expect(await screen.findByLabelText('Name')).toHaveValue('bulbasaur')
    await userEvent.type(screen.getByLabelText('Region'), 'Kanto')
    await userEvent.type(screen.getByLabelText('Tags'), 'starter, grass')
    await userEvent.click(screen.getByRole('button', { name: 'Save changes' }))

    expect(await screen.findByRole('heading', { name: 'My Pokemon' })).toBeInTheDocument()
    expect(body).toEqual({
      name: 'bulbasaur', height: 7, weight: 69, category: 'Seed Pokémon',
      description: 'A strange seed was planted on its back at birth.',
      abilities: ['overgrow', 'chlorophyll'], localizedName: null, region: 'Kanto', tags: ['starter', 'grass'],
    })
  })

  it('blocks submission and explains client-side validation errors', async () => {
    mockGet()
    let called = false
    server.use(http.put(`${API}/local/pokemon/1`, () => {
      called = true
      return HttpResponse.json(pokemon())
    }))
    renderApp('/my-pokemon/1/edit')

    const weight = await screen.findByLabelText('Weight (hg)')
    await userEvent.clear(weight)
    await userEvent.type(weight, '-5')
    await userEvent.clear(screen.getByLabelText('Name'))
    await userEvent.click(screen.getByRole('button', { name: 'Save changes' }))

    expect(await screen.findByText('Weight must be a positive whole number')).toBeInTheDocument()
    expect(screen.getByText('Name is required')).toBeInTheDocument()
    expect(screen.getByLabelText('Weight (hg)')).toHaveAttribute('aria-invalid', 'true')
    expect(called).toBe(false)
  })

  it('shows server-side validation errors', async () => {
    mockGet()
    server.use(
      http.put(`${API}/local/pokemon/1`, () =>
        HttpResponse.json({ detail: 'Request validation failed', errors: ['name: size must be between 1 and 100'] }, { status: 400 }),
      ),
    )
    renderApp('/my-pokemon/1/edit')

    await screen.findByLabelText('Name')
    await userEvent.click(screen.getByRole('button', { name: 'Save changes' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('name: size must be between 1 and 100')
  })

  it('shows a not found page for records that are not stored locally', async () => {
    server.use(http.get(`${API}/local/pokemon/99`, () => HttpResponse.json({ detail: 'nope' }, { status: 404 })))
    renderApp('/my-pokemon/99/edit')
    expect(await screen.findByRole('heading', { name: 'Pokemon not found' })).toBeInTheDocument()
  })
})
