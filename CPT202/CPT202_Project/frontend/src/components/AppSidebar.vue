<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { clearAuth } from '../utils/api'

interface NavItem {
  to: string
  icon: string
  label: string
}

defineProps<{
  navItems: NavItem[]
}>()

const router = useRouter()
const route = useRoute()
const expanded = ref(false)

function logout() {
  clearAuth()
  router.push('/login')
}

function isActive(path: string): boolean {
  return route.path === path || route.path.startsWith(path + '/')
}
</script>

<template>
  <div class="layout" :class="{ 'sidebar-expanded': expanded }">
    <aside
      class="sidebar"
      @mouseenter="expanded = true"
      @mouseleave="expanded = false"
    >
      <nav class="nav-group">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav-item"
          :class="{ active: isActive(item.to) }"
        >
          <i :class="item.icon" class="nav-icon"></i>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <button class="logout-btn" @click="logout">
          <i class="bi bi-box-arrow-right nav-icon"></i>
          <span class="nav-label">Logout</span>
        </button>
      </div>
    </aside>

    <main class="main">
      <slot />
    </main>
  </div>
</template>

<style scoped>
@import url('https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css');

.layout {
  min-height: 100vh;
}

.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  background: #ffffff;
  padding: 24px 12px;
  color: var(--text);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  width: 72px;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;
}

.layout.sidebar-expanded .sidebar {
  width: 220px;
}

.nav-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  justify-content: center;
  padding: 48px 0;
}

.nav-item {
  border: none;
  background: transparent;
  color: var(--text);
  font-size: 0.95rem;
  padding: 12px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s ease;
  display: flex;
  align-items: center;
  gap: 14px;
  justify-content: center;
  text-decoration: none;
  white-space: nowrap;
  outline: none;
}

.nav-item:focus {
  outline: none;
}

.nav-item:focus-visible {
  background: rgba(28, 27, 51, 0.06);
}

.layout.sidebar-expanded .nav-item {
  justify-content: flex-start;
}

.nav-item:hover {
  background: rgba(28, 27, 51, 0.06);
}

.nav-item.active {
  font-weight: 600;
}

.nav-item.active .nav-icon {
  color: var(--deep);
}

.nav-icon {
  font-size: 1.25rem;
  min-width: 24px;
  text-align: center;
  flex-shrink: 0;
}

.nav-label {
  opacity: 0;
  max-width: 0;
  overflow: hidden;
  transition:
    opacity 0.25s ease 0.05s,
    max-width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
}

.layout.sidebar-expanded .nav-label {
  opacity: 1;
  max-width: 150px;
}

.sidebar-footer {
  margin-top: auto;
}

.logout-btn {
  width: 100%;
  border: none;
  background: transparent;
  color: var(--muted);
  font-size: 0.95rem;
  padding: 12px 16px;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 14px;
  justify-content: center;
  font-family: inherit;
  transition:
    background 0.15s ease,
    color 0.15s ease;
  white-space: nowrap;
  outline: none;
}

.logout-btn:focus {
  outline: none;
}

.logout-btn:focus-visible {
  background: rgba(28, 27, 51, 0.06);
}

.layout.sidebar-expanded .logout-btn {
  justify-content: flex-start;
}

.logout-btn:hover {
  background: rgba(28, 27, 51, 0.06);
  color: var(--text);
}

.main {
  margin-left: 72px;
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 32px;
  background: var(--bg);
  min-height: 100vh;
  overflow-x: hidden;
}

.layout.sidebar-expanded .main {
  margin-left: 220px;
}

@media (max-width: 960px) {
  .sidebar {
    padding: 16px 10px;
  }

  .layout.sidebar-expanded .sidebar {
    width: 72px;
  }

  .main {
    margin-left: 72px;
    padding: 20px 16px 20px 20px;
  }

  .layout.sidebar-expanded .main {
    margin-left: 72px;
  }

  .sidebar-footer {
    margin-top: 0;
  }

  .nav-item,
  .logout-btn {
    padding-left: 0;
    padding-right: 0;
  }

  .nav-label {
    display: none;
  }

  .layout.sidebar-expanded .nav-item,
  .layout.sidebar-expanded .logout-btn {
    justify-content: center;
  }

  .nav-group {
    justify-content: flex-start;
    padding: 24px 0;
  }
}
</style>
