'use client'

import { useState, useCallback } from 'react'
import ImageUpload from '@/components/ImageUpload'
import { useLocalStorage } from '@/hooks/useLocalStorage'
import { useApiKey } from '@/hooks/useApiKey'
import { KEYS, generateId } from '@/lib/storage'
import { MenuAnalysisResult, DishCookingGuide, MenuDish } from '@/lib/types'
import {
  ScanSearch, ChefHat, Trash2, ChevronDown, ChevronUp,
  Star, AlertTriangle, Wine, Lightbulb, Utensils, Clock, Thermometer,
  Loader2, History, X, Sparkles,
} from 'lucide-react'

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

  // Step 1: scan menu image
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

      // Save this scan to history
      const record: MenuAnalysisResult = {
        id: generateId(),
        imageDataUrl: image,
        detectedDishes: data.dishes,
        analysisDate: new Date().toISOString(),
      }
      updateAnalyses(prev => [record, ...prev].slice(0, 30))
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : '解析に失敗しました')
    } finally {
      setScanning(false)
    }
  }, [image, apiKey, updateAnalyses])

  // Step 2: generate cooking guide
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

      const newGuide: DishCookingGuide = {
        ...data.guide,
        id: generateId(),
        menuAnalysisId: '',
        dishName,
        analysisDate: new Date().toISOString(),
      }
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
          <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            <ScanSearch className="text-purple-500" size={22} />
            メニュー解析
          </h1>
          <p className="text-sm text-gray-500 mt-0.5">
            レストランのメニューを撮影 → プロ級レシピを取得
          </p>
        </div>
        <button
          onClick={() => setShowHistory(!showHistory)}
          className="btn-secondary text-sm"
        >
          <History size={16} />
          履歴
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-3 text-sm text-red-600 flex items-center gap-2">
          <AlertTriangle size={16} />
          {error}
        </div>
      )}

      {/* History panel */}
      {showHistory && (
        <div className="card space-y-4">
          <h2 className="section-title">解析履歴</h2>

          {analyses.length === 0 && guides.length === 0 && (
            <p className="text-sm text-gray-400 text-center py-4">履歴はありません</p>
          )}

          {guides.length > 0 && (
            <div>
              <h3 className="text-sm font-semibold text-gray-600 mb-2">料理ガイド履歴</h3>
              <div className="space-y-2">
                {guides.map(g => (
                  <div key={g.id} className="flex items-center justify-between p-3 bg-purple-50 rounded-xl">
                    <button
                      className="flex-1 text-left"
                      onClick={() => { setGuide(g); setShowHistory(false) }}
                    >
                      <p className="font-medium text-gray-800 text-sm">{g.dishName}</p>
                      <p className="text-xs text-gray-400">
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
              <h3 className="text-sm font-semibold text-gray-600 mb-2">メニュースキャン履歴</h3>
              <div className="space-y-2">
                {analyses.map(a => (
                  <div key={a.id} className="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                    <div className="flex-1">
                      <p className="text-xs text-gray-500">
                        {new Date(a.analysisDate).toLocaleDateString('ja-JP')} —
                        {a.detectedDishes.length}品検出
                      </p>
                      <p className="text-sm text-gray-700 truncate">
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

      {/* Step 1: Upload menu */}
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
            <button
              onClick={scanMenu}
              disabled={scanning}
              className="btn-primary w-full justify-center"
            >
              {scanning
                ? <><Loader2 size={18} className="animate-spin" />解析中…</>
                : <><ScanSearch size={18} />メニューを解析する</>
              }
            </button>
          )}
        </div>
      )}

      {/* Step 2: Select dish */}
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
                className={`w-full text-left p-3 rounded-xl border-2 transition-all ${
                  selectedDish === dish.name
                    ? 'border-purple-400 bg-purple-50'
                    : 'border-gray-200 hover:border-gray-300 bg-white'
                }`}
              >
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <p className="font-medium text-gray-800">{dish.name}</p>
                    {dish.description && (
                      <p className="text-xs text-gray-500 mt-0.5">{dish.description}</p>
                    )}
                  </div>
                  <div className="flex-shrink-0 text-right">
                    {dish.category && (
                      <span className="badge bg-gray-100 text-gray-600">{dish.category}</span>
                    )}
                    {dish.price && (
                      <p className="text-xs text-gray-400 mt-1">{dish.price}</p>
                    )}
                  </div>
                </div>
              </button>
            ))}
          </div>

          {/* Manual dish input */}
          <div className="border-t pt-4">
            <p className="text-sm text-gray-500 mb-2">または料理名を直接入力</p>
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
                : <><ChefHat size={18} />「{selectedDish}」のプロ級レシピを取得</>
              }
            </button>
          )}
        </div>
      )}

      {/* Step 3: Cooking Guide */}
      {guide && <CookingGuide guide={guide} onClose={() => setGuide(null)} />}

      {/* Direct search (no image) */}
      {!guide && dishes.length === 0 && (
        <div className="card">
          <h2 className="section-title">料理名で直接検索</h2>
          <p className="text-sm text-gray-500 mb-3">
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
              {generatingGuide
                ? <Loader2 size={18} className="animate-spin" />
                : <ChefHat size={18} />
              }
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
      <div className="card bg-gradient-to-br from-purple-600 to-purple-700 text-white border-0">
        <div className="flex items-start justify-between gap-3">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <ChefHat size={20} />
              <span className="text-sm font-medium text-purple-200">プロ級レシピ</span>
            </div>
            <h2 className="text-xl font-bold">{guide.dishName}</h2>
          </div>
          <button onClick={onClose} className="p-1.5 bg-white/20 hover:bg-white/30 rounded-lg transition-colors">
            <X size={18} />
          </button>
        </div>
        {guide.overview && (
          <p className="text-purple-100 text-sm mt-3 leading-relaxed">{guide.overview}</p>
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
          {guide.ingredients?.map((ing, i) => (
            <div key={i} className="p-3 bg-gray-50 rounded-xl">
              <div className="flex items-start justify-between gap-2">
                <div className="flex-1">
                  <p className="font-semibold text-gray-800">
                    {ing.name}
                    <span className="ml-2 text-sm font-normal text-gray-500">
                      {ing.amount} {ing.unit}
                    </span>
                  </p>
                  {ing.quality && (
                    <p className="text-xs mt-1 text-purple-600 font-medium">⭐ {ing.quality}</p>
                  )}
                  {ing.whereToFind && (
                    <p className="text-xs mt-1 text-blue-600">🏪 {ing.whereToFind}</p>
                  )}
                  {ing.notes && (
                    <p className="text-xs mt-1 text-gray-500">💡 {ing.notes}</p>
                  )}
                  {ing.substitute && (
                    <p className="text-xs mt-1 text-green-600">↔️ 代替: {ing.substitute}</p>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </CollapsibleSection>

      {/* Techniques */}
      {guide.techniques?.length > 0 && (
        <CollapsibleSection
          title="プロの調理技法"
          icon={<Star size={18} />}
          sectionKey="techniques"
          expanded={expandedSection === 'techniques'}
          onToggle={() => toggleSection('techniques')}
        >
          <div className="space-y-3">
            {guide.techniques.map((t, i) => (
              <div key={i} className="p-3 bg-yellow-50 rounded-xl border border-yellow-100">
                <p className="font-semibold text-gray-800 mb-1">🔥 {t.name}</p>
                <p className="text-sm text-gray-700 mb-1">{t.description}</p>
                <p className="text-xs text-yellow-700">なぜ重要？ {t.whyImportant}</p>
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
          {guide.steps?.map((step, i) => (
            <div key={i} className="border border-gray-100 rounded-xl overflow-hidden">
              <button
                className="w-full flex items-center gap-3 p-3 text-left hover:bg-gray-50"
                onClick={() => setExpandedStep(expandedStep === i ? null : i)}
              >
                <span className="w-8 h-8 bg-purple-100 text-purple-700 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0">
                  {step.stepNumber}
                </span>
                <span className="font-medium text-gray-800 flex-1">{step.title}</span>
                <div className="flex items-center gap-2 flex-shrink-0">
                  {step.duration && (
                    <span className="badge bg-blue-50 text-blue-600 text-xs">
                      <Clock size={10} className="mr-1" />{step.duration}
                    </span>
                  )}
                  {expandedStep === i ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                </div>
              </button>
              {expandedStep === i && (
                <div className="px-4 pb-4 space-y-3 bg-white">
                  <p className="text-sm text-gray-700 leading-relaxed">{step.description}</p>
                  {step.temperature && (
                    <div className="flex items-center gap-1 text-xs text-orange-600">
                      <Thermometer size={14} />
                      温度: {step.temperature}
                    </div>
                  )}
                  {step.tips?.length > 0 && (
                    <div className="bg-green-50 rounded-lg p-3">
                      <p className="text-xs font-semibold text-green-700 mb-1">💡 プロのコツ</p>
                      {step.tips.map((tip, j) => (
                        <p key={j} className="text-xs text-green-700">• {tip}</p>
                      ))}
                    </div>
                  )}
                  {step.warnings && (
                    <div className="bg-red-50 rounded-lg p-3 flex gap-2">
                      <AlertTriangle size={14} className="text-red-500 flex-shrink-0 mt-0.5" />
                      <p className="text-xs text-red-600">{step.warnings}</p>
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      </CollapsibleSection>

      {/* Professional tips */}
      {guide.professionalTips?.length > 0 && (
        <CollapsibleSection
          title="シェフの秘訣"
          icon={<Lightbulb size={18} />}
          sectionKey="tips"
          expanded={expandedSection === 'tips'}
          onToggle={() => toggleSection('tips')}
        >
          <div className="space-y-2">
            {guide.professionalTips.map((tip, i) => (
              <div key={i} className="flex gap-2 p-3 bg-amber-50 rounded-xl">
                <span className="text-amber-500 flex-shrink-0">✨</span>
                <p className="text-sm text-gray-700">{tip}</p>
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
          <p className="text-sm text-gray-700 leading-relaxed">{guide.platingGuide}</p>
        </CollapsibleSection>
      )}

      {/* Drink pairings */}
      {guide.drinkPairings?.length > 0 && (
        <CollapsibleSection
          title="ドリンクペアリング"
          icon={<Wine size={18} />}
          sectionKey="drinks"
          expanded={expandedSection === 'drinks'}
          onToggle={() => toggleSection('drinks')}
        >
          <div className="space-y-2">
            {guide.drinkPairings.map((drink, i) => (
              <p key={i} className="text-sm text-gray-700">🍷 {drink}</p>
            ))}
          </div>
        </CollapsibleSection>
      )}

      {/* Common mistakes */}
      {guide.commonMistakes?.length > 0 && (
        <CollapsibleSection
          title="よくある失敗と対処法"
          icon={<AlertTriangle size={18} />}
          sectionKey="mistakes"
          expanded={expandedSection === 'mistakes'}
          onToggle={() => toggleSection('mistakes')}
        >
          <div className="space-y-2">
            {guide.commonMistakes.map((m, i) => (
              <div key={i} className="flex gap-2 p-3 bg-red-50 rounded-xl">
                <span className="text-red-400 flex-shrink-0">⚠️</span>
                <p className="text-sm text-gray-700">{m}</p>
              </div>
            ))}
          </div>
        </CollapsibleSection>
      )}

      {/* Variations */}
      {guide.variations?.length > 0 && (
        <CollapsibleSection
          title="アレンジ・バリエーション"
          icon={<Sparkles size={18} />}
          sectionKey="variations"
          expanded={expandedSection === 'variations'}
          onToggle={() => toggleSection('variations')}
        >
          <div className="space-y-2">
            {guide.variations.map((v, i) => (
              <p key={i} className="text-sm text-gray-700">🔄 {v}</p>
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
        <div className="flex items-center gap-2 text-gray-800 font-semibold">
          {icon}
          {title}
        </div>
        {expanded ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
      </button>
      {expanded && <div className="mt-4">{children}</div>}
    </div>
  )
}

