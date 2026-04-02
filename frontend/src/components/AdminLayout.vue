<template>
  <div class="layout">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="leaf leaf-1">🍃</div>
      <div class="leaf leaf-2">🌿</div>
      <div class="leaf leaf-3">🌱</div>
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
    </div>

    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-texture" aria-hidden="true"></div>
      <div class="sidebar-header">
        <div class="admin-avatar" aria-hidden="true">
          <span class="admin-avatar-inner">{{ adminInitial }}</span>
        </div>
        <h2 class="admin-name">{{ userName }}</h2>
        <p class="admin-subtitle">系统管理平台</p>
      </div>
      <ul class="sidebar-menu">
        <li class="sidebar-item" :class="{ active: isActive('/admin/dashboard') }">
          <router-link to="/admin/dashboard" class="sidebar-link">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="7" height="7" rx="1.2" />
                <rect x="14" y="3" width="7" height="7" rx="1.2" />
                <rect x="3" y="14" width="7" height="7" rx="1.2" />
                <rect x="14" y="14" width="7" height="7" rx="1.2" />
              </svg>
            </span>
            <span class="menu-text">数据大屏</span>
          </router-link>
        </li>
        <li class="sidebar-item" :class="{ active: isActive('/admin/merchants') }">
          <router-link to="/admin/merchants" class="sidebar-link">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                <polyline points="9 22 9 12 15 12 15 22" />
              </svg>
            </span>
            <span class="menu-text">商户管理</span>
          </router-link>
        </li>
        <li class="sidebar-item" :class="{ active: isActive('/admin/communities') }">
          <router-link to="/admin/communities" class="sidebar-link">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <rect x="4" y="2" width="16" height="20" rx="2" />
                <path d="M9 22v-4h6v4" />
                <path d="M8 6h.01M12 6h.01M16 6h.01M8 10h.01M12 10h.01M16 10h.01M8 14h8" />
              </svg>
            </span>
            <span class="menu-text">社区管理</span>
          </router-link>
        </li>
        <li class="sidebar-item" :class="{ active: isActive('/admin/categories') }">
          <router-link to="/admin/categories" class="sidebar-link">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
              </svg>
            </span>
            <span class="menu-text">商品分类</span>
          </router-link>
        </li>
        <li class="sidebar-item" :class="{ active: isActive('/admin/orders') }">
          <router-link to="/admin/orders" class="sidebar-link">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
                <polyline points="10 9 9 9 8 9" />
              </svg>
            </span>
            <span class="menu-text">订单管理</span>
          </router-link>
        </li>
        <li class="sidebar-item" :class="{ active: isActive('/admin/notifications') }">
          <router-link to="/admin/notifications" class="sidebar-link notify-link">
            <span class="menu-icon">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                <path d="M13.73 21a2 2 0 0 1-3.46 0" />
              </svg>
            </span>
            <span class="menu-text">提醒中心</span>
            <span v-if="unreadCount > 0" class="notify-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </router-link>
        </li>
      </ul>
      <div class="sidebar-footer">
        <button type="button" @click="handleLogout" class="logout-btn">
          <svg class="logout-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
          <span class="logout-label">退出登录</span>
        </button>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
      <router-view></router-view>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getNotifications } from '@/api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)
let timer = null

const userName = computed(() => userStore.userInfo?.nickName || userStore.userInfo?.username || '管理员')

const adminInitial = computed(() => {
  const n = String(userName.value || '管').trim()
  return n.slice(0, 1).toUpperCase()
})

const getDismissedKey = () => {
  const uid = userStore.userInfo?.userId || 'unknown'
  return `notifications.dismissed.user.${uid}.type.${userStore.userType || 0}`
}

const loadUnreadCount = async () => {
  try {
    const res = await getNotifications()
    const list = res.data || []
    let dismissed = []
    try {
      dismissed = JSON.parse(localStorage.getItem(getDismissedKey()) || '[]')
    } catch (e) {
      dismissed = []
    }
    const hidden = new Set(dismissed)
    unreadCount.value = list.filter(x => !hidden.has(x.id)).length
  } catch (e) {
    unreadCount.value = 0
  }
}

onMounted(async () => {
  await loadUnreadCount()
  timer = setInterval(loadUnreadCount, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const isActive = (path) => {
  return route.path === path
}

const handleLogout = () => {
  userStore.logoutAction()
  router.push('/login')
}
</script>

<style scoped>
/* 主布局 */
.layout {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 50%, #fff8e1 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: fixed;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.leaf {
  position: absolute;
  font-size: 2em;
  opacity: 0.2;
  animation: float 6s ease-in-out infinite;
}

.leaf-1 { top: 10%; left: 5%; animation-delay: 0s; }
.leaf-2 { top: 60%; right: 10%; animation-delay: 1s; }
.leaf-3 { bottom: 20%; left: 15%; animation-delay: 2s; }

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: #4CAF50;
  top: -150px;
  right: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: #8BC34A;
  bottom: -100px;
  left: 20%;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-15px) rotate(5deg); }
}

/* ========== 侧边栏：灰绿森林质感 + 扁平导航 ========== */
.sidebar {
  --admin-accent: #5cad6e;
  --admin-accent-soft: rgba(92, 173, 110, 0.22);
  --admin-text-muted: rgba(232, 241, 236, 0.52);
  --admin-text: rgba(245, 250, 247, 0.88);
  width: 260px;
  background: linear-gradient(168deg, #4a5e54 0%, #3d4f48 38%, #354842 100%);
  box-shadow: 4px 0 32px rgba(20, 35, 30, 0.18);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10;
  flex-shrink: 0;
  color: var(--admin-text);
  overflow: hidden;
}

/* 森林感纹理：低对比有机噪点 + 斜向叶脉感线条 */
.sidebar-texture {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  opacity: 0.55;
  background-image:
    radial-gradient(ellipse 140% 90% at 10% 0%, rgba(129, 199, 132, 0.14) 0%, transparent 42%),
    radial-gradient(ellipse 100% 70% at 100% 100%, rgba(76, 175, 80, 0.1) 0%, transparent 48%),
    radial-gradient(circle at 30% 70%, rgba(255, 255, 255, 0.04) 0%, transparent 22%),
    repeating-linear-gradient(
      -28deg,
      transparent,
      transparent 14px,
      rgba(255, 255, 255, 0.018) 14px,
      rgba(255, 255, 255, 0.018) 15px
    ),
    repeating-linear-gradient(
      18deg,
      transparent,
      transparent 22px,
      rgba(0, 0, 0, 0.03) 22px,
      rgba(0, 0, 0, 0.03) 23px
    );
}

.sidebar-header {
  padding: 28px 20px 22px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
  z-index: 1;
}

.admin-avatar {
  width: 56px;
  height: 56px;
  margin: 0 auto 14px;
  border-radius: 50%;
  background: linear-gradient(145deg, rgba(129, 199, 132, 0.35), rgba(46, 125, 50, 0.45));
  border: 2px solid rgba(200, 230, 204, 0.35);
  box-shadow:
    0 4px 16px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.admin-avatar-inner {
  font-size: 1.35rem;
  font-weight: 700;
  color: #e8f5e9;
  letter-spacing: 0.02em;
  line-height: 1;
}

.admin-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.94);
  margin: 0 0 6px;
  letter-spacing: 0.03em;
}

.admin-subtitle {
  font-size: 0.8rem;
  margin: 0;
  color: var(--admin-text-muted);
  font-weight: 400;
  letter-spacing: 0.04em;
}

.sidebar-menu {
  flex: 1;
  list-style: none;
  padding: 16px 0;
  margin: 0;
  position: relative;
  z-index: 1;
}

.sidebar-item {
  margin: 2px 10px;
  opacity: 0;
  transform: translateX(-6px);
  animation: menu-fade-in 320ms ease forwards;
}

.sidebar-item:nth-child(1) { animation-delay: 30ms; }
.sidebar-item:nth-child(2) { animation-delay: 60ms; }
.sidebar-item:nth-child(3) { animation-delay: 90ms; }
.sidebar-item:nth-child(4) { animation-delay: 120ms; }
.sidebar-item:nth-child(5) { animation-delay: 150ms; }
.sidebar-item:nth-child(6) { animation-delay: 180ms; }

@keyframes menu-fade-in {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.sidebar-link {
  display: flex;
  align-items: center;
  padding: 12px 14px 12px 16px;
  margin: 0 2px;
  color: var(--admin-text-muted);
  text-decoration: none;
  border-radius: 10px;
  border-left: 3px solid transparent;
  transition:
    background 0.22s ease,
    color 0.22s ease,
    border-color 0.22s ease;
  font-weight: 500;
  font-size: 0.94rem;
  position: relative;
}

.sidebar-link:hover {
  background: rgba(255, 255, 255, 0.07);
  color: rgba(255, 255, 255, 0.82);
}

.menu-icon {
  margin-right: 12px;
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(129, 199, 132, 0.42);
  transition: color 0.22s ease;
}

.sidebar-link:hover .menu-icon {
  color: rgba(165, 214, 167, 0.75);
}

/* 选中：左侧细线 + 淡绿底，克制高亮 */
.sidebar-item.active .sidebar-link {
  background: var(--admin-accent-soft);
  border-left-color: var(--admin-accent);
  color: rgba(255, 255, 255, 0.95);
  font-weight: 600;
}

.sidebar-item.active .menu-icon {
  color: #a5d6a7;
}

.notify-link {
  position: relative;
}

.notify-badge {
  margin-left: auto;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: rgba(229, 115, 115, 0.95);
  color: #fff;
  font-size: 10px;
  line-height: 18px;
  text-align: center;
  font-weight: 700;
}

.menu-text {
  letter-spacing: 0.04em;
}

.sidebar-footer {
  padding: 16px 14px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
  z-index: 1;
}

.logout-btn {
  width: 100%;
  padding: 11px 16px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 10px;
  color: var(--admin-text-muted);
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.85);
  border-color: rgba(255, 255, 255, 0.18);
}

.logout-icon {
  opacity: 0.75;
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 30px;
  position: relative;
  z-index: 10;
  overflow-y: auto;
  background:
    radial-gradient(circle at 16% 10%, rgba(145, 201, 154, 0.22), transparent 26%),
    radial-gradient(circle at 88% 70%, rgba(248, 188, 124, 0.2), transparent 24%),
    linear-gradient(160deg, #f7faf5 0%, #f2f6f1 48%, #eef4ef 100%);
}

/* 通用卡片样式 */
.card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

/* 通用按钮样式 */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  font-size: 0.95em;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #4CAF50, #2E7D32);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(76, 175, 80, 0.3);
}

.btn-secondary {
  background: #E8F5E9;
  color: #2E7D32;
}

.btn-secondary:hover {
  background: #C8E6C9;
}

@media (max-width: 768px) {
  .sidebar {
    width: 80px;
  }

  .admin-name,
  .admin-subtitle {
    display: none;
  }

  .admin-avatar {
    width: 44px;
    height: 44px;
    margin-bottom: 8px;
  }

  .admin-avatar-inner {
    font-size: 1.1rem;
  }

  .menu-text {
    display: none;
  }

  .sidebar-link {
    justify-content: center;
    padding: 12px 10px;
  }

  .menu-icon {
    margin-right: 0;
  }

  .notify-badge {
    position: absolute;
    top: 6px;
    right: 6px;
    margin-left: 0;
  }

  .logout-label {
    display: none;
  }

  .logout-btn {
    padding: 12px;
  }
}
</style>
