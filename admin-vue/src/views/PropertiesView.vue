<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { createBuilding, createUnit } from '../services/api'
import { useAdminStore } from '../stores/admin'
import { usePropertyStore } from '../stores/properties'

const router = useRouter()
const store = useAdminStore()
const propStore = usePropertyStore()

const activeTab = ref('list') // 'list' | 'building' | 'unit'

const buildingForm = ref({ name: '', address: '' })
const unitForm = ref({ buildingId: '', unitNo: '', title: '', rooms: 1, halls: 1, bathrooms: 1, areaSqm: 45 })

async function submitBuilding() {
  try {
    const id = await createBuilding(buildingForm.value.name, buildingForm.value.address)
    propStore.addBuilding({
      id,
      name: buildingForm.value.name,
      address: buildingForm.value.address,
      createdAt: new Date().toLocaleDateString()
    })
    store.notify('楼栋创建成功！ID已自动保存')
    buildingForm.value = { name: '', address: '' }
    activeTab.value = 'list'
  } catch (e) {
    store.notify(e instanceof Error ? e.message : '创建失败')
  }
}

async function submitUnit() {
  if (!unitForm.value.buildingId) {
    store.notify('请选择或输入楼栋 ID')
    return
  }
  try {
    const selectedBuilding = propStore.buildings.find(b => b.id === unitForm.value.buildingId)
    const buildingName = selectedBuilding ? selectedBuilding.name : '未知楼栋'

    const id = await createUnit(
      unitForm.value.buildingId,
      unitForm.value.unitNo,
      unitForm.value.title,
      unitForm.value.rooms,
      unitForm.value.halls,
      unitForm.value.bathrooms,
      unitForm.value.areaSqm
    )
    
    propStore.addUnit({
      id,
      buildingId: unitForm.value.buildingId,
      buildingName,
      unitNo: unitForm.value.unitNo,
      title: unitForm.value.title,
      rooms: unitForm.value.rooms,
      halls: unitForm.value.halls,
      bathrooms: unitForm.value.bathrooms,
      areaSqm: unitForm.value.areaSqm,
      createdAt: new Date().toLocaleDateString()
    })

    store.notify('房间单元创建成功！ID已自动保存')
    unitForm.value.unitNo = ''
    unitForm.value.title = ''
    activeTab.value = 'list'
  } catch (e) {
    store.notify(e instanceof Error ? e.message : '创建失败')
  }
}

function publishUnitListing(unit: any) {
  router.push({
    path: '/listings',
    query: {
      unitId: unit.id,
      title: `${unit.unitNo}-${unit.title}`,
      communityName: unit.buildingName,
      address: unit.buildingName
    }
  })
}
</script>

<template>
  <div class="properties-view">
    <div class="top-header">
      <div>
        <h2>房产资产建档管理中心</h2>
        <p class="subtitle">录入管理物理楼盘与具体单元，一键联动发布为租赁房源。</p>
      </div>
      <div class="tabs">
        <button :class="{ active: activeTab === 'list' }" @click="activeTab = 'list'">资产清单列表</button>
        <button :class="{ active: activeTab === 'building' }" @click="activeTab = 'building'">+ 录入楼栋</button>
        <button :class="{ active: activeTab === 'unit' }" @click="activeTab = 'unit'">+ 录入房间单元</button>
      </div>
    </div>

    <!-- 列表模式 -->
    <div v-if="activeTab === 'list'" class="list-section">
      <div class="card list-card">
        <h3>已录入的物理房间单元 ({{ propStore.units.length }})</h3>
        <table v-if="propStore.units.length" class="apple-table">
          <thead>
            <tr>
              <th>单元 ID</th>
              <th>所属楼盘</th>
              <th>房号</th>
              <th>内部命名</th>
              <th>户型规格</th>
              <th>面积</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="unit in propStore.units" :key="unit.id">
              <td class="id-code">{{ unit.id }}</td>
              <td><strong>{{ unit.buildingName }}</strong></td>
              <td>{{ unit.unitNo }}</td>
              <td>{{ unit.title }}</td>
              <td>{{ unit.rooms }}房{{ unit.halls }}厅{{ unit.bathrooms }}卫</td>
              <td>{{ unit.areaSqm }} ㎡</td>
              <td>
                <button class="btn-primary-sm" @click="publishUnitListing(unit)">
                  🚀 对此房间发布房源
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-box">
          <p>暂无房间记录。建议直接在顶部点击 <strong>“+ 录入楼栋”</strong> 或在 <strong>“房源运营”</strong> 中体验一体化全自动发布！</p>
        </div>
      </div>
    </div>

    <!-- 添加楼栋 -->
    <div v-if="activeTab === 'building'" class="card form-card">
      <h3>录入楼栋/小区资产</h3>
      <form @submit.prevent="submitBuilding" class="styled-form">
        <label>楼盘/小区名称
          <input class="apple-input" v-model="buildingForm.name" required placeholder="如：水斗老围村 10栋" />
        </label>
        <label>详细地址
          <input class="apple-input" v-model="buildingForm.address" required placeholder="如：深圳市龙华区水斗街道88号" />
        </label>
        <div class="form-actions">
          <button type="button" class="btn-secondary" @click="activeTab = 'list'">取消</button>
          <button type="submit" class="btn-primary">保存楼栋</button>
        </div>
      </form>
    </div>

    <!-- 添加房间单元 -->
    <div v-if="activeTab === 'unit'" class="card form-card">
      <h3>录入具体房间单元</h3>
      <form @submit.prevent="submitUnit" class="styled-form">
        <label>选择所属楼盘
          <select v-if="propStore.buildings.length" class="apple-input" v-model="unitForm.buildingId" required>
            <option value="" disabled>-- 请选择已知楼盘 --</option>
            <option v-for="b in propStore.buildings" :key="b.id" :value="b.id">
              {{ b.name }} (ID: {{ b.id }})
            </option>
          </select>
          <input v-else class="apple-input" v-model="unitForm.buildingId" required placeholder="请输入 Building ID (如 343331585782910976)" />
        </label>

        <div class="form-row">
          <label>房号 <input class="apple-input" v-model="unitForm.unitNo" required placeholder="如 502" /></label>
          <label>内部房型命名 <input class="apple-input" v-model="unitForm.title" required placeholder="如 阳台一房一厅" /></label>
        </div>

        <div class="form-row">
          <label>室 <input class="apple-input" type="number" v-model="unitForm.rooms" min="1" required /></label>
          <label>厅 <input class="apple-input" type="number" v-model="unitForm.halls" min="0" required /></label>
          <label>卫 <input class="apple-input" type="number" v-model="unitForm.bathrooms" min="1" required /></label>
          <label>面积(㎡) <input class="apple-input" type="number" step="0.1" v-model="unitForm.areaSqm" required /></label>
        </div>

        <div class="form-actions">
          <button type="button" class="btn-secondary" @click="activeTab = 'list'">取消</button>
          <button type="submit" class="btn-primary">保存房间单元</button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.properties-view {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.top-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.top-header h2 {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  font-size: 13px;
  color: var(--apple-muted);
}

.tabs {
  display: flex;
  gap: 8px;
}

.tabs button {
  background: #ffffff;
  border: 1px solid var(--apple-border);
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  color: var(--apple-text);
  transition: all 0.15s;
}

.tabs button.active {
  background: var(--accent-green);
  color: #ffffff;
  border-color: var(--accent-green);
  font-weight: 600;
}

.list-card {
  padding: 0;
  overflow: hidden;
}

.list-card h3 {
  padding: 16px 20px;
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  border-bottom: 1px solid var(--apple-border);
  background: #fafafa;
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
}

.id-code {
  font-family: monospace;
  font-size: 12px;
  color: var(--apple-muted);
}

.btn-primary-sm {
  background: var(--accent-green-light);
  color: var(--accent-green);
  border: 1px solid var(--accent-green-border);
  border-radius: 5px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.15s;
}

.btn-primary-sm:hover {
  background: var(--accent-green);
  color: #ffffff;
  border-color: var(--accent-green);
}

.form-card {
  max-width: 640px;
}

.form-card h3 {
  margin: 0 0 20px;
  font-size: 17px;
  font-weight: 700;
}

.styled-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.styled-form label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-row label {
  flex: 1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}

.empty-box {
  padding: 40px;
  text-align: center;
  color: var(--apple-muted);
  font-size: 14px;
}
</style>
