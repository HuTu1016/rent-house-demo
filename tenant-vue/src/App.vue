<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BottomTabBar from './components/BottomTabBar.vue'
import { useRentalStore } from './stores/rental'
const route = useRoute(); const store = useRentalStore(); const showTabbar = computed(() => Boolean(route.meta.tab))
</script>
<template><main class="tenant-app"><RouterView /><BottomTabBar v-if="showTabbar" /><Transition name="toast"><div v-if="store.toast" class="toast">{{ store.toast }}</div></Transition></main></template>
