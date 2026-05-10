import type { Metadata, Viewport } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'
import Navigation from '@/components/Navigation'

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-sans',
  display: 'swap',
})

export const metadata: Metadata = {
  title: 'FoodAI - AI料理アシスタント',
  description: 'AI搭載の料理管理アプリ。写真解析、レシピ提案、献立計画、買い物リストを一括管理。',
}

export const viewport: Viewport = {
  themeColor: '#f97316',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ja" className={inter.variable}>
      <body className="font-sans antialiased">
        <div className="flex min-h-dvh flex-col">
          <Navigation />
          <main className="flex-1 max-w-2xl mx-auto w-full px-4 pt-5 pb-28">
            {children}
          </main>
        </div>
      </body>
    </html>
  )
}
