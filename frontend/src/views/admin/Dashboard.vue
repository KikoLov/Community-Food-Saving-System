<template>
  <div class="dashboard-page">
    <h4 class="mb-1"><i class="fas fa-chart-line me-2"></i>管理员经营看板</h4>
    <p class="text-muted mb-4">欢迎回来，{{ userName }}。这里展示平台运营关键指标。</p>

    <div class="row g-3 mb-3">
      <div class="col-md-3" v-for="item in statCards" :key="item.label">
        <div class="stat-card" :class="item.tone">
          <div class="stat-top">
            <div class="stat-label">{{ item.label }}</div>
            <span class="stat-icon">{{ item.icon }}</span>
          </div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-art">{{ item.art }}</div>
          <div class="shine-layer"></div>
        </div>
      </div>
    </div>

    <div class="row g-3 mb-4">
      <div class="col-md-6">
        <div class="panel-card h-100">
          <div class="panel-title">异常订单占比</div>
          <div class="ratio-ring-wrap">
            <div class="ratio-ring" :style="{ '--p': `${abnormalRate}` }">
              <span>{{ abnormalRate }}%</span>
            </div>
          </div>
          <small class="text-muted d-block mt-2">异常订单 = 已取消 + 已过期</small>
        </div>
      </div>
      <div class="col-md-6">
        <div class="panel-card h-100">
          <div class="panel-title">今日运行状态</div>
          <p class="mb-1"><strong>后端服务：</strong><span class="ok-dot">● 正常运行</span></p>
          <p class="mb-1"><strong>数据库：</strong><span class="ok-dot">● 已连接</span></p>
          <p class="mb-0"><strong>系统时间：</strong>{{ currentTime }}</p>
        </div>
      </div>
    </div>

    <div class="panel-card mb-4">
      <div class="d-flex justify-content-between align-items-center mb-2">
        <div class="panel-title mb-0">近 7 天平台订单趋势</div>
        <small class="text-muted">绿色=订单数，橙色=核销数</small>
      </div>
      <div v-if="trendMax > 0" class="wave-chart-wrap">
        <svg class="wave-chart" viewBox="0 0 760 220" preserveAspectRatio="none">
          <defs>
            <linearGradient id="orderFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#73d47f" stop-opacity="0.95" />
              <stop offset="100%" stop-color="#73d47f" stop-opacity="0.12" />
            </linearGradient>
            <linearGradient id="verifyFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#ffb75f" stop-opacity="0.95" />
              <stop offset="100%" stop-color="#ffb75f" stop-opacity="0.12" />
            </linearGradient>
            <linearGradient id="orderStroke" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stop-color="#58bd65" />
              <stop offset="100%" stop-color="#2d8f40" />
            </linearGradient>
            <linearGradient id="verifyStroke" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stop-color="#ffbe72" />
              <stop offset="100%" stop-color="#f28e2d" />
            </linearGradient>
          </defs>
          <path :d="orderAreaPath" fill="url(#orderFill)"></path>
          <path :d="verifyAreaPath" fill="url(#verifyFill)"></path>
          <path :d="orderLinePath" stroke="url(#orderStroke)" stroke-width="3.2" fill="none" stroke-linecap="round"></path>
          <path :d="verifyLinePath" stroke="url(#verifyStroke)" stroke-width="3.2" fill="none" stroke-linecap="round"></path>
        </svg>
        <div class="wave-axis">
          <span v-for="point in orderTrend7d" :key="point.date">{{ shortDate(point.date) }}</span>
        </div>
      </div>
      <div v-else class="text-muted">暂无趋势数据</div>
    </div>

    <div class="panel-card">
      <div class="panel-title mb-3">快捷操作</div>
      <div class="row g-2">
        <div class="col-md-3"><button @click="go('/admin/merchants')" class="btn btn-primary w-100">商户管理</button></div>
        <div class="col-md-3"><button @click="go('/admin/communities')" class="btn btn-success w-100">社区管理</button></div>
        <div class="col-md-3"><button @click="go('/admin/categories')" class="btn btn-warning w-100">分类管理</button></div>
        <div class="col-md-3"><button @click="go('/admin/orders')" class="btn btn-danger w-100">订单管理</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getAllOrders, getDashboardStats } from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()

const userName = computed(() => userStore.userInfo?.nickName || '管理员')
const currentTime = ref('')
const stats = ref({})
const allOrders = ref([])
const orderTrend7d = ref([])
let timer = null

const statCards = computed(() => {
  const totalAmount = Number(stats.value.totalTransactionAmount || 0).toFixed(2)
  const totalCarbon = Number(stats.value.totalCarbonSaved || 0).toFixed(1)
  const totalFood = Number(stats.value.totalFoodSaved || 0).toFixed(1)
  const totalOrders = allOrders.value.length
  return [
    { label: '交易总额', value: `¥${totalAmount}`, icon: '¥', art: '💰', tone: 'tone-money' },
    { label: '碳减排总量', value: `${totalCarbon} kg`, icon: 'CO2', art: '🌱', tone: 'tone-carbon' },
    { label: '挽救食品总量', value: `${totalFood} kg`, icon: 'SAVE', art: '🧺', tone: 'tone-food' },
    { label: '订单总数', value: totalOrders, icon: 'ORD', art: '📦', tone: 'tone-order' }
  ]
})

const abnormalRate = computed(() => {
  const total = allOrders.value.length
  if (!total) return 0
  const abnormal = allOrders.value.filter(x => x.orderStatus === 2 || x.orderStatus === 3).length
  return Number((abnormal * 100 / total).toFixed(2))
})

const trendMax = computed(() => {
  if (!orderTrend7d.value.length) return 0
  return orderTrend7d.value.reduce((m, x) => Math.max(m, x.orderCount, x.verifiedCount), 0)
})

const trendPoints = computed(() => {
  const list = orderTrend7d.value
  if (!list.length || !trendMax.value) return []
  const width = 760
  const height = 190
  const baseY = 182
  const step = width / Math.max(1, (list.length - 1))
  return list.map((p, idx) => {
    const x = idx * step
    const oy = baseY - (p.orderCount / trendMax.value) * height
    const vy = baseY - (p.verifiedCount / trendMax.value) * height
    return { x, oy, vy }
  })
})

const buildSmoothPath = (key) => {
  const pts = trendPoints.value
  if (!pts.length) return ''
  const yKey = key === 'order' ? 'oy' : 'vy'
  let d = `M ${pts[0].x} ${pts[0][yKey]}`
  for (let i = 1; i < pts.length; i++) {
    const prev = pts[i - 1]
    const curr = pts[i]
    const cx = (prev.x + curr.x) / 2
    d += ` Q ${cx} ${prev[yKey]} ${curr.x} ${curr[yKey]}`
  }
  return d
}

const orderLinePath = computed(() => buildSmoothPath('order'))
const verifyLinePath = computed(() => buildSmoothPath('verify'))
const orderAreaPath = computed(() => {
  if (!orderLinePath.value) return ''
  return `${orderLinePath.value} L 760 182 L 0 182 Z`
})
const verifyAreaPath = computed(() => {
  if (!verifyLinePath.value) return ''
  return `${verifyLinePath.value} L 760 182 L 0 182 Z`
})

const updateTime = () => {
  currentTime.value = new Date().toLocaleString('zh-CN')
}

const buildTrend = () => {
  const arr = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    d.setHours(0, 0, 0, 0)
    const dayStart = d.getTime()
    const dayEnd = dayStart + 24 * 60 * 60 * 1000
    const orderCount = allOrders.value.filter(o => {
      const t = new Date(o.createTime).getTime()
      return t >= dayStart && t < dayEnd
    }).length
    const verifiedCount = allOrders.value.filter(o => {
      if (!o.verifyTime) return false
      const t = new Date(o.verifyTime).getTime()
      return t >= dayStart && t < dayEnd
    }).length
    arr.push({
      date: new Date(dayStart).toISOString().slice(0, 10),
      orderCount,
      verifiedCount
    })
  }
  orderTrend7d.value = arr
}

const shortDate = (dateStr) => {
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const go = (path) => router.push(path)

onMounted(async () => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  const [statsRes, ordersRes] = await Promise.all([getDashboardStats(), getAllOrders()])
  stats.value = statsRes.data || {}
  allOrders.value = ordersRes.data || []
  buildTrend()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.dashboard-page {
  padding: 20px 0;
  position: relative;
}

.dashboard-page::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 90% 8%, rgba(152, 197, 149, 0.16), transparent 30%),
    radial-gradient(circle at 15% 95%, rgba(251, 180, 114, 0.14), transparent 28%);
}

.stat-card {
  border: 1px solid rgba(196, 219, 199, 0.7);
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(6px);
  border-radius: 16px;
  padding: 14px 14px 12px;
  box-shadow:
    0 10px 24px rgba(24, 86, 45, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.65);
  position: relative;
  overflow: hidden;
}
.shine-layer {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(130deg, rgba(255, 255, 255, 0.42), rgba(255, 255, 255, 0) 44%);
  pointer-events: none;
}
.stat-top { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.stat-label { color: #607566; font-size: 0.88rem; }
.stat-value { color: #204c2c; font-size: 2rem; font-weight: 800; margin-top: 2px; letter-spacing: 0.2px; }
.stat-icon {
  min-width: 36px;
  height: 24px;
  border-radius: 999px;
  padding: 0 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 700;
  background: rgba(76, 175, 80, 0.14);
  color: #2f6f3c;
}
.stat-art {
  position: absolute;
  right: 10px;
  bottom: 8px;
  font-size: 2.7rem;
  opacity: 0.78;
  filter: saturate(1.08);
}
.stat-card.tone-money { border-color: #f7d9c6; background: linear-gradient(180deg, #fffdf8, #fff); }
.stat-card.tone-money .stat-icon { background: rgba(251, 140, 0, 0.18); color: #b45309; }
.stat-card.tone-carbon { border-color: #d6ead8; background: linear-gradient(180deg, #f8fdf8, #fff); }
.stat-card.tone-carbon .stat-icon { background: rgba(67, 160, 71, 0.16); color: #2e7d32; }
.stat-card.tone-food { border-color: #d6e4f6; background: linear-gradient(180deg, #f9fbff, #fff); }
.stat-card.tone-food .stat-icon { background: rgba(66, 133, 244, 0.16); color: #1d4ed8; }
.stat-card.tone-order { border-color: #e3d9f3; background: linear-gradient(180deg, #fcf9ff, #fff); }
.stat-card.tone-order .stat-icon { background: rgba(124, 58, 237, 0.16); color: #6d28d9; }
.panel-card {
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(208, 226, 211, 0.8);
  border-radius: 16px;
  padding: 14px 16px;
  box-shadow:
    0 10px 22px rgba(20, 84, 43, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.65);
}
.panel-title { color: #2d6238; font-weight: 700; margin-bottom: 8px; }
.ratio-value { font-size: 1.7rem; font-weight: 700; color: #d84315; }
.ratio-progress { height: 10px; border-radius: 999px; }
.ratio-progress-bar {
  background: linear-gradient(90deg, #ffb36b, #ff8a4c 58%, #f06642);
  box-shadow: 0 0 10px rgba(240, 102, 66, 0.35);
}
.ok-dot { color: #2e7d32; font-weight: 600; }
.ratio-ring-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 4px;
}

.ratio-ring {
  --p: 0;
  width: 118px;
  height: 118px;
  border-radius: 50%;
  background:
    radial-gradient(farthest-side, #fff 77%, transparent 78%),
    conic-gradient(#f28d2a calc(var(--p) * 1%), #dfe7dd 0);
  display: grid;
  place-items: center;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.08);
}

.ratio-ring span {
  font-size: 1.9rem;
  font-weight: 800;
  color: #0e4f2f;
}

.wave-chart-wrap {
  border: 1px solid rgba(167, 198, 173, 0.5);
  border-radius: 14px;
  padding: 8px 10px 10px;
  background: linear-gradient(180deg, rgba(255,255,255,0.56), rgba(241, 249, 241, 0.4));
}

.wave-chart {
  width: 100%;
  height: 180px;
}

.wave-axis {
  margin-top: -2px;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  color: #557464;
  font-size: 0.78rem;
  text-align: center;
}
</style>
