'use client'

import { useState, useMemo } from 'react'
import { useLocalStorage } from '@/hooks/useLocalStorage'
import { KEYS, generateId } from '@/lib/storage'
import { MealPlan } from '@/lib/types'
import { Calendar, ChevronLeft, ChevronRight, Plus, Trash2, X } from 'lucide-react'

const MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'] as const
const MEAL_LABELS: Record<string, string> = { BREAKFAST: '朝食', LUNCH: '昼食', DINNER: '夕食', SNACK: '間食' }
const DAY_LABELS = ['月', '火', '水', '木', '金', '土', '日']
const MEAL_COLORS: Record<string, string> = {
  BREAKFAST: 'bg-yellow-100 text-yellow-700',
  LUNCH: 'bg-blue-100 text-blue-700',
  DINNER: 'bg-purple-100 text-purple-700',
  SNACK: 'bg-green-100 text-green-700',
}

function getMonday(date: Date): Date {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  d.setDate(diff)
  d.setHours(0, 0, 0, 0)
  return d
}

function toDateStr(d: Date) { return d.toISOString().slice(0, 10) }

export default function CalendarPage() {
  const { value: plans, update } = useLocalStorage<MealPlan[]>(KEYS.MEAL_PLANS, [])
  const [weekStart, setWeekStart] = useState(() => getMonday(new Date()))
  const [dialog, setDialog] = useState<{ date: string; mealType: typeof MEAL_TYPES[number]; plan?: MealPlan } | null>(null)
  const [formName, setFormName] = useState('')
  const [formNotes, setFormNotes] = useState('')

  const weekDays = useMemo(() =>
    Array.from({ length: 7 }, (_, i) => {
      const d = new Date(weekStart); d.setDate(d.getDate() + i)
      return { dateStr: toDateStr(d), label: DAY_LABELS[i], day: d.getDate() }
    }), [weekStart])

  const todayStr = toDateStr(new Date())

  const plansMap = useMemo(() => {
    const m: Record<string, MealPlan[]> = {}
    plans.forEach(p => {
      const k = `${p.date}__${p.mealType}`
      m[k] = [...(m[k] ?? []), p]
    })
    return m
  }, [plans])

  const prevWeek = () => { const d = new Date(weekStart); d.setDate(d.getDate() - 7); setWeekStart(d) }
  const nextWeek = () => { const d = new Date(weekStart); d.setDate(d.getDate() + 7); setWeekStart(d) }
  const goToday = () => setWeekStart(getMonday(new Date()))

  const openAdd = (date: string, mealType: typeof MEAL_TYPES[number]) => {
    setDialog({ date, mealType })
    setFormName(''); setFormNotes('')
  }

  const openEdit = (plan: MealPlan) => {
    setDialog({ date: plan.date, mealType: plan.mealType, plan })
    setFormName(plan.recipeName); setFormNotes(plan.notes)
  }

  const savePlan = () => {
    if (!dialog || !formName.trim()) return
    if (dialog.plan) {
      update(prev => prev.map(p => p.id === dialog.plan!.id ? { ...p, recipeName: formName, notes: formNotes } : p))
    } else {
      const plan: MealPlan = {
        id: generateId(), date: dialog.date, mealType: dialog.mealType,
        recipeName: formName, notes: formNotes, isRoutine: false,
      }
      update(prev => [...prev, plan])
    }
    setDialog(null)
  }

  const deletePlan = (id: string) => { update(prev => prev.filter(p => p.id !== id)); setDialog(null) }

  const weekLabel = `${weekDays[0].dateStr} 〜 ${weekDays[6].dateStr}`

  return (
    <div className="space-y-4 animate-fade-in">
      <div>
        <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <Calendar className="text-indigo-500" size={22} /> 献立カレンダー
        </h1>
      </div>

      {/* Week nav */}
      <div className="flex items-center justify-between">
        <button onClick={prevWeek} className="p-2 rounded-xl hover:bg-gray-100">
          <ChevronLeft size={20} />
        </button>
        <div className="text-center">
          <p className="text-sm font-semibold text-gray-700">{weekLabel}</p>
          <button onClick={goToday} className="text-xs text-indigo-500 hover:underline">今週に戻る</button>
        </div>
        <button onClick={nextWeek} className="p-2 rounded-xl hover:bg-gray-100">
          <ChevronRight size={20} />
        </button>
      </div>

      {/* Calendar grid */}
      <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden shadow-sm">
        {/* Header */}
        <div className="grid grid-cols-8 bg-gray-50 border-b">
          <div className="p-2 text-xs text-gray-400 font-medium"></div>
          {weekDays.map(({ dateStr, label, day }) => (
            <div key={dateStr} className={`p-2 text-center ${dateStr === todayStr ? 'bg-indigo-50' : ''}`}>
              <p className={`text-xs font-medium ${label === '土' ? 'text-blue-500' : label === '日' ? 'text-red-500' : 'text-gray-500'}`}>{label}</p>
              <p className={`text-sm font-bold mt-0.5 ${dateStr === todayStr ? 'text-indigo-600' : 'text-gray-700'}`}>{day}</p>
            </div>
          ))}
        </div>

        {/* Rows for each meal type */}
        {MEAL_TYPES.map(mealType => (
          <div key={mealType} className="grid grid-cols-8 border-b last:border-0">
            <div className="p-2 flex items-center justify-center border-r bg-gray-50">
              <span className="text-xs font-medium text-gray-500 writing-mode-vertical">{MEAL_LABELS[mealType]}</span>
            </div>
            {weekDays.map(({ dateStr }) => {
              const key = `${dateStr}__${mealType}`
              const dayPlans = plansMap[key] ?? []
              return (
                <div key={dateStr}
                  className={`min-h-[56px] p-1 border-r last:border-r-0 ${dateStr === todayStr ? 'bg-indigo-50/30' : ''}`}>
                  {dayPlans.map(p => (
                    <button key={p.id} onClick={() => openEdit(p)}
                      className={`w-full text-left text-[10px] font-medium px-1.5 py-1 rounded-md mb-1 leading-tight ${MEAL_COLORS[mealType]}`}>
                      {p.recipeName}
                    </button>
                  ))}
                  <button onClick={() => openAdd(dateStr, mealType)}
                    className="w-full flex items-center justify-center text-gray-200 hover:text-gray-400 transition-colors h-5">
                    <Plus size={12} />
                  </button>
                </div>
              )
            })}
          </div>
        ))}
      </div>

      {/* Legend */}
      <div className="flex flex-wrap gap-2">
        {MEAL_TYPES.map(t => (
          <span key={t} className={`badge ${MEAL_COLORS[t]}`}>{MEAL_LABELS[t]}</span>
        ))}
      </div>

      {/* Dialog */}
      {dialog && (
        <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/30">
          <div className="bg-white rounded-t-2xl w-full max-w-md p-5 space-y-4 animate-fade-in">
            <div className="flex justify-between items-center">
              <h2 className="font-bold text-gray-800">
                {dialog.date} — {MEAL_LABELS[dialog.mealType]}
              </h2>
              <button onClick={() => setDialog(null)}><X size={20} /></button>
            </div>
            <input className="input" placeholder="料理名 *" value={formName} onChange={e => setFormName(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && savePlan()} autoFocus />
            <input className="input" placeholder="メモ（任意）" value={formNotes} onChange={e => setFormNotes(e.target.value)} />
            <div className="flex gap-2">
              <button onClick={savePlan} disabled={!formName.trim()} className="btn-primary flex-1 justify-center">
                {dialog.plan ? '更新' : '追加'}
              </button>
              {dialog.plan && (
                <button onClick={() => deletePlan(dialog.plan!.id)} className="btn-danger">
                  <Trash2 size={16} />削除
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
