<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppPagination from '../../components/AppPagination.vue'
import { useResponsivePageResult } from '../../composables/useResponsivePageResult'
import { adminApi } from '../../utils/api'
import { toast, confirm } from '../../utils/ui-feedback'

const formName = ref('')
const formDescription = ref('')
const editingId = ref<number | null>(null)
const formStatus = ref('')
const formStatusType = ref<'success' | 'error' | ''>('')
const saving = ref(false)
const showFormModal = ref(false)

const {
  tableWrapperRef,
  loading,
  records: tags,
  currentPage,
  pageSize,
  total,
  totalPages,
  visiblePages,
  initialize,
  loadPage: loadTags,
} = useResponsivePageResult<any>({
  loadPage: async ({ pageNum, pageSize }) => {
    const res = await adminApi.listTagsPage({ pageNum, pageSize })
    return res.data
  },
  onLoadError: (error) => {
    const message =
      error instanceof Error ? error.message : 'Failed to load tags'
    toast.error(message)
  },
})

void tableWrapperRef

function setFormStatus(msg: string, type: 'success' | 'error' | '') {
  formStatus.value = msg
  formStatusType.value = type
}

function resetForm() {
  formName.value = ''
  formDescription.value = ''
  editingId.value = null
  setFormStatus('', '')
}

function openCreateModal() {
  resetForm()
  showFormModal.value = true
}

function editTag(tag: any) {
  editingId.value = tag.tagId
  formName.value = tag.tagName || ''
  formDescription.value = tag.description || ''
  setFormStatus('Editing mode', '')
  showFormModal.value = true
}

function closeFormModal() {
  showFormModal.value = false
  resetForm()
}

async function handleSave() {
  if (!formName.value.trim()) {
    setFormStatus('Tag name is required.', 'error')
    return
  }

  saving.value = true
  setFormStatus('Saving...', '')
  try {
    const payload = {
      tagName: formName.value.trim(),
      description: formDescription.value.trim(),
    }
    if (editingId.value !== null) {
      await adminApi.updateTag(editingId.value, payload)
      setFormStatus('Tag updated.', 'success')
      toast.success('Tag updated')
    } else {
      await adminApi.createTag(payload)
      setFormStatus('Tag created.', 'success')
      toast.success('Tag created')
    }
    closeFormModal()
    await loadTags(currentPage.value)
  } catch (e: any) {
    setFormStatus(e.message || 'Failed to save tag', 'error')
  } finally {
    saving.value = false
  }
}

async function handleDelete(tagId: number, name: string) {
  const confirmed = await confirm(
    `Delete tag "${name}"? This may affect projects using this tag.`,
  )
  if (!confirmed) return
  try {
    await adminApi.deleteTag(tagId)
    toast.success('Tag deleted')
    if (editingId.value === tagId) resetForm()
    await loadTags(currentPage.value)
  } catch (e: any) {
    toast.error(e.message || 'Failed to delete tag')
  }
}

onMounted(() => {
  void initialize()
})
</script>

<template>
  <div class="page">
    <header class="page-header">
      <h1>Tag Management</h1>
      <div style="flex-grow: 1"></div>
      <button class="btn-primary" @click="openCreateModal">
        <i class="bi bi-plus-lg"></i> Create Tag
      </button>
    </header>

    <div class="panel">
      <div ref="tableWrapperRef" class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tag Name</th>
              <th>Description</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="4" style="text-align: center; color: #888">
                Loading...
              </td>
            </tr>
            <tr v-else-if="tags.length === 0">
              <td colspan="4" style="text-align: center; color: #888">
                No tags yet
              </td>
            </tr>
            <tr v-for="tag in tags" :key="tag.tagId">
              <td>{{ tag.tagId }}</td>
              <td>
                <strong>{{ tag.tagName }}</strong>
              </td>
              <td>{{ tag.description || '-' }}</td>
              <td>
                <div class="action-btns">
                  <button class="btn-sm btn-edit" @click="editTag(tag)">
                    <i class="bi bi-pencil"></i> Edit
                  </button>
                  <button
                    class="btn-sm btn-delete"
                    @click="handleDelete(tag.tagId, tag.tagName)"
                  >
                    <i class="bi bi-trash"></i> Delete
                  </button>
                </div>
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
        item-label="tags"
        @change="loadTags"
      />

      <Teleport to="body">
        <div
          v-if="showFormModal"
          class="modal-overlay"
          @click.self="closeFormModal"
        >
          <div class="modal-dialog">
            <div class="modal-header">
              <h2>{{ editingId !== null ? 'Edit Tag' : 'Create Tag' }}</h2>
              <button class="icon-button" @click="closeFormModal">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>
            <form class="modal-form" @submit.prevent="handleSave">
              <div class="form-field">
                <label>Tag Name <span class="required">*</span></label>
                <input
                  v-model="formName"
                  type="text"
                  class="form-control"
                  placeholder="e.g. Python"
                />
              </div>
              <div class="form-field">
                <label>Description</label>
                <input
                  v-model="formDescription"
                  type="text"
                  class="form-control"
                  placeholder="Optional description"
                />
              </div>
              <div class="form-status" :class="formStatusType">
                {{ formStatus }}
              </div>
              <div class="modal-actions">
                <button
                  type="button"
                  class="btn-secondary"
                  @click="closeFormModal"
                >
                  Cancel
                </button>
                <button type="submit" class="btn-primary" :disabled="saving">
                  {{
                    saving
                      ? 'Saving...'
                      : editingId !== null
                        ? 'Save Changes'
                        : 'Create Tag'
                  }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </Teleport>
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
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text);
}
.table-wrapper {
  overflow-x: auto;
  border-radius: 18px;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 500px;
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
.action-btns {
  display: flex;
  gap: 8px;
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
.btn-edit {
  border-color: var(--deep);
  color: var(--deep);
}
.btn-edit:hover {
  background: var(--deep);
  color: #fff;
}
.btn-delete {
  border-color: var(--red);
  color: var(--red);
}
.btn-delete:hover {
  background: var(--red);
  color: #fff;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-field label {
  font-weight: 600;
  color: var(--text);
  font-size: 0.9rem;
}
.required {
  color: var(--red);
}
.form-control {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid rgba(90, 43, 152, 0.18);
  border-radius: 12px;
  font-size: 0.95rem;
  font-family: inherit;
  outline: none;
  background: #fff;
}
.form-control:focus {
  border-color: var(--deep);
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.1);
}
.form-status {
  font-size: 0.9rem;
  min-height: 22px;
  color: var(--muted);
}
.form-status.success {
  color: #167d68;
}
.form-status.error {
  color: #b02a37;
}
.btn-primary {
  background: var(--deep);
  border: none;
  color: white;
  padding: 10px 24px;
  border-radius: 40px;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  font-family: inherit;
  display: inline-flex;
  align-items: center;
  justify-content: center;
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
.modal-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 2px;
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
@media (max-width: 640px) {
  .page-header {
    align-items: stretch;
  }
  .page-header .btn-primary {
    width: 100%;
  }
  .modal-actions {
    flex-direction: column-reverse;
  }
  .modal-actions .btn-primary,
  .modal-actions .btn-secondary {
    width: 100%;
  }
}
</style>
