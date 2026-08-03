import type { Bill, ChatMessage, House, Ticket } from './types'

const video = 'https://assets.mixkit.co/videos/preview/mixkit-interior-of-a-modern-apartment-41561-large.mp4'
const images = [
  'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800',
  'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800',
  'https://images.unsplash.com/photo-1484154218962-a197022b5858?w=800',
]

function house(id: number, name: string, layout: string, price: number, tags: string[], special = 0): House {
  const image = images[id % images.length]
  return { id, name, layout, type: '整租', priceCents: price * 100, specialPriceCents: special ? special * 100 : undefined, specialDays: special ? (id % 5) + 1 : undefined, status: id <= 2 ? (id === 2 ? 'expiring' : 'rented') : 'vacant', image, media: [{ type: 'video', url: video, poster: image, duration: '00:18' }, { type: 'image', url: images[(id + 1) % images.length] }, { type: 'image', url: images[(id + 2) % images.length] }], tags, amenities: ['空调', '洗衣机', ...(id % 3 === 0 ? ['天然气'] : [])], description: `${price}元包管理，民水民电，家电齐全，支持随时看房。` }
}

export const seedHouses: House[] = [
  house(1, '340-水斗老围村电梯大两房一厅', '二房一厅 · 55㎡', 2900, ['精装修', '带阳台', '一年起租'], 2700),
  house(2, '60-水斗新围村电梯5楼单间', '单间 · 20㎡', 1050, ['采光好', '短租']),
  house(3, '117-水斗新围村一房一厅', '一房一厅 · 35㎡', 1550, ['通风好', '半年起租']),
  house(4, '108-水斗新围村7楼单间', '单间 · 18㎡', 860, ['采光好', '短租']),
  house(5, '353-富豪新村电梯两房一厅', '二房一厅 · 60㎡', 1700, ['带阳台', '一年起租'], 1600),
  house(6, '168-水斗老围村大单间', '大单间 · 30㎡', 1050, ['精装修', '公寓', '携宠入住']),
  house(7, '703-上油松精装大单间', '大单间 · 32㎡', 1300, ['公寓', '采光好', '半年起租']),
  house(8, '143-水斗老围村一房一厅', '一房一厅 · 42㎡', 1800, ['天然气', '带阳台', '一年起租'], 1650),
  house(9, '22-水斗新围村大单间', '大单间 · 30㎡', 1200, ['精装修', '采光好', '短租'], 1100),
  house(10, '119-水斗老围村靠山两房一厅', '二房一厅 · 68㎡', 2500, ['带阳台', '携宠入住', '一年起租']),
  house(11, '96-富豪新村一房一厅', '一房一厅 · 38㎡', 1600, ['带阳台', '半年起租'], 1450),
  house(12, '801-水斗老围村景观三房一厅', '三房一厅 · 95㎡', 3800, ['精装修', '带阳台', '一年起租'], 3500),
]

export const seedBills: Bill[] = [
  { id: 101, month: '2026-07', houseName: seedHouses[0].name, amountCents: 298250, status: 'pending_payment', breakdown: '租金 ¥2,800 + 水费 ¥38.50 + 电费 ¥144.00' },
  { id: 100, month: '2026-06', houseName: seedHouses[0].name, amountCents: 270000, status: 'paid', breakdown: '租金 ¥2,700' },
]
export const seedTickets: Ticket[] = [{ id: 1, type: '家电损坏 (热水器)', description: '热水器不出热水。', createdAt: '10分钟前', status: 'pending' }]
export const seedChats: Record<number, ChatMessage[]> = {
  1: [{ id: 1, sender: 'tenant', type: 'text', text: '你好！请问周六方便看房吗？' }, { id: 2, sender: 'landlord', type: 'text', text: '你好！周六下午2点可以安排看房。' }],
  2: [{ id: 3, sender: 'tenant', type: 'text', text: '请问包含管理费和网费吗？' }, { id: 4, sender: 'landlord', type: 'text', text: '包管理和网络，民水民电。' }],
  5: [{ id: 5, sender: 'tenant', type: 'text', text: '这套两房还在出租吗？' }, { id: 6, sender: 'landlord', type: 'text', text: '在的，随时可以安排现场看房～' }],
}
