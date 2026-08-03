export type HouseStatus = 'vacant' | 'rented' | 'expiring'
export type BillStatus = 'pending_payment' | 'pending_verification' | 'paid'
export type TicketStatus = 'pending' | 'processing' | 'awaiting_confirmation' | 'completed'
export type AppointmentStatus = 'pending' | 'confirmed' | 'declined'

export interface MediaItem { type: 'image' | 'video'; url: string; poster?: string; duration?: string }
export interface House { id: number; name: string; layout: string; type: string; priceCents: number; specialPriceCents?: number; specialDays?: number; status: HouseStatus; image: string; media: MediaItem[]; tags: string[]; amenities: string[]; description: string }
export interface Bill { id: number; month: string; houseName: string; amountCents: number; status: BillStatus; breakdown: string }
export interface Ticket { id: number; type: string; description: string; createdAt: string; status: TicketStatus; assignee?: string }
export interface Appointment { id: number; houseId: number; date: string; time: string; status: AppointmentStatus }
export interface ChatMessage { id: number; sender: 'tenant' | 'landlord'; type: 'text' | 'appointment'; text?: string; appointmentId?: number }
export interface Review { id: number; score: number; content: string; createdAt: string }
export interface FilterState { location: string; layout: string; features: string[]; sort: 'default' | 'priceAsc' | 'priceDesc' | 'hot' | 'newest' }
