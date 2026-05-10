'use client'

import { useLocalStorage } from './useLocalStorage'
import { KEYS } from '@/lib/storage'
import { UserPreferences } from '@/lib/types'

const DEFAULT_PREFS: UserPreferences = {
  allergens: [],
  dislikedIngredients: [],
  dietaryRestrictions: [],
  servingSize: 2,
  geminiApiKey: '',
  notificationsEnabled: true,
}

export function useApiKey() {
  const { value: prefs } = useLocalStorage<UserPreferences>(KEYS.PREFERENCES, DEFAULT_PREFS)
  return prefs.geminiApiKey || undefined
}
