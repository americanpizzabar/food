import Link from 'next/link'
import { Camera, Sparkles, ScanSearch, BookOpen, ShoppingCart, Package, Calendar } from 'lucide-react'

const features = [
  {
    href: '/photo-analysis',
    icon: Camera,
    title: '料理写真解析',
    desc: '写真からレシピ・カロリーを自動生成',
    gradient: 'linear-gradient(135deg, #f97316, #ef4444)',
    glow: 'rgba(249,115,22,.25)',
  },
  {
    href: '/menu-analysis',
    icon: ScanSearch,
    title: 'メニュー解析',
    desc: 'レストランメニューをプロ級レシピに変換',
    gradient: 'linear-gradient(135deg, #a855f7, #6366f1)',
    glow: 'rgba(168,85,247,.22)',
    badge: 'NEW',
  },
  {
    href: '/recipe-suggestion',
    icon: Sparkles,
    title: 'AIレシピ提案',
    desc: '気分・体調・在庫から最適なレシピを提案',
    gradient: 'linear-gradient(135deg, #e8b84b, #d4932a)',
    glow: 'rgba(232,184,75,.25)',
  },
  {
    href: '/journal',
    icon: BookOpen,
    title: '食事日記',
    desc: '料理の評価・感想・体調を記録',
    gradient: 'linear-gradient(135deg, #10b981, #0d9488)',
    glow: 'rgba(16,185,129,.22)',
  },
  {
    href: '/shopping',
    icon: ShoppingCart,
    title: '買い物リスト',
    desc: 'レシピから自動生成・オフライン対応',
    gradient: 'linear-gradient(135deg, #3b82f6, #06b6d4)',
    glow: 'rgba(59,130,246,.22)',
  },
  {
    href: '/pantry',
    icon: Package,
    title: 'パントリー管理',
    desc: '在庫・賞味期限を管理して使い切り',
    gradient: 'linear-gradient(135deg, #f43f5e, #e11d48)',
    glow: 'rgba(244,63,94,.22)',
  },
  {
    href: '/calendar',
    icon: Calendar,
    title: '献立カレンダー',
    desc: '週間の食事プランを計画・管理',
    gradient: 'linear-gradient(135deg, #8b5cf6, #a855f7)',
    glow: 'rgba(139,92,246,.22)',
  },
]

export default function Home() {
  return (
    <div className="space-y-8 animate-fade-in">

      {/* ── Hero ── */}
      <div className="relative overflow-hidden rounded-3xl p-7"
        style={{
          background: 'linear-gradient(145deg, #16140f 0%, #0e0c0a 50%, #120f0a 100%)',
          border: '1px solid rgba(232,184,75,.12)',
          boxShadow: '0 0 0 1px rgba(255,255,255,.04) inset, 0 8px 40px rgba(0,0,0,.6)',
        }}>
        {/* Orbs */}
        <div className="absolute -top-16 -right-12 w-64 h-64 rounded-full pointer-events-none"
          style={{ background: 'radial-gradient(circle, rgba(232,184,75,.12) 0%, transparent 70%)' }} />
        <div className="absolute -bottom-16 -left-10 w-52 h-52 rounded-full pointer-events-none"
          style={{ background: 'radial-gradient(circle, rgba(180,90,20,.10) 0%, transparent 70%)' }} />
        <div className="absolute top-1/3 left-1/3 w-36 h-36 rounded-full pointer-events-none"
          style={{ background: 'radial-gradient(circle, rgba(232,184,75,.05) 0%, transparent 70%)' }} />

        <div className="relative">
          {/* Badge */}
          <div className="inline-flex items-center gap-2 rounded-full px-3 py-1 text-[11px] font-semibold mb-5"
            style={{
              background: 'rgba(232,184,75,.10)',
              border: '1px solid rgba(232,184,75,.2)',
              color: '#e8b84b',
            }}>
            <span className="w-1.5 h-1.5 rounded-full bg-[#e8b84b] animate-pulse" />
            Powered by Google Gemini 2.5 Flash
          </div>

          {/* Title */}
          <h1 className="text-4xl font-black tracking-tight mb-1 leading-none">
            <span className="text-[#ececec]">Food</span>
            <span style={{
              background: 'linear-gradient(90deg, #e8b84b 0%, #f5d06e 50%, #d4932a 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
            }}>AI</span>
          </h1>
          <p className="text-sm mb-4" style={{ color: '#666' }}>
            AI搭載のプレミアム料理アシスタント
          </p>

          {/* Divider */}
          <div className="w-12 h-px mb-4" style={{ background: 'linear-gradient(90deg, #e8b84b, transparent)' }} />

          <p className="text-sm leading-relaxed" style={{ color: '#888' }}>
            写真解析・レシピ提案・献立計画・<br />
            買い物管理を一括でサポート。
          </p>
        </div>
      </div>

      {/* ── Feature grid ── */}
      <div>
        <p className="text-[10px] font-bold uppercase tracking-[.15em] mb-4"
          style={{ color: '#444' }}>
          機能一覧
        </p>
        <div className="grid grid-cols-2 gap-3">
          {features.map(({ href, icon: Icon, title, desc, gradient, glow, badge }) => (
            <Link key={href} href={href} className="group card-hover">
              <div className="rounded-3xl p-4 h-full"
                style={{
                  background: 'var(--surface)',
                  border: '1px solid var(--border)',
                  boxShadow: '0 4px 24px rgba(0,0,0,.35)',
                  transition: 'border-color .2s, box-shadow .2s, transform .2s',
                }}>
                {/* Icon */}
                <div className="w-12 h-12 rounded-2xl flex items-center justify-center mb-3.5 transition-transform duration-200 group-hover:scale-105"
                  style={{
                    background: gradient,
                    boxShadow: `0 4px 16px ${glow}`,
                  }}>
                  <Icon size={21} color="white" strokeWidth={1.8} />
                </div>

                {/* Title */}
                <div className="flex items-center gap-1.5 mb-1 flex-wrap">
                  <span className="text-[13px] font-bold leading-tight" style={{ color: '#dedede' }}>
                    {title}
                  </span>
                  {badge && (
                    <span className="badge text-[9px]"
                      style={{
                        background: 'rgba(168,85,247,.15)',
                        color: '#c084fc',
                        border: '1px solid rgba(168,85,247,.2)',
                      }}>
                      {badge}
                    </span>
                  )}
                </div>

                {/* Desc */}
                <p className="text-[11px] leading-relaxed" style={{ color: '#666' }}>
                  {desc}
                </p>
              </div>
            </Link>
          ))}
        </div>
      </div>

    </div>
  )
}
