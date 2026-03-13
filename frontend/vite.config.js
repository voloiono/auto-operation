import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        // SSE 支持：禁用响应压缩和缓冲，确保 EventSource 实时推送
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            if (req.headers.accept === 'text/event-stream') {
              proxyReq.setHeader('Cache-Control', 'no-cache')
              proxyReq.setHeader('Accept', 'text/event-stream')
            }
          })
          proxy.on('proxyRes', (proxyRes, req) => {
            if (req.headers.accept === 'text/event-stream') {
              proxyRes.headers['cache-control'] = 'no-cache'
              proxyRes.headers['x-accel-buffering'] = 'no'
            }
          })
        }
      }
    }
  }
})
