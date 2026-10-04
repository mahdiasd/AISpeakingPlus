import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 8082,
    host: true,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      '/resources': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      '/static': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
});
