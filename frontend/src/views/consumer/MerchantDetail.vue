<template>
  <div>
    <div class="d-flex justify-between align-center mb-3">
      <h2>🏪 商家详情</h2>
      <button class="btn btn-secondary" @click="goBack">返回商品大厅</button>
    </div>

    <div v-if="loading" class="card text-center">
      <div class="spinner"></div>
      <p>加载商家详情...</p>
    </div>

    <div v-else-if="!merchant" class="card text-center">
      <p class="text-muted">商家不存在或已下线</p>
    </div>

    <div v-else>
      <div class="card mb-3">
        <h3>{{ merchant.merchantName }}</h3>
        <p class="text-muted">联系电话：{{ merchant.contactPhone || '未填写' }}</p>
        <p class="text-muted">地址：{{ merchant.address || '未填写' }}</p>
        <p class="text-muted">营业时间：{{ merchant.openingHours || '未填写' }}</p>
        <p class="text-muted">店铺简介：{{ merchant.description || '暂无简介' }}</p>
      </div>

      <div class="card mb-3">
        <h3>商品总览</h3>
        <div class="d-flex align-center" style="gap: 10px; flex-wrap: wrap;">
          <span class="badge on-sale">在售 {{ summary.onSale }}</span>
          <span class="badge sold-out">售罄 {{ summary.soldOut }}</span>
          <span class="badge off-shelf">下架 {{ summary.offShelf }}</span>
          <span class="badge expired">已过期 {{ summary.expired }}</span>
          <span class="badge total">总计 {{ products.length }}</span>
        </div>
      </div>

      <div class="card" v-if="products.length > 0">
        <h3 class="mb-3">全部商品</h3>
        <div class="row">
          <div class="col-4" v-for="p in products" :key="p.productId">
            <div class="card item-card">
              <div class="d-flex justify-between align-center mb-2">
                <h4>{{ p.productName }}</h4>
                <span class="status-tag" :class="statusClass(p)">{{ statusText(p) }}</span>
              </div>
              <p class="text-muted">库存：{{ p.stock }}</p>
              <p class="text-muted">价格：¥{{ p.discountPrice }}（原价 ¥{{ p.originalPrice }}）</p>
              <p class="text-muted">过期时间：{{ formatDate(p.expireDatetime) }}</p>
              <p class="text-muted">描述：{{ p.description || '暂无描述' }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="card text-center" v-else>
        <p class="text-muted">该商家暂无商品</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getConsumerMerchantDetail } from '@/api/consumer'
import { Message } from '@/utils/message'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const merchant = ref(null)
const products = ref([])

const summary = computed(() => {
  let onSale = 0
  let soldOut = 0
  let offShelf = 0
  let expired = 0
  const now = Date.now()
  for (const p of products.value) {
    const isExpired = p?.expireDatetime ? new Date(p.expireDatetime).getTime() <= now : false
    if (isExpired) {
      expired++
      continue
    }
    const status = Number(p?.status ?? -1)
    if (status === 1 && Number(p?.stock || 0) > 0) onSale++
    else if (status === 2 || Number(p?.stock || 0) <= 0) soldOut++
    else offShelf++
  }
  return { onSale, soldOut, offShelf, expired }
})

const statusText = (p) => {
  const now = Date.now()
  const isExpired = p?.expireDatetime ? new Date(p.expireDatetime).getTime() <= now : false
  if (isExpired) return '已过期'
  if (Number(p?.status) === 1 && Number(p?.stock || 0) > 0) return '在售'
  if (Number(p?.status) === 2 || Number(p?.stock || 0) <= 0) return '售罄'
  return '下架'
}

const statusClass = (p) => {
  const text = statusText(p)
  if (text === '在售') return 'tag-on-sale'
  if (text === '售罄') return 'tag-sold-out'
  if (text === '下架') return 'tag-off-shelf'
  return 'tag-expired'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '未知'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const goBack = () => {
  router.push('/consumer/products')
}

const loadDetail = async () => {
  loading.value = true
  try {
    const merchantId = route.params.merchantId
    const communityId = route.query.communityId || undefined
    const res = await getConsumerMerchantDetail(merchantId, communityId)
    merchant.value = res?.data?.merchant || null
    products.value = res?.data?.products || []
  } catch (e) {
    Message.error(e?.message || '加载商家详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.item-card { min-height: 210px; }
.status-tag { font-size: 12px; font-weight: 700; padding: 4px 10px; border-radius: 999px; }
.tag-on-sale { background: #e8f5e9; color: #2e7d32; }
.tag-sold-out { background: #fff3e0; color: #ef6c00; }
.tag-off-shelf { background: #eceff1; color: #546e7a; }
.tag-expired { background: #ffebee; color: #c62828; }
.badge { font-size: 12px; font-weight: 700; padding: 4px 10px; border-radius: 999px; }
.badge.on-sale { background: #e8f5e9; color: #2e7d32; }
.badge.sold-out { background: #fff3e0; color: #ef6c00; }
.badge.off-shelf { background: #eceff1; color: #546e7a; }
.badge.expired { background: #ffebee; color: #c62828; }
.badge.total { background: #e3f2fd; color: #1565c0; }
</style>
