import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    rolldownOptions: {
      output: {
        codeSplitting: {
          maxSize: 450000,
          groups: [
            { name: 'element-plus', test: /[\\/]node_modules[\\/]element-plus[\\/]/, priority: 20 },
            { name: 'vue-vendor', test: /[\\/]node_modules[\\/](vue|vue-router|pinia)[\\/]/, priority: 15 },
            { name: 'vendor', test: /[\\/]node_modules[\\/]/, priority: 10 }
          ]
        }
      }
    }
  }
})
