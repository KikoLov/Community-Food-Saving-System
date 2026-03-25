<template>
  <div class="carbon-page">
    <h4 class="mb-4"><i class="fas fa-leaf me-2"></i>低碳中心</h4>

    <div class="row g-4 mb-4">
      <div class="col-md-4">
        <div class="card stat-card">
          <div class="card-body d-flex align-items-center">
            <div class="stat-icon me-4">
              <i class="fas fa-coins fa-2x text-warning"></i>
            </div>
            <div>
              <p class="text-muted mb-1">低碳积分</p>
              <h3 class="mb-0">{{ profile?.carbonPoints || 0 }}</h3>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card stat-card">
          <div class="card-body d-flex align-items-center">
            <div class="stat-icon me-4">
              <i class="fas fa-chart-line fa-2x text-success"></i>
            </div>
            <div>
              <p class="text-muted mb-1">累计碳减排(kg)</p>
              <h3 class="mb-0">{{ profile?.totalCarbonSaved || 0 }}</h3>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card stat-card">
          <div class="card-body d-flex align-items-center">
            <div class="stat-icon me-4">
              <i class="fas fa-box fa-2x text-primary"></i>
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
            <h5 class="mb-0 text-success">{{ avgCarbonPerAction }} kg</h5>
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
                <th>碳减排(kg)</th>
                <th>描述</th>
                <th>时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in logs" :key="log.logId">
                <td>
                  <span :class="['badge', log.logType === 1 ? 'bg-success' : 'bg-secondary']">
                    {{ log.logType === 1 ? '获得' : '扣减' }}
                  </span>
                </td>
                <td>
                  <span :class="log.logType === 1 ? 'text-success fw-bold' : 'text-danger fw-bold'">
                    {{ log.logType === 1 ? '+' : '-' }}{{ log.carbonPoints }}
                  </span>
                </td>
                <td>{{ log.carbonSaved }}</td>
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
const avgCarbonPerAction = computed(() => {
  const c = verifiedCount.value
  if (!c) return '0.0000'
  const total = logs.value
    .filter(x => x.logType === 1)
    .reduce((sum, x) => sum + Number(x.carbonSaved || 0), 0)
  return (total / c).toFixed(4)
})

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
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(145deg, #f6fcf7, #e8f3ea);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
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
