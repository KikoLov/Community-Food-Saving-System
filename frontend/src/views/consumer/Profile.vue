<template>
  <div class="profile-page">
    <h4 class="mb-4"><i class="fas fa-user me-2"></i>个人中心</h4>

    <div class="card">
      <div class="card-body">
        <form @submit.prevent="handleSave">
          <div class="mb-3">
            <label class="form-label">用户名</label>
            <input type="text" class="form-control" v-model="profileForm.userName" disabled>
          </div>

          <div class="mb-3">
            <label class="form-label">昵称</label>
            <input type="text" class="form-control" v-model="profileForm.nickName">
          </div>

          <div class="mb-3">
            <label class="form-label">手机号</label>
            <input type="text" class="form-control" v-model="profileForm.phonenumber">
          </div>

          <div class="mb-3">
            <label class="form-label">邮箱</label>
            <input type="email" class="form-control" v-model="profileForm.email">
          </div>

          <div class="mb-3">
            <label class="form-label">所属社区</label>
            <select class="form-select" v-model="profileForm.communityId">
              <option :value="null">请选择社区</option>
              <option v-for="item in communities" :key="item.communityId" :value="item.communityId">
                {{ item.communityName }}
              </option>
            </select>
          </div>

          <button type="submit" class="btn btn-primary">
            <i class="fas fa-save me-2"></i>保存
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getCommunities, bindCommunity } from '@/api/consumer'
import { getUserInfo } from '@/api/auth'
import { Message } from '@/utils/message'
import { fixCommunityRecord } from '@/utils/textFixer'

const userStore = useUserStore()

const communities = ref([])
const profileForm = reactive({
  userName: '',
  nickName: '',
  phonenumber: '',
  email: '',
  communityId: null
})

onMounted(async () => {
  try {
    const communityRes = await getCommunities()
    communities.value = (communityRes.data || []).map(fixCommunityRecord)

    const userRes = await getUserInfo()
    const userInfo = userRes.data
    Object.assign(profileForm, {
      userName: userInfo.userName,
      nickName: userInfo.nickName,
      phonenumber: userInfo.phonenumber,
      email: userInfo.email
    })
  } catch (error) {
    console.error(error)
  }
})

const handleSave = async () => {
  try {
    await bindCommunity(profileForm.communityId)
    Message.success('保存成功')
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.profile-page {
  padding: 20px 0;
}

.btn-primary {
  background-color: #4e57d9;
  border-color: #4e57d9;
}

.btn-primary:hover {
  background-color: #3b47b8;
  border-color: #3b47b8;
}
</style>
