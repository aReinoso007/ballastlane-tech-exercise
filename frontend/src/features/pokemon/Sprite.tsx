import { useState } from 'react'

interface Props {
  src: string | null
  alt: string
  className?: string
}

export function Sprite({ src, alt, className = '' }: Props) {
  // Remember which URL finished loading so a changed `src` fades in again instead of
  // showing an empty box while the new image downloads.
  const [loadedSrc, setLoadedSrc] = useState<string | null>(null)

  if (!src) {
    return (
      <div
        role="img"
        aria-label={`${alt} (no image available)`}
        className={`flex items-center justify-center rounded-full bg-slate-100 text-slate-400 ${className}`}
      >
        ?
      </div>
    )
  }

  const loaded = loadedSrc === src
  return (
    <div className={`relative ${className} ${loaded ? '' : 'rounded-full bg-slate-100'}`}>
      <img
        key={src}
        src={src}
        alt={alt}
        loading="lazy"
        decoding="async"
        ref={(img) => {
          if (img?.complete && img.naturalWidth > 0) setLoadedSrc(src)
        }}
        onLoad={() => setLoadedSrc(src)}
        onError={() => setLoadedSrc(src)}
        className={`h-full w-full object-contain transition-opacity duration-200 ${loaded ? 'opacity-100' : 'opacity-0'}`}
      />
    </div>
  )
}
