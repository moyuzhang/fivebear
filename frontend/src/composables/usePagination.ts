import { ref, computed, Ref, ComputedRef } from 'vue'

export interface PaginationOptions {
    page?: number
    pageSize?: number
    total?: number
}

export interface UsePaginationReturn {
    currentPage: Ref<number>
    pageSize: Ref<number>
    total: Ref<number>
    totalPages: ComputedRef<number>
    offset: ComputedRef<number>
    setPage: (page: number) => void
    setPageSize: (size: number) => void
    setTotal: (total: number) => void
    nextPage: () => void
    prevPage: () => void
    isFirstPage: ComputedRef<boolean>
    isLastPage: ComputedRef<boolean>
    reset: () => void
}

/**
 * 通用分页管理
 * @param options 分页选项
 * @returns 分页管理对象
 */
export function usePagination(options: PaginationOptions = {}): UsePaginationReturn {
    const currentPage = ref(options.page || 1)
    const pageSize = ref(options.pageSize || 20)
    const total = ref(options.total || 0)

    const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
    const offset = computed(() => (currentPage.value - 1) * pageSize.value)
    const isFirstPage = computed(() => currentPage.value === 1)
    const isLastPage = computed(() => currentPage.value >= totalPages.value)

    const setPage = (page: number) => {
        if (page >= 1 && page <= totalPages.value) {
            currentPage.value = page
        }
    }

    const setPageSize = (size: number) => {
        if (size > 0) {
            pageSize.value = size
            // 重新计算当前页，确保不超出范围
            if (currentPage.value > totalPages.value && totalPages.value > 0) {
                currentPage.value = totalPages.value
            }
        }
    }

    const setTotal = (newTotal: number) => {
        total.value = Math.max(0, newTotal)
    }

    const nextPage = () => {
        if (!isLastPage.value) {
            currentPage.value++
        }
    }

    const prevPage = () => {
        if (!isFirstPage.value) {
            currentPage.value--
        }
    }

    const reset = () => {
        currentPage.value = 1
        total.value = 0
    }

    return {
        currentPage,
        pageSize,
        total,
        totalPages,
        offset,
        setPage,
        setPageSize,
        setTotal,
        nextPage,
        prevPage,
        isFirstPage,
        isLastPage,
        reset
    }
}