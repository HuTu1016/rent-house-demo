<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ModalShell from '../components/ModalShell.vue'
import HouseCard from '../components/HouseCard.vue'
import { useRentalStore } from '../stores/rental'
const store = useRentalStore(); const route = useRoute(); const router = useRouter(); const compareOpen = ref(false)
const tab = computed(() => route.query.tab === 'history' ? 'history' : 'favorites'); const houses = computed(() => (tab.value === 'history' ? store.history : store.favorites).map(store.getHouse).filter(Boolean))
function change(value: 'favorites' | 'history') { router.replace({ query: value === 'history' ? { tab: value } : {} }) }; function open(id: number) { store.openHouse(id); router.push(`/house/${id}`) }
</script>
<template><section class="page wishlist-page"><header class="page-title"><h1>心愿单</h1></header><div class="two-tabs"><button :class="{ active: tab === 'favorites' }" @click="change('favorites')">收藏房源</button><button :class="{ active: tab === 'history' }" @click="change('history')">浏览记录</button></div><div v-if="houses.length" class="house-list"><HouseCard v-for="house in houses" :key="house!.id" :house="house!" compare-mode :checked="store.compareList.includes(house!.id)" @open="open" @chat="id => router.push(`/messages/${id}`)" @compare="store.toggleCompare" /></div><div v-else class="empty-state">🏠🔍<br/>暂无{{ tab === 'favorites' ? '收藏' : '浏览记录' }}<button @click="router.push('/home')">搜索房源</button></div><div v-if="store.compareList.length" class="compare-bar"><span>已选 {{ store.compareList.length }} 套</span><button @click="compareOpen = true">开始对比</button></div><ModalShell v-if="compareOpen" @close="compareOpen = false"><h2>房源横向对比</h2><div class="compare-grid"><article v-for="id in store.compareList" :key="id"><img :src="store.getHouse(id)?.image" alt="房源"/><h3>{{ store.getHouse(id)?.name }}</h3><strong>¥{{ (store.price(store.getHouse(id)!) / 100).toLocaleString() }}/月</strong><p>{{ store.getHouse(id)?.layout }}</p><p>{{ store.getHouse(id)?.amenities.join(' · ') }}</p><button @click="store.toggleCompare(id)">移除</button></article></div></ModalShell></section></template>
