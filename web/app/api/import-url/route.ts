import { NextRequest, NextResponse } from 'next/server'
import { importRecipeFromUrl, extractJson } from '@/lib/gemini'

function validatePublicUrl(raw: string): URL {
  let parsed: URL
  try {
    parsed = new URL(raw)
  } catch {
    throw new Error('無効なURLです')
  }
  if (!['http:', 'https:'].includes(parsed.protocol)) {
    throw new Error('HTTPまたはHTTPSのURLのみ対応しています')
  }
  const h = parsed.hostname
  if (
    h === 'localhost' ||
    h === '127.0.0.1' ||
    h === '::1' ||
    /^10\./.test(h) ||
    /^192\.168\./.test(h) ||
    /^172\.(1[6-9]|2\d|3[01])\./.test(h) ||
    /^169\.254\./.test(h) ||
    h.endsWith('.internal') ||
    h.endsWith('.local')
  ) {
    throw new Error('このURLにはアクセスできません')
  }
  return parsed
}

export async function POST(req: NextRequest) {
  try {
    const { url, apiKey } = await req.json()
    if (!url) return NextResponse.json({ error: 'URLが必要です' }, { status: 400 })

    const parsedUrl = validatePublicUrl(url)

    // Fetch page content server-side
    const res = await fetch(parsedUrl.toString(), {
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
