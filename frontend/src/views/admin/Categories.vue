<template>
  <div>
    <!-- Breadcrumb -->
    <ol class="breadcrumb mb-4">
      <li class="breadcrumb-item"><a href="#">首页</a></li>
      <li class="breadcrumb-item active">商品分类</li>
    </ol>

    <div class="card mb-4">
      <div class="card-header d-flex justify-content-between align-items-center">
        <div><i class="fas fa-th me-2"></i>分类列表</div>
        <button class="btn btn-primary btn-sm" @click="handleAdd">
          <i class="fas fa-plus me-1"></i> 添加分类
        </button>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-bordered table-hover mb-0">
            <thead>
              <tr>
                <th>分类名称</th>
                <th>分类编码</th>
                <th>碳排放因子</th>
                <th>排序</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in categories" :key="row.categoryId">
                <td><strong>{{ row.categoryName }}</strong></td>
                <td><code>{{ row.categoryCode }}</code></td>
                <td>{{ row.carbonFactor }} kg CO₂/kg</td>
                <td>{{ row.sortOrder }}</td>
                <td>
                  <span :class="['badge', row.status === 1 ? 'bg-success' : 'bg-secondary']">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-outline-primary btn-sm me-1" @click="handleEdit(row)">
                    <i class="fas fa-edit"></i>
                  </button>
                  <button class="btn btn-outline-danger btn-sm" @click="handleDelete(row.categoryId)">
                    <i class="fas fa-trash"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="categories.length === 0">
                <td colspan="6" class="text-center py-4">
                  <div class="text-muted">
                    <i class="fas fa-th fa-3x mb-3 d-block"></i>
                    暂无分类
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Category Modal -->
    <div class="modal fade" id="categoryModal" tabindex="-1" aria-hidden="true" ref="modalRef">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ dialogTitle }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="handleSubmit">
              <div class="mb-3">
                <label class="form-label">分类名称</label>
                <input type="text" class="form-control" v-model="categoryForm.categoryName" required>
              </div>
              <div class="mb-3">
                <label class="form-label">分类编码</label>
                <input type="text" class="form-control" v-model="categoryForm.categoryCode" required>
              </div>
              <div class="mb-3">
                <label class="form-label">碳排放因子 (kg CO₂/kg)</label>
                <input type="number" class="form-control" v-model="categoryForm.carbonFactor" min="0" step="0.01" required>
              </div>
              <div class="mb-3">
                <label class="form-label">排序</label>
                <input type="number" class="form-control" v-model="categoryForm.sortOrder" min="0" required>
              </div>
              <div class="mb-3">
                <label class="form-label">状态</label>
                <div class="form-check form-switch">
                  <input class="form-check-input" type="checkbox" v-model="categoryForm.statusSwitch" @change="categoryForm.status = categoryForm.statusSwitch ? 1 : 0">
                  <label class="form-check-label">{{ categoryForm.statusSwitch ? '启用' : '禁用' }}</label>
                </div>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" @click="handleSubmit">
              <i class="fas fa-save me-1"></i>确定
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCategoriesAdmin, addCategory, updateCategory, deleteCategory } from '@/api/admin'
import { Message } from '@/utils/message'

const categories = ref([])
let categoryModal = null
const dialogTitle = ref('添加分类')
const isEdit = ref(false)

const categoryForm = reactive({
  categoryId: null,
  categoryName: '',
  categoryCode: '',
  carbonFactor: 1.5,
  sortOrder: 0,
  status: 1,
  statusSwitch: true
})

onMounted(async () => {
  await loadCategories()
})

const loadCategories = async () => {
  try {
    const res = await getCategoriesAdmin()
    categories.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加分类'
  isEdit.value = false
  Object.assign(categoryForm, { categoryId: null, categoryName: '', categoryCode: '', carbonFactor: 1.5, sortOrder: 0, status: 1, statusSwitch: true })
  openModal()
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑分类'
  isEdit.value = true
  Object.assign(categoryForm, row, { statusSwitch: row.status === 1 })
  openModal()
}

const openModal = () => {
  if (!categoryModal) {
    categoryModal = new bootstrap.Modal(document.getElementById('categoryModal'))
  }
  categoryModal.show()
}

const handleDelete = async (id) => {
  if (confirm('确定要删除该分类吗?')) {
    try {
      await deleteCategory(id)
      Message.success('删除成功')
      await loadCategories()
    } catch (error) {
      console.error(error)
    }
  }
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await updateCategory(categoryForm.categoryId, categoryForm)
      Message.success('更新成功')
    } else {
      await addCategory(categoryForm)
      Message.success('添加成功')
    }
    if (categoryModal) {
      categoryModal.hide()
    }
    await loadCategories()
  } catch (error) {
    console.error(error)
  }
}
</script>
