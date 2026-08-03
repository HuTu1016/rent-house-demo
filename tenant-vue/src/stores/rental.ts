import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { Appointment, ChatMessage, FilterState, House, TenantIdentity } from '../types'
import * as api from '../services/api'

const clone = <T>(value: T): T => structuredClone(value)
const initialFilter: FilterState = { location: 'all', layout: 'all', features: [], sort: 'default' }

export const useRentalStore = defineStore('rental', () => {
  const houses = ref<House[]>([])
  const chats = ref<Record<number, ChatMessage[]>>({})
  const favorites = ref<number[]>([])
  const history = ref<number[]>([])
  const compareList = ref<number[]>([])
  const filter = ref<FilterState>(clone(initialFilter))
  const appointments = ref<Appointment[]>([])
  const identity = ref<TenantIdentity>({ realName: '', idNumber: '', mobile: '', homeAddress: '', companyName: '', companyAddress: '' })
  const toast = ref('')
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
  function sendMessage(houseId: number, text: string) { if (!text.trim()) return; void api.createConversation(houseId).then(conversation => api.sendMessage(conversation.id, text.trim())).then(() => loadChat(houseId)).catch(error => notify(error.message)) }
  function bookViewing(houseId: number, date: string, time: string) { const match = date.match(/\d{4}-\d{2}-\d{2}/); void api.fetchProfile().then(profile => api.createAppointment(houseId, `${match?.[0] ?? date}T${time.slice(0, 5)}`, profile.realName || profile.nickname || '租客', profile.mobile || '')).then(() => loadChat(houseId)).then(() => notify('预约已提交，等待中介确认')).catch(error => notify(error.message)) }
  function updateIdentity(value: TenantIdentity) { void api.updateProfile(value).then(() => { identity.value = clone(value); notify('身份资料已保存') }).catch(error => notify(error.message)) }
  async function loadRemote() {
    try {
      await api.login()
      const [listingPage, profile, favoritePage, historyPage, appointmentPage] = await Promise.all([api.fetchListings(), api.fetchProfile(), api.fetchFavorites(), api.fetchHistory(), api.fetchAppointments()])
      houses.value = listingPage.records.map(api.mapListing)
      favorites.value = favoritePage.records.map(item => Number(item.id)); history.value = historyPage.records.map(item => Number(item.id)); appointments.value = appointmentPage.records.map(api.mapAppointment)
      identity.value = { realName: profile.realName ?? '', idNumber: '', mobile: profile.mobile ?? '', homeAddress: profile.homeAddress ?? '', companyName: profile.companyName ?? '', companyAddress: profile.companyAddress ?? '' }
    } catch (error) { notify(error instanceof Error ? error.message : '后端服务暂不可用') }
  }
  async function loadChat(houseId: number) { try { const conversation = await api.createConversation(houseId); const page = await api.fetchMessages(conversation.id); chats.value[houseId] = page.records.map(api.mapMessage) } catch (error) { notify(error instanceof Error ? error.message : '消息加载失败') } }
  async function loadHouseDetail(houseId: number) { try { const detail = await api.fetchListingDetail(houseId); const mapped = api.mapListing(detail.listing); const target = houses.value.find(item => item.id === houseId); if (target) Object.assign(target, { ...mapped, description: detail.description, amenities: detail.facilities, media: detail.media.map(item => ({ type: item.type.toLowerCase() === 'video' ? 'video' : 'image', url: item.url, poster: item.coverUrl })) }) } catch (error) { notify(error instanceof Error ? error.message : '房源详情加载失败') } }
  return { houses, chats, favorites, history, compareList, filter, appointments, identity, toast, availableHouses, specialHouses, filteredHouses, price, getHouse, openHouse, toggleFavorite, toggleCompare, resetFilters, notify, sendMessage, bookViewing, updateIdentity, loadRemote, loadChat, loadHouseDetail }
})
