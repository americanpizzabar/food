'use client'

import { useState, useCallback } from 'react'
import ImageUpload from '@/components/ImageUpload'
import { useLocalStorage } from '@/hooks/useLocalStorage'
import { useApiKey } from '@/hooks/useApiKey'
import { KEYS, generateId } from '@/lib/storage'
import { Recipe, Ingredient } from '@/lib/types'
import { Camera, Save, ShoppingCart, Edit3, Check, Trash2, Plus, Loader2, AlertTriangle } from 'lucide-react'

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
      createdAt: new Date().toISOString()
    }))
    updateShopping(prev => [...prev, ...items])
    setAddedToShopping(true)
  }

  const updateField = (field: keyof Recipe, value: unknown) =>
    setRecipe(r => r ? { ...r, [field]: value } : r)

  const updateIngredient = (i: number, field: keyof Ingredient, value: string) =>
    setRecipe(r => {
      if (!r?.ingredients) return r
      const ings = [...r.ingredients]
      ings[i] = { ...ings[i], [field]: value }
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

  return (
    <div className="space-y-5 animate-fade-in">
      <div>
        <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <Camera className="text-orange-500" size={22} /> 料理写真解析
        </h1>
        <p className="text-sm text-gray-500 mt-0.5">写真からレシピ・カロリー・栄養素を自動生成</p>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-3 text-sm text-red-600 flex gap-2">
          <AlertTriangle size={16} className="flex-shrink-0 mt-0.5" />{error}
        </div>
      )}

      {!recipe && (
        <div className="card space-y-4">
          <ImageUpload value={image} onChange={setImage} onClear={() => setImage(undefined)}
            label="料理の写真を撮影またはアップロード" />
          {image && (
            <button onClick={analyze} disabled={loading} className="btn-primary w-full justify-center">
              {loading ? <><Loader2 size={18} className="animate-spin" />解析中…</>
                       : <><Camera size={18} />AIで解析する</>}
            </button>
          )}
        </div>
      )}

      {recipe && (
        <div className="space-y-4 animate-fade-in">
          {/* Actions */}
          <div className="flex gap-2 flex-wrap">
            <button onClick={() => setEditing(!editing)} className="btn-secondary text-sm">
              {editing ? <><Check size={16} />編集完了</> : <><Edit3 size={16} />編集する</>}
            </button>
            <button onClick={saveRecipe} disabled={saved} className="btn-primary text-sm">
              <Save size={16} />{saved ? '保存済み' : 'レシピを保存'}
            </button>
            <button onClick={addToShopping} disabled={addedToShopping} className="btn-secondary text-sm">
              <ShoppingCart size={16} />{addedToShopping ? '追加済み' : '買い物リストへ'}
            </button>
            <button onClick={() => { setRecipe(null); setImage(undefined); setSaved(false); setAddedToShopping(false) }}
              className="btn-secondary text-sm">
              <Camera size={16} />別の写真
            </button>
          </div>

          {/* Name */}
          <div className="card">
            {editing
              ? <input className="input text-xl font-bold" value={recipe.name ?? ''} onChange={e => updateField('name', e.target.value)} />
              : <h2 className="text-xl font-bold text-gray-800">{recipe.name}</h2>}
            {editing
              ? <textarea className="input mt-2 text-sm" rows={2} value={recipe.description ?? ''} onChange={e => updateField('description', e.target.value)} />
              : <p className="text-sm text-gray-500 mt-1">{recipe.description}</p>}
          </div>

          {/* Nutrition summary */}
          <div className="card">
            <h3 className="section-title">カロリー・栄養</h3>
            <div className="grid grid-cols-4 gap-3 text-center">
              {[
                { label: 'カロリー', value: editing ? undefined : `${recipe.calories ?? 0}`, unit: 'kcal', field: 'calories' },
              ].map(({ label, value, unit, field }) => (
                <div key={label} className="bg-orange-50 rounded-xl p-2">
                  <p className="text-lg font-bold text-orange-600">
                    {editing
                      ? <input type="number" className="w-full text-center bg-transparent font-bold text-orange-600 text-lg"
                          value={(recipe as Record<string, unknown>)[field] as number ?? 0}
                          onChange={e => updateField(field as keyof Recipe, parseInt(e.target.value))} />
                      : value}
                  </p>
                  <p className="text-xs text-gray-500">{unit}</p>
                  <p className="text-xs font-medium text-gray-700">{label}</p>
                </div>
              ))}
              {recipe.nutrition && Object.entries({
                'タンパク質': `${recipe.nutrition.protein}g`,
                '炭水化物': `${recipe.nutrition.carbs}g`,
                '脂質': `${recipe.nutrition.fat}g`,
              }).map(([k, v]) => (
                <div key={k} className="bg-gray-50 rounded-xl p-2">
                  <p className="text-lg font-bold text-gray-700">{v}</p>
                  <p className="text-xs font-medium text-gray-600 mt-1">{k}</p>
                </div>
              ))}
            </div>
          </div>

          {/* Ingredients */}
          <div className="card">
            <div className="flex justify-between items-center mb-3">
              <h3 className="section-title mb-0">材料 ({recipe.servings ?? 2}人分)</h3>
              {editing && <button onClick={addIngredient} className="btn-secondary text-xs"><Plus size={14} />追加</button>}
            </div>
            <div className="space-y-2">
              {recipe.ingredients?.map((ing: Ingredient, i: number) => editing ? (
                <div key={i} className="flex gap-2">
                  <input className="input flex-1" placeholder="食材名" value={ing.name} onChange={e => updateIngredient(i, 'name', e.target.value)} />
                  <input className="input w-20" placeholder="量" value={ing.amount} onChange={e => updateIngredient(i, 'amount', e.target.value)} />
                  <input className="input w-16" placeholder="単位" value={ing.unit} onChange={e => updateIngredient(i, 'unit', e.target.value)} />
                  <button onClick={() => removeIngredient(i)} className="text-red-400 hover:text-red-600"><Trash2 size={16} /></button>
                </div>
              ) : (
                <div key={i} className="flex justify-between text-sm py-1 border-b border-gray-50">
                  <span className="text-gray-800">{ing.name}</span>
                  <span className="text-gray-500">{ing.amount} {ing.unit}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Steps */}
          <div className="card">
            <div className="flex justify-between items-center mb-3">
              <h3 className="section-title mb-0">作り方</h3>
              {editing && <button onClick={addStep} className="btn-secondary text-xs"><Plus size={14} />追加</button>}
            </div>
            <div className="space-y-3">
              {recipe.steps?.map((step: string, i: number) => (
                <div key={i} className="flex gap-3">
                  <span className="w-7 h-7 bg-orange-100 text-orange-600 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0">
                    {i + 1}
                  </span>
                  {editing ? (
                    <div className="flex-1 flex gap-2">
                      <textarea className="input flex-1 text-sm" rows={2} value={step} onChange={e => updateStep(i, e.target.value)} />
                      <button onClick={() => removeStep(i)} className="text-red-400"><Trash2 size={16} /></button>
                    </div>
                  ) : (
                    <p className="text-sm text-gray-700 leading-relaxed">{step}</p>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Plating advice */}
          {recipe.platingAdvice && (
            <div className="card bg-purple-50 border-purple-100">
              <h3 className="section-title text-purple-700">🍽️ 盛り付けアドバイス</h3>
              <p className="text-sm text-purple-800">{recipe.platingAdvice}</p>
            </div>
          )}

          {/* Remake ideas */}
          {recipe.remakeIdeas && recipe.remakeIdeas.length > 0 && (
            <div className="card bg-green-50 border-green-100">
              <h3 className="section-title text-green-700">🔄 リメイクアイデア</h3>
              {recipe.remakeIdeas.map((idea: string, i: number) => (
                <p key={i} className="text-sm text-green-800">• {idea}</p>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
