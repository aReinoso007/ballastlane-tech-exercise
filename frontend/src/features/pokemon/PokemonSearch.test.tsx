import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { page, pokemon, summary } from '../../test/fixtures'
import { renderApp } from '../../test/render'
import { API, server } from '../../test/server'

const pikachu = { id: 25, name: 'pikachu', spriteUrl: 'https://img/25.png' }
const pichu = { id: 172, name: 'pichu', spriteUrl: 'https://img/172.png' }

function mockSearch(handler: (q: string) => unknown[]) {
  const seen: string[] = []
  server.use(
    http.get(`${API}/pokemon/search`, ({ request }) => {
      const q = new URL(request.url).searchParams.get('q') ?? ''
      seen.push(q)
      return HttpResponse.json(handler(q))
    }),
    http.get(`${API}/pokemon`, () => HttpResponse.json(page([summary()]))),
    http.get(`${API}/pokemon/pikachu`, () => HttpResponse.json(pokemon({ id: 25, name: 'pikachu', evolutions: [] }))),
    http.get(`${API}/pokemon/pichu`, () => HttpResponse.json(pokemon({ id: 172, name: 'pichu', evolutions: [] }))),
  )
  return seen
}

const searchBox = () => screen.getByRole('combobox', { name: 'Search Pokemon by name or number' })

describe('PokemonSearch (header)', () => {
  it('is available on every page, not only the list', async () => {
    mockSearch(() => [])
    renderApp('/login')
    expect(searchBox()).toBeInTheDocument()
  })

  it('suggests close matches while typing, even with a typo', async () => {
    const seen = mockSearch((q) => (q === 'pikachuu' ? [pikachu] : []))
    renderApp('/')

    await userEvent.type(searchBox(), 'pikachuu')

    const option = await screen.findByRole('option', { name: /pikachu/i })
    expect(option).toHaveTextContent('#0025')
    expect(seen).toEqual(['pikachuu']) // debounced: one request, not one per keystroke
  })

  it('opens the chosen suggestion', async () => {
    mockSearch(() => [pikachu, pichu])
    renderApp('/')
    await userEvent.type(searchBox(), 'pi')

    await userEvent.click(await screen.findByRole('option', { name: /pichu/i }))

    expect(await screen.findByRole('heading', { name: 'Pichu' })).toBeInTheDocument()
    expect(searchBox()).toHaveValue('')
  })

  it('supports the keyboard: arrows select, Enter opens', async () => {
    mockSearch(() => [pikachu, pichu])
    renderApp('/')
    await userEvent.type(searchBox(), 'pi')
    await screen.findAllByRole('option')

    await userEvent.keyboard('{ArrowDown}{ArrowDown}{Enter}')

    expect(await screen.findByRole('heading', { name: 'Pichu' })).toBeInTheDocument()
  })

  it('pressing Enter on a misspelled name opens the closest match', async () => {
    mockSearch((q) => (q === 'pikachuu' ? [pikachu] : []))
    renderApp('/')

    await userEvent.type(searchBox(), 'pikachuu{Enter}')

    expect(await screen.findByRole('heading', { name: 'Pikachu' })).toBeInTheDocument()
  })

  it('says so when nothing is close', async () => {
    mockSearch(() => [])
    renderApp('/')

    await userEvent.type(searchBox(), 'xqzwv{Enter}')

    expect(await screen.findByText('No Pokemon found for “xqzwv”')).toBeInTheDocument()
  })

  it('still opens an exact name if the suggestion service fails', async () => {
    server.use(
      http.get(`${API}/pokemon/search`, () => HttpResponse.json({ detail: 'down' }, { status: 502 })),
      http.get(`${API}/pokemon`, () => HttpResponse.json(page([summary()]))),
      http.get(`${API}/pokemon/pikachu`, () => HttpResponse.json(pokemon({ id: 25, name: 'pikachu', evolutions: [] }))),
    )
    renderApp('/')

    await userEvent.type(searchBox(), 'Pikachu{Enter}')

    expect(await screen.findByRole('heading', { name: 'Pikachu' })).toBeInTheDocument()
  })

  it('closes the list with Escape', async () => {
    mockSearch(() => [pikachu])
    renderApp('/')
    await userEvent.type(searchBox(), 'pika')
    await screen.findByRole('option')

    await userEvent.keyboard('{Escape}')

    await waitFor(() => expect(screen.queryByRole('listbox')).not.toBeInTheDocument())
  })
})
