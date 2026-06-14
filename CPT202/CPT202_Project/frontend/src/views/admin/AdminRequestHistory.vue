<script setup lang="ts">
import { onMounted } from 'vue'
import AppPagination from '../../components/AppPagination.vue'
import { useResponsivePageResult } from '../../composables/useResponsivePageResult'
import { adminApi } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

const {
  tableWrapperRef,
  loading,
  records,
  currentPage,
  pageSize,
  total,
  totalPages,
  visiblePages,
  initialize,
  loadPage: loadData,
} = useResponsivePageResult<any>({
  loadPage: async ({ pageNum, pageSize }) => {
    const res = await adminApi.listRequestHistoryRecordsPage({
      pageNum,
      pageSize,
    })
    return res.data
  },
  onLoadError: (error) => {
    const message =
      error instanceof Error ? error.message : 'Failed to load history'
    toast.error(message)
  },
})

void tableWrapperRef

function formatDate(value: string | null | undefined): string {
  if (!value) return '-'
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value).replace('T', ' ').slice(0, 16)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => {
  void initialize()
})
</script>

<template>
  <div class="page">
    <header class="page-header">
      <h1>History</h1>
    </header>

    <div class="panel">
      <div ref="tableWrapperRef" class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>History ID</th>
              <th>Request ID</th>
              <th>Old Request Status</th>
              <th>New Request Status</th>
              <th>Changed By</th>
              <th>Remark</th>
              <th>Changed At</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" style="text-align: center; color: #888">
                Loading...
              </td>
            </tr>
            <tr v-else-if="records.length === 0">
              <td colspan="7" style="text-align: center; color: #888">
                No history records
              </td>
            </tr>
            <tr v-for="r in records" :key="r.historyId || r.id">
              <td>{{ r.historyId || r.id || '-' }}</td>
              <td>{{ r.requestId || '-' }}</td>
              <td>{{ r.oldStatus || '-' }}</td>
              <td>{{ r.newStatus || '-' }}</td>
              <td>{{ r.changedByStudentName || r.changedBy || '-' }}</td>
              <td>{{ r.remark || '-' }}</td>
              <td>{{ formatDate(r.changedAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <AppPagination
        v-if="total > 0"
        :current-page="currentPage"
        :total-pages="totalPages"
        :total-items="total"
        :page-size="pageSize"
        :pages="visiblePages"
        item-label="history records"
        @change="loadData"
      />
    </div>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.page-header {
  padding: 0;
}
.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text);
}
.panel {
  background: #fff;
  border: 1px solid rgba(90, 43, 152, 0.16);
  border-radius: 18px;
  padding: 24px 28px;
  box-shadow: 0 12px 30px rgba(90, 43, 152, 0.05);
}
.table-wrapper {
  overflow-x: auto;
  border-radius: 18px;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 700px;
  font-size: 0.9rem;
}
.data-table th {
  text-align: left;
  padding: 16px;
  background: #f8f5ff;
  font-weight: 600;
  color: var(--deep);
  border-bottom: 2px solid rgba(90, 43, 152, 0.2);
}
.data-table td {
  padding: 16px;
  border-bottom: 1px solid rgba(156, 156, 178, 0.2);
}
.data-table tbody tr:hover {
  background: rgba(90, 43, 152, 0.03);
}
.summary {
  margin-top: 16px;
  font-size: 0.9rem;
  color: var(--muted);
}
</style>
