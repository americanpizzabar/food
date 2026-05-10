import type { Metadata } from 'next'
import './globals.css'
import Navigation from '@/components/Navigation'

export const metadata: Metadata = {
  title: 'FoodAI - AI料理アシスタント',
  description: 'AI搭載の料理管理アプリ。写真解析、レシピ提案、献立計画、買い物リストを一括管理。',
  manifest: '/manifest.json',
  themeColor: '#f97316',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="ja">
      <body className="font-sans antialiased">
        <div className="flex min-h-screen flex-col">
          <Navigation />
          <main className="flex-1 max-w-2xl mx-auto w-full px-4 py-6 pb-24">
            {children}
          </main>
        </div>
      </body>
    </html>
  )
}
