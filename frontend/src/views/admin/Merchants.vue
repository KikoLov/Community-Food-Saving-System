<template>
  <div>
    <div class="card mb-4">
      <div class="card-header">
        <h2 class="mb-0">🏪 商户管理</h2>
      </div>
      <div class="card-body">
        <div v-if="loading" class="text-center" style="padding: 40px;">
          <div class="spinner"></div>
          <p>加载商户列表...</p>
        </div>
        <div v-else>
          <table class="table" style="width: 100%; border-collapse: collapse;">
            <thead>
              <tr style="background: #f8f9fa; border-bottom: 2px solid #dee2e6;">
                <th style="padding: 12px; text-align: left;">商户名称</th>
                <th style="padding: 12px; text-align: left;">联系电话</th>
                <th style="padding: 12px; text-align: left;">地址</th>
                <th style="padding: 12px; text-align: left;">资质状态</th>
                <th style="padding: 12px; text-align: left;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="merchant in merchants" :key="merchant.merchantId" style="border-bottom: 1px solid #dee2e6;">
                <td style="padding: 12px;">{{ merchant.merchantName }}</td>
                <td style="padding: 12px;">{{ merchant.contactPhone || '-' }}</td>
                <td style="padding: 12px;">{{ merchant.address || '-' }}</td>
                <td style="padding: 12px;">
                  <span class="badge" :class="getStatusClass(merchant.licenseStatus)">
                    {{ getStatusText(merchant.licenseStatus) }}
                  </span>
                </td>
                <td style="padding: 12px;">
                  <button type="button" class="btn btn-outline-primary btn-sm me-1" @click="openDetail(merchant)">
                    👁 查看
                  </button>
                  <template v-if="merchant.licenseStatus === 0">
                    <button
                      type="button"
                      class="btn btn-success btn-sm me-1"
                      @click="handleAudit(merchant, 1)"
                    >
                      ✓ 通过
                    </button>
                    <button
                      type="button"
                      class="btn btn-danger btn-sm me-1"
                      @click="handleAudit(merchant, 2)"
                    >
                      ✗ 拒绝
                    </button>
                  </template>
                  <button
                    type="button"
                    class="btn btn-outline-danger btn-sm"
                    @click="handleDelete(merchant)"
                  >
                    删除
                  </button>
                </td>
              </tr>
              <tr v-if="merchants.length === 0">
                <td colspan="5" style="padding: 40px; text-align: center;">
                  <div style="color: #6c757d;">
                    <div style="font-size: 3em; margin-bottom: 10px;">🏪</div>
                    <p>暂无商户</p>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-if="detailVisible" class="modal-overlay" @click.self="closeDetail">
      <div class="modal-panel">
        <div class="modal-head">
          <h3 class="mb-0">商户详情</h3>
          <button type="button" class="btn-close" aria-label="关闭" @click="closeDetail">×</button>
        </div>
        <div v-if="detailLoading" class="p-4 text-center text-muted">加载中…</div>
        <div v-else-if="detailStats" class="modal-body">
          <div class="detail-title">{{ detailStats.merchant?.merchantName }}</div>
          <div class="detail-grid">
            <div><span class="k">联系电话</span><span class="v">{{ detailStats.merchant?.contactPhone || '-' }}</span></div>
            <div><span class="k">地址</span><span class="v">{{ detailStats.merchant?.address || '-' }}</span></div>
            <div><span class="k">资质状态</span><span class="v">{{ getStatusText(detailStats.merchant?.licenseStatus) }}</span></div>
            <div><span class="k">在售商品</span><span class="v text-success">{{ detailStats.onSaleProductCount ?? 0 }} 件</span></div>
          </div>
          <div class="stats-title">经营数据</div>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-label">今日销售额</div>
              <div class="stat-value">{{ formatMoney(detailStats.todaySales) }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">今日订单数</div>
              <div class="stat-value">{{ detailStats.todayOrderCount ?? 0 }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">本月销售额</div>
              <div class="stat-value">{{ formatMoney(detailStats.monthSales) }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">本月订单数</div>
              <div class="stat-value">{{ detailStats.monthOrderCount ?? 0 }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">待核销</div>
              <div class="stat-value text-warning">{{ detailStats.pendingVerifyCount ?? 0 }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">累计已核销</div>
              <div class="stat-value text-success">{{ detailStats.totalVerifiedCount ?? 0 }}</div>
            </div>
          </div>
        </div>
        <div class="modal-foot">
          <button type="button" class="btn btn-secondary" @click="closeDetail">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMerchants, auditMerchant, getMerchantStats, deleteMerchant } from '@/api/admin'
import { Message } from '@/utils/message'

const merchants = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailStats = ref(null)

onMounted(() => {
  loadMerchants()
})

const loadMerchants = async () => {
  loading.value = true
  try {
    const res = await getMerchants()
    merchants.value = res.data || []
  } catch (error) {
    Message.error('加载商户列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getStatusClass = (status) => {
  const classes = { 0: 'badge-warning', 1: 'badge-success', 2: 'badge-danger' }
  return classes[status] || 'badge-secondary'
}

const getStatusText = (status) => {
  const texts = { 0: '⏳ 待审核', 1: '✓ 已通过', 2: '✗ 已拒绝' }
  return texts[status] || '未知'
}

const formatMoney = (v) => {
  const n = Number(v)
  if (Number.isNaN(n)) return '¥0.00'
  return `¥${n.toFixed(2)}`
}

const openDetail = async (merchant) => {
  detailVisible.value = true
  detailStats.value = null
  detailLoading.value = true
  try {
    const res = await getMerchantStats(merchant.merchantId)
    detailStats.value = res.data || null
  } catch (e) {
    Message.error(e?.message || '加载商户详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

const closeDetail = () => {
  detailVisible.value = false
  detailStats.value = null
}

const handleAudit = async (merchant, status) => {
  try {
    await auditMerchant(merchant.merchantId, status)
    Message.success(status === 1 ? '审核通过' : '已拒绝')
    await loadMerchants()
  } catch (error) {
    Message.error('操作失败')
    console.error(error)
  }
}

const handleDelete = async (merchant) => {
  if (
    !confirm(
      `确定删除商户「${merchant.merchantName}」吗？\n将同时删除该商户下属订单与商品。\n演示账号 merchant1/merchant2 下「名称正常」的商户不可删；乱码重复行可删。`
    )
  ) {
    return
  }
  try {
    await deleteMerchant(merchant.merchantId, true)
    Message.success('已删除')
    if (detailVisible.value) closeDetail()
    await loadMerchants()
  } catch (e) {
    Message.error(e?.message || '删除失败')
  }
}
</script>

<style scoped>
.table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.table thead th {
  font-weight: 600;
  color: #495057;
}

.table tbody tr:hover {
  background: #f8f9fa;
}

.badge {
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: 500;
  display: inline-block;
}

.badge-warning {
  background: #ffc107;
  color: #212529;
}

.badge-success {
  background: #28a745;
  color: white;
}

.badge-danger {
  background: #dc3545;
  color: white;
}

.badge-secondary {
  background: #6c757d;
  color: white;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 0.9em;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  padding: 16px;
}

.modal-panel {
  background: #fff;
  border-radius: 14px;
  width: min(560px, 100%);
  max-height: 90vh;
  overflow: auto;
  box-shadow: 0 14px 40px rgba(0, 0, 0, 0.18);
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid #e9ecef;
}

.btn-close {
  border: none;
  background: none;
  font-size: 1.75rem;
  line-height: 1;
  cursor: pointer;
  color: #6c757d;
}

.modal-body {
  padding: 20px;
}

.modal-foot {
  padding: 12px 18px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
}

.detail-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 14px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 14px;
  margin-bottom: 18px;
}

.detail-grid .k {
  display: block;
  font-size: 0.82rem;
  color: #6b7280;
}

.detail-grid .v {
  color: #111827;
}

.stats-title {
  font-weight: 600;
  color: #374151;
  margin-bottom: 10px;
  font-size: 0.95rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

@media (min-width: 480px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

.stat-card {
  background: #f7fcf8;
  border: 1px solid #d7ead9;
  border-radius: 10px;
  padding: 12px;
}

.stat-label {
  font-size: 0.78rem;
  color: #6b7280;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 1.15rem;
  font-weight: 700;
  color: #1b5e20;
}

.text-warning {
  color: #d97706 !important;
}

.text-success {
  color: #15803d !important;
}
</style>
