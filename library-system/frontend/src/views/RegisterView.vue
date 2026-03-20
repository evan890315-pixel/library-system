<template>
  <div class="auth-page">
    <div class="auth-card card">
      <!-- Header -->
      <div class="auth-header">
        <div class="auth-icon">📖</div>
        <h1 class="auth-title">建立帳號</h1>
        <p class="auth-subtitle">加入典藏圖書館，開始您的閱讀之旅</p>
      </div>

      <!-- Alert -->
      <div v-if="alertMsg" :class="['alert', alertType]" role="alert">
        {{ alertMsg }}
      </div>

      <!-- Form -->
      <form class="auth-form" @submit.prevent="handleRegister" novalidate>
        <div class="form-group">
          <label class="form-label" for="userName">使用者名稱</label>
          <input
            id="userName"
            v-model.trim="form.userName"
            type="text"
            class="form-input"
            placeholder="請輸入您的名稱"
            maxlength="100"
            autocomplete="name"
          />
          <span v-if="errors.userName" class="form-error">{{ errors.userName }}</span>
        </div>

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
              placeholder="至少 8 個字元"
              autocomplete="new-password"
            />
            <button type="button" class="eye-btn" @click="showPwd = !showPwd"
                    :aria-label="showPwd ? '隱藏密碼' : '顯示密碼'">
              {{ showPwd ? '🙈' : '👁️' }}
            </button>
          </div>
          <span v-if="errors.password" class="form-error">{{ errors.password }}</span>
        </div>

        <button type="submit" class="btn btn-primary btn-lg submit-btn" :disabled="loading">
          <span v-if="loading" class="spinner" aria-hidden="true"></span>
          <span>{{ loading ? '註冊中…' : '立即註冊' }}</span>
        </button>
      </form>

      <div class="auth-footer">
        <span class="text-muted">已有帳號？</span>
        <router-link to="/login" class="text-accent">登入</router-link>
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
const alertType = ref('alert-info')

const form = reactive({ userName: '', phoneNumber: '', password: '' })
const errors = reactive({ userName: '', phoneNumber: '', password: '' })

function validate() {
  let valid = true
  errors.userName = errors.phoneNumber = errors.password = ''
  if (!form.userName.trim()) { errors.userName = '請輸入使用者名稱'; valid = false }
  if (!/^09\d{8}$/.test(form.phoneNumber)) { errors.phoneNumber = '請輸入有效的手機號碼（格式：09xxxxxxxx）'; valid = false }
  if (form.password.length < 8) { errors.password = '密碼至少需要 8 個字元'; valid = false }
  return valid
}

async function handleRegister() {
  if (!validate()) return
  loading.value = true
  alertMsg.value = ''
  try {
    const res = await auth.register({
      userName: form.userName,
      phoneNumber: form.phoneNumber,
      password: form.password
    })
    if (res.success) {
      alertType.value = 'alert-success'
      alertMsg.value = '✓ 註冊成功！即將跳轉到登入頁面…'
      setTimeout(() => router.push('/login'), 1500)
    } else {
      alertType.value = 'alert-error'
      alertMsg.value = res.message || '註冊失敗，請稍後再試'
    }
  } catch (e) {
    alertType.value = 'alert-error'
    alertMsg.value = e.response?.data?.message || '網路錯誤，請稍後再試'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}
.auth-card {
  width: 100%;
  max-width: 440px;
  padding: 40px;
}
.auth-header { text-align: center; margin-bottom: 28px; }
.auth-icon   { font-size: 2.4rem; margin-bottom: 12px; }
.auth-title  { font-size: 1.8rem; color: var(--ink); margin-bottom: 6px; }
.auth-subtitle { font-size: .88rem; color: var(--ink-faint); }

.alert { margin-bottom: 20px; }

.auth-form { display: flex; flex-direction: column; gap: 18px; }

.input-wrapper { position: relative; }
.input-wrapper .form-input { padding-right: 44px; width: 100%; }
.eye-btn {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
  background: none; border: none; padding: 0; font-size: 1rem; line-height: 1;
  color: var(--ink-faint); cursor: pointer;
}

.submit-btn { width: 100%; margin-top: 4px; }

.auth-footer {
  text-align: center;
  margin-top: 24px;
  font-size: .88rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
</style>
