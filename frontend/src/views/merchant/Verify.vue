<template>
  <div class="verify-page">
    <h4 class="mb-4"><i class="fas fa-check-circle me-2"></i>核销工作台</h4>

    <div class="card verify-card mb-4">
      <div class="card-body">
        <div class="verify-toolbar">
          <div class="verify-input-wrap">
            <i class="fas fa-barcode"></i>
            <input
              v-model.trim="verifyCode"
              type="text"
              class="verify-input"
              placeholder="请输入6位核销码"
              maxlength="6"
              @keyup.enter="handlePreview"
            >
          </div>
          <button class="btn btn-outline-success" type="button" @click="handlePreview" :disabled="loadingPreview">
            <span v-if="loadingPreview" class="spinner-border spinner-border-sm me-2"></span>
            预览订单
          </button>
        </div>

        <div v-if="previewOrder" class="preview-card mt-4">
          <div class="preview-title">
            <i class="fas fa-file-invoice me-2"></i>核销预览（请确认后再核销）
          </div>
          <div class="row mt-3">
            <div class="col-md-6 mb-3">
              <p class="meta-label">订单号</p>
              <p class="meta-value">{{ previewOrder.orderNo }}</p>
            </div>
            <div class="col-md-6 mb-3">
              <p class="meta-label">商品</p>
              <p class="meta-value">{{ previewOrder.productName }} x {{ previewOrder.quantity }}</p>
            </div>
            <div class="col-md-6 mb-3">
              <p class="meta-label">金额</p>
              <p class="meta-value text-danger">¥{{ previewOrder.totalAmount }}</p>
            </div>
            <div class="col-md-6 mb-3">
              <p class="meta-label">碳减排</p>
              <p class="meta-value text-success">{{ previewOrder.carbonSaved }} kg</p>
            </div>
            <div class="col-md-6 mb-3">
              <p class="meta-label">下单时间</p>
              <p class="meta-value">{{ formatDateTime(previewOrder.createTime) }}</p>
            </div>
            <div class="col-md-6 mb-3">
              <p class="meta-label">买家ID</p>
              <p class="meta-value">{{ previewOrder.userId }}</p>
            </div>
          </div>
          <div class="confirm-bar">
            <button class="btn btn-success" @click="confirmVerify" :disabled="loadingVerify">
              <span v-if="loadingVerify" class="spinner-border spinner-border-sm me-2"></span>
              确认核销
            </button>
            <button class="btn btn-light" @click="clearPreview">取消</button>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <i class="fas fa-history me-2"></i>最近核销记录
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover mb-0">
            <thead class="table-light">
              <tr>
                <th>订单号</th>
                <th>商品</th>
                <th>金额</th>
                <th>核销时间</th>
                <th>核销码</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in recentVerifiedOrders" :key="row.orderId">
                <td>{{ row.orderNo }}</td>
                <td>{{ row.productName }} x {{ row.quantity }}</td>
                <td class="fw-semibold text-danger">¥{{ row.totalAmount }}</td>
                <td>{{ formatDateTime(row.verifyTime) }}</td>
                <td><span class="badge bg-warning text-dark">{{ row.verifyCode }}</span></td>
              </tr>
              <tr v-if="recentVerifiedOrders.length === 0">
                <td colspan="5" class="text-center py-4 text-muted">暂无最近核销记录</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getMerchantOrders, previewVerifyOrder, verifyOrder } from '@/api/merchant'
import { Message } from '@/utils/message'

const verifyCode = ref('')
const loadingPreview = ref(false)
const loadingVerify = ref(false)
const previewOrder = ref(null)
const orders = ref([])

const recentVerifiedOrders = computed(() => {
  return (orders.value || [])
    .filter(o => o.orderStatus === 1)
    .sort((a, b) => new Date(b.verifyTime || 0).getTime() - new Date(a.verifyTime || 0).getTime())
    .slice(0, 8)
})

onMounted(async () => {
  await loadOrders()
})

const loadOrders = async () => {
  const res = await getMerchantOrders()
  orders.value = res.data || []
}

const handlePreview = async () => {
  if (!verifyCode.value || verifyCode.value.length !== 6) {
    Message.warning('请输入6位核销码')
    return
  }
  loadingPreview.value = true
  try {
    const res = await previewVerifyOrder(verifyCode.value)
    previewOrder.value = res.data
  } catch (error) {
    previewOrder.value = null
    const msg = error?.message || '核销预览失败'
    Message.error(msg)
  } finally {
    loadingPreview.value = false
  }
}

const confirmVerify = async () => {
  if (!previewOrder.value) return
  if (!confirm(`确认核销订单 ${previewOrder.value.orderNo} 吗？`)) return
  loadingVerify.value = true
  try {
    const res = await verifyOrder(verifyCode.value)
    previewOrder.value = res.data
    Message.success('核销成功')
    verifyCode.value = ''
    await loadOrders()
  } catch (error) {
    const msg = error?.message || '核销失败'
    Message.error(msg)
  } finally {
    loadingVerify.value = false
  }
}

const clearPreview = () => {
  previewOrder.value = null
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return new Date(datetime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.verify-page {
  padding: 20px 0;
}

.verify-card {
  border: 1px solid #e6f1e8;
}

.verify-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.verify-input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid #d7ead9;
  background: #f7fcf8;
  border-radius: 999px;
  padding: 0 12px;
  min-width: 320px;
}

.verify-input-wrap i {
  color: #5a9460;
}

.verify-input {
  border: none;
  outline: none;
  background: transparent;
  padding: 9px 4px;
  width: 100%;
}

.preview-card {
  border: 1px solid #d9ebdc;
  border-radius: 12px;
  padding: 16px;
  background: #fbfefb;
}

.preview-title {
  color: #2e7d32;
  font-weight: 700;
}

.meta-label {
  margin-bottom: 4px;
  color: #7d8f80;
  font-size: 0.86rem;
}

.meta-value {
  margin-bottom: 0;
  font-weight: 600;
  color: #2e3f31;
}

.confirm-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
