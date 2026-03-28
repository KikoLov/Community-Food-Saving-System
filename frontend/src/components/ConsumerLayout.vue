<template>
  <div class="consumer-layout">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="leaf leaf-1">🍃</div>
      <div class="leaf leaf-2">🌿</div>
      <div class="leaf leaf-3">🌱</div>
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
    </div>

    <!-- Navbar -->
    <nav class="navbar">
      <div class="navbar-content">
        <router-link to="/consumer/products" class="navbar-brand">
          <span class="brand-icon">🌿</span>
          <span class="brand-text">临期食品平台</span>
        </router-link>
        <div class="navbar-menu">
          <router-link to="/consumer/products" class="navbar-item">
            <span class="nav-icon">🛍</span>
            <span class="nav-text">商品大厅</span>
          </router-link>
          <router-link to="/consumer/cart" class="navbar-item">
            <span class="nav-icon">🛒</span>
            <span class="nav-text">购物车</span>
          </router-link>
          <router-link to="/consumer/orders" class="navbar-item">
            <span class="nav-icon">📋</span>
            <span class="nav-text">我的订单</span>
          </router-link>
          <router-link to="/consumer/carbon" class="navbar-item">
            <span class="nav-icon">🌿</span>
            <span class="nav-text">低碳中心</span>
          </router-link>
          <router-link to="/consumer/gamification" class="navbar-item">
            <span class="nav-icon">🪙</span>
            <span class="nav-text">碳积分商城</span>
          </router-link>
          <router-link to="/consumer/profile" class="navbar-item">
            <span class="nav-icon">👤</span>
            <span class="nav-text">个人中心</span>
          </router-link>
          <router-link to="/consumer/notifications" class="navbar-item notify-item">
            <span class="nav-icon">🔔</span>
            <span class="nav-text">提醒</span>
            <span v-if="unreadCount > 0" class="notify-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </router-link>
          <a @click="handleLogout" class="navbar-item logout">
            <span class="nav-icon">🚪</span>
            <span class="nav-text">退出</span>
          </a>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="main-content">
      <router-view></router-view>
    </main>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-content">
        <p>社区临期食品低碳减损系统</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getNotificationUnreadCount } from '@/api/notification'

const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)
let timer = null

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

const handleLogout = () => {
  userStore.logoutAction()
  router.push('/login')
}
</script>

<style scoped>
/* 主布局 */
.consumer-layout {
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 50%, #fff8e1 100%);
  position: relative;
  display: flex;
  flex-direction: column;
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

.leaf-1 { top: 15%; left: 3%; animation-delay: 0s; }
.leaf-2 { top: 70%; right: 5%; animation-delay: 1s; }
.leaf-3 { bottom: 25%; left: 10%; animation-delay: 2s; }

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}

.circle-1 {
  width: 500px;
  height: 500px;
  background: #4CAF50;
  top: -200px;
  right: -150px;
}

.circle-2 {
  width: 350px;
  height: 350px;
  background: #8BC34A;
  bottom: -150px;
  left: -100px;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-15px) rotate(5deg); }
}

/* 导航栏 */
.navbar {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 20px rgba(18, 73, 45, 0.08);
  position: relative;
  z-index: 100;
  border-bottom: 1px solid rgba(207, 227, 212, 0.7);
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.navbar-brand {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #2E7D32;
  font-weight: 600;
}

.brand-icon {
  font-size: 2em;
  margin-right: 10px;
}

.brand-text {
  font-size: 1.3em;
}

.navbar-menu {
  display: flex;
  align-items: center;
  gap: 8px;
}

.navbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 78px;
  padding: 8px 12px 7px;
  color: #555;
  text-decoration: none;
  border-radius: 14px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.navbar-item:hover {
  background: rgba(129, 199, 132, 0.18);
  color: #215e2d;
}

.navbar-item.router-link-active {
  background: linear-gradient(135deg, #38ff61, #12df48);
  color: #063b20;
  box-shadow: 0 0 18px rgba(64, 255, 119, 0.4);
}

.navbar-item.logout {
  color: #E65100;
  margin-left: 10px;
}

.navbar-item.logout:hover {
  background: #FFF3E0;
  color: #E65100;
}

.notify-item {
  position: relative;
}

.notify-badge {
  position: absolute;
  top: 4px;
  right: 6px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background: #e53935;
  color: #fff;
  font-size: 10px;
  line-height: 16px;
  text-align: center;
  font-weight: 700;
}

.nav-icon {
  font-size: 1.2em;
  line-height: 1;
}

.nav-text {
  font-size: 0.78em;
  margin-top: 4px;
  line-height: 1.2;
  letter-spacing: 0.2px;
  white-space: nowrap;
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 30px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  position: relative;
  z-index: 10;
  background:
    radial-gradient(circle at 16% 10%, rgba(145, 201, 154, 0.2), transparent 26%),
    radial-gradient(circle at 88% 70%, rgba(248, 188, 124, 0.16), transparent 24%),
    linear-gradient(160deg, #f7faf5 0%, #f2f6f1 48%, #eef4ef 100%);
  border-radius: 16px;
  margin-top: 14px;
  margin-bottom: 14px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.52);
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
  border-radius: 12px;
  font-size: 0.95em;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.22s ease;
  box-shadow:
    0 8px 16px rgba(15, 73, 41, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.36);
}

.btn-primary {
  background: linear-gradient(135deg, #4CAF50, #2E7D32);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(76, 175, 80, 0.3);
}

.btn:active {
  transform: translateY(0);
  box-shadow:
    inset 0 3px 8px rgba(0, 0, 0, 0.18),
    0 2px 8px rgba(15, 73, 41, 0.12);
}

.btn-secondary {
  background: #E8F5E9;
  color: #2E7D32;
}

.btn-secondary:hover {
  background: #C8E6C9;
}

/* 页脚 */
.footer {
  background: linear-gradient(135deg, #1f6b3d, #144f2e);
  color: white;
  padding: 25px 0;
  margin-top: auto;
  position: relative;
  z-index: 10;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  text-align: center;
}

.footer-content p {
  margin: 0;
  font-size: 0.95em;
}

/* 响应式 */
@media (max-width: 1024px) {
  .navbar-item {
    min-width: 64px;
    padding: 7px 8px 6px;
  }

  .brand-text {
    display: none;
  }

  .nav-text {
    font-size: 0.72em;
  }
}

@media (max-width: 768px) {
  .navbar-content {
    padding: 0 15px;
    height: 60px;
  }

  .main-content {
    padding: 15px;
  }

  .navbar-item {
    min-width: 54px;
    padding: 6px 6px 5px;
  }

  .nav-icon {
    font-size: 1.18em;
  }

  .nav-text {
    font-size: 0.66em;
    margin-top: 3px;
  }
}
</style>
