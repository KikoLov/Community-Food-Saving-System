import request from '@/utils/request'

// Login
export function login(username, password) {
  return request({
    url: '/auth/login',
    method: 'post',
    data: { username, password }
  })
}

// Register
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// Get user info
export function getUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

// Logout
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
