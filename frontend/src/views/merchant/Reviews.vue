<template>
  <div class="reviews-page">
    <h4 class="mb-4"><i class="fas fa-star me-2"></i>评价管理</h4>

    <div class="card mb-3">
      <div class="card-body d-flex justify-between align-center">
        <div>
          <div class="text-muted">平均评分</div>
          <div class="score">{{ avgRating }}</div>
        </div>
        <div>
          <div class="text-muted">好评率（4-5星）</div>
          <div class="score good">{{ goodRate }}%</div>
        </div>
        <div>
          <div class="text-muted">评价总数</div>
          <div class="score">{{ reviews.length }}</div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">顾客评价列表</div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover mb-0">
            <thead class="table-light">
              <tr>
                <th>商品</th>
                <th>评分</th>
                <th>评价内容</th>
                <th>图片</th>
                <th>顾客</th>
                <th>时间</th>
                <th>回复</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in reviews" :key="r.reviewId">
                <td>{{ r.productName }}</td>
                <td>{{ '★'.repeat(Number(r.rating || 0)) }}</td>
                <td>{{ r.content || '（无文字评价）' }}</td>
                <td>
                  <img v-if="r.imageUrl" :src="r.imageUrl" class="thumb" alt="评价图">
                  <span v-else class="text-muted">-</span>
                </td>
                <td>{{ r.userName || `用户#${r.userId}` }}</td>
                <td>{{ formatDateTime(r.createTime) }}</td>
                <td>
                  <div v-if="r.replyContent" class="reply-box">{{ r.replyContent }}</div>
                  <button v-else class="btn btn-sm btn-outline-primary" @click="reply(r)">回复</button>
                </td>
              </tr>
              <tr v-if="reviews.length === 0">
                <td colspan="7" class="text-center py-4 text-muted">暂无评价</td>
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
import { getMerchantReviews, replyMerchantReview } from '@/api/merchant'
import { Message } from '@/utils/message'

const reviews = ref([])

const avgRating = computed(() => {
  if (!reviews.value.length) return '0.0'
  const total = reviews.value.reduce((s, r) => s + Number(r.rating || 0), 0)
  return (total / reviews.value.length).toFixed(2)
})

const goodRate = computed(() => {
  if (!reviews.value.length) return '0.00'
  const good = reviews.value.filter(r => Number(r.rating || 0) >= 4).length
  return (good * 100 / reviews.value.length).toFixed(2)
})

const load = async () => {
  try {
    const res = await getMerchantReviews()
    reviews.value = res.data || []
  } catch (e) {
    Message.error('加载评价失败')
  }
}

onMounted(load)

const reply = async (row) => {
  const text = window.prompt('请输入回复内容：')
  if (!text) return
  try {
    await replyMerchantReview(row.reviewId, text)
    Message.success('回复成功')
    await load()
  } catch (e) {
    Message.error('回复失败')
  }
}

const formatDateTime = (val) => {
  if (!val) return '-'
  return new Date(val).toLocaleString('zh-CN')
}
</script>

<style scoped>
.reviews-page { padding: 20px 0; }
.score { font-size: 1.6rem; font-weight: 700; color: #2e7d32; }
.score.good { color: #f57c00; }
.thumb { width: 56px; height: 56px; border-radius: 8px; object-fit: cover; border: 1px solid #e5e7eb; }
.reply-box {
  max-width: 220px;
  white-space: pre-wrap;
  background: #f6fbf7;
  border: 1px solid #dcedc8;
  border-radius: 8px;
  padding: 6px 8px;
}
</style>

