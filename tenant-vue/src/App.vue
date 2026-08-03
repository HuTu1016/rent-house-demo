<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import BottomTabBar from './components/BottomTabBar.vue'
import { useRentalStore } from './stores/rental'
const route = useRoute(); const store = useRentalStore(); const showTabbar = computed(() => Boolean(route.meta.tab))
onMounted(() => { void store.loadRemote() })
</script>
<template><main class="tenant-app"><div v-if="store.connectionError" class="backend-error"><strong>无法连接业务服务</strong><p>{{ store.connectionError }}</p><small>请确认后端已启动，并在 tenant-vue/.env.local 配置 VITE_API_BASE_URL、VITE_TENANT_MOBILE 和 VITE_TENANT_PASSWORD。</small><button @click="store.loadRemote">重新连接</button></div><RouterView v-else /><BottomTabBar v-if="showTabbar && !store.connectionError" /><Transition name="toast"><div v-if="store.toast" class="toast">{{ store.toast }}</div></Transition></main></template>

<style scoped>
.backend-error { min-height: 100vh; display: grid; place-content: center; gap: 12px; padding: 32px; text-align: center; background: #f5f6f8; color: #252525; }
.backend-error p { margin: 0; color: #d44; }
.backend-error small { color: #777; line-height: 1.6; }
.backend-error button { justify-self: center; border: 0; border-radius: 999px; padding: 10px 22px; background: #ffca05; font-weight: 700; cursor: pointer; }
</style>
