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

      <MerchantReviewsPanel
        v-if="merchant"
        :merchant-id="merchant.merchantId"
        :merchant-name="merchant.merchantName"
      />

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
        <p class="text-muted small mb-3">点击卡片可查看详情；在售商品可直接加入购物车。</p>
        <div class="row">
          <div class="col-4" v-for="p in products" :key="p.productId">
            <div
              class="card item-card item-card--interactive"
              role="button"
              tabindex="0"
              @click="openProductDetail(p)"
              @keydown.enter.prevent="openProductDetail(p)"
            >
              <div class="item-thumb-wrap">
                <img
                  class="item-thumb"
                  :src="resolveProductImageSrc(p, { size: 96 })"
                  alt=""
                  @error="onThumbError"
                >
              </div>
              <div class="d-flex justify-between align-center mb-2">
                <h4 class="item-title">{{ p.productName }}</h4>
                <span class="status-tag" :class="statusClass(p)">{{ statusText(p) }}</span>
              </div>
              <p v-if="p.surpriseBag" class="text-muted small mb-1">名义价值：¥{{ p.bagValue ?? '-' }}</p>
              <p class="text-muted">库存：{{ p.stock }}</p>
              <p class="text-muted">价格：¥{{ p.discountPrice }}（原价 ¥{{ p.originalPrice }}）</p>
              <p class="text-muted item-desc-preview">描述：{{ p.description || '暂无描述' }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="card text-center" v-else>
        <p class="text-muted">该商家暂无商品</p>
      </div>
    </div>

    <Teleport to="body">
      <div
        v-if="detailProduct"
        class="product-detail-backdrop"
        @click.self="closeProductDetail"
      >
        <div class="product-detail-dialog card" role="dialog" aria-modal="true" @click.stop>
          <div class="product-detail-header">
            <h3 class="mb-0">{{ detailProduct.productName }}</h3>
            <button type="button" class="btn-close-dialog" aria-label="关闭" @click="closeProductDetail">×</button>
          </div>
          <div class="product-detail-body">
            <div class="detail-image-wrap">
              <img
                :src="resolveProductImageSrc(detailProduct, { size: 200 })"
                alt=""
                @error="onThumbError"
              >
            </div>
            <div class="detail-meta">
              <span class="status-tag" :class="statusClass(detailProduct)">{{ statusText(detailProduct) }}</span>
              <span v-if="detailProduct.surpriseBag" class="badge bg-info text-dark ms-2">盲盒</span>
            </div>
            <p v-if="detailProduct.surpriseBag" class="mb-2">
              <span class="text-muted">名义价值：</span>¥{{ detailProduct.bagValue ?? '-' }}
            </p>
            <p class="mb-2">
              <span class="text-muted">库存：</span>{{ detailProduct.stock }}
            </p>
            <p class="mb-2">
              <span class="text-muted line-price">¥{{ detailProduct.originalPrice }}</span>
              <strong class="text-danger ms-2 fs-5">¥{{ detailProduct.discountPrice }}</strong>
              <span class="text-muted small ms-1">现价</span>
            </p>
            <p class="mb-2">
              <span class="text-muted">过期时间：</span>{{ formatDate(detailProduct.expireDatetime) }}
            </p>
            <p v-if="detailProduct.categoryName" class="mb-2">
              <span class="text-muted">分类：</span>{{ detailProduct.categoryName }}
            </p>
            <p class="mb-3">
              <span class="text-muted">描述：</span>{{ detailProduct.description || '暂无描述' }}
            </p>

            <div v-if="canPurchase(detailProduct)" class="detail-buy-row">
              <label class="me-2 text-muted">数量</label>
              <input
                v-model.number="detailQty"
                type="number"
                class="form-control detail-qty-input"
                min="1"
                :max="detailProduct.stock"
                step="1"
              >
            </div>
            <p v-else class="text-warning small mb-0">当前状态不可购买（已过期、售罄或已下架时请留意商家补货）。</p>
          </div>
          <div class="product-detail-footer">
            <button type="button" class="btn btn-secondary" @click="closeProductDetail">关闭</button>
            <button
              v-if="canPurchase(detailProduct)"
              type="button"
              class="btn btn-primary"
              :disabled="detailQty < 1 || detailQty > detailProduct.stock"
              @click="addDetailToCart"
            >
              加入购物车
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getConsumerMerchantDetail, addToCart } from '@/api/consumer'
import { Message } from '@/utils/message'
import { resolveProductImageSrc, buildNameBasedProductImage } from '@/utils/productImage'
import { normalizeProductRecord } from '@/utils/demoTextNormalizer'
import MerchantReviewsPanel from '@/components/MerchantReviewsPanel.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const merchant = ref(null)
const products = ref([])
const detailProduct = ref(null)
const detailQty = ref(1)

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

const canPurchase = (p) => {
  if (!p) return false
  return statusText(p) === '在售'
}

const openProductDetail = (p) => {
  detailProduct.value = p
  detailQty.value = 1
}

const closeProductDetail = () => {
  detailProduct.value = null
}

const onThumbError = (e) => {
  e.target.src = buildNameBasedProductImage({}, 96)
}

const addDetailToCart = async () => {
  const p = detailProduct.value
  if (!p || !canPurchase(p)) return
  const max = Math.max(1, Number(p.stock) || 1)
  const q = Math.min(max, Math.max(1, Math.floor(Number(detailQty.value) || 1)))
  try {
    await addToCart(p.productId, q)
    Message.success(`已将 ${p.productName} ×${q} 加入购物车`)
    closeProductDetail()
  } catch (e) {
    Message.error(e?.message || '加入购物车失败')
  }
}

const onKeyEscape = (e) => {
  if (e.key === 'Escape' && detailProduct.value) {
    closeProductDetail()
  }
}

watch(detailProduct, (v) => {
  if (v) {
    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', onKeyEscape)
  } else {
    document.body.style.overflow = ''
    window.removeEventListener('keydown', onKeyEscape)
  }
})

onUnmounted(() => {
  document.body.style.overflow = ''
  window.removeEventListener('keydown', onKeyEscape)
})

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
    products.value = (res?.data?.products || []).map(normalizeProductRecord)
  } catch (e) {
    Message.error(e?.message || '加载商家详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.item-card {
  min-height: 210px;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}
.item-card--interactive {
  cursor: pointer;
}
.item-card--interactive:hover {
  box-shadow: 0 8px 24px rgba(46, 125, 50, 0.15);
  transform: translateY(-2px);
}
.item-card--interactive:focus-visible {
  outline: 2px solid #2e7d32;
  outline-offset: 2px;
}
.item-thumb-wrap {
  text-align: center;
  padding: 12px 0 8px;
  background: #f8faf8;
  border-radius: 10px;
  margin: -8px -12px 12px -12px;
}
.item-thumb {
  width: 96px;
  height: 96px;
  object-fit: cover;
  border-radius: 12px;
}
.item-title {
  font-size: 1.05rem;
  margin: 0;
  flex: 1;
  min-width: 0;
}
.item-desc-preview {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-detail-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1050;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}
.product-detail-dialog {
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow: auto;
  border: none;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
  border-radius: 16px;
}
.product-detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 16px 0;
  border-bottom: 1px solid #e8f5e9;
  padding-bottom: 12px;
}
.btn-close-dialog {
  border: none;
  background: transparent;
  font-size: 1.75rem;
  line-height: 1;
  color: #666;
  cursor: pointer;
  padding: 0 4px;
}
.btn-close-dialog:hover {
  color: #333;
}
.product-detail-body {
  padding: 16px;
}
.detail-image-wrap {
  text-align: center;
  margin-bottom: 12px;
}
.detail-image-wrap img {
  max-width: 100%;
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 14px;
}
.detail-meta {
  margin-bottom: 8px;
}
.line-price {
  text-decoration: line-through;
  color: #999;
}
.detail-buy-row {
  display: flex;
  align-items: center;
  margin-top: 8px;
}
.detail-qty-input {
  width: 100px;
  display: inline-block;
}
.product-detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 16px 16px;
  border-top: 1px solid #eee;
}

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
