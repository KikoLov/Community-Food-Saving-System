import request from '@/utils/request'

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
    params: { productIds, status }
  })
}

// Batch delete products
export function batchDeleteProducts(productIds) {
  return request({
    url: '/merchant/products/batch',
    method: 'delete',
    params: { productIds }
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
