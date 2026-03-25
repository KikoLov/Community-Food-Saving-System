<template>
  <div class="notify-page">
    <div class="page-header">
      <h4 class="mb-0"><i class="fas fa-bell me-2"></i>{{ pageTitle }}</h4>
      <div class="header-actions">
        <button class="btn btn-outline-secondary btn-sm" :disabled="notifications.length === 0" @click="markAllRead">
          <i class="fas fa-check-double me-1"></i>全部已读
        </button>
        <button class="btn btn-outline-success btn-sm" @click="loadNotifications">
          <i class="fas fa-sync-alt me-1"></i>刷新
        </button>
      </div>
    </div>

    <div class="summary-card mt-3 mb-3">
      <div class="summary-number">{{ visibleNotifications.length }}</div>
      <div class="summary-text">当前提醒数（未读）</div>
    </div>
    <div class="filter-bar mb-3">
      <select v-model="selectedLevel" class="form-select form-select-sm level-select">
        <option value="">全部级别</option>
        <option value="danger">重要</option>
        <option value="warning">提醒</option>
        <option value="success">成功</option>
        <option value="info">通知</option>
      </select>
      <label class="unread-only">
        <input type="checkbox" v-model="onlyUnread">
        仅看未读
      </label>
    </div>

    <div class="card">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover mb-0">
            <thead class="table-light">
              <tr>
                <th>时间</th>
                <th>标题</th>
                <th>内容</th>
                <th>级别</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in visibleNotifications" :key="item.id">
                <td><small>{{ formatDateTime(item.createTime) }}</small></td>
                <td class="fw-semibold">{{ item.title }}</td>
                <td>{{ item.content }}</td>
                <td>
                  <span class="badge" :class="levelClass(item.level)">
                    {{ levelText(item.level) }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-light" @click="goTarget(item.targetPath)">去处理</button>
                </td>
              </tr>
              <tr v-if="visibleNotifications.length === 0">
                <td colspan="5" class="text-center py-4 text-muted">
                  <i class="fas fa-inbox fa-2x d-block mb-2"></i>
                  暂无提醒（或已全部标记已读）
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getNotifications } from '@/api/notification'
import { Message } from '@/utils/message'

const router = useRouter()
const userStore = useUserStore()
const notifications = ref([])
const dismissedIds = ref([])
const dismissedKey = computed(() => `notifications.dismissed.userType.${userStore.userType || 0}`)
const selectedLevel = ref('')
const onlyUnread = ref(true)

const pageTitle = computed(() => {
  if (userStore.userType === 3) return '管理员提醒中心'
  if (userStore.userType === 2) return '商家提醒中心'
  return '我的提醒'
})

onMounted(async () => {
  await loadNotifications()
})

const loadNotifications = async () => {
  try {
    dismissedIds.value = JSON.parse(localStorage.getItem(dismissedKey.value) || '[]')
  } catch (e) {
    dismissedIds.value = []
  }
  const res = await getNotifications()
  notifications.value = res.data || []
}

const visibleNotifications = computed(() => {
  const hidden = new Set(dismissedIds.value)
  let list = notifications.value
  if (selectedLevel.value) {
    list = list.filter(item => item.level === selectedLevel.value)
  }
  if (onlyUnread.value) {
    list = list.filter(item => !hidden.has(item.id))
  }
  return list
})

const goTarget = (path) => {
  if (!path) return
  router.push(path)
}

const markAllRead = () => {
  if (!visibleNotifications.value.length) return
  const ids = visibleNotifications.value.map(x => x.id)
  dismissedIds.value = [...new Set([...dismissedIds.value, ...ids])]
  localStorage.setItem(dismissedKey.value, JSON.stringify(dismissedIds.value))
  Message.success('已全部标记为已读')
}

const formatDateTime = (val) => {
  if (!val) return '-'
  return new Date(val).toLocaleString('zh-CN')
}

const levelText = (level) => {
  if (level === 'success') return '成功'
  if (level === 'warning') return '提醒'
  if (level === 'danger') return '重要'
  return '通知'
}

const levelClass = (level) => {
  if (level === 'success') return 'bg-success'
  if (level === 'warning') return 'bg-warning text-dark'
  if (level === 'danger') return 'bg-danger'
  return 'bg-info'
}
</script>

<style scoped>
.notify-page {
  padding: 8px 0;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.level-select {
  width: 160px;
  border-radius: 999px;
}

.unread-only {
  color: #355d3e;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.summary-card {
  border: 1px solid #d7ead9;
  border-radius: 12px;
  background: #f7fcf8;
  padding: 14px 16px;
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.summary-number {
  min-width: 42px;
  height: 42px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  background: linear-gradient(135deg, #66bb6a, #43a047);
}

.summary-text {
  color: #2f4d35;
  font-weight: 600;
}
</style>
