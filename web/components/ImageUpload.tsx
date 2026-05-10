'use client'

import { useRef, useCallback } from 'react'
import { Camera, Upload, X } from 'lucide-react'
import Image from 'next/image'

interface Props {
  value?: string
  onChange: (dataUrl: string) => void
  onClear?: () => void
  label?: string
  accept?: string
  capture?: 'user' | 'environment'
}

export default function ImageUpload({
  value,
  onChange,
  onClear,
  label = '画像を選択',
  capture = 'environment',
}: Props) {
  const fileRef = useRef<HTMLInputElement>(null)
  const cameraRef = useRef<HTMLInputElement>(null)

  const readFile = useCallback((file: File) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const result = e.target?.result as string
      if (result) onChange(result)
    }
    reader.readAsDataURL(file)
  }, [onChange])

  const handleFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) readFile(file)
    e.target.value = ''
  }

  if (value) {
    return (
      <div className="relative w-full rounded-2xl overflow-hidden"
        style={{ background: 'var(--surface-2)', border: '1px solid rgba(255,255,255,.06)' }}>
        <div className="relative w-full h-56">
          <Image src={value} alt="選択した画像" fill className="object-cover" />
        </div>
        {onClear && (
          <button
            onClick={onClear}
            className="absolute top-2 right-2 p-1.5 bg-black/60 hover:bg-black/80 text-white rounded-full transition-colors"
          >
            <X size={16} />
          </button>
        )}
      </div>
    )
  }

  return (
    <div className="w-full">
      <div className="rounded-2xl p-8 text-center transition-colors"
        style={{
          background: 'var(--surface-2)',
          border: '2px dashed rgba(255,255,255,.10)',
        }}>
        <div className="text-4xl mb-3 opacity-60">📷</div>
        <p className="text-[#888] text-sm mb-4">{label}</p>
        <div className="flex gap-3 justify-center flex-wrap">
          <button onClick={() => cameraRef.current?.click()} className="btn-primary text-sm">
            <Camera size={16} />
            カメラで撮影
          </button>
          <button onClick={() => fileRef.current?.click()} className="btn-secondary text-sm">
            <Upload size={16} />
            ファイルを選択
          </button>
        </div>
      </div>

      <input ref={cameraRef} type="file" accept="image/*" capture={capture} className="hidden" onChange={handleFile} />
      <input ref={fileRef} type="file" accept="image/*" className="hidden" onChange={handleFile} />
    </div>
  )
}
