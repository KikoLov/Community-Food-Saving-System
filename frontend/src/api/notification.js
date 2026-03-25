import request from '@/utils/request'

// Get notifications
export function getNotifications() {
  return request({
    url: '/notify/list',
    method: 'get'
  })
}

// Get unread notification count
export function getNotificationUnreadCount() {
  return request({
    url: '/notify/unread-count',
    method: 'get'
  })
}
