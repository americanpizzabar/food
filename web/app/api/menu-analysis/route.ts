import { NextRequest, NextResponse } from 'next/server'
import { analyzeMenuPhoto, generateDishCookingGuide, extractJson } from '@/lib/gemini'

// POST /api/menu-analysis?action=scan  — scan menu image → list of dishes
// POST /api/menu-analysis?action=guide — generate cooking guide for a dish
export async function POST(req: NextRequest) {
  const action = req.nextUrl.searchParams.get('action') ?? 'scan'

  try {
    if (action === 'scan') {
      const { imageDataUrl, apiKey } = await req.json()
      if (!imageDataUrl) return NextResponse.json({ error: '画像が必要です' }, { status: 400 })

      const raw = await analyzeMenuPhoto(imageDataUrl, apiKey)
      const json = extractJson(raw)
      const dishes = JSON.parse(json)
      return NextResponse.json({ dishes: Array.isArray(dishes) ? dishes : [] })
    }

    if (action === 'guide') {
      const { dishName, apiKey } = await req.json()
      if (!dishName) return NextResponse.json({ error: '料理名が必要です' }, { status: 400 })

      const raw = await generateDishCookingGuide(dishName, apiKey)
      const json = extractJson(raw)
      const guide = JSON.parse(json)
      return NextResponse.json({ guide })
    }

    return NextResponse.json({ error: '不明なアクション' }, { status: 400 })
  } catch (e: unknown) {
    const message = e instanceof Error ? e.message : '処理に失敗しました'
    return NextResponse.json({ error: message }, { status: 500 })
  }
}
