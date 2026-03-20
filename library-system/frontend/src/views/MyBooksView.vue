<template>
  <div class="my-books-page">
    <div class="container">
      <!-- ── Page Header ─────────────────────────────────────────── -->
      <div class="page-header">
        <div>
          <h1 class="page-title">我的借閱紀錄</h1>
          <p class="page-subtitle">{{ auth.user?.userName }} 的借閱歷史</p>
        </div>
        <div class="header-stats" v-if="!loading && !loadError">
          <div class="stat-chip">
            <span class="stat-num">{{ activeRecords.length }}</span>
            <span class="stat-label">借閱中</span>
          </div>
          <div class="stat-chip stat-chip--muted">
            <span class="stat-num">{{ returnedRecords.length }}</span>
            <span class="stat-label">已歸還</span>
          </div>
        </div>
      </div>

      <!-- ── Tabs ───────────────────────────────────────────────── -->
      <div class="tab-bar">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          :class="['tab-btn', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
          <span class="tab-count">{{ tab.count }}</span>
        </button>
      </div>

      <!-- ── Loading ────────────────────────────────────────────── -->
      <div v-if="loading" class="state-box">
        <div class="spinner spinner-dark"></div>
        <span>載入中…</span>
      </div>

      <!-- ── Error ──────────────────────────────────────────────── -->
      <div v-else-if="loadError" class="alert alert-error">{{ loadError }}</div>

      <!-- ── Empty ──────────────────────────────────────────────── -->
      <div v-else-if="displayedRecords.length === 0" class="state-box">
        <span style="font-size:2.5rem">📚</span>
        <span class="text-muted">
          {{ activeTab === 'active' ? '目前沒有借閱中的書籍' : '尚無歸還紀錄' }}
        </span>
        <router-link to="/" class="btn btn-primary btn-sm mt-3">去瀏覽書目</router-link>
      </div>

      <!-- ── Records ────────────────────────────────────────────── -->
      <div v-else class="records-list">
        <div
          v-for="rec in displayedRecords"
          :key="rec.recordId"
          class="record-card card"
        >
          <!-- Cover -->
          <div class="rec-cover" :style="coverStyle(rec.isbn)">
            <span class="cover-letter">{{ rec.bookName?.charAt(0) }}</span>
          </div>

          <!-- Info -->
          <div class="rec-info">
            <div class="rec-top">
              <div>
                <h3 class="rec-title">{{ rec.bookName }}</h3>
                <p class="rec-author text-muted">{{ rec.bookAuthor }}</p>
              </div>
              <span :class="['badge', statusBadge(rec).cls]">
                {{ statusBadge(rec).label }}
              </span>
            </div>

            <div class="rec-dates">
              <div class="date-item">
                <span class="date-label">借閱日期</span>
                <span class="date-val font-mono">{{ formatDate(rec.borrowingTime) }}</span>
              </div>
              <div class="date-item" v-if="rec.dueDate && !rec.returnTime">
                <span class="date-label">應還日期</span>
                <span
                  :class="['date-val font-mono', isOverdue(rec.dueDate) ? 'text-error' : '']"
                >
                  {{ formatDate(rec.dueDate) }}
                  <span v-if="isOverdue(rec.dueDate)" class="overdue-tag">逾期</span>
                </span>
              </div>
              <div class="date-item" v-if="rec.returnTime">
                <span class="date-label">歸還日期</span>
                <span class="date-val font-mono text-success">{{ formatDate(rec.returnTime) }}</span>
              </div>
            </div>

            <div class="rec-meta">
              <span class="font-mono text-muted" style="font-size:.72rem">
                庫存 #{{ rec.inventoryId }} · {{ rec.location || '—' }}
              </span>
            </div>
          </div>

          <!-- Action -->
          <div class="rec-action" v-if="!rec.returnTime">
            <button
              class="btn btn-outline btn-sm"
              :disabled="returningId === rec.inventoryId"
              @click="handleReturn(rec)"
            >
              <span v-if="returningId === rec.inventoryId" class="spinner spinner-dark" style="width:14px;height:14px;border-width:1.5px"></span>
              {{ returningId === rec.inventoryId ? '處理中…' : '還書' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Return Result Toast ──────────────────────────────────── -->
    <transition name="toast">
      <div v-if="toast.show" :class="['toast', toast.success ? 'toast-success' : 'toast-error']">
        {{ toast.message }}
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { borrowApi } from '@/api'

const auth = useAuthStore()

const records    = ref([])
const loading    = ref(false)
const loadError  = ref('')
const returningId = ref(null)
const activeTab  = ref('active')

const toast = reactive({ show: false, success: true, message: '' })

const activeRecords  = computed(() => records.value.filter(r => !r.returnTime))
const returnedRecords = computed(() => records.value.filter(r => !!r.returnTime))

const tabs = computed(() => [
  { key: 'active',   label: '借閱中', count: activeRecords.value.length  },
  { key: 'returned', label: '已歸還', count: returnedRecords.value.length }
])

const displayedRecords = computed(() =>
  activeTab.value === 'active' ? activeRecords.value : returnedRecords.value
)

async function loadRecords() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await borrowApi.myBorrowings()
    records.value = res.data.data || []
  } catch {
    loadError.value = '無法載入借閱紀錄，請稍後再試'
  } finally {
    loading.value = false
  }
}

async function handleReturn(rec) {
  returningId.value = rec.inventoryId
  try {
    const res = await borrowApi.returnBook({ inventoryId: rec.inventoryId })
    if (res.data.success) {
      // Update local record
      const found = records.value.find(r => r.inventoryId === rec.inventoryId && !r.returnTime)
      if (found) found.returnTime = new Date().toISOString()
      showToast(true, `《${rec.bookName}》還書成功！`)
    } else {
      showToast(false, res.data.message || '還書失敗')
    }
  } catch (e) {
    showToast(false, e.response?.data?.message || '發生錯誤，請稍後再試')
  } finally {
    returningId.value = null
  }
}

function showToast(success, message) {
  toast.success = success
  toast.message = message
  toast.show    = true
  setTimeout(() => { toast.show = false }, 3500)
}

function statusBadge(rec) {
  if (rec.returnTime)               return { cls: 'badge-available', label: '已歸還' }
  if (isOverdue(rec.dueDate))       return { cls: 'badge-borrowed',  label: '逾期未還' }
  return { cls: 'badge-borrowed', label: '借閱中' }
}

function formatDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit'
  })
}

function isOverdue(dueIso) {
  if (!dueIso) return false
  return new Date(dueIso) < new Date()
}

const PALETTE = ['#c9973a','#8b2500','#2d6a3f','#1a4a6e','#5a2d82','#7a3b1e']
function coverStyle(isbn) {
  if (!isbn) return { background: PALETTE[0] }
  const idx = isbn.split('').reduce((a, c) => a + c.charCodeAt(0), 0) % PALETTE.length
  return { background: PALETTE[idx] }
}

onMounted(loadRecords)
</script>

<style scoped>
.my-books-page { padding: 48px 0 80px; }

/* ── Page Header ─────────────────────────────────────────────────────── */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}
.page-title    { font-size: 1.9rem; }
.page-subtitle { font-size: .88rem; color: var(--ink-faint); margin-top: 4px; }

.header-stats  { display: flex; gap: 12px; }
.stat-chip {
  display: flex; flex-direction: column; align-items: center;
  background: #fff;
  border: 1px solid var(--paper-deep);
  border-radius: var(--radius-md);
  padding: 10px 20px;
  min-width: 80px;
  box-shadow: var(--shadow-sm);
}
.stat-chip--muted .stat-num { color: var(--ink-faint); }
.stat-num   { font-family: var(--font-serif); font-size: 1.6rem; line-height: 1; color: var(--accent); }
.stat-label { font-size: .72rem; color: var(--ink-faint); margin-top: 2px; letter-spacing: .05em; }

/* ── Tabs ────────────────────────────────────────────────────────────── */
.tab-bar {
  display: flex; gap: 4px;
  border-bottom: 2px solid var(--paper-deep);
  margin-bottom: 28px;
}
.tab-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 20px;
  background: none; border: none;
  font-family: var(--font-body); font-size: .9rem;
  color: var(--ink-soft);
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: var(--transition);
  cursor: pointer;
}
.tab-btn:hover  { color: var(--ink); }
.tab-btn.active { color: var(--accent); border-bottom-color: var(--accent); font-weight: 600; }
.tab-count {
  background: var(--paper-warm);
  color: var(--ink-faint);
  font-size: .7rem; font-weight: 700;
  padding: 1px 7px;
  border-radius: 100px;
}
.tab-btn.active .tab-count { background: rgba(139,37,0,.1); color: var(--accent); }

/* ── State ───────────────────────────────────────────────────────────── */
.state-box {
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 12px; padding: 80px 0;
  color: var(--ink-faint);
}

/* ── Records ─────────────────────────────────────────────────────────── */
.records-list { display: flex; flex-direction: column; gap: 14px; }

.record-card {
  display: flex;
  align-items: stretch;
  overflow: hidden;
  transition: var(--transition);
}
.record-card:hover { box-shadow: var(--shadow-md); }

.rec-cover {
  width: 72px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.cover-letter {
  font-family: var(--font-serif);
  font-size: 1.8rem;
  color: rgba(255,255,255,.8);
}

.rec-info {
  flex: 1;
  padding: 16px 20px;
  display: flex; flex-direction: column; gap: 10px;
}

.rec-top {
  display: flex; align-items: flex-start;
  justify-content: space-between; gap: 12px;
}
.rec-title  { font-size: 1rem; line-height: 1.3; }
.rec-author { font-size: .82rem; margin-top: 2px; }

.rec-dates {
  display: flex; flex-wrap: wrap; gap: 16px;
}
.date-item  { display: flex; flex-direction: column; gap: 2px; }
.date-label { font-size: .68rem; font-weight: 700; text-transform: uppercase; letter-spacing: .06em; color: var(--ink-faint); }
.date-val   { font-size: .82rem; display: flex; align-items: center; gap: 6px; }

.overdue-tag {
  background: rgba(160,28,28,.12);
  color: var(--error);
  font-size: .65rem; font-weight: 700;
  padding: 1px 6px; border-radius: 100px;
  text-transform: uppercase; letter-spacing: .05em;
}

.rec-meta { margin-top: 2px; }

.rec-action {
  display: flex; align-items: center;
  padding: 0 16px;
  flex-shrink: 0;
  border-left: 1px solid var(--paper-deep);
}

/* ── Toast ───────────────────────────────────────────────────────────── */
.toast {
  position: fixed; bottom: 28px; left: 50%; transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: var(--radius-md);
  font-size: .9rem; font-weight: 600;
  box-shadow: var(--shadow-lg);
  z-index: 300; white-space: nowrap;
}
.toast-success { background: var(--success); color: #fff; }
.toast-error   { background: var(--error);   color: #fff; }

.toast-enter-active, .toast-leave-active { transition: opacity .25s, transform .25s; }
.toast-enter-from { opacity: 0; transform: translateX(-50%) translateY(12px); }
.toast-leave-to   { opacity: 0; transform: translateX(-50%) translateY(12px); }

@media (max-width: 600px) {
  .rec-action { padding: 12px 16px; border-left: none; border-top: 1px solid var(--paper-deep); }
  .record-card { flex-direction: column; }
  .rec-cover { width: 100%; height: 72px; }
}
</style>
