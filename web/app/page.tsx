import Link from 'next/link'
import { Camera, Sparkles, ScanSearch, BookOpen, ShoppingCart, Package, Calendar } from 'lucide-react'

const features = [
  {
    href: '/photo-analysis',
    icon: Camera,
    title: '料理写真解析',
    desc: '写真からレシピ・カロリーを自動生成',
    gradient: 'from-orange-400 to-rose-500',
  },
  {
    href: '/menu-analysis',
    icon: ScanSearch,
    title: 'メニュー解析',
    desc: 'レストランメニューをプロ級レシピに変換',
    gradient: 'from-violet-500 to-indigo-600',
    badge: 'NEW',
  },
  {
    href: '/recipe-suggestion',
    icon: Sparkles,
    title: 'AIレシピ提案',
    desc: '気分・体調・在庫から最適なレシピを提案',
    gradient: 'from-amber-400 to-orange-500',
  },
  {
    href: '/journal',
    icon: BookOpen,
    title: '食事日記',
    desc: '料理の評価・感想・体調を記録',
    gradient: 'from-emerald-400 to-teal-500',
  },
  {
    href: '/shopping',
    icon: ShoppingCart,
    title: '買い物リスト',
    desc: 'レシピから自動生成・オフライン対応',
    gradient: 'from-sky-400 to-blue-500',
  },
  {
    href: '/pantry',
    icon: Package,
    title: 'パントリー管理',
    desc: '在庫・賞味期限を管理して使い切り',
    gradient: 'from-rose-400 to-pink-600',
  },
  {
    href: '/calendar',
    icon: Calendar,
    title: '献立カレンダー',
    desc: '週間の食事プランを計画・管理',
    gradient: 'from-indigo-400 to-violet-500',
  },
]

export default function Home() {
  return (
    <div className="space-y-6 animate-fade-in">

      {/* ── Hero ── */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-gray-900 via-gray-800 to-[#3a1a0a] p-6 text-white min-h-[164px] flex flex-col justify-between">
        {/* Orbs */}
        <div className="absolute -top-10 -right-10 w-48 h-48 bg-orange-500/20 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-12 -left-8 w-40 h-40 bg-orange-700/20 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute top-1/2 right-4 w-24 h-24 bg-rose-500/10 rounded-full blur-2xl pointer-events-none" />

        <div className="relative">
          <div className="inline-flex items-center gap-2 bg-white/10 backdrop-blur-sm rounded-full px-3 py-1 text-[11px] font-medium mb-4">
            <span className="w-1.5 h-1.5 rounded-full bg-green-400 animate-pulse" />
            Powered by Google Gemini 2.5 Flash
          </div>
          <h1 className="text-3xl font-bold tracking-tight">
            Food<span className="text-orange-400">AI</span>
          </h1>
          <p className="text-white/60 text-sm mt-1.5 leading-relaxed">
            AI搭載の料理アシスタント。<br />
            写真解析・献立計画・買い物管理を一括サポート。
          </p>
        </div>
      </div>

      {/* ── Feature grid ── */}
      <div>
        <p className="text-[11px] font-semibold text-gray-400 uppercase tracking-widest mb-3">機能一覧</p>
        <div className="grid grid-cols-2 gap-3">
          {features.map(({ href, icon: Icon, title, desc, gradient, badge }) => (
            <Link key={href} href={href} className="group">
              <div className="card hover:shadow-[0_4px_28px_rgba(0,0,0,.10)] hover:-translate-y-0.5 transition-all duration-200 h-full">
                <div className={`w-11 h-11 rounded-2xl bg-gradient-to-br ${gradient} flex items-center justify-center mb-3 shadow-sm group-hover:scale-105 transition-transform duration-200`}>
                  <Icon size={20} className="text-white" strokeWidth={1.8} />
                </div>
                <div className="flex items-center gap-1.5 mb-1 flex-wrap">
                  <span className="font-semibold text-gray-900 text-[13px] leading-tight">{title}</span>
                  {badge && (
                    <span className="badge bg-violet-100 text-violet-600" style={{ fontSize: '9px' }}>{badge}</span>
                  )}
                </div>
                <p className="text-[11px] text-gray-500 leading-relaxed">{desc}</p>
              </div>
            </Link>
          ))}
        </div>
      </div>

    </div>
  )
}
