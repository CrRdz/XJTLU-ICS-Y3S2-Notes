<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  studentApi,
  getCurrentUser,
  type StudentRequestSummary,
} from '../../utils/api'
import { chartAutoresize } from '../../utils/chart'
import { toast, confirm } from '../../utils/ui-feedback'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CanvasRenderer,
])

const router = useRouter()

const loading = ref(true)
const requestSummary = ref<StudentRequestSummary | null>(null)
const projects = ref<any[]>([])

const user = getCurrentUser()
const displayName = computed(() => user.fullName || user.username || 'Student')

const pending = computed(() => Number(requestSummary.value?.pendingCount || 0))
const accepted = computed(() =>
  Number(requestSummary.value?.acceptedCount || 0),
)
const rejected = computed(() =>
  Number(requestSummary.value?.rejectedCount || 0),
)
const withdrawn = computed(() =>
  Number(requestSummary.value?.withdrawnCount || 0),
)
const activeCount = computed(() => pending.value + accepted.value)
const totalRequests = computed(() =>
  Number(requestSummary.value?.totalRequests || 0),
)

const statusEntries = computed(() => {
  const entries = [
    { label: 'Pending', value: pending.value, className: 'tone-pending' },
    { label: 'Accepted', value: accepted.value, className: 'tone-accepted' },
    { label: 'Rejected', value: rejected.value, className: 'tone-rejected' },
    { label: 'Withdrawn', value: withdrawn.value, className: 'tone-withdrawn' },
  ].filter((item) => item.value > 0)

  return entries.length
    ? entries
    : [{ label: 'Requests', value: 0, className: 'tone-empty' }]
})

const withdrawnProjectIds = computed(() => {
  return new Set(
    (requestSummary.value?.withdrawnProjectIds || []).map((projectId) =>
      String(projectId),
    ),
  )
})

const recommendedProjects = computed(() => {
  return projects.value
    .filter((p) => normalizeStatus(p.projectStatus) === 'AVAILABLE')
    .filter((p) => !withdrawnProjectIds.value.has(String(p.projectId)))
    .slice(0, 3)
})

const recentRequests = computed(() => {
  const requests = Array.isArray(requestSummary.value?.recentRequests)
    ? requestSummary.value.recentRequests
    : []
  return [...requests]
    .sort((a, b) => {
      const rankDiff =
        Number(a.preferenceRank || 999) - Number(b.preferenceRank || 999)
      if (rankDiff !== 0) return rankDiff
      return (
        new Date(b.submittedAt || 0).getTime() -
        new Date(a.submittedAt || 0).getTime()
      )
    })
    .slice(0, 3)
})

// --- Chart data ---

const statusChartOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, textStyle: { color: '#666' } },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: [
        {
          value: pending.value,
          name: 'Pending',
          itemStyle: { color: '#f6a63d' },
        },
        {
          value: accepted.value,
          name: 'Accepted',
          itemStyle: { color: '#2fc5a8' },
        },
        {
          value: rejected.value,
          name: 'Rejected',
          itemStyle: { color: '#c54545' },
        },
        {
          value: withdrawn.value,
          name: 'Withdrawn',
          itemStyle: { color: '#9c9cb2' },
        },
      ].filter((d) => d.value > 0),
    },
  ],
}))

const categoryChartOption = computed(() => {
  const counts: Record<string, number> = {}
  projects.value.forEach((p) => {
    const cat = p.categoryName || 'Uncategorized'
    counts[cat] = (counts[cat] || 0) + 1
  })
  const names = Object.keys(counts)
  const values = Object.values(counts)
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { rotate: names.length > 4 ? 30 : 0, color: '#666' },
    },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#666' } },
    series: [
      {
        type: 'bar',
        data: values,
        barMaxWidth: 40,
        itemStyle: {
          color: '#24b3ff',
          borderRadius: [6, 6, 0, 0],
        },
      },
    ],
  }
})

const tagChartOption = computed(() => {
  const statusCounts: Record<string, number> = {}
  projects.value.forEach((p) => {
    const s = normalizeStatus(p.projectStatus)
    statusCounts[s] = (statusCounts[s] || 0) + 1
  })
  const statusColorMap: Record<string, string> = {
    AVAILABLE: '#2fc5a8',
    REQUESTED: '#f6a63d',
    AGREED: '#24b3ff',
    CLOSED: '#9c9cb2',
    ARCHIVED: '#9c9cb2',
  }
  const names = Object.keys(statusCounts)
  const values = names.map((n) => statusCounts[n])
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: names, axisLabel: { color: '#666' } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#666' } },
    series: [
      {
        type: 'bar',
        data: values.map((v, i) => ({
          value: v,
          itemStyle: { color: statusColorMap[names[i]] || '#7c5cfc' },
        })),
        barMaxWidth: 40,
        itemStyle: { borderRadius: [6, 6, 0, 0] },
      },
    ],
  }
})

function normalizeStatus(status: string | null | undefined): string {
  return String(status || 'UNKNOWN').toUpperCase()
}

function statusClass(status: string): string {
  const s = normalizeStatus(status)
  if (s === 'AVAILABLE') return 'status-available'
  if (s === 'PENDING' || s === 'REQUESTED') return 'status-requested'
  if (s === 'ACCEPTED' || s === 'AGREED') return 'status-agreed'
  if (s === 'REJECTED') return 'status-rejected'
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

function formatDate(value: string | null | undefined): string {
  if (!value) return '-'
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value).slice(0, 10)
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

async function withdrawRequest(requestId: number | string) {
  const confirmed = await confirm(
    'Are you sure you want to withdraw this application?',
  )
  if (!confirmed) return
  try {
    await studentApi.withdrawRequest(requestId)
    toast.success('Application withdrawn')
    await loadData()
  } catch (e: any) {
    toast.error(e.message || 'Failed to withdraw')
  }
}

async function loadData() {
  loading.value = true
  try {
    const [summaryRes, projData] = await Promise.all([
      studentApi.getRequestSummary(),
      studentApi.getProjects(1, 50),
    ])
    requestSummary.value = summaryRes.data || null
    projects.value = Array.isArray(projData.data?.records)
      ? projData.data.records
      : []
  } catch (e: any) {
    toast.error(e.message || 'Failed to load dashboard data')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="dashboard">
    <!-- Hero -->
    <section class="hero">
      <div>
        <h1>Welcome, {{ displayName }}</h1>
        <p class="hero-info">
          This is your student workspace. You can view current applications,
          search for projects published by supervisors, or check past request
          records.
        </p>
        <span class="badge"
          >Active applications: {{ loading ? 'Loading...' : activeCount }}</span
        >
      </div>
      <button class="cta" @click="router.push('/student/projects')">
        Quick Apply
        <small>Prioritize projects matching your skills</small>
      </button>
    </section>

    <!-- Stats -->
    <section class="stats">
      <div class="panel">
        <h3>Application Status</h3>
        <div v-if="loading" class="status-summary">Loading...</div>
        <div v-else class="status-list">
          <span
            v-for="item in statusEntries"
            :key="item.label"
            class="status-chip"
            :class="item.className"
          >
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
          </span>
        </div>
      </div>
      <div class="panel">
        <h3>Accepted Applications</h3>
        <strong>{{ accepted }}</strong>
      </div>
      <div class="panel">
        <h3>Total Applications</h3>
        <strong>{{ totalRequests }}</strong>
      </div>
    </section>

    <!-- Charts -->
    <section class="charts">
      <h2>Data Overview</h2>
      <div class="chart-grid">
        <div class="chart-panel">
          <h3>Application Status</h3>
          <div v-if="loading" class="chart-placeholder">Loading...</div>
          <VChart
            v-else
            class="chart"
            :option="statusChartOption"
            :autoresize="chartAutoresize"
          />
        </div>
        <div class="chart-panel">
          <h3>Projects by Category</h3>
          <div v-if="loading" class="chart-placeholder">Loading...</div>
          <VChart
            v-else
            class="chart"
            :option="categoryChartOption"
            :autoresize="chartAutoresize"
          />
        </div>
        <div class="chart-panel">
          <h3>Project Status</h3>
          <div v-if="loading" class="chart-placeholder">Loading...</div>
          <VChart
            v-else
            class="chart"
            :option="tagChartOption"
            :autoresize="chartAutoresize"
          />
        </div>
      </div>
    </section>

    <!-- Recommended Projects -->
    <section>
      <h2>Recommended Projects</h2>
      <div class="grid">
        <template v-if="recommendedProjects.length > 0">
          <article
            v-for="project in recommendedProjects"
            :key="project.projectId"
            class="card"
          >
            <span
              class="status-pill"
              :class="statusClass(project.projectStatus)"
            >
              Project: {{ projectStatusText(project.projectStatus) }}
            </span>
            <h4>{{ project.title || 'Untitled Project' }}</h4>
            <p class="meta">
              Category: {{ project.categoryName || '-' }} | Quota:
              {{ project.currentAgreedCount || 0 }}/{{
                project.maxStudents || 0
              }}
            </p>
            <p class="meta">
              Supervisor: {{ project.teacherName || '-' }} | Area:
              {{ project.topicArea || '-' }}
            </p>
            <button
              @click="router.push(`/student/projects/${project.projectId}`)"
            >
              View Details
            </button>
          </article>
        </template>
        <article v-else class="card">
          <h4>No recommended projects</h4>
          <p class="meta">No projects available at the moment</p>
          <button class="secondary" @click="router.push('/student/projects')">
            Browse Projects
          </button>
        </article>
      </div>
    </section>

    <!-- Recent Requests -->
    <section class="timeline">
      <h3>My Applications</h3>
      <template v-if="recentRequests.length > 0">
        <div v-for="item in recentRequests" :key="item.requestId" class="item">
          <div style="display: flex; flex-direction: column">
            <div class="application-title-row">
              <strong>{{ item.projectTitle || 'Untitled Project' }}</strong>
              <span class="status" :class="statusClass(item.requestStatus)">
                Application: {{ normalizeStatus(item.requestStatus) }}
              </span>
            </div>
            <div class="meta-row">
              <span>Submitted: {{ formatDate(item.submittedAt) }}</span>
              <span
                >Updated:
                {{
                  formatDate(
                    item.reviewedAt || item.withdrawnAt || item.submittedAt,
                  )
                }}</span
              >
              <span>Rank: {{ item.preferenceRank || '-' }}</span>
            </div>
            <p class="meta" style="margin-top: 8px">
              Notes: {{ item.notes || '-' }}
            </p>
            <p v-if="item.decisionComment" class="meta" style="margin-top: 4px">
              Feedback: {{ item.decisionComment }}
            </p>
          </div>
          <div class="application-actions">
            <button
              v-if="normalizeStatus(item.requestStatus) === 'PENDING'"
              class="withdraw-btn"
              @click="withdrawRequest(item.requestId)"
            >
              Withdraw
            </button>
          </div>
        </div>
      </template>
      <div v-else class="item">
        <div style="display: flex; flex-direction: column">
          <strong style="margin-bottom: 6px">No applications yet</strong>
          <p class="meta" style="margin-top: 8px">
            Your applications will appear here once submitted
          </p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.hero,
.panel,
.card,
.timeline,
.chart-panel {
  background: var(--panel, #fff);
  border-radius: 28px;
  padding: 24px 28px;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 32px;
}

.hero h1 {
  margin: 0;
  font-size: 1.9rem;
}
.hero-info {
  max-width: 520px;
  color: var(--muted);
  line-height: 1.6;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 0.85rem;
  background: rgba(36, 179, 255, 0.15);
  color: var(--accent);
}

.cta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: none;
  border-radius: 18px;
  padding: 14px 24px;
  background: linear-gradient(135deg, var(--accent), #0c76e3);
  color: white;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 12px 30px rgba(36, 179, 255, 0.4);
  font-family: inherit;
}

.cta small {
  font-weight: 400;
  opacity: 0.85;
  font-size: 0.8rem;
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 16px;
}

.stats .panel {
  border: 1px solid rgba(90, 43, 152, 0.18);
  border-radius: 18px;
  padding: 20px 24px;
  box-shadow: 0 12px 30px rgba(90, 43, 152, 0.06);
}

.panel h3 {
  margin: 0 0 12px;
  font-size: 0.95rem;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.panel strong {
  font-size: 2rem;
  display: block;
}

.status-summary {
  color: var(--text);
  font-size: 2rem;
  font-weight: 700;
}

.status-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 10px;
}

.status-chip {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 10px;
  border: 1px solid rgba(28, 27, 51, 0.1);
  border-radius: 12px;
  padding: 12px 14px;
  background: #fff;
}

.status-chip::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--muted);
}

.status-chip strong {
  font-size: 1.15rem;
  text-align: right;
  color: var(--text);
}

.status-chip span {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--text);
}

.status-chip.tone-pending::before {
  background: var(--orange);
}
.status-chip.tone-accepted::before {
  background: var(--green);
}
.status-chip.tone-rejected::before {
  background: var(--red);
}
.status-chip.tone-withdrawn::before,
.status-chip.tone-empty::before {
  background: var(--muted);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
  margin-top: 16px;
}

.card {
  border: 1px solid rgba(90, 43, 152, 0.16);
  border-radius: 18px;
  padding: 22px;
  min-height: 210px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow: 0 12px 30px rgba(90, 43, 152, 0.05);
}

.card h4 {
  margin: 0;
  font-size: 1.2rem;
}
.card .meta {
  font-size: 0.85rem;
  color: var(--muted);
  line-height: 1.5;
}

.status-pill {
  align-self: flex-start;
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

.card button {
  margin-top: auto;
  border: none;
  border-radius: 12px;
  padding: 12px 18px;
  background: var(--deep);
  color: white;
  cursor: pointer;
  font-weight: 600;
  font-family: inherit;
  transition: transform 0.2s ease;
}

.card button.secondary {
  background: #e8e8f8;
  color: #3c2e7b;
}

.card button:active {
  transform: translateY(1px);
}

h2 {
  margin: 0 0 8px;
  font-size: 1.4rem;
  color: var(--text);
}
h3 {
  margin: 0 0 16px;
  font-size: 1.1rem;
  color: var(--text);
}

.timeline .item {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid rgba(156, 156, 178, 0.25);
}

.timeline .item:last-child {
  border-bottom: none;
}

.timeline .meta-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 0.85rem;
  color: var(--muted);
}

.application-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.timeline .status {
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.85rem;
}

.timeline .meta {
  font-size: 0.85rem;
  color: var(--muted);
}

.application-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 10px;
}

.withdraw-btn {
  padding: 8px 14px;
  border-radius: 999px;
  border: 1.5px solid #f56c6c;
  color: #f56c6c;
  background: transparent;
  cursor: pointer;
  font-weight: 600;
  font-family: inherit;
  font-size: 0.85rem;
}

.withdraw-btn:hover {
  background: #f56c6c;
  color: white;
}

.charts h2 {
  margin: 0 0 16px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.chart-panel {
  border: 1px solid rgba(90, 43, 152, 0.16);
  box-shadow: 0 12px 30px rgba(90, 43, 152, 0.05);
  min-height: 320px;
  display: flex;
  flex-direction: column;
}

.chart-panel h3 {
  margin: 0 0 12px;
  font-size: 1rem;
  color: var(--muted);
}

.chart {
  flex: 1;
  min-height: 260px;
}

.chart-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--muted);
  font-size: 0.95rem;
}

@media (max-width: 960px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 640px) {
  .timeline .item {
    grid-template-columns: 1fr;
  }

  .application-actions {
    align-items: flex-start;
  }
}
</style>
