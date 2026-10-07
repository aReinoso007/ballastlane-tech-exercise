import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { page, summary } from '../../test/fixtures'
import { renderApp } from '../../test/render'
import { API, server } from '../../test/server'

describe('PokemonListPage (US01)', () => {
  it('shows sprite, category, weight and abilities for each Pokemon', async () => {
    server.use(
      http.get(`${API}/pokemon`, () =>
        HttpResponse.json(page([summary(), summary({ id: 4, name: 'charmander', category: 'Lizard Pokémon', weight: 85, abilities: ['blaze'] })], 0, 12, 24)),
      ),
    )
    renderApp('/')

    const card = (await screen.findByRole('link', { name: /bulbasaur/i }))
    expect(within(card).getByRole('img', { name: 'Bulbasaur' })).toHaveAttribute('src', 'https://img/1.png')
    expect(within(card).getByText('Seed Pokémon')).toBeInTheDocument()
    expect(within(card).getByText('6.9 kg')).toBeInTheDocument()
    expect(within(card).getByText('Overgrow')).toBeInTheDocument()
    expect(within(card).getByText('Chlorophyll')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /charmander/i })).toBeInTheDocument()
    expect(screen.getByText('Page 1 of 2')).toBeInTheDocument()
  })

  it('requests the next page when clicking Next', async () => {
    const requested: string[] = []
    server.use(
      http.get(`${API}/pokemon`, ({ request }) => {
        const pageIndex = Number(new URL(request.url).searchParams.get('page'))
        requested.push(String(pageIndex))
        return HttpResponse.json(
          page([summary({ id: pageIndex + 1, name: pageIndex === 0 ? 'bulbasaur' : 'ivysaur' })], pageIndex, 12, 24),
        )
      }),
    )
    renderApp('/')
    await screen.findByRole('link', { name: /bulbasaur/i })

    await userEvent.click(screen.getByRole('button', { name: 'Next' }))

    expect(await screen.findByRole('link', { name: /ivysaur/i })).toBeInTheDocument()
    expect(screen.getByText('Page 2 of 2')).toBeInTheDocument()
    expect(requested).toEqual(['0', '1'])
  })

  it('opens the requested page from the URL', async () => {
    let seen = ''
    server.use(
      http.get(`${API}/pokemon`, ({ request }) => {
        seen = new URL(request.url).searchParams.get('page') ?? ''
        return HttpResponse.json(page([summary()], 2, 12, 36))
      }),
    )
    renderApp('/?page=3')
    await screen.findByRole('link', { name: /bulbasaur/i })
    expect(seen).toBe('2')
    expect(screen.getByRole('button', { name: 'Next' })).toBeDisabled()
  })

  it('navigates to a Pokemon via the search box', async () => {
    server.use(
      http.get(`${API}/pokemon`, () => HttpResponse.json(page([summary()]))),
      http.get(`${API}/pokemon/pikachu`, () => HttpResponse.json({ ...summary({ id: 25, name: 'pikachu' }), stats: [], evolutions: [], height: 4, description: null, localizedName: null, region: null, tags: [] })),
    )
    renderApp('/')
    await userEvent.type(screen.getByLabelText('Find by name or number'), 'Pikachu')
    await userEvent.click(screen.getByRole('button', { name: 'Go' }))

    expect(await screen.findByRole('heading', { name: 'Pikachu' })).toBeInTheDocument()
  })

  it('shows an error with a retry action when the API fails', async () => {
    let calls = 0
    server.use(
      http.get(`${API}/pokemon`, () => {
        calls++
        return calls === 1
          ? HttpResponse.json({ detail: 'The Pokemon catalog (PokeAPI) is currently unavailable.' }, { status: 502 })
          : HttpResponse.json(page([summary()]))
      }),
    )
    renderApp('/')

    expect(await screen.findByRole('alert')).toHaveTextContent('currently unavailable')
    await userEvent.click(screen.getByRole('button', { name: 'Try again' }))
    expect(await screen.findByRole('link', { name: /bulbasaur/i })).toBeInTheDocument()
  })
})
