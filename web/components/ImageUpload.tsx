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
      <div className="relative w-full rounded-2xl overflow-hidden border border-gray-200 bg-gray-50">
        <div className="relative w-full h-56">
          <Image src={value} alt="選択した画像" fill className="object-cover" />
        </div>
        {onClear && (
          <button
            onClick={onClear}
            className="absolute top-2 right-2 p-1.5 bg-black/50 hover:bg-black/70 text-white rounded-full transition-colors"
          >
            <X size={16} />
          </button>
        )}
      </div>
    )
  }

  return (
    <div className="w-full">
      <div className="border-2 border-dashed border-gray-200 rounded-2xl p-8 text-center bg-gray-50 hover:bg-gray-100 transition-colors">
        <div className="text-4xl mb-3">📷</div>
        <p className="text-gray-500 text-sm mb-4">{label}</p>
        <div className="flex gap-3 justify-center flex-wrap">
          <button
            onClick={() => cameraRef.current?.click()}
            className="btn-primary text-sm"
          >
            <Camera size={16} />
            カメラで撮影
          </button>
          <button
            onClick={() => fileRef.current?.click()}
            className="btn-secondary text-sm"
          >
            <Upload size={16} />
            ファイルを選択
          </button>
        </div>
      </div>

      {/* Camera input */}
      <input
        ref={cameraRef}
        type="file"
        accept="image/*"
        capture={capture}
        className="hidden"
        onChange={handleFile}
      />
      {/* File picker */}
      <input
        ref={fileRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={handleFile}
      />
    </div>
  )
}
