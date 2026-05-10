'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { Home, Camera, Sparkles, BookOpen, ShoppingCart, Package, Calendar, ScanSearch, User } from 'lucide-react'

const NAV_ITEMS = [
  { href: '/',                 label: 'ホーム',     icon: Home },
  { href: '/photo-analysis',   label: '写真解析',   icon: Camera },
  { href: '/menu-analysis',    label: 'メニュー',   icon: ScanSearch },
  { href: '/recipe-suggestion',label: '提案',       icon: Sparkles },
  { href: '/journal',          label: '日記',       icon: BookOpen },
  { href: '/shopping',         label: '買い物',     icon: ShoppingCart },
  { href: '/pantry',           label: '在庫',       icon: Package },
  { href: '/calendar',         label: 'カレンダー', icon: Calendar },
  { href: '/profile',          label: '設定',       icon: User },
]

export default function Navigation() {
  const pathname = usePathname()

  return (
    <>
      {/* Top header */}
      <header className="sticky top-0 z-40 bg-white/80 backdrop-blur border-b border-gray-100">
        <div className="max-w-2xl mx-auto px-4 h-14 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2">
            <span className="text-2xl">🍽️</span>
            <span className="font-bold text-gray-800 text-lg">FoodAI</span>
          </Link>
          <Link href="/profile" className="p-2 rounded-xl hover:bg-gray-100 transition-colors">
            <User size={20} className="text-gray-600" />
          </Link>
        </div>
      </header>

      {/* Bottom nav */}
      <nav className="fixed bottom-0 inset-x-0 z-40 bg-white/90 backdrop-blur border-t border-gray-100 pb-safe">
        <div className="max-w-2xl mx-auto px-2 flex justify-around">
          {NAV_ITEMS.slice(0, 8).map(({ href, label, icon: Icon }) => {
            const active = pathname === href
            return (
              <Link
                key={href}
                href={href}
                className={`flex flex-col items-center gap-0.5 py-2 px-1 min-w-0 flex-1 transition-colors ${
                  active ? 'text-orange-500' : 'text-gray-400 hover:text-gray-600'
                }`}
              >
                <Icon size={20} strokeWidth={active ? 2.5 : 1.8} />
                <span className="text-[10px] font-medium truncate">{label}</span>
              </Link>
            )
          })}
        </div>
      </nav>
    </>
  )
}
