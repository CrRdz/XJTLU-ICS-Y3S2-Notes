import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import type { PageResult } from '../utils/api'

interface LoadPageParams {
  pageNum: number
  pageSize: number
}

interface ResponsivePageResultOptions<T> {
  loadPage: (params: LoadPageParams) => Promise<PageResult<T>>
  onLoadError?: (error: unknown) => void
  defaultPageSize?: number
  mobileBreakpoint?: number
  tabletBreakpoint?: number
  mobileItemsPerRow?: number
  tabletItemsPerRow?: number
  desktopItemsPerRow?: number
  mobileRowHeight?: number
  desktopRowHeight?: number
  mobilePaginationSpace?: number
  desktopPaginationSpace?: number
  mobileMinRows?: number
  tabletMinRows?: number
  desktopMinRows?: number
  mobileMaxRows?: number
  tabletMaxRows?: number
  desktopMaxRows?: number
  resizeDebounceMs?: number
  initialTableTop?: number
}

const DEFAULT_PAGE_SIZE = 8
const DEFAULT_MOBILE_BREAKPOINT = 720
const DEFAULT_TABLET_BREAKPOINT = 960
const DEFAULT_MOBILE_ROW_HEIGHT = 60
const DEFAULT_DESKTOP_ROW_HEIGHT = 56
const DEFAULT_MOBILE_PAGINATION_SPACE = 176
const DEFAULT_DESKTOP_PAGINATION_SPACE = 128
const DEFAULT_RESIZE_DEBOUNCE_MS = 120
const DEFAULT_INITIAL_TABLE_TOP = 320

function clamp(value: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, value))
}

function toPositiveInt(value: unknown, fallback: number): number {
  const normalized = Number(value)
  if (!Number.isFinite(normalized) || normalized < 1) return fallback
  return Math.floor(normalized)
}

function buildVisiblePages(currentPage: number, totalPages: number): number[] {
  if (totalPages <= 5) {
    return Array.from({ length: totalPages }, (_, index) => index + 1)
  }

  if (currentPage <= 3) return [1, 2, 3, 4, 5]
  if (currentPage >= totalPages - 2) {
    return [
      totalPages - 4,
      totalPages - 3,
      totalPages - 2,
      totalPages - 1,
      totalPages,
    ]
  }

  return [
    currentPage - 2,
    currentPage - 1,
    currentPage,
    currentPage + 1,
    currentPage + 2,
  ]
}

export function useResponsivePageResult<T>(
  options: ResponsivePageResultOptions<T>,
) {
  const tableWrapperRef = ref<HTMLElement | null>(null)
  const loading = ref(true)
  const records = ref<T[]>([])
  const currentPage = ref(1)
  const pageSize = ref(
    toPositiveInt(options.defaultPageSize, DEFAULT_PAGE_SIZE),
  )
  const total = ref(0)
  const totalPages = ref(1)
  const visiblePages = computed(() =>
    buildVisiblePages(currentPage.value, totalPages.value),
  )

  const mobileBreakpoint = toPositiveInt(
    options.mobileBreakpoint,
    DEFAULT_MOBILE_BREAKPOINT,
  )
  const tabletBreakpoint = toPositiveInt(
    options.tabletBreakpoint,
    DEFAULT_TABLET_BREAKPOINT,
  )
  const mobileRowHeight = toPositiveInt(
    options.mobileRowHeight,
    DEFAULT_MOBILE_ROW_HEIGHT,
  )
  const mobileItemsPerRow = toPositiveInt(options.mobileItemsPerRow, 1)
  const tabletItemsPerRow = toPositiveInt(options.tabletItemsPerRow, 1)
  const desktopItemsPerRow = toPositiveInt(options.desktopItemsPerRow, 1)
  const desktopRowHeight = toPositiveInt(
    options.desktopRowHeight,
    DEFAULT_DESKTOP_ROW_HEIGHT,
  )
  const mobilePaginationSpace = toPositiveInt(
    options.mobilePaginationSpace,
    DEFAULT_MOBILE_PAGINATION_SPACE,
  )
  const desktopPaginationSpace = toPositiveInt(
    options.desktopPaginationSpace,
    DEFAULT_DESKTOP_PAGINATION_SPACE,
  )
  const mobileMinRows = toPositiveInt(options.mobileMinRows, 4)
  const tabletMinRows = toPositiveInt(options.tabletMinRows, 5)
  const desktopMinRows = toPositiveInt(options.desktopMinRows, 6)
  const mobileMaxRows = toPositiveInt(options.mobileMaxRows, 8)
  const tabletMaxRows = toPositiveInt(options.tabletMaxRows, 10)
  const desktopMaxRows = toPositiveInt(options.desktopMaxRows, 14)
  const resizeDebounceMs = toPositiveInt(
    options.resizeDebounceMs,
    DEFAULT_RESIZE_DEBOUNCE_MS,
  )
  const initialTableTop = toPositiveInt(
    options.initialTableTop,
    DEFAULT_INITIAL_TABLE_TOP,
  )

  let latestLoadId = 0
  let resizeTimer: ReturnType<typeof setTimeout> | null = null
  let initialized = false

  function calculateResponsivePageSize(): number {
    if (typeof window === 'undefined') return pageSize.value

    const viewportWidth = window.innerWidth
    const tableTop =
      tableWrapperRef.value?.getBoundingClientRect().top ?? initialTableTop
    const isMobile = viewportWidth <= mobileBreakpoint
    const isTablet = viewportWidth <= tabletBreakpoint
    const itemsPerRow = isMobile
      ? mobileItemsPerRow
      : isTablet
        ? tabletItemsPerRow
        : desktopItemsPerRow
    const rowHeight = isMobile ? mobileRowHeight : desktopRowHeight
    const paginationSpace = isMobile
      ? mobilePaginationSpace
      : desktopPaginationSpace
    const minRows = isMobile
      ? mobileMinRows
      : isTablet
        ? tabletMinRows
        : desktopMinRows
    const maxRows = isMobile
      ? mobileMaxRows
      : isTablet
        ? tabletMaxRows
        : desktopMaxRows
    const availableHeight = Math.max(
      rowHeight * minRows,
      window.innerHeight - tableTop - paginationSpace,
    )

    return (
      clamp(Math.floor(availableHeight / rowHeight), minRows, maxRows) *
      itemsPerRow
    )
  }

  async function loadPage(page = currentPage.value) {
    const loadId = ++latestLoadId
    loading.value = true

    try {
      const result = await options.loadPage({
        pageNum: page,
        pageSize: pageSize.value,
      })

      if (loadId !== latestLoadId) return

      const nextRecords = Array.isArray(result.records) ? result.records : []
      const nextPageSize = toPositiveInt(result.pageSize, pageSize.value)
      const nextTotal = Math.max(nextRecords.length, Number(result.total) || 0)
      const nextTotalPages = Math.max(
        1,
        toPositiveInt(
          result.totalPages,
          Math.ceil(nextTotal / nextPageSize) || 1,
        ),
      )
      const nextPage = clamp(
        toPositiveInt(result.pageNum, page),
        1,
        nextTotalPages,
      )

      records.value = nextRecords
      total.value = nextTotal
      totalPages.value = nextTotalPages
      currentPage.value = nextPage
      pageSize.value = nextPageSize
    } catch (error) {
      if (loadId !== latestLoadId) return

      records.value = []
      total.value = 0
      totalPages.value = 1
      currentPage.value = 1
      options.onLoadError?.(error)
    } finally {
      if (loadId === latestLoadId) {
        loading.value = false
      }
    }
  }

  async function syncPageSize(shouldReload: boolean) {
    const nextPageSize = calculateResponsivePageSize()
    if (nextPageSize === pageSize.value) return

    const firstRecordIndex = Math.max(
      0,
      (currentPage.value - 1) * pageSize.value,
    )
    pageSize.value = nextPageSize

    if (!shouldReload) return

    const nextPage = Math.floor(firstRecordIndex / nextPageSize) + 1
    await loadPage(nextPage)
  }

  function schedulePageSizeSync() {
    if (resizeTimer) clearTimeout(resizeTimer)
    resizeTimer = setTimeout(() => {
      if (!initialized) return
      void syncPageSize(true)
    }, resizeDebounceMs)
  }

  async function initialize(page = 1) {
    await nextTick()
    await syncPageSize(false)
    await loadPage(page)
    initialized = true

    if (typeof window !== 'undefined') {
      window.addEventListener('resize', schedulePageSizeSync)
    }
  }

  onBeforeUnmount(() => {
    if (resizeTimer) clearTimeout(resizeTimer)

    if (typeof window !== 'undefined') {
      window.removeEventListener('resize', schedulePageSizeSync)
    }
  })

  return {
    tableWrapperRef,
    loading,
    records,
    currentPage,
    pageSize,
    total,
    totalPages,
    visiblePages,
    initialize,
    loadPage,
    syncPageSize,
  }
}
