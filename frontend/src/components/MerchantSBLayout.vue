<template>
  <SBAdminLayout brandName="商户管理后台">
    <template #menu>
      <div class="sb-sidenav-menu-heading">概览</div>
      <a class="nav-link" :class="{ active: isActive('/merchant/dashboard') }" href="#" @click.prevent="navigateTo('/merchant/dashboard')">
        <div class="sb-nav-link-icon"><i class="fas fa-tachometer-alt"></i></div>
        控制台
      </a>
      <div class="sb-sidenav-menu-heading">业务管理</div>
      <a class="nav-link" :class="{ active: isActive('/merchant/products') }" href="#" @click.prevent="navigateTo('/merchant/products')">
        <div class="sb-nav-link-icon"><i class="fas fa-box"></i></div>
        商品管理
      </a>
      <a class="nav-link" :class="{ active: isActive('/merchant/orders') }" href="#" @click.prevent="navigateTo('/merchant/orders')">
        <div class="sb-nav-link-icon"><i class="fas fa-list"></i></div>
        订单管理
      </a>
      <a class="nav-link" :class="{ active: isActive('/merchant/verify') }" href="#" @click.prevent="navigateTo('/merchant/verify')">
        <div class="sb-nav-link-icon"><i class="fas fa-check-circle"></i></div>
        核销工作台
      </a>
      <div class="sb-sidenav-menu-heading">设置</div>
      <a class="nav-link" :class="{ active: isActive('/merchant/settings') }" href="#" @click.prevent="navigateTo('/merchant/settings')">
        <div class="sb-nav-link-icon"><i class="fas fa-cog"></i></div>
        店铺设置
      </a>
    </template>

    <template #user-menu>
      <li>
        <router-link class="dropdown-item" to="/merchant/settings">
          <i class="fas fa-cog fa-sm fa-fw me-2 text-gray-400"></i>
          店铺设置
        </router-link>
      </li>
      <li>
        <router-link class="dropdown-item" to="/merchant/dashboard">
          <i class="fas fa-tachometer-alt fa-sm fa-fw me-2 text-gray-400"></i>
          控制台
        </router-link>
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
