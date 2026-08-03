<script setup lang="ts">
import { computed } from 'vue'
import type { House } from '../types'

const props = defineProps<{ house: House; compareMode?: boolean; checked?: boolean }>()
const emit = defineEmits<{ open: [id: number]; chat: [id: number]; compare: [id: number] }>()

const isSpecial = computed(() => Boolean(props.house.specialPriceCents))
const displayPrice = computed(() => (props.house.specialPriceCents ?? props.house.priceCents) / 100)
const originalPrice = computed(() => props.house.priceCents / 100)

const mediaItems = computed(() => {
  const list = props.house.media || []
  if (list.length >= 3) return list
  const fallback = [
    { type: 'image' as const, url: props.house.image || 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=400' },
    { type: 'image' as const, url: 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400' },
    { type: 'image' as const, url: 'https://images.unsplash.com/photo-1484154218962-a197022b5858?w=400' }
  ]
  return [...list, ...fallback.slice(list.length)]
})

const badgeText = computed(() => {
  if (props.house.specialPriceCents) {
    const diff = (props.house.priceCents - props.house.specialPriceCents) / 100
    return `🔥 特价立省¥${diff}`
  }
  return props.house.tags[0] ? `📍 ${props.house.tags[0]}` : '✨ 热门真实房源'
})

function toggleVideo(event: Event) {
  event.stopPropagation()
  const target = event.currentTarget as HTMLElement
  const video = target.querySelector('video')
  if (video) {
    video.paused ? video.play() : video.pause()
  }
}
</script>

<template>
  <article class="ios-house-card" @click="emit('open', house.id)">
    <!-- 对比切换按钮 -->
    <div v-if="compareMode" class="compare-toggle-btn" :class="{ checked }" @click.stop="emit('compare', house.id)">
      <div class="compare-checkbox">
        <svg v-if="checked" viewBox="0 0 24 24" class="check-icon"><path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"></path></svg>
      </div>
      <span>加入对比</span>
    </div>

    <!-- 1主2副 媒体区 -->
    <div class="ios-house-hero-grid">
      <!-- 主媒体 -->
      <div class="ios-hero-main" @click="toggleVideo">
        <template v-if="mediaItems[0].type === 'video'">
          <video :src="mediaItems[0].url" :poster="mediaItems[0].poster || house.image" autoplay loop muted playsinline></video>
          <div class="ios-video-tag">📹 视频带看 {{ mediaItems[0].duration || '00:18' }}</div>
          <div class="ios-video-play-btn">▶</div>
        </template>
        <template v-else>
          <img :src="mediaItems[0].url || house.image" alt="房源主图" />
        </template>
        <div class="ios-img-badge">{{ badgeText }}</div>
      </div>

      <!-- 副媒体两列 -->
      <div class="ios-hero-sub">
        <div class="ios-hero-sub-item" @click="toggleVideo">
          <video v-if="mediaItems[1].type === 'video'" :src="mediaItems[1].url" :poster="mediaItems[1].poster" autoplay loop muted playsinline></video>
          <img v-else :src="mediaItems[1].url" alt="副图1" />
          <div v-if="mediaItems[1].type === 'video'" class="ios-video-play-btn sub-play">▶</div>
        </div>
        <div class="ios-hero-sub-item" @click="toggleVideo">
          <video v-if="mediaItems[2].type === 'video'" :src="mediaItems[2].url" :poster="mediaItems[2].poster" autoplay loop muted playsinline></video>
          <img v-else :src="mediaItems[2].url" alt="副图2" />
          <div v-if="mediaItems[2].type === 'video'" class="ios-video-play-btn sub-play">▶</div>
        </div>
      </div>
    </div>

    <!-- 房源标题与价格突出区 -->
    <div class="ios-house-heading">
      <div class="title-block">
        <h3 class="house-title">{{ house.name }}</h3>
        <div class="house-subtitle">{{ house.layout }} · {{ house.type }}</div>
      </div>
      <div class="price-block">
        <div class="price-main">
          <span class="currency">¥</span><span class="amount">{{ displayPrice }}</span><span class="unit">/月</span>
        </div>
        <div v-if="isSpecial" class="price-original">原价¥{{ originalPrice }}</div>
      </div>
    </div>

    <!-- 描述摘要 -->
    <div class="ios-house-desc">
      {{ house.description }}
    </div>

    <!-- 底部：标签、设施与 CTA 组合区 -->
    <div class="ios-house-footer">
      <div class="left-tags">
        <div class="chips-row">
          <span v-for="tag in house.tags" :key="tag" class="tag-chip">{{ tag }}</span>
        </div>
        <div v-if="house.amenities?.length" class="amenities-row">
          <span v-for="item in house.amenities" :key="item" class="amenity-chip">{{ item }}</span>
        </div>
      </div>
      <button class="ask-agent-btn" @click.stop="emit('chat', house.id)">
        💬 问中介
      </button>
    </div>
  </article>
</template>
