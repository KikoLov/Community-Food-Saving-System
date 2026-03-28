import request from '@/utils/request'

// Get all merchants
export function getMerchants() {
  return request({
    url: '/admin/merchants',
    method: 'get'
  })
}

// Merchant stats (today sales, orders, etc.)
export function getMerchantStats(merchantId) {
  return request({
    url: `/admin/merchants/${merchantId}/stats`,
    method: 'get'
  })
}

// Delete merchant. force=true 时级联删除该商户下订单与商品（merchant1/merchant2 下仅名称正常的商户受保护）
export function deleteMerchant(merchantId, force = false) {
  return request({
    url: `/admin/merchants/${merchantId}`,
    method: 'delete',
    params: force ? { force: true } : {}
  })
}

// Audit merchant
export function auditMerchant(merchantId, status) {
  return request({
    url: `/admin/merchant/${merchantId}/audit`,
    method: 'put',
    params: { status }
  })
}

// Get communities
export function getCommunitiesAdmin() {
  return request({
    url: '/admin/communities',
    method: 'get'
  })
}

// Add community
export function addCommunity(data) {
  return request({
    url: '/admin/communities',
    method: 'post',
    data
  })
}

// Update community
export function updateCommunity(communityId, data) {
  return request({
    url: `/admin/communities/${communityId}`,
    method: 'put',
    data
  })
}

// Delete community
export function deleteCommunity(communityId) {
  return request({
    url: `/admin/communities/${communityId}`,
    method: 'delete'
  })
}

// Get categories
export function getCategoriesAdmin() {
  return request({
    url: '/admin/categories',
    method: 'get'
  })
}

// Add category
export function addCategory(data) {
  return request({
    url: '/admin/categories',
    method: 'post',
    data
  })
}

// Update category
export function updateCategory(categoryId, data) {
  return request({
    url: `/admin/categories/${categoryId}`,
    method: 'put',
    data
  })
}

// Delete category
export function deleteCategory(categoryId) {
  return request({
    url: `/admin/categories/${categoryId}`,
    method: 'delete'
  })
}

// Get dashboard stats
export function getDashboardStats() {
  return request({
    url: '/admin/dashboard/stats',
    method: 'get'
  })
}

// Get chart data
export function getChartData() {
  return request({
    url: '/admin/dashboard/chart',
    method: 'get'
  })
}

// Get all orders
export function getAllOrders() {
  return request({
    url: '/admin/orders',
    method: 'get'
  })
}

// Get operation logs
export function getOperationLogs() {
  return request({
    url: '/admin/operation-logs',
    method: 'get'
  })
}
