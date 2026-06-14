type DateValue = string | number | number[] | null | undefined

function pad(value: number): string {
  return String(value).padStart(2, '0')
}

function isRealDate(year: number, month: number, day: number): boolean {
  const date = new Date(year, month - 1, day)
  return (
    date.getFullYear() === year &&
    date.getMonth() === month - 1 &&
    date.getDate() === day
  )
}

function todayDateOnly(): string {
  const today = new Date()
  return `${today.getFullYear()}-${pad(today.getMonth() + 1)}-${pad(today.getDate())}`
}

export function normalizeDateOnly(value: DateValue): string {
  if (!value) return ''

  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day] = value.map(Number)
    if (!isRealDate(year, month, day)) return ''
    return `${year}-${pad(month)}-${pad(day)}`
  }

  const match = String(value)
    .trim()
    .match(/^(\d{4})-(\d{2})-(\d{2})/)
  if (!match) return ''

  const year = Number(match[1])
  const month = Number(match[2])
  const day = Number(match[3])
  if (!isRealDate(year, month, day)) return ''

  return `${match[1]}-${match[2]}-${match[3]}`
}

export function isValidDateOnly(value: DateValue): boolean {
  return normalizeDateOnly(value) !== ''
}

export function isFutureDateOnly(value: DateValue): boolean {
  const normalized = normalizeDateOnly(value)
  return normalized !== '' && normalized > todayDateOnly()
}

export function parseDateTime(value: DateValue): Date | null {
  if (!value) return null

  if (Array.isArray(value)) {
    const dateOnly = normalizeDateOnly(value)
    if (!dateOnly) return null
    const [year, month, day] = dateOnly.split('-').map(Number)
    return new Date(year, month - 1, day)
  }

  const text = String(value).trim()
  if (!text) return null

  const dateOnly = normalizeDateOnly(text)
  if (dateOnly && !/[T\s]\d{2}:\d{2}/.test(text)) {
    const [year, month, day] = dateOnly.split('-').map(Number)
    return new Date(year, month - 1, day)
  }

  const normalized = text.includes('T') ? text : text.replace(' ', 'T')
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

export function getDateTimeMillis(value: DateValue): number {
  return parseDateTime(value)?.getTime() || 0
}

export function formatDateTime(value: DateValue): string {
  const date = parseDateTime(value)
  if (!date) return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export function formatShortDate(value: DateValue): string {
  const date = parseDateTime(value)
  if (!date) return value ? String(value).slice(0, 10) : '-'
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

export function toDateTimeLocalValue(value: DateValue): string {
  const date = parseDateTime(value)
  if (!date) return ''
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export function toIsoLocalDateTime(value: DateValue): string {
  const date = parseDateTime(value)
  if (!date) return ''
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}
