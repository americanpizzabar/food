export interface Ingredient {
  name: string
  amount: string
  unit: string
  calories?: number
  notes?: string
}

export interface NutritionInfo {
  protein: number
  carbs: number
  fat: number
  fiber: number
  sugar: number
  sodium: number
}

export interface Recipe {
  id: string
  name: string
  description: string
  ingredients: Ingredient[]
  steps: string[]
  calories: number
  nutrition: NutritionInfo
  tags: string[]
  imageDataUrl?: string
  sourceName?: string
  sourceUrl?: string
  servings: number
  cookTimeMinutes: number
  prepTimeMinutes: number
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  platingAdvice?: string
  remakeIdeas?: string[]
  createdAt: string
}

export interface JournalEntry {
  id: string
  recipeId?: string
  recipeName: string
  date: string
  rating: number
  comment: string
  mood?: string
  healthCondition?: string
  feelingAfter?: string
  imageDataUrl?: string
  tags: string[]
  createdAt: string
}

export interface ShoppingItem {
  id: string
  name: string
  amount: string
  unit: string
  category: string
  isChecked: boolean
  recipeId?: string
  recipeName?: string
  notes: string
  createdAt: string
}

export interface PantryItem {
  id: string
  name: string
  amount: string
  unit: string
  category: string
  expiryDate?: string
  notes: string
  createdAt: string
}

export interface MealPlan {
  id: string
  date: string
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK'
  recipeName: string
  notes: string
  isRoutine: boolean
  routineTag?: string
}

export interface HealthLog {
  id: string
  date: string
  journalEntryId: string
  recipeName: string
  feelingBefore: number
  feelingAfter: number
  energyLevel: number
  digestiveComfort: number
  notes: string
}

export interface UserPreferences {
  allergens: string[]
  dislikedIngredients: string[]
  dietaryRestrictions: string[]
  servingSize: number
  geminiApiKey: string
  notificationsEnabled: boolean
}

// Menu Analysis types
export interface MenuDish {
  name: string
  description?: string
  price?: string
  category?: string
}

export interface MenuAnalysisResult {
  id: string
  imageDataUrl: string
  detectedDishes: MenuDish[]
  analysisDate: string
}

export interface DishCookingGuide {
  id: string
  menuAnalysisId: string
  dishName: string
  overview: string
  ingredients: ProIngredient[]
  techniques: CookingTechnique[]
  steps: DetailedStep[]
  professionalTips: string[]
  platingGuide: string
  variations: string[]
  drinkPairings: string[]
  commonMistakes: string[]
  analysisDate: string
}

export interface ProIngredient {
  name: string
  amount: string
  unit: string
  quality: string      // "最高品質", "A5ランク"など
  whereToFind: string  // 入手場所のヒント
  substitute?: string  // 代替品
  notes?: string
}

export interface CookingTechnique {
  name: string
  description: string
  whyImportant: string
}

export interface DetailedStep {
  stepNumber: number
  title: string
  description: string
  duration?: string
  temperature?: string
  tips: string[]
  warnings?: string
}
