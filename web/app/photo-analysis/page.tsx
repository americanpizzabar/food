'use client'

import { useState, useCallback } from 'react'
import ImageUpload from '@/components/ImageUpload'
import { useLocalStorage } from '@/hooks/useLocalStorage'
import { useApiKey } from '@/hooks/useApiKey'
import { KEYS, generateId } from '@/lib/storage'
import { Recipe, Ingredient, CookingTechnique } from '@/lib/types'
import {
  Camera, Save, ShoppingCart, Edit3, Check, Trash2, Plus, Loader2,
  AlertTriangle, ChefHat, Utensils, Wine, Leaf, Package, Lightbulb,
  Star, Clock, Flame, RefreshCw, Sparkles, ChevronDown, ChevronUp,
  BadgeInfo, DollarSign,
} from 'lucide-react'

// ── Collapsible section ─────────────────────────────────────────────────────
function Section({
  title, icon, children, defaultOpen = false,
}: {
  title: string
  icon: React.ReactNode
  children: React.ReactNode
  defaultOpen?: boolean
}) {
  const [open, setOpen] = useState(defaultOpen)
  return (
    <div className="card">
      <button className="w-full flex items-center justify-between gap-3 text-left" onClick={() => setOpen(!open)}>
        <div className="flex items-center gap-2 font-bold text-[#e0e0e0]">
          <span style={{ color: '#e8b84b' }}>{icon}</span>
          {title}
        </div>
        {open ? <ChevronUp size={18} className="text-[#666]" /> : <ChevronDown size={18} className="text-[#666]" />}
      </button>
      {open && <div className="mt-4">{children}</div>}
    </div>
  )
}

// ── Info pill ───────────────────────────────────────────────────────────────
function Pill({ children, color = '#e8b84b', bg = 'rgba(232,184,75,.1)' }: {
  children: React.ReactNode; color?: string; bg?: string
}) {
  return (
    <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold" style={{ background: bg, color }}>
      {children}
    </span>
  )
}

// ── Main page ───────────────────────────────────────────────────────────────
export default function PhotoAnalysisPage() {
  const apiKey = useApiKey()
  const [image, setImage] = useState<string>()
  const [loading, setLoading] = useState(false)
  const [recipe, setRecipe] = useState<Partial<Recipe> | null>(null)
  const [editing, setEditing] = useState(false)
  const [error, setError] = useState('')
  const [saved, setSaved] = useState(false)
  const [addedToShopping, setAddedToShopping] = useState(false)

  const { update: updateRecipes } = useLocalStorage<Recipe[]>(KEYS.RECIPES, [])
  const { update: updateShopping } = useLocalStorage<import('@/lib/types').ShoppingItem[]>(KEYS.SHOPPING, [])

  const analyze = useCallback(async () => {
    if (!image) return
    setLoading(true); setError(''); setRecipe(null); setSaved(false)
    try {
      const res = await fetch('/api/analyze-photo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ imageDataUrl: image, apiKey }),
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data.error)
      setRecipe({ ...data.recipe, imageDataUrl: image, id: generateId(), createdAt: new Date().toISOString() })
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : '解析に失敗しました')
    } finally {
      setLoading(false)
    }
  }, [image, apiKey])

  const saveRecipe = () => {
    if (!recipe) return
    updateRecipes(prev => [{ ...recipe, id: recipe.id ?? generateId() } as Recipe, ...prev])
    setSaved(true)
  }

  const addToShopping = () => {
    if (!recipe?.ingredients) return
    const items = recipe.ingredients.map((ing: Ingredient) => ({
      id: generateId(), name: ing.name, amount: ing.amount ?? '', unit: ing.unit ?? '',
      category: 'その他', isChecked: false, recipeName: recipe.name, notes: '',
      createdAt: new Date().toISOString(),
    }))
    updateShopping(prev => [...prev, ...items])
    setAddedToShopping(true)
  }

  const updateField = (field: keyof Recipe, value: unknown) =>
    setRecipe(r => r ? { ...r, [field]: value } : r)

  const updateIngredient = (i: number, field: keyof Ingredient, value: string) =>
    setRecipe(r => {
      if (!r?.ingredients) return r
      const ings = [...r.ingredients]; ings[i] = { ...ings[i], [field]: value }
      return { ...r, ingredients: ings }
    })

  const removeIngredient = (i: number) =>
    setRecipe(r => r ? { ...r, ingredients: r.ingredients?.filter((_, idx) => idx !== i) } : r)

  const addIngredient = () =>
    setRecipe(r => r ? { ...r, ingredients: [...(r.ingredients ?? []), { name: '', amount: '', unit: '' }] } : r)

  const updateStep = (i: number, value: string) =>
    setRecipe(r => {
      if (!r?.steps) return r
      const steps = [...r.steps]; steps[i] = value
      return { ...r, steps }
    })

  const removeStep = (i: number) =>
    setRecipe(r => r ? { ...r, steps: r.steps?.filter((_, idx) => idx !== i) } : r)

  const addStep = () =>
    setRecipe(r => r ? { ...r, steps: [...(r.steps ?? []), ''] } : r)

  const difficultyLabel: Record<string, string> = { EASY: '初級', MEDIUM: '中級', HARD: '上級' }
  const difficultyColor: Record<string, string> = { EASY: '#34d399', MEDIUM: '#fbbf24', HARD: '#f87171' }
  const difficultyBg: Record<string, string> = { EASY: 'rgba(16,185,129,.12)', MEDIUM: 'rgba(251,191,36,.12)', HARD: 'rgba(239,68,68,.12)' }

  return (
    <div className="space-y-5 animate-fade-in">
      {/* Header */}
      <div>
        <h1 className="text-xl font-bold flex items-center gap-2">
          <Camera className="text-orange-400" size={22} /> 料理写真解析
        </h1>
        <p className="text-sm text-[#777] mt-0.5">写真からプロ級レシピ・栄養・調理技法を自動生成</p>
      </div>

      {error && (
        <div className="rounded-xl p-3 text-sm flex gap-2"
          style={{ background: 'rgba(239,68,68,.08)', border: '1px solid rgba(239,68,68,.18)', color: '#f87171' }}>
          <AlertTriangle size={16} className="flex-shrink-0 mt-0.5" />{error}
        </div>
      )}

      {/* Upload */}
      {!recipe && (
        <div className="card space-y-4">
          <ImageUpload value={image} onChange={setImage} onClear={() => setImage(undefined)}
            label="料理の写真を撮影またはアップロード" />
          {image && (
            <button onClick={analyze} disabled={loading} className="btn-primary w-full justify-center">
              {loading
                ? <><Loader2 size={18} className="animate-spin" />プロ解析中…</>
                : <><ChefHat size={18} />AIでプロ解析する</>}
            </button>
          )}
        </div>
      )}

      {/* Result */}
      {recipe && (
        <div className="space-y-4 animate-fade-in">

          {/* Actions */}
          <div className="flex gap-2 flex-wrap">
            <button onClick={() => setEditing(!editing)} className="btn-secondary text-sm">
              {editing ? <><Check size={16} />完了</> : <><Edit3 size={16} />編集</>}
            </button>
            <button onClick={saveRecipe} disabled={saved} className="btn-primary text-sm">
              <Save size={16} />{saved ? '保存済み' : 'レシピを保存'}
            </button>
            <button onClick={addToShopping} disabled={addedToShopping} className="btn-secondary text-sm">
              <ShoppingCart size={16} />{addedToShopping ? '追加済み' : '買い物リスト'}
            </button>
            <button onClick={() => { setRecipe(null); setImage(undefined); setSaved(false); setAddedToShopping(false) }}
              className="btn-secondary text-sm">
              <Camera size={16} />別の写真
            </button>
          </div>

          {/* ── Hero card ── */}
          <div className="card relative overflow-hidden"
            style={{ background: 'linear-gradient(145deg, #16140f 0%, #0e0c0a 50%, #120f0a 100%)', border: '1px solid rgba(232,184,75,.15)' }}>
            <div className="absolute -top-12 -right-10 w-48 h-48 rounded-full pointer-events-none"
              style={{ background: 'radial-gradient(circle, rgba(232,184,75,.12), transparent 70%)' }} />
            <div className="relative">
              {editing
                ? <input className="input text-xl font-bold mb-2" value={recipe.name ?? ''} onChange={e => updateField('name', e.target.value)} />
                : <h2 className="text-2xl font-black text-[#ececec] mb-2">{recipe.name}</h2>}
              {editing
                ? <textarea className="input text-sm mb-3" rows={2} value={recipe.description ?? ''} onChange={e => updateField('description', e.target.value)} />
                : <p className="text-sm text-[#888] mb-3 leading-relaxed">{recipe.description}</p>}

              {/* Meta pills */}
              <div className="flex flex-wrap gap-2">
                {recipe.difficulty && (
                  <Pill color={difficultyColor[recipe.difficulty] ?? '#e8b84b'} bg={difficultyBg[recipe.difficulty] ?? 'rgba(232,184,75,.1)'}>
                    <Star size={11} />{difficultyLabel[recipe.difficulty] ?? recipe.difficulty}
                  </Pill>
                )}
                {recipe.prepTimeMinutes != null && (
                  <Pill color="#60a5fa" bg="rgba(59,130,246,.12)">
                    <Clock size={11} />下準備 {recipe.prepTimeMinutes}分
                  </Pill>
                )}
                {recipe.cookTimeMinutes != null && (
                  <Pill color="#fb923c" bg="rgba(249,115,22,.12)">
                    <Flame size={11} />調理 {recipe.cookTimeMinutes}分
                  </Pill>
                )}
                {recipe.costEstimate && (
                  <Pill color="#a78bfa" bg="rgba(139,92,246,.12)">
                    <DollarSign size={11} />コスト: {recipe.costEstimate}
                  </Pill>
                )}
                {recipe.tags?.map(t => (
                  <Pill key={t} color="#9ca3af" bg="rgba(255,255,255,.06)">{t}</Pill>
                ))}
              </div>

              {/* Origin */}
              {recipe.origin && (
                <p className="text-xs text-[#666] mt-3 flex items-start gap-1.5">
                  <BadgeInfo size={13} className="flex-shrink-0 mt-0.5 text-[#555]" />
                  {recipe.origin}
                </p>
              )}
            </div>
          </div>

          {/* ── Nutrition ── */}
          <div className="card">
            <h3 className="section-title">カロリー・栄養素</h3>
            <div className="grid grid-cols-4 gap-2 text-center">
              <div className="rounded-xl p-3" style={{ background: 'rgba(249,115,22,.1)' }}>
                {editing
                  ? <input type="number" className="w-full text-center bg-transparent font-bold text-orange-400 text-lg"
                      value={recipe.calories ?? 0} onChange={e => updateField('calories', parseInt(e.target.value))} />
                  : <p className="text-lg font-bold text-orange-400">{recipe.calories ?? 0}</p>}
                <p className="text-[10px] text-[#666]">kcal</p>
                <p className="text-[11px] font-medium text-[#999]">カロリー</p>
              </div>
              {recipe.nutrition && (
                <>
                  {([
                    ['タンパク質', `${recipe.nutrition.protein}g`, '#60a5fa', 'rgba(59,130,246,.12)'],
                    ['炭水化物',   `${recipe.nutrition.carbs}g`,   '#a78bfa', 'rgba(139,92,246,.12)'],
                    ['脂質',       `${recipe.nutrition.fat}g`,     '#fb923c', 'rgba(249,115,22,.12)'],
                  ] as [string, string, string, string][]).map(([label, val, color, bg]) => (
                    <div key={label} className="rounded-xl p-3" style={{ background: bg }}>
                      <p className="text-lg font-bold" style={{ color }}>{val}</p>
                      <p className="text-[11px] font-medium text-[#777] mt-1">{label}</p>
                    </div>
                  ))}
                </>
              )}
            </div>
            {recipe.nutrition && (
              <div className="mt-3 grid grid-cols-3 gap-2">
                {([
                  ['食物繊維', `${recipe.nutrition.fiber}g`],
                  ['糖質',     `${recipe.nutrition.sugar}g`],
                  ['塩分',     `${Math.round((recipe.nutrition.sodium ?? 0) / 400 * 10) / 10}g`],
                ] as [string, string][]).map(([label, val]) => (
                  <div key={label} className="rounded-lg px-3 py-2 flex justify-between"
                    style={{ background: 'var(--surface-2)', border: '1px solid rgba(255,255,255,.04)' }}>
                    <span className="text-xs text-[#777]">{label}</span>
                    <span className="text-xs font-bold text-[#bbb]">{val}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* ── Ingredients ── */}
          <Section title={`材料（${recipe.servings ?? 2}人分）`} icon={<Utensils size={17} />} defaultOpen>
            {editing && (
              <button onClick={addIngredient} className="btn-secondary text-xs mb-3">
                <Plus size={14} />食材を追加
              </button>
            )}
            <div className="space-y-2">
              {recipe.ingredients?.map((ing: Ingredient, i: number) => editing ? (
                <div key={i} className="flex gap-2">
                  <input className="input flex-1" placeholder="食材名" value={ing.name} onChange={e => updateIngredient(i, 'name', e.target.value)} />
                  <input className="input w-20" placeholder="量" value={ing.amount} onChange={e => updateIngredient(i, 'amount', e.target.value)} />
                  <input className="input w-16" placeholder="単位" value={ing.unit} onChange={e => updateIngredient(i, 'unit', e.target.value)} />
                  <button onClick={() => removeIngredient(i)} className="text-red-400 hover:text-red-300"><Trash2 size={16} /></button>
                </div>
              ) : (
                <div key={i} className="p-2.5 rounded-xl" style={{ background: 'var(--surface-2)', border: '1px solid rgba(255,255,255,.04)' }}>
                  <div className="flex justify-between items-start">
                    <span className="text-sm font-medium text-[#d0d0d0]">{ing.name}</span>
                    <span className="text-sm text-[#888] ml-2 flex-shrink-0">{ing.amount} {ing.unit}</span>
                  </div>
                  {ing.selectionTip && (
                    <p className="text-xs text-amber-400/80 mt-1 flex items-start gap-1">
                      <Lightbulb size={11} className="flex-shrink-0 mt-0.5" />
                      {ing.selectionTip}
                    </p>
                  )}
                </div>
              ))}
            </div>
          </Section>

          {/* ── Steps ── */}
          <Section title="作り方" icon={<ChefHat size={17} />} defaultOpen>
            {editing && (
              <button onClick={addStep} className="btn-secondary text-xs mb-3">
                <Plus size={14} />ステップを追加
              </button>
            )}
            <div className="space-y-3">
              {recipe.steps?.map((step: string, i: number) => (
                <div key={i} className="flex gap-3">
                  <span className="w-7 h-7 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0"
                    style={{ background: 'rgba(232,184,75,.15)', color: '#e8b84b' }}>
                    {i + 1}
                  </span>
                  {editing ? (
                    <div className="flex-1 flex gap-2">
                      <textarea className="input flex-1 text-sm" rows={2} value={step} onChange={e => updateStep(i, e.target.value)} />
                      <button onClick={() => removeStep(i)} className="text-red-400"><Trash2 size={16} /></button>
                    </div>
                  ) : (
                    <p className="text-sm text-[#bbb] leading-relaxed pt-0.5">{step}</p>
                  )}
                </div>
              ))}
            </div>
          </Section>

          {/* ── Cooking techniques ── */}
          {Array.isArray(recipe.cookingTechniques) && recipe.cookingTechniques.length > 0 && (
            <Section title="プロの調理技法" icon={<Flame size={17} />}>
              <div className="space-y-3">
                {recipe.cookingTechniques.map((t: CookingTechnique, i: number) => (
                  <div key={i} className="p-3 rounded-xl"
                    style={{ background: 'rgba(232,184,75,.07)', border: '1px solid rgba(232,184,75,.15)' }}>
                    <p className="font-semibold text-[#e0e0e0] mb-1">🔥 {t.name}</p>
                    <p className="text-sm text-[#bbb] mb-1">{t.description}</p>
                    {t.whyImportant && (
                      <p className="text-xs text-amber-400">なぜ重要？ {t.whyImportant}</p>
                    )}
                  </div>
                ))}
              </div>
            </Section>
          )}

          {/* ── Professional tips ── */}
          {Array.isArray(recipe.professionalTips) && recipe.professionalTips.length > 0 && (
            <Section title="シェフの秘訣" icon={<Star size={17} />}>
              <div className="space-y-2">
                {recipe.professionalTips.map((tip: string, i: number) => (
                  <div key={i} className="flex gap-2.5 p-3 rounded-xl"
                    style={{ background: 'rgba(232,184,75,.07)', border: '1px solid rgba(232,184,75,.12)' }}>
                    <span className="text-amber-400 flex-shrink-0">✨</span>
                    <p className="text-sm text-[#bbb]">{tip}</p>
                  </div>
                ))}
              </div>
            </Section>
          )}

          {/* ── Plating advice ── */}
          {recipe.platingAdvice && (
            <Section title="盛り付けガイド" icon={<Sparkles size={17} />}>
              <div className="p-3 rounded-xl"
                style={{ background: 'rgba(168,85,247,.07)', border: '1px solid rgba(168,85,247,.15)' }}>
                <p className="text-sm text-purple-300/90 leading-relaxed">{recipe.platingAdvice}</p>
              </div>
            </Section>
          )}

          {/* ── Drink pairings ── */}
          {Array.isArray(recipe.drinkPairings) && recipe.drinkPairings.length > 0 && (
            <Section title="ドリンクペアリング" icon={<Wine size={17} />}>
              <div className="space-y-2">
                {recipe.drinkPairings.map((d: string, i: number) => (
                  <p key={i} className="text-sm text-[#bbb] flex gap-2"><span>🍷</span>{d}</p>
                ))}
              </div>
            </Section>
          )}

          {/* ── Health benefits ── */}
          {Array.isArray(recipe.healthBenefits) && recipe.healthBenefits.length > 0 && (
            <Section title="健康・栄養メリット" icon={<Leaf size={17} />}>
              <div className="space-y-2">
                {recipe.healthBenefits.map((b: string, i: number) => (
                  <div key={i} className="flex gap-2.5 p-3 rounded-xl"
                    style={{ background: 'rgba(16,185,129,.07)', border: '1px solid rgba(16,185,129,.12)' }}>
                    <span className="text-emerald-400 flex-shrink-0">💚</span>
                    <p className="text-sm text-emerald-400/80">{b}</p>
                  </div>
                ))}
              </div>
            </Section>
          )}

          {/* ── Storage ── */}
          {recipe.storageInfo && (
            <Section title="保存方法" icon={<Package size={17} />}>
              <div className="p-3 rounded-xl"
                style={{ background: 'rgba(59,130,246,.07)', border: '1px solid rgba(59,130,246,.12)' }}>
                <p className="text-sm text-blue-300/90 leading-relaxed">{recipe.storageInfo}</p>
              </div>
            </Section>
          )}

          {/* ── Remake ideas ── */}
          {Array.isArray(recipe.remakeIdeas) && recipe.remakeIdeas.length > 0 && (
            <Section title="リメイクアイデア" icon={<RefreshCw size={17} />}>
              <div className="space-y-2">
                {recipe.remakeIdeas.map((idea: string, i: number) => (
                  <p key={i} className="text-sm text-[#bbb] flex gap-2"><span>🔄</span>{idea}</p>
                ))}
              </div>
            </Section>
          )}

          {/* ── Variations ── */}
          {Array.isArray(recipe.variations) && recipe.variations.length > 0 && (
            <Section title="アレンジ・バリエーション" icon={<Sparkles size={17} />}>
              <div className="space-y-2">
                {recipe.variations.map((v: string, i: number) => (
                  <p key={i} className="text-sm text-[#bbb] flex gap-2"><span>🍳</span>{v}</p>
                ))}
              </div>
            </Section>
          )}

        </div>
      )}
    </div>
  )
}
