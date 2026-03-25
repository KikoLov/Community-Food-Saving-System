<template>
  <SBAdminLayout brandName="食品临期平台">
    <template #menu>
      <div class="sb-sidenav-menu-heading">购物</div>
      <a class="nav-link" :class="{ active: isActive('/consumer/products') }" href="#" @click.prevent="navigateTo('/consumer/products')">
        <div class="sb-nav-link-icon"><i class="fas fa-shopping-bag"></i></div>
        商品大厅
      </a>
      <a class="nav-link" :class="{ active: isActive('/consumer/cart') }" href="#" @click.prevent="navigateTo('/consumer/cart')">
        <div class="sb-nav-link-icon"><i class="fas fa-shopping-cart"></i></div>
        购物车
      </a>
      <a class="nav-link" :class="{ active: isActive('/consumer/orders') }" href="#" @click.prevent="navigateTo('/consumer/orders')">
        <div class="sb-nav-link-icon"><i class="fas fa-list"></i></div>
        我的订单
      </a>
      <div class="sb-sidenav-menu-heading">低碳生活</div>
      <a class="nav-link" :class="{ active: isActive('/consumer/carbon') }" href="#" @click.prevent="navigateTo('/consumer/carbon')">
        <div class="sb-nav-link-icon"><i class="fas fa-leaf"></i></div>
        低碳中心
      </a>
      <div class="sb-sidenav-menu-heading">账户</div>
      <a class="nav-link" :class="{ active: isActive('/consumer/profile') }" href="#" @click.prevent="navigateTo('/consumer/profile')">
        <div class="sb-nav-link-icon"><i class="fas fa-user"></i></div>
        个人中心
      </a>
    </template>

    <template #user-menu>
      <li>
        <router-link class="dropdown-item" to="/consumer/profile">
          <i class="fas fa-user fa-sm fa-fw me-2 text-gray-400"></i>
          个人中心
        </router-link>
      </li>
      <li>
        <router-link class="dropdown-item" to="/consumer/orders">
          <i class="fas fa-list fa-sm fa-fw me-2 text-gray-400"></i>
          我的订单
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
