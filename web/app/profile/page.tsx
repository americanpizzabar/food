'use client'

import { useState } from 'react'
import { useLocalStorage } from '@/hooks/useLocalStorage'
import { KEYS } from '@/lib/storage'
import { UserPreferences } from '@/lib/types'
import { User, Key, Eye, EyeOff, Plus, X, Save } from 'lucide-react'

const DEFAULT_PREFS: UserPreferences = {
  allergens: [], dislikedIngredients: [], dietaryRestrictions: [],
  servingSize: 2, geminiApiKey: '', notificationsEnabled: true,
}

const RESTRICTIONS = ['ベジタリアン', 'ヴィーガン', 'グルテンフリー', '乳製品不使用', '卵不使用', '低糖質']

export default function ProfilePage() {
  const { value: prefs, update } = useLocalStorage<UserPreferences>(KEYS.PREFERENCES, DEFAULT_PREFS)
  const [apiKeyVisible, setApiKeyVisible] = useState(false)
  const [newAllergen, setNewAllergen] = useState('')
  const [newDisliked, setNewDisliked] = useState('')
  const [saved, setSaved] = useState(false)

  const set = (patch: Partial<UserPreferences>) => update(prev => ({ ...prev, ...patch }))

  const addAllergen = () => {
    if (!newAllergen.trim()) return
    set({ allergens: [...(prefs.allergens ?? []), newAllergen.trim()] })
    setNewAllergen('')
  }
  const removeAllergen = (v: string) => set({ allergens: prefs.allergens.filter(a => a !== v) })

  const addDisliked = () => {
    if (!newDisliked.trim()) return
    set({ dislikedIngredients: [...(prefs.dislikedIngredients ?? []), newDisliked.trim()] })
    setNewDisliked('')
  }
  const removeDisliked = (v: string) => set({ dislikedIngredients: prefs.dislikedIngredients.filter(d => d !== v) })

  const toggleRestriction = (r: string) => {
    const current = prefs.dietaryRestrictions ?? []
    set({ dietaryRestrictions: current.includes(r) ? current.filter(x => x !== r) : [...current, r] })
  }

  const saveApiKey = () => { setSaved(true); setTimeout(() => setSaved(false), 2000) }

  return (
    <div className="space-y-5 animate-fade-in">
      <div>
        <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <User className="text-gray-500" size={22} /> プロフィール設定
        </h1>
      </div>

      {/* API Key */}
      <div className="card space-y-3">
        <div className="flex items-center gap-2 mb-1">
          <Key size={18} className="text-orange-500" />
          <h2 className="font-bold text-gray-800">Google AI Studio APIキー</h2>
        </div>
        <p className="text-xs text-gray-500">
          AI機能を使うには <a href="https://aistudio.google.com/app/apikey" target="_blank" rel="noreferrer" className="text-blue-500 underline">Google AI Studio</a> でAPIキーを取得して設定してください。
        </p>
        <div className="flex gap-2">
          <input
            className="input flex-1"
            type={apiKeyVisible ? 'text' : 'password'}
            placeholder="AIzaSy..."
            value={prefs.geminiApiKey ?? ''}
            onChange={e => set({ geminiApiKey: e.target.value })}
          />
          <button onClick={() => setApiKeyVisible(!apiKeyVisible)} className="btn-secondary px-3">
            {apiKeyVisible ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        </div>
        <button onClick={saveApiKey} className="btn-primary text-sm">
          <Save size={16} />{saved ? '保存しました！' : 'APIキーを保存'}
        </button>
      </div>

      {/* Allergens */}
      <div className="card space-y-3">
        <h2 className="font-bold text-gray-800">⚠️ アレルギー食材</h2>
        <p className="text-xs text-gray-500">登録した食材はレシピ提案から自動除外されます</p>
        <div className="flex flex-wrap gap-2">
          {(prefs.allergens ?? []).map(a => (
            <span key={a} className="badge bg-red-100 text-red-700 flex items-center gap-1 pr-1">
              {a}
              <button onClick={() => removeAllergen(a)} className="hover:text-red-900"><X size={12} /></button>
            </span>
          ))}
        </div>
        <div className="flex gap-2">
          <input className="input flex-1" placeholder="例: 卵、小麦、えび" value={newAllergen}
            onChange={e => setNewAllergen(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && addAllergen()} />
          <button onClick={addAllergen} className="btn-secondary px-3"><Plus size={16} /></button>
        </div>
      </div>

      {/* Disliked */}
      <div className="card space-y-3">
        <h2 className="font-bold text-gray-800">😣 苦手な食材</h2>
        <div className="flex flex-wrap gap-2">
          {(prefs.dislikedIngredients ?? []).map(d => (
            <span key={d} className="badge bg-gray-100 text-gray-700 flex items-center gap-1 pr-1">
              {d}
              <button onClick={() => removeDisliked(d)} className="hover:text-gray-900"><X size={12} /></button>
            </span>
          ))}
        </div>
        <div className="flex gap-2">
          <input className="input flex-1" placeholder="例: パクチー、レバー" value={newDisliked}
            onChange={e => setNewDisliked(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && addDisliked()} />
          <button onClick={addDisliked} className="btn-secondary px-3"><Plus size={16} /></button>
        </div>
      </div>

      {/* Dietary restrictions */}
      <div className="card space-y-3">
        <h2 className="font-bold text-gray-800">🥗 食事制限</h2>
        <div className="flex flex-wrap gap-2">
          {RESTRICTIONS.map(r => (
            <button key={r} onClick={() => toggleRestriction(r)}
              className={`px-3 py-1.5 rounded-full text-sm border transition-all ${
                (prefs.dietaryRestrictions ?? []).includes(r)
                  ? 'bg-green-500 text-white border-green-500'
                  : 'bg-white text-gray-600 border-gray-200'
              }`}>
              {r}
            </button>
          ))}
        </div>
      </div>

      {/* Serving size */}
      <div className="card">
        <h2 className="font-bold text-gray-800 mb-3">👥 デフォルト人数</h2>
        <div className="flex items-center gap-4">
          <button onClick={() => set({ servingSize: Math.max(1, (prefs.servingSize ?? 2) - 1) })}
            className="w-10 h-10 rounded-full border-2 border-gray-200 text-xl font-bold hover:border-orange-300 transition-colors">−</button>
          <span className="text-2xl font-bold text-gray-800 w-16 text-center">{prefs.servingSize ?? 2}人</span>
          <button onClick={() => set({ servingSize: (prefs.servingSize ?? 2) + 1 })}
            className="w-10 h-10 rounded-full border-2 border-gray-200 text-xl font-bold hover:border-orange-300 transition-colors">＋</button>
        </div>
      </div>

      {/* App info */}
      <div className="card text-center">
        <p className="text-sm font-bold text-gray-700">🍽️ FoodAI v1.0</p>
        <p className="text-xs text-gray-400 mt-1">Powered by Google Gemini 2.5 Flash</p>
        <p className="text-xs text-gray-400">データはブラウザのlocalStorageに保存されます</p>
      </div>
    </div>
  )
}
