'use client'

import { useState, useMemo } from 'react'
import { useLocalStorage } from '@/hooks/useLocalStorage'
import { KEYS, generateId } from '@/lib/storage'
import { ShoppingItem } from '@/lib/types'
import { ShoppingCart, Plus, Trash2, X, Eye, EyeOff } from 'lucide-react'

const CATEGORIES = ['野菜', '肉・魚', '乳製品', '穀物・豆', '調味料', '飲み物', 'お菓子', 'その他']

export default function ShoppingPage() {
  const { value: items, update } = useLocalStorage<ShoppingItem[]>(KEYS.SHOPPING, [])
  const [showChecked, setShowChecked] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [showDeleteAll, setShowDeleteAll] = useState(false)
  const [form, setForm] = useState({ name: '', amount: '', unit: '', category: 'その他' })

  const displayed = useMemo(() =>
    showChecked ? items : items.filter(i => !i.isChecked),
    [items, showChecked]
  )
  const grouped = useMemo(() =>
    CATEGORIES.reduce((acc, cat) => {
      const catItems = displayed.filter(i => i.category === cat)
      if (catItems.length > 0) acc[cat] = catItems
      const unknownItems = displayed.filter(i => !CATEGORIES.includes(i.category))
      if (unknownItems.length > 0) acc['その他2'] = unknownItems
      return acc
    }, {} as Record<string, ShoppingItem[]>),
    [displayed]
  )

  const toggle = (id: string) => update(prev => prev.map(i => i.id === id ? { ...i, isChecked: !i.isChecked } : i))
  const deleteItem = (id: string) => update(prev => prev.filter(i => i.id !== id))
  const deleteChecked = () => update(prev => prev.filter(i => !i.isChecked))
  const deleteAll = () => { update(() => []); setShowDeleteAll(false) }

  const saveItem = () => {
    if (!form.name.trim()) return
    const item: ShoppingItem = {
      id: generateId(), name: form.name, amount: form.amount, unit: form.unit,
      category: form.category, isChecked: false, notes: '', createdAt: new Date().toISOString(),
    }
    update(prev => [...prev, item])
    setForm({ name: '', amount: '', unit: '', category: 'その他' })
    setShowForm(false)
  }

  const uncheckedCount = items.filter(i => !i.isChecked).length

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold flex items-center gap-2">
            <ShoppingCart className="text-blue-500" size={22} /> 買い物リスト
          </h1>
          <p className="text-sm text-[#777]">残り {uncheckedCount}品 / 合計 {items.length}品</p>
        </div>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary text-sm">
          <Plus size={16} />追加
        </button>
      </div>

      {/* Controls */}
      <div className="flex gap-2 flex-wrap">
        <button onClick={() => setShowChecked(!showChecked)} className="btn-secondary text-sm">
          {showChecked ? <EyeOff size={16} /> : <Eye size={16} />}
          {showChecked ? '完了品を隠す' : '完了品を表示'}
        </button>
        <button onClick={deleteChecked} className="btn-secondary text-sm">
          <Trash2 size={16} />完了品を削除
        </button>
        <button onClick={() => setShowDeleteAll(true)} className="btn-danger">
          全て削除
        </button>
      </div>

      {showDeleteAll && (
        <div className="card" style={{ background: 'rgba(239,68,68,.08)', border: '1px solid rgba(239,68,68,.15)' }}>
          <p className="text-sm text-red-400 mb-3">買い物リストを全て削除しますか？</p>
          <div className="flex gap-2">
            <button onClick={deleteAll} className="btn-danger">削除する</button>
            <button onClick={() => setShowDeleteAll(false)} className="btn-secondary text-sm">キャンセル</button>
          </div>
        </div>
      )}

      {showForm && (
        <div className="card space-y-3 animate-fade-in">
          <div className="flex justify-between items-center">
            <h2 className="font-bold">アイテムを追加</h2>
            <button onClick={() => setShowForm(false)}><X size={18} className="text-[#777]" /></button>
          </div>
          <input className="input" placeholder="食材名 *" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
          <div className="flex gap-2">
            <input className="input flex-1" placeholder="数量" value={form.amount} onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} />
            <input className="input w-24" placeholder="単位 (g/ml/個)" value={form.unit} onChange={e => setForm(f => ({ ...f, unit: e.target.value }))} />
          </div>
          <div className="flex flex-wrap gap-2">
            {CATEGORIES.map(c => (
              <button key={c} onClick={() => setForm(f => ({ ...f, category: c }))}
                className={`px-3 py-1 rounded-full text-sm border transition-all ${form.category === c ? 'bg-blue-500 text-white border-blue-500' : 'hover:border-[rgba(255,255,255,.15)]'}`}
                style={form.category === c ? {} : { background: 'var(--surface-2)', border: '1px solid rgba(255,255,255,.08)', color: '#888' }}>
                {c}
              </button>
            ))}
          </div>
          <button onClick={saveItem} disabled={!form.name.trim()} className="btn-primary w-full justify-center">追加する</button>
        </div>
      )}

      {items.length === 0 && (
        <div className="text-center py-12 text-[#777]">
          <ShoppingCart size={40} className="mx-auto mb-3 opacity-30" />
          <p>買い物リストは空です</p>
          <p className="text-xs mt-1">レシピ提案から自動追加またはＡを押して手動追加</p>
        </div>
      )}

      <div className="space-y-4">
        {Object.entries(grouped).map(([category, catItems]) => (
          <div key={category}>
            <div className="rounded-lg px-3 py-1.5 mb-2" style={{ background: 'var(--surface-2)' }}>
              <span className="text-xs font-semibold text-[#666]">{category === 'その他2' ? 'その他' : category} ({catItems.length})</span>
            </div>
            <div className="space-y-1">
              {catItems.map(item => (
                <div key={item.id}
                  className="flex items-center gap-3 p-3 rounded-xl transition-all"
                  style={item.isChecked
                    ? { background: 'var(--surface-2)', borderColor: 'rgba(255,255,255,.06)', border: '1px solid rgba(255,255,255,.06)', opacity: 0.5 }
                    : { background: 'var(--surface-2)', border: '1px solid rgba(255,255,255,.06)' }}>
                  <input type="checkbox" checked={item.isChecked} onChange={() => toggle(item.id)}
                    className="w-5 h-5 rounded accent-blue-500 flex-shrink-0 cursor-pointer" />
                  <div className="flex-1 min-w-0">
                    <p className={`text-sm font-medium ${item.isChecked ? 'line-through text-[#777]' : ''}`}>{item.name}</p>
                    {(item.amount || item.unit) && (
                      <p className="text-xs text-[#777]">{item.amount} {item.unit}</p>
                    )}
                    {item.recipeName && (
                      <span className="text-xs text-blue-500">{item.recipeName}</span>
                    )}
                  </div>
                  <button onClick={() => deleteItem(item.id)} className="text-[#444] hover:text-red-400 transition-colors">
                    <X size={16} />
                  </button>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
