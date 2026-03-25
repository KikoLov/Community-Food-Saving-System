<template>
  <div>
    <div class="logs-toolbar mb-3">
      <div class="toolbar-left">
        <div class="search-box">
          <i class="fas fa-search"></i>
          <input
            v-model.trim="keyword"
            type="text"
            class="search-input"
            placeholder="搜索操作者/目标名称/详情"
          >
        </div>
      </div>
      <div class="toolbar-right">
        <select v-model="actionType" class="action-select">
          <option value="all">全部操作</option>
          <option value="ADD_PRODUCT">新增商品</option>
          <option value="DELETE_PRODUCT">删除商品</option>
          <option value="VERIFY_ORDER">核销订单</option>
          <option value="BATCH_UPDATE_PRODUCT_STATUS">批量上/下架</option>
          <option value="BATCH_DELETE_PRODUCT">批量删除商品</option>
          <option value="AUDIT_MERCHANT">审核商户</option>
          <option value="ADD_COMMUNITY">新增社区</option>
          <option value="UPDATE_COMMUNITY">更新社区</option>
          <option value="DELETE_COMMUNITY">删除社区</option>
          <option value="ADD_CATEGORY">新增分类</option>
          <option value="UPDATE_CATEGORY">更新分类</option>
          <option value="DELETE_CATEGORY">删除分类</option>
        </select>
        <select v-model="timeRange" class="action-select">
          <option value="all">全部时间</option>
          <option value="today">今天</option>
          <option value="7d">近 7 天</option>
          <option value="30d">近 30 天</option>
        </select>
        <select v-model.number="pageSize" class="page-size-select">
          <option :value="10">每页 10 条</option>
          <option :value="20">每页 20 条</option>
          <option :value="50">每页 50 条</option>
        </select>
        <button class="btn btn-sm btn-outline-success" @click="exportCsv">
          <i class="fas fa-file-csv me-1"></i>导出当前结果
        </button>
      </div>
    </div>

    <div class="stats-grid mb-3">
      <div class="stat-card">
        <div class="stat-label">筛选后日志数</div>
        <div class="stat-value">{{ filteredLogs.length }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">新增商品</div>
        <div class="stat-value">{{ actionCount('ADD_PRODUCT') }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">删除商品</div>
        <div class="stat-value">{{ actionCount('DELETE_PRODUCT') + actionCount('BATCH_DELETE_PRODUCT') }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">订单核销</div>
        <div class="stat-value">{{ actionCount('VERIFY_ORDER') }}</div>
      </div>
    </div>

    <div class="card mb-4">
      <div class="card-header">
        <i class="fas fa-history me-2"></i>操作审计日志
      </div>
      <div class="card-body">
        <div class="table-responsive">
          <table class="table table-bordered table-hover">
            <thead>
              <tr>
                <th>时间</th>
                <th>操作者</th>
                <th>角色</th>
                <th>操作</th>
                <th>目标</th>
                <th>详情</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in pagedLogs" :key="row.id">
                <td><small class="text-muted">{{ formatDateTime(row.createTime) }}</small></td>
                <td>
                  <div class="fw-semibold">{{ row.actorUsername }}</div>
                  <small class="text-muted">ID: {{ row.actorUserId }}</small>
                </td>
                <td>
                  <span class="badge bg-light text-dark">{{ row.actorRole }}</span>
                </td>
                <td>
                  <span class="badge action-badge">{{ actionTypeText(row.actionType) }}</span>
                </td>
                <td>
                  <div class="fw-semibold">{{ row.targetName || '-' }}</div>
                  <small class="text-muted">{{ row.targetType }} #{{ row.targetId ?? '-' }}</small>
                </td>
                <td>{{ row.detail || '-' }}</td>
              </tr>
              <tr v-if="filteredLogs.length === 0">
                <td colspan="6" class="text-center py-4 text-muted">
                  <i class="fas fa-inbox fa-2x d-block mb-2"></i>
                  暂无审计日志
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="filteredLogs.length > 0" class="pager-wrap mt-3">
          <div class="pager-info">
            共 {{ filteredLogs.length }} 条，当前第 {{ currentPage }} / {{ totalPages }} 页
          </div>
          <div class="pager-actions">
            <button class="btn btn-sm btn-light" :disabled="currentPage === 1" @click="currentPage--">
              上一页
            </button>
            <button class="btn btn-sm btn-light" :disabled="currentPage >= totalPages" @click="currentPage++">
              下一页
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { getOperationLogs } from '@/api/admin'

const logs = ref([])
const keyword = ref('')
const actionType = ref('all')
const timeRange = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)

onMounted(async () => {
  const res = await getOperationLogs()
  logs.value = res.data || []
})

const filteredLogs = computed(() => {
  let data = logs.value
  if (actionType.value !== 'all') {
    data = data.filter(x => x.actionType === actionType.value)
  }
  if (timeRange.value !== 'all') {
    const now = Date.now()
    const days = timeRange.value === 'today' ? 1 : (timeRange.value === '7d' ? 7 : 30)
    const start = new Date()
    start.setHours(0, 0, 0, 0)
    const windowMs = days === 1 ? (now - start.getTime()) : days * 24 * 60 * 60 * 1000
    data = data.filter(x => {
      const t = x.createTime ? new Date(x.createTime).getTime() : 0
      return t && now - t <= windowMs
    })
  }
  if (!keyword.value) {
    return data
  }
  const q = keyword.value.toLowerCase()
  return data.filter(x =>
    String(x.actorUsername || '').toLowerCase().includes(q) ||
    String(x.targetName || '').toLowerCase().includes(q) ||
    String(x.detail || '').toLowerCase().includes(q)
  )
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredLogs.value.length / pageSize.value)))
const pagedLogs = computed(() => {
  const safePage = Math.min(currentPage.value, totalPages.value)
  const start = (safePage - 1) * pageSize.value
  return filteredLogs.value.slice(start, start + pageSize.value)
})

watch([keyword, actionType, timeRange, pageSize], () => {
  currentPage.value = 1
})

watch(totalPages, (val) => {
  if (currentPage.value > val) currentPage.value = val
})

const actionTypeText = (type) => {
  const map = {
    ADD_PRODUCT: '新增商品',
    DELETE_PRODUCT: '删除商品',
    VERIFY_ORDER: '核销订单',
    BATCH_UPDATE_PRODUCT_STATUS: '批量上/下架',
    BATCH_DELETE_PRODUCT: '批量删除商品',
    AUDIT_MERCHANT: '审核商户',
    ADD_COMMUNITY: '新增社区',
    UPDATE_COMMUNITY: '更新社区',
    DELETE_COMMUNITY: '删除社区',
    ADD_CATEGORY: '新增分类',
    UPDATE_CATEGORY: '更新分类',
    DELETE_CATEGORY: '删除分类'
  }
  return map[type] || type
}

const actionCount = (type) => {
  return filteredLogs.value.filter(x => x.actionType === type).length
}

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleString('zh-CN')
}

const exportCsv = () => {
  if (!filteredLogs.value.length) return
  const header = ['时间', '操作者ID', '操作者', '角色', '操作', '目标类型', '目标ID', '目标名称', '详情']
  const rows = filteredLogs.value.map(x => [
    formatDateTime(x.createTime),
    x.actorUserId,
    x.actorUsername,
    x.actorRole,
    actionTypeText(x.actionType),
    x.targetType,
    x.targetId,
    x.targetName,
    x.detail
  ])
  const content = [header, ...rows]
    .map(r => r.map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob(['\uFEFF' + content], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `operation-logs-${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.logs-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f7fcf8;
  border: 1px solid #d7ead9;
  border-radius: 999px;
  padding: 0 12px;
  min-width: 280px;
}

.search-box i {
  color: #66a86d;
  font-size: 0.9rem;
}

.search-input {
  border: none;
  background: transparent;
  outline: none;
  padding: 7px 8px;
  width: 100%;
  color: #2d4730;
}

.action-select,
.page-size-select {
  border: 1px solid #d7ead9;
  background: #f7fcf8;
  color: #2e7d32;
  border-radius: 999px;
  padding: 0.35rem 0.8rem;
}

.action-badge {
  background: linear-gradient(135deg, #66bb6a, #43a047);
  color: #fff;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 10px;
}

.stat-card {
  border: 1px solid #d7ead9;
  border-radius: 12px;
  background: #f7fcf8;
  padding: 10px 12px;
}

.stat-label {
  color: #5a7560;
  font-size: 0.84rem;
}

.stat-value {
  color: #1f5f2e;
  font-size: 1.35rem;
  font-weight: 700;
  line-height: 1.1;
  margin-top: 3px;
}

.pager-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.pager-info {
  color: #4a6b4c;
  font-size: 0.92rem;
}

.pager-actions {
  display: flex;
  gap: 8px;
}
</style>
