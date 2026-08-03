import { createRouter, createWebHistory } from 'vue-router'
import HomeView from './views/HomeView.vue'
import HouseView from './views/HouseView.vue'
import WishlistView from './views/WishlistView.vue'
import MessagesView from './views/MessagesView.vue'
import ChatView from './views/ChatView.vue'
import ProfileView from './views/ProfileView.vue'

export const router = createRouter({ history: createWebHistory(import.meta.env.BASE_URL), scrollBehavior: (_, __, saved) => saved ?? { top: 0 }, routes: [
  { path: '/', redirect: '/home' }, { path: '/home', component: HomeView, meta: { tab: 'home' } }, { path: '/house/:houseId', component: HouseView, props: true },
  { path: '/wishlist', component: WishlistView, meta: { tab: 'wishlist' } }, { path: '/messages', component: MessagesView, meta: { tab: 'messages' } }, { path: '/messages/:houseId', component: ChatView, props: true }, { path: '/profile', component: ProfileView, meta: { tab: 'profile' } }, { path: '/:pathMatch(.*)*', redirect: '/home' },
] })
