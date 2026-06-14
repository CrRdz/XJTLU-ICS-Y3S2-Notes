import { useRouter } from 'vue-router'

function getPreviousPath(): string {
  const previous = window.history.state?.back
  return typeof previous === 'string' ? previous : ''
}

export function useSmartBack(fallbackPath: string, sectionPrefix?: string) {
  const router = useRouter()

  function goBack() {
    const previousPath = getPreviousPath()
    const canReturn =
      previousPath.startsWith('/') &&
      (!sectionPrefix || previousPath.startsWith(sectionPrefix))

    if (canReturn) {
      router.back()
      return
    }

    router.push(fallbackPath)
  }

  return { goBack }
}
