type ApiEnvelope<T> = { code: string; message: string; data: T }
type Page<T> = { records: T[]; total: number; page: number; size: number; hasNext: boolean }

const baseUrl = String(import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8090/api/v1').replace(/\/$/, '')
const tokenKey = 'admin-access-token'

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

export async function login(mobile?: string, password?: string) {
  mobile = mobile || String(import.meta.env.VITE_ADMIN_MOBILE ?? '13800000001')
  password = password || String(import.meta.env.VITE_ADMIN_PASSWORD ?? '123456')
  const data = await request<{ accessToken: string; refreshToken: string }>('/auth/password/login', { method: 'POST', body: JSON.stringify({ mobile, password }) })
  sessionStorage.setItem(tokenKey, data.accessToken)
}

export function clearSession() { sessionStorage.removeItem(tokenKey) }
export function isAuthenticated() { return !!sessionStorage.getItem(tokenKey) }

// --- 房产管理 (Properties) ---
export async function createBuilding(name: string, address: string) {
  return request<string>('/agent/properties/buildings', { method: 'POST', body: JSON.stringify({ name, address }) })
}

export async function createUnit(buildingId: string, unitNo: string, title: string, rooms: number, halls: number, bathrooms: number, areaSqm: number) {
  return request<string>('/agent/properties/units', { method: 'POST', body: JSON.stringify({ buildingId, unitNo, title, rooms, halls, bathrooms, areaSqm }) })
}

// --- 房源管理 (Listings) ---
export type ApiAgentListing = { id: string; title: string; rentCent: number; publishStatus: string; special: boolean; occupancyStatus: string }

export async function createListing(unitId: string, title: string, communityName: string, district: string, address: string, rentCent: number, depositCent: number) {
  return request<ApiAgentListing>('/agent/listings', { method: 'POST', body: JSON.stringify({ unitId, title, communityName, district, address, rentCent, depositCent }) })
}

export async function fetchListings(page = 1, size = 50) {
  return request<Page<ApiAgentListing>>(`/agent/listings?page=${page}&size=${size}`)
}

export async function publishListing(id: string | number) { return request<void>(`/agent/listings/${id}/publish`, { method: 'POST' }) }
export async function offlineListing(id: string | number) { return request<void>(`/agent/listings/${id}/offline`, { method: 'POST' }) }
export async function updateSpecial(id: string | number, enabled: boolean, sort: number = 0) { return request<void>(`/agent/listings/${id}/special`, { method: 'PATCH', body: JSON.stringify({ enabled, sort }) }) }
export async function addMedia(id: string | number, type: string, url: string, coverUrl?: string, sort = 0) {
  return request<void>(`/agent/listings/${id}/media`, { method: 'POST', body: JSON.stringify({ type, url, coverUrl, sort }) })
}

// --- 消息管理 (Conversations) ---
export type ApiConversation = { id: string; listingId: string; listingTitle?: string; lastMessagePreview?: string; tenantId?: string }
export type ApiMessage = { id: string; senderId: string; messageType?: string; content: string; createdAt: string; mine: boolean }

export async function fetchConversations() { return request<Page<ApiConversation>>('/conversations') }
export async function fetchMessages(conversationId: string | number) { return request<Page<ApiMessage>>(`/conversations/${conversationId}/messages`) }
export async function sendMessage(conversationId: string | number, content: string) { return request<ApiMessage>(`/conversations/${conversationId}/messages`, { method: 'POST', body: JSON.stringify({ content }) }) }
