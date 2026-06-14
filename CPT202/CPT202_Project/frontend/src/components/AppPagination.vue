<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  currentPage: number
  totalPages: number
  totalItems: number
  pageSize: number
  pages: number[]
  itemLabel?: string
}>()

const emit = defineEmits<{
  change: [page: number]
}>()

const label = props.itemLabel || 'items'

const firstItem = computed(() => {
  if (props.totalItems === 0) return 0
  return (props.currentPage - 1) * props.pageSize + 1
})

const lastItem = computed(() => {
  return Math.min(props.currentPage * props.pageSize, props.totalItems)
})

function goToPage(page: number) {
  if (page < 1 || page > props.totalPages || page === props.currentPage) return
  emit('change', page)
}
</script>

<template>
  <nav class="app-pagination" aria-label="Pagination">
    <div class="pagination-meta">
      <span class="meta-strong">{{ firstItem }}-{{ lastItem }}</span>
      <span>of {{ totalItems }} {{ label }}</span>
      <span class="meta-divider"></span>
      <span>Page {{ currentPage }} of {{ totalPages }}</span>
    </div>

    <div class="pagination-controls">
      <button
        type="button"
        class="page-button nav-button"
        :disabled="currentPage <= 1"
        aria-label="Previous page"
        @click="goToPage(currentPage - 1)"
      >
        <i class="bi bi-chevron-left"></i>
        <span>Prev</span>
      </button>

      <div class="page-numbers">
        <button
          v-for="page in pages"
          :key="page"
          type="button"
          class="page-button number-button"
          :class="{ active: page === currentPage }"
          :aria-current="page === currentPage ? 'page' : undefined"
          @click="goToPage(page)"
        >
          {{ page }}
        </button>
      </div>

      <button
        type="button"
        class="page-button nav-button"
        :disabled="currentPage >= totalPages"
        aria-label="Next page"
        @click="goToPage(currentPage + 1)"
      >
        <span>Next</span>
        <i class="bi bi-chevron-right"></i>
      </button>
    </div>
  </nav>
</template>

<style scoped>
.app-pagination {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px;
  border: 1px solid rgba(90, 43, 152, 0.12);
  border-radius: 14px;
  background: #fff;
}

.pagination-meta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--muted);
  font-size: 0.9rem;
  white-space: nowrap;
}

.meta-strong {
  color: var(--text);
  font-weight: 700;
}

.meta-divider {
  width: 1px;
  height: 16px;
  background: rgba(156, 156, 178, 0.35);
}

.pagination-controls,
.page-numbers {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.page-button {
  min-width: 38px;
  height: 38px;
  border: 1px solid rgba(90, 43, 152, 0.16);
  border-radius: 10px;
  background: #fff;
  color: var(--text);
  cursor: pointer;
  font-family: inherit;
  font-size: 0.9rem;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition:
    background 0.15s ease,
    border-color 0.15s ease,
    color 0.15s ease;
}

.page-button:hover:not(:disabled),
.page-button.active {
  border-color: var(--deep);
  background: var(--deep);
  color: #fff;
}

.page-button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.nav-button {
  padding: 0 12px;
}

@media (max-width: 720px) {
  .app-pagination {
    align-items: stretch;
    flex-direction: column;
  }

  .pagination-meta {
    justify-content: center;
  }

  .pagination-controls {
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>
