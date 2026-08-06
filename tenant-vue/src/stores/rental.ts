import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { Appointment, ChatMessage, FilterState, House, TenantIdentity } from '../types'
import * as api from '../services/api'
import { preloadImages } from '../utils/imageCache'

const clone = <T>(value: T): T => JSON.parse(JSON.stringify(value))
const initialFilter: FilterState = { location: 'all', layout: 'all', features: [], sort: 'default' }
const listingCacheTtl = 30_000
const detailCacheTtl = 60_000
const listingPageSize = 10

export const useRentalStore = defineStore('rental', () => {
  const houses = ref<House[]>([])
  const chats = ref<Record<number, ChatMessage[]>>({})
  const favorites = ref<number[]>([])
  const history = ref<number[]>([])
  const compareList = ref<number[]>([])
  const filter = ref<FilterState>(clone(initialFilter))
  const appointments = ref<Appointment[]>([])
  const identity = ref<TenantIdentity>({ realName: '', mobile: '', homeAddress: '' })
  const toast = ref('')
  const connectionError = ref('')
  const hasMore = ref(false)
  const loadingMore = ref(false)
  const listingPage = ref(1)
  const activeListingParams = ref<Record<string, string | number | undefined>>({})
  const listingCache = new Map<string, { expiresAt: number; houses: House[]; hasMore: boolean }>()
  const listingRequests = new Map<string, Promise<{ houses: House[]; hasMore: boolean }>>()
  const detailCache = new Map<number, { expiresAt: number; house: House }>()
  const chatLoadedAt = new Map<number, number>()
  let toastTimer = 0

  const availableHouses = computed(() => houses.value.filter(h => h.status === 'vacant'))
  const specialHouses = computed(() => availableHouses.value.filter(h => h.specialPriceCents))
  const filteredHouses = computed(() => {
    const source = availableHouses.value.filter(h => (filter.value.location === 'all' || h.name.includes(filter.value.location)) && (filter.value.layout === 'all' || h.layout.includes(filter.value.layout) || (filter.value.layout === '单间' && h.layout.includes('大单间'))) && filter.value.features.every(f => h.tags.includes(f) || h.amenities.includes(f)))
    return [...source].sort((a, b) => filter.value.sort === 'priceAsc' ? price(a) - price(b) : filter.value.sort === 'priceDesc' ? price(b) - price(a) : filter.value.sort === 'newest' ? b.id - a.id : 0)
  })
  function price(house: House) { return house.specialPriceCents ?? house.priceCents }
  function notify(message: string) { toast.value = message; window.clearTimeout(toastTimer); toastTimer = window.setTimeout(() => { toast.value = '' }, 2400) }
  function getHouse(id: number) { return houses.value.find(h => h.id === id) }
  function openHouse(id: number) { history.value = [id, ...history.value.filter(item => item !== id)] }
  function toggleFavorite(id: number) { const enabled = !favorites.value.includes(id); favorites.value = enabled ? [id, ...favorites.value] : favorites.value.filter(item => item !== id); void api.setFavorite(id, enabled).catch(error => notify(error.message)) }
  function toggleCompare(id: number) { if (compareList.value.includes(id)) compareList.value = compareList.value.filter(item => item !== id); else if (compareList.value.length < 3) compareList.value.push(id); else notify('最多只能选择 3 个房源进行对比哦！') }
  function resetFilters() { filter.value = clone(initialFilter); notify('筛选条件已清空') }
  function listingKey(params: Record<string, string | number | undefined>) { return Object.entries(params).filter(([, value]) => value !== undefined && value !== '').sort(([a], [b]) => a.localeCompare(b)).map(([key, value]) => `${key}=${value}`).join('&') || 'default' }
  async function fetchListingsCached(params: Record<string, string | number | undefined>, force = false) {
    const key = listingKey(params)
    const cached = listingCache.get(key)
    if (!force && cached && cached.expiresAt > Date.now()) return { houses: clone(cached.houses), hasMore: cached.hasMore }
    const pending = listingRequests.get(key)
    if (pending) return pending
    const request = api.fetchListings(params).then(page => { const items = page.records.map(api.mapListing); listingCache.set(key, { expiresAt: Date.now() + listingCacheTtl, houses: clone(items), hasMore: page.hasNext }); return { houses: items, hasMore: page.hasNext } }).finally(() => listingRequests.delete(key))
    listingRequests.set(key, request)
    return request
  }
  async function applyFilters(next: FilterState) {
    const rooms = next.layout === 'all' ? undefined : next.layout.includes('三') ? 3 : next.layout.includes('二') ? 2 : next.layout.includes('一') || next.layout.includes('单间') ? 1 : undefined
    const params: Record<string, string | number | undefined> = {
      keyword: next.location === 'all' ? undefined : next.location,
      rooms,
      sort: next.sort === 'priceAsc' ? 'rent_asc' : next.sort === 'priceDesc' ? 'rent_desc' : next.sort === 'newest' ? 'newest' : 'recommended',
    }
    try {
      const result = await fetchListingsCached({ ...params, page: 1, size: listingPageSize })
      houses.value = result.houses
      hasMore.value = result.hasMore
      listingPage.value = 1
      activeListingParams.value = params
      void preloadImages(houses.value.flatMap(house => [house.image, ...house.media.map(item => item.url)]), 12)
      filter.value = { ...next, features: [...next.features] }
    } catch (error) {
      notify(error instanceof Error ? error.message : '筛选房源失败')
      throw error
    }
  }
  function sendMessage(houseId: number, text: string) { if (!text.trim()) return; void api.createConversation(houseId).then(conversation => api.sendMessage(conversation.id, text.trim())).then(() => loadChat(houseId, true)).catch(error => notify(error.message)) }
  function bookViewing(houseId: number, date: string, time: string) { const match = date.match(/\d{4}-\d{2}-\d{2}/); void api.fetchProfile().then(profile => api.createAppointment(houseId, `${match?.[0] ?? date}T${time.slice(0, 5)}`, profile.realName || profile.nickname || '租客', profile.mobile || '')).then(() => loadChat(houseId, true)).then(() => notify('预约已提交，等待中介确认')).catch(error => notify(error.message)) }
  function updateIdentity(value: TenantIdentity) { void api.updateProfile(value).then(() => { identity.value = clone(value); notify('身份资料已保存') }).catch(error => notify(error.message)) }
  async function loadRemote() {
    try {
      await api.login()
      const [result, profile, favoritePage, historyPage, appointmentPage] = await Promise.all([fetchListingsCached({ page: 1, size: listingPageSize }, true), api.fetchProfile(), api.fetchFavorites(), api.fetchHistory(), api.fetchAppointments()])
      houses.value = result.houses
      hasMore.value = result.hasMore
      listingPage.value = 1
      activeListingParams.value = {}
      void preloadImages(houses.value.flatMap(house => [house.image, ...house.media.map(item => item.url)]), 12)
      favorites.value = favoritePage.records.map(item => Number(item.id)); history.value = historyPage.records.map(item => Number(item.id)); appointments.value = appointmentPage.records.map(api.mapAppointment)
      identity.value = { realName: profile.realName ?? '', mobile: profile.mobile ?? '', homeAddress: profile.homeAddress ?? '' }
      connectionError.value = ''
    } catch (error) {
      connectionError.value = error instanceof Error ? error.message : '后端服务暂不可用'
      notify(connectionError.value)
    }
  }
  async function loadMore() {
    if (!hasMore.value || loadingMore.value) return
    loadingMore.value = true
    const nextPage = listingPage.value + 1
    try {
      const result = await fetchListingsCached({ ...activeListingParams.value, page: nextPage, size: listingPageSize })
      const items = result.houses
      const existing = new Set(houses.value.map(item => item.id))
      houses.value = [...houses.value, ...items.filter(item => !existing.has(item.id))]
      hasMore.value = result.hasMore
      listingPage.value = nextPage
      void preloadImages(items.flatMap(house => [house.image, ...house.media.map(item => item.url)]), 12)
    } catch (error) { notify(error instanceof Error ? error.message : '加载更多房源失败') } finally { loadingMore.value = false }
  }
  async function loadChat(houseId: number, force = false) {
    try {
      if (!force && chatLoadedAt.get(houseId) && chatLoadedAt.get(houseId)! > Date.now() - 10_000) return
      const conversations = await api.fetchConversations()
      const conversation = conversations.records.find(item => Number(item.listingId) === houseId && Boolean(item.lastMessagePreview?.trim()))
      if (!conversation) { delete chats.value[houseId]; return }
      const page = await api.fetchMessages(conversation.id)
      chats.value[houseId] = page.records.map(api.mapMessage)
      chatLoadedAt.set(houseId, Date.now())
    } catch (error) { notify(error instanceof Error ? error.message : '消息加载失败') }
  }
  async function loadHouseDetail(houseId: number) { try { const cached = detailCache.get(houseId); if (cached && cached.expiresAt > Date.now()) { const target = houses.value.find(item => item.id === houseId); if (target) Object.assign(target, clone(cached.house)); void preloadImages([cached.house.image, ...cached.house.media.map(item => item.url)], 8); return } const detail = await api.fetchListingDetail(houseId); const mapped = api.mapListing(detail.listing); const enriched = { ...mapped, description: detail.description, amenities: detail.facilities, media: detail.media.map(item => ({ type: item.type.toLowerCase() === 'video' ? 'video' as const : 'image' as const, url: item.url, poster: item.coverUrl })) }; detailCache.set(houseId, { expiresAt: Date.now() + detailCacheTtl, house: clone(enriched) }); void preloadImages([enriched.image, ...enriched.media.map(item => item.url)], 12); const target = houses.value.find(item => item.id === houseId); if (target) Object.assign(target, enriched) } catch (error) { notify(error instanceof Error ? error.message : '房源详情加载失败') } }
  return { houses, chats, favorites, history, compareList, filter, appointments, identity, toast, connectionError, hasMore, loadingMore, availableHouses, specialHouses, filteredHouses, price, getHouse, openHouse, toggleFavorite, toggleCompare, resetFilters, applyFilters, loadMore, notify, sendMessage, bookViewing, updateIdentity, loadRemote, loadChat, loadHouseDetail }
})
