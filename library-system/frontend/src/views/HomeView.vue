<template>
  <div class="home-page">
    <!-- ── Hero ─────────────────────────────────────────────────── -->
    <section class="hero">
      <div class="container hero-inner">
        <div class="hero-text">
          <p class="hero-label">典藏圖書館</p>
          <h1 class="hero-title">探索知識<br /><em>從這裡開始</em></h1>
          <p class="hero-desc">豐富館藏，線上借閱，隨時享受閱讀的樂趣。</p>
        </div>
        <div class="hero-deco" aria-hidden="true">
          <span class="deco-char">書</span>
        </div>
      </div>
    </section>

    <!-- ── Book Catalog ─────────────────────────────────────────── -->
    <section class="catalog">
      <div class="container">
        <!-- Toolbar -->
        <div class="catalog-toolbar">
          <h2 class="catalog-title">館藏書目</h2>
          <div class="toolbar-right">
            <input
              v-model="search"
              type="search"
              class="form-input search-input"
              placeholder="搜尋書名或作者…"
            />
          </div>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="state-box">
          <div class="spinner spinner-dark"></div>
          <span>載入中…</span>
        </div>

        <!-- Error -->
        <div v-else-if="loadError" class="alert alert-error">
          {{ loadError }}
        </div>

        <!-- Empty -->
        <div v-else-if="filteredBooks.length === 0" class="state-box">
          <span style="font-size:2rem">📭</span>
          <span class="text-muted">找不到符合的書籍</span>
        </div>

        <!-- Book Grid -->
        <div v-else class="book-grid">
          <article
            v-for="book in filteredBooks"
            :key="book.isbn"
            class="book-card card"
          >
            <!-- Cover placeholder -->
            <div class="book-cover" :style="coverStyle(book)">
              <span class="cover-letter">{{ book.name.charAt(0) }}</span>
            </div>

            <div class="book-body">
              <div class="book-meta">
                <span class="book-isbn font-mono">{{ book.isbn }}</span>
                <span
                  :class="['badge', book.availableCopies > 0 ? 'badge-available' : 'badge-none']"
                >
                  {{ book.availableCopies > 0 ? `可借 ${book.availableCopies} 冊` : '已借完' }}
                </span>
              </div>

              <h3 class="book-title">{{ book.name }}</h3>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-intro">{{ book.introduction }}</p>

              <div class="book-footer">
                <span class="book-copies text-muted">共 {{ book.totalCopies }} 冊</span>
                <button
                  class="btn btn-primary btn-sm"
                  :disabled="book.availableCopies === 0 || borrowingIsbn === book.isbn"
                  @click="handleBorrow(book)"
                >
                  <span v-if="borrowingIsbn === book.isbn" class="spinner"></span>
                  {{ borrowingIsbn === book.isbn ? '處理中…' : '借閱' }}
                </button>
              </div>
            </div>
          </article>
        </div>
      </div>
    </section>

    <!-- ── Borrow Result Modal ───────────────────────────────────── -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="modal.show" class="modal-overlay" @click.self="modal.show = false">
          <div class="modal-box card" role="dialog" aria-modal="true">
            <div class="modal-icon">{{ modal.success ? '✅' : '❌' }}</div>
            <h3 class="modal-title">{{ modal.title }}</h3>
            <p class="modal-msg">{{ modal.message }}</p>
            <div v-if="modal.success" class="modal-detail">
              <div class="detail-row">
                <span class="detail-label">書名</span>
                <span>{{ modal.bookName }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">應還日期</span>
                <span class="font-mono">{{ modal.dueDate }}</span>
              </div>
            </div>
            <button class="btn btn-primary" @click="modal.show = false">確認</button>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { bookApi, borrowApi } from '@/api'

const auth   = useAuthStore()
const router = useRouter()

const books        = ref([])
const loading      = ref(false)
const loadError    = ref('')
const search       = ref('')
const borrowingIsbn = ref('')

const modal = reactive({
  show: false, success: false,
  title: '', message: '', bookName: '', dueDate: ''
})

const filteredBooks = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return books.value
  return books.value.filter(b =>
    b.name.toLowerCase().includes(q) ||
    b.author.toLowerCase().includes(q)
  )
})

async function loadBooks() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await bookApi.getAll()
    books.value = res.data.data || []
  } catch {
    loadError.value = '無法載入書目，請稍後再試'
  } finally {
    loading.value = false
  }
}

async function handleBorrow(book) {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }
  borrowingIsbn.value = book.isbn
  try {
    const res = await borrowApi.borrow({ isbn: book.isbn })
    if (res.data.success) {
      // optimistic update
      book.availableCopies--
      const dueTs = new Date(Date.now() + 30 * 86400 * 1000)
      modal.success  = true
      modal.title    = '借閱成功 🎉'
      modal.message  = '請於應還日期前歸還書籍。'
      modal.bookName = book.name
      modal.dueDate  = dueTs.toLocaleDateString('zh-TW', { year:'numeric', month:'2-digit', day:'2-digit' })
    } else {
      modal.success = false
      modal.title   = '借閱失敗'
      modal.message = res.data.message
    }
  } catch (e) {
    modal.success = false
    modal.title   = '借閱失敗'
    modal.message = e.response?.data?.message || '發生錯誤，請稍後再試'
  } finally {
    borrowingIsbn.value = ''
    modal.show = true
  }
}

// deterministic pastel from ISBN
const PALETTE = ['#c9973a','#8b2500','#2d6a3f','#1a4a6e','#5a2d82','#7a3b1e']
function coverStyle(book) {
  const idx = book.isbn.split('').reduce((a, c) => a + c.charCodeAt(0), 0) % PALETTE.length
  return { background: PALETTE[idx] }
}

onMounted(loadBooks)
</script>

<style scoped>
/* ── Hero ────────────────────────────────────────────────────────────── */
.hero {
  background: linear-gradient(135deg, var(--paper-warm) 0%, var(--paper) 100%);
  border-bottom: 1px solid var(--paper-deep);
  padding: 64px 0 56px;
  overflow: hidden;
}
.hero-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
}
.hero-label {
  font-size: .75rem;
  font-weight: 700;
  letter-spacing: .15em;
  text-transform: uppercase;
  color: var(--accent);
  margin-bottom: 10px;
}
.hero-title {
  font-size: clamp(2.2rem, 5vw, 3.4rem);
  color: var(--ink);
  line-height: 1.1;
  margin-bottom: 16px;
}
.hero-title em { color: var(--accent); font-style: normal; }
.hero-desc { font-size: 1rem; color: var(--ink-soft); max-width: 380px; }
.hero-deco {
  flex-shrink: 0;
  width: 140px; height: 140px;
  background: var(--accent);
  border-radius: 24px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: var(--shadow-lg);
  transform: rotate(6deg);
}
.deco-char {
  font-family: var(--font-serif);
  font-size: 5rem;
  color: rgba(255,255,255,.9);
  line-height: 1;
}

/* ── Catalog ─────────────────────────────────────────────────────────── */
.catalog { padding: 48px 0 80px; }

.catalog-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  gap: 16px;
  flex-wrap: wrap;
}
.catalog-title { font-size: 1.5rem; }
.search-input  { width: 260px; }

.state-box {
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 12px;
  padding: 80px 0;
  color: var(--ink-faint);
}

/* ── Book Grid ───────────────────────────────────────────────────────── */
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.book-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: var(--transition);
}
.book-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.book-cover {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.cover-letter {
  font-family: var(--font-serif);
  font-size: 3rem;
  color: rgba(255,255,255,.8);
  line-height: 1;
}

.book-body {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.book-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.book-isbn { font-size: .72rem; color: var(--ink-faint); }

.book-title  { font-size: 1.05rem; line-height: 1.3; }
.book-author { font-size: .82rem; color: var(--ink-soft); }
.book-intro  {
  font-size: .82rem;
  color: var(--ink-faint);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.book-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid var(--paper-deep);
}
.book-copies { font-size: .78rem; }

/* ── Modal ───────────────────────────────────────────────────────────── */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(26,20,16,.5);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  z-index: 200;
  padding: 24px;
}
.modal-box {
  width: 100%; max-width: 400px;
  padding: 36px;
  text-align: center;
  display: flex; flex-direction: column; align-items: center; gap: 12px;
}
.modal-icon  { font-size: 2.4rem; }
.modal-title { font-size: 1.4rem; }
.modal-msg   { font-size: .9rem; color: var(--ink-soft); }

.modal-detail {
  width: 100%;
  background: var(--paper-warm);
  border-radius: var(--radius-sm);
  padding: 14px 16px;
  display: flex; flex-direction: column; gap: 8px;
  text-align: left;
  margin: 4px 0;
}
.detail-row   { display: flex; justify-content: space-between; align-items: center; gap: 8px; font-size: .88rem; }
.detail-label { color: var(--ink-faint); font-size: .78rem; font-weight: 600; text-transform: uppercase; letter-spacing: .05em; }

.modal-enter-active, .modal-leave-active { transition: opacity .2s, transform .2s; }
.modal-enter-from { opacity: 0; transform: scale(.95); }
.modal-leave-to   { opacity: 0; transform: scale(.95); }

@media (max-width: 600px) {
  .hero-deco { display: none; }
  .search-input { width: 100%; }
}
</style>
