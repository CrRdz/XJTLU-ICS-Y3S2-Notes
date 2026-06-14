<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppPagination from '../../components/AppPagination.vue'
import { useResponsivePageResult } from '../../composables/useResponsivePageResult'
import {
  getCurrentUser,
  normalizePageResult,
  studentApi,
} from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

const router = useRouter()

const readStorageKey = computed(() => {
  const user = getCurrentUser()
  return `studentNotificationRead:${user.userId || user.username || 'current'}`
})

function readMap(): Record<string, boolean> {
  try {
    return JSON.parse(localStorage.getItem(readStorageKey.value) || '{}')
  } catch {
    return {}
  }
}

function saveRead(id: string) {
  const map = readMap()
  map[id] = true
  localStorage.setItem(readStorageKey.value, JSON.stringify(map))
}

function normalizeStatus(status: string | null | undefined): string {
  return String(status || 'UNKNOWN').toUpperCase()
}

function formatDate(value: string | null | undefined): string {
  if (!value) return '-'
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value).slice(0, 16)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function notificationTime(r: any): string {
  return r.reviewedAt || r.withdrawnAt || r.submittedAt
}

function notificationType(status: string): string {
  if (status === 'PENDING') return 'new'
  if (status === 'WITHDRAWN') return 'withdrawn'
  if (status === 'ACCEPTED') return 'accepted'
  if (status === 'REJECTED') return 'rejected'
  return 'system'
}

function notificationTitle(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'Application Submitted',
    WITHDRAWN: 'Application Withdrawn',
    ACCEPTED: 'Application Accepted',
    REJECTED: 'Application Rejected',
  }
  return map[status] || 'Status Update'
}

function notificationMessage(r: any, status: string): string {
  const project = r.projectTitle || 'Untitled Project'
  if (status === 'PENDING')
    return `Your application for "${project}" has been submitted and is waiting for review.`
  if (status === 'WITHDRAWN')
    return `You withdrew your application for "${project}".`
  if (status === 'ACCEPTED')
    return `Your application for "${project}" has been accepted.`
  if (status === 'REJECTED') {
    const feedback = r.decisionComment ? ` Feedback: ${r.decisionComment}` : ''
    return `Your application for "${project}" was rejected.${feedback}`
  }
  return `Your application for "${project}" has been updated.`
}

function iconClass(type: string): string {
  const map: Record<string, string> = {
    new: 'bi-envelope-paper',
    withdrawn: 'bi-exclamation-triangle',
    accepted: 'bi-check-circle',
    rejected: 'bi-x-circle',
    system: 'bi-info-circle',
  }
  return map[type] || 'bi-bell'
}

function iconBg(type: string): string {
  if (type === 'withdrawn' || type === 'rejected') return 'rgba(246,166,61,0.1)'
  if (type === 'accepted') return 'rgba(47,197,168,0.12)'
  return 'rgba(90,43,152,0.1)'
}

function iconColor(type: string): string {
  if (type === 'withdrawn' || type === 'rejected') return 'var(--orange)'
  if (type === 'accepted') return 'var(--green)'
  return 'var(--deep)'
}

interface NotificationItem {
  id: string
  type: string
  title: string
  message: string
  time: string
  status: string
  read: boolean
  requestId: number
  projectId: number
}

function buildNotifications(requests: any[]): NotificationItem[] {
  const map = readMap()
  return requests
    .map((r) => {
      const status = normalizeStatus(r.requestStatus)
      const time = notificationTime(r)
      const id = `student-request-${r.requestId}-${status}-${time || 'unknown'}`
      return {
        id,
        type: notificationType(status),
        title: notificationTitle(status),
        message: notificationMessage(r, status),
        time,
        status,
        read: Boolean(map[id]),
        requestId: r.requestId,
        projectId: r.projectId,
      }
    })
    .sort((a, b) => {
      if (a.read !== b.read) return a.read ? 1 : -1
      return new Date(b.time || 0).getTime() - new Date(a.time || 0).getTime()
    })
}

const {
  tableWrapperRef,
  loading,
  records: notifications,
  currentPage,
  pageSize,
  total,
  totalPages,
  visiblePages,
  initialize,
  loadPage: loadNotifications,
} = useResponsivePageResult<NotificationItem>({
  loadPage: async ({ pageNum, pageSize }) => {
    const res = await studentApi.getRequestsPage({ pageNum, pageSize })
    const pageResult = normalizePageResult(res.data, { pageNum, pageSize })
    return {
      ...pageResult,
      records: buildNotifications(pageResult.records),
    }
  },
  onLoadError: (error) => {
    const message =
      error instanceof Error ? error.message : 'Failed to load notifications'
    toast.error(message)
  },
  mobileRowHeight: 184,
  desktopRowHeight: 156,
  mobileMinRows: 2,
  tabletMinRows: 3,
  desktopMinRows: 3,
  mobileMaxRows: 4,
  tabletMaxRows: 5,
  desktopMaxRows: 5,
})

void tableWrapperRef

const unreadCount = computed(
  () => notifications.value.filter((n) => !n.read).length,
)

function markRead(item: NotificationItem) {
  if (item.read) return
  item.read = true
  saveRead(item.id)
}

function markAllRead() {
  notifications.value.forEach((n) => markRead(n))
}

function viewDetail(projectId: number) {
  router.push(`/student/projects/${projectId}`)
}

onMounted(() => {
  void initialize()
})
</script>

<template>
  <div class="notifications-page">
    <header class="page-header">
      <h1>Notifications</h1>
    </header>

    <div class="toolbar">
      <span class="status-text">
        <template v-if="loading">Loading...</template>
        <template v-else-if="unreadCount === 0"
          >No unread notifications</template
        >
        <template v-else>{{ unreadCount }} unread notification(s)</template>
      </span>
      <button
        class="btn-mark-all"
        :disabled="unreadCount === 0"
        @click="markAllRead"
      >
        Mark All Read
      </button>
    </div>

    <div ref="tableWrapperRef">
      <div
        v-if="loading"
        class="panel"
        style="text-align: center; padding: 40px; color: var(--muted)"
      >
        Loading...
      </div>

      <div
        v-else-if="notifications.length === 0"
        class="panel"
        style="text-align: center; padding: 40px; color: var(--muted)"
      >
        No notifications yet.
      </div>

      <div v-else class="notification-list">
        <div
          v-for="n in notifications"
          :key="n.id"
          class="notification-item"
          :class="{ unread: !n.read }"
          @click="markRead(n)"
        >
          <div
            class="notification-icon"
            :style="{ background: iconBg(n.type), color: iconColor(n.type) }"
          >
            <i :class="`bi ${iconClass(n.type)}`"></i>
          </div>
          <div class="msg-body">
            <div class="notification-title">
              {{ n.title }}
              <span v-if="!n.read" class="unread-dot"></span>
            </div>
            <div class="notification-message">{{ n.message }}</div>
            <div class="notification-meta">
              <span><i class="bi bi-clock"></i> {{ formatDate(n.time) }}</span>
              <span><i class="bi bi-tag"></i> {{ n.status }}</span>
            </div>
          </div>
          <div class="notification-actions">
            <button
              v-if="!n.read"
              class="btn-sm btn-read"
              @click.stop="markRead(n)"
            >
              Read
            </button>
            <button
              class="btn-sm btn-detail"
              @click.stop="viewDetail(n.projectId)"
            >
              <i class="bi bi-arrow-right-circle"></i>
            </button>
          </div>
        </div>

        <AppPagination
          :current-page="currentPage"
          :total-pages="totalPages"
          :total-items="total"
          :page-size="pageSize"
          :pages="visiblePages"
          item-label="notifications"
          @change="loadNotifications"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.notifications-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text);
}

.panel {
  background: #fff;
  border-radius: 28px;
  padding: 24px 28px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-text {
  font-size: 0.9rem;
  color: var(--muted);
}

.btn-mark-all {
  padding: 8px 14px;
  border-radius: 12px;
  border: 1px solid rgba(90, 43, 152, 0.16);
  background: linear-gradient(180deg, #fff, #f7f2ff);
  color: var(--deep);
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  font-size: 0.85rem;
}

.btn-mark-all:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-mark-all:hover:not(:disabled) {
  background: linear-gradient(180deg, #fff, #efe4ff);
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.notification-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 16px;
  align-items: flex-start;
  padding: 18px 20px;
  border-radius: 22px;
  border: 1px solid rgba(90, 43, 152, 0.1);
  background: #fcfbff;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
  cursor: pointer;
}

.notification-item:hover {
  transform: translateY(-1px);
}

.notification-item.unread {
  background: linear-gradient(
    180deg,
    rgba(90, 43, 152, 0.05),
    rgba(36, 179, 255, 0.03)
  );
}

.notification-icon {
  width: 46px;
  height: 46px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.msg-body {
  min-width: 0;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 1rem;
  font-weight: 700;
  color: var(--text);
}

.notification-message {
  color: #444;
  line-height: 1.65;
  font-size: 0.9rem;
}

.notification-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 10px;
  color: var(--muted);
  font-size: 0.84rem;
}

.unread-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #ff6b6b;
  display: inline-block;
}

.notification-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.btn-sm {
  padding: 6px 14px;
  border-radius: 20px;
  border: 1.5px solid;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
  background: transparent;
}

.btn-read {
  border-color: var(--deep);
  color: var(--deep);
}

.btn-read:hover {
  background: var(--deep);
  color: #fff;
}

.btn-detail {
  border-color: var(--muted);
  color: var(--muted);
}

.btn-detail:hover {
  background: var(--muted);
  color: #fff;
}
</style>
