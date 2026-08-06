<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchListings, publishListing, offlineListing, createListing, createBuilding, createUnit, addMedia } from '../services/api'
import type { ApiAgentListing } from '../services/api'
import { useAdminStore } from '../stores/admin'

const route = useRoute()
const store = useAdminStore()

const listings = ref<ApiAgentListing[]>([])
const loading = ref(false)
const submitting = ref(false)
const showCreateModal = ref(false)

// 预设高精美精装房间封面图，供中介一键选择
const sampleImages = [
  'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800',
  'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800',
  'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800',
  'https://images.unsplash.com/photo-1484154218962-a197022b5858?w=800'
]

const form = ref({
  unitId: '',
  title: '502-水斗老围村阳台一房一厅',
  communityName: '水斗老围村',
  district: '龙华区',
  address: '水斗老围村88号',
  unitNo: '502',
  rooms: 1,
  halls: 1,
  bathrooms: 1,
  areaSqm: 45,
  rentYuan: 1800,
  depositYuan: 1800,
  coverUrl: sampleImages[0],
  isSpecial: false
})

async function load() {
  loading.value = true
  try {
    const res = await fetchListings(1, 50)
    listings.value = res.records
  } catch (e) {
    store.notify('加载房源列表失败')
  } finally {
    loading.value = false
  }
}

// 支持全自动一体化创建发布 (免去手动输入复杂 ID)
async function submitCreate() {
  submitting.value = true
  try {
    let targetUnitId = form.value.unitId

    // 如果未传入已知 UnitID，则后台一键自动创建楼栋与房间单元！
    if (!targetUnitId) {
      const buildingId = await createBuilding(form.value.communityName, form.value.address)
      targetUnitId = await createUnit(
        buildingId,
        form.value.unitNo || '101',
        form.value.title,
        form.value.rooms,
        form.value.halls,
        form.value.bathrooms,
        form.value.areaSqm
      )
    }

    // 转换元为分 (1元 = 100分)
    const rentCent = Math.round(Number(form.value.rentYuan) * 100)
    const depositCent = Math.round(Number(form.value.depositYuan) * 100)

    // 创建房源
    const listing = await createListing(
      targetUnitId,
      form.value.title,
      form.value.communityName,
      form.value.district,
      form.value.address,
      rentCent,
      depositCent
    )

    // 自动上架房源
    await publishListing(listing.id)

    // 上传封面图片
    if (form.value.coverUrl) {
      await addMedia(listing.id, 'IMAGE', form.value.coverUrl, form.value.coverUrl, 0)
    }

    store.notify('🎉 房源全自动建档并发布上架成功！')
    showCreateModal.value = false
    load()
  } catch (e) {
    store.notify(e instanceof Error ? e.message : '发布失败')
  } finally {
    submitting.value = false
  }
}

async function togglePublish(item: ApiAgentListing) {
  try {
    if (item.publishStatus === 'PUBLISHED') {
      await offlineListing(item.id)
      item.publishStatus = 'OFFLINE'
      store.notify('房源已下架')
    } else {
      await publishListing(item.id)
      item.publishStatus = 'PUBLISHED'
      store.notify('房源已发布展示')
    }
  } catch (e) {
    store.notify('操作失败')
  }
}

// 检查 URL 是否带参一键跳转发布
onMounted(() => {
  load()
  if (route.query.unitId) {
    form.value.unitId = String(route.query.unitId)
    if (route.query.title) form.value.title = String(route.query.title)
    if (route.query.communityName) form.value.communityName = String(route.query.communityName)
    if (route.query.address) form.value.address = String(route.query.address)
    showCreateModal.value = true
  }
})
</script>

<template>
  <div class="listings-view">
    <div class="action-bar">
      <div class="title-group">
        <h3>全量租赁房源运营看板</h3>
        <span class="total-count">共 {{ listings.length }} 套线上房源</span>
      </div>
      <button class="btn-primary" @click="showCreateModal = true">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="margin-right: 6px;">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        发布新房源信息配置
      </button>
    </div>

    <div class="list-card card">
      <table v-if="listings.length" class="apple-table">
        <thead>
          <tr>
            <th>房源编号</th>
            <th>房源展示标题</th>
            <th>租金 (元/月)</th>
            <th>出租状态</th>
            <th>上架状态</th>
            <th>精选</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in listings" :key="item.id">
            <td class="id-cell">{{ item.id }}</td>
            <td><strong class="title-text">{{ item.title }}</strong></td>
            <td class="price">¥{{ (item.rentCent / 100).toFixed(0) }} <span class="unit-text">/月</span></td>
            <td><span class="badge neutral">{{ item.occupancyStatus === 'VACANT' ? '空置' : '已租' }}</span></td>
            <td>
              <span class="badge" :class="item.publishStatus === 'PUBLISHED' ? 'published' : 'offline'">
                {{ item.publishStatus === 'PUBLISHED' ? '展示中' : '已下架' }}
              </span>
            </td>
            <td>
              <span v-if="item.special" class="badge special">精选推荐</span>
              <span v-else class="muted-text">-</span>
            </td>
            <td>
              <button class="action-btn-line" @click="togglePublish(item)">
                {{ item.publishStatus === 'PUBLISHED' ? '下架' : '上架' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else-if="!loading" class="empty">暂无房源数据</div>
      <div v-if="loading" class="empty">加载中...</div>
    </div>

    <!-- 房源配置发布模态框 (一体化无需繁琐 ID) -->
    <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
      <div class="modal-card card">
        <div class="modal-header">
          <h2>全自动化发布新房源</h2>
          <span class="tip-badge">自动完成物理建档与上架</span>
        </div>
        
        <form @submit.prevent="submitCreate" class="styled-form">
          <!-- 基础信息配置 -->
          <div class="form-row">
            <label>房源展示标题 *
              <input class="apple-input" v-model="form.title" required placeholder="如 502-水斗老围村阳台一房一厅"/>
            </label>
            <label>楼盘/小区名称 *
              <input class="apple-input" v-model="form.communityName" required placeholder="如 水斗老围村"/>
            </label>
          </div>

          <div class="form-row">
            <label>所属行政区 *
              <input class="apple-input" v-model="form.district" required placeholder="如 龙华区 / 南山区"/>
            </label>
            <label>详细地址 *
              <input class="apple-input" v-model="form.address" required placeholder="如 街道88号"/>
            </label>
          </div>

          <!-- 户型规格 -->
          <div class="form-row grid-4">
            <label>房号 <input class="apple-input" v-model="form.unitNo" placeholder="如 502"/></label>
            <label>室 <input class="apple-input" type="number" v-model="form.rooms" min="1" required/></label>
            <label>厅 <input class="apple-input" type="number" v-model="form.halls" min="0" required/></label>
            <label>面积 (㎡) <input class="apple-input" type="number" step="0.1" v-model="form.areaSqm" required/></label>
          </div>

          <!-- 租金押金配置 (直接填元) -->
          <div class="form-row">
            <label>月租金 (元/月) *
              <input class="apple-input price-input" type="number" v-model="form.rentYuan" required placeholder="1800"/>
            </label>
            <label>押金 (元) *
              <input class="apple-input price-input" type="number" v-model="form.depositYuan" required placeholder="1800"/>
            </label>
          </div>

          <!-- 封面图片选择 -->
          <div class="image-selector">
            <label>封面展示图 (点击快捷挑选预设房源美图)</label>
            <div class="image-grid">
              <div 
                v-for="(img, idx) in sampleImages" 
                :key="idx" 
                class="img-thumb" 
                :class="{ active: form.coverUrl === img }"
                @click="form.coverUrl = img"
              >
                <img :src="img" alt="room sample">
              </div>
            </div>
            <input class="apple-input" v-model="form.coverUrl" placeholder="或粘贴自定义图片 URL 地址" style="margin-top: 8px;"/>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-secondary" @click="showCreateModal = false">取消</button>
            <button type="submit" class="btn-primary" :disabled="submitting">
              {{ submitting ? '正在全自动建档发布中...' : '确认发布上架' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.listings-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-group {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.title-group h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.total-count {
  font-size: 13px;
  color: var(--apple-muted);
}

.list-card {
  padding: 0;
  overflow: hidden;
}

.apple-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.apple-table th {
  background: #fafafa;
  padding: 12px 20px;
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-muted);
  border-bottom: 1px solid var(--apple-border);
}

.apple-table td {
  padding: 14px 20px;
  border-bottom: 1px solid var(--apple-border);
  font-size: 14px;
  color: var(--apple-text);
  vertical-align: middle;
}

.apple-table tr:last-child td {
  border-bottom: none;
}

.id-cell {
  font-family: monospace;
  font-size: 12px;
  color: var(--apple-muted);
}

.title-text {
  font-weight: 600;
  color: #1a1a1a;
}

.price {
  color: var(--accent-green-hover);
  font-family: -apple-system, BlinkMacSystemFont, "SF Mono", monospace;
  font-weight: 700;
}

.unit-text {
  font-size: 12px;
  font-weight: 400;
  color: var(--apple-muted);
}

.badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid var(--apple-border);
}

.badge.neutral { 
  background: #f4f4f5; 
  color: #71717a; 
  border-color: #e4e4e7; 
}

.badge.published { 
  background: var(--accent-green-light); 
  color: var(--accent-green); 
  border-color: var(--accent-green-border); 
  font-weight: 600; 
}

.badge.offline { 
  background: #ffffff; 
  color: #a1a1aa; 
  border-color: #e4e4e7; 
}

.badge.special { 
  background: #fff7ed; 
  color: #ea580c; 
  border-color: #ffedd5; 
  font-weight: 600; 
}

.muted-text {
  color: #d4d4d8;
}

.action-btn-line {
  background: #ffffff;
  border: 1px solid var(--apple-border);
  color: var(--apple-text);
  border-radius: 5px;
  padding: 4px 12px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.15s ease;
}

.action-btn-line:hover {
  background: var(--accent-green-light);
  color: var(--accent-green);
  border-color: var(--accent-green-border);
}

.empty {
  padding: 40px;
  text-align: center;
  color: var(--apple-muted);
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-card {
  width: 100%;
  max-width: 620px;
  padding: 28px;
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.tip-badge {
  background: var(--accent-green-light);
  color: var(--accent-green);
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.styled-form { display: flex; flex-direction: column; gap: 16px; }
.form-row { display: flex; gap: 16px; }

.grid-4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.form-row label, .styled-form > label, .grid-4 label {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
}

.price-input {
  font-weight: 700;
  color: var(--accent-green-hover);
}

.image-selector label {
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 8px;
  display: block;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.img-thumb {
  height: 64px;
  border-radius: 6px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.15s;
}

.img-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.img-thumb:hover {
  opacity: 0.85;
}

.img-thumb.active {
  border-color: var(--accent-green);
  box-shadow: 0 0 0 2px rgba(32, 165, 58, 0.2);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}
</style>
