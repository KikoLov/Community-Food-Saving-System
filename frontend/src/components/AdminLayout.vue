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
      <div class="sidebar-header">
        <div class="logo" aria-label="品牌图标">
          <span class="logo-crown">👑</span>
          <span class="logo-building">🏢</span>
          <span class="logo-leaf">🍃</span>
        </div>
        <h2>管理员</h2>
        <p class="text-muted">系统管理平台</p>
      </div>
      <ul class="sidebar-menu">
        <li class="sidebar-item" :class="{ active: isActive('/admin/dashboard') }">
          <router-link to="/admin/dashboard" class="sidebar-link">
            <span class="menu-icon">📊</span>
            <span class="menu-text">数据大屏</span>
          </router-link>
        </li>
        <li class="sidebar-item" :class="{ active: isActive('/admin/merchants') }">
          <router-link to="/admin/merchants" class="sidebar-link">
            <span class="menu-icon">🏪</span>
            <span class="menu-text">商户管理</span>
          </router-link>
        </li>
        <li class="sidebar-item" :class="{ active: isActive('/admin/communities') }">
          <router-link to="/admin/communities" class="sidebar-link">
            <span class="menu-icon">🏢</span>
            <span class="menu-text">社区管理</span>
          </router-link>
        </li>
        <li class="sidebar-item" :class="{ active: isActive('/admin/categories') }">
          <router-link to="/admin/categories" class="sidebar-link">
            <span class="menu-icon">📁</span>
            <span class="menu-text">商品分类</span>
          </router-link>
        </li>
        <li class="sidebar-item" :class="{ active: isActive('/admin/orders') }">
          <router-link to="/admin/orders" class="sidebar-link">
            <span class="menu-icon">📋</span>
            <span class="menu-text">订单管理</span>
          </router-link>
        </li>
        <li class="sidebar-item" :class="{ active: isActive('/admin/notifications') }">
          <router-link to="/admin/notifications" class="sidebar-link notify-link">
            <span class="menu-icon">🔔</span>
            <span class="menu-text">提醒中心</span>
            <span v-if="unreadCount > 0" class="notify-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </router-link>
        </li>
      </ul>
      <div class="sidebar-footer">
        <button @click="handleLogout" class="logout-btn">
          <span>🚪</span> 退出登录
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
import { getNotificationUnreadCount } from '@/api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)
let timer = null

const userName = computed(() => userStore.userInfo?.nickName || '管理员')

const loadUnreadCount = async () => {
  try {
    const res = await getNotificationUnreadCount()
    unreadCount.value = res.data?.count || 0
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

/* 侧边栏 */
.sidebar {
  width: 260px;
  background: linear-gradient(180deg, #0b4329 0%, #0e5a37 42%, #176b45 100%);
  box-shadow: 4px 0 24px rgba(18, 58, 37, 0.28);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10;
  flex-shrink: 0;
  color: #eef7f0;
  overflow: hidden;
}

.sidebar::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 18% 10%, rgba(167, 224, 189, 0.2), transparent 32%),
    radial-gradient(circle at 88% 82%, rgba(242, 169, 92, 0.19), transparent 28%),
    repeating-linear-gradient(130deg, rgba(255, 255, 255, 0.035) 0 2px, transparent 2px 10px);
  pointer-events: none;
}

.sidebar-header {
  padding: 30px 20px;
  text-align: center;
  border-bottom: 1px solid rgba(206, 231, 214, 0.24);
  position: relative;
  z-index: 1;
}

.logo {
  width: 86px;
  height: 86px;
  margin: 0 auto 8px;
  border-radius: 24px;
  background: linear-gradient(145deg, #1a6d45, #0f4f31);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.25),
    inset 0 -10px 18px rgba(0, 0, 0, 0.15),
    0 12px 24px rgba(9, 40, 24, 0.3);
  position: relative;
  margin-bottom: 10px;
  display: grid;
  place-items: center;
  color: #e8f7ea;
}

.logo-crown {
  position: absolute;
  top: 8px;
  font-size: 1rem;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.25));
}

.logo-leaf {
  position: absolute;
  font-size: 1.1rem;
  top: 40px;
  left: 46px;
}

.logo-building {
  font-size: 1.7rem;
  transform: translateY(8px);
}

.sidebar-header h2 {
  font-size: 1.3em;
  color: #f5fbf6;
  margin: 10px 0 5px;
  letter-spacing: 0.4px;
}

.sidebar-header p {
  font-size: 0.85em;
  margin: 0;
  color: rgba(232, 245, 236, 0.76);
}

.sidebar-menu {
  flex: 1;
  list-style: none;
  padding: 20px 0;
  margin: 0;
}

.sidebar-item {
  margin: 5px 12px;
}

.sidebar-link {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  color: rgba(241, 250, 243, 0.9);
  text-decoration: none;
  border-radius: 14px;
  transition: all 0.3s ease;
  font-weight: 500;
  border: 1px solid transparent;
}

.sidebar-link:hover {
  background: rgba(214, 241, 222, 0.12);
  color: #fff;
  border-color: rgba(189, 235, 204, 0.18);
}

.sidebar-item.active .sidebar-link {
  background: linear-gradient(135deg, #38ff61, #12df48);
  color: #07381f;
  border-color: rgba(210, 255, 221, 0.9);
  box-shadow:
    inset 0 0 0 1px rgba(234, 255, 240, 0.45),
    0 0 22px rgba(64, 255, 119, 0.45);
  font-weight: 700;
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
  background: #ff6b6b;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  font-weight: 700;
}

.menu-icon {
  font-size: 1.3em;
  margin-right: 12px;
  width: 20px;
  text-align: center;
}

.menu-text {
  font-size: 1em;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(206, 231, 214, 0.24);
  position: relative;
  z-index: 1;
}

.logout-btn {
  width: 100%;
  padding: 12px 20px;
  background: rgba(255, 214, 177, 0.2);
  border: none;
  border-radius: 12px;
  color: #ffe4cf;
  font-size: 1em;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: rgba(255, 196, 144, 0.28);
  transform: translateY(-2px);
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

/* 响应式 */
@media (max-width: 768px) {
  .sidebar {
    width: 80px;
  }

  .sidebar-header h2,
  .sidebar-header p,
  .menu-text {
    display: none;
  }

  .sidebar-link {
    justify-content: center;
    padding: 14px;
  }

  .menu-icon {
    margin-right: 0;
  }

  .logout-btn span:first-child {
    margin-right: 0;
  }

  .logout-btn span:last-child {
    display: none;
  }
}
</style>
