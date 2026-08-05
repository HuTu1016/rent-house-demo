<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import HouseCard from '../components/HouseCard.vue'
import { useRentalStore } from '../stores/rental'
import type { FilterState } from '../types'
const store = useRentalStore(); const router = useRouter(); const openPanel = ref<'location' | 'layout' | 'features' | 'sort' | null>(null)
const cloneFilter = (value: FilterState): FilterState => ({ ...value, features: [...value.features] })
const draftFilter = ref<FilterState>(cloneFilter(store.filter))
const loadMoreTarget = ref<HTMLElement>()
let loadMoreObserver: IntersectionObserver | undefined
const locations = ['all', '水斗新围村', '水斗老围村', '富豪新村', '上油松']; const layouts = ['all', '单间', '大单间', '一房一厅', '二房一厅', '三房一厅']; const features = ['精装修', '公寓', '采光好', '通风好', '带阳台', '空调', '天然气', '洗衣机', '携宠入住', '半年起租', '一年起租', '短租']
const title = (value: string, fallback: string) => value === 'all' ? fallback : value
const fallbackImage = 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800'
function open(id: number) { store.openHouse(id); router.push(`/house/${id}`) }; function chat(id: number) { router.push(`/messages/${id}`) }
function openFilter(panel: 'location' | 'layout' | 'features' | 'sort') { draftFilter.value = cloneFilter(store.filter); openPanel.value = openPanel.value === panel ? null : panel }
async function apply() { await store.applyFilters(draftFilter.value); openPanel.value = null }
function resetDraft() { draftFilter.value = { location: 'all', layout: 'all', features: [], sort: 'default' } }
function toggleFeature(value: string) { draftFilter.value.features = draftFilter.value.features.includes(value) ? draftFilter.value.features.filter(item => item !== value) : [...draftFilter.value.features, value] }
function replaceImage(event: Event) { const image = event.currentTarget as HTMLImageElement; if (image.dataset.fallback === 'true') { image.style.visibility = 'hidden'; return }; image.dataset.fallback = 'true'; image.src = fallbackImage }
onMounted(() => { if (typeof IntersectionObserver === 'undefined') return; loadMoreObserver = new IntersectionObserver(entries => { if (entries[0]?.isIntersecting) void store.loadMore() }, { rootMargin: '240px' }); if (loadMoreTarget.value) loadMoreObserver.observe(loadMoreTarget.value) })
watch(loadMoreTarget, target => { if (target && loadMoreObserver) loadMoreObserver.observe(target) })
onBeforeUnmount(() => loadMoreObserver?.disconnect())
</script>
<template>
  <section class="page home-page">
    <header class="store-header"><div><div class="store-title-row"><h1>本地租房·水斗新围村</h1><span class="platform-badge">无中介费平台</span></div><p>🛡️ 店铺地址：创艺照相馆（欢迎随时来访）</p></div><span>•••</span></header>
    <section class="community-invite"><div class="community-icon">●</div><div class="community-copy"><strong>房东邀请你加入本地租房群，好物不...</strong><span><i>●</i><i>●</i><i>●</i><i>●</i> 等 128 位邻居已加入</span></div><button @click="store.notify('进群功能即将开放')">立即进群</button></section>
    <section v-if="store.specialHouses.length" class="special-zone"><div class="section-heading"><strong>🔥 中介特价特推置顶</strong><span>限时特惠</span></div><div class="special-scroll"><button v-for="house in store.specialHouses" :key="house.id" class="special-card" @click="open(house.id)"><img :src="house.image || fallbackImage" alt="房源" loading="lazy" decoding="async" @error="replaceImage"/><span><strong>{{ house.name }}</strong><small>{{ house.layout }}</small><em>¥{{ (store.price(house) / 100).toLocaleString() }}/月</em></span></button></div></section>
    <div class="list-heading"><h2>推荐房源</h2><span>共 <b>{{ store.filteredHouses.length }}</b> 套在租</span></div>
    <div class="filter-shell">
      <div class="filters"><button :class="{ selected: openPanel === 'location', active: store.filter.location !== 'all' }" @click="openFilter('location')">{{ title(store.filter.location, '位置') }}<b>⌄</b></button><button :class="{ selected: openPanel === 'layout', active: store.filter.layout !== 'all' }" @click="openFilter('layout')">{{ title(store.filter.layout, '户型') }}<b>⌄</b></button><button :class="{ selected: openPanel === 'features', active: store.filter.features.length }" @click="openFilter('features')">{{ store.filter.features.length ? `筛选(${store.filter.features.length})` : '筛选' }}<b>⌄</b></button><button :class="{ selected: openPanel === 'sort', active: store.filter.sort !== 'default' }" @click="openFilter('sort')">排序<b>⌄</b></button></div>
      <div v-if="openPanel" class="filter-backdrop" @click="openPanel = null"></div>
      <div v-if="openPanel" class="filter-panel" @click.stop><template v-if="openPanel === 'location'"><button v-for="item in locations" :key="item" class="pill" :class="{ picked: draftFilter.location === item }" @click="draftFilter.location = item">{{ title(item, '不限') }}</button></template><template v-else-if="openPanel === 'layout'"><button v-for="item in layouts" :key="item" class="pill" :class="{ picked: draftFilter.layout === item }" @click="draftFilter.layout = item">{{ title(item, '不限') }}</button></template><template v-else-if="openPanel === 'features'"><button v-for="item in features" :key="item" class="pill" :class="{ picked: draftFilter.features.includes(item) }" @click="toggleFeature(item)">{{ item }}</button></template><template v-else><button v-for="item in [['default','默认排序'],['priceAsc','价格从低到高'],['priceDesc','价格从高到低'],['newest','最近发布']]" :key="item[0]" class="sort-row" :class="{ picked: draftFilter.sort === item[0] }" @click="draftFilter.sort = item[0] as typeof draftFilter.sort">{{ item[1] }}</button></template><div class="filter-actions"><button @click="resetDraft">重置</button><button @click="apply">确定</button></div></div>
    </div>
    <div v-if="store.filteredHouses.length" class="house-list"><HouseCard v-for="house in store.filteredHouses" :key="house.id" :house="house" @open="open" @chat="chat" /></div><div v-else class="empty-state">🔍 没有找到匹配条件的房源<br/><button @click="store.resetFilters()">清空筛选条件</button></div>
    <div v-if="store.filteredHouses.length && (store.hasMore || store.loadingMore)" ref="loadMoreTarget" class="load-more-state"><span v-if="store.loadingMore" class="loading-spinner"></span><span>{{ store.loadingMore ? '正在加载更多房源…' : '上拉加载更多' }}</span></div><div v-else-if="store.filteredHouses.length" class="load-more-state load-more-done">已经到底啦</div>
  </section>
</template>
