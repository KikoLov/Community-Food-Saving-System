<template>
  <div class="carbon-page">
    <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-4">
      <h4 class="mb-0"><i class="fas fa-leaf me-2"></i>低碳中心</h4>
      <div class="d-flex flex-wrap gap-2">
        <router-link to="/consumer/share-poster" class="btn btn-sm btn-success">
          <i class="fas fa-image me-1"></i>生成成就海报
        </router-link>
        <router-link to="/consumer/gamification" class="btn btn-sm btn-outline-success">
          <i class="fas fa-store me-1"></i>碳积分商城（兑换树 / 徽章 / 券）
        </router-link>
      </div>
    </div>

    <div class="row g-4 mb-4">
      <div class="col-md-4">
        <div class="card stat-card">
          <div class="card-body d-flex align-items-center">
            <div class="stat-icon stat-icon--points me-4" aria-hidden="true">
              <svg class="stat-illustration" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
                <defs>
                  <linearGradient id="statPtsCoin" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#fff8e1" />
                    <stop offset="55%" stop-color="#ffca28" />
                    <stop offset="100%" stop-color="#f9a825" />
                  </linearGradient>
                  <linearGradient id="statPtsShine" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="#ffffff" stop-opacity="0.55" />
                    <stop offset="45%" stop-color="#ffffff" stop-opacity="0" />
                  </linearGradient>
                </defs>
                <ellipse cx="32" cy="52" rx="22" ry="7" fill="#000" opacity="0.07" />
                <ellipse cx="32" cy="40" rx="17" ry="5" fill="#f57f17" opacity="0.88" />
                <ellipse cx="32" cy="34" rx="17" ry="5" fill="#ffb300" />
                <ellipse cx="32" cy="28" rx="17" ry="5" fill="url(#statPtsCoin)" stroke="#e65100" stroke-width="0.8" />
                <circle cx="32" cy="18" r="11" fill="url(#statPtsCoin)" stroke="#e65100" stroke-width="1.1" />
                <ellipse cx="26" cy="14" rx="7" ry="4" fill="url(#statPtsShine)" transform="rotate(-25 26 14)" />
                <path d="M32 11v14M25 18h14" stroke="#bf360c" stroke-width="1.2" stroke-linecap="round" opacity="0.35" />
              </svg>
            </div>
            <div>
              <p class="text-muted mb-1">碳积分 <small class="text-muted">(Carbon Coins)</small></p>
              <h3 class="mb-0">{{ profile?.carbonPoints || 0 }}</h3>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card stat-card">
          <div class="card-body d-flex align-items-center">
            <div class="stat-icon stat-icon--carbon me-4" aria-hidden="true">
              <svg class="stat-illustration" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
                <defs>
                  <linearGradient id="statLeafBody" x1="0%" y1="100%" x2="100%" y2="0%">
                    <stop offset="0%" stop-color="#1b5e20" />
                    <stop offset="50%" stop-color="#388e3c" />
                    <stop offset="100%" stop-color="#81c784" />
                  </linearGradient>
                  <radialGradient id="statLeafGlow" cx="35%" cy="30%" r="55%">
                    <stop offset="0%" stop-color="#c8e6c9" stop-opacity="0.9" />
                    <stop offset="100%" stop-color="#81c784" stop-opacity="0" />
                  </radialGradient>
                </defs>
                <circle cx="32" cy="32" r="26" fill="url(#statLeafGlow)" opacity="0.65" />
                <path
                  d="M32 10c14 2 22 14 20 26-2 12-12 20-20 22-8-2-18-10-20-22-2-12 6-24 20-26z"
                  fill="url(#statLeafBody)"
                  stroke="#2e7d32"
                  stroke-width="0.9"
                />
                <path d="M32 16v30" stroke="#1b5e20" stroke-width="1.4" stroke-linecap="round" opacity="0.35" />
                <path d="M32 22c6 4 10 10 10 16M32 26c-5 3-9 8-10 14" stroke="#a5d6a7" stroke-width="1.1" stroke-linecap="round" opacity="0.7" />
                <circle cx="44" cy="18" r="4.2" fill="#e8f5e9" stroke="#43a047" stroke-width="0.85" opacity="0.95" />
                <circle cx="50" cy="26" r="3.2" fill="#c8e6c9" stroke="#66bb6a" stroke-width="0.75" opacity="0.92" />
              </svg>
            </div>
            <div>
              <p class="text-muted mb-1">累计碳减排 (g CO₂)</p>
              <h3 class="mb-0">{{ carbonKgToGramsStr(profile?.totalCarbonSaved) }}</h3>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card stat-card">
          <div class="card-body d-flex align-items-center">
            <div class="stat-icon stat-icon--food me-4" aria-hidden="true">
              <svg class="stat-illustration" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
                <defs>
                  <linearGradient id="statBagGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="#e3f2fd" />
                    <stop offset="100%" stop-color="#64b5f6" />
                  </linearGradient>
                  <linearGradient id="statAppleGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#ffcc80" />
                    <stop offset="100%" stop-color="#ef6c00" />
                  </linearGradient>
                </defs>
                <path
                  d="M18 24h28l-3 32H21L18 24z"
                  fill="url(#statBagGrad)"
                  stroke="#1976d2"
                  stroke-width="1.1"
                  stroke-linejoin="round"
                />
                <path d="M18 24l14-8 14 8" fill="none" stroke="#1565c0" stroke-width="1.2" stroke-linecap="round" />
                <path d="M32 16v-4" stroke="#5d4037" stroke-width="1.6" stroke-linecap="round" />
                <ellipse cx="32" cy="14" rx="5" ry="2.2" fill="#66bb6a" stroke="#2e7d32" stroke-width="0.6" />
                <circle cx="32" cy="38" r="9" fill="url(#statAppleGrad)" stroke="#e65100" stroke-width="0.9" />
                <path d="M32 30c-2 4-2 8 0 12" stroke="#fff3e0" stroke-width="1.2" stroke-linecap="round" opacity="0.55" />
                <path d="M26 44c3 2 7 2 12 0" fill="none" stroke="#bf360c" stroke-width="0.9" stroke-linecap="round" opacity="0.4" />
              </svg>
            </div>
            <div>
              <p class="text-muted mb-1">累计挽救食品(kg)</p>
              <h3 class="mb-0">{{ profile?.totalFoodSaved || 0 }}</h3>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-4 mb-4">
      <div class="col-md-6">
        <div class="card insight-card">
          <div class="card-body">
            <h6 class="mb-2"><i class="fas fa-bolt me-2"></i>行为洞察</h6>
            <p class="mb-1 text-muted">已核销订单（推断）</p>
            <h3 class="mb-2">{{ verifiedCount }}</h3>
            <p class="mb-1 text-muted">单次平均减碳</p>
            <h5 class="mb-0 text-success">{{ carbonKgToGramsStr(avgCarbonPerActionKg) }} g</h5>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card insight-card">
          <div class="card-body">
            <h6 class="mb-2"><i class="fas fa-chart-area me-2"></i>近 7 天积分趋势</h6>
            <div v-if="trendMax > 0" class="wave-chart-wrap">
              <svg class="wave-chart" viewBox="0 0 760 220" preserveAspectRatio="none">
                <defs>
                  <linearGradient id="cTrendFill" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#73d47f" stop-opacity="0.92" />
                    <stop offset="100%" stop-color="#73d47f" stop-opacity="0.12" />
                  </linearGradient>
                </defs>
                <path :d="trendAreaPath" fill="url(#cTrendFill)"></path>
                <path :d="trendLinePath" stroke="#2d8f40" stroke-width="3.2" fill="none" stroke-linecap="round"></path>
              </svg>
              <div class="wave-axis">
                <span v-for="x in trend7d" :key="x.date">{{ shortDate(x.date) }}</span>
              </div>
            </div>
            <div v-else class="text-muted">暂无趋势数据</div>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h5 class="mb-0"><i class="fas fa-history me-2"></i>积分记录</h5>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover mb-0">
            <thead class="table-light">
              <tr>
                <th>类型</th>
                <th>积分</th>
                <th>碳减排 (g)</th>
                <th>描述</th>
                <th>时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in logs" :key="log.logId">
                <td>
                  <span :class="['badge', log.logType === 1 ? 'bg-success' : 'bg-warning text-dark']">
                    {{ logTypeLabel(log) }}
                  </span>
                </td>
                <td>
                  <span :class="log.logType === 1 ? 'text-success fw-bold' : 'text-danger fw-bold'">
                    {{ log.logType === 1 ? '+' : '-' }}{{ log.carbonPoints }}
                  </span>
                </td>
                <td>{{ carbonKgToGramsStr(log.carbonSaved) }}</td>
                <td>{{ log.description }}</td>
                <td><small>{{ formatDateTime(log.createTime) }}</small></td>
              </tr>
              <tr v-if="logs.length === 0">
                <td colspan="5" class="text-center py-4">
                  <i class="fas fa-history fa-3x text-muted mb-3 d-block"></i>
                  <p class="text-muted">暂无记录</p>
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
import { getCarbonCenter } from '@/api/consumer'

const profile = ref(null)
const logs = ref([])

const verifiedCount = computed(() => logs.value.filter(x => x.logType === 1).length)
/** 接口 carbonSaved 为 kg，单次平均减碳（kg） */
const avgCarbonPerActionKg = computed(() => {
  const c = verifiedCount.value
  if (!c) return 0
  const total = logs.value
    .filter(x => x.logType === 1)
    .reduce((sum, x) => sum + Number(x.carbonSaved || 0), 0)
  return total / c
})

/** 与成就海报一致：kg CO₂ → g 展示 */
function carbonKgToGramsStr(kg) {
  const raw = (Number(kg) || 0) * 1000
  if (!(raw > 0)) return '0'
  const g = Number(raw.toPrecision(10))
  if (g >= 100) return Math.round(g).toLocaleString('zh-CN')
  if (g >= 1) return (Math.round(g * 10) / 10).toLocaleString('zh-CN')
  return (Math.round(g * 1000) / 1000).toLocaleString('zh-CN')
}

const trend7d = computed(() => {
  const result = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    d.setHours(0, 0, 0, 0)
    const start = d.getTime()
    const end = start + 24 * 60 * 60 * 1000
    const points = logs.value
      .filter(x => {
        const t = new Date(x.createTime).getTime()
        return t >= start && t < end
      })
      .reduce((s, x) => s + Number(x.carbonPoints || 0), 0)
    result.push({ date: new Date(start).toISOString().slice(0, 10), points })
  }
  return result
})

const trendMax = computed(() => trend7d.value.reduce((m, x) => Math.max(m, x.points), 0))

const trendPoints = computed(() => {
  if (!trend7d.value.length || !trendMax.value) return []
  const width = 760
  const height = 190
  const baseY = 182
  const step = width / Math.max(1, (trend7d.value.length - 1))
  return trend7d.value.map((p, idx) => ({
    x: idx * step,
    y: baseY - (p.points / trendMax.value) * height
  }))
})

const trendLinePath = computed(() => {
  const pts = trendPoints.value
  if (!pts.length) return ''
  let d = `M ${pts[0].x} ${pts[0].y}`
  for (let i = 1; i < pts.length; i++) {
    const prev = pts[i - 1]
    const curr = pts[i]
    const cx = (prev.x + curr.x) / 2
    d += ` Q ${cx} ${prev.y} ${curr.x} ${curr.y}`
  }
  return d
})
const trendAreaPath = computed(() => (trendLinePath.value ? `${trendLinePath.value} L 760 182 L 0 182 Z` : ''))

onMounted(async () => {
  try {
    const res = await getCarbonCenter()
    profile.value = res.data.profile
    logs.value = res.data.logs || []
  } catch (error) {
    console.error(error)
  }
})

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN')
}

const logTypeLabel = (log) => {
  if (log.logType === 1) return '获得'
  if (log.description && String(log.description).includes('兑换')) return '兑换'
  return '扣减'
}

const shortDate = (dateStr) => {
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()}`
}
</script>

<style scoped>
.carbon-page {
  padding: 20px 0;
  position: relative;
}

.carbon-page h4 {
  font-size: 1.65rem;
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
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.65);
  background: linear-gradient(145deg, #f6fcf7, #e8f3ea);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.75);
}

.stat-icon .stat-illustration {
  width: 46px;
  height: 46px;
  display: block;
}

/* 碳积分：暖色底 + 金币叠放 */
.stat-icon--points {
  background:
    radial-gradient(circle at 28% 20%, rgba(255, 213, 79, 0.45) 0%, transparent 42%),
    radial-gradient(circle at 78% 78%, rgba(255, 183, 77, 0.22) 0%, transparent 45%),
    linear-gradient(155deg, #fffdf7, #ffe8b0);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 3px 10px rgba(245, 127, 23, 0.14);
}

/* 累计碳减排：绿色底 + 叶片意象 */
.stat-icon--carbon {
  background:
    radial-gradient(circle at 72% 22%, rgba(165, 214, 167, 0.5) 0%, transparent 40%),
    radial-gradient(circle at 22% 70%, rgba(102, 187, 106, 0.2) 0%, transparent 48%),
    linear-gradient(155deg, #f4fbf5, #dcedc8);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.85),
    0 3px 10px rgba(46, 125, 50, 0.12);
}

/* 累计挽救食品：冷色底 + 购物袋与果实 */
.stat-icon--food {
  background:
    radial-gradient(circle at 50% 28%, rgba(100, 181, 246, 0.35) 0%, transparent 46%),
    radial-gradient(circle at 20% 82%, rgba(129, 212, 250, 0.2) 0%, transparent 42%),
    linear-gradient(155deg, #f8fcff, #e3f2fd);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 3px 10px rgba(25, 118, 210, 0.11);
}

.card-header {
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.insight-card {
  border: 1px solid rgba(205, 227, 210, 0.72);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 8px 20px rgba(20, 84, 43, 0.06);
}

.wave-chart-wrap {
  border: 1px solid rgba(167, 198, 173, 0.5);
  border-radius: 12px;
  padding: 8px 10px 10px;
  background: linear-gradient(180deg, rgba(255,255,255,0.56), rgba(241, 249, 241, 0.4));
}

.wave-chart {
  width: 100%;
  height: 120px;
}

.wave-axis {
  margin-top: -2px;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  color: #557464;
  font-size: 0.74rem;
  text-align: center;
}
</style>
