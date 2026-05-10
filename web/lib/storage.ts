'use client'

// Generic localStorage helper
export function getItem<T>(key: string, defaultValue: T): T {
  if (typeof window === 'undefined') return defaultValue
  try {
    const raw = localStorage.getItem(key)
    return raw ? (JSON.parse(raw) as T) : defaultValue
  } catch {
    return defaultValue
  }
}

export function setItem<T>(key: string, value: T): void {
  if (typeof window === 'undefined') return
  localStorage.setItem(key, JSON.stringify(value))
}

export function removeItem(key: string): void {
  if (typeof window === 'undefined') return
  localStorage.removeItem(key)
}

// Storage keys
export const KEYS = {
  RECIPES: 'foodai_recipes',
  JOURNAL: 'foodai_journal',
  SHOPPING: 'foodai_shopping',
  PANTRY: 'foodai_pantry',
  MEAL_PLANS: 'foodai_meal_plans',
  HEALTH_LOGS: 'foodai_health_logs',
  PREFERENCES: 'foodai_preferences',
  MENU_ANALYSES: 'foodai_menu_analyses',
  DISH_GUIDES: 'foodai_dish_guides',
} as const

// ID generator
export function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
}
