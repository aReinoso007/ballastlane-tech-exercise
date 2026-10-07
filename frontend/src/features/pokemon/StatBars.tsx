import { statLabel } from '../../shared/lib/format'
import type { Stat } from './types'

const MAX_STAT = 255

export function StatBars({ stats }: { stats: Stat[] }) {
  return (
    <dl className="space-y-2">
      {stats.map((stat) => (
        <div key={stat.name} className="grid grid-cols-[6rem_2.5rem_1fr] items-center gap-2 text-sm">
          <dt className="text-slate-600">{statLabel(stat.name)}</dt>
          <dd className="font-semibold">{stat.baseStat}</dd>
          <div className="h-2 overflow-hidden rounded-full bg-slate-200" aria-hidden="true">
            <div className="h-full rounded-full bg-brand" style={{ width: `${Math.min(100, (stat.baseStat / MAX_STAT) * 100)}%` }} />
          </div>
        </div>
      ))}
    </dl>
  )
}
