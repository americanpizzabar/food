import { NextRequest, NextResponse } from 'next/server'
import { importRecipeFromUrl, extractJson } from '@/lib/gemini'

export async function POST(req: NextRequest) {
  try {
    const { url, apiKey } = await req.json()
    if (!url) return NextResponse.json({ error: 'URLが必要です' }, { status: 400 })

    // Fetch page content server-side
    const res = await fetch(url, {
      headers: { 'User-Agent': 'Mozilla/5.0 FoodAI/1.0' },
      signal: AbortSignal.timeout(10000)
    })
    if (!res.ok) throw new Error(`ページの取得に失敗しました: ${res.status}`)
    const html = await res.text()
    // Strip HTML tags for text extraction
    const text = html.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()

    const raw = await importRecipeFromUrl(url, text, apiKey)
    const json = extractJson(raw)
    const recipe = JSON.parse(json)
    return NextResponse.json({ recipe: { ...recipe, sourceUrl: url } })
  } catch (e: unknown) {
    const message = e instanceof Error ? e.message : 'インポートに失敗しました'
    return NextResponse.json({ error: message }, { status: 500 })
  }
}
