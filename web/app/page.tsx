import Link from 'next/link'
import { Camera, Sparkles, ScanSearch, BookOpen, ShoppingCart, Package, Calendar } from 'lucide-react'

const features = [
  {
    href: '/photo-analysis',
    icon: Camera,
    emoji: '📸',
    title: '料理写真解析',
    desc: '写真から材料・手順・カロリーを自動生成',
    color: 'bg-orange-50 border-orange-200',
    iconColor: 'text-orange-500',
  },
  {
    href: '/menu-analysis',
    icon: ScanSearch,
    emoji: '🍽️',
    title: 'メニュー解析',
    desc: 'レストランメニューの料理をプロ級レシピに変換',
    color: 'bg-purple-50 border-purple-200',
    iconColor: 'text-purple-500',
    badge: 'NEW',
  },
  {
    href: '/recipe-suggestion',
    icon: Sparkles,
    emoji: '✨',
    title: 'AIレシピ提案',
    desc: '今の気分・体調・在庫からレシピを提案',
    color: 'bg-yellow-50 border-yellow-200',
    iconColor: 'text-yellow-500',
  },
  {
    href: '/journal',
    icon: BookOpen,
    emoji: '📔',
    title: '食事日記',
    desc: '料理の感想・評価・体調を記録',
    color: 'bg-green-50 border-green-200',
    iconColor: 'text-green-500',
  },
  {
    href: '/shopping',
    icon: ShoppingCart,
    emoji: '🛒',
    title: '買い物リスト',
    desc: 'レシピから自動生成・オフライン対応',
    color: 'bg-blue-50 border-blue-200',
    iconColor: 'text-blue-500',
  },
  {
    href: '/pantry',
    icon: Package,
    emoji: '📦',
    title: 'パントリー管理',
    desc: '在庫・賞味期限を管理して使い切り提案',
    color: 'bg-red-50 border-red-200',
    iconColor: 'text-red-400',
  },
  {
    href: '/calendar',
    icon: Calendar,
    emoji: '📅',
    title: '献立カレンダー',
    desc: '週間の食事プランを計画・管理',
    color: 'bg-indigo-50 border-indigo-200',
    iconColor: 'text-indigo-500',
  },
]

export default function Home() {
  return (
    <div className="space-y-6 animate-fade-in">
      {/* Hero */}
      <div className="card bg-gradient-to-br from-orange-500 to-orange-600 text-white border-0">
        <div className="flex items-center gap-3 mb-2">
          <span className="text-4xl">🍽️</span>
          <div>
            <h1 className="text-2xl font-bold">FoodAI</h1>
            <p className="text-orange-100 text-sm">AI搭載の料理アシスタント</p>
          </div>
        </div>
        <p className="text-orange-50 text-sm mt-3">
          写真解析・レシピ提案・献立計画・買い物管理を一括サポート。
          Google Gemini AIが料理をあらゆる角度からサポートします。
        </p>
      </div>

      {/* Feature grid */}
      <div className="grid grid-cols-1 gap-3">
        {features.map(({ href, emoji, title, desc, color, iconColor, badge }) => (
          <Link key={href} href={href}>
            <div className={`card border ${color} hover:shadow-md transition-all cursor-pointer flex items-center gap-4`}>
              <span className="text-3xl">{emoji}</span>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <span className="font-semibold text-gray-800">{title}</span>
                  {badge && (
                    <span className="badge bg-purple-100 text-purple-700">{badge}</span>
                  )}
                </div>
                <p className="text-sm text-gray-500 mt-0.5 truncate">{desc}</p>
              </div>
              <span className="text-gray-300 text-xl flex-shrink-0">›</span>
            </div>
          </Link>
        ))}
      </div>

      <p className="text-center text-xs text-gray-400">
        Powered by Google Gemini 2.0 Flash
      </p>
    </div>
  )
}
