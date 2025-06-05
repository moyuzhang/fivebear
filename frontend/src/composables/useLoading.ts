import { ref, Ref } from 'vue'

export interface UseLoadingReturn {
    loading: Ref<boolean>
    startLoading: () => void
    stopLoading: () => void
    withLoading: <T>(fn: () => Promise<T>) => Promise<T>
}

/**
 * 通用加载状态管理
 * @param initialValue 初始加载状态
 * @returns 加载状态管理对象
 */
export function useLoading(initialValue = false): UseLoadingReturn {
    const loading = ref(initialValue)

    const startLoading = () => {
        loading.value = true
    }

    const stopLoading = () => {
        loading.value = false
    }

    /**
     * 在执行异步函数时自动管理加载状态
     * @param fn 要执行的异步函数
     * @returns 异步函数的返回值
     */
    const withLoading = async <T>(fn: () => Promise<T>): Promise<T> => {
        startLoading()
        try {
            return await fn()
        } finally {
            stopLoading()
        }
    }

    return {
        loading,
        startLoading,
        stopLoading,
        withLoading
    }
}