import { ref } from 'vue'

// Toast
export interface ToastItem {
  id: number
  message: string
  type: 'info' | 'success' | 'error' | 'warning'
}

export const toasts = ref<ToastItem[]>([])
let toastId = 0

function showToast(
  message: string,
  type: ToastItem['type'] = 'info',
  duration = 2600,
) {
  const id = ++toastId
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }, duration)
}

export const toast = {
  info: (msg: string) => showToast(msg, 'info'),
  success: (msg: string) => showToast(msg, 'success'),
  error: (msg: string) => showToast(msg, 'error'),
  warning: (msg: string) => showToast(msg, 'warning'),
}

// Confirm dialog
export const confirmState = ref<{
  show: boolean
  message: string
  title: string
  resolve: ((value: boolean) => void) | null
}>({ show: false, message: '', title: '', resolve: null })

export function confirm(message: string, title = 'Confirm'): Promise<boolean> {
  return new Promise((resolve) => {
    confirmState.value = { show: true, message, title, resolve }
  })
}

export function handleConfirm(result: boolean) {
  confirmState.value.resolve?.(result)
  confirmState.value.show = false
}
