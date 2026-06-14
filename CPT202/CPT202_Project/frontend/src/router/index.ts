import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // Auth
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/auth/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/auth/LoginView.vue'),
    },
    {
      path: '/forgot-password',
      name: 'ForgotPassword',
      component: () => import('../views/auth/LoginView.vue'),
    },
    {
      path: '/auth/forgot-password',
      name: 'AuthForgotPassword',
      component: () => import('../views/auth/LoginView.vue'),
    },
    {
      path: '/reset-password',
      name: 'ResetPassword',
      component: () => import('../views/auth/LoginView.vue'),
    },

    // Student
    {
      path: '/student',
      component: () => import('../layouts/StudentLayout.vue'),
      children: [
        { path: '', redirect: '/student/dashboard' },
        {
          path: 'dashboard',
          name: 'StudentDashboard',
          component: () => import('../views/student/StudentDashboard.vue'),
        },
        {
          path: 'projects',
          name: 'StudentProjects',
          component: () => import('../views/student/StudentProjectList.vue'),
        },
        {
          path: 'projects/:id',
          name: 'StudentProjectDetail',
          component: () => import('../views/student/StudentProjectDetail.vue'),
        },
        {
          path: 'history',
          name: 'StudentHistory',
          component: () => import('../views/student/StudentRequestHistory.vue'),
        },
        {
          path: 'notifications',
          name: 'StudentNotifications',
          component: () => import('../views/student/StudentNotifications.vue'),
        },
        {
          path: 'profile',
          name: 'StudentProfile',
          component: () => import('../views/student/StudentProfile.vue'),
        },
      ],
    },

    // Teacher
    {
      path: '/teacher',
      component: () => import('../layouts/TeacherLayout.vue'),
      children: [
        { path: '', redirect: '/teacher/projects' },
        {
          path: 'projects',
          name: 'TeacherProjects',
          component: () => import('../views/teacher/TeacherProjects.vue'),
        },
        {
          path: 'create-project',
          name: 'TeacherCreateProject',
          component: () => import('../views/teacher/TeacherCreateProject.vue'),
        },
        {
          path: 'dashboard',
          name: 'TeacherDashboard',
          component: () => import('../views/teacher/TeacherDashboard.vue'),
        },
        {
          path: 'analytics',
          name: 'TeacherAnalytics',
          component: () => import('../views/teacher/TeacherAnalytics.vue'),
        },
        {
          path: 'request/:id',
          name: 'TeacherRequestDetail',
          component: () => import('../views/teacher/TeacherRequestDetail.vue'),
        },
        {
          path: 'history',
          name: 'TeacherHistory',
          component: () => import('../views/teacher/TeacherHistory.vue'),
        },
        {
          path: 'notifications',
          name: 'TeacherNotifications',
          component: () => import('../views/teacher/TeacherNotifications.vue'),
        },
        {
          path: 'settings',
          name: 'TeacherSettings',
          component: () => import('../views/teacher/TeacherSettings.vue'),
        },
      ],
    },

    // Admin
    {
      path: '/admin',
      component: () => import('../layouts/AdminLayout.vue'),
      children: [
        { path: '', redirect: '/admin/projects' },
        {
          path: 'projects',
          name: 'AdminProjects',
          component: () => import('../views/admin/AdminProjects.vue'),
        },
        {
          path: 'analytics',
          name: 'AdminAnalytics',
          component: () => import('../views/admin/AdminAnalytics.vue'),
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('../views/admin/AdminUsers.vue'),
        },
        {
          path: 'requests',
          name: 'AdminRequests',
          component: () => import('../views/admin/AdminRequests.vue'),
        },
        {
          path: 'request-history',
          name: 'AdminRequestHistory',
          component: () => import('../views/admin/AdminRequestHistory.vue'),
        },
        {
          path: 'categories',
          name: 'AdminCategories',
          component: () => import('../views/admin/AdminCategories.vue'),
        },
        {
          path: 'tags',
          name: 'AdminTags',
          component: () => import('../views/admin/AdminTags.vue'),
        },
        {
          path: 'settings',
          name: 'AdminSettings',
          component: () => import('../views/admin/AdminSettings.vue'),
        },
      ],
    },

    // Default redirect
    { path: '/', redirect: '/login' },
  ],
})

export default router
