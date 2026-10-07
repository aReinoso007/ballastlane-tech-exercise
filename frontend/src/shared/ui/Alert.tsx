import type { ReactNode } from 'react'

const TONES = {
  error: 'bg-red-50 text-red-800 ring-red-200',
  success: 'bg-green-50 text-green-800 ring-green-200',
  info: 'bg-sky-50 text-sky-800 ring-sky-200',
}

export function Alert({ tone = 'info', children }: { tone?: keyof typeof TONES; children: ReactNode }) {
  return (
    <div role={tone === 'error' ? 'alert' : 'status'} className={`rounded-lg px-4 py-3 text-sm ring-1 ${TONES[tone]}`}>
      {children}
    </div>
  )
}
