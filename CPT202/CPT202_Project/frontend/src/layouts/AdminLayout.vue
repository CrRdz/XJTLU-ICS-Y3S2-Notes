<script setup lang="ts">
import AppSidebar from '../components/AppSidebar.vue'

const navItems = [
  { to: '/admin/projects', icon: 'bi bi-kanban', label: 'Projects' },
  { to: '/admin/analytics', icon: 'bi bi-bar-chart-line', label: 'Analytics' },
  { to: '/admin/users', icon: 'bi bi-people', label: 'Users' },
  {
    to: '/admin/requests',
    icon: 'bi bi-file-earmark-check',
    label: 'Requests',
  },
  {
    to: '/admin/request-history',
    icon: 'bi bi-clock-history',
    label: 'History',
  },
  { to: '/admin/categories', icon: 'bi bi-folder', label: 'Categories' },
  { to: '/admin/tags', icon: 'bi bi-tags', label: 'Tags' },
  { to: '/admin/settings', icon: 'bi bi-person-gear', label: 'Settings' },
]
</script>

<template>
  <AppSidebar :nav-items="navItems">
    <router-view />
  </AppSidebar>
</template>
