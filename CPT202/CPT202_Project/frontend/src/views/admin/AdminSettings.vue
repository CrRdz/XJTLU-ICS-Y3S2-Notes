<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import QRCode from 'qrcode'
import SettingsAccountPanel from '../../components/SettingsAccountPanel.vue'
import { adminApi, clearAuth } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

const router = useRouter()

const loading = ref(true)
const saving = ref(false)
const showPasswordModal = ref(false)
const profileStatus = ref('')
const profileStatusType = ref<'success' | 'error' | ''>('')

// Profile fields
const account = ref('')
const fullName = ref('')
const email = ref('')

// 2FA
const twoFactorEnabled = ref(false)
const twoFactorSetupBox = ref(false)
const twoFactorManualKey = ref('')
const twoFactorOtpAuthUri = ref('')
const twoFactorQrCanvas = ref<HTMLCanvasElement | null>(null)
const twoFactorEnableCode = ref('')
const twoFactorDisablePassword = ref('')
const twoFactorStatus = ref('')
const twoFactorStatusType = ref<'success' | 'error' | ''>('')
const setupLoading = ref(false)
const twoFactorCollapsed = ref(true)

// Password
const oldPassword = ref('')
const newPassword = ref('')
const confirmNewPassword = ref('')
const passwordStatus = ref('')
const passwordStatusType = ref<'success' | 'error' | ''>('')
const passwordSaving = ref(false)

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

async function fetchProfile() {
  loading.value = true
  try {
    const res = await adminApi.getProfile()
    const p = res.data
    if (p) {
      account.value = p.username || p.account || ''
      fullName.value = p.fullName || ''
      email.value = p.email || ''
      twoFactorEnabled.value = Boolean(p.twoFactorEnabled)
      if (p.fullName) localStorage.setItem('fullName', p.fullName)
    }
    profileStatus.value = 'Profile loaded.'
    profileStatusType.value = 'success'
  } catch (e: any) {
    profileStatus.value = e.message || 'Failed to load profile'
    profileStatusType.value = 'error'
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!fullName.value.trim()) {
    profileStatus.value = 'Full name is required.'
    profileStatusType.value = 'error'
    return
  }
  if (!email.value.trim()) {
    profileStatus.value = 'Email is required.'
    profileStatusType.value = 'error'
    return
  }
  if (!EMAIL_PATTERN.test(email.value.trim())) {
    profileStatus.value = 'Invalid email format.'
    profileStatusType.value = 'error'
    return
  }

  saving.value = true
  profileStatus.value = 'Saving...'
  profileStatusType.value = ''
  try {
    await adminApi.updateProfile({
      fullName: fullName.value.trim(),
      email: email.value.trim(),
    })
    localStorage.setItem('fullName', fullName.value.trim())
    profileStatus.value = 'Profile saved successfully.'
    profileStatusType.value = 'success'
    toast.success('Profile saved')
  } catch (e: any) {
    profileStatus.value = e.message || 'Failed to save profile'
    profileStatusType.value = 'error'
  } finally {
    saving.value = false
  }
}

// 2FA
async function setupTwoFactor() {
  setupLoading.value = true
  twoFactorStatus.value = 'Generating QR code...'
  twoFactorStatusType.value = ''
  try {
    const res = await adminApi.initializeTwoFactorSetup()
    const setup = res.data
    if (setup?.enabled) {
      twoFactorEnabled.value = true
      twoFactorSetupBox.value = false
      twoFactorStatus.value = '2FA is already enabled.'
      twoFactorStatusType.value = 'success'
      return
    }
    twoFactorSetupBox.value = true
    twoFactorManualKey.value = setup?.manualEntryKey || ''
    twoFactorOtpAuthUri.value = setup?.otpAuthUri || ''
    twoFactorEnableCode.value = ''
    twoFactorStatus.value = 'Scan the QR code, then enter the 6-digit code.'
    twoFactorStatusType.value = 'success'
    // Render QR code after DOM update
    setTimeout(() => {
      if (twoFactorQrCanvas.value && twoFactorOtpAuthUri.value) {
        QRCode.toCanvas(twoFactorQrCanvas.value, twoFactorOtpAuthUri.value, {
          width: 180,
          margin: 1,
        })
      }
    }, 0)
  } catch (e: any) {
    twoFactorStatus.value = e.message || 'Failed to setup 2FA'
    twoFactorStatusType.value = 'error'
  } finally {
    setupLoading.value = false
  }
}

function collapseTwoFactorSetup() {
  twoFactorSetupBox.value = false
  twoFactorEnableCode.value = ''
  twoFactorStatus.value = ''
  twoFactorStatusType.value = ''
}

function toggleTwoFactorSetup() {
  if (twoFactorSetupBox.value) {
    collapseTwoFactorSetup()
    return
  }
  twoFactorCollapsed.value = false
  setupTwoFactor()
}

async function enableTwoFactor() {
  if (!/^\d{6}$/.test(twoFactorEnableCode.value)) {
    twoFactorStatus.value = 'Please enter a valid 6-digit code.'
    twoFactorStatusType.value = 'error'
    return
  }
  setupLoading.value = true
  twoFactorStatus.value = 'Enabling 2FA...'
  twoFactorStatusType.value = ''
  try {
    await adminApi.enableTwoFactor(twoFactorEnableCode.value)
    twoFactorEnabled.value = true
    twoFactorSetupBox.value = false
    twoFactorStatus.value = '2FA enabled successfully.'
    twoFactorStatusType.value = 'success'
  } catch (e: any) {
    twoFactorStatus.value = e.message || 'Failed to enable 2FA'
    twoFactorStatusType.value = 'error'
  } finally {
    setupLoading.value = false
  }
}

async function disableTwoFactor() {
  if (!twoFactorDisablePassword.value.trim()) {
    twoFactorStatus.value = 'Please enter your current password.'
    twoFactorStatusType.value = 'error'
    return
  }
  setupLoading.value = true
  twoFactorStatus.value = 'Disabling 2FA...'
  twoFactorStatusType.value = ''
  try {
    await adminApi.disableTwoFactor(twoFactorDisablePassword.value)
    twoFactorEnabled.value = false
    twoFactorDisablePassword.value = ''
    twoFactorStatus.value = '2FA disabled.'
    twoFactorStatusType.value = 'success'
  } catch (e: any) {
    twoFactorStatus.value = e.message || 'Failed to disable 2FA'
    twoFactorStatusType.value = 'error'
  } finally {
    setupLoading.value = false
  }
}

// Password
async function handleChangePassword() {
  if (!oldPassword.value.trim()) {
    passwordStatus.value = 'Please enter your current password.'
    passwordStatusType.value = 'error'
    return
  }
  if (!newPassword.value.trim()) {
    passwordStatus.value = 'Please enter a new password.'
    passwordStatusType.value = 'error'
    return
  }
  if (newPassword.value !== confirmNewPassword.value) {
    passwordStatus.value = 'Passwords do not match.'
    passwordStatusType.value = 'error'
    return
  }

  passwordSaving.value = true
  passwordStatus.value = 'Changing password...'
  passwordStatusType.value = ''
  try {
    await adminApi.changePassword({
      oldPassword: oldPassword.value,
      newPassword: newPassword.value,
    })
    passwordStatus.value = 'Password changed successfully.'
    passwordStatusType.value = 'success'
    toast.success('Password changed')
    setTimeout(() => {
      showPasswordModal.value = false
    }, 700)
  } catch (e: any) {
    passwordStatus.value = e.message || 'Failed to change password'
    passwordStatusType.value = 'error'
  } finally {
    passwordSaving.value = false
  }
}

function handleLogout() {
  clearAuth()
  router.push('/login')
}

onMounted(fetchProfile)
</script>

<template>
  <div class="settings-page">
    <div class="page-header">
      <h1>Settings</h1>
    </div>

    <SettingsAccountPanel
      :account="account"
      :email="email"
      role-label="Administrator"
      hint="Keep this account name for admin sign-in and security reviews."
    />
    <div class="content-panel">
      <form class="settings-form" @submit.prevent="handleSave">
        <!-- Basic Info -->
        <div class="form-section">
          <div class="section-title">Basic Information</div>

          <div class="form-row">
            <span class="form-label"><i class="bi bi-person"></i> Account</span>
            <input :value="account" type="text" class="form-control" disabled />
          </div>
          <div class="form-row">
            <span class="form-label"
              ><i class="bi bi-person-badge"></i> Full Name</span
            >
            <input
              v-model="fullName"
              type="text"
              class="form-control"
              placeholder="Your full name"
            />
          </div>
          <div class="form-row">
            <span class="form-label"><i class="bi bi-envelope"></i> Email</span>
            <input
              v-model="email"
              type="email"
              class="form-control"
              placeholder="Your email"
            />
          </div>
          <div class="form-row">
            <span class="form-label"><i class="bi bi-shield"></i> Role</span>
            <div style="color: var(--muted)">System Administrator</div>
          </div>
        </div>

        <!-- 2FA -->
        <div class="form-section">
          <div
            class="section-title"
            style="
              user-select: none;
              display: flex;
              align-items: center;
              justify-content: space-between;
            "
          >
            <span>Two-Factor Authentication (TOTP)</span>
            <button
              type="button"
              class="setup-toggle-btn"
              :disabled="setupLoading"
              @click="toggleTwoFactorSetup"
            >
              {{
                twoFactorSetupBox
                  ? 'Collapse setup'
                  : twoFactorEnabled
                    ? 'Regenerate QR Code'
                    : 'Setup 2FA'
              }}
            </button>
          </div>
          <div v-show="!twoFactorCollapsed">
            <div class="form-row">
              <span class="form-label"
                ><i class="bi bi-shield-lock"></i> Status</span
              >
              <div style="color: var(--muted)">
                {{ twoFactorEnabled ? 'Enabled' : 'Disabled' }}
              </div>
            </div>

            <div v-if="twoFactorSetupBox" class="tfa-box">
              <canvas
                ref="twoFactorQrCanvas"
                style="max-width: 180px; display: block; margin: 0 auto 12px"
              ></canvas>
              <p
                style="font-size: 0.9rem; color: var(--muted); margin: 0 0 8px"
              >
                Manual key: <strong>{{ twoFactorManualKey }}</strong>
              </p>
              <input
                v-model="twoFactorEnableCode"
                type="text"
                inputmode="numeric"
                maxlength="6"
                class="form-control"
                placeholder="6-digit code"
              />
              <button
                type="button"
                class="btn-primary"
                style="margin-top: 12px"
                :disabled="setupLoading"
                @click="enableTwoFactor"
              >
                {{ setupLoading ? 'Enabling...' : 'Enable 2FA' }}
              </button>
            </div>

            <div v-if="twoFactorEnabled" class="tfa-box">
              <input
                v-model="twoFactorDisablePassword"
                type="password"
                class="form-control"
                placeholder="Enter current password to disable 2FA"
              />
              <button
                type="button"
                class="btn-secondary"
                style="margin-top: 12px"
                :disabled="setupLoading"
                @click="disableTwoFactor"
              >
                {{ setupLoading ? 'Disabling...' : 'Disable 2FA' }}
              </button>
            </div>

            <div class="form-status" :class="twoFactorStatusType">
              {{ twoFactorStatus }}
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="form-actions">
          <button type="submit" class="btn-primary" :disabled="saving">
            {{ saving ? 'Saving...' : 'Save Changes' }}
          </button>
          <button
            type="button"
            class="btn-secondary"
            @click="showPasswordModal = true"
          >
            Change Password
          </button>
          <button type="button" class="btn-logout" @click="handleLogout">
            Logout
          </button>
          <span
            style="
              flex: 1;
              text-align: right;
              font-size: 0.8rem;
              color: var(--muted);
            "
          >
            <i class="bi bi-shield-check"></i> Encrypted transmission
          </span>
          <div class="form-status" :class="profileStatusType">
            {{ profileStatus }}
          </div>
        </div>
      </form>
    </div>

    <!-- Password Modal -->
    <Teleport to="body">
      <div
        v-if="showPasswordModal"
        class="password-modal"
        @click.self="showPasswordModal = false"
      >
        <div class="password-dialog">
          <div class="password-dialog-header">
            <h2>Change Password</h2>
            <button class="icon-button" @click="showPasswordModal = false">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <form class="password-form" @submit.prevent="handleChangePassword">
            <div class="password-field">
              <label>Current Password</label>
              <input
                v-model="oldPassword"
                type="password"
                class="form-control"
                placeholder="Current password"
              />
            </div>
            <div class="password-field">
              <label>New Password</label>
              <input
                v-model="newPassword"
                type="password"
                class="form-control"
                placeholder="New password"
              />
            </div>
            <div class="password-field">
              <label>Confirm New Password</label>
              <input
                v-model="confirmNewPassword"
                type="password"
                class="form-control"
                placeholder="Confirm new password"
              />
            </div>
            <div class="form-status" :class="passwordStatusType">
              {{ passwordStatus }}
            </div>
            <div class="password-actions">
              <button
                type="button"
                class="btn-secondary"
                @click="showPasswordModal = false"
              >
                Cancel
              </button>
              <button
                type="submit"
                class="btn-primary"
                :disabled="passwordSaving"
              >
                {{ passwordSaving ? 'Changing...' : 'Change Password' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.settings-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 100%;
}

.page-header {
  width: min(860px, 100%);
}

.page-header h1 {
  margin: 0;
  font-size: 1.9rem;
  font-weight: 600;
  color: var(--text);
}

.content-panel {
  background: #fff;
  border-radius: 28px;
  padding: 32px 36px;
  border: 1px solid rgba(28, 27, 51, 0.08);
  width: min(860px, 100%);
  box-sizing: border-box;
}

.settings-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.form-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.section-title {
  font-weight: 600;
  font-size: 1.1rem;
  color: var(--deep);
  margin-bottom: 4px;
  border-left: 4px solid var(--deep);
  padding-left: 14px;
}

.setup-toggle-btn {
  border: 1.5px solid var(--muted);
  border-radius: 999px;
  background: transparent;
  color: var(--text);
  cursor: pointer;
  font-family: inherit;
  font-size: 0.9rem;
  font-weight: 600;
  padding: 8px 18px;
  white-space: nowrap;
}

.setup-toggle-btn:hover {
  border-color: var(--deep);
  color: var(--deep);
}

.setup-toggle-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.form-row {
  display: grid;
  grid-template-columns: 160px 1fr;
  align-items: center;
  gap: 16px;
}

.form-label {
  font-weight: 500;
  color: var(--text);
  font-size: 0.95rem;
}
.form-label i {
  color: var(--muted);
  margin-right: 6px;
  font-size: 1rem;
}

.form-control {
  padding: 12px 16px;
  border: 1.5px solid #d9d2e8;
  border-radius: 16px;
  font-size: 0.95rem;
  background: white;
  transition:
    border 0.2s,
    box-shadow 0.2s;
  font-family: inherit;
  width: 100%;
  outline: none;
  box-sizing: border-box;
}

.form-control:focus {
  border-color: var(--deep);
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.1);
}
.form-control:disabled {
  background: var(--bg);
  color: var(--muted);
}

.tfa-box {
  border: 1px solid rgba(90, 43, 152, 0.12);
  border-radius: 20px;
  padding: 18px;
  background: #faf8ff;
}

.form-actions {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(156, 156, 178, 0.25);
  align-items: center;
  flex-wrap: wrap;
}

.form-status {
  width: 100%;
  min-height: 22px;
  font-size: 0.9rem;
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
  padding: 12px 30px;
  border-radius: 40px;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  font-family: inherit;
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
  padding: 12px 28px;
  border-radius: 40px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
}
.btn-secondary:hover {
  border-color: var(--deep);
  color: var(--deep);
}

.btn-logout {
  background: transparent;
  border: 1.5px solid #dc3545;
  color: #dc3545;
  padding: 12px 28px;
  border-radius: 40px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  margin-left: auto;
}
.btn-logout:hover {
  background: rgba(220, 53, 69, 0.05);
}

/* Password Modal */
.password-modal {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(28, 27, 51, 0.45);
  z-index: 20;
}

.password-dialog {
  width: min(440px, 100%);
  background: #fff;
  border-radius: 24px;
  border: 1px solid rgba(28, 27, 51, 0.08);
  padding: 28px;
}

.password-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.password-dialog-header h2 {
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

.password-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.password-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.password-field label {
  color: var(--text);
  font-weight: 500;
  font-size: 0.92rem;
}

.password-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 8px;
}

@media (max-width: 960px) {
  .form-row {
    grid-template-columns: 1fr;
    gap: 6px;
  }
  .content-panel {
    padding: 24px;
  }
  .btn-logout {
    margin-left: 0;
  }
}

@media (max-width: 640px) {
  .page-header,
  .content-panel {
    width: 100%;
  }

  .content-panel {
    border-radius: 20px;
    padding: 22px 18px;
  }

  .form-actions {
    align-items: stretch;
  }

  .form-actions > button {
    width: 100%;
  }
}
</style>
