import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
    plugins: [
        vue()
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    define: {
        global: 'globalThis',
    },
    server: {
        host: '0.0.0.0',
        port: 3000,
        open: true, // 自动打开浏览器
        cors: true, // 启用CORS
        // 热重载配置
        hmr: {
            overlay: true // 显示错误覆盖层
        },
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false, // 如果是https接口，需要配置这个参数
                rewrite: (path) => path.replace(/^\/api/, ''),
                // 代理超时配置
                timeout: 30000,
                // 日志
                configure: (proxy, _options) => {
                    proxy.on('error', (err, _req, _res) => {
                        console.log('proxy error', err);
                    });
                    proxy.on('proxyReq', (proxyReq, req, _res) => {
                        console.log('Sending Request to the Target:', req.method, req.url);
                    });
                    proxy.on('proxyRes', (proxyRes, req, _res) => {
                        console.log('Received Response from the Target:', proxyRes.statusCode, req.url);
                    });
                },
                // WebSocket 代理
                ws: true
            }
        }
    },
    build: {
        outDir: 'dist',
        sourcemap: false,
        minify: 'terser',
        rollupOptions: {
            output: {
                manualChunks: {
                    'vue-vendor': ['vue', 'vue-router', 'pinia'],
                    'element-plus': ['element-plus', '@element-plus/icons-vue'],
                    'utils': ['axios', 'js-cookie', 'crypto-js', 'sockjs-client']
                },
                entryFileNames: 'js/[name].[hash].js',
                chunkFileNames: 'js/[name].[hash].js',
                assetFileNames: (assetInfo) => {
                    if (!assetInfo.name) {
                        return 'assets/[name].[hash].[ext]'
                    }
                    const info = assetInfo.name.split('.')
                    let extType = info[info.length - 1]
                    if (/\.(mp4|webm|ogg|mp3|wav|flac|aac)(\?.*)?$/i.test(assetInfo.name)) {
                        extType = 'media'
                    } else if (/\.(png|jpe?g|gif|svg|webp|ico)(\?.*)?$/i.test(assetInfo.name)) {
                        extType = 'img'
                    } else if (/\.(woff2?|eot|ttf|otf)(\?.*)?$/i.test(assetInfo.name)) {
                        extType = 'fonts'
                    }
                    return `${extType}/[name].[hash].[ext]`
                }
            }
        },
        reportCompressedSize: false,
        chunkSizeWarningLimit: 2000
    },
    optimizeDeps: {
        include: [
            'vue',
            'vue-router',
            'pinia',
            'axios',
            'element-plus',
            '@element-plus/icons-vue',
            'js-cookie',
            'crypto-js',
            'sockjs-client'
        ]
    }
}) 