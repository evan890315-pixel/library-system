<template>
  <div class="auth-page">
    <div class="auth-card card">
      <div class="auth-header">
        <div class="auth-icon">🔑</div>
        <h1 class="auth-title">歡迎回來</h1>
        <p class="auth-subtitle">請登入您的帳號</p>
      </div>

      <div v-if="alertMsg" :class="['alert', alertType]" role="alert">
        {{ alertMsg }}
      </div>

      <form class="auth-form" @submit.prevent="handleLogin" novalidate>
        <div class="form-group">
          <label class="form-label" for="phoneNumber">手機號碼</label>
          <input
            id="phoneNumber"
            v-model.trim="form.phoneNumber"
            type="tel"
            class="form-input"
            placeholder="09xxxxxxxx"
            autocomplete="tel"
          />
          <span v-if="errors.phoneNumber" class="form-error">{{ errors.phoneNumber }}</span>
        </div>

        <div class="form-group">
          <label class="form-label" for="password">密碼</label>
          <div class="input-wrapper">
            <input
              id="password"
              v-model="form.password"
              :type="showPwd ? 'text' : 'password'"
              class="form-input"
              placeholder="請輸入密碼"
              autocomplete="current-password"
            />
            <button type="button" class="eye-btn" @click="showPwd = !showPwd">
              {{ showPwd ? '🙈' : '👁️' }}
            </button>
          </div>
          <span v-if="errors.password" class="form-error">{{ errors.password }}</span>
        </div>

        <button type="submit" class="btn btn-primary btn-lg submit-btn" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          {{ loading ? '登入中…' : '登入' }}
        </button>
      </form>

      <div class="auth-footer">
        <span class="text-muted">還沒有帳號？</span>
        <router-link to="/register" class="text-accent">立即註冊</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth    = useAuthStore()
const router  = useRouter()
const loading = ref(false)
const showPwd = ref(false)
const alertMsg  = ref('')
const alertType = ref('alert-error')

const form   = reactive({ phoneNumber: '', password: '' })
const errors = reactive({ phoneNumber: '', password: '' })

function validate() {
  let valid = true
  errors.phoneNumber = errors.password = ''
  if (!form.phoneNumber.trim()) { errors.phoneNumber = '請輸入手機號碼'; valid = false }
  if (!form.password)           { errors.password    = '請輸入密碼';     valid = false }
  return valid
}

async function handleLogin() {
  if (!validate()) return
  loading.value = true
  alertMsg.value = ''
  try {
    const res = await auth.login({ phoneNumber: form.phoneNumber, password: form.password })
    if (res.success) {
      router.push('/')
    } else {
      alertType.value = 'alert-error'
      alertMsg.value  = res.message || '登入失敗'
    }
  } catch (e) {
    alertType.value = 'alert-error'
    alertMsg.value  = e.response?.data?.message || '網路錯誤，請稍後再試'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 120px);
  display: flex; align-items: center; justify-content: center;
  padding: 40px 24px;
}
.auth-card { width: 100%; max-width: 420px; padding: 40px; }
.auth-header { text-align: center; margin-bottom: 28px; }
.auth-icon   { font-size: 2.4rem; margin-bottom: 12px; }
.auth-title  { font-size: 1.8rem; margin-bottom: 6px; }
.auth-subtitle { font-size: .88rem; color: var(--ink-faint); }
.alert { margin-bottom: 20px; }
.auth-form { display: flex; flex-direction: column; gap: 18px; }
.input-wrapper { position: relative; }
.input-wrapper .form-input { width: 100%; padding-right: 44px; }
.eye-btn {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
  background: none; border: none; font-size: 1rem; color: var(--ink-faint); cursor: pointer;
}
.submit-btn { width: 100%; margin-top: 4px; }
.auth-footer {
  text-align: center; margin-top: 24px; font-size: .88rem;
  display: flex; align-items: center; justify-content: center; gap: 6px;
}
</style>
