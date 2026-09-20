'use client'

import { useState } from 'react'
import { Share2, Copy, Check } from 'lucide-react'

interface ShareButtonProps {
  title: string
  text: string
  url?: string
  className?: string
  variant?: 'primary' | 'secondary'
  size?: 'sm' | 'md'
  label?: string
}

export default function ShareButton({
  title,
  text,
  url,
  className,
  variant = 'secondary',
  size = 'md',
  label = '共有',
}: ShareButtonProps) {
  const [copied, setCopied] = useState(false)
  const [sharing, setSharing] = useState(false)

  const share = async () => {
    if (sharing) return
    setSharing(true)
    const shareData: ShareData = { title, text }
    if (url) shareData.url = url

    const nav = typeof navigator !== 'undefined' ? navigator : undefined
    const canShare = !!nav?.share
    if (canShare) {
      try {
        await nav.share(shareData)
        setSharing(false)
        return
      } catch (e) {
        if (e instanceof Error && e.name === 'AbortError') {
          setSharing(false)
          return
        }
      }
    }

    try {
      const body = url ? `${title}\n\n${text}\n\n${url}` : `${title}\n\n${text}`
      if (nav?.clipboard?.writeText) {
        await nav.clipboard.writeText(body)
      } else {
        const ta = document.createElement('textarea')
        ta.value = body
        ta.style.position = 'fixed'
        ta.style.opacity = '0'
        document.body.appendChild(ta)
        ta.focus()
        ta.select()
        document.execCommand('copy')
        document.body.removeChild(ta)
      }
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {
      // silent
    } finally {
      setSharing(false)
    }
  }

  const base = variant === 'primary' ? 'btn-primary' : 'btn-secondary'
  const sz = size === 'sm' ? 'text-xs' : 'text-sm'

  return (
    <button
      onClick={share}
      disabled={sharing}
      className={`${base} ${sz} ${className || ''}`}
      aria-label={label}
      title="LINE・メッセージなどで共有"
    >
      {copied
        ? <><Check size={size === 'sm' ? 14 : 16} />コピー済み</>
        : <><Share2 size={size === 'sm' ? 14 : 16} />{label}</>}
    </button>
  )
}
