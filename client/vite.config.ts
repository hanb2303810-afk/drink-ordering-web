import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  // Dev proxy: client 5173 gọi API 8080 của server Spring Boot qua cùng origin.
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
