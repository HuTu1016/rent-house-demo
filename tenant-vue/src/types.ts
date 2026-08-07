export type HouseStatus = 'vacant' | 'rented' | 'expiring'
export type AppointmentStatus = 'pending' | 'confirmed' | 'declined'

export interface MediaItem { type: 'image' | 'video'; url: string; poster?: string; duration?: string }
export interface House { id: number; name: string; layout: string; type: string; priceCents: number; specialPriceCents?: number; specialDays?: number; status: HouseStatus; image: string; media: MediaItem[]; tags: string[]; amenities: string[]; description: string }
export interface Appointment { id: number; houseId: number; date: string; time: string; status: AppointmentStatus }
export interface ChatMessage { id: number; sender: 'tenant' | 'agent'; type: 'text' | 'appointment'; text?: string; appointmentId?: number }
export interface FilterState { location: string; layout: string; features: string[]; sort: 'default' | 'priceAsc' | 'priceDesc' | 'hot' | 'newest' }
export interface TenantIdentity { realName: string; mobile: string }
