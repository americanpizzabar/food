import type { Config } from 'tailwindcss'

const config: Config = {
  content: [
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['var(--font-sans)', 'system-ui', '-apple-system', 'sans-serif'],
      },
      borderRadius: {
        '4xl': '2rem',
        '5xl': '2.5rem',
      },
      boxShadow: {
        'card': '0 1px 3px rgba(0,0,0,.05), 0 6px 20px rgba(0,0,0,.04)',
        'card-hover': '0 4px 28px rgba(0,0,0,.10)',
        'nav': '0 -1px 0 rgba(0,0,0,.05), 0 -8px 24px rgba(0,0,0,.06)',
        'orange': '0 4px_16px rgba(249,115,22,.4)',
      },
    },
  },
  plugins: [],
}

export default config
