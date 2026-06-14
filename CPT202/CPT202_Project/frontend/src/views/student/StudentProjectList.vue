<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import AppPagination from '../../components/AppPagination.vue'
import { useResponsivePageResult } from '../../composables/useResponsivePageResult'
import { normalizePageResult, studentApi } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

const tags = ref<any[]>([])
const categories = ref<any[]>([])
const selectedTagIds = ref<Set<string>>(new Set())

const keyword = ref('')
const categoryId = ref('')
const statusFilter = ref('')

let keywordTimer: ReturnType<typeof setTimeout> | null = null

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
    const res = await studentApi.getProjects(pageNum, pageSize, {
      keyword: keyword.value.trim(),
      categoryId: categoryId.value,
      status: statusFilter.value,
      tagIds: Array.from(selectedTagIds.value),
    })
    return normalizePageResult(res.data, { pageNum, pageSize })
  },
  onLoadError: (error) => {
    const message =
      error instanceof Error ? error.message : 'Failed to load projects'
    toast.error(message)
  },
})

void tableWrapperRef

// Helpers
function normalizeStatus(status: string | null | undefined): string {
  return String(status || 'UNKNOWN').toUpperCase()
}

function statusClass(status: string): string {
  const s = normalizeStatus(status)
  if (s === 'AVAILABLE') return 'status-available'
  if (s === 'REQUESTED') return 'status-requested'
  if (s === 'AGREED') return 'status-agreed'
  return 'status-unavailable'
}

function projectStatusText(status: string | null | undefined): string {
  const map: Record<string, string> = {
    AVAILABLE: 'Available',
    REQUESTED: 'Requested',
    AGREED: 'Agreed',
    CLOSED: 'Closed',
    ARCHIVED: 'Closed',
  }
  return map[normalizeStatus(status)] || normalizeStatus(status)
}

function onKeywordInput() {
  if (keywordTimer) clearTimeout(keywordTimer)
  keywordTimer = setTimeout(() => {
    void loadProjects(1)
  }, 250)
}

function toggleTag(tagId: string) {
  if (selectedTagIds.value.has(tagId)) {
    selectedTagIds.value.delete(tagId)
  } else {
    selectedTagIds.value.add(tagId)
  }
  // Force reactivity
  selectedTagIds.value = new Set(selectedTagIds.value)
  void loadProjects(1)
}

function clearTagFilters() {
  selectedTagIds.value = new Set()
  void loadProjects(1)
}

function clearAllFilters() {
  keyword.value = ''
  categoryId.value = ''
  statusFilter.value = ''
  selectedTagIds.value = new Set()
  void loadProjects(1)
}

async function loadTags() {
  try {
    const res = await studentApi.listProjectTags()
    tags.value = Array.isArray(res.data) ? res.data : []
  } catch {
    tags.value = []
  }
}

async function loadCategories() {
  try {
    const res = await studentApi.listProjectCategories()
    categories.value = Array.isArray(res.data) ? res.data : []
  } catch {
    categories.value = []
  }
}

async function init() {
  await Promise.all([loadTags(), loadCategories()])
  await initialize()
}

onMounted(init)

onBeforeUnmount(() => {
  if (keywordTimer) clearTimeout(keywordTimer)
})
</script>

<template>
  <div class="project-list-page">
    <div class="page-header">
      <h1>Available Projects</h1>
    </div>

    <div class="panel-card">
      <!-- Filters -->
      <div class="filters-row">
        <span class="filter-control search-control">
          <i class="bi bi-search"></i>
          <input
            v-model="keyword"
            type="search"
            placeholder="Project name, area, or skill"
            @input="onKeywordInput"
          />
        </span>
        <span class="filter-control select-control">
          <select
            v-model="categoryId"
            aria-label="Category"
            @change="loadProjects(1)"
          >
            <option value="">All Categories</option>
            <option
              v-for="cat in categories"
              :key="cat.categoryId"
              :value="cat.categoryId"
            >
              {{ cat.categoryName }}
            </option>
          </select>
        </span>
        <span class="filter-control select-control">
          <select
            v-model="statusFilter"
            aria-label="Project Status"
            @change="loadProjects(1)"
          >
            <option value="">All Project Status</option>
            <option value="AVAILABLE">Available</option>
            <option value="REQUESTED">Requested</option>
            <option value="AGREED">Agreed</option>
            <option value="CLOSED">Closed</option>
          </select>
        </span>
        <button class="clear-btn" @click="clearAllFilters">
          <i class="bi bi-arrow-counterclockwise"></i>
          Clear
        </button>
      </div>

      <!-- Tag Filters -->
      <div class="tag-filter-bar">
        <span class="tag-label">Tags</span>
        <template v-if="tags.length > 0">
          <button
            v-for="tag in tags"
            :key="tag.tagId"
            class="tag-btn"
            :class="{ active: selectedTagIds.has(String(tag.tagId)) }"
            @click="toggleTag(String(tag.tagId))"
          >
            {{ tag.tagName }}
          </button>
          <button
            class="tag-btn tag-clear"
            :disabled="selectedTagIds.size === 0"
            @click="clearTagFilters"
          >
            Clear tags
          </button>
        </template>
        <span v-else class="tag-label">No tags available</span>
      </div>

      <!-- Table -->
      <div ref="tableWrapperRef" class="table-wrapper">
        <table class="project-table">
          <thead>
            <tr>
              <th>Project Code</th>
              <th>Project Title</th>
              <th>Supervisor</th>
              <th>Category</th>
              <th>Topic Area</th>
              <th>Quota</th>
              <th>Project Status</th>
              <th>Details</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="8" style="text-align: center; color: #888">
                Loading...
              </td>
            </tr>
            <tr v-else-if="projects.length === 0">
              <td colspan="8" style="text-align: center; color: #888">
                No projects available
              </td>
            </tr>
            <tr v-for="project in projects" :key="project.projectId">
              <td>
                <strong>{{ project.projectId }}</strong>
              </td>
              <td>{{ project.title }}</td>
              <td>{{ project.teacherName || '-' }}</td>
              <td>{{ project.categoryName || '-' }}</td>
              <td>{{ project.topicArea || '-' }}</td>
              <td>
                {{ project.currentAgreedCount || 0 }}/{{
                  project.maxStudents || 0
                }}
              </td>
              <td>
                <span
                  class="status-pill"
                  :class="statusClass(project.projectStatus)"
                >
                  {{ projectStatusText(project.projectStatus) }}
                </span>
              </td>
              <td>
                <router-link
                  class="view-btn"
                  :to="`/student/projects/${project.projectId}`"
                >
                  View
                </router-link>
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
  </div>
</template>

<style scoped>
.project-list-page {
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

.panel-card {
  background: #fff;
  border-radius: 28px;
  padding: 24px 28px;
}

.filters-row {
  display: grid;
  grid-template-columns:
    minmax(260px, 1.6fr) minmax(190px, 1fr) minmax(170px, 0.9fr)
    auto;
  gap: 12px;
  align-items: end;
  margin-bottom: 12px;
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

.search-control {
  gap: 8px;
  padding: 0 12px;
}

.search-control i {
  color: var(--muted);
  font-size: 0.95rem;
}

.filter-control input,
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

.tag-filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 18px;
}

.tag-label {
  color: var(--muted);
  font-size: 0.82rem;
  font-weight: 700;
  margin-right: 2px;
}

.tag-btn {
  padding: 7px 14px;
  border-radius: 999px;
  border: 1.5px solid rgba(90, 43, 152, 0.16);
  background: #fff;
  color: var(--deep);
  font-size: 0.88rem;
  cursor: pointer;
  font-family: inherit;
  font-weight: 700;
  line-height: 1;
}

.tag-btn:hover:not(:disabled),
.tag-btn.active {
  background: var(--deep);
  color: #fff;
  border-color: var(--deep);
}

.tag-clear {
  color: var(--muted);
}

.tag-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.table-wrapper {
  overflow-x: auto;
  border-radius: 18px;
}

.project-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
  font-size: 0.9rem;
}

.project-table th {
  text-align: left;
  padding: 16px;
  background: #f8f5ff;
  font-weight: 600;
  color: var(--deep);
  border-bottom: 2px solid rgba(90, 43, 152, 0.2);
}

.project-table td {
  padding: 16px;
  border-bottom: 1px solid rgba(156, 156, 178, 0.2);
}

.project-table tbody tr:hover {
  background: rgba(90, 43, 152, 0.03);
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
.status-rejected {
  background: rgba(199, 69, 69, 0.12);
  color: var(--red);
}
.status-unavailable {
  background: rgba(156, 156, 178, 0.2);
  color: rgba(28, 27, 51, 0.65);
}

.view-btn {
  background: #fff;
  border: 1.5px solid var(--deep);
  color: var(--deep);
  font-weight: 600;
  padding: 8px 18px;
  border-radius: 40px;
  font-size: 0.8rem;
  cursor: pointer;
  text-decoration: none;
  display: inline-block;
  transition: all 0.2s;
}

.view-btn:hover {
  background: var(--deep);
  color: #fff;
}

@media (max-width: 960px) {
  .filters-row {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
