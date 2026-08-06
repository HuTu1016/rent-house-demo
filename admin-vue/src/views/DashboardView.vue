<script setup lang="ts">
import { ref } from 'vue'

const stats = ref([
  { label: '在租房源', value: '12', unit: '套' },
  { label: '本月带看', value: '8', unit: '次' },
  { label: '今日未读', value: '3', unit: '条' },
  { label: '空置率', value: '15', unit: '%' }
])
</script>

<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div v-for="stat in stats" :key="stat.label" class="stat-card card">
        <h3 class="label">{{ stat.label }}</h3>
        <div class="value-block">
          <span class="value">{{ stat.value }}</span>
          <span class="unit">{{ stat.unit }}</span>
        </div>
      </div>
    </div>
    
    <div class="recent-actions card">
      <h3>快捷操作</h3>
      <div class="actions-grid">
        <button class="action-btn" @click="$router.push('/properties')">
          <div class="icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M9 8h1"/><path d="M9 12h1"/><path d="M9 16h1"/><path d="M14 8h1"/><path d="M14 12h1"/><path d="M14 16h1"/><path d="M5 21V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16"/></svg>
          </div>
          <span>添加房产</span>
        </button>
        <button class="action-btn" @click="$router.push('/listings')">
          <div class="icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
          </div>
          <span>发布房源</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-card .label {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-muted);
}

.value-block {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.value-block .value {
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -1px;
}

.value-block .unit {
  font-size: 16px;
  font-weight: 600;
}

.recent-actions h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 700;
}

.actions-grid {
  display: flex;
  gap: 16px;
}

.action-btn {
  background: #ffffff;
  border: 1px solid var(--apple-border);
  border-radius: 10px;
  padding: 16px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: var(--apple-text);
  font-weight: 500;
  font-size: 14px;
  transition: all 0.15s ease;
  min-width: 120px;
}

.action-btn:hover {
  background: #111111;
  color: #ffffff;
  border-color: #111111;
}

.action-btn .icon {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
