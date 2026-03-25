<template>
  <div>
    <div class="card">
      <div class="card-header">
        <div class="d-flex justify-between align-center">
          <h2>🏢 社区管理</h2>
          <button class="btn btn-primary" @click="showAddModal = true">
            + 添加社区
          </button>
        </div>
      </div>
      <div class="card-body">
        <div v-if="loading" class="text-center" style="padding: 40px;">
          <div class="spinner"></div>
          <p>加载社区列表...</p>
        </div>
        <div v-else>
          <table class="table" style="width: 100%; border-collapse: collapse;">
            <thead>
              <tr style="background: #f8f9fa; border-bottom: 2px solid #dee2e6;">
                <th style="padding: 12px; text-align: left;">社区名称</th>
                <th style="padding: 12px; text-align: left;">社区编码</th>
                <th style="padding: 12px; text-align: left;">位置</th>
                <th style="padding: 12px; text-align: left;">详细地址</th>
                <th style="padding: 12px; text-align: left;">状态</th>
                <th style="padding: 12px; text-align: left;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="community in communities" :key="community.communityId" style="border-bottom: 1px solid #dee2e6;">
                <td style="padding: 12px; font-weight: 500;">{{ community.communityName }}</td>
                <td style="padding: 12px;"><code>{{ community.communityCode }}</code></td>
                <td style="padding: 12px;">
                  <span class="badge-info">{{ community.province }}</span>
                  <span class="badge-info">{{ community.city }}</span>
                  <span class="badge-info">{{ community.district }}</span>
                </td>
                <td style="padding: 12px;">{{ community.address }}</td>
                <td style="padding: 12px;">
                  <span class="badge" :class="community.status === 1 ? 'badge-success' : 'badge-secondary'">
                    {{ community.status === 1 ? '✓ 启用' : '✗ 禁用' }}
                  </span>
                </td>
                <td style="padding: 12px;">
                  <button class="btn btn-secondary btn-sm" style="margin-right: 8px;" @click="editCommunity(community)">
                    ✏ 编辑
                  </button>
                  <button class="btn btn-danger btn-sm" @click="deleteCommunity(community.communityId)">
                    🗑 删除
                  </button>
                </td>
              </tr>
              <tr v-if="communities.length === 0">
                <td colspan="6" style="padding: 40px; text-align: center;">
                  <div style="color: #6c757d;">
                    <div style="font-size: 3em; margin-bottom: 10px;">🏢</div>
                    <p>暂无社区</p>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Add/Edit Modal -->
    <div v-if="showAddModal || showEditModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ showEditModal ? '编辑社区' : '添加社区' }}</h3>
          <button class="modal-close" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>社区名称</label>
            <input type="text" v-model="form.communityName" class="form-control" placeholder="请输入社区名称">
          </div>
          <div class="form-group">
            <label>社区编码</label>
            <input type="text" v-model="form.communityCode" class="form-control" placeholder="请输入社区编码">
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>省份</label>
              <input type="text" v-model="form.province" class="form-control" placeholder="如：北京市">
            </div>
            <div class="form-group">
              <label>城市</label>
              <input type="text" v-model="form.city" class="form-control" placeholder="如：北京市">
            </div>
            <div class="form-group">
              <label>区县</label>
              <input type="text" v-model="form.district" class="form-control" placeholder="如：朝阳区">
            </div>
          </div>
          <div class="form-group">
            <label>详细地址</label>
            <input type="text" v-model="form.address" class="form-control" placeholder="请输入详细地址">
          </div>
          <div class="form-group">
            <label>状态</label>
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.status" :true-value="1" :false-value="0">
              <span>启用</span>
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="saveCommunity">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCommunitiesAdmin, addCommunity, updateCommunity, deleteCommunity as deleteCommunityApi } from '@/api/admin'
import { Message } from '@/utils/message'

const communities = ref([])
const loading = ref(false)
const showAddModal = ref(false)
const showEditModal = ref(false)

const form = reactive({
  communityId: null,
  communityName: '',
  communityCode: '',
  province: '',
  city: '',
  district: '',
  address: '',
  status: 1
})

onMounted(() => {
  loadCommunities()
})

const loadCommunities = async () => {
  loading.value = true
  try {
    const res = await getCommunitiesAdmin()
    communities.value = res.data || []
  } catch (error) {
    Message.error('加载社区列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const editCommunity = (community) => {
  Object.assign(form, community)
  showEditModal.value = true
}

const deleteCommunity = async (id) => {
  if (confirm('确定要删除该社区吗？')) {
    try {
      await deleteCommunityApi(id)
      Message.success('删除成功')
      await loadCommunities()
    } catch (error) {
      Message.error('删除失败')
      console.error(error)
    }
  }
}

const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  Object.assign(form, {
    communityId: null,
    communityName: '',
    communityCode: '',
    province: '',
    city: '',
    district: '',
    address: '',
    status: 1
  })
}

const saveCommunity = async () => {
  try {
    if (showEditModal.value) {
      await updateCommunity(form.communityId, form)
      Message.success('更新成功')
    } else {
      await addCommunity(form)
      Message.success('添加成功')
    }
    closeModal()
    await loadCommunities()
  } catch (error) {
    Message.error('保存失败')
    console.error(error)
  }
}
</script>

<style scoped>
.table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.table thead th {
  font-weight: 600;
  color: #495057;
}

.table tbody tr:hover {
  background: #f8f9fa;
}

.badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.85em;
  margin-right: 4px;
}

.badge-info {
  background: #17a2b8;
  color: white;
}

.badge-success {
  background: #28a745;
  color: white;
}

.badge-secondary {
  background: #6c757d;
  color: white;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 2em;
  cursor: pointer;
  line-height: 1;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #dee2e6;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 1em;
}

.form-control:focus {
  outline: none;
  border-color: #80bdff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
}
</style>
