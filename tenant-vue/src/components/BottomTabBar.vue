<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
const tabs = [
  { key: 'home', label: '首页', to: '/home' },
  { key: 'wishlist', label: '心愿单', to: '/wishlist' },
  { key: 'messages', label: '消息', to: '/messages' },
  { key: 'profile', label: '我的', to: '/profile' }
]
</script>

<template>
  <nav class="ios-tabbar" aria-label="主导航">
    <RouterLink
      v-for="tab in tabs"
      :key="tab.key"
      :to="tab.to"
      class="ios-tab-item"
      :class="{ active: route.meta.tab === tab.key }"
    >
      <!-- 首页 Icon -->
      <template v-if="tab.key === 'home'">
        <svg class="icon-active" viewBox="0 0 24 24">
          <path d="M4 10 L12 3 L20 10 L20 20 A2 2 0 0 1 18 22 L6 22 A2 2 0 0 1 4 20 Z" />
          <line x1="12" y1="13" x2="12" y2="17" />
        </svg>
        <svg class="icon-inactive" viewBox="0 0 24 24">
          <path d="M4 10 L12 3 L20 10 L20 20 A2 2 0 0 1 18 22 L6 22 A2 2 0 0 1 4 20 Z" />
          <line x1="12" y1="13" x2="12" y2="17" />
        </svg>
      </template>

      <!-- 心愿单 Icon -->
      <template v-else-if="tab.key === 'wishlist'">
        <svg class="icon-active" viewBox="0 0 24 24">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
        </svg>
        <svg class="icon-inactive" viewBox="0 0 24 24">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
        </svg>
      </template>

      <!-- 消息 Icon -->
      <template v-else-if="tab.key === 'messages'">
        <svg class="icon-active" viewBox="0 0 24 24">
          <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
        </svg>
        <svg class="icon-inactive" viewBox="0 0 24 24">
          <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
        </svg>
      </template>

      <!-- 我的 Icon -->
      <template v-else-if="tab.key === 'profile'">
        <svg class="icon-active" viewBox="0 0 24 24">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
        <svg class="icon-inactive" viewBox="0 0 24 24">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
      </template>

      <span>{{ tab.label }}</span>
    </RouterLink>
  </nav>
</template>
