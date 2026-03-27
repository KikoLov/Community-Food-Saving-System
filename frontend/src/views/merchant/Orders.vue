<template>
  <div class="orders-page">
    <h4 class="mb-4"><i class="fas fa-list me-2"></i>订单管理</h4>

    <div class="orders-toolbar mb-3">
      <div class="toolbar-left">
        <div class="search-box">
          <i class="fas fa-search"></i>
          <input
            v-model.trim="keyword"
            type="text"
            class="search-input"
            placeholder="搜索订单号/商品名/核销码"
          >
        </div>
      </div>
      <div class="toolbar-right">
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

    <ul class="nav nav-tabs order-tabs mb-3">
      <li class="nav-item">
        <a class="nav-link" :class="{ active: activeTab === 'all' }" href="#" @click.prevent="activeTab = 'all'">
          <i class="fas fa-list me-1"></i>全部
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link" :class="{ active: activeTab === '0' }" href="#" @click.prevent="activeTab = '0'">
          <i class="fas fa-clock me-1"></i>待核销
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link" :class="{ active: activeTab === '1' }" href="#" @click.prevent="activeTab = '1'">
          <i class="fas fa-check me-1"></i>已核销
        </a>
      </li>
    </ul>

    <div class="card">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover mb-0">
            <thead class="table-light">
              <tr>
                <th>订单信息</th>
                <th>买家</th>
                <th>金额</th>
                <th>核销码</th>
                <th>状态</th>
                <th>下单时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in pagedOrders" :key="row.orderId">
                <td>
                  <p class="mb-1">订单号: {{ row.orderNo }}</p>
                  <small class="text-muted">{{ row.productName }} x {{ row.quantity }}</small>
                </td>
                <td>{{ row.userName }}</td>
                <td><span class="fw-bold text-danger">¥{{ row.totalAmount }}</span></td>
                <td><span class="badge bg-warning text-dark">{{ row.verifyCode }}</span></td>
                <td>
                  <span :class="['badge', getStatusClass(row.orderStatus)]">
                    {{ getStatusText(row.orderStatus) }}
                  </span>
                </td>
                <td><small>{{ formatDateTime(row.createTime) }}</small></td>
                <td>
                  <button class="btn btn-outline-primary btn-sm" @click="openDetail(row)">查看详情</button>
                </td>
              </tr>
              <tr v-if="filteredOrders.length === 0">
                <td colspan="7" class="text-center py-4">
                  <i class="fas fa-receipt fa-3x text-muted mb-3 d-block"></i>
                  <p class="text-muted">暂无订单</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div v-if="filteredOrders.length > 0" class="pager-wrap p-3 border-top">
        <div class="pager-info">
          共 {{ filteredOrders.length }} 条，当前第 {{ currentPage }} / {{ totalPages }} 页
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

    <div v-if="detailOrder" class="detail-mask" @click.self="detailOrder = null">
      <div class="detail-card">
        <div class="detail-head">
          <h5 class="mb-0">订单详情</h5>
          <div class="detail-actions">
            <button class="btn btn-sm btn-outline-success" @click="copyText(detailOrder.orderNo, '订单号')">复制订单号</button>
            <button class="btn btn-sm btn-outline-warning" @click="copyText(detailOrder.verifyCode, '核销码')">复制核销码</button>
            <button class="btn btn-sm btn-outline-primary" @click="printDetail">打印凭证</button>
            <button class="btn btn-sm btn-light" @click="detailOrder = null">关闭</button>
          </div>
        </div>
        <div class="detail-grid">
          <div><span class="k">订单号</span><span class="v">{{ detailOrder.orderNo }}</span></div>
          <div><span class="k">状态</span><span class="v">{{ getStatusText(detailOrder.orderStatus) }}</span></div>
          <div><span class="k">买家</span><span class="v">{{ detailOrder.userName || '-' }}</span></div>
          <div><span class="k">商品</span><span class="v">{{ detailOrder.productName }} x{{ detailOrder.quantity }}</span></div>
          <div><span class="k">金额</span><span class="v text-danger">¥{{ detailOrder.totalAmount }}</span></div>
          <div><span class="k">核销码</span><span class="v">{{ detailOrder.verifyCode }}</span></div>
          <div><span class="k">下单时间</span><span class="v">{{ formatDateTime(detailOrder.createTime) }}</span></div>
        </div>
        <div class="thumb-wrap">
          <img :src="resolveProductImageSrc(detailOrder, { size: 220 })" class="thumb" alt="商品图" @error="handleImgError">
        </div>
        <div class="timeline-wrap">
          <div class="timeline-title">状态时间线</div>
          <div class="timeline">
            <div v-for="(step, idx) in detailTimeline" :key="idx" class="timeline-item">
              <span :class="['dot', { active: step.active }]"></span>
              <span :class="['line', { active: step.active && idx < detailTimeline.length - 1 }]"></span>
              <span :class="['text', { active: step.active }]">{{ step.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { getMerchantOrders } from '@/api/merchant'
import { Message } from '@/utils/message'
import { resolveProductImageSrc, buildNameBasedProductImage } from '@/utils/productImage'
import { normalizeProductRecord } from '@/utils/demoTextNormalizer'

const orders = ref([])
const activeTab = ref('all')
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const detailOrder = ref(null)

onMounted(async () => {
  try {
    const res = await getMerchantOrders()
    orders.value = (res.data || []).map(normalizeProductRecord)
  } catch (error) {
    console.error(error)
  }
})

const filteredOrders = computed(() => {
  let data = orders.value
  if (activeTab.value !== 'all') {
    data = data.filter(o => o.orderStatus === parseInt(activeTab.value))
  }

  if (!keyword.value) return data
  const q = keyword.value.toLowerCase()
  return data.filter(o =>
    String(o.orderNo || '').toLowerCase().includes(q) ||
    String(o.productName || '').toLowerCase().includes(q) ||
    String(o.verifyCode || '').toLowerCase().includes(q)
  )
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredOrders.value.length / pageSize.value)))

const pagedOrders = computed(() => {
  const safePage = Math.min(currentPage.value, totalPages.value)
  const start = (safePage - 1) * pageSize.value
  return filteredOrders.value.slice(start, start + pageSize.value)
})

watch([activeTab, keyword, pageSize], () => {
  currentPage.value = 1
})

watch(totalPages, (val) => {
  if (currentPage.value > val) currentPage.value = val
})

const getStatusClass = (status) => {
  const classes = { 0: 'bg-warning text-dark', 1: 'bg-success', 2: 'bg-secondary', 3: 'bg-danger' }
  return classes[status] || 'bg-secondary'
}

const getStatusText = (status) => {
  const texts = { 0: '待核销', 1: '已核销', 2: '已取消', 3: '已过期' }
  return texts[status] || '未知'
}

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleString('zh-CN')
}

const openDetail = (row) => {
  detailOrder.value = row
}

const copyText = async (text, label) => {
  if (!text) return
  try {
    await navigator.clipboard.writeText(String(text))
    Message.success(`${label}已复制`)
  } catch (e) {
    Message.warning('复制失败，请手动复制')
  }
}

const printDetail = () => {
  if (!detailOrder.value) return
  const o = detailOrder.value
  const html = `
    <html><head><title>订单凭证</title>
    <style>body{font-family:Arial;padding:20px;color:#222}h2{margin:0 0 12px}p{margin:6px 0}</style>
    </head><body>
      <h2>订单凭证</h2>
      <p>订单号：${o.orderNo || ''}</p>
      <p>状态：${getStatusText(o.orderStatus)}</p>
      <p>买家：${o.userName || '-'}</p>
      <p>商品：${o.productName || ''} x${o.quantity || 0}</p>
      <p>金额：¥${o.totalAmount || 0}</p>
      <p>核销码：${o.verifyCode || ''}</p>
      <p>下单时间：${formatDateTime(o.createTime)}</p>
    </body></html>`
  const w = window.open('', '_blank')
  if (!w) return
  w.document.write(html)
  w.document.close()
  w.focus()
  w.print()
}

const handleImgError = (e) => {
  e.target.src = buildNameBasedProductImage({}, 220)
}

const getTimelineSteps = (order) => {
  const status = Number(order?.orderStatus ?? 0)
  return [
    { label: `下单 ${formatDateTime(order?.createTime) || ''}`.trim(), active: true },
    { label: '待核销', active: status >= 0 },
    { label: '已核销', active: status === 1 },
    { label: '已取消', active: status === 2 },
    { label: '已过期', active: status === 3 }
  ]
}

const detailTimeline = computed(() => {
  return detailOrder.value ? getTimelineSteps(detailOrder.value) : []
})

const exportCsv = () => {
  if (!filteredOrders.value.length) return
  const header = ['订单号', '商品信息', '买家', '金额', '核销码', '状态', '下单时间']
  const rows = filteredOrders.value.map(row => [
    row.orderNo,
    `${row.productName} x${row.quantity}`,
    row.userName,
    row.totalAmount,
    row.verifyCode,
    getStatusText(row.orderStatus),
    formatDateTime(row.createTime)
  ])
  const content = [header, ...rows]
    .map(r => r.map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')

  const blob = new Blob(['\uFEFF' + content], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `merchant-orders-${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.orders-page {
  padding: 20px 0;
}

.orders-toolbar {
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

.page-size-select {
  border: 1px solid #d7ead9;
  background: #f7fcf8;
  color: #2e7d32;
  border-radius: 999px;
  padding: 0.35rem 0.8rem;
}

.order-tabs {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  border-bottom: none;
  gap: 10px;
  overflow-x: auto;
  margin: 0;
  padding-left: 0;
  list-style: none;
}

.order-tabs .nav-item {
  float: none;
  flex: 0 0 auto;
  list-style: none;
}

.order-tabs .nav-link {
  display: inline-flex;
  align-items: center;
  border: 1px solid #d7ead9;
  background: #f7fcf8;
  color: #2e7d32;
  border-radius: 999px;
  padding: 0.45rem 1rem;
  font-weight: 600;
  white-space: nowrap;
  text-decoration: none;
  transition: all 0.2s ease;
}

.order-tabs .nav-link:hover {
  border-color: #4caf50;
  background: #edf8ef;
  color: #1b5e20;
  text-decoration: none;
}

.order-tabs .nav-link.active {
  background: linear-gradient(135deg, #66bb6a, #43a047);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 6px 14px rgba(67, 160, 71, 0.24);
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

.detail-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}

.detail-card {
  width: min(760px, 92vw);
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 14px 32px rgba(0, 0, 0, 0.18);
  padding: 16px;
}

.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 14px;
}

.detail-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 12px;
}

.detail-grid .k {
  display: inline-block;
  min-width: 64px;
  color: #6b7280;
}

.detail-grid .v {
  color: #1f2937;
}

.thumb-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.thumb {
  width: 220px;
  height: 120px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
}

.timeline-wrap {
  margin-top: 12px;
  border-top: 1px dashed #e5e7eb;
  padding-top: 10px;
}

.timeline-title {
  color: #4b5563;
  font-weight: 600;
  margin-bottom: 8px;
}

.timeline {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.timeline-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.timeline-item .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #cfd8dc;
}

.timeline-item .dot.active {
  background: #43a047;
}

.timeline-item .line {
  width: 16px;
  height: 2px;
  background: #d1d5db;
}

.timeline-item .line.active {
  background: #66bb6a;
}

.timeline-item .text {
  color: #6b7280;
  font-size: 0.88rem;
}

.timeline-item .text.active {
  color: #1f2937;
  font-weight: 600;
}
</style>
