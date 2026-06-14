<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppPagination from '../../components/AppPagination.vue'
import { useResponsivePageResult } from '../../composables/useResponsivePageResult'
import { normalizePageResult, teacherApi } from '../../utils/api'
import { toast, confirm } from '../../utils/ui-feedback'

const router = useRouter()
const statusFilter = ref('')
const showRejectModal = ref(false)
const rejectRequestId = ref<number | null>(null)
const rejectComment = ref('')
const rejecting = ref(false)

function normalizeStatus(status: string | null | undefined): string {
  return String(status || 'UNKNOWN').toUpperCase()
}

function statusColor(status: string): string {
  const s = normalizeStatus(status)
  if (s === 'PENDING') return 'var(--orange)'
  if (s === 'ACCEPTED') return 'var(--green)'
  if (s === 'REJECTED') return 'var(--red)'
  if (s === 'WITHDRAWN') return 'var(--muted)'
  return 'var(--deep)'
}

function statusText(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'Pending',
    ACCEPTED: 'Accepted',
    REJECTED: 'Rejected',
    WITHDRAWN: 'Withdrawn',
  }
  return map[normalizeStatus(status)] || normalizeStatus(status)
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

const {
  tableWrapperRef,
  loading,
  records: requests,
  currentPage,
  pageSize,
  total,
  totalPages,
  visiblePages,
  initialize,
  loadPage: loadRequests,
} = useResponsivePageResult<any>({
  loadPage: async ({ pageNum, pageSize }) => {
    const res = await teacherApi.listRequestsPage(
      statusFilter.value || undefined,
      {
        pageNum,
        pageSize,
      },
    )
    return normalizePageResult(res.data, { pageNum, pageSize })
  },
  onLoadError: (error) => {
    const message =
      error instanceof Error ? error.message : 'Failed to load requests'
    toast.error(message)
  },
  mobileRowHeight: 92,
  desktopRowHeight: 72,
  mobileMinRows: 4,
  tabletMinRows: 5,
  desktopMinRows: 6,
  mobileMaxRows: 8,
  tabletMaxRows: 10,
  desktopMaxRows: 12,
})

void tableWrapperRef

function clearFilters() {
  statusFilter.value = ''
  void loadRequests(1)
}

function viewDetail(requestId: number) {
  router.push(`/teacher/request/${requestId}`)
}

async function handleAccept(requestId: number) {
  const confirmed = await confirm(
    'Are you sure you want to accept this application?',
  )
  if (!confirmed) return
  try {
    await teacherApi.reviewRequest(requestId, 'ACCEPTED')
    toast.success('Application accepted')
    await loadRequests(currentPage.value)
  } catch (e: any) {
    toast.error(e.message || 'Failed to accept')
  }
}

function openRejectModal(requestId: number) {
  rejectRequestId.value = requestId
  rejectComment.value = ''
  showRejectModal.value = true
}

function closeRejectModal() {
  showRejectModal.value = false
  rejectRequestId.value = null
  rejectComment.value = ''
}

async function handleReject() {
  if (rejectRequestId.value === null) return
  rejecting.value = true
  try {
    await teacherApi.reviewRequest(
      rejectRequestId.value,
      'REJECTED',
      rejectComment.value.trim(),
    )
    toast.success('Application rejected')
    closeRejectModal()
    await loadRequests(currentPage.value)
  } catch (e: any) {
    toast.error(e.message || 'Failed to reject')
  } finally {
    rejecting.value = false
  }
}

onMounted(() => {
  void initialize()
})
</script>

<template>
  <div class="dashboard-page">
    <header class="page-header">
      <h1>Review Requests</h1>
    </header>

    <div class="panel">
      <div class="filters-row">
        <span class="filter-control select-control">
          <select
            v-model="statusFilter"
            aria-label="Request Status"
            @change="loadRequests(1)"
          >
            <option value="">All Request Status</option>
            <option value="PENDING">Pending</option>
            <option value="ACCEPTED">Accepted</option>
            <option value="REJECTED">Rejected</option>
            <option value="WITHDRAWN">Withdrawn</option>
          </select>
        </span>
        <button class="clear-btn" @click="clearFilters">
          <i class="bi bi-arrow-counterclockwise"></i>
          Clear
        </button>
      </div>

      <div ref="tableWrapperRef" class="table-wrapper">
        <table class="request-table">
          <thead>
            <tr>
              <th>Student</th>
              <th>Project</th>
              <th>Submitted</th>
              <th>Request Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="5" style="text-align: center; color: #888">
                Loading...
              </td>
            </tr>
            <tr v-else-if="requests.length === 0">
              <td colspan="5" style="text-align: center; color: #888">
                No requests found
              </td>
            </tr>
            <tr v-for="r in requests" :key="r.requestId">
              <td>
                <div class="student-info">
                  <span class="student-name">{{ r.studentName || '-' }}</span>
                  <span class="student-id">{{ r.studentId || '' }}</span>
                </div>
              </td>
              <td>{{ r.projectTitle || '-' }}</td>
              <td>{{ formatDate(r.submittedAt) }}</td>
              <td>
                <span
                  class="status-pill"
                  :style="{
                    color: statusColor(r.requestStatus),
                    background: statusColor(r.requestStatus) + '18',
                  }"
                >
                  {{ statusText(r.requestStatus) }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button
                    class="btn-sm btn-detail"
                    @click="viewDetail(r.requestId)"
                  >
                    <i class="bi bi-eye"></i> Detail
                  </button>
                  <button
                    v-if="normalizeStatus(r.requestStatus) === 'PENDING'"
                    class="btn-sm btn-accept"
                    @click="handleAccept(r.requestId)"
                  >
                    <i class="bi bi-check-lg"></i> Accept
                  </button>
                  <button
                    v-if="normalizeStatus(r.requestStatus) === 'PENDING'"
                    class="btn-sm btn-reject"
                    @click="openRejectModal(r.requestId)"
                  >
                    <i class="bi bi-x-lg"></i> Reject
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AppPagination
        v-if="!loading && total > 0"
        :current-page="currentPage"
        :total-pages="totalPages"
        :total-items="total"
        :page-size="pageSize"
        :pages="visiblePages"
        item-label="requests"
        @change="loadRequests"
      />

      <div class="summary">{{ total }} request(s)</div>
    </div>

    <Teleport to="body">
      <div
        v-if="showRejectModal"
        class="modal-overlay"
        @click.self="closeRejectModal"
      >
        <div class="modal-dialog">
          <div class="modal-header">
            <h2>Reject Application</h2>
            <button class="icon-button" type="button" @click="closeRejectModal">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <div class="modal-body">
            <label class="field-label">Rejection Reason (optional)</label>
            <textarea
              v-model="rejectComment"
              rows="4"
              class="reject-textarea"
              placeholder="Enter feedback for the student..."
            ></textarea>
          </div>
          <div class="modal-actions">
            <button
              type="button"
              class="btn-secondary"
              @click="closeRejectModal"
            >
              Cancel
            </button>
            <button
              type="button"
              class="btn-reject modal-reject-btn"
              :disabled="rejecting"
              @click="handleReject"
            >
              {{ rejecting ? 'Rejecting...' : 'Confirm Reject' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.dashboard-page {
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

.filters-row {
  display: grid;
  grid-template-columns: minmax(170px, 0.9fr) auto;
  gap: 12px;
  align-items: end;
  margin-bottom: 16px;
  max-width: 360px;
}

.filter-control {
  min-height: 44px;
  border: 1.5px solid rgba(90, 43, 152, 0.16);
  border-radius: 12px;
  background: #fff;
  display: flex;
  align-items: center;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease;
}

.filter-control:focus-within {
  border-color: var(--deep);
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.08);
}

.filter-control select {
  width: 100%;
  min-width: 0;
  border: 0;
  outline: none;
  background: transparent;
  color: var(--text);
  font: inherit;
  font-size: 0.95rem;
}

.select-control select {
  height: 42px;
  padding: 0 12px;
  cursor: pointer;
}

.clear-btn {
  min-height: 44px;
  padding: 0 16px;
  border-radius: 12px;
  border: 1.5px solid rgba(90, 43, 152, 0.16);
  background: #fff;
  color: var(--deep);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  font-family: inherit;
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}

.clear-btn:hover {
  border-color: var(--deep);
  background: rgba(90, 43, 152, 0.06);
}

.table-wrapper {
  overflow-x: auto;
  border-radius: 18px;
}

.request-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 700px;
  font-size: 0.9rem;
}

.request-table th {
  text-align: left;
  padding: 16px;
  background: #f8f5ff;
  font-weight: 600;
  color: var(--deep);
  border-bottom: 2px solid rgba(90, 43, 152, 0.2);
}

.request-table td {
  padding: 16px;
  border-bottom: 1px solid rgba(156, 156, 178, 0.2);
}

.request-table tbody tr:hover {
  background: rgba(90, 43, 152, 0.03);
}

.student-info {
  display: flex;
  flex-direction: column;
}

.student-name {
  font-weight: 600;
  color: var(--text);
}

.student-id {
  font-size: 0.8rem;
  color: var(--muted);
}

.status-pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
}

.action-btns {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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
}

.btn-detail {
  border-color: var(--deep);
  color: var(--deep);
  background: transparent;
}

.btn-detail:hover {
  background: var(--deep);
  color: #fff;
}

.btn-accept {
  border-color: var(--green);
  color: var(--green);
  background: transparent;
}

.btn-accept:hover {
  background: var(--green);
  color: #fff;
}

.btn-reject {
  border-color: var(--red);
  color: var(--red);
  background: transparent;
}

.btn-reject:hover {
  background: var(--red);
  color: #fff;
}

.summary {
  margin-top: 16px;
  font-size: 0.9rem;
  color: var(--muted);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(28, 27, 51, 0.45);
  z-index: 30;
}

.modal-dialog {
  width: min(520px, 100%);
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 30px 80px rgba(28, 27, 51, 0.22);
  padding: 24px 28px 26px;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.modal-header h2 {
  margin: 0;
  color: var(--text);
  font-size: 1.35rem;
  font-weight: 600;
}

.icon-button {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 1px solid rgba(90, 43, 152, 0.16);
  background: rgba(90, 43, 152, 0.06);
  color: var(--deep);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  font-weight: 600;
  color: var(--text);
  font-size: 0.9rem;
}

.reject-textarea {
  width: 100%;
  min-height: 110px;
  resize: vertical;
  padding: 12px 14px;
  border: 1.5px solid rgba(90, 43, 152, 0.18);
  border-radius: 12px;
  font-size: 0.95rem;
  font-family: inherit;
  outline: none;
  background: #fff;
}

.reject-textarea:focus {
  border-color: var(--deep);
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.1);
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 18px;
}

.btn-secondary {
  background: transparent;
  border: 1.5px solid var(--muted);
  color: var(--text);
  padding: 10px 20px;
  border-radius: 40px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
}

.btn-secondary:hover {
  border-color: var(--deep);
  color: var(--deep);
}

.modal-reject-btn {
  padding: 10px 20px;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
}

.modal-reject-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 560px) {
  .filters-row {
    grid-template-columns: 1fr;
    max-width: none;
  }

  .modal-actions {
    flex-direction: column-reverse;
  }

  .modal-actions .btn-secondary,
  .modal-actions .modal-reject-btn {
    width: 100%;
  }
}
</style>
