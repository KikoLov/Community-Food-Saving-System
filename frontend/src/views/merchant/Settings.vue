<template>
  <div class="settings-page">
    <h4 class="mb-4"><i class="fas fa-cog me-2"></i>店铺设置</h4>

    <div class="card">
      <div class="card-body">
        <form @submit.prevent="handleSave">
          <div class="mb-3">
            <label class="form-label">商户名称</label>
            <input type="text" class="form-control" v-model="merchantForm.merchantName">
          </div>

          <div class="mb-3">
            <label class="form-label">联系电话</label>
            <input type="text" class="form-control" v-model="merchantForm.contactPhone">
          </div>

          <div class="mb-3">
            <label class="form-label">详细地址</label>
            <input type="text" class="form-control" v-model="merchantForm.address">
          </div>

          <div class="mb-3">
            <label class="form-label">营业时间</label>
            <input type="text" class="form-control" v-model="merchantForm.openingHours" placeholder="如: 08:00-22:00">
          </div>

          <div class="mb-3">
            <label class="form-label">店铺描述</label>
            <textarea class="form-control" v-model="merchantForm.description" rows="3"></textarea>
          </div>

          <div class="mb-3">
            <label class="form-label">资质状态</label>
            <div>
              <span :class="['badge', getLicenseStatusClass(merchantForm.licenseStatus)]">
                {{ getLicenseStatusText(merchantForm.licenseStatus) }}
              </span>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label">营业执照</label>
            <div>
              <label class="btn btn-outline-primary" v-if="!merchantForm.businessLicense">
                <i class="fas fa-upload me-1"></i> 上传营业执照
                <input type="file" class="d-none" accept="image/*" @change="handleUpload">
              </label>
              <img v-else :src="merchantForm.businessLicense" class="license-preview" @click="removeLicense">
            </div>
          </div>

          <div>
            <button type="submit" class="btn btn-primary me-2" :disabled="saving">
              <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
              保存
            </button>
            <button v-if="merchantForm.licenseStatus === 2" type="button" class="btn btn-success" @click="handleSubmitAudit">
              提交审核
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMerchantProfile, updateMerchantProfile, uploadLicense, submitLicenseAudit } from '@/api/merchant'
import { useUserStore } from '@/store/user'
import { Message } from '@/utils/message'

const userStore = useUserStore()

const saving = ref(false)
const merchantForm = reactive({
  merchantId: null,
  merchantName: '',
  contactPhone: '',
  address: '',
  openingHours: '',
  description: '',
  licenseStatus: 0,
  businessLicense: ''
})

onMounted(async () => {
  try {
    const res = await getMerchantProfile()
    if (res.data) {
      Object.assign(merchantForm, res.data)
    }
  } catch (error) {
    console.error(error)
  }
})

const getLicenseStatusClass = (status) => {
  const classes = { 0: 'bg-warning', 1: 'bg-success', 2: 'bg-danger' }
  return classes[status] || 'bg-secondary'
}

const getLicenseStatusText = (status) => {
  const texts = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return texts[status] || '未知'
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateMerchantProfile(merchantForm)
    Message.success('保存成功')
  } catch (error) {
    console.error(error)
  } finally {
    saving.value = false
  }
}

const handleUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    const formData = new FormData()
    formData.append('file', file)
    await uploadLicense(file)
    Message.success('上传成功')
    merchantForm.businessLicense = URL.createObjectURL(file)
  } catch (error) {
    console.error(error)
  }
  event.target.value = ''
}

const removeLicense = () => {
  merchantForm.businessLicense = ''
}

const handleSubmitAudit = async () => {
  try {
    await submitLicenseAudit()
    Message.success('提交成功，请等待审核')
    merchantForm.licenseStatus = 0
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.settings-page {
  padding: 20px 0;
}

.license-preview {
  width: 200px;
  height: auto;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 10px;
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
