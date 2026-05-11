import { GoogleGenerativeAI, Part } from '@google/generative-ai'

const PRIMARY_MODEL = 'gemini-2.5-flash'
const FALLBACK_MODEL = 'gemini-2.5-flash-lite'

function getClient(apiKey?: string) {
  const key = apiKey || process.env.GEMINI_API_KEY || ''
  if (!key) throw new Error('Gemini APIキーが設定されていません。設定画面でGoogle AI StudioのAPIキーを登録してください。')
  return new GoogleGenerativeAI(key)
}

function imagePartFromDataUrl(dataUrl: string): Part {
  const [header, data] = dataUrl.split(',')
  const mimeType = header.split(':')[1].split(';')[0] as 'image/jpeg' | 'image/png' | 'image/webp'
  return { inlineData: { data, mimeType } }
}

function isQuotaError(e: unknown): boolean {
  if (!(e instanceof Error)) return false
  const m = e.message
  return m.includes('429') || m.includes('Quota') || m.includes('quota') || m.includes('RESOURCE_EXHAUSTED')
}

function isOverloadError(e: unknown): boolean {
  if (!(e instanceof Error)) return false
  const m = e.message
  return m.includes('503') || m.includes('Service Unavailable') || m.includes('overloaded') || m.includes('high demand')
}

function isRetryableError(e: unknown): boolean {
  return isQuotaError(e) || isOverloadError(e)
}

function isAuthError(e: unknown): boolean {
  if (!(e instanceof Error)) return false
  const m = e.message
  return m.includes('401') || m.includes('403') || m.includes('API_KEY_INVALID') || m.includes('PERMISSION_DENIED')
}

function friendlyError(e: unknown): never {
  if (isQuotaError(e)) {
    throw new Error('AI APIの利用上限に達しました。しばらく時間を置いてから再試行するか、Google AI Studioで料金プランを確認してください。')
  }
  if (isOverloadError(e)) {
    throw new Error('AIサーバーが混雑しています。しばらく待ってから再試行してください。')
  }
  if (isAuthError(e)) {
    throw new Error('APIキーが無効か、権限がありません。設定画面で正しいGoogle AI StudioのAPIキーを設定してください。')
  }
  if (e instanceof Error) throw e
  throw new Error('AI処理に失敗しました')
}

type Prompt = string | (string | Part)[]

async function generateWithFallback(prompt: Prompt, apiKey?: string): Promise<string> {
  const client = getClient(apiKey)
  let lastError: unknown

  for (const modelName of [PRIMARY_MODEL, FALLBACK_MODEL]) {
    try {
      const model = client.getGenerativeModel({ model: modelName })
      const result = await model.generateContent(prompt as never)
      return result.response.text()
    } catch (e) {
      lastError = e
      if (!isRetryableError(e)) break
    }
  }
  friendlyError(lastError)
}

// ---- Food Photo Analysis ----
export async function analyzeFoodPhoto(imageDataUrl: string, apiKey?: string): Promise<string> {
  const prompt = `あなたはミシュラン三つ星シェフ兼栄養士のAIです。この料理の写真を詳細に分析し、プロレベルの情報を含むJSON形式のみで回答してください（他の文章は一切不要）:
{
  "name": "料理名（日本語）",
  "description": "料理の詳細な説明（150文字以内）",
  "origin": "この料理の起源・歴史・文化的背景（80文字以内）",
  "costEstimate": "安い|普通|高め",
  "ingredients": [
    {
      "name": "食材名",
      "amount": "量",
      "unit": "単位",
      "calories": カロリー数値,
      "selectionTip": "この食材の選び方・品質見極めのプロのポイント（30文字）"
    }
  ],
  "steps": ["詳細な手順1（プロのポイント含む）", "手順2"],
  "calories": 1人分カロリー数値,
  "nutrition": {
    "protein": タンパク質g,
    "carbs": 炭水化物g,
    "fat": 脂質g,
    "fiber": 食物繊維g,
    "sugar": 糖質g,
    "sodium": ナトリウムmg
  },
  "tags": ["タグ"],
  "servings": 人数,
  "cookTimeMinutes": 調理時間分,
  "prepTimeMinutes": 下準備時間分,
  "difficulty": "EASY|MEDIUM|HARD",
  "cookingTechniques": [
    {
      "name": "技法名（例: メイラード反応・乳化・低温調理）",
      "description": "この料理でどう使われているか（60文字）",
      "whyImportant": "なぜこの料理に重要か（40文字）"
    }
  ],
  "professionalTips": [
    "プロのコツ1（具体的・実践的）",
    "プロのコツ2",
    "プロのコツ3",
    "プロのコツ4"
  ],
  "platingAdvice": "プロ級の盛り付けアドバイス（器の選び方・ソースの引き方・色のバランス・高さの出し方を含む詳細なガイド）",
  "drinkPairings": ["合うワイン・日本酒・ビール・ノンアルコール飲料1", "合う飲み物2", "合う飲み物3"],
  "healthBenefits": ["この料理の健康効果・栄養メリット1", "健康効果2", "健康効果3"],
  "storageInfo": "保存方法（冷蔵・冷凍・常温の別、保存期間、再加熱のコツ）",
  "remakeIdeas": ["残り物のリメイク案1", "リメイク案2"],
  "variations": ["アレンジ・バリエーション案1（異なる食材・調理法）", "バリエーション2"]
}`

  return generateWithFallback([imagePartFromDataUrl(imageDataUrl), prompt], apiKey)
}

// ---- Recipe Suggestion ----
export async function suggestRecipes(params: {
  mood: string
  craving: string
  healthCondition: string
  nutritionNeeds: string
  pantryItems: string[]
  allergens: string[]
  dislikedIngredients: string[]
  apiKey?: string
}): Promise<string> {
  const prompt = `あなたはプロの料理専門家AIです。以下の条件に合うレシピを3つ提案してください。JSON配列形式のみで回答（他の文章不要）:

気分: ${params.mood || '普通'}
食べたいもの: ${params.craving || '指定なし'}
体調: ${params.healthCondition || '良好'}
必要栄養素: ${params.nutritionNeeds || '指定なし'}
冷蔵庫の食材（優先使用）: ${params.pantryItems.join(', ') || 'なし'}
除外食材（アレルギー・苦手）: ${[...params.allergens, ...params.dislikedIngredients].join(', ') || 'なし'}

[
  {
    "name": "料理名",
    "description": "説明（80文字以内）",
    "ingredients": [{"name": "食材", "amount": "量", "unit": "単位", "calories": 0}],
    "steps": ["手順1", "手順2"],
    "calories": 500,
    "nutrition": {"protein": 20, "carbs": 60, "fat": 15, "fiber": 5, "sugar": 10, "sodium": 800},
    "tags": ["タグ"],
    "servings": 2,
    "cookTimeMinutes": 30,
    "prepTimeMinutes": 10,
    "difficulty": "EASY"
  }
]`

  return generateWithFallback(prompt, params.apiKey)
}

// ---- Menu Analysis: detect dishes from menu photo ----
export async function analyzeMenuPhoto(imageDataUrl: string, apiKey?: string): Promise<string> {
  const prompt = `このレストランのメニュー写真を分析し、メニューに掲載されている料理を全て抽出してください。JSON配列形式のみで回答（説明不要）:
[
  {
    "name": "料理名",
    "description": "料理の簡単な説明（あれば）",
    "price": "価格（あれば）",
    "category": "カテゴリ（前菜・メイン・デザート等）"
  }
]`

  return generateWithFallback([imagePartFromDataUrl(imageDataUrl), prompt], apiKey)
}

// ---- Dish Cooking Guide: professional-level cooking instructions ----
export async function generateDishCookingGuide(dishName: string, apiKey?: string): Promise<string> {
  const prompt = `あなたはミシュラン三つ星レストランのシェフです。「${dishName}」を家庭で最高レベルに仕上げるための完全ガイドを作成してください。
プロが使う最高品質の食材選び、本格的な調理技法、細かい手順を詳しく丁寧に説明してください。
JSON形式のみで回答（他の文章不要）:
{
  "dishName": "${dishName}",
  "overview": "この料理の概要と美味しさの秘訣（200文字）",
  "ingredients": [
    {
      "name": "食材名",
      "amount": "量",
      "unit": "単位",
      "quality": "品質の選び方（例: A5ランク黒毛和牛、無農薬有機栽培）",
      "whereToFind": "入手場所のヒント",
      "substitute": "代替品（なければnull）",
      "notes": "下処理や選び方のポイント"
    }
  ],
  "techniques": [
    {
      "name": "技法名（例: 低温調理、乳化、メイラード反応）",
      "description": "技法の詳細説明",
      "whyImportant": "なぜこの料理に重要か"
    }
  ],
  "steps": [
    {
      "stepNumber": 1,
      "title": "ステップのタイトル",
      "description": "詳細な手順説明（丁寧に）",
      "duration": "所要時間（例: 30分）",
      "temperature": "温度（例: 180℃、あれば）",
      "tips": ["プロのコツ1", "プロのコツ2"],
      "warnings": "失敗しやすいポイント（あれば）"
    }
  ],
  "professionalTips": [
    "プロの秘訣1",
    "プロの秘訣2",
    "プロの秘訣3"
  ],
  "platingGuide": "盛り付けの詳細ガイド（器の選び方、飾り付け、色のバランス）",
  "variations": ["アレンジ案1", "アレンジ案2"],
  "drinkPairings": ["合うワイン・飲み物1", "合う飲み物2"],
  "commonMistakes": ["よくある失敗1とその対処法", "よくある失敗2とその対処法"]
}`

  return generateWithFallback(prompt, apiKey)
}

// ---- URL Recipe Import ----
export async function importRecipeFromUrl(url: string, htmlText: string, apiKey?: string): Promise<string> {
  const prompt = `以下のWebページのテキストからレシピを抽出し、JSON形式のみで回答してください:
URL: ${url}
テキスト: ${htmlText.slice(0, 4000)}

{
  "name": "料理名",
  "description": "説明",
  "ingredients": [{"name": "食材", "amount": "量", "unit": "単位", "calories": 0}],
  "steps": ["手順1"],
  "calories": 0,
  "nutrition": {"protein": 0, "carbs": 0, "fat": 0, "fiber": 0, "sugar": 0, "sodium": 0},
  "tags": [],
  "servings": 2,
  "cookTimeMinutes": 30,
  "prepTimeMinutes": 10,
  "difficulty": "MEDIUM"
}`

  return generateWithFallback(prompt, apiKey)
}

// ---- Health Correlation Analysis ----
export async function analyzeHealthCorrelation(logs: string[], apiKey?: string): Promise<string> {
  const prompt = `以下の食事と体調ログを分析し、体調改善に良い食事パターンを200文字以内で日本語で教えてください:
${logs.join('\n')}

また、おすすめのメニューを3つ提案してください。`

  return generateWithFallback(prompt, apiKey)
}

// JSON extractor helper
export function extractJson(text: string): string {
  const objStart = text.indexOf('{')
  const arrStart = text.indexOf('[')
  if (arrStart >= 0 && (objStart < 0 || arrStart < objStart)) {
    const end = text.lastIndexOf(']') + 1
    return text.slice(arrStart, end)
  }
  if (objStart >= 0) {
    const end = text.lastIndexOf('}') + 1
    return text.slice(objStart, end)
  }
  return text
}
