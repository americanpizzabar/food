'use client'

import { useState } from 'react'
import { useLocalStorage } from '@/hooks/useLocalStorage'
import { useApiKey } from '@/hooks/useApiKey'
import { KEYS, generateId } from '@/lib/storage'
import { Recipe, ShoppingItem, PantryItem } from '@/lib/types'
import { Sparkles, Save, ShoppingCart, ChevronDown, ChevronUp, Loader2, AlertTriangle, Clock, Users } from 'lucide-react'

const MOODS = ['😊 嬉しい', '😴 疲れた', '😰 ストレス', '⚡ 元気', '😌 リラックス', '😐 普通']
const CONDITIONS = ['絶好調', '良好', '普通', '不調', '体調不良']

export default function RecipeSuggestionPage() {
  const apiKey = useApiKey()
  const [mood, setMood] = useState('')
  const [craving, setCraving] = useState('')
  const [health, setHealth] = useState('')
  const [nutrition, setNutrition] = useState('')
  const [loading, setLoading] = useState(false)
  const [suggestions, setSuggestions] = useState<Recipe[]>([])
  const [error, setError] = useState('')
  const [savedIds, setSavedIds] = useState<Set<string>>(new Set())

  const { value: pantryItems } = useLocalStorage<PantryItem[]>(KEYS.PANTRY, [])
  const { update: updateRecipes } = useLocalStorage<Recipe[]>(KEYS.RECIPES, [])
  const { update: updateShopping } = useLocalStorage<ShoppingItem[]>(KEYS.SHOPPING, [])

  const getSuggestions = async () => {
    setLoading(true); setError(''); setSuggestions([])
    try {
      const res = await fetch('/api/suggest-recipes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          mood, craving, healthCondition: health, nutritionNeeds: nutrition,
          pantryItems: pantryItems.map(p => p.name),
          allergens: [], dislikedIngredients: [],
          apiKey,
        }),
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data.error)
      setSuggestions(data.recipes.map((r: Recipe) => ({ ...r, id: generateId(), createdAt: new Date().toISOString() })))
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : '提案に失敗しました')
    } finally {
      setLoading(false)
    }
  }

  const saveRecipe = (recipe: Recipe) => {
    updateRecipes(prev => [recipe, ...prev])
    setSavedIds(prev => new Set(prev).add(recipe.id))
  }

  const addToShopping = (recipe: Recipe) => {
    const items: ShoppingItem[] = (recipe.ingredients ?? []).map(ing => ({
      id: generateId(), name: ing.name, amount: ing.amount ?? '', unit: ing.unit ?? '',
      category: 'その他', isChecked: false, recipeName: recipe.name,
      notes: '', createdAt: new Date().toISOString(),
    }))
    updateShopping(prev => [...prev, ...items])
  }

  return (
    <div className="space-y-5 animate-fade-in">
      <div>
        <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <Sparkles className="text-yellow-500" size={22} /> AIレシピ提案
        </h1>
        <p className="text-sm text-gray-500 mt-0.5">気分・体調・在庫から最適なレシピを提案</p>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-3 text-sm text-red-600 flex gap-2">
          <AlertTriangle size={16} className="flex-shrink-0" />{error}
        </div>
      )}

      <div className="card space-y-4">
        <div>
          <p className="text-sm font-semibold text-gray-700 mb-2">今の気分</p>
          <div className="flex flex-wrap gap-2">
            {MOODS.map(m => (
              <button key={m} onClick={() => setMood(mood === m ? '' : m)}
                className={`px-3 py-1.5 rounded-full text-sm border transition-all ${mood === m ? 'bg-orange-500 text-white border-orange-500' : 'bg-white text-gray-600 border-gray-200'}`}>
                {m}
              </button>
            ))}
          </div>
        </div>

        <div>
          <p className="text-sm font-semibold text-gray-700 mb-2">体調</p>
          <div className="flex flex-wrap gap-2">
            {CONDITIONS.map(c => (
              <button key={c} onClick={() => setHealth(health === c ? '' : c)}
                className={`px-3 py-1.5 rounded-full text-sm border transition-all ${health === c ? 'bg-blue-500 text-white border-blue-500' : 'bg-white text-gray-600 border-gray-200'}`}>
                {c}
              </button>
            ))}
          </div>
        </div>

        <input className="input" placeholder="食べたいもの（例: あっさり、温かいもの）" value={craving} onChange={e => setCraving(e.target.value)} />
        <input className="input" placeholder="必要な栄養素（例: タンパク質多め、ビタミンC）" value={nutrition} onChange={e => setNutrition(e.target.value)} />

        {pantryItems.length > 0 && (
          <p className="text-xs text-green-600">
            ✅ パントリーの食材 {pantryItems.length}品 を優先的に使ったレシピを提案します
          </p>
        )}

        <button onClick={getSuggestions} disabled={loading} className="btn-primary w-full justify-center">
          {loading ? <><Loader2 size={18} className="animate-spin" />提案中…</> : <><Sparkles size={18} />レシピを提案する</>}
        </button>
      </div>

      {suggestions.map(recipe => (
        <RecipeCard key={recipe.id} recipe={recipe} saved={savedIds.has(recipe.id)}
          onSave={() => saveRecipe(recipe)} onAddToShopping={() => addToShopping(recipe)} />
      ))}
    </div>
  )
}

function RecipeCard({ recipe, saved, onSave, onAddToShopping }: {
  recipe: Recipe; saved: boolean; onSave: () => void; onAddToShopping: () => void
}) {
  const [expanded, setExpanded] = useState(false)
  const diffColor = { EASY: 'bg-green-100 text-green-700', MEDIUM: 'bg-yellow-100 text-yellow-700', HARD: 'bg-red-100 text-red-700' }
  const diffLabel = { EASY: '簡単', MEDIUM: '普通', HARD: '難しい' }

  return (
    <div className="card space-y-3 animate-fade-in">
      <div className="flex items-start justify-between gap-2">
        <div>
          <h3 className="font-bold text-gray-800 text-lg">{recipe.name}</h3>
          <p className="text-sm text-gray-500 mt-0.5">{recipe.description}</p>
        </div>
        <span className={`badge flex-shrink-0 ${diffColor[recipe.difficulty ?? 'MEDIUM']}`}>
          {diffLabel[recipe.difficulty ?? 'MEDIUM']}
        </span>
      </div>
      <div className="flex gap-4 text-sm text-gray-500">
        <span>🔥 {recipe.calories} kcal</span>
        <span className="flex items-center gap-1"><Clock size={14} />{(recipe.prepTimeMinutes ?? 0) + (recipe.cookTimeMinutes ?? 0)}分</span>
        <span className="flex items-center gap-1"><Users size={14} />{recipe.servings}人分</span>
      </div>
      <div className="flex gap-2">
        <button onClick={onSave} disabled={saved} className="btn-secondary text-sm flex-1 justify-center">
          <Save size={16} />{saved ? '保存済み' : '保存'}
        </button>
        <button onClick={onAddToShopping} className="btn-primary text-sm flex-1 justify-center">
          <ShoppingCart size={16} />買い物へ
        </button>
      </div>
      <button onClick={() => setExpanded(!expanded)} className="text-sm text-orange-500 hover:text-orange-600 flex items-center gap-1">
        {expanded ? <><ChevronUp size={16} />詳細を閉じる</> : <><ChevronDown size={16} />材料・手順を見る</>}
      </button>
      {expanded && (
        <div className="space-y-3 pt-2 border-t">
          <div>
            <p className="text-sm font-semibold text-gray-700 mb-1">材料</p>
            {recipe.ingredients?.map((ing, i) => (
              <div key={i} className="flex justify-between text-sm py-0.5">
                <span>{ing.name}</span><span className="text-gray-400">{ing.amount} {ing.unit}</span>
              </div>
            ))}
          </div>
          <div>
            <p className="text-sm font-semibold text-gray-700 mb-1">作り方</p>
            {recipe.steps?.map((step, i) => (
              <div key={i} className="flex gap-2 text-sm mb-2">
                <span className="w-6 h-6 bg-orange-100 text-orange-600 rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0">{i+1}</span>
                <p className="text-gray-700">{step}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
