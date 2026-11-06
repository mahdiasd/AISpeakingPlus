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
        apple: {
          blue: '#007aff',
          green: '#34c759',
          indigo: '#5856d6',
          orange: '#ff9500',
          pink: '#ff2d55',
          purple: '#af52de',
          red: '#ff3b30',
          teal: '#5ac8fa',
          yellow: '#ffcc00',
          gray: {
            50: '#f9f9fb',
            100: '#f2f2f7',
            200: '#e5e5ea',
            300: '#d1d1d6',
            400: '#c7c7cc',
            500: '#8e8e93',
            600: '#636366',
            700: '#48484a',
            800: '#2c2c2e',
            900: '#1c1c1e',
            950: '#0c0c0e',
          },
          dark: {
            canvas: '#000000',
            elevated: '#161618',
            surface: '#1c1c1e',
            subtle: '#2c2c2e',
            border: 'rgba(255, 255, 255, 0.12)',
          }
        }
      },
      fontFamily: {
        sans: [
          'Vazirmatn Variable',
          'Vazirmatn',
          '-apple-system',
          'BlinkMacSystemFont',
          '"SF Pro Text"',
          '"SF Pro Display"',
          'system-ui',
          'sans-serif'
        ],
      },
      boxShadow: {
        'apple-sm': '0 2px 8px -2px rgba(0, 0, 0, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.2)',
        'apple-md': '0 8px 24px -4px rgba(0, 0, 0, 0.15), inset 0 1.5px 0 rgba(255, 255, 255, 0.35)',
        'apple-lg': '0 16px 40px -8px rgba(0, 0, 0, 0.25), inset 0 1.5px 0 rgba(255, 255, 255, 0.5)',
        'apple-glass': '0 12px 32px 0 rgba(0, 0, 0, 0.18), inset 0 1.5px 0 0 rgba(255, 255, 255, 0.75)',
        'apple-glass-dark': '0 16px 36px 0 rgba(0, 0, 0, 0.45), inset 0 1px 0 0 rgba(255, 255, 255, 0.18)',
      },
      backdropBlur: {
        'apple-xs': '8px',
        'apple-sm': '12px',
        'apple-md': '20px',
        'apple-lg': '32px',
      },
      transitionTimingFunction: {
        'apple-spring': 'cubic-bezier(0.32, 0.72, 0, 1)',
        'apple-bounce': 'cubic-bezier(0.34, 1.56, 0.64, 1)',
      }
    },
  },
  plugins: [],
};
