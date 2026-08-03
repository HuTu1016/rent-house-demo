import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { seedBills, seedChats, seedHouses, seedTickets } from '../data'
import type { Appointment, FilterState, Review, TicketStatus } from '../types'

const clone = <T>(value: T): T => structuredClone(value)
const initialFilter: FilterState = { location: 'all', layout: 'all', features: [], sort: 'default' }
const labels: Record<TicketStatus, string> = { pending: '待处理', processing: '处理中', awaiting_confirmation: '待租客确认', completed: '已完成' }

export const useRentalStore = defineStore('rental', () => {
  const houses = ref(clone(seedHouses)); const bills = ref(clone(seedBills)); const tickets = ref(clone(seedTickets)); const chats = ref(clone(seedChats))
  const favorites = ref<number[]>([2, 8, 9]); const history = ref<number[]>([11, 5, 2]); const compareList = ref<number[]>([])
  const filter = ref<FilterState>(clone(initialFilter)); const appointments = ref<Appointment[]>([]); const reviews = ref<Review[]>([]); const toast = ref('')
  let toastTimer = 0; let nextId = 1000
  const availableHouses = computed(() => houses.value.filter(h => h.status === 'vacant'))
  const specialHouses = computed(() => availableHouses.value.filter(h => h.specialPriceCents))
  const filteredHouses = computed(() => {
    const source = availableHouses.value.filter(h => (filter.value.location === 'all' || h.name.includes(filter.value.location)) && (filter.value.layout === 'all' || h.layout.includes(filter.value.layout) || (filter.value.layout === '单间' && h.layout.includes('大单间'))) && filter.value.features.every(f => h.tags.includes(f) || h.amenities.includes(f)))
    return [...source].sort((a, b) => filter.value.sort === 'priceAsc' ? price(a) - price(b) : filter.value.sort === 'priceDesc' ? price(b) - price(a) : filter.value.sort === 'newest' ? b.id - a.id : 0)
  })
  function price(house: typeof houses.value[number]) { return house.specialPriceCents ?? house.priceCents }
  function notify(message: string) { toast.value = message; window.clearTimeout(toastTimer); toastTimer = window.setTimeout(() => { toast.value = '' }, 2400) }
  function getHouse(id: number) { return houses.value.find(h => h.id === id) }
  function openHouse(id: number) { history.value = [id, ...history.value.filter(item => item !== id)] }
  function toggleFavorite(id: number) { favorites.value = favorites.value.includes(id) ? favorites.value.filter(item => item !== id) : [id, ...favorites.value]; notify(favorites.value.includes(id) ? '已加入收藏' : '已取消收藏') }
  function toggleCompare(id: number) { if (compareList.value.includes(id)) compareList.value = compareList.value.filter(item => item !== id); else if (compareList.value.length < 3) compareList.value.push(id); else notify('最多只能选择 3 个房源进行对比哦！') }
  function resetFilters() { filter.value = clone(initialFilter); notify('筛选条件已清空') }
  function sendMessage(houseId: number, text: string) { if (!text.trim()) return; (chats.value[houseId] ??= []).push({ id: nextId++, sender: 'tenant', type: 'text', text: text.trim() }) }
  function bookViewing(houseId: number, date: string, time: string) { const appointment = { id: nextId++, houseId, date, time, status: 'pending' as const }; appointments.value.unshift(appointment); (chats.value[houseId] ??= []).push({ id: nextId++, sender: 'tenant', type: 'appointment', appointmentId: appointment.id }); notify('预约已提交，等待房东确认') }
  function reportPaid(id: number) { const bill = bills.value.find(item => item.id === id); if (bill) { bill.status = 'pending_verification'; notify('付款信息已报备，等待房东确认') } }
  function submitRepair(type: string, description: string) { if (!description.trim()) return false; tickets.value.unshift({ id: nextId++, type, description, createdAt: '刚刚', status: 'pending' }); notify('报修工单已提交'); return true }
  function confirmRepair(id: number) { const ticket = tickets.value.find(item => item.id === id); if (ticket) { ticket.status = 'completed'; notify('维修结果已确认') } }
  function submitReview(score: number, content: string) { if (!content.trim()) return false; reviews.value.unshift({ id: nextId++, score, content, createdAt: '刚刚' }); notify('评价已提交，感谢您的反馈'); return true }
  return { houses, bills, tickets, chats, favorites, history, compareList, filter, appointments, reviews, toast, availableHouses, specialHouses, filteredHouses, price, getHouse, openHouse, toggleFavorite, toggleCompare, resetFilters, notify, sendMessage, bookViewing, reportPaid, submitRepair, confirmRepair, submitReview, ticketLabel: (value: TicketStatus) => labels[value] }
})
