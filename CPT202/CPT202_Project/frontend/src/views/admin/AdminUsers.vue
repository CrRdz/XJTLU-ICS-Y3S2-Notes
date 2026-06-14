<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppPagination from '../../components/AppPagination.vue'
import { useResponsivePageResult } from '../../composables/useResponsivePageResult'
import { adminApi } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

const roleFilter = ref('')
const statusFilter = ref('')
const showEditModal = ref(false)
const editingUserId = ref<number | null>(null)
const formUsername = ref('')
const formFullName = ref('')
const formEmail = ref('')
const formStatus = ref('')
const formStatusType = ref<'success' | 'error' | ''>('')
const saving = ref(false)

const {
  tableWrapperRef,
  loading,
  records: users,
  currentPage,
  pageSize,
  total,
  totalPages,
  visiblePages,
  initialize,
  loadPage: loadUsers,
} = useResponsivePageResult<any>({
  loadPage: async ({ pageNum, pageSize }) => {
    const res = await adminApi.listUsersPage(
      roleFilter.value || undefined,
      statusFilter.value || undefined,
      { pageNum, pageSize },
    )
    return res.data
  },
  onLoadError: (error) => {
    const message =
      error instanceof Error ? error.message : 'Failed to load users'
    toast.error(message)
  },
})

void tableWrapperRef

function statusColor(status: string): string {
  const s = String(status || '').toUpperCase()
  return s === 'ACTIVE' ? 'var(--green)' : 'var(--red)'
}

function resetFilters() {
  roleFilter.value = ''
  statusFilter.value = ''
  void loadUsers(1)
}

function setFormStatus(msg: string, type: 'success' | 'error' | '') {
  formStatus.value = msg
  formStatusType.value = type
}

function resetEditForm() {
  editingUserId.value = null
  formUsername.value = ''
  formFullName.value = ''
  formEmail.value = ''
  setFormStatus('', '')
}

function openEditModal(user: any) {
  editingUserId.value = user.userId
  formUsername.value = user.username || ''
  formFullName.value = user.fullName || ''
  formEmail.value = user.email || ''
  setFormStatus('Editing mode', '')
  showEditModal.value = true
}

function closeEditModal() {
  showEditModal.value = false
  resetEditForm()
}

async function saveUser() {
  if (!formUsername.value.trim()) {
    setFormStatus('Username is required.', 'error')
    return
  }
  if (!formFullName.value.trim()) {
    setFormStatus('Full name is required.', 'error')
    return
  }
  if (!formEmail.value.trim()) {
    setFormStatus('Email is required.', 'error')
    return
  }
  if (editingUserId.value === null) return

  saving.value = true
  setFormStatus('Saving...', '')
  try {
    const payload = {
      username: formUsername.value.trim(),
      fullName: formFullName.value.trim(),
      email: formEmail.value.trim(),
    }
    await adminApi.updateUser(editingUserId.value, payload)
    const currentUserId = Number(localStorage.getItem('userId') || '0')
    if (currentUserId === editingUserId.value) {
      localStorage.setItem('username', payload.username)
      localStorage.setItem('fullName', payload.fullName)
    }
    toast.success('User updated')
    closeEditModal()
    await loadUsers(currentPage.value)
  } catch (e: any) {
    setFormStatus(e.message || 'Failed to update user', 'error')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(userId: number, currentStatus: string) {
  const newStatus =
    currentStatus.toUpperCase() === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  try {
    await adminApi.updateUserStatus(userId, newStatus)
    toast.success(`User ${newStatus === 'ACTIVE' ? 'enabled' : 'disabled'}`)
    await loadUsers(currentPage.value)
  } catch (e: any) {
    toast.error(e.message || 'Failed to update user status')
  }
}

onMounted(() => {
  void initialize()
})
</script>

<template>
  <div class="page">
    <header class="page-header">
      <h1>User Management</h1>
    </header>

    <div class="panel">
      <div class="filters-row">
        <span class="filter-control select-control">
          <select v-model="roleFilter" aria-label="Role" @change="loadUsers(1)">
            <option value="">All Roles</option>
            <option value="ADMIN">Admin</option>
            <option value="TEACHER">Teacher</option>
            <option value="STUDENT">Student</option>
          </select>
        </span>
        <span class="filter-control select-control">
          <select
            v-model="statusFilter"
            aria-label="Account Status"
            @change="loadUsers(1)"
          >
            <option value="">All Account Status</option>
            <option value="ACTIVE">Active</option>
            <option value="DISABLED">Disabled</option>
          </select>
        </span>
        <button class="clear-btn" @click="resetFilters">
          <i class="bi bi-arrow-counterclockwise"></i>
          Clear
        </button>
      </div>

      <div ref="tableWrapperRef" class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>Full Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Account Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" style="text-align: center; color: #888">
                Loading...
              </td>
            </tr>
            <tr v-else-if="users.length === 0">
              <td colspan="7" style="text-align: center; color: #888">
                No users found
              </td>
            </tr>
            <tr v-for="u in users" :key="u.userId">
              <td>{{ u.userId }}</td>
              <td>{{ u.username || '-' }}</td>
              <td>{{ u.fullName || '-' }}</td>
              <td>{{ u.email || '-' }}</td>
              <td>{{ u.role || '-' }}</td>
              <td>
                <span
                  class="status-chip"
                  :style="{
                    color: statusColor(u.accountStatus),
                    background: statusColor(u.accountStatus) + '18',
                  }"
                >
                  {{ u.accountStatus || '-' }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn-sm btn-edit" @click="openEditModal(u)">
                    <i class="bi bi-pencil"></i>
                    Edit
                  </button>
                  <button
                    class="btn-sm"
                    :class="
                      String(u.accountStatus).toUpperCase() === 'ACTIVE'
                        ? 'btn-disable'
                        : 'btn-enable'
                    "
                    @click="toggleStatus(u.userId, u.accountStatus)"
                  >
                    {{
                      String(u.accountStatus).toUpperCase() === 'ACTIVE'
                        ? 'Disable'
                        : 'Enable'
                    }}
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
        item-label="users"
        @change="loadUsers"
      />

      <Teleport to="body">
        <div
          v-if="showEditModal"
          class="modal-overlay"
          @click.self="closeEditModal"
        >
          <div class="modal-dialog">
            <div class="modal-header">
              <h2>Edit User</h2>
              <button class="icon-button" type="button" @click="closeEditModal">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>
            <form class="modal-form" @submit.prevent="saveUser">
              <div class="form-field">
                <label>Username <span class="required">*</span></label>
                <input
                  v-model="formUsername"
                  type="text"
                  class="form-control"
                  placeholder="Login username"
                />
              </div>
              <div class="form-field">
                <label>Full Name <span class="required">*</span></label>
                <input
                  v-model="formFullName"
                  type="text"
                  class="form-control"
                  placeholder="Display name"
                />
              </div>
              <div class="form-field">
                <label>Email <span class="required">*</span></label>
                <input
                  v-model="formEmail"
                  type="email"
                  class="form-control"
                  placeholder="name@example.com"
                />
              </div>
              <div class="form-status" :class="formStatusType">
                {{ formStatus }}
              </div>
              <div class="modal-actions">
                <button
                  type="button"
                  class="btn-secondary"
                  @click="closeEditModal"
                >
                  Cancel
                </button>
                <button type="submit" class="btn-primary" :disabled="saving">
                  {{ saving ? 'Saving...' : 'Save Changes' }}
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
.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text);
}
.filters-row {
  display: grid;
  grid-template-columns: minmax(190px, 1fr) minmax(170px, 0.9fr) auto;
  gap: 12px;
  align-items: end;
  margin-bottom: 16px;
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
.status-chip {
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
  background: transparent;
  white-space: nowrap;
}
.btn-edit {
  border-color: var(--deep);
  color: var(--deep);
}
.btn-edit:hover {
  background: var(--deep);
  color: #fff;
}
.btn-disable {
  border-color: var(--red);
  color: var(--red);
}
.btn-disable:hover {
  background: var(--red);
  color: #fff;
}
.btn-enable {
  border-color: var(--green);
  color: var(--green);
}
.btn-enable:hover {
  background: var(--green);
  color: #fff;
}
.summary {
  margin-top: 16px;
  font-size: 0.9rem;
  color: var(--muted);
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
@media (max-width: 760px) {
  .filters-row {
    grid-template-columns: 1fr;
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
