import type { Appointment, ChatMessage, House, TenantIdentity } from '../types'

type ApiEnvelope<T> = { code: string; message: string; data: T }
type Page<T> = { records: T[]; total: number; page: number; size: number; hasNext: boolean }

const baseUrl = String(import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1').replace(/\/$/, '')
const tokenKey = 'rent-house-access-token'

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Content-Type', 'application/json')
  const token = sessionStorage.getItem(tokenKey)
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const response = await fetch(`${baseUrl}${path}`, { ...init, headers })
  const body = (await response.json()) as ApiEnvelope<T>
  if (!response.ok || body.code !== 'OK') throw new Error(body.message || `请求失败(${response.status})`)
  return body.data
}

export async function login() {
  const mobile = String(import.meta.env.VITE_TENANT_MOBILE ?? '')
  const password = String(import.meta.env.VITE_TENANT_PASSWORD ?? '')
  if (!mobile || !password) throw new Error('未配置租客登录凭据，请设置 VITE_TENANT_MOBILE 和 VITE_TENANT_PASSWORD')
  const data = await request<{ accessToken: string; refreshToken: string }>('/auth/password/login', { method: 'POST', body: JSON.stringify({ mobile, password }) })
  sessionStorage.setItem(tokenKey, data.accessToken)
  sessionStorage.setItem('rent-house-refresh-token', data.refreshToken)
}

export function clearSession() { sessionStorage.removeItem(tokenKey); sessionStorage.removeItem('rent-house-refresh-token') }

export async function fetchListings(params: Record<string, string | number | undefined> = {}) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => value !== undefined && value !== '' && query.set(key, String(value)))
  return request<Page<ApiListing>>(`/listings?${query}`)
}

export async function fetchListingDetail(id: number) { return request<ApiDetail>(`/listings/${id}`) }
export async function setFavorite(id: number, enabled: boolean) { return request<void>(`/tenant/favorites/${id}`, { method: enabled ? 'PUT' : 'DELETE' }) }
export async function fetchFavorites() { return request<Page<ApiListing>>('/tenant/favorites') }
export async function fetchHistory() { return request<Page<ApiListing>>('/tenant/history') }
export async function createConversation(listingId: number) { return request<{ id: string; listingId: string }>(`/tenant/listings/${listingId}/conversation`, { method: 'POST' }) }
export async function fetchConversations() { return request<Page<ApiConversation>>('/conversations') }
export async function fetchMessages(conversationId: string | number) { return request<Page<ApiMessage>>(`/conversations/${conversationId}/messages`) }
export async function sendMessage(conversationId: string | number, content: string) { return request<ApiMessage>(`/conversations/${conversationId}/messages`, { method: 'POST', body: JSON.stringify({ content }) }) }
export async function createAppointment(listingId: number, scheduledAt: string, contactName: string, contactMobile: string) { return request<ApiAppointment>(`/tenant/listings/${listingId}/appointments`, { method: 'POST', body: JSON.stringify({ scheduledAt, contactName, contactMobile }) }) }
export async function fetchAppointments() { return request<Page<ApiAppointment>>('/appointments') }
export async function fetchProfile() { return request<ApiProfile>('/tenant/profile') }
export async function updateProfile(identity: TenantIdentity) { return request<void>('/tenant/profile', { method: 'PATCH', body: JSON.stringify(identity) }) }

export type ApiListing = { id: string; title: string; communityName: string; district: string; address: string; rentCent: number; roomCount: number; hallCount: number; bathroomCount: number; areaSqm: number; tags: string[]; coverUrl: string; favorite: boolean; special: boolean }
export type ApiDetail = { listing: ApiListing; description: string; facilities: string[]; media: { id: string; type: string; url: string; coverUrl?: string; sortNo: number }[] }
export type ApiConversation = { id: string; listingId: string; listingTitle?: string; lastMessagePreview?: string }
export type ApiMessage = { id: string; senderId: string; messageType?: string; content: string; appointmentId?: string; createdAt: string; mine: boolean }
export type ApiAppointment = { id: string; listingId: string; scheduledAt: string; status: string }
export type ApiProfile = { nickname: string; avatarUrl?: string; mobile?: string; realName?: string; idNumberMasked?: string; homeAddress?: string; companyName?: string; companyAddress?: string; favorites: number; histories: number }

export function mapListing(item: ApiListing): House {
  const room = item.roomCount > 0 ? `${item.roomCount}房${item.hallCount}厅` : '单间'
  return { id: Number(item.id), name: item.title, layout: `${room} · ${item.areaSqm}㎡`, type: '整租', priceCents: item.rentCent, specialPriceCents: item.special ? item.rentCent : undefined, status: 'vacant', image: item.coverUrl, media: [{ type: 'image', url: item.coverUrl }], tags: item.tags ?? [], amenities: [], description: '' }
}

export function mapAppointment(item: ApiAppointment): Appointment { const date = item.scheduledAt.replace('T', ' '); return { id: Number(item.id), houseId: Number(item.listingId), date: date.slice(0, 10), time: date.slice(11, 16), status: item.status.toLowerCase() as Appointment['status'] } }
export function mapMessage(item: ApiMessage): ChatMessage { return { id: Number(item.id), sender: item.mine ? 'tenant' : 'agent', type: item.messageType === 'APPOINTMENT' ? 'appointment' : 'text', text: item.content, appointmentId: item.appointmentId ? Number(item.appointmentId) : undefined } }
