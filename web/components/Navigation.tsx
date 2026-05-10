'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import {
  Home, Camera, Sparkles, BookOpen,
  ShoppingCart, Package, Calendar, ScanSearch, User,
} from 'lucide-react'

const NAV_ITEMS = [
  { href: '/',                  label: 'ホーム',   icon: Home },
  { href: '/photo-analysis',    label: '写真',     icon: Camera },
  { href: '/menu-analysis',     label: 'メニュー', icon: ScanSearch },
  { href: '/recipe-suggestion', label: '提案',     icon: Sparkles },
  { href: '/journal',           label: '日記',     icon: BookOpen },
  { href: '/shopping',          label: '買い物',   icon: ShoppingCart },
  { href: '/pantry',            label: '在庫',     icon: Package },
  { href: '/calendar',          label: '予定',     icon: Calendar },
]

export default function Navigation() {
  const pathname = usePathname()

  return (
    <>
      {/* ── Top header ── */}
      <header className="sticky top-0 z-40 backdrop-blur-2xl border-b"
        style={{ background: 'rgba(8,8,9,.85)', borderColor: 'rgba(255,255,255,.06)' }}>
        <div className="max-w-2xl mx-auto px-4 h-14 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2.5 group">
            {/* Logo mark */}
            <div className="w-8 h-8 rounded-xl flex items-center justify-center text-sm"
              style={{
                background: 'linear-gradient(135deg, #e8b84b 0%, #d4932a 100%)',
                boxShadow: '0 2px 12px rgba(232,184,75,.35)',
              }}>
              🍽️
            </div>
            {/* Wordmark */}
            <span className="text-lg font-bold tracking-tight">
              <span className="text-[#ececec]">Food</span>
              <span style={{ background: 'linear-gradient(90deg, #e8b84b, #f5d06e)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                AI
              </span>
            </span>
          </Link>

          <Link href="/profile"
            className="w-9 h-9 rounded-xl flex items-center justify-center transition-colors"
            style={{
              background: pathname === '/profile' ? 'rgba(232,184,75,.12)' : undefined,
              color: pathname === '/profile' ? '#e8b84b' : '#777777',
            }}>
            <User size={18} />
          </Link>
        </div>
      </header>

      {/* ── Bottom nav ── */}
      <nav className="fixed bottom-0 inset-x-0 z-40 backdrop-blur-2xl border-t"
        style={{ background: 'rgba(10,10,12,.88)', borderColor: 'rgba(255,255,255,.06)' }}>
        <div className="max-w-2xl mx-auto flex">
          {NAV_ITEMS.map(({ href, label, icon: Icon }) => {
            const active = pathname === href
            return (
              <Link key={href} href={href}
                className="flex flex-col items-center justify-center gap-[3px] py-2.5 flex-1 group">
                {/* Icon pill */}
                <div className="flex items-center justify-center w-9 h-[30px] rounded-[10px] transition-all duration-200"
                  style={active ? {
                    background: 'linear-gradient(135deg, #e8b84b 0%, #d4932a 100%)',
                    boxShadow: '0 2px 12px rgba(232,184,75,.4)',
                    transform: 'scale(1.05)',
                  } : {}}>
                  <Icon
                    size={17}
                    strokeWidth={active ? 2.5 : 1.8}
                    style={{ color: active ? '#0a0808' : '#555' }}
                    className="group-hover:!text-[#888] transition-colors"
                  />
                </div>
                <span className="text-[9px] font-semibold leading-none transition-colors"
                  style={{ color: active ? '#e8b84b' : '#555' }}>
                  {label}
                </span>
              </Link>
            )
          })}
        </div>
      </nav>
    </>
  )
}
