import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    host: true,  // <- 允許外部訪問
    allowedHosts: 'all',  // <- 允許所有主機訪問
    proxy: {
      '/api': {
        target: 'http://localhost:8083',
        changeOrigin: true
      }
    }
  }
})
