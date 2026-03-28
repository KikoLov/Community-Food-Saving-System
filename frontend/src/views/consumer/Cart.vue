<template>
  <div>
    <h2 style="margin-bottom: 20px;">🛒 购物车</h2>

    <div v-if="!cartList || cartList.length === 0" class="card text-center" style="padding: 60px 20px;">
      <div style="font-size: 4em; margin-bottom: 20px;">🛒</div>
      <p style="color: #6c757d; font-size: 1.2em; margin-bottom: 20px;">购物车为空</p>
      <router-link to="/consumer/products" class="btn btn-primary">
        🛍 去逛逛
      </router-link>
    </div>

    <div v-else>
      <div class="card">
        <div style="padding: 20px;">
          <table class="cart-table" style="width: 100%; border-collapse: collapse;">
            <thead>
              <tr style="background: #f8f9fa; border-bottom: 2px solid #dee2e6;">
                <th style="padding: 15px; text-align: left;">商品信息</th>
                <th style="padding: 15px; text-align: left;">单价</th>
                <th style="padding: 15px; text-align: left;">数量</th>
                <th style="padding: 15px; text-align: left;">小计</th>
                <th style="padding: 15px; text-align: left;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in cartList" :key="item.cartId" style="border-bottom: 1px solid #dee2e6;">
                <td style="padding: 15px;">
                  <div style="display: flex; align-items: center;">
                    <div style="width: 60px; height: 60px; background: #f8f9fa; border-radius: 8px; display: flex; align-items: center; justify-content: center; margin-right: 15px; font-size: 2em;">
                      📦
                    </div>
                    <div>
                      <div style="font-weight: 500; margin-bottom: 5px;">{{ item.productName }}</div>
                      <div style="color: #6c757d; font-size: 0.9em;">
                        ⏰ 过期: {{ formatDateTime(item.expireDatetime) }}
                      </div>
                    </div>
                  </div>
                </td>
                <td style="padding: 15px;">¥{{ item.discountPrice }}</td>
                <td style="padding: 15px;">
                  <div style="display: flex; align-items: center; gap: 10px;">
                    <button class="btn btn-sm" @click="item.quantity > 1 && updateQty(item, -1)" :disabled="item.quantity <= 1">-</button>
                    <input type="number" v-model="item.quantity" readonly style="width: 60px; text-align: center; padding: 5px; border: 1px solid #ced4da; border-radius: 4px;">
                    <button class="btn btn-sm" @click="item.quantity < item.stock && updateQty(item, 1)" :disabled="item.quantity >= item.stock">+</button>
                  </div>
                </td>
                <td style="padding: 15px; font-weight: bold; color: #dc3545;">¥{{ (item.discountPrice * item.quantity).toFixed(2) }}</td>
                <td style="padding: 15px;">
                  <button class="btn btn-danger btn-sm" @click="handleDelete(item.cartId)">
                    🗑 删除
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card" style="margin-top: 20px;">
        <div style="padding: 20px;">
          <div class="coupon-row">
            <label class="coupon-label">优惠券（碳积分商城兑换）</label>
            <select v-model="selectedCouponCode" class="coupon-select">
              <option value="">不使用优惠券</option>
              <option v-for="c in unusedCoupons" :key="c.couponId" :value="c.couponCode">
                {{ couponLabel(c) }}
              </option>
            </select>
            <input
              v-model.trim="manualCouponCode"
              type="text"
              class="coupon-input"
              placeholder="或手动输入券码"
              maxlength="64"
            >
          </div>
          <p v-if="couponHint" class="coupon-hint">{{ couponHint }}</p>
          <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; margin-top: 16px;">
            <div>
              <span style="font-size: 1.1em;">已选商品 </span>
              <span style="font-weight: bold; color: var(--primary-color);">{{ totalItems }} 件</span>
              <span style="margin-left: 20px; font-size: 1.1em;">小计：</span>
              <span style="font-size: 1.2em; font-weight: bold;">¥{{ totalAmount }}</span>
              <template v-if="effectiveCouponCode && payablePreview !== null && payablePreview !== Number(totalAmount)">
                <span style="margin-left: 12px; font-size: 1em; color: #2e7d32;">优惠后约 ¥{{ payablePreview }}</span>
              </template>
            </div>
            <button class="btn btn-primary btn-lg" @click="handleCheckout" :disabled="checkingOut || !cartList || cartList.length === 0">
              💳 去结算
            </button>
          </div>
          <p v-if="effectiveCouponCode && cartList.length > 1" class="coupon-warn">使用优惠券时请只保留一件商品后再结算</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCartList, updateCart, deleteCart, checkoutCart, getMyCoupons } from '@/api/consumer'
import { Message } from '@/utils/message'
import { normalizeProductRecord } from '@/utils/demoTextNormalizer'

const cartList = ref([])
const checkingOut = ref(false)
const router = useRouter()
const unusedCoupons = ref([])
const selectedCouponCode = ref('')
const manualCouponCode = ref('')

const totalItems = computed(() => {
  return cartList.value.reduce((sum, item) => sum + item.quantity, 0)
})

const totalAmount = computed(() => {
  return cartList.value.reduce((sum, item) => sum + (item.discountPrice * item.quantity), 0).toFixed(2)
})

const effectiveCouponCode = computed(() => {
  const m = (manualCouponCode.value || '').trim()
  if (m) return m
  return (selectedCouponCode.value || '').trim() || ''
})

const payablePreview = computed(() => {
  const sub = Number(totalAmount.value)
  if (!effectiveCouponCode.value) return null
  const c = unusedCoupons.value.find((x) => x.couponCode === effectiveCouponCode.value)
  if (!c) return null
  const min = Number(c.minAmount)
  const off = Number(c.discountAmount)
  if (Number.isNaN(sub) || sub < min) return sub
  return Math.max(0, Math.round((sub - off) * 100) / 100)
})

const couponHint = computed(() => {
  if (!effectiveCouponCode.value) return ''
  const c = unusedCoupons.value.find((x) => x.couponCode === effectiveCouponCode.value)
  if (!c && manualCouponCode.value.trim()) return '手动输入的券码将在服务端校验'
  if (!c) return ''
  const sub = Number(totalAmount.value)
  if (sub < Number(c.minAmount)) return `未满足满 ${c.minAmount} 元门槛（当前小计 ¥${totalAmount.value}）`
  return `将减免 ¥${c.discountAmount}，实付约 ¥${(Math.max(0, sub - Number(c.discountAmount))).toFixed(2)}`
})

const couponLabel = (c) => {
  const tag = c.sourceItemCode === 'coupon_merchant_15' ? '商户券' : '平台券'
  return `${tag} 满${c.minAmount}减${c.discountAmount} · ${c.couponCode}`
}

onMounted(() => {
  loadCart()
  loadCoupons()
})

const loadCart = async () => {
  try {
    const res = await getCartList()
    cartList.value = (res.data || []).map(normalizeProductRecord)
  } catch (error) {
    Message.error('加载购物车失败')
    console.error(error)
  }
}

const loadCoupons = async () => {
  try {
    const res = await getMyCoupons()
    unusedCoupons.value = res.data || []
  } catch (e) {
    unusedCoupons.value = []
  }
}

const updateQty = async (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty < 1 || newQty > item.stock) return

  try {
    await updateCart(item.cartId, newQty)
    item.quantity = newQty
  } catch (error) {
    Message.error('更新数量失败')
    console.error(error)
  }
}

const handleDelete = async (cartId) => {
  if (confirm('确定要删除该商品吗？')) {
    try {
      await deleteCart(cartId)
      Message.success('删除成功')
      await loadCart()
    } catch (error) {
      Message.error('删除失败')
      console.error(error)
    }
  }
}

const handleCheckout = async () => {
  if (!cartList.value || cartList.value.length === 0) {
    Message.warning('购物车为空')
    return
  }
  const code = effectiveCouponCode.value
  if (code && cartList.value.length > 1) {
    Message.warning('使用优惠券时请先只保留一件商品再结算')
    return
  }
  if (!confirm(code ? `确认使用优惠券结算？\n券码：${code}` : '确认结算当前购物车商品吗？')) {
    return
  }
  checkingOut.value = true
  try {
    const cartIds = cartList.value.map(item => item.cartId)
    const res = await checkoutCart(cartIds, code || null)
    const count = Array.isArray(res?.data) ? res.data.length : 0
    Message.success(`结算成功，已创建 ${count} 笔订单`)
    await loadCart()
    await loadCoupons()
    await router.push('/consumer/orders')
  } catch (error) {
    Message.error(error?.response?.data?.msg || '结算失败')
    console.error(error)
  } finally {
    checkingOut.value = false
  }
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}
</script>

<style scoped>
.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.btn-sm {
  padding: 4px 12px;
  font-size: 0.9em;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-lg {
  padding: 12px 24px;
  font-size: 1.1em;
}

.coupon-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.coupon-label {
  font-weight: 600;
  color: #2e7d32;
  margin: 0;
}

.coupon-select {
  min-width: 220px;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #ced4da;
}

.coupon-input {
  flex: 1;
  min-width: 160px;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #ced4da;
}

.coupon-hint {
  font-size: 0.9em;
  color: #5a6b5f;
  margin: 0 0 4px;
}

.coupon-warn {
  margin: 10px 0 0;
  color: #c62828;
  font-size: 0.9em;
}
</style>
