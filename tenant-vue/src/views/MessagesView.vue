<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useRentalStore } from '../stores/rental'
const store = useRentalStore(); const router = useRouter(); const ids = computed(() => Object.keys(store.chats).map(Number))
</script>
<template><section class="page messages-page"><header class="page-title"><h1>消息</h1></header><div class="conversation-list"><button class="conversation system" @click="store.notify('系统通知：暂无新通知')"><b>📣</b><span><strong>系统通知</strong><small>欢迎使用房乐管小程序，房东直租无中介费</small></span><time>09:30</time></button><button v-for="id in ids" :key="id" class="conversation" @click="router.push(`/messages/${id}`)"><img :src="store.getHouse(id)?.image" alt="房源"/><span><strong>{{ store.getHouse(id)?.name }}</strong><small>{{ store.chats[id].at(-1)?.type === 'appointment' ? '[📅 看房预约单]' : store.chats[id].at(-1)?.text }}</small></span><time>16:22</time></button></div></section></template>
