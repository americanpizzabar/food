'use client'

import { useEffect } from 'react'
import { AlertTriangle, RefreshCw, Home } from 'lucide-react'
import Link from 'next/link'

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string }
  reset: () => void
}) {
  useEffect(() => {
    console.error('App error:', error)
  }, [error])

  return (
    <div className="space-y-5 animate-fade-in max-w-md mx-auto pt-8">
      <div className="card space-y-4"
        style={{ background: 'rgba(239,68,68,.06)', border: '1px solid rgba(239,68,68,.2)' }}>
        <div className="flex items-center gap-2">
          <AlertTriangle size={20} className="text-red-400" />
          <h1 className="text-lg font-bold text-red-400">エラーが発生しました</h1>
        </div>
        <p className="text-sm text-[#bbb] leading-relaxed">
          ページの表示中に問題が起きました。再読み込みをお試しください。
        </p>
        {error.message && (
          <div className="rounded-lg p-3 text-xs font-mono break-words"
            style={{ background: 'var(--surface-2)', color: '#888', border: '1px solid rgba(255,255,255,.05)' }}>
            {error.message}
          </div>
        )}
        <div className="flex gap-2 flex-wrap">
          <button onClick={reset} className="btn-primary text-sm">
            <RefreshCw size={16} />再試行
          </button>
          <Link href="/" className="btn-secondary text-sm">
            <Home size={16} />ホームへ
          </Link>
        </div>
      </div>
    </div>
  )
}
