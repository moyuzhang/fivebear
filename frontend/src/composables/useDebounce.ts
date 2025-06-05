import { ref, customRef, Ref } from 'vue'

/**
 * 创建一个防抖的ref
 * @param value 初始值
 * @param delay 防抖延迟时间（毫秒）
 * @returns 防抖的ref
 */
export function useDebouncedRef<T>(value: T, delay = 200): Ref<T> {
    let timeout: ReturnType<typeof setTimeout>
    
    return customRef((track, trigger) => {
        return {
            get() {
                track()
                return value
            },
            set(newValue: T) {
                clearTimeout(timeout)
                timeout = setTimeout(() => {
                    value = newValue
                    trigger()
                }, delay)
            }
        }
    })
}

/**
 * 创建一个防抖函数
 * @param fn 要防抖的函数
 * @param delay 防抖延迟时间（毫秒）
 * @returns 防抖后的函数
 */
export function useDebounce<T extends (...args: any[]) => any>(
    fn: T,
    delay = 200
): (...args: Parameters<T>) => void {
    let timeout: ReturnType<typeof setTimeout> | null = null

    return (...args: Parameters<T>) => {
        if (timeout) {
            clearTimeout(timeout)
        }
        
        timeout = setTimeout(() => {
            fn(...args)
            timeout = null
        }, delay)
    }
}

/**
 * 创建一个带有即时执行选项的防抖函数
 * @param fn 要防抖的函数
 * @param delay 防抖延迟时间（毫秒）
 * @param immediate 是否立即执行
 * @returns 防抖后的函数和取消函数
 */
export function useDebounceWithImmediate<T extends (...args: any[]) => any>(
    fn: T,
    delay = 200,
    immediate = false
): {
    debouncedFn: (...args: Parameters<T>) => void
    cancel: () => void
} {
    let timeout: ReturnType<typeof setTimeout> | null = null
    let hasExecuted = false

    const debouncedFn = (...args: Parameters<T>) => {
        const callNow = immediate && !hasExecuted

        if (timeout) {
            clearTimeout(timeout)
        }

        if (callNow) {
            fn(...args)
            hasExecuted = true
        }

        timeout = setTimeout(() => {
            if (!immediate) {
                fn(...args)
            }
            timeout = null
            hasExecuted = false
        }, delay)
    }

    const cancel = () => {
        if (timeout) {
            clearTimeout(timeout)
            timeout = null
        }
        hasExecuted = false
    }

    return {
        debouncedFn,
        cancel
    }
}