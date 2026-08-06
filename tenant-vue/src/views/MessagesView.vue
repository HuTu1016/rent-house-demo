<script setup lang="ts">
import { onActivated, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useRentalStore } from '../stores/rental'
import * as api from '../services/api'

const store = useRentalStore()
const router = useRouter()
const conversations = ref<api.ApiConversation[]>([])

async function loadConversations() {
  try {
    const data = await api.fetchConversations()
    conversations.value = data.records.filter(item => Boolean(item.lastMessagePreview?.trim()))
  } catch (error) {
    store.notify(error instanceof Error ? error.message : '消息加载失败')
  }
}

onMounted(() => { void loadConversations() })
onActivated(() => { void loadConversations() })
</script>

<template>
  <section class="page messages-page">
    <header class="page-title">
      <h1>消息</h1>
    </header>
    <div class="conversation-list">
      <button
        v-for="conversation in conversations"
        :key="conversation.id"
        class="conversation"
        @click="router.push(`/messages/${conversation.listingId}`)"
      >
        <img :src="store.getHouse(Number(conversation.listingId))?.image" alt="房源" />
        <span>
          <strong>{{ conversation.listingTitle || store.getHouse(Number(conversation.listingId))?.name }}</strong>
          <small>{{ conversation.lastMessagePreview }}</small>
        </span>
        <time>16:22</time>
      </button>
    </div>
  </section>
</template>
