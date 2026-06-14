<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { teacherApi } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'
import { useSmartBack } from '../../utils/navigation'
import { toIsoLocalDateTime } from '../../utils/date'

const router = useRouter()
const { goBack } = useSmartBack('/teacher/projects', '/teacher')

const title = ref('')
const quota = ref(3)
const categoryId = ref('')
const description = ref('')
const requirements = ref('')
const topicArea = ref('')
const applicationDeadline = ref('')
const categories = ref<any[]>([])
const tags = ref<any[]>([])
const selectedTagIds = ref<Set<number>>(new Set())
const publishing = ref(false)
const formError = ref('')
const formErrorType = ref<'success' | 'error' | ''>('')

function setError(msg: string, type: 'success' | 'error' | '') {
  formError.value = msg
  formErrorType.value = type
}

function toggleTag(tagId: number) {
  const s = new Set(selectedTagIds.value)
  if (s.has(tagId)) s.delete(tagId)
  else s.add(tagId)
  selectedTagIds.value = s
}

async function loadCategories() {
  try {
    const res = await teacherApi.listCategories()
    categories.value = Array.isArray(res.data) ? res.data : []
  } catch {
    categories.value = []
  }
}

async function loadTags() {
  try {
    const res = await teacherApi.listTags()
    tags.value = Array.isArray(res.data) ? res.data : []
  } catch {
    tags.value = []
  }
}

async function handleSubmit() {
  if (!categoryId.value) {
    setError('Please select a category.', 'error')
    return
  }
  if (!title.value.trim()) {
    setError('Title is required.', 'error')
    return
  }
  if (!description.value.trim()) {
    setError('Description is required.', 'error')
    return
  }
  if (quota.value < 1) {
    setError('Quota must be at least 1.', 'error')
    return
  }
  if (!applicationDeadline.value) {
    setError('Application deadline is required.', 'error')
    return
  }

  publishing.value = true
  setError('Publishing...', '')

  try {
    const res = await teacherApi.createProject({
      categoryId: Number(categoryId.value),
      title: title.value.trim(),
      description: description.value.trim(),
      requiredSkills: requirements.value.trim(),
      topicArea: topicArea.value.trim(),
      maxStudents: quota.value,
      closeDate: toIsoLocalDateTime(applicationDeadline.value),
    })

    const project = res.data
    const tagIds = [...selectedTagIds.value]
    if (project?.projectId && tagIds.length > 0) {
      await teacherApi.bindProjectTags(project.projectId, tagIds)
    }

    toast.success('Project published')
    router.push('/teacher/projects')
  } catch (e: any) {
    setError(e.message || 'Failed to publish project', 'error')
  } finally {
    publishing.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadTags()
})
</script>

<template>
  <div class="create-page">
    <div class="page-header">
      <button type="button" class="back-link" @click="goBack">
        <i class="bi bi-arrow-left"></i> Back to My Projects
      </button>
      <h1>Publish New Project</h1>
    </div>

    <div class="panel">
      <form class="create-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>Project Title <span class="required">*</span></label>
          <input
            v-model="title"
            type="text"
            class="form-control"
            placeholder="e.g. Student Project Allocation System"
          />
        </div>

        <div class="form-grid-2">
          <div class="form-group">
            <label>Quota <span class="required">*</span></label>
            <input
              v-model.number="quota"
              type="number"
              min="1"
              step="1"
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label>Category <span class="required">*</span></label>
            <select v-model="categoryId" class="form-control">
              <option value="">Select category</option>
              <option
                v-for="cat in categories"
                :key="cat.categoryId"
                :value="String(cat.categoryId)"
              >
                {{ cat.categoryName }}
              </option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label>Application Deadline <span class="required">*</span></label>
          <input
            v-model="applicationDeadline"
            type="datetime-local"
            class="form-control"
          />
        </div>

        <div class="form-group">
          <label>Description <span class="required">*</span></label>
          <textarea
            v-model="description"
            rows="4"
            class="form-control"
            placeholder="Describe the project purpose, main modules, and expected outcomes..."
          ></textarea>
        </div>

        <div class="form-group">
          <label>Requirements</label>
          <textarea
            v-model="requirements"
            rows="3"
            class="form-control"
            placeholder="e.g. Basic Java knowledge, familiarity with Spring Boot and relational databases..."
          ></textarea>
        </div>

        <div class="form-group">
          <label>Topic Area</label>
          <input
            v-model="topicArea"
            type="text"
            class="form-control"
            placeholder="e.g. AI in Education / Data Visualization"
          />
        </div>

        <div class="form-group">
          <label>Tags</label>
          <div class="tags-container">
            <label v-for="tag in tags" :key="tag.tagId" class="tag-checkbox">
              <input
                type="checkbox"
                :checked="selectedTagIds.has(tag.tagId)"
                @change="toggleTag(tag.tagId)"
              />
              <span>{{ tag.tagName }}</span>
            </label>
            <span
              v-if="tags.length === 0"
              style="color: var(--muted); font-size: 0.9rem"
              >No tags available</span
            >
          </div>
        </div>

        <hr class="divider" />

        <div class="form-status" :class="formErrorType">{{ formError }}</div>

        <div class="form-actions">
          <button type="submit" class="btn-primary" :disabled="publishing">
            <i class="bi bi-rocket-takeoff"></i>
            {{ publishing ? 'Publishing...' : 'Publish' }}
          </button>
          <button type="button" class="btn-secondary" @click="goBack">
            Cancel
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.create-page {
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

.panel {
  background: #fff;
  border-radius: 28px;
  padding: 24px 28px;
  max-width: 720px;
}

.create-form {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.form-group label {
  font-weight: 600;
  color: var(--text);
  font-size: 0.9rem;
}

.required {
  color: var(--red);
}

.form-control {
  padding: 10px 14px;
  border: 1.5px solid rgba(90, 43, 152, 0.18);
  border-radius: 12px;
  font-size: 0.95rem;
  font-family: inherit;
  outline: none;
  background: #fff;
  width: 100%;
}

.form-control:focus {
  border-color: var(--deep);
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.1);
}

textarea.form-control {
  resize: vertical;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-checkbox {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 12px;
  border: 1px solid rgba(90, 43, 152, 0.18);
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s;
}

.tag-checkbox:has(input:checked) {
  background: var(--deep);
  color: #fff;
  border-color: var(--deep);
}

.tag-checkbox input {
  display: none;
}

.divider {
  border: none;
  border-top: 1px solid rgba(156, 156, 178, 0.25);
  margin: 20px 0;
}

.form-status {
  font-size: 0.9rem;
  min-height: 22px;
  margin-bottom: 12px;
  color: var(--muted);
}

.form-status.success {
  color: #167d68;
}
.form-status.error {
  color: #b02a37;
}

.form-actions {
  display: flex;
  gap: 16px;
}

.btn-primary {
  background: var(--deep);
  border: none;
  color: white;
  padding: 12px 28px;
  border-radius: 40px;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  font-family: inherit;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-primary:hover {
  background: var(--deep);
}
.btn-primary:disabled {
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
  text-decoration: none;
  display: inline-flex;
  align-items: center;
}

.btn-secondary:hover {
  border-color: var(--deep);
  color: var(--deep);
}

@media (max-width: 768px) {
  .form-grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>
