import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

const BACKEND_TARGET = process.env.VITE_BACKEND_URL || 'https://staging.aispeaking.ir';

export default defineConfig({
  plugins: [react()],
  base: './',
  resolve: {
    dedupe: ['react', 'react-dom'],
    alias: [
      { find: '@', replacement: path.resolve(__dirname, './src') },
      { find: /^@react-cupertino-ui\/shared\/lib\/(.*)/, replacement: '@react-cupertino-ui/shared/dist/lib/$1' },
    ]
  },
  server: {
    port: 8083,
    host: true,
    proxy: {
      '/api': {
        target: BACKEND_TARGET,
        changeOrigin: true,
        secure: false,
        ws: true,
      },
      '/uploads': {
        target: BACKEND_TARGET,
        changeOrigin: true,
        secure: false,
      },
      '/static': {
        target: BACKEND_TARGET,
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor-react': ['react', 'react-dom', 'react-router-dom'],
          'vendor-motion': ['motion'],
          'vendor-icons': ['lucide-react'],
        },
      },
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        loadPaths: [
          path.resolve(__dirname, 'node_modules/@react-cupertino-ui/button/dist'),
          path.resolve(__dirname, 'node_modules'),
        ]
      }
    }
  }
});
