<template>
  <SBAdminLayout brandName="超级管理后台">
    <template #menu>
      <div class="sb-sidenav-menu-heading">数据</div>
      <a class="nav-link" :class="{ active: isActive('/admin/dashboard') }" href="#" @click.prevent="navigateTo('/admin/dashboard')">
        <div class="sb-nav-link-icon"><i class="fas fa-chart-area"></i></div>
        数据大屏
      </a>
      <div class="sb-sidenav-menu-heading">系统管理</div>
      <a class="nav-link" :class="{ active: isActive('/admin/merchants') }" href="#" @click.prevent="navigateTo('/admin/merchants')">
        <div class="sb-nav-link-icon"><i class="fas fa-store"></i></div>
        商户管理
      </a>
      <a class="nav-link" :class="{ active: isActive('/admin/communities') }" href="#" @click.prevent="navigateTo('/admin/communities')">
        <div class="sb-nav-link-icon"><i class="fas fa-building"></i></div>
        社区管理
      </a>
      <a class="nav-link" :class="{ active: isActive('/admin/categories') }" href="#" @click.prevent="navigateTo('/admin/categories')">
        <div class="sb-nav-link-icon"><i class="fas fa-th"></i></div>
        商品分类
      </a>
      <a class="nav-link" :class="{ active: isActive('/admin/orders') }" href="#" @click.prevent="navigateTo('/admin/orders')">
        <div class="sb-nav-link-icon"><i class="fas fa-list"></i></div>
        订单管理
      </a>
      <a class="nav-link" :class="{ active: isActive('/admin/operation-logs') }" href="#" @click.prevent="navigateTo('/admin/operation-logs')">
        <div class="sb-nav-link-icon"><i class="fas fa-history"></i></div>
        操作审计
      </a>
    </template>

    <template #user-menu>
      <li>
        <a class="dropdown-item" href="#">
          <i class="fas fa-user fa-sm fa-fw me-2 text-gray-400"></i>
          管理员
        </a>
      </li>
      <li>
        <a class="dropdown-item" href="#">
          <i class="fas fa-cogs fa-sm fa-fw me-2 text-gray-400"></i>
          系统设置
        </a>
      </li>
    </template>

    <slot></slot>
  </SBAdminLayout>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SBAdminLayout from './SBAdminLayout.vue'

const route = useRoute()
const router = useRouter()

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/')
}

const navigateTo = (path) => {
  router.push(path)
}
</script>
