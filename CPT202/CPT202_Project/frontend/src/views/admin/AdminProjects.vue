<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppPagination from '../../components/AppPagination.vue'
import { useResponsivePageResult } from '../../composables/useResponsivePageResult'
import { adminApi } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

const showDetail = ref(false)
const selectedProject = ref<any>(null)

const {
  tableWrapperRef,
  loading,
  records: projects,
  currentPage,
  pageSize,
  total,
  totalPages,
  visiblePages,
  initialize,
  loadPage: loadProjects,
} = useResponsivePageResult<any>({
  loadPage: async ({ pageNum, pageSize }) => {
    const res = await adminApi.listProjectsPage({ pageNum, pageSize })
    return res.data
  },
  onLoadError: (error) => {
    const message =
      error instanceof Error ? error.message : 'Failed to load projects'
    toast.error(message)
  },
})

void tableWrapperRef

function normalizeStatus(status: string | null | undefined): string {
  const s = String(status || 'UNKNOWN').toUpperCase()
  return s === 'ARCHIVED' ? 'CLOSED' : s
}

function statusClass(status: string): string {
  const s = normalizeStatus(status)
  if (s === 'AVAILABLE') return 'status-available'
  if (s === 'REQUESTED') return 'status-requested'
  if (s === 'AGREED') return 'status-agreed'
  if (s === 'CLOSED') return 'status-unavailable'
  return 'status-unavailable'
}

function formatDate(value: string | null | undefined): string {
  if (!value) return '-'
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value).split('T')[0]
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function openDetail(project: any) {
  selectedProject.value = project
  showDetail.value = true
}

onMounted(() => {
  void initialize()
})
</script>

<template>
  <div class="page">
    <header class="page-header">
      <h1>Project Overview</h1>
      <span class="badge">{{ total }}</span>
    </header>

    <div class="panel">
      <div ref="tableWrapperRef" class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Project Name</th>
              <th>Teacher</th>
              <th>Category</th>
              <th>Tags</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" style="text-align: center; color: #888">
                Loading...
              </td>
            </tr>
            <tr v-else-if="projects.length === 0">
              <td colspan="6" style="text-align: center; color: #888">
                No projects found
              </td>
            </tr>
            <tr v-for="p in projects" :key="p.projectId">
              <td>
                <strong>{{ p.projectId }}</strong>
              </td>
              <td>{{ p.title || '-' }}</td>
              <td>{{ p.teacherName || '-' }}</td>
              <td>{{ p.categoryName || '-' }}</td>
              <td>
                <span v-if="p.topicArea" class="tag-pill">{{
                  p.topicArea
                }}</span>
                <span v-else style="color: var(--muted)">-</span>
              </td>
              <td>
                <button class="btn-sm btn-detail" @click="openDetail(p)">
                  <i class="bi bi-eye"></i> Detail
                </button>
              </td>
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
        item-label="projects"
        @change="loadProjects"
      />
    </div>

    <!-- Detail Modal -->
    <Teleport to="body">
      <div
        v-if="showDetail"
        class="modal-overlay"
        @click.self="showDetail = false"
      >
        <div class="modal-dialog">
          <div class="modal-header">
            <h2>Project Details</h2>
            <button class="icon-button" @click="showDetail = false">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <div v-if="selectedProject" class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">Project ID</span>
              <span class="detail-value">{{ selectedProject.projectId }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Teacher</span>
              <span class="detail-value">{{
                selectedProject.teacherName || '-'
              }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Category</span>
              <span class="detail-value">{{
                selectedProject.categoryName || '-'
              }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Topic Area</span>
              <span class="detail-value">{{
                selectedProject.topicArea || '-'
              }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Required Skills</span>
              <span class="detail-value">{{
                selectedProject.requiredSkills || '-'
              }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Quota</span>
              <span class="detail-value"
                >{{ selectedProject.currentAgreedCount || 0 }}/{{
                  selectedProject.maxStudents || 0
                }}</span
              >
            </div>
            <div class="detail-item">
              <span class="detail-label">Project Status</span>
              <span class="detail-value">
                <span
                  class="status-pill"
                  :class="statusClass(selectedProject.projectStatus)"
                >
                  {{ normalizeStatus(selectedProject.projectStatus) }}
                </span>
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Publish Date</span>
              <span class="detail-value">{{
                formatDate(selectedProject.publishDate)
              }}</span>
            </div>
          </div>
          <div v-if="selectedProject?.description" class="detail-block">
            <h3>Description</h3>
            <div class="detail-text">{{ selectedProject.description }}</div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text);
}
.badge {
  background: var(--deep);
  color: #fff;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 600;
}
.table-wrapper {
  overflow-x: auto;
  border-radius: 18px;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
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
.tag-pill {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 0.8rem;
  background: rgba(90, 43, 152, 0.08);
  color: var(--deep);
}
.status-pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
}
.status-available {
  background: rgba(47, 197, 168, 0.12);
  color: var(--green);
}
.status-requested {
  background: rgba(246, 166, 61, 0.12);
  color: var(--orange);
}
.status-agreed {
  background: rgba(36, 179, 255, 0.15);
  color: var(--accent);
}
.status-unavailable {
  background: rgba(156, 156, 178, 0.2);
  color: rgba(28, 27, 51, 0.65);
}
.btn-sm {
  padding: 6px 14px;
  border-radius: 20px;
  border: 1.5px solid var(--deep);
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  background: transparent;
  color: var(--deep);
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}
.btn-sm:hover {
  background: var(--deep);
  color: #fff;
}
.summary {
  margin-top: 16px;
  font-size: 0.9rem;
  color: var(--muted);
}

/* Modal */
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
  width: min(720px, 100%);
  max-height: 90vh;
  overflow-y: auto;
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
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  background: #f9f5ff;
  border-radius: 20px;
  padding: 18px 20px;
}
.detail-item {
  display: flex;
  flex-direction: column;
}
.detail-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--muted);
  font-weight: 600;
}
.detail-value {
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
@media (max-width: 640px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
