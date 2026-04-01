<template>
  <div class="products-hall">
    <!-- Community Selection Modal -->
    <div v-if="!selectedCommunity" class="card">
      <div class="card-header">
        <h2>📍 选择您的社区</h2>
      </div>
      <div class="row" v-if="loading">
        <div class="text-center" style="width: 100%;">
          <div class="spinner"></div>
          <p>加载社区列表...</p>
        </div>
      </div>
      <div class="row" v-else>
        <div class="col-4" v-for="community in communities" :key="community.communityId">
          <div
            class="card"
            style="cursor: pointer; transition: transform 0.2s;"
            @click="selectCommunity(community)"
            @mouseover="$event.currentTarget.style.transform = 'translateY(-5px)'"
            @mouseout="$event.currentTarget.style.transform = 'translateY(0)'"
          >
            <h3>{{ community.communityName }}</h3>
          </div>
        </div>
      </div>
    </div>

    <!-- Products List -->
    <div v-else>
      <div class="d-flex justify-between align-center mb-3">
        <h2>{{ selectedCommunity.communityName }} - 临期商品</h2>
        <button @click="clearCommunity" class="btn btn-primary">切换社区</button>
      </div>

      <!-- Merchant Overview -->
      <div class="card mb-3" v-if="merchantList.length > 0">
        <div class="card-header">
          <h3>🏪 本社区商家（{{ merchantList.length }}）</h3>
        </div>
        <div class="row">
          <div class="col-4" v-for="merchant in merchantList" :key="merchant.merchantId">
            <div class="card merchant-card">
              <h4>{{ merchant.merchantName }}</h4>
              <p class="text-muted">在售商品：{{ merchant.productCount }} 件</p>
              <p v-if="merchantRatingMap[merchant.merchantId]" class="text-muted">
                好评率：{{ merchantRatingMap[merchant.merchantId].goodRate }}%（{{ merchantRatingMap[merchant.merchantId].totalReviews }}条）
              </p>
              <button class="btn btn-primary" @click="goMerchantDetail(merchant.merchantId)">查看详情</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Category Filter -->
      <div class="d-flex align-center mb-3" style="gap: 10px; flex-wrap: wrap;">
        <input
          v-model.trim="keyword"
          type="text"
          class="search-input"
          placeholder="搜索商品名/描述"
        >
        <select v-model="sortType" class="sort-select">
          <option value="surprise_first">盲盒优先</option>
          <option value="expire_asc">按临期优先</option>
          <option value="price_asc">价格从低到高</option>
          <option value="price_desc">价格从高到低</option>
          <option value="stock_desc">库存从多到少</option>
        </select>
        <label class="d-flex align-center" style="gap: 6px; cursor: pointer;">
          <input type="checkbox" v-model="surpriseBagOnly">
          <span>仅看盲盒</span>
        </label>
      </div>

      <div class="mb-3">
        <button
          v-for="cat in categories"
          :key="cat.categoryId"
          @click="filterByCategory(cat.categoryId)"
          class="btn"
          :class="selectedCategory === cat.categoryId ? 'btn-primary' : 'btn-outline-primary'"
          style="margin-right: 10px; margin-bottom: 10px;"
        >
          {{ cat.categoryName }}
        </button>
        <button
          v-if="selectedCategory"
          @click="selectedCategory = null"
          class="btn btn-primary"
        >
          全部分类
        </button>
      </div>

      <!-- Products Grid -->
      <div class="row" v-if="loading">
        <div class="text-center" style="width: 100%;">
          <div class="spinner"></div>
          <p>加载商品...</p>
        </div>
      </div>

      <div class="row" v-else-if="processedProducts.length === 0">
        <div class="col-12">
          <div class="card text-center">
            <p class="text-muted">暂无商品</p>
          </div>
        </div>
      </div>

      <div v-else>
        <div class="card mb-3" v-for="merchant in merchantSections" :key="merchant.merchantId">
          <div class="card-header d-flex justify-between align-center">
            <h3>{{ merchant.merchantName }}</h3>
            <div class="d-flex align-center" style="gap: 10px;">
              <span class="text-muted">共 {{ merchant.products.length }} 件在售商品</span>
              <button class="btn btn-primary" @click="goMerchantDetail(merchant.merchantId)">查看商家详情</button>
            </div>
          </div>
          <div class="row">
            <div class="col-4" v-for="product in merchant.products" :key="product.productId">
              <div class="card">
                <div style="text-align: center; padding: 20px 0; background: #f8f9fa; border-radius: 8px 8px 0 0;">
                  <img
                    :src="resolveProductImageSrc(product, { size: 88 })"
                    @error="handleImgError"
                    style="width: 88px; height: 88px; object-fit: cover; border-radius: 10px;"
                    alt="商品图"
                  >
                </div>
                <div style="padding: 15px;">
                  <div class="d-flex align-items-center" style="gap: 6px; margin-bottom: 6px;">
                    <h3 style="font-size: 1.1em; margin: 0;">{{ product.productName }}</h3>
                    <span v-if="product.surpriseBag" class="badge bg-info text-dark">盲盒</span>
                  </div>
                  <p class="text-muted" style="font-size: 0.9em; margin-bottom: 10px;">{{ product.description }}</p>
                  <p v-if="product.surpriseBag" style="font-size: 0.85em; color: #d63384; margin-bottom: 8px;">名义价值：¥{{ product.bagValue ?? '-' }}</p>

                  <div class="d-flex justify-between align-center mb-2">
                    <div>
                      <span style="text-decoration: line-through; color: #999;">¥{{ product.originalPrice }}</span>
                      <span style="font-size: 1.3em; font-weight: bold; color: var(--danger-color); margin-left: 10px;">
                        ¥{{ product.discountPrice }}
                      </span>
                    </div>
                    <span class="text-muted">库存: {{ product.stock }}</span>
                  </div>

                  <p class="text-muted" style="font-size: 0.85em;">
                    <span>⏰ 过期时间: {{ formatDate(product.expireDatetime) }}</span>
                  </p>

                  <button
                    @click="handleAddToCart(product)"
                    class="btn btn-primary btn-block"
                    :disabled="product.stock === 0"
                  >
                    {{ product.stock === 0 ? '已售罄' : '加入购物车' }}
                  </button>
                </div>
              </div>
            </div>
            <div v-if="merchant.products.length === 0" class="col-12">
              <p class="text-muted">该商家暂无符合筛选条件的商品</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCommunities, getProducts, getCommunityMerchants, addToCart as addToCartApi, getMerchantRatingSummary } from '@/api/consumer'
import { Message } from '@/utils/message'
import { fixCommunityRecord, isLikelyGarbled } from '@/utils/textFixer'
import { resolveProductImageSrc, buildNameBasedProductImage } from '@/utils/productImage'
import { normalizeProductRecord, normalizeCategoryName } from '@/utils/demoTextNormalizer'

const selectedCommunity = ref(null)
const router = useRouter()
const communities = ref([])
const products = ref([])
const merchantList = ref([])
const categories = ref([])
const selectedCategory = ref(null)
const keyword = ref('')
const sortType = ref('surprise_first')
const surpriseBagOnly = ref(false)
const loading = ref(false)
const merchantRatingMap = ref({})

const processedProducts = computed(() => {
  let list = [...products.value]
  if (selectedCategory.value) {
    list = list.filter(p => p.categoryId === selectedCategory.value)
  }
  if (surpriseBagOnly.value) {
    list = list.filter(p => p.surpriseBag === 1 || p.surpriseBag === true)
  }
  if (keyword.value) {
    const q = keyword.value.toLowerCase()
    list = list.filter(p =>
      String(p.productName || '').toLowerCase().includes(q) ||
      String(p.description || '').toLowerCase().includes(q)
    )
  }
  if (sortType.value === 'surprise_first') {
    list.sort((a, b) => {
      const aIsSurprise = (a.surpriseBag === 1 || a.surpriseBag === true) ? 0 : 1
      const bIsSurprise = (b.surpriseBag === 1 || b.surpriseBag === true) ? 0 : 1
      if (aIsSurprise !== bIsSurprise) return aIsSurprise - bIsSurprise
      return new Date(a.expireDatetime).getTime() - new Date(b.expireDatetime).getTime()
    })
  } else if (sortType.value === 'price_asc') {
    list.sort((a, b) => Number(a.discountPrice || 0) - Number(b.discountPrice || 0))
  } else if (sortType.value === 'price_desc') {
    list.sort((a, b) => Number(b.discountPrice || 0) - Number(a.discountPrice || 0))
  } else if (sortType.value === 'stock_desc') {
    list.sort((a, b) => Number(b.stock || 0) - Number(a.stock || 0))
  } else {
    list.sort((a, b) => new Date(a.expireDatetime).getTime() - new Date(b.expireDatetime).getTime())
  }
  return list
})

const merchantSections = computed(() => {
  const map = new Map((merchantList.value || []).map((m) => [m.merchantId, {
    merchantId: m.merchantId,
    merchantName: m.merchantName || `商家#${m.merchantId}`,
    products: []
  }]))
  processedProducts.value.forEach((p) => {
    const key = p.merchantId
    if (!map.has(key)) {
      map.set(key, {
        merchantId: p.merchantId,
        merchantName: p.merchantName || `商家#${p.merchantId}`,
        products: []
      })
    }
    map.get(key).products.push(p)
  })
  return Array.from(map.values())
})

const loadCommunities = async () => {
  loading.value = true
  try {
    const res = await getCommunities()
    communities.value = (res.data || []).map(fixCommunityRecord)
  } catch (error) {
    Message.error('加载社区列表失败')
  } finally {
    loading.value = false
  }
}

const loadProducts = async () => {
  loading.value = true
  try {
    const selected = selectedCommunity.value || {}
    const communityId =
      selected?.communityId ?? selected?.community_id ?? selected?.id ?? null
    if (!communityId) {
      // 不要静默失败，否则用户会一直看到空白页
      Message.error('请选择有效的社区后再查看商品')
      products.value = []
      categories.value = []
      return
    }

    const [productsRes, merchantsRes] = await Promise.all([
      getProducts(communityId),
      getCommunityMerchants(communityId)
    ])
    const rawList = productsRes.data || []
    merchantList.value = (merchantsRes.data || [])
      .map((m) => ({
        merchantId: m.merchantId,
        merchantName: m.merchantName,
        productCount: Number(m.productCount || 0)
      }))
      .sort((a, b) => b.productCount - a.productCount)
    const fallbackByMerchant = {
      7: ['绿城新鲜牛奶', '绿城全麦面包', '绿城苹果果切'],
      8: ['阳光原味酸奶', '阳光蛋糕卷', '阳光鲜橙汁']
    }
    const enToZhProductName = {
      'Greencity Milk': '绿城新鲜牛奶',
      'Greencity Whole Wheat Bread': '绿城全麦面包',
      'Greencity Apple Slices': '绿城苹果果切',
      'SunGarden Yogurt': '阳光原味酸奶',
      'SunGarden Cake Roll': '阳光蛋糕卷',
      'SunGarden Orange Juice': '阳光鲜橙汁'
    }
    const enToZhMerchantName = {
      'Greencity Demo Store': '绿城小区社区便利店',
      'SunGarden Demo Store': '阳光花园社区便利店'
    }
    const counter = {}
    products.value = rawList.map((p) => {
      const next = normalizeProductRecord({ ...p })
      if (enToZhMerchantName[next.merchantName]) {
        next.merchantName = enToZhMerchantName[next.merchantName]
      }
      if (enToZhProductName[next.productName]) {
        next.productName = enToZhProductName[next.productName]
      }
      if (isLikelyGarbled(next.merchantName)) {
        next.merchantName = next.merchantId === 7 ? '绿城小区社区便利店' : next.merchantId === 8 ? '阳光花园社区便利店' : next.merchantName
      }
      if (isLikelyGarbled(next.productName)) {
        counter[next.merchantId] = (counter[next.merchantId] || 0)
        const idx = counter[next.merchantId]
        counter[next.merchantId]++
        const fallback = fallbackByMerchant[next.merchantId]?.[idx]
        if (fallback) next.productName = fallback
      }
      if (isLikelyGarbled(next.description)) {
        next.description = '演示商品'
      }
      return next
    })

    // Extract unique categories
    const uniqueCategories = [...new Set(products.value.map(p => p.categoryId))]
    categories.value = uniqueCategories.map(catId => {
      const product = products.value.find(p => p.categoryId === catId)
      return {
        categoryId: catId,
        categoryName: normalizeCategoryName(product?.categoryName) || '分类' + catId
      }
    })
    await loadMerchantRatings()
  } catch (error) {
    Message.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const loadMerchantRatings = async () => {
  const ids = [...new Set((merchantList.value || []).map(m => m.merchantId).filter(Boolean))]
  const next = {}
  await Promise.all(ids.map(async (merchantId) => {
    try {
      const res = await getMerchantRatingSummary(merchantId)
      next[merchantId] = res.data || { totalReviews: 0, goodRate: 0, avgRating: 0 }
    } catch (e) {
      next[merchantId] = { totalReviews: 0, goodRate: 0, avgRating: 0 }
    }
  }))
  merchantRatingMap.value = next
}

const selectCommunity = (community) => {
  const fixed = fixCommunityRecord(community)
  // 兼容不同字段命名：后端 JSON 理论上是 communityId，但为避免历史数据字段差异导致空列表
  const communityId =
    community?.communityId ?? community?.community_id ?? community?.id ??
    fixed?.communityId ?? fixed?.community_id ?? fixed?.id ?? null
  selectedCommunity.value = { ...fixed, communityId }

  // 切换社区时重置筛选，避免上一社区选中的分类/关键词导致“看不到商品”
  selectedCategory.value = null
  keyword.value = ''
  loadProducts()
}

const clearCommunity = () => {
  selectedCommunity.value = null
  products.value = []
  merchantList.value = []
  categories.value = []
  selectedCategory.value = null
}

const filterByCategory = (categoryId) => {
  selectedCategory.value = categoryId
}

const handleAddToCart = async (product) => {
  try {
    await addToCartApi(product.productId, 1)
    Message.success(`已将 ${product.productName} 加入购物车`)
  } catch (e) {
    Message.error('加入购物车失败')
  }
}

const handleImgError = (e) => {
  e.target.src = buildNameBasedProductImage({}, 88)
}

const goMerchantDetail = (merchantId) => {
  const communityId = selectedCommunity.value?.communityId || undefined
  router.push({
    name: 'ConsumerMerchantDetail',
    params: { merchantId },
    query: communityId ? { communityId } : undefined
  })
}

const formatDate = (dateStr) => {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadCommunities()
})
</script>

<style scoped>
.search-input {
  min-width: 240px;
  border: 1px solid #d7ead9;
  border-radius: 999px;
  padding: 8px 12px;
  outline: none;
}

.sort-select {
  border: 1px solid #d7ead9;
  border-radius: 999px;
  padding: 8px 12px;
  background: #f7fcf8;
}

/* 与「加入购物车」同为 btn-primary；未选分类用线框主色，避免 Bootstrap 默认蓝描边 */
.products-hall :deep(.btn-outline-primary) {
  color: var(--primary-color, #2e7d32);
  border: 2px solid var(--primary-color, #2e7d32);
  background: transparent;
}
.products-hall :deep(.btn-outline-primary:hover) {
  background: rgba(46, 125, 50, 0.12);
  color: var(--dark-color, #1b5e20);
  border-color: var(--dark-color, #1b5e20);
}
</style>
