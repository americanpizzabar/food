'use client'

import { useState, useCallback } from 'react'
import ImageUpload from '@/components/ImageUpload'
import { useLocalStorage } from '@/hooks/useLocalStorage'
import { useApiKey } from '@/hooks/useApiKey'
import { KEYS, generateId } from '@/lib/storage'
import { MenuAnalysisResult, DishCookingGuide, MenuDish, DetailedStep } from '@/lib/types'
import {
  ScanSearch, ChefHat, Trash2, ChevronDown, ChevronUp,
  Star, AlertTriangle, Wine, Lightbulb, Utensils, Clock, Thermometer,
  Loader2, History, X, Sparkles,
} from 'lucide-react'

function normalizeGuide(input: unknown): DishCookingGuide {
  const g = (input ?? {}) as Record<string, unknown>
  return {
    id: typeof g.id === 'string' ? g.id : generateId(),
    menuAnalysisId: typeof g.menuAnalysisId === 'string' ? g.menuAnalysisId : '',
    dishName: typeof g.dishName === 'string' ? g.dishName : '',
    overview: typeof g.overview === 'string' ? g.overview : '',
    platingGuide: typeof g.platingGuide === 'string' ? g.platingGuide : '',
    analysisDate: typeof g.analysisDate === 'string' ? g.analysisDate : new Date().toISOString(),
    ingredients: Array.isArray(g.ingredients) ? g.ingredients : [],
    techniques: Array.isArray(g.techniques) ? g.techniques : [],
    steps: (Array.isArray(g.steps) ? g.steps : []).map((s: DetailedStep) => ({
      ...s,
      tips: Array.isArray(s.tips) ? s.tips : [],
    })),
    professionalTips: Array.isArray(g.professionalTips) ? g.professionalTips : [],
    variations: Array.isArray(g.variations) ? g.variations : [],
    drinkPairings: Array.isArray(g.drinkPairings) ? g.drinkPairings : [],
    commonMistakes: Array.isArray(g.commonMistakes) ? g.commonMistakes : [],
  }
}

export default function MenuAnalysisPage() {
  const apiKey = useApiKey()
  const [image, setImage] = useState<string>()
  const [scanning, setScanning] = useState(false)
  const [dishes, setDishes] = useState<MenuDish[]>([])
  const [selectedDish, setSelectedDish] = useState<string>('')
  const [customDish, setCustomDish] = useState('')
  const [generatingGuide, setGeneratingGuide] = useState(false)
  const [guide, setGuide] = useState<DishCookingGuide | null>(null)
  const [error, setError] = useState('')
  const [showHistory, setShowHistory] = useState(false)

  const { value: analyses, update: updateAnalyses } =
    useLocalStorage<MenuAnalysisResult[]>(KEYS.MENU_ANALYSES, [])
  const { value: guides, update: updateGuides } =
    useLocalStorage<DishCookingGuide[]>(KEYS.DISH_GUIDES, [])

  const scanMenu = useCallback(async () => {
    if (!image) return
    setScanning(true)
    setDishes([])
    setGuide(null)
    setError('')
    try {
      const res = await fetch('/api/menu-analysis?action=scan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ imageDataUrl: image, apiKey }),
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data.error)
      setDishes(data.dishes)

      const record: MenuAnalysisResult = {
        id: generateId(),
        imageDataUrl: '',
        detectedDishes: data.dishes,
        analysisDate: new Date().toISOString(),
      }
      updateAnalyses(prev => [record, ...prev].slice(0, 20))
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : '解析に失敗しました')
    } finally {
      setScanning(false)
    }
  }, [image, apiKey, updateAnalyses])

  const getGuide = useCallback(async (dishName: string) => {
    if (!dishName.trim()) return
    setGeneratingGuide(true)
    setGuide(null)
    setError('')
    try {
      const res = await fetch('/api/menu-analysis?action=guide', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ dishName, apiKey }),
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data.error)

      const newGuide = normalizeGuide({
        ...(data.guide || {}),
        id: generateId(),
        menuAnalysisId: '',
        dishName,
        analysisDate: new Date().toISOString(),
      })
      setGuide(newGuide)
      updateGuides(prev => [newGuide, ...prev].slice(0, 50))
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'ガイド生成に失敗しました')
    } finally {
      setGeneratingGuide(false)
    }
  }, [apiKey, updateGuides])

  const deleteAnalysis = (id: string) => updateAnalyses(prev => prev.filter(a => a.id !== id))
  const deleteGuide = (id: string) => updateGuides(prev => prev.filter(g => g.id !== id))

  return (
    <div className="space-y-5 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold flex items-center gap-2">
            <ScanSearch className="text-purple-400" size={22} />
            メニュー解析
          </h1>
          <p className="text-sm text-[#777] mt-0.5">
            レストランのメニューを撮影 → プロ級レシピを取得
          </p>
        </div>
        <button onClick={() => setShowHistory(!showHistory)} className="btn-secondary text-sm">
          <History size={16} />
          履歴
        </button>
      </div>

      {error && (
        <div className="rounded-xl p-3 text-sm flex items-center gap-2"
          style={{ background: 'rgba(239,68,68,.08)', border: '1px solid rgba(239,68,68,.18)', color: '#f87171' }}>
          <AlertTriangle size={16} />
          {error}
        </div>
      )}

      {/* History panel */}
      {showHistory && (
        <div className="card space-y-4">
          <h2 className="section-title">解析履歴</h2>

          {analyses.length === 0 && guides.length === 0 && (
            <p className="text-sm text-[#666] text-center py-4">履歴はありません</p>
          )}

          {guides.length > 0 && (
            <div>
              <h3 className="text-sm font-semibold text-[#aaa] mb-2">料理ガイド履歴</h3>
              <div className="space-y-2">
                {guides.map(g => (
                  <div key={g.id} className="flex items-center justify-between p-3 rounded-xl"
                    style={{ background: 'rgba(168,85,247,.08)', border: '1px solid rgba(168,85,247,.15)' }}>
                    <button
                      className="flex-1 text-left"
                      onClick={() => { setGuide(normalizeGuide(g)); setShowHistory(false) }}
                    >
                      <p className="font-medium text-[#e0e0e0] text-sm">{g.dishName}</p>
                      <p className="text-xs text-[#666]">
                        {new Date(g.analysisDate).toLocaleDateString('ja-JP')}
                      </p>
                    </button>
                    <button onClick={() => deleteGuide(g.id)} className="btn-danger ml-2">
                      <Trash2 size={14} />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {analyses.length > 0 && (
            <div>
              <h3 className="text-sm font-semibold text-[#aaa] mb-2">メニュースキャン履歴</h3>
              <div className="space-y-2">
                {analyses.map(a => (
                  <div key={a.id} className="flex items-center gap-3 p-3 rounded-xl"
                    style={{ background: 'var(--surface-2)', border: '1px solid rgba(255,255,255,.05)' }}>
                    <div className="flex-1">
                      <p className="text-xs text-[#777]">
                        {new Date(a.analysisDate).toLocaleDateString('ja-JP')} —
                        {a.detectedDishes.length}品検出
                      </p>
                      <p className="text-sm text-[#bbb] truncate">
                        {a.detectedDishes.slice(0, 3).map(d => d.name).join('、')}
                        {a.detectedDishes.length > 3 && '…'}
                      </p>
                    </div>
                    <button onClick={() => deleteAnalysis(a.id)} className="btn-danger">
                      <Trash2 size={14} />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Step 1 */}
      {!guide && (
        <div className="card space-y-4">
          <h2 className="section-title">STEP 1 — メニューを撮影</h2>
          <ImageUpload
            value={image}
            onChange={setImage}
            onClear={() => { setImage(undefined); setDishes([]) }}
            label="レストランのメニューを撮影またはアップロード"
          />
          {image && (
            <button onClick={scanMenu} disabled={scanning} className="btn-primary w-full justify-center">
              {scanning
                ? <><Loader2 size={18} className="animate-spin" />解析中…</>
                : <><ScanSearch size={18} />メニューを解析する</>}
            </button>
          )}
        </div>
      )}

      {/* Step 2 */}
      {dishes.length > 0 && !guide && (
        <div className="card space-y-4">
          <h2 className="section-title">
            STEP 2 — 料理を選択 ({dishes.length}品検出)
          </h2>

          <div className="space-y-2">
            {dishes.map((dish, i) => (
              <button
                key={i}
                onClick={() => setSelectedDish(dish.name)}
                className="w-full text-left p-3 rounded-xl transition-all"
                style={selectedDish === dish.name
                  ? { background: 'rgba(168,85,247,.12)', border: '2px solid rgba(168,85,247,.4)' }
                  : { background: 'var(--surface-2)', border: '2px solid rgba(255,255,255,.06)' }}
              >
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <p className="font-medium text-[#e0e0e0]">{dish.name}</p>
                    {dish.description && (
                      <p className="text-xs text-[#777] mt-0.5">{dish.description}</p>
                    )}
                  </div>
                  <div className="flex-shrink-0 text-right">
                    {dish.category && (
                      <span className="badge"
                        style={{ background: 'rgba(255,255,255,.06)', color: '#aaa' }}>
                        {dish.category}
                      </span>
                    )}
                    {dish.price && (
                      <p className="text-xs text-[#666] mt-1">{dish.price}</p>
                    )}
                  </div>
                </div>
              </button>
            ))}
          </div>

          <div className="border-t pt-4" style={{ borderColor: 'rgba(255,255,255,.06)' }}>
            <p className="text-sm text-[#888] mb-2">または料理名を直接入力</p>
            <div className="flex gap-2">
              <input
                className="input flex-1"
                placeholder="例: ビーフシチュー"
                value={customDish}
                onChange={e => setCustomDish(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && getGuide(customDish)}
              />
              <button
                onClick={() => getGuide(customDish)}
                disabled={!customDish.trim() || generatingGuide}
                className="btn-primary"
              >
                検索
              </button>
            </div>
          </div>

          {selectedDish && (
            <button
              onClick={() => getGuide(selectedDish)}
              disabled={generatingGuide}
              className="btn-primary w-full justify-center"
            >
              {generatingGuide
                ? <><Loader2 size={18} className="animate-spin" />プロ級レシピを生成中…</>
                : <><ChefHat size={18} />「{selectedDish}」のプロ級レシピを取得</>}
            </button>
          )}
        </div>
      )}

      {/* Step 3 */}
      {guide && <CookingGuide guide={guide} onClose={() => setGuide(null)} />}

      {/* Direct search */}
      {!guide && dishes.length === 0 && (
        <div className="card">
          <h2 className="section-title">料理名で直接検索</h2>
          <p className="text-sm text-[#777] mb-3">
            メニュー写真なしでも、料理名を入力してプロ級レシピを取得できます
          </p>
          <div className="flex gap-2">
            <input
              className="input flex-1"
              placeholder="料理名を入力（例: 牛フィレステーキ、パスタカルボナーラ）"
              value={customDish}
              onChange={e => setCustomDish(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && getGuide(customDish)}
            />
            <button
              onClick={() => getGuide(customDish)}
              disabled={!customDish.trim() || generatingGuide}
              className="btn-primary flex-shrink-0"
            >
              {generatingGuide ? <Loader2 size={18} className="animate-spin" /> : <ChefHat size={18} />}
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

// ---- Cooking Guide Display ----
function CookingGuide({ guide, onClose }: { guide: DishCookingGuide; onClose: () => void }) {
  const [expandedStep, setExpandedStep] = useState<number | null>(null)
  const [expandedSection, setExpandedSection] = useState<string | null>('ingredients')

  const toggleSection = (s: string) => setExpandedSection(prev => prev === s ? null : s)

  return (
    <div className="space-y-4 animate-fade-in">
      {/* Header */}
      <div className="card relative overflow-hidden p-5"
        style={{
          background: 'linear-gradient(135deg, #2a1a4a 0%, #1a0e2e 100%)',
          border: '1px solid rgba(168,85,247,.25)',
        }}>
        <div className="absolute -top-12 -right-8 w-40 h-40 rounded-full pointer-events-none"
          style={{ background: 'radial-gradient(circle, rgba(168,85,247,.18), transparent 70%)' }} />
        <div className="relative flex items-start justify-between gap-3">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <ChefHat size={20} className="text-purple-300" />
              <span className="text-sm font-medium text-purple-300">プロ級レシピ</span>
            </div>
            <h2 className="text-xl font-bold text-white">{guide.dishName}</h2>
          </div>
          <button onClick={onClose} className="p-1.5 bg-white/10 hover:bg-white/20 text-white rounded-lg transition-colors">
            <X size={18} />
          </button>
        </div>
        {guide.overview && (
          <p className="text-purple-100/80 text-sm mt-3 leading-relaxed relative">{guide.overview}</p>
        )}
      </div>

      {/* Ingredients */}
      <CollapsibleSection
        title="最高品質の食材"
        icon={<Utensils size={18} />}
        sectionKey="ingredients"
        expanded={expandedSection === 'ingredients'}
        onToggle={() => toggleSection('ingredients')}
      >
        <div className="space-y-3">
          {Array.isArray(guide.ingredients) && guide.ingredients.map((ing, i) => (
            <div key={i} className="p-3 rounded-xl"
              style={{ background: 'var(--surface-2)', border: '1px solid rgba(255,255,255,.04)' }}>
              <div className="flex items-start justify-between gap-2">
                <div className="flex-1">
                  <p className="font-semibold text-[#e0e0e0]">
                    {ing.name}
                    <span className="ml-2 text-sm font-normal text-[#777]">
                      {ing.amount} {ing.unit}
                    </span>
                  </p>
                  {ing.quality && (
                    <p className="text-xs mt-1 text-purple-300 font-medium">⭐ {ing.quality}</p>
                  )}
                  {ing.whereToFind && (
                    <p className="text-xs mt-1 text-blue-300">🏪 {ing.whereToFind}</p>
                  )}
                  {ing.notes && (
                    <p className="text-xs mt-1 text-[#888]">💡 {ing.notes}</p>
                  )}
                  {ing.substitute && (
                    <p className="text-xs mt-1 text-emerald-400">↔️ 代替: {ing.substitute}</p>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </CollapsibleSection>

      {/* Techniques */}
      {Array.isArray(guide.techniques) && guide.techniques.length > 0 && (
        <CollapsibleSection
          title="プロの調理技法"
          icon={<Star size={18} />}
          sectionKey="techniques"
          expanded={expandedSection === 'techniques'}
          onToggle={() => toggleSection('techniques')}
        >
          <div className="space-y-3">
            {guide.techniques.map((t, i) => (
              <div key={i} className="p-3 rounded-xl"
                style={{ background: 'rgba(232,184,75,.08)', border: '1px solid rgba(232,184,75,.18)' }}>
                <p className="font-semibold text-[#e0e0e0] mb-1">🔥 {t.name}</p>
                <p className="text-sm text-[#bbb] mb-1">{t.description}</p>
                <p className="text-xs text-amber-400">なぜ重要？ {t.whyImportant}</p>
              </div>
            ))}
          </div>
        </CollapsibleSection>
      )}

      {/* Steps */}
      <CollapsibleSection
        title="詳細な調理手順"
        icon={<Clock size={18} />}
        sectionKey="steps"
        expanded={expandedSection === 'steps'}
        onToggle={() => toggleSection('steps')}
      >
        <div className="space-y-3">
          {Array.isArray(guide.steps) && guide.steps.map((step, i) => (
            <div key={i} className="rounded-xl overflow-hidden"
              style={{ border: '1px solid rgba(255,255,255,.06)' }}>
              <button
                className="w-full flex items-center gap-3 p-3 text-left transition-colors hover:bg-[rgba(255,255,255,.03)]"
                onClick={() => setExpandedStep(expandedStep === i ? null : i)}
              >
                <span className="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0"
                  style={{ background: 'rgba(168,85,247,.18)', color: '#c084fc' }}>
                  {step.stepNumber}
                </span>
                <span className="font-medium text-[#e0e0e0] flex-1">{step.title}</span>
                <div className="flex items-center gap-2 flex-shrink-0">
                  {step.duration && (
                    <span className="badge text-xs"
                      style={{ background: 'rgba(59,130,246,.12)', color: '#60a5fa' }}>
                      <Clock size={10} className="mr-1" />{step.duration}
                    </span>
                  )}
                  {expandedStep === i
                    ? <ChevronUp size={16} className="text-[#888]" />
                    : <ChevronDown size={16} className="text-[#888]" />}
                </div>
              </button>
              {expandedStep === i && (
                <div className="px-4 pb-4 space-y-3"
                  style={{ background: 'var(--surface-2)' }}>
                  <p className="text-sm text-[#bbb] leading-relaxed pt-3">{step.description}</p>
                  {step.temperature && (
                    <div className="flex items-center gap-1 text-xs text-orange-400">
                      <Thermometer size={14} />
                      温度: {step.temperature}
                    </div>
                  )}
                  {Array.isArray(step.tips) && step.tips.length > 0 && (
                    <div className="rounded-lg p-3"
                      style={{ background: 'rgba(16,185,129,.08)', border: '1px solid rgba(16,185,129,.15)' }}>
                      <p className="text-xs font-semibold text-emerald-400 mb-1">💡 プロのコツ</p>
                      {step.tips.map((tip, j) => (
                        <p key={j} className="text-xs text-emerald-400/80">• {tip}</p>
                      ))}
                    </div>
                  )}
                  {step.warnings && (
                    <div className="rounded-lg p-3 flex gap-2"
                      style={{ background: 'rgba(239,68,68,.08)', border: '1px solid rgba(239,68,68,.15)' }}>
                      <AlertTriangle size={14} className="text-red-400 flex-shrink-0 mt-0.5" />
                      <p className="text-xs text-red-400">{step.warnings}</p>
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      </CollapsibleSection>

      {/* Professional tips */}
      {Array.isArray(guide.professionalTips) && guide.professionalTips.length > 0 && (
        <CollapsibleSection
          title="シェフの秘訣"
          icon={<Lightbulb size={18} />}
          sectionKey="tips"
          expanded={expandedSection === 'tips'}
          onToggle={() => toggleSection('tips')}
        >
          <div className="space-y-2">
            {guide.professionalTips.map((tip, i) => (
              <div key={i} className="flex gap-2 p-3 rounded-xl"
                style={{ background: 'rgba(232,184,75,.08)', border: '1px solid rgba(232,184,75,.15)' }}>
                <span className="text-amber-400 flex-shrink-0">✨</span>
                <p className="text-sm text-[#bbb]">{tip}</p>
              </div>
            ))}
          </div>
        </CollapsibleSection>
      )}

      {/* Plating */}
      {guide.platingGuide && (
        <CollapsibleSection
          title="盛り付けガイド"
          icon={<Utensils size={18} />}
          sectionKey="plating"
          expanded={expandedSection === 'plating'}
          onToggle={() => toggleSection('plating')}
        >
          <p className="text-sm text-[#bbb] leading-relaxed">{guide.platingGuide}</p>
        </CollapsibleSection>
      )}

      {/* Drinks */}
      {Array.isArray(guide.drinkPairings) && guide.drinkPairings.length > 0 && (
        <CollapsibleSection
          title="ドリンクペアリング"
          icon={<Wine size={18} />}
          sectionKey="drinks"
          expanded={expandedSection === 'drinks'}
          onToggle={() => toggleSection('drinks')}
        >
          <div className="space-y-2">
            {guide.drinkPairings.map((drink, i) => (
              <p key={i} className="text-sm text-[#bbb]">🍷 {drink}</p>
            ))}
          </div>
        </CollapsibleSection>
      )}

      {/* Mistakes */}
      {Array.isArray(guide.commonMistakes) && guide.commonMistakes.length > 0 && (
        <CollapsibleSection
          title="よくある失敗と対処法"
          icon={<AlertTriangle size={18} />}
          sectionKey="mistakes"
          expanded={expandedSection === 'mistakes'}
          onToggle={() => toggleSection('mistakes')}
        >
          <div className="space-y-2">
            {guide.commonMistakes.map((m, i) => (
              <div key={i} className="flex gap-2 p-3 rounded-xl"
                style={{ background: 'rgba(239,68,68,.08)', border: '1px solid rgba(239,68,68,.15)' }}>
                <span className="text-red-400 flex-shrink-0">⚠️</span>
                <p className="text-sm text-[#bbb]">{m}</p>
              </div>
            ))}
          </div>
        </CollapsibleSection>
      )}

      {/* Variations */}
      {Array.isArray(guide.variations) && guide.variations.length > 0 && (
        <CollapsibleSection
          title="アレンジ・バリエーション"
          icon={<Sparkles size={18} />}
          sectionKey="variations"
          expanded={expandedSection === 'variations'}
          onToggle={() => toggleSection('variations')}
        >
          <div className="space-y-2">
            {guide.variations.map((v, i) => (
              <p key={i} className="text-sm text-[#bbb]">🔄 {v}</p>
            ))}
          </div>
        </CollapsibleSection>
      )}

      <button onClick={onClose} className="btn-secondary w-full justify-center">
        別の料理を検索する
      </button>
    </div>
  )
}

function CollapsibleSection({
  title, icon, sectionKey, expanded, onToggle, children
}: {
  title: string
  icon: React.ReactNode
  sectionKey: string
  expanded: boolean
  onToggle: () => void
  children: React.ReactNode
}) {
  return (
    <div className="card">
      <button
        className="w-full flex items-center justify-between gap-3 text-left"
        onClick={onToggle}
      >
        <div className="flex items-center gap-2 text-[#e0e0e0] font-semibold">
          <span style={{ color: '#e8b84b' }}>{icon}</span>
          {title}
        </div>
        {expanded
          ? <ChevronUp size={18} className="text-[#888]" />
          : <ChevronDown size={18} className="text-[#888]" />}
      </button>
      {expanded && <div className="mt-4">{children}</div>}
    </div>
  )
}
