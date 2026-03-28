import request from '@/utils/request'

// Get products by community
export function getProducts(communityId) {
  return request({
    url: '/consumer/products',
    method: 'get',
    params: { communityId }
  })
}

// Get product detail
export function getProductDetail(productId) {
  return request({
    url: `/consumer/products/${productId}`,
    method: 'get'
  })
}

// Add to cart
export function addToCart(productId, quantity = 1) {
  return request({
    url: '/consumer/cart/add',
    method: 'post',
    params: { productId, quantity }
  })
}

// Get cart list
export function getCartList() {
  return request({
    url: '/consumer/cart',
    method: 'get'
  })
}

// Update cart quantity
export function updateCart(cartId, quantity) {
  return request({
    url: `/consumer/cart/${cartId}`,
    method: 'put',
    params: { quantity }
  })
}

// Delete cart item
export function deleteCart(cartId) {
  return request({
    url: `/consumer/cart/${cartId}`,
    method: 'delete'
  })
}

// Checkout cart items；couponCode 可选，使用券时仅支持单条购物车
export function checkoutCart(cartIds = [], couponCode = null) {
  return request({
    url: '/consumer/cart/checkout',
    method: 'post',
    data: { cartIds, couponCode: couponCode || undefined }
  })
}

// 未使用的优惠券（碳积分兑换）
export function getMyCoupons() {
  return request({
    url: '/consumer/coupons',
    method: 'get'
  })
}

// Create order
export function createOrder(data, idempotencyKey = null) {
  const headers = idempotencyKey ? { 'X-Idempotency-Key': idempotencyKey } : undefined
  return request({
    url: '/consumer/order/create',
    method: 'post',
    data,
    headers
  })
}

// Get my orders
export function getMyOrders() {
  return request({
    url: '/consumer/orders',
    method: 'get'
  })
}

// Get order detail
export function getOrderDetail(orderId) {
  return request({
    url: `/consumer/order/${orderId}`,
    method: 'get'
  })
}

// Cancel order
export function cancelOrder(orderId) {
  return request({
    url: `/consumer/order/${orderId}/cancel`,
    method: 'post'
  })
}

// Get carbon center
export function getCarbonCenter() {
  return request({
    url: '/consumer/carbon',
    method: 'get'
  })
}

// 游戏化：碳积分商城目录
export function getGamificationCatalog() {
  return request({
    url: '/consumer/gamification/catalog',
    method: 'get'
  })
}

// 游戏化：当前状态（余额、森林、徽章、券）
export function getGamificationState() {
  return request({
    url: '/consumer/gamification/state',
    method: 'get'
  })
}

// 游戏化：兑换
export function redeemGamification(itemCode) {
  return request({
    url: '/consumer/gamification/redeem',
    method: 'post',
    data: { itemCode }
  })
}

// Bind community
export function bindCommunity(communityId) {
  return request({
    url: '/consumer/community/bind',
    method: 'post',
    params: { communityId }
  })
}

// Get communities
export function getCommunities() {
  return request({
    url: '/consumer/communities',
    method: 'get'
  })
}

// Upload review image
export function uploadReviewImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/consumer/reviews/upload-image',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// Create review
export function createReview(data) {
  return request({
    url: '/consumer/reviews',
    method: 'post',
    data
  })
}

// My reviews
export function getMyReviews() {
  return request({
    url: '/consumer/reviews/my',
    method: 'get'
  })
}

// Merchant rating summary
export function getMerchantRatingSummary(merchantId) {
  return request({
    url: '/consumer/reviews/merchant-summary',
    method: 'get',
    params: { merchantId }
  })
}

// Latest reviews for a merchant
export function getMerchantLatestReviews(merchantId, limit = 5) {
  return request({
    url: '/consumer/reviews/merchant-latest',
    method: 'get',
    params: { merchantId, limit }
  })
}
