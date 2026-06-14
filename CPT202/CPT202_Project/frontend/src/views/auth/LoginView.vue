<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authApi, setAuth } from '../../utils/api'

//luyou control
const router = useRouter()
const route = useRoute()

// Panel state in5
type PanelId = 'login' | 'emailOtp' | 'twoFactor' | 'register' | 'reset'
const currentPanel = ref<PanelId>('login')

// Login
const loginUsername = ref('') //this word will update automatically
const loginPassword = ref('')
const loginError = ref('')
const loginLoading = ref(false)

// Email OTP
const otpEmail = ref('')
const otpCode = ref('')
const otpDigits = ref<string[]>(['', '', '', '', '', '']) //yanzhengma
const otpRefs = ref<HTMLInputElement[]>([])
const otpRequestMessage = ref('')
const otpRequestLoading = ref(false)
const otpLoginMessage = ref('')
const otpLoginLoading = ref(false)

// Handle OTP input
function onOtpInput(index: number, e: Event) {
  const raw = (e.target as HTMLInputElement).value
  const val = raw.replace(/[^0-9]/g, '') //num
  otpDigits.value[index] = val.slice(-1)
  ;(e.target as HTMLInputElement).value = otpDigits.value[index]
  if (val && index < 5) otpRefs.value[index + 1]?.focus()
  otpCode.value = otpDigits.value.join('')
}

function onOtpKeydown(index: number, e: KeyboardEvent) {
  if (e.key === 'Backspace' && !otpDigits.value[index] && index > 0) {
    otpDigits.value[index - 1] = '' //remove
    otpRefs.value[index - 1]?.focus()
    otpCode.value = otpDigits.value.join('')
  }
}

// Handle paste of OTP code
function onOtpPaste(e: ClipboardEvent) {
  const text = e.clipboardData?.getData('text')?.replace(/\D/g, '') ?? ''
  if (text.length >= 6) {
    e.preventDefault()
    for (let i = 0; i < 6; i++) otpDigits.value[i] = text[i]
    otpCode.value = otpDigits.value.join('')
    otpRefs.value[5]?.focus()
  }
}

//clear
function resetOtpDigits() {
  otpDigits.value = ['', '', '', '', '', '']
  otpCode.value = ''
}

// Two-Factor
const twoFactorCode = ref('')
const twoFactorDigits = ref<string[]>(['', '', '', '', '', ''])
const twoFactorRefs = ref<HTMLInputElement[]>([])
const twoFactorChallengeToken = ref('')
const twoFactorMessage = ref('')
const twoFactorLoading = ref(false)
const twoFactorUsername = ref('')

// Handle Two-Factor input
function onTwoFactorInput(index: number, e: Event) {
  const raw = (e.target as HTMLInputElement).value
  const val = raw.replace(/[^0-9]/g, '')
  twoFactorDigits.value[index] = val.slice(-1)
  ;(e.target as HTMLInputElement).value = twoFactorDigits.value[index]
  if (val && index < 5) twoFactorRefs.value[index + 1]?.focus()
  twoFactorCode.value = twoFactorDigits.value.join('')
}
// Handle Two-Factor backspace（same）
function onTwoFactorKeydown(index: number, e: KeyboardEvent) {
  if (e.key === 'Backspace' && !twoFactorDigits.value[index] && index > 0) {
    twoFactorDigits.value[index - 1] = ''
    twoFactorRefs.value[index - 1]?.focus()
    twoFactorCode.value = twoFactorDigits.value.join('')
  }
}

// Handle Two-Factor paste（same）
function onTwoFactorPaste(e: ClipboardEvent) {
  const text = e.clipboardData?.getData('text')?.replace(/\D/g, '') ?? ''
  if (text.length >= 6) {
    e.preventDefault()
    for (let i = 0; i < 6; i++) twoFactorDigits.value[i] = text[i]
    twoFactorCode.value = twoFactorDigits.value.join('')
    twoFactorRefs.value[5]?.focus()
  }
}
//clear
function resetTwoFactorDigits() {
  twoFactorDigits.value = ['', '', '', '', '', '']
  twoFactorCode.value = ''
}

// Register
const regUsername = ref('')
const regPassword = ref('')
const regEmail = ref('')
const regFullName = ref('')
const regOtp = ref('')
const regStudentNo = ref('')
const regProgramme = ref('')
const regEntryDate = ref('')
const regPhone = ref('')
const regInterests = ref('')
const regStaffNo = ref('')
const regDepartment = ref('')
const regTitle = ref('')
const regResearchArea = ref('')
const regOffice = ref('')
const registerError = ref('')
const registerSuccess = ref('')
const registerLoading = ref(false)
const registerOtpSending = ref(false)
const registerOtpCountdown = ref(0)

// Reset password
const resetEmail = ref('')
const resetToken = ref('')
const resetPassword = ref('')
const resetConfirmPassword = ref('')
const resetRequestMessage = ref('')
const resetRequestLoading = ref(false)
const resetConfirmMessage = ref('')
const resetConfirmLoading = ref(false)

const hasResetToken = computed(() => !!resetToken.value)
const otpMessage = computed(
  () => otpLoginMessage.value || otpRequestMessage.value,
)
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/ //format
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/
const STUDENT_EMAIL_DOMAIN = 'student.xjtlu.edu.cn'
const TEACHER_EMAIL_DOMAIN = 'xjtlu.edu.cn'

const inferredRegisterRole = computed<'STUDENT' | 'TEACHER' | ''>(() => {
  const email = regEmail.value.trim().toLowerCase()
  const domain = email.includes('@') ? email.split('@').pop() || '' : ''
  if (domain === STUDENT_EMAIL_DOMAIN) return 'STUDENT'
  if (domain === TEACHER_EMAIL_DOMAIN) return 'TEACHER'
  return ''
})

const registerRoleLabel = computed(() => {
  if (inferredRegisterRole.value === 'STUDENT') return 'Student account'
  if (inferredRegisterRole.value === 'TEACHER') return 'Teacher account'
  return 'Role will be inferred from your email'
})

const registerRoleHint = computed(() => {
  if (inferredRegisterRole.value === 'STUDENT') {
    return 'Using @student.xjtlu.edu.cn means student profile fields are required.'
  }
  if (inferredRegisterRole.value === 'TEACHER') {
    return 'Using @xjtlu.edu.cn means teacher profile fields are required.'
  }
  return 'Only @student.xjtlu.edu.cn and @xjtlu.edu.cn addresses can register.'
})

const canSendRegisterOtp = computed(
  () =>
    validateEmail(regEmail.value) &&
    !!inferredRegisterRole.value &&
    registerOtpCountdown.value === 0,
)

//Validate email format
function validateEmail(email: string) {
  return EMAIL_PATTERN.test(email.trim())
}

// Check if date is in the future
function isFutureDate(value: string) {
  if (!value) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return new Date(`${value}T00:00:00`) > today
}

function promptMessage(message: unknown, fallback = '') {
  const text = typeof message === 'string' ? message.trim() : ''
  if (!text) return fallback
  return text
}

function getErrorMessage(
  error: unknown,
  fallback = 'Network error, please try again.',
) {
  return error instanceof Error && error.message
    ? promptMessage(error.message, fallback)
    : fallback
}

function resetRegisterForm() {
  regUsername.value = ''
  regPassword.value = ''
  regEmail.value = ''
  regFullName.value = ''
  regOtp.value = ''
  regStudentNo.value = ''
  regProgramme.value = ''
  regEntryDate.value = ''
  regPhone.value = ''
  regInterests.value = ''
  regStaffNo.value = ''
  regDepartment.value = ''
  regTitle.value = ''
  regResearchArea.value = ''
  regOffice.value = ''
  registerError.value = ''
  registerSuccess.value = ''
  registerOtpSending.value = false
  registerOtpCountdown.value = 0
}

function startRegisterOtpCountdown(seconds = 60) {
  registerOtpCountdown.value = seconds
  const timer = window.setInterval(() => {
    if (registerOtpCountdown.value <= 1) {
      registerOtpCountdown.value = 0
      window.clearInterval(timer)
      return
    }
    registerOtpCountdown.value -= 1
  }, 1000)
}

//change panel
function showPanel(id: PanelId) {
  currentPanel.value = id
  if (id === 'login') {
    loginError.value = ''
  }
  if (id === 'emailOtp') {
    resetOtpDigits()
    otpRequestMessage.value = ''
    otpLoginMessage.value = ''
  }
  if (id === 'twoFactor') {
    resetTwoFactorDigits()
    twoFactorMessage.value = ''
  }
  if (id === 'register') {
    registerError.value = ''
    registerSuccess.value = ''
  }
}
//redirection
function redirectByRole(role: string) {
  const routes: Record<string, string> = {
    TEACHER: '/teacher/dashboard',
    STUDENT: '/student/dashboard',
    ADMIN: '/admin/projects',
  }
  const target = routes[role]
  if (target) {
    router.push(target)
  } else {
    loginError.value = 'Unknown role, cannot redirect.'
  }
}

// Login
async function handleLogin() {
  loginError.value = ''
  loginLoading.value = true
  try {
    const res = await authApi.login({
      username: loginUsername.value.trim(),
      password: loginPassword.value,
    })
    if (res.code === 1 && res.data) {
      //sucess
      const data = res.data as any
      if (data.twoFactorRequired) {
        twoFactorChallengeToken.value = data.twoFactorChallengeToken || ''
        twoFactorUsername.value = data.username || ''
        resetTwoFactorDigits()
        twoFactorMessage.value = ''
        showPanel('twoFactor')
        return
      }
      setAuth(data)
      redirectByRole(data.role)
    } else {
      loginError.value = promptMessage(res.msg, 'Login failed')
    }
  } catch (e) {
    loginError.value = getErrorMessage(e, 'Login failed')
  } finally {
    loginLoading.value = false
  }
}

// Two-Factor
async function handleTwoFactor() {
  twoFactorMessage.value = ''
  if (!twoFactorChallengeToken.value) {
    twoFactorMessage.value = 'Session expired. Please log in again.'
    return
  }
  if (!/^\d{6}$/.test(twoFactorCode.value)) {
    twoFactorMessage.value = 'Please enter a valid 6-digit code.'
    return
  }
  twoFactorLoading.value = true
  try {
    const res = await authApi.verifyTwoFactor(
      twoFactorChallengeToken.value,
      twoFactorCode.value,
    )
    if (res.code === 1 && res.data) {
      setAuth(res.data as any)
      redirectByRole((res.data as any).role)
    } else {
      twoFactorMessage.value = promptMessage(res.msg, 'Verification failed')
    }
  } catch (e) {
    twoFactorMessage.value = getErrorMessage(e, 'Verification failed')
  } finally {
    twoFactorLoading.value = false
  }
}

// Email OTP - send code
async function handleSendOtp() {
  otpRequestMessage.value = ''
  otpLoginMessage.value = ''
  if (!validateEmail(otpEmail.value)) {
    otpRequestMessage.value = 'Invalid email format.'
    return
  }
  otpRequestLoading.value = true
  try {
    const res = await authApi.sendEmailOtp(otpEmail.value.trim())
    otpRequestMessage.value =
      res.code === 1
        ? promptMessage(res.data, 'Verification code sent.')
        : promptMessage(res.msg, 'Failed to send code')
    if (res.code === 1) resetOtpDigits()
  } catch (e) {
    otpRequestMessage.value = getErrorMessage(e, 'Failed to send code')
  } finally {
    otpRequestLoading.value = false
  }
}

// Email OTP - login
async function handleOtpLogin() {
  otpLoginMessage.value = ''
  if (!validateEmail(otpEmail.value)) {
    otpLoginMessage.value = 'Invalid email format.'
    return
  }
  if (!/^\d{6}$/.test(otpCode.value)) {
    otpLoginMessage.value = 'Please enter a 6-digit code.'
    return
  }
  otpLoginLoading.value = true
  try {
    const res = await authApi.emailOtpLogin(
      otpEmail.value.trim(),
      otpCode.value.trim(),
    )
    if (res.code === 1 && res.data) {
      setAuth(res.data as any)
      redirectByRole((res.data as any).role)
    } else {
      otpLoginMessage.value = promptMessage(res.msg, 'Login failed')
    }
  } catch (e) {
    otpLoginMessage.value = getErrorMessage(e, 'Login failed')
  } finally {
    otpLoginLoading.value = false
  }
}

// Register
async function handleRegister() {
  registerError.value = ''
  registerSuccess.value = ''
  registerLoading.value = true

  const payload: Record<string, string> = {
    username: regUsername.value.trim(),
    password: regPassword.value,
    email: regEmail.value.trim(),
    fullName: regFullName.value.trim(),
    otp: regOtp.value.trim(),
  }

  if (!validateEmail(payload.email)) {
    registerError.value = 'Invalid email format.'
    registerLoading.value = false
    return
  }
  if (!inferredRegisterRole.value) {
    registerError.value =
      'Please use your XJTLU student or teacher email address.'
    registerLoading.value = false
    return
  }
  if (!/^\d{6}$/.test(payload.otp)) {
    registerError.value =
      'Please enter the 6-digit verification code sent to your email.'
    registerLoading.value = false
    return
  }

  if (inferredRegisterRole.value === 'STUDENT') {
    payload.studentNo = regStudentNo.value.trim()
    payload.programme = regProgramme.value.trim()
    payload.enrollmentDate = regEntryDate.value.trim()
    payload.phone = regPhone.value.trim()
    payload.interests = regInterests.value.trim()
  } else {
    payload.staffNo = regStaffNo.value.trim()
    payload.department = regDepartment.value.trim()
    payload.title = regTitle.value.trim()
    payload.researchArea = regResearchArea.value.trim()
    payload.office = regOffice.value.trim()
  }

  if (
    inferredRegisterRole.value === 'STUDENT' &&
    !DATE_PATTERN.test(payload.enrollmentDate || '')
  ) {
    registerError.value = 'Enrollment date must use yyyy-MM-dd format.'
    registerLoading.value = false
    return
  }
  if (
    inferredRegisterRole.value === 'STUDENT' &&
    isFutureDate(payload.enrollmentDate)
  ) {
    registerError.value = 'Enrollment date cannot be in the future.'
    registerLoading.value = false
    return
  }

  try {
    const res = await authApi.register(payload)
    if (res.code === 1 && res.data) {
      setAuth(res.data as any)
      resetRegisterForm()
      registerSuccess.value =
        'Register success. Redirecting to your dashboard...'
      redirectByRole((res.data as any).role)
    } else if (res.code === 1) {
      resetRegisterForm()
      registerSuccess.value = 'Register success. Please log in.'
      showPanel('login')
      loginError.value = 'Register success. Please log in.'
    } else {
      registerError.value = promptMessage(res.msg, 'Register failed')
    }
  } catch (e) {
    registerError.value = getErrorMessage(e, 'Register failed')
  } finally {
    registerLoading.value = false
  }
}

async function handleSendRegisterOtp() {
  registerError.value = ''
  registerSuccess.value = ''
  if (!validateEmail(regEmail.value)) {
    registerError.value = 'Please enter a valid email before requesting a code.'
    return
  }
  if (!inferredRegisterRole.value) {
    registerError.value =
      'Only @student.xjtlu.edu.cn and @xjtlu.edu.cn addresses can receive registration codes.'
    return
  }

  registerOtpSending.value = true
  try {
    const res = await authApi.sendRegisterEmailOtp(regEmail.value.trim())
    if (res.code === 1) {
      registerSuccess.value = promptMessage(
        res.msg || res.data,
        'Verification code sent.',
      )
      startRegisterOtpCountdown()
    } else {
      registerError.value = promptMessage(
        res.msg,
        'Failed to send verification code.',
      )
    }
  } catch (e) {
    registerError.value = getErrorMessage(
      e,
      'Failed to send verification code.',
    )
  } finally {
    registerOtpSending.value = false
  }
}

// Reset - request
async function handleResetRequest() {
  resetRequestMessage.value = ''
  if (!validateEmail(resetEmail.value)) {
    resetRequestMessage.value = 'Invalid email format.'
    return
  }
  resetRequestLoading.value = true
  try {
    const res = await authApi.forgotPassword(resetEmail.value.trim())
    resetRequestMessage.value =
      res.code === 1
        ? promptMessage(
            res.data,
            'If the email exists, a reset link has been sent.',
          )
        : promptMessage(res.msg, 'Failed to send reset email')
  } catch (e) {
    resetRequestMessage.value = getErrorMessage(e, 'Failed to send reset email')
  } finally {
    resetRequestLoading.value = false
  }
}

// Reset - confirm
async function handleResetConfirm() {
  resetConfirmMessage.value = ''
  if (!resetToken.value) {
    resetConfirmMessage.value = 'Reset token is missing.'
    return
  }
  if (resetPassword.value !== resetConfirmPassword.value) {
    resetConfirmMessage.value = 'Passwords do not match.'
    return
  }
  resetConfirmLoading.value = true
  try {
    const res = await authApi.resetPassword(
      resetToken.value,
      resetPassword.value,
    )
    if (res.code === 1) {
      resetConfirmMessage.value = 'Password reset successfully. Please log in.'
      resetToken.value = ''
      showPanel('login')
    } else {
      resetConfirmMessage.value = promptMessage(res.msg, 'Reset failed')
    }
  } catch (e) {
    resetConfirmMessage.value = getErrorMessage(e, 'Reset failed')
  } finally {
    resetConfirmLoading.value = false
  }
}

//check URL for panel
onMounted(async () => {
  // Check for reset token in URL
  const params = new URLSearchParams(window.location.search)
  const token = params.get('token')
  if (token) {
    resetToken.value = token
  }

  const isResetRoute =
    route.path.includes('forgot-password') ||
    route.path.includes('reset-password')
  if (route.path === '/register') {
    showPanel('register')
  } else if (isResetRoute || window.location.hash === '#reset' || token) {
    showPanel('reset')
  }

  // Anime.js animations
  try {
    const { animate, stagger, splitText } = await import('animejs')
    await nextTick()

    const { chars } = splitText('.guide-eyebrow', { words: false, chars: true })

    animate(chars, {
      y: [
        { to: '-0.7rem', ease: 'outExpo', duration: 600 },
        { to: 0, ease: 'outBounce', duration: 800, delay: 100 },
      ],
      rotate: { from: '-1turn', delay: 0 },
      delay: stagger(50),
      ease: 'inOutCirc',
      loopDelay: 1000,
      loop: true,
    })
  } catch {
    // animejs not available, skip animations
  }
})
</script>

<template>
  <section class="guide-page" aria-label="Project introduction">
    <div class="guide-copy">
      <p class="guide-eyebrow">CPT202 Project Selection System</p>
      <h1>Find and manage your final year project in one place.</h1>
      <p class="guide-text">
        Students can browse available topics and submit applications. Teachers
        can publish projects, review requests, and keep project capacity under
        control.
      </p>

      <div class="guide-points" aria-label="Core features">
        <div>
          <strong>Students</strong>
          <span
            >Search projects, track requests, and manage profile details.</span
          >
        </div>
        <div>
          <strong>Teachers</strong>
          <span>Create projects, review applications, and bind tags.</span>
        </div>
        <div>
          <strong>Admins</strong>
          <span>Maintain users, categories, tags, and request records.</span>
        </div>
      </div>
    </div>

    <div class="guide-visual">
      <div
        class="login-card auth-panel-card"
        :class="{ 'register-mode': currentPanel === 'register' }"
      >
        <div class="auth-tabs" aria-label="Authentication options">
          <button
            type="button"
            class="auth-tab"
            :class="{ active: currentPanel === 'login' }"
            @click="showPanel('login')"
          >
            Login
          </button>
          <button
            type="button"
            class="auth-tab"
            :class="{ active: currentPanel === 'register' }"
            @click="showPanel('register')"
          >
            Register
          </button>
        </div>

        <!-- Login Panel -->
        <div v-show="currentPanel === 'login'" class="auth-panel active">
          <h2>Welcome Back!</h2>
          <h3>Please log in to your account</h3>

          <form @submit.prevent="handleLogin">
            <input
              v-model="loginUsername"
              type="text"
              placeholder="Username"
              required
            />
            <input
              v-model="loginPassword"
              type="password"
              placeholder="Password"
              required
            />

            <div class="forgot-wrap">
              <button
                type="button"
                class="text-link"
                @click="showPanel('reset')"
              >
                Forgot Password?
              </button>
            </div>

            <div class="helper">{{ loginError }}</div>
            <button type="submit" :disabled="loginLoading">
              {{ loginLoading ? 'Logging in...' : 'Login' }}
            </button>
          </form>

          <button type="button" class="otp-btn" @click="showPanel('emailOtp')">
            Login with Email OTP
          </button>
        </div>

        <!-- Email OTP Panel -->
        <div v-show="currentPanel === 'emailOtp'" class="auth-panel active">
          <h2>Email OTP Login</h2>
          <h3>Use your registered email to receive a one-time code</h3>

          <form class="otp-send-row" @submit.prevent="handleSendOtp">
            <input
              v-model="otpEmail"
              type="email"
              placeholder="Enter your email"
              required
            />
            <button
              type="submit"
              class="otp-send-btn"
              :disabled="otpRequestLoading"
            >
              {{ otpRequestLoading ? '...' : 'Send' }}
            </button>
          </form>

          <div class="otp-divider">
            <span>Verification Code</span>
          </div>

          <form class="otp-verify-form" @submit.prevent="handleOtpLogin">
            <div class="otp-inputs" @paste="onOtpPaste">
              <input
                v-for="(_, i) in 6"
                :key="i"
                :ref="
                  (el) => {
                    if (el) otpRefs[i] = el as HTMLInputElement
                  }
                "
                type="text"
                inputmode="numeric"
                pattern="[0-9]"
                maxlength="1"
                class="otp-box"
                :value="otpDigits[i]"
                @input="onOtpInput(i, $event)"
                @keydown="onOtpKeydown(i, $event)"
              />
            </div>
            <div class="helper otp-helper">{{ otpMessage }}</div>
            <button type="submit" :disabled="otpLoginLoading">
              {{ otpLoginLoading ? 'Verifying...' : 'Login with Code' }}
            </button>
          </form>

          <div class="forgot-wrap">
            <button type="button" class="text-link" @click="showPanel('login')">
              Back to Password Login
            </button>
          </div>
        </div>

        <!-- Two-Factor Panel -->
        <div v-show="currentPanel === 'twoFactor'" class="auth-panel active">
          <h2>Two-Factor Verification</h2>
          <h3>Enter the 6-digit code for {{ twoFactorUsername }}</h3>

          <form @submit.prevent="handleTwoFactor">
            <div class="otp-inputs" @paste="onTwoFactorPaste">
              <input
                v-for="(_, i) in 6"
                :key="i"
                :ref="
                  (el) => {
                    if (el) twoFactorRefs[i] = el as HTMLInputElement
                  }
                "
                type="text"
                inputmode="numeric"
                maxlength="1"
                class="otp-box"
                :value="twoFactorDigits[i]"
                @input="onTwoFactorInput(i, $event)"
                @keydown="onTwoFactorKeydown(i, $event)"
              />
            </div>
            <div class="helper">{{ twoFactorMessage }}</div>
            <button type="submit" :disabled="twoFactorLoading">
              {{ twoFactorLoading ? 'Verifying...' : 'Verify and Login' }}
            </button>
          </form>

          <div class="forgot-wrap">
            <button type="button" class="text-link" @click="showPanel('login')">
              Use another login method
            </button>
          </div>
        </div>

        <!-- Register Panel -->
        <div v-show="currentPanel === 'register'" class="auth-panel active">
          <h2>Create Account</h2>
          <h3>Register with your XJTLU email, verify it.</h3>

          <form class="register-form" @submit.prevent="handleRegister">
            <div class="register-intro full-span">
              <div
                class="register-role-chip"
                :class="
                  inferredRegisterRole
                    ? inferredRegisterRole.toLowerCase()
                    : 'pending'
                "
              >
                {{ registerRoleLabel }}
              </div>
              <p>{{ registerRoleHint }}</p>
            </div>

            <div class="form-field">
              <label class="inline-label">Full Name</label>
              <input
                v-model="regFullName"
                type="text"
                placeholder="Your full name"
                required
              />
            </div>

            <div class="form-field">
              <label class="inline-label">Email</label>
              <input
                v-model="regEmail"
                type="email"
                placeholder="name@student.xjtlu.edu.cn"
                required
              />
            </div>

            <div class="form-field">
              <label class="inline-label">Username</label>
              <input
                v-model="regUsername"
                type="text"
                placeholder="Choose a username"
                required
              />
            </div>

            <div class="form-field">
              <label class="inline-label">Password</label>
              <input
                v-model="regPassword"
                type="password"
                placeholder="Create a password"
                required
              />
            </div>

            <div class="field-group field-group-otp">
              <div class="form-field">
                <label class="inline-label">Email Verification Code</label>
                <input
                  v-model="regOtp"
                  type="text"
                  inputmode="numeric"
                  maxlength="6"
                  placeholder="Enter 6-digit code"
                  required
                />
              </div>
              <button
                type="button"
                class="otp-send-btn register-otp-btn"
                :disabled="registerOtpSending || !canSendRegisterOtp"
                @click="handleSendRegisterOtp"
              >
                {{
                  registerOtpSending
                    ? 'Sending...'
                    : registerOtpCountdown > 0
                      ? `${registerOtpCountdown}s`
                      : 'Send Code'
                }}
              </button>
            </div>

            <div class="register-profile-header full-span">
              <strong>
                {{
                  inferredRegisterRole === 'TEACHER'
                    ? 'Teacher Profile'
                    : inferredRegisterRole === 'STUDENT'
                      ? 'Student Profile'
                      : 'Profile Details'
                }}
              </strong>
              <span>
                {{
                  inferredRegisterRole === 'TEACHER'
                    ? 'These fields are required for teacher emails.'
                    : inferredRegisterRole === 'STUDENT'
                      ? 'These fields are required for student emails.'
                      : 'Enter a valid XJTLU email first to unlock the correct required fields.'
                }}
              </span>
            </div>

            <div
              v-show="inferredRegisterRole === 'STUDENT'"
              class="field-group"
            >
              <div class="form-field">
                <label class="inline-label">Student No</label>
                <input
                  v-model="regStudentNo"
                  type="text"
                  placeholder="Student number"
                  :required="inferredRegisterRole === 'STUDENT'"
                />
              </div>
              <div class="form-field">
                <label class="inline-label">Programme</label>
                <input
                  v-model="regProgramme"
                  type="text"
                  placeholder="Programme"
                  :required="inferredRegisterRole === 'STUDENT'"
                />
              </div>
              <div class="form-field">
                <label class="inline-label">Enrollment Date</label>
                <input
                  v-model="regEntryDate"
                  type="date"
                  :required="inferredRegisterRole === 'STUDENT'"
                />
              </div>
              <div class="form-field">
                <label class="inline-label">Phone</label>
                <input
                  v-model="regPhone"
                  type="text"
                  placeholder="Optional contact number"
                />
              </div>
              <div class="form-field full-span">
                <label class="inline-label">Interests</label>
                <input
                  v-model="regInterests"
                  type="text"
                  placeholder="Optional interests or research topics"
                />
              </div>
            </div>

            <div
              v-show="inferredRegisterRole === 'TEACHER'"
              class="field-group"
            >
              <div class="form-field">
                <label class="inline-label">Staff No</label>
                <input
                  v-model="regStaffNo"
                  type="text"
                  placeholder="Staff number"
                  :required="inferredRegisterRole === 'TEACHER'"
                />
              </div>
              <div class="form-field">
                <label class="inline-label">Department</label>
                <input
                  v-model="regDepartment"
                  type="text"
                  placeholder="Department"
                  :required="inferredRegisterRole === 'TEACHER'"
                />
              </div>
              <div class="form-field">
                <label class="inline-label">Title</label>
                <input
                  v-model="regTitle"
                  type="text"
                  placeholder="Title"
                  :required="inferredRegisterRole === 'TEACHER'"
                />
              </div>
              <div class="form-field">
                <label class="inline-label">Office</label>
                <input
                  v-model="regOffice"
                  type="text"
                  placeholder="Optional office location"
                />
              </div>
              <div class="form-field full-span">
                <label class="inline-label">Research Area</label>
                <input
                  v-model="regResearchArea"
                  type="text"
                  placeholder="Optional research area"
                />
              </div>
            </div>

            <div v-if="registerSuccess" class="general-success">
              {{ registerSuccess }}
            </div>
            <div class="general-error">{{ registerError }}</div>
            <button class="full-span" type="submit" :disabled="registerLoading">
              {{ registerLoading ? 'Creating Account...' : 'Create Account' }}
            </button>
          </form>
        </div>

        <!-- Reset Password Panel -->
        <div v-show="currentPanel === 'reset'" class="auth-panel active">
          <h2>Reset Password</h2>
          <h3>
            {{
              hasResetToken
                ? 'Set a new password for your account'
                : 'Enter your email to receive a reset link'
            }}
          </h3>

          <form v-if="!hasResetToken" @submit.prevent="handleResetRequest">
            <input
              v-model="resetEmail"
              type="email"
              placeholder="Email"
              required
            />
            <div class="helper">{{ resetRequestMessage }}</div>
            <button type="submit" :disabled="resetRequestLoading">
              {{ resetRequestLoading ? 'Sending...' : 'Send Reset Link' }}
            </button>
          </form>

          <form v-else @submit.prevent="handleResetConfirm">
            <input
              v-model="resetPassword"
              type="password"
              placeholder="New Password"
              required
            />
            <input
              v-model="resetConfirmPassword"
              type="password"
              placeholder="Confirm New Password"
              required
            />
            <div class="helper">{{ resetConfirmMessage }}</div>
            <button type="submit" :disabled="resetConfirmLoading">
              {{ resetConfirmLoading ? 'Setting...' : 'Set New Password' }}
            </button>
          </form>

          <div class="forgot-wrap">
            <button type="button" class="text-link" @click="showPanel('login')">
              Back to Password Login
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

//css
<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap');

* {
  box-sizing: border-box;
}

:root {
  --bg: var(--bg);
  --deep: var(--deep);
  --mid: var(--mid);
  --panel: #ffffff;
  --text: var(--text);
  --muted: var(--muted);
  --accent: var(--accent);
  --green: var(--green);
  --danger: #d93025;
  --border: rgba(28, 27, 51, 0.1);
}

.guide-page {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) minmax(360px, 420px);
  gap: 52px;
  align-items: center;
  min-height: 100vh;
  width: min(100%, 1040px);
  margin: 0 auto;
  padding: 56px 28px 52px;
  font-family: 'Segoe UI', 'Microsoft YaHei', Arial, sans-serif;
  color: var(--text);
  background: var(--bg);
}

.guide-copy {
  max-width: 560px;
  transform: translateY(-6px);
}

.guide-eyebrow {
  margin: 0 0 14px;
  color: var(--deep);
  font-size: 0.78rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.guide-copy h1 {
  margin: 0;
  color: var(--text);
  font-size: 2.6rem;
  font-weight: 800;
  line-height: 1.08;
}

.guide-text {
  max-width: 500px;
  margin: 18px 0 0;
  color: var(--muted);
  font-size: 1rem;
  line-height: 1.7;
}

.guide-points {
  display: grid;
  gap: 10px;
  margin-top: 28px;
}

.guide-points div {
  display: grid;
  grid-template-columns: 92px 1fr;
  gap: 14px;
  align-items: start;
  padding: 12px 0;
  border-top: 1px solid var(--border);
}

.guide-points strong {
  color: var(--text);
  font-size: 0.9rem;
}

.guide-points span {
  color: var(--muted);
  font-size: 0.9rem;
  line-height: 1.5;
}

.guide-visual {
  display: flex;
  justify-content: center;
  position: relative;
  transform: translateY(26px);
}

.login-card {
  width: min(100%, 336px);
  padding: 22px 20px;
  border: 1px solid var(--border);
  border-radius: 18px;
  background: var(--panel);
}

.auth-panel-card {
  position: relative;
  width: min(100%, 372px);
  max-height: calc(100vh - 112px);
  overflow-y: auto;
  z-index: 1;
}

.auth-panel-card.register-mode {
  width: min(100%, 430px);
}

.auth-tabs {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
  margin-bottom: 16px;
  padding: 4px;
  border-radius: 12px;
  background: var(--bg);
}

.auth-tab {
  min-height: 32px;
  padding: 6px 8px;
  border: none;
  border-radius: 9px;
  background: transparent;
  color: var(--muted);
  font-size: 0.82rem;
  font-weight: 700;
  cursor: pointer;
}

.auth-tab:hover {
  background: rgba(90, 43, 152, 0.08);
  color: var(--deep);
}

.auth-tab.active {
  background: var(--panel);
  color: var(--deep);
}

h2 {
  margin: 0 0 8px;
  font-size: 1.38rem;
  font-weight: 700;
  color: var(--text);
}

h3 {
  margin: 0 0 6px;
  font-size: 0.88rem;
  font-weight: 400;
  color: var(--muted);
}

form {
  display: grid;
  gap: 10px;
}

input,
select {
  width: 100%;
  min-height: 38px;
  padding: 7px 10px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: #fff;
  color: var(--text);
  font: inherit;
  outline: none;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

input:focus,
select:focus {
  border-color: var(--deep);
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.14);
}

label,
.inline-label {
  display: block;
  color: var(--muted);
  font-size: 0.82rem;
  font-weight: 600;
}

.inline-label {
  margin: 2px 0 2px;
}

button,
.register-link {
  width: 100%;
  min-height: 38px;
  padding: 8px 13px;
  border: none;
  border-radius: 12px;
  background: var(--deep);
  color: #fff;
  cursor: pointer;
  display: block;
  font: inherit;
  font-weight: 700;
  line-height: 1.2;
  text-align: center;
  text-decoration: none;
  transition:
    background-color 0.2s ease,
    transform 0.2s ease;
}

button:hover {
  background: var(--mid);
}

button:active {
  transform: translateY(1px);
}

button:disabled {
  background: #c4c4d8;
  cursor: not-allowed;
  transform: none;
}

.text-link {
  width: auto;
  min-height: 0;
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  color: var(--deep);
  display: inline;
  font: inherit;
  font-size: 0.82rem;
  font-weight: 600;
  text-align: left;
  cursor: pointer;
}

.text-link:hover {
  background: transparent;
  text-decoration: underline;
}

.forgot-wrap {
  margin: -2px 0 0;
  text-align: right;
}

.otp-btn {
  width: 100%;
  min-height: 38px;
  margin-top: 6px;
  padding: 8px 13px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #fff;
  color: var(--deep);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.otp-btn:hover {
  border-color: var(--deep);
  box-shadow: 0 2px 8px rgba(90, 43, 152, 0.1);
}

.otp-send-row {
  display: flex;
  gap: 8px;
}

.otp-send-row input {
  flex: 1;
  min-width: 0;
}

.otp-send-btn {
  width: auto !important;
  min-width: 72px;
  padding: 0 16px !important;
  white-space: nowrap;
}

.otp-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 12px 0 8px;
}

.otp-divider::before,
.otp-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border);
}

.otp-divider span {
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--muted);
  white-space: nowrap;
}

.otp-verify-form {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.otp-inputs {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.otp-box {
  width: 42px;
  height: 48px;
  text-align: center;
  font-size: 1.2rem;
  font-weight: 700;
  border: 1.5px solid var(--border);
  border-radius: 10px;
  background: #fff;
  color: var(--text);
  outline: none;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.otp-box:focus {
  border-color: var(--deep);
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.14);
}

.helper {
  min-height: 16px;
  margin: -6px 0 8px;
  color: var(--danger);
  font-size: 0.78rem;
}

.otp-helper {
  min-height: 36px;
  margin: 0;
  line-height: 1.45;
}

.general-error {
  min-height: 18px;
  margin-bottom: 10px;
  color: var(--danger);
  font-size: 0.85rem;
}

.general-success {
  margin-bottom: 10px;
  padding: 10px 12px;
  border: 1px solid rgba(47, 197, 168, 0.24);
  border-radius: 12px;
  background: rgba(47, 197, 168, 0.08);
  color: #177b68;
  font-size: 0.84rem;
}

.register-form {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.register-intro {
  padding: 14px;
  border: 1px solid rgba(90, 43, 152, 0.12);
  border-radius: 16px;
  background:
    radial-gradient(
      circle at top right,
      rgba(36, 179, 255, 0.12),
      transparent 35%
    ),
    linear-gradient(180deg, rgba(90, 43, 152, 0.05), rgba(90, 43, 152, 0.02));
}

.register-intro p {
  margin: 10px 0 0;
  color: var(--muted);
  font-size: 0.82rem;
  line-height: 1.55;
}

.register-role-chip {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 0.76rem;
  font-weight: 800;
  letter-spacing: 0.03em;
  text-transform: uppercase;
}

.register-role-chip.pending {
  background: rgba(90, 43, 152, 0.08);
  color: var(--deep);
}

.register-role-chip.student {
  background: rgba(36, 179, 255, 0.12);
  color: #0a699c;
}

.register-role-chip.teacher {
  background: rgba(246, 166, 61, 0.16);
  color: #8c5a0d;
}

.form-field {
  min-width: 0;
}

.field-group-otp {
  grid-template-columns: minmax(0, 1fr) auto !important;
  align-items: end;
}

.register-otp-btn {
  min-width: 108px;
}

.register-profile-header {
  display: grid;
  gap: 3px;
  padding-top: 4px;
}

.register-profile-header strong {
  font-size: 0.86rem;
  color: var(--text);
}

.register-profile-header span {
  color: var(--muted);
  font-size: 0.78rem;
}

.register-form .field-group {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.register-form .general-error,
.register-form .field-group,
.register-form .full-span {
  grid-column: 1 / -1;
}

@media (max-width: 520px) {
  .guide-page {
    grid-template-columns: 1fr;
    gap: 26px;
    padding: 42px 18px 32px;
  }

  .guide-copy {
    transform: none;
  }

  .guide-visual {
    justify-content: center;
    transform: none;
  }

  .auth-panel-card {
    max-height: none;
  }

  .auth-panel-card.register-mode {
    width: min(100%, 420px);
  }

  .guide-copy h1 {
    font-size: 2rem;
  }

  .guide-text {
    font-size: 0.94rem;
  }

  .guide-points div {
    grid-template-columns: 1fr;
    gap: 4px;
  }

  .login-card {
    padding: 24px 20px;
  }

  .register-form .field-group,
  .field-group-otp {
    grid-template-columns: 1fr !important;
  }

  .register-otp-btn {
    width: 100% !important;
  }
}
</style>
