import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from './services/api'

import LoginView from './views/LoginView.vue'
import LayoutView from './views/LayoutView.vue'
import DashboardView from './views/DashboardView.vue'
import PropertiesView from './views/PropertiesView.vue'
import ListingsView from './views/ListingsView.vue'

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', component: LoginView },
    {
      path: '/',
      component: LayoutView,
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', component: DashboardView },
        { path: 'properties', component: PropertiesView },
        { path: 'listings', component: ListingsView }
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.path !== '/login' && !isAuthenticated()) return '/login'
  if (to.path === '/login' && isAuthenticated()) return '/'
})
