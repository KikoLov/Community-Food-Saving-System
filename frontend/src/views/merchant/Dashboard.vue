<template>
  <div class="dashboard-page">
    <h4 class="mb-4"><i class="fas fa-chart-line me-2"></i>控制台</h4>

    <div v-if="warningProducts.length > 0" class="alert alert-warning mb-4">
      <i class="fas fa-exclamation-triangle me-2"></i>
      <strong>库存预警提醒</strong>：以下商品即将过期，请及时处理
    </div>

    <div class="row g-4 mb-4">
      <div class="col-md-3">
        <div class="card stat-card">
          <div class="card-body d-flex align-items-center">
            <div class="stat-icon me-4">
              <i class="fas fa-box fa-2x text-primary"></i>
            </div>
            <div>
              <p class="text-muted mb-1">商品总数</p>
              <h3 class="mb-0">{{ stats.productCount || 0 }}</h3>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card stat-card">
          <div class="card-body d-flex align-items-center">
            <div class="stat-icon me-4">
              <i class="fas fa-list fa-2x text-warning"></i>
            </div>
            <div>
              <p class="text-muted mb-1">待核销订单</p>
              <h3 class="mb-0">{{ stats.pendingOrderCount || 0 }}</h3>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card stat-card">
          <div class="card-body d-flex align-items-center">
            <div class="stat-icon me-4">
              <i class="fas fa-calendar-day fa-2x text-info"></i>
            </div>
            <div>
              <p class="text-muted mb-1">今日订单</p>
              <h3 class="mb-0">{{ stats.todayOrderCount || 0 }}</h3>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card stat-card">
          <div class="card-body d-flex align-items-center">
            <div class="stat-icon me-4">
              <i class="fas fa-leaf fa-2x text-success"></i>
            </div>
            <div>
              <p class="text-muted mb-1">累计碳减排(kg)</p>
              <h3 class="mb-0">{{ stats.totalCarbonSaved || 0 }}</h3>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-4 mb-4">
      <div class="col-md-6">
        <div class="card stat-card h-100">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <p class="text-muted mb-0">核销率</p>
              <span class="badge bg-success-subtle text-success">近全部订单</span>
            </div>
            <h3 class="mb-3">{{ stats.verifyRate || 0 }}%</h3>
            <div class="progress verify-progress">
              <div class="progress-bar bg-success" role="progressbar" :style="{ width: `${stats.verifyRate || 0}%` }"></div>
            </div>
            <small class="text-muted mt-2 d-block">
              已核销 {{ stats.verifiedOrderCount || 0 }} 单 / 待核销 {{ stats.pendingOrderCount || 0 }} 单
            </small>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card stat-card h-100">
          <div class="card-body">
            <p class="text-muted mb-1">累计成交额</p>
            <h3 class="mb-0 text-danger">¥{{ stats.totalAmount || 0 }}</h3>
          </div>
        </div>
      </div>
    </div>

    <div class="card mb-4">
      <div class="card-header bg-success bg-opacity-10 d-flex justify-content-between align-items-center">
        <h5 class="mb-0"><i class="fas fa-chart-area text-success me-2"></i>近 7 天订单趋势</h5>
        <small class="text-muted">订单数 / 核销数</small>
      </div>
      <div class="card-body">
        <div class="wave-chart-wrap" v-if="trendMax > 0">
          <svg class="wave-chart" viewBox="0 0 760 220" preserveAspectRatio="none">
            <defs>
              <linearGradient id="mOrderFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#73d47f" stop-opacity="0.95" />
                <stop offset="100%" stop-color="#73d47f" stop-opacity="0.12" />
              </linearGradient>
              <linearGradient id="mVerifyFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#ffb75f" stop-opacity="0.95" />
                <stop offset="100%" stop-color="#ffb75f" stop-opacity="0.12" />
              </linearGradient>
            </defs>
            <path :d="orderAreaPath" fill="url(#mOrderFill)"></path>
            <path :d="verifyAreaPath" fill="url(#mVerifyFill)"></path>
            <path :d="orderLinePath" stroke="#2d8f40" stroke-width="3.2" fill="none" stroke-linecap="round"></path>
            <path :d="verifyLinePath" stroke="#f28e2d" stroke-width="3.2" fill="none" stroke-linecap="round"></path>
          </svg>
          <div class="wave-axis">
            <span v-for="point in stats.orderTrend7d || []" :key="point.date">{{ shortDate(point.date) }}</span>
          </div>
        </div>
        <div v-else class="text-muted">暂无趋势数据</div>
      </div>
    </div>

    <div v-if="warningProducts.length > 0" class="card">
      <div class="card-header bg-warning bg-opacity-10">
        <h5 class="mb-0"><i class="fas fa-exclamation-circle text-warning me-2"></i>即将过期商品</h5>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover mb-0">
            <thead class="table-light">
              <tr>
                <th>商品名称</th>
                <th>库存</th>
                <th>过期时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in warningProducts" :key="product.productId">
                <td>{{ product.productName }}</td>
                <td>{{ product.stock }}</td>
                <td><span class="text-danger">{{ formatDateTime(product.expireDatetime) }}</span></td>
                <td>
                  <button class="btn btn-primary btn-sm" @click="$router.push('/merchant/products')">
                    去处理
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { getWarningProducts, getMerchantStats } from '@/api/merchant'
import { normalizeProductRecord } from '@/utils/demoTextNormalizer'

const warningProducts = ref([])
const stats = ref({})
const trendMax = computed(() => {
  const arr = stats.value.orderTrend7d || []
  const max = arr.reduce((m, x) => Math.max(m, x.orderCount || 0, x.verifiedCount || 0), 0)
  return max || 0
})

const trendPoints = computed(() => {
  const list = stats.value.orderTrend7d || []
  if (!list.length || !trendMax.value) return []
  const width = 760
  const height = 190
  const baseY = 182
  const step = width / Math.max(1, (list.length - 1))
  return list.map((p, idx) => {
    const x = idx * step
    const oy = baseY - ((p.orderCount || 0) / trendMax.value) * height
    const vy = baseY - ((p.verifiedCount || 0) / trendMax.value) * height
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
const orderAreaPath = computed(() => (orderLinePath.value ? `${orderLinePath.value} L 760 182 L 0 182 Z` : ''))
const verifyAreaPath = computed(() => (verifyLinePath.value ? `${verifyLinePath.value} L 760 182 L 0 182 Z` : ''))

onMounted(async () => {
  try {
    const warningRes = await getWarningProducts()
    warningProducts.value = (warningRes.data || []).map(normalizeProductRecord)

    const statsRes = await getMerchantStats()
    stats.value = statsRes.data || {}
  } catch (error) {
    console.error(error)
  }
})

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN')
}

const shortDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()}`
}
</script>

<style scoped>
.dashboard-page {
  padding: 20px 0;
  position: relative;
}

.dashboard-page h4 {
  font-size: 1.7rem;
  font-weight: 800;
  color: #1f5131;
}

.stat-card {
  border: 1px solid rgba(200, 222, 204, 0.76);
  background: rgba(255, 255, 255, 0.74);
  backdrop-filter: blur(8px);
  box-shadow: 0 10px 22px rgba(20, 84, 43, 0.08);
  border-radius: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(145deg, #f6fcf7, #e8f3ea);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.verify-progress {
  height: 10px;
  border-radius: 999px;
  background: #edf5ee;
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

.btn-primary {
  background: linear-gradient(135deg, #4fa25f, #2f7d3f);
  border-color: #2f7d3f;
  border-radius: 12px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.28);
}

.btn-primary:hover {
  background: linear-gradient(135deg, #60b370, #358646);
  border-color: #2f7d3f;
}
</style>
