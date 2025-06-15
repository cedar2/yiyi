import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import vuetify from "vite-plugin-vuetify";
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    vuetify({ autoImport: true }),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 1516,
    proxy: {
      '/api': {
        // target: 'http://localhost:2025',
        target: 'http://121.250.208.122:2025',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, ''),
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq) => {
            proxyReq.removeHeader('origin');
          });
        }
      },
      '/qwen14b': {
        target: 'http://121.250.208.122:1234',
        //target: 'http://0.0.0.0:1234',
        changeOrigin: true,
      },
      '/qwen32b': {
        target: 'http://121.250.208.122:1234',
        //target: 'http://0.0.0.0:1234',
        changeOrigin: true,
      },
      '/getGraph': {
        target: 'http://121.250.208.122:1234',
        //target: 'http://0.0.0.0:1234',
        changeOrigin: true,
      }
    }
  }
})
