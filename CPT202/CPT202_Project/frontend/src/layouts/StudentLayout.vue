<script setup lang="ts">
import AppSidebar from '../components/AppSidebar.vue'

const navItems = [
  { to: '/student/dashboard', icon: 'bi bi-kanban', label: 'Dashboard' },
  { to: '/student/projects', icon: 'bi bi-search', label: 'Projects' },
  { to: '/student/history', icon: 'bi bi-clock-history', label: 'History' },
  {
    to: '/student/notifications',
    icon: 'bi bi-chat-dots',
    label: 'Notifications',
  },
  { to: '/student/profile', icon: 'bi bi-person-gear', label: 'Profile' },
]
</script>

<template>
  <AppSidebar :nav-items="navItems">
    <router-view />
  </AppSidebar>
</template>
