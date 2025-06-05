interface PerformanceMetric {
    name: string
    startTime: number
    endTime?: number
    duration?: number
    metadata?: Record<string, any>
}

class PerformanceMonitor {
    private metrics: Map<string, PerformanceMetric> = new Map()
    private history: PerformanceMetric[] = []
    private maxHistorySize = 100

    /**
     * 开始测量性能
     * @param name 测量名称
     * @param metadata 额外的元数据
     */
    start(name: string, metadata?: Record<string, any>): void {
        this.metrics.set(name, {
            name,
            startTime: performance.now(),
            metadata
        })
    }

    /**
     * 结束测量性能
     * @param name 测量名称
     * @returns 持续时间（毫秒）
     */
    end(name: string): number | null {
        const metric = this.metrics.get(name)
        if (!metric) {
            console.warn(`Performance metric "${name}" not found`)
            return null
        }

        metric.endTime = performance.now()
        metric.duration = metric.endTime - metric.startTime

        // 添加到历史记录
        this.history.push({ ...metric })
        if (this.history.length > this.maxHistorySize) {
            this.history.shift()
        }

        // 从活动测量中移除
        this.metrics.delete(name)

        // 记录到控制台（开发环境）
        if (import.meta.env.DEV) {
            const emoji = metric.duration < 100 ? '🚀' : metric.duration < 500 ? '⚡' : '🐌'
            console.log(
                `${emoji} [Performance] ${name}: ${metric.duration.toFixed(2)}ms`,
                metric.metadata || ''
            )
        }

        return metric.duration
    }

    /**
     * 测量异步函数的执行时间
     * @param name 测量名称
     * @param fn 要测量的函数
     * @param metadata 额外的元数据
     * @returns 函数执行结果
     */
    async measure<T>(
        name: string,
        fn: () => Promise<T>,
        metadata?: Record<string, any>
    ): Promise<T> {
        this.start(name, metadata)
        try {
            const result = await fn()
            this.end(name)
            return result
        } catch (error) {
            this.end(name)
            throw error
        }
    }

    /**
     * 测量同步函数的执行时间
     * @param name 测量名称
     * @param fn 要测量的函数
     * @param metadata 额外的元数据
     * @returns 函数执行结果
     */
    measureSync<T>(
        name: string,
        fn: () => T,
        metadata?: Record<string, any>
    ): T {
        this.start(name, metadata)
        try {
            const result = fn()
            this.end(name)
            return result
        } catch (error) {
            this.end(name)
            throw error
        }
    }

    /**
     * 获取性能统计
     * @param name 测量名称（可选，不提供则返回所有）
     * @returns 性能统计
     */
    getStats(name?: string): {
        count: number
        avg: number
        min: number
        max: number
        total: number
    } | null {
        const metrics = name
            ? this.history.filter(m => m.name === name)
            : this.history

        if (metrics.length === 0) {
            return null
        }

        const durations = metrics
            .map(m => m.duration)
            .filter((d): d is number => d !== undefined)

        return {
            count: durations.length,
            avg: durations.reduce((a, b) => a + b, 0) / durations.length,
            min: Math.min(...durations),
            max: Math.max(...durations),
            total: durations.reduce((a, b) => a + b, 0)
        }
    }

    /**
     * 清除历史记录
     */
    clearHistory(): void {
        this.history = []
    }

    /**
     * 获取所有历史记录
     */
    getHistory(): PerformanceMetric[] {
        return [...this.history]
    }
}

// 创建全局性能监控实例
export const performanceMonitor = new PerformanceMonitor()

/**
 * 创建性能测量装饰器
 * @param name 测量名称
 * @returns 装饰器函数
 */
export function measurePerformance(name: string) {
    return function (
        target: any,
        propertyKey: string,
        descriptor: PropertyDescriptor
    ) {
        const originalMethod = descriptor.value

        descriptor.value = async function (...args: any[]) {
            return performanceMonitor.measure(
                `${name}.${propertyKey}`,
                () => originalMethod.apply(this, args),
                { args }
            )
        }

        return descriptor
    }
}

// Vue组件性能测量
export function usePerformance(componentName: string) {
    const measure = async <T>(
        operation: string,
        fn: () => Promise<T>
    ): Promise<T> => {
        return performanceMonitor.measure(
            `${componentName}.${operation}`,
            fn
        )
    }

    const measureSync = <T>(
        operation: string,
        fn: () => T
    ): T => {
        return performanceMonitor.measureSync(
            `${componentName}.${operation}`,
            fn
        )
    }

    return {
        measure,
        measureSync,
        monitor: performanceMonitor
    }
}