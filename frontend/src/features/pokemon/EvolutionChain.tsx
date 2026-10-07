import { Link } from 'react-router-dom'
import { capitalize } from '../../shared/lib/format'
import { usePrefetchPokemon } from './hooks'
import { Sprite } from './Sprite'
import type { EvolutionStage } from './types'

interface Props {
  stages: EvolutionStage[]
  currentName: string
  /** Disable links (used for local records where the remote detail may not matter). */
  linkable?: boolean
}

export function EvolutionChain({ stages, currentName, linkable = true }: Props) {
  const prefetch = usePrefetchPokemon()
  if (stages.length <= 1) {
    return <p className="text-sm text-slate-500">This Pokemon does not evolve.</p>
  }
  const byStage = new Map<number, EvolutionStage[]>()
  stages.forEach((s) => byStage.set(s.stage, [...(byStage.get(s.stage) ?? []), s]))

  return (
    <ol className="flex flex-wrap items-center gap-2" aria-label="Evolution lineage">
      {[...byStage.entries()].map(([stage, members], index) => (
        <li key={stage} className="flex items-center gap-2">
          {index > 0 && <span aria-hidden="true" className="text-2xl text-slate-400">→</span>}
          <ul className="flex flex-col gap-2">
            {members.map((member) => {
              const isCurrent = member.name === currentName
              const body = (
                <>
                  <Sprite src={member.spriteUrl} alt={capitalize(member.name)} className="h-20 w-20" />
                  <span className="text-sm font-medium">{capitalize(member.name)}</span>
                </>
              )
              const classes = `flex flex-col items-center rounded-lg p-2 ring-1 ${isCurrent ? 'bg-red-50 ring-brand' : 'bg-white ring-slate-200'}`
              return (
                <li key={member.id}>
                  {linkable && !isCurrent ? (
                    <Link
                      to={`/pokemon/${member.name}`}
                      className={`${classes} hover:ring-brand`}
                      onMouseEnter={() => prefetch(member.name)}
                      onFocus={() => prefetch(member.name)}
                    >
                      {body}
                    </Link>
                  ) : (
                    <div className={classes} aria-current={isCurrent ? 'true' : undefined}>
                      {body}
                    </div>
                  )}
                </li>
              )
            })}
          </ul>
        </li>
      ))}
    </ol>
  )
}
