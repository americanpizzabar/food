import { NextRequest, NextResponse } from 'next/server'
import { analyzeFoodPhoto, extractJson } from '@/lib/gemini'

export async function POST(req: NextRequest) {
  try {
    const { imageDataUrl, apiKey } = await req.json()
    if (!imageDataUrl) return NextResponse.json({ error: '画像が必要です' }, { status: 400 })

    const raw = await analyzeFoodPhoto(imageDataUrl, apiKey)
    const json = extractJson(raw)
    const recipe = JSON.parse(json)
    return NextResponse.json({ recipe })
  } catch (e: unknown) {
    const message = e instanceof Error ? e.message : '解析に失敗しました'
    return NextResponse.json({ error: message }, { status: 500 })
  }
}
