/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eef2ff',
          100: '#e0e7ff',
          200: '#c7d2fe',
          300: '#a5b4fc',
          400: '#818cf8',
          500: '#6366f1',
          600: '#4f46e5',
          700: '#4338ca',
          800: '#3730a3',
          900: '#312e81',
          950: '#1e1b4b',
        },
        obsidian: {
          canvas: '#0B0F17',
          card: '#111726',
          'card-hover': '#151D30',
          elevated: '#172033',
          panel: '#0E1422',
          border: '#1E293B',
          'border-subtle': 'rgba(255, 255, 255, 0.07)',
          'border-highlight': 'rgba(255, 255, 255, 0.12)',
        },
        dark: {
          bg: '#0B0F17',
          card: '#111726',
          surface: '#172033',
          border: '#1E293B',
          muted: '#94a3b8'
        }
      },
      boxShadow: {
        'card-subtle': '0 1px 3px 0 rgba(0, 0, 0, 0.35), 0 1px 2px -1px rgba(0, 0, 0, 0.35), inset 0 1px 0 0 rgba(255, 255, 255, 0.05)',
        'card-elevated': '0 8px 30px -4px rgba(0, 0, 0, 0.6), inset 0 1px 0 0 rgba(255, 255, 255, 0.07)',
        'brand-glow': '0 0 25px -4px rgba(99, 102, 241, 0.25)',
      },
      fontFamily: {
        sans: ['Vazirmatn', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['Fira Code', 'JetBrains Mono', 'ui-monospace', 'monospace'],
      }
    },
  },
  plugins: [],
}

