'use client'

import { useState, useEffect, useCallback } from 'react'
import { getItem, setItem } from '@/lib/storage'

export function useLocalStorage<T>(key: string, defaultValue: T) {
  const [value, setValue] = useState<T>(defaultValue)
  const [hydrated, setHydrated] = useState(false)

  useEffect(() => {
    setValue(getItem<T>(key, defaultValue))
    setHydrated(true)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key])

  const update = useCallback((next: T | ((prev: T) => T)) => {
    setValue(prev => {
      const updated = typeof next === 'function' ? (next as (p: T) => T)(prev) : next
      setItem(key, updated)
      return updated
    })
  }, [key])

  return { value, update, hydrated }
}
