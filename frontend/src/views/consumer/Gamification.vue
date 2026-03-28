<template>
  <div class="gam-page">
    <header class="gam-hero">
      <div class="hero-copy">
        <p class="eyebrow">Gamification · 像蚂蚁森林一样攒能量</p>
        <h1>碳积分能量商城</h1>
        <p class="lead">
          购买临期食品核销后获得<strong>碳积分（Carbon Coins）</strong>，可兑换虚拟树苗、环保徽章与合作优惠券。
        </p>
        <div class="hero-stats">
          <div class="stat-pill">
            <span class="lab">当前碳积分</span>
            <span class="val">{{ fmtCoins(state?.carbonCoins) }}</span>
          </div>
          <div class="stat-pill trees">
            <span class="lab">我的森林</span>
            <span class="val">{{ state?.treesPlanted ?? 0 }} 棵</span>
          </div>
          <router-link to="/consumer/carbon" class="link-soft">← 返回低碳中心</router-link>
        </div>
      </div>
      <div class="hero-forest" aria-hidden="true">
        <div class="forest-ring">
          <div
            v-for="n in Math.min(treeDisplayCount, 12)"
            :key="n"
            class="tree"
            :style="{ '--d': (n * 0.12) + 's' }"
          >
            🌳
          </div>
        </div>
        <p class="forest-hint">每兑换一次「虚拟树苗」，这里会多一棵树</p>
      </div>
    </header>

    <section class="card-block">
      <h2><span class="ico">🛒</span> 能量商城</h2>
      <div class="shop-grid">
        <article v-for="item in catalog" :key="item.code" class="shop-card">
          <div class="card-top">
            <span class="big-ico">{{ item.icon }}</span>
            <span class="cat">{{ catLabel(item.category) }}</span>
          </div>
          <h3>{{ item.title }}</h3>
          <p class="sub">{{ item.subtitle }}</p>
          <div class="cost-row">
            <span class="cost">{{ fmtCoins(item.cost) }}</span>
            <span class="unit">碳积分</span>
          </div>
          <button
            type="button"
            class="btn btn-redeem"
            :disabled="redeeming || !canRedeem(item)"
            @click="doRedeem(item)"
          >
            {{ redeemLabel(item) }}
          </button>
        </article>
      </div>
    </section>

    <div class="two-col">
      <section class="card-block">
        <h2><span class="ico">🎖️</span> 我的徽章</h2>
        <ul v-if="(state?.badges?.length || 0) > 0" class="reward-list">
          <li v-for="b in state.badges" :key="b.redemptionId">
            <strong>{{ b.title }}</strong>
            <span class="muted">{{ formatTime(b.createTime) }}</span>
          </li>
        </ul>
        <p v-else class="empty">暂无徽章，去商城兑换吧</p>
      </section>
      <section class="card-block">
        <h2><span class="ico">🎫</span> 我的优惠券</h2>
        <ul v-if="(state?.coupons?.length || 0) > 0" class="reward-list">
          <li v-for="c in state.coupons" :key="c.redemptionId">
            <div>
              <strong>{{ c.title }}</strong>
              <div class="coupon-code">{{ c.detail }}</div>
            </div>
            <span class="muted">{{ formatTime(c.createTime) }}</span>
          </li>
        </ul>
        <p v-else class="empty">暂无优惠券</p>
      </section>
    </div>

    <section v-if="(state?.recentRedemptions?.length || 0) > 0" class="card-block muted-block">
      <h2><span class="ico">📜</span> 最近兑换</h2>
      <ul class="mini-log">
        <li v-for="r in state.recentRedemptions" :key="r.redemptionId">
          <span>{{ r.title }}</span>
          <span class="muted">-{{ fmtCoins(r.pointsCost) }}</span>
          <span class="muted">{{ formatTime(r.createTime) }}</span>
        </li>
      </ul>
    </section>

    <transition name="pop">
      <div v-if="toast" class="toast">{{ toast }}</div>
    </transition>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { getGamificationCatalog, getGamificationState, redeemGamification } from '@/api/consumer'

const catalog = ref([])
const state = ref(null)
const redeeming = ref(false)
const toast = ref('')

const treeDisplayCount = computed(() => {
  const n = Number(state.value?.treesPlanted || 0)
  return Math.max(1, Math.min(n, 12))
})

const fmtCoins = (v) => {
  const n = Number(v)
  if (Number.isNaN(n)) return '0'
  return n % 1 === 0 ? String(Math.round(n)) : n.toFixed(2)
}

const catLabel = (c) => {
  const m = { TREE: '虚拟树', BADGE: '徽章', COUPON_PLATFORM: '平台券', COUPON_MERCHANT: '商户券' }
  return m[c] || c
}

const canRedeem = (item) => {
  if (item.category === 'BADGE') {
    const owned = state.value?.ownedBadgeCodes?.[item.code]
    return !owned
  }
  return true
}

const redeemLabel = (item) => {
  if (item.category === 'BADGE' && state.value?.ownedBadgeCodes?.[item.code]) {
    return '已拥有'
  }
  return '立即兑换'
}

const load = async () => {
  const [c, s] = await Promise.all([getGamificationCatalog(), getGamificationState()])
  catalog.value = c.data || []
  state.value = s.data || {}
}

const doRedeem = async (item) => {
  if (!canRedeem(item)) return
  redeeming.value = true
  try {
    const res = await redeemGamification(item.code)
    toast.value = res.data?.message || '兑换成功'
    setTimeout(() => { toast.value = '' }, 2600)
    await load()
  } catch (e) {
    // request 已弹 Message
  } finally {
    redeeming.value = false
  }
}

const formatTime = (t) => {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(() => {
  load().catch(() => {})
})
</script>

<style scoped>
.gam-page {
  max-width: 1100px;
  margin: 0 auto;
}

.gam-hero {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 28px;
  align-items: center;
  padding: 28px 26px;
  border-radius: 18px;
  background: linear-gradient(135deg, #e8f8ec 0%, #f4fff6 45%, #fff9e6 100%);
  border: 1px solid rgba(120, 190, 140, 0.35);
  box-shadow: 0 12px 36px rgba(30, 90, 50, 0.08);
  margin-bottom: 28px;
}

.eyebrow {
  font-size: 0.78rem;
  letter-spacing: 0.06em;
  color: #3d6b47;
  text-transform: uppercase;
  margin-bottom: 6px;
}

.gam-hero h1 {
  font-size: 1.75rem;
  font-weight: 800;
  color: #174a22;
  margin: 0 0 10px;
}

.lead {
  color: #3d5344;
  line-height: 1.55;
  margin: 0 0 18px;
  font-size: 0.98rem;
}

.hero-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.stat-pill {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 14px;
  padding: 12px 18px;
  border: 1px solid rgba(160, 210, 170, 0.6);
  box-shadow: 0 4px 14px rgba(40, 100, 60, 0.06);
}

.stat-pill .lab {
  display: block;
  font-size: 0.75rem;
  color: #5a6b5f;
}

.stat-pill .val {
  font-size: 1.35rem;
  font-weight: 800;
  color: #1b5e20;
}

.stat-pill.trees .val {
  color: #2e7d32;
}

.link-soft {
  font-size: 0.88rem;
  color: #2e7d32;
  text-decoration: none;
  margin-left: 0.5rem;
}

.link-soft:hover {
  text-decoration: underline;
}

.hero-forest {
  text-align: center;
}

.forest-ring {
  min-height: 140px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: flex-end;
  gap: 10px 14px;
  padding: 16px;
}

.tree {
  font-size: 2em;
  animation: popIn 0.7s ease-out both;
  animation-delay: var(--d);
  filter: drop-shadow(0 4px 6px rgba(0, 80, 40, 0.15));
}

@keyframes popIn {
  from {
    transform: translateY(12px) scale(0.6);
    opacity: 0;
  }
  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}

.forest-hint {
  font-size: 0.82rem;
  color: #5a6b5f;
  margin: 0;
}

.card-block {
  background: #fff;
  border-radius: 16px;
  padding: 22px 24px;
  margin-bottom: 22px;
  box-shadow: 0 4px 22px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(220, 235, 224, 0.9);
}

.card-block h2 {
  font-size: 1.15rem;
  font-weight: 700;
  color: #1b3d24;
  margin: 0 0 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ico {
  font-size: 1.2em;
}

.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 18px;
}

.shop-card {
  border: 1px solid rgba(200, 225, 205, 0.95);
  border-radius: 14px;
  padding: 16px;
  background: linear-gradient(180deg, #fbfffc 0%, #f6faf7 100%);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.big-ico {
  font-size: 2rem;
}

.cat {
  font-size: 0.72rem;
  padding: 4px 8px;
  border-radius: 999px;
  background: #e8f5e9;
  color: #2e7d32;
}

.shop-card h3 {
  margin: 0;
  font-size: 1.05rem;
  color: #1b5e20;
}

.sub {
  margin: 0;
  font-size: 0.86rem;
  color: #5a6b5f;
  line-height: 1.45;
  flex: 1;
}

.cost-row {
  margin-top: 4px;
}

.cost {
  font-size: 1.35rem;
  font-weight: 800;
  color: #c67c00;
}

.unit {
  font-size: 0.78rem;
  color: #8d6e63;
  margin-left: 4px;
}

.btn-redeem {
  margin-top: 8px;
  padding: 10px 14px;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(135deg, #43a047, #2e7d32);
  color: #fff;
  box-shadow: 0 6px 16px rgba(46, 125, 50, 0.25);
}

.btn-redeem:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.reward-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.reward-list li {
  padding: 10px 0;
  border-bottom: 1px dashed #e0ebe0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.coupon-code {
  font-family: ui-monospace, monospace;
  font-size: 0.85rem;
  color: #1b5e20;
  word-break: break-all;
}

.muted {
  color: #7a8b8a;
  font-size: 0.82rem;
}

.empty {
  color: #8a9a96;
  margin: 0;
}

.muted-block {
  background: #fafcf9;
}

.mini-log {
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 0.9rem;
}

.mini-log li {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #eef4ef;
}

.toast {
  position: fixed;
  bottom: 32px;
  left: 50%;
  transform: translateX(-50%);
  background: #1b5e20;
  color: #fff;
  padding: 12px 22px;
  border-radius: 999px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.18);
  z-index: 2000;
}

.pop-enter-active,
.pop-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(8px);
}

@media (max-width: 900px) {
  .gam-hero {
    grid-template-columns: 1fr;
  }
  .two-col {
    grid-template-columns: 1fr;
  }
  .mini-log li {
    grid-template-columns: 1fr;
  }
}
</style>
