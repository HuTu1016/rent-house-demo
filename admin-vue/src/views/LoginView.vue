<script setup lang="ts">
import { ref } from 'vue'
import { useAdminStore } from '../stores/admin'

const store = useAdminStore()
const mobile = ref('13800000001')
const password = ref('123456')
const loading = ref(false)

async function handleLogin() {
  if (!mobile.value || !password.value) return
  loading.value = true
  await store.login(mobile.value, password.value)
  loading.value = false
}
</script>

<template>
  <div class="login-page">
    <div class="login-card card glass">
      <div class="logo-area">
        <h1>后台管理系统</h1>
        <p>登录以管理您的房产与房源</p>
      </div>
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label>手机号</label>
          <input class="apple-input" v-model.trim="mobile" type="tel" required placeholder="13800000001">
        </div>
        <div class="form-group">
          <label>密码</label>
          <input class="apple-input" v-model="password" type="password" required placeholder="••••••">
        </div>
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: url('https://images.unsplash.com/photo-1558002038-1055907df827?w=1600') center/cover;
  position: relative;
}
.login-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(245, 245, 247, 0.4);
  backdrop-filter: blur(10px);
}
.login-card {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 380px;
  padding: 40px;
  border-radius: 20px;
  text-align: center;
}
.logo-area h1 {
  font-size: 24px;
  font-weight: 800;
  margin: 0;
  letter-spacing: -0.5px;
}
.logo-area p {
  color: var(--apple-muted);
  font-size: 14px;
  margin: 8px 0 32px;
}
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.form-group {
  display: flex;
  flex-direction: column;
  text-align: left;
  gap: 8px;
}
.form-group label {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text);
}
.form-group input {
  height: 44px;
}
button {
  height: 44px;
  margin-top: 10px;
  font-size: 16px;
}
</style>
