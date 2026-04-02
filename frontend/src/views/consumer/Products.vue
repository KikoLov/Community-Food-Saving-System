<template>
  <div class="products-hall">
    <!-- 社区选择：情感化背景 + 插图卡片 + 垂直居中 -->
    <div v-if="!selectedCommunity" class="community-pick-page">
      <div class="community-pick-bg" aria-hidden="true">
        <div class="community-pick-bg-photo"></div>
        <div class="community-pick-bg-tint"></div>
      </div>
      <div class="community-pick-decor" aria-hidden="true">
        <span class="decor decor-leaf decor-l1">🍃</span>
        <span class="decor decor-leaf decor-l2">🌿</span>
        <span class="decor decor-recycle" title="循环">♻</span>
        <span class="decor decor-co2">CO₂</span>
        <span class="decor decor-grid">▦</span>
        <span class="decor decor-globe">◉</span>
      </div>

      <div class="community-pick-panel">
        <div v-if="loading" class="community-pick-loading text-center">
          <div class="spinner"></div>
          <p class="text-muted mt-2">加载社区列表...</p>
        </div>

        <template v-else>
          <header class="community-pick-header">
            <span class="title-hearts" aria-hidden="true">💚</span>
            <div class="title-block">
              <div class="title-row">
                <svg class="title-pin" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                  <path
                    fill="currentColor"
                    d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5A2.5 2.5 0 1 1 12 6.5a2.5 2.5 0 0 1 0 5z"
                  />
                </svg>
                <h2 class="community-pick-title">选择您的社区</h2>
              </div>
              <p class="community-pick-lead">邻里新鲜，低碳减损，从选对社区开始</p>
            </div>
            <span class="title-hearts" aria-hidden="true">💚</span>
          </header>

          <div class="community-search-wrap">
            <span class="search-icon" aria-hidden="true">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="7" />
                <path d="M20 20l-4-4" />
              </svg>
            </span>
            <input
              v-model.trim="communitySearch"
              type="search"
              class="community-search-input"
              placeholder="搜索社区或定位您的位置…"
              autocomplete="off"
            >
            <span class="search-locate" aria-hidden="true" title="定位">◎</span>
          </div>

          <p class="community-section-label">您所在的社区</p>

          <div v-if="filteredCommunities.length === 0" class="community-empty text-muted text-center py-4">
            未找到匹配的社区，请尝试其他关键词
          </div>

          <div v-else class="row community-cards-row g-4">
            <div
              v-for="community in filteredCommunities"
              :key="community.communityId"
              class="col-12 col-md-6 col-lg-4"
            >
              <div
                class="community-card"
                role="button"
                tabindex="0"
                @click="selectCommunity(community)"
                @keydown.enter.prevent="selectCommunity(community)"
              >
                <div class="community-card-art" :class="'theme-' + communityTheme(community)">
                  <!-- 绿城：邻里楼宇 + 绿树 + 骑行 -->
                  <svg
                    v-if="communityTheme(community) === 'green-city'"
                    class="art-svg"
                    viewBox="0 0 320 200"
                    xmlns="http://www.w3.org/2000/svg"
                    aria-hidden="true"
                  >
                    <defs>
                      <linearGradient :id="'gc-sky-' + community.communityId" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" stop-color="#e8f5e9" />
                        <stop offset="100%" stop-color="#c8e6c9" />
                      </linearGradient>
                      <linearGradient :id="'gc-grass-' + community.communityId" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" stop-color="#a5d6a7" />
                        <stop offset="100%" stop-color="#66bb6a" />
                      </linearGradient>
                    </defs>
                    <rect width="320" height="200" :fill="'url(#gc-sky-' + community.communityId + ')'" />
                    <ellipse cx="260" cy="36" rx="28" ry="26" fill="#fff9c4" opacity="0.9" />
                    <rect y="150" width="320" height="50" :fill="'url(#gc-grass-' + community.communityId + ')'" />
                    <rect x="28" y="88" width="52" height="62" rx="4" fill="#81c784" />
                    <rect x="34" y="96" width="14" height="10" fill="#e8f5e9" opacity="0.85" />
                    <rect x="52" y="96" width="14" height="10" fill="#e8f5e9" opacity="0.85" />
                    <rect x="90" y="62" width="64" height="88" rx="4" fill="#4caf50" />
                    <rect x="98" y="74" width="16" height="14" fill="#fffde7" opacity="0.9" />
                    <rect x="120" y="74" width="16" height="14" fill="#fffde7" opacity="0.9" />
                    <rect x="98" y="98" width="16" height="14" fill="#fffde7" opacity="0.9" />
                    <rect x="168" y="78" width="56" height="72" rx="4" fill="#66bb6a" />
                    <rect x="176" y="88" width="12" height="10" fill="#e3f2fd" opacity="0.95" />
                    <rect x="192" y="88" width="12" height="10" fill="#e3f2fd" opacity="0.95" />
                    <circle cx="238" cy="118" r="22" fill="#2e7d32" />
                    <circle cx="238" cy="118" r="12" fill="#43a047" />
                    <ellipse cx="200" cy="168" rx="22" ry="8" fill="#37474f" opacity="0.15" />
                    <circle cx="188" cy="154" r="7" fill="#5d4037" />
                    <circle cx="212" cy="154" r="7" fill="#5d4037" />
                    <path d="M186 148 L200 138 L214 148" stroke="#ff7043" stroke-width="3" fill="none" stroke-linecap="round" />
                    <circle cx="274" cy="132" r="6" fill="#ffb74d" />
                    <circle cx="288" cy="140" r="5" fill="#ffb74d" />
                  </svg>
                  <!-- 阳光花园：凉亭 + 花草 -->
                  <svg
                    v-else-if="communityTheme(community) === 'sun-garden'"
                    class="art-svg"
                    viewBox="0 0 320 200"
                    xmlns="http://www.w3.org/2000/svg"
                    aria-hidden="true"
                  >
                    <defs>
                      <linearGradient :id="'sg-sky-' + community.communityId" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" stop-color="#fff8e1" />
                        <stop offset="100%" stop-color="#ffe082" stop-opacity="0.35" />
                      </linearGradient>
                    </defs>
                    <rect width="320" height="200" :fill="'url(#sg-sky-' + community.communityId + ')'" />
                    <circle cx="72" cy="48" r="36" fill="#ffd54f" opacity="0.95" />
                    <circle cx="82" cy="38" r="8" fill="#fff9c4" opacity="0.7" />
                    <ellipse cx="160" cy="175" rx="140" ry="28" fill="#81c784" />
                    <ellipse cx="160" cy="168" rx="120" ry="18" fill="#a5d6a7" />
                    <path d="M120 120 L200 120 L190 88 L130 88 Z" fill="#8d6e63" />
                    <path d="M118 120 L202 120 L160 72 Z" fill="#6d4c41" />
                    <rect x="150" y="120" width="20" height="40" fill="#5d4037" />
                    <circle cx="100" cy="150" r="10" fill="#e91e63" />
                    <circle cx="128" cy="158" r="8" fill="#f48fb1" />
                    <circle cx="220" cy="152" r="9" fill="#ff9800" />
                    <circle cx="248" cy="160" r="7" fill="#ffcc80" />
                    <ellipse cx="72" cy="162" rx="14" ry="6" fill="#4e342e" opacity="0.2" />
                    <ellipse cx="92" cy="156" rx="10" ry="5" fill="#4e342e" opacity="0.15" />
                    <path d="M260 130 Q280 100 300 130" stroke="#43a047" stroke-width="4" fill="none" stroke-linecap="round" />
                    <circle cx="268" cy="118" r="6" fill="#66bb6a" />
                  </svg>
                  <!-- 月亮湾：傍晚 + 月牙 -->
                  <svg
                    v-else-if="communityTheme(community) === 'moon-bay'"
                    class="art-svg"
                    viewBox="0 0 320 200"
                    xmlns="http://www.w3.org/2000/svg"
                    aria-hidden="true"
                  >
                    <defs>
                      <linearGradient :id="'mb-sky-' + community.communityId" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" stop-color="#1a237e" stop-opacity="0.5" />
                        <stop offset="55%" stop-color="#5c6bc0" stop-opacity="0.35" />
                        <stop offset="100%" stop-color="#c5cae9" stop-opacity="0.5" />
                      </linearGradient>
                    </defs>
                    <rect width="320" height="200" :fill="'url(#mb-sky-' + community.communityId + ')'" />
                    <path d="M220 48 A28 28 0 1 1 220 76 A22 22 0 1 0 220 48Z" fill="#fffde7" opacity="0.95" />
                    <rect x="40" y="100" width="48" height="70" rx="3" fill="#7986cb" />
                    <rect x="48" y="112" width="10" height="8" fill="#e8eaf6" opacity="0.9" />
                    <rect x="100" y="78" width="70" height="92" rx="4" fill="#5c6bc0" />
                    <rect x="110" y="92" width="12" height="10" fill="#fff59d" opacity="0.85" />
                    <rect x="128" y="92" width="12" height="10" fill="#fff59d" opacity="0.85" />
                    <rect x="188" y="88" width="60" height="82" rx="3" fill="#7e57c2" opacity="0.85" />
                    <rect y="155" width="320" height="45" fill="#3949ab" opacity="0.25" />
                    <ellipse cx="160" cy="178" rx="100" ry="12" fill="#1a237e" opacity="0.15" />
                  </svg>
                  <!-- 默认社区 -->
                  <svg
                    v-else
                    class="art-svg"
                    viewBox="0 0 320 200"
                    xmlns="http://www.w3.org/2000/svg"
                    aria-hidden="true"
                  >
                    <rect width="320" height="200" fill="#e8f5e9" />
                    <rect x="60" y="70" width="200" height="80" rx="12" fill="#a5d6a7" />
                    <rect x="80" y="90" width="36" height="28" rx="4" fill="#fff8e1" />
                    <rect x="130" y="90" width="36" height="28" rx="4" fill="#fff8e1" />
                    <rect x="180" y="90" width="36" height="28" rx="4" fill="#fff8e1" />
                    <path d="M140 60 L160 40 L180 60 Z" fill="#66bb6a" />
                    <circle cx="250" cy="56" r="20" fill="#fff59d" opacity="0.9" />
                    <text x="160" y="178" text-anchor="middle" font-size="14" fill="#2e7d32" font-family="system-ui,sans-serif" font-weight="700">邻里社区</text>
                  </svg>
                  <span class="community-card-badge" :class="demoMeta(community).badgeClass">
                    {{ demoMeta(community).badge }}
                  </span>
                </div>
                <div class="community-card-body">
                  <h3 class="community-card-name">{{ community.communityName }}</h3>
                  <p class="community-card-meta">{{ communityStatsLine(community) }}</p>
                </div>
              </div>
            </div>
          </div>
        </template>
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
const communitySearch = ref('')

const filteredCommunities = computed(() => {
  const list = communities.value || []
  const q = communitySearch.value.trim().toLowerCase()
  if (!q) return list
  return list.filter((c) => {
    const name = String(c.communityName || '').toLowerCase()
    const code = String(c.communityCode || c.community_code || '').toLowerCase()
    return name.includes(q) || code.includes(q)
  })
})

/** 社区卡片插图主题 */
const communityTheme = (community) => {
  const name = String(community?.communityName || '')
  const code = String(community?.communityCode || community?.community_code || '').toLowerCase()
  if (/绿城|greencity|gc_/i.test(name) || code.includes('gc_')) return 'green-city'
  if (/阳光|花园|sungarden|yg_/i.test(name) || code.includes('yg_')) return 'sun-garden'
  if (/月亮|月亮湾|moon/i.test(name) || code.includes('moon')) return 'moon-bay'
  return 'default'
}

/** 角标仍为示意（与演示社区主题对应） */
const demoMeta = (community) => {
  const theme = communityTheme(community)
  const table = {
    'green-city': { badge: '已认证', badgeClass: 'badge-verified' },
    'sun-garden': { badge: '活跃', badgeClass: 'badge-active' },
    'moon-bay': { badge: '推荐', badgeClass: 'badge-rec' }
  }
  const v = table[theme] || { badge: '社区', badgeClass: 'badge-neutral' }
  return { badge: v.badge, badgeClass: v.badgeClass }
}

/** 后端汇总：该社区商户数 + 在售商品数（与居民端商品列表口径一致） */
const communityStatsLine = (community) => {
  const m = Number(community?.merchantCount ?? community?.merchant_count ?? 0) || 0
  const p = Number(community?.onSaleProductCount ?? community?.on_sale_product_count ?? 0) || 0
  return `商家 ${m} 家｜在售商品 ${p} 件`
}

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

/* ========== 社区选择页：情感背景 + 插图卡片 + 垂直居中 ========== */
.community-pick-page {
  position: relative;
  isolation: isolate;
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28px 12px 40px;
  margin: -12px -8px 0;
}

.community-pick-bg {
  position: absolute;
  inset: 0;
  border-radius: 20px;
  overflow: hidden;
  z-index: 0;
}

.community-pick-bg-photo {
  position: absolute;
  inset: -48px;
  background-color: #e8f5e9;
  background-image: url('https://images.unsplash.com/photo-1449844908441-8829872d2607?auto=format&fit=crop&w=1800&q=78');
  background-size: cover;
  background-position: center 40%;
  /* 略减模糊 + 提高亮度，减轻边缘发黑/暗角 */
  filter: blur(11px) saturate(1.05) brightness(1.22);
  transform: scale(1.12);
}

.community-pick-bg-tint {
  position: absolute;
  inset: 0;
  /* 去掉底部偏灰绿的渐变，整体偏亮白；底部再叠一层提亮，避免「模糊发暗」 */
  background:
    linear-gradient(180deg, transparent 0%, transparent 55%, rgba(255, 255, 255, 0.72) 100%),
    radial-gradient(ellipse 95% 65% at 50% 12%, rgba(255, 255, 255, 0.55), transparent 60%),
    linear-gradient(180deg, rgba(248, 253, 248, 0.55) 0%, rgba(255, 255, 255, 0.9) 100%);
  pointer-events: none;
}

.community-pick-decor {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
  overflow: hidden;
  border-radius: 20px;
}

.community-pick-decor .decor {
  position: absolute;
  opacity: 0.14;
  color: #1b5e20;
  font-weight: 800;
  user-select: none;
}

.decor-leaf {
  font-size: 2.6rem;
  filter: grayscale(0.2);
}
.decor-l1 {
  top: 8%;
  left: 4%;
  transform: rotate(-18deg);
}
.decor-l2 {
  bottom: 14%;
  right: 6%;
  transform: rotate(12deg);
}
.decor-recycle {
  top: 22%;
  right: 10%;
  font-size: 2rem;
  opacity: 0.1;
}
.decor-co2 {
  bottom: 28%;
  left: 8%;
  font-size: 0.85rem;
  letter-spacing: 0.06em;
  border: 2px solid currentColor;
  border-radius: 999px;
  padding: 4px 10px;
  opacity: 0.11;
}
.decor-grid {
  right: 5%;
  bottom: 38%;
  font-size: 1.8rem;
  opacity: 0.09;
}
.decor-globe {
  left: 12%;
  top: 38%;
  font-size: 1.5rem;
  opacity: 0.1;
}

.community-pick-panel {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 1100px;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-radius: 22px;
  padding: 36px 32px 40px;
  /* 仅保留浅色描边，避免大卡片底部出现大块投影/发黑 */
  box-shadow: none;
  border: 1px solid rgba(200, 230, 201, 0.85);
}

.community-pick-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-bottom: 10px;
}

.title-hearts {
  font-size: 1.35rem;
  opacity: 0.85;
  filter: drop-shadow(0 1px 2px rgba(46, 125, 50, 0.2));
}

.title-block {
  text-align: center;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.title-pin {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  color: #2e7d32;
  filter: drop-shadow(0 2px 6px rgba(46, 125, 50, 0.25));
}

.community-pick-title {
  margin: 0;
  font-size: clamp(1.65rem, 3.5vw, 2.05rem);
  font-weight: 800;
  letter-spacing: 0.04em;
  color: #1b4332;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.8);
}

.community-pick-lead {
  margin: 10px 0 0;
  font-size: 0.95rem;
  color: #558b2f;
  font-weight: 500;
}

.community-search-wrap {
  position: relative;
  max-width: 560px;
  margin: 22px auto 8px;
}

.community-search-input {
  width: 100%;
  padding: 14px 44px 14px 46px;
  border: 1px solid rgba(129, 199, 132, 0.55);
  border-radius: 999px;
  font-size: 0.98rem;
  background: rgba(255, 255, 255, 0.92);
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.community-search-input:focus {
  border-color: #43a047;
  box-shadow: 0 0 0 3px rgba(67, 160, 71, 0.2);
}

.community-search-wrap .search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #66bb6a;
  display: flex;
  pointer-events: none;
}

.community-search-wrap .search-locate {
  position: absolute;
  right: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: #2e7d32;
  font-size: 1.1rem;
  opacity: 0.55;
  pointer-events: none;
}

.community-section-label {
  text-align: center;
  font-size: 0.92rem;
  font-weight: 600;
  color: #33691e;
  margin: 18px 0 20px;
  letter-spacing: 0.06em;
}

.community-cards-row {
  justify-content: center;
}

.community-card {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid rgba(200, 230, 201, 0.75);
  box-shadow: 0 8px 28px rgba(46, 125, 50, 0.08);
  transition:
    transform 0.28s cubic-bezier(0.34, 1.3, 0.64, 1),
    box-shadow 0.28s ease;
}

.community-card:hover {
  transform: translateY(-8px);
  box-shadow:
    0 20px 48px rgba(27, 94, 32, 0.18),
    0 0 0 1px rgba(129, 199, 132, 0.45);
}

.community-card:focus-visible {
  outline: 3px solid #43a047;
  outline-offset: 3px;
}

.community-card-art {
  position: relative;
  height: 168px;
  overflow: hidden;
}

.community-card-art .art-svg {
  display: block;
  width: 100%;
  height: 100%;
}

.community-card-badge {
  position: absolute;
  right: 10px;
  bottom: 10px;
  font-size: 11px;
  font-weight: 800;
  padding: 4px 10px;
  border-radius: 999px;
  letter-spacing: 0.04em;
}

.badge-verified {
  background: rgba(232, 245, 233, 0.95);
  color: #2e7d32;
  border: 1px solid rgba(76, 175, 80, 0.45);
}
.badge-active {
  background: rgba(255, 248, 225, 0.95);
  color: #f57f17;
  border: 1px solid rgba(255, 183, 77, 0.5);
}
.badge-rec {
  background: rgba(237, 231, 246, 0.95);
  color: #5e35b1;
  border: 1px solid rgba(149, 117, 205, 0.4);
}
.badge-neutral {
  background: rgba(236, 239, 241, 0.95);
  color: #455a64;
  border: 1px solid rgba(176, 190, 197, 0.5);
}

.community-card-body {
  padding: 16px 18px 20px;
}

.community-card-name {
  margin: 0 0 8px;
  font-size: 1.2rem;
  font-weight: 800;
  color: #1b4332;
}

.community-card-meta {
  margin: 0;
  font-size: 0.82rem;
  line-height: 1.55;
  color: #546e7a;
}

@media (max-width: 768px) {
  .community-pick-panel {
    padding: 24px 16px 28px;
  }
  .community-pick-title {
    font-size: 1.45rem;
  }
  .community-card-art {
    height: 150px;
  }
}
</style>
