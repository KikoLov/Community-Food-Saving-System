<template>
  <div>
    <div class="card mb-4">
      <div class="card-header">
        <h2>🏪 商户管理</h2>
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
                  <button
                    v-if="merchant.licenseStatus === 0"
                    class="btn btn-success btn-sm"
                    style="margin-right: 8px;"
                    @click="handleAudit(merchant, 1)"
                  >
                    ✓ 通过
                  </button>
                  <button
                    v-if="merchant.licenseStatus === 0"
                    class="btn btn-danger btn-sm"
                    @click="handleAudit(merchant, 2)"
                  >
                    ✗ 拒绝
                  </button>
                  <button
                    v-if="merchant.licenseStatus !== 0"
                    class="btn btn-secondary btn-sm"
                    @click="handleView(merchant)"
                  >
                    👁 查看
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMerchants, auditMerchant } from '@/api/admin'
import { Message } from '@/utils/message'

const merchants = ref([])
const loading = ref(false)

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
  const classes = {
    0: 'badge-warning',
    1: 'badge-success',
    2: 'badge-danger'
  }
  return classes[status] || 'badge-secondary'
}

const getStatusText = (status) => {
  const texts = {
    0: '⏳ 待审核',
    1: '✓ 已通过',
    2: '✗ 已拒绝'
  }
  return texts[status] || '未知'
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

const handleView = (merchant) => {
  Message.info(`查看商户: ${merchant.merchantName}`)
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
</style>
