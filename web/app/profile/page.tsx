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

  const chipInactive = {
    background: 'var(--surface-2)',
    border: '1px solid rgba(255,255,255,.08)',
    color: '#999',
  }

  return (
    <div className="space-y-5 animate-fade-in">
      <div>
        <h1 className="text-xl font-bold flex items-center gap-2">
          <User className="text-[#888]" size={22} /> プロフィール設定
        </h1>
      </div>

      {/* API Key */}
      <div className="card space-y-3">
        <div className="flex items-center gap-2 mb-1">
          <Key size={18} style={{ color: '#e8b84b' }} />
          <h2 className="font-bold text-[#ddd]">Google AI Studio APIキー</h2>
        </div>
        <p className="text-xs text-[#666]">
          AI機能を使うには{' '}
          <a href="https://aistudio.google.com/app/apikey" target="_blank" rel="noreferrer"
            className="text-[#60a5fa] underline">
            Google AI Studio
          </a>{' '}
          でAPIキーを取得して設定してください。
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
        <h2 className="font-bold text-[#ddd]">⚠️ アレルギー食材</h2>
        <p className="text-xs text-[#666]">登録した食材はレシピ提案から自動除外されます</p>
        <div className="flex flex-wrap gap-2">
          {(prefs.allergens ?? []).map(a => (
            <span key={a} className="badge flex items-center gap-1 pr-1"
              style={{ background: 'rgba(239,68,68,.12)', color: '#f87171', border: '1px solid rgba(239,68,68,.2)' }}>
              {a}
              <button onClick={() => removeAllergen(a)} className="hover:opacity-70"><X size={12} /></button>
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
        <h2 className="font-bold text-[#ddd]">😣 苦手な食材</h2>
        <div className="flex flex-wrap gap-2">
          {(prefs.dislikedIngredients ?? []).map(d => (
            <span key={d} className="badge flex items-center gap-1 pr-1"
              style={{ background: 'var(--surface-2)', color: '#aaa', border: '1px solid rgba(255,255,255,.08)' }}>
              {d}
              <button onClick={() => removeDisliked(d)} className="hover:opacity-70"><X size={12} /></button>
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
        <h2 className="font-bold text-[#ddd]">🥗 食事制限</h2>
        <div className="flex flex-wrap gap-2">
          {RESTRICTIONS.map(r => (
            <button key={r} onClick={() => toggleRestriction(r)}
              className="px-3 py-1.5 rounded-full text-sm transition-all"
              style={(prefs.dietaryRestrictions ?? []).includes(r)
                ? { background: 'rgba(16,185,129,.15)', color: '#34d399', border: '1px solid rgba(16,185,129,.25)' }
                : chipInactive}>
              {r}
            </button>
          ))}
        </div>
      </div>

      {/* Serving size */}
      <div className="card">
        <h2 className="font-bold text-[#ddd] mb-3">👥 デフォルト人数</h2>
        <div className="flex items-center gap-4">
          <button onClick={() => set({ servingSize: Math.max(1, (prefs.servingSize ?? 2) - 1) })}
            className="w-10 h-10 rounded-full text-xl font-bold transition-colors"
            style={{ border: '2px solid rgba(255,255,255,.1)', color: '#aaa' }}>
            −
          </button>
          <span className="text-2xl font-bold w-16 text-center text-[#e0e0e0]">{prefs.servingSize ?? 2}人</span>
          <button onClick={() => set({ servingSize: (prefs.servingSize ?? 2) + 1 })}
            className="w-10 h-10 rounded-full text-xl font-bold transition-colors"
            style={{ border: '2px solid rgba(255,255,255,.1)', color: '#aaa' }}>
            ＋
          </button>
        </div>
      </div>

      {/* App info */}
      <div className="card text-center">
        <p className="text-sm font-bold text-[#bbb]">🍽️ FoodAI v1.0</p>
        <p className="text-xs text-[#555] mt-1">Powered by Google Gemini 2.5 Flash</p>
        <p className="text-xs text-[#555]">データはブラウザのlocalStorageに保存されます</p>
      </div>
    </div>
  )
}
