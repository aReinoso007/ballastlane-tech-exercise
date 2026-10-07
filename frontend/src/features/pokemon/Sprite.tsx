export function Sprite({ src, alt, className = '' }: { src: string | null; alt: string; className?: string }) {
  if (!src) {
    return (
      <div role="img" aria-label={`${alt} (no image available)`}
        className={`flex items-center justify-center rounded-full bg-slate-100 text-slate-400 ${className}`}>
        ?
      </div>
    )
  }
  return <img src={src} alt={alt} loading="lazy" className={`object-contain ${className}`} />
}
