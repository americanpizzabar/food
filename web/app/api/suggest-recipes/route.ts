import { NextRequest, NextResponse } from 'next/server'
import { suggestRecipes, extractJson } from '@/lib/gemini'

export async function POST(req: NextRequest) {
  try {
    const body = await req.json()
    const raw = await suggestRecipes(body)
    const json = extractJson(raw)
    const recipes = JSON.parse(json)
    return NextResponse.json({ recipes: Array.isArray(recipes) ? recipes : [recipes] })
  } catch (e: unknown) {
    const message = e instanceof Error ? e.message : '提案に失敗しました'
    return NextResponse.json({ error: message }, { status: 500 })
  }
}
