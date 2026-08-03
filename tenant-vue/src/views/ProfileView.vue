<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import HouseCard from '../components/HouseCard.vue'
import { useRentalStore } from '../stores/rental'
import type { TenantIdentity } from '../types'

const store = useRentalStore()
const router = useRouter()
const editing = ref(false)
const form = ref<TenantIdentity>({ ...store.identity })
const recommendations = computed(() => [...new Set([...store.favorites, ...store.history])].map(store.getHouse).filter(Boolean).slice(0, 6))
function openHouse(id: number) { store.openHouse(id); router.push(`/house/${id}`) }
function edit() { form.value = { ...store.identity }; editing.value = true }
function save() { store.updateIdentity(form.value); editing.value = false }
</script>

<template>
  <section class="page profile-page">
    <header class="profile-header"><img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&h=120" alt="头像"/><div><h1>租客资料</h1><p>{{ store.identity.realName || '未完善身份资料' }}</p></div><button @click="edit">编辑</button></header>
    <section class="service-panel"><h2>身份资料</h2><div class="identity-summary"><p><b>姓名</b><span>{{ store.identity.realName || '未填写' }}</span></p><p><b>手机号</b><span>{{ store.identity.mobile || '未填写' }}</span></p><p><b>身份证</b><span>{{ store.identity.idNumber ? `${store.identity.idNumber.slice(0, 3)}***********${store.identity.idNumber.slice(-4)}` : '未填写' }}</span></p><p><b>家庭住址</b><span>{{ store.identity.homeAddress || '选填' }}</span></p><p><b>公司信息</b><span>{{ store.identity.companyName || '选填' }}{{ store.identity.companyAddress ? ` · ${store.identity.companyAddress}` : '' }}</span></p></div></section>
    <section class="service-panel"><h2>快捷入口</h2><div class="service-grid"><button @click="router.push('/wishlist')">☆<span>我的收藏</span></button><button @click="router.push('/wishlist?tab=history')">◷<span>浏览足迹</span></button><button @click="router.push('/messages')">💬<span>咨询消息</span></button></div></section>
    <section class="recommendations"><h2>猜你想看</h2><HouseCard v-for="house in recommendations" :key="house!.id" :house="house!" @open="openHouse" @chat="id => router.push(`/messages/${id}`)" /></section>
    <button class="logout" @click="store.notify('演示账号已退出')">退出登录</button>
    <div v-if="editing" class="filter-backdrop" @click.self="editing = false"><form class="filter-panel identity-form" @submit.prevent="save"><h2>完善身份资料</h2><label>姓名<input v-model="form.realName" required></label><label>身份证号<input v-model="form.idNumber" pattern="^\d{17}[\dXx]$" required></label><label>手机号<input v-model="form.mobile" pattern="^1\d{10}$" required></label><label>家庭住址（选填）<input v-model="form.homeAddress"></label><label>公司名称（选填）<input v-model="form.companyName"></label><label>公司地址（选填）<input v-model="form.companyAddress"></label><div class="filter-actions"><button type="button" @click="editing = false">取消</button><button type="submit">保存</button></div></form></div>
  </section>
</template>
