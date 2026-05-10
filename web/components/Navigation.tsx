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
      <header className="sticky top-0 z-40 bg-white/75 backdrop-blur-2xl border-b border-black/[0.06]">
        <div className="max-w-2xl mx-auto px-4 h-14 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2.5 group">
            <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-orange-400 to-orange-600 flex items-center justify-center shadow-sm group-hover:scale-105 transition-transform">
              <span className="text-sm leading-none">🍽️</span>
            </div>
            <span className="text-lg font-bold tracking-tight text-gray-900">
              Food<span className="text-orange-500">AI</span>
            </span>
          </Link>
          <Link
            href="/profile"
            className={`w-9 h-9 rounded-xl flex items-center justify-center transition-colors ${
              pathname === '/profile'
                ? 'bg-orange-50 text-orange-500'
                : 'hover:bg-gray-100 text-gray-500'
            }`}
          >
            <User size={18} />
          </Link>
        </div>
      </header>

      {/* ── Bottom nav ── */}
      <nav className="fixed bottom-0 inset-x-0 z-40 bg-white/80 backdrop-blur-2xl border-t border-black/[0.06]">
        <div className="max-w-2xl mx-auto flex">
          {NAV_ITEMS.map(({ href, label, icon: Icon }) => {
            const active = pathname === href
            return (
              <Link
                key={href}
                href={href}
                className="flex flex-col items-center justify-center gap-[3px] py-2.5 flex-1 group"
              >
                <div className={`relative flex items-center justify-center w-9 h-[30px] rounded-[10px] transition-all duration-200 ${
                  active
                    ? 'bg-gradient-to-b from-orange-500 to-orange-600 shadow-[0_2px_10px_rgba(249,115,22,.4)] scale-105'
                    : 'group-hover:bg-gray-100'
                }`}>
                  <Icon
                    size={17}
                    strokeWidth={active ? 2.5 : 1.8}
                    className={active ? 'text-white' : 'text-gray-400 group-hover:text-gray-600'}
                  />
                </div>
                <span className={`text-[9px] font-semibold leading-none transition-colors ${
                  active ? 'text-orange-500' : 'text-gray-400 group-hover:text-gray-500'
                }`}>
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
