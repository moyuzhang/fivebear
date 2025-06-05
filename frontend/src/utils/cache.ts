interface CacheItem<T> {
    data: T
    timestamp: number
    ttl: number
}

class ResponseCache {
    private cache = new Map<string, CacheItem<any>>()
    private defaultTTL = 5 * 60 * 1000 // 默认5分钟

    /**
     * 设置缓存
     * @param key 缓存键
     * @param data 缓存数据
     * @param ttl 生存时间（毫秒）
     */
    set<T>(key: string, data: T, ttl?: number): void {
        this.cache.set(key, {
            data,
            timestamp: Date.now(),
            ttl: ttl || this.defaultTTL
        })
    }

    /**
     * 获取缓存
     * @param key 缓存键
     * @returns 缓存数据或null
     */
    get<T>(key: string): T | null {
        const item = this.cache.get(key)
        
        if (!item) {
            return null
        }

        const now = Date.now()
        if (now - item.timestamp > item.ttl) {
            this.cache.delete(key)
            return null
        }

        return item.data as T
    }

    /**
     * 删除缓存
     * @param key 缓存键
     */
    delete(key: string): void {
        this.cache.delete(key)
    }

    /**
     * 清除所有缓存
     */
    clear(): void {
        this.cache.clear()
    }

    /**
     * 清除过期缓存
     */
    clearExpired(): void {
        const now = Date.now()
        for (const [key, item] of this.cache.entries()) {
            if (now - item.timestamp > item.ttl) {
                this.cache.delete(key)
            }
        }
    }

    /**
     * 根据前缀清除缓存
     * @param prefix 键前缀
     */
    clearByPrefix(prefix: string): void {
        for (const key of this.cache.keys()) {
            if (key.startsWith(prefix)) {
                this.cache.delete(key)
            }
        }
    }
}

// 创建全局缓存实例
export const responseCache = new ResponseCache()

// 定期清理过期缓存
setInterval(() => {
    responseCache.clearExpired()
}, 60 * 1000) // 每分钟清理一次

/**
 * 创建带缓存的请求函数
 * @param fn 请求函数
 * @param getCacheKey 获取缓存键的函数
 * @param ttl 缓存生存时间
 * @returns 带缓存的请求函数
 */
export function withCache<T extends (...args: any[]) => Promise<any>>(
    fn: T,
    getCacheKey: (...args: Parameters<T>) => string,
    ttl?: number
): T {
    return (async (...args: Parameters<T>) => {
        const cacheKey = getCacheKey(...args)
        
        // 尝试从缓存获取
        const cached = responseCache.get(cacheKey)
        if (cached !== null) {
            console.log('📦 Cache hit:', cacheKey)
            return cached
        }

        // 执行请求
        console.log('🌐 Cache miss, fetching:', cacheKey)
        const result = await fn(...args)
        
        // 存入缓存
        responseCache.set(cacheKey, result, ttl)
        
        return result
    }) as T
}