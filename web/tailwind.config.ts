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
      colors: {
        gold: {
          300: '#f5d06e',
          400: '#e8b84b',
          500: '#d4932a',
          600: '#b87520',
        },
        dark: {
          50:  '#ececec',
          200: '#777777',
          700: '#222228',
          800: '#18181d',
          900: '#101013',
          950: '#080809',
        },
      },
      borderRadius: {
        '4xl': '2rem',
        '5xl': '2.5rem',
      },
    },
  },
  plugins: [],
}

export default config
