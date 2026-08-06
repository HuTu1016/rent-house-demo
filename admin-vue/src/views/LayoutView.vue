<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAdminStore } from '../stores/admin'

const route = useRoute()
const store = useAdminStore()

const isCollapsed = ref(false)
const showUserDropdown = ref(false)
const userMenuRef = ref<HTMLElement | null>(null)

const menus = [
  { 
    path: '/dashboard', 
    label: '数据概览', 
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>` 
  },
  { 
    path: '/properties', 
    label: '房产与单元', 
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M9 8h1"/><path d="M9 12h1"/><path d="M9 16h1"/><path d="M14 8h1"/><path d="M14 12h1"/><path d="M14 16h1"/><path d="M5 21V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16"/></svg>` 
  },
  { 
    path: '/listings', 
    label: '房源运营', 
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>` 
  },
]

const currentMenu = computed(() => menus.find(m => route.path.startsWith(m.path))?.label || '概览')

function handleClickOutside(event: MouseEvent) {
  if (userMenuRef.value && !userMenuRef.value.contains(event.target as Node)) {
    showUserDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="layout">
    <!-- 宝塔风格深色侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <div v-show="!isCollapsed" class="logo-brand">
          <span class="brand-badge">租</span>
          <span class="logo-title">租房管理后台</span>
        </div>
        <button class="collapse-btn" @click="isCollapsed = !isCollapsed" :title="isCollapsed ? '展开侧边栏' : '收起侧边栏'">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <line x1="9" y1="3" x2="9" y2="21"/>
          </svg>
        </button>
      </div>
      <nav class="menu">
        <RouterLink v-for="menu in menus" :key="menu.path" :to="menu.path" class="menu-item" active-class="active" :title="isCollapsed ? menu.label : ''">
          <span class="icon" v-html="menu.icon"></span>
          <span v-show="!isCollapsed" class="menu-label">{{ menu.label }}</span>
        </RouterLink>
      </nav>
    </aside>

    <!-- 主主体区域 -->
    <main class="main-content" :style="{ marginLeft: isCollapsed ? '64px' : '230px' }">
      <header class="header glass-header">
        <h2>{{ currentMenu }}</h2>

        <!-- 右上角用户信息与下拉菜单 -->
        <div class="user-menu-wrapper" ref="userMenuRef">
          <div class="user-info" @click="showUserDropdown = !showUserDropdown">
            <img src="https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=80" alt="Avatar">
            <span>管理员</span>
            <svg class="arrow-icon" :class="{ open: showUserDropdown }" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </div>

          <!-- 用户下拉浮窗 -->
          <Transition name="dropdown">
            <div v-if="showUserDropdown" class="user-dropdown card">
              <div class="user-dropdown-header">
                <div class="user-name">超级管理员</div>
                <div class="user-role">房源中介 / 业主权限</div>
              </div>
              <div class="divider"></div>
              <button @click="store.logout" class="dropdown-item logout-btn">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                  <polyline points="16 17 21 12 16 7"/>
                  <line x1="21" y1="12" x2="9" y2="12"/>
                </svg>
                <span>退出登录</span>
              </button>
            </div>
          </Transition>
        </div>
      </header>

      <div class="page-container">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: var(--apple-bg);
}

/* 宝塔风深暗色侧边栏 */
.sidebar {
  width: 230px;
  background: var(--sidebar-bg);
  border-right: 1px solid var(--sidebar-border);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
  transition: width 0.2s ease;
  user-select: none;
}

.sidebar.collapsed {
  width: 64px;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid var(--sidebar-border);
}

.sidebar.collapsed .logo {
  justify-content: center;
  padding: 0;
}

.logo-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-badge {
  background: var(--accent-green);
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-title {
  color: #ffffff;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: -0.2px;
  white-space: nowrap;
}

.collapse-btn {
  background: transparent;
  color: var(--sidebar-text);
  padding: 6px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
}

.menu {
  flex: 1;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: var(--sidebar-text);
  font-weight: 500;
  font-size: 14px;
  transition: all 0.15s;
  background: transparent;
  white-space: nowrap;
  text-decoration: none;
  border-left: 3px solid transparent;
}

.sidebar.collapsed .menu-item {
  justify-content: center;
  padding: 12px 0;
  border-left: none;
}

.menu-item .icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-item:hover {
  background: var(--sidebar-hover);
  color: #ffffff;
}

.menu-item.active {
  background: var(--sidebar-active-bg);
  color: #ffffff;
  font-weight: 600;
  border-left-color: var(--accent-green);
}

.menu-item.active .icon {
  color: var(--accent-green);
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.2s ease;
}

.header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
}

.header h2 {
  font-size: 17px;
  font-weight: 700;
  margin: 0;
  color: var(--apple-text);
}

/* 右上角用户下拉 */
.user-menu-wrapper {
  position: relative;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  font-size: 14px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 8px;
  transition: background 0.15s;
  user-select: none;
}

.user-info:hover {
  background: rgba(0, 0, 0, 0.04);
}

.user-info img {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--apple-border);
}

.arrow-icon {
  transition: transform 0.2s ease;
  color: var(--apple-muted);
}

.arrow-icon.open {
  transform: rotate(180deg);
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 200px;
  padding: 12px;
  z-index: 1000;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.user-dropdown-header {
  padding: 4px 8px 8px;
}

.user-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--apple-text);
}

.user-role {
  font-size: 12px;
  color: var(--apple-muted);
  margin-top: 2px;
}

.divider {
  height: 1px;
  background: var(--apple-border);
  margin: 8px 0;
}

.dropdown-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  background: transparent;
  color: var(--apple-text);
  font-size: 13px;
  font-weight: 500;
  transition: background 0.15s;
}

.dropdown-item:hover {
  background: #f4f4f5;
}

.logout-btn {
  color: #ef4444;
}

.logout-btn:hover {
  background: #fef2f2;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.15s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.page-container {
  padding: 28px;
  flex: 1;
  overflow-x: hidden;
}
</style>
