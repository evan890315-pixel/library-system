<template>
  <div id="app-root">
    <!-- ── Navigation Bar ───────────────────────────────────────── -->
    <nav class="navbar">
      <div class="container navbar-inner">
        <router-link to="/" class="navbar-brand">
          <span class="brand-icon">📚</span>
          <span class="brand-text">典藏圖書館</span>
        </router-link>

        <div class="navbar-links">
          <router-link to="/" class="nav-link">書目瀏覽</router-link>
          <router-link v-if="auth.isLoggedIn" to="/my-books" class="nav-link">我的借閱</router-link>
        </div>

        <div class="navbar-auth">
          <template v-if="auth.isLoggedIn">
            <span class="user-name">{{ auth.user?.userName }}</span>
            <button class="btn btn-outline btn-sm" @click="handleLogout">登出</button>
          </template>
          <template v-else>
            <router-link to="/login"    class="btn btn-ghost btn-sm">登入</router-link>
            <router-link to="/register" class="btn btn-primary btn-sm">註冊</router-link>
          </template>
        </div>
      </div>
    </nav>

    <!-- ── Page Content ─────────────────────────────────────────── -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- ── Footer ───────────────────────────────────────────────── -->
    <footer class="site-footer">
      <div class="container footer-inner">
        <span class="footer-copy">© 2024 典藏圖書館 · Library Borrowing System</span>
        <span class="footer-stack font-mono">Vue 3 · Spring Boot · MySQL</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const auth   = useAuthStore()
const router = useRouter()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
/* ── Navbar ────────────────────────────────────────────────────────── */
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(245,240,232,.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--paper-deep);
}
.navbar-inner {
  display: flex;
  align-items: center;
  height: 64px;
  gap: 24px;
}

/* brand */
.navbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}
.brand-icon   { font-size: 1.4rem; line-height: 1; }
.brand-text   {
  font-family: var(--font-serif);
  font-size: 1.2rem;
  color: var(--ink);
  letter-spacing: .01em;
}

/* nav links */
.navbar-links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}
.nav-link {
  padding: 6px 14px;
  border-radius: var(--radius-sm);
  font-size: .88rem;
  font-weight: 500;
  color: var(--ink-soft);
  transition: var(--transition);
}
.nav-link:hover              { color: var(--ink); background: var(--paper-warm); }
.nav-link.router-link-active { color: var(--accent); background: rgba(139,37,0,.07); }

/* auth area */
.navbar-auth {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-name {
  font-size: .85rem;
  color: var(--ink-soft);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ── Main ────────────────────────────────────────────────────────────── */
.main-content {
  min-height: calc(100vh - 64px - 56px);
}

/* ── Footer ──────────────────────────────────────────────────────────── */
.site-footer {
  border-top: 1px solid var(--paper-deep);
  background: var(--paper-warm);
  height: 56px;
}
.footer-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}
.footer-copy, .footer-stack {
  font-size: .75rem;
  color: var(--ink-faint);
}
</style>
