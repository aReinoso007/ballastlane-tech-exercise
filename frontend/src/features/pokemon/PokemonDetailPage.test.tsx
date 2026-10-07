import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { describe, expect, it, vi } from 'vitest'
import { page, pokemon, signIn } from '../../test/fixtures'
import { renderApp } from '../../test/render'
import { API, server } from '../../test/server'

function mockDetail() {
  server.use(http.get(`${API}/pokemon/bulbasaur`, () => HttpResponse.json(pokemon())))
}

describe('PokemonDetailPage (US02)', () => {
  it('shows image, stats, description and evolution lineage', async () => {
    mockDetail()
    renderApp('/pokemon/bulbasaur')

    expect(await screen.findByRole('heading', { name: 'Bulbasaur' })).toBeInTheDocument()
    expect(screen.getAllByRole('img', { name: 'Bulbasaur' })[0]).toHaveAttribute('src', 'https://img/1.png')
    expect(screen.getByText('A strange seed was planted on its back at birth.')).toBeInTheDocument()
    expect(screen.getByText('0.7 m')).toBeInTheDocument()
    expect(screen.getByText('6.9 kg')).toBeInTheDocument()

    const stats = screen.getByRole('heading', { name: 'Base stats' }).closest('section')!
    expect(within(stats).getByText('HP')).toBeInTheDocument()
    expect(within(stats).getByText('Sp. Atk')).toBeInTheDocument()
    expect(within(stats).getByText('65')).toBeInTheDocument()

    const evolution = screen.getByRole('list', { name: 'Evolution lineage' })
    expect(within(evolution).getByRole('link', { name: /ivysaur/i })).toHaveAttribute('href', '/pokemon/ivysaur')
  })

  it('keeps the current Pokemon on screen while the next evolution loads', async () => {
    let releaseIvysaur: () => void = () => {}
    const gate = new Promise<void>((resolve) => (releaseIvysaur = resolve))
    server.use(
      http.get(`${API}/pokemon/bulbasaur`, () => HttpResponse.json(pokemon())),
      http.get(`${API}/pokemon/ivysaur`, async () => {
        await gate
        return HttpResponse.json(pokemon({ id: 2, name: 'ivysaur' }))
      }),
    )
    renderApp('/pokemon/bulbasaur')
    await screen.findByRole('heading', { name: 'Bulbasaur' })

    await userEvent.click(within(screen.getByRole('list', { name: 'Evolution lineage' })).getByRole('link', { name: /ivysaur/i }))

    // Still showing Bulbasaur (dimmed), no spinner and no blank page.
    expect(screen.getByRole('heading', { name: 'Bulbasaur' })).toBeInTheDocument()
    expect(screen.queryByRole('status')).not.toBeInTheDocument()

    releaseIvysaur()
    expect(await screen.findByRole('heading', { name: 'Ivysaur' })).toBeInTheDocument()
  })

  it('prefetches the rest of the evolution line so later navigation is instant', async () => {
    const requested: string[] = []
    server.use(
      http.get(`${API}/pokemon/bulbasaur`, () =>
        HttpResponse.json(
          pokemon({
            evolutions: [
              { id: 1, name: 'bulbasaur', spriteUrl: null, stage: 0, evolvesFrom: null },
              { id: 2, name: 'ivysaur', spriteUrl: null, stage: 1, evolvesFrom: 'bulbasaur' },
              { id: 3, name: 'venusaur', spriteUrl: null, stage: 2, evolvesFrom: 'ivysaur' },
            ],
          }),
        ),
      ),
      http.get(`${API}/pokemon/:name`, ({ params }) => {
        requested.push(String(params.name))
        return HttpResponse.json(pokemon({ id: 2, name: String(params.name) }))
      }),
    )
    renderApp('/pokemon/bulbasaur')
    await screen.findByRole('heading', { name: 'Bulbasaur' })

    await vi.waitFor(() => expect(requested).toEqual(expect.arrayContaining(['ivysaur', 'venusaur'])))
  })

  it('says when a Pokemon does not evolve', async () => {
    server.use(http.get(`${API}/pokemon/ditto`, () => HttpResponse.json(pokemon({ name: 'ditto', evolutions: [] }))))
    renderApp('/pokemon/ditto')
    expect(await screen.findByText('This Pokemon does not evolve.')).toBeInTheDocument()
  })

  it('renders a friendly page for unknown Pokemon', async () => {
    server.use(http.get(`${API}/pokemon/nope`, () => HttpResponse.json({ detail: "Pokemon 'nope' was not found" }, { status: 404 })))
    renderApp('/pokemon/nope')
    expect(await screen.findByRole('heading', { name: 'Pokemon not found' })).toBeInTheDocument()
  })

  it('asks anonymous users to sign in before saving', async () => {
    mockDetail()
    renderApp('/pokemon/bulbasaur')
    await screen.findByRole('heading', { name: 'Bulbasaur' })

    expect(screen.queryByRole('button', { name: /save to my pokedex/i })).not.toBeInTheDocument()
    expect(within(screen.getByRole('main')).getByRole('link', { name: 'Sign in' })).toHaveAttribute('href', '/login')
  })
})

describe('Saving locally (US03)', () => {
  it('syncs the Pokemon and then shows it as saved', async () => {
    signIn()
    mockDetail()
    let synced = false
    server.use(
      http.get(`${API}/local/pokemon`, () => HttpResponse.json(page(synced ? [pokemon()] : [], 0, 100))),
      http.post(`${API}/local/pokemon/bulbasaur/sync`, () => {
        synced = true
        return HttpResponse.json(pokemon(), { status: 201 })
      }),
    )
    renderApp('/pokemon/bulbasaur')

    const save = await screen.findByRole('button', { name: 'Save to my Pokedex' })
    await userEvent.click(save)

    expect(await screen.findByText('Saved locally')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Customize' })).toHaveAttribute('href', '/my-pokemon/1/edit')
    expect(synced).toBe(true)
  })

  it('shows already-saved state without offering to sync again', async () => {
    signIn()
    mockDetail()
    server.use(http.get(`${API}/local/pokemon`, () => HttpResponse.json(page([pokemon()], 0, 100))))
    renderApp('/pokemon/bulbasaur')

    expect(await screen.findByText('Saved locally')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Save to my Pokedex' })).not.toBeInTheDocument()
  })

  it('surfaces conflicts returned by the server', async () => {
    signIn()
    mockDetail()
    server.use(
      http.get(`${API}/local/pokemon`, () => HttpResponse.json(page([], 0, 100))),
      http.post(`${API}/local/pokemon/bulbasaur/sync`, () =>
        HttpResponse.json({ detail: 'Pokemon with id 1 is already stored locally' }, { status: 409 }),
      ),
    )
    renderApp('/pokemon/bulbasaur')

    await userEvent.click(await screen.findByRole('button', { name: 'Save to my Pokedex' }))
    expect(await screen.findAllByText('Pokemon with id 1 is already stored locally')).not.toHaveLength(0)
  })
})
