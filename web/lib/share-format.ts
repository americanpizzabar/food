import type { Recipe, DishCookingGuide, MenuDish } from './types'

export interface Shareable {
  title: string
  text: string
}

const line = (s: string | undefined | null): string => (s && s.trim() ? s.trim() : '')

export function formatMenuAnalysis(dishes: MenuDish[]): Shareable {
  const lines: string[] = ['📋 メニュー解析結果', '']
  const byCategory: Record<string, MenuDish[]> = {}
  for (const d of dishes) {
    const cat = line(d.category) || 'その他'
    byCategory[cat] ??= []
    byCategory[cat].push(d)
  }
  for (const [cat, items] of Object.entries(byCategory)) {
    lines.push(`■ ${cat}`)
    for (const d of items) {
      const price = line(d.price)
      lines.push(`・${d.name}${price ? ` (${price})` : ''}`)
      if (line(d.description)) lines.push(`  ${d.description}`)
    }
    lines.push('')
  }
  return { title: `メニュー解析 (${dishes.length}品)`, text: lines.join('\n').trimEnd() }
}

export function formatCookingGuide(g: DishCookingGuide): Shareable {
  const lines: string[] = [`👨‍🍳 プロ級レシピ: ${g.dishName}`, '']
  if (line(g.overview)) {
    lines.push('■ 概要')
    lines.push(g.overview.trim())
    lines.push('')
  }

  if (Array.isArray(g.ingredients) && g.ingredients.length) {
    lines.push('■ 最高品質の食材')
    for (const ing of g.ingredients) {
      const amt = [line(ing.amount), line(ing.unit)].filter(Boolean).join(' ')
      lines.push(`・${ing.name}${amt ? ` — ${amt}` : ''}`)
      if (line(ing.quality)) lines.push(`  品質: ${ing.quality}`)
      if (line(ing.whereToFind)) lines.push(`  入手: ${ing.whereToFind}`)
      if (line(ing.substitute)) lines.push(`  代替: ${ing.substitute}`)
      if (line(ing.notes)) lines.push(`  ${ing.notes}`)
    }
    lines.push('')
  }

  if (Array.isArray(g.techniques) && g.techniques.length) {
    lines.push('■ プロの調理技法')
    for (const t of g.techniques) {
      lines.push(`・${t.name}: ${line(t.description)}`)
      if (line(t.whyImportant)) lines.push(`  なぜ重要: ${t.whyImportant}`)
    }
    lines.push('')
  }

  if (Array.isArray(g.steps) && g.steps.length) {
    lines.push('■ 調理手順')
    for (const s of g.steps) {
      const meta = [line(s.duration), line(s.temperature)].filter(Boolean).join(' / ')
      lines.push(`${s.stepNumber}. ${line(s.title)}${meta ? ` (${meta})` : ''}`)
      if (line(s.description)) lines.push(`   ${s.description}`)
      if (Array.isArray(s.tips) && s.tips.length) {
        for (const tip of s.tips) lines.push(`   💡 ${tip}`)
      }
      if (line(s.warnings)) lines.push(`   ⚠️ ${s.warnings}`)
    }
    lines.push('')
  }

  if (Array.isArray(g.professionalTips) && g.professionalTips.length) {
    lines.push('■ シェフの秘訣')
    for (const t of g.professionalTips) lines.push(`✨ ${t}`)
    lines.push('')
  }

  if (line(g.platingGuide)) {
    lines.push('■ 盛り付けガイド')
    lines.push(g.platingGuide.trim())
    lines.push('')
  }

  if (Array.isArray(g.drinkPairings) && g.drinkPairings.length) {
    lines.push('■ ドリンクペアリング')
    for (const d of g.drinkPairings) lines.push(`🍷 ${d}`)
    lines.push('')
  }

  if (Array.isArray(g.commonMistakes) && g.commonMistakes.length) {
    lines.push('■ よくある失敗と対処法')
    for (const m of g.commonMistakes) lines.push(`⚠️ ${m}`)
    lines.push('')
  }

  if (Array.isArray(g.variations) && g.variations.length) {
    lines.push('■ アレンジ・バリエーション')
    for (const v of g.variations) lines.push(`🔄 ${v}`)
    lines.push('')
  }

  return { title: `プロ級レシピ: ${g.dishName}`, text: lines.join('\n').trimEnd() }
}

export function formatRecipe(r: Partial<Recipe>): Shareable {
  const name = line(r.name) || 'レシピ'
  const lines: string[] = [`🍳 ${name}`, '']
  if (line(r.description)) {
    lines.push(r.description!.trim())
    lines.push('')
  }
  const meta: string[] = []
  if (r.servings != null) meta.push(`${r.servings}人分`)
  if (r.prepTimeMinutes != null) meta.push(`下準備 ${r.prepTimeMinutes}分`)
  if (r.cookTimeMinutes != null) meta.push(`調理 ${r.cookTimeMinutes}分`)
  if (r.calories != null) meta.push(`${r.calories} kcal`)
  if (r.difficulty) {
    const d: Record<string, string> = { EASY: '簡単', MEDIUM: '普通', HARD: '難しい' }
    meta.push(d[r.difficulty] ?? r.difficulty)
  }
  if (meta.length) {
    lines.push(meta.join(' / '))
    lines.push('')
  }

  if (Array.isArray(r.ingredients) && r.ingredients.length) {
    lines.push('■ 材料')
    for (const ing of r.ingredients) {
      const amt = [line(ing.amount), line(ing.unit)].filter(Boolean).join(' ')
      lines.push(`・${ing.name}${amt ? ` — ${amt}` : ''}`)
    }
    lines.push('')
  }

  if (Array.isArray(r.steps) && r.steps.length) {
    lines.push('■ 作り方')
    r.steps.forEach((step, i) => lines.push(`${i + 1}. ${step}`))
    lines.push('')
  }

  if (Array.isArray(r.professionalTips) && r.professionalTips.length) {
    lines.push('■ プロのコツ')
    for (const t of r.professionalTips) lines.push(`✨ ${t}`)
    lines.push('')
  }

  if (line(r.platingAdvice)) {
    lines.push('■ 盛り付け')
    lines.push(r.platingAdvice!.trim())
    lines.push('')
  }

  if (Array.isArray(r.drinkPairings) && r.drinkPairings.length) {
    lines.push('■ ドリンクペアリング')
    for (const d of r.drinkPairings) lines.push(`🍷 ${d}`)
    lines.push('')
  }

  if (line(r.sourceUrl)) {
    lines.push(`出典: ${r.sourceUrl}`)
  }

  return { title: name, text: lines.join('\n').trimEnd() }
}
