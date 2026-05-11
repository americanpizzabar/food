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

export class StorageQuotaError extends Error {
  constructor() {
    super('保存容量の上限に達しました。古い履歴を削除してください。')
    this.name = 'StorageQuotaError'
  }
}

function isQuotaError(e: unknown): boolean {
  if (!(e instanceof Error)) return false
  return (
    e.name === 'QuotaExceededError' ||
    e.name === 'NS_ERROR_DOM_QUOTA_REACHED' ||
    e.message.includes('quota')
  )
}

export function setItem<T>(key: string, value: T): void {
  if (typeof window === 'undefined') return
  const json = JSON.stringify(value)
  try {
    localStorage.setItem(key, json)
  } catch (e) {
    if (!isQuotaError(e)) throw e
    // Quota exceeded: try shedding old entries from history-style array values
    if (Array.isArray(value) && value.length > 1) {
      let trimmed = value.slice(0, Math.max(1, Math.floor(value.length / 2)))
      while (trimmed.length > 0) {
        try {
          localStorage.setItem(key, JSON.stringify(trimmed))
          return
        } catch (retryErr) {
          if (!isQuotaError(retryErr)) throw retryErr
          trimmed = trimmed.slice(0, Math.floor(trimmed.length / 2))
        }
      }
    }
    throw new StorageQuotaError()
  }
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
