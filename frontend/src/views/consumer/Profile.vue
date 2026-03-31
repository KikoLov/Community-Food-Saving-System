<template>
  <div class="profile-page">
    <h4 class="mb-4"><i class="fas fa-user me-2"></i>个人中心</h4>

    <div class="card mb-3">
      <div class="card-body">
        <div class="balance-panel">
          <div class="balance-label">钱包余额</div>
          <div class="balance-value">¥{{ formatMoney(profileForm.walletBalance) }}</div>
          <div class="balance-tip">顾客账号默认初始余额 ¥200.00，下单支付后会自动扣减。</div>
        </div>
      </div>
    </div>

    <div class="card mb-3">
      <div class="card-body">
        <button type="button" class="wallet-dropdown-btn" @click="walletLogsVisible = !walletLogsVisible">
          <span><i class="fas fa-receipt me-2"></i>余额变动记录</span>
          <span class="wallet-dropdown-arrow">{{ walletLogsVisible ? '▲' : '▼' }}</span>
        </button>
        <div v-if="walletLogsVisible" class="wallet-dropdown-content">
          <div v-if="walletLogs.length === 0" class="text-muted small">暂无余额变动记录</div>
          <div v-else class="wallet-log-list">
            <div v-for="item in walletLogs" :key="item.key" class="wallet-log-item">
              <div>
                <div class="wallet-log-title">{{ item.title }}</div>
                <div class="wallet-log-time">{{ formatDateTime(item.time) }}</div>
              </div>
              <div :class="['wallet-log-amount', item.amount >= 0 ? 'income' : 'expense']">
                {{ item.amount >= 0 ? '+' : '-' }}¥{{ formatMoney(Math.abs(item.amount)) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-body">
        <div>
          <div class="mb-3">
            <label class="form-label">用户名</label>
            <input type="text" class="form-control" v-model="profileForm.userName" disabled>
          </div>

          <div class="mb-3">
            <label class="form-label">昵称</label>
            <input type="text" class="form-control" v-model="profileForm.nickName">
          </div>

          <div class="mb-3">
            <label class="form-label">手机号</label>
            <input type="text" class="form-control" v-model="profileForm.phonenumber">
          </div>

          <div class="mb-3">
            <label class="form-label">邮箱</label>
            <input type="email" class="form-control" v-model="profileForm.email">
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCarbonCenter, getMyOrders } from '@/api/consumer'
import { getUserInfo } from '@/api/auth'

const walletLogs = ref([])
const walletLogsVisible = ref(false)
const profileForm = reactive({
  userName: '',
  nickName: '',
  phonenumber: '',
  email: '',
  walletBalance: 0
})

onMounted(async () => {
  try {
    const userRes = await getUserInfo()
    const userInfo = userRes.data
    Object.assign(profileForm, {
      userName: userInfo.userName,
      nickName: userInfo.nickName,
      phonenumber: userInfo.phonenumber,
      email: userInfo.email
    })

    const carbonRes = await getCarbonCenter()
    profileForm.walletBalance = Number(carbonRes?.data?.profile?.walletBalance || 0)

    const ordersRes = await getMyOrders()
    walletLogs.value = buildWalletLogs(ordersRes?.data || [])
  } catch (error) {
    console.error(error)
  }
})

const formatMoney = (v) => {
  const n = Number(v)
  if (Number.isNaN(n)) return '0.00'
  return n.toFixed(2)
}

const formatDateTime = (v) => {
  if (!v) return '-'
  const d = new Date(v)
  return d.toLocaleString('zh-CN')
}

const buildWalletLogs = (orders) => {
  // 仅展示最近两次“消费扣款”记录，避免历史演示数据干扰余额对账。
  const payLogs = []
  for (const o of orders) {
    const amount = Number(o?.totalAmount || 0)
    if (!amount) continue
    const orderNo = o?.orderNo || '-'
    const productName = o?.productName || '商品'
    payLogs.push({
      key: `pay-${o.orderId || orderNo}`,
      title: `订单支付 · ${productName}（${orderNo}）`,
      amount: -amount,
      time: o?.createTime || o?.updateTime
    })
  }
  return payLogs
    .sort((a, b) => new Date(b.time || 0).getTime() - new Date(a.time || 0).getTime())
    .slice(0, 2)
}
</script>

<style scoped>
.profile-page {
  padding: 20px 0;
}

.balance-panel {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.balance-label {
  color: #6b7280;
  font-size: 0.9rem;
}

.balance-value {
  font-size: 2rem;
  font-weight: 800;
  color: #1f8b4c;
  line-height: 1.2;
}

.balance-tip {
  color: #6b7280;
  font-size: 0.85rem;
}

.wallet-log-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.wallet-dropdown-btn {
  width: 100%;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #1f2937;
  cursor: pointer;
}

.wallet-dropdown-arrow {
  color: #6b7280;
  font-size: 0.9rem;
}

.wallet-dropdown-content {
  margin-top: 12px;
}

.wallet-log-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fafafa;
}

.wallet-log-title {
  font-size: 0.92rem;
  color: #111827;
  margin-bottom: 2px;
}

.wallet-log-time {
  font-size: 0.8rem;
  color: #6b7280;
}

.wallet-log-amount {
  font-size: 1rem;
  font-weight: 700;
}

.wallet-log-amount.income {
  color: #0f7b3f;
}

.wallet-log-amount.expense {
  color: #c62828;
}

</style>
