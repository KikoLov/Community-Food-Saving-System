import request from '@/utils/request'

/**
 * Spring MVC 绑定 @RequestParam List<Long> productIds 需要 productIds=1&productIds=2；
 * Axios 默认会序列化成 productIds[]=1，导致绑定失败并触发后端 500「系统繁忙」。
 */
function serializeSpringListParams(params) {
  const usp = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null) return
    if (Array.isArray(value)) {
      value.forEach((item) => {
        if (item !== undefined && item !== null) {
          usp.append(key, String(item))
        }
      })
    } else {
      usp.append(key, String(value))
    }
  })
  return usp.toString()
}

// Get merchant profile
export function getMerchantProfile() {
  return request({
    url: '/merchant/profile',
    method: 'get'
  })
}

// Update merchant profile
export function updateMerchantProfile(data) {
  return request({
    url: '/merchant/profile',
    method: 'put',
    data
  })
}

// Upload license
export function uploadLicense(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/merchant/license/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// Upload product image
export function uploadProductImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/merchant/products/upload-image',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// Delete uploaded product image
export function deleteProductImage(imageUrl) {
  return request({
    url: '/merchant/products/delete-image',
    method: 'delete',
    params: { imageUrl }
  })
}

// Submit license audit
export function submitLicenseAudit() {
  return request({
    url: '/merchant/license/submit',
    method: 'post'
  })
}

// Add product
export function addProduct(data) {
  return request({
    url: '/merchant/products',
    method: 'post',
    data
  })
}

// Update product
export function updateProduct(productId, data) {
  return request({
    url: `/merchant/products/${productId}`,
    method: 'put',
    data
  })
}

// Delete product
export function deleteProduct(productId) {
  return request({
    url: `/merchant/products/${productId}`,
    method: 'delete'
  })
}

// Batch update product status
export function batchUpdateProductStatus(productIds, status) {
  return request({
    url: '/merchant/products/batch-status',
    method: 'put',
    params: { productIds, status },
    paramsSerializer: serializeSpringListParams
  })
}

// Batch delete products
export function batchDeleteProducts(productIds) {
  return request({
    url: '/merchant/products/batch',
    method: 'delete',
    params: { productIds },
    paramsSerializer: serializeSpringListParams
  })
}

// Get merchant products
export function getMerchantProducts(pageNum = 1, pageSize = 10) {
  return request({
    url: '/merchant/products',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

// Get warning products
export function getWarningProducts() {
  return request({
    url: '/merchant/products/warning',
    method: 'get'
  })
}

// Import products from Excel
export function importProducts(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/merchant/products/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// Get merchant orders
export function getMerchantOrders() {
  return request({
    url: '/merchant/orders',
    method: 'get'
  })
}

// 同意 / 拒绝顾客退款申请
export function approveMerchantRefund(orderId) {
  return request({
    url: `/merchant/order/${orderId}/refund/approve`,
    method: 'post'
  })
}

export function rejectMerchantRefund(orderId, reason) {
  return request({
    url: `/merchant/order/${orderId}/refund/reject`,
    method: 'post',
    data: { reason }
  })
}

// Verify order
export function verifyOrder(verifyCode) {
  return request({
    url: '/merchant/order/verify',
    method: 'post',
    data: { verifyCode }
  })
}

// Preview order before verify
export function previewVerifyOrder(verifyCode) {
  return request({
    url: '/merchant/order/preview',
    method: 'post',
    data: { verifyCode }
  })
}

// Get merchant stats
export function getMerchantStats() {
  return request({
    url: '/merchant/stats',
    method: 'get'
  })
}

// Get categories (for product form)
export function getCategories() {
  return request({
    url: '/merchant/categories',
    method: 'get'
  })
}

// Get merchant reviews
export function getMerchantReviews() {
  return request({
    url: '/merchant/reviews',
    method: 'get'
  })
}

// Reply review
export function replyMerchantReview(reviewId, replyContent) {
  return request({
    url: `/merchant/reviews/${reviewId}/reply`,
    method: 'post',
    data: { replyContent }
  })
}
