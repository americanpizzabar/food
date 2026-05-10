'use client'

import { useState, useMemo } from 'react'
import { useLocalStorage } from '@/hooks/useLocalStorage'
import { KEYS, generateId } from '@/lib/storage'
import { PantryItem } from '@/lib/types'
import { Package, Plus, Trash2, Edit3, X, Search, AlertTriangle } from 'lucide-react'

const CATEGORIES = ['野菜', '肉・魚', '乳製品', '穀物・豆', '調味料', '飲み物', '缶詰・乾物', '冷凍食品', 'その他']

const EMPTY_FORM = { name: '', amount: '', unit: '', category: 'その他', expiry: '', notes: '' }

export default function PantryPage() {
  const { value: items, update } = useLocalStorage<PantryItem[]>(KEYS.PANTRY, [])
  const [search, setSearch] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [editId, setEditId] = useState<string | null>(null)
  const [form, setForm] = useState(EMPTY_FORM)

  const today = new Date().toISOString().slice(0, 10)

  const filtered = useMemo(() => {
    let list = [...items]
    if (search) list = list.filter(i => i.name.includes(search))
    return list
  }, [items, search])

  const expiring = items.filter(i => {
    if (!i.expiryDate) return false
    const diff = Math.ceil((new Date(i.expiryDate).getTime() - Date.now()) / 86400000)
    return diff <= 3 && diff >= 0
  })

  const expired = items.filter(i => i.expiryDate && new Date(i.expiryDate) < new Date(today))

  const openAdd = () => { setEditId(null); setForm(EMPTY_FORM); setShowForm(true) }
  const openEdit = (item: PantryItem) => {
    setEditId(item.id)
    setForm({ name: item.name, amount: item.amount, unit: item.unit, category: item.category, expiry: item.expiryDate ?? '', notes: item.notes })
    setShowForm(true)
  }

  const save = () => {
    if (!form.name.trim()) return
    if (editId) {
      update(prev => prev.map(i => i.id === editId
        ? { ...i, name: form.name, amount: form.amount, unit: form.unit, category: form.category, expiryDate: form.expiry || undefined, notes: form.notes }
        : i))
    } else {
      const item: PantryItem = {
        id: generateId(), name: form.name, amount: form.amount, unit: form.unit,
        category: form.category, expiryDate: form.expiry || undefined,
        notes: form.notes, createdAt: new Date().toISOString(),
      }
      update(prev => [...prev, item])
    }
    setShowForm(false); setEditId(null)
  }

  const deleteItem = (id: string) => update(prev => prev.filter(i => i.id !== id))
  const grouped = CATEGORIES.reduce((acc, cat) => {
    const catItems = filtered.filter(i => i.category === cat)
    if (catItems.length) acc[cat] = catItems
    return acc
  }, {} as Record<string, PantryItem[]>)

  const getDaysLeft = (expiryDate: string) => {
    const diff = Math.ceil((new Date(expiryDate).getTime() - Date.now()) / 86400000)
    if (diff < 0) return <span className="text-xs text-red-400 font-medium">期限切れ</span>
    if (diff === 0) return <span className="text-xs text-red-400">今日が期限</span>
    if (diff <= 3) return <span className="text-xs text-amber-400">{diff}日後が期限</span>
    return <span className="text-xs text-[#555]">{expiryDate}</span>
  }

  const chipInactive = {
    background: 'var(--surface-2)',
    border: '1px solid rgba(255,255,255,.08)',
    color: '#888',
  }

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold flex items-center gap-2">
            <Package className="text-rose-400" size={22} /> パントリー
          </h1>
          <p className="text-sm text-[#777]">{items.length}品の在庫</p>
        </div>
        <button onClick={openAdd} className="btn-primary text-sm"><Plus size={16} />追加</button>
      </div>

      {(expiring.length > 0 || expired.length > 0) && (
        <div className="card space-y-2" style={{ background: 'rgba(245,158,11,.07)', border: '1px solid rgba(245,158,11,.18)' }}>
          <div className="flex items-center gap-2">
            <AlertTriangle size={16} className="text-amber-400" />
            <p className="text-sm font-semibold text-amber-400">賞味期限に注意</p>
          </div>
          {expired.map(i => <p key={i.id} className="text-xs text-red-400">• {i.name} — 期限切れ</p>)}
          {expiring.map(i => <p key={i.id} className="text-xs text-amber-400">• {i.name} — {getDaysLeft(i.expiryDate!)}</p>)}
        </div>
      )}

      <div className="relative">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[#555]" />
        <input className="input pl-9" placeholder="食材を検索" value={search} onChange={e => setSearch(e.target.value)} />
      </div>

      {showForm && (
        <div className="card space-y-3 animate-fade-in">
          <div className="flex justify-between items-center">
            <h2 className="font-bold text-[#ddd]">{editId ? '食材を編集' : '食材を追加'}</h2>
            <button onClick={() => setShowForm(false)} style={{ color: '#555' }}><X size={18} /></button>
          </div>
          <input className="input" placeholder="食材名 *" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
          <div className="flex gap-2">
            <input className="input flex-1" placeholder="数量" value={form.amount} onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} />
            <input className="input w-24" placeholder="単位" value={form.unit} onChange={e => setForm(f => ({ ...f, unit: e.target.value }))} />
          </div>
          <div className="flex flex-wrap gap-2">
            {CATEGORIES.map(c => (
              <button key={c} onClick={() => setForm(f => ({ ...f, category: c }))}
                className="px-3 py-1 rounded-full text-sm transition-all"
                style={form.category === c
                  ? { background: 'rgba(244,63,94,.18)', color: '#fb7185', border: '1px solid rgba(244,63,94,.25)' }
                  : chipInactive}>
                {c}
              </button>
            ))}
          </div>
          <div>
            <label className="text-sm text-[#888] block mb-1">賞味期限（任意）</label>
            <input type="date" className="input" value={form.expiry} onChange={e => setForm(f => ({ ...f, expiry: e.target.value }))} />
          </div>
          <input className="input" placeholder="メモ" value={form.notes} onChange={e => setForm(f => ({ ...f, notes: e.target.value }))} />
          <button onClick={save} disabled={!form.name.trim()} className="btn-primary w-full justify-center">
            {editId ? '更新する' : '追加する'}
          </button>
        </div>
      )}

      {items.length === 0 && (
        <div className="text-center py-12 text-[#444]">
          <Package size={40} className="mx-auto mb-3 opacity-30" />
          <p>パントリーは空です</p>
        </div>
      )}

      <div className="space-y-4">
        {Object.entries(grouped).map(([cat, catItems]) => (
          <div key={cat}>
            <div className="rounded-lg px-3 py-1.5 mb-2" style={{ background: 'var(--surface-2)' }}>
              <span className="text-xs font-semibold text-[#666]">{cat} ({catItems.length})</span>
            </div>
            <div className="space-y-1">
              {catItems.map(item => (
                <div key={item.id} className="flex items-center gap-3 p-3 rounded-xl"
                  style={{ background: 'var(--surface-2)', border: '1px solid rgba(255,255,255,.05)' }}>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-[#ddd]">{item.name}</p>
                    <div className="flex items-center gap-2 mt-0.5">
                      {(item.amount || item.unit) && <span className="text-xs text-[#555]">{item.amount} {item.unit}</span>}
                      {item.expiryDate && getDaysLeft(item.expiryDate)}
                      {item.notes && <span className="text-xs text-[#555] truncate">{item.notes}</span>}
                    </div>
                  </div>
                  <button onClick={() => openEdit(item)} className="transition-colors text-[#444] hover:text-[#888]">
                    <Edit3 size={15} />
                  </button>
                  <button onClick={() => deleteItem(item.id)} className="transition-colors text-[#444] hover:text-red-400">
                    <Trash2 size={15} />
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
