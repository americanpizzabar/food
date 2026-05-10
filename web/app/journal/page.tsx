'use client'

import { useState, useMemo } from 'react'
import { useLocalStorage } from '@/hooks/useLocalStorage'
import { KEYS, generateId } from '@/lib/storage'
import { JournalEntry } from '@/lib/types'
import { BookOpen, Plus, Trash2, Search, Star, ChevronDown, X } from 'lucide-react'

const MOODS = ['😊', '😴', '😰', '⚡', '😌', '😐']
const MOOD_LABELS = ['嬉しい', '疲れた', 'ストレス', '元気', 'リラックス', '普通']
const CONDITIONS = ['絶好調', '良好', '普通', '不調', '体調不良']
const FEELINGS = ['とても良い', '良い', '普通', '少し不調', '不調']

export default function JournalPage() {
  const { value: entries, update } = useLocalStorage<JournalEntry[]>(KEYS.JOURNAL, [])
  const [search, setSearch] = useState('')
  const [sortBy, setSortBy] = useState<'date' | 'rating'>('date')
  const [sortDir, setSortDir] = useState<'desc' | 'asc'>('desc')
  const [filterRating, setFilterRating] = useState(0)
  const [showForm, setShowForm] = useState(false)
  const [expandedId, setExpandedId] = useState<string | null>(null)

  // Form state
  const [form, setForm] = useState({
    recipeName: '', rating: 3, comment: '', mood: '', health: '', feeling: '',
    date: new Date().toISOString().slice(0, 10),
  })

  const filtered = useMemo(() => {
    let list = [...entries]
    if (search) list = list.filter(e => e.recipeName.includes(search) || e.comment.includes(search))
    if (filterRating) list = list.filter(e => e.rating === filterRating)
    list.sort((a, b) => {
      const cmp = sortBy === 'date' ? a.date.localeCompare(b.date) : a.rating - b.rating
      return sortDir === 'desc' ? -cmp : cmp
    })
    return list
  }, [entries, search, filterRating, sortBy, sortDir])

  const save = () => {
    if (!form.recipeName.trim()) return
    const entry: JournalEntry = {
      id: generateId(), recipeName: form.recipeName, date: form.date,
      rating: form.rating, comment: form.comment, mood: form.mood,
      healthCondition: form.health, feelingAfter: form.feeling,
      tags: [], createdAt: new Date().toISOString(),
    }
    update(prev => [entry, ...prev])
    setForm({ recipeName: '', rating: 3, comment: '', mood: '', health: '', feeling: '', date: new Date().toISOString().slice(0, 10) })
    setShowForm(false)
  }

  const deleteEntry = (id: string) => update(prev => prev.filter(e => e.id !== id))

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            <BookOpen className="text-green-500" size={22} /> 食事日記
          </h1>
          <p className="text-sm text-gray-500">{entries.length}件の記録</p>
        </div>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary text-sm">
          <Plus size={16} />記録する
        </button>
      </div>

      {/* Add form */}
      {showForm && (
        <div className="card space-y-4 animate-fade-in">
          <div className="flex justify-between items-center">
            <h2 className="font-bold text-gray-800">新しい記録</h2>
            <button onClick={() => setShowForm(false)}><X size={18} className="text-gray-400" /></button>
          </div>
          <input className="input" placeholder="料理名 *" value={form.recipeName} onChange={e => setForm(f => ({ ...f, recipeName: e.target.value }))} />
          <input type="date" className="input" value={form.date} onChange={e => setForm(f => ({ ...f, date: e.target.value }))} />

          <div>
            <p className="text-sm font-semibold text-gray-700 mb-2">評価</p>
            <div className="flex gap-1">
              {[1,2,3,4,5].map(n => (
                <button key={n} onClick={() => setForm(f => ({ ...f, rating: n }))}>
                  <Star size={28} className={n <= form.rating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-200'} />
                </button>
              ))}
            </div>
          </div>

          <div>
            <p className="text-sm font-semibold text-gray-700 mb-2">気分</p>
            <div className="flex flex-wrap gap-2">
              {MOODS.map((m, i) => (
                <button key={m} onClick={() => setForm(f => ({ ...f, mood: f.mood === m ? '' : m }))}
                  className={`px-3 py-1.5 rounded-full border text-sm transition-all ${form.mood === m ? 'bg-orange-500 text-white border-orange-500' : 'bg-white border-gray-200'}`}>
                  {m} {MOOD_LABELS[i]}
                </button>
              ))}
            </div>
          </div>

          <div>
            <p className="text-sm font-semibold text-gray-700 mb-2">体調</p>
            <div className="flex flex-wrap gap-2">
              {CONDITIONS.map(c => (
                <button key={c} onClick={() => setForm(f => ({ ...f, health: f.health === c ? '' : c }))}
                  className={`px-3 py-1.5 rounded-full border text-sm transition-all ${form.health === c ? 'bg-blue-500 text-white border-blue-500' : 'bg-white border-gray-200'}`}>
                  {c}
                </button>
              ))}
            </div>
          </div>

          <div>
            <p className="text-sm font-semibold text-gray-700 mb-2">食後の体調</p>
            <div className="flex flex-wrap gap-2">
              {FEELINGS.map(f => (
                <button key={f} onClick={() => setForm(prev => ({ ...prev, feeling: prev.feeling === f ? '' : f }))}
                  className={`px-3 py-1.5 rounded-full border text-sm transition-all ${form.feeling === f ? 'bg-green-500 text-white border-green-500' : 'bg-white border-gray-200'}`}>
                  {f}
                </button>
              ))}
            </div>
          </div>

          <textarea className="input" rows={3} placeholder="感想・メモ" value={form.comment} onChange={e => setForm(f => ({ ...f, comment: e.target.value }))} />
          <button onClick={save} disabled={!form.recipeName.trim()} className="btn-primary w-full justify-center">保存する</button>
        </div>
      )}

      {/* Search & filters */}
      <div className="space-y-2">
        <div className="relative">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input className="input pl-9" placeholder="料理名・コメントで検索" value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <div className="flex gap-2 flex-wrap">
          <select className="input flex-1 text-sm py-1.5" value={sortBy} onChange={e => setSortBy(e.target.value as 'date' | 'rating')}>
            <option value="date">日付順</option>
            <option value="rating">評価順</option>
          </select>
          <button onClick={() => setSortDir(d => d === 'desc' ? 'asc' : 'desc')}
            className="btn-secondary text-sm px-3">{sortDir === 'desc' ? '↓' : '↑'}</button>
          <div className="flex gap-1">
            {[0,1,2,3,4,5].map(n => (
              <button key={n} onClick={() => setFilterRating(filterRating === n ? 0 : n)}
                className={`px-2 py-1 rounded-lg text-sm border transition-all ${filterRating === n ? 'bg-yellow-400 border-yellow-400 text-white' : 'bg-white border-gray-200 text-gray-500'}`}>
                {n === 0 ? '全て' : '★'.repeat(n)}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Entries */}
      {filtered.length === 0 && (
        <div className="text-center py-12 text-gray-400">
          <BookOpen size={40} className="mx-auto mb-3 opacity-30" />
          <p>記録がありません</p>
        </div>
      )}

      <div className="space-y-3">
        {filtered.map(entry => (
          <div key={entry.id} className="card">
            <div className="flex items-start justify-between gap-2">
              <div className="flex-1">
                <button className="text-left w-full" onClick={() => setExpandedId(expandedId === entry.id ? null : entry.id)}>
                  <div className="flex items-center justify-between">
                    <h3 className="font-semibold text-gray-800">{entry.recipeName}</h3>
                    <ChevronDown size={16} className={`text-gray-400 transition-transform ${expandedId === entry.id ? 'rotate-180' : ''}`} />
                  </div>
                  <div className="flex items-center gap-3 mt-1">
                    <div className="flex">
                      {[1,2,3,4,5].map(n => <Star key={n} size={14} className={n <= entry.rating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-200'} />)}
                    </div>
                    <span className="text-xs text-gray-400">{entry.date}</span>
                    {entry.mood && <span className="text-sm">{entry.mood}</span>}
                  </div>
                </button>
              </div>
              <button onClick={() => deleteEntry(entry.id)} className="btn-danger ml-2 px-2 py-1">
                <Trash2 size={14} />
              </button>
            </div>

            {expandedId === entry.id && (
              <div className="mt-3 pt-3 border-t space-y-2 text-sm animate-fade-in">
                {entry.comment && <p className="text-gray-700">{entry.comment}</p>}
                <div className="flex flex-wrap gap-2 text-xs text-gray-500">
                  {entry.healthCondition && <span className="badge bg-blue-50 text-blue-600">体調: {entry.healthCondition}</span>}
                  {entry.feelingAfter && <span className="badge bg-green-50 text-green-600">食後: {entry.feelingAfter}</span>}
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
