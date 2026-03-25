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
