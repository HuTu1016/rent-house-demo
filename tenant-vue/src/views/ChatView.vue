<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ModalShell from '../components/ModalShell.vue'
import { useRentalStore } from '../stores/rental'
const route = useRoute(); const router = useRouter(); const store = useRentalStore(); const input = ref(''); const appointmentOpen = ref(false); const chatBox = ref<HTMLElement>()
const houseId = computed(() => Number(route.params.houseId)); const house = computed(() => store.getHouse(houseId.value)); const messages = computed(() => store.chats[houseId.value] ?? [])
onMounted(() => { void store.loadChat(houseId.value) })
function send() { store.sendMessage(houseId.value, input.value); input.value = ''; nextTick(() => chatBox.value?.scrollTo({ top: chatBox.value.scrollHeight })) }; function book() { store.bookViewing(houseId.value, '本周六 (2026-08-01)', '14:00 - 15:00'); appointmentOpen.value = false; nextTick(() => chatBox.value?.scrollTo({ top: chatBox.value.scrollHeight })) }
const appointmentFor = (id?: number) => store.appointments.find(item => item.id === id); watch(houseId, () => nextTick(() => chatBox.value?.scrollTo({ top: chatBox.value.scrollHeight })), { immediate: true })
</script>
<template><section v-if="house" class="page chat-page"><header class="chat-header"><button @click="router.push('/messages')">‹ 消息</button><strong>{{ house.name }}</strong><button @click="appointmentOpen = true">📅 预约</button></header><div ref="chatBox" class="chat-history"><template v-for="message in messages" :key="message.id"><div v-if="message.type === 'appointment'" class="appointment-message"><b>📅 看房预约单</b><img :src="house.image" alt="房源"/><strong>{{ house.name }}</strong><span>本周六 (2026-08-01) 14:00 - 15:00</span><em>⏳ {{ appointmentFor(message.appointmentId)?.status === 'confirmed' ? '中介已确认' : '等待中介确认' }}</em></div><p v-else class="bubble" :class="message.sender">{{ message.text }}</p></template></div><form class="chat-input" @submit.prevent="send"><input v-model="input" placeholder="输入消息..."/><button>发送</button></form><ModalShell v-if="appointmentOpen" @close="appointmentOpen = false"><h2>📅 预约看房时段</h2><label>日期<select><option>本周六 (2026-08-01)</option></select></label><label>时间段<select><option>14:00 - 15:00</option></select></label><button class="modal-primary" @click="book">确认发送看房预约</button></ModalShell></section></template>
