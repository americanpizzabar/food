import { GoogleGenerativeAI, Part } from '@google/generative-ai'

const PRIMARY_MODEL = 'gemini-2.5-flash'
const FALLBACK_MODEL = 'gemini-2.5-flash-lite'
const HIGH_ACCURACY_MODEL = 'gemini-2.5-pro'

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

function isModelUnavailable(e: unknown): boolean {
  if (!(e instanceof Error)) return false
  const m = e.message
  return m.includes('404') || m.includes('not found') || m.includes('NOT_FOUND') || m.includes('is not supported')
}

function isRetryableError(e: unknown): boolean {
  return isQuotaError(e) || isOverloadError(e) || isModelUnavailable(e)
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

type GenOpts = {
  models?: string[]
  generationConfig?: {
    temperature?: number
    topP?: number
    topK?: number
    maxOutputTokens?: number
    responseMimeType?: string
  }
}

async function generateWithFallback(prompt: Prompt, apiKey?: string, opts?: GenOpts): Promise<string> {
  const client = getClient(apiKey)
  const models = opts?.models ?? [PRIMARY_MODEL, FALLBACK_MODEL]
  const generationConfig = opts?.generationConfig
  let lastError: unknown

  for (const modelName of models) {
    try {
      const model = client.getGenerativeModel({
        model: modelName,
        ...(generationConfig ? { generationConfig } : {}),
      })
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
  const prompt = `あなたはレストランメニューを完全に読み取るプロのOCR兼メニュー解析AIです。
画像に写っているメニュー内の料理・ドリンク・コースを、1品も漏らさず全て抽出してください。

【重要な指示】
1. 手書き・印刷・黒板・看板・タブレット表示など、あらゆる形式に対応する
2. 日本語・英語・外国語が混在していても全て日本語で表記 (原語併記も可)
3. 部分的に見切れている料理でも、読み取れる範囲で含める
4. 完全に同じ料理は重複排除。似た名前でも別料理なら両方含める
5. 価格は表示通りの形式で記載 (¥1,500・1500円・$15・税込/税抜 等)
6. カテゴリはメニュー上の見出し (前菜・鮮魚・肉料理・ご飯物・甘味 等) を最優先。なければ料理内容から推定
7. コース料理はコース名を1品として抽出し、可能ならコース内の個別料理も別項目として抽出
8. ドリンク (ワイン・日本酒・焼酎・ビール・カクテル・ソフトドリンク・お茶) も必ず含める
9. 説明文がなくても料理名だけで登録して良い
10. 少しでも料理・飲み物と判別できるものは全て含め、判読困難でも推定を試みる
11. 出力は必ずJSON配列 (説明文・マークダウン・コードブロック不要)

JSON配列形式のみで回答:
[
  {
    "name": "料理名 (日本語で正確に。外国語は「原語 (日本語)」形式も可)",
    "description": "料理の説明・使用食材・調理法 (メニューに記載がある場合のみ、なければ空文字)",
    "price": "価格文字列 (¥1,500 の形式・税表記も含む・なければ空文字)",
    "category": "前菜/鮮魚/肉料理/ご飯物/デザート/ドリンク/コース 等"
  }
]`

  return generateWithFallback(
    [imagePartFromDataUrl(imageDataUrl), prompt],
    apiKey,
    {
      models: [HIGH_ACCURACY_MODEL, PRIMARY_MODEL, FALLBACK_MODEL],
      generationConfig: {
        temperature: 0.1,
        topP: 0.95,
        responseMimeType: 'application/json',
      },
    }
  )
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
