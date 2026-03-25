<template>
  <div class="p-4">
    <h1>测试页面</h1>
    <p>如果你能看到这个页面，说明路由和组件都正常工作。</p>
    <div class="alert alert-info">
      用户信息: {{ userInfo }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const userInfo = computed(() => {
  return userStore.userInfo ? JSON.stringify(userStore.userInfo) : '未登录'
})
</script>
