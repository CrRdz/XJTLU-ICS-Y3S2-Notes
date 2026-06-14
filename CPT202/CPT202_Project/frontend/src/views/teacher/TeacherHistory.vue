<script setup lang="ts">
import { onMounted } from 'vue'
import AppPagination from '../../components/AppPagination.vue'
import { useResponsivePageResult } from '../../composables/useResponsivePageResult'
import { normalizePageResult, teacherApi } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

function normalizeStatus(status: string | null | undefined): string {
  return String(status || 'UNKNOWN').toUpperCase()
}

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

function statusText(status: string): string {
  const map: Record<string, string> = {
    ACCEPTED: 'Accepted',
    REJECTED: 'Rejected',
    WITHDRAWN: 'Withdrawn',
    PENDING: 'Pending',
  }
  return map[normalizeStatus(status)] || normalizeStatus(status)
}

function statusColor(status: string): string {
  const s = normalizeStatus(status)
  if (s === 'ACCEPTED') return 'var(--green)'
  if (s === 'REJECTED') return 'var(--red)'
  if (s === 'WITHDRAWN') return 'var(--muted)'
  return 'var(--text)'
}

function historyTime(r: any): string {
  return r.reviewedAt || r.withdrawnAt || r.submittedAt
}

const {
  tableWrapperRef,
  loading,
  records: historyItems,
  currentPage,
  pageSize,
  total,
  totalPages,
  visiblePages,
  initialize,
  loadPage: loadHistory,
} = useResponsivePageResult<any>({
  loadPage: async ({ pageNum, pageSize }) => {
    const res = await teacherApi.listHistoryPage({ pageNum, pageSize })
    const pageResult = normalizePageResult(res.data, { pageNum, pageSize })
    return {
      ...pageResult,
      records: pageResult.records.map((r) => ({
        requestId: r.requestId,
        studentName: r.studentName || '-',
        studentId: r.studentId || '-',
        projectTitle: r.projectTitle || 'Untitled Project',
        status: normalizeStatus(r.requestStatus),
        notes: r.notes || '',
        decisionComment: r.decisionComment || '',
        changedAt: historyTime(r),
      })),
    }
  },
  onLoadError: (error) => {
    const message =
      error instanceof Error ? error.message : 'Failed to load history'
    toast.error(message)
  },
  mobileRowHeight: 176,
  desktopRowHeight: 148,
  mobileMinRows: 2,
  tabletMinRows: 3,
  desktopMinRows: 3,
  mobileMaxRows: 4,
  tabletMaxRows: 5,
  desktopMaxRows: 5,
})

void tableWrapperRef

onMounted(() => {
  void initialize()
})
</script>

<template>
  <div class="history-page">
    <header class="page-header">
      <h1>History</h1>
    </header>

    <div ref="tableWrapperRef" class="panel">
      <div v-if="loading" class="timeline-vertical">
        <div class="item">
          <div class="timeline-title">Loading...</div>
          <div class="timeline-desc">Please wait</div>
        </div>
      </div>

      <div v-else-if="historyItems.length === 0" class="timeline-vertical">
        <div class="item">
          <div class="timeline-title">No history yet</div>
          <div class="timeline-desc">Reviewed requests will appear here.</div>
        </div>
      </div>

      <div v-else class="timeline-vertical">
        <div
          v-for="item in historyItems"
          :key="`${item.requestId}-${item.changedAt}`"
          class="item"
        >
          <div class="timeline-date">{{ formatDate(item.changedAt) }}</div>
          <div
            class="timeline-title"
            :style="{ color: statusColor(item.status) }"
          >
            Request: {{ statusText(item.status) }} — {{ item.projectTitle }}
          </div>
          <div class="timeline-desc">
            <div>
              Student: {{ item.studentName }} (ID: {{ item.studentId }})
            </div>
            <div v-if="item.notes">Notes: {{ item.notes }}</div>
            <div v-if="item.decisionComment">
              Feedback: {{ item.decisionComment }}
            </div>
          </div>
        </div>

        <AppPagination
          :current-page="currentPage"
          :total-pages="totalPages"
          :total-items="total"
          :page-size="pageSize"
          :pages="visiblePages"
          item-label="history records"
          @change="loadHistory"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.history-page {
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

.timeline-vertical {
  position: relative;
  padding-left: 28px;
  margin-top: 10px;
}

.timeline-vertical::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 8px;
  bottom: 24px;
  width: 2px;
  background: #e4e1eb;
}

.timeline-vertical .item {
  position: relative;
  padding-bottom: 28px;
}

.timeline-vertical .item:last-child {
  padding-bottom: 0;
}

.timeline-vertical .item::before {
  content: '';
  position: absolute;
  left: -26.5px;
  top: 2px;
  width: 13px;
  height: 13px;
  border-radius: 50%;
  background: var(--mid);
}

.timeline-date {
  font-size: 0.85rem;
  color: var(--muted);
  margin-bottom: 6px;
}

.timeline-title {
  font-size: 1.1rem;
  font-weight: 700;
  margin-bottom: 6px;
}

.timeline-desc {
  color: #444;
  line-height: 1.6;
  font-size: 0.9rem;
}

.timeline-desc div {
  margin-bottom: 2px;
}
</style>
