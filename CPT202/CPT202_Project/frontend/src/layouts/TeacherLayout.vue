<script setup lang="ts">
import AppSidebar from '../components/AppSidebar.vue'

const navItems = [
  { to: '/teacher/projects', icon: 'bi bi-kanban', label: 'My Projects' },
  {
    to: '/teacher/dashboard',
    icon: 'bi bi-file-earmark-check',
    label: 'Review Requests',
  },
  {
    to: '/teacher/analytics',
    icon: 'bi bi-bar-chart-line',
    label: 'Analytics',
  },
  { to: '/teacher/history', icon: 'bi bi-clock-history', label: 'History' },
  {
    to: '/teacher/notifications',
    icon: 'bi bi-chat-dots',
    label: 'Notifications',
  },
  { to: '/teacher/settings', icon: 'bi bi-person-gear', label: 'Settings' },
]
</script>

<template>
  <AppSidebar :nav-items="navItems">
    <router-view />
  </AppSidebar>
</template>
