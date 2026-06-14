<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { teacherApi } from '../../utils/api'
import { toast, confirm } from '../../utils/ui-feedback'
import { useSmartBack } from '../../utils/navigation'

const route = useRoute()
const { goBack } = useSmartBack('/teacher/dashboard', '/teacher')

const loading = ref(true)
const request = ref<any>(null)
const studentProfile = ref<any>(null)
const project = ref<any>(null)
const showRejectModal = ref(false)
const rejectComment = ref('')
const processing = ref(false)

const requestId = route.params.id as string

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

async function loadData() {
  loading.value = true
  try {
    const res = await teacherApi.getRequest(requestId)
    request.value = res.data || null
    if (!request.value) {
      project.value = null
      studentProfile.value = null
      return
    }

    studentProfile.value = {
      fullName: request.value.studentName,
      studentNo: request.value.studentNo,
      email: request.value.studentEmail,
      programme: request.value.studentProgramme,
      phone: request.value.studentPhone,
      interests: request.value.studentInterests,
    }

    if (request.value?.projectId) {
      try {
        const projectRes = await teacherApi.getProject(request.value.projectId)
        project.value = projectRes.data
      } catch {
        project.value = null
      }
    }
  } catch (e: any) {
    toast.error(e.message || 'Failed to load request details')
    request.value = null
  } finally {
    loading.value = false
  }
}

async function handleAccept() {
  const confirmed = await confirm(
    'Are you sure you want to accept this application?',
  )
  if (!confirmed) return
  processing.value = true
  try {
    await teacherApi.reviewRequest(requestId, 'ACCEPTED')
    toast.success('Application accepted')
    await loadData()
  } catch (e: any) {
    toast.error(e.message || 'Failed to accept')
  } finally {
    processing.value = false
  }
}

function openRejectModal() {
  rejectComment.value = ''
  showRejectModal.value = true
}

async function handleReject() {
  processing.value = true
  try {
    await teacherApi.reviewRequest(requestId, 'REJECTED', rejectComment.value)
    toast.success('Application rejected')
    showRejectModal.value = false
    await loadData()
  } catch (e: any) {
    toast.error(e.message || 'Failed to reject')
  } finally {
    processing.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="detail-page">
    <div class="page-header">
      <button type="button" class="back-link" @click="goBack">
        <i class="bi bi-arrow-left"></i> Back to Requests
      </button>
      <h1>Request Details</h1>
    </div>

    <div
      v-if="loading"
      class="panel-card"
      style="text-align: center; padding: 40px; color: var(--muted)"
    >
      Loading...
    </div>

    <div
      v-else-if="!request"
      class="panel-card"
      style="text-align: center; padding: 40px"
    >
      Request not found.
      <button type="button" class="inline-link" @click="goBack">
        Back to list
      </button>
    </div>

    <template v-else>
      <!-- Student Profile -->
      <div v-if="studentProfile" class="panel-card">
        <h2 class="section-title">
          <i class="bi bi-person"></i> Student Profile
        </h2>
        <div class="meta-grid">
          <div class="meta-item">
            <span class="meta-label">Full Name</span>
            <span class="meta-value">{{ studentProfile.fullName || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Student No</span>
            <span class="meta-value">{{
              studentProfile.studentNo || '-'
            }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Email</span>
            <span class="meta-value">{{ studentProfile.email || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Programme</span>
            <span class="meta-value">{{
              studentProfile.programme || '-'
            }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Phone</span>
            <span class="meta-value">{{ studentProfile.phone || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Interests</span>
            <span class="meta-value">{{
              studentProfile.interests || '-'
            }}</span>
          </div>
        </div>
      </div>

      <!-- Application Detail -->
      <div class="panel-card">
        <h2 class="section-title">
          <i class="bi bi-file-earmark-text"></i> Application Detail
        </h2>
        <div class="meta-grid">
          <div class="meta-item">
            <span class="meta-label">Project</span>
            <span class="meta-value">{{ request.projectTitle || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Application Status</span>
            <span class="meta-value">
              <span
                class="status-pill"
                :style="{
                  color: statusColor(request.requestStatus),
                  background: statusColor(request.requestStatus) + '18',
                }"
              >
                {{ statusText(request.requestStatus) }}
              </span>
            </span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Submitted</span>
            <span class="meta-value">{{
              formatDate(request.submittedAt)
            }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Preference Rank</span>
            <span class="meta-value">{{ request.preferenceRank || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Topic Area</span>
            <span class="meta-value">{{ project?.topicArea || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Required Skills</span>
            <span class="meta-value">{{ project?.requiredSkills || '-' }}</span>
          </div>
        </div>

        <div v-if="request.notes" class="detail-block">
          <h3>Student Notes</h3>
          <div class="detail-text">{{ request.notes }}</div>
        </div>

        <div v-if="request.decisionComment" class="detail-block">
          <h3>Decision Comment</h3>
          <div class="detail-text">{{ request.decisionComment }}</div>
        </div>
      </div>

      <!-- Actions -->
      <div
        v-if="normalizeStatus(request.requestStatus) === 'PENDING'"
        class="panel-card action-panel"
      >
        <div class="action-buttons">
          <button
            class="btn-accept"
            :disabled="processing"
            @click="handleAccept"
          >
            <i class="bi bi-check-lg"></i>
            {{ processing ? 'Processing...' : 'Accept' }}
          </button>
          <button
            class="btn-reject"
            :disabled="processing"
            @click="openRejectModal"
          >
            <i class="bi bi-x-lg"></i> Reject
          </button>
        </div>
      </div>
    </template>

    <!-- Reject Modal -->
    <Teleport to="body">
      <div
        v-if="showRejectModal"
        class="modal-overlay"
        @click.self="showRejectModal = false"
      >
        <div class="modal-dialog">
          <div class="modal-header">
            <h2>Reject Application</h2>
            <button class="icon-button" @click="showRejectModal = false">
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
            <button class="btn-secondary" @click="showRejectModal = false">
              Cancel
            </button>
            <button
              class="btn-reject"
              :disabled="processing"
              @click="handleReject"
            >
              {{ processing ? 'Rejecting...' : 'Confirm Reject' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text);
}

.back-link {
  color: var(--deep);
  text-decoration: none;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 30px;
  border: 0;
  background: rgba(90, 43, 152, 0.1);
  cursor: pointer;
  font-family: inherit;
  transition: background 0.2s;
}

.back-link:hover {
  background: rgba(90, 43, 152, 0.2);
  text-decoration: none;
}

.inline-link {
  border: 0;
  background: transparent;
  color: var(--deep);
  cursor: pointer;
  font: inherit;
  font-weight: 600;
  padding: 0;
}

.panel-card {
  background: #fff;
  border-radius: 28px;
  padding: 24px 28px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  font-size: 1.1rem;
  color: var(--deep);
  margin: 0 0 18px;
  border-left: 4px solid var(--deep);
  padding-left: 14px;
}

.section-title i {
  font-size: 1.2rem;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px 28px;
  background: #f9f5ff;
  border-radius: 20px;
  padding: 18px 20px;
}

.meta-item {
  display: flex;
  flex-direction: column;
}

.meta-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--muted);
  font-weight: 600;
}

.meta-value {
  font-weight: 600;
  color: var(--text);
  margin-top: 2px;
}

.detail-block {
  margin-top: 20px;
}

.detail-block h3 {
  font-weight: 600;
  font-size: 1rem;
  margin-bottom: 10px;
  color: var(--deep);
  border-left: 4px solid var(--deep);
  padding-left: 14px;
}

.detail-text {
  background: #faf9ff;
  padding: 16px 20px;
  border-radius: 18px;
  line-height: 1.7;
  color: var(--text);
  border: 1px solid rgba(90, 43, 152, 0.15);
}

.status-pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
}

.action-panel {
  border: 2px solid rgba(90, 43, 152, 0.12);
}

.action-buttons {
  display: flex;
  gap: 16px;
}

.btn-accept {
  background: var(--green);
  color: white;
  border: none;
  padding: 14px 32px;
  border-radius: 40px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  font-family: inherit;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
}

.btn-accept:hover {
  background: #25a88e;
}

.btn-accept:disabled {
  background: #c4c4e0;
  cursor: not-allowed;
}

.btn-reject {
  background: var(--red);
  color: white;
  border: none;
  padding: 14px 32px;
  border-radius: 40px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  font-family: inherit;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
}

.btn-reject:hover {
  background: #a83a3a;
}

.btn-reject:disabled {
  background: #c4c4e0;
  cursor: not-allowed;
}

.btn-secondary {
  background: transparent;
  border: 1.5px solid var(--muted);
  color: var(--text);
  padding: 12px 24px;
  border-radius: 40px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
}

.btn-secondary:hover {
  border-color: var(--deep);
  color: var(--deep);
}

/* Reject Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(28, 27, 51, 0.45);
  z-index: 20;
}

.modal-dialog {
  width: min(480px, 100%);
  background: #fff;
  border-radius: 24px;
  border: 1px solid rgba(28, 27, 51, 0.08);
  padding: 28px;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
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
  margin-bottom: 20px;
}

.field-label {
  display: block;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text);
  font-size: 0.9rem;
}

.reject-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1.5px solid rgba(90, 43, 152, 0.18);
  border-radius: 16px;
  font-family: inherit;
  resize: vertical;
  outline: none;
  font-size: 0.95rem;
}

.reject-textarea:focus {
  border-color: var(--deep);
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.14);
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>
